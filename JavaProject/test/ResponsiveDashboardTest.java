import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import model.*;
import ui.frames.*;
import ui.panels.*;
import util.config.*;

/**
 * Uses Swing's native validation, rather than the preview renderer's recursive
 * layout helper.
 */
public class ResponsiveDashboardTest {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value)
            throw new AssertionError(message);
    }

    private static Object field(Object target, String name) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Component named(Container parent, String name) {
        for (Component c : parent.getComponents()) {
            if (name.equals(c.getName()))
                return c;
            if (c instanceof Container nested) {
                Component found = named(nested, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private static Rectangle bounds(Component component, Container parent) {
        return SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), parent);
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("laundry-responsive-");
        System.setProperty("laundry.dataDir", root.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", root.resolve("missing.properties").toString());
        JFrame[] frame = { null };
        WorkspacePanel[] workspace = { null };
        SwingUtilities.invokeAndWait(() -> {
            UIStyle.installDefaults();
            frame[0] = new JFrame();
            workspace[0] = new WorkspacePanel();
            frame[0].setContentPane(workspace[0]);
            frame[0].pack();
            frame[0].setSize(1366, 768);
            frame[0].validate();
        });
        try {
            long deadline = System.currentTimeMillis() + 15000;
            boolean ready = false;
            while (System.currentTimeMillis() < deadline) {
                boolean[] settled = { false };
                SwingUtilities.invokeAndWait(
                        () -> settled[0] = !((AnalyticsDashboard) workspace[0].page("Overview")).isLoading());
                if (settled[0]) {
                    ready = true;
                    break;
                }
                Thread.sleep(30);
            }
            check(ready, "Dashboard data settles in a native Swing hierarchy");
            for (Dimension size : new Dimension[] { new Dimension(1366, 768), new Dimension(1024, 650),
                    new Dimension(940, 680) }) {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        frame[0].setSize(size);
                        frame[0].invalidate();
                        frame[0].validate();
                        var dashboard = (AnalyticsDashboard) workspace[0].page("Overview");
                        dashboard.revalidate();
                        frame[0].validate();
                        JPanel stack = (JPanel) field(dashboard, "stack"),
                                metrics = (JPanel) field(dashboard, "metrics"),
                                finance = (JPanel) field(dashboard, "finances");
                        Component trend = (Component) field(dashboard, "trend"),
                                recent = (Component) field(dashboard, "recent");
                        for (Component card : metrics.getComponents())
                            check(card.getWidth() > 120 && card.getHeight() > 100,
                                    "Metric cards have real child bounds after native validation at " + size);
                        Rectangle metricRect = bounds(metrics, stack), trendRect = bounds(trend, stack),
                                financeRect = bounds(finance, stack), recentRect = bounds(recent, stack);
                        check(trendRect.y >= metricRect.y + metricRect.height + 12
                                && financeRect.y >= trendRect.y + trendRect.height,
                                "Charts and secondary totals do not overlap at " + size);
                        check(recentRect.y > financeRect.y + financeRect.height && recent.getWidth() > 400,
                                "Recent orders retain a full-width readable section at " + size);
                        check(!((JPanel) field(dashboard, "dateFilters")).isVisible(),
                                "Preset ranges do not show redundant date controls");
                        var sidebar = (JPanel) field(workspace[0], "sidebar");
                        check(size.width >= 1180 ? sidebar.getWidth() > 180 : sidebar.getWidth() < 100,
                                "Navigation collapses automatically on laptop widths");
                        JScrollPane navigation = (JScrollPane) named(workspace[0], "sidebar-scroll");
                        check(!navigation.getVerticalScrollBar().isOpaque(),
                                "Sidebar scrollbar has no white track background");
                        BufferedImage image = new BufferedImage(workspace[0].getWidth(), workspace[0].getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = image.createGraphics();
                        workspace[0].printAll(g);
                        g.dispose();
                        if (navigation.getVerticalScrollBar().isVisible()) {
                            JScrollBar bar = navigation.getVerticalScrollBar();
                            Point point = SwingUtilities.convertPoint(bar, bar.getWidth() / 2, bar.getHeight() / 3,
                                    workspace[0]);
                            Color pixel = new Color(image.getRGB(point.x, point.y));
                            check(Math.max(pixel.getRed(), Math.max(pixel.getGreen(), pixel.getBlue())) < 160,
                                    "Actual painted sidebar gutter stays dark");
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> frame[0].dispose());
        }
        System.out.println(
                "PASS: " + checks + " native dashboard validation, responsive layout and sidebar rendering checks.");
    }
}
