package src.ui.panels.inventoryPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import src.util.components.Scrollbar;

public class AddProduct extends JDialog {

    private static final Color FIELD_BG = new Color(243, 244, 246);
    private static final Color BLUE_ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_GRAY = new Color(229, 231, 235);

    private boolean confirmed = false;

    public AddProduct(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);

        // Adaptive sizing
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 450;
        int height = Math.min(800, screen.height - 100);
        setSize(width, height);
        setLocationRelativeTo(parent);

        // Use a rounded content pane
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(59, 130, 246, 100)); // Subtle blue selection/border glow
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header with Exit Button
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Add New Product");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Fill in the details to add a new item to your inventory.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_SECONDARY);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(subtitle);

        headerWrapper.add(headerText, BorderLayout.WEST);

        // Exit Icon Button
        JButton exitBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 68, 68)); // Pure red text on hover
                } else {
                    g2.setColor(TEXT_SECONDARY);
                }
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        exitBtn.setPreferredSize(new Dimension(32, 32));
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> dispose());

        headerWrapper.add(exitBtn, BorderLayout.EAST);
        headerWrapper.add(Box.createVerticalStrut(25), BorderLayout.SOUTH);

        container.add(headerWrapper, BorderLayout.NORTH);

        // Form Fields (Scrollable)
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.weightx = 1.0;

        int row = 0;

        // Row 1: Product Name & Category
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;

        JPanel namePanel = createFieldPanel("Product Name", "Product name", true, false);
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 10);
        form.add(namePanel, gbc);

        JPanel categoryPanel = createManagedComboBoxPanel("Category", true,
                new ArrayList<>(Arrays.asList("Powder Detergent", "Liquid Detergent", "Fabric Conditioner")),
                NotificationDialog.EntityType.CATEGORY);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        form.add(categoryPanel, gbc);

        // Row 2: Description
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);

        JPanel descPanel = createDescriptionPanel("Description", "Product description", true);
        form.add(descPanel, gbc);

        // Row 3: Selling Price & Cost Price
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;

        JPanel sellingPricePanel = createFieldPanel("Selling Price * (₱)", "0.00", false, true);
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 10);
        form.add(sellingPricePanel, gbc);

        JPanel costPricePanel = createFieldPanel("Cost Price * (₱)", "0.00", false, true);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        form.add(costPricePanel, gbc);

        // Row 4: Initial Stock & Reorder Point
        gbc.gridy = row++;

        JPanel initialStockPanel = createFieldPanel("Initial Stock", "0", true, true);
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 10);
        form.add(initialStockPanel, gbc);

        JPanel reorderPointPanel = createFieldPanel("Reorder Point", "10", true, true);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        form.add(reorderPointPanel, gbc);

        // Row 5: Supplier
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        JPanel supplierPanel = createManagedComboBoxPanel("Supplier", true,
                new ArrayList<>(Arrays.asList("Unilever", "Procter & Gamble", "Local Supplier")),
                NotificationDialog.EntityType.SUPPLIER);
        form.add(supplierPanel, gbc);

        // Wrap form in ScrollPane
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scrollPane, BorderLayout.CENTER);

        // Footer Button
        JButton addBtn = new JButton("Add Product") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        addBtn.setPreferredSize(new Dimension(0, 50));
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addBtn.setForeground(Color.WHITE);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            if (NotificationDialog.showAdd(SwingUtilities.getWindowAncestor(this))) {
                confirmed = true;
                dispose();
            }
        });

        // Close when clicking outside or ESC
        container.add(addBtn, BorderLayout.SOUTH);

        add(container);
        setBackground(new Color(0, 0, 0, 0)); // Transparent bg
    }

    private JPanel createFieldPanel(String labelText, String placeholder, boolean showAsterisk, boolean isBold) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel();
        label.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        label.setText("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", isBold ? Font.BOLD : Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        JTextField field = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            public void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        field.setPreferredSize(new Dimension(0, 42));
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setForeground(TEXT_SECONDARY);
        field.setBorder(new EmptyBorder(0, 15, 0, 15));
        field.setOpaque(false);
        p.add(field, BorderLayout.CENTER);

        return p;
    }

    private JPanel createManagedComboBoxPanel(String labelText, boolean showAsterisk, List<String> items,
            NotificationDialog.EntityType type) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel();
        label.setText("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Select " + labelText.toLowerCase());
        for (String item : items)
            model.addElement(item);
        model.addElement("Manage " + labelText + "s...");

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.BLACK);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                                (getHeight() + fm.getAscent()) / 2 - 2);
                        g2.dispose();
                    }
                };
                btn.setBorder(null);
                btn.setContentAreaFilled(false);
                return btn;
            }

            @Override
            protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {
                    @Override
                    protected JScrollPane createScroller() {
                        JScrollPane scroller = super.createScroller();
                        scroller.setBorder(null);
                        return scroller;
                    }
                };
            }
        });

        combo.addActionListener(e -> {
            String selected = (String) combo.getSelectedItem();
            if (selected != null && selected.startsWith("Manage")) {
                List<String> current = new ArrayList<>();
                for (int i = 1; i < model.getSize() - 1; i++) {
                    current.add(model.getElementAt(i));
                }
                List<String> updated = EntityManagerDialog.showManager(SwingUtilities.getWindowAncestor(p), type,
                        current);

                // Update model
                model.removeAllElements();
                model.addElement("Select " + labelText.toLowerCase());
                for (String item : updated)
                    model.addElement(item);
                model.addElement("Manage " + labelText + "s...");
                combo.setSelectedIndex(0);
            }
        });

        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setForeground(TEXT_SECONDARY);
        combo.setPreferredSize(new Dimension(0, 42));
        combo.setBorder(null);
        combo.setOpaque(false);

        JPanel comboWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        comboWrapper.setOpaque(false);
        comboWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        comboWrapper.add(combo, BorderLayout.CENTER);

        p.add(comboWrapper, BorderLayout.CENTER);
        return p;
    }

    private JPanel createDescriptionPanel(String labelText, String placeholder, boolean showAsterisk) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel label = new JLabel();
        label.setText("<html>" + labelText + (showAsterisk ? " <font color='red'>*</font>" : "") + "</html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        p.add(label, BorderLayout.NORTH);

        JTextArea area = new JTextArea(placeholder);
        area.setFont(new Font("SansSerif", Font.PLAIN, 12));
        area.setForeground(TEXT_SECONDARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(15, 15, 15, 15));
        area.setOpaque(false);

        JPanel scrollWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        scrollWrapper.setOpaque(false);
        scrollWrapper.setPreferredSize(new Dimension(0, 130));
        scrollWrapper.add(area, BorderLayout.CENTER);

        p.add(scrollWrapper, BorderLayout.CENTER);
        return p;
    }

    public static boolean showAddProduct(Window parent) {
        AddProduct dialog = new AddProduct(parent);
        dialog.setVisible(true);
        return dialog.confirmed;
    }
}
