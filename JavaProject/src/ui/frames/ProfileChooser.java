package ui.frames;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.ActiveProfile;
import security.LocalAuth;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

/**
 * Displays only the authenticated identity. Switching accounts requires a fresh
 * sign-in.
 */
public final class ProfileChooser extends JDialog {
    public ProfileChooser(Window owner, Runnable changed) {
        super(owner, "Your account", ModalityType.APPLICATION_MODAL);
        var account = LocalAuth.current();
        var person = ActiveProfile.current();
        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(UIStyle.BACKGROUND);
        JLabel portrait = label(person.name(), UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY);
        portrait.setIcon(new Avatar(person, 64));
        portrait.setIconTextGap(16);
        root.add(portrait, BorderLayout.NORTH);
        JPanel details = column();
        details.add(label(account.username() + " · " + account.role(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
        details.add(Box.createVerticalStrut(12));
        details.add(label("Sign out to use a different account.", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        root.add(details, BorderLayout.CENTER);
        JPanel actions = row(8);
        JButton edit = button("Edit profile", AppIcon.Name.EDIT, false),
                password = button("Change password", AppIcon.Name.SHIELD, true);
        edit.addActionListener(e -> new ProfileEditor(this, ActiveProfile.current(), true, () -> {
            changed.run();
            dispose();
        }).setVisible(true));
        password.addActionListener(e -> {
            if (AuthDialogs.changePassword(this))
                dispose();
        });
        actions.add(edit);
        actions.add(password);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(520, 310);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
