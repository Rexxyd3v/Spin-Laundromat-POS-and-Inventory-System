package util.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Shared persistence: a failed write never replaces the previous valid file.
 */
public final class AtomicProperties {
    private AtomicProperties() {
    }

    public static Path dataPath(String name) {
        return persistence.DataLocation.resolve(name);
    }

    public static Properties read(Path path) throws IOException {
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            }
        }
        return properties;
    }

    public static void write(Path path, Properties properties) throws IOException {
        Path target = path.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), ".laundry-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                properties.store(writer, "Laundry POS");
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
