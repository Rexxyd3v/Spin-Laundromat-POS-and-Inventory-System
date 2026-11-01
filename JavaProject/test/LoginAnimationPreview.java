import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import model.*;
import security.LocalAuth;
import ui.frames.*;
import ui.panels.AnalyticsDashboard;
import util.components.MotionPreferences;
import util.config.*;

/**
 * Records the actual authenticated Master Key -> employee login -> workspace
 * flow using isolated demo data.
 */
public final class LoginAnimationPreview {
    private static final String MASTER = "Documentation master key", PASSWORD = "Documentation account password";
    private static final TransitionPane[] scene = { null };
    private static final WorkspacePanel[] workspace = { null };
    private static final int[] stage = { 0 };
    private static final List<byte[]> captures = new ArrayList<>();
    private static PortfolioFrames portfolio;

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents())
            if (child instanceof Container next)
                layout(next);
    }

    private static Component named(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container next) {
                Component found = named(next, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void demoData() throws Exception {
        System.setProperty("laundry.dataDir", Files.createTempDirectory("login-flow-demo-").toString());
        System.setProperty("laundry.legacyServices",
                AtomicProperties.dataPath("unused-services.properties").toString());
        char[] master = MASTER.toCharArray(), password = PASSWORD.toCharArray();
        try {
            LocalAuth.initialize("admin", "Sample Admin", password, master);
            LocalAuth.unlock(master);
            LocalAuth.signIn("admin", password);
            CatalogStore catalog = new CatalogStore();
            application.CatalogSampleImages.applyMissing(catalog);
            var settings = PosSettings.get();
            for (int day = 0; day < 14; day++) {
                var item = catalog.items().get(day % 4);
                var lines = List.of(new OrderPricing.Line(item.id(), item.name(), item.unit().toString(),
                        BigDecimal.valueOf(2 + day % 5), item.price()));
                var order = new OrderStore.Order(UUID.randomUUID().toString(), "Demo customer " + (day % 4 + 1), "", "",
                        "Cash", "", BigDecimal.valueOf(1000), lines,
                        OrderPricing.calculate(lines, settings.taxEnabled(), settings.taxRate()), settings);
                Path file = OrderStore.save(order);
                Properties data = AtomicProperties.read(file);
                data.setProperty("created", LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(day).atTime(10, 30)
                        .atOffset(ZoneOffset.ofHours(8)).toString());
                AtomicProperties.write(file, data);
            }
        } finally {
            Arrays.fill(master, '\0');
            Arrays.fill(password, '\0');
            LocalAuth.signOut();
        }
    }

    private static void staffLogin() {
        if (LocalAuth.isSignedIn())
            throw new IllegalStateException("Master Key alone must not sign in an account.");
        stage[0] = 1;
        scene[0].transitionTo(() -> new WelcomePanel(true, LoginAnimationPreview::mainContent));
    }

    private static void mainContent() {
        if (!LocalAuth.current().username().equals("admin"))
            throw new IllegalStateException("Demo account was not verified.");
        workspace[0] = new WorkspacePanel();
        stage[0] = 2;
        scene[0].transitionTo(() -> workspace[0]);
    }

    private static void capture() throws Exception {
        BufferedImage[] image = { null }, original = { null };
        String[] caption = { "" };
        SwingUtilities.invokeAndWait(() -> {
            for (int i = 0; i < 3; i++)
                layout(scene[0]);
            BufferedImage nativeFrame = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
            Graphics2D nativeGraphics = nativeFrame.createGraphics();
            scene[0].printAll(nativeGraphics);
            nativeGraphics.dispose();
            original[0] = nativeFrame;
            BufferedImage canvas = new BufferedImage(960, 640, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 960, 640);
            g.drawImage(nativeFrame, 0, 0, 960, 600, null);
            g.dispose();
            g = canvas.createGraphics();
            g.setColor(new Color(15, 23, 42));
            g.fillRect(0, 600, 960, 40);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            String title = switch (stage[0]) {
            case 0 -> "01 / 03   Enter Master Key";
            case 1 -> "02 / 03   Employee sign-in";
            default -> "03 / 03   Main workspace";
            };
            caption[0] = title;
            g.drawString(title, 20, 625);
            g.setColor(new Color(148, 163, 184));
            g.drawString("SPIN LAUNDROMAT  /  DEMO DATA", 686, 625);
            g.dispose();
            image[0] = canvas;
        });
        // Compress between capture and GIF encoding to keep the recording's memory use
        // small.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image[0], "png", bytes);
        captures.add(bytes.toByteArray());
        if (portfolio != null)
            portfolio.capture(original[0], caption[0]);
        Thread.sleep(80);
    }

    private static void hold(int frames) throws Exception {
        for (int i = 0; i < frames; i++)
            capture();
    }

    private static void type(String field, String value, int stride) throws Exception {
        for (int i = stride; i < value.length() + stride; i += stride) {
            String text = value.substring(0, Math.min(i, value.length()));
            SwingUtilities.invokeAndWait(() -> ((JTextField) named(scene[0], field)).setText(text));
            capture();
        }
    }

    private static void submit() throws Exception {
        SwingUtilities.invokeAndWait(() -> ((JButton) named(scene[0], "loginSubmit")).doClick());
        capture();
    }

    private static void await(int expected, boolean dashboard) throws Exception {
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            boolean[] ready = { false };
            SwingUtilities.invokeAndWait(() -> ready[0] = stage[0] == expected && !scene[0].isTransitioning()
                    && (!dashboard || !((AnalyticsDashboard) workspace[0].page("Overview")).isLoading()));
            if (ready[0])
                return;
            capture();
        }
        throw new IllegalStateException("The recorded authentication flow did not reach stage " + expected);
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        Files.createDirectories(output);
        if (args.length == 4)
            portfolio = new PortfolioFrames(Path.of(args[1]));
        else if (args.length != 1)
            throw new IllegalArgumentException(
                    "Pass output, optionally followed by frames, screenshot and video folders.");
        demoData();
        SwingUtilities.invokeAndWait(() -> {
            UIStyle.installDefaults();
            MotionPreferences.setEnabled(true);
            scene[0] = new TransitionPane(new WelcomePanel(false, LoginAnimationPreview::staffLogin));
            scene[0].setSize(1280, 800);
            layout(scene[0]);
        });
        try {
            hold(7);
            type("loginPassword", MASTER, 2);
            hold(3);
            submit();
            await(1, false);
            hold(5);
            type("loginUsername", "admin", 1);
            type("loginPassword", PASSWORD, 2);
            hold(3);
            submit();
            await(2, true);
            hold(24);
            if (portfolio != null)
                portfolio.finish(Path.of(args[2]), Path.of(args[3]));
            ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
            Path gif = output.resolve("login-animation.gif");
            try (ImageOutputStream stream = ImageIO.createImageOutputStream(gif.toFile())) {
                writer.setOutput(stream);
                writer.prepareWriteSequence(null);
                for (int i = 0; i < captures.size(); i++) {
                    BufferedImage frame = ImageIO.read(new java.io.ByteArrayInputStream(captures.get(i)));
                    DocumentationMedia.frame(writer, frame, 10, i == 0);
                }
                writer.endWriteSequence();
            } finally {
                writer.dispose();
            }
            DocumentationMedia.verifyGif(gif, captures.size());
            System.out.println("Verified " + captures.size()
                    + " frames: real Master Key verification, employee sign-in and authenticated main workspace. Saved "
                    + gif);
        } finally {
            LocalAuth.signOut();
            SwingUtilities.invokeAndWait(() -> MotionPreferences.setEnabled(false));
        }
    }
}
