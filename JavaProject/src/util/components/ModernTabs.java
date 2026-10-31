package util.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import util.config.UIStyle;

/**
 * Flat, bordered tabs with the normal Swing keyboard and accessibility
 * behavior.
 */
public final class ModernTabs extends JTabbedPane {
    public ModernTabs() {
        setFont(UIStyle.BOLD);
        setForeground(UIStyle.TEXT_PRIMARY);
        setOpaque(false);
        setUI(new BasicTabbedPaneUI() {
            protected Insets getTabInsets(int placement, int index) {
                return new Insets(10, 18, 10, 18);
            }

            protected Insets getContentBorderInsets(int placement) {
                return new Insets(12, 0, 0, 0);
            }

            protected Insets getTabAreaInsets(int placement) {
                return new Insets(0, 0, 0, 0);
            }

            protected void paintContentBorder(Graphics g, int placement, int selected) {
            }

            protected void paintTabBackground(Graphics graphics, int placement, int index, int x, int y, int w, int h,
                    boolean selected) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(selected ? UIStyle.tint() : Color.WHITE);
                g.fillRoundRect(x + 1, y + 1, w - 3, h - 3, 10, 10);
                g.dispose();
            }

            protected void paintTabBorder(Graphics graphics, int placement, int index, int x, int y, int w, int h,
                    boolean selected) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(selected ? UIStyle.PRIMARY : UIStyle.INPUT_BORDER);
                g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 10, 10);
                g.dispose();
            }
        });
    }
}
