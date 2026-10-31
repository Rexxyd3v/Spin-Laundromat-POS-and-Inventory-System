package security;

import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import model.TeamStore;
import persistence.LocalTransactions;
import util.config.*;

/**
 * Local authentication. Sessions are memory-only; every privileged action
 * checks the saved account again.
 */
public final class LocalAuth {
    public enum Permission {
        CHECKOUT, CATALOG_EDIT, STOCK_EDIT, OPERATIONS, REPORTS, TEAM_EDIT, SETTINGS, ACCOUNTS, BACKUP
    }

    public record Account(String id, String username, String profileId, String name, String role, boolean active,
            boolean mustChangePassword, long version) {
    }

    private record Session(String id, long version, long masterVersion) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 600_000;
    private static volatile Session session;
    private static volatile long unlockedVersion;

    private static Path path() {
        return AtomicProperties.dataPath("authentication.properties");
    }

    public static boolean configured() {
        return Files.exists(path());
    }

    private static Properties read() throws IOException {
        try {
            Properties p = AtomicProperties.read(path());
            if (!"1".equals(p.getProperty("format")) || Integer.parseInt(p.getProperty("count")) < 1)
                throw new IllegalArgumentException();
            if (Long.parseLong(p.getProperty("master.version")) < 1)
                throw new IllegalArgumentException();
            validateHash(p, "master.");
            Set<String> names = new HashSet<>(), ids = new HashSet<>(), profiles = new HashSet<>();
            for (int i = 0; i < count(p); i++) {
                Account a = account(p, i);
                if (!names.add(a.username()) || !ids.add(a.id()) || !profiles.add(a.profileId())
                        || !Set.of("Owner", "Manager", "Cashier").contains(a.role()))
                    throw new IllegalArgumentException();
                validateHash(p, key(i));
            }
            return p;
        } catch (RuntimeException ex) {
            throw new IOException(
                    "Account storage needs attention. Restore a trusted backup; accounts have not been reset.", ex);
        }
    }

    private static int count(Properties p) {
        int n = Integer.parseInt(p.getProperty("count"));
        if (n < 1 || n > 1000)
            throw new IllegalArgumentException("Invalid account count");
        return n;
    }

    private static String key(int i) {
        return "account." + i + ".";
    }

    private static Account account(Properties p, int i) {
        String k = key(i);
        String id = Objects.requireNonNull(p.getProperty(k + "id"));
        String profile = Objects.requireNonNull(p.getProperty(k + "profile"));
        String name = Objects.requireNonNull(p.getProperty(k + "name"));
        long version = Long.parseLong(p.getProperty(k + "version"));
        if (id.isBlank() || id.length() > 128 || profile.isBlank() || profile.length() > 128 || name.isBlank()
                || name.length() > 80 || version < 1)
            throw new IllegalArgumentException();
        return new Account(id, normalize(p.getProperty(k + "username")), profile, name, p.getProperty(k + "role"),
                requiredBoolean(p, k + "active"), requiredBoolean(p, k + "change"), version);
    }

    private static boolean requiredBoolean(Properties p, String key) {
        String value = p.getProperty(key);
        if (!"true".equals(value) && !"false".equals(value))
            throw new IllegalArgumentException("Invalid account status.");
        return Boolean.parseBoolean(value);
    }

    private static void validateHash(Properties p, String k) {
        int it = Integer.parseInt(p.getProperty(k + "iterations"));
        if (it < ITERATIONS || it > 2_000_000 || Base64.getDecoder().decode(p.getProperty(k + "salt")).length != 16
                || Base64.getDecoder().decode(p.getProperty(k + "hash")).length != 32)
            throw new IllegalArgumentException();
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password protection is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static void hash(Properties p, String k, char[] password) {
        validatePassword(password);
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] result = derive(password, salt, ITERATIONS);
        p.setProperty(k + "salt", Base64.getEncoder().encodeToString(salt));
        p.setProperty(k + "hash", Base64.getEncoder().encodeToString(result));
        p.setProperty(k + "iterations", Integer.toString(ITERATIONS));
        Arrays.fill(result, (byte) 0);
    }

