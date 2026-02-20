package src.ui.panels.reportPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class LaundryReportsSystem extends JPanel {

    private JTable profitabilityTable;
    private DefaultTableModel tableModel;

    public LaundryReportsSystem() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

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
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(220, 250, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JLabel subtitle = new JLabel("Generate comprehensive business reports");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitle);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(new Color(220, 250, 250));

        // Tab buttons
        JPanel tabPanel = createTabPanel();
        contentPanel.add(tabPanel, BorderLayout.NORTH);

        // Report content (initially showing profitability)
        JPanel profitabilityPanel = createProfitabilityPanel();

        // Show profitability by default
        contentPanel.add(profitabilityPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createTabPanel() {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setBackground(new Color(220, 250, 250));
        tabPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton profitabilityBtn = createTabButton("Profitability", true);
        JButton cashierBtn = createTabButton("Cashier Performance", false);
        JButton transactionBtn = createTabButton("Transaction History", false);

        profitabilityBtn.addActionListener(e -> switchTab(profitabilityBtn, cashierBtn, transactionBtn));
        cashierBtn.addActionListener(e -> switchTab(cashierBtn, profitabilityBtn, transactionBtn));
        transactionBtn.addActionListener(e -> switchTab(transactionBtn, profitabilityBtn, cashierBtn));

        tabPanel.add(profitabilityBtn);
        tabPanel.add(cashierBtn);
        tabPanel.add(transactionBtn);

        return tabPanel;
    }

    private JButton createTabButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (selected) {
            button.setBackground(new Color(33, 150, 243));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(33, 150, 243)),
                    BorderFactory.createEmptyBorder(10, 20, 7, 20)));
        } else {
            button.setBackground(new Color(180, 240, 245));
            button.setForeground(Color.BLACK);
            button.setBorderPainted(false);
        }

        return button;
    }

    private void switchTab(JButton selectedBtn, JButton... otherBtns) {
        // Update button styles
        selectedBtn.setBackground(new Color(33, 150, 243));
        selectedBtn.setForeground(Color.WHITE);
        selectedBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(33, 150, 243)),
                BorderFactory.createEmptyBorder(10, 20, 7, 20)));

        for (JButton btn : otherBtns) {
            btn.setBackground(new Color(180, 240, 245));
            btn.setForeground(Color.BLACK);
            btn.setBorderPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        // Switch content (you would implement this based on which button was clicked)
        // For now, we'll keep showing the profitability panel
    }

    private JPanel createProfitabilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(220, 250, 250));

        // Stats cards
        JPanel statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);

        // Table
        JPanel tablePanel = createProfitabilityTable();
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(new Color(220, 250, 250));
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Total Gross Profit Card
        JPanel profitCard = createMetricCard("Total Gross Profit", "₱7,300.00", "📈", new Color(76, 175, 80));

        // Average Margin Card
        JPanel marginCard = createMetricCard("Average Margin", "15.9%", "📄", Color.BLACK);

        // Best Margin Card
        JPanel bestCard = createBestMarginCard();

        statsPanel.add(profitCard);
        statsPanel.add(marginCard);
        statsPanel.add(bestCard);

        return statsPanel;
    }

    private JPanel createMetricCard(String title, String value, String icon, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(valueLabel);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }

    private JPanel createBestMarginCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Best Margin");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel("36.4%");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(76, 175, 80));

        JLabel productLabel = new JLabel("Full Service");
        productLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        productLabel.setForeground(Color.GRAY);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(valueLabel);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(productLabel);

        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }

    private JPanel createProfitabilityTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Create table
        String[] columns = { "RANK", "PRODUCT", "COST PRICE", "SELLING PRICE", "GROSS PROFIT", "MARGIN", "STATUS" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add sample data
        Object[][] data = {
                { "#1", "Full Service", "₱60.00", "₱195.00", "₱135.00", "35.0%", "Excellent" },
                { "#2", "Detergent (Powder)", "₱6.00", "₱18.00", "₱12.00", "20.0%", "Poor" }
        };

        for (Object[] row : data) {
            tableModel.addRow(row);
        }

        profitabilityTable = new JTable(tableModel);
        profitabilityTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profitabilityTable.setRowHeight(50);
        profitabilityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        profitabilityTable.getTableHeader().setBackground(Color.WHITE);
        profitabilityTable.getTableHeader().setForeground(Color.BLACK);
        profitabilityTable.setSelectionBackground(new Color(33, 150, 243, 30));
        profitabilityTable.setGridColor(new Color(240, 240, 240));
        profitabilityTable.setShowGrid(true);
        profitabilityTable.setIntercellSpacing(new Dimension(1, 1));

        // Custom renderer for status column
        profitabilityTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        // Set column widths
        profitabilityTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        profitabilityTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        profitabilityTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(profitabilityTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    // Placeholder panels for other tabs
    // Custom cell renderer for status badges
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.setOpaque(true);

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }

            JLabel label = new JLabel(value.toString());
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

            if (value.equals("Excellent")) {
                label.setBackground(new Color(33, 150, 243));
                label.setForeground(Color.WHITE);
            } else if (value.equals("Poor")) {
                label.setBackground(new Color(244, 67, 54));
                label.setForeground(Color.WHITE);
            }

            label.setBorder(BorderFactory.createCompoundBorder(
                    label.getBorder(),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));

            // Make the label rounded
            label.setOpaque(true);

            panel.add(label);
            return panel;
        }
    }
}