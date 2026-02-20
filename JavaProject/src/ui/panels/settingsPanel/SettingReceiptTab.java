package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.BrandIcons;
import src.util.UIStyle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingReceiptTab extends JPanel {

    public SettingReceiptTab() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIStyle.BACKGROUND);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(Box.createVerticalStrut(30));

        add(createReceiptConfigCard());
        add(Box.createVerticalGlue());
    }

    private JPanel createReceiptConfigCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Header
        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.RECEIPT_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Receipt Configuration");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(20));

        // Header Text
        content.add(createFieldGroup("Header Text", ""));
        content.add(Box.createVerticalStrut(12));

        // Footer Text
        content.add(createFieldGroup("Footer Text", ""));
        content.add(Box.createVerticalStrut(15));

        // Toggles
        content.add(createToggleRow("Show Cashier Name", null, true));
        content.add(createToggleRow("Show Store Logo", null, true));
        content.add(Box.createVerticalStrut(20));

        // Save Button (Full Width)
        JButton saveBtn = createStyledButton("Save Receipt Settings", UIStyle.BRAND_BROWN);
        saveBtn.setPreferredSize(new Dimension(220, 50));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(saveBtn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFieldGroup(String labelText, String fieldText) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(Color.WHITE);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(UIStyle.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new RoundedTextField(fieldText);
        field.setPreferredSize(new Dimension(0, 50));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        group.add(lbl);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        return group;
    }

    private JPanel createToggleRow(String title, String subtitle, boolean initialState) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);
        textPanel.add(titleLbl);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLbl = new JLabel(subtitle);
            subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            subtitleLbl.setForeground(UIStyle.TEXT_SECONDARY);
            textPanel.add(subtitleLbl);
        }

        ToggleSwitch toggle = new ToggleSwitch(initialState);
        row.add(textPanel, BorderLayout.WEST);
        row.add(toggle, BorderLayout.EAST);
        return row;
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
}
