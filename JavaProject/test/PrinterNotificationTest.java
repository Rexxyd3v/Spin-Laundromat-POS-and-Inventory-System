import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.print.*;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.swing.*;
import application.*;
import model.*;
import persistence.*;
import ui.panels.*;
import ui.panels.settingsPanel.*;
import util.components.*;
import util.config.*;

/**
 * No jobs are submitted by these tests; printer selection is exercised with
 * controlled print services.
 */
public class PrinterNotificationTest {
    private static int checks;

    private static void check(boolean value, String text) {
        checks++;
        if (!value)
            throw new AssertionError(text);
    }

    private interface Task {
        void run() throws Exception;
    }

    private static void reject(Task task, String text) throws Exception {
        try {
            task.run();
            throw new AssertionError(text);
        } catch (java.io.IOException | IllegalArgumentException | PrinterException expected) {
            checks++;
        }
    }

    private static Object field(Object value, String name) throws Exception {
        Class<?> type = value.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(value);
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Component named(Container root, String name) {
        if (name.equals(root.getName()))
            return root;
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName()))
                return c;
            if (c instanceof Container container) {
                Component found = named(container, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static JButton button(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText()))
                return b;
            if (c instanceof Container container) {
                JButton found = button(container, text);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static int count(Container root, String name) {
        int result = 0;
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName()))
                result++;
            if (c instanceof Container container)
                result += count(container, name);
        }
        return result;
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents())
            if (child instanceof Container container)
                layout(container);
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            boolean[] done = { false };
            SwingUtilities.invokeAndWait(() -> done[0] = condition.getAsBoolean());
            if (done[0])
                return;
            Thread.sleep(30);
        }
        throw new AssertionError("UI did not settle");
    }

    private static PrintService service(String name, boolean accepting) {
        return (PrintService) Proxy.newProxyInstance(PrinterNotificationTest.class.getClassLoader(),
                new Class[] { PrintService.class }, (proxy, method, args) -> switch (method.getName()) {
                case "getName", "toString" -> name;
                case "getAttribute" -> args[0] == PrinterIsAcceptingJobs.class
                        ? (accepting ? PrinterIsAcceptingJobs.ACCEPTING_JOBS
                                : PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS)
                        : null;
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                default -> null;
                });
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-printer-notification-");
        System.setProperty("laundry.dataDir", root.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", root.resolve("missing.properties").toString());
        UIStyle.installDefaults();
        var defaults = PrinterPreferences.load();
        check(defaults.paperWidth() == 80 && defaults.showDialog(),
                "Legacy installs inherit an 80 mm profile with print confirmation");
        var config = new PrinterPreferences.Config("Thermal POS-58", 58, 2, 3, 4, 5, 8, 8, false);
        PrinterPreferences.save(config);
        check(PrinterPreferences.load().equals(config),
                "Selected queue, margins, feed, font and dialog option persist");
        reject(() -> new PrinterPreferences.Config("", 60, 3, 3, 3, 3, 6, 9, true), "Unsupported paper widths fail");
        reject(() -> new PrinterPreferences.Config("", 58, Double.NaN, 3, 3, 3, 6, 9, true), "Non-finite margins fail");
        reject(() -> new PrinterPreferences.Config("", 58, -1, 3, 3, 3, 6, 9, true), "Negative margins fail");
        reject(() -> new PrinterPreferences.Config("", 58, 3, 3, 3, 3, 31, 9, true), "Oversized extra feed fails");
        reject(() -> new PrinterPreferences.Config("", 58, 3, 3, 3, 3, 6, 20, true), "Unsupported text sizes fail");
        var shortReceipt = new ReceiptPrinter("Receipt\nShort item", config);
        var shortPage = shortReceipt.pageFormat();
        double mm = 72.0 / 25.4;
        check(Math.abs(shortPage.getWidth() / mm - 58) < 0.01 && Math.abs(shortPage.getImageableX() / mm - 2) < 0.01,
                "Thermal paper and left margin use millimetres");
        check(Math.abs(shortPage.getImageableWidth() / mm - 53) < 0.01, "Both side margins constrain the receipt text");
        check(shortPage.getHeight() / mm < 297, "Short receipts do not always feed an A4-length sheet");
        var longReceipt = new ReceiptPrinter("Long receipt row\n".repeat(500), config);
        check(Math.abs(longReceipt.pageFormat().getHeight() / mm - 297) < 0.01,
                "Long receipts retain bounded pagination");
        var noFeed = new PrinterPreferences.Config("", 80, 3, 3, 3, 3, 0, 9, true);
        var extraFeed = new PrinterPreferences.Config("", 80, 3, 3, 3, 3, 15, 9, true);
        String sample = ReceiptPrinterPanel.sample();
        check(new ReceiptPrinter(sample, extraFeed).pageFormat().getHeight() > new ReceiptPrinter(sample, noFeed)
                .pageFormat().getHeight(), "Extra feed increases content-height receipts");
        PrintService thermal = service("Thermal POS-58", true), office = service("Office", true),
                stopped = service("Paused", false);
        check(PrinterServiceManager.choose("Thermal POS-58", List.of(thermal, office), office) == thermal,
                "Explicit queue is used instead of the Windows default");
        check(PrinterServiceManager.choose("", List.of(thermal, office), office) == office,
                "Default selection uses the actual system default");
        reject(() -> PrinterServiceManager.choose("Missing", List.of(thermal), thermal),
                "Missing saved queue never silently falls back");
        reject(() -> PrinterServiceManager.choose("", List.of(), null), "Missing default printer is reported");
        reject(() -> PrinterServiceManager.choose("Paused", List.of(stopped), null),
                "A queue that refuses jobs is reported");
        byte[] good = Files.readAllBytes(DataLocation.resolve("printer.properties"));
        Files.writeString(DataLocation.resolve("printer.properties"), "paper.width=broken\n");
        reject(PrinterPreferences::load, "Malformed printer settings require review");
        check(Files.readString(DataLocation.resolve("printer.properties")).contains("broken"),
                "Loading does not overwrite malformed settings");
        Files.write(DataLocation.resolve("printer.properties"), good);
        ReceiptPrinterPanel[] panel = { null };
        AtomicInteger lookups = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> panel[0] = new ReceiptPrinterPanel(() -> {
            lookups.incrementAndGet();
            return List.of(new PrinterServiceManager.Device("Thermal POS-58", "Idle", false, true),
                    new PrinterServiceManager.Device("Office", "Idle", true, true));
        }));
        waitFor(() -> !panel[0].isScanning());
        SwingUtilities.invokeAndWait(() -> {
            try {
                JComboBox<?> choices = (JComboBox<?>) named(panel[0], "receiptPrinterChoice");
                check(choices.getSelectedItem().toString().contains("Thermal POS-58"),
                        "Saved thermal printer is restored after discovery");
                ((JTextField) named(panel[0], "printerSearch")).setText("Office");
                check(choices.getItemCount() >= 2,
                        "Installed printers can be filtered without losing the selected printer");
                ((JSpinner) named(panel[0], "printerMarginLeft")).setValue(4.0);
                button(panel[0], "Save printer").doClick();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        waitFor(() -> button(panel[0], "Save printer").isEnabled());
        check(PrinterPreferences.load().leftMm() == 4.0, "Printer panel saves edited device settings");
        check(lookups.get() == 1, "Discovery runs explicitly without submitting a print job");
        SwingUtilities.invokeAndWait(() -> {
            panel[0].setSize(540, 420);
            for (int i = 0; i < 6; i++)
                layout(panel[0]);
            JButton save = button(panel[0], "Save printer"), test = button(panel[0], "Test print");
            Rectangle saveBounds = SwingUtilities.convertRectangle(save.getParent(), save.getBounds(), panel[0]),
                    testBounds = SwingUtilities.convertRectangle(test.getParent(), test.getBounds(), panel[0]);
            check(saveBounds.y + saveBounds.height <= 420 && testBounds.y + testBounds.height <= 420,
                    "Save and Test remain visible in a compact printer panel");
            check(named(panel[0], "printerFontSize").getHeight() >= 38, "Printer number controls keep a usable height");
        });
        var catalog = new CatalogStore();
        catalog.save(catalog.items());
        var products = catalog.items().stream().filter(CatalogItem::product).toList();
        var low = products.get(0);
        var out = products.get(1);
        new StockStore().setCount(low.id(), BigDecimal.valueOf(3), BigDecimal.valueOf(3), "Low threshold");
        new StockStore().setCount(out.id(), BigDecimal.ZERO, BigDecimal.valueOf(2), "Out");
        var north = OperationsStore.addBranch("North");
        new StockStore(north.id()).setCount(low.id(), BigDecimal.ONE, BigDecimal.valueOf(2), "North warning");
        StockNotificationButton[] bell = { null };
        JPanel[] content = { null };
        SwingUtilities.invokeAndWait(() -> {
            bell[0] = new StockNotificationButton();
            bell[0].addNotify();
        });
        waitFor(() -> !bell[0].isLoading());
        SwingUtilities.invokeAndWait(() -> {
            content[0] = bell[0].notificationContent();
            check(count(content[0], "notificationAlertCard") == 3,
                    "Flyout renders one card per active branch/product alert");
            ((JButton) named(content[0], "notificationFilterOutofstock")).doClick();
            check(count(content[0], "notificationAlertCard") == 1, "Out-of-stock filter excludes low-stock warnings");
            ((JButton) named(content[0], "notificationFilterLowstock")).doClick();
            check(count(content[0], "notificationAlertCard") == 2, "Low-stock filter excludes zero balances");
            JComboBox<?> branches = (JComboBox<?>) named(content[0], "notificationBranchFilter");
            for (int i = 0; i < branches.getItemCount(); i++)
                if (branches.getItemAt(i).toString().equals("North")) {
                    branches.setSelectedIndex(i);
                    break;
                }
            check(count(content[0], "notificationAlertCard") == 1, "Branch filter scopes warning cards");
            content[0].setSize(460, 570);
            for (int i = 0; i < 5; i++)
                layout(content[0]);
            check(named(content[0], "notificationList").getHeight() > 100, "Filtered alerts remain scrollable");
        });
        BusinessPages.Inventory[] inventory = { null };
        SwingUtilities.invokeAndWait(() -> {
            inventory[0] = new BusinessPages.Inventory();
            inventory[0].focusProduct(north.id(), low.id());
        });
        waitFor(() -> {
            try {
                var picker = (JComboBox<?>) field(inventory[0], "branches");
                var table = (JTable) field(inventory[0], "table");
                return picker.getSelectedItem().toString().equals("North") && table.getSelectedRow() >= 0;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        check(true, "Notification destination selects the relevant branch and product");
        new StockStore(north.id()).setCount(low.id(), BigDecimal.valueOf(10), BigDecimal.valueOf(2), "Replenished");
        waitFor(() -> bell[0].alerts().size() == 2 && !bell[0].isLoading());
        SwingUtilities.invokeAndWait(() -> {
            check(count(content[0], "notificationAlertCard") == 0, "Open flyout updates after a warning resolves");
            bell[0].removeNotify();
        });
        System.out.println("PASS: " + checks
                + " printer configuration, queue selection, thermal geometry and notification UI checks. No print jobs sent.");
    }
}
