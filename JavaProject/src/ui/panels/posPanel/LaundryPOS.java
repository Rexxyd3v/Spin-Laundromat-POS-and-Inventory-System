package src.ui.panels.posPanel;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import src.util.config.UIStyle;
import src.util.components.ShadowPanel;
import src.util.components.Scrollbar;

public class LaundryPOS extends JPanel {
    private final ArrayList<Item> cart = new ArrayList<>();
    private JPanel cartPanel;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private static final int PADDING = 20;

    public LaundryPOS() {
        setLayout(new GridBagLayout());
        setBackground(UIStyle.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();

        // Left Column: Menu (Adaptive/Scrollable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        JScrollPane centerScroll = new JScrollPane(createCenter());
        centerScroll.setBorder(null);
        centerScroll.setBackground(UIStyle.BACKGROUND);
        centerScroll.getViewport().setBackground(UIStyle.BACKGROUND);
        centerScroll.getVerticalScrollBar().setUnitIncrement(16);
        centerScroll.getVerticalScrollBar().setUI(new Scrollbar());
        centerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(centerScroll, gbc);
        centerScroll.setOpaque(false);

        // Right Column: Cart (Fixed Width)
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        add(createCart(), gbc);
    }

    private JPanel createCenter() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Search Section
        panel.add(createSearchSection());
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

    private JPanel createSearchSection() {
        ShadowPanel searchCard = new ShadowPanel();
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel searchLabel = new JLabel("Service Search");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchLabel.setHorizontalAlignment(SwingConstants.LEFT);
        inner.add(searchLabel);
        inner.add(Box.createVerticalStrut(12));

        // Pill search bar (Enlarged)
        JPanel searchPill = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        searchPill.setOpaque(false);
        searchPill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        searchPill.setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
        searchPill.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        searchPill.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Search icon
        JLabel searchIcon = new JLabel();
        try {
            java.net.URL imgUrl = getClass().getResource("/src/resources/images/search.png");
            if (imgUrl != null) {
                ImageIcon original = new ImageIcon(imgUrl);
                Image scaled = original.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                searchIcon.setIcon(new ImageIcon(scaled));
            } else {
                searchIcon.setText("\uD83D\uDD0D");
                searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                searchIcon.setForeground(new Color(148, 163, 184));
            }
        } catch (Exception e) {
            searchIcon.setText("\uD83D\uDD0D");
        }

        JTextField searchField = new JTextField("Search services....");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setForeground(new Color(148, 163, 184));

        searchPill.add(searchIcon, BorderLayout.WEST);
        searchPill.add(searchField, BorderLayout.CENTER);
        inner.add(searchPill);

        searchCard.add(inner);
        return searchCard;
    }

    // Creates a white rounded section container with title + grid inside
    private JPanel createSection(String title, String[][] items) {
        ShadowPanel sectionCard = new ShadowPanel();
        sectionCard.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(label);
        inner.add(Box.createVerticalStrut(15));

        inner.add(createGrid(items));

        sectionCard.add(inner);
        return sectionCard;
    }

    private JPanel createGrid(String[][] items) {
        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String[] item : items) {
            grid.add(createCard(item[0], item[1], item[2]));
        }
        return grid;
    }

    private JPanel createCard(String name, String desc, String price) {
        JPanel card = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLabel);

        info.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(100, 116, 139));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(descLabel);

        info.add(Box.createVerticalStrut(10));

        JLabel priceLabel = new JLabel("\u20B1" + price);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(priceLabel);

        card.add(info, BorderLayout.CENTER);

        // Circle "+" button (wrapped to prevent stretching)
        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);

        JButton btn = new JButton("+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(147, 197, 253)); // Light blue bubble
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setMinimumSize(new Dimension(32, 32));
        btn.setMaximumSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> addToCart(name, desc, Double.parseDouble(price)));

        btnWrapper.add(btn);
        card.add(btnWrapper, BorderLayout.EAST);

        return card;
    }

    private JPanel createCart() {
        ShadowPanel outerCard = new ShadowPanel();
        outerCard.setPreferredSize(new Dimension(400, 0));
        outerCard.setMinimumSize(new Dimension(380, 0));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Current Order");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        // Cart items scroll
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(cartPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll);

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
        JButton checkout = new JButton("Proceed to Checkout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235)); // Modern Blue
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        checkout.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        checkout.setContentAreaFilled(false);
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

                CheckoutScreen checkoutScreen = new CheckoutScreen(LaundryPOS.this, new ArrayList<>(cart), subtotal,
                        tax);
                checkoutDialog.add(checkoutScreen);
                checkoutDialog.setVisible(true);
            }
        });
        panel.add(checkout);

        outerCard.add(panel);
        return outerCard;
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
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Add right padding

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
            JPanel itemPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(248, 250, 252)); // Very light gray
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.dispose();
                }
            };
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setOpaque(false);
            itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nameLabel = new JLabel(item.name);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.add(nameLabel);

            itemPanel.add(Box.createVerticalStrut(3));

            JLabel priceDescLabel = new JLabel("\u20B1" + String.format("%.2f", item.price) + " " + item.desc);
            priceDescLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            priceDescLabel.setForeground(new Color(100, 116, 139));
            priceDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemPanel.add(priceDescLabel);

            itemPanel.add(Box.createVerticalStrut(10));

            // Quantity and price row
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            qtyPanel.setOpaque(false);

            class QtyBtn extends JButton {
                public QtyBtn(String t) {
                    super(t);
                    setPreferredSize(new Dimension(28, 28));
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    setBackground(Color.WHITE);
                    setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
                    setFocusPainted(false);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            QtyBtn minus = new QtyBtn("-");
            minus.addActionListener(e -> {
                if (item.qty > 1) {
                    item.qty -= 1;
                } else {
                    cart.remove(item);
                }
                updateCart();
            });

            JLabel qtyLabel = new JLabel(item.qty + " kg");
            qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            qtyLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            QtyBtn plus = new QtyBtn("+");
            plus.addActionListener(e -> {
                item.qty += 1;
                updateCart();
            });

            qtyPanel.add(minus);
            qtyPanel.add(qtyLabel);
            qtyPanel.add(plus);

            double itemTotal = item.price * item.qty;
            JLabel priceLabel = new JLabel("\u20B1" + String.format("%.2f", itemTotal));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); // Add padding to avoid clipping

            bottom.add(qtyPanel, BorderLayout.WEST);
            bottom.add(priceLabel, BorderLayout.EAST);

            itemPanel.add(bottom);

            cartPanel.add(itemPanel);
            cartPanel.add(Box.createVerticalStrut(15));

            subtotal += itemTotal;
        }

        double tax = subtotal * 0.12;
        double total = subtotal + tax;

        subtotalLabel.setText("\u20B1" + String.format("%.2f", subtotal));
        taxLabel.setText("\u20B1" + String.format("%.2f", tax));
        totalLabel.setText("\u20B1" + String.format("%.2f", total)); // FIX: was showing subtotal before

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
