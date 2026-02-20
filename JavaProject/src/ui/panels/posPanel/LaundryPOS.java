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

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.add(Box.createVerticalStrut(10));
        search.add(searchField);
        panel.add(search);

        panel.add(Box.createVerticalStrut(25));

        // Services
        panel.add(createLabel("Services"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createGrid(new String[][] {
                { "Wash Only", "Per 7 kilogram/Comforter", "70.00" },
                { "Dry Only", "Per 7 kilogram/Comforter", "70.00" },
                { "Full Service", "Per 7 kilogram/Comforter", "170.00" },
                { "Fold Only", "Per 7 kilogram/Comforter", "30.00" }
        }));

        panel.add(Box.createVerticalStrut(25));

        // Products
        panel.add(createLabel("Products"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createGrid(new String[][] {
                { "Surf Powder", "Detergent Rose Fresh 75g sachet", "7.00" },
                { "Wings Detergent Powder", "White 40g", "7.00" },
                { "Breeze Liquid Detergent", "60ml sachet", "15.00" },
                { "Downy Sunrise Fresh", "60ml", "6.00" }
        }));

        panel.add(Box.createVerticalGlue());
        return panel;
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
        grid.setBackground(new Color(175, 238, 238));
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
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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

        JLabel priceLabel = new JLabel("₱" + price);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(priceLabel);

        card.add(info, BorderLayout.CENTER);

        // Add button
        JButton btn = new JButton("+");
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setFont(new Font("Arial", Font.BOLD, 28));
        btn.setBackground(new Color(135, 206, 250));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
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

        // Cart items
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

        // Totals
        JPanel subtotalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        subtotalPanel.setBackground(Color.WHITE);
        subtotalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        subtotalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subLabel = new JLabel("Subtotal: ");
        subLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        subtotalLabel = new JLabel("₱0.00");
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        subtotalPanel.add(subLabel);
        subtotalPanel.add(subtotalLabel);
        panel.add(subtotalPanel);

        panel.add(Box.createVerticalStrut(10));

        JPanel taxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        taxPanel.setBackground(Color.WHITE);
        taxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        taxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel taxLabelTitle = new JLabel("Tax(12%): ");
        taxLabelTitle.setFont(new Font("Arial", Font.PLAIN, 15));
        taxLabel = new JLabel("₱0.00");
        taxLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        taxPanel.add(taxLabelTitle);
        taxPanel.add(taxLabel);
        panel.add(taxPanel);

        panel.add(Box.createVerticalStrut(15));

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel totalLabelTitle = new JLabel("Total: ");
        totalLabelTitle.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel = new JLabel("₱0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalPanel.add(totalLabelTitle);
        totalPanel.add(totalLabel);
        panel.add(totalPanel);
        panel.add(Box.createVerticalStrut(25));

        // Checkout button - centered
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
                // Calculate totals
                double subtotal = 0;
                for (Item item : cart) {
                    subtotal += item.price * (item.qty / 7.0);
                }
                double tax = subtotal * 0.12;

                // Open checkout screen as a dialog
                JDialog checkoutDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(LaundryPOS.this),
                        "Checkout", true);
                checkoutDialog.setSize(900, 700);
                checkoutDialog.setLocationRelativeTo(LaundryPOS.this);

                CheckoutScreen checkoutScreen = new CheckoutScreen(LaundryPOS.this, new ArrayList<>(cart), subtotal,
                        tax);
                checkoutDialog.add(checkoutScreen);
                checkoutDialog.setVisible(true);
            }
        });
        buttonPanel.add(checkout);
        panel.add(buttonPanel);

        return panel;
    }

    // Method to clear cart after successful checkout
    public void clearCart() {
        cart.clear();
        updateCart();
    }

    private void addToCart(String name, String desc, double price) {
        // Check if item exists
        for (Item item : cart) {
            if (item.name.equals(name)) {
                item.qty += 7;
                updateCart();
                return;
            }
        }
        // Add new item
        cart.add(new Item(name, desc, price, 7));
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

            // Show price + description combined (like "₱70.00 Per 7 kilogram/Comforter")
            JLabel priceDescLabel = new JLabel("₱" + String.format("%.2f", item.price) + " " + item.desc);
            priceDescLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            priceDescLabel.setForeground(Color.GRAY);
            priceDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.add(priceDescLabel);

            itemPanel.add(Box.createVerticalStrut(10));

            // Quantity and price
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
                if (item.qty > 7) {
                    item.qty -= 7;
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
                item.qty += 7;
                updateCart();
            });

            qtyPanel.add(minus);
            qtyPanel.add(qtyLabel);
            qtyPanel.add(plus);

            double itemTotal = item.price * (item.qty / 7.0);
            JLabel priceLabel = new JLabel("₱" + String.format("%.2f", itemTotal));
            priceLabel.setFont(new Font("Arial", Font.BOLD, 15));

            bottom.add(qtyPanel);
            bottom.add(priceLabel);

            itemPanel.add(bottom);

            cartPanel.add(itemPanel);
            cartPanel.add(Box.createVerticalStrut(15));

            subtotal += itemTotal;
        }

        double tax = subtotal * 0.12;

        subtotalLabel.setText("₱" + String.format("%.2f", subtotal));
        taxLabel.setText("₱" + String.format("%.2f", tax));
        totalLabel.setText("₱" + String.format("%.2f", subtotal));

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