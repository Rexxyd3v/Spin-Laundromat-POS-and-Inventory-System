package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.BrandIcons;
import src.util.config.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingAlertsTab extends JPanel {

    public SettingAlertsTab() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIStyle.BACKGROUND);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(Box.createVerticalStrut(30));
        add(createMasterAlertCard());
        add(Box.createVerticalStrut(25));
        add(createAlertSettingsCard());
        add(Box.createVerticalStrut(25));
        add(createStockThresholdCard());
        add(Box.createVerticalGlue());
    }

    private JPanel createMasterAlertCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(241, 245, 249, 150));
        content.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Enable Stocks Alert");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitleLbl = new JLabel("Master switch for all stock alerts notifications");
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLbl.setForeground(UIStyle.TEXT_SECONDARY);

        textPanel.add(titleLbl);
        textPanel.add(subtitleLbl);

        ToggleSwitch toggle = new ToggleSwitch(true);

        content.add(textPanel, BorderLayout.WEST);
        content.add(toggle, BorderLayout.EAST);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAlertSettingsCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Sections
        content.add(createSectionHeader("Automated Report Settings"));
        content.add(createToggleRow("Enable Automated Reports",
                "Generate daily, weekly, monthly and yearly reports automatically", true));

        content.add(createSectionHeader("Sales Alerts"));
        content.add(createToggleRow("Daily Sales Summary", "End of day sales report", true));

        content.add(createSectionHeader("System Alerts"));
        content.add(createToggleRow("System Errors", "Critical system error notifications", true));

        content.add(Box.createVerticalStrut(20));

        // Save Button (Full Width)
        JButton saveBtn = createStyledButton("Save Alert Settings", UIStyle.BRAND_BROWN);
        saveBtn.setPreferredSize(new Dimension(220, 50));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(saveBtn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStockThresholdCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.ALERTS_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Stock Alert Configuration");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(5));

        JLabel subtitle = new JLabel("Stock Alert & Threshold & Colors");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(UIStyle.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(15));

        JPanel alertsRow = new JPanel(new GridLayout(0, 3, 15, 0));
        alertsRow.setBackground(Color.WHITE);
        alertsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        alertsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        alertsRow.add(createAlertThresholdSection("Low Stock Alert", "#FFADE9E"));
        alertsRow.add(createAlertThresholdSection("Critical Stock Alert", "#F2E632"));
        alertsRow.add(createAlertThresholdSection("Out of Stock Alert", "#D0806E"));
        content.add(alertsRow);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JLabel createSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(UIStyle.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(10, 0, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createToggleRow(String title, String subtitle, boolean initialState) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 245, 249), 1),
                new EmptyBorder(8, 15, 8, 15)));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
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

    private JPanel createAlertThresholdSection(String title, String colorCode) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLbl);
        section.add(Box.createVerticalStrut(8));

        JLabel thresholdLbl = new JLabel("Threshold");
        thresholdLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        thresholdLbl.setForeground(UIStyle.TEXT_SECONDARY);
        thresholdLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(thresholdLbl);
        section.add(Box.createVerticalStrut(4));

        JTextField thresholdField = new RoundedTextField("");
        thresholdField.setPreferredSize(new Dimension(0, 50));
        thresholdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        thresholdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(thresholdField);
        section.add(Box.createVerticalStrut(8));

        JLabel colorLbl = new JLabel("Alert Color");
        colorLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        colorLbl.setForeground(UIStyle.TEXT_SECONDARY);
        colorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(colorLbl);
        section.add(Box.createVerticalStrut(4));

        JPanel colorPanel = new JPanel(new BorderLayout(8, 0));
        colorPanel.setBackground(Color.WHITE);
        colorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(32, 32));
        colorBox.setBackground(Color.decode(colorCode));
        colorBox.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        colorBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JTextField colorField = new RoundedTextField(colorCode);
        colorField.setPreferredSize(new Dimension(100, 50));
        colorField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        colorField.setFocusable(false); // Make it read-only but clickable for the picker

        java.awt.event.MouseAdapter colorPickerListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Color initialColor = colorBox.getBackground();
                Color selectedColor = JColorChooser.showDialog(SettingAlertsTab.this, "Select Alert Color",
                        initialColor);
                if (selectedColor != null) {
                    colorBox.setBackground(selectedColor);
                    String hex = String.format("#%02X%02X%02X", selectedColor.getRed(), selectedColor.getGreen(),
                            selectedColor.getBlue());
                    colorField.setText(hex);
                }
            }
        };

        colorBox.addMouseListener(colorPickerListener);
        colorField.addMouseListener(colorPickerListener);

        colorPanel.add(colorBox, BorderLayout.WEST);
        colorPanel.add(colorField, BorderLayout.CENTER);
        section.add(colorPanel);

        return section;
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

    private static class RoundedTextField extends JTextField {
        public RoundedTextField(String text) {
            super(text);
            setOpaque(false);
            setBackground(new Color(241, 245, 249));
            setBorder(new EmptyBorder(0, 20, 0, 20));
            setFont(new Font("SansSerif", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Pill shape
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
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
}
