import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import model.*;
import security.LocalAuth;
import security.LocalAuth.Permission;
import util.config.*;
import persistence.*;

public class LocalAuthTest {
    private static int checks;

    private interface Task {
        void run() throws Exception;
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value)
            throw new AssertionError(message);
    }

    private static String reject(Task task, String message) throws Exception {
        try {
            task.run();
            throw new AssertionError(message);
        } catch (IOException | IllegalArgumentException | SecurityException expected) {
            checks++;
            return expected.getMessage();
        }
    }

    private static void owner() throws Exception {
        LocalAuth.unlock(AuthTestSupport.MASTER.toCharArray());
        LocalAuth.signIn("owner", AuthTestSupport.PASSWORD.toCharArray());
    }

    private static void signIn(String username, String password) throws Exception {
        LocalAuth.unlock(AuthTestSupport.MASTER.toCharArray());
        LocalAuth.signIn(username, password.toCharArray());
    }

    private static OrderStore.Order order() throws Exception {
        var item = new CatalogStore().items().get(0);
        var s = PosSettings.get();
        var lines = List.of(
                new OrderPricing.Line(item.id(), item.name(), item.unit().toString(), BigDecimal.ONE, item.price()));
        return new OrderStore.Order(UUID.randomUUID().toString(), "Auth test customer", "", "", "Cash", "",
                BigDecimal.valueOf(1000), lines, OrderPricing.calculate(lines, s.taxEnabled(), s.taxRate()), s);
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("probe")) {
            System.setProperty("laundry.dataDir", args[1]);
            check(!LocalAuth.isSignedIn(), "A process restart does not restore a session");
            reject(() -> LocalAuth.signIn("cashier", "Cashier new password".toCharArray()),
                    "Master key cannot be skipped after restart");
            signIn("cashier", "Cashier new password");
            check(LocalAuth.current().role().equals("Cashier"), "Saved password verifies after restart");
            System.out.println("AUTH_RESTART_OK");
            return;
        }
        Path root = Files.createTempDirectory("laundry-auth-test-");
        System.setProperty("laundry.dataDir", root.toString());
        System.setProperty("laundry.legacyServices", root.resolve("none").toString());
        check(!LocalAuth.configured() && !LocalAuth.isSignedIn(), "New directory has no implicit Owner session");
        check(!ActiveProfile.current().role().equals("Owner"), "Signed-out identity cannot fall back to Owner");
        reject(() -> new CatalogStore().save(new CatalogStore().items()), "Catalog saves require sign-in");
        reject(() -> OrderStore.save(order()), "Checkout requires sign-in");
        AuthTestSupport.loginOwner();
        var owner = LocalAuth.current();
        check(owner.role().equals("Owner") && ActiveProfile.current().id().equals(owner.profileId()),
                "Owner session controls display identity");
        reject(() -> LocalAuth.initialize("another", "Another", AuthTestSupport.PASSWORD.toCharArray(),
                AuthTestSupport.MASTER.toCharArray()), "Existing accounts cannot be overwritten");
        reject(() -> LocalAuth.updateAccount(owner.id(), "Cashier", true), "Cannot demote the last Owner");
        reject(() -> LocalAuth.updateAccount(owner.id(), "Owner", false), "Cannot disable the last Owner");
        LocalAuth.createAccount("cashier", "Cashier Sample", "Cashier", "Temporary password".toCharArray());
        LocalAuth.createAccount("manager", "Manager Sample", "Manager", "Temporary password".toCharArray());
        reject(() -> LocalAuth.createAccount("CASHIER", "Duplicate", "Owner", "Temporary password".toCharArray()),
                "Usernames are case-insensitively unique");
        reject(() -> LocalAuth.createAccount("bad user", "Bad", "Owner", "Temporary password".toCharArray()),
                "Invalid username rejected");
        reject(() -> LocalAuth.createAccount("short", "Short", "Owner", "short".toCharArray()),
                "Short password rejected");
        var accounts = LocalAuth.accounts();
        var cashier = accounts.stream().filter(a -> a.username().equals("cashier")).findFirst().orElseThrow();
        var manager = accounts.stream().filter(a -> a.username().equals("manager")).findFirst().orElseThrow();
        Properties stored = AtomicProperties.read(root.resolve("authentication.properties"));
        check(!stored.getProperty("account.1.salt").equals(stored.getProperty("account.2.salt")),
                "Each account has a unique random salt");
        check(!stored.getProperty("account.1.hash").equals(stored.getProperty("account.2.hash")),
                "Same passwords have distinct stored hashes");
        String file = Files.readString(root.resolve("authentication.properties"));
        check(!file.contains("Temporary password") && !file.contains(AuthTestSupport.PASSWORD)
                && !file.contains(AuthTestSupport.MASTER), "Passwords and master key are never persisted as plaintext");
        LocalAuth.signOut();
        reject(() -> LocalAuth.signIn("owner", AuthTestSupport.PASSWORD.toCharArray()),
                "Valid account cannot bypass master key");
        reject(() -> new StockStore().setCount("surf", BigDecimal.ONE, BigDecimal.ONE, ""),
                "Signed-out stock mutation blocked");
        reject(() -> new WorkspaceProbe().check(), "Workspace construction requires authentication");
        reject(() -> LocalAuth.unlock("wrong master".toCharArray()), "Wrong master key blocked");
        LocalAuth.unlock(AuthTestSupport.MASTER.toCharArray());
        String unknown = reject(() -> LocalAuth.signIn("unknown", "Wrong password".toCharArray()),
                "Unknown account rejected");
        String wrong = reject(() -> LocalAuth.signIn("owner", "Wrong password".toCharArray()),
                "Wrong password rejected");
        check(unknown.equals(wrong), "Unknown and incorrect accounts have the same error");
        LocalAuth.signIn(" CASHIER ", "Temporary password".toCharArray());
        check(LocalAuth.pending().mustChangePassword() && !LocalAuth.isSignedIn(),
                "Temporary account cannot enter workspace until password changed");
        reject(() -> OrderStore.save(order()), "Temporary password cannot authorize checkout");
        reject(() -> LocalAuth.changePassword("wrong".toCharArray(), "Cashier new password".toCharArray()),
                "Password change needs the current password");
        reject(() -> LocalAuth.changePassword("Temporary password".toCharArray(), "Temporary password".toCharArray()),
                "Temporary password must be replaced");
        LocalAuth.changePassword("Temporary password".toCharArray(), "Cashier new password".toCharArray());
        check(LocalAuth.isSignedIn() && !LocalAuth.current().mustChangePassword(),
                "Successful first-login password change activates session");
        check(LocalAuth.allowed(Permission.CHECKOUT) && !LocalAuth.allowed(Permission.CATALOG_EDIT),
                "Cashier has checkout without catalog edit permission");
        reject(() -> new CatalogStore().save(new CatalogStore().items()), "Cashier direct catalog write blocked");
        reject(() -> PosSettings.save(PosSettings.defaults()), "Cashier cannot change tax/store settings");
        reject(() -> OperationsStore.addBranch("Forbidden"), "Cashier cannot manage operations");
        reject(() -> LocalAuth.accounts(), "Cashier cannot manage accounts");
        reject(() -> application.BackupService.create(root.getParent().resolve(UUID.randomUUID() + ".zip")),
                "Cashier cannot export all business data");
        reject(() -> ActiveProfile.select(owner.profileId()), "Profile selection cannot impersonate Owner");
        var team = new ArrayList<>(new TeamStore().read());
        int index = -1;
        for (int i = 0; i < team.size(); i++)
            if (team.get(i).id().equals(cashier.profileId()))
                index = i;
        var own = team.get(index);
        team.set(index,
                new TeamStore.Member(own.id(), "My own name", "Contact", own.role(), own.active(), own.image()));
        new TeamStore().save(team);
        check(ActiveProfile.current().name().equals("My own name"), "Cashier can update own display profile");
        team.set(index, new TeamStore.Member(own.id(), "Escalated", "", "Owner", true));
        reject(() -> new TeamStore().save(team), "Profile edit cannot elevate role");
        var ownOrder = OrderStore.save(order());
        Properties saved = AtomicProperties.read(ownOrder);
        check(saved.getProperty("cashier.id").equals(cashier.profileId())
                && saved.getProperty("cashier.name").equals("My own name"), "Orders record verified cashier identity");
        PrinterPreferences.save(PrinterPreferences.defaults(80));
        check(Files.exists(root.resolve("printer.properties")), "Cashier can configure their local receipt printer");
        LocalAuth.signOut();
        check(!LocalAuth.isSignedIn(), "Sign-out clears session");
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Djava.awt.headless=true", "-cp", System.getProperty("java.class.path"), "LocalAuthTest", "probe",
                root.toString()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        check(process.waitFor() == 0 && output.contains("AUTH_RESTART_OK"),
                "Local credentials persist across a fresh process: " + output);
        signIn("manager", "Temporary password");
        LocalAuth.changePassword("Temporary password".toCharArray(), "Manager new password".toCharArray());
        check(LocalAuth.allowed(Permission.REPORTS) && LocalAuth.allowed(Permission.CATALOG_EDIT)
                && !LocalAuth.allowed(Permission.ACCOUNTS),
                "Manager has operational access without Owner account controls");
        new CatalogStore().save(new CatalogStore().items());
        checks++;
        // Changes written by another running instance invalidate the already-open
        // session.
        stored = AtomicProperties.read(root.resolve("authentication.properties"));
        stored.setProperty("account.2.active", "false");
        stored.setProperty("account.2.version", "3");
        AtomicProperties.write(root.resolve("authentication.properties"), stored);
        check(!LocalAuth.isSignedIn(), "Disabled/changed account invalidates existing session");
        reject(() -> OrderStore.save(order()), "Revoked session cannot submit an existing cart");
        owner();
        LocalAuth.resetPassword(cashier.id(), "Reset temp password".toCharArray());
        LocalAuth.unlock(AuthTestSupport.MASTER.toCharArray());
        reject(() -> LocalAuth.signIn("cashier", "Cashier new password".toCharArray()),
                "Old password fails after Owner reset");
        LocalAuth.signIn("cashier", "Reset temp password".toCharArray());
        check(LocalAuth.pending().mustChangePassword(), "Owner reset requires another password change");
        LocalAuth.cancelSignIn();
        check(!LocalAuth.isSignedIn(), "Cancel first-login change leaves no authenticated session");
        owner();
        for (int i = 0; i < 5; i++)
            reject(() -> LocalAuth.signIn("owner", "Wrong password".toCharArray()), "Wrong attempt rejected");
        check(reject(() -> LocalAuth.signIn("owner", AuthTestSupport.PASSWORD.toCharArray()),
                "Lockout rejects correct password temporarily").contains("one minute"),
                "Repeated failures are throttled");
        stored = AtomicProperties.read(root.resolve("authentication.properties"));
        check(Long.parseLong(stored.getProperty("account.0.lockedUntil")) > System.currentTimeMillis(),
                "Throttle persists on disk");
        stored.setProperty("account.0.lockedUntil", "0");
        AtomicProperties.write(root.resolve("authentication.properties"), stored);
        owner();
        LocalAuth.changeMasterKey(AuthTestSupport.MASTER.toCharArray(), "Replacement master key".toCharArray());
        check(!LocalAuth.isSignedIn(), "Master-key change signs out the current session");
        reject(() -> LocalAuth.unlock(AuthTestSupport.MASTER.toCharArray()), "Old master key no longer unlocks");
        LocalAuth.unlock("Replacement master key".toCharArray());
        LocalAuth.signIn("owner", AuthTestSupport.PASSWORD.toCharArray());
        check(LocalAuth.isSignedIn(), "New master key unlocks saved accounts");
        String validAccounts = Files.readString(root.resolve("authentication.properties"));
        Files.writeString(root.resolve("authentication.properties"), "bad=" + "\\uZZZZ");
        check(!LocalAuth.isSignedIn(), "Malformed property encoding fails closed without crashing the session check");
        Files.writeString(root.resolve("authentication.properties"), validAccounts);
        check(LocalAuth.isSignedIn(), "Trusted valid storage remains usable after a failed decode");
        Properties incomplete = AtomicProperties.read(root.resolve("authentication.properties"));
        incomplete.remove("account.0.change");
        AtomicProperties.write(root.resolve("authentication.properties"), incomplete);
        check(!LocalAuth.isSignedIn(), "Missing password-change flag fails closed");
        Files.writeString(root.resolve("authentication.properties"), "corrupt");
        check(!LocalAuth.isSignedIn(), "Corrupt storage fails closed");
        reject(() -> LocalAuth.initialize("owner", "Replacement", AuthTestSupport.PASSWORD.toCharArray(),
                AuthTestSupport.MASTER.toCharArray()), "Corrupt account file is never silently replaced");
        System.out.println("PASS: " + checks + " local authentication, permissions, lockout and restart checks.");
    }

    private static class WorkspaceProbe {
        void check() {
            new ui.frames.WorkspacePanel();
        }
    }
}
