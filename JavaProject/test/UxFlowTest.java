import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import model.*;
import ui.frames.*;
import ui.panels.*;
import ui.panels.posPanel.*;
import util.components.*;
import util.config.*;

public class UxFlowTest {
    private static int checks;

    private static void check(boolean condition, String reason) {
        checks++;
        if (!condition)
            throw new AssertionError(reason);
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static SavedSales.Sale sale(LocalDate day, int index, String name, BigDecimal price) {
        var lines = List.of(new OrderPricing.Line("wash", "Wash", "kg", BigDecimal.ONE, price));
        var totals = OrderPricing.calculate(lines, true, new BigDecimal("12"));
        return new SavedSales.Sale(UUID.randomUUID().toString(), name, "",
                day.atTime(12, 0).atOffset(ZoneOffset.ofHours(8)), index % 2 == 0 ? "Cash" : "GCash", totals.subtotal(),
                totals.tax(), totals.total(), lines, "");
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-ux-test-");
        System.setProperty("laundry.dataDir", root.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", root.resolve("none.properties").toString());
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
        List<SavedSales.Sale> sales = new ArrayList<>();
        for (int i = 0; i < 14; i++)
            sales.add(sale(today.minusDays(i), i, "Customer " + (i % 3), new BigDecimal(i < 7 ? "100.00" : "50.00")));
        sales.add(sale(today.plusDays(1), 99, "Future", new BigDecimal("999")));
        SalesAnalytics.Window seven = SalesAnalytics.calculate(sales, 7, today);
        check(seven.orders().size() == 7, "Seven-day range includes both boundaries and excludes future orders");
        check(seven.previous().size() == 7, "Previous window has no overlap");
        check(seven.revenue().compareTo(new BigDecimal("784.00")) == 0, "Current revenue");
        check(seven.previousRevenue().compareTo(new BigDecimal("392.00")) == 0, "Previous revenue");
        check(seven.average().compareTo(new BigDecimal("112.00")) == 0, "Average order");
        check(seven.returning() == 3 && seven.customers() == 3, "Identified repeat customers");
        check(seven.days().size() == 7 && seven.previousDays().size() == 7, "Chart windows align");
        check(SalesAnalytics.change(seven.revenue(), seven.previousRevenue()).startsWith("+100%"), "Growth comparison");
        check(seven.payments().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(seven.revenue()) == 0, "Payment mix reconciles to revenue");
        check(seven.topItems().get(0).revenue().compareTo(new BigDecimal("700.00")) == 0, "Best sellers exclude tax");
        check(SalesAnalytics.calculate(sales, 30, today).orders().size() == 14, "Thirty-day period updates all data");
        var anonymous = List.of(sale(today, 1, "Walk-in customer", BigDecimal.TEN),
                sale(today, 2, "Walk-in customer", BigDecimal.TEN));
        check(SalesAnalytics.calculate(anonymous, 7, today).returning() == 0,
                "Anonymous walk-ins are not counted as repeat customers");
        java.awt.image.BufferedImage portrait = new java.awt.image.BufferedImage(80, 80,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D ink = portrait.createGraphics();
        ink.setColor(new Color(70, 100, 180));
        ink.fillRect(0, 0, 80, 80);
        ink.dispose();
        Path portraitFile = root.resolve("portrait.png");
        javax.imageio.ImageIO.write(portrait, "png", portraitFile.toFile());
        String portraitRef = ItemImages.importPhoto(portraitFile);
        TeamStore.Member owner = new TeamStore.Member("test-owner", "Alex Rivera", "0900", "Owner", true, portraitRef);
        new TeamStore().save(List.of(owner, new TeamStore.Member("inactive", "Inactive staff", "", "Cashier", false)));
        ActiveProfile.select(owner.id());
        check(ActiveProfile.current().name().equals("Alex Rivera"), "Selected profile is persisted");
        check(ActiveProfile.current().image().equals(portraitRef), "Profile photo reference persists");
        java.util.concurrent.CountDownLatch avatarReady = new java.util.concurrent.CountDownLatch(1);
        Avatar[] avatar = { null };
        SwingUtilities.invokeAndWait(() -> {
            avatar[0] = new Avatar(owner, 48).onLoaded(avatarReady::countDown);
        });
        check(avatarReady.await(8, java.util.concurrent.TimeUnit.SECONDS), "Profile photo loads asynchronously");
        SwingUtilities.invokeAndWait(() -> {
            java.awt.image.BufferedImage painted = new java.awt.image.BufferedImage(48, 48,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            avatar[0].paintIcon(new JLabel(), painted.getGraphics(), 0, 0);
            check(painted.getRGB(24, 24) == portrait.getRGB(40, 40), "Profile photo appears inside circular avatar");
        });
        try {
            ActiveProfile.select("inactive");
            throw new AssertionError("Inactive profile selected");
        } catch (java.io.IOException expected) {
            checks++;
        }
        CatalogItem product = new CatalogStore().items().get(4);
        var lines = List.of(new OrderPricing.Line(product.id(), product.name(), product.unit().toString(),
                BigDecimal.ONE, product.price()));
        var settings = PosSettings.get();
        var order = new OrderStore.Order(UUID.randomUUID().toString(), "Customer", "", "", "Cash", "",
                BigDecimal.valueOf(100), lines,
                OrderPricing.calculate(lines, settings.taxEnabled(), settings.taxRate()), settings);
        OrderStore.save(order);
        new TeamStore()
                .save(List.of(new TeamStore.Member(owner.id(), "Renamed operator", "", "Owner", true, owner.image())));
        check(SavedSales.read().sales().get(0).receipt().contains("Processed by: Alex Rivera"),
                "Receipt keeps the original operator after profile edits");
        WorkspacePanel[] workspace = { null };
        SwingUtilities.invokeAndWait(() -> {
            try {
                workspace[0] = new WorkspacePanel();
                AppEvents.add((PropertyChangeListener) field(workspace[0], "dataListener"));
                check(((JLabel) field(workspace[0], "topProfile")).getText().equals("Renamed operator"),
                        "Top bar shows selected profile");
                JButton profile = (JButton) field(workspace[0], "profileButton");
                check(profile.getIcon() instanceof Avatar, "Sidebar displays profile image component");
                for (String name : new String[] { "Overview", "Catalog", "Inventory", "Reports", "Team", "Settings" }) {
                    workspace[0].showPage(name);
                    workspace[0].setSize(1024, 650);
                    layout(workspace[0]);
                    JScrollPane scroll = findScroll(workspace[0].page(name), "page-scroll");
                    check(scroll != null, "Page has a main scrolling viewport: " + name);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        waitFor(() -> !((AnalyticsDashboard) workspace[0].page("Overview")).isLoading());
        SwingUtilities.invokeAndWait(() -> {
            try {
                workspace[0].showPage("Overview");
                ((JComboBox<?>) field(workspace[0].page("Overview"), "period")).setSelectedIndex(1);
                check(((AnalyticsDashboard) workspace[0].page("Overview")).window().days().size() == 30,
                        "Dashboard period control updates its data window");
                workspace[0].setSize(1024, 650);
                layout(workspace[0]);
                JScrollPane pane = findScroll(workspace[0].page("Overview"), "page-scroll");
                layout(workspace[0]);
                JScrollBar bar = pane.getVerticalScrollBar();
                check(bar.getMaximum() > bar.getVisibleAmount(), "Analytics extends beyond the viewport");
                bar.setValue(0);
                pane.dispatchEvent(new MouseWheelEvent(pane, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(), 0, 50,
                        50, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, 3));
                check(bar.getValue() > 0, "Mouse wheel scrolls the dashboard");
                bar.setValue(bar.getMaximum());
                check(bar.getValue() > 0, "Bottom analytics sections are reachable");
                AppEvents.fire("navigate-pos");
                check(((JLabel) field(workspace[0], "breadcrumb")).getText().endsWith("Point of sale"),
                        "Dashboard action navigates to POS");
                LaundryPOS pos = (LaundryPOS) workspace[0].page("Point of sale");
                pos.addToCart(product);
                JPanel cart = (JPanel) field(pos, "cartItems");
                JSpinner spinner = findSpinner(cart);
                check(spinner != null, "Cart supports keyboard quantity entry");
                JButton plus = findButton(cart, "Increase " + product.name());
                check(plus != null && plus.getPreferredSize().width >= 32, "Cart step action is usable");
                plus.doClick();
                check(pos.orderLines().get(0).quantity().compareTo(BigDecimal.valueOf(2)) == 0,
                        "Plus changes quantity");
                findButton(cart, "Decrease " + product.name()).doClick();
                check(pos.orderLines().get(0).quantity().compareTo(BigDecimal.ONE) == 0, "Minus changes quantity");
                ((JTextField) field(pos, "customerName")).setText("Order customer");
                AppEvents.fire("navigate-reports");
                AppEvents.fire("navigate-pos");
                check(pos.orderLines().size() == 1, "Navigation preserves the current order");
                check(((JTextField) field(pos, "customerName")).getText().equals("Order customer"),
                        "Navigation preserves customer context");
                pos.clearCart();
                check(((JTextField) field(pos, "customerName")).getText().isEmpty(),
                        "New order clears customer context");
                AppEvents.remove((PropertyChangeListener) field(workspace[0], "dataListener"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        System.out.println("PASS: " + checks + " analytics, scrolling, profile and cart-flow checks.");
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
        throw new AssertionError("UI update timed out");
    }

    private static void layout(Container c) {
        for (int i = 0; i < 4; i++)
            layoutOnce(c);
    }

    private static void layoutOnce(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layoutOnce(next);
    }

    private static JScrollPane findScroll(Container c, String name) {
        for (Component child : c.getComponents()) {
            if (child instanceof JScrollPane s && name.equals(s.getName()))
                return s;
            if (child instanceof Container next) {
                JScrollPane found = findScroll(next, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static JSpinner findSpinner(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JSpinner s)
                return s;
            if (child instanceof Container next) {
                JSpinner found = findSpinner(next);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static JButton findButton(Container c, String accessible) {
        for (Component child : c.getComponents()) {
            if (child instanceof JButton b && b.getAccessibleContext().getAccessibleName().equals(accessible))
                return b;
            if (child instanceof Container next) {
                JButton found = findButton(next, accessible);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
}
