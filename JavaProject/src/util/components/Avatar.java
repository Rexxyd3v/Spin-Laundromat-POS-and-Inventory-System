package util.components;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.*;
import model.*;
import util.config.UIStyle;

public final class Avatar extends JComponent implements Icon {
    private String name = "", request = "";
    private BufferedImage photo;
    private Runnable repaintHost = () -> {
    }, loaded = () -> {
    };

    public Avatar onLoaded(Runnable action) {
        loaded = action;
        return this;
    }

    public int getIconWidth() {
        return getPreferredSize().width;
    }

    public int getIconHeight() {
        return getPreferredSize().height;
    }

    public void paintIcon(Component host, Graphics graphics, int x, int y) {
        repaintHost = host::repaint;
        setSize(getPreferredSize());
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        paintComponent(g);
        g.dispose();
    }

    public Avatar(TeamStore.Member member, int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setPerson(member);
    }

    public void setPerson(TeamStore.Member member) {
        name = member.name();
        request = member.image();
        photo = null;
        setToolTipText(member.name() + " · " + member.role());
        repaint();
        if (request.isBlank())
            return;
        try {
            load(persistence.Repositories.assets().resolve(request), request);
        } catch (Exception ignored) {
        }
    }

    public void preview(java.nio.file.Path file, String displayName) {
        name = displayName;
        photo = null;
        request = file.toAbsolutePath().toString();
        load(file, request);
    }

    private void load(java.nio.file.Path file, String expected) {
        new SwingWorker<BufferedImage, Void>() {
            protected BufferedImage doInBackground() throws Exception {
                BufferedImage source = persistence.Repositories.assets().read(file);
                double scale = Math.min(1.0, 160.0 / Math.max(source.getWidth(), source.getHeight()));
                BufferedImage image = new BufferedImage(Math.max(1, (int) (source.getWidth() * scale)),
                        Math.max(1, (int) (source.getHeight() * scale)), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(source, 0, 0, image.getWidth(), image.getHeight(), null);
                g.dispose();
                return image;
            }

            protected void done() {
                try {
                    BufferedImage image = get();
                    if (expected.equals(request)) {
                        photo = image;
                        repaint();
                        repaintHost.run();
                        loaded.run();
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.min(getWidth(), getHeight());
        if (size < 4) {
            g.dispose();
            return;
        }
        Shape originalClip = g.getClip();
        g.clip(new Ellipse2D.Double(2, 2, size - 4, size - 4));
        if (photo != null) {
            double scale = Math.max((double) size / photo.getWidth(), (double) size / photo.getHeight());
            int w = (int) Math.ceil(photo.getWidth() * scale), h = (int) Math.ceil(photo.getHeight() * scale);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(photo, (size - w) / 2, (size - h) / 2, w, h, null);
        } else {
            g.setColor(UIStyle.PRIMARY);
            g.fillOval(0, 0, size, size);
            String[] parts = name.trim().split("\\s+");
            String initials = parts[0].substring(0, 1)
                    + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "");
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, size / 3)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initials.toUpperCase(), (size - fm.stringWidth(initials.toUpperCase())) / 2,
                    (size + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.setClip(originalClip);
        g.setColor(new Color(184, 197, 210));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Double(0.9, 0.9, size - 1.8, size - 1.8));
        g.dispose();
    }
}
