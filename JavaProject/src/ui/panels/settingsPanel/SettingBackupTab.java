package src.ui.panels.settingsPanel;

import src.util.components.ShadowPanel;
import src.util.components.BrandIcons;
import src.util.components.SidebarButton;
import src.util.config.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingBackupTab extends JPanel {

    public SettingBackupTab() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UIStyle.BACKGROUND);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(Box.createVerticalStrut(30));
        add(createDataBackupImportCard());
        add(Box.createVerticalStrut(25));

        add(createBackupScheduleCard());
        add(Box.createVerticalStrut(25));

        add(Box.createVerticalGlue());
    }

    private JPanel createDataBackupImportCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1200));
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
                BrandIcons.BACKUP_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Data Backup & Import");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(20));

        // Export Data section
        content.add(createExportImportGroup("Export Data",
                "Download a complete backup for your system data including products, transactions and user data.",
                "Export System Data", BrandIcons.DOWNLOAD));

        content.add(Box.createVerticalStrut(20));

        // Import Data section
        content.add(createExportImportGroup("Import Data",
                "Import data from a previous backup file. This will overwrite existing data.",
                "Import System Data", BrandIcons.UPLOAD));

        content.add(Box.createVerticalStrut(25));

        // Important Notes Alert Box
        content.add(createImportantNotesBox());

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBackupScheduleCard() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.BACKUP_IMG.paint((Graphics2D) g, 0, 0, 24, UIStyle.TEXT_PRIMARY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };
        JLabel cardTitle = new JLabel("Backup Schedule");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardHeader.add(iconLbl);
        cardHeader.add(cardTitle);
        content.add(cardHeader);
        content.add(Box.createVerticalStrut(20));

        content.add(createToggleRow("Automatic Daily Backup", "Backup run at every archive", true));
        content.add(createSeparator());
        content.add(createToggleRow("Weekly Full Backup", "Creates a full backup copy", true));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createExportImportGroup(String title, String subtitle, String btnText,
            SidebarButton.IconPainter icon) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(Color.WHITE);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLbl.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitleLbl = new JLabel(subtitle);
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLbl.setForeground(UIStyle.TEXT_SECONDARY);

        group.add(titleLbl);
        group.add(subtitleLbl);
        group.add(Box.createVerticalStrut(10));

        JButton btn = new JButton(btnText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Pill shape
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

                int iconSize = 18;
                int iconX = Math.max(20, getWidth() / 2 - 100);
                int iconY = (getHeight() - iconSize) / 2;
                icon.paint(g2, iconX, iconY, iconSize, UIStyle.TEXT_PRIMARY);

                g2.setColor(UIStyle.TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), iconX + iconSize + 15, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        group.add(btn);
        return group;
    }

    private JPanel createImportantNotesBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(254, 242, 242));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
                new EmptyBorder(15, 20, 15, 20)));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel iconLbl = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                BrandIcons.ALERTS_IMG.paint((Graphics2D) g, 0, 0, 16, new Color(220, 38, 38));
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }
        };

        JLabel title = new JLabel("Important Notes");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(new Color(220, 38, 38));

        header.add(iconLbl);
        header.add(title);
        box.add(header);
        box.add(Box.createVerticalStrut(10));

        String[] notes = {
                "Always backup your data before importing",
                "Import operation cannot be undone",
                "Large imports may take several minutes",
                "System will be locked during import process"
        };

        for (String note : notes) {
            JLabel lbl = new JLabel("• " + note);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(new Color(185, 28, 28));
            lbl.setBorder(new EmptyBorder(2, 5, 2, 0));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(lbl);
        }

        return box;
    }

    private JPanel createToggleRow(String title, String subtitle, boolean initialState) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(10, 0, 10, 0));
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
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
