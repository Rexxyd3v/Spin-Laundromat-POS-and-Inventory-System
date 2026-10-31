package ui.frames;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import security.LocalAuth;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * Shared credential forms; password work runs away from the Swing event thread.
 */
public final class AuthDialogs {
    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    public static boolean form(Component parent, String title, JPanel fields, Action action, Runnable clear) {
        Window owner = parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UIStyle.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.add(label(title, UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY), BorderLayout.NORTH);
        root.add(scroll(fields), BorderLayout.CENTER);
        JTextArea status = new JTextArea("Use at least 10 characters. Passwords are case-sensitive.", 2, 34);
        status.setFont(UIStyle.SMALL);
        status.setForeground(UIStyle.TEXT_SECONDARY);
        status.setBorder(BorderFactory.createEmptyBorder());
        status.setOpaque(false);
        status.setEditable(false);
        status.setFocusable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JButton save = button("Continue", null, true), cancel = button("Cancel", null, false);
        boolean[] result = { false };
        JPanel bottom = column();
        bottom.add(status);
        bottom.add(Box.createVerticalStrut(12));
        JPanel actions = row(8);
        actions.add(save);
        actions.add(cancel);
        bottom.add(actions);
        root.add(bottom, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            save.setEnabled(false);
            cancel.setEnabled(false);
            setEnabled(fields, false);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            status.setText("Saving securely…");
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    action.run();
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        result[0] = true;
                        dialog.dispose();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        status.setText(cause.getMessage());
                        save.setEnabled(true);
                        cancel.setEnabled(true);
                        setEnabled(fields, true);
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    } finally {
                        clear.run();
                    }
                }
            }.execute();
        });
        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(save);
        dialog.setSize(570, Math.min(720, 230 + fields.getPreferredSize().height));
        dialog.setMinimumSize(new Dimension(480, 380));
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        clear.run();
        return result[0];
    }

    private static void setEnabled(Container parent, boolean enabled) {
        for (Component c : parent.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container v)
                setEnabled(v, enabled);
        }
    }

    public static void add(JPanel form, String title, JComponent field) {
        JComponent row = formField(title, field);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        form.add(row);
        form.add(Box.createVerticalStrut(16));
    }

    public static JPasswordField password() {
        JPasswordField f = new JPasswordField(24);
        f.setFont(UIStyle.REGULAR);
        return f;
    }

    public static boolean setup(Component owner) {
        JPanel fields = column();
        JTextField username = field("Owner username"), name = field("Owner name");
        username.setText("owner");
        name.setText("Store Owner");
        JPasswordField pw = password(), confirm = password(), master = password(), masterConfirm = password();
        add(fields, "Owner username", username);
        add(fields, "Display name", name);
        add(fields, "Password", pw);
        add(fields, "Confirm password", confirm);
        add(fields, "Master key", master);
        add(fields, "Confirm master key", masterConfirm);
        return form(owner, "Set up this store", fields, () -> {
            char[] a = pw.getPassword(), b = confirm.getPassword(), c = master.getPassword(),
                    d = masterConfirm.getPassword();
            try {
                if (!Arrays.equals(a, b) || !Arrays.equals(c, d))
                    throw new IllegalArgumentException("Confirmation does not match.");
                LocalAuth.initialize(username.getText(), name.getText(), a, c);
            } finally {
                wipe(a, b, c, d);
            }
        }, () -> {
            pw.setText("");
            confirm.setText("");
            master.setText("");
            masterConfirm.setText("");
        });
    }

    public static boolean changePassword(Component owner) {
        JPanel fields = column();
        JPasswordField old = password(), next = password(), confirm = password();
        add(fields, "Current password", old);
        add(fields, "New password", next);
        add(fields, "Confirm new password", confirm);
        return form(owner, "Change your password", fields, () -> {
            char[] a = old.getPassword(), b = next.getPassword(), c = confirm.getPassword();
            try {
                if (!Arrays.equals(b, c))
                    throw new IllegalArgumentException("Confirmation does not match.");
                LocalAuth.changePassword(a, b);
            } finally {
                wipe(a, b, c);
            }
        }, () -> {
            old.setText("");
            next.setText("");
            confirm.setText("");
        });
    }

    public static void changeMaster(Component owner) {
        JPanel fields = column();
        JPasswordField old = password(), next = password(), confirm = password();
        add(fields, "Current master key", old);
        add(fields, "New master key", next);
        add(fields, "Confirm master key", confirm);
        form(owner, "Change master key", fields, () -> {
            char[] a = old.getPassword(), b = next.getPassword(), c = confirm.getPassword();
            try {
                if (!Arrays.equals(b, c))
                    throw new IllegalArgumentException("Confirmation does not match.");
                LocalAuth.changeMasterKey(a, b);
            } finally {
                wipe(a, b, c);
            }
        }, () -> {
            old.setText("");
            next.setText("");
            confirm.setText("");
        });
    }

    public static void wipe(char[]... secrets) {
        for (char[] s : secrets)
            Arrays.fill(s, '\0');
    }

    private AuthDialogs() {
    }
}
