package ui.panels.settingsPanel;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import security.LocalAuth;
import ui.frames.AuthDialogs;
import util.config.UIStyle;
import static util.components.ModernUI.*;

public final class AccountsPanel extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(new String[] { "Username", "Name", "Role", "Status" },
            0) {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<LocalAuth.Account> accounts = List.of();
    private final JLabel status = label("Each team member signs in with their own password.", UIStyle.SMALL,
            UIStyle.TEXT_SECONDARY);

    public AccountsPanel() {
        super(new BorderLayout(0, 18));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        LocalAuth.require(LocalAuth.Permission.ACCOUNTS);
        JPanel header = column();
        header.add(label("Accounts & access", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        header.add(Box.createVerticalStrut(8));
        header.add(status);
        add(header, BorderLayout.NORTH);
        util.components.PageLayout.styleTable(table);
        table.setRowHeight(46);
        table.setFont(UIStyle.REGULAR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(scroll(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 2, 8, 8));
        actions.setOpaque(false);
        JButton create = button("Add account", null, true), edit = button("Role & status", null, false),
                reset = button("Reset password", null, false), master = button("Master key", null, false);
        for (JButton b : new JButton[] { create, edit, reset, master })
            actions.add(b);
        add(actions, BorderLayout.SOUTH);
        create.addActionListener(e -> create());
        edit.addActionListener(e -> edit());
        reset.addActionListener(e -> reset());
        master.addActionListener(e -> AuthDialogs.changeMaster(this));
        refresh();
    }

    private void refresh() {
        try {
            accounts = LocalAuth.accounts();
            var profiles = new model.TeamStore().read().stream()
                    .collect(java.util.stream.Collectors.toMap(member -> member.id(), member -> member.name()));
            model.setRowCount(0);
            for (var a : accounts)
                model.addRow(new Object[] { a.username(), profiles.getOrDefault(a.profileId(), a.name()), a.role(),
                        !a.active() ? "Disabled" : a.mustChangePassword() ? "Change password" : "Active" });
        } catch (Exception ex) {
            status.setText(ex.getMessage());
        }
    }

    private LocalAuth.Account selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            status.setText("Select an account first.");
            return null;
        }
        return accounts.get(table.convertRowIndexToModel(row));
    }

    private void create() {
        JPanel fields = column();
        JTextField username = field("Username"), name = field("Full name");
        JComboBox<String> role = combo(new String[] { "Cashier", "Manager", "Owner" });
        JPasswordField pw = AuthDialogs.password();
        AuthDialogs.add(fields, "Username", username);
        AuthDialogs.add(fields, "Display name", name);
        AuthDialogs.add(fields, "Role", role);
        AuthDialogs.add(fields, "Temporary password", pw);
        if (AuthDialogs.form(this, "Create account", fields, () -> {
            char[] p = pw.getPassword();
            try {
                LocalAuth.createAccount(username.getText(), name.getText(), (String) role.getSelectedItem(), p);
            } finally {
                AuthDialogs.wipe(p);
            }
        }, () -> pw.setText("")))
            refresh();
    }

    private void edit() {
        var a = selected();
        if (a == null)
            return;
        JPanel fields = column();
        JComboBox<String> role = combo(new String[] { "Owner", "Manager", "Cashier" });
        role.setSelectedItem(a.role());
        JCheckBox active = new JCheckBox("Account enabled", a.active());
        active.setOpaque(false);
        AuthDialogs.add(fields, "Role", role);
        fields.add(active);
        if (AuthDialogs.form(this, "Access for " + a.username(), fields,
                () -> LocalAuth.updateAccount(a.id(), (String) role.getSelectedItem(), active.isSelected()), () -> {
                }))
            refresh();
    }

    private void reset() {
        var a = selected();
        if (a == null)
            return;
        JPanel fields = column();
        JPasswordField pw = AuthDialogs.password();
        AuthDialogs.add(fields, "New temporary password", pw);
        if (AuthDialogs.form(this, "Reset password: " + a.username(), fields, () -> {
            char[] p = pw.getPassword();
            try {
                LocalAuth.resetPassword(a.id(), p);
            } finally {
                AuthDialogs.wipe(p);
            }
        }, () -> pw.setText("")))
            refresh();
    }
}
