package ui.panels;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import model.*;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

/** The operational pages share real saved data and a single visual system. */
public final class BusinessPages {
    public interface Refreshable {
        void refresh();
    }

    private static final ZoneId ZONE = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d · h:mm a");

    private static DefaultTableModel tableModel(String... names) {
        return new DefaultTableModel(names, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private static BigDecimal revenue(List<SavedSales.Sale> sales) {
        return sales.stream().map(SavedSales.Sale::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    private static JPanel metricRow() {
        return new JPanel(new GridLayout(1, 0, 16, 0)) {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(400, 132));
            }
        };
    }

    private static String shortId(String id) {
        return id.substring(0, Math.min(8, id.length())).toUpperCase(Locale.ROOT);
    }

    private static void fillOrders(DefaultTableModel model, List<SavedSales.Sale> sales) {
        model.setRowCount(0);
        for (SavedSales.Sale s : sales)
            model.addRow(new Object[] { "#" + shortId(s.id()), s.customer(),
                    DATE.format(s.created().atZoneSameInstant(ZONE)), s.payment(), OrderPricing.money(s.total()) });
    }

    private static void receipt(Component owner, SavedSales.Sale sale) {
        ReceiptView.show(owner, sale.receipt(), true);
    }

    private static void listen(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    public static class Reports extends PageLayout implements Refreshable {
        private final JComboBox<String> period = combo(
                new String[] { "Last 7 days", "Today", "Last 30 days", "All orders" });
        private final JTextField search = field("Search orders");
        private final JPanel metrics = metricRow(), tableHost = new JPanel(new BorderLayout());
        private final DefaultTableModel model = tableModel("ORDER", "CUSTOMER", "DATE & TIME", "PAYMENT", "TOTAL");
        private final JTable table = new JTable(model);
        private final JLabel status = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
        private List<SavedSales.Sale> sales = List.of(), filtered = List.of();

        public Reports() {
            super("BUSINESS INSIGHTS", "Sales & reports", "Every saved checkout, with the details that matter.");
            JButton export = button("Export CSV", AppIcon.Name.DOWNLOAD, false),
                    view = button("View receipt", AppIcon.Name.RECEIPT, true);
            actions.add(export);
            actions.add(view);
            JPanel top = new JPanel(new BorderLayout(0, 20));
            top.setOpaque(false);
            top.add(metrics, BorderLayout.NORTH);
            JPanel tools = new JPanel(new BorderLayout(12, 0));
            tools.setOpaque(false);
            search.putClientProperty("placeholder", "Search customer or order number…");
            tools.add(search, BorderLayout.CENTER);
            period.setPreferredSize(new Dimension(170, 44));
            tools.add(period, BorderLayout.EAST);
            top.add(tools, BorderLayout.SOUTH);
            body.add(top, BorderLayout.NORTH);
            styleTable(table);
            tableHost.setOpaque(false);
            body.add(tableHost, BorderLayout.CENTER);
            period.addActionListener(e -> filter());
            listen(search, this::filter);
            view.addActionListener(e -> {
                if (table.getSelectedRow() >= 0)
                    receipt(this, filtered.get(table.getSelectedRow()));
                else
                    status.setText("Select an order to open its receipt.");
            });
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
                        receipt(Reports.this, filtered.get(table.getSelectedRow()));
                }
            });
            export.addActionListener(e -> export());
            refresh();
        }

        private long generation;

        public void refresh() {
            long request = ++generation;
            new SwingWorker<SavedSales.Result, Void>() {
                protected SavedSales.Result doInBackground() throws Exception {
                    return persistence.Repositories.orders().read();
                }

                protected void done() {
                    if (request != generation)
                        return;
                    try {
                        var result = get();
                        sales = result.sales();
                        filter();
                        if (result.unreadable() > 0)
                            status.setText(result.unreadable() + " saved order files could not be read.");
                    } catch (Exception ex) {
                        status.setText("Saved orders could not be loaded.");
                    }
                }
            }.execute();
        }

        private void filter() {
            LocalDate now = LocalDate.now(ZONE), from = switch (period.getSelectedIndex()) {
            case 0 -> now.minusDays(6);
            case 1 -> now;
            case 2 -> now.minusDays(29);
            default -> LocalDate.MIN;
            };
            String term = search.getText().trim().toLowerCase(Locale.ROOT);
            filtered = sales.stream().filter(s -> !s.created().atZoneSameInstant(ZONE).toLocalDate().isBefore(from))
                    .filter(s -> (s.customer() + " " + s.id()).toLowerCase(Locale.ROOT).contains(term)).toList();
            metrics.removeAll();
            metrics.add(
                    metric("Total sales", OrderPricing.money(revenue(filtered)), "Selected period", AppIcon.Name.CASH));
            metrics.add(metric("Orders", Integer.toString(filtered.size()), "Saved checkouts", AppIcon.Name.RECEIPT));
            BigDecimal tax = filtered.stream().map(SavedSales.Sale::tax).reduce(BigDecimal.ZERO, BigDecimal::add);
            metrics.add(metric("Tax recorded", OrderPricing.money(tax), "From saved receipts", AppIcon.Name.CHART));
            fillOrders(model, filtered);
            tableHost.removeAll();
            tableHost
                    .add(filtered.isEmpty()
                            ? empty("No orders in this view", "Try a different period or complete your first checkout.",
                                    AppIcon.Name.CHART)
                            : tableCard("Order history", table, status));
            status.setText(filtered.size() + " orders · Totals reflect saved prices and tax.");
            revalidate();
            repaint();
        }

        private void export() {
            if (filtered.isEmpty()) {
                JOptionPane.showMessageDialog(this, "There are no orders in the current view.");
                return;
            }
            JFileChooser picker = new JFileChooser();
            picker.setSelectedFile(new File("sales-report.csv"));
            if (picker.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            Path path = picker.getSelectedFile().toPath();
            if (Files.exists(path) && JOptionPane.showConfirmDialog(this, "Replace this file?", "Export report",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
                return;
            List<SavedSales.Sale> snapshot = List.copyOf(filtered);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try (BufferedWriter out = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                        out.write("Order,Customer,Date,Payment,Subtotal,Tax,Total\n");
                        for (SavedSales.Sale s : snapshot)
                            out.write(csv(s.id()) + "," + csv(s.customer()) + "," + csv(s.created().toString()) + ","
                                    + csv(s.payment()) + "," + s.subtotal() + "," + s.tax() + "," + s.total() + "\n");
                    }
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        status.setText("Report exported.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Reports.this, "The report could not be exported.");
                    }
                }
            }.execute();
        }

        private String csv(String value) {
            String safe = value.matches("^[=+@\\-].*") ? "'" + value : value;
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
    }

    public static class Inventory extends PageLayout implements Refreshable {
        private final JTextField search = field("Search inventory");
        private final JPanel metrics = metricRow();
        private final DefaultTableModel model = tableModel("PRODUCT", "CATEGORY", "ON HAND", "LOW STOCK AT", "STATUS");
        private final JTable table = new JTable(model);
        private final JLabel status = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
        private List<CatalogItem> products = List.of(), filtered = List.of();
        private persistence.RepositoryProvider.Inventory stock;
        private final JComboBox<OperationsStore.Branch> branches = combo(
                new OperationsStore.Branch[] { new OperationsStore.Branch("main", "Main branch") });
        private boolean loadingBranches;
        private String requestedBranch, requestedItem;

        public void focusProduct(String branch, String item) {
            requestedBranch = branch;
            requestedItem = item;
            search.setText("");
            refresh();
        }

        private List<SavedSales.Sale> sales = List.of();

        public Inventory() {
            super("STOCK CONTROL", "Inventory", "Keep the essentials ready for your next customer.");
            JButton count = button("Update stock", AppIcon.Name.EDIT, true);
            count.setEnabled(security.LocalAuth.allowed(security.LocalAuth.Permission.STOCK_EDIT));
            actions.add(count);
            JPanel top = new JPanel(new BorderLayout(0, 20));
            top.setOpaque(false);
            top.add(metrics, BorderLayout.NORTH);
            search.putClientProperty("placeholder", "Search products or categories…");
            JPanel filters = new JPanel(new BorderLayout(12, 0));
            filters.setOpaque(false);
            filters.add(search, BorderLayout.CENTER);
            filters.add(branches, BorderLayout.EAST);
            top.add(filters, BorderLayout.SOUTH);
            branches.addActionListener(e -> {
                if (!loadingBranches)
                    refresh();
            });
            body.add(top, BorderLayout.NORTH);
            styleTable(table);
            body.add(tableCard("Product stock", table, status), BorderLayout.CENTER);
            listen(search, this::filter);
            count.addActionListener(e -> update());
            refresh();
        }

        private long generation;

        private record InventoryData(List<CatalogItem> items, persistence.RepositoryProvider.Inventory stock,
                List<SavedSales.Sale> sales, List<OperationsStore.Branch> branches, int unreadable) {
        }

        public void refresh() {
            String selected = requestedBranch != null ? requestedBranch
                    : branches.getSelectedItem() instanceof OperationsStore.Branch branch ? branch.id() : "main";
            long request = ++generation;
            new SwingWorker<InventoryData, Void>() {
                protected InventoryData doInBackground() throws Exception {
                    synchronized (persistence.LocalTransactions.LOCK) {
                        var orders = persistence.Repositories.orders().read();
                        return new InventoryData(
                                persistence.Repositories.catalog().items().stream().filter(CatalogItem::product)
                                        .toList(),
                                persistence.Repositories.inventory(selected), orders.sales(),
                                persistence.Repositories.operations().branches(), orders.unreadable());
                    }
                }

                protected void done() {
                    if (request != generation)
                        return;
                    try {
                        var result = get();
                        products = result.items();
                        stock = result.stock();
                        sales = result.sales();
                        loadingBranches = true;
                        try {
                            branches.removeAllItems();
                            for (var branch : result.branches()) {
                                branches.addItem(branch);
                                if (branch.id().equals(selected))
                                    branches.setSelectedItem(branch);
                            }
                        } finally {
                            loadingBranches = false;
                        }
                        if (result.unreadable() > 0) {
                            stock = null;
                            model.setRowCount(0);
                            status.setText("Unreadable orders prevent reliable stock balances. Review the data audit.");
                            return;
                        }
                        requestedBranch = null;
                        filter();
                        if (requestedItem != null) {
                            for (int row = 0; row < filtered.size(); row++)
                                if (filtered.get(row).id().equals(requestedItem)) {
                                    table.setRowSelectionInterval(row, row);
                                    table.scrollRectToVisible(table.getCellRect(row, 0, true));
                                    break;
                                }
                            requestedItem = null;
                        }
                    } catch (Exception ex) {
                        stock = null;
                        model.setRowCount(0);
                        status.setText("Inventory could not be loaded. Saved stock was not changed.");
                    }
                }
            }.execute();
        }

        private void filter() {
            if (stock == null)
                return;
            String term = search.getText().trim().toLowerCase(Locale.ROOT);
            filtered = products.stream()
                    .filter(i -> (i.name() + " " + i.category()).toLowerCase(Locale.ROOT).contains(term)).toList();
            model.setRowCount(0);
            int tracked = 0, low = 0;
            for (CatalogItem i : products) {
                if (stock.tracked(i.id())) {
                    tracked++;
                    if (stock.available(i.id(), sales).compareTo(stock.minimum(i.id())) <= 0)
                        low++;
                }
            }
            for (CatalogItem i : filtered) {
                boolean trackedItem = stock.tracked(i.id());
                BigDecimal available = stock.available(i.id(), sales);
                model.addRow(new Object[] { i.name(), i.category(),
                        trackedItem ? available.stripTrailingZeros().toPlainString() + " " + i.unit() : "—",
                        trackedItem ? stock.minimum(i.id()).stripTrailingZeros().toPlainString() : "—",
                        !trackedItem ? "Not tracked"
                                : available.signum() <= 0 ? "Out of stock"
                                        : available.compareTo(stock.minimum(i.id())) <= 0 ? "Low stock" : "In stock" });
            }
            metrics.removeAll();
            metrics.add(metric("Retail products", Integer.toString(products.size()), "From your catalog",
                    AppIcon.Name.BOX));
            metrics.add(metric("Tracked items", Integer.toString(tracked), "Stock counts enabled", AppIcon.Name.CHECK));
            metrics.add(metric("Need attention", Integer.toString(low), "At or below minimum", AppIcon.Name.WARNING));
            status.setText(products.isEmpty() ? "Add retail products in Catalog to manage their stock."
                    : "Select a product to record a physical count. Saved sales reduce tracked stock.");
            revalidate();
            repaint();
        }

        private void update() {
            int row = table.getSelectedRow();
            if (row < 0 || stock == null) {
                status.setText("Choose a product to update its stock.");
                return;
            }
            CatalogItem item = filtered.get(row);
            JTextField quantity = field("Counted stock"), minimum = field("Low stock threshold"),
                    note = field("Stock note");
            quantity.setText(
                    stock.tracked(item.id()) ? stock.available(item.id(), sales).max(BigDecimal.ZERO).toPlainString()
                            : "0");
            minimum.setText(stock.minimum(item.id()).toPlainString());
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 14));
            form.add(formField("Count on hand (" + item.unit() + ")", quantity));
            form.add(formField("Low stock threshold", minimum));
            form.add(formField("Reason / note", note));
            while (JOptionPane.showConfirmDialog(this, form, "Update " + item.name(), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                try {
                    BigDecimal count = new BigDecimal(quantity.getText().trim()),
                            min = new BigDecimal(minimum.getText().trim());
                    if (count.signum() < 0 || min.signum() < 0 || count.compareTo(new BigDecimal("999999")) > 0
                            || min.compareTo(new BigDecimal("999999")) > 0
                            || (item.unit() != CatalogItem.Unit.KG && (count.stripTrailingZeros().scale() > 0
                                    || min.stripTrailingZeros().scale() > 0)))
                        throw new IllegalArgumentException();
                    var stockToUpdate = stock;
                    String stockNote = note.getText().trim();
                    new SwingWorker<Void, Void>() {
                        protected Void doInBackground() throws Exception {
                            stockToUpdate.setCount(item.id(), count, min, stockNote);
                            return null;
                        }

                        protected void done() {
                            try {
                                get();
                                refresh();
                            } catch (Exception ex) {
                                status.setText("The stock count could not be saved.");
                            }
                        }
                    }.execute();
                    return;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Enter valid non-negative quantities using the item's unit.");
                }
            }
        }
    }

    public static class Team extends PageLayout implements Refreshable {
        private final persistence.RepositoryProvider.Team store = persistence.Repositories.team();
        private final Map<String, Avatar> avatars = new HashMap<>();
        private List<TeamStore.Member> members = List.of(), filtered = List.of();
        private final JTextField search = field("Search team");
        private final JPanel metrics = metricRow();
        private final JLabel status = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
        private final DefaultTableModel model = tableModel("TEAM MEMBER", "CONTACT", "ROLE", "STATUS");
        private final JTable table = new JTable(model);

        public Team() {
            super("THE PEOPLE BEHIND IT", "Your team", "Keep staff profiles and contact details organized.");
            JButton add = button("Add member", AppIcon.Name.PLUS, true),
                    edit = button("Edit profile", AppIcon.Name.EDIT, false);
            actions.add(edit);
            add.setEnabled(security.LocalAuth.allowed(security.LocalAuth.Permission.TEAM_EDIT));
            edit.setEnabled(security.LocalAuth.allowed(security.LocalAuth.Permission.TEAM_EDIT));
            actions.add(add);
            JPanel top = new JPanel(new BorderLayout(0, 20));
            top.setOpaque(false);
            top.add(metrics, BorderLayout.NORTH);
            search.putClientProperty("placeholder", "Search team members…");
            top.add(search, BorderLayout.SOUTH);
            body.add(top, BorderLayout.NORTH);
            styleTable(table);
            table.setRowHeight(70);
            table.getColumnModel().getColumn(0).setCellRenderer((view, value, selected, focus, row, col) -> {
                TeamStore.Member person = filtered.get(row);
                JLabel cell = label(person.name(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
                cell.setIcon(avatars.computeIfAbsent(person.id() + person.image(),
                        key -> new Avatar(person, 38).onLoaded(table::repaint)));
                cell.setIconTextGap(12);
                cell.setOpaque(true);
                cell.setBackground(selected ? UIStyle.tint() : Color.WHITE);
                cell.setBorder(new EmptyBorder(10, 16, 10, 16));
                return cell;
            });
            body.add(tableCard("Staff profiles", table, status), BorderLayout.CENTER);
            listen(search, this::filter);
            add.addActionListener(e -> edit(null));
            edit.addActionListener(e -> {
                if (table.getSelectedRow() >= 0)
                    edit(filtered.get(table.getSelectedRow()));
                else
                    status.setText("Choose a team member first.");
            });
            refresh();
        }

        public void refresh() {
            try {
                members = store.read();
                filter();
            } catch (Exception ex) {
                status.setText("Staff profiles could not be loaded. The saved file was not changed.");
            }
        }

        private void filter() {
            String term = search.getText().trim().toLowerCase(Locale.ROOT);
            filtered = members.stream().filter(m -> (m.name() + " " + m.role()).toLowerCase(Locale.ROOT).contains(term))
                    .toList();
            model.setRowCount(0);
            for (TeamStore.Member m : filtered)
                model.addRow(new Object[] { m.name(), m.contact(), m.role(), m.active() ? "Active" : "Inactive" });
            metrics.removeAll();
            metrics.add(metric("Team members", Integer.toString(members.size()), "Saved staff profiles",
                    AppIcon.Name.USERS));
            metrics.add(metric("Active", Long.toString(members.stream().filter(TeamStore.Member::active).count()),
                    "Current team", AppIcon.Name.CHECK));
            metrics.add(metric("Owners & managers",
                    Long.toString(members.stream().filter(m -> !m.role().equals("Cashier")).count()), "Profile roles",
                    AppIcon.Name.SHIELD));
            status.setText("Profiles organize staff details; sign-in permissions are not configured yet.");
            revalidate();
            repaint();
        }

        private void edit(TeamStore.Member member) {
            boolean current = member != null && member.id().equals(ActiveProfile.current().id());
            new ui.frames.ProfileEditor(SwingUtilities.getWindowAncestor(this), member, current, this::refresh)
                    .setVisible(true);
        }
    }

    private BusinessPages() {
    }
}
