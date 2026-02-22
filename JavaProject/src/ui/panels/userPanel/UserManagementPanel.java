package src.ui.panels.userPanel;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import src.model.User;
import src.util.config.UIStyle;
import src.util.components.ShadowPanel;
import src.util.components.Scrollbar;

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

        public UserManagementPanel() {
                setLayout(new BorderLayout());
                setBackground(UIStyle.BACKGROUND);

                // Initialize with sample data
                initializeSampleData();

                // Main content panel
                JPanel mainPanel = new JPanel(new BorderLayout(0, 30));
                mainPanel.setOpaque(false);
                mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

                // Add sections
                mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

                JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
                contentPanel.setBackground(UIStyle.BACKGROUND);
                contentPanel.add(createStatsPanel(), BorderLayout.NORTH);
                contentPanel.add(createTablePanel(), BorderLayout.CENTER);
                contentPanel.add(createPermissionsPanel(), BorderLayout.SOUTH);

                mainPanel.add(contentPanel, BorderLayout.CENTER);

                JScrollPane mainScroll = new JScrollPane(mainPanel);
                mainScroll.setBorder(null);
                mainScroll.setOpaque(false);
                mainScroll.getViewport().setOpaque(false); // Ensure viewport is transparent
                mainScroll.setBackground(UIStyle.BACKGROUND);
                mainScroll.getVerticalScrollBar().setUnitIncrement(16);
                mainScroll.getVerticalScrollBar().setUI(new Scrollbar());
                mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                add(mainScroll, BorderLayout.CENTER);
        }

        private void initializeSampleData() {
                List<User> users = new ArrayList<>();

                users.add(new User("101", "Jefferson Padua", "0912-345-6789",
                                User.UserRole.ADMIN, true,
                                LocalDate.of(2025, 12, 10), LocalDate.of(2026, 2, 21)));

                users.add(new User("102", "Lester M. Estranja", "0917-555-0123",
                                User.UserRole.STAFF, true,
                                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 20)));

                users.add(new User("103", "Krizza Dela Cruz", "0920-888-4321",
                                User.UserRole.STAFF, true,
                                LocalDate.of(2026, 1, 20), LocalDate.of(2026, 2, 19)));

                users.add(new User("104", "Khinito Perlamarte", "0915-999-7777",
                                User.UserRole.STAFF, false,
                                LocalDate.of(2026, 2, 01), LocalDate.of(2026, 2, 05)));

                users.add(new User("105", "Maria Santos", "0918-111-2222",
                                User.UserRole.ADMIN, true,
                                LocalDate.of(2025, 11, 20), LocalDate.of(2026, 2, 21)));

                users.add(new User("106", "Juan Dela Cruz", "0922-333-4444",
                                User.UserRole.STAFF, true,
                                LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 21)));

                tableModel = new UserTableModel(users);
        }

        private JPanel createHeaderPanel() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(false);

                // Title section
                JPanel titlePanel = new JPanel();
                titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
                titlePanel.setOpaque(false);

                JLabel titleLabel = new JLabel("User Management");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
                titleLabel.setForeground(UIStyle.TEXT_PRIMARY);

                JLabel subtitleLabel = new JLabel("Management user accounts and permissions");
                subtitleLabel.setFont(UIStyle.REGULAR);
                subtitleLabel.setForeground(UIStyle.TEXT_SECONDARY);

                titlePanel.add(titleLabel);
                titlePanel.add(Box.createVerticalStrut(8));
                titlePanel.add(subtitleLabel);

                // Add User button
                JButton addButton = new JButton("+ Add User") {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(new Color(30, 41, 59)); // Dark slate
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                                super.paintComponent(g2);
                                g2.dispose();
                        }
                };
                addButton.setFont(new Font("SansSerif", Font.BOLD, 12));
                addButton.setForeground(Color.WHITE);
                addButton.setPreferredSize(new Dimension(110, 34));
                addButton.setMaximumSize(new Dimension(110, 34));
                addButton.setFocusPainted(false);
                addButton.setBorderPainted(false);
                addButton.setContentAreaFilled(false);
                addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                addButton.addActionListener(e -> showAddUserDialog());

                // Wrap in panel so BorderLayout.EAST doesn't stretch the height
                JPanel btnWrapper = new JPanel();
                btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.Y_AXIS));
                btnWrapper.setOpaque(false);
                btnWrapper.add(Box.createVerticalGlue());
                btnWrapper.add(addButton);
                btnWrapper.add(Box.createVerticalGlue());

                panel.add(titlePanel, BorderLayout.WEST);
                panel.add(btnWrapper, BorderLayout.EAST);

                return panel;
        }

        private JPanel createStatsPanel() {
                statsPanel = new JPanel(new GridLayout(1, 4, 30, 0));
                statsPanel.setOpaque(false);
                statsPanel.setPreferredSize(new Dimension(0, 140)); // Only height is fixed, width adapts

                totalAccountsCard = new UserManagementPanel.StatCard("Total Accounts",
                                String.valueOf(tableModel.getRowCount()), "TOTAL", new Color(241, 245, 249),
                                new Color(100, 116, 139));
                activeAccountsCard = new UserManagementPanel.StatCard("Active Accounts",
                                String.valueOf(tableModel.getActiveCount()), "ACTIVE", new Color(220, 252, 231),
                                new Color(34, 197, 94));
                administratorsCard = new UserManagementPanel.StatCard("Administrators",
                                String.valueOf(tableModel.getAdminCount()), "ADMIN", new Color(254, 226, 226),
                                new Color(239, 68, 68));
                staffsCard = new UserManagementPanel.StatCard("Staffs",
                                String.valueOf(tableModel.getStaffCount()), "STAFF", new Color(224, 242, 254),
                                UIStyle.PRIMARY);

                statsPanel.add(totalAccountsCard);
                statsPanel.add(activeAccountsCard);
                statsPanel.add(administratorsCard);
                statsPanel.add(staffsCard);

                return statsPanel;
        }

        private JPanel createTablePanel() {
                // ── Outer wrapper: holds search bar + table card stacked vertically ──
                JPanel wrapper = new JPanel(new BorderLayout(0, 14));
                wrapper.setOpaque(false);

                // ── Search / Filter Bar (standalone, outside the card) ──────────────
                JPanel searchRow = new JPanel(new BorderLayout(12, 0));
                searchRow.setOpaque(false);

                // Search pill
                JPanel searchPill = new JPanel(new BorderLayout(10, 0)) {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(Color.WHITE);
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                                g2.setColor(new Color(226, 232, 240));
                                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);
                                g2.dispose();
                        }
                };
                searchPill.setOpaque(false);
                searchPill.setBackground(Color.WHITE);
                searchPill.setBorder(new EmptyBorder(9, 20, 9, 20));

                // Search icon
                JLabel searchIcon = new JLabel();
                try {
                        java.net.URL imgUrl = getClass().getResource("/src/resources/images/search.png");
                        if (imgUrl != null) {
                                ImageIcon original = new ImageIcon(imgUrl);
                                Image scaled = original.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                                searchIcon.setIcon(new ImageIcon(scaled));
                        } else {
                                searchIcon.setText("\uD83D\uDD0D");
                                searchIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
                                searchIcon.setForeground(new Color(148, 163, 184));
                        }
                } catch (Exception e) {
                        searchIcon.setText("\uD83D\uDD0D");
                }

                JTextField searchField = new JTextField();
                searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                searchField.setForeground(UIStyle.TEXT_SECONDARY);
                searchField.setBackground(new Color(0, 0, 0, 0));
                searchField.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
                searchField.setOpaque(false);
                searchField.setToolTipText("Search users...");

                // Placeholder behaviour
                searchField.setText("Search users...");
                searchField.addFocusListener(new java.awt.event.FocusAdapter() {
                        @Override
                        public void focusGained(java.awt.event.FocusEvent e) {
                                if (searchField.getText().equals("Search users...")) {
                                        searchField.setText("");
                                        searchField.setForeground(UIStyle.TEXT_PRIMARY);
                                }
                        }

                        @Override
                        public void focusLost(java.awt.event.FocusEvent e) {
                                if (searchField.getText().isEmpty()) {
                                        searchField.setText("Search users...");
                                        searchField.setForeground(UIStyle.TEXT_SECONDARY);
                                }
                        }
                });

                searchPill.add(searchIcon, BorderLayout.WEST);
                searchPill.add(searchField, BorderLayout.CENTER);

                // Role filter pill
                JPanel filterPill = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(Color.WHITE);
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                                g2.setColor(new Color(226, 232, 240));
                                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);
                                g2.dispose();
                        }
                };
                filterPill.setOpaque(false);
                filterPill.setBackground(Color.WHITE);
                filterPill.setBorder(new EmptyBorder(6, 16, 6, 16));

                JComboBox<String> roleFilter = new JComboBox<>(new String[] { "All Roles", "Admin", "Staff" });
                roleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                roleFilter.setForeground(UIStyle.TEXT_PRIMARY);
                roleFilter.setBackground(Color.WHITE);
                roleFilter.setBorder(null);
                roleFilter.setOpaque(false);
                roleFilter.setPreferredSize(new Dimension(130, 30));
                roleFilter.setFocusable(false);
                // Custom UI: remove box border, invisible arrow button with a simple ▾ drawn
                roleFilter.setUI(new BasicComboBoxUI() {
                        @Override
                        protected JButton createArrowButton() {
                                JButton btn = new BasicArrowButton(BasicArrowButton.SOUTH,
                                                Color.WHITE, Color.WHITE,
                                                new Color(100, 116, 139), Color.WHITE);
                                btn.setBorder(BorderFactory.createEmptyBorder());
                                btn.setBackground(Color.WHITE);
                                btn.setOpaque(false);
                                return btn;
                        }

                        @Override
                        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                                // No background — pill paints it
                        }
                });

                filterPill.add(roleFilter, BorderLayout.CENTER);

                searchRow.add(searchPill, BorderLayout.CENTER);
                searchRow.add(filterPill, BorderLayout.EAST);

                wrapper.add(searchRow, BorderLayout.NORTH);

                // ── Table Card ───────────────────────────────────────────────────────
                JPanel card = new JPanel(new BorderLayout(0, 0));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                                new EmptyBorder(24, 24, 24, 24)));

                // Card top bar: title + count badge
                JPanel cardTopBar = new JPanel(new BorderLayout());
                cardTopBar.setOpaque(false);
                cardTopBar.setBorder(new EmptyBorder(0, 0, 16, 0));

                JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                titleGroup.setOpaque(false);

                JLabel tableHeader = new JLabel("Accounts");
                tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 17));
                tableHeader.setForeground(UIStyle.TEXT_PRIMARY);

                // Count badge
                JLabel countBadge = new JLabel(String.valueOf(tableModel.getRowCount())) {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(new Color(241, 245, 249));
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                                g2.dispose();
                                super.paintComponent(g);
                        }
                };
                countBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
                countBadge.setForeground(new Color(100, 116, 139));
                countBadge.setBorder(new EmptyBorder(2, 10, 2, 10));
                countBadge.setOpaque(false);

                titleGroup.add(tableHeader);
                titleGroup.add(countBadge);
                cardTopBar.add(titleGroup, BorderLayout.WEST);

                card.add(cardTopBar, BorderLayout.NORTH);

                // ── The JTable ────────────────────────────────────────────────────
                final Color ROW_ODD = Color.WHITE;
                final Color ROW_EVEN = new Color(248, 250, 252);
                final Color ROW_SEL = new Color(239, 246, 255);

                userTable = new JTable(tableModel) {
                        @Override
                        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                                        int col) {
                                Component c = super.prepareRenderer(renderer, row, col);
                                if (!isRowSelected(row)) {
                                        c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                                } else {
                                        c.setBackground(ROW_SEL);
                                }
                                return c;
                        }
                };
                userTable.setRowHeight(62);
                userTable.setShowGrid(false);
                userTable.setIntercellSpacing(new Dimension(0, 0));
                userTable.setBackground(ROW_ODD);
                userTable.setSelectionBackground(ROW_SEL);
                userTable.setFocusable(false);

                // Header
                javax.swing.table.JTableHeader th = userTable.getTableHeader();
                th.setFont(new Font("Segoe UI", Font.BOLD, 11));
                th.setForeground(new Color(100, 116, 139));
                th.setBackground(new Color(248, 250, 252));
                th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
                th.setPreferredSize(new Dimension(0, 38));
                th.setReorderingAllowed(false);

                // Column 0 (User) header stays LEFT, all others CENTER
                DefaultTableCellRenderer headerLeft = new DefaultTableCellRenderer();
                headerLeft.setHorizontalAlignment(JLabel.LEFT);
                headerLeft.setBackground(new Color(248, 250, 252));
                headerLeft.setForeground(new Color(30, 41, 59));
                headerLeft.setBorder(new EmptyBorder(0, 12, 0, 12));
                headerLeft.setFont(new Font("Segoe UI", Font.BOLD, 11));

                DefaultTableCellRenderer headerCenter = new DefaultTableCellRenderer();
                headerCenter.setHorizontalAlignment(JLabel.CENTER);
                headerCenter.setBackground(new Color(248, 250, 252));
                headerCenter.setForeground(new Color(30, 41, 59));
                headerCenter.setBorder(new EmptyBorder(0, 0, 0, 0));
                headerCenter.setFont(new Font("Segoe UI", Font.BOLD, 11));

                userTable.getColumnModel().getColumn(0).setHeaderRenderer(headerLeft);
                for (int i = 1; i < userTable.getColumnCount(); i++) {
                        userTable.getColumnModel().getColumn(i).setHeaderRenderer(headerCenter);
                }

                // Cell renderers
                userTable.getColumnModel().getColumn(0).setCellRenderer(new UserTableRenderers.UserCellRenderer());
                userTable.getColumnModel().getColumn(1).setCellRenderer(new UserTableRenderers.RoleCellRenderer());
                userTable.getColumnModel().getColumn(2).setCellRenderer(new UserTableRenderers.StatusCellRenderer());
                userTable.getColumnModel().getColumn(3).setCellRenderer(new UserTableRenderers.DateCellRenderer());
                userTable.getColumnModel().getColumn(4).setCellRenderer(new UserTableRenderers.DateCellRenderer());
                userTable.getColumnModel().getColumn(5).setCellRenderer(new UserTableRenderers.ActionCellRenderer());

                // Column widths
                userTable.getColumnModel().getColumn(0).setPreferredWidth(240);
                userTable.getColumnModel().getColumn(1).setPreferredWidth(140);
                userTable.getColumnModel().getColumn(2).setPreferredWidth(110);
                userTable.getColumnModel().getColumn(3).setPreferredWidth(130);
                userTable.getColumnModel().getColumn(4).setPreferredWidth(130);
                userTable.getColumnModel().getColumn(5).setPreferredWidth(90);

                JScrollPane scrollPane = new JScrollPane(userTable);
                scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
                scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
                scrollPane.getViewport().setBackground(ROW_ODD);
                scrollPane.setBackground(Color.WHITE);

                card.add(scrollPane, BorderLayout.CENTER);

                ShadowPanel shadowWrapper = new ShadowPanel();
                shadowWrapper.add(card);
                wrapper.add(shadowWrapper, BorderLayout.CENTER);

                return wrapper;
        }

        private JPanel createPermissionsPanel() {
                JPanel panel = new JPanel(new BorderLayout(0, 25));
                panel.setOpaque(false);

                JLabel header = new JLabel("Role Permissions");
                header.setFont(UIStyle.SUBTITLE);
                header.setForeground(UIStyle.TEXT_PRIMARY);

                JPanel rolesPanel = new JPanel(new GridLayout(1, 2, 40, 0));
                rolesPanel.setOpaque(false);

                rolesPanel.add(wrapInShadow(createRoleCard("Administrator", "ADMIN", new String[] {
                                "Full system access",
                                "User management",
                                "System Settings",
                                "All reports"
                }, new Color(254, 226, 226), UIStyle.DANGER)));

                rolesPanel.add(wrapInShadow(createRoleCard("Staff", "STAFF", new String[] {
                                "POS operations",
                                "View inventory",
                                "Basic Dashboard",
                                "Inventory Management"
                }, new Color(224, 242, 254), UIStyle.PRIMARY)));

                panel.add(header, BorderLayout.NORTH);
                panel.add(rolesPanel, BorderLayout.CENTER);

                return panel;
        }

        private JPanel wrapInShadow(JPanel content) {
                ShadowPanel shadow = new ShadowPanel();
                shadow.add(content);
                return shadow;
        }

        private JPanel createRoleCard(String roleName, String iconType, String[] permissions, Color iconBg,
                        Color iconFg) {
                JPanel card = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(getBackground());
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                                g2.dispose();
                        }
                };
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(Color.WHITE);
                card.setBorder(new EmptyBorder(25, 25, 25, 25));

                JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
                headerPanel.setOpaque(false);
                headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Load Icon
                JLabel iconLabel = new JLabel();
                try {
                        String iconPath = iconType.equals("ADMIN") ? "/src/resources/images/administrator.png"
                                        : "/src/resources/images/staff.png";
                        java.net.URL imgUrl = getClass().getResource(iconPath);
                        if (imgUrl != null) {
                                ImageIcon original = new ImageIcon(imgUrl);
                                Image scaled = original.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                                iconLabel.setIcon(new ImageIcon(scaled));
                        } else {
                                iconLabel.setText(iconType);
                                iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                                iconLabel.setForeground(iconFg);
                        }
                } catch (Exception e) {
                        iconLabel.setText(iconType);
                }

                JLabel nameLabel = new JLabel(roleName);
                nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                nameLabel.setForeground(UIStyle.TEXT_PRIMARY);

                headerPanel.add(iconLabel);
                headerPanel.add(nameLabel);

                card.add(headerPanel);
                card.add(Box.createVerticalStrut(20));

                for (String permission : permissions) {
                        JLabel permLabel = new JLabel(" •  " + permission);
                        permLabel.setFont(UIStyle.REGULAR);
                        permLabel.setForeground(new Color(75, 85, 99));
                        permLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        card.add(permLabel);
                        card.add(Box.createVerticalStrut(10));
                }

                return card;
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

                        User.UserRole role = ((String) roleCombo.getSelectedItem()).equals("Admin")
                                        ? User.UserRole.ADMIN
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
                label.setForeground(UIStyle.TEXT_PRIMARY);
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                return label;
        }

        private void refreshStats() {
                statsPanel.removeAll();

                totalAccountsCard = new UserManagementPanel.StatCard("Total Accounts",
                                String.valueOf(tableModel.getRowCount()), "TOTAL", new Color(241, 245, 249),
                                new Color(100, 116, 139));
                activeAccountsCard = new UserManagementPanel.StatCard("Active Accounts",
                                String.valueOf(tableModel.getActiveCount()), "ACTIVE", new Color(220, 252, 231),
                                new Color(34, 197, 94));
                administratorsCard = new UserManagementPanel.StatCard("Administrators",
                                String.valueOf(tableModel.getAdminCount()), "ADMIN", new Color(254, 226, 226),
                                new Color(239, 68, 68));
                staffsCard = new UserManagementPanel.StatCard("Staffs",
                                String.valueOf(tableModel.getStaffCount()), "STAFF", new Color(224, 242, 254),
                                UIStyle.PRIMARY);

                statsPanel.add(totalAccountsCard);
                statsPanel.add(activeAccountsCard);
                statsPanel.add(administratorsCard);
                statsPanel.add(staffsCard);

                statsPanel.revalidate();
                statsPanel.repaint();
        }

        // Inner class for Stat Card
        public class StatCard extends JPanel {

                public StatCard(String title, String value, String iconType, Color iconBg, Color iconFg) {

                        setLayout(new GridBagLayout());
                        setBackground(Color.WHITE);
                        setBorder(new EmptyBorder(20, 25, 20, 25));

                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.fill = GridBagConstraints.BOTH;
                        gbc.weightx = 1.0;

                        // Title (Top Left)
                        JLabel titleLabel = new JLabel(title);
                        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        titleLabel.setForeground(UIStyle.TEXT_SECONDARY);
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.weighty = 0.3;
                        add(titleLabel, gbc);

                        // Icon (Top Right)
                        JPanel iconContainer = new JPanel() {
                                @Override
                                protected void paintComponent(Graphics g) {
                                        Graphics2D g2 = (Graphics2D) g.create();
                                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                        RenderingHints.VALUE_ANTIALIAS_ON);
                                        g2.setColor(iconBg);
                                        g2.fillOval(0, 0, getWidth(), getHeight());

                                        // Draw generic icon symbol
                                        g2.setColor(iconFg);
                                        int w = getWidth();
                                        int h = getHeight();
                                        if (iconType.equals("TOTAL")) {
                                                try {
                                                        java.net.URL imgUrl = getClass().getResource(
                                                                        "/src/resources/images/accounts.png");
                                                        if (imgUrl != null) {
                                                                Image img = new ImageIcon(imgUrl).getImage();
                                                                g2.drawImage(img, w / 4, h / 4, w / 2, h / 2, null);
                                                        } else {
                                                                g2.fillOval(w / 4, h / 4, w / 2, h / 2);
                                                        }
                                                } catch (Exception e) {
                                                        g2.fillOval(w / 4, h / 4, w / 2, h / 2);
                                                }
                                        } else if (iconType.equals("ACTIVE")) {
                                                try {
                                                        java.net.URL imgUrl = getClass().getResource(
                                                                        "/src/resources/images/active.png");
                                                        if (imgUrl != null) {
                                                                Image img = new ImageIcon(imgUrl).getImage();
                                                                g2.drawImage(img, w / 4, h / 4, w / 2, h / 2, null);
                                                        } else {
                                                                g2.setStroke(new BasicStroke(2));
                                                                g2.drawLine(w / 4, h / 2, w / 2, h * 3 / 4);
                                                                g2.drawLine(w / 2, h * 3 / 4, w * 3 / 4, h / 4);
                                                        }
                                                } catch (Exception e) {
                                                        g2.setStroke(new BasicStroke(2));
                                                        g2.drawLine(w / 4, h / 2, w / 2, h * 3 / 4);
                                                        g2.drawLine(w / 2, h * 3 / 4, w * 3 / 4, h / 4);
                                                }
                                        } else if (iconType.equals("ADMIN")) {
                                                try {
                                                        java.net.URL imgUrl = getClass().getResource(
                                                                        "/src/resources/images/administrator.png");
                                                        if (imgUrl != null) {
                                                                Image img = new ImageIcon(imgUrl).getImage();
                                                                g2.drawImage(img, w / 4, h / 4, w / 2, h / 2, null);
                                                        } else {
                                                                g2.drawRoundRect(w / 4, h / 4, w / 2, h / 2, 5, 5);
                                                        }
                                                } catch (Exception e) {
                                                        g2.drawRoundRect(w / 4, h / 4, w / 2, h / 2, 5, 5);
                                                }
                                        } else { // STAFF
                                                try {
                                                        java.net.URL imgUrl = getClass().getResource(
                                                                        "/src/resources/images/staff.png");
                                                        if (imgUrl != null) {
                                                                Image img = new ImageIcon(imgUrl).getImage();
                                                                g2.drawImage(img, w / 4, h / 4, w / 2, h / 2, null);
                                                        } else {
                                                                g2.fillOval(w / 3, h / 4, w / 3, h / 3);
                                                                g2.fillArc(w / 4, h / 2, w / 2, h / 2, 0, 180);
                                                        }
                                                } catch (Exception e) {
                                                        g2.fillOval(w / 3, h / 4, w / 3, h / 3);
                                                        g2.fillArc(w / 4, h / 2, w / 2, h / 2, 0, 180);
                                                }
                                        }
                                        g2.dispose();
                                }
                        };
                        iconContainer.setPreferredSize(new Dimension(40, 40));
                        iconContainer.setOpaque(false);
                        gbc.gridx = 1;
                        gbc.gridy = 0;
                        gbc.weightx = 0.0;
                        gbc.anchor = GridBagConstraints.NORTHEAST;
                        add(iconContainer, gbc);

                        // Value (Bottom Left)
                        JLabel valueLabel = new JLabel(value);
                        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
                        valueLabel.setForeground(UIStyle.TEXT_PRIMARY);
                        gbc.gridx = 0;
                        gbc.gridy = 1;
                        gbc.gridwidth = 2;
                        gbc.weightx = 1.0;
                        gbc.weighty = 0.7;
                        gbc.anchor = GridBagConstraints.SOUTHWEST;
                        add(valueLabel, gbc);
                }

                @Override
                protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                        g2.dispose();
                }
        }
}
