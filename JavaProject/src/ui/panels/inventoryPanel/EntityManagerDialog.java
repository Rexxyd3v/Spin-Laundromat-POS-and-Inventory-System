package src.ui.panels.inventoryPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.util.components.Scrollbar;

public class EntityManagerDialog extends JDialog {

    private static final Color FIELD_BG = new Color(243, 244, 246);
    private static final Color BLUE_ACCENT = new Color(37, 99, 235);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_GRAY = new Color(229, 231, 235);
    private static final Color GREEN_BTN = new Color(114, 198, 75);
    private static final Color RED_BTN = new Color(229, 77, 77);

    private DefaultListModel<String> listModel;
    private JList<String> list;
    private JTextField inputField;
    private NotificationDialog.EntityType entityType;

    public EntityManagerDialog(Window parent, NotificationDialog.EntityType entityType, List<String> currentItems) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.entityType = entityType;
        setUndecorated(true);
        setSize(400, 500);
        setLocationRelativeTo(parent);

        String entityTitle = entityType == NotificationDialog.EntityType.CATEGORY ? "Categories" : "Suppliers";

        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow / Dimming effect
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 30, 30);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 30, 30);

                g2.setColor(new Color(59, 130, 246, 120));
                g2.drawRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 30, 30);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(35, 35, 35, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Manage " + entityTitle);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton exitBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? RED_BTN : TEXT_SECONDARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        exitBtn.setPreferredSize(new Dimension(30, 30));
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusPainted(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> dispose());
        header.add(exitBtn, BorderLayout.EAST);

        container.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(25, 0, 0, 0));

        // Input Field Wrapper
        JPanel inputWrapper = new JPanel(new BorderLayout(0, 8));
        inputWrapper.setOpaque(false);

        JLabel inputLabel = new JLabel("Entry Name");
        inputLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        inputLabel.setForeground(TEXT_SECONDARY);
        inputWrapper.add(inputLabel, BorderLayout.NORTH);

        inputField = new JTextField() {
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
        inputField.setPreferredSize(new Dimension(0, 45));
        inputField.setBorder(new EmptyBorder(0, 15, 0, 15));
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputWrapper.add(inputField, BorderLayout.CENTER);

        content.add(inputWrapper, BorderLayout.NORTH);

        // List Styling
        listModel = new DefaultListModel<>();
        for (String item : currentItems) {
            listModel.addElement(item);
        }
        list = new JList<>(listModel);
        list.setOpaque(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new CustomListRenderer());
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleEdit();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        content.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        btnPanel.setOpaque(false);

        btnPanel.add(createActionButton("Add", BLUE_ACCENT, e -> handleAdd()));
        btnPanel.add(createActionButton("Edit", GREEN_BTN, e -> handleEdit()));
        btnPanel.add(createActionButton("Delete", RED_BTN, e -> handleDelete()));

        content.add(btnPanel, BorderLayout.SOUTH);

        container.add(content, BorderLayout.CENTER);
        add(container);
        setBackground(new Color(0, 0, 0, 0));
    }

    private class CustomListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(10, 15, 10, 15));
            label.setFont(new Font("SansSerif", isSelected ? Font.BOLD : Font.PLAIN, 14));

            if (isSelected) {
                label.setBackground(new Color(59, 130, 246, 40));
                label.setForeground(BLUE_ACCENT);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(TEXT_PRIMARY);
            }
            label.setOpaque(true);
            return label;
        }
    }

    private JButton createActionButton(String text, Color bg, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 35));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(l);
        return btn;
    }

    private void handleAdd() {
        String name = inputField.getText().trim();
        if (name.isEmpty())
            return;

        boolean confirmed = false;
        if (entityType == NotificationDialog.EntityType.CATEGORY) {
            confirmed = NotificationDialog.showAddCategory(this);
        } else {
            confirmed = NotificationDialog.showAddSupplier(this);
        }

        if (confirmed) {
            listModel.addElement(name);
            inputField.setText("");
        }
    }

    private void handleEdit() {
        int idx = list.getSelectedIndex();
        if (idx == -1)
            return;
        String oldName = listModel.getElementAt(idx);

        String newName = RenameDialog.showRename(this, oldName);
        if (newName != null && !newName.isEmpty() && !newName.equals(oldName)) {
            boolean confirmed = false;
            if (entityType == NotificationDialog.EntityType.CATEGORY) {
                confirmed = NotificationDialog.showUpdateCategory(this);
            } else {
                confirmed = NotificationDialog.showUpdateSupplier(this);
            }

            if (confirmed) {
                listModel.set(idx, newName);
            }
        }
    }

    private static class RenameDialog extends JDialog {
        private String result = null;
        private JTextField inputField;

        private RenameDialog(Window parent, String currentName) {
            super(parent, ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setSize(320, 200);
            setLocationRelativeTo(parent);

            JPanel container = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(new Color(0, 0, 0, 50));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                }
            };
            container.setOpaque(false);
            container.setBorder(new EmptyBorder(25, 25, 25, 25));

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);

            JLabel title = new JLabel(currentName);
            title.setFont(new Font("SansSerif", Font.BOLD, 18));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(title);

            JLabel subtitle = new JLabel("CHANGE TO");
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
            subtitle.setForeground(TEXT_SECONDARY);
            subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(Box.createVerticalStrut(5));
            content.add(subtitle);

            inputField = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(FIELD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Stadium
                    super.paintComponent(g2);
                    g2.dispose();
                }

                @Override
                public void paintBorder(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BORDER_GRAY);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                    g2.dispose();
                }
            };
            inputField.setMaximumSize(new Dimension(300, 35));
            inputField.setBorder(new EmptyBorder(0, 15, 0, 15));
            inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            inputField.setOpaque(false);
            content.add(Box.createVerticalStrut(15));
            content.add(inputField);

            JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            btnPanel.setOpaque(false);
            btnPanel.setMaximumSize(new Dimension(300, 35));

            JButton saveBtn = createStyledButton("SAVE", GREEN_BTN, e -> {
                result = inputField.getText().trim();
                dispose();
            });
            JButton cancelBtn = createStyledButton("CANCEL", RED_BTN, e -> dispose());

            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);

            content.add(Box.createVerticalStrut(20));
            content.add(btnPanel);

            container.add(content, BorderLayout.CENTER);
            add(container);
            setBackground(new Color(0, 0, 0, 0));
        }

        private JButton createStyledButton(String text, Color bg, java.awt.event.ActionListener l) {
            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent()) / 2 - 1);
                    g2.dispose();
                }
            };
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(l);
            return btn;
        }

        public static String showRename(Window parent, String currentName) {
            RenameDialog dialog = new RenameDialog(parent, currentName);
            dialog.setVisible(true);
            return dialog.result;
        }
    }

    private void handleDelete() {
        int idx = list.getSelectedIndex();
        if (idx == -1)
            return;

        boolean confirmed = false;
        if (entityType == NotificationDialog.EntityType.CATEGORY) {
            confirmed = NotificationDialog.showDeleteCategory(this);
        } else {
            confirmed = NotificationDialog.showDeleteSupplier(this);
        }

        if (confirmed) {
            listModel.remove(idx);
            inputField.setText("");
        }
    }

    public static List<String> showManager(Window parent, NotificationDialog.EntityType type, List<String> items) {
        EntityManagerDialog dialog = new EntityManagerDialog(parent, type, items);
        dialog.setVisible(true);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dialog.listModel.size(); i++) {
            result.add(dialog.listModel.get(i));
        }
        return result;
    }
}
