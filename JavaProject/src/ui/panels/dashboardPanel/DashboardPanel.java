package src.ui.panels.dashboardPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import src.util.components.Scrollbar;
import src.util.components.ShadowPanel;

public class DashboardPanel extends JPanel {

    private static final Color MAIN_BG = new Color(200, 240, 248);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    private static final Color GREEN_POSITIVE = new Color(76, 175, 80);
    private static final Color RED_NEGATIVE = new Color(244, 67, 54);
    private static final Color SIDEBAR_SELECTED = new Color(33, 150, 243);

    // -------------------------------------------------------------------------
    // Icon file paths — replace these with your actual image paths
    // -------------------------------------------------------------------------
    private static final String ICON_BELL         = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\bell.png";
    private static final String ICON_DELETE       = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\trashcan.png";
    private static final String ICON_REVENUE      = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\revenue.png";
    private static final String ICON_ORDERS       = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\box.png";
    private static final String ICON_LOW_STOCK    = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\warning.png";
    private static final String ICON_COMPLETED    = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\check.png";
    private static final String ICON_WARNING      = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\warning.png";
    private static final String ICON_STOCK_UPDATE = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\update.png";
    private static final String ICON_ITEM_REMOVED = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\remove.png";
    private static final String ICON_UNREAD       = "C:\\Users\\Acer\\laundrydraft\\JavaProject\\src\\resources\\images\\unread.png";

    // Helper: load and scale an ImageIcon from a file path
    private static ImageIcon loadIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public DashboardPanel() {
        setLayout(new BorderLayout());
        initMainContent();
    }

