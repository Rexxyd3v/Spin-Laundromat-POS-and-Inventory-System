import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.*;
import java.math.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.*;
import ui.frames.*;
import ui.panels.AnalyticsDashboard;
import ui.panels.posPanel.*;
import util.config.*;

/**
 * Actual Swing rendering using isolated demonstration data, never the user's
 * orders.
 */
public class RenderPreview {
    private static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layout(next);
    }

    private static void save(JComponent c, int w, int h, Path path) throws Exception {
        c.setSize(w, h);
        for (int i = 0; i < 6; i++)
            layout(c);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        c.printAll(g);
        g.dispose();
        ImageIO.write(image, "png", path.toFile());
    }

    private static void seed() throws Exception {
        TeamStore.Member admin = new TeamStore.Member("preview-owner", "Sample Admin", "", "Owner", true);
        new TeamStore()
                .save(List.of(admin, new TeamStore.Member("preview-cashier", "Sample Cashier", "", "Cashier", true)));
        ActiveProfile.select(admin.id());
        CatalogStore catalog = new CatalogStore();
        PosSettings.Values settings = PosSettings.get();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
        for (int day = 0; day < 21; day++) {
            int count = day < 7 ? 2 : 1;
            for (int i = 0; i < count; i++) {
                CatalogItem service = catalog.items().get(day % 4), product = catalog.items().get(4 + day % 4);
                List<OrderPricing.Line> lines = List.of(
                        new OrderPricing.Line(service.id(), service.name(), service.unit().toString(),
                                new BigDecimal(2 + (day * 3 + i) % 6), service.price()),
                        new OrderPricing.Line(product.id(), product.name(), product.unit().toString(),
                                new BigDecimal(1 + day % 3), product.price()));
                var totals = OrderPricing.calculate(lines, settings.taxEnabled(), settings.taxRate());
                String id = UUID.randomUUID().toString();
                var order = new OrderStore.Order(id, "Sample customer " + (day % 5 + 1), "0900000000" + (day % 5), "",
                        (day + i) % 3 == 0 ? "GCash" : "Cash", "DEMO-" + day, BigDecimal.valueOf(1000), lines, totals,
                        settings);
                Path file = OrderStore.save(order);
                Properties properties = AtomicProperties.read(file);
                properties.setProperty("created", today.minusDays(day).atTime(9 + i * 5 + day % 3, 15)
                        .atOffset(ZoneOffset.ofHours(8)).toString());
                AtomicProperties.write(file, properties);
            }
        }
    }

    private static JButton findButton(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText()))
                return button;
            if (child instanceof Container nested) {
                JButton found = findButton(nested, text);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static Component named(Container c, String name) {
        for (Component child : c.getComponents()) {
            if (name.equals(child.getName()))
                return child;
            if (child instanceof Container next) {
                Component found = named(next, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static void waitForDashboard(WorkspacePanel workspace) throws Exception {
        long end = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < end) {
            boolean[] done = { false };
            SwingUtilities
                    .invokeAndWait(() -> done[0] = !((AnalyticsDashboard) workspace.page("Overview")).isLoading());
            if (done[0])
                return;
            Thread.sleep(40);
        }
        throw new AssertionError("Analytics preview timed out");
    }

    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        Files.createDirectories(output);
        System.setProperty("laundry.dataDir", Files.createTempDirectory("laundry-preview-").toString());
        seed();
        CatalogItem stockItem = new CatalogStore().items().stream().filter(CatalogItem::product).findFirst()
                .orElseThrow();
        new StockStore().setCount(stockItem.id(), new BigDecimal("15"), new BigDecimal("5"), "Demo opening count");
        var branch = OperationsStore.addBranch("North branch");
        var received = OperationsStore.request("main", branch.id(), stockItem.id(), new BigDecimal("5"),
                "Opening supplies");
        OperationsStore.advanceTransfer(received.id(), OperationsStore.TransferState.DISPATCHED);
        OperationsStore.advanceTransfer(received.id(), OperationsStore.TransferState.RECEIVED);
        var transit = OperationsStore.request("main", branch.id(), stockItem.id(), new BigDecimal("7"),
                "Weekly replenishment");
        OperationsStore.advanceTransfer(transit.id(), OperationsStore.TransferState.DISPATCHED);
        var washer = OperationsStore.addMachine("Washer 01", "Washer");
        OperationsStore.addMachine("Dryer 01", "Dryer");
        var now = Instant.now();
        var running = OperationsStore.reserve(washer.id(), "", now.minusSeconds(5), now.plusSeconds(1200),
                "Counter order — gentle cycle");
        OperationsStore.advanceBooking(running.id(), OperationsStore.BookingState.RUNNING);
        OperationsStore.reserve(washer.id(), "", now.plusSeconds(1800), now.plusSeconds(4500), "Next laundry batch");

        WorkspacePanel[] holder = { null };
        SwingUtilities.invokeAndWait(() -> holder[0] = new WorkspacePanel());
        WorkspacePanel workspace = holder[0];
        waitForDashboard(workspace);
        SwingUtilities.invokeAndWait(() -> {
            try {
                LaundryPOS pos = (LaundryPOS) workspace.page("Point of sale");
                CatalogStore catalog = new CatalogStore();
                pos.addToCart(catalog.items().get(0));
                pos.addToCart(catalog.items().get(0));
                pos.addToCart(catalog.items().get(3));
                pos.addToCart(catalog.items().get(5));
                workspace.showPage("Point of sale");
                save(workspace, 1440, 900, output.resolve("pos-desktop.png"));
                save(workspace, 1024, 768, output.resolve("pos-laptop.png"));
                save(pos, 740, 710, output.resolve("pos-small.png"));
                save(new CheckoutScreen(null, pos.orderLines(), pos.totals(), PosSettings.get()), 900, 720,
                        output.resolve("pos-checkout.png"));
                pos.clearCart();
                CatalogItem breeze = catalog.items().stream().filter(i -> i.id().equals("breeze")).findFirst()
                        .orElse(catalog.items().get(6));
                pos.addToCart(breeze);
                pos.addToCart(breeze);
                pos.addToCart(breeze);
                JComponent summary = (JComponent) named(pos, "order-summary");
                save(summary, 360, 620, output.resolve("order-summary.png"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        for (String name : new String[] { "Overview", "Catalog", "Inventory", "Operations", "Reports", "Team",
                "Settings" }) {
            SwingUtilities.invokeAndWait(() -> workspace.showPage(name));
            if (name.equals("Overview"))
                waitForDashboard(workspace);
            if (name.equals("Operations")) {
                long timeout = System.currentTimeMillis() + 10000;
                while (System.currentTimeMillis() < timeout) {
                    boolean[] loading = { true };
                    SwingUtilities.invokeAndWait(
                            () -> loading[0] = ((ui.panels.OperationsPage) workspace.page(name)).isLoading());
                    if (!loading[0])
                        break;
                    Thread.sleep(30);
                }
            }

            SwingUtilities.invokeAndWait(() -> {
                try {
                    save(workspace, 1440, 900, output.resolve(name.toLowerCase() + ".png"));
                    if (name.equals("Operations")) {
                        JTabbedPane tabs = (JTabbedPane) named(workspace.page(name), "operationsTabs");
                        tabs.setSelectedIndex(1);
                        save(workspace, 1440, 900, output.resolve("machine-schedule.png"));
                        tabs.setSelectedIndex(2);
                        save(workspace, 1440, 900, output.resolve("machines.png"));
                        tabs.setSelectedIndex(0);
                        save(workspace, 1024, 768, output.resolve("operations-laptop.png"));
                    }
                    if (name.equals("Overview")) {
                        JScrollPane page = (JScrollPane) named(workspace.page(name), "page-scroll");
                        page.getVerticalScrollBar().setValue(page.getVerticalScrollBar().getMaximum());
                        save(workspace, 1440, 900, output.resolve("overview-details.png"));
                        page.getVerticalScrollBar().setValue(0);
                        save(workspace, 1024, 768, output.resolve("overview-laptop.png"));
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                var notification = (util.components.StockNotificationButton) named(workspace, "stockNotifications");
                save(notification.notificationContent(), 480, 350, output.resolve("stock-notifications.png"));
                JButton dataButton = findButton(workspace.page("Settings"), "Data & backup");
                if (dataButton != null)
                    dataButton.doClick();
                workspace.showPage("Settings");
                save(workspace, 1440, 900, output.resolve("data-tools.png"));
                save(new util.components.ReceiptView(SavedSales.read().sales().get(0).receipt(), true), 500, 650,
                        output.resolve("receipt-options.png"));
                save(new WelcomePanel(false, () -> {
                }), 1200, 800, output.resolve("welcome.png"));
                save(new WelcomePanel(true, () -> {
                }), 1200, 800, output.resolve("staff-login.png"));
                save(new WelcomePanel(false, () -> {
                }), 940, 680, output.resolve("welcome-small.png"));
                save(new WelcomePanel(true, () -> {
                }), 940, 680, output.resolve("staff-login-small.png"));
                CatalogStore catalog = new CatalogStore();
                CatalogEditor editor = new CatalogEditor(null, catalog, catalog.items().get(0), () -> {
                });
                try {
                    save((JComponent) editor.getContentPane(), 780, 580, output.resolve("image-editor.png"));
                    save((JComponent) editor.getContentPane(), 380, 360, output.resolve("image-editor-small.png"));
                    JScrollPane formScroll = (JScrollPane) named(editor.getContentPane(), "catalogFormScroll");
                    formScroll.getVerticalScrollBar().setValue(formScroll.getVerticalScrollBar().getMaximum());
                    save((JComponent) editor.getContentPane(), 380, 360,
                            output.resolve("image-editor-small-bottom.png"));
                } finally {
                    editor.dispose();
                }
                CatalogEditor addEditor = new CatalogEditor(null, catalog, null, () -> {
                });
                try {
                    save((JComponent) addEditor.getContentPane(), 420, 440, output.resolve("add-item-small.png"));
                } finally {
                    addEditor.dispose();
                }
                ProfileEditor profile = new ProfileEditor(null, ActiveProfile.current(), true, () -> {
                });
                try {
                    save((JComponent) profile.getContentPane(), 540, 700, output.resolve("profile-editor.png"));
                } finally {
                    profile.dispose();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        System.out.println(
                "Rendered analytics, scrolling details, order summary, sidebar profiles and every workspace page with isolated demo data.");
    }
}
