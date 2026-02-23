package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.SidebarButton;
import src.util.components.BrandIcons;
import src.util.config.*;
import src.util.components.Scrollbar;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private String activeTab = "Store";
    private JPanel mainContainer;
    private JScrollPane scrollPane;
    private java.util.Map<String, JComponent> tabSections = new java.util.LinkedHashMap<>();
    private boolean isProgrammaticScroll = false;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        // Create sticky header section
        JPanel stickyHeader = new JPanel();
        stickyHeader.setLayout(new BoxLayout(stickyHeader, BoxLayout.Y_AXIS));
        stickyHeader.setBackground(UIStyle.BACKGROUND);
        stickyHeader.setBorder(new EmptyBorder(30, 40, 0, 40));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UIStyle.BACKGROUND);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("System Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Configure your POS system and preferences");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UIStyle.TEXT_SECONDARY);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subtitle);

        stickyHeader.add(headerPanel);
        stickyHeader.add(Box.createVerticalStrut(25));

        // Tab Bar
        stickyHeader.add(createTabBar());
        stickyHeader.add(Box.createVerticalStrut(15));

        add(stickyHeader, BorderLayout.NORTH);

        mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(UIStyle.BACKGROUND);
        mainContainer.setBorder(new EmptyBorder(10, 40, 30, 40));

        // Load all tap content sections
        initTabSections();

        scrollPane = new JScrollPane(mainContainer);
        scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UIStyle.BACKGROUND);
        scrollPane.getViewport().setBackground(UIStyle.BACKGROUND);

        // Add scroll listener for automatic tab switching
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!isProgrammaticScroll) {
                updateActiveTabOnScroll();
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    private void initTabSections() {
        // Store
        SettingStoreTab storeTab = new SettingStoreTab();
        tabSections.put("Store", storeTab);
        mainContainer.add(storeTab);
        mainContainer.add(Box.createVerticalStrut(30));

        // System
        JPanel systemTab = createSystemConfigCard();
        tabSections.put("System", systemTab);
        mainContainer.add(systemTab);
        mainContainer.add(Box.createVerticalStrut(30));

        // Receipt
        SettingReceiptTab receiptTab = new SettingReceiptTab();
        tabSections.put("Receipt", receiptTab);
        mainContainer.add(receiptTab);
        mainContainer.add(Box.createVerticalStrut(30));

        // Security
        SettingSecurityTab securityTab = new SettingSecurityTab();
        tabSections.put("Security", securityTab);
        mainContainer.add(securityTab);
        mainContainer.add(Box.createVerticalStrut(30));

        // Alerts
        SettingAlertsTab alertsTab = new SettingAlertsTab();
        tabSections.put("Alerts", alertsTab);
        mainContainer.add(alertsTab);
        mainContainer.add(Box.createVerticalStrut(30));

        // Backup
        SettingBackupTab backupTab = new SettingBackupTab();
        tabSections.put("Backup", backupTab);
        mainContainer.add(backupTab);

        mainContainer.add(Box.createVerticalStrut(200)); // Padding at bottom
    }

    private void updateActiveTabOnScroll() {
        String newActiveTab = activeTab;
        int viewportTop = scrollPane.getViewport().getViewPosition().y;

        for (java.util.Map.Entry<String, JComponent> entry : tabSections.entrySet()) {
            if (entry.getValue().getY() <= viewportTop + 100) {
                newActiveTab = entry.getKey();
            }
        }

        if (!newActiveTab.equals(activeTab)) {
            activeTab = newActiveTab;
            repaint(); // Repaint tab bar to show new active state
        }
    }

    private void switchTab(String tabName) {
        activeTab = tabName;
        JComponent target = tabSections.get(tabName);
        if (target != null) {
            isProgrammaticScroll = true;
            // Scroll to the target component
            Rectangle rect = target.getBounds();
            // Adjust to show header a bit better
            rect.y -= 25;
            rect.height = scrollPane.getViewport().getHeight();
            mainContainer.scrollRectToVisible(rect);

            Timer timer = new Timer(500, e -> isProgrammaticScroll = false);
            timer.setRepeats(false);
            timer.start();
        }
        repaint();
    }

    private JPanel createTabBar() {
        JPanel tabContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow / Border effect
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                g2.setColor(new Color(226, 232, 240)); // Slate 200 border
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                g2.dispose();
            }
        };
        tabContainer.setOpaque(false);
        tabContainer.setBackground(Color.WHITE);
        tabContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        tabContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        tabContainer.add(createTabButton("Store", BrandIcons.STORE_IMG));
        tabContainer.add(createTabButton("System", BrandIcons.SYSTEM_IMG));
        tabContainer.add(createTabButton("Receipt", BrandIcons.RECEIPT_IMG));
        tabContainer.add(createTabButton("Security", BrandIcons.SECURITY_IMG));
        tabContainer.add(createTabButton("Alerts", BrandIcons.ALERTS_IMG));
        tabContainer.add(createTabButton("Backup", BrandIcons.BACKUP_IMG));

        return tabContainer;
    }

    private JButton createTabButton(String text, SidebarButton.IconPainter icon) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = text.equals(activeTab);
                if (isActive) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Pill shape
                }
                int iconSize = 20;
                int iconX = 15; // Adjusted padding
                int iconY = (getHeight() - iconSize) / 2;

                // Alert icon in tab bar is red in mockup
                Color color = isActive ? UIStyle.TEXT_PRIMARY : UIStyle.TEXT_SECONDARY;
                if (text.equals("Alerts")) {
                    color = new Color(220, 38, 38);
                }

                icon.paint(g2, iconX, iconY, iconSize, color);

                // Hover/Press feedback for tab buttons
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 20)); // Subtle overlay
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 0, 0, 10)); // Subtle overlay
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                }

                g2.setColor(isActive ? UIStyle.TEXT_PRIMARY : UIStyle.TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", isActive ? Font.BOLD : Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), iconX + iconSize + 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.BOLD, 13));
                int width = 15 + 20 + 10 + fm.stringWidth(getText()) + 15;
                return new Dimension(width, 42);
            }
        };
        btn.addActionListener(e -> switchTab(text));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                btn.repaint();
            }
        });

        return btn;
    }

    private JPanel createSystemConfigCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(2000, 420));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setMaximumSize(new Dimension(2000, 40));
        cardHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.SYSTEM_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("System Configuration");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(20));

        content.add(createToggleRow("Automatic Backup", "Daily backup of system data", true));
        content.add(createSeparator());
        content.add(createToggleRow("Low Stock Alerts", "Notify when products are low stocks", true));
        content.add(createSeparator());
        content.add(createToggleRow("Notifications", "Send notification alerts and reports", true));
        content.add(createSeparator());
        content.add(createToggleRow("Auto Receipt Printing", "Automatically print receipt after sale", true));
        content.add(createSeparator());
        content.add(createToggleRow("Audit Logging", "Log all user actions", false));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setMaximumSize(new Dimension(2000, 50));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn = createStyledButton("Save System Settings", UIStyle.BRAND_BROWN);
        btnPanel.add(saveBtn);
        content.add(Box.createVerticalStrut(10));
        content.add(btnPanel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createToggleRow(String title, String subtitle, boolean initialState) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(2000, 60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);
        JLabel subtitleLbl = new JLabel(subtitle);
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitleLbl.setForeground(UIStyle.TEXT_SECONDARY);
        textPanel.add(titleLbl);
        textPanel.add(subtitleLbl);
        ToggleSwitch toggle = new ToggleSwitch(initialState);
        row.add(textPanel, BorderLayout.WEST);
        row.add(toggle, BorderLayout.EAST);
        return row;
    }

    private static class ToggleSwitch extends JLabel {
        private boolean active;

        public ToggleSwitch(boolean initialState) {
            this.active = initialState;
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    active = !active;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            int size = 22; // Increased size
            if (active)
                BrandIcons.TOGGLE_ON.paint((Graphics2D) g, 0, (getHeight() - size) / 2, size, UIStyle.BRAND_BROWN);
            else
                BrandIcons.TOGGLE_OFF.paint((Graphics2D) g, 0, (getHeight() - size) / 2, size, null);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(50, 24);
        }
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(241, 245, 249));
        sep.setMaximumSize(new Dimension(2000, 1));
        return sep;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalBg = bg;
            private Color hoverBg = originalBg.brighter();
            private Color pressedBg = originalBg.darker();

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(originalBg);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                btn.setBackground(pressedBg);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (btn.getBounds().contains(e.getPoint())) {
                    btn.setBackground(hoverBg);
                } else {
                    btn.setBackground(originalBg);
                }
            }
        });

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), c.getHeight(), c.getHeight()); // Pill shape
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }
}
