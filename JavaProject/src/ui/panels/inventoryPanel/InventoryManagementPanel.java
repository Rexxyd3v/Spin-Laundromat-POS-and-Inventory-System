package src.ui.panels.inventoryPanel;

import javax.swing.*;
import java.awt.*;
import src.util.components.ShadowPanel;
import src.util.components.Scrollbar;

public class InventoryManagementPanel extends JPanel {
    private static final Color MAIN_BG = new Color(200, 240, 248);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    private static final Color ACCENT_BLUE = new Color(14, 165, 233);

    public InventoryManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        initUI();
    }

    private void initUI() {
        // Main content container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(MAIN_BG);
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Card panel for inventory management
        JPanel cardPanel = createCardPanel();
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(cardPanel);
        contentPanel.add(Box.createVerticalGlue());

        // Wrap in scroll pane for responsive behavior
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setBackground(MAIN_BG);
        scrollPane.getViewport().setBackground(MAIN_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createCardPanel() {
        ShadowPanel outerCard = new ShadowPanel();
        outerCard.setLayout(new BorderLayout());
        outerCard.setMaximumSize(new Dimension(650, 450));
        outerCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_BG);
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Top section with icon and title
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(CARD_BG);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        topPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Icon/Emoji
        JLabel iconLabel = new JLabel("📦");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(iconLabel);
        topPanel.add(Box.createVerticalStrut(15));

        // Title
        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(10));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Manage supplies, stocks, and equipment inventory");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(subtitleLabel);

        cardPanel.add(topPanel, BorderLayout.NORTH);

        // Middle section with description
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBackground(CARD_BG);
        middlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        middlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel descLabel = new JLabel(
                "<html><div style='text-align: center;'>Access detailed inventory information, update stock levels, track stock history, and manage supplies efficiently.</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        middlePanel.add(descLabel);

        cardPanel.add(middlePanel, BorderLayout.CENTER);

        // Bottom section with button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(CARD_BG);
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton goToInventoryBtn = createStyledButton("Go to Inventory Stocks", ACCENT_BLUE, Color.WHITE, 280, 50);
        goToInventoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        goToInventoryBtn.addActionListener(e -> openInventoryStocks());
        bottomPanel.add(goToInventoryBtn);

        cardPanel.add(bottomPanel, BorderLayout.SOUTH);

        outerCard.add(cardPanel);
        return outerCard;
    }

    private void openInventoryStocks() {
        // Create a new dialog with StockManagementPanel
        JDialog inventoryDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Inventory Stocks Management", false);
        inventoryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        inventoryDialog.setUndecorated(false); // Ensure window controls are visible
        inventoryDialog.setResizable(true); // Enable resize functionality
        inventoryDialog.setSize(1200, 800);
        inventoryDialog.setMinimumSize(new Dimension(900, 600));
        inventoryDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        // Add the StockManagementPanel
        StockManagementPanel stockPanel = new StockManagementPanel();
        inventoryDialog.add(stockPanel, BorderLayout.CENTER);

        inventoryDialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = bgColor;
                if (getModel().isPressed()) {
                    color = bgColor.darker();
                } else if (getModel().isRollover()) {
                    color = new Color(
                            Math.min(bgColor.getRed() + 20, 255),
                            Math.min(bgColor.getGreen() + 20, 255),
                            Math.min(bgColor.getBlue() + 20, 255));
                }

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(fgColor);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        btn.setMinimumSize(new Dimension(Math.max(200, width - 100), height - 10));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
