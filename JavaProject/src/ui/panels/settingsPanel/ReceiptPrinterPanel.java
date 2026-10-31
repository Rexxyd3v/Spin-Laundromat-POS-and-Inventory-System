package ui.panels.settingsPanel;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import application.*;
import persistence.PrinterPreferences;
import util.components.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * Printer discovery uses Windows-installed queues; it never sends a job until
 * the user clicks Print.
 */
public final class ReceiptPrinterPanel extends ModernUI.Surface {
    private final Supplier<List<PrinterServiceManager.Device>> discovery;

    private record Choice(String name, String label) {
        public String toString() {
            return label;
        }
    }

    private final JTextField search = field("Find installed printer");
    private final JComboBox<Choice> printers = combo(new Choice[] { new Choice("", "Use Windows default printer") });
    private final JComboBox<Integer> paper = combo(new Integer[] { 58, 80 });
    private final JSpinner left = decimal(3, 12), right = decimal(3, 12), top = decimal(3, 12), bottom = decimal(3, 12),
            feed = decimal(6, 30), font = new JSpinner(new SpinnerNumberModel(9, 6, 14, 1));
    private final JCheckBox dialog = new JCheckBox("Show the print dialog before sending a receipt");
    private final JButton refresh = button("Find printers", AppIcon.Name.REFRESH, false),
            save = button("Save printer", AppIcon.Name.CHECK, true),
            test = button("Test print", AppIcon.Name.PRINT, false);
    private final JLabel deviceStatus = label("Looking for installed printers…", UIStyle.SMALL, UIStyle.TEXT_SECONDARY),
            status = label("Settings apply to this PC.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final Preview preview = new Preview();
    private List<PrinterServiceManager.Device> devices = List.of();
    private String selectedName = "";
    private boolean scanning, busy, rebuilding;
    private long scanGeneration;

    public ReceiptPrinterPanel() {
        this(PrinterServiceManager::discover);
    }

    public ReceiptPrinterPanel(Supplier<List<PrinterServiceManager.Device>> discovery) {
        for (JSpinner spinner : spinners())
            styleSpinner(spinner);
        this.discovery = Objects.requireNonNull(discovery);
        setName("receiptPrinterSettings");
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel heading = column();
        heading.add(label("Thermal receipt printer", UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        heading.add(Box.createVerticalStrut(6));
        heading.add(wrapped(
                "Connect your USB or network printer and install its Windows driver. Find printers lists the installed print queues.",
                2));
        add(heading, BorderLayout.NORTH);
        JPanel fields = column();
        search.setName("printerSearch");
        printers.setName("receiptPrinterChoice");
        paper.setName("thermalPaperWidth");
        left.setName("printerMarginLeft");
        right.setName("printerMarginRight");
        top.setName("printerMarginTop");
        bottom.setName("printerMarginBottom");
        feed.setName("printerExtraFeed");
        font.setName("printerFontSize");
        status.setName("printerSettingsStatus");
        JPanel finder = new JPanel(new BorderLayout(10, 0));
        finder.setOpaque(false);
        finder.add(search, BorderLayout.CENTER);
        finder.add(refresh, BorderLayout.EAST);
        addField(fields, formField("Find an installed printer", finder));
        addField(fields, formField("Receipt printer", printers));
        addField(fields, deviceStatus);
        JPanel sizing = new JPanel(new GridLayout(1, 2, 12, 0));
        sizing.setOpaque(false);
        sizing.add(formField("Paper width (mm)", paper));
        sizing.add(formField("Text size (pt)", font));
        addField(fields, sizing);
        JPanel margins = new JPanel(new GridLayout(2, 2, 12, 14));
        margins.setOpaque(false);
        margins.add(formField("Left margin (mm)", left));
        margins.add(formField("Right margin (mm)", right));
        margins.add(formField("Top margin (mm)", top));
        margins.add(formField("Bottom margin (mm)", bottom));
        addField(fields, margins);
        addField(fields, formField("Extra paper after receipt (mm)", feed));
        dialog.setOpaque(false);
        dialog.setFont(UIStyle.REGULAR);
        addField(fields, dialog);
        addField(fields, wrapped(
                "Paper length follows receipt content. Very long receipts paginate. Confirm the driver paper size and margins with a test print; cutter and cash-drawer controls depend on the printer model.",
                3));
        preview.setName("thermalReceiptPreview");
        preview.setPreferredSize(new Dimension(300, 280));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        preview.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(preview);
        fields.add(Box.createVerticalStrut(12));
        add(scroll(fields), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(status, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(test);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        try {
            load(PrinterPreferences.load());
        } catch (Exception ex) {
            load(PrinterPreferences.defaults(persistence.Repositories.settings().get().receiptWidth()));
            status.setForeground(UIStyle.WARNING);
            status.setText("Saved printer settings need review. Defaults shown; save to replace them.");
        }
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        printers.addActionListener(e -> {
            if (!rebuilding && printers.getSelectedItem() instanceof Choice c) {
                selectedName = c.name();
                showDeviceStatus();
                changed();
            }
        });
        refresh.addActionListener(e -> scan());
        save.addActionListener(e -> save());
        test.addActionListener(e -> test());
        paper.addActionListener(e -> {
            preview.repaint();
            changed();
        });
        dialog.addActionListener(e -> changed());
        for (JSpinner spinner : spinners())
            spinner.addChangeListener(e -> {
                preview.repaint();
                changed();
            });
        scan();
    }

    private void changed() {
        status.setForeground(UIStyle.TEXT_SECONDARY);
        status.setText("Unsaved printer changes. Save to use them for receipts.");
    }

    private static void styleSpinner(JSpinner spinner) {
        spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            private JButton step(AppIcon.Name icon) {
                JButton b = new JButton(new AppIcon(icon, 12, UIStyle.TEXT_SECONDARY));
                b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
                b.setBorder(null);
                b.setContentAreaFilled(false);
                b.setPreferredSize(new Dimension(28, 20));
                b.setFocusable(false);
                return b;
            }

            protected Component createNextButton() {
                JButton button = step(AppIcon.Name.PLUS);
                installNextButtonListeners(button);
                return button;
            }

            protected Component createPreviousButton() {
                JButton button = step(AppIcon.Name.MINUS);
                installPreviousButtonListeners(button);
                return button;
            }
        });
        spinner.setFont(UIStyle.REGULAR);
        spinner.setBackground(Color.WHITE);
        spinner.setBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER));
        spinner.setPreferredSize(new Dimension(150, 42));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.setOpaque(false);
            var input = editor.getTextField();
            input.setUI(new javax.swing.plaf.basic.BasicFormattedTextFieldUI());
            input.setBorder(new EmptyBorder(8, 12, 8, 4));
            input.setFont(UIStyle.REGULAR);
            input.setHorizontalAlignment(JTextField.LEFT);
            input.setOpaque(false);
        }
    }

