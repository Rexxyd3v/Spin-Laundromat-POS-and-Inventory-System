package src.ui.panels.inventoryPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class NotificationDialog extends JDialog {

    private boolean confirmed = false;
    private static final Color BG_COLOR = new Color(209, 233, 246); // #D1E9F6
    private static final Color GREEN_BTN = new Color(114, 198, 75); // #72C64B
    private static final Color RED_BTN = new Color(229, 77, 77); // #E54D4D

    public enum Type {
        ADD, UPDATE, DELETE
    }

    public enum EntityType {
        PRODUCT, CATEGORY, SUPPLIER, STOCK
    }

    private NotificationDialog(Window parent, EntityType entity, Type type) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(400, 180);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);

        String entityName = switch (entity) {
            case PRODUCT -> "product";
            case CATEGORY -> "Category";
            case SUPPLIER -> "Supplier";
            case STOCK -> "stock";
        };

        String message;
        if (entity == EntityType.STOCK && type == Type.UPDATE) {
            message = "<html><center><font size='5'><b>Are you sure?</b></font><br>You want to update this stock?</center></html>";
        } else {
            message = switch (type) {
                case ADD -> "Add this " + entityName + "?";
                case UPDATE -> "Update this " + entityName + "?";
                case DELETE -> "Delete this " + entityName + "?";
            };
        }

        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setForeground(Color.BLACK);
        content.add(lbl, gbc);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);

        JButton yesBtn = createStyledButton("Yes", GREEN_BTN);
        JButton noBtn = createStyledButton("No", RED_BTN);

        yesBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        noBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        // Ordering: Latest image shows [Yes] [No] for all Category/Supplier variants.
        // We maintain [No] [Yes] only for Product Delete if that's the preferred safety
        // pattern.
        if (type == Type.DELETE && entity == EntityType.PRODUCT) {
            btnPanel.add(noBtn);
            btnPanel.add(yesBtn);
        } else {
            btnPanel.add(yesBtn);
            btnPanel.add(noBtn);
        }

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btnPanel, gbc);

        add(content);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- Product Variants ---
    public static boolean showAdd(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.PRODUCT, Type.ADD);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showUpdate(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.PRODUCT, Type.UPDATE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showDelete(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.PRODUCT, Type.DELETE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showUpdateStock(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.STOCK, Type.UPDATE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    // --- Category Variants ---
    public static boolean showAddCategory(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.CATEGORY, Type.ADD);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showUpdateCategory(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.CATEGORY, Type.UPDATE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showDeleteCategory(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.CATEGORY, Type.DELETE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    // --- Supplier Variants ---
    public static boolean showAddSupplier(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.SUPPLIER, Type.ADD);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showUpdateSupplier(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.SUPPLIER, Type.UPDATE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static boolean showDeleteSupplier(Window parent) {
        NotificationDialog dialog = new NotificationDialog(parent, EntityType.SUPPLIER, Type.DELETE);
        dialog.setVisible(true);
        return dialog.confirmed;
    }
}
