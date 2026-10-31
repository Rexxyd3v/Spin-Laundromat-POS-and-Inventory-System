import java.awt.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import security.LocalAuth;
import ui.frames.*;

public class LocalAuthUiTest {
    private static int checks;

    private static void check(boolean v, String m) {
        checks++;
        if (!v)
            throw new AssertionError(m);
    }

    private static Component named(Container p, String n) {
        for (Component c : p.getComponents()) {
            if (n.equals(c.getName()))
                return c;
            if (c instanceof Container v) {
                Component found = named(v, n);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static JButton button(Container p, String t) {
        for (Component c : p.getComponents()) {
            if (c instanceof JButton b && t.equals(b.getText()))
                return b;
            if (c instanceof Container v) {
                JButton found = button(v, t);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            boolean[] done = { false };
            SwingUtilities.invokeAndWait(() -> done[0] = condition.getAsBoolean());
            if (done[0])
                return;
            Thread.sleep(25);
        }
        throw new AssertionError("Login did not settle");
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-auth-ui-");
        System.setProperty("laundry.dataDir", root.toString());
        AuthTestSupport.loginOwner();
        LocalAuth.signOut();
        AtomicInteger masterNext = new AtomicInteger(), staffNext = new AtomicInteger();
        WelcomePanel[] master = { null }, staff = { null };
        SwingUtilities.invokeAndWait(() -> {
            master[0] = new WelcomePanel(false, masterNext::incrementAndGet);
            staff[0] = new WelcomePanel(true, staffNext::incrementAndGet);
            ((JButton) named(master[0], "loginSubmit")).doClick();
            check(masterNext.get() == 0, "Empty master key never advances");
            ((JPasswordField) named(master[0], "loginPassword")).setText("invalid master");
            ((JButton) named(master[0], "loginSubmit")).doClick();
            check(((JPasswordField) named(master[0], "loginPassword")).getPassword().length == 0,
                    "Submitted key cleared from field immediately");
        });
        waitFor(() -> ((JButton) named(master[0], "loginSubmit")).isEnabled());
        check(masterNext.get() == 0, "Wrong master key does not advance");
        SwingUtilities.invokeAndWait(() -> {
            ((JPasswordField) named(master[0], "loginPassword")).setText(AuthTestSupport.MASTER);
            ((JButton) named(master[0], "loginSubmit")).doClick();
        });
        waitFor(() -> masterNext.get() == 1);
        check(masterNext.get() == 1, "Verified master key advances once");
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) named(staff[0], "loginUsername")).setText("owner");
            ((JPasswordField) named(staff[0], "loginPassword")).setText("wrong password");
            ((JButton) named(staff[0], "loginSubmit")).doClick();
        });
        waitFor(() -> ((JButton) named(staff[0], "loginSubmit")).isEnabled());
        check(staffNext.get() == 0 && !LocalAuth.isSignedIn(), "Wrong password leaves login and session locked");
        SwingUtilities.invokeAndWait(() -> {
            ((JPasswordField) named(staff[0], "loginPassword")).setText(AuthTestSupport.PASSWORD);
            ((JButton) named(staff[0], "loginSubmit")).doClick();
        });
        waitFor(() -> staffNext.get() == 1);
        check(LocalAuth.isSignedIn(), "Correct credentials create session before advancing");
        LocalAuth.createAccount("cashier", "Sample Cashier", "Cashier", "Temp password 2026".toCharArray());
        LocalAuth.signIn("cashier", "Temp password 2026".toCharArray());
        LocalAuth.changePassword("Temp password 2026".toCharArray(), "Cashier changed password".toCharArray());
        SwingUtilities.invokeAndWait(() -> {
            WorkspacePanel w = new WorkspacePanel();
            check(w.page("Overview") == null && w.page("Reports") == null && w.page("Team") == null
                    && w.page("Operations") == null, "Cashier navigation excludes privileged pages");
            check(!button(w.page("Catalog"), "Add item").isEnabled(), "Cashier catalog editing is visibly disabled");
            check(!button(w.page("Inventory"), "Update stock").isEnabled(),
                    "Cashier stock editing is visibly disabled");
            check(button(w.page("Settings"), "Accounts & access") == null
                    && button(w.page("Settings"), "Data & backup") == null,
                    "Cashier settings show printer access only");
        });
        System.out.println("PASS: " + checks + " authenticated login and role-based UI checks.");
    }
}