    private static JSpinner decimal(double initial, double max) {
        return new JSpinner(new SpinnerNumberModel(initial, 0.0, max, 0.5));
    }

    private static JTextArea wrapped(String text, int rows) {
        JTextArea label = new JTextArea(text, rows, 20);
        label.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        label.setFont(UIStyle.SMALL);
        label.setForeground(UIStyle.TEXT_SECONDARY);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setEditable(false);
        label.setFocusable(false);
        label.setOpaque(false);
        return label;
    }

    private void addField(JPanel target, JComponent value) {
        value.setAlignmentX(LEFT_ALIGNMENT);
        value.setMaximumSize(new Dimension(Integer.MAX_VALUE, value.getPreferredSize().height));
        target.add(value);
        target.add(Box.createVerticalStrut(16));
    }

    private JSpinner[] spinners() {
        return new JSpinner[] { left, right, top, bottom, feed, font };
    }

    private double number(JSpinner spinner) {
        return ((Number) spinner.getValue()).doubleValue();
    }

    private PrinterPreferences.Config read(boolean commit) {
        if (commit)
            for (var spinner : spinners())
                try {
                    spinner.commitEdit();
                } catch (java.text.ParseException ex) {
                    throw new IllegalArgumentException("Enter valid margin, feed and text-size values.");
                }
        return new PrinterPreferences.Config(selectedName, (Integer) paper.getSelectedItem(), number(left),
                number(right), number(top), number(bottom), number(feed), ((Number) font.getValue()).intValue(),
                dialog.isSelected());
    }

    private void load(PrinterPreferences.Config config) {
        selectedName = config.printerName();
        paper.setSelectedItem(config.paperWidth());
        left.setValue(config.leftMm());
        right.setValue(config.rightMm());
        top.setValue(config.topMm());
        bottom.setValue(config.bottomMm());
        feed.setValue(config.feedMm());
        font.setValue(config.fontSize());
        dialog.setSelected(config.showDialog());
        filter();
        preview.repaint();
    }

    public boolean isScanning() {
        return scanning;
    }

    public void scan() {
        long request = ++scanGeneration;
        scanning = true;
        refresh.setEnabled(false);
        deviceStatus.setText("Searching installed Windows printers…");
        new SwingWorker<List<PrinterServiceManager.Device>, Void>() {
            protected List<PrinterServiceManager.Device> doInBackground() {
                return discovery.get();
            }

            protected void done() {
                if (request != scanGeneration)
                    return;
                scanning = false;
                refresh.setEnabled(!busy);
                try {
                    devices = get();
                    filter();
                } catch (Exception ex) {
                    devices = List.of();
                    filter();
                    deviceStatus.setText("Printer discovery failed. Check Windows printer setup, then retry.");
                }
            }
        }.execute();
    }

