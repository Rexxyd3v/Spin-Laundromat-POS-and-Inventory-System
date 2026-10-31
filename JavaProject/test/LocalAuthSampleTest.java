import java.nio.file.*;
import java.util.*;
import security.LocalAuth;
import util.config.AtomicProperties;

public class LocalAuthSampleTest {
    private static int checks;

    private static void check(boolean v, String m) {
        checks++;
        if (!v)
            throw new AssertionError(m);
    }

    private static Process setup(Path root) throws Exception {
        return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Djava.awt.headless=true", "-Dlaundry.dataDir=" + root, "-cp", System.getProperty("java.class.path"),
                "security.LocalAuth", "--samples").redirectErrorStream(true).start();
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-auth-sample-");
        System.setProperty("laundry.dataDir", root.toString());
        Properties team = new Properties();
        team.setProperty("count", "1");
        team.setProperty("member.0.id", "existing-owner");
        team.setProperty("member.0.name", "Existing Name");
        team.setProperty("member.0.role", "Owner");
        team.setProperty("member.0.active", "true");
        team.setProperty("member.0.image", "images/keep-photo.png");
        AtomicProperties.write(root.resolve("team.properties"), team);
        byte[] beforeTeam = Files.readAllBytes(root.resolve("team.properties"));
        Files.writeString(root.resolve("business-record.txt"), "Existing business data");
        var process = setup(root);
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        check(process.waitFor() == 0, "First sample initialization succeeds");
        Map<String, String> credentials = new HashMap<>();
        for (String line : output.split("\\R")) {
            int colon = line.indexOf(": ");
            if (colon > 0)
                credentials.put(line.substring(0, colon), line.substring(colon + 2));
        }
        check(credentials.size() == 4, "Master key and all three credentials are displayed exactly once");
        check(new HashSet<>(credentials.values()).size() == 4, "Generated master key and passwords are distinct");
        check(LocalAuth.profiles().size() == 3, "All sample accounts committed together");
        var owner = LocalAuth.profiles().stream().filter(a -> a.username().equals("owner")).findFirst().orElseThrow();
        check(owner.profileId().equals("existing-owner"), "Sample Owner keeps existing profile identity");
        check(Arrays.equals(beforeTeam, Files.readAllBytes(root.resolve("team.properties"))),
                "Sample setup does not rewrite profile photos or names");
        check(Files.readString(root.resolve("business-record.txt")).equals("Existing business data"),
                "Sample setup preserves business files");
        for (String user : List.of("owner", "manager", "cashier")) {
            LocalAuth.unlock(credentials.get("Master key").toCharArray());
            var a = LocalAuth.signIn(user, credentials.get(user).toCharArray());
            check(a.mustChangePassword() && !LocalAuth.isSignedIn(),
                    "Sample " + user + " must change temporary password");
            LocalAuth.cancelSignIn();
        }
        String stored = Files.readString(root.resolve("authentication.properties"));
        check(credentials.values().stream().noneMatch(stored::contains), "No generated secret appears in local file");
        byte[] beforeAccounts = Files.readAllBytes(root.resolve("authentication.properties"));
        var duplicate = setup(root);
        duplicate.getInputStream().readAllBytes();
        check(duplicate.waitFor() != 0
                && Arrays.equals(beforeAccounts, Files.readAllBytes(root.resolve("authentication.properties"))),
                "Repeating setup cannot replace existing accounts");
        System.out.println("PASS: " + checks + " sample account setup and preservation checks.");
    }
}
