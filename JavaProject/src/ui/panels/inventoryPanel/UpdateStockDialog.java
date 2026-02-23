package src.ui.panels.inventoryPanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import src.util.components.Scrollbar;

public class UpdateStockDialog extends JDialog {

    private static final Color FIELD_BG = new Color(243, 244, 246);
    private static final Color BLUE_ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_GRAY = new Color(229, 231, 235);
    private static final Color GREEN_PREVIEW = new Color(34, 197, 94);
    private static final Color RED_PREVIEW = new Color(239, 68, 68);

    private boolean confirmed = false;
    private int initialStock;
    private boolean isStockIn = true;
    private JTextField quantityField;
    private JLabel previewLabel;

    public UpdateStockDialog(Window parent, String productName, int currentStock) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.initialStock = currentStock;
        setUndecorated(true);

        // Adaptive sizing based on screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 480;
        int height = Math.min(650, screen.height - 100);
        setSize(width, height);
        setLocationRelativeTo(parent);

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(59, 130, 246, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 0, 30, 0));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel title = new JLabel("Update Stock Inventory");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Adjust stock levels with real-time preview.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_SECONDARY);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(6));
        headerText.add(subtitle);

        headerWrapper.add(headerText, BorderLayout.WEST);

        JButton exitBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? RED_PREVIEW : TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        exitBtn.setPreferredSize(new Dimension(36, 36));
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> dispose());
        headerWrapper.add(exitBtn, BorderLayout.EAST);

        container.add(headerWrapper, BorderLayout.NORTH);

        // Scrollable content area
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;

        // Product Name (Compact)
        gbc.gridy = row++;
        form.add(createLabel("Product Name"), gbc);
        gbc.gridy = row++;
        form.add(createReadOnlyField(productName), gbc);

        // Update Type (Pill Toggles with Icons)
        gbc.gridy = row++;
        form.add(createLabel("Update Action"), gbc);
        gbc.gridy = row++;
        form.add(createTypeTogglePanel(), gbc);

        // Quantity + Preview Section
        gbc.gridy = row++;
        form.add(createLabel("Quantity to Adjust"), gbc);
        gbc.gridy = row++;
        form.add(createQuantitySection(), gbc);

        // Live Preview Box
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 0, 0, 0);
        form.add(createPreviewBox(), gbc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        container.add(scroll, BorderLayout.CENTER);

        // Footer
        JButton updateBtn = new JButton("Confirm Update") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BLUE_ACCENT.darker() : BLUE_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        updateBtn.setPreferredSize(new Dimension(0, 52));
        updateBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setContentAreaFilled(false);
        updateBtn.setBorderPainted(false);
        updateBtn.setFocusPainted(false);
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateBtn.addActionListener(e -> handleUpdate());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(30, 0, 0, 0));
        footer.add(updateBtn, BorderLayout.CENTER);

        container.add(footer, BorderLayout.SOUTH);

        add(container);
        setBackground(new Color(0, 0, 0, 0));

        updatePreview();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text + " *");
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JPanel createReadOnlyField(String text) {
        JTextField field = new JTextField(text);
        field.setEditable(false);
        field.setFont(new Font("SansSerif", Font.BOLD, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 15, 0, 15));
        field.setPreferredSize(new Dimension(0, 48));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(249, 250, 251));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createQuantitySection() {
        quantityField = new JTextField();
        quantityField.setFont(new Font("SansSerif", Font.BOLD, 18));
        quantityField.setForeground(BLUE_ACCENT);
        quantityField.setOpaque(false);
        quantityField.setBorder(new EmptyBorder(0, 15, 0, 15));
        quantityField.setPreferredSize(new Dimension(0, 52));

        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FIELD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BLUE_ACCENT);
                if (quantityField.hasFocus()) {
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
                } else {
                    g2.setColor(BORDER_GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                }
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(quantityField, BorderLayout.CENTER);

        quantityField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrapper.repaint();
            }
        });

        return wrapper;
    }

    private JPanel createPreviewBox() {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(243, 244, 246, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setStroke(
                        new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] { 5, 5 }, 0));
                g2.setColor(BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(0, 80));
        box.setBorder(new EmptyBorder(15, 20, 15, 20));

        previewLabel = new JLabel("");
        previewLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(previewLabel, BorderLayout.CENTER);

        return box;
    }

    private void updatePreview() {
        String qtyText = quantityField.getText().trim();
        try {
            int qty = qtyText.isEmpty() ? 0 : Integer.parseInt(qtyText);
            int newVal = isStockIn ? initialStock + qty : initialStock - qty;

            Color statusColor = isStockIn ? GREEN_PREVIEW : RED_PREVIEW;

            String html = "<html><center><font color='#6B7280' size='4'>Stock Level Preview</font><br>" +
                    "<b><font size='6'>" + initialStock + " <font color='#9CA3AF'>→</font> " +
                    "<font color='"
                    + String.format("#%02x%02x%02x", statusColor.getRed(), statusColor.getGreen(),
                            statusColor.getBlue())
                    + "'>" +
                    newVal + "</font></font></b></center></html>";

            previewLabel.setText(html);
        } catch (NumberFormatException e) {
            previewLabel.setText("<html><font color='red'>Invalid Quantity</font></html>");
        }
    }

    private JPanel createTypeTogglePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 50));

        JButton stockInBtn = createToggleButton("Stock In", "↑");
        JButton stockOutBtn = createToggleButton("Stock Out", "↓");

        stockInBtn.putClientProperty("selected", true);
        stockOutBtn.putClientProperty("selected", false);

        stockInBtn.addActionListener(e -> {
            isStockIn = true;
            stockInBtn.putClientProperty("selected", true);
            stockOutBtn.putClientProperty("selected", false);
            updatePreview();
            panel.repaint();
            stockOutBtn.repaint();
        });

        stockOutBtn.addActionListener(e -> {
            isStockIn = false;
            stockInBtn.putClientProperty("selected", false);
            stockOutBtn.putClientProperty("selected", true);
            updatePreview();
            panel.repaint();
            stockInBtn.repaint();
        });

        panel.add(stockInBtn);
        panel.add(stockOutBtn);
        return panel;
    }

    private JButton createToggleButton(String text, String icon) {
        JButton btn = new JButton(icon + "  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isSel = (boolean) getClientProperty("selected");

                if (isSel) {
                    g2.setColor(BLUE_ACCENT);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 246, 255));
                } else {
                    g2.setColor(Color.WHITE);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(isSel ? BLUE_ACCENT : BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                g2.setColor(isSel ? Color.WHITE : (getModel().isRollover() ? BLUE_ACCENT : TEXT_SECONDARY));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleUpdate() {
        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isStockIn && (initialStock - qty < 0)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock for this operation.", "Stock Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (NotificationDialog.showUpdateStock(this)) {
            confirmed = true;
            dispose();
        }
    }

    public static Object[] showUpdateStock(Window parent, String productName, int currentStock) {
        UpdateStockDialog dialog = new UpdateStockDialog(parent, productName, currentStock);
        dialog.setVisible(true);
        if (dialog.confirmed) {
            return new Object[] {
                    dialog.isStockIn ? "Stock In" : "Stock Out",
                    dialog.quantityField.getText().trim()
            };
        }
        return null;
    }
}
