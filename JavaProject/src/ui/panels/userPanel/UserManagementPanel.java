package src.ui.panels.userPanel;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.model.User;

/**
 * User Management Panel integrated into main UI
 */
public class UserManagementPanel extends JPanel {
    private UserTableModel tableModel;
    private JTable userTable;
    private JPanel statsPanel;
    private UserManagementPanel.StatCard totalAccountsCard;
    private UserManagementPanel.StatCard activeAccountsCard;
    private UserManagementPanel.StatCard administratorsCard;
    private UserManagementPanel.StatCard staffsCard;

    // Colors matching the main UI theme
    private static final Color MAIN_BG = new Color(224, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    private static final Color BUTTON_COLOR = new Color(50, 50, 50);

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // Initialize with sample data
        initializeSampleData();

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(MAIN_BG);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Add sections
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(MAIN_BG);
        contentPanel.add(createStatsPanel(), BorderLayout.NORTH);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createPermissionsPanel(), BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void initializeSampleData() {
        List<User> users = new ArrayList<>();

        users.add(new User("1", "Jefferson Padua", "09123456891",
                User.UserRole.ADMIN, true,
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20)));

        users.add(new User("2", "Lester M. Estranja", "09123456891",
                User.UserRole.STAFF, true,
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20)));

        users.add(new User("3", "Krizza Dela Cruz", "09123456891",
                User.UserRole.STAFF, true,
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20)));

        users.add(new User("4", "Khinito Perlamarte", "09123456891",
                User.UserRole.STAFF, true,
                LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20)));

        tableModel = new UserTableModel(users);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG);

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(MAIN_BG);

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage user accounts and permissions");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        // Add User button
        JButton addButton = createRoundedButton("+ Add User", BUTTON_COLOR);
        addButton.addActionListener(e -> showAddUserDialog());

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(addButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(MAIN_BG);

        totalAccountsCard = new UserManagementPanel.StatCard("Total Accounts",
                String.valueOf(tableModel.getRowCount()), "👥");
        activeAccountsCard = new UserManagementPanel.StatCard("Active Accounts",
                String.valueOf(tableModel.getActiveCount()), "✅");
        administratorsCard = new UserManagementPanel.StatCard("Administrators",
                String.valueOf(tableModel.getAdminCount()), "🛡️");
        staffsCard = new UserManagementPanel.StatCard("Staffs",
                String.valueOf(tableModel.getStaffCount()), "👤");

        statsPanel.add(totalAccountsCard);
        statsPanel.add(activeAccountsCard);
        statsPanel.add(administratorsCard);
        statsPanel.add(staffsCard);

        return statsPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)));

        // Search and filter
        JPanel searchPanel = new JPanel(new BorderLayout(15, 0));
        searchPanel.setBackground(CARD_BG);

        JTextField searchField = new JTextField("Search users...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(300, 40));

        JComboBox<String> roleFilter = new JComboBox<>(
                new String[] { "All Roles", "Admin", "Staff" });
        roleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleFilter.setPreferredSize(new Dimension(150, 40));

        searchPanel.add(searchField, BorderLayout.WEST);
        searchPanel.add(roleFilter, BorderLayout.EAST);

        // Table header
        JLabel tableHeader = new JLabel("Accounts (" + tableModel.getRowCount() + ")");
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableHeader.setForeground(TEXT_PRIMARY);
        tableHeader.setBorder(new EmptyBorder(20, 0, 10, 0));

        // Create table
        userTable = new JTable(tableModel);
        userTable.setRowHeight(65);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        userTable.setBackground(CARD_BG);
        userTable.setSelectionBackground(new Color(240, 248, 255));
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        userTable.getTableHeader().setForeground(TEXT_SECONDARY);
        userTable.getTableHeader().setBackground(CARD_BG);
        userTable.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        // Set custom renderers
        userTable.getColumnModel().getColumn(0).setCellRenderer(
                new UserTableRenderers.UserCellRenderer());
        userTable.getColumnModel().getColumn(1).setCellRenderer(
                new UserTableRenderers.RoleCellRenderer());
        userTable.getColumnModel().getColumn(2).setCellRenderer(
                new UserTableRenderers.StatusCellRenderer());
        userTable.getColumnModel().getColumn(3).setCellRenderer(
                new UserTableRenderers.DateCellRenderer());
        userTable.getColumnModel().getColumn(4).setCellRenderer(
                new UserTableRenderers.DateCellRenderer());
        userTable.getColumnModel().getColumn(5).setCellRenderer(
                new UserTableRenderers.ActionCellRenderer());

        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(searchPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(CARD_BG);
        centerPanel.add(tableHeader, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPermissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel header = new JLabel("Role Permissions");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(TEXT_PRIMARY);

        JPanel rolesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        rolesPanel.setBackground(CARD_BG);

        rolesPanel.add(createRoleCard("Administrator", "🛡️", new String[] {
                "Full system access",
                "User management",
                "System Settings",
                "All reports"
        }));

        rolesPanel.add(createRoleCard("Staff", "👤", new String[] {
                "POS operations",
                "View inventory",
                "Basic Dashboard",
                "Inventory Management"
        }));

        panel.add(header, BorderLayout.NORTH);
        panel.add(rolesPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRoleCard(String roleName, String icon, String[] permissions) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(CARD_BG);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel nameLabel = new JLabel(roleName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        headerPanel.add(iconLabel);
        headerPanel.add(nameLabel);

        card.add(headerPanel);
        card.add(Box.createVerticalStrut(10));

        for (String permission : permissions) {
            JLabel permLabel = new JLabel("• " + permission);
            permLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            permLabel.setForeground(new Color(70, 70, 70));
            permLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(permLabel);
            card.add(Box.createVerticalStrut(5));
        }

        return card;
    }

    // Helper methods
    private JButton createRoundedButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Public methods for external interaction
    public void addUser(User user) {
        tableModel.addUser(user);
        refreshStats();
    }

    public void removeUser(int index) {
        tableModel.removeUser(index);
        refreshStats();
    }

    public User getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getUserAt(selectedRow);
        }
        return null;
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        JTextField userIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[] { "Admin", "Staff" });
        JCheckBox activeCheckbox = new JCheckBox("Active", true);

        formPanel.add(createFormLabel("User ID:"));
        userIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(userIdField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(createFormLabel("Name:"));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(createFormLabel("Contact Number:"));
        contactField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(contactField);
        formPanel.add(Box.createVerticalStrut(10));

        formPanel.add(createFormLabel("Role:"));
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(roleCombo);
        formPanel.add(Box.createVerticalStrut(10));

        activeCheckbox.setBackground(Color.WHITE);
        activeCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(activeCheckbox);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();

            if (userId.isEmpty() || name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            User.UserRole role = ((String) roleCombo.getSelectedItem()).equals("Admin") ? User.UserRole.ADMIN
                    : User.UserRole.STAFF;
            User newUser = new User(userId, name, contact, role, activeCheckbox.isSelected(),
                    LocalDate.now(), LocalDate.now());
            addUser(newUser);

            JOptionPane.showMessageDialog(dialog, "User " + name + " has been created successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void refreshStats() {
        statsPanel.removeAll();

        totalAccountsCard = new UserManagementPanel.StatCard("Total Accounts",
                String.valueOf(tableModel.getRowCount()), "👥");
        activeAccountsCard = new UserManagementPanel.StatCard("Active Accounts",
                String.valueOf(tableModel.getActiveCount()), "✅");
        administratorsCard = new UserManagementPanel.StatCard("Administrators",
                String.valueOf(tableModel.getAdminCount()), "🛡️");
        staffsCard = new UserManagementPanel.StatCard("Staffs",
                String.valueOf(tableModel.getStaffCount()), "👤");

        statsPanel.add(totalAccountsCard);
        statsPanel.add(activeAccountsCard);
        statsPanel.add(administratorsCard);
        statsPanel.add(staffsCard);

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    // Inner class for Stat Card
    public class StatCard extends JPanel {
        public StatCard(String title, String value, String icon) {
            setLayout(new BorderLayout(10, 5));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(15, 20, 15, 20)));
            setPreferredSize(new Dimension(250, 90));

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            topPanel.setBackground(CARD_BG);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            titleLabel.setForeground(new Color(130, 130, 130));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

            topPanel.add(titleLabel);
            topPanel.add(iconLabel);

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            valueLabel.setForeground(TEXT_PRIMARY);

            add(topPanel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
        }
    }
}
