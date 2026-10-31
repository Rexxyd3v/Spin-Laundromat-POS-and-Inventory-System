import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import org.w3c.dom.Node;
import ui.frames.*;
import util.components.*;
import util.config.*;

/**
 * Short animated recording of the actual login components, rendered without
 * showing a window.
 */
public class LoginAnimationPreview {
    private static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layout(next);
    }

    private static IIOMetadataNode node(IIOMetadataNode root, String name) {
        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
            if (child.getNodeName().equals(name))
                return (IIOMetadataNode) child;
        IIOMetadataNode created = new IIOMetadataNode(name);
        root.appendChild(created);
        return created;
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        Files.createDirectories(output);
        System.setProperty("laundry.dataDir", Files.createTempDirectory("login-animation-preview-").toString());
        TransitionPane[] scene = { null };
        SwingUtilities.invokeAndWait(() -> {
            UIStyle.installDefaults();
            MotionPreferences.setEnabled(true);
            scene[0] = new TransitionPane(new WelcomePanel(false, () -> {
            }));
            scene[0].setSize(1200, 800);
            for (int i = 0; i < 4; i++)
                layout(scene[0]);
        });
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream stream = ImageIO
                .createImageOutputStream(output.resolve("login-animation.gif").toFile())) {
            writer.setOutput(stream);
            writer.prepareWriteSequence(null);
            for (int frame = 0; frame < 38; frame++) {
                if (frame == 18)
                    SwingUtilities.invokeAndWait(() -> scene[0].transitionTo(() -> new WelcomePanel(true, () -> {
                    })));
                BufferedImage[] captured = { null };
                SwingUtilities.invokeAndWait(() -> {
                    for (int i = 0; i < 3; i++)
                        layout(scene[0]);
                    BufferedImage image = new BufferedImage(960, 640, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = image.createGraphics();
                    g.scale(.8, .8);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    scene[0].printAll(g);
                    g.dispose();
                    captured[0] = image;
                });
                ImageWriteParam parameters = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer
                        .getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(captured[0]), parameters);
                String format = metadata.getNativeMetadataFormatName();
                IIOMetadataNode tree = (IIOMetadataNode) metadata.getAsTree(format);
                IIOMetadataNode control = node(tree, "GraphicControlExtension");
                control.setAttribute("disposalMethod", "none");
                control.setAttribute("userInputFlag", "FALSE");
                control.setAttribute("transparentColorFlag", "FALSE");
                control.setAttribute("delayTime", "8");
                control.setAttribute("transparentColorIndex", "0");
                if (frame == 0) {
                    IIOMetadataNode extension = new IIOMetadataNode("ApplicationExtension");
                    extension.setAttribute("applicationID", "NETSCAPE");
                    extension.setAttribute("authenticationCode", "2.0");
                    extension.setUserObject(new byte[] { 1, 0, 0 });
                    node(tree, "ApplicationExtensions").appendChild(extension);
                }
                metadata.setFromTree(format, tree);
                writer.writeToSequence(new IIOImage(captured[0], null, metadata), parameters);
                Thread.sleep(65);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        System.out.println("Rendered 38 animated login frames to " + output.resolve("login-animation.gif"));
    }
}
