package src.ui.panels.inventoryPanel;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import src.util.UIStyle;

public class StockManagementPanel extends JPanel {

    public StockManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        initUI();
    }

    private void initUI() {
        // Header with back button
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(UIStyle.BACKGROUND);

        // Filter section
        JPanel filterPanel = createFilterPanel();
        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Stock list section
        JPanel stockListPanel = createStockListPanel();
        mainPanel.add(stockListPanel, BorderLayout.CENTER);

        // History section
        JPanel historyPanel = createHistoryPanel();
        mainPanel.add(historyPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Inventory Stocks Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Update, Stock In, Stock Out Inventory Management System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(UIStyle.TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        // Back button
        JButton backButton = createStyledButton("Go back to Inventory", UIStyle.TEXT_PRIMARY, Color.WHITE, 200, 40);
        backButton.addActionListener(e -> goBackToInventory());

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void goBackToInventory() {
        // Close current window
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel filterLabel = new JLabel("Filter");
        filterLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        filterLabel.setForeground(UIStyle.TEXT_PRIMARY);

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterControls.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JComboBox<String> stockLevelFilter = new JComboBox<>(
                new String[] { "All Stock Levels", "In Stock", "Low Stock", "Out of Stock" });
        stockLevelFilter.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        thresholdSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) thresholdSpinner.getEditor()).getTextField().setColumns(3);

        JButton setThresholdBtn = createStyledButton("Set Low Stock Treshold", new Color(249, 115, 22), Color.WHITE,
                180, 35);

        filterControls.add(searchField);
        filterControls.add(stockLevelFilter);
        filterControls.add(thresholdSpinner);
        filterControls.add(setThresholdBtn);

        JPanel filterTop = new JPanel(new BorderLayout());
        filterTop.setBackground(Color.WHITE);
        filterTop.add(filterLabel, BorderLayout.NORTH);
        filterTop.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        filterPanel.add(filterTop, BorderLayout.NORTH);
        filterPanel.add(filterControls, BorderLayout.CENTER);

        return filterPanel;
    }

    private JPanel createStockListPanel() {
        JPanel stockListPanel = new JPanel(new BorderLayout(0, 10));
        stockListPanel.setBackground(Color.WHITE);
        stockListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel stockListLabel = new JLabel("Inventory Stock List");
        stockListLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        stockListLabel.setForeground(UIStyle.TEXT_PRIMARY);

        // Stock Table
        String[] stockColumns = { "Product Name", "Category", "Current Stock", "Reorder Point", "Status" };
        DefaultTableModel stockTableModel = new DefaultTableModel(stockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable stockTable = createTable(stockTableModel);

        // Sample data
        stockTableModel.addRow(new Object[] { "Ariel Powder 1kg", "Powder Detergent", "25", "10", "In Stock" });
        stockTableModel.addRow(new Object[] { "Tide Liquid 2L", "Liquid Detergent", "15", "10", "In Stock" });
        stockTableModel.addRow(new Object[] { "Downy 1.5L", "Fabric Conditioner", "8", "10", "Low Stock" });
        stockTableModel.addRow(new Object[] { "Surf Powder 500g", "Powder Detergent", "30", "15", "In Stock" });
        stockTableModel.addRow(new Object[] { "Breeze Liquid 1L", "Liquid Detergent", "5", "10", "Low Stock" });

        JScrollPane stockScrollPane = new JScrollPane(stockTable);
        stockScrollPane.setBorder(BorderFactory.createEmptyBorder());
        stockScrollPane.setPreferredSize(new Dimension(0, 250));

        JPanel stockActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        stockActionPanel.setBackground(Color.WHITE);

        JButton updateStockBtn = createStyledButton("Update Stock", new Color(34, 197, 94), Color.WHITE, 150, 38);
        updateStockBtn.addActionListener(e -> showUpdateStockConfirmation(stockTable));
        stockActionPanel.add(updateStockBtn);

        stockListPanel.add(stockListLabel, BorderLayout.NORTH);
        stockListPanel.add(stockScrollPane, BorderLayout.CENTER);
        stockListPanel.add(stockActionPanel, BorderLayout.SOUTH);

        return stockListPanel;
    }

    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout(0, 10));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel historyLabel = new JLabel("Stock Update History");
        historyLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        historyLabel.setForeground(UIStyle.TEXT_PRIMARY);

        // History Table
        String[] historyColumns = { "Date", "Product", "Type", "Quantity", "Updated By" };
        DefaultTableModel historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = createTable(historyTableModel);

        // Sample data
        historyTableModel.addRow(new Object[] { "2025-02-15", "Ariel Powder 1kg", "Stock In", "+20", "Admin" });
        historyTableModel.addRow(new Object[] { "2025-02-14", "Tide Liquid 2L", "Stock Out", "-5", "Admin" });
        historyTableModel.addRow(new Object[] { "2025-02-14", "Downy 1.5L", "Stock In", "+10", "Admin" });
        historyTableModel.addRow(new Object[] { "2025-02-13", "Surf Powder 500g", "Stock Out", "-8", "Admin" });

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyScrollPane.setPreferredSize(new Dimension(0, 200));

        JPanel historyActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        historyActionPanel.setBackground(Color.WHITE);

        JButton generateReportBtn = createStyledButton("Generate Report", new Color(34, 197, 94), Color.WHITE, 150, 38);
        generateReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Report generated successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE));
        historyActionPanel.add(generateReportBtn);

        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        historyPanel.add(historyActionPanel, BorderLayout.SOUTH);

        return historyPanel;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 250, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    private void showUpdateStockConfirmation(JTable stockTable) {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update stock.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show custom confirmation dialog matching your design
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirmation", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(207, 250, 254));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));

        JLabel messageLabel = new JLabel(
                "<html><div style='text-align: center;'>Are you sure?<br>You want to update this stock?</div></html>");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        messageLabel.setForeground(UIStyle.TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(207, 250, 254));

        JButton yesButton = createDialogButton("Yes", new Color(34, 197, 94));
        JButton noButton = createDialogButton("No", new Color(239, 68, 68));

        yesButton.addActionListener(e -> {
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Stock updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        noButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        contentPanel.add(buttonPanel);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = bgColor;
                if (getModel().isPressed())
                    color = bgColor.darker();
                else if (getModel().isRollover())
                    color = bgColor.brighter();

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(fgColor);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createDialogButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = bgColor;
                if (getModel().isPressed())
                    color = bgColor.darker();
                else if (getModel().isRollover())
                    color = bgColor.brighter();

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(100, 45));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}