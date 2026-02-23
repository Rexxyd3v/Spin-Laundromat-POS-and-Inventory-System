package src.ui.frames;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MasterKey extends JFrame {
    private final JPasswordField masterKeyField;
    private static final Color BRAND_PURPLE = new Color(93, 33, 209);
    private static final Color SUBTITLE_BLUE = new Color(59, 130, 246);
    private static final Color HEADING_DARK = new Color(55, 48, 163);
    private static final Color INPUT_BG = new Color(219, 234, 254);
    private static final Color INPUT_TEXT = new Color(147, 197, 253);
    private static final Color BUTTON_BLUE = new Color(37, 99, 235);
    private static final Color LINK_BLUE = new Color(14, 165, 233);
    private static final Color BG_CYAN = new Color(194, 240, 242);

    public MasterKey() {
        setTitle("Spin Laundromat Service - Master Key");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        // Set Window Icon
        try {
            java.net.URL iconUrl = getClass().getResource("/src/resources/images/desktoplogo.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
        }

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_CYAN);

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconContainer.setOpaque(false);
        iconContainer.add(createBrandIcon());

        JLabel brandTitle = new JLabel("Spin Laundromat Service");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        brandTitle.setForeground(BRAND_PURPLE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("WASH, DRY, FOLD IN 2 HOURS");
        brandSubtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        brandSubtitle.setForeground(SUBTITLE_BLUE);
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sectionTitle = new JLabel("Enter Master Key");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        sectionTitle.setForeground(HEADING_DARK);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel passwordPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(INPUT_TEXT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
            }
        };
        passwordPanel.setOpaque(false);
        passwordPanel.setMaximumSize(new Dimension(420, 50));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        masterKeyField = new RoundPasswordField(20);
        masterKeyField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        masterKeyField.setBackground(new Color(0, 0, 0, 0));
        masterKeyField.setBorder(new EmptyBorder(0, 45, 0, 5));

        // Eye toggle button
        JButton eyeButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADING_DARK);
                int cx = getWidth() / 2, cy = getHeight() / 2;

                if (masterKeyField.getEchoChar() == 0) {
                    g2.drawOval(cx - 8, cy - 5, 16, 10);
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                } else {
                    g2.drawOval(cx - 8, cy - 5, 16, 10);
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(cx - 10, cy + 6, cx + 10, cy - 6);
                }
                g2.dispose();
            }
        };

        eyeButton.setPreferredSize(new Dimension(50, 50));
        eyeButton.setContentAreaFilled(false);
        eyeButton.setBorderPainted(false);
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeButton.setToolTipText("Show/Hide password");

        eyeButton.addActionListener(e -> {
            if (masterKeyField.getEchoChar() == 0) {
                masterKeyField.setEchoChar('•');
                eyeButton.setToolTipText("Show password");
            } else {
                masterKeyField.setEchoChar((char) 0);
                eyeButton.setToolTipText("Hide password");
            }
            masterKeyField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            eyeButton.repaint();
            masterKeyField.requestFocusInWindow();
        });

        passwordPanel.add(masterKeyField, BorderLayout.CENTER);
        passwordPanel.add(eyeButton, BorderLayout.EAST);

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(420, 30));

        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rememberMe.setForeground(HEADING_DARK);
        rememberMe.setOpaque(false);

        JLabel forgotPass = new JLabel("Forgot password?");
        forgotPass.setFont(new Font("SansSerif", Font.PLAIN, 13));
        forgotPass.setForeground(HEADING_DARK);
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optionsPanel.add(rememberMe, BorderLayout.WEST);
        optionsPanel.add(forgotPass, BorderLayout.EAST);

        // 5. Sign In Button
        JButton signInBtn = createSignInButton();
        signInBtn.addActionListener(e -> {
            // Mock Transition to Staff Login Preview
            this.dispose();
            new StaffLogin().display();
        });

        // 6. Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel footerText = new JLabel("Don't have an account yet? ");
        footerText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footerText.setForeground(HEADING_DARK);

        JLabel contactAdmin = new JLabel("Contact Admin");
        contactAdmin.setFont(new Font("SansSerif", Font.BOLD, 13));
        contactAdmin.setForeground(LINK_BLUE);
        contactAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(footerText);
        footer.add(contactAdmin);

        // Assembly
        card.add(Box.createVerticalStrut(40));
        card.add(iconContainer);
        card.add(Box.createVerticalStrut(15));
        card.add(brandTitle);
        card.add(brandSubtitle);
        card.add(Box.createVerticalStrut(40));
        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(30));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(optionsPanel);
        card.add(Box.createVerticalStrut(50));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(30));
        card.add(footer);
        card.add(Box.createVerticalGlue());

        root.add(card);
        add(root);
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

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 40, 40);

                // White background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 40, 40);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(600, 650));
        card.setBorder(new EmptyBorder(0, 40, 0, 40));
        return card;
    }

    private JComponent createBrandIcon() {
        return new JPanel() {
            private Image brandImg;
            {
                try {
                    // Try to load from resources
                    java.net.URL imgUrl = getClass().getResource("/src/resources/images/machine_wash.png");
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

                // Blue background box
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

    private JButton createSignInButton() {
        JButton btn = new JButton("Sign in") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!getModel().isPressed()) {
                    g2.setColor(new Color(37, 99, 235, 100));
                    g2.fillRoundRect(0, 5, getWidth(), getHeight() - 5, 20, 20);
                }

                g2.setColor(BUTTON_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 5, 20, 20);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText()) - 40) / 2;
                int textY = (getHeight() - 5 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                int circleSize = 30;
                int circleX = textX + fm.stringWidth(getText()) + 15;
                int circleY = (getHeight() - 5 - circleSize) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(circleX, circleY, circleSize, circleSize);

                g2.setColor(BUTTON_BLUE);
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
        btn.setPreferredSize(new Dimension(220, 65));
        btn.setMaximumSize(new Dimension(220, 65));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    class RoundPasswordField extends JPasswordField {
        public RoundPasswordField(int columns) {
            super(columns);
            setOpaque(false);
            setBackground(INPUT_BG);
            setForeground(HEADING_DARK);
            setCaretColor(HEADING_DARK);
            setBorder(new EmptyBorder(10, 45, 10, 15));
            setFont(new Font("SansSerif", Font.PLAIN, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Removed background filling to show parent's background
            g2.setColor(INPUT_TEXT);
            g2.setStroke(new BasicStroke(1.5f));
            int ix = 18;
            int iy = 15;
            g2.drawRoundRect(ix, iy + 6, 14, 10, 2, 2);
            g2.drawArc(ix + 3, iy, 8, 12, 0, 180);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void paintBorder(Graphics g) {
            // Border is now handled by the parent container
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MasterKey().display());
    }
}
