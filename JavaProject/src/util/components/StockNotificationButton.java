package util.components;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import application.StockAlerts;
import util.config.*;
import static util.components.ModernUI.*;

/**
 * Live stock flyout with branch/severity filters. Warnings remain until stock
 * is replenished.
 */
public final class StockNotificationButton extends JButton {
    private final Map<String, String> knownBranches = new TreeMap<>();
    private List<StockAlerts.Alert> alerts = List.of();
    private String error, filter = "All", branch = "";
    private boolean loading = true;
    private long generation;
    private LocalTime updated;
    private JPanel displayed;
    private JPopupMenu popup;
    private SwingWorker<List<StockAlerts.Alert>, Void> worker;
    private final javax.swing.Timer debounce = new javax.swing.Timer(180, e -> refresh());
    private final PropertyChangeListener changes = e -> {
        if (Set.of("stock", "orders", "catalog", "transfers", "branches").contains(e.getPropertyName()))
            debounce.restart();
    };

    private record BranchChoice(String id, String name) {
        public String toString() {
            return name;
        }
    }

    public StockNotificationButton() {
        super("…");
        setUI(new javax.swing.plaf.basic.BasicButtonUI());
        setName("stockNotifications");
        setPreferredSize(new Dimension(48, 40));
        setMinimumSize(new Dimension(48, 40));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Stock notifications");
        debounce.setRepeats(false);
        addActionListener(e -> open());
        refresh();
    }

    public List<StockAlerts.Alert> alerts() {
        return alerts;
    }

    public boolean isLoading() {
        return loading;
    }

