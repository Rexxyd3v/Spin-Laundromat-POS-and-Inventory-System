package util.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Properties;

public final class PosSettings {
    public record Values(String storeName, String address, String phone, String receiptFooter, String accent,
            boolean compact, boolean listView, boolean taxEnabled, BigDecimal taxRate, boolean gcashEnabled,
            int receiptWidth) {
        public Values(String storeName, String address, String phone, String receiptFooter, String accent,
                boolean compact, boolean listView, boolean taxEnabled, BigDecimal taxRate, boolean gcashEnabled) {
            this(storeName, address, phone, receiptFooter, accent, compact, listView, taxEnabled, taxRate, gcashEnabled,
                    80);
        }

        public Values {
            if (receiptWidth != 58 && receiptWidth != 80)
                throw new IllegalArgumentException("Choose 58 or 80 mm receipt paper.");
            if (storeName == null || storeName.isBlank() || storeName.length() > 80)
                throw new IllegalArgumentException("Store name must contain 1–80 characters.");
            if (!java.util.Set.of("Teal", "Blue", "Violet").contains(accent))
                throw new IllegalArgumentException("Choose a supported accent color.");
            if (taxRate == null || taxRate.signum() < 0 || taxRate.compareTo(new BigDecimal("100")) > 0)
                throw new IllegalArgumentException("Tax rate must be between 0 and 100.");
        }
    }

    private static final Path FILE = AtomicProperties.dataPath("pos-settings.properties");
    private static final PropertyChangeSupport EVENTS = new PropertyChangeSupport(PosSettings.class);
    private static String loadError;
    private static Values current = load();

    private PosSettings() {
    }

    public static Values defaults() {
        return new Values("Spin Laundromat", "143 Lamar St., Pasay City", "",
                "Thank you for choosing us. See you next wash!", "Teal", false, false, true, new BigDecimal("12"),
                false);
    }

    public static Values get() {
        return current;
    }

    public static String loadError() {
        return loadError;
    }

    public static void addListener(PropertyChangeListener listener) {
        EVENTS.addPropertyChangeListener(listener);
    }

    public static void removeListener(PropertyChangeListener listener) {
        EVENTS.removePropertyChangeListener(listener);
    }

    private static Values load() {
        Values d = defaults();
        try {
            Properties p = AtomicProperties.read(FILE);
            return new Values(p.getProperty("store.name", d.storeName()), p.getProperty("store.address", d.address()),
                    p.getProperty("store.phone", d.phone()), p.getProperty("receipt.footer", d.receiptFooter()),
                    p.getProperty("appearance.accent", d.accent()),
                    Boolean.parseBoolean(p.getProperty("appearance.compact", "false")),
                    Boolean.parseBoolean(p.getProperty("catalog.listView", "false")),
                    Boolean.parseBoolean(p.getProperty("tax.enabled", "true")),
                    new BigDecimal(p.getProperty("tax.rate", "12")),
                    Boolean.parseBoolean(p.getProperty("payment.gcash", "false")),
                    Integer.parseInt(p.getProperty("receipt.width", "80")));
        } catch (IOException | IllegalArgumentException ex) {
            loadError = "Settings could not be loaded. Defaults are shown; the saved file has not been changed.";
            return d;
        }
    }

    public static void save(Values values) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            security.LocalAuth.require(security.LocalAuth.Permission.SETTINGS);
            Properties p = new Properties();
            p.setProperty("store.name", values.storeName().trim());
            p.setProperty("store.address", values.address());
            p.setProperty("store.phone", values.phone());
            p.setProperty("receipt.footer", values.receiptFooter());
            p.setProperty("appearance.accent", values.accent());
            p.setProperty("appearance.compact", Boolean.toString(values.compact()));
            p.setProperty("catalog.listView", Boolean.toString(values.listView()));
            p.setProperty("tax.enabled", Boolean.toString(values.taxEnabled()));
            p.setProperty("tax.rate", values.taxRate().toPlainString());
            p.setProperty("payment.gcash", Boolean.toString(values.gcashEnabled()));
            p.setProperty("receipt.width", Integer.toString(values.receiptWidth()));
            AtomicProperties.write(FILE, p);
            Values previous = current;
            current = values;
            loadError = null;
            Runnable announce = () -> EVENTS.firePropertyChange("settings", previous, values);
            if (javax.swing.SwingUtilities.isEventDispatchThread())
                announce.run();
            else
                javax.swing.SwingUtilities.invokeLater(announce);

        }
    }
}
