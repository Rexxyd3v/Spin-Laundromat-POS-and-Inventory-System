package application;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import persistence.*;

/**
 * Checksummed local snapshots and isolated restores; never overwrites the live
 * data folder.
 */
public final class BackupService {
    private static final long MAX_TOTAL = 2L * 1024 * 1024 * 1024;

    public record Verified(int files, long bytes) {
    }

    private static String hash(InputStream input) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536];
            int n;
            while ((n = input.read(buffer)) != -1)
                digest.update(buffer, 0, n);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String hash(Path file) throws IOException {
        try (var input = Files.newInputStream(file)) {
            return hash(input);
        }
    }

    private static void safeName(String name) throws IOException {
        if (name.isBlank() || name.contains("\\") || name.contains(":") || name.startsWith("/")
                || Arrays.stream(name.split("/", -1)).anyMatch(p -> p.equals("..") || p.equals(".") || p.isEmpty())
                || !(name.startsWith("data/") || name.startsWith("legacy/") || name.startsWith("export/")))
            throw new IOException("Unsafe archive entry.");
    }

    private static Path canonical(Path path) throws IOException {
        Path absolute = path.toAbsolutePath().normalize();
        if (Files.exists(absolute))
            return absolute.toRealPath();
        Path parent = absolute.getParent();
        return parent == null ? absolute : canonical(parent).resolve(absolute.getFileName()).normalize();
    }

    public static Path create(Path destination) throws IOException {
        security.LocalAuth.require(security.LocalAuth.Permission.BACKUP);
        Path target = canonical(destination);
        if (target.startsWith(canonical(DataLocation.root())))
            throw new IOException("Save the backup outside the active data folder.");
        if (Files.exists(target))
            throw new FileAlreadyExistsException("Choose a new backup filename.");
        if (target.getParent() == null || !Files.isDirectory(target.getParent()))
            throw new IOException("Choose an existing destination folder.");
        Path temporary = Files.createTempFile(target.getParent(), ".laundry-backup-", ".zip");
        try {
            synchronized (LocalTransactions.LOCK) {
                Map<String, Path> source = new TreeMap<>();
                collect(DataLocation.root(), "data/", source);
                Path legacy = Path.of(System.getProperty("laundry.legacyServices", "services.properties"))
                        .toAbsolutePath().normalize();
                if (Files.isRegularFile(legacy))
                    source.put("legacy/services.properties", legacy);
                // Standard launch may have an older repository-level data folder. Preserve it
                // as a separate source.
                if (System.getProperty("laundry.dataDir") == null) {
                    Path working = Path.of("").toAbsolutePath();
                    Path older = working.getParent() == null ? working : working.getParent().resolve("data");
                    if (!older.equals(DataLocation.root()) && Files.isDirectory(older))
                        collect(older, "legacy/other-data/", source);
                    Path olderServices = working.getParent() == null ? legacy
                            : working.getParent().resolve("services.properties");
                    if (Files.isRegularFile(olderServices) && !olderServices.equals(legacy))
                        source.put("legacy/other-services.properties", olderServices);
                }
                source.entrySet().removeIf(e -> e.getValue().equals(temporary) || e.getValue().equals(target));
                if (Files.exists(target))
                    throw new FileAlreadyExistsException("Choose a new backup filename.");
                Properties manifest = new Properties();
                manifest.setProperty("format", "laundry-snapshot-1");
                manifest.setProperty("created", java.time.OffsetDateTime.now().toString());
                int n = 0;
                long bytes = 0;
                try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temporary))) {
                    for (var entry : source.entrySet()) {
                        safeName(entry.getKey());
                        bytes += Files.size(entry.getValue());
                        if (bytes > MAX_TOTAL)
                            throw new IOException("Backup exceeds the 2 GB local package limit.");
                        zip.putNextEntry(new ZipEntry(entry.getKey()));
                        Files.copy(entry.getValue(), zip);
                        zip.closeEntry();
                        manifest.setProperty("file." + n + ".path", entry.getKey());
                        manifest.setProperty("file." + n + ".sha256", hash(entry.getValue()));
                        n++;
                    }
                    // Export may fail on damaged source data. Keep a recoverable raw backup with an
                    // explicit review note.
                    Map<String, byte[]> exports;
                    try {
                        exports = MigrationAudit.exports();
                    } catch (Exception ex) {
                        exports = Map.of("export/audit.txt",
                                ("Export needs review: " + ex.getMessage()
                                        + "\nRaw source files were preserved; no import ran.")
                                        .getBytes(StandardCharsets.UTF_8));
                    }
                    for (var entry : exports.entrySet()) {
                        zip.putNextEntry(new ZipEntry(entry.getKey()));
                        zip.write(entry.getValue());
                        zip.closeEntry();
                        manifest.setProperty("file." + n + ".path", entry.getKey());
                        manifest.setProperty("file." + n + ".sha256", hash(new ByteArrayInputStream(entry.getValue())));
                        n++;
                    }
                    manifest.setProperty("count", Integer.toString(n));
                    zip.putNextEntry(new ZipEntry("manifest.properties"));
                    var writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
                    manifest.store(writer, "Verified laundry backup");
                    writer.flush();
                    zip.closeEntry();
                }
                verify(temporary);
                Files.move(temporary, target);
                return target;
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void collect(Path folder, String prefix, Map<String, Path> output) throws IOException {
        if (!Files.exists(folder))
            return;
        try (var paths = Files.walk(folder)) {
            for (Path file : paths.toList()) {
                if (Files.isSymbolicLink(file))
                    throw new IOException("Backups do not follow symbolic links.");
                if (Files.isRegularFile(file))
                    output.put(prefix + folder.relativize(file).toString().replace('\\', '/'), file);
            }
        }
    }

    private static Properties manifest(ZipFile zip) throws IOException {
        var entry = zip.getEntry("manifest.properties");
        if (entry == null || entry.getSize() < 0 || entry.getSize() > 8 * 1024 * 1024)
            throw new IOException("Missing or oversized backup manifest.");
        Properties result = new Properties();
        try (var r = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
            result.load(r);
        }
        if (!"laundry-snapshot-1".equals(result.getProperty("format")))
            throw new IOException("Unsupported backup format.");
        return result;
    }

    public static Verified verify(Path file) throws IOException {
        security.LocalAuth.require(security.LocalAuth.Permission.BACKUP);
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Properties manifest = manifest(zip);
            int count;
            try {
                count = Integer.parseInt(manifest.getProperty("count"));
            } catch (RuntimeException ex) {
                throw new IOException("Invalid manifest count.", ex);
            }
            if (count < 0 || count > 50000)
                throw new IOException("Invalid manifest count.");
            Set<String> expected = new HashSet<>();
            long total = 0;
            for (int i = 0; i < count; i++) {
                String name = manifest.getProperty("file." + i + ".path", "");
                safeName(name);
                if (!expected.add(name))
                    throw new IOException("Duplicate manifest entry.");
                var entry = zip.getEntry(name);
                if (entry == null || entry.isDirectory() || entry.getSize() < 0 || entry.getSize() > MAX_TOTAL - total)
                    throw new IOException("Missing or oversized archive content.");
                total += entry.getSize();
                try (var input = zip.getInputStream(entry)) {
                    if (!hash(input).equals(manifest.getProperty("file." + i + ".sha256")))
                        throw new IOException("Backup checksum mismatch: " + name);
                }
            }
            Set<String> seen = new HashSet<>();
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!seen.add(name) || (!name.equals("manifest.properties") && !expected.contains(name)))
                    throw new IOException("Unexpected or duplicate archive content.");
            }
            return new Verified(count, total);
        }
    }

    public static Path restoreCopy(Path archive, Path destination) throws IOException {
        security.LocalAuth.require(security.LocalAuth.Permission.BACKUP);
        verify(archive);
        Path target = canonical(destination);
        if (target.startsWith(canonical(DataLocation.root())) || Files.exists(target) || target.getParent() == null
                || !Files.isDirectory(target.getParent()))
            throw new IOException(
                    "Restore to a new folder outside active data. Existing folders are never overwritten.");
        // Stage privately. On failure, leave no partially restored destination.
        Path staging = Files.createTempDirectory(target.getParent(), ".laundry-restore-");
        try {
            try (ZipFile zip = new ZipFile(archive.toFile())) {
                Properties manifest = manifest(zip);
                int count = Integer.parseInt(manifest.getProperty("count"));
                for (int i = 0; i < count; i++) {
                    String name = manifest.getProperty("file." + i + ".path");
                    safeName(name);
                    Path out = staging.resolve(name).normalize();
                    if (!out.startsWith(staging))
                        throw new IOException("Invalid restore path.");
                    Files.createDirectories(out.getParent());
                    try (var input = zip.getInputStream(zip.getEntry(name))) {
                        Files.copy(input, out);
                    }
                    if (!hash(out).equals(manifest.getProperty("file." + i + ".sha256")))
                        throw new IOException("Restored checksum mismatch.");
                }
            }
            Files.move(staging, target);
            return target;
        } finally {
            if (Files.exists(staging))
                try (var paths = Files.walk(staging)) {
                    for (Path p : paths.sorted(Comparator.reverseOrder()).toList())
                        Files.deleteIfExists(p);
                }
        }
    }

    private BackupService() {
    }
}
