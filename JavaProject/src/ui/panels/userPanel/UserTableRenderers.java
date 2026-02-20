package src.ui.panels.userPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import src.model.User;

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

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
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
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                nameLabel.setForeground(new Color(50, 50, 50));

                JLabel contactLabel = new JLabel(user.getContactNumber());
                contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                contactLabel.setForeground(new Color(120, 120, 120));

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

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (value instanceof User.UserRole) {
                User.UserRole role = (User.UserRole) value;

                JLabel iconLabel = new JLabel("👤");
                iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                panel.add(iconLabel);

                Color badgeColor = role == User.UserRole.ADMIN ? new Color(50, 50, 50) : new Color(100, 100, 100);

                JLabel badge = createBadge(role.getDisplayName(), badgeColor);
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

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            if (value instanceof String) {
                String status = (String) value;
                Color badgeColor = status.equals("Active") ? new Color(76, 175, 80) : new Color(158, 158, 158);

                JLabel badge = createBadge(status, badgeColor);
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

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JButton editButton = new JButton("✏️");
            editButton.setToolTipText("Edit User");
            editButton.setFocusPainted(false);
            editButton.setContentAreaFilled(false);
            editButton.setBorderPainted(false);
            editButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JButton viewButton = new JButton("👤");
            viewButton.setToolTipText("View Details");
            viewButton.setFocusPainted(false);
            viewButton.setContentAreaFilled(false);
            viewButton.setBorderPainted(false);
            viewButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            panel.add(editButton);
            panel.add(viewButton);

            return panel;
        }
    }

    /**
     * Helper method to create avatar label
     */
    private static JLabel createAvatarLabel(String initials) {
        JLabel label = new JLabel(initials, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(100, 100, 100));
        label.setPreferredSize(new Dimension(45, 45));
        label.setOpaque(true);
        label.setBackground(new Color(230, 230, 230));
        return label;
    }

    /**
     * Helper method to create badge
     */
    private static JLabel createBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return badge;
    }
}
