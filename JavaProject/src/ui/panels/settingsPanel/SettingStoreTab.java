package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.BrandIcons;
import src.util.config.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingStoreTab extends JPanel {

    public SettingStoreTab() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIStyle.BACKGROUND);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add top spacing
        add(Box.createVerticalStrut(30));

        // Store Information Card
        add(createStoreInfoCard());
        add(Box.createVerticalGlue());
    }

    private JPanel createStoreInfoCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Header
        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.STORE_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Store Information");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        content.add(cardHeader, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        gbc.gridy = 1;
        content.add(createFieldGroup("Store Name", "Spin Laundromat Service"), gbc);
        gbc.gridx = 1;
        content.add(createFieldGroup("Phone Number", "09123456789"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        content.add(createFieldGroup("Address", "123 Street, City, Philippines"), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        content.add(createFieldGroup("Email", "admin@spinlaundry.com"), gbc);
        gbc.gridx = 1;
        content.add(createFieldGroup("TIN Number", "000-000-000-000"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;

        JPanel currencyGroup = new JPanel();
        currencyGroup.setLayout(new BoxLayout(currencyGroup, BoxLayout.Y_AXIS));
        currencyGroup.setBackground(Color.WHITE);
        currencyGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] currencies = { "PHP (₱)", "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)" };
        RoundedComboBox currencyDropdown = new RoundedComboBox(currencies);
        currencyDropdown.setPreferredSize(new Dimension(0, 50));
        currencyDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        currencyGroup.add(currencyDropdown);
        content.add(currencyGroup, gbc);

        // Save Button (Right Aligned in a panel)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton saveBtn = createStyledButton("Save Store Settings", UIStyle.BRAND_BROWN);
        btnPanel.add(saveBtn);

        gbc.gridx = 1;
        gbc.gridy = 5;
        content.add(btnPanel, gbc);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFieldGroup(String labelText, String fieldText) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(Color.WHITE);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new RoundedTextField(labelText);
        field.setPreferredSize(new Dimension(0, 50));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        group.add(field);
        return group;
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
        btn.setMaximumSize(new Dimension(220, 50));
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

    private static class RoundedComboBox extends JComboBox<String> {
        public RoundedComboBox(String[] items) {
            super(items);
            setOpaque(false);
            setBackground(new Color(241, 245, 249));
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton() {
                        @Override
                        public Dimension getPreferredSize() {
                            return new Dimension(0, 0);
                        }
                    };
                    btn.setVisible(false);
                    return btn;
                }
            });
            setBorder(new EmptyBorder(0, 20, 0, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            // Border
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

            g2.dispose();
            super.paintComponent(g);

            // Custom arrow icon
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setColor(UIStyle.TEXT_SECONDARY);
            int size = 10;
            int x = getWidth() - 30;
            int y = (getHeight() - size / 2) / 2;
            int[] px = { x, x + size, x + size / 2 };
            int[] py = { y, y, y + size / 2 };
            g3.fillPolygon(px, py, 3);
            g3.dispose();
        }
    }

    private static class RoundedTextField extends JTextField {
        private String placeholder;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBackground(new Color(241, 245, 249));
            setBorder(new EmptyBorder(0, 20, 0, 20));
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setForeground(UIStyle.TEXT_PRIMARY);

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

            // Border
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

            g2.dispose();
            super.paintComponent(g);

            // Placeholder
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(UIStyle.TEXT_SECONDARY);
                g3.setFont(getFont());
                FontMetrics fm = g3.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(placeholder, 20, y);
                g3.dispose();
            }
        }
    }
}
