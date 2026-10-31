package util.components;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import util.config.UIStyle;

/** Scalable application icons; independent of fonts and image files. */
public final class AppIcon implements Icon {
    public enum Name {
        DASHBOARD, POS, BOX, CHART, USERS, SETTINGS, LOGOUT, SEARCH, PLUS, MINUS, CLOSE, TRASH, EDIT, CHECK, BELL,
        RECEIPT, PRINT, WASH, DRY, FOLD, CARD, CASH, ARROW, BACK, GRID, LIST, STORE, SHIELD, DOWNLOAD, UPLOAD, WARNING,
        DOT, CHEVRON, TOGGLE_ON, TOGGLE_OFF, REFRESH, EYE, EYE_OFF
    }

    private final Name name;
    private final int size;
    private final Color color;

    public AppIcon(Name name, int size) {
        this(name, size, null);
    }

    public AppIcon(Name name, int size, Color color) {
        this.name = name;
        this.size = size;
        this.color = color;
    }

    public int getIconWidth() {
        return size;
    }

    public int getIconHeight() {
        return size;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        draw(name, (Graphics2D) g, x, y, size,
                color != null ? color : c != null && c.isEnabled() ? c.getForeground() : UIStyle.TEXT_SECONDARY);
    }

    public static void draw(Name name, Graphics2D source, int x, int y, int size, Color color) {
        Graphics2D g = (Graphics2D) source.create();
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color == null ? UIStyle.PRIMARY : color);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (name) {
        case EYE, EYE_OFF -> {
            Path2D eye = new Path2D.Double();
            eye.moveTo(2, 12);
            eye.curveTo(7, 4, 17, 4, 22, 12);
            eye.curveTo(17, 20, 7, 20, 2, 12);
            g.draw(eye);
            g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            if (name == Name.EYE_OFF)
                line(g, 3, 3, 21, 21);
        }
        case SEARCH -> {
            g.draw(new Ellipse2D.Double(3, 3, 12, 12));
            line(g, 13, 13, 21, 21);
        }
        case PLUS -> {
            line(g, 5, 12, 19, 12);
            line(g, 12, 5, 12, 19);
        }
        case MINUS -> line(g, 5, 12, 19, 12);
        case CLOSE -> {
            line(g, 6, 6, 18, 18);
            line(g, 18, 6, 6, 18);
        }
        case CHECK -> path(g, 4, 12, 9, 17, 20, 6);
        case CHEVRON -> path(g, 6, 9, 12, 15, 18, 9);
        case REFRESH -> {
            g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 285, Arc2D.OPEN));
            path(g, 16, 3, 21, 4, 20, 9);
        }
        case ARROW -> {
            line(g, 4, 12, 20, 12);
            path(g, 14, 6, 20, 12, 14, 18);
        }
        case BACK -> {
            line(g, 4, 12, 20, 12);
            path(g, 10, 6, 4, 12, 10, 18);
        }
        case GRID, DASHBOARD -> {
            for (int a : new int[] { 3, 14 })
                for (int b : new int[] { 3, 14 })
                    rect(g, a, b, 7, 7, 2);
        }
        case LIST -> {
            for (int yy : new int[] { 5, 12, 19 }) {
                line(g, 8, yy, 21, yy);
                g.fill(new Ellipse2D.Double(2, yy - 1, 2, 2));
            }
        }
        case BOX -> {
            path(g, 3, 7, 12, 2, 21, 7, 21, 17, 12, 22, 3, 17, 3, 7, 12, 12, 21, 7);
            line(g, 12, 12, 12, 22);
            line(g, 7, 5, 16, 10);
        }
        case WASH -> {
            rect(g, 3, 2, 18, 20, 3);
            line(g, 3, 7, 21, 7);
            g.draw(new Ellipse2D.Double(7, 10, 10, 10));
            path(g, 8, 15, 11, 13, 14, 16, 17, 14);
            g.fillOval(6, 4, 1, 1);
        }
        case DRY -> {
            g.draw(new Ellipse2D.Double(7, 7, 10, 10));
            for (int a = 0; a < 8; a++) {
                double r = a * Math.PI / 4;
                line(g, 12 + 8 * Math.cos(r), 12 + 8 * Math.sin(r), 12 + 10 * Math.cos(r), 12 + 10 * Math.sin(r));
            }
        }
        case FOLD -> path(g, 3, 7, 8, 3, 12, 5, 16, 3, 21, 7, 18, 11, 16, 9, 16, 21, 8, 21, 8, 9, 6, 11, 3, 7);
        case CARD, POS -> {
            rect(g, 2, 4, 20, 16, 3);
            line(g, 2, 9, 22, 9);
            line(g, 6, 15, 10, 15);
        }
        case CASH -> {
            rect(g, 2, 5, 20, 14, 2);
            g.draw(new Ellipse2D.Double(9, 8, 6, 8));
            line(g, 5, 10, 5, 14);
            line(g, 19, 10, 19, 14);
        }
        case CHART -> {
            path(g, 3, 3, 3, 21, 21, 21);
            path(g, 6, 15, 11, 10, 15, 13, 21, 5);
        }
        case USERS -> {
            g.draw(new Ellipse2D.Double(7, 3, 8, 8));
            g.draw(new Arc2D.Double(3, 13, 16, 14, 0, 180, Arc2D.OPEN));
            g.draw(new Arc2D.Double(14, 4, 7, 7, -90, 180, Arc2D.OPEN));
            g.draw(new Arc2D.Double(16, 13, 7, 12, -5, 90, Arc2D.OPEN));
        }
        case SETTINGS -> {
            g.draw(new Ellipse2D.Double(5, 5, 14, 14));
            g.draw(new Ellipse2D.Double(9, 9, 6, 6));
            for (int a = 0; a < 8; a++) {
                double r = a * Math.PI / 4;
                line(g, 12 + 7 * Math.cos(r), 12 + 7 * Math.sin(r), 12 + 10 * Math.cos(r), 12 + 10 * Math.sin(r));
            }
        }
        case LOGOUT -> {
            path(g, 10, 3, 3, 3, 3, 21, 10, 21);
            line(g, 9, 12, 21, 12);
            path(g, 16, 7, 21, 12, 16, 17);
        }
        case TRASH -> {
            line(g, 3, 6, 21, 6);
            path(g, 8, 6, 8, 3, 16, 3, 16, 6);
            path(g, 5, 6, 6, 21, 18, 21, 19, 6);
            line(g, 10, 10, 10, 17);
            line(g, 14, 10, 14, 17);
        }
        case EDIT -> {
            path(g, 4, 16, 15, 5, 19, 9, 8, 20, 3, 21, 4, 16);
            path(g, 15, 5, 18, 2, 22, 6, 19, 9);
        }
        case BELL -> {
            path(g, 4, 17, 6, 14, 6, 9);
            g.draw(new Arc2D.Double(6, 3, 12, 12, 0, 180, Arc2D.OPEN));
            path(g, 18, 9, 18, 14, 20, 17, 4, 17);
            g.draw(new Arc2D.Double(9, 17, 6, 5, 180, 180, Arc2D.OPEN));
        }
        case RECEIPT -> {
            path(g, 5, 2, 19, 2, 19, 22, 16, 20, 12, 22, 8, 20, 5, 22, 5, 2);
            line(g, 8, 7, 16, 7);
            line(g, 8, 11, 16, 11);
            line(g, 8, 15, 13, 15);
        }
        case PRINT -> {
            rect(g, 2, 8, 20, 10, 2);
            path(g, 6, 8, 6, 2, 18, 2, 18, 8);
            rect(g, 6, 14, 12, 8, 1);
        }
        case STORE -> {
            rect(g, 4, 9, 16, 13, 1);
            path(g, 2, 9, 5, 2, 19, 2, 22, 9, 2, 9);
            rect(g, 9, 14, 6, 8, 1);
        }
        case SHIELD -> {
            path(g, 12, 2, 21, 6, 19, 16, 12, 22, 5, 16, 3, 6, 12, 2);
            path(g, 8, 11, 11, 14, 16, 8);
        }
        case DOWNLOAD, UPLOAD -> {
            path(g, 3, 16, 3, 21, 21, 21, 21, 16);
            line(g, 12, 3, 12, 16);
            if (name == Name.DOWNLOAD)
                path(g, 7, 11, 12, 16, 17, 11);
            else
                path(g, 7, 8, 12, 3, 17, 8);
        }
        case WARNING -> {
            path(g, 12, 2, 23, 21, 1, 21, 12, 2);
            line(g, 12, 8, 12, 13);
            g.fillOval(11, 16, 2, 2);
        }
        case DOT -> g.fill(new Ellipse2D.Double(8, 8, 8, 8));
        case TOGGLE_ON, TOGGLE_OFF -> {
            rect(g, 1, 6, 22, 12, 12);
            g.fill(new Ellipse2D.Double(name == Name.TOGGLE_ON ? 14 : 4, 9, 6, 6));
        }
        }
        g.dispose();
    }

    private static void line(Graphics2D g, double x, double y, double xx, double yy) {
        g.draw(new Line2D.Double(x, y, xx, yy));
    }

    private static void rect(Graphics2D g, double x, double y, double w, double h, double r) {
        g.draw(new RoundRectangle2D.Double(x, y, w, h, r, r));
    }

    private static void path(Graphics2D g, double... points) {
        Path2D p = new Path2D.Double();
        p.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2)
            p.lineTo(points[i], points[i + 1]);
        g.draw(p);
    }
}