    public void refresh() {
        long request = ++generation;
        loading = true;
        if (worker != null)
            worker.cancel(true);
        refreshContent();
        worker = new SwingWorker<List<StockAlerts.Alert>, Void>() {
            protected List<StockAlerts.Alert> doInBackground() throws Exception {
                return StockAlerts.read();
            }

            protected void done() {
                if (request != generation)
                    return;
                loading = false;
                try {
                    alerts = get();
                    for (var alert : alerts)
                        knownBranches.put(alert.branchId(), alert.branch());
                    error = null;
                    updated = LocalTime.now(ZoneId.of("Asia/Manila"));
                    setText(Integer.toString(alerts.size()));
                    getAccessibleContext().setAccessibleName(alerts.size() + " stock alerts");
                    setToolTipText(alerts.isEmpty() ? "No stock alerts" : alerts.size() + " stock alerts");
                } catch (Exception ex) {
                    error = "Stock balances could not be verified. Review inventory or run the data audit, then refresh.";
                    setText("!");
                    getAccessibleContext().setAccessibleName("Stock alerts unavailable");
                    setToolTipText("Stock alerts unavailable — click to retry");
                }
                refreshContent();
                repaint();
            }
        };
        worker.execute();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getModel().isRollover() ? UIStyle.BACKGROUND : Color.WHITE);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        g.setColor(isFocusOwner() ? UIStyle.PRIMARY : UIStyle.INPUT_BORDER);
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
        AppIcon.draw(AppIcon.Name.BELL, g, (getWidth() - 21) / 2, (getHeight() - 21) / 2, 21, UIStyle.TEXT_SECONDARY);
        String count = error != null ? "!"
                : alerts.isEmpty() ? null : alerts.size() > 99 ? "99+" : Integer.toString(alerts.size());
        if (count != null) {
            int w = count.length() > 2 ? 25 : 18, x = getWidth() - w, y = 1;
            g.setColor(error != null || alerts.stream().anyMatch(a -> a.available().signum() <= 0) ? UIStyle.DANGER
                    : new Color(180, 83, 9));
            g.fillRoundRect(x, y, w, 18, 18, 18);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            g.drawString(count, x + (w - g.getFontMetrics().stringWidth(count)) / 2, y + 13);
        }
        g.dispose();
    }

    public JPanel notificationContent() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 18, 16, 18));
        displayed = root;
        render(root);
        return root;
    }

    private void refreshContent() {
        if (displayed != null) {
            render(displayed);
            displayed.revalidate();
            displayed.repaint();
            if (popup != null && popup.isVisible())
                popup.pack();
        }
    }

    private JLabel plain(String text, Font font, Color color) {
        JLabel label = label(text, font, color);
        label.putClientProperty("html.disable", true);
        label.setToolTipText(text);
        return label;
    }

    private void render(JPanel root) {
        root.removeAll();
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        JPanel title = column();
        title.add(label("Notifications", new Font("Segoe UI", Font.BOLD, 20), UIStyle.TEXT_PRIMARY));
        title.add(Box.createVerticalStrut(4));
        title.add(label("Inventory that needs attention", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        header.add(title, BorderLayout.CENTER);
        JPanel tools = row(6);
        JButton reload = iconButton("Refresh notifications", AppIcon.Name.REFRESH),
                close = iconButton("Close notifications", AppIcon.Name.CLOSE);
        reload.setEnabled(!loading);
        reload.addActionListener(e -> refresh());
        close.addActionListener(e -> {
            if (popup != null)
                popup.setVisible(false);
        });
        tools.add(reload);
        tools.add(close);
        header.add(tools, BorderLayout.EAST);
        JPanel filters = new JPanel(new BorderLayout(0, 10));
        filters.setOpaque(false);
        JPanel chips = new JPanel(new GridLayout(1, 3, 6, 0));
        chips.setOpaque(false);
        for (String value : new String[] { "All", "Low stock", "Out of stock" }) {
            long count = alerts.stream().filter(a -> branch.isBlank() || a.branchId().equals(branch))
                    .filter(a -> value.equals("All") || a.severity().equals(value)).count();
            JButton chip = button(value + " (" + count + ")", null, filter.equals(value));
            chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
            chip.setBorder(new EmptyBorder(9, 5, 9, 5));
            chip.setName("notificationFilter" + value.replace(" ", ""));
            chip.addActionListener(e -> {
                filter = value;
                refreshContent();
            });
            chips.add(chip);
        }
        filters.add(chips, BorderLayout.NORTH);
        JComboBox<BranchChoice> branchPicker = combo(new BranchChoice[] { new BranchChoice("", "All branches") });
        branchPicker.setName("notificationBranchFilter");
        TreeMap<String, String> branches = new TreeMap<>(knownBranches);
        for (var a : alerts)
            branches.put(a.branchId(), a.branch());
        BranchChoice selected = (BranchChoice) branchPicker.getItemAt(0);
        for (var entry : branches.entrySet()) {
            var option = new BranchChoice(entry.getKey(), entry.getValue());
            branchPicker.addItem(option);
            if (entry.getKey().equals(branch))
                selected = option;
        }
        if (!branches.containsKey(branch) && !branch.isBlank())
            branch = "";
        branchPicker.setSelectedItem(selected);
        branchPicker.addActionListener(e -> {
            branch = ((BranchChoice) branchPicker.getSelectedItem()).id();
            refreshContent();
        });
        filters.add(branchPicker, BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);
        JPanel rows = column();
        List<StockAlerts.Alert> visible = alerts.stream().filter(a -> branch.isBlank() || a.branchId().equals(branch))
                .filter(a -> filter.equals("All") || a.severity().equals(filter)).toList();
        if (error != null)
            rows.add(emptyState("Unable to check stock", error, AppIcon.Name.WARNING));
        else if (visible.isEmpty())
            rows.add(emptyState(
                    loading ? "Checking stock…" : alerts.isEmpty() ? "All caught up" : "No alerts in this view",
                    loading ? "Fetching the latest tracked balances."
                            : alerts.isEmpty() ? "Tracked products are above their alert thresholds."
                                    : "Try another branch or alert type.",
                    loading ? AppIcon.Name.REFRESH : AppIcon.Name.CHECK));
        else
            for (var a : visible) {
                rows.add(alertCard(a));
                rows.add(Box.createVerticalStrut(10));
            }
        JScrollPane list = scroll(rows);
        list.setName("notificationList");
        int height = popupHeight();
        list.setPreferredSize(new Dimension(424, Math.min(height, Math.max(130, visible.size() * 184))));
        list.getVerticalScrollBar().setUnitIncrement(28);
        root.add(list, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        JLabel timestamp = label(
                loading ? "Updating balances…"
                        : updated == null ? "Refresh to check stock"
                                : "Updated " + updated.format(DateTimeFormatter.ofPattern("h:mm a"))
                                        + " · Updates after sales and stock changes",
                new Font("Segoe UI", Font.PLAIN, 10), UIStyle.TEXT_SECONDARY);
        footer.add(timestamp, BorderLayout.NORTH);
        JButton inventory = button("Open inventory", AppIcon.Name.BOX, true);
        inventory.addActionListener(e -> {
            if (popup != null)
                popup.setVisible(false);
            AppEvents.fire("navigate-inventory");
        });
        footer.add(inventory, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
    }

    private int popupHeight() {
        Window window = SwingUtilities.getWindowAncestor(this);
        return window == null ? 300 : Math.max(140, Math.min(320, window.getHeight() - 370));
    }

    private JPanel emptyState(String title, String hint, AppIcon.Name icon) {
        JPanel box = column();
        box.setBorder(new EmptyBorder(24, 12, 24, 12));
        JLabel heading = label(title, UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
        heading.setIcon(new AppIcon(icon, 24, icon == AppIcon.Name.WARNING ? UIStyle.WARNING : UIStyle.PRIMARY));
        heading.setIconTextGap(10);
        box.add(heading);
        box.add(Box.createVerticalStrut(12));
        box.add(note(hint, 3));
        return box;
    }

    private JTextArea note(String text, int rows) {
        JTextArea note = new JTextArea(text, rows, 20);
        note.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        note.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        note.setForeground(UIStyle.TEXT_SECONDARY);
        note.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setEditable(false);
        note.setFocusable(false);
        return note;
    }

    private JPanel alertCard(StockAlerts.Alert alert) {
        Surface card = new Surface();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setName("notificationAlertCard");
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 174));
        card.setPreferredSize(new Dimension(390, 174));
        boolean out = alert.available().signum() <= 0;
        Color color = out ? UIStyle.DANGER : UIStyle.WARNING;
        JPanel heading = new JPanel(new BorderLayout(8, 0));
        heading.setOpaque(false);
        JPanel names = column();
        names.add(plain(alert.name(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
        names.add(Box.createVerticalStrut(3));
        names.add(plain(alert.branch(), UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        heading.add(names, BorderLayout.CENTER);
        JLabel badge = label(out ? "OUT OF STOCK" : "LOW STOCK", new Font("Segoe UI", Font.BOLD, 9), color);
        badge.setOpaque(true);
        badge.setBackground(out ? new Color(254, 242, 242) : new Color(255, 247, 237));
        badge.setBorder(new EmptyBorder(5, 7, 5, 7));
        heading.add(badge, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);
        JPanel quantities = new JPanel(new GridLayout(1, 2, 16, 0));
        quantities.setOpaque(false);
        quantities.add(quantity("AVAILABLE",
                alert.available().stripTrailingZeros().toPlainString() + " " + alert.unit(), color));
        quantities.add(quantity("THRESHOLD",
                alert.threshold().stripTrailingZeros().toPlainString() + " " + alert.unit(), UIStyle.TEXT_PRIMARY));
        card.add(quantities, BorderLayout.CENTER);
        JPanel action = new JPanel(new BorderLayout(8, 0));
        action.setOpaque(false);
        action.add(note("Restock above the threshold to clear this alert.", 2), BorderLayout.CENTER);
        JButton view = iconButton("View " + alert.name() + " in " + alert.branch() + " inventory", AppIcon.Name.ARROW);
        view.setName("viewAlertInventory");
        view.addActionListener(e -> {
            if (popup != null)
                popup.setVisible(false);
            AppEvents.fire("open-stock-alert", alert);
        });
        action.add(view, BorderLayout.EAST);
        card.add(action, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quantity(String title, String value, Color color) {
        JPanel p = column();
        p.add(label(title, new Font("Segoe UI", Font.BOLD, 9), UIStyle.TEXT_SECONDARY));
        p.add(Box.createVerticalStrut(4));
        p.add(plain(value, new Font("Segoe UI", Font.BOLD, 20), color));
        return p;
    }

    private void open() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER));
        popup.add(notificationContent());
        popup.show(this, getWidth() - popup.getPreferredSize().width, getHeight() + 6);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        AppEvents.add(changes);
        refresh();
    }

    @Override
    public void removeNotify() {
        AppEvents.remove(changes);
        debounce.stop();
        generation++;
        if (worker != null)
            worker.cancel(true);
        if (popup != null)
            popup.setVisible(false);
        super.removeNotify();
    }
}
