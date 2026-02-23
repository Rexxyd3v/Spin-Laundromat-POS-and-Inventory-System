package src.util.components;

import java.awt.*;
import java.awt.geom.*;

public class BrandIcons {

    public static final SidebarButton.IconPainter DASHBOARD = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, size, size, 4, 4);
        g2.fillRect(x + 4, y + 4, 4, 4);
        g2.fillRect(x + 12, y + 4, 4, 4);
        g2.fillRect(x + 4, y + 12, 4, 4);
    };

    public static final SidebarButton.IconPainter BRAND_WASH = fromImage("/src/resources/images/machine_wash.png");

    public static final SidebarButton.IconPainter POS = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        // Cart body
        g2.draw(new Path2D.Double() {
            {
                moveTo(x, y);
                lineTo(x + 4, y);
                lineTo(x + 6, y + 12);
                lineTo(x + 16, y + 12);
                lineTo(x + 18, y + 4);
                lineTo(x + 5, y + 4);
            }
        });
        // Wheels
        g2.fillOval(x + 6, y + 14, 4, 4);
        g2.fillOval(x + 14, y + 14, 4, 4);
    };

    public static final SidebarButton.IconPainter INVENTORY = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y + 4, size, size - 4);
        g2.drawLine(x, y + 4, x + size / 2, y);
        g2.drawLine(x + size, y + 4, x + size / 2, y);
        g2.drawLine(x + size / 2, y, x + size / 2, y + size);
    };

    public static final SidebarButton.IconPainter REPORTS = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x, y + size, x + size, y + size); // Base
        g2.draw(new Path2D.Double() {
            {
                moveTo(x, y + size - 4);
                lineTo(x + size / 3, y + size / 2);
                lineTo(x + 2 * size / 3, y + size / 3);
                lineTo(x + size, y);
            }
        });
    };

    public static final SidebarButton.IconPainter USERS = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x + size / 4, y, size / 2, size / 2);
        g2.drawArc(x, y + size / 2, size, size, 0, 180);
    };

    public static final SidebarButton.IconPainter SETTINGS = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x + size / 4, y + size / 4, size / 2, size / 2);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x1 = (int) (x + size / 2 + Math.cos(angle) * (size / 2 - 2));
            int y1 = (int) (y + size / 2 + Math.sin(angle) * (size / 2 - 2));
            int x2 = (int) (x + size / 2 + Math.cos(angle) * (size / 2));
            int y2 = (int) (y + size / 2 + Math.sin(angle) * (size / 2));
            g2.drawLine(x1, y1, x2, y2);
        }
    };

    public static final SidebarButton.IconPainter LOGOUT = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, size - 6, size);
        g2.drawLine(x + size - 8, y + size / 2, x + size, y + size / 2);
        g2.drawLine(x + size - 4, y + size / 2 - 4, x + size, y + size / 2);
        g2.drawLine(x + size - 4, y + size / 2 + 4, x + size, y + size / 2);
    };

    // Revenue Icon for Stat Cards
    public static final SidebarButton.IconPainter REVENUE = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y + 2, size, size - 4, 4, 4);
        g2.drawOval(x + size / 2 - 4, y + size / 2 - 2, 8, 8);
    };

    public static final SidebarButton.IconPainter NOTIFICATION = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawArc(x + 2, y, size - 4, size, 0, 180);
        g2.drawLine(x, y + size - 4, x + size, y + size - 4);
        g2.fillOval(x + size / 2 - 3, y + size - 3, 6, 4);
    };

    public static final SidebarButton.IconPainter CHECK = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 4, y + size / 2 + 2, x + size / 2 - 2, y + size - 4);
        g2.drawLine(x + size / 2 - 2, y + size - 4, x + size - 4, y + 4);
    };

    public static final SidebarButton.IconPainter STORE = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 2, y + 6, size - 4, size - 8, 3, 3);
        g2.drawArc(x + size / 2 - 4, y + 2, 8, 8, 0, 180);
    };

    public static final SidebarButton.IconPainter RECEIPT = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x + 2, y + 2, size - 4, size - 4);
        g2.drawLine(x + 5, y + 6, x + size - 5, y + 6);
        g2.drawLine(x + 5, y + 10, x + size - 5, y + 10);
        g2.drawLine(x + 5, y + 14, x + size - 5, y + 14);
    };

    public static final SidebarButton.IconPainter SECURITY = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        Path2D shield = new Path2D.Double();
        shield.moveTo(x + size / 2, y + 2);
        shield.lineTo(x + size - 3, y + 5);
        shield.lineTo(x + size - 3, y + size / 2);
        shield.quadTo(x + size - 3, y + size - 2, x + size / 2, y + size - 2);
        shield.quadTo(x + 3, y + size - 2, x + 3, y + size / 2);
        shield.lineTo(x + 3, y + 5);
        shield.closePath();
        g2.draw(shield);
    };

    public static final SidebarButton.IconPainter ALERTS = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 2, y + 2, size - 4, size - 4);
        g2.fillRect(x + size / 2 - 1, y + 6, 2, 6);
        g2.fillRect(x + size / 2 - 1, y + 13, 2, 2);
    };

    public static final SidebarButton.IconPainter BACKUP = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        // Simple Archive box icon
        g2.drawRect(x + 2, y + 6, size - 4, size - 8);
        g2.drawRect(x + 1, y + 3, size - 2, 3); // Lid
        g2.drawLine(x + size / 2 - 3, y + 10, x + size / 2 + 3, y + 10); // Handle
    };

    public static final SidebarButton.IconPainter TOGGLE_OFF = (g2, x, y, size, color) -> {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(203, 213, 225)); // Slate 300
        g2.fillRoundRect(x, y, size * 2, size, size, size);
        g2.setColor(Color.WHITE);
        int dotSize = (int) (size * 0.75);
        int dotOffset = (size - dotSize) / 2;
        g2.fillOval(x + dotOffset, y + dotOffset, dotSize, dotSize);
    };

    public static final SidebarButton.IconPainter TOGGLE_ON = (g2, x, y, size, color) -> {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color != null ? color : new Color(74, 57, 51)); // Brand Brown
        g2.fillRoundRect(x, y, size * 2, size, size, size);
        g2.setColor(Color.WHITE);
        int dotSize = (int) (size * 0.75);
        int dotOffset = (size - dotSize) / 2;
        g2.fillOval(x + size + dotOffset, y + dotOffset, dotSize, dotSize);
    };

    public static final SidebarButton.IconPainter UPLOAD = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x + size / 2, y + 2, x + size / 2, y + size - 8);
        g2.drawLine(x + size / 2, y + 2, x + size / 2 - 4, y + 6);
        g2.drawLine(x + size / 2, y + 2, x + size / 2 + 4, y + 6);
        g2.drawLine(x + 2, y + size - 4, x + size - 2, y + size - 4);
    };

    public static final SidebarButton.IconPainter DOWNLOAD = (g2, x, y, size, color) -> {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x + size / 2, y + 2, x + size / 2, y + size - 8);
        g2.drawLine(x + size / 2, y + size - 8, x + size / 2 - 4, y + size - 12);
        g2.drawLine(x + size / 2, y + size - 8, x + size / 2 + 4, y + size - 12);
        g2.drawLine(x + 2, y + size - 4, x + size - 2, y + size - 4);
    };

    public static SidebarButton.IconPainter fromImage(String resourcePath) {
        return (g2, x, y, size, color) -> {
            try {
                String[] attempts = {
                        resourcePath,
                        resourcePath.startsWith("/src") ? resourcePath.substring(4) : "/src" + resourcePath,
                        resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath
                };

                java.net.URL imgUrl = null;
                for (String path : attempts) {
                    imgUrl = BrandIcons.class.getResource(path);
                    if (imgUrl != null)
                        break;

                    // Also try classloader directly (usually expects no leading slash)
                    String noSlash = path.startsWith("/") ? path.substring(1) : path;
                    imgUrl = BrandIcons.class.getClassLoader().getResource(noSlash);
                    if (imgUrl != null)
                        break;
                }

                if (imgUrl != null) {
                    Image img = javax.imageio.ImageIO.read(imgUrl);
                    if (img != null) {
                        g2.drawImage(img, x, y, size, size, null);
                    }
                }
            } catch (Exception e) {
            }
        };
    }

    public static final SidebarButton.IconPainter STORE_IMG = fromImage("/src/resources/images/store.png");
    public static final SidebarButton.IconPainter SYSTEM_IMG = fromImage("/src/resources/images/system.png");
    public static final SidebarButton.IconPainter RECEIPT_IMG = fromImage("/src/resources/images/reciept.png");
    public static final SidebarButton.IconPainter SECURITY_IMG = fromImage("/src/resources/images/security.png");
    public static final SidebarButton.IconPainter ALERTS_IMG = fromImage("/src/resources/images/alerts.png");
    public static final SidebarButton.IconPainter BACKUP_IMG = fromImage("/src/resources/images/backup.png");
}
