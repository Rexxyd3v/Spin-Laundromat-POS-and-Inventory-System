package ui.frames;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import util.components.MotionPreferences;
import util.config.UIStyle;

/**
 * Crossfades actual screens in one window and blocks duplicate input during the
 * transition.
 */
public final class TransitionPane extends JPanel {
    private final JLayer<JComponent> layer;
    private final Timer timer = new Timer(16, e -> advance());
    private BufferedImage previous;
    private boolean transitioning;
    private long started;
    private float progress = 1;
    private final PropertyChangeListener preferences = e -> {
        if (!MotionPreferences.isEnabled())
            finish();
    };

    public TransitionPane(JComponent initial) {
        super(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        timer.setCoalesce(true);
        layer = new JLayer<>(initial, new LayerUI<JComponent>() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                        | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
            }

            @Override
            public void uninstallUI(JComponent c) {
                ((JLayer<?>) c).setLayerEventMask(0);
                super.uninstallUI(c);
            }

            @Override
            public void eventDispatched(AWTEvent event, JLayer<? extends JComponent> owner) {
                if (transitioning && event instanceof InputEvent input)
                    input.consume();
            }

            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                float eased = progress * progress * (3 - 2 * progress);
                if (previous != null) {
                    g.setComposite(AlphaComposite.SrcOver);
                    g.drawImage(previous, 0, 0, component.getWidth(), component.getHeight(), null);
                }
                g.setComposite(AlphaComposite.SrcOver.derive(eased));
                super.paint(g, component);
                g.dispose();
            }
        });
        add(layer, BorderLayout.CENTER);
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public JComponent currentView() {
        return layer.getView();
    }

    public boolean transitionTo(Supplier<? extends JComponent> next) {
        if (transitioning)
            return false;
        if (MotionPreferences.isEnabled() && getWidth() > 0 && getHeight() > 0) {
            previous = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = previous.createGraphics();
            layer.printAll(g);
            g.dispose();
        }
        JComponent view;
        try {
            view = next.get();
        } catch (RuntimeException ex) {
            previous = null;
            throw ex;
        }
        layer.setView(view);
        revalidate();
        if (previous == null || !MotionPreferences.isEnabled()) {
            finish();
            return true;
        }
        transitioning = true;
        progress = 0;
        started = System.nanoTime();
        timer.start();
        repaint();
        return true;
    }

    private void advance() {
        progress = Math.min(1, (System.nanoTime() - started) / 320_000_000f);
        if (progress >= 1)
            finish();
        else
            repaint();
    }

    private void finish() {
        timer.stop();
        previous = null;
        progress = 1;
        transitioning = false;
        revalidate();
        repaint();
        if (layer.getView() instanceof WelcomePanel welcome)
            welcome.focusInput();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        MotionPreferences.addListener(preferences);
    }

    @Override
    public void removeNotify() {
        finish();
        MotionPreferences.removeListener(preferences);
        super.removeNotify();
    }
}
