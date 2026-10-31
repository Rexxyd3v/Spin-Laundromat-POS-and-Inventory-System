package ui.panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.*;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

public class AnalyticsDashboard extends PageLayout implements BusinessPages.Refreshable {
    private final JComboBox<String> period = combo(
            new String[] { "Last 7 days", "Last 30 days", "Last 90 days", "Custom range" });
    private final JPanel metrics = new JPanel(new GridLayout(1, 4, 16, 16));
    private final Surface trend = new Surface(), payments = new Surface(), top = new Surface(), health = new Surface(),
            recent = new Surface();
    private final JLabel greeting = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY),
            status = label("Loading saved sales…", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final JPanel dateFilters = new JPanel(new GridLayout(1, 3, 12, 0));
    private final Surface finances = new Surface();
    private final JPanel financeValues = new JPanel(new GridLayout(1, 3, 20, 0));
    private final JTextField dateFrom = field("From date"), dateTo = field("To date");

    private record DashboardSnapshot(SavedSales.Result sales, int items, int products, int low, int team, long transit,
            long reserved, long running) {
    }

    private DashboardSnapshot healthData;
    private final DashboardStack stack = new DashboardStack();
    private SalesAnalytics.Window data;
    private long generation;
    private final JButton refresh = iconButton("Refresh analytics", AppIcon.Name.REFRESH);
    private final JButton start = button("New order", AppIcon.Name.PLUS, true);
    private List<SavedSales.Sale> all = List.of();

    public AnalyticsDashboard() {
        super("STORE PERFORMANCE", "Analytics overview", "A clearer view of your sales, customers and daily activity.");
        actions.add(period);
        actions.add(refresh);
        actions.add(start);
        period.setPreferredSize(new Dimension(152, 42));
        period.addActionListener(e -> renderData());
        refresh.addActionListener(e -> refresh());
        start.addActionListener(e -> AppEvents.fire("navigate-pos"));
        metrics.setOpaque(false);
        stack.setOpaque(false);
        stack.add(greeting);
        stack.add(metrics);
        dateFilters.setOpaque(false);
        dateFilters.setVisible(false);
        financeValues.setOpaque(false);
        finances.setBorder(new EmptyBorder(16, 20, 16, 20));
        finances.add(financeValues, BorderLayout.CENTER);
        dateFrom.setName("analyticsFrom");
        dateTo.setName("analyticsTo");
        dateFrom.setText(LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(6).toString());
        dateTo.setText(LocalDate.now(ZoneId.of("Asia/Manila")).toString());
        dateFilters.add(formField("From · yyyy-MM-dd", dateFrom));
        dateFilters.add(formField("To · yyyy-MM-dd", dateTo));
        JButton apply = button("Apply range", AppIcon.Name.CHECK, false);
        apply.addActionListener(e -> renderData());
        dateFilters.add(apply);
        stack.add(dateFilters);
        stack.add(finances);
        stack.add(trend);
        stack.add(payments);
        stack.add(top);
        stack.add(health);
        stack.add(recent);
        stack.add(status);
        body.add(stack, BorderLayout.CENTER);
        for (int i = 0; i < 4; i++)
            metrics.add(
                    metricCard(new String[] { "Total revenue", "Paid orders", "Average order", "Repeat customers" }[i],
                            "—", "Loading saved orders", i == 0, AppIcon.Name.CHART));
        refresh();
    }

    public boolean isLoading() {
        return !refresh.isEnabled();
    }

    public SalesAnalytics.Window window() {
        return data;
    }

    @Override
    public void refresh() {
        long request = ++generation;
        refresh.setEnabled(false);
        status.setText("Updating analytics…");
        new SwingWorker<DashboardSnapshot, Void>() {
            protected DashboardSnapshot doInBackground() throws Exception {
                synchronized (persistence.LocalTransactions.LOCK) {
                    var sales = persistence.Repositories.orders().read();
                    try {
                        var items = persistence.Repositories.catalog().items();
                        var ops = persistence.Repositories.operations();
                        return new DashboardSnapshot(sales, items.size(),
                                (int) items.stream().filter(CatalogItem::product).count(),
                                sales.unreadable() > 0 ? -1 : application.StockAlerts.read().size(),
                                persistence.Repositories.team().read().size(),
                                ops.transfers().stream()
                                        .filter(t -> t.state() == OperationsStore.TransferState.DISPATCHED).count(),
                                ops.bookings().stream().filter(b -> b.state() == OperationsStore.BookingState.RESERVED)
                                        .count(),
                                ops.bookings().stream().filter(b -> b.state() == OperationsStore.BookingState.RUNNING)
                                        .count());
                    } catch (Exception operationalDataIssue) {
                        return new DashboardSnapshot(sales, -1, -1, -1, -1, -1, -1, -1);
                    }

                }
            }

            protected void done() {
                if (request != generation)
                    return;
                refresh.setEnabled(true);
                try {
                    healthData = get();
                    SavedSales.Result result = healthData.sales();
                    all = result.sales();
                    renderData();
                    status.setText(result.unreadable() > 0
                            ? result.unreadable() + " order files could not be included. Review your saved data."
                            : "Based on saved, completed checkouts · Updated " + LocalTime.now(ZoneId.of("Asia/Manila"))
                                    .format(DateTimeFormatter.ofPattern("h:mm a")));
                } catch (Exception ex) {
                    status.setText("Analytics could not be loaded. Saved orders have not been changed.");
                }
            }
        }.execute();
    }

    private void renderData() {
        int days;
        LocalDate end;
        try {
            boolean custom = period.getSelectedIndex() == 3;
            dateFilters.setVisible(custom);
            dateFrom.setEnabled(custom);
            dateTo.setEnabled(custom);
            end = custom ? LocalDate.parse(dateTo.getText().trim()) : LocalDate.now(ZoneId.of("Asia/Manila"));
            days = custom ? Math.toIntExact(
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(dateFrom.getText().trim()), end) + 1)
                    : new int[] { 7, 30, 90 }[period.getSelectedIndex()];
            if (days < 1 || days > 366)
                throw new IllegalArgumentException();
            data = SalesAnalytics.calculate(all, days, end);
            if (!custom) {
                dateFrom.setText(data.start().toString());
                dateTo.setText(data.end().toString());
            }
        } catch (RuntimeException ex) {
            status.setText("Choose valid dates in yyyy-MM-dd format, from 1 to 366 inclusive days.");
            return;
        }
        financeValues.removeAll();
        BigDecimal base = data.orders().stream().map(SavedSales.Sale::subtotal).reduce(BigDecimal.ZERO,
                BigDecimal::add),
                tax = data.orders().stream().map(SavedSales.Sale::tax).reduce(BigDecimal.ZERO, BigDecimal::add);
        financeValues.add(financeValue("Sales excluding tax", OrderPricing.money(base), "Confirmed sales before tax"));
        financeValues.add(financeValue("Tax recorded", OrderPricing.money(tax), "From receipt snapshots"));
        financeValues.add(financeValue("Cash collections",
                OrderPricing.money(data.payments().getOrDefault("Cash", BigDecimal.ZERO)), "Fully paid cash sales"));
        TeamStore.Member person = ActiveProfile.current();
        greeting.setText("Hello, " + person.name() + ". Here is your store from "
                + data.start().format(DateTimeFormatter.ofPattern("MMM d")) + " to "
                + data.end().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + ".");
        metrics.removeAll();
        metrics.add(metricCard("Total revenue", OrderPricing.money(data.revenue()),
                SalesAnalytics.change(data.revenue(), data.previousRevenue()), true, AppIcon.Name.CASH));
        metrics.add(metricCard("Paid orders", Integer.toString(data.orders().size()),
                data.previous().size() + " in the previous period", false, AppIcon.Name.RECEIPT));
        metrics.add(metricCard("Average order", OrderPricing.money(data.average()),
                SalesAnalytics.change(data.average(), data.previousAverage()), false, AppIcon.Name.CHART));
        metrics.add(metricCard("Repeat customers", Integer.toString(data.returning()),
                data.customers() + " identified customers this period", false, AppIcon.Name.USERS));
        trend.removeAll();
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JPanel headings = column();
        headings.add(label("Revenue trend", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        headings.add(Box.createVerticalStrut(6));
        headings.add(label("Paid sales compared with the previous " + days + " days", UIStyle.SMALL,
                UIStyle.TEXT_SECONDARY));
        title.add(headings, BorderLayout.CENTER);
        title.add(label(OrderPricing.money(data.revenue()), UIStyle.SUBTITLE, UIStyle.PRIMARY), BorderLayout.EAST);
        trend.add(title, BorderLayout.NORTH);
        trend.add(new RevenueChart(data), BorderLayout.CENTER);
        JPanel legend = row(16);
        legend.add(legend("Selected period", UIStyle.PRIMARY));
        legend.add(legend("Previous period", new Color(161, 174, 194)));
        trend.add(legend, BorderLayout.SOUTH);
        renderPayments();
        renderTop();
        renderHealth();
        renderRecent();
        stack.revalidate();
        stack.repaint();
        body.revalidate();
        revalidate();
        repaint();
    }

    private JPanel metricCard(String title, String value, String hint, boolean primary, AppIcon.Name icon) {
        Surface card = new Surface() {
            protected void paintComponent(Graphics graphics) {
                if (!primary) {
                    super.paintComponent(graphics);
                    return;
                }
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(
                        new GradientPaint(0, 0, new Color(20, 36, 55), getWidth(), getHeight(), new Color(22, 73, 77)));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
            }
        };
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setLayout(new BorderLayout(0, 8));
        Color muted = primary ? new Color(183, 208, 212) : UIStyle.TEXT_SECONDARY;
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label(title, UIStyle.SMALL, muted), BorderLayout.CENTER);
        header.add(new JLabel(new AppIcon(icon, 18, primary ? new Color(153, 223, 207) : UIStyle.PRIMARY)),
                BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        JLabel amount = new JLabel(value) {
            protected void paintComponent(Graphics graphics) {
                int size = 28;
                while (size > 17 && graphics.getFontMetrics(new Font("Segoe UI", Font.BOLD, size))
                        .stringWidth(getText()) > getWidth())
                    size--;
                if (getFont().getSize() != size)
                    setFont(new Font("Segoe UI", Font.BOLD, size));
                super.paintComponent(graphics);
            }
        };
        amount.setFont(new Font("Segoe UI", Font.BOLD, 28));
        amount.setForeground(primary ? Color.WHITE : UIStyle.TEXT_PRIMARY);
        amount.setToolTipText(value);
        card.add(amount, BorderLayout.CENTER);
        JLabel note = label(hint, new Font("Segoe UI", Font.PLAIN, 11), muted);
        note.setToolTipText(hint);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JLabel legend(String text, Color color) {
        JLabel l = label(text, new Font("Segoe UI", Font.PLAIN, 11), UIStyle.TEXT_SECONDARY);
        l.setIcon(new AppIcon(AppIcon.Name.DOT, 12, color));
        l.setIconTextGap(5);
        return l;
    }

    private void renderPayments() {
        payments.removeAll();
        payments.add(sectionTitle("Payment mix", "How your customers paid"), BorderLayout.NORTH);
        payments.add(new PaymentRing(data), BorderLayout.CENTER);
        JPanel keys = column();
        if (data.payments().isEmpty())
            keys.add(label("No recorded payments in this period.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        int index = 0;
        for (var entry : data.payments().entrySet()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            Color color = index++ == 0 ? UIStyle.PRIMARY : new Color(107, 114, 205);
            row.add(legend(entry.getKey(), color), BorderLayout.WEST);
            String share = data.revenue().signum() == 0 ? "0"
                    : entry.getValue().multiply(BigDecimal.valueOf(100)).divide(data.revenue(), 0, RoundingMode.HALF_UP)
                            .toPlainString();
            row.add(label(share + "% · " + OrderPricing.money(entry.getValue()), UIStyle.SMALL, UIStyle.TEXT_PRIMARY),
                    BorderLayout.EAST);
            keys.add(row);
            keys.add(Box.createVerticalStrut(9));
        }
        payments.add(keys, BorderLayout.SOUTH);
    }

    private void renderTop() {
        top.removeAll();
        top.add(sectionTitle("Best sellers", "Ranked by item sales before tax"), BorderLayout.NORTH);
        JPanel rows = column();
        rows.setBorder(new EmptyBorder(16, 0, 0, 0));
        if (data.topItems().isEmpty()) {
            rows.add(label("Your best sellers will appear after checkout.", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        } else {
            BigDecimal max = data.topItems().get(0).revenue().max(BigDecimal.ONE);
            int rank = 1;
            for (var item : data.topItems()) {
                JPanel row = new JPanel(new BorderLayout(10, 6));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
                JLabel name = label(rank++ + ".  " + item.name(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
                name.setToolTipText(item.name());
                row.add(name, BorderLayout.CENTER);
                row.add(label(OrderPricing.money(item.revenue()), UIStyle.BOLD, UIStyle.TEXT_PRIMARY),
                        BorderLayout.EAST);
                JPanel bar = new JPanel() {
                    protected void paintComponent(Graphics g) {
                        g.setColor(UIStyle.BACKGROUND);
                        g.fillRoundRect(0, 0, getWidth(), 5, 5, 5);
                        g.setColor(UIStyle.PRIMARY);
                        g.fillRoundRect(0, 0, (int) (getWidth() * item.revenue().doubleValue() / max.doubleValue()), 5,
                                5, 5);
                    }
                };
                bar.setPreferredSize(new Dimension(100, 5));
                row.add(bar, BorderLayout.SOUTH);
                rows.add(row);
                rows.add(Box.createVerticalStrut(14));
            }
        }
        top.add(rows, BorderLayout.CENTER);
    }

    private void renderHealth() {
        health.removeAll();
        health.add(sectionTitle("Keep things moving", "Useful next steps for your store"), BorderLayout.NORTH);
        JPanel rows = column();
        rows.setBorder(new EmptyBorder(16, 0, 0, 0));
        if (healthData != null && healthData.items() >= 0) {
            rows.add(actionRow(
                    healthData.low() < 0 ? "Stock needs data review"
                            : healthData.low() > 0 ? healthData.low() + " stock warnings" : "Stock check",
                    "Review " + healthData.products() + " retail products", AppIcon.Name.BOX, "navigate-inventory"));
            rows.add(Box.createVerticalStrut(14));
            rows.add(actionRow("Transfers in transit", healthData.transit() + " dispatched transfers",
                    AppIcon.Name.REFRESH, "navigate-operations"));
            rows.add(Box.createVerticalStrut(14));
            rows.add(actionRow("Machine schedule",
                    healthData.reserved() + " reserved · " + healthData.running() + " running", AppIcon.Name.WASH,
                    "navigate-operations"));
            rows.add(Box.createVerticalStrut(14));
            rows.add(actionRow("Your team", healthData.team() + " saved staff profiles", AppIcon.Name.USERS,
                    "navigate-team"));
        } else
            rows.add(label("Store details are unavailable.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        health.add(scroll(rows), BorderLayout.CENTER);
    }

    private JPanel actionRow(String title, String hint, AppIcon.Name icon, String event) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.add(new JLabel(new AppIcon(icon, 22, UIStyle.PRIMARY)), BorderLayout.WEST);
        JPanel titles = column();
        titles.add(label(title, UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
        titles.add(Box.createVerticalStrut(4));
        titles.add(label(hint, UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        p.add(titles, BorderLayout.CENTER);
        JButton go = iconButton(title, AppIcon.Name.ARROW);
        go.addActionListener(e -> AppEvents.fire(event));
        p.add(go, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        return p;
    }

    private JPanel sectionTitle(String title, String hint) {
        JPanel p = column();
        p.add(label(title, UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(6));
        p.add(label(hint, UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        return p;
    }

    private void renderRecent() {
        recent.removeAll();
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(sectionTitle("Recent orders", "Latest checkouts in the selected period"), BorderLayout.CENTER);
        JButton view = button("View all orders", AppIcon.Name.ARROW, false);
        view.addActionListener(e -> AppEvents.fire("navigate-reports"));
        heading.add(view, BorderLayout.EAST);
        recent.add(heading, BorderLayout.NORTH);
        if (data.orders().isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.add(
                    PageLayout.empty("Ready for your first sale?",
                            "Create an order and your activity will appear here.", AppIcon.Name.RECEIPT),
                    BorderLayout.CENTER);
            JButton begin = button("Start an order", AppIcon.Name.PLUS, true);
            begin.addActionListener(e -> AppEvents.fire("navigate-pos"));
            JPanel action = row(0);
            action.add(begin);
            empty.add(action, BorderLayout.SOUTH);
            recent.add(empty, BorderLayout.CENTER);
            return;
        }
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ORDER", "CUSTOMER", "WHEN", "PAYMENT", "TOTAL" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        List<SavedSales.Sale> latest = data.orders().stream().limit(6).toList();
        for (var sale : latest)
            model.addRow(new Object[] { "#" + sale.id().substring(0, 8).toUpperCase(), sale.customer(),
                    sale.created().atZoneSameInstant(ZoneId.of("Asia/Manila"))
                            .format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                    sale.payment(), OrderPricing.money(sale.total()) });
        JTable table = new JTable(model);
        PageLayout.styleTable(table);
        table.setRowHeight(50);
        JScrollPane scroll = scroll(table);
        scroll.setBorder(new EmptyBorder(18, 0, 0, 0));
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
                    showReceipt(latest.get(table.getSelectedRow()));
            }
        });
        recent.add(scroll, BorderLayout.CENTER);
    }

    private void showReceipt(SavedSales.Sale sale) {
        ReceiptView.show(this, sale.receipt(), true);
    }

    private JPanel financeValue(String title, String amount, String hint) {
        JPanel p = column();
        p.add(label(title, UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        p.add(Box.createVerticalStrut(8));
        JLabel value = label(amount, new Font("Segoe UI", Font.BOLD, 22), UIStyle.TEXT_PRIMARY);
        value.setToolTipText(hint);
        p.add(value);
        return p;
    }

    /**
     * Standard layout managers size every child; resize changes only the column
     * count.
     */
    private class DashboardStack extends JPanel {
        private final JPanel charts = new JPanel(), details = new JPanel();
        private int metricColumns = -1, sectionColumns = -1, financeColumns = -1, lastRecent = -1;

        DashboardStack() {
            setLayout(new GridBagLayout());
            charts.setOpaque(false);
            details.setOpaque(false);
        }

        private void arrange() {
            int width = getWidth() > 0 ? getWidth() : 900;
            int columns = width >= 920 ? 4 : width >= 500 ? 2 : 1;
            int sections = width >= 900 ? 2 : 1, financeCount = width >= 500 ? 3 : 1,
                    recentCount = data == null ? 0 : data.orders().size();
            if (columns == metricColumns && sections == sectionColumns && financeCount == financeColumns
                    && recentCount == lastRecent && charts.getParent() == this)
                return;
            if (columns != metricColumns) {
                metrics.setLayout(new GridLayout(0, columns, 16, 16));
                metricColumns = columns;
            }
            metrics.setPreferredSize(new Dimension(1, (4 / columns) * 126 + (4 / columns - 1) * 16));

            if (sections != sectionColumns) {
                charts.setLayout(new GridBagLayout());
                details.setLayout(new GridLayout(0, sections, 18, 18));
                charts.removeAll();
                details.removeAll();
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.weighty = 1;
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = sections == 2 ? 0.66 : 1;
                c.insets = new Insets(0, 0, sections == 1 ? 18 : 0, sections == 2 ? 18 : 0);
                charts.add(trend, c);
                c.gridx = sections == 2 ? 1 : 0;
                c.gridy = sections == 2 ? 0 : 1;
                c.weightx = sections == 2 ? 0.34 : 1;
                c.insets = new Insets(0, 0, 0, 0);
                charts.add(payments, c);
                details.add(top);
                details.add(health);
                sectionColumns = sections;
            }
            charts.setPreferredSize(new Dimension(1, sections == 2 ? 330 : 620));
            details.setPreferredSize(new Dimension(1, sections == 2 ? 340 : 680));
            finances.setPreferredSize(new Dimension(1, width >= 500 ? 88 : 192));
            financeValues.setLayout(new GridLayout(width >= 500 ? 1 : 3, width >= 500 ? 3 : 1, 20, 12));
            financeColumns = financeCount;
            lastRecent = recentCount;
            dateFilters.setPreferredSize(new Dimension(1, 62));
            recent.setPreferredSize(new Dimension(1,
                    data == null || data.orders().isEmpty() ? 260 : Math.min(420, 170 + data.orders().size() * 50)));
            if (greeting.getParent() != this || charts.getParent() != this) {
                removeAll();
                JComponent[] rows = { greeting, dateFilters, metrics, charts, finances, details, recent, status };
                for (int i = 0; i < rows.length; i++) {
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = i;
                    c.weightx = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    c.insets = new Insets(0, 0, i == rows.length - 1 ? 0 : 18, 0);
                    add(rows[i], c);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            arrange();
            Dimension size = super.getPreferredSize();
            return new Dimension(1, size.height);
        }

        @Override
        public void doLayout() {
            arrange();
            super.doLayout();
        }
    }

    private static class PaymentRing extends JComponent {
        final SalesAnalytics.Window data;

        PaymentRing(SalesAnalytics.Window data) {
            this.data = data;
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.max(50, Math.min(getWidth() - 30, getHeight() - 24)), x = (getWidth() - size) / 2,
                    y = (getHeight() - size) / 2;
            g.setStroke(new BasicStroke(20, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setColor(UIStyle.INPUT_BORDER);
            g.draw(new Ellipse2D.Double(x + 12, y + 12, size - 24, size - 24));
            double angle = 90;
            int index = 0;
            for (var payment : data.payments().entrySet()) {
                double sweep = data.revenue().signum() == 0 ? 0
                        : payment.getValue().doubleValue() / data.revenue().doubleValue() * 360;
                g.setColor(index++ == 0 ? UIStyle.PRIMARY : new Color(107, 114, 205));
                g.draw(new Arc2D.Double(x + 12, y + 12, size - 24, size - 24, angle, -sweep, Arc2D.OPEN));
                angle -= sweep;
            }
            g.setColor(UIStyle.TEXT_PRIMARY);
            g.setFont(new Font("Segoe UI", Font.BOLD, 26));
            String count = Integer.toString(data.orders().size());
            g.drawString(count, (getWidth() - g.getFontMetrics().stringWidth(count)) / 2, getHeight() / 2 + 3);
            g.setFont(UIStyle.SMALL);
            g.setColor(UIStyle.TEXT_SECONDARY);
            String label = "paid orders";
            g.drawString(label, (getWidth() - g.getFontMetrics().stringWidth(label)) / 2, getHeight() / 2 + 24);
            g.dispose();
        }
    }

    private static class RevenueChart extends JComponent {
        final SalesAnalytics.Window data;
        int hover = -1;
        final int left = 64, right = 20, top = 32, bottom = 38;

        RevenueChart(SalesAnalytics.Window data) {
            this.data = data;
            setToolTipText("");
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    hover = Math.max(0, Math.min(data.days().size() - 1, (int) Math.round(
                            (e.getX() - left) * (data.days().size() - 1.0) / Math.max(1, getWidth() - left - right))));
                    SalesAnalytics.Day day = data.days().get(hover);
                    setToolTipText(day.date().format(DateTimeFormatter.ofPattern("MMM d")) + " · "
                            + OrderPricing.money(day.revenue()) + " · " + day.orders() + " orders");
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = Math.max(1, getWidth() - left - right), h = Math.max(1, getHeight() - top - bottom);
            double max = java.util.stream.Stream.concat(data.days().stream(), data.previousDays().stream())
                    .mapToDouble(d -> d.revenue().doubleValue()).max().orElse(0);
            max = max == 0 ? 100
                    : Math.ceil(max / Math.pow(10, Math.floor(Math.log10(max))))
                            * Math.pow(10, Math.floor(Math.log10(max)));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < 5; i++) {
                int y = top + h * i / 4;
                g.setColor(UIStyle.INPUT_BORDER);
                g.drawLine(left, y, left + w, y);
                g.setColor(UIStyle.TEXT_SECONDARY);
                double amount = max * (4 - i) / 4;
                String text = amount >= 1000 ? String.format(Locale.US, "₱%.1fk", amount / 1000)
                        : String.format(Locale.US, "₱%.0f", amount);
                g.drawString(text, left - 10 - g.getFontMetrics().stringWidth(text), y + 4);
            }
            int n = data.days().size();
            Path2D line = path(data.days(), max, w, h);
            Path2D area = (Path2D) line.clone();
            area.lineTo(left + w, top + h);
            area.lineTo(left, top + h);
            area.closePath();
            g.setPaint(new GradientPaint(0, top,
                    new Color(UIStyle.PRIMARY.getRed(), UIStyle.PRIMARY.getGreen(), UIStyle.PRIMARY.getBlue(), 45), 0,
                    top + h, new Color(255, 255, 255, 0)));
            g.fill(area);
            g.setColor(new Color(160, 174, 194));
            g.setStroke(
                    new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] { 4, 5 }, 0));
            g.draw(path(data.previousDays(), max, w, h));
            g.setColor(UIStyle.PRIMARY);
            g.setStroke(new BasicStroke(2.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(line);
            int tick = n > 10 ? 5 : 1;
            for (int i = 0; i < n; i++) {
                int x = left + (int) (w * i / Math.max(1, n - 1.0));
                if (i % tick == 0 || i == n - 1) {
                    String day = data.days().get(i).date()
                            .format(DateTimeFormatter.ofPattern(n > 10 ? "MMM d" : "EEE"));
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g.setColor(UIStyle.TEXT_SECONDARY);
                    g.drawString(day, x - g.getFontMetrics().stringWidth(day) / 2, top + h + 23);
                }
                if (n <= 7) {
                    int y = top + h - (int) (data.days().get(i).revenue().doubleValue() / max * h);
                    g.setColor(Color.WHITE);
                    g.fillOval(x - 4, y - 4, 8, 8);
                    g.setColor(UIStyle.PRIMARY);
                    g.drawOval(x - 4, y - 4, 8, 8);
                }
            }
            if (hover >= 0) {
                int x = left + (int) (w * hover / Math.max(1, n - 1.0)),
                        y = top + h - (int) (data.days().get(hover).revenue().doubleValue() / max * h);
                g.setColor(new Color(148, 163, 184));
                g.setStroke(new BasicStroke(1));
                g.drawLine(x, top, x, top + h);
                g.setColor(UIStyle.PRIMARY);
                g.fillOval(x - 5, y - 5, 10, 10);
            }
            if (data.revenue().signum() == 0) {
                g.setFont(UIStyle.BOLD);
                g.setColor(UIStyle.TEXT_SECONDARY);
                String empty = "No paid sales in this period";
                g.drawString(empty, left + (w - g.getFontMetrics().stringWidth(empty)) / 2, top + h / 2);
            }
            g.dispose();
        }

        private Path2D path(List<SalesAnalytics.Day> days, double max, int w, int h) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < days.size(); i++) {
                double x = left + w * i / Math.max(1, days.size() - 1.0),
                        y = top + h - days.get(i).revenue().doubleValue() / max * h;
                if (i == 0)
                    path.moveTo(x, y);
                else
                    path.lineTo(x, y);
            }
            return path;
        }
    }
}
