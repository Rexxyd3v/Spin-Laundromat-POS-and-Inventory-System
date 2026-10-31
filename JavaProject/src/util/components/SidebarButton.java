package util.components;

import java.awt.*;
import javax.swing.*;
import util.config.UIStyle;

public class SidebarButton extends JButton {
    private boolean active, compact;
    private final IconPainter painter;

    public interface IconPainter {
        void paint(Graphics2D g, int x, int y, int size, Color color);
    }

    public SidebarButton(String text, IconPainter painter) {
        super(text);
        this.painter = painter;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setPreferredSize(new Dimension(216, 48));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        setAlignmentX(LEFT_ALIGNMENT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(text);
        getAccessibleContext().setAccessibleName(text);
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
        repaint();
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (active || getModel().isRollover()) {
            g.setColor(active ? UIStyle.PRIMARY : new Color(31, 41, 55));
            g.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 12, 12);
        }
        Color ink = active ? Color.WHITE : new Color(203, 213, 225);
        if (painter != null)
            painter.paint(g, compact ? (getWidth() - 20) / 2 : 22, (getHeight() - 20) / 2, 20, ink);
        if (!compact) {
            g.setColor(ink);
            g.setFont(UIStyle.BOLD);
            g.drawString(getText(), 56,
                    (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
        }
        if (isFocusOwner()) {
            g.setColor(Color.WHITE);
            g.drawRoundRect(9, 3, getWidth() - 18, getHeight() - 6, 12, 12);
        }
        g.dispose();
    }
}
