package src.ui.panels.userPanel;

import java.awt.*;
import java.awt.RenderingHints;
import javax.swing.*;
import javax.swing.border.*;
import src.model.User;
import src.util.components.Scrollbar;

public class EditUserDialog extends JDialog {
    private static final Color FIELD_BG = new Color(243, 244, 246);
    private static final Color BLUE_ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_GRAY = new Color(229, 231, 235);

    private boolean updated = false;
    private boolean deleted = false;
    private JTextField nameField, contactField; 
    private JComboBox<String> roleCombo;
    private JComboBox<String> statusCombo;
    private User user;

    public EditUserDialog(Window parent, User user) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.user = user;
        setUndecorated(true);

        // Adaptive sizing
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 450;
        int height = Math.min(700, screen.height - 100);
        setSize(width, height);
        setLocationRelativeTo(parent);

        // Rounded container (similar to AddUserDialog)
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(59, 130, 246, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
         container.setOpaque(false);
        container.setBorder(new EmptyBorder(35, 35, 35, 35));

        // Header
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Edit User");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Update user details such as name, contact number, role, and status. You can also delete the account.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(subtitle);

        headerWrapper.add(headerText, BorderLayout.WEST);

        JButton exitBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(239, 68, 68) : TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        exitBtn.setPreferredSize(new Dimension(32, 32));
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> dispose());
        headerWrapper.add(exitBtn, BorderLayout.EAST);

        container.add(headerWrapper, BorderLayout.NORTH);

         // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 18, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;
        nameField = new JTextField(user.getName());
        gbc.gridy = row++;
        form.add(createFieldPanel("Name", "Enter full name", true, nameField = new JTextField(user.getName())), gbc);
        contactField = new JTextField(user.getContactNumber());
        gbc.gridy = row++;
        form.add(createFieldPanel("Contact Number", "Enter contact number", true, contactField = new JTextField(user.getContactNumber())), gbc);

        gbc.gridy = row++;
        form.add(createComboBoxPanel("Role", true, new String[]{"Admin", "Staff"}, user.getRole() == User.UserRole.ADMIN ? 0 : 1), gbc);

        gbc.gridy = row++;
        form.add(createStatusComboBoxPanel("Status", true, new String[]{"Active", "Inactive"}, user.isActive() ? 0 : 1), gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scroll, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));

        JButton updateBtn = new JButton("Confirm Update") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
         updateBtn.setPreferredSize(new Dimension(0, 50));
        updateBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setContentAreaFilled(false);
        updateBtn.setBorderPainted(false);
        updateBtn.setFocusPainted(false);
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateBtn.addActionListener(e -> handleUpdate());

        JButton deleteBtn = new JButton("Delete User") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        deleteBtn.setPreferredSize(new Dimension(150, 50));
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> handleDelete());

        JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateBtn, BorderLayout.CENTER);
        buttonPanel.add(deleteBtn, BorderLayout.EAST);

        footer.add(buttonPanel, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);

        add(container);
        setBackground(new Color(0, 0, 0, 0));
    }

    
    private JPanel createFieldPanel(String labelText, String placeholder, boolean showAsterisk, JTextField field) {
        // Same as in AddUserDialog.java
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        field.setPreferredSize(new Dimension(0, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 15, 0, 15));
        field.setForeground(TEXT_SECONDARY);

        JPanel fieldWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);

        p.add(fieldWrapper, BorderLayout.CENTER);
        return p;
    }

    private JPanel createComboBoxPanel(String labelText, boolean showAsterisk, String[] items, int selectedIndex) {
        // Similar to AddUserDialog, but with pre-selection
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        roleCombo = new JComboBox<>(items);
        roleCombo.setSelectedIndex(selectedIndex);
        roleCombo.setPreferredSize(new Dimension(0, 45));
        roleCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleCombo.setBorder(null);
        roleCombo.setOpaque(false);

        JPanel comboWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        comboWrapper.setOpaque(false);
        comboWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        comboWrapper.add(roleCombo, BorderLayout.CENTER);

        p.add(comboWrapper, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatusComboBoxPanel(String labelText, boolean showAsterisk, String[] items, int selectedIndex) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        statusCombo = new JComboBox<>(items);
        statusCombo.setSelectedIndex(selectedIndex);
        statusCombo.setPreferredSize(new Dimension(0, 45));
        statusCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusCombo.setBorder(null);
        statusCombo.setOpaque(false);

        JPanel comboWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        comboWrapper.setOpaque(false);
        comboWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        comboWrapper.add(statusCombo, BorderLayout.CENTER);

        p.add(comboWrapper, BorderLayout.CENTER);
        return p;
    }

    private void handleUpdate() {
        // Validation and update logic
        if (nameField.getText().trim().isEmpty() || contactField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to update this user?\n\nName: " + nameField.getText().trim() + "\nRole: " + roleCombo.getSelectedItem() + "\nStatus: " + statusCombo.getSelectedItem(),
            "Confirm Update",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            user.setName(nameField.getText().trim());
            user.setContactNumber(contactField.getText().trim());
            user.setRole(roleCombo.getSelectedItem().equals("Admin") ? User.UserRole.ADMIN : User.UserRole.STAFF);
            user.setActive(statusCombo.getSelectedItem().equals("Active"));
            updated = true;
            JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private void handleDelete() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            deleted = true;
            dispose();
        }
    }
    public boolean isUpdated() { return updated; }
    public boolean isDeleted() { return deleted; }
}
