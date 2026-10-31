package util.components;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import model.*;
import util.config.UIStyle;

/**
 * Async photo loading and aspect-preserving cover rendering, with an
 * illustrated fallback.
 */
public class ItemPhoto extends JComponent {
    private static final Map<String, BufferedImage> CACHE = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> entry) {
            return size() > 48;
        }
    });
    private BufferedImage image;
    private final boolean product;
    private String request;
    private Runnable loaded = () -> {
    };

    public ItemPhoto onLoaded(Runnable action) {
        loaded = action;
        return this;
    }

    public ItemPhoto(CatalogItem item) {
        this(item.product());
        if (!item.image().isBlank()) {
            try {
                load(persistence.Repositories.assets().resolve(item.image()));
            } catch (Exception ignored) {
            }
        }
    }

    public ItemPhoto(boolean product) {
        this.product = product;
        setPreferredSize(new Dimension(200, 140));
        setMinimumSize(new Dimension(40, 40));
    }

    public void load(Path path) {
        String key = path.toAbsolutePath().toString();
        request = key;
        image = CACHE.get(key);
        if (image != null) {
            repaint();
            return;
        }
        new SwingWorker<BufferedImage, Void>() {
            protected BufferedImage doInBackground() throws Exception {
                BufferedImage original = persistence.Repositories.assets().read(path);
                double factor = Math.min(1.0, 720.0 / Math.max(original.getWidth(), original.getHeight()));
                int width = Math.max(1, (int) (original.getWidth() * factor));
                int height = Math.max(1, (int) (original.getHeight() * factor));
                BufferedImage thumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = thumb.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(original, 0, 0, width, height, null);
                g.dispose();
                return thumb;
            }

            protected void done() {
                try {
                    BufferedImage result = get();
                    CACHE.put(key, result);
                    if (key.equals(request)) {
                        image = result;
                        repaint();
                        loaded.run();
                    }
                } catch (Exception ignored) {
                    if (key.equals(request)) {
                        image = null;
                        repaint();
                    }
                }
            }
        }.execute();
    }

    public void clear() {
        request = null;
        image = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.clip(new RoundRectangle2D.Double(0, 0, w, h, 16, 16));
        if (image != null) {
            double scale = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
            int iw = (int) Math.ceil(image.getWidth() * scale), ih = (int) Math.ceil(image.getHeight() * scale);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        } else {
            Color light = product ? new Color(238, 237, 253) : new Color(223, 242, 239),
                    dark = product ? new Color(210, 205, 246) : new Color(181, 223, 216);
            g.setPaint(new GradientPaint(0, 0, light, w, h, Color.WHITE));
            g.fillRect(0, 0, w, h);
            g.setColor(dark);
            g.fillOval(w * 2 / 3, -h / 2, h, h);
            g.setColor(new Color(255, 255, 255, 120));
            g.fillOval(-h / 2, h / 3, h, h);
            int size = Math.min(w, h) * 3 / 5, x = (w - size) / 2, y = (h - size) / 2;
            Color ink = product ? new Color(109, 93, 173) : UIStyle.PRIMARY;
            if (product) {
                g.setColor(ink);
                g.fillRoundRect(x + size / 4, y + size / 4, size / 2, size * 2 / 3, 12, 12);
                g.fillRoundRect(x + size / 3, y + size / 10, size / 3, size / 4, 4, 4);
                g.setColor(Color.WHITE);
                g.fillRoundRect(x + size / 3, y + size / 2, size / 3, size / 4, 5, 5);
            } else
                AppIcon.draw(AppIcon.Name.WASH, g, x, y, size, ink);
        }
        g.dispose();
    }
}
