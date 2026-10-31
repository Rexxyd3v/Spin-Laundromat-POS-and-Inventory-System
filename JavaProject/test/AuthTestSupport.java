import security.LocalAuth;
import util.config.AtomicProperties;
import java.util.Properties;

/**
 * Fixtures use the public authentication flow in an isolated test data
 * directory.
 */
public final class AuthTestSupport {
    static final String PASSWORD = "Test owner password 2026", MASTER = "Test master key 2026";

    public static void loginOwner() throws Exception {
        loginOwner("test-owner");
    }

    public static void loginOwner(String profileId) throws Exception {
        if (!LocalAuth.configured()) {
            Properties p = new Properties();
            p.setProperty("count", "1");
            p.setProperty("member.0.id", profileId);
            p.setProperty("member.0.name", "Test Owner");
            p.setProperty("member.0.contact", "");
            p.setProperty("member.0.role", "Owner");
            p.setProperty("member.0.active", "true");
            AtomicProperties.write(AtomicProperties.dataPath("team.properties"), p);
            char[] password = PASSWORD.toCharArray(), master = MASTER.toCharArray();
            try {
                LocalAuth.initialize("owner", "Test Owner", password, master);
            } finally {
                java.util.Arrays.fill(password, '\0');
                java.util.Arrays.fill(master, '\0');
            }
        }
        LocalAuth.unlock(MASTER.toCharArray());
        LocalAuth.signIn("owner", PASSWORD.toCharArray());
    }

    private AuthTestSupport() {
    }
}
