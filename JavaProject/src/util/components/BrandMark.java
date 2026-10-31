package util.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import util.config.UIStyle;

/**
 * Shared generated mascot, loaded from packaged resources with its alpha
 * intact.
 */
public final class BrandMark extends JComponent implements Icon {
    private static final BufferedImage ARTWORK = loadArtwork();
    private final int size;

    public BrandMark(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);
        getAccessibleContext().setAccessibleName("Spin Laundromat cartoon washing-machine mascot");
    }

    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null)
            accessibleContext = new AccessibleJComponent() {
            };
        return accessibleContext;
    }

    private static BufferedImage loadArtwork() {
        try {
            java.net.URL resource = BrandMark.class.getResource("/resources/brand/laundry-mascot-v1.png");
            return resource == null ? null : ImageIO.read(resource);
        } catch (java.io.IOException ex) {
            return null;
        }
    }

    public static boolean isArtworkAvailable() {
        return ARTWORK != null;
    }

    public static boolean hasTransparentArtwork() {
        return ARTWORK != null && ARTWORK.getColorModel().hasAlpha();
    }

    public int getIconWidth() {
        return size;
    }

    public int getIconHeight() {
        return size;
    }

    public static void draw(Graphics2D source, int x, int y, int width, int height) {
        if (ARTWORK == null) {
            AppIcon.draw(AppIcon.Name.WASH, source, x, y, Math.min(width, height), UIStyle.PRIMARY);
            return;
        }
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.min((double) width / ARTWORK.getWidth(), (double) height / ARTWORK.getHeight());
        int w = (int) Math.round(ARTWORK.getWidth() * scale), h = (int) Math.round(ARTWORK.getHeight() * scale);
        g.drawImage(ARTWORK, x + (width - w) / 2, y + (height - h) / 2, w, h, null);
        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        draw((Graphics2D) g, 0, 0, getWidth(), getHeight());
    }

    public void paintIcon(Component host, Graphics g, int x, int y) {
        draw((Graphics2D) g, x, y, size, size);
    }

    public static List<Image> windowIcons() {
        return java.util.stream.IntStream.of(32, 48, 64, 128, 256).mapToObj(size -> {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            draw(g, 0, 0, size, size);
            g.dispose();
            return (Image) image;
        }).toList();
    }
}
