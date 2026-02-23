package src.util.components;

import java.awt.*;
import javax.swing.*;

public class ShadowPanel extends JPanel {
    private final int borderRadius = 20;

    public ShadowPanel() {
        setOpaque(false);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card dimensions
        int margin = 12; // Increased margin for smoother spread
        int cardX = margin;
        int cardY = margin - 4;
        int cardW = w - margin * 2;
        int cardH = h - margin * 2;

        // HIGH DENSITY multi-layered soft shadow for smooth blur effect
        int shadowLayers = 16;
        for (int i = 0; i < shadowLayers; i++) {
            // Quadratic falloff for much smoother transition
            double ratio = (double) i / shadowLayers;
            float alpha = (float) (0.04f * Math.pow(1.0 - ratio, 2.5));
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 255)));

            // Sub-pixel like expansion and downward offset
            int sX = cardX - i / 2;
            int sY = cardY + 4 + i;
            int sW = cardW + i;
            int sH = cardH + i;

            g2.fillRoundRect(sX, sY, sW, sH, borderRadius, borderRadius);
        }

        // Draw the white card on top
        g2.setColor(getBackground());
        g2.fillRoundRect(cardX, cardY, cardW, cardH, borderRadius, borderRadius);

        g2.dispose();
    }

    @Override
    public Insets getInsets() {
        return new Insets(10, 15, 20, 15);
    }
}
