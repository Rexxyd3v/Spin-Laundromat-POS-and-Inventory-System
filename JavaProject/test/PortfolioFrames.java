import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;

/**
 * Lossless 1080p frames for a short portfolio clip; input is isolated demo UI
 * only.
 */
public final class PortfolioFrames {
    private final Path folder;
    private int count;
    private BufferedImage last;
    private Path lastFile;

    public PortfolioFrames(Path folder) throws IOException {
        this.folder = folder;
        Files.createDirectories(folder);
        try (var files = Files.list(folder)) {
            if (files.findAny().isPresent())
                throw new IOException("Choose an empty frame folder for this recording.");
        }
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g;
    }

    private static BufferedImage frame(BufferedImage screenshot, String caption) {
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphics(image);
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, 1920, 1080);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("SPIN LAUNDROMAT", 96, 38);
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g.drawString("JAVA DESKTOP POS", 1632, 38);
        double scale = Math.min(1728.0 / screenshot.getWidth(), 944.0 / screenshot.getHeight());
        int w = (int) Math.round(screenshot.getWidth() * scale), h = (int) Math.round(screenshot.getHeight() * scale);
        int x = (1920 - w) / 2, y = 64 + (944 - h) / 2;
        g.drawImage(screenshot, x, y, w, h, null);
        g.setColor(new Color(51, 65, 85));
        g.drawRect(x - 1, y - 1, w + 1, h + 1);
        g.setColor(new Color(226, 232, 240));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g.drawString(caption, 96, 1051);
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g.drawString("PORTFOLIO PREVIEW / DEMO DATA", 1560, 1051);
        g.dispose();
        return image;
    }

    public void capture(BufferedImage screenshot, String caption) throws IOException {
        write(frame(screenshot, caption));
    }

    private void write(BufferedImage image) throws IOException {
        Path file = folder.resolve(String.format("frame-%05d.png", count++));
        ImageIO.write(image, "png", file.toFile());
        last = image;
        lastFile = file;
    }

    private void hold(int frames) throws IOException {
        Path source = lastFile;
        for (int i = 0; i < frames; i++) {
            Path next = folder.resolve(String.format("frame-%05d.png", count++));
            Files.copy(source, next);
            lastFile = next;
        }
    }

    private void transition(BufferedImage next) throws IOException {
        BufferedImage previous = last;
        for (int i = 1; i <= 6; i++) {
            BufferedImage blend = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = graphics(blend);
            g.drawImage(previous, 0, 0, null);
            g.setComposite(AlphaComposite.SrcOver.derive(i / 6f));
            g.drawImage(next, 0, 0, null);
            g.dispose();
            write(blend);
        }
    }

    public void finish(Path screenshots, Path videoFolder) throws IOException {
        Files.createDirectories(videoFolder);
        ImageIO.write(last, "png", videoFolder.resolve("poster.png").toFile());
        String[][] pages = { { "pos-desktop", "Point of sale / Build an order" },
                { "catalog", "Services & products / Sample photos" }, { "inventory", "Inventory / Stock thresholds" },
                { "reports", "Reports / Sales and receipts" } };
        for (String[] page : pages) {
            BufferedImage screenshot = ImageIO.read(screenshots.resolve(page[0] + ".png").toFile());
            if (screenshot == null)
                throw new IOException("Missing portfolio screen: " + page[0]);
            transition(frame(screenshot, page[1]));
            hold(22);
        }
        BufferedImage end = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphics(end);
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, 1920, 1080);
        g.setColor(new Color(13, 148, 136));
        g.fillRoundRect(880, 390, 160, 6, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 64));
        String title = "Spin Laundromat";
        g.drawString(title, (1920 - g.getFontMetrics().stringWidth(title)) / 2, 510);
        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        String subtitle = "Laundry POS / Desktop application";
        g.drawString(subtitle, (1920 - g.getFontMetrics().stringWidth(subtitle)) / 2, 570);
        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        String note = "Portfolio preview / Demo data";
        g.drawString(note, (1920 - g.getFontMetrics().stringWidth(note)) / 2, 650);
        g.dispose();
        transition(end);
        hold(10);
        System.out.println("Exported " + count + " lossless portfolio frames at 1920x1080 (10 fps source).");
    }
}