    private void filter() {
        String term = search.getText().trim().toLowerCase(Locale.ROOT);
        rebuilding = true;
        try {
            printers.removeAllItems();
            Choice chosen = new Choice("", "Use Windows default printer");
            printers.addItem(chosen);
            for (var device : devices) {
                if (!device.name().toLowerCase(Locale.ROOT).contains(term) && !device.name().equals(selectedName))
                    continue;
                Choice c = new Choice(device.name(),
                        device.name() + (device.systemDefault() ? " (Windows default)" : ""));
                printers.addItem(c);
                if (device.name().equals(selectedName))
                    chosen = c;
            }
            if (!selectedName.isBlank() && chosen.name().isBlank()) {
                chosen = new Choice(selectedName, selectedName + " — unavailable");
                printers.addItem(chosen);
            }
            printers.setSelectedItem(chosen);
        } finally {
            rebuilding = false;
        }
        showDeviceStatus();
    }

    private void showDeviceStatus() {
        if (scanning)
            return;
        var selected = devices.stream()
                .filter(d -> selectedName.isBlank() ? d.systemDefault() : d.name().equals(selectedName)).findFirst();
        deviceStatus.setText(selected.map(d -> d.status())
                .orElse(devices.isEmpty() ? "No printer found. Install/connect it in Windows, then Find printers."
                        : selectedName.isBlank() ? "No compatible Windows default was found. Select a printer above."
                                : "Saved printer is unavailable. Reconnect it or select another queue."));
        deviceStatus.setToolTipText(deviceStatus.getText());
    }

    private void busy(boolean value) {
        busy = value;
        save.setEnabled(!value);
        test.setEnabled(!value);
        refresh.setEnabled(!value && !scanning);
        printers.setEnabled(!value);
        paper.setEnabled(!value);
        search.setEnabled(!value);
        dialog.setEnabled(!value);
        for (var spinner : spinners())
            spinner.setEnabled(!value);
    }

    private void error(Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        status.setForeground(UIStyle.DANGER);
        status.setText(cause.getMessage() == null ? "Printer operation failed." : cause.getMessage());
        status.setToolTipText(status.getText());
    }

    private void save() {
        PrinterPreferences.Config config;
        try {
            config = read(true);
        } catch (Exception ex) {
            error(ex);
            return;
        }
        busy(true);
        status.setText("Saving printer settings…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                PrinterPreferences.save(config);
                return null;
            }

            protected void done() {
                busy(false);
                try {
                    get();
                    status.setForeground(UIStyle.SUCCESS);
                    status.setText("Printer settings saved for this PC.");
                } catch (Exception ex) {
                    error(ex);
                }
            }
        }.execute();
    }

    private void test() {
        PrinterPreferences.Config config;
        try {
            config = read(true);
        } catch (Exception ex) {
            error(ex);
            return;
        }
        busy(true);
        status.setForeground(UIStyle.TEXT_SECONDARY);
        status.setText("Preparing test receipt…");
        new SwingWorker<PrinterServiceManager.Prepared, Void>() {
            protected PrinterServiceManager.Prepared doInBackground() throws Exception {
                return PrinterServiceManager.prepare(sample(), config);
            }

            protected void done() {
                try {
                    var prepared = get();
                    if (!prepared.job().printDialog()) {
                        busy(false);
                        status.setText("Test print cancelled.");
                        return;
                    }
                    new SwingWorker<Void, Void>() {
                        protected Void doInBackground() throws Exception {
                            prepared.job().print();
                            return null;
                        }

                        protected void done() {
                            busy(false);
                            try {
                                get();
                                status.setForeground(UIStyle.SUCCESS);
                                status.setText("Test job sent. Check the paper output.");
                            } catch (Exception ex) {
                                error(ex);
                            }
                        }
                    }.execute();
                } catch (Exception ex) {
                    busy(false);
                    error(ex);
                }
            }
        }.execute();
    }

    public static String sample() {
        return "SPIN LAUNDROMAT\nTHERMAL PRINTER TEST\n--------------------------\nWash, dry & fold\n3 kg x PHP 25.00\nSubtotal       PHP 75.00\nTax             PHP 9.00\nTOTAL          PHP 84.00\n--------------------------\nAlignment: 1234567890\nPeso symbol: ₱84.00\nLong text should wrap cleanly\ninside the configured margins.\n\nThis is a test, not a sale.\nThank you!";
    }

    private final class Preview extends JComponent {
        Preview() {
            setToolTipText("Preview of the configured receipt. The installed driver may adjust the final print area.");
        }

        protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D) source.create();
            try {
                g.setColor(UIStyle.BACKGROUND);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                var document = new ReceiptPrinter(sample(), read(false));
                var page = document.pageFormat();
                double scale = Math.min((getWidth() - 28) / page.getWidth(), (getHeight() - 28) / page.getHeight());
                double x = (getWidth() - page.getWidth() * scale) / 2, y = 14;
                g.translate(x, y);
                g.scale(scale, scale);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, (int) Math.ceil(page.getWidth()), (int) Math.ceil(page.getHeight()));
                document.print(g, page, 0);
            } catch (Exception ex) {
                g.setColor(UIStyle.TEXT_SECONDARY);
                g.setFont(UIStyle.SMALL);
                g.drawString("Check layout values to preview the receipt.", 14, 28);
            } finally {
                g.dispose();
            }
        }
    }
}
