package ui.panels;

import java.awt.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import application.*;
import persistence.DataLocation;
import util.components.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * All disk work runs outside the event thread; restores always create a
 * separate copy.
 */
public final class DataToolsPanel extends ModernUI.Surface {
    private final JTextArea output = new JTextArea(14, 30);
    private final JButton audit = button("Run data audit", AppIcon.Name.SEARCH, false),
            backup = button("Export backup", AppIcon.Name.DOWNLOAD, true),
            verify = button("Verify backup", AppIcon.Name.CHECK, false),
            restore = button("Restore a copy", AppIcon.Name.UPLOAD, false);

    public DataToolsPanel() {
        setLayout(new BorderLayout(0, 18));
        JPanel heading = column();
        heading.add(label("Data & recovery", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        heading.add(Box.createVerticalStrut(8));
        JTextArea location = new JTextArea("Active data folder\n" + DataLocation.root()
                + "\n\nExport includes source files, photos, migration CSVs and checksums. Restore creates a separate folder; it never replaces live data.");
        location.setFont(UIStyle.SMALL);
        location.setLineWrap(true);
        location.setWrapStyleWord(true);
        location.setEditable(false);
        location.setOpaque(false);
        location.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        heading.add(location);
        add(heading, BorderLayout.NORTH);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setText("Run the audit to review data quality before migration. No Supabase import is performed here.");
        output.setName("dataAuditOutput");
        add(scroll(output), BorderLayout.CENTER);
        JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));
        for (JButton b : new JButton[] { audit, backup, verify, restore })
            controls.add(b);
        add(controls, BorderLayout.SOUTH);
        audit.addActionListener(e -> run(() -> MigrationAudit.scan().text()));
        backup.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setSelectedFile(new java.io.File("laundry-backup-" + stamp() + ".zip"));
            if (picker.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                Path path = picker.getSelectedFile().toPath();
                run(() -> "Verified backup saved:\n" + BackupService.create(path)
                        + "\n\nContains private business data. Keep a separate trusted copy.");
            }
        });
        verify.addActionListener(e -> {
            Path file = chooseArchive();
            if (file != null)
                run(() -> {
                    var result = BackupService.verify(file);
                    return "Backup verified: " + result.files() + " files, " + result.bytes()
                            + " bytes.\nChecksums and paths passed.\n" + file;
                });
        });
        restore.addActionListener(e -> {
            Path file = chooseArchive();
            if (file == null)
                return;
            JFileChooser folder = new JFileChooser();
            folder.setDialogTitle("Choose parent folder for a separate restored copy");
            folder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (folder.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            Path destination = folder.getSelectedFile().toPath().resolve("laundry-restored-" + stamp());
            run(() -> "Restored and verified a separate copy:\n" + BackupService.restoreCopy(file, destination)
                    + "\n\nLive data remains in:\n" + DataLocation.root()
                    + "\nReview the restored data and audit before any cutover.");
        });
    }

    private String stamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private Path chooseArchive() {
        JFileChooser picker = new JFileChooser();
        picker.setDialogTitle("Choose a laundry backup ZIP");
        return picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? picker.getSelectedFile().toPath() : null;
    }

    private interface Task {
        String run() throws Exception;
    }

    private void run(Task task) {
        for (JButton b : new JButton[] { audit, backup, verify, restore })
            b.setEnabled(false);
        output.setText("Working…");
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                return task.run();
            }

            protected void done() {
                for (JButton b : new JButton[] { audit, backup, verify, restore })
                    b.setEnabled(true);
                try {
                    output.setText(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    output.setText("Could not complete this operation:\n" + cause.getMessage()
                            + "\n\nLive data was not replaced.");
                }
                output.setCaretPosition(0);
            }
        }.execute();
    }
}
