package src.ui.panels.inventoryPanel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class LaundryInventorySystem extends JPanel {

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> supplierCombo;
    private JComboBox<String> stockLevelCombo;
    private JTextField searchField;

    public LaundryInventorySystem() {
        initComponents();
    }

    private void initComponents() {
        // Main container
        setLayout(new BorderLayout());
        setBackground(new Color(220, 250, 250));

        // Main content area
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(220, 250, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(220, 250, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Title section
        JPanel titleSection = new JPanel();
        titleSection.setLayout(new BoxLayout(titleSection, BoxLayout.Y_AXIS));
        titleSection.setBackground(new Color(220, 250, 250));

        JLabel title = new JLabel("Supplies & Equipment Inventory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Manage laundry supplies and stock levels");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        titleSection.add(title);
        titleSection.add(subtitle);

        // Button
        JButton stocksButton = new JButton("Go to Inventory Stocks");
        stocksButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stocksButton.setBackground(new Color(40, 40, 40));
        stocksButton.setForeground(Color.WHITE);
        stocksButton.setFocusPainted(false);
        stocksButton.setBorderPainted(false);
        stocksButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stocksButton.setPreferredSize(new Dimension(180, 35));
        stocksButton.addActionListener(e -> {
            // Open the StockManagementPanel as a dialog
            JDialog stocksDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(LaundryInventorySystem.this),
                    "Inventory Stocks Management", false);
            stocksDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            stocksDialog.setUndecorated(false); // Ensure window controls are visible
            stocksDialog.setResizable(true); // Enable resize functionality
            stocksDialog.setSize(1200, 800);
            stocksDialog.setMinimumSize(new Dimension(900, 600));
            stocksDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(LaundryInventorySystem.this));

            StockManagementPanel stockPanel = new StockManagementPanel();
            stocksDialog.add(stockPanel, BorderLayout.CENTER);
            stocksDialog.setVisible(true);
        });

        headerPanel.add(titleSection, BorderLayout.WEST);
        headerPanel.add(stocksButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(new Color(220, 250, 250));

        // Stats cards
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, BorderLayout.NORTH);

        // Inventory section
        JPanel inventorySection = createInventorySection();
        contentPanel.add(inventorySection, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(new Color(220, 250, 250));
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsPanel.setPreferredSize(new Dimension(0, 100));

        // Total Products Card
        JPanel totalCard = createStatCard("Total Products", "18", "�");

        // Low Stocks Card
        JPanel lowStockCard = createStatCard("Low Stocks", "3", "⚠️");

        // Inventory Value Card
        JPanel valueCard = createStatCard("Inventory Value", "₱234,200.00", "₱");

        statsPanel.add(totalCard);
        statsPanel.add(lowStockCard);
        statsPanel.add(valueCard);

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(valueLabel);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }

    private JPanel createInventorySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        // Filter panel
        JPanel filterPanel = createFilterPanel();
        section.add(filterPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = createTablePanel();
        section.add(tablePanel, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = createActionPanel();
        section.add(actionPanel, BorderLayout.SOUTH);

        return section;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel filterLabel = new JLabel("Filter");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        searchField.setMinimumSize(new Dimension(180, 30));
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setMaximumSize(new Dimension(250, 30));

        categoryCombo = new JComboBox<>(new String[] { "All Categories" });
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryCombo.setPreferredSize(new Dimension(150, 30));

        supplierCombo = new JComboBox<>(new String[] { "All Suppliers" });
        supplierCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        supplierCombo.setPreferredSize(new Dimension(150, 30));

        stockLevelCombo = new JComboBox<>(new String[] { "All Stock Levels" });
        stockLevelCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stockLevelCombo.setPreferredSize(new Dimension(150, 30));

        filterPanel.add(filterLabel);
        filterPanel.add(searchField);
        filterPanel.add(categoryCombo);
        filterPanel.add(supplierCombo);
        filterPanel.add(stockLevelCombo);

        return filterPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tableTitle = new JLabel("Inventory List");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Create table
        String[] columns = { "Product Code", "Product Name", "Category", "Supplier", "Quantity", "Unit", "Price",
                "Total Value", "Stock Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inventoryTable.setRowHeight(40);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        inventoryTable.getTableHeader().setBackground(new Color(250, 250, 250));
        inventoryTable.setSelectionBackground(new Color(33, 150, 243, 50));
        inventoryTable.setGridColor(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JButton addButton = createActionButton("+ Add Product", new Color(92, 184, 92));
        JButton editButton = createActionButton("Edit Product", new Color(91, 192, 222));
        JButton deleteButton = createActionButton("Delete Product", new Color(217, 83, 79));

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        return actionPanel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }
}
