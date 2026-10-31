package model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import util.config.AtomicProperties;

/**
 * Photos are copied into application storage; moving the original will not
 * break them.
 */
public final class ItemImages {
    public static String importPhoto(Path source) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            security.LocalAuth.current();
            validate(source);
            String format;
            try (ImageInputStream stream = ImageIO.createImageInputStream(source.toFile())) {
                ImageReader reader = ImageIO.getImageReaders(stream).next();
                try {
                    format = reader.getFormatName().toLowerCase(Locale.ROOT);
                } finally {
                    reader.dispose();
                }
            }
            String relative = "images/" + UUID.randomUUID() + (format.equals("png") ? ".png" : ".jpg");
            Path target = resolve(relative);
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
            return relative;

        }
    }

    public static void validate(Path source) throws IOException {
        if (!Files.isRegularFile(source) || Files.size(source) > 12 * 1024 * 1024)
            throw new IOException("Choose a PNG or JPEG image smaller than 12 MB.");
        try (ImageInputStream stream = ImageIO.createImageInputStream(source.toFile())) {
            if (stream == null)
                throw new IOException("This image could not be opened.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext())
                throw new IOException("Choose a valid PNG or JPEG image.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "jpg").contains(format))
                    throw new IOException("PNG and JPEG images are supported.");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 20_000_000)
                    throw new IOException("Choose an image up to 20 megapixels.");
            } finally {
                reader.dispose();
            }
        }
    }

    public static Path resolve(String relative) throws IOException {
        Path base = AtomicProperties.dataPath("").toAbsolutePath().normalize();
        Path target = base.resolve(relative).normalize();
        if (relative.isBlank() || !target.startsWith(base.resolve("images")) || target.equals(base.resolve("images")))
            throw new IOException("Invalid image location.");
        return target;
    }

    public static BufferedImage read(Path path) throws IOException {
        validate(path);
        return ImageIO.read(path.toFile());
    }

    private ItemImages() {
    }
}
