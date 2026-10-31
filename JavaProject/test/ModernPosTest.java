import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import model.*;
import ui.panels.posPanel.*;
import util.config.*;

/**
 * Run with assertions; all writes go to a newly created temporary directory.
 */
public class ModernPosTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition)
            throw new AssertionError(message);
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void rejects(Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError(message);
        } catch (IllegalArgumentException expected) {
            checks++;
        }
    }

    public static void main(String[] args) throws Exception {
        Path data = Files.createTempDirectory("laundry-modern-test-");
        System.setProperty("laundry.dataDir", data.toString());
        Path legacy = data.resolve("legacy-services.properties");
        Files.writeString(legacy, "count=1\nservice.0.name=Test wash\nservice.0.desc=Per kg\nservice.0.price=24.29\n");
        System.setProperty("laundry.legacyServices", legacy.toString());
        String original = Files.readString(legacy);
        CatalogStore store = new CatalogStore();
        List<CatalogItem> initial = store.items();
        check(initial.size() == 5, "Legacy import plus retail catalog");
        check(Files.readString(legacy).equals(original), "Import must not change original services");
        store.save(initial);
        check(new CatalogStore().items().equals(initial), "Catalog round trip");
        store.save(List.of());
        check(new CatalogStore().items().isEmpty(), "An intentionally empty catalog must stay empty");
        store.save(initial);
        Files.writeString(data.resolve("catalog.properties"), "count=broken\n");
        try {
            new CatalogStore();
            throw new AssertionError("Corrupt catalog must be rejected");
        } catch (java.io.IOException expected) {
            check(Files.readString(data.resolve("catalog.properties")).equals("count=broken\n"),
                    "Corrupt file must not be overwritten");
        }
        store.save(initial);
        rejects(() -> new CatalogItem("x", "Bad", "", "Laundry", CatalogItem.Unit.KG, new BigDecimal("-1"), false),
                "Negative price");
        rejects(() -> new CatalogItem("x", "Bad", "", "Laundry", CatalogItem.Unit.KG, new BigDecimal("1.234"), false),
                "Fractional cents");
        List<OrderPricing.Line> lines = List.of(
                new OrderPricing.Line("wash", "Wash", "kg", new BigDecimal("2.375"), new BigDecimal("24.29")),
                new OrderPricing.Line("soap", "Soap", "pc", new BigDecimal("2"), new BigDecimal("7.00")));
        OrderPricing.Totals totals = OrderPricing.calculate(lines, true, new BigDecimal("12"));
        check(totals.subtotal().toPlainString().equals("71.69"), "Per-line decimal rounding");
        check(totals.tax().toPlainString().equals("8.60"), "Tax rounding");
        check(totals.total().toPlainString().equals("80.29"), "Grand total");
        check(OrderPricing.calculate(lines, false, new BigDecimal("12")).tax().signum() == 0, "Disabled tax");
        UIStyle.installDefaults();
        PosSettings.Values settings = new PosSettings.Values("Test Café Laundry", "Test address", "09000000000",
                "Salamat!", "Blue", true, true, true, new BigDecimal("5"), true);
        PosSettings.save(settings);
        SwingUtilities.invokeAndWait(() -> {
        });
        Properties saved = AtomicProperties.read(data.resolve("pos-settings.properties"));
        check(saved.getProperty("store.name").equals("Test Café Laundry"), "Unicode settings persistence");
        check(UIStyle.PRIMARY.equals(UIStyle.accent("Blue")), "Live accent application");
        OrderStore.Order order = new OrderStore.Order(UUID.randomUUID().toString(), "Customer", "09", "Separate whites",
                "Cash", "", new BigDecimal("100"), lines, totals, PosSettings.defaults());
        Path file = OrderStore.save(order);
        Properties persisted = AtomicProperties.read(file);
        check(persisted.getProperty("line.0.quantity").equals("2.375"), "Decimal weight snapshot");
        check(persisted.getProperty("total").equals("80.29"), "Saved total snapshot");
        check(OrderStore.receipt(order).contains("Change: ₱19.71"), "Receipt change");
        try {
            OrderStore.save(order);
            throw new AssertionError("Duplicate order overwrite");
        } catch (FileAlreadyExistsException expected) {
            checks++;
        }
        LaundryPOS[] pos = { null };
        CheckoutScreen[] checkout = { null };
        SwingUtilities.invokeAndWait(() -> {
            try {
                pos[0] = new LaundryPOS();
                PosSettings.addListener((java.beans.PropertyChangeListener) field(pos[0], "settingsListener"));
                pos[0].addToCart(initial.get(0));
                pos[0].addToCart(initial.get(1));
                check(pos[0].orderLines().get(1).unit().equals("pc"), "Product unit must be pieces");
                JPanel cart = (JPanel) field(pos[0], "cartItems");
                JSpinner qty = findSpinner(cart);
                qty.setValue(2.375);
                check(pos[0].orderLines().get(0).quantity().compareTo(new BigDecimal("2.375")) == 0,
                        "Editable decimal weight");
                JTextField search = (JTextField) field(pos[0], "search");
                search.setText("no matching item");
                check(((JLabel) field(pos[0], "count")).getText().startsWith("0 items"), "Live search empty state");
                search.setText("Test wash");
                check(((JLabel) field(pos[0], "count")).getText().startsWith("1 item"), "Live search matches");
                search.setText("");
                PosSettings.save(PosSettings.defaults());
                check(pos[0].orderLines().size() == 2, "Settings change preserves cart");
                check(pos[0].orderLines().get(0).quantity().compareTo(new BigDecimal("2.375")) == 0,
                        "Settings change preserves weight");
                pos[0].setSize(1100, 760);
                layout(pos[0]);
                BufferedImage image = new BufferedImage(1100, 760, BufferedImage.TYPE_INT_RGB);
                pos[0].paint(image.getGraphics());
                check(image.getRGB(0, 0) != 0, "POS paints");
                checkout[0] = new CheckoutScreen(pos[0], pos[0].orderLines(), pos[0].totals(), PosSettings.get());
                ((JTextField) field(checkout[0], "customer")).setText("Test customer");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Path blocked = data.resolve("blocked");
        Files.writeString(blocked, "not a directory");
        System.setProperty("laundry.dataDir", blocked.toString());
        clickConfirm(checkout[0]);
        waitFor(() -> {
            try {
                return ((JLabel) field(checkout[0], "feedback")).getText().startsWith("Order could not");
            } catch (Exception e) {
                return false;
            }
        });
        check(pos[0].orderLines().size() == 2, "Failed save preserves cart");
        System.setProperty("laundry.dataDir", data.toString());
        clickConfirm(checkout[0]);
        waitFor(() -> pos[0].orderLines().isEmpty());
        check(pos[0].orderLines().isEmpty(), "Successful save clears cart");
        try (var files = Files.list(data.resolve("orders"))) {
            check(files.count() == 2, "Exactly one order is saved after retry");
        }
        PosSettings.removeListener((java.beans.PropertyChangeListener) field(pos[0], "settingsListener"));
        System.out.println("PASS: " + checks + " checks. Test data: " + data);
    }

    private static JSpinner findSpinner(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JSpinner s)
                return s;
            if (child instanceof Container next) {
                JSpinner s = findSpinner(next);
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    private static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layout(next);
    }

    private static void clickConfirm(CheckoutScreen c) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ((JButton) field(c, "confirm")).doClick();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
            boolean[] done = { false };
            SwingUtilities.invokeAndWait(() -> done[0] = condition.getAsBoolean());
            if (done[0])
                return;
            Thread.sleep(30);
        }
        throw new AssertionError("Timed out waiting for checkout");
    }
}
