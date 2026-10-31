package util.components;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import application.StockAlerts;
import util.config.*;
import static util.components.ModernUI.*;

/**
 * A live, non-modal alert list. Resolved stock warnings disappear after the
 * next refresh.
 */
public final class StockNotificationButton extends JButton {
    private List<StockAlerts.Alert> alerts = List.of();
    private String error;
    private long generation;
    private final Timer debounce = new Timer(180, e -> refresh());
    private final PropertyChangeListener changes = e -> {
        if (java.util.Set.of("stock", "orders", "catalog", "transfers", "branches").contains(e.getPropertyName()))
            debounce.restart();
    };

    public StockNotificationButton() {
        super("…");
        setUI(new javax.swing.plaf.basic.BasicButtonUI());
        setName("stockNotifications");
        setIcon(new AppIcon(AppIcon.Name.BELL, 19));
        setFont(UIStyle.BOLD);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER),
                new EmptyBorder(7, 10, 7, 10)));
        setBackground(Color.WHITE);
        setToolTipText("Stock notifications");
        debounce.setRepeats(false);
        addActionListener(e -> open());
        refresh();
    }

    public List<StockAlerts.Alert> alerts() {
        return alerts;
    }

    public boolean isLoading() {
        return getText().equals("…");
    }

    public void refresh() {
        long request = ++generation;
        new SwingWorker<List<StockAlerts.Alert>, Void>() {
            protected List<StockAlerts.Alert> doInBackground() throws Exception {
                return StockAlerts.read();
            }

            protected void done() {
                if (request != generation)
                    return;
                try {
                    alerts = get();
                    error = null;
                    setText(Integer.toString(alerts.size()));
                    setForeground(alerts.isEmpty() ? UIStyle.TEXT_SECONDARY : UIStyle.WARNING);
                    getAccessibleContext().setAccessibleName(alerts.size() + " stock alerts");
                } catch (Exception ex) {
                    error = "Stock alerts unavailable. Review inventory and unreadable orders.";
                    setText("!");
                    setForeground(UIStyle.DANGER);
                    getAccessibleContext().setAccessibleName(error);
                }
                repaint();
            }
        }.execute();
    }

    public JPanel notificationContent() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(Color.WHITE);
        root.add(label("Stock notifications", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY), BorderLayout.NORTH);
        JPanel rows = column();
        if (error != null)
            rows.add(label(error, UIStyle.SMALL, UIStyle.DANGER));
        else if (alerts.isEmpty())
            rows.add(label("No tracked products at or below their threshold.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        for (var a : alerts) {
            JPanel row = column();
            row.setBorder(new EmptyBorder(10, 0, 10, 0));
            row.add(label(a.name() + " · " + a.branch(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
            row.add(Box.createVerticalStrut(4));
            row.add(label(
                    a.severity() + " · " + a.available().stripTrailingZeros().toPlainString() + " " + a.unit()
                            + " available · threshold " + a.threshold().stripTrailingZeros().toPlainString(),
                    UIStyle.SMALL, a.available().signum() <= 0 ? UIStyle.DANGER : UIStyle.WARNING));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            rows.add(row);
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            rows.add(separator);
        }
        JScrollPane scroll = scroll(rows);
        scroll.setPreferredSize(new Dimension(430, Math.min(300, Math.max(65, alerts.size() * 78))));
        root.add(scroll, BorderLayout.CENTER);
        JButton inventory = button("Open inventory", AppIcon.Name.BOX, true);
        inventory.addActionListener(e -> {
            var popup = (JPopupMenu) SwingUtilities.getAncestorOfClass(JPopupMenu.class, inventory);
            if (popup != null)
                popup.setVisible(false);
            AppEvents.fire("navigate-inventory");
        });
        root.add(inventory, BorderLayout.SOUTH);
        return root;
    }

    private void open() {
        JPopupMenu popup = new JPopupMenu();
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
        super.removeNotify();
    }
}
