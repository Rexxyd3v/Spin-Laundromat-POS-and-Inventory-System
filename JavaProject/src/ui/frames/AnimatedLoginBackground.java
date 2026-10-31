package ui.frames;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.*;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import util.components.MotionPreferences;

/**
 * Slow bubbles and laundry-water waves; no image regeneration or network work
 * per frame.
 */
public final class AnimatedLoginBackground extends JPanel {
    private final Timer timer = new Timer(16, e -> repaint());
    private final PropertyChangeListener motionChanged = e -> {
        updateTimer();
        repaint();
    };

    public AnimatedLoginBackground() {
        super(new BorderLayout());
        setOpaque(true);
        timer.setCoalesce(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                updateTimer();
        });
    }

    public double animationSeconds() {
        return MotionPreferences.seconds();
    }

    public boolean isAnimationRunning() {
        return timer.isRunning();
    }

    private void updateTimer() {
        if (isShowing() && MotionPreferences.isEnabled())
            timer.start();
        else
            timer.stop();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        MotionPreferences.addListener(motionChanged);
        updateTimer();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        MotionPreferences.removeListener(motionChanged);
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        paintFrame((Graphics2D) graphics, animationSeconds());
    }

    public void paintFrame(Graphics2D source, double seconds) {
        Graphics2D g = (Graphics2D) source.create();
        int width = getWidth(), height = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(14, 30, 46), width, height, new Color(13, 77, 75)));
        g.fillRect(0, 0, width, height);
        for (int layer = 0; layer < 3; layer++) {
            Path2D wave = new Path2D.Double();
            double baseline = height * (0.74 + layer * 0.075);
            wave.moveTo(0, height);
            wave.lineTo(0, baseline);
            for (int x = 0; x <= width + 24; x += 24) {
                double y = baseline
                        + Math.sin(x / 150.0 + seconds * (0.16 + layer * 0.025) + layer * 1.6) * (20 + layer * 8);
                wave.lineTo(x, y);
            }
            wave.lineTo(width, height);
            wave.closePath();
            g.setColor(new Color(94, 190, 172, 9 + layer * 5));
            g.fill(wave);
        }
        for (int i = 0; i < 16; i++) {
            double radius = 5 + (i * 13 % 31), duration = height + radius * 4;
            double x = width * ((i * 0.61803398875 + 0.12) % 1) + Math.sin(seconds * 0.15 + i) * 12;
            double y = height + radius * 2 - ((seconds * (5 + i % 5 * 1.3) + i * 97) % Math.max(1, duration));
            g.setStroke(new BasicStroke(1.2f));
            g.setColor(i % 5 == 0 ? new Color(244, 197, 66, 55) : new Color(209, 250, 241, 22 + i % 4 * 7));
            g.draw(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
            g.setColor(new Color(235, 255, 251, 38));
            g.draw(new Arc2D.Double(x - radius * 0.68, y - radius * 0.68, radius * 1.36, radius * 1.36, 65, 48,
                    Arc2D.OPEN));
        }
        g.dispose();
    }
}
