package persistence;

import java.nio.file.Path;

/** One location for active application data; changing it requires a restart. */
public final class DataLocation {
    public static Path root() {
        return Path.of(System.getProperty("laundry.dataDir", "data")).toAbsolutePath().normalize();
    }

    public static Path resolve(String relative) {
        Path result = root().resolve(relative).normalize();
        if (!result.startsWith(root()))
            throw new IllegalArgumentException("Data path must stay within the active folder.");
        return result;
    }

    private DataLocation() {
    }
}
