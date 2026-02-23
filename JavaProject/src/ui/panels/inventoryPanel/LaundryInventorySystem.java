package src.ui.panels.inventoryPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.*;
import src.util.components.Scrollbar;
import src.util.components.ShadowPanel;
import src.util.config.UIStyle;

public class LaundryInventorySystem extends JPanel {

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    @SuppressWarnings("unused")
    private JComboBox<String> categoryCombo;
    @SuppressWarnings("unused")
    private JComboBox<String> supplierCombo;
    @SuppressWarnings("unused")
    private JComboBox<String> stockLevelCombo;
    private JTextField searchField;

    private static final Color ORANGE_ACCENT = new Color(245, 158, 11);
    private static final Color GREEN_BTN = new Color(34, 197, 94);
    private static final Color BLUE_BTN = new Color(59, 130, 246);
    private static final Color RED_BTN = new Color(239, 68, 68);

    public LaundryInventorySystem() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        JPanel body = buildBody();
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 40, 32, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 30, 0);

        // Header
        gbc.gridy = 0;
        p.add(buildPageHeader(), gbc);

        // Stat Cards
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        p.add(buildStatsPanel(), gbc);

        // Filter Row
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        p.add(buildTableFilterRow(), gbc);

        // Inventory List Card
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(buildInventoryListCard(), gbc);

        return p;
    }

    private JPanel buildPageHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Supplies & Equipment Inventory");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage laundry supplies and stock levels");
        subtitleLabel.setFont(UIStyle.REGULAR);
        subtitleLabel.setForeground(UIStyle.TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);

        // Wrap button to prevent stretching
        JPanel btnWrapper = new JPanel();
        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.Y_AXIS));
        btnWrapper.setOpaque(false);
        btnWrapper.add(Box.createVerticalGlue());
        btnWrapper.add(buildGoToStocksButton());
        btnWrapper.add(Box.createVerticalGlue());

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(btnWrapper, BorderLayout.EAST);
        return panel;
    }

    private JButton buildGoToStocksButton() {
        JButton btn = new JButton("Go to Inventory Stocks") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(185, 34));
        btn.setMaximumSize(new Dimension(185, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inventory Stocks Management",
                    false);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setSize(1200, 800);
            dlg.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dlg.add(new StockManagementPanel(), BorderLayout.CENTER);
            dlg.setVisible(true);
        });
        return btn;
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 30, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 140));

        panel.add(new StatCard("Total Products", "18", UIStyle.TEXT_PRIMARY));
        panel.add(new StatCard("Low Stocks", "3", ORANGE_ACCENT));
        panel.add(new StatCard("Inventory Value", "₱234,200", UIStyle.PRIMARY));

        return panel;
    }

    private JPanel buildTableFilterRow() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 0));
        wrapper.setOpaque(false);

        // Search Pill (Identical to User Panel)
        JPanel searchPill = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);
                g2.dispose();
            }
        };
        searchPill.setOpaque(false);
        searchPill.setBorder(new EmptyBorder(9, 20, 9, 20));

        JLabel searchIcon = new JLabel();
        try {
            java.net.URL imgUrl = getClass().getResource("/src/resources/images/search.png");
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                Image scaled = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                searchIcon.setIcon(new ImageIcon(scaled));
            } else {
                searchIcon.setText("🔍");
            }
        } catch (Exception e) {
            searchIcon.setText("🔍");
        }

        searchField = new JTextField("Search products...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(UIStyle.TEXT_SECONDARY);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setBackground(new Color(0, 0, 0, 0));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search products...")) {
                    searchField.setText("");
                    searchField.setForeground(UIStyle.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search products...");
                    searchField.setForeground(UIStyle.TEXT_SECONDARY);
                }
            }
        });

        searchPill.add(searchIcon, BorderLayout.WEST);
        searchPill.add(searchField, BorderLayout.CENTER);

        // Filter Panel
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filters.setOpaque(false);

        filters.add(createPillFilter(categoryCombo = new JComboBox<>(new String[] { "All Categories" }), 150));
        filters.add(createPillFilter(supplierCombo = new JComboBox<>(new String[] { "All Suppliers" }), 150));
        filters.add(createPillFilter(stockLevelCombo = new JComboBox<>(new String[] { "All Stock Levels" }), 150));

        wrapper.add(searchPill, BorderLayout.CENTER);
        wrapper.add(filters, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createPillFilter(JComboBox<String> combo, int width) {
        JPanel pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(width, 42));
        pill.setBorder(new EmptyBorder(6, 16, 6, 16));

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(UIStyle.TEXT_PRIMARY);
        combo.setBackground(Color.WHITE);
        combo.setBorder(null);
        combo.setOpaque(false);
        combo.setFocusable(false);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new BasicArrowButton(BasicArrowButton.SOUTH, Color.WHITE, Color.WHITE,
                        new Color(100, 116, 139), Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setBackground(Color.WHITE);
                btn.setOpaque(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Background painted by pill
            }
        });

        pill.add(combo, BorderLayout.CENTER);
        return pill;
    }

    private JPanel buildInventoryListCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(24, 24, 24, 24)));

        JLabel listTitle = new JLabel("Products List");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        listTitle.setForeground(UIStyle.TEXT_PRIMARY);
        listTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(listTitle, BorderLayout.NORTH);

        String[] cols = { "Code", "Product Name", "Category", "Supplier", "Stock", "Unit", "Price", "Value", "Status" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                } else {
                    c.setBackground(new Color(239, 246, 255));
                }
                return c;
            }
        };
        inventoryTable.setRowHeight(62);
        inventoryTable.setShowGrid(false); // No Grid Lines!
        inventoryTable.setIntercellSpacing(new Dimension(0, 0));
        inventoryTable.setFocusable(false);
        inventoryTable.setBorder(null);

        JTableHeader th = inventoryTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setForeground(new Color(100, 116, 139));
        th.setBackground(new Color(248, 250, 252));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240))); // Only bottom border
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);

        // Header Renderers
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.LEFT);
        headerRenderer.setBackground(new Color(248, 250, 252));
        headerRenderer.setForeground(new Color(30, 41, 59));
        headerRenderer.setBorder(new EmptyBorder(0, 12, 0, 12));
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setDefaultRenderer(headerRenderer);

        // Cell Renderer (Removes all "hati")
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240))); // Top border for table
                                                                                                 // body
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 450));

        card.add(scroll, BorderLayout.CENTER);
        card.add(buildActionPanel(), BorderLayout.SOUTH);

        ShadowPanel shadow = new ShadowPanel();
        shadow.add(card);
        return shadow;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panel.setOpaque(false);
        JButton addBtn = buildActionButton("+ Add Product", GREEN_BTN);
        addBtn.addActionListener(e -> {
            if (AddProduct.showAddProduct(SwingUtilities.getWindowAncestor(panel))) {
                System.out.println("Add Product Processed Successfully");
            }
        });
        panel.add(addBtn);

        JButton editBtn = buildActionButton("Edit", BLUE_BTN);
        editBtn.addActionListener(e -> {
            if (EditProduct.showEditProduct(SwingUtilities.getWindowAncestor(panel))) {
                System.out.println("Update Product Processed Successfully");
            }
        });
        panel.add(editBtn);

        JButton deleteBtn = buildActionButton("Delete", RED_BTN);
        deleteBtn.addActionListener(e -> {
            if (NotificationDialog.showDelete(SwingUtilities.getWindowAncestor(panel))) {
                System.out.println("Delete Product Confirmed");
                // Original delete logic would go here
            }
        });
        panel.add(deleteBtn);
        return panel;
    }

    private JButton buildActionButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.darker() : color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Inner class for Stat Card aligned with User Panel (Icons removed as
    // requested)
    private class StatCard extends JPanel {
        public StatCard(String title, String value, Color valueColor) {
            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(25, 25, 25, 25));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            titleLabel.setForeground(UIStyle.TEXT_SECONDARY);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            add(titleLabel, gbc);

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
            valueLabel.setForeground(valueColor);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.insets = new Insets(10, 0, 0, 0);
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            add(valueLabel, gbc);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
            g2.dispose();
        }
    }
}
