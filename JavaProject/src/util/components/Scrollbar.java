package util.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class Scrollbar extends BasicScrollBarUI {
    private final int THICKNESS = 6;
    private final Color idle, hover;

    public Scrollbar() {
        this(new Color(203, 213, 225), new Color(148, 163, 184));
    }

    public Scrollbar(Color idle, Color hover) {
        this.idle = idle;
        this.hover = hover;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton jbutton = new JButton();
        jbutton.setPreferredSize(new Dimension(0, 0));
        jbutton.setMinimumSize(new Dimension(0, 0));
        jbutton.setMaximumSize(new Dimension(0, 0));
        return jbutton;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // Track is transparent
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color color = idle;
        if (isDragging) {
            color = hover;
        } else if (isThumbRollover()) {
            color = hover;
        }

        g2.setColor(color);
        int x = thumbBounds.x
                + (scrollbar.getOrientation() == JScrollBar.VERTICAL ? (thumbBounds.width - THICKNESS) / 2 : 0);
        int y = thumbBounds.y
                + (scrollbar.getOrientation() == JScrollBar.VERTICAL ? 0 : (thumbBounds.height - THICKNESS) / 2);
        int width = scrollbar.getOrientation() == JScrollBar.VERTICAL ? THICKNESS : thumbBounds.width;
        int height = scrollbar.getOrientation() == JScrollBar.VERTICAL ? thumbBounds.height : THICKNESS;

        g2.fillRoundRect(x, y, width, height, THICKNESS, THICKNESS);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            d.width = THICKNESS + 4;
        } else {
            d.height = THICKNESS + 4;
        }
        return d;
    }
}
