package ui.panels.posPanel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import model.*;
import util.components.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

public class CatalogPage extends PageLayout {
    private persistence.RepositoryProvider.Catalog store;
    private final JTextField search = field("Search catalog");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "IMAGE", "ITEM NAME", "CATEGORY", "TYPE", "PRICE / UNIT" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private java.util.List<CatalogItem> filtered = java.util.List.of();
    private final Map<String, ItemPhoto> photos = new HashMap<>();
    private final JLabel status = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);

    public CatalogPage() {
        super("YOUR COLLECTION", "Services & products", "Your prices, your categories, your images.");
        JButton add = button("Add item", AppIcon.Name.PLUS, true), edit = button("Edit item", AppIcon.Name.EDIT, false),
                remove = iconButton("Remove selected item", AppIcon.Name.TRASH);
        boolean canEdit = security.LocalAuth.allowed(security.LocalAuth.Permission.CATALOG_EDIT);
        add.setEnabled(canEdit); edit.setEnabled(canEdit); remove.setEnabled(canEdit);
        actions.add(edit);
        actions.add(remove);
        actions.add(add);
        search.putClientProperty("placeholder", "Search names or categories…");
        JPanel tools = new JPanel(new BorderLayout());
        tools.setOpaque(false);
        tools.add(search, BorderLayout.CENTER);
        body.add(tools, BorderLayout.NORTH);
        PageLayout.styleTable(table);
        table.setRowHeight(86);
        table.getColumnModel().getColumn(0).setMaxWidth(108);
        table.getColumnModel().getColumn(0).setMinWidth(100);
        table.getColumnModel().getColumn(0).setCellRenderer((t, value, selected, focus, row, col) -> {
            CatalogItem item = filtered.get(row);
            ItemPhoto photo = photos.computeIfAbsent(item.id() + item.image(),
                    key -> new ItemPhoto(item).onLoaded(table::repaint));
            JPanel cell = new JPanel() {
                protected void paintComponent(Graphics graphics) {
                    graphics.setColor(selected ? UIStyle.tint() : Color.WHITE);
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                    photo.setSize(Math.max(1, getWidth() - 24), Math.max(1, getHeight() - 16));
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.translate(12, 8);
                    photo.paint(g);
                    g.dispose();
                }
            };
            return cell;
        });
        body.add(PageLayout.tableCard("Catalog", table, status), BorderLayout.CENTER);
        add.addActionListener(e -> edit(null));
        edit.addActionListener(e -> {
            CatalogItem item = selected();
            if (item != null)
                edit(item);
        });
        remove.addActionListener(e -> remove());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    CatalogItem item = selected();
                    if (item != null)
                        edit(item);
                }
            }
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        refresh();
    }

    public void refresh() {
        try {
            store = persistence.Repositories.catalog();
            filter();
        } catch (Exception ex) {
            status.setForeground(UIStyle.DANGER);
            status.setText("Catalog could not be loaded. The saved file was not changed.");
        }
    }

    private void filter() {
        if (store == null)
            return;
        String term = search.getText().trim().toLowerCase(Locale.ROOT);
        filtered = store.items().stream()
                .filter(i -> (i.name() + " " + i.category()).toLowerCase(Locale.ROOT).contains(term)).toList();
        model.setRowCount(0);
        for (CatalogItem i : filtered)
            model.addRow(new Object[] { "", i.name(), i.category(), i.product() ? "Product" : "Service",
                    OrderPricing.money(i.price()) + " / " + i.unit() });
        status.setText(filtered.size() + " items · Select an item to edit details or upload a photo.");
    }

    private CatalogItem selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            status.setText("Choose an item first.");
            return null;
        }
        return filtered.get(row);
    }

    private void edit(CatalogItem item) {
        if (store != null)
            new CatalogEditor(SwingUtilities.getWindowAncestor(this), store, item, this::refresh).setVisible(true);
    }

    private void remove() {
        CatalogItem item = selected();
        if (item == null)
            return;
        if (JOptionPane.showConfirmDialog(this,
                "Remove " + item.name() + " from the catalog? Existing orders are kept.", "Remove item",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        java.util.List<CatalogItem> next = new ArrayList<>(store.items());
        next.remove(item);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                store.save(next);
                return null;
            }

            protected void done() {
                try {
                    get();
                    refresh();
                } catch (Exception ex) {
                    status.setText("Could not remove this item. Please try again.");
                }
            }
        }.execute();
    }
}
