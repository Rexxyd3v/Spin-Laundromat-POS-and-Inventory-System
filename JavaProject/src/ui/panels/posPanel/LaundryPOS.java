package src.ui.panels.posPanel;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.*;
import src.util.components.Scrollbar;
import src.util.components.ShadowPanel;
import src.util.config.UIStyle;

public class LaundryPOS extends JPanel {
    private final ArrayList<Item> cart = new ArrayList<>();
    private JPanel cartPanel;
    private JLabel subtotalLabel, taxLabel, totalLabel;
    private static final int PADDING = 20;
    private final ArrayList<String[]> servicesList = new ArrayList<>();
    private JPanel servicesGrid; 

    private final String[][] productsList = {
        { "Surf Powder",           "Detergent Rose Fresh 75g sachet", "7.00"  },
        { "Wings Detergent Powder","White 40g",                       "7.00"  },
        { "Breeze Liquid Detergent","60ml sachet",                    "15.00" },
        { "Downy Sunrise Fresh",   "60ml",                            "6.00"  }
    };
    private JPanel productsGrid; 
    private static final String SERVICES_FILE = "services.properties";

    public LaundryPOS() {
        loadServices();

        setLayout(new GridBagLayout());
        setBackground(UIStyle.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();

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
        panel.add(createSearchSection());
        panel.add(Box.createVerticalStrut(25));

        JPanel serviceHeader = new JPanel(new BorderLayout());
        serviceHeader.setOpaque(false);
        serviceHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        serviceHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel servicesTitle = new JLabel("Services");
        servicesTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        serviceHeader.add(servicesTitle, BorderLayout.WEST);

        JButton addServiceBtn = new JButton("+ Add Service") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        addServiceBtn.setPreferredSize(new Dimension(130, 34));
        addServiceBtn.setContentAreaFilled(false);
        addServiceBtn.setBorderPainted(false);
        addServiceBtn.setFocusPainted(false);
        addServiceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addServiceBtn.addActionListener(e -> showAddServiceDialog());
        serviceHeader.add(addServiceBtn, BorderLayout.EAST);

        ShadowPanel servicesCard = new ShadowPanel();
        servicesCard.setOpaque(false);
        JPanel servicesInner = new JPanel();
        servicesInner.setOpaque(false);
        servicesInner.setLayout(new BoxLayout(servicesInner, BoxLayout.Y_AXIS));
        servicesInner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        servicesInner.add(serviceHeader);
        servicesInner.add(Box.createVerticalStrut(15));

        servicesGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        servicesGrid.setOpaque(false);
        servicesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String[] s : servicesList) {
            servicesGrid.add(createServiceCard(s[0], s[1], s[2]));
        }
        servicesInner.add(servicesGrid);
        servicesCard.add(servicesInner);
        panel.add(servicesCard);

        panel.add(Box.createVerticalStrut(25));

        // Products section
        ShadowPanel productsCard = new ShadowPanel();
        productsCard.setOpaque(false);
        JPanel productsInner = new JPanel();
        productsInner.setOpaque(false);
        productsInner.setLayout(new BoxLayout(productsInner, BoxLayout.Y_AXIS));
        productsInner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel productsTitle = new JLabel("Products");
        productsTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        productsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        productsInner.add(productsTitle);
        productsInner.add(Box.createVerticalStrut(15));

        productsGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        productsGrid.setOpaque(false);
        productsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String[] p : productsList) {
            productsGrid.add(createCard(p[0], p[1], p[2]));
        }
        productsInner.add(productsGrid);
        productsCard.add(productsInner);
        panel.add(productsCard);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createSearchSection() {
        ShadowPanel searchCard = new ShadowPanel();
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel searchLabel = new JLabel("Search Services & Products");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(searchLabel);
        inner.add(Box.createVerticalStrut(12));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        searchPill.setPreferredSize(new Dimension(0, 45));
        searchPill.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

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

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setForeground(new Color(30, 41, 59));

        searchField.setText("Search services & products...");
        searchField.setForeground(new Color(148, 163, 184));
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Search services & products...")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(30, 41, 59));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Search services & products...");
                    searchField.setForeground(new Color(148, 163, 184));
                }
            }
        });

        searchPill.add(searchIcon, BorderLayout.WEST);
        searchPill.add(searchField, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(29, 78, 216) : new Color(37, 99, 235));
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
        searchBtn.setPreferredSize(new Dimension(90, 45));
        searchBtn.setMinimumSize(new Dimension(90, 45));
        searchBtn.setMaximumSize(new Dimension(90, 45));
        searchBtn.setContentAreaFilled(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable doSearch = () -> {
            String query = searchField.getText().trim().toLowerCase();
            if (query.equals("search services & products...")) query = "";
            servicesGrid.removeAll();
            boolean sFound = false;
            for (String[] s : servicesList) {
                if (query.isEmpty() || s[0].toLowerCase().contains(query) || s[1].toLowerCase().contains(query)) {
                    servicesGrid.add(createServiceCard(s[0], s[1], s[2]));
                    sFound = true;
                }
            }
            if (!sFound) {
                JLabel noResult = new JLabel("No services match your search.");
                noResult.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                noResult.setForeground(new Color(148, 163, 184));
                servicesGrid.add(noResult);
            }
            servicesGrid.revalidate();
            servicesGrid.repaint();

            productsGrid.removeAll();
            boolean pFound = false;
            for (String[] p : productsList) {
                if (query.isEmpty() || p[0].toLowerCase().contains(query) || p[1].toLowerCase().contains(query)) {
                    productsGrid.add(createCard(p[0], p[1], p[2]));
                    pFound = true;
                }
            }
            if (!pFound) {
                JLabel noResult = new JLabel("No products match your search.");
                noResult.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                noResult.setForeground(new Color(148, 163, 184));
                productsGrid.add(noResult);
            }
            productsGrid.revalidate();
            productsGrid.repaint();
        };

        searchBtn.addActionListener(e -> doSearch.run());
        searchField.addActionListener(e -> doSearch.run()); // also trigger on Enter

        searchRow.add(searchPill, BorderLayout.CENTER);
        searchRow.add(searchBtn, BorderLayout.EAST);

        inner.add(searchRow);
        searchCard.add(inner);
        return searchCard;
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

    private void showAddServiceDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "", true);
        dialog.setUndecorated(true);
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder());

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));

        JPanel headerRow = new JPanel(new BorderLayout(0, 0));
        headerRow.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Add New Service");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(15, 23, 42));
        titleBlock.add(titleLbl);

        JLabel subtitleLbl = new JLabel("Fill in the service name and price per kilogram.");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLbl.setForeground(new Color(100, 116, 139));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        JButton closeBtn = new JButton("×") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("×", (getWidth() - fm.stringWidth("×")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        closeBtn.setPreferredSize(new Dimension(32, 32));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        headerRow.add(titleBlock, BorderLayout.CENTER);
        headerRow.add(closeBtn, BorderLayout.EAST);

        // ── Form panel ────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;

        JTextField nameField = makeStyledField("e.g. Delicate Wash");
        JTextField priceField = makeStyledField("e.g. 25.00");

        JLabel nameLbl = makeFieldLabel("Service Name *");
        gc.gridy = 0; gc.insets = new Insets(0, 0, 6, 0);
        form.add(nameLbl, gc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 18, 0);
        form.add(nameField, gc);

        JLabel priceLbl = makeFieldLabel("Price per kg (₱) *");
        gc.gridy = 2; gc.insets = new Insets(0, 0, 6, 0);
        form.add(priceLbl, gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 6, 0);
        form.add(priceField, gc);

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        errorLbl.setForeground(new Color(220, 38, 38));
        gc.gridy = 4; gc.insets = new Insets(0, 0, 18, 0);
        form.add(errorLbl, gc);
    
        JButton addBtn = new JButton("Create Service") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(29, 78, 216) : new Color(58, 103, 229));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        addBtn.setPreferredSize(new Dimension(0, 50));
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(e -> {
            String serviceName = nameField.getText().trim();
            String priceText = priceField.getText().trim();

            if (serviceName.isEmpty()) {
                errorLbl.setText("⚠  Service name cannot be empty.");
                return;
            }
            if (priceText.isEmpty()) {
                errorLbl.setText("⚠  Price cannot be empty.");
                return;
            }
            double parsedPrice;
            try {
                parsedPrice = Double.parseDouble(priceText);
                if (parsedPrice < 0) {
                    errorLbl.setText("⚠  Price must be a positive number.");
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLbl.setText("⚠  Invalid price. Use numbers only (e.g. 25.00).");
                return;
            }

            String priceFormatted = String.format("%.2f", parsedPrice);
            servicesList.add(new String[]{ serviceName, "Per kg", priceFormatted });
            saveServices();
            refreshServicesGrid();
            dialog.dispose();
        });

        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);
        form.add(addBtn, gc);

        root.add(headerRow, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        JPanel shadow = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 6; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 8));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 22, 22);
                }
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 20, 20);
                g2.dispose();
            }
        };
        shadow.setOpaque(false);
        shadow.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        shadow.add(root);

        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.add(shadow);
        dialog.setVisible(true);
    }

    private void showDeleteConfirmDialog(String name, String price) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "", true);
        dialog.setUndecorated(true);
        dialog.setSize(360, 190);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 7));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 22, 22);
                }
                g2.setColor(new Color(219, 234, 254)); // light blue
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));

        JLabel question = new JLabel("Delete this Service?");
        question.setFont(new Font("Segoe UI", Font.BOLD, 17));
        question.setForeground(new Color(15, 23, 42));
        question.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);

        JButton yesBtn = new JButton("Yes") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(21, 128, 61) : new Color(34, 197, 94));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        yesBtn.setPreferredSize(new Dimension(110, 44));
        yesBtn.setContentAreaFilled(false);
        yesBtn.setBorderPainted(false);
        yesBtn.setFocusPainted(false);
        yesBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesBtn.addActionListener(e -> {
            servicesList.removeIf(s -> s[0].equals(name) && s[2].equals(price));
            saveServices();
            refreshServicesGrid();
            dialog.dispose();
        });

        JButton noBtn = new JButton("No") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(185, 28, 28) : new Color(239, 68, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        ((getHeight() - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        noBtn.setPreferredSize(new Dimension(110, 44));
        noBtn.setContentAreaFilled(false);
        noBtn.setBorderPainted(false);
        noBtn.setFocusPainted(false);
        noBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noBtn.addActionListener(e -> dialog.dispose());

        btnRow.add(yesBtn);
        btnRow.add(noBtn);

        root.add(question, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);

        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.add(root);
        dialog.setVisible(true);
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    private JTextField makeStyledField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? new Color(58, 103, 229) : new Color(203, 213, 225));
                g2.setStroke(isFocusOwner() ? new BasicStroke(1.8f) : new BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBackground(new Color(241, 245, 249));
        field.setForeground(new Color(30, 41, 59));
        field.setCaretColor(new Color(58, 103, 229));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        field.setPreferredSize(new Dimension(0, 46));

        // Placeholder text
        field.setText(placeholder);
        field.setForeground(new Color(148, 163, 184));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(30, 41, 59));
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(148, 163, 184));
                }
            }
        });
        return field;
    }

    private void refreshServicesGrid() {
        servicesGrid.removeAll();
        for (String[] s : servicesList) {
            servicesGrid.add(createServiceCard(s[0], s[1], s[2]));
        }
        servicesGrid.revalidate();
        servicesGrid.repaint();
    }

    private JPanel createServiceCard(String name, String desc, String price) {
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

        // Right column: add-to-cart button (top) + delete button (bottom)
        JPanel rightCol = new JPanel(new GridBagLayout());
        rightCol.setOpaque(false);

        GridBagConstraints rc = new GridBagConstraints();
        rc.gridx = 0;
        rc.fill = GridBagConstraints.NONE;
        rc.anchor = GridBagConstraints.CENTER;
        rc.insets = new Insets(0, 0, 6, 0);

        // "+" add button
        JButton addBtn = new JButton("+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(147, 197, 253));
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
        addBtn.setPreferredSize(new Dimension(32, 32));
        addBtn.setMinimumSize(new Dimension(32, 32));
        addBtn.setMaximumSize(new Dimension(32, 32));
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> addToCart(name, desc, Double.parseDouble(price)));

        rc.gridy = 0;
        rightCol.add(addBtn, rc);

        // Trash delete button
        JButton deleteBtn = new JButton("\uD83D\uDDD1") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(254, 202, 202) : new Color(255, 237, 237);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(220, 38, 38));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                int x = (getWidth() - fm.stringWidth(t)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(t, x, y);
                g2.dispose();
            }
        };
        deleteBtn.setPreferredSize(new Dimension(28, 28));
        deleteBtn.setMinimumSize(new Dimension(28, 28));
        deleteBtn.setMaximumSize(new Dimension(28, 28));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setToolTipText("Delete this service");
        deleteBtn.addActionListener(e -> showDeleteConfirmDialog(name, price));

        rc.gridy = 1;
        rc.insets = new Insets(0, 0, 0, 0);
        rightCol.add(deleteBtn, rc);

        card.add(rightCol, BorderLayout.EAST);
        return card;
    }

    private void loadServices() {
        File file = new File(SERVICES_FILE);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(in);
                int count = Integer.parseInt(props.getProperty("count", "0"));
                for (int i = 0; i < count; i++) {
                    String n = props.getProperty("service." + i + ".name", "");
                    String d = props.getProperty("service." + i + ".desc", "Per kg");
                    String p = props.getProperty("service." + i + ".price", "0.00");
                    if (!n.isEmpty()) {
                        servicesList.add(new String[]{ n, d, p });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // Fall back to defaults if nothing was loaded
        if (servicesList.isEmpty()) {
            servicesList.add(new String[]{ "Wash Only",    "Per kg", "10.00" });
            servicesList.add(new String[]{ "Dry Only",     "Per kg", "10.00" });
            servicesList.add(new String[]{ "Full Service", "Per kg", "24.29" });
            servicesList.add(new String[]{ "Fold Only",    "Per kg", "4.29"  });
            saveServices(); // persist defaults immediately
        }
    }

    private void saveServices() {
        Properties props = new Properties();
        props.setProperty("count", String.valueOf(servicesList.size()));
        for (int i = 0; i < servicesList.size(); i++) {
            props.setProperty("service." + i + ".name",  servicesList.get(i)[0]);
            props.setProperty("service." + i + ".desc",  servicesList.get(i)[1]);
            props.setProperty("service." + i + ".price", servicesList.get(i)[2]);
        }
        try (OutputStream out = new FileOutputStream(SERVICES_FILE)) {
            props.store(out, "LaundryPOS – saved services list");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

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
            nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameLabel.getPreferredSize().height));
            itemPanel.add(nameLabel);

            itemPanel.add(Box.createVerticalStrut(3));

            JLabel priceDescLabel = new JLabel("\u20B1" + String.format("%.2f", item.price) + " " + item.desc);
            priceDescLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            priceDescLabel.setForeground(new Color(100, 116, 139));
            priceDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            priceDescLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, priceDescLabel.getPreferredSize().height));
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
            priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); 
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
        totalLabel.setText("\u20B1" + String.format("%.2f", total)); 

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