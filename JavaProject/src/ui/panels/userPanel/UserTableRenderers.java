package src.ui.panels.userPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import src.model.User;
import src.util.config.UIStyle;

/**
 * Custom cell renderers for the user table
 */
public class UserTableRenderers {

    /**
     * Renderer for user column with avatar
     */
    public static class UserCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (value instanceof User) {
                User user = (User) value;

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

                // Avatar
                JLabel avatar = createAvatarLabel(user.getInitials());
                panel.add(avatar);

                // Name and contact
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setOpaque(false);

                JLabel nameLabel = new JLabel(user.getName());
                nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                nameLabel.setForeground(UIStyle.TEXT_PRIMARY);

                JLabel contactLabel = new JLabel(user.getContactNumber());
                contactLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                contactLabel.setForeground(UIStyle.TEXT_SECONDARY);

                infoPanel.add(nameLabel);
                infoPanel.add(Box.createVerticalStrut(2));
                infoPanel.add(contactLabel);

                panel.add(infoPanel);
                return panel;
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    /**
     * Renderer for role column
     */
    public static class RoleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (value instanceof User.UserRole) {
                User.UserRole role = (User.UserRole) value;
                String roleStr = role.getDisplayName();
                Color badgeBg = role == User.UserRole.ADMIN ? new Color(31, 41, 55) : new Color(224, 242, 254);
                Color badgeFg = role == User.UserRole.ADMIN ? Color.WHITE : new Color(3, 105, 161);

                JLabel badge = createIconBadge(roleStr, role == User.UserRole.ADMIN ? "ADMIN" : "STAFF", badgeBg,
                        badgeFg);
                panel.add(badge);
            }

            return panel;
        }
    }

    /**
     * Renderer for status column
     */
    public static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (value instanceof String) {
                String status = (String) value;
                Color badgeBg = new Color(55, 65, 81); // Dark badge for status as per screenshot
                Color badgeFg = Color.WHITE;

                JLabel badge = createBadge(status, badgeBg, badgeFg);
                panel.add(badge);
            }

            return panel;
        }
    }

    /**
     * Renderer for date columns
     */
    public static class DateCellRenderer extends DefaultTableCellRenderer {
        public DateCellRenderer() {
            setHorizontalAlignment(LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(70, 70, 70));

            if (!isSelected) {
                setBackground(Color.WHITE);
            }

            return c;
        }
    }

    /**
     * Renderer for action buttons
     */
    public static class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel editLabel = new JLabel();
            boolean imgLoaded = false;
            try {
                String imgPath = "/src/resources/images/edit.png";
                java.net.URL imgUrl = getClass().getResource(imgPath);
                if (imgUrl != null) {
                    ImageIcon editIcon = new ImageIcon(imgUrl);
                    // Increased size to 28x28 for better visibility
                    Image img = editIcon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                    editLabel.setIcon(new ImageIcon(img));
                    imgLoaded = true;
                }
            } catch (Exception e) {
                imgLoaded = false;
            }

            if (!imgLoaded) {
                // Fallback to custom drawing if image fails
                editLabel = new JLabel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(245, 158, 11));
                        int w = getWidth();
                        int h = getHeight();
                        g2.fillRoundRect(w / 4, h / 4, w / 2, h / 2, 4, 4);
                        g2.dispose();
                    }
                };
            }
            editLabel.setPreferredSize(new Dimension(35, 35));

            panel.add(editLabel);
            return panel;
        }
    }

    /**
     * Helper method to create avatar label
     */
    private static JLabel createAvatarLabel(String initials) {
        JLabel label = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(new Color(75, 85, 99));
        label.setPreferredSize(new Dimension(45, 45));
        label.setOpaque(false);
        label.setBackground(new Color(229, 231, 235));
        return label;
    }

    private static JLabel createIconBadge(String text, String type, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel("    " + text) { // Space for icon
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                // Draw icon
                g2.setColor(getForeground());
                int h = getHeight();
                if (type.equals("ADMIN")) {
                    g2.drawRoundRect(10, h / 3, 10, 8, 2, 2);
                } else { // STAFF
                    g2.fillOval(10, h / 4, 6, 6);
                    g2.fillArc(8, h / 2, 10, 6, 0, 180);
                }

                super.paintComponent(g2);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBackground(bgColor);
        badge.setForeground(fgColor);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 25, 4, 15));
        return badge;
    }

    private static JLabel createBadge(String text, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBackground(bgColor);
        badge.setForeground(fgColor);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }
}
