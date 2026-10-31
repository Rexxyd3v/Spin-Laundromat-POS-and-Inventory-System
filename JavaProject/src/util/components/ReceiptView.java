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
        paper.setSelectedItem(persistence.Repositories.settings().get().receiptWidth());
        paper.getAccessibleContext().setAccessibleName("Receipt paper width in millimetres");
        JButton print = button(reprint ? "Print reprint" : "Print receipt", AppIcon.Name.PRINT, true);
        controls.add(formField("Paper width (mm)", paper), BorderLayout.WEST);
        controls.add(print, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);
        print.addActionListener(e -> {
            try {
                ReceiptPrinter document = new ReceiptPrinter(printable, (Integer) paper.getSelectedItem());
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Laundry receipt");
                job.setPrintable(document, document.pageFormat());
                if (!job.printDialog())
                    return;
                print.setEnabled(false);
                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() throws Exception {
                        job.print();
                        return null;
                    }

                    protected void done() {
                        print.setEnabled(true);
                        try {
                            get();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ReceiptView.this,
                                    "Printing failed. The order stays saved; retry printing without creating another sale.",
                                    "Print receipt", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No printer is available. Your order remains saved.",
                        "Print receipt", JOptionPane.ERROR_MESSAGE);
            }
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
