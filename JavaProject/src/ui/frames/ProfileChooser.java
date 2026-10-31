package ui.frames;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.*;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

public final class ProfileChooser extends JDialog {
    public ProfileChooser(Window owner, Runnable changed) {
        super(owner, "Workspace profile", ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(UIStyle.BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        JPanel heading = column();
        heading.add(label("Who's at the counter?", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        heading.add(Box.createVerticalStrut(8));
        heading.add(label("Choose a profile or update your name and photo.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        root.add(heading, BorderLayout.NORTH);
        DefaultListModel<TeamStore.Member> model = new DefaultListModel<>();
        try {
            for (var member : persistence.Repositories.team().read())
                if (member.active())
                    model.addElement(member);
        } catch (Exception ignored) {
        }
        TeamStore.Member current = ActiveProfile.current();
        if (model.isEmpty())
            model.addElement(current);
        JList<TeamStore.Member> list = new JList<>(model);
        list.setFixedCellHeight(72);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        java.util.Map<String, Avatar> portraits = new java.util.HashMap<>();
        list.setCellRenderer((view, member, index, selected, focus) -> {
            JLabel cell = label(member.name() + "  ·  " + member.role(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
            cell.setIcon(portraits.computeIfAbsent(member.id() + member.image(),
                    key -> new Avatar(member, 42).onLoaded(list::repaint)));
            cell.setIconTextGap(14);
            cell.setOpaque(true);
            cell.setBackground(selected ? UIStyle.tint() : Color.WHITE);
            cell.setBorder(new EmptyBorder(10, 14, 10, 14));
            return cell;
        });
        list.setSelectedIndex(0);
        root.add(scroll(list), BorderLayout.CENTER);
        JLabel status = label("Profile switching does not verify passwords.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.NORTH);
        JPanel actions = row(8);
        JButton use = button("Use profile", AppIcon.Name.CHECK, true),
                edit = button("Edit my profile", AppIcon.Name.EDIT, false);
        actions.add(use);
        actions.add(edit);
        bottom.add(actions, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);
        edit.addActionListener(e -> {
            new ProfileEditor(this, ActiveProfile.current(), true, () -> {
                changed.run();
                dispose();
            }).setVisible(true);
        });
        use.addActionListener(e -> {
            var selected = list.getSelectedValue();
            if (selected == null)
                return;
            if (selected.id().equals("local-admin")) {
                new ProfileEditor(this, selected, true, () -> {
                    changed.run();
                    dispose();
                }).setVisible(true);
                return;
            }
            use.setEnabled(false);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    ActiveProfile.select(selected.id());
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        changed.run();
                        dispose();
                    } catch (Exception ex) {
                        use.setEnabled(true);
                        status.setText("This profile could not be selected. Please try again.");
                    }
                }
            }.execute();
        });
        setContentPane(root);
        setSize(520, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
