package util.components;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import application.ReceiptPrinter;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * One preview/printing flow for confirmed checkout receipts and historical
 * reprints.
 */
public final class ReceiptView extends JPanel {
    public ReceiptView(String receipt, boolean reprint) {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        String printable = (reprint ? "REPRINT\n" : "") + receipt;
        JTextArea text = new JTextArea(printable);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        text.setBorder(new javax.swing.border.EmptyBorder(16, 16, 16, 16));
        text.setCaretPosition(0);
        add(scroll(text), BorderLayout.CENTER);
        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        JComboBox<Integer> paper = combo(new Integer[] { 58, 80 });
        try {
            paper.setSelectedItem(persistence.PrinterPreferences.load().paperWidth());
        } catch (Exception ex) {
            paper.setSelectedItem(persistence.Repositories.settings().get().receiptWidth());
        }
        paper.getAccessibleContext().setAccessibleName("Receipt paper width in millimetres");
        JButton print = button(reprint ? "Print reprint" : "Print receipt", AppIcon.Name.PRINT, true);
        controls.add(formField("Paper width (mm)", paper), BorderLayout.WEST);
        controls.add(print, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);
        JLabel printerLabel = label("Saved printer and margins are used when printing.", UIStyle.SMALL,
                UIStyle.TEXT_SECONDARY);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(controls, BorderLayout.NORTH);
        footer.add(printerLabel, BorderLayout.SOUTH);
        remove(controls);
        add(footer, BorderLayout.SOUTH);
        print.addActionListener(e -> {
            persistence.PrinterPreferences.Config config;
            try {
                config = persistence.PrinterPreferences.load().withWidth((Integer) paper.getSelectedItem());
            } catch (Exception ex) {
                printerLabel.setText("Review Receipt printer settings before printing.");
                return;
            }
            print.setEnabled(false);
            paper.setEnabled(false);
            printerLabel.setText("Preparing saved printer…");
            new SwingWorker<application.PrinterServiceManager.Prepared, Void>() {
                protected application.PrinterServiceManager.Prepared doInBackground() throws Exception {
                    return application.PrinterServiceManager.prepare(printable, config);
                }

                protected void done() {
                    try {
                        var prepared = get();
                        if (config.showDialog() && !prepared.job().printDialog()) {
                            print.setEnabled(true);
                            paper.setEnabled(true);
                            printerLabel.setText("Print cancelled. Order stays saved.");
                            return;
                        }
                        new SwingWorker<Void, Void>() {
                            protected Void doInBackground() throws Exception {
                                prepared.job().print();
                                return null;
                            }

                            protected void done() {
                                print.setEnabled(true);
                                paper.setEnabled(true);
                                try {
                                    get();
                                    printerLabel.setText("Print job sent.");
                                } catch (Exception ex) {
                                    printerLabel.setText("Printing failed. Retry without creating another sale.");
                                    printerLabel.setToolTipText(
                                            ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage());
                                }
                            }
                        }.execute();
                    } catch (Exception ex) {
                        print.setEnabled(true);
                        paper.setEnabled(true);
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        printerLabel.setText("Printer unavailable. Check Receipt printer settings.");
                        printerLabel.setToolTipText(cause.getMessage());
                    }
                }
            }.execute();
        });
    }

    public static void show(Component parent, String receipt, boolean reprint) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), reprint ? "Receipt reprint" : "Receipt",
                Dialog.ModalityType.APPLICATION_MODAL);
        ReceiptView view = new ReceiptView(receipt, reprint);
        view.setBorder(new javax.swing.border.EmptyBorder(18, 18, 18, 18));
        dialog.setContentPane(view);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(Math.min(520, screen.width - 40), Math.min(680, screen.height - 80));
        dialog.setMinimumSize(new Dimension(360, 360));
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
