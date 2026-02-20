package src.util.components;

import java.awt.*;
import javax.swing.*;

public class ShadowPanel extends JPanel {
    private final int shadowSize = 5;
    private final int borderRadius = 15;

    public ShadowPanel() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2,
                borderRadius, borderRadius);

        // Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - shadowSize, getHeight() - shadowSize, borderRadius, borderRadius);

        g2.dispose();
    }

    @Override
    public Insets getInsets() {
        return new Insets(15, 15, 15, 15);
    }
}
