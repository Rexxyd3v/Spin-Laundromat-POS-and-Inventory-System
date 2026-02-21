package src.ui.panels.posPanel;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class LaundryPOS extends JPanel {
    private final ArrayList<Item> cart = new ArrayList<>();
    private JPanel cartPanel;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private static final int PADDING = 30;

    public LaundryPOS() {
        setLayout(new BorderLayout());
        setBackground(new Color(175, 238, 238));

        // Center content (scrollable for responsiveness)
        JScrollPane centerScroll = new JScrollPane(createCenter());
        centerScroll.setBorder(null);
        centerScroll.setBackground(new Color(175, 238, 238));
        centerScroll.getViewport().setBackground(new Color(175, 238, 238));
        add(centerScroll, BorderLayout.CENTER);

        // Right panel (cart)
        add(createCart(), BorderLayout.EAST);
    }

    private JPanel createCenter() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(175, 238, 238));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Search
        JPanel search = new JPanel();
        search.setBackground(Color.WHITE);
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));
        search.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        search.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchLabel = new JLabel("Service Search");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.add(searchLabel);
        search.add(Box.createVerticalStrut(10));

        // Search field row with icon
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(Color.WHITE);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        searchRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel iconLabel = new JLabel("\uD83D\uDD0D"); // 🔍 emoji
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        iconLabel.setForeground(Color.GRAY);

        JTextField searchField = new JTextField("Search services....");
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(null);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setBackground(Color.WHITE);

        // Clear placeholder on focus
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Search services....")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search services....");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchRow.add(iconLabel, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);
        search.add(searchRow);

        panel.add(search);
        panel.add(Box.createVerticalStrut(25));

        // Services section
        panel.add(createSection("Services", new String[][] {
                { "Wash Only", "Per 7 kilogram/Comforter", "70.00" },
                { "Dry Only", "Per 7 kilogram/Comforter", "70.00" },
                { "Full Service", "Per 7 kilogram/Comforter", "170.00" },
                { "Fold Only", "Per 7 kilogram/Comforter", "30.00" }
        }));

        panel.add(Box.createVerticalStrut(25));

        // Products section
        panel.add(createSection("Products", new String[][] {
                { "Surf Powder", "Detergent Rose Fresh 75g sachet", "7.00" },
                { "Wings Detergent Powder", "White 40g", "7.00" },
                { "Breeze Liquid Detergent", "60ml sachet", "15.00" },
                { "Downy Sunrise Fresh", "60ml", "6.00" }
        }));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // Creates a white rounded section container with title + grid inside
    private JPanel createSection(String title, String[][] items) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(createLabel(title));
        section.add(Box.createVerticalStrut(15));
        section.add(createGrid(items));
        return section;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private JPanel createGrid(String[][] items) {
        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
        grid.setBackground(Color.WHITE);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String[] item : items) {
            grid.add(createCard(item[0], item[1], item[2]));
        }
        return grid;
    }

    private JPanel createCard(String name, String desc, String price) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLabel);

        info.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(descLabel);

        info.add(Box.createVerticalStrut(10));

        JLabel priceLabel = new JLabel("\u20B1" + price);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(priceLabel);

        card.add(info, BorderLayout.CENTER);

        // Circle "+" button
        class CircleButton extends JButton {
            public CircleButton(String label) {
                super(label);
                setPreferredSize(new Dimension(50, 50));
                setFont(new Font("Arial", Font.BOLD, 28));
                setBackground(new Color(135, 206, 250));
                setForeground(Color.WHITE);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground().darker());
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }

            @Override
            public boolean contains(int x, int y) {
                int radius = getWidth() / 2;
                return (x - radius) * (x - radius) + (y - radius) * (y - radius) <= radius * radius;
            }
        }

        CircleButton btn = new CircleButton("+");
        btn.setMaximumSize(new Dimension(35, 35));
        btn.setMinimumSize(new Dimension(35, 35));
        btn.addActionListener(e -> addToCart(name, desc, Double.parseDouble(price)));
        card.add(btn, BorderLayout.EAST);

        return card;
    }

    private JPanel createCart() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setMinimumSize(new Dimension(280, 0));
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Current Order");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));

        // Cart items scroll
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(cartPanel);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll);

        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(20));

        // Subtotal row
        subtotalLabel = new JLabel("\u20B10.00");
        panel.add(createTotalRow("Subtotal:", subtotalLabel, new Font("Arial", Font.PLAIN, 15)));
        panel.add(Box.createVerticalStrut(10));

        // Tax row
        taxLabel = new JLabel("\u20B10.00");
        panel.add(createTotalRow("Tax(12%):", taxLabel, new Font("Arial", Font.PLAIN, 15)));
        panel.add(Box.createVerticalStrut(15));

        // Separator line
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 220, 220));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(15));

        // Total row
        totalLabel = new JLabel("\u20B10.00");
        panel.add(createTotalRow("Total:", totalLabel, new Font("Arial", Font.BOLD, 18)));
        panel.add(Box.createVerticalStrut(25));

        // Checkout button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JButton checkout = new JButton("Proceed to Checkout");
        checkout.setPreferredSize(new Dimension(320, 55));
        checkout.setFont(new Font("Arial", Font.BOLD, 16));
        checkout.setBackground(new Color(65, 105, 225));
        checkout.setForeground(Color.WHITE);
        checkout.setBorderPainted(false);
        checkout.setFocusPainted(false);
        checkout.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
            } else {
                double subtotal = 0;
                for (Item item : cart) {
                    subtotal += item.price * item.qty;
                }
                double tax = subtotal * 0.12;

                JDialog checkoutDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(LaundryPOS.this),
                        "Checkout", true);
                checkoutDialog.setSize(900, 700);
                checkoutDialog.setLocationRelativeTo(LaundryPOS.this);

                CheckoutScreen checkoutScreen = new CheckoutScreen(LaundryPOS.this, new ArrayList<>(cart), subtotal, tax);
                checkoutDialog.add(checkoutScreen);
                checkoutDialog.setVisible(true);
            }
        });
        buttonPanel.add(checkout);
        panel.add(buttonPanel);

        return panel;
    }

    // Helper to create a right-aligned total row
    private JPanel createTotalRow(String labelText, JLabel valueLabel, Font font) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(font);
        valueLabel.setFont(font);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // Method to clear cart after successful checkout
    public void clearCart() {
        cart.clear();
        updateCart();
    }

    private void addToCart(String name, String desc, double price) {
        for (Item item : cart) {
            if (item.name.equals(name)) {
                item.qty += 1;
                updateCart();
                return;
            }
        }
        cart.add(new Item(name, desc, price, 1));
        updateCart();
    }

    private void updateCart() {
        cartPanel.removeAll();
        double subtotal = 0;

        for (Item item : cart) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setBackground(new Color(245, 245, 245));
            itemPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            itemPanel.setPreferredSize(new Dimension(300, 110));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nameLabel = new JLabel(item.name);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.add(nameLabel);

            itemPanel.add(Box.createVerticalStrut(3));

            JLabel priceDescLabel = new JLabel("\u20B1" + String.format("%.2f", item.price) + " " + item.desc);
            priceDescLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            priceDescLabel.setForeground(Color.GRAY);
            priceDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.add(priceDescLabel);

            itemPanel.add(Box.createVerticalStrut(10));

            // Quantity and price row
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            bottom.setBackground(new Color(245, 245, 245));
            bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            bottom.setPreferredSize(new Dimension(280, 35));

            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            qtyPanel.setBackground(new Color(245, 245, 245));

            JButton minus = new JButton("-");
            minus.setPreferredSize(new Dimension(30, 30));
            minus.setFont(new Font("Arial", Font.BOLD, 16));
            minus.setBackground(Color.WHITE);
            minus.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            minus.setFocusPainted(false);
            minus.addActionListener(e -> {
                if (item.qty > 1) {
                    item.qty -= 1;
                } else {
                    cart.remove(item);
                }
                updateCart();
            });

            JLabel qtyLabel = new JLabel(" " + item.qty + " kg ");
            qtyLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            JButton plus = new JButton("+");
            plus.setPreferredSize(new Dimension(30, 30));
            plus.setFont(new Font("Arial", Font.BOLD, 16));
            plus.setBackground(Color.WHITE);
            plus.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            plus.setFocusPainted(false);
            plus.addActionListener(e -> {
                item.qty += 1;
                updateCart();
            });

            qtyPanel.add(minus);
            qtyPanel.add(qtyLabel);
            qtyPanel.add(plus);

            double itemTotal = item.price * item.qty;
            JLabel priceLabel = new JLabel("\u20B1" + String.format("%.2f", itemTotal));
            priceLabel.setFont(new Font("Arial", Font.BOLD, 15));

            bottom.add(qtyPanel);
            bottom.add(priceLabel);

            itemPanel.add(bottom);

            cartPanel.add(itemPanel);
            cartPanel.add(Box.createVerticalStrut(15));

            subtotal += itemTotal;
        }

        double tax = subtotal * 0.12;
        double total = subtotal + tax;

        subtotalLabel.setText("\u20B1" + String.format("%.2f", subtotal));
        taxLabel.setText("\u20B1" + String.format("%.2f", tax));
        totalLabel.setText("\u20B1" + String.format("%.2f", total));  // FIX: was showing subtotal before

        cartPanel.revalidate();
        cartPanel.repaint();
    }

    public class Item {
        public String name, desc;
        public double price;
        public int qty;

        public Item(String name, String desc, double price, int qty) {
            this.name = name;
            this.desc = desc;
            this.price = price;
            this.qty = qty;
        }
    }
}
