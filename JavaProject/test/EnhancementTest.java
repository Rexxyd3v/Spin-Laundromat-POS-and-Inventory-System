import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.*;
import ui.frames.WorkspacePanel;
import util.components.ItemPhoto;
import util.config.*;

/**
 * Integration checks for uploaded photos, stock, reporting and complete page
 * layouts.
 */
public class EnhancementTest {
    private static int checks;

    private static void check(boolean yes, String reason) {
        checks++;
        if (!yes)
            throw new AssertionError(reason);
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("reload")) {
            CatalogStore catalog = new CatalogStore();
            check(!catalog.items().get(0).image().isBlank(), "Photo persists after restart");
            check(ItemImages.read(ItemImages.resolve(catalog.items().get(0).image())) != null,
                    "Managed photo loads after restart");
            check(PosSettings.get().storeName().equals("Photo test store"), "Branding persists after restart");
            check(new TeamStore().read().size() == 2, "Staff profile persists after restart");
            System.out.println("RELOAD OK");
            return;
        }
        Path data = Files.createTempDirectory("laundry-enhancement-test-");
        System.setProperty("laundry.dataDir", data.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", data.resolve("not-present.properties").toString());
        Path source = data.resolve("chosen-photo.png");
        BufferedImage fixture = new BufferedImage(90, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = fixture.createGraphics();
        graphics.setColor(new Color(212, 64, 100));
        graphics.fillRect(0, 0, 90, 60);
        graphics.dispose();
        ImageIO.write(fixture, "png", source.toFile());
        String photo = ItemImages.importPhoto(source);
        Path managed = ItemImages.resolve(photo);
        Files.move(source, data.resolve("moved-original.png"));
        check(Files.exists(managed), "Photo is copied into managed storage");
        check(ItemImages.read(managed).getRGB(45, 30) == fixture.getRGB(45, 30),
                "Managed photo remains intact after moving original");
        Path invalid = data.resolve("invalid.png");
        Files.writeString(invalid, "not an image");
        try {
            ItemImages.importPhoto(invalid);
            throw new AssertionError("Invalid upload accepted");
        } catch (java.io.IOException expected) {
            checks++;
        }
        try {
            ItemImages.resolve("../../outside.png");
            throw new AssertionError("Path traversal accepted");
        } catch (java.io.IOException expected) {
            checks++;
        }
        CatalogStore catalog = new CatalogStore();
        List<CatalogItem> items = new ArrayList<>(catalog.items());
        CatalogItem first = items.get(0);
        items.set(0, new CatalogItem(first.id(), first.name(), first.description(), first.category(), first.unit(),
                first.price(), first.product(), photo));
        catalog.save(items);
        check(new CatalogStore().items().get(0).image().equals(photo), "Catalog saves uploaded image");
        CountDownLatch loaded = new CountDownLatch(1);
        ItemPhoto[] component = { null };
        SwingUtilities.invokeAndWait(() -> {
            component[0] = new ItemPhoto(false).onLoaded(loaded::countDown);
            component[0].load(managed);
        });
        check(loaded.await(8, TimeUnit.SECONDS), "Async photo load completes");
        SwingUtilities.invokeAndWait(() -> {
            component[0].setSize(200, 140);
            BufferedImage rendered = new BufferedImage(200, 140, BufferedImage.TYPE_INT_RGB);
            component[0].paint(rendered.getGraphics());
            check(rendered.getRGB(100, 70) == fixture.getRGB(45, 30), "Uploaded image is painted on its card");
        });
        CatalogItem product = items.stream().filter(CatalogItem::product).findFirst().orElseThrow();
        StockStore stock = new StockStore();
        stock.setCount(product.id(), new BigDecimal("12"), new BigDecimal("3"), "Opening count");
        check(new StockStore().tracked(product.id()), "Stock tracking persists");
        List<OrderPricing.Line> lines = List.of(new OrderPricing.Line(product.id(), product.name(),
                product.unit().toString(), new BigDecimal("4"), product.price()));
        PosSettings.Values settings = PosSettings.defaults();
        OrderPricing.Totals totals = OrderPricing.calculate(lines, settings.taxEnabled(), settings.taxRate());
        OrderStore.save(new OrderStore.Order(UUID.randomUUID().toString(), "Photo test customer", "", "", "Cash", "",
                new BigDecimal("100"), lines, totals, settings));
        SavedSales.Result sales = SavedSales.read();
        check(sales.sales().size() == 1, "Reports read saved checkout");
        check(stock.available(product.id(), sales.sales()).compareTo(new BigDecimal("8")) == 0,
                "Saved order reduces tracked stock");
        List<OrderPricing.Line> tooMany = List
                .of(new OrderPricing.Line(product.id(), product.name(), "pc", new BigDecimal("9"), product.price()));
        try {
            OrderStore.save(new OrderStore.Order(UUID.randomUUID().toString(), "Customer", "", "", "Cash", "",
                    new BigDecimal("1000"), tooMany, OrderPricing.calculate(tooMany, true, new BigDecimal("12")),
                    settings));
            throw new AssertionError("Overselling accepted");
        } catch (java.io.IOException expected) {
            checks++;
        }
        check(SavedSales.read().sales().size() == 1, "Rejected checkout writes no partial order");
        stock.setCount(product.id(), new BigDecimal("5"), new BigDecimal("2"), "Recount");
        check(new StockStore().available(product.id(), SavedSales.read().sales()).compareTo(new BigDecimal("5")) == 0,
                "Physical count accounts for previous sales");
        new TeamStore().save(List.of(model.ActiveProfile.current(),new TeamStore.Member(UUID.randomUUID().toString(), "Test team member",
                "09000000000", "Cashier", true)));
        PosSettings.save(new PosSettings.Values("Photo test store", "Address", "Phone", "Thank you", "Violet", false,
                false, true, new BigDecimal("12"), false));
        SwingUtilities.invokeAndWait(() -> {
        });
        Process restart = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Djava.awt.headless=true", "-Dlaundry.dataDir=" + data, "-cp", System.getProperty("java.class.path"),
                "EnhancementTest", "reload").redirectErrorStream(true).start();
        check(restart.waitFor(10, TimeUnit.SECONDS), "Restart check completes");
        String output = new String(restart.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        check(restart.exitValue() == 0 && output.contains("RELOAD OK"),
                "Catalog, photos, settings and staff survive a fresh process: " + output);
        SwingUtilities.invokeAndWait(() -> {
            WorkspacePanel workspace = new WorkspacePanel();
            for (int width : new int[] { 1440, 1024 }) {
                for (String name : new String[] { "Point of sale", "Overview", "Catalog", "Inventory", "Reports",
                        "Team", "Settings" }) {
                    workspace.showPage(name);
                    workspace.setSize(width, 768);
                    for (int pass = 0; pass < 4; pass++)
                        layout(workspace);
                    BufferedImage image = new BufferedImage(width, 768, BufferedImage.TYPE_INT_RGB);
                    workspace.printAll(image.getGraphics());
                    check(workspace.page(name).getWidth() > 0 && workspace.page(name).getHeight() > 0,
                            "Page lays out at " + width + ": " + name);
                }
            }
        });
        System.out.println("PASS: " + checks + " enhancement checks. Isolated data: " + data);
    }

    private static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container next)
                layout(next);
    }
}
