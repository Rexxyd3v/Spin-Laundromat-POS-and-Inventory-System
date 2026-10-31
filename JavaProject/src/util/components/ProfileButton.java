package util.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/** Flat profile control: explicit paint states without look-and-feel glow. */
public final class ProfileButton extends JButton {
    public ProfileButton() {
        setUI(new BasicButtonUI());
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
    }

    @Override
    public void updateUI() {
        setUI(new BasicButtonUI());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getModel().isPressed() ? new Color(24, 38, 53)
                : getModel().isRollover() ? new Color(37, 53, 71) : new Color(31, 46, 64);
        g.setColor(fill);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g.setColor(isFocusOwner() ? new Color(164, 185, 205) : new Color(69, 87, 106));
        g.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g.dispose();
        super.paintComponent(graphics);
    }
}
