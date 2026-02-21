package src.ui.panels.userPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import src.model.User;
import src.util.config.UIStyle;

/**
 * Custom cell renderers for the user table.
 * All renderers are JLabel-based (not JPanel) so columns
 * align exactly with their headers.
 */
public class UserTableRenderers {

    /**
     * Left padding applied to every cell — must match the header renderer border
     */
    private static final int PAD = 12;

    private static Color rowBg(JTable t, boolean selected, int row) {
        if (selected)
            return new Color(239, 246, 255);
        return row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User column — avatar circle + name + contact (uses a JPanel for layout)
    // This is the only renderer that NEEDS a JPanel because it has two sub-rows.
    // Alignment is kept tight via BorderLayout, not FlowLayout.
    // ─────────────────────────────────────────────────────────────────────────
    public static class UserCellRenderer extends DefaultTableCellRenderer {

        private final JPanel shell = new JPanel(new BorderLayout(10, 0));
        private final JLabel avatar = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // always draw a perfect circle using min(width, height)
                int d = Math.min(getWidth(), getHeight());
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;
                g2.setColor(getBackground());
                g2.fillOval(x, y, d - 1, d - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        private final JLabel nameL = new JLabel();
        private final JLabel contactL = new JLabel();
        private final JPanel textBox = new JPanel(new GridLayout(2, 1, 0, 1));

        public UserCellRenderer() {
            // Avatar — fixed-size circle
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            avatar.setForeground(new Color(75, 85, 99));
            avatar.setPreferredSize(new Dimension(40, 40));
            avatar.setMinimumSize(new Dimension(40, 40));
            avatar.setMaximumSize(new Dimension(40, 40));
            avatar.setOpaque(false);

            nameL.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameL.setForeground(UIStyle.TEXT_PRIMARY);

            contactL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            contactL.setForeground(UIStyle.TEXT_SECONDARY);

            textBox.setOpaque(false);
            textBox.add(nameL);
            textBox.add(contactL);

            shell.setBorder(new EmptyBorder(0, PAD, 0, PAD));
            shell.add(avatar, BorderLayout.WEST);
            shell.add(textBox, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Color bg = rowBg(table, isSelected, row);
            shell.setOpaque(true);
            shell.setBackground(bg);
            textBox.setBackground(bg);

            if (value instanceof User) {
                User u = (User) value;
                avatar.setText(u.getInitials());
                avatar.setBackground(new Color(229, 231, 235));
                nameL.setText(u.getName());
                contactL.setText(u.getContactNumber());
            }
            return shell;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Role column — badge painted directly on a JLabel
    // ─────────────────────────────────────────────────────────────────────────
    public static class RoleCellRenderer extends DefaultTableCellRenderer {

        /** Inner JLabel that paints its own pill background */
        private final BadgeLabel badge = new BadgeLabel();

        /** Wrapper cell so we can left-pad and centre the badge vertically */
        private final JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            {
                setOpaque(true);
            }
        };

        public RoleCellRenderer() {
            cell.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Color bg = rowBg(table, isSelected, row);
            cell.setBackground(bg);

            // Vertically center badge inside the row
            int vGap = Math.max(0, (table.getRowHeight(row) - badge.getPreferredSize().height) / 2);
            ((FlowLayout) cell.getLayout()).setVgap(vGap);

            if (value instanceof User.UserRole) {
                User.UserRole role = (User.UserRole) value;
                boolean isAdmin = role == User.UserRole.ADMIN;
                badge.setup(
                        role.getDisplayName(),
                        isAdmin ? "ADMIN" : "STAFF",
                        isAdmin ? new Color(31, 41, 55) : new Color(224, 242, 254),
                        isAdmin ? Color.WHITE : new Color(3, 105, 161));
            }
            return cell;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status column — simple coloured pill
    // ─────────────────────────────────────────────────────────────────────────
    public static class StatusCellRenderer extends DefaultTableCellRenderer {

        private final PillLabel pill = new PillLabel();
        private final JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            {
                setOpaque(true);
            }
        };

        public StatusCellRenderer() {
            cell.add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Color bg = rowBg(table, isSelected, row);
            cell.setBackground(bg);

            // Vertically center pill inside the row
            int vGap = Math.max(0, (table.getRowHeight(row) - pill.getPreferredSize().height) / 2);
            ((FlowLayout) cell.getLayout()).setVgap(vGap);

            if (value instanceof String) {
                String status = (String) value;
                boolean active = status.equalsIgnoreCase("Active");
                pill.setup(
                        status,
                        active ? new Color(34, 197, 94) : new Color(239, 68, 68),
                        Color.WHITE);
            }
            return cell;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date columns
    // ─────────────────────────────────────────────────────────────────────────
    public static class DateCellRenderer extends DefaultTableCellRenderer {

        public DateCellRenderer() {
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setForeground(new Color(55, 65, 81));
            setBackground(rowBg(table, isSelected, row));
            setOpaque(true);
            return this;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Actions column
    // ─────────────────────────────────────────────────────────────────────────
    public static class ActionCellRenderer extends DefaultTableCellRenderer {

        private final JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            {
                setOpaque(true);
            }
        };
        private final JLabel icon = new JLabel();

        private ImageIcon editIcon;

        public ActionCellRenderer() {
            try {
                java.net.URL url = getClass().getResource("/src/resources/images/edit.png");
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    editIcon = new ImageIcon(img);
                }
            } catch (Exception ignored) {
            }

            if (editIcon == null) {
                // Canvas fallback
                icon.setPreferredSize(new Dimension(32, 32));
            } else {
                icon.setIcon(editIcon);
            }
            cell.add(icon);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Color bg = rowBg(table, isSelected, row);
            cell.setBackground(bg);

            int height = table.getRowHeight(row);
            int vGap = Math.max(0, (height - 32) / 2);
            ((FlowLayout) cell.getLayout()).setVgap(vGap);

            return cell;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private helper labels that paint their own backgrounds
    // ═════════════════════════════════════════════════════════════════════════

    /** Badge with icon (Admin / Staff) */
    private static class BadgeLabel extends JLabel {
        private String iconType = "STAFF";

        BadgeLabel() {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(new EmptyBorder(5, 26, 5, 14));
        }

        void setup(String text, String type, Color bg, Color fg) {
            this.iconType = type;
            setText("    " + text);
            setBackground(bg);
            setForeground(fg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(getForeground());
            int h = getHeight();
            if ("ADMIN".equals(iconType)) {
                g2.drawRoundRect(10, h / 3, 10, 8, 2, 2);
            } else {
                g2.fillOval(10, h / 4, 6, 6);
                g2.fillArc(8, h / 2, 10, 6, 0, 180);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Simple coloured pill */
    private static class PillLabel extends JLabel {

        PillLabel() {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(5, 14, 5, 14));
        }

        void setup(String text, Color bg, Color fg) {
            setText(text);
            setBackground(bg);
            setForeground(fg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
