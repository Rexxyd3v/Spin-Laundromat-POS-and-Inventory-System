import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.List;
import javax.swing.*;
import application.*;
import model.*;
import ui.frames.*;
import ui.panels.*;
import util.components.*;
import util.config.*;

public class FeatureUiTest {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value)
            throw new AssertionError(message);
    }

    private static Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(object);
    }

    private static JButton button(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton b && text.equals(b.getText()))
                return b;
            if (child instanceof Container nested) {
                JButton b = button(nested, text);
                if (b != null)
                    return b;
            }
        }
        return null;
    }

    private static Component named(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container nested) {
                Component found = named(nested, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents())
            if (child instanceof Container nested)
                layout(nested);
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            boolean[] done = { false };
            SwingUtilities.invokeAndWait(() -> done[0] = condition.getAsBoolean());
            if (done[0])
                return;
            Thread.sleep(30);
        }
        throw new AssertionError("Feature UI did not settle");
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-feature-ui-");
        System.setProperty("laundry.dataDir", root.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", root.resolve("missing.properties").toString());
        UIStyle.installDefaults();
        var catalog = new CatalogStore();
        catalog.save(catalog.items());
        var product = catalog.items().stream().filter(CatalogItem::product).findFirst().orElseThrow();
        new StockStore().setCount(product.id(), BigDecimal.valueOf(3), BigDecimal.valueOf(3), "Equal threshold");
        OperationsStore.addBranch("North branch");
        OperationsStore.addMachine("Washer 1", "Washer");
        WorkspacePanel[] workspace = { null };
        StockNotificationButton[] notification = { null };
        SwingUtilities.invokeAndWait(() -> {
            workspace[0] = new WorkspacePanel();
            notification[0] = new StockNotificationButton();
            notification[0].addNotify();
        });
        waitFor(() -> !notification[0].isLoading() && notification[0].alerts().size() == 1);
        check(notification[0].getAccessibleContext().getAccessibleName().equals("1 stock alerts"),
                "Stock bell exposes its live warning count accessibly");
        new StockStore().setCount(product.id(), BigDecimal.valueOf(10), BigDecimal.valueOf(3), "Restocked");
        waitFor(() -> notification[0].alerts().isEmpty());
        check(notification[0].getText().equals("0"),
                "Stock event clears resolved notifications without reopening the app");
        new StockStore().setCount(product.id(), BigDecimal.valueOf(3), BigDecimal.valueOf(3), "At threshold again");
        waitFor(() -> notification[0].alerts().size() == 1);
        check(notification[0].alerts().get(0).available().equals(BigDecimal.valueOf(3)),
                "Equality reappears in live notifications");
        waitFor(() -> !((AnalyticsDashboard) workspace[0].page("Overview")).isLoading()
                && !((OperationsPage) workspace[0].page("Operations")).isLoading());
        SwingUtilities.invokeAndWait(() -> {
            try {
                var analytics = (AnalyticsDashboard) workspace[0].page("Overview");
                JComboBox<?> period = (JComboBox<?>) field(analytics, "period");
                period.setSelectedIndex(2);
                check(analytics.window().days().size() == 90, "Ninety-day analytics preset is available");
                period.setSelectedIndex(3);
                JTextField from = (JTextField) named(analytics, "analyticsFrom"),
                        to = (JTextField) named(analytics, "analyticsTo");
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
                from.setText(today.minusDays(2).toString());
                to.setText(today.toString());
                button(analytics, "Apply range").doClick();
                check(analytics.window().days().size() == 3, "Custom analytics ranges include both endpoints");
                to.setText(today.minusDays(3).toString());
                button(analytics, "Apply range").doClick();
                check(analytics.window().days().size() == 3
                        && ((JLabel) field(analytics, "status")).getText().contains("valid dates"),
                        "Invalid ranges keep the last valid result and explain the error");
                from.setText(today.toString());
                to.setText(today.toString());
                button(analytics, "Apply range").doClick();
                check(analytics.window().days().size() == 1, "A single-day range is supported");
                for (String page : new String[] { "Overview", "Operations", "Inventory", "Settings" }) {
                    workspace[0].showPage(page);
                    workspace[0].setSize(1024, 650);
                    for (int i = 0; i < 6; i++)
                        layout(workspace[0]);
                    BufferedImage image = new BufferedImage(1024, 650, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics();
                    workspace[0].printAll(graphics);
                    graphics.dispose();
                    check(workspace[0].page(page).getWidth() > 0, "Feature page renders in a laptop window: " + page);
                }
                workspace[0].showPage("Operations");
                check(button(workspace[0].page("Operations"), "Request transfer") != null
                        && button(workspace[0].page("Operations"), "Reserve machine") != null,
                        "Operations exposes transfer and booking controls");
                check(((JTable) named(workspace[0].page("Operations"), "machinesTable")).getRowCount() == 1,
                        "Saved machines load into the scheduling UI");
                workspace[0].showPage("Settings");
                button(workspace[0].page("Settings"), "Data & backup").doClick();
                check(button(workspace[0].page("Settings"), "Export backup") != null
                        && button(workspace[0].page("Settings"), "Restore a copy") != null,
                        "Data recovery controls are available in Settings");
                button(workspace[0].page("Settings"), "Run data audit").doClick();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        waitFor(() -> ((JTextArea) named(workspace[0].page("Settings"), "dataAuditOutput")).getText()
                .startsWith("LOCAL DATA AUDIT"));
        check(button(workspace[0].page("Settings"), "Run data audit").isEnabled(),
                "Background data audit returns a readable result and re-enables actions");
        BufferedImage sheet = new BufferedImage(400, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = sheet.createGraphics();
        var narrow = new ReceiptPrinter("Long receipt line " + "X".repeat(100) + "\nSecond line", 58);
        var wide = new ReceiptPrinter("Receipt", 80);
        check(narrow.pageFormat().getPaper().getWidth() < wide.pageFormat().getPaper().getWidth(),
                "58/80 mm formats use different actual paper widths");
        graphics.setFont(new Font("Monospaced", Font.PLAIN, 8));
        var lines = narrow.lines(graphics.getFontMetrics(), narrow.pageFormat().getImageableWidth());
        check(lines.size() > 2 && lines.stream().allMatch(
                line -> graphics.getFontMetrics().stringWidth(line) <= narrow.pageFormat().getImageableWidth()),
                "Long text wraps inside the receipt print area");
        check(narrow.print(graphics, narrow.pageFormat(), 0) == Printable.PAGE_EXISTS
                && narrow.print(graphics, narrow.pageFormat(), 999) == Printable.NO_SUCH_PAGE,
                "Printable receipt has a valid first page and terminates");
        var longReceipt = new ReceiptPrinter("Receipt row\n".repeat(1000), 58);
        check(longReceipt.print(graphics, longReceipt.pageFormat(), 1) == Printable.PAGE_EXISTS,
                "Long receipts paginate instead of truncating");
        graphics.dispose();
        SwingUtilities.invokeAndWait(() -> notification[0].removeNotify());
        System.out.println("PASS: " + checks
                + " live notification, data-tool, analytics, operations-layout and receipt-print checks.");
    }
}
