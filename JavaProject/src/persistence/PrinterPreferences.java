package persistence;

import java.io.IOException;
import java.util.Properties;
import util.config.AtomicProperties;

/**
 * Device printing configuration is local, separate from organization/store
 * settings.
 */
public final class PrinterPreferences {
    public record Config(String printerName, int paperWidth, double leftMm, double rightMm, double topMm,
            double bottomMm, double feedMm, int fontSize, boolean showDialog) {
        public Config {
            if (printerName == null || printerName.length() > 250 || printerName.indexOf('\n') >= 0
                    || printerName.indexOf('\r') >= 0)
                throw new IllegalArgumentException("Choose a valid printer name.");
            if (paperWidth != 58 && paperWidth != 80)
                throw new IllegalArgumentException("Choose 58 or 80 mm paper.");
            for (double margin : new double[] { leftMm, rightMm, topMm, bottomMm })
                if (!Double.isFinite(margin) || margin < 0 || margin > 12)
                    throw new IllegalArgumentException("Margins must be between 0 and 12 mm.");
            if (paperWidth - leftMm - rightMm < 30)
                throw new IllegalArgumentException("Keep at least 30 mm of printable width.");
            if (!Double.isFinite(feedMm) || feedMm < 0 || feedMm > 30)
                throw new IllegalArgumentException("Extra paper feed must be between 0 and 30 mm.");
            if (fontSize < 6 || fontSize > 14)
                throw new IllegalArgumentException("Choose a font size from 6 to 14 pt.");
        }

        public Config withWidth(int width) {
            return new Config(printerName, width, leftMm, rightMm, topMm, bottomMm, feedMm, fontSize, showDialog);
        }
    }

    public static Config defaults(int width) {
        return new Config("", width, 3, 3, 3, 3, 6, width == 58 ? 8 : 9, true);
    }

    public static Config load() throws IOException {
        var p = AtomicProperties.read(DataLocation.resolve("printer.properties"));
        try {
            int width = Integer.parseInt(
                    p.getProperty("paper.width", Integer.toString(Repositories.settings().get().receiptWidth())));
            var d = defaults(width);
            return new Config(p.getProperty("printer.name", ""), width,
                    Double.parseDouble(p.getProperty("margin.left", "3")),
                    Double.parseDouble(p.getProperty("margin.right", "3")),
                    Double.parseDouble(p.getProperty("margin.top", "3")),
                    Double.parseDouble(p.getProperty("margin.bottom", "3")),
                    Double.parseDouble(p.getProperty("paper.feed", "6")),
                    Integer.parseInt(p.getProperty("font.size", Integer.toString(d.fontSize()))),
                    Boolean.parseBoolean(p.getProperty("print.dialog", "true")));
        } catch (RuntimeException ex) {
            throw new IOException("Saved printer settings need attention. Review Receipt printer settings.", ex);
        }
    }

    public static void save(Config value) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            security.LocalAuth.current();
            Properties p = new Properties();
            p.setProperty("printer.name", value.printerName());
            p.setProperty("paper.width", Integer.toString(value.paperWidth()));
            p.setProperty("margin.left", Double.toString(value.leftMm()));
            p.setProperty("margin.right", Double.toString(value.rightMm()));
            p.setProperty("margin.top", Double.toString(value.topMm()));
            p.setProperty("margin.bottom", Double.toString(value.bottomMm()));
            p.setProperty("paper.feed", Double.toString(value.feedMm()));
            p.setProperty("font.size", Integer.toString(value.fontSize()));
            p.setProperty("print.dialog", Boolean.toString(value.showDialog()));
            AtomicProperties.write(DataLocation.resolve("printer.properties"), p);
        }
    }

    private PrinterPreferences() {
    }
}
