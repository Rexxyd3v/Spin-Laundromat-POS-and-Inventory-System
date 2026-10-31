package util.components;

import java.awt.*;
import java.beans.*;

/**
 * Session motion setting honors the OS preference and the reduced-motion JVM
 * flag.
 */
public final class MotionPreferences {
    private static long resumedAt = System.nanoTime();
    private static double accumulated;
    private static boolean enabled = initialPreference();
    private static final PropertyChangeSupport CHANGES = new PropertyChangeSupport(MotionPreferences.class);

    private static boolean initialPreference() {
        if (Boolean.getBoolean("laundry.reduceMotion"))
            return false;
        if (GraphicsEnvironment.isHeadless())
            return true;
        return !Boolean.FALSE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("win.clientAreaAnimation"));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static double seconds() {
        return accumulated + (enabled ? (System.nanoTime() - resumedAt) / 1_000_000_000.0 : 0);
    }

    public static void setEnabled(boolean value) {
        if (enabled == value)
            return;
        if (enabled)
            accumulated += (System.nanoTime() - resumedAt) / 1_000_000_000.0;
        else
            resumedAt = System.nanoTime();
        boolean old = enabled;
        enabled = value;
        CHANGES.firePropertyChange("motion", old, value);
    }

    public static void addListener(PropertyChangeListener listener) {
        CHANGES.addPropertyChangeListener(listener);
    }

    public static void removeListener(PropertyChangeListener listener) {
        CHANGES.removePropertyChangeListener(listener);
    }

    private MotionPreferences() {
    }
}
