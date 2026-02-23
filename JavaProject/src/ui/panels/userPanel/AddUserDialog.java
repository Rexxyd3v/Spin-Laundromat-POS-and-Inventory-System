package src.ui.panels.userPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import src.util.components.Scrollbar;

public class AddUserDialog extends JDialog {

    private static final Color FIELD_BG = new Color(243, 244, 246);
    private static final Color BLUE_ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_GRAY = new Color(229, 231, 235);

    private boolean confirmed = false;
    private JTextField firstNameField, middleInitialField, surnameField, contactNumberField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;

    public AddUserDialog(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        // Adaptive sizing
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 450;
        int height = Math.min(850, screen.height - 100);
        setSize(width, height);
        setLocationRelativeTo(parent);

        // Use a rounded content pane
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(59, 130, 246, 100)); // Subtle blue border glow
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(35, 35, 35, 35));

        // Header with Exit Button
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Add New User");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Create a new user account with appropriate role and permissions.");
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

        gbc.gridy = row++;
        form.add(createFieldPanel("First Name", "Enter full name", true, firstNameField = new JTextField()), gbc);

        gbc.gridy = row++;
        form.add(createFieldPanel("Middle Initial", "Enter full name", true, middleInitialField = new JTextField()),
                gbc);

        gbc.gridy = row++;
        form.add(createFieldPanel("Surname", "Enter full name", true, surnameField = new JTextField()), gbc);

        gbc.gridy = row++;
        form.add(
                createFieldPanel("Contact Number", "Enter contact number", true, contactNumberField = new JTextField()),
                gbc);

        gbc.gridy = row++;
        form.add(createFieldPanel("Password", "Enter password", true, passwordField = new JPasswordField()), gbc);

        gbc.gridy = row++;
        form.add(createFieldPanel("Confirm Password", "Confirm password", true,
                confirmPasswordField = new JPasswordField()), gbc);

        gbc.gridy = row++;
        form.add(createComboBoxPanel("Role", true, new String[] { "Admin", "Staff" }), gbc);

        // Wrap in scroll pane if too tall
        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scroll, BorderLayout.CENTER);

        // Footer
        JButton createBtn = new JButton("Create User") {
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
        createBtn.setPreferredSize(new Dimension(0, 50));
        createBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        createBtn.setForeground(Color.WHITE);
        createBtn.setContentAreaFilled(false);
        createBtn.setBorderPainted(false);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.addActionListener(e -> handleCreate());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(25, 0, 0, 0));
        footer.add(createBtn, BorderLayout.CENTER);

        container.add(footer, BorderLayout.SOUTH);

        add(container);
        setBackground(new Color(0, 0, 0, 0));
    }

    private JPanel createFieldPanel(String labelText, String placeholder, boolean showAsterisk, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel(
                "<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        field.setPreferredSize(new Dimension(0, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 15, 0, 15));
        field.setForeground(TEXT_SECONDARY);
        // Set placeholder behavior if needed, for now just basic

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

    private JPanel createComboBoxPanel(String labelText, boolean showAsterisk, String[] items) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel(
                "<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        roleCombo = new JComboBox<>(items);
        roleCombo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.BLACK);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                                (getHeight() + fm.getAscent()) / 2 - 2);
                        g2.dispose();
                    }
                };
                btn.setBorder(null);
                btn.setContentAreaFilled(false);
                return btn;
            }
        });
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

    private void handleCreate() {
        // Validation logic
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (firstNameField.getText().trim().isEmpty() || surnameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        confirmed = true;
        dispose();
    }

    public static Object[] showAddUser(Window parent) {
        AddUserDialog dialog = new AddUserDialog(parent);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            String fullName = dialog.firstNameField.getText().trim() + " " +
                    dialog.middleInitialField.getText().trim() + " " +
                    dialog.surnameField.getText().trim();
            fullName = fullName.replaceAll("\\s+", " ");
            return new Object[] {
                    fullName,
                    dialog.contactNumberField.getText().trim(),
                    dialog.roleCombo.getSelectedItem(),
                    new String(dialog.passwordField.getPassword())
            };
        }
        return null;
    }
}