    private static boolean matches(Properties p, String k, char[] password) {
        byte[] actual = derive(password, Base64.getDecoder().decode(p.getProperty(k + "salt")),
                Integer.parseInt(p.getProperty(k + "iterations")));
        try {
            return MessageDigest.isEqual(actual, Base64.getDecoder().decode(p.getProperty(k + "hash")));
        } finally {
            Arrays.fill(actual, (byte) 0);
        }
    }

    public static void validatePassword(char[] password) {
        if (password == null || password.length < 10 || password.length > 128)
            throw new IllegalArgumentException("Use 10 to 128 characters for the password or master key.");
    }

    private static String normalize(String username) {
        String u = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!u.matches("[a-z0-9._-]{3,40}"))
            throw new IllegalArgumentException("Username: 3–40 letters, numbers, dots, underscores or hyphens.");
        return u;
    }

    private static void putAccount(Properties p, int i, String username, String name, String role, String profile,
            char[] password, boolean change) {
        String k = key(i);
        String u = normalize(username);
        if (name == null || name.isBlank() || name.length() > 80
                || !Set.of("Owner", "Manager", "Cashier").contains(role))
            throw new IllegalArgumentException("Enter a name and valid role.");
        p.setProperty(k + "id", UUID.randomUUID().toString());
        p.setProperty(k + "username", u);
        p.setProperty(k + "profile", profile);
        p.setProperty(k + "name", name.trim());
        p.setProperty(k + "role", role);
        p.setProperty(k + "active", "true");
        p.setProperty(k + "change", Boolean.toString(change));
        p.setProperty(k + "version", "1");
        hash(p, k, password);
    }

    public static void initialize(String username, String name, char[] password, char[] master) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            if (configured())
                throw new IOException("Accounts already exist. Sign in with an Owner account.");
            validatePassword(master);
            validatePassword(password);
            String profile = new TeamStore().read().stream().filter(m -> m.active() && m.role().equals("Owner"))
                    .map(TeamStore.Member::id).findFirst().orElse("local-admin");
            Properties p = new Properties();
            p.setProperty("format", "1");
            p.setProperty("count", "1");
            p.setProperty("master.version", "1");
            hash(p, "master.", master);
            putAccount(p, 0, username, name, "Owner", profile, password, false);
            AtomicProperties.write(path(), p);
        }
    }

    private static void checkLock(Properties p, String k) throws IOException {
        try {
            if (Long.parseLong(p.getProperty(k + "lockedUntil", "0")) > System.currentTimeMillis())
                throw new IOException("Too many attempts. Please wait one minute and try again.");
        } catch (NumberFormatException e) {
            throw new IOException("Account lock information is invalid.", e);
        }
    }

    private static void failed(Properties p, String k) throws IOException {
        int n = Integer.parseInt(p.getProperty(k + "failures", "0")) + 1;
        p.setProperty(k + "failures", Integer.toString(n >= 5 ? 0 : n));
        if (n >= 5)
            p.setProperty(k + "lockedUntil", Long.toString(System.currentTimeMillis() + 60_000));
        AtomicProperties.write(path(), p);
        throw new IOException("Sign-in details are incorrect or the account is unavailable.");
    }

    private static void clearFailures(Properties p, String k) {
        p.remove(k + "failures");
        p.remove(k + "lockedUntil");
    }

    public static void unlock(char[] master) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            session = null;
            unlockedVersion = 0;
            Properties p = read();
            checkLock(p, "master.");
            if (master == null || master.length > 128 || !matches(p, "master.", master))
                failed(p, "master.");
            clearFailures(p, "master.");
            AtomicProperties.write(path(), p);
            unlockedVersion = Long.parseLong(p.getProperty("master.version"));
        }
    }

    public static Account signIn(String username, char[] password) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            session = null;
            Properties p = read();
            if (unlockedVersion == 0 || unlockedVersion != Long.parseLong(p.getProperty("master.version")))
                throw new IOException("Enter the master key first.");
            String u = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
            int i = -1;
            for (int j = 0; j < count(p); j++)
                if (account(p, j).username().equals(u))
                    i = j;
            String k = i < 0 ? "unknown." : key(i);
            checkLock(p, k);
            // Unknown accounts perform the same password derivation, without exposing
            // account existence.
            boolean valid = password != null && password.length <= 128 && matches(p, i < 0 ? "master." : k, password);
            if (i < 0 || !valid || !account(p, i).active())
                failed(p, k);
            clearFailures(p, k);
            AtomicProperties.write(path(), p);
            Account a = account(p, i);
            session = new Session(a.id(), a.version(), unlockedVersion);
            return a;
        }
    }

    private static Account validated(boolean allowChange) {
        Session s = session;
        if (s == null)
            throw new SecurityException("Please sign in to continue.");
        try {
            Properties p = read();
            if (unlockedVersion != s.masterVersion
                    || s.masterVersion != Long.parseLong(p.getProperty("master.version")))
                throw new SecurityException("Please enter the master key again.");
            for (int i = 0; i < count(p); i++) {
                Account a = account(p, i);
                if (a.id().equals(s.id) && a.version() == s.version && a.active()) {
                    if (!allowChange && a.mustChangePassword())
                        throw new SecurityException("Change your temporary password before continuing.");
                    return a;
                }
            }
        } catch (IOException ex) {
            throw new SecurityException(ex.getMessage(), ex);
        }
        throw new SecurityException("Your account has changed. Please sign in again.");
    }

    public static Account current() {
        return validated(false);
    }

    public static Account pending() {
        return validated(true);
    }

    public static boolean isSignedIn() {
        try {
            current();
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }

    public static boolean allowed(Permission permission) {
        try {
            String role = current().role();
            return role.equals("Owner") || permission == Permission.CHECKOUT || (role.equals("Manager")
                    && Set.of(Permission.CATALOG_EDIT, Permission.STOCK_EDIT, Permission.OPERATIONS, Permission.REPORTS)
                            .contains(permission));
        } catch (SecurityException ex) {
            return false;
        }
    }

    public static void require(Permission permission) {
        current();
        if (!allowed(permission))
            throw new SecurityException("Your account does not have permission for this action.");
    }

    public static void signOut() {
        synchronized (LocalTransactions.LOCK) {
            session = null;
            unlockedVersion = 0;
        }
        AppEvents.fire("auth");
    }

    public static void cancelSignIn() {
        synchronized (LocalTransactions.LOCK) {
            session = null;
        }
    }

    public static List<Account> profiles() throws IOException {
        if (!configured())
            return List.of();
        Properties p = read();
        List<Account> list = new ArrayList<>();
        for (int i = 0; i < count(p); i++)
            list.add(account(p, i));
        return List.copyOf(list);
    }

    public static List<Account> accounts() throws IOException {
        require(Permission.ACCOUNTS);
        return profiles();
    }

    public static void createAccount(String username, String name, String role, char[] password) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            require(Permission.ACCOUNTS);
            Properties p = read();
            String u = normalize(username);
            if (profiles().stream().anyMatch(a -> a.username().equals(u)))
                throw new IOException("That username is already in use.");
            int n = count(p);
            if (n >= 1000)
                throw new IOException("Account limit reached.");
            putAccount(p, n, u, name, role, UUID.randomUUID().toString(), password, true);
            p.setProperty("count", Integer.toString(n + 1));
            AtomicProperties.write(path(), p);
            AppEvents.fire("team");
        }
    }

    public static void updateAccount(String id, String role, boolean active) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            require(Permission.ACCOUNTS);
            if (!Set.of("Owner", "Manager", "Cashier").contains(role))
                throw new IllegalArgumentException("Choose a valid role.");
            Properties p = read();
            int index = find(p, id);
            Account a = account(p, index);
            if (a.role().equals("Owner") && a.active() && (!active || !role.equals("Owner"))
                    && profiles().stream().filter(v -> v.active() && v.role().equals("Owner")).count() <= 1)
                throw new IOException("Keep at least one active Owner account.");
            if (a.role().equals(role) && a.active() == active)
                return;
            p.setProperty(key(index) + "role", role);
            p.setProperty(key(index) + "active", Boolean.toString(active));
            bump(p, index);
            AtomicProperties.write(path(), p);
            AppEvents.fire("team");
            AppEvents.fire("auth-changed");
        }
    }

    public static void resetPassword(String id, char[] temporary) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            require(Permission.ACCOUNTS);
            Properties p = read();
            int i = find(p, id);
            hash(p, key(i), temporary);
            p.setProperty(key(i) + "change", "true");
            clearFailures(p, key(i));
            bump(p, i);
            AtomicProperties.write(path(), p);
            AppEvents.fire("auth-changed");
        }
    }

    public static void changePassword(char[] oldPassword, char[] replacement) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            Account a = pending();
            Properties p = read();
            int i = find(p, a.id());
            checkLock(p, key(i));
            if (oldPassword == null || oldPassword.length > 128 || !matches(p, key(i), oldPassword))
                failed(p, key(i));
            validatePassword(replacement);
            if (Arrays.equals(oldPassword, replacement))
                throw new IllegalArgumentException("Choose a different password.");
            hash(p, key(i), replacement);
            p.setProperty(key(i) + "change", "false");
            clearFailures(p, key(i));
            bump(p, i);
            AtomicProperties.write(path(), p);
            session = new Session(a.id(), a.version() + 1, unlockedVersion);
        }
    }

    public static void changeMasterKey(char[] oldKey, char[] replacement) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            require(Permission.ACCOUNTS);
            Properties p = read();
            checkLock(p, "master.");
            if (oldKey == null || oldKey.length > 128 || !matches(p, "master.", oldKey))
                failed(p, "master.");
            validatePassword(replacement);
            if (Arrays.equals(oldKey, replacement))
                throw new IllegalArgumentException("Choose a different master key.");
            hash(p, "master.", replacement);
            clearFailures(p, "master.");
            p.setProperty("master.version", Long.toString(Long.parseLong(p.getProperty("master.version")) + 1));
            AtomicProperties.write(path(), p);
            signOut();
        }
    }

    private static int find(Properties p, String id) throws IOException {
        for (int i = 0; i < count(p); i++)
            if (account(p, i).id().equals(id))
                return i;
        throw new IOException("Account not found.");
    }

    private static void bump(Properties p, int i) {
        p.setProperty(key(i) + "version", Long.toString(account(p, i).version() + 1));
    }

    public static String temporaryPassword() {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Explicit first-time sample setup; refuses to replace existing authentication.
     * No plaintext credential file.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !args[0].equals("--samples"))
            throw new IllegalArgumentException("Use --samples for first-time sample setup.");
        String master = temporaryPassword(), owner = temporaryPassword(), manager = temporaryPassword(),
                cashier = temporaryPassword();
        char[] m = master.toCharArray(), o = owner.toCharArray(), mp = manager.toCharArray(),
                cp = cashier.toCharArray();
        try {
            synchronized (LocalTransactions.LOCK) {
                if (configured())
                    throw new IOException("Accounts already exist; no changes were made.");
                String ownerProfile = new TeamStore().read().stream()
                        .filter(v -> v.active() && v.role().equals("Owner")).map(TeamStore.Member::id).findFirst()
                        .orElse("local-admin");
                Properties p = new Properties();
                p.setProperty("format", "1");
                p.setProperty("count", "3");
                p.setProperty("master.version", "1");
                hash(p, "master.", m);
                putAccount(p, 0, "owner", "Store Owner", "Owner", ownerProfile, o, true);
                putAccount(p, 1, "manager", "Sample Manager", "Manager", UUID.randomUUID().toString(), mp, true);
                putAccount(p, 2, "cashier", "Sample Cashier", "Cashier", UUID.randomUUID().toString(), cp, true);
                // Commit all three accounts together; never partially provision a sample store.
                AtomicProperties.write(path(), p);
            }
        } finally {
            Arrays.fill(m, '\0');
            Arrays.fill(o, '\0');
            Arrays.fill(mp, '\0');
            Arrays.fill(cp, '\0');
            signOut();
        }
        System.out.println("Master key: " + master);
        System.out.println("owner: " + owner);
        System.out.println("manager: " + manager);
        System.out.println("cashier: " + cashier);
        System.out.println(
                "Temporary passwords must be changed on first sign-in. Save these details; they are shown only once.");
    }

    private LocalAuth() {
    }
}
