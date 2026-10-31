package ui.frames;

import java.awt.*;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

/**
 * Shared branded login, with readable stationary fields and gentle background
 * motion.
 */
public final class WelcomePanel extends JPanel {
    private final AnimatedLoginBackground story = new AnimatedLoginBackground();
    private final JButton submit = button("Sign in", AppIcon.Name.ARROW, true);
    private final JTextField firstInput;
    private final IntroPanel intro;
    private JRootPane attachedRoot;
    private final PropertyChangeListener motionListener;

    public WelcomePanel(boolean staff, Runnable proceed) {
        super(new GridLayout(1, 2));
        UIStyle.installDefaults();
        setBackground(UIStyle.BACKGROUND);
        story.setBorder(new EmptyBorder(30, 34, 26, 34));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 48));
        JLabel brand = label(persistence.Repositories.settings().get().storeName(), new Font("Segoe UI", Font.BOLD, 19),
                Color.WHITE);
        header.add(brand, BorderLayout.CENTER);
        story.add(header, BorderLayout.NORTH);
        JComponent art = new JComponent() {
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), size = Math.min(340, Math.min(w - 12, (int) (h * .58)));
                int y = Math.max(6, (int) (h * .03));
                BrandMark.draw(g, (w - size) / 2, y, size, size);
                int baseline = Math.max(y + size + 42, (int) (h * .68));
                int fontSize = w < 390 ? 30 : 36;
                g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
                g.setColor(Color.WHITE);
                g.drawString("Fresh laundry.", 12, baseline);
                g.setColor(UIStyle.YELLOW);
                g.drawString("A smoother day.", 12, baseline + fontSize + 8);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g.setColor(new Color(190, 212, 213));
                g.drawString("Your counter, your team, your everyday.", 12, baseline + fontSize + 42);
                g.dispose();
            }
        };
        story.add(art, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(label("A FRESH START, EVERY DAY", new Font("Segoe UI", Font.BOLD, 10), new Color(168, 199, 197)),
                BorderLayout.CENTER);
        story.add(footer, BorderLayout.SOUTH);
        add(story);
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(250, 250, 247));
        right.setBorder(new EmptyBorder(32, 24, 32, 24));
        JPanel form = new Surface();
        form.setBorder(new EmptyBorder(36, 30, 32, 30));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        centered(form, staff ? "Employee Sign In" : "Enter Master Key", new Font("Segoe UI", Font.BOLD, 28),
                UIStyle.TEXT_PRIMARY);
        form.add(Box.createVerticalStrut(10));
        centered(form, staff ? "Your team's laundry workspace" : "Continue to employee sign in", UIStyle.REGULAR,
                UIStyle.TEXT_SECONDARY);
        form.add(Box.createVerticalStrut(32));
        JTextField username = null;
        if (staff) {
            username = field("Username");
            username.getAccessibleContext().setAccessibleName("Username");
            addField(form, formField("Username", username));
        }
        JPasswordField password = new JPasswordField();
        password.setName("loginPassword");
        password.setFont(UIStyle.REGULAR);
        password.setOpaque(false);
        password.setBackground(new Color(245, 248, 250));
        password.setBorder(new EmptyBorder(12, 14, 12, 4));
        password.getAccessibleContext().setAccessibleName(staff ? "Password" : "Master key");
        JPanel passwordRow = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(245, 248, 250));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g.setColor(UIStyle.INPUT_BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g.dispose();
            }
        };
        passwordRow.setOpaque(false);
        passwordRow.setPreferredSize(new Dimension(300, 50));
        passwordRow.setBorder(new EmptyBorder(1, 8, 1, 4));
        JLabel lock = new JLabel(new AppIcon(AppIcon.Name.SHIELD, 19, UIStyle.TEXT_SECONDARY));
        lock.setBorder(new EmptyBorder(0, 6, 0, 0));
        passwordRow.add(lock, BorderLayout.WEST);
        passwordRow.add(password, BorderLayout.CENTER);
        JButton eye = new JButton(new AppIcon(AppIcon.Name.EYE, 19, UIStyle.TEXT_SECONDARY));
        eye.setName("showLoginPassword");
        eye.setPreferredSize(new Dimension(40, 44));
        eye.setContentAreaFilled(false);
        eye.setBorderPainted(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.setToolTipText("Show password");
        eye.getAccessibleContext().setAccessibleName("Show password");
        char echo = password.getEchoChar();
        eye.addActionListener(e -> {
            boolean show = password.getEchoChar() != 0;
            password.setEchoChar(show ? (char) 0 : echo);
            eye.setIcon(new AppIcon(show ? AppIcon.Name.EYE_OFF : AppIcon.Name.EYE, 19, UIStyle.TEXT_SECONDARY));
            String description = show ? "Hide password" : "Show password";
            eye.setToolTipText(description);
            eye.getAccessibleContext().setAccessibleName(description);
            password.requestFocusInWindow();
        });
        passwordRow.add(eye, BorderLayout.EAST);
        addField(form, formField(staff ? "Password" : "Master key", passwordRow));
        firstInput = staff ? username : password;
        submit.setName("loginSubmit");
        submit.setAlignmentX(CENTER_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        submit.addActionListener(e -> {
            if (!submit.isEnabled())
                return;
            submit.setEnabled(false);
            try {
                proceed.run();
            } catch (RuntimeException failure) {
                submit.setEnabled(true);
                throw failure;
            }
        });
        form.add(submit);
        form.add(Box.createVerticalStrut(18));
        centered(form, "Preview mode · credentials are not verified yet", new Font("Segoe UI", Font.PLAIN, 11),
                UIStyle.TEXT_SECONDARY);
        form.setPreferredSize(new Dimension(420, form.getPreferredSize().height));
        intro = new IntroPanel(form);
        motionListener = e -> intro.finishIfReduced();
        right.add(intro);
        add(right);
    }

    public void focusInput() {
        firstInput.requestFocusInWindow();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        MotionPreferences.addListener(motionListener);
        attachedRoot = getRootPane();
        if (attachedRoot != null)
            attachedRoot.setDefaultButton(submit);
        intro.begin();
    }

    @Override
    public void removeNotify() {
        MotionPreferences.removeListener(motionListener);
        if (attachedRoot != null && attachedRoot.getDefaultButton() == submit)
            attachedRoot.setDefaultButton(null);
        attachedRoot = null;
        super.removeNotify();
    }

    private static void centered(JPanel panel, String text, Font font, Color color) {
        JLabel label = label(text, font, color);
        label.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(label);
    }

    private void addField(JPanel form, JComponent field) {
        field.setAlignmentX(CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        form.add(field);
        form.add(Box.createVerticalStrut(20));
    }

    private static final class IntroPanel extends JPanel {
        private final Timer timer = new Timer(16, e -> advance());
        private long began;
        private float progress = 1;

        IntroPanel(JComponent view) {
            super(new BorderLayout());
            setOpaque(false);
            add(view);
            timer.setCoalesce(true);
        }

        void begin() {
            if (!MotionPreferences.isEnabled()) {
                progress = 1;
                return;
            }
            began = System.nanoTime();
            progress = 0;
            timer.start();
        }

        void finishIfReduced() {
            if (!MotionPreferences.isEnabled()) {
                timer.stop();
                progress = 1;
                repaint();
            }
        }

        private void advance() {
            progress = Math.min(1, (System.nanoTime() - began) / 420_000_000f);
            if (progress >= 1)
                timer.stop();
            repaint();
        }

        protected void paintChildren(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            float eased = 1 - (float) Math.pow(1 - progress, 3);
            g.setComposite(AlphaComposite.SrcOver.derive(eased));
            super.paintChildren(g);
            g.dispose();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }
    }
}
