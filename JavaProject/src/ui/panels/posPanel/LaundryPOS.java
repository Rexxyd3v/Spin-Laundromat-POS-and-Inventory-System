package ui.panels.posPanel;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import model.*;
import util.components.*;
import util.config.*;
import ui.panels.settingsPanel.SettingsPanel;
import static util.components.ModernUI.*;

/**
 * Catalog and cart stay separate so view/theme changes never discard an order.
 */
public class LaundryPOS extends JPanel {
    private persistence.RepositoryProvider.Catalog catalog;
    private String catalogError;
    private final List<Item> cart = new ArrayList<>();
    private final JTextField search = field("Search services and products");
    private final JPanel categories = row(8), cartItems = column();
    private final CatalogGrid grid = new CatalogGrid();
    private final JLabel count = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final JLabel cartCount = label("", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final JLabel subtotalLabel = label("", UIStyle.REGULAR, UIStyle.TEXT_PRIMARY),
            taxLabel = label("", UIStyle.REGULAR, UIStyle.TEXT_PRIMARY),
            totalLabel = label("", UIStyle.TITLE, UIStyle.TEXT_PRIMARY);
    private final JLabel taxCaption = label("", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY);
    private final JButton checkout = button("Checkout", AppIcon.Name.ARROW, true);
    private final JButton clear = iconButton("Clear order", AppIcon.Name.TRASH);
    private final JButton view = iconButton("Switch catalog layout", AppIcon.Name.LIST);
    private String category = "All";
    private boolean listView = persistence.Repositories.settings().get().listView();
    private final PropertyChangeListener catalogListener = e -> {
        if (e.getPropertyName().equals("catalog"))
            reloadCatalog();
    };
    private final PropertyChangeListener settingsListener = e -> {
        listView = persistence.Repositories.settings().get().listView();
        refreshCatalog();
        updateCart();
        revalidate();
        repaint();
    };

    private final JTextField customerName = field("Customer name");
    private String orderNote = "";
    private final JButton noteButton = button("Add note", AppIcon.Name.EDIT, false);
    private final JPanel costBreakdown = column();

    public LaundryPOS() {
        UIStyle.installDefaults();
        try {
            catalog = persistence.Repositories.catalog();
        } catch (IOException ex) {
            catalogError = ex.getMessage();
        }
        setLayout(new BorderLayout(0, 24));
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(buildHeader(), BorderLayout.NORTH);
        JPanel catalogPanel = new JPanel(new BorderLayout(0, 18));
        catalogPanel.setOpaque(false);
        catalogPanel.add(buildTools(), BorderLayout.NORTH);
        catalogPanel.add(scroll(grid), BorderLayout.CENTER);
        JPanel footer = row(0);
        footer.add(count);
        catalogPanel.add(footer, BorderLayout.SOUTH);
        JPanel orderPanel = buildCart();
        JPanel body = new JPanel(null) {
            private boolean drawerOpen;
            private final JButton open = button("View order", AppIcon.Name.RECEIPT, true);
            {
                add(catalogPanel);
                add(orderPanel);
                add(open);
                open.addActionListener(e -> {
                    drawerOpen = true;
                    remove(orderPanel);
                    JDialog drawer = new JDialog(SwingUtilities.getWindowAncestor(LaundryPOS.this), "Current order",
                            Dialog.ModalityType.APPLICATION_MODAL);
                    drawer.setContentPane(orderPanel);
                    drawer.setSize(410, 650);
                    drawer.setLocationRelativeTo(LaundryPOS.this);
                    drawer.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    drawer.addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent event) {
                            drawer.setContentPane(new JPanel());
                            add(orderPanel);
                            drawerOpen = false;
                            revalidate();
                            repaint();
                        }
                    });
                    drawer.setVisible(true);
                });
            }

