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

public class StockManagementPanel extends JPanel {

    private JTable stockTable;
    private JTable historyTable;
    private DefaultTableModel stockModel;
    private DefaultTableModel historyModel;
    private JTextField searchField;
    private JSpinner thresholdSpinner;

    private static final Color AMBER_BTN = new Color(245, 158, 11);
    private static final Color GREEN_BTN = new Color(34, 197, 94);
    private static final Color DARK_SLATE = new Color(30, 41, 59);

    public StockManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        JPanel body = buildBody();
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
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

        // Filter Row (Pill Styling)
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        p.add(buildFilterRow(), gbc);

        // Inventory Stock List Card
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        p.add(buildStockListCard(), gbc);

        // Stock Update History Card
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        p.add(buildHistoryCard(), gbc);

        return p;
    }

    private JPanel buildPageHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Inventory Stocks Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Update, Stock In, Stock Out Inventory Management System");
        subtitleLabel.setFont(UIStyle.REGULAR);
        subtitleLabel.setForeground(UIStyle.TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);

        // Back Button (34px pill)
        JButton backBtn = new JButton("Go back to Inventory") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DARK_SLATE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setForeground(Color.WHITE);
        backBtn.setPreferredSize(new Dimension(185, 34));
        backBtn.setMaximumSize(new Dimension(185, 34));
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null)
                window.dispose();
        });

        JPanel btnWrapper = new JPanel();
        btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.Y_AXIS));
        btnWrapper.setOpaque(false);
        btnWrapper.add(Box.createVerticalGlue());
        btnWrapper.add(backBtn);
        btnWrapper.add(Box.createVerticalGlue());

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(btnWrapper, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFilterRow() {
        JPanel wrapper = new JPanel(new BorderLayout(15, 0));
        wrapper.setOpaque(false);

        // Search Pill
        JPanel searchPill = createPillWrapper(300, 42);
        JLabel searchIcon = createScaledIcon("/src/resources/images/search.png", 16, 16);
        searchField = new JTextField("Search products...");
        styleSearchField(searchField);
        searchPill.add(searchIcon, BorderLayout.WEST);
        searchPill.add(searchField, BorderLayout.CENTER);

        // Extra Filters
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setOpaque(false);

        JComboBox<String> stockCombo = new JComboBox<>(
                new String[] { "All Stock Levels", "In Stock", "Low Stock", "Out of Stock" });
        rightSide.add(createPillCombo(stockCombo, 160));

        // Threshold Spinner (Pill shaped)
        JPanel thresholdPill = createPillWrapper(80, 42);
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));
        styleSpinner(thresholdSpinner);
        thresholdPill.add(thresholdSpinner, BorderLayout.CENTER);
        rightSide.add(thresholdPill);

        // Set Threshold Button (Amber Pill)
        JButton setThresholdBtn = buildActionButton("Set Low Stock Threshold", AMBER_BTN, 180, 42);
        rightSide.add(setThresholdBtn);

        wrapper.add(searchPill, BorderLayout.CENTER);
        wrapper.add(rightSide, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildStockListCard() {
        JPanel card = createContentCard("Inventory Stock List");
        card.setPreferredSize(new Dimension(0, 420));

        String[] cols = { "Product Name", "Category", "Current Stock", "Reorder Point", "Status" };
        stockModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        stockTable = createStyledTable(stockModel);

        // Sample Data
        stockModel.addRow(new Object[] { "Ariel Powder 1kg", "Powder Detergent", "25", "10", "In Stock" });
        stockModel.addRow(new Object[] { "Tide Liquid 2L", "Liquid Detergent", "15", "10", "In Stock" });
        stockModel.addRow(new Object[] { "Downy 1.5L", "Fabric Conditioner", "8", "10", "Low Stock" });
        stockModel.addRow(new Object[] { "Surf Powder 500g", "Powder Detergent", "30", "15", "In Stock" });

        JScrollPane scroll = createStyledScrollPane(stockTable);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actionP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        actionP.setOpaque(false);
        JButton updateBtn = buildActionButton("Update Stock", GREEN_BTN, 140, 38);
        updateBtn.addActionListener(e -> {
            int selectedRow = stockTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to update.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String productName = (String) stockModel.getValueAt(selectedRow, 0);
            int currentStock = Integer.parseInt((String) stockModel.getValueAt(selectedRow, 2));
            Object[] result = UpdateStockDialog.showUpdateStock(SwingUtilities.getWindowAncestor(this), productName,
                    currentStock);

            if (result != null) {
                String type = (String) result[0];
                int qty = Integer.parseInt((String) result[1]);

                // Update Stock Model
                int newStock = type.equals("Stock In") ? currentStock + qty : currentStock - qty;

                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock for this operation.", "Stock Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                stockModel.setValueAt(String.valueOf(newStock), selectedRow, 2);

                // Update Status
                int reorder = Integer.parseInt((String) stockModel.getValueAt(selectedRow, 3));
                String status = newStock == 0 ? "Out of Stock" : (newStock <= reorder ? "Low Stock" : "In Stock");
                stockModel.setValueAt(status, selectedRow, 4);

                // Update History Model
                String date = java.time.LocalDate.now().toString();
                String qtyStr = (type.equals("Stock In") ? "+" : "-") + qty;
                historyModel.insertRow(0, new Object[] { date, productName, type, qtyStr, "Admin" });

                JOptionPane.showMessageDialog(this, "Stock for " + productName + " updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        actionP.add(updateBtn);
        card.add(actionP, BorderLayout.SOUTH);

        return wrapInShadow(card);
    }

    private JPanel buildHistoryCard() {
        JPanel card = createContentCard("Stock Update History");
        card.setPreferredSize(new Dimension(0, 350));

        String[] cols = { "Date", "Product", "Type", "Quantity", "Updated By" };
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        historyTable = createStyledTable(historyModel);

        // Sample Data
        historyModel.addRow(new Object[] { "2025-02-15", "Ariel Powder 1kg", "Stock In", "+20", "Admin" });
        historyModel.addRow(new Object[] { "2025-02-14", "Tide Liquid 2L", "Stock Out", "-5", "Staff" });

        JScrollPane scroll = createStyledScrollPane(historyTable);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actionP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        actionP.setOpaque(false);
        actionP.add(buildActionButton("Generate Report", GREEN_BTN, 140, 38));
        card.add(actionP, BorderLayout.SOUTH);

        return wrapInShadow(card);
    }

    // ── Utility Component Builders ───────────────────────────────────────────

    private JPanel createContentCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setForeground(UIStyle.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
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
        table.setRowHeight(62);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBorder(null);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setForeground(new Color(100, 116, 139));
        th.setBackground(new Color(248, 250, 252));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);

        // Custom Header Renderer to remove "hati"
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setBackground(new Color(248, 250, 252));
                l.setForeground(new Color(100, 116, 139));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setOpaque(true);
                return l;
            }
        };
        th.setDefaultRenderer(hr);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });
        return table;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(226, 232, 240)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel createPillWrapper(int width, int height) {
        JPanel p = new JPanel(new BorderLayout()) {
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
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, height));
        p.setBorder(new EmptyBorder(0, 15, 0, 15));
        return p;
    }

    private JPanel createPillCombo(JComboBox<String> combo, int width) {
        JPanel pill = createPillWrapper(width, 42);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setFocusable(false);
        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setBorder(null);
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
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                // Ensure no default background painting
                super.paint(g, c);
            }
        });
        pill.add(combo, BorderLayout.CENTER);
        return pill;
    }

    private void styleSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(UIStyle.TEXT_SECONDARY);
        field.setBorder(new EmptyBorder(0, 8, 0, 0));
        field.setOpaque(false);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("Search products...")) {
                    field.setText("");
                    field.setForeground(UIStyle.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("Search products...");
                    field.setForeground(UIStyle.TEXT_SECONDARY);
                }
            }
        });
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBorder(BorderFactory.createEmptyBorder());
        spinner.setOpaque(false);
        spinner.setBackground(new Color(0, 0, 0, 0));

        // Aggressively remove borders from all sub-components
        for (Component c : spinner.getComponents()) {
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
            }
        }

        JComponent editor = spinner.getEditor();
        editor.setOpaque(false);
        editor.setBackground(new Color(0, 0, 0, 0));
        editor.setBorder(BorderFactory.createEmptyBorder());

        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setOpaque(false);
            tf.setBackground(new Color(0, 0, 0, 0));
            tf.setBorder(BorderFactory.createEmptyBorder());
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setFont(new Font("Segoe UI", Font.BOLD, 13));
            tf.setForeground(UIStyle.TEXT_PRIMARY);
        }

        spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                return null;
            }

            @Override
            protected Component createPreviousButton() {
                return null;
            }
        });
    }

    private JButton buildActionButton(String text, Color color, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.darker() : color);
                // Use pill rounding (height)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
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
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createScaledIcon(String path, int w, int h) {
        JLabel lbl = new JLabel();
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon original = new ImageIcon(url);
                Image scaled = original.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                lbl.setIcon(new ImageIcon(scaled));
            } else {
                lbl.setText("🔍");
            }
        } catch (Exception e) {
            lbl.setText("🔍");
        }
        return lbl;
    }

    private JPanel wrapInShadow(JPanel content) {
        ShadowPanel sp = new ShadowPanel();
        sp.add(content);
        return sp;
    }
}