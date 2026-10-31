import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import ui.frames.*;
import util.components.*;
import util.config.*;

/**
 * Verifies motion lifecycle, selected logo packaging, and input-safe
 * transitions.
 */
public class LoginMotionTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition)
            throw new AssertionError(message);
    }

    private static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layout(next);
    }

    private static BufferedImage background(AnimatedLoginBackground panel, double time) {
        BufferedImage image = new BufferedImage(600, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paintFrame(g, time);
        g.dispose();
        return image;
    }

    private static BufferedImage snapshot(JComponent view) {
        BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        view.printAll(graphics);
        graphics.dispose();
        return image;
    }

    private static boolean hasToggle(Container view) {
        for (Component child : view.getComponents()) {
            if (child instanceof JToggleButton)
                return true;
            if (child instanceof Container nested && hasToggle(nested))
                return true;
        }
        return false;
    }

    private static void verifyLoginAppearance(WelcomePanel master) {
        check(!hasToggle(master), "Login has no pause-motion toggle");
        JPanel story = (JPanel) master.getComponent(0);
        JComponent artwork = (JComponent) ((BorderLayout) story.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        BufferedImage earlier = snapshot(artwork);
        try {
            Thread.sleep(80);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
        BufferedImage later = snapshot(artwork);
        check(java.util.Arrays.equals(
                earlier.getRGB(0, 0, earlier.getWidth(), earlier.getHeight(), null, 0, earlier.getWidth()),
                later.getRGB(0, 0, later.getWidth(), later.getHeight(), null, 0, later.getWidth())),
                "Mascot and foreground stay fixed while the background clock advances");
        JButton ordinary = ModernUI.button("Checkout", null, true);
        ordinary.setSize(160, 48);
        check(snapshot(ordinary).getRGB(15, 15) == UIStyle.PRIMARY.getRGB(),
                "Workspace buttons use the configured accent");
        JButton login = (JButton) named(master, "loginSubmit");
        check(snapshot(login).getRGB(15, 15) == UIStyle.PRIMARY.getRGB(), "Sign in uses the store's primary accent");
        JPasswordField password = (JPasswordField) named(master, "loginPassword");
        JButton eye = (JButton) named(master, "showLoginPassword");
        password.setText("sample-key");
        char echo = password.getEchoChar();
        eye.doClick();
        check(password.getEchoChar() == 0 && "Hide password".equals(eye.getAccessibleContext().getAccessibleName()),
                "Password visibility control reveals the key and updates its accessible name");
        eye.doClick();
        check(password.getEchoChar() == echo
                && java.util.Arrays.equals(password.getPassword(), "sample-key".toCharArray()),
                "Password visibility control restores masking without altering the key");
    }

    private static Component named(Container container, String name) {
        for (Component child : container.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container nested) {
                Component found = named(nested, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("laundry.dataDir", Files.createTempDirectory("login-motion-test-").toString());
        UIStyle.installDefaults();
        check(BrandMark.isArtworkAvailable(), "Selected mascot is packaged with the app");
        check(BrandMark.hasTransparentArtwork(), "Logo alpha is preserved");
        BufferedImage mark = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = mark.createGraphics();
        BrandMark.draw(g, 0, 0, 96, 96);
        g.dispose();
        check((mark.getRGB(0, 0) >>> 24) == 0, "Mascot has no baked-in background");
        check((mark.getRGB(48, 48) >>> 24) > 0, "Mascot renders at icon size");
        AnimatedLoginBackground[] bg = { null };
        TransitionPane[] host = { null };
        AtomicInteger constructed = new AtomicInteger();
        JPanel first = new JPanel(), second = new JPanel();
        first.setBackground(Color.RED);
        second.setBackground(Color.BLUE);
        SwingUtilities.invokeAndWait(() -> {
            bg[0] = new AnimatedLoginBackground();
            bg[0].setSize(600, 700);
            check(!bg[0].isAnimationRunning(), "Hidden login starts no repeating animation timer");
            BufferedImage a = background(bg[0], 0), b = background(bg[0], 8);
            int changed = 0;
            for (int y = 0; y < 700; y += 3)
                for (int x = 0; x < 600; x += 3)
                    if (a.getRGB(x, y) != b.getRGB(x, y))
                        changed++;
            check(changed > 200, "Background changes across animation frames");
            bg[0].addNotify();
            MotionPreferences.setEnabled(false);
            check(!bg[0].isAnimationRunning(), "Pause stops the background timer");
            host[0] = new TransitionPane(first);
            host[0].setSize(400, 300);
            layout(host[0]);
            host[0].addNotify();
            MotionPreferences.setEnabled(true);
            check(host[0].transitionTo(() -> {
                constructed.incrementAndGet();
                return second;
            }), "Screen transition starts");
            check(host[0].isTransitioning(), "Transition is animated when motion is enabled");
            check(!host[0].transitionTo(() -> {
                constructed.incrementAndGet();
                return new JPanel();
            }), "Duplicate transition input is ignored");
            check(constructed.get() == 1, "Duplicate input constructs only one next screen");
        });
        waitFor(() -> !host[0].isTransitioning());
        SwingUtilities.invokeAndWait(() -> {
            check(host[0].currentView() == second, "Crossfade finishes on the intended screen");
            MotionPreferences.setEnabled(false);
            JPanel reduced = new JPanel();
            host[0].transitionTo(() -> reduced);
            check(!host[0].isTransitioning() && host[0].currentView() == reduced,
                    "Reduced motion switches screens without an animation");
        });
        double paused = MotionPreferences.seconds();
        Thread.sleep(35);
        check(Math.abs(MotionPreferences.seconds() - paused) < 0.001, "Paused clock does not drift");
        SwingUtilities.invokeAndWait(() -> {
            MotionPreferences.setEnabled(true);
            check(MotionPreferences.seconds() - paused < 0.1,
                    "Resuming motion does not jump ahead by the pause duration");
            host[0].transitionTo(JPanel::new);
            host[0].removeNotify();
            check(!host[0].isTransitioning(), "Leaving a transition stops its timer");
            bg[0].removeNotify();
            check(!bg[0].isAnimationRunning(), "Removing login stops the background timer");
            WelcomePanel master = new WelcomePanel(false, () -> {
            });
            master.setSize(1000, 680);
            layout(master);
            BufferedImage image = new BufferedImage(1000, 680, BufferedImage.TYPE_INT_RGB);
            master.printAll(image.getGraphics());
            check(master.getComponentCount() == 2, "Master-key screen renders both panels at minimum window size");
            verifyLoginAppearance(master);
            WelcomePanel staff = new WelcomePanel(true, () -> {
            });
            staff.setSize(1000, 680);
            layout(staff);
            staff.printAll(image.getGraphics());
            check(staff.getComponentCount() == 2, "Staff screen renders at minimum window size");
        });
        System.out.println("PASS: " + checks + " logo, background lifecycle and smooth-transition checks.");
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            boolean[] done = { false };
            SwingUtilities.invokeAndWait(() -> done[0] = condition.getAsBoolean());
            if (done[0])
                return;
            Thread.sleep(25);
        }
        throw new AssertionError("Transition did not settle");
    }
}
