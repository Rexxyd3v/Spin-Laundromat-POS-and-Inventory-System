package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.BrandIcons;
import src.util.UIStyle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingSecurityTab extends JPanel {

    public SettingSecurityTab() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIStyle.BACKGROUND);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add top spacing
        add(Box.createVerticalStrut(30));

        // Security Settings Card
        add(createSecuritySettingsCard());
        add(Box.createVerticalGlue());
    }

    private JPanel createSecuritySettingsCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
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
                BrandIcons.SECURITY_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Security Settings");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(20));

        // Fields in a row (Reflexive GridLayout)
        JPanel fieldsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        fieldsRow.setBackground(Color.WHITE);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        fieldsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldsRow.add(createFieldGroup("Session Timeout (minutes)", ""));
        fieldsRow.add(createFieldGroup("Max Login Attempts", ""));
        content.add(fieldsRow);
        content.add(Box.createVerticalStrut(20));

        // Save Button (Full Width)
        JButton saveBtn = createStyledButton("Save Security Settings", UIStyle.BRAND_BROWN);
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
}
