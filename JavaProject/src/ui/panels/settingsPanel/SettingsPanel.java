package ui.panels.settingsPanel;

import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

public class SettingsPanel extends PageLayout {
    private final JTextField store = field("Store name"), phone = field("Store phone"), rate = field("Tax percentage");
    private final JTextArea address = new JTextArea(3, 30), footer = new JTextArea(3, 30);
    private final JComboBox<String> accent = combo(new String[] { "Teal", "Blue", "Violet" });
    private final JCheckBox compact = new JCheckBox("Use compact catalog cards"),
            list = new JCheckBox("Open the catalog in list view"), tax = new JCheckBox("Add tax to the subtotal"),
            gcash = new JCheckBox("Record verified GCash payments");
    private final JLabel status = label("Changes apply after saving.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY),
            previewName = label("", UIStyle.BRAND, UIStyle.TEXT_PRIMARY);
    private final JButton save = button("Save changes", AppIcon.Name.CHECK, true),
            reset = button("Restore defaults", null, false);
    private final JPanel sections = new JPanel(new CardLayout()), nav = column();
    private String active = "Appearance";
    private final Surface settingsFooter = new Surface();

    public SettingsPanel() {
        super("WORKSPACE SETTINGS", "Your store, your way.", "Fine-tune the details that make this workspace yours.");
        for (JTextArea area : new JTextArea[] { address, footer }) {
            area.setFont(UIStyle.REGULAR);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(new EmptyBorder(10, 10, 10, 10));
        }
        address.getAccessibleContext().setAccessibleName("Store address");
        footer.getAccessibleContext().setAccessibleName("Receipt footer");
        nav.setPreferredSize(new Dimension(190, 200));
        sections.setOpaque(false);
        boolean owner = security.LocalAuth.allowed(security.LocalAuth.Permission.SETTINGS);
        if (!owner)
            active = "Receipt printer";
        if (owner) {
            sections.add(appearance(), "Appearance");
            sections.add(branding(), "Store & receipt");
            sections.add(pricing(), "Checkout");
            sections.add(new AccountsPanel(), "Accounts & access");
        }
        sections.add(new ReceiptPrinterPanel(), "Receipt printer");
        if (owner)
            sections.add(new ui.panels.DataToolsPanel(), "Data & backup");
        JPanel content = new JPanel(new BorderLayout(24, 0));
        content.setOpaque(false);
        content.add(nav, BorderLayout.WEST);
        content.add(sections, BorderLayout.CENTER);
        body.add(content, BorderLayout.CENTER);
        Surface bottom = settingsFooter;
        bottom.setBorder(new EmptyBorder(12, 16, 12, 16));
        JPanel controls = row(8);
        controls.add(reset);
        controls.add(save);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(controls, BorderLayout.EAST);
        body.add(bottom, BorderLayout.SOUTH);
        save.addActionListener(e -> save());
        reset.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Load the default settings into this form? Save to apply them.",
                    "Restore defaults", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                load(persistence.Repositories.settings().defaults());
                status.setText("Defaults loaded. Save changes to apply.");
            }
        });
        accent.addActionListener(e -> updatePreview());
        load(persistence.Repositories.settings().get());
        settingsFooter.setVisible(owner);
        ((CardLayout) sections.getLayout()).show(sections, active);
        navigation();
        if (persistence.Repositories.settings().loadError() != null) {
            status.setForeground(UIStyle.WARNING);
            status.setText("Saved settings need attention; defaults are shown.");
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int available = pageScroll.getViewport().getExtentSize().height;
        if (available > 0) {
            int height = Math.max(300,
                    available - (settingsFooter.isVisible() ? settingsFooter.getPreferredSize().height + 20 : 0));
            Dimension desired = new Dimension(1, height);
            if (!desired.equals(sections.getPreferredSize())) {
                sections.setPreferredSize(desired);
                body.revalidate();
            }
        }
    }

    private void navigation() {
        nav.removeAll();
        String[] names = security.LocalAuth.allowed(security.LocalAuth.Permission.SETTINGS)
                ? new String[] { "Appearance", "Store & receipt", "Receipt printer", "Checkout", "Accounts & access",
                        "Data & backup" }
                : new String[] { "Receipt printer" };
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            AppIcon.Name icon = switch (name) {
            case "Store & receipt" -> AppIcon.Name.STORE;
            case "Receipt printer" -> AppIcon.Name.PRINT;
            case "Checkout" -> AppIcon.Name.CARD;
            case "Accounts & access" -> AppIcon.Name.SHIELD;
            case "Data & backup" -> AppIcon.Name.DOWNLOAD;
            default -> AppIcon.Name.SETTINGS;
            };
            JButton b = button(name, icon, active.equals(name));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.addActionListener(e -> {
                active = name;
                settingsFooter.setVisible(!name.equals("Receipt printer") && !name.equals("Data & backup")
                        && !name.equals("Accounts & access"));
                ((CardLayout) sections.getLayout()).show(sections, name);
                navigation();
            });
            nav.add(b);
            nav.add(Box.createVerticalStrut(8));
        }
        nav.revalidate();
        nav.repaint();
    }

    private JPanel form(String title, String description) {
        Surface p = new Surface();
        p.setLayout(new BorderLayout(0, 22));
        JPanel header = column();
        header.add(label(title, UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        header.add(Box.createVerticalStrut(6));
        header.add(label(description, UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        p.add(header, BorderLayout.NORTH);
        return p;
    }

    private void addField(JPanel p, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        p.add(c);
        p.add(Box.createVerticalStrut(20));
    }

    private JPanel appearance() {
        JPanel card = form("Appearance", "Choose a look that feels right for your store.");
        JPanel fields = column();
        addField(fields, formField("Accent color", accent));
        compact.setOpaque(false);
        list.setOpaque(false);
        addField(fields, compact);
        addField(fields, list);
        JPanel preview = new JPanel(new BorderLayout(0, 12)) {
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(UIStyle.BACKGROUND);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g.dispose();
            }
        };
        preview.setOpaque(false);
        preview.setBorder(new EmptyBorder(20, 20, 20, 20));
        preview.setPreferredSize(new Dimension(300, 156));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 156));
        preview.add(previewName, BorderLayout.NORTH);
        preview.add(label("A little color. A clearer workspace.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY),
                BorderLayout.CENTER);
        JLabel button = label("Checkout", UIStyle.BOLD, Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setPreferredSize(new Dimension(120, 42));
        button.setOpaque(true);
        preview.add(button, BorderLayout.SOUTH);
        accent.addActionListener(e -> {
            button.setBackground(UIStyle.accent((String) accent.getSelectedItem()));
            preview.repaint();
        });
        addField(fields, preview);
        card.add(scroll(fields), BorderLayout.CENTER);
        return card;
    }

    private JPanel branding() {
        JPanel card = form("Store & receipt", "The details your customers see on every receipt.");
        JPanel fields = column();
        addField(fields, formField("Store name", store));
        addField(fields, formField("Address", new JScrollPane(address)));
        addField(fields, formField("Phone", phone));
        addField(fields, formField("Receipt message", new JScrollPane(footer)));
        addField(fields,
                label("Thermal printer and paper options: Receipt printer.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        card.add(scroll(fields), BorderLayout.CENTER);
        return card;
    }

    private JPanel pricing() {
        JPanel card = form("Checkout preferences", "Make payment collection fit your counter.");
        JPanel fields = column();
        tax.setOpaque(false);
        gcash.setOpaque(false);
        addField(fields, tax);
        addField(fields, formField("Tax rate (%)", rate));
        addField(fields,
                label("Tax is added to the subtotal of new checkouts.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        addField(fields, gcash);
        addField(fields,
                label("Cashiers verify GCash payment and enter its reference.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        card.add(scroll(fields), BorderLayout.CENTER);
        return card;
    }

    private void load(PosSettings.Values s) {
        store.setText(s.storeName());
        address.setText(s.address());
        phone.setText(s.phone());
        footer.setText(s.receiptFooter());
        accent.setSelectedItem(s.accent());
        compact.setSelected(s.compact());
        list.setSelected(s.listView());
        tax.setSelected(s.taxEnabled());
        rate.setText(s.taxRate().stripTrailingZeros().toPlainString());
        gcash.setSelected(s.gcashEnabled());

        updatePreview();
    }

    private void updatePreview() {
        previewName.setText(accent.getSelectedItem() + " collection");
        previewName.setForeground(UIStyle.accent((String) accent.getSelectedItem()));
        repaint();
    }

    private void save() {
        PosSettings.Values next;
        try {
            next = new PosSettings.Values(store.getText().trim(), address.getText().trim(), phone.getText().trim(),
                    footer.getText().trim(), (String) accent.getSelectedItem(), compact.isSelected(), list.isSelected(),
                    tax.isSelected(), new BigDecimal(rate.getText().trim()), gcash.isSelected(),
                    persistence.Repositories.settings().get().receiptWidth());
        } catch (IllegalArgumentException ex) {
            status.setForeground(UIStyle.DANGER);
            status.setText("Check the store name and tax rate (0–100).");
            return;
        }
        save.setEnabled(false);
        reset.setEnabled(false);
        status.setForeground(UIStyle.TEXT_SECONDARY);
        status.setText("Saving changes…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                persistence.Repositories.settings().save(next);
                return null;
            }

            protected void done() {
                save.setEnabled(true);
                reset.setEnabled(true);
                try {
                    get();
                    status.setForeground(UIStyle.SUCCESS);
                    status.setText("Changes saved.");
                } catch (Exception ex) {
                    status.setForeground(UIStyle.DANGER);
                    status.setText("Could not save. Previous settings are kept.");
                }
            }
        }.execute();
    }
}
