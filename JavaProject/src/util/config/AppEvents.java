package util.config;

import java.beans.*;
import javax.swing.SwingUtilities;

/** Application data changes are delivered on the Swing UI thread. */
public final class AppEvents {
    private static final PropertyChangeSupport EVENTS = new PropertyChangeSupport(AppEvents.class);

    public static void add(PropertyChangeListener listener) {
        EVENTS.addPropertyChangeListener(listener);
    }

    public static void remove(PropertyChangeListener listener) {
        EVENTS.removePropertyChangeListener(listener);
    }

    public static void fire(String topic) {
        Runnable publish = () -> EVENTS.firePropertyChange(topic, null, Boolean.TRUE);
        if (SwingUtilities.isEventDispatchThread())
            publish.run();
        else
            SwingUtilities.invokeLater(publish);
    }

    private AppEvents() {
    }
}
