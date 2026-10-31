package ui.frames;

import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.*;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

public final class ProfileEditor extends JDialog {
    public ProfileEditor(Window owner, TeamStore.Member member, boolean useProfile, Runnable changed) {
        super(owner, member == null ? "Add team member" : "Edit profile", ModalityType.APPLICATION_MODAL);
        JTextField name = field("Full name"), contact = field("Contact number");
        JComboBox<String> role = combo(new String[] { "Owner", "Manager", "Cashier" });
        JCheckBox active = new JCheckBox("Active team member", member == null || member.active());
        active.setOpaque(false);
        String id = member == null ? UUID.randomUUID().toString() : member.id();
        boolean linked = false;
        try {
            linked = member != null
                    && security.LocalAuth.profiles().stream().anyMatch(a -> a.profileId().equals(member.id()));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        boolean ownerAccess = security.LocalAuth.allowed(security.LocalAuth.Permission.TEAM_EDIT);
        if (!ownerAccess && (member == null || !member.id().equals(security.LocalAuth.current().profileId())))
            throw new SecurityException("You can edit only your own profile.");
        role.setEnabled(ownerAccess && !linked);
        active.setEnabled(ownerAccess && !linked);

        String[] image = { member == null ? "" : member.image() };
        Path[] selected = { null };
        if (member != null) {
            name.setText(member.name());
            contact.setText(member.contact());
            role.setSelectedItem(member.role());
        }
        TeamStore.Member initial = member == null ? new TeamStore.Member(id, "New member", "", "Cashier", true)
                : member;
        Avatar avatar = new Avatar(initial, 112);
        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.setBackground(UIStyle.BACKGROUND);
        JPanel title = column();
        title.add(label(member == null ? "Meet your next team member" : "A profile that feels like you",
                UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        title.add(Box.createVerticalStrut(8));
        title.add(label("Your name and photo appear in the workspace sidebar.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        root.add(title, BorderLayout.NORTH);
        JPanel form = column();
        JPanel photoRow = row(20);
        photoRow.add(avatar);
        JPanel upload = column();
        JButton choose = button("Choose photo", AppIcon.Name.UPLOAD, false),
                remove = button("Remove photo", null, false);
        upload.add(choose);
        upload.add(Box.createVerticalStrut(8));
        upload.add(remove);
        photoRow.add(upload);
        addField(form, photoRow);
        addField(form, formField("Full name", name));
        addField(form, formField("Contact", contact));
        addField(form, formField("Role label", role));
        addField(form, active);
        root.add(scroll(form), BorderLayout.CENTER);
        JLabel status = label("PNG or JPEG · up to 12 MB", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
        JButton save = button("Save profile", AppIcon.Name.CHECK, true), cancel = button("Cancel", null, false);
        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.NORTH);
        JPanel actions = row(8);
        actions.add(save);
        actions.add(cancel);
        bottom.add(actions, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        choose.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setFileFilter(new FileNameExtensionFilter("PNG or JPEG", "png", "jpg", "jpeg"));
            if (picker.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            Path file = picker.getSelectedFile().toPath();
            save.setEnabled(false);
            choose.setEnabled(false);
            remove.setEnabled(false);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    persistence.Repositories.assets().validate(file);
                    return null;
                }

                protected void done() {
                    save.setEnabled(true);
                    choose.setEnabled(true);
                    remove.setEnabled(true);
                    try {
                        get();
                        selected[0] = file;
                        avatar.preview(file, name.getText().isBlank() ? "New member" : name.getText());
                        status.setText("Photo ready. Save the profile to apply it.");
                    } catch (Exception ex) {
                        status.setText("Choose a valid PNG/JPEG up to 12 MB and 20 megapixels.");
                    }
                }
            }.execute();
        });
        remove.addActionListener(e -> {
            image[0] = "";
            selected[0] = null;
            avatar.setPerson(new TeamStore.Member(id, name.getText().isBlank() ? "New member" : name.getText(), "",
                    (String) role.getSelectedItem(), true));
        });
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            TeamStore.Member updated;
            try {
                updated = new TeamStore.Member(id, name.getText().trim(), contact.getText().trim(),
                        (String) role.getSelectedItem(), active.isSelected(), image[0]);
                if (useProfile && !updated.active())
                    throw new IllegalArgumentException("Keep the current workspace profile active.");
            } catch (IllegalArgumentException ex) {
                status.setText(ex.getMessage());
                return;
            }
            save.setEnabled(false);
            choose.setEnabled(false);
            cancel.setEnabled(false);
            remove.setEnabled(false);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            Path sourcePhoto = selected[0];
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    TeamStore.Member profile = new TeamStore.Member(updated.id(), updated.name(), updated.contact(),
                            updated.role(), updated.active(), sourcePhoto == null ? updated.image()
                                    : persistence.Repositories.assets().importPhoto(sourcePhoto));
                    persistence.RepositoryProvider.Team store = persistence.Repositories.team();
                    java.util.List<TeamStore.Member> next = new ArrayList<>(store.read());
                    int index = -1;
                    for (int i = 0; i < next.size(); i++)
                        if (next.get(i).id().equals(id))
                            index = i;
                    if (index < 0)
                        next.add(profile);
                    else
                        next.set(index, profile);
                    store.save(next);
                    if (useProfile)
                        ActiveProfile.select(id);
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        changed.run();
                        dispose();
                    } catch (Exception ex) {
                        save.setEnabled(true);
                        choose.setEnabled(true);
                        cancel.setEnabled(true);
                        remove.setEnabled(true);
                        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        status.setText("Could not save this profile. Please try again.");
                    }
                }
            }.execute();
        });
        setContentPane(root);
        setSize(540, 700);
        setMinimumSize(new Dimension(500, 620));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addField(JPanel form, JComponent field) {
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        form.add(field);
        form.add(Box.createVerticalStrut(18));
    }
}
