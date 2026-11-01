import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Node;

/** Builds shareable README media from the isolated Swing preview renders. */
public final class DocumentationMedia {
    private static final int WIDTH = 960, HEIGHT = 640;
    private static final List<String> SCREENSHOTS = List.of("welcome", "staff-login", "overview", "pos-desktop",
            "pos-checkout", "catalog", "inventory", "reports", "operations", "team", "local-accounts",
            "thermal-printer-settings", "stock-notifications", "image-editor");

    private record Scene(String file, String title) {
    }

    private static final List<Scene> TOUR = List.of(new Scene("welcome", "Store access"),
            new Scene("overview", "Analytics dashboard"), new Scene("pos-desktop", "Point of sale"),
            new Scene("pos-checkout", "Review & pay"), new Scene("inventory", "Inventory & stock levels"),
            new Scene("thermal-printer-settings", "Thermal printer settings"),
            new Scene("local-accounts", "Accounts & access"));

    private static IIOMetadataNode node(IIOMetadataNode root, String name) {
        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
            if (child.getNodeName().equals(name))
                return (IIOMetadataNode) child;
        IIOMetadataNode created = new IIOMetadataNode(name);
        root.appendChild(created);
        return created;
    }

    private static BufferedImage scene(Path source, Scene scene, int index) throws IOException {
        BufferedImage screenshot = ImageIO.read(source.resolve(scene.file() + ".png").toFile());
        if (screenshot == null)
            throw new IOException("Missing or unreadable screenshot: " + scene.file());
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(new Color(245, 247, 251));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double scale = Math.min((double) WIDTH / screenshot.getWidth(), 600.0 / screenshot.getHeight());
        int w = (int) Math.round(screenshot.getWidth() * scale), h = (int) Math.round(screenshot.getHeight() * scale);
        g.drawImage(screenshot, (WIDTH - w) / 2, (600 - h) / 2, w, h, null);
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 600, WIDTH, 40);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.drawString(String.format("%02d / %02d   %s", index + 1, TOUR.size(), scene.title()), 20, 625);
        g.setColor(new Color(148, 163, 184));
        g.drawString("SPIN LAUNDROMAT  /  DEMO DATA", 686, 625);
        g.dispose();
        return canvas;
    }

    static void frame(ImageWriter writer, BufferedImage image, int delay, boolean first) throws IOException {
        ImageWriteParam parameters = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image),
                parameters);
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode tree = (IIOMetadataNode) metadata.getAsTree(format);
        IIOMetadataNode control = node(tree, "GraphicControlExtension");
        control.setAttribute("disposalMethod", "doNotDispose");
        control.setAttribute("userInputFlag", "FALSE");
        control.setAttribute("transparentColorFlag", "FALSE");
        control.setAttribute("delayTime", Integer.toString(delay));
        control.setAttribute("transparentColorIndex", "0");
        if (first) {
            IIOMetadataNode extension = new IIOMetadataNode("ApplicationExtension");
            extension.setAttribute("applicationID", "NETSCAPE");
            extension.setAttribute("authenticationCode", "2.0");
            extension.setUserObject(new byte[] { 1, 0, 0 });
            node(tree, "ApplicationExtensions").appendChild(extension);
        }
        metadata.setFromTree(format, tree);
        writer.writeToSequence(new IIOImage(image, null, metadata), parameters);
    }

    static void verifyGif(Path file, int expectedFrames) throws IOException {
        try (var input = ImageIO.createImageInputStream(file.toFile())) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            try {
                reader.setInput(input);
                if (reader.getNumImages(true) != expectedFrames || reader.getWidth(0) != WIDTH
                        || reader.getHeight(0) != HEIGHT)
                    throw new IOException("GIF verification failed: " + file);
                for (int i = 0; i < expectedFrames; i++)
                    reader.read(i);
            } finally {
                reader.dispose();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2)
            throw new IllegalArgumentException("Pass the preview folder and documentation image folder.");
        Path source = Path.of(args[0]), output = Path.of(args[1]);
        Files.createDirectories(output);
        for (String name : SCREENSHOTS) {
            Path file = source.resolve(name + ".png");
            if (ImageIO.read(file.toFile()) == null)
                throw new IOException("Invalid screenshot: " + name);
            Files.copy(file, output.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
        }
        var scenes = new java.util.ArrayList<BufferedImage>();
        for (int i = 0; i < TOUR.size(); i++)
            scenes.add(scene(source, TOUR.get(i), i));
        Path gif = output.resolve("system-tour.gif");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(gif.toFile())) {
            writer.setOutput(stream);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < scenes.size(); i++) {
                BufferedImage current = scenes.get(i), next = scenes.get((i + 1) % scenes.size());
                frame(writer, current, 240, i == 0);
                for (int step = 1; step <= 5; step++) {
                    BufferedImage blend = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = blend.createGraphics();
                    g.drawImage(current, 0, 0, null);
                    g.setComposite(AlphaComposite.SrcOver.derive(step / 6f));
                    g.drawImage(next, 0, 0, null);
                    g.dispose();
                    frame(writer, blend, 6, false);
                }
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        verifyGif(gif, TOUR.size() * 6);
        System.out.println("Published " + SCREENSHOTS.size()
                + " screenshots and a verified 42-frame, 18.9-second looping system tour to " + output);
    }
}
