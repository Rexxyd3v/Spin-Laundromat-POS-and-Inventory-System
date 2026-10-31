package ui.panels.posPanel;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.*;
import util.components.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

public final class CatalogEditor extends JDialog {
    private final JTextField name = field("Item name"), description = field("Description"),
            category = field("Category"), price = field("Price per unit");
    private final JComboBox<CatalogItem.Unit> unit = combo(CatalogItem.Unit.values());
    private final JComboBox<String> type = combo(new String[] { "Service", "Product" });
    private final JTextArea status = new JTextArea(2, 0);
    private final ItemPhoto preview;
    private Path selected;
    private String image;

    public CatalogEditor(Window owner, persistence.RepositoryProvider.Catalog store, CatalogItem item,
            Runnable changed) {
        super(owner, item == null ? "New catalog item" : "Edit catalog item", ModalityType.APPLICATION_MODAL);
        image = item == null ? "" : item.image();
        preview = item == null ? new ItemPhoto(false) : new ItemPhoto(item);
        name.setName("catalogName");
        price.setName("catalogPrice");
        unit.setName("catalogUnit");
        status.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        status.setBorder(null);
        status.setFont(UIStyle.SMALL);
        status.setForeground(UIStyle.DANGER);
        status.setOpaque(false);
        status.setEditable(false);
        status.setFocusable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setVisible(false);
        status.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void refresh() {
                status.setVisible(!status.getText().isEmpty());
                if (status.getParent() != null)
                    status.getParent().revalidate();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                refresh();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                refresh();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                refresh();
            }
        });
        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(UIStyle.BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel titles = column();
        titles.add(label(item == null ? "Create something new" : "Edit catalog item", UIStyle.TITLE,
                UIStyle.TEXT_PRIMARY));
        titles.add(Box.createVerticalStrut(6));
        titles.add(label("<html>A clear photo helps your cashier find the right item.</html>", UIStyle.REGULAR,
                UIStyle.TEXT_SECONDARY));
        root.add(titles, BorderLayout.NORTH);
        JPanel left = column();
        preview.setPreferredSize(new Dimension(240, 200));
        preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        preview.setAlignmentX(LEFT_ALIGNMENT);
        left.add(preview);
        left.add(Box.createVerticalStrut(14));
        JButton choose = button("Choose image", AppIcon.Name.UPLOAD, false),
                remove = button("Remove image", AppIcon.Name.TRASH, false);
        choose.setAlignmentX(LEFT_ALIGNMENT);
        remove.setAlignmentX(LEFT_ALIGNMENT);
        left.add(choose);
        left.add(Box.createVerticalStrut(8));
        left.add(remove);
        left.add(Box.createVerticalStrut(12));
        left.add(label("PNG or JPEG · up to 12 MB", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        left.setPreferredSize(new Dimension(220, left.getPreferredSize().height));
        JPanel form = column();
        addField(form, formField("Item name", name));
        addField(form, formField("Description", description));
        JPanel types = new JPanel(new GridLayout(1, 2, 12, 0));
        types.setOpaque(false);
        types.add(formField("Type", type));
        types.add(formField("Category", category));
        addField(form, types);
        JPanel pricing = new JPanel(new GridLayout(1, 2, 12, 0));
        pricing.setOpaque(false);
        pricing.add(formField("Price per unit", price));
        pricing.add(formField("Unit", unit));
        addField(form, pricing);
        JScrollPane scroll = scroll(new EditorBody(left, form, types, pricing));
        scroll.setName("catalogFormScroll");
        scroll.getViewport().setBackground(UIStyle.BACKGROUND);
        scroll.setMinimumSize(new Dimension(0, 0));
        root.add(scroll, BorderLayout.CENTER);
        if (item != null) {
            name.setText(item.name());
            description.setText(item.description());
            category.setText(item.category());
            price.setText(item.price().toPlainString());
            unit.setSelectedItem(item.unit());
            type.setSelectedIndex(item.product() ? 1 : 0);
        } else {
            category.setText("Laundry");
            price.setText("0.00");
        }
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(status, BorderLayout.NORTH);
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.setName("catalogActions");
        JButton save = button("Save item", AppIcon.Name.CHECK, true), cancel = button("Cancel", null, false);
        save.setName("catalogSave");
        actions.add(save);
        actions.add(cancel);
        footer.add(actions, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        choose.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setDialogTitle("Choose a service or product image");
            picker.setFileFilter(new FileNameExtensionFilter("PNG and JPEG images", "png", "jpg", "jpeg"));
            if (picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Path path = picker.getSelectedFile().toPath();
                choose.setEnabled(false);
                remove.setEnabled(false);
                save.setEnabled(false);
                status.setText("Checking image…");
                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() throws Exception {
                        persistence.Repositories.assets().validate(path);
                        return null;
                    }

                    protected void done() {
                        choose.setEnabled(true);
                        remove.setEnabled(true);
                        save.setEnabled(true);
                        try {
                            get();
                            selected = path;
                            preview.load(path);
                            status.setText("Image selected. Save the item to keep it.");
                        } catch (Exception ex) {
                            status.setText("Choose a valid PNG or JPEG, up to 12 MB / 20 megapixels.");
                        }
                    }
                }.execute();
            }
        });
        remove.addActionListener(e -> {
            selected = null;
            image = "";
            preview.clear();
            status.setText("Image removed. Save the item to apply.");
        });
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            CatalogItem value;
            try {
                value = new CatalogItem(item == null ? UUID.randomUUID().toString() : item.id(), name.getText().trim(),
                        description.getText().trim(), category.getText().trim(),
                        (CatalogItem.Unit) unit.getSelectedItem(), new BigDecimal(price.getText().trim()),
                        type.getSelectedIndex() == 1, image);
            } catch (IllegalArgumentException ex) {
                status.setText(ex instanceof NumberFormatException ? "Enter a valid price." : ex.getMessage());
                return;
            }
            save.setEnabled(false);
            choose.setEnabled(false);
            remove.setEnabled(false);
            cancel.setEnabled(false);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            status.setText("Saving item…");
            Path source = selected;
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    String photo = source == null ? value.image()
                            : persistence.Repositories.assets().importPhoto(source);
                    CatalogItem updated = new CatalogItem(value.id(), value.name(), value.description(),
                            value.category(), value.unit(), value.price(), value.product(), photo);
                    java.util.List<CatalogItem> next = new ArrayList<>(store.items());
                    if (item == null)
                        next.add(updated);
                    else
                        next.set(next.indexOf(item), updated);
                    store.save(next);
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
                        remove.setEnabled(true);
                        cancel.setEnabled(true);
                        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        status.setText("Could not save the item. Your previous catalog is unchanged.");
                    }
                }
            }.execute();
        });
        setContentPane(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> {
            if (getDefaultCloseOperation() == DISPOSE_ON_CLOSE)
                dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        for (JComponent input : new JComponent[] { name, description, type, category, price, unit }) {
            input.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    SwingUtilities.invokeLater(() -> input.scrollRectToVisible(new Rectangle(input.getSize())));
                }
            });
        }
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        Rectangle available = new Rectangle(configuration.getBounds());
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        available.width -= screenInsets.left + screenInsets.right + 32;
        available.height -= screenInsets.top + screenInsets.bottom + 32;
        setMinimumSize(new Dimension(Math.min(380, available.width), Math.min(360, available.height)));
        setSize(Math.min(820, available.width), Math.min(640, available.height));
        setLocationRelativeTo(owner);
    }

    /**
     * Reflows existing controls without discarding entered values or the selected
     * photo.
     */
    private static final class EditorBody extends JPanel implements Scrollable {
        private final JPanel photo, form, types, pricing;
        private Boolean stacked, singleFields;

        EditorBody(JPanel photo, JPanel form, JPanel types, JPanel pricing) {
            super(new BorderLayout(24, 24));
            this.photo = photo;
            this.form = form;
            this.types = types;
            this.pricing = pricing;
            setOpaque(false);
            setName("catalogFormBody");
            reflow(740);
        }

        private void reflow(int width) {
            boolean stack = width < 620;
            boolean single = (stack ? width : width - 244) < 350;
            if (stacked == null || stack != stacked) {
                removeAll();
                add(photo, stack ? BorderLayout.NORTH : BorderLayout.WEST);
                add(form, BorderLayout.CENTER);
                stacked = stack;
                revalidate();
            }
            if (singleFields == null || single != singleFields) {
                types.setLayout(new GridLayout(single ? 2 : 1, single ? 1 : 2, 12, 12));
                pricing.setLayout(new GridLayout(single ? 2 : 1, single ? 1 : 2, 12, 12));
                // BoxLayout caches row sizes; discard those after changing the number of rows.
                for (JPanel row : new JPanel[] { types, pricing }) {
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
                }
                form.invalidate();
                singleFields = single;
                revalidate();
            }
        }

        @Override
        public void doLayout() {
            reflow(getWidth());
            super.doLayout();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(740, 400);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle view, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle view, int orientation, int direction) {
            return Math.max(24, view.height - 24);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private void addField(JPanel form, JComponent field) {
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        form.add(field);
        form.add(Box.createVerticalStrut(18));
    }
}