            public void doLayout() {
                int w = getWidth(), h = getHeight(), gap = 20;
                boolean wide = w >= 790;
                open.setVisible(!wide);
                if (!drawerOpen)
                    orderPanel.setVisible(wide);
                if (wide) {
                    int cw = Math.min(350, Math.max(310, w / 3));
                    catalogPanel.setBounds(0, 0, w - cw - gap, h);
                    if (!drawerOpen)
                        orderPanel.setBounds(w - cw, 0, cw, h);
                } else {
                    catalogPanel.setBounds(0, 0, w, Math.max(0, h - 66));
                    open.setText("View order · " + OrderPricing.money(totals().total()));
                    open.setBounds(0, Math.max(0, h - 48), w, 48);
                }
            }
        };
        body.setOpaque(false);
        add(body, BorderLayout.CENTER);
        search.putClientProperty("placeholder", "Search services or products…");
        search.setToolTipText("Search by item name, description or category. Ctrl+F focuses search.");
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshCatalog();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshCatalog();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshCatalog();
            }
        });
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "search");
        getActionMap().put("search", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                search.requestFocusInWindow();
            }
        });
        checkout.addActionListener(e -> openCheckout());
        clear.addActionListener(e -> {
            if (!cart.isEmpty() && JOptionPane.showConfirmDialog(this, "Remove all items from this order?",
                    "Clear order", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                clearCart();
        });
        view.addActionListener(e -> {
            listView = !listView;
            refreshCatalog();
        });
        rebuildCategories();
        refreshCatalog();
        updateCart();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        persistence.Repositories.settings().addListener(settingsListener);
        AppEvents.add(catalogListener);
    }

    @Override
    public void removeNotify() {
        persistence.Repositories.settings().removeListener(settingsListener);
        AppEvents.remove(catalogListener);
        super.removeNotify();
    }

    public void reloadCatalog() {
        try {
            catalog = persistence.Repositories.catalog();
            catalogError = null;
            rebuildCategories();
            refreshCatalog();
        } catch (IOException ex) {
            catalogError = ex.getMessage();
        }
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);
        JPanel titles = column();
        titles.add(label("YOUR DAILY WORKSPACE", UIStyle.SMALL, UIStyle.PRIMARY));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label("Point of sale", UIStyle.TITLE, UIStyle.TEXT_PRIMARY));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label("Fresh laundry. A smoother checkout.", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        p.add(titles, BorderLayout.CENTER);
        JPanel actions = row(8);
        JButton manage = button("Catalog", AppIcon.Name.EDIT, false);
        manage.setToolTipText("Add, edit or remove services and products");
        manage.setEnabled(catalog != null);
        manage.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            CatalogManager manager = new CatalogManager(owner, catalog, this::reloadCatalog);
            manager.setVisible(true);
        });
        JButton customize = iconButton("Customize your POS", AppIcon.Name.SETTINGS);
        customize.addActionListener(e -> {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Customize your POS",
                    Dialog.ModalityType.APPLICATION_MODAL);
            d.setContentPane(new SettingsPanel());
            d.setSize(780, 650);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });
        actions.add(manage);
        actions.add(customize);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    private JPanel buildTools() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        JPanel searchBar = new Surface();
        searchBar.setBorder(new EmptyBorder(1, 12, 1, 4));
        JLabel icon = new JLabel(new AppIcon(AppIcon.Name.SEARCH, 20, UIStyle.TEXT_SECONDARY));
        searchBar.add(icon, BorderLayout.WEST);
        search.setBorder(new EmptyBorder(12, 10, 12, 8));
        searchBar.add(search, BorderLayout.CENTER);
        searchBar.add(view, BorderLayout.EAST);
        p.add(searchBar, BorderLayout.NORTH);
        JScrollPane chips = scroll(categories);
        chips.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        chips.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chips.setPreferredSize(new Dimension(10, 52));
        p.add(chips, BorderLayout.SOUTH);
        return p;
    }

    private void rebuildCategories() {
        Set<String> names = new LinkedHashSet<>();
        names.add("All");
        if (catalog != null)
            for (CatalogItem item : catalog.items())
                names.add(item.category());
        if (!names.contains(category))
            category = "All";
        categories.removeAll();
        for (String name : names) {
            JButton chip = button(name, null, name.equals(category));
            chip.setBorder(new EmptyBorder(8, 14, 8, 14));
            chip.addActionListener(e -> {
                category = name;
                rebuildCategories();
                refreshCatalog();
            });
            categories.add(chip);
        }
        categories.revalidate();
        categories.repaint();
    }

    private void refreshCatalog() {
        grid.removeAll();
        view.setIcon(new AppIcon(listView ? AppIcon.Name.GRID : AppIcon.Name.LIST, 18));
        String term = search.getText().trim().toLowerCase(Locale.ROOT);
        int found = 0;
        if (catalog != null)
            for (CatalogItem item : catalog.items()) {
                if (!category.equals("All") && !item.category().equals(category))
                    continue;
                if (!(item.name() + " " + item.description() + " " + item.category()).toLowerCase(Locale.ROOT)
                        .contains(term))
                    continue;
                grid.add(itemCard(item));
                found++;
            }
        if (found == 0) {
            JPanel empty = column();
            empty.setBorder(new EmptyBorder(30, 8, 20, 8));
            empty.add(new JLabel(new AppIcon(catalogError == null ? AppIcon.Name.SEARCH : AppIcon.Name.WARNING, 30,
                    UIStyle.TEXT_SECONDARY)));
            empty.add(Box.createVerticalStrut(12));
            empty.add(label(catalogError == null ? "No items found" : "Catalog unavailable", UIStyle.SUBTITLE,
                    UIStyle.TEXT_PRIMARY));
            JLabel hint = label(
                    catalogError == null ? "Try another search or add an item in Catalog."
                            : "Your saved catalog was not changed. Check the catalog file before continuing.",
                    UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
            hint.setToolTipText(catalogError);
            empty.add(hint);
            grid.add(empty);
        }
        count.setText(found + " item" + (found == 1 ? "" : "s") + " · Prices per displayed unit");
        grid.revalidate();
        grid.repaint();
    }

    private AppIcon.Name itemIcon(CatalogItem item) {
        String name = item.name().toLowerCase(Locale.ROOT);
        if (item.product())
            return AppIcon.Name.BOX;
        if (name.contains("fold"))
            return AppIcon.Name.FOLD;
        if (name.contains("dry") && !name.contains("wash"))
            return AppIcon.Name.DRY;
        return AppIcon.Name.WASH;
    }

    private JPanel itemCard(CatalogItem item) {
        Surface card = new Surface();
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setToolTipText(item.description());
        ItemPhoto photo = new ItemPhoto(item);
        JPanel text = column();
        text.add(label(item.category().toUpperCase(Locale.ROOT), new Font("Segoe UI", Font.BOLD, 10),
                UIStyle.TEXT_SECONDARY));
        text.add(Box.createVerticalStrut(6));
        JLabel name = label(item.name(), new Font("Segoe UI", Font.BOLD, 15), UIStyle.TEXT_PRIMARY);
        name.setToolTipText(item.name());
        text.add(name);
        if (!persistence.Repositories.settings().get().compact() || listView) {
            text.add(Box.createVerticalStrut(5));
            JLabel detail = label(item.description(), new Font("Segoe UI", Font.PLAIN, 11), UIStyle.TEXT_SECONDARY);
            detail.setToolTipText(item.description());
            text.add(detail);
        }
        JButton add = button("", AppIcon.Name.PLUS, true);
        add.setPreferredSize(new Dimension(36, 36));
        add.setToolTipText("Add " + item.name());
        add.getAccessibleContext().setAccessibleName("Add " + item.name());
        add.addActionListener(e -> addToCart(item));
        JPanel price = row(4);
        price.add(label(OrderPricing.money(item.price()), new Font("Segoe UI", Font.BOLD, 16), UIStyle.TEXT_PRIMARY));
        price.add(label("/ " + item.unit(), UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        if (listView) {
            card.setLayout(new BorderLayout(16, 0));
            photo.setPreferredSize(new Dimension(76, 76));
            card.add(photo, BorderLayout.WEST);
            card.add(text, BorderLayout.CENTER);
            JPanel right = row(12);
            right.add(price);
            right.add(add);
            card.add(right, BorderLayout.EAST);
        } else {
            photo.setPreferredSize(new Dimension(150, 126));
            card.add(photo, BorderLayout.NORTH);
            text.setBorder(new EmptyBorder(12, 2, 12, 2));
            card.add(text, BorderLayout.CENTER);
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(price, BorderLayout.WEST);
            bottom.add(add, BorderLayout.EAST);
            card.add(bottom, BorderLayout.SOUTH);
        }
        return card;
    }

    private JPanel buildCart() {
        Surface panel = new Surface();
        panel.setName("order-summary");
        panel.setBorder(new EmptyBorder(20, 18, 18, 18));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel titles = column();
        titles.add(label("Order summary", new Font("Segoe UI", Font.BOLD, 20), UIStyle.TEXT_PRIMARY));
        titles.add(Box.createVerticalStrut(6));
        titles.add(cartCount);
        header.add(titles, BorderLayout.CENTER);
        JPanel clearHolder = row(0);
        clearHolder.add(clear);
        header.add(clearHolder, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));
        panel.add(header, BorderLayout.NORTH);
        JPanel contents = column();
        JPanel person = new JPanel(new BorderLayout(8, 0));
        person.setOpaque(false);
        customerName.putClientProperty("placeholder", "Walk-in customer");
        customerName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER),
                new EmptyBorder(10, 10, 10, 10)));
        person.add(customerName, BorderLayout.CENTER);
        JButton profile = iconButton("Customer name", AppIcon.Name.USERS);
        profile.addActionListener(e -> customerName.requestFocusInWindow());
        person.add(profile, BorderLayout.WEST);
        person.setAlignmentX(LEFT_ALIGNMENT);
        person.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        contents.add(person);
        contents.add(Box.createVerticalStrut(18));
        cartItems.setAlignmentX(LEFT_ALIGNMENT);
        contents.add(cartItems);
        contents.add(Box.createVerticalStrut(12));
        noteButton.setAlignmentX(LEFT_ALIGNMENT);
        noteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        noteButton.addActionListener(e -> {
            JTextArea note = new JTextArea(orderNote, 5, 28);
            note.setLineWrap(true);
            note.setWrapStyleWord(true);
            note.setFont(UIStyle.REGULAR);
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(note), "Order instructions",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                orderNote = note.getText().trim();
                noteButton.setText(orderNote.isBlank() ? "Add note" : "Edit order note");
                noteButton.setToolTipText(orderNote);
            }
        });
        contents.add(noteButton);
        contents.add(Box.createVerticalStrut(20));
        costBreakdown.setBorder(new EmptyBorder(16, 0, 12, 0));
        costBreakdown.add(totalRow(label("Subtotal", UIStyle.SMALL, UIStyle.TEXT_SECONDARY), subtotalLabel));
        costBreakdown.add(Box.createVerticalStrut(10));
        costBreakdown.add(totalRow(taxCaption, taxLabel));
        contents.add(costBreakdown);
        JScrollPane orderScroll = scroll(contents);
        orderScroll.getVerticalScrollBar().setUnitIncrement(32);
        panel.add(orderScroll, BorderLayout.CENTER);
        JPanel bottom = column();
        bottom.setBorder(new EmptyBorder(16, 0, 0, 0));
        JPanel due = new JPanel(new BorderLayout());
        due.setOpaque(false);
        JPanel caption = column();
        caption.add(label("AMOUNT DUE", new Font("Segoe UI", Font.BOLD, 10), UIStyle.TEXT_SECONDARY));
        caption.add(Box.createVerticalStrut(4));
        caption.add(label("Tax included in total", new Font("Segoe UI", Font.PLAIN, 10), UIStyle.TEXT_SECONDARY));
        due.add(caption, BorderLayout.CENTER);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        due.add(totalLabel, BorderLayout.EAST);
        due.setAlignmentX(LEFT_ALIGNMENT);
        due.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        bottom.add(new JSeparator());
        bottom.add(Box.createVerticalStrut(16));
        bottom.add(due);
        bottom.add(Box.createVerticalStrut(16));
        checkout.setText("Review & pay");
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        checkout.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(checkout);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel totalRow(JLabel caption, JLabel amount) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.add(caption, BorderLayout.WEST);
        p.add(amount, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return p;
    }

    public void addToCart(CatalogItem product) {
        for (Item item : cart)
            if (item.id.equals(product.id())) {
                if (item.quantity.compareTo(new BigDecimal("99998")) > 0)
                    return;
                item.quantity = item.quantity.add(BigDecimal.ONE);
                updateCart();
                return;
            }
        cart.add(new Item(product));
        updateCart();
    }

    public void clearCart() {
        cart.clear();
        customerName.setText("");
        orderNote = "";
        noteButton.setText("Add note");
        updateCart();
    }

    public List<OrderPricing.Line> orderLines() {
        return cart.stream().map(Item::line).toList();
    }

    public OrderPricing.Totals totals() {
        PosSettings.Values s = persistence.Repositories.settings().get();
        return OrderPricing.calculate(orderLines(), s.taxEnabled(), s.taxRate());
    }

    private void updateCart() {
        cartItems.removeAll();
        cartCount.setText(cart.size() + " line" + (cart.size() == 1 ? "" : "s") + " · Add items, then review payment");
        if (cart.isEmpty()) {
            JPanel empty = column();
            empty.setBorder(new EmptyBorder(24, 12, 24, 12));
            empty.add(new JLabel(new AppIcon(AppIcon.Name.RECEIPT, 36, UIStyle.TEXT_SECONDARY)));
            empty.add(Box.createVerticalStrut(12));
            empty.add(label("Let's start an order", UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
            empty.add(Box.createVerticalStrut(7));
            empty.add(label("Choose a service or product from the catalog.", new Font("Segoe UI", Font.PLAIN, 11),
                    UIStyle.TEXT_SECONDARY));
            empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            cartItems.add(empty);
        }
        for (Item item : cart) {
            Surface row = new Surface() {
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(248, 250, 252));
                    g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g.setColor(UIStyle.INPUT_BORDER);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g.dispose();
                }
            };
            row.setBorder(new EmptyBorder(12, 12, 12, 12));
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
            row.setPreferredSize(new Dimension(280, 112));
            JPanel top = new JPanel(new BorderLayout(10, 0));
            top.setOpaque(false);
            CatalogItem catalogItem = catalog == null ? null
                    : catalog.items().stream().filter(i -> i.id().equals(item.id)).findFirst().orElse(null);
            if (catalogItem != null) {
                ItemPhoto photo = new ItemPhoto(catalogItem);
                photo.setPreferredSize(new Dimension(38, 38));
                top.add(photo, BorderLayout.WEST);
            }
            JPanel text = column();
            JLabel name = label(item.name, new Font("Segoe UI", Font.BOLD, 13), UIStyle.TEXT_PRIMARY);
            name.setToolTipText(item.name);
            text.add(name);
            text.add(Box.createVerticalStrut(5));
            text.add(label(OrderPricing.money(item.unitPrice) + " / " + item.unit, new Font("Segoe UI", Font.PLAIN, 11),
                    UIStyle.TEXT_SECONDARY));
            top.add(text, BorderLayout.CENTER);
            JButton remove = iconButton("Remove " + item.name, AppIcon.Name.TRASH);
            remove.setPreferredSize(new Dimension(28, 28));
            remove.setBorder(new EmptyBorder(2, 2, 2, 2));
            remove.addActionListener(e -> {
                cart.remove(item);
                updateCart();
            });
            JPanel removeWrap = row(0);
            removeWrap.add(remove);
            top.add(removeWrap, BorderLayout.EAST);
            row.add(top, BorderLayout.NORTH);
            JPanel bottom = new JPanel(new BorderLayout(8, 0));
            bottom.setOpaque(false);
            QuantityControl control = new QuantityControl(item.quantity.doubleValue(),
                    item.unit == CatalogItem.Unit.KG ? 0.001 : 1, 99999, item.unit == CatalogItem.Unit.KG ? 0.25 : 1,
                    item.unit == CatalogItem.Unit.KG, item.name);
            JSpinner qty = control.spinner();
            JLabel amount = label(OrderPricing.money(item.line().total()), UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
            qty.addChangeListener(e -> {
                BigDecimal q = new BigDecimal(qty.getValue().toString());
                if (item.unit != CatalogItem.Unit.KG && q.stripTrailingZeros().scale() > 0) {
                    qty.setValue(item.quantity.doubleValue());
                    return;
                }
                item.quantity = q;
                amount.setText(OrderPricing.money(item.line().total()));
                updateTotals();
            });
            JPanel quantityRow = new JPanel(new BorderLayout(5, 0));
            quantityRow.setOpaque(false);
            quantityRow.add(control, BorderLayout.CENTER);
            quantityRow.add(label(item.unit.toString(), new Font("Segoe UI", Font.PLAIN, 10), UIStyle.TEXT_SECONDARY),
                    BorderLayout.EAST);
            bottom.add(quantityRow, BorderLayout.WEST);
            bottom.add(amount, BorderLayout.EAST);
            bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
            row.add(bottom, BorderLayout.SOUTH);
            cartItems.add(row);
            cartItems.add(Box.createVerticalStrut(10));
        }
        cartItems.revalidate();
        cartItems.repaint();
        updateTotals();
        revalidate();
        repaint();
    }

    private void updateTotals() {
        OrderPricing.Totals t = totals();
        PosSettings.Values s = persistence.Repositories.settings().get();
        subtotalLabel.setText(OrderPricing.money(t.subtotal()));
        taxLabel.setText(OrderPricing.money(t.tax()));
        totalLabel.setText(OrderPricing.money(t.total()));
        taxCaption.setText(
                s.taxEnabled() ? "Tax (" + s.taxRate().stripTrailingZeros().toPlainString() + "%)" : "Tax disabled");
        costBreakdown.setVisible(!cart.isEmpty());
        noteButton.setEnabled(!cart.isEmpty());
        checkout.setEnabled(!cart.isEmpty());
        clear.setEnabled(!cart.isEmpty());
    }

    private void openCheckout() {
        if (cart.isEmpty())
            return;
        // Commit typed spinner values before taking the order snapshot.
        try {
            commitSpinners(cartItems);
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity before checkout.", "Check quantity",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Checkout",
                Dialog.ModalityType.APPLICATION_MODAL);
        CheckoutScreen payment = new CheckoutScreen(this, orderLines(), totals(),
                persistence.Repositories.settings().get());
        payment.setOrderDetails(customerName.getText().isBlank() ? "Walk-in customer" : customerName.getText().trim(),
                orderNote);
        dialog.setContentPane(payment);
        dialog.setSize(900, 720);
        dialog.setMinimumSize(new Dimension(680, 600));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void commitSpinners(Container container) throws java.text.ParseException {
        for (Component c : container.getComponents()) {
            if (c instanceof JSpinner spinner)
                spinner.commitEdit();
            else if (c instanceof Container child)
                commitSpinners(child);
        }
    }

    public static final class Item {
        public final String id, name;
        public final CatalogItem.Unit unit;
        public final BigDecimal unitPrice;
        public BigDecimal quantity = BigDecimal.ONE;

        Item(CatalogItem item) {
            id = item.id();
            name = item.name();
            unit = item.unit();
            unitPrice = item.price();
        }

        OrderPricing.Line line() {
            return new OrderPricing.Line(id, name, unit.toString(), quantity, unitPrice);
        }
    }

    private class CatalogGrid extends JPanel implements Scrollable {
        CatalogGrid() {
            setOpaque(false);
        }

        private int columns() {
            return listView ? 1 : Math.max(1, getWidth() / 210);
        }

        private int cellHeight() {
            return listView ? 106 : persistence.Repositories.settings().get().compact() ? 255 : 284;
        }

        public Dimension getPreferredSize() {
            int cols = columns();
            return new Dimension(200, Math.max(1, (getComponentCount() + cols - 1) / cols) * (cellHeight() + 14));
        }

        public void doLayout() {
            int cols = columns(), gap = 14, w = Math.max(0, (getWidth() - gap * (cols - 1)) / cols);
            for (int i = 0; i < getComponentCount(); i++)
                getComponent(i).setBounds((i % cols) * (w + gap), (i / cols) * (cellHeight() + gap), w, cellHeight());
        }

        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(600, 500);
        }

        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 24;
        }

        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return r.height - 24;
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
