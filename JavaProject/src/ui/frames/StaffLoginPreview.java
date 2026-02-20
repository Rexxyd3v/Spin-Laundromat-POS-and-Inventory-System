package src.ui.frames;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StaffLoginPreview extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Design Colors
    private static final Color BRAND_PURPLE = new Color(93, 33, 209);
    private static final Color SUBTITLE_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_GREY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(243, 244, 246);
    private static final Color INPUT_BORDER = new Color(209, 213, 219);
    private static final Color BUTTON_BLACK = new Color(10, 10, 10);
    private static final Color LINK_BLUE = new Color(14, 165, 233);
    private static final Color BG_CYAN = new Color(194, 240, 242);
    private static final Color NAVY_DARK = new Color(55, 48, 163);

    public StaffLoginPreview() {
        setTitle("Spin Laundromat Service - Employee Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_CYAN);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconContainer.setOpaque(false);
        iconContainer.add(createBrandIcon());

        JLabel brandTitle = new JLabel("Spin Laundromat Service");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        brandTitle.setForeground(BRAND_PURPLE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("POS & Inventory System");
        brandSubtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandSubtitle.setForeground(TEXT_GREY);
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconContainer);
        headerPanel.add(Box.createVerticalStrut(15));
        headerPanel.add(brandTitle);
        headerPanel.add(brandSubtitle);

        // Card Panel
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Employee Sign In");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        sectionTitle.setForeground(Color.BLACK);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(650, 180));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uLabel = createFieldLabel("Username");
        usernameField = createStyledTextField("Enter your username", false);

        JLabel pLabel = createFieldLabel("Password");
        passwordField = (JPasswordField) createStyledTextField("Enter your password", true);

        formPanel.add(uLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(pLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(passwordField);

        // 4. Sign In Button
        JButton signInBtn = createBlackSignInButton();
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.addActionListener(e -> {
            // Mock Transition to Dashboard Preview
            this.dispose();
            new DashboardPreview().display();
        });

        // 5. Footer Links
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(420, 30));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel forgotPass = new JLabel("Forgot Password?");
        forgotPass.setFont(new Font("SansSerif", Font.BOLD, 13));
        forgotPass.setForeground(NAVY_DARK);
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel contactAdmin = new JLabel("Contact Admin");
        contactAdmin.setFont(new Font("SansSerif", Font.BOLD, 13));
        contactAdmin.setForeground(LINK_BLUE);
        contactAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(forgotPass);
        footer.add(contactAdmin);

        // Assembly
        card.add(Box.createVerticalStrut(40));
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(40));
        card.add(formPanel);
        card.add(Box.createVerticalStrut(40));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(30));
        card.add(footer);
        card.add(Box.createVerticalGlue());

        JPanel verticalWrapper = new JPanel();
        verticalWrapper.setLayout(new BoxLayout(verticalWrapper, BoxLayout.Y_AXIS));
        verticalWrapper.setOpaque(false);
        verticalWrapper.add(headerPanel);
        verticalWrapper.add(Box.createVerticalStrut(20));
        verticalWrapper.add(card);

        root.add(verticalWrapper);
        add(root);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField(String placeholder, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.setBackground(INPUT_BG);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        field.setMaximumSize(new Dimension(650, 45));
        field.setPreferredSize(new Dimension(650, 45));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setText(placeholder);
        field.setForeground(TEXT_GREY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_GREY);
                }
            }
        });
        return field;
    }

    public void display() {
        setVisible(true);
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 40, 40);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 40, 40);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(750, 480));
        card.setMaximumSize(new Dimension(750, 480));
        card.setBorder(new EmptyBorder(0, 40, 0, 40));
        return card;
    }

    private JComponent createBrandIcon() {
        return new JPanel() {
            private Image brandImg;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/util/images/icons8-machine-wash-100.png");
                    if (imgUrl != null) {
                        brandImg = new ImageIcon(imgUrl).getImage();
                    }
                } catch (Exception e) {
                }
                setPreferredSize(new Dimension(90, 90));
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SUBTITLE_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                if (brandImg != null) {
                    int imgW = 60;
                    int imgH = 60;
                    int x = (getWidth() - imgW) / 2;
                    int y = (getHeight() - imgH) / 2;
                    g2.drawImage(brandImg, x, y, imgW, imgH, null);
                }
                g2.dispose();
            }
        };
    }

    private JButton createBlackSignInButton() {
        JButton btn = new JButton("Sign in") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 30, 30);
                }
                g2.setColor(BUTTON_BLACK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText()) - 40) / 2;
                int textY = (getHeight() - 5 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                int circleSize = 28;
                int circleX = textX + fm.stringWidth(getText()) + 15;
                int circleY = (getHeight() - 5 - circleSize) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(circleX, circleY, circleSize, circleSize);
                g2.setColor(BUTTON_BLACK);
                g2.setStroke(new BasicStroke(2.5f));
                int arrowPadding = 8;
                g2.drawLine(circleX + arrowPadding, circleY + circleSize / 2, circleX + circleSize - arrowPadding,
                        circleY + circleSize / 2);
                g2.drawLine(circleX + circleSize - arrowPadding - 5, circleY + arrowPadding,
                        circleX + circleSize - arrowPadding, circleY + circleSize / 2);
                g2.drawLine(circleX + circleSize - arrowPadding - 5, circleY + circleSize - arrowPadding,
                        circleX + circleSize - arrowPadding, circleY + circleSize / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(170, 65));
        btn.setMaximumSize(new Dimension(170, 65));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffLoginPreview().display());
    }
}