    private void initMainContent() {
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel header = createHeader();
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        add(header, BorderLayout.NORTH);

        JPanel dashboardContent = createDashboardContent();
        add(dashboardContent, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_BG);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(MAIN_BG);
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Analytics & Monitoring");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        JPanel bellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bellPanel.setBackground(MAIN_BG);

        JLabel notificationBell = new JLabel(loadIcon(ICON_BELL, 22));
        notificationBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellPanel.add(notificationBell);

        JLabel badge = new JLabel("1");
        badge.setForeground(Color.WHITE);
        badge.setBackground(Color.RED);
        badge.setOpaque(true);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        bellPanel.add(badge);

        notificationBell.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showNotificationPopup(notificationBell);
            }
        });

        header.add(bellPanel, BorderLayout.EAST);

        return header;
    }

    private void showNotificationPopup(JLabel notificationBell) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setPreferredSize(new Dimension(350, 400));

        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Notifications", loadIcon(ICON_BELL, 16), JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel countBadge = new JLabel("3");
        countBadge.setForeground(Color.WHITE);
        countBadge.setBackground(Color.RED);
        countBadge.setOpaque(true);
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        countBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        headerPanel.add(countBadge, BorderLayout.CENTER);

        // --- FUNCTIONAL: Delete all notifications ---
        List<JPanel> notificationCards = new ArrayList<>();
        JPanel notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(Color.WHITE);

        JPanel card1 = createNotificationCard("Low Stock Alert!",  loadIcon(ICON_WARNING,       14), "Downy has only 10 units remaining!",               "Unread", loadIcon(ICON_UNREAD, 10), "1m ago",  new Color(255, 243, 224));
        JPanel card2 = createNotificationCard("Stock Updated!",    loadIcon(ICON_STOCK_UPDATE,  14), "Tide powder 35 pcs has been added to inventory!",  "Unread", loadIcon(ICON_UNREAD, 10), "3m ago",  new Color(227, 242, 253));
        JPanel card3 = createNotificationCard("Item Removed!",     loadIcon(ICON_ITEM_REMOVED,  14), "Ariel powder has been removed to your inventory!", "Unread", loadIcon(ICON_UNREAD, 10), "12m ago", new Color(255, 235, 238));

        notificationCards.add(card1);
        notificationCards.add(card2);
        notificationCards.add(card3);

        notificationsContainer.add(card1);
        notificationsContainer.add(Box.createVerticalStrut(10));
        notificationsContainer.add(card2);
        notificationsContainer.add(Box.createVerticalStrut(10));
        notificationsContainer.add(card3);

        JLabel deleteIcon = new JLabel(loadIcon(ICON_DELETE, 16));
        deleteIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notificationsContainer.removeAll();
                JLabel empty = new JLabel("No notifications");
                empty.setForeground(TEXT_SECONDARY);
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                notificationsContainer.add(empty);
                countBadge.setText("0");
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
            }
        });
        headerPanel.add(deleteIcon, BorderLayout.EAST);

        container.add(headerPanel);
        container.add(notificationsContainer);

        popup.add(container);
        popup.show(notificationBell, -320, notificationBell.getHeight() + 10);
    }

    private JPanel createDashboardContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(MAIN_BG);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setBackground(MAIN_BG);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        statsRow.add(wrapInShadow(createStatCard(ICON_REVENUE,   "Today's Revenue",  "₱142,395.00", "+15.23% from yesterday", true)));
        statsRow.add(wrapInShadow(createStatCard(ICON_ORDERS,    "Orders Today",     "876",         "+8.32% from yesterday",  true)));
        statsRow.add(wrapInShadow(createStatCard(ICON_LOW_STOCK, "Low Stock Items",  "23",          "-3.1% from yesterday",   false)));
        statsRow.add(wrapInShadow(createStatCard(ICON_COMPLETED, "Orders Completed", "835",         "+3.9% from yesterday",   true)));
        content.add(statsRow);

        content.add(Box.createVerticalStrut(10));

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 10, 0));
        middleRow.setBackground(MAIN_BG);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        middleRow.add(wrapInShadow(createRevenueOverviewPanel()));
        middleRow.add(wrapInShadow(createRecentOrdersPanel()));
        content.add(middleRow);

        content.add(Box.createVerticalStrut(10));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomRow.setBackground(MAIN_BG);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        bottomRow.add(wrapInShadow(createBestSellingPanel()));
        bottomRow.add(wrapInShadow(createLowStockPanel()));
        content.add(bottomRow);

        content.add(Box.createVerticalGlue());

        return content;
    }

    private JPanel createStatCard(String iconPath, String title, String value, String change, boolean positive) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel iconLabel = new JLabel(loadIcon(iconPath, 32));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(5));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);

        card.add(Box.createVerticalStrut(5));

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        changeLabel.setForeground(positive ? GREEN_POSITIVE : RED_NEGATIVE);
        changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(changeLabel);

        return card;
    }

    // -------------------------------------------------------------------------
    // REVENUE OVERVIEW — functional tab buttons switching chart data
    // -------------------------------------------------------------------------
    private JPanel createRevenueOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        JLabel title = new JLabel("Revenue Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        int[][] revenueData = {
            { 2300, 1600, 2400, 2300, 3000, 3500, 2900 },
            { 1200, 3400, 2800, 4100, 3700, 5200, 4600 },
            { 8000, 9200, 7500, 11000, 13000, 10500, 12000 },
            { 45000, 52000, 48000, 61000, 58000, 70000, 65000 }
        };
        String[][] revenueLabels = {
            { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" },
            { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" },
            { "W1",  "W2",  "W3",  "W4",  "W5",  "W6",  "W7"  },
            { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul" }
        };

        RevenueChartPanel chartPanel = new RevenueChartPanel();
        chartPanel.updateData(revenueData[1], revenueLabels[1]);

        String[] tabNames = { "Today", "Week", "Month", "Year" };
        int[] defaultSelected = { 1 };
        JButton[] tabs = new JButton[tabNames.length];

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        tabPanel.setBackground(CARD_BG);

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            tabs[i] = createTabButton(tabNames[i], i == defaultSelected[0]);
            tabs[i].addActionListener(e -> {
                for (JButton btn : tabs) {
                    btn.setBackground(new Color(220, 220, 220));
                    btn.setForeground(TEXT_SECONDARY);
                }
                tabs[idx].setBackground(SIDEBAR_SELECTED);
                tabs[idx].setForeground(Color.WHITE);
                chartPanel.updateData(revenueData[idx], revenueLabels[idx]);
            });
            tabPanel.add(tabs[i]);
        }

        header.add(tabPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------------------
    // BEST SELLING — functional tab buttons switching chart data
    // -------------------------------------------------------------------------
    private JPanel createBestSellingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        JLabel title = new JLabel("Best Selling Services & Products");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        int[][] salesData = {
            { 18, 12, 10,  9,  9,  6,  4,  2 },
            { 65, 48, 40, 35, 35, 24, 15,  6 },
            { 90, 72, 60, 52, 52, 38, 22, 10 },
            { 145, 105, 88, 75, 75, 54, 32, 14 }
        };
        double[][] amountData = {
            { 3400,  2700,  2800,  2900,  2900,  2100,  1900,   900 },
            { 12500, 10200,  9800,  9500,  9500,  7200,  5600,  2600 },
            { 18000, 14500, 13000, 12000, 12000,  9000,  7000,  3200 },
            { 27320, 24230, 24330, 24336, 24337, 19337, 17340,  8329 }
        };

        BarChartPanel chartPanel = new BarChartPanel();
        chartPanel.updateData(salesData[3], amountData[3]);

        String[] tabNames = { "Today", "Week", "Month", "Year" };
        int[] defaultSelected = { 3 };
        JButton[] tabs = new JButton[tabNames.length];

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        tabPanel.setBackground(CARD_BG);

        for (int i = 0; i < tabNames.length; i++) {
            final int idx = i;
            tabs[i] = createTabButton(tabNames[i], i == defaultSelected[0]);
            tabs[i].addActionListener(e -> {
                for (JButton btn : tabs) {
                    btn.setBackground(new Color(220, 220, 220));
                    btn.setForeground(TEXT_SECONDARY);
                }
                tabs[idx].setBackground(SIDEBAR_SELECTED);
                tabs[idx].setForeground(Color.WHITE);
                chartPanel.updateData(salesData[idx], amountData[idx]);
            });
            tabPanel.add(tabs[i]);
        }

        header.add(tabPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createTabButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        if (selected) {
            button.setBackground(SIDEBAR_SELECTED);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(220, 220, 220));
            button.setForeground(TEXT_SECONDARY);
        }
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return button;
    }

    private JPanel createRecentOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Recent Orders");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        String[] columns = { "Date", "Time", "Name", "Amount" };
        Object[][] data = {
                { "02-18-26", "12:15PM", "Jericho", "₱150.00" },
                { "02-18-26", "11:46AM", "Vinon",   "₱422.00" },
                { "02-18-26", "11:35AM", "Ilya",    "₱531.00" },
                { "02-18-26", "11:30AM", "Athina",  "₱123.00" },
                { "02-18-26", "11:23AM", "Joseph",  "₱354.00" },
                { "02-18-26", "11:20AM", "Zyrich",  "₱435.00" },
                { "02-18-26", "11:00AM", "Denife",  "₱111.00" },
                { "02-18-26", "10:45AM", "Adrian",  "₱54.00"  }
        };

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(CARD_BG);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Low Stock Quick List");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(CARD_BG);

        listPanel.add(createLowStockItem(ICON_WARNING, "Downy - Low stock (10 remaining)",      new Color(255, 248, 225)));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createLowStockItem(ICON_WARNING, "Tide Powder - Low stock (2 remaining)", new Color(255, 235, 238)));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createLowStockItem(ICON_WARNING, "Surf - Low stock (5 remaining)",        new Color(255, 248, 225)));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createLowStockItem(ICON_WARNING, "Champion - Low stock (1 remaining)",    new Color(255, 248, 225)));

        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowStockItem(String iconPath, String text, Color bgColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        item.setBackground(bgColor);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setBorder(new RoundedBorder(6));

        JLabel iconLabel = new JLabel(loadIcon(iconPath, 14));
        item.add(iconLabel);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_PRIMARY);
        item.add(label);

        return item;
    }

    private JPanel createNotificationCard(String title, ImageIcon titleIcon, String message, String status, ImageIcon statusIcon, String time, Color borderColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, borderColor),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(title, titleIcon, JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_PRIMARY);
        topRow.add(titleLabel, BorderLayout.WEST);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(TEXT_SECONDARY);
        topRow.add(timeLabel, BorderLayout.EAST);
        card.add(topRow);

        card.add(Box.createVerticalStrut(5));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        messageLabel.setForeground(TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(messageLabel);

        card.add(Box.createVerticalStrut(5));

        JLabel statusLabel = new JLabel(status, statusIcon, JLabel.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        statusLabel.setForeground(SIDEBAR_SELECTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);

        return card;
    }

    private JPanel wrapInShadow(JPanel content) {
        ShadowPanel shadow = new ShadowPanel();
        shadow.add(content);
        return shadow;
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    static class RoundedBorder extends AbstractBorder {
        private final int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(230, 230, 230));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    class RevenueChartPanel extends JPanel {
        private int[] values = { 2300, 1600, 2400, 2300, 3000, 3500, 2900 };
        private String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

        public RevenueChartPanel() {
            setBackground(CARD_BG);
        }

        public void updateData(int[] newValues, String[] newLabels) {
            this.values = newValues;
            this.days = newLabels;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 50;
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;

            int maxVal = 0;
            for (int v : values) if (v > maxVal) maxVal = v;
            int minVal = Integer.MAX_VALUE;
            for (int v : values) if (v < minVal) minVal = v;
            int range = Math.max(maxVal - minVal, 1);

            g2d.setColor(new Color(240, 240, 240));
            for (int i = 0; i <= 5; i++) {
                int y = padding + (chartHeight * i / 5);
                g2d.drawLine(padding, y, width - padding, y);
            }

            g2d.setColor(SIDEBAR_SELECTED);
            g2d.setStroke(new BasicStroke(2.5f));

            for (int i = 0; i < values.length - 1; i++) {
                int x1 = padding + (chartWidth * i / (values.length - 1));
                int y1 = padding + chartHeight - (values[i] - minVal) * chartHeight / range;
                int x2 = padding + (chartWidth * (i + 1) / (values.length - 1));
                int y2 = padding + chartHeight - (values[i + 1] - minVal) * chartHeight / range;
                g2d.drawLine(x1, y1, x2, y2);
            }

            for (int i = 0; i < values.length; i++) {
                int x = padding + (chartWidth * i / (values.length - 1));
                int y = padding + chartHeight - (values[i] - minVal) * chartHeight / range;
                g2d.fillOval(x - 5, y - 5, 10, 10);
            }

            g2d.setColor(TEXT_SECONDARY);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i < days.length; i++) {
                int x = padding + (chartWidth * i / (values.length - 1));
                g2d.drawString(days[i], x - 12, height - padding + 20);
            }

            for (int i = 0; i <= 5; i++) {
                int value = maxVal - (i * range / 5);
                int y = padding + (chartHeight * i / 5);
                g2d.drawString(String.valueOf(value), 5, y + 5);
            }
        }
    }

    class BarChartPanel extends JPanel {
        private final String[] labels = { "Full\nService", "Wash\n&\nDry", "Downy", "Tide\nPowder",
                "Surf\nPowder", "Wash\nOnly", "Breeze\nPowder", "Dry\nOnly" };
        private int[] sales = { 145, 105, 88, 75, 75, 54, 32, 14 };
        private double[] amounts = { 27320.00, 24230.00, 24330.00, 24336.00, 24337.00, 19337.00, 17340.00, 8329.00 };

        public BarChartPanel() {
            setBackground(CARD_BG);
        }

        public void updateData(int[] newSales, double[] newAmounts) {
            this.sales = newSales;
            this.amounts = newAmounts;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 30;
            int chartHeight = height - 100;
            int barWidth = (width - 2 * padding) / labels.length - 15;

            int maxSales = 0;
            for (int s : sales) if (s > maxSales) maxSales = s;
            if (maxSales == 0) maxSales = 1;

            for (int i = 0; i < labels.length; i++) {
                int barHeight = (sales[i] * chartHeight) / maxSales;
                int x = padding + i * (barWidth + 15);
                int y = chartHeight - barHeight + padding;

                g2d.setColor(SIDEBAR_SELECTED);
                g2d.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                g2d.setColor(TEXT_PRIMARY);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String salesText = sales[i] + " Sales";
                int textWidth = g2d.getFontMetrics().stringWidth(salesText);
                g2d.drawString(salesText, x + (barWidth - textWidth) / 2, y - 18);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String amountText = String.format("₱%.2f", amounts[i]);
                textWidth = g2d.getFontMetrics().stringWidth(amountText);
                g2d.drawString(amountText, x + (barWidth - textWidth) / 2, y - 6);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String[] words = labels[i].split("\n");
                int labelY = chartHeight + padding + 18;
                for (String word : words) {
                    textWidth = g2d.getFontMetrics().stringWidth(word);
                    g2d.drawString(word, x + (barWidth - textWidth) / 2, labelY);
                    labelY += 11;
                }
            }
        }
    }
}