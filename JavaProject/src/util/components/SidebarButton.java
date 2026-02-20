package src.util.components;

import java.awt.*;
import javax.swing.*;
import src.util.UIStyle;

public class SidebarButton extends JButton {
    private boolean active = false;
    private final IconPainter iconPainter;

    public interface IconPainter {
        void paint(Graphics2D g2, int x, int y, int size, Color color);
    }

    public SidebarButton(String text, IconPainter painter) {
        super(text);
        this.iconPainter = painter;
        setFont(new Font("SansSerif", Font.PLAIN, 13));
        setForeground(new Color(71, 85, 105));
        setBackground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!active)
                    repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!active)
                    repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        if (active) {
            g2.setColor(UIStyle.PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(249, 250, 251));
            g2.fillRoundRect(5, 0, getWidth() - 10, getHeight(), 12, 12);
        }

        // Icon
        if (iconPainter != null) {
            Color iconColor = active ? Color.WHITE : new Color(100, 116, 139);
            iconPainter.paint(g2, 20, (getHeight() - 20) / 2, 20, iconColor);
        }

        // Text
        g2.setColor(active ? Color.WHITE : new Color(71, 85, 105));
        g2.setFont(active ? new Font("SansSerif", Font.BOLD, 13) : new Font("SansSerif", Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        int textX = 55;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }
}
