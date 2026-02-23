package src.ui.panels.posPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import src.util.components.Scrollbar;
import src.util.components.ShadowPanel;
import src.util.config.UIStyle;

public class CheckoutScreen extends JPanel {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextArea specialInstructionsArea;
    private ButtonGroup paymentGroup;
    private JRadioButton cashRadio;
    private JRadioButton gcashRadio;
    private ArrayList<LaundryPOS.Item> cartItems;
    private double subtotal;
    private double tax;
    private LaundryPOS parentFrame;
    private String orderNumber;

    // ─── Brand Constants ─────────────────────────────────────────────────────
    private static final String STORE_NAME = "Machine Wash";
    private static final String STORE_ADDRESS = "143 Lamar St., Brgy. 164 Zone 13,\nRivera Village NAIA, Pasay City";

    public CheckoutScreen() {
        this(null, new ArrayList<>(), 0, 0);
    }

    public CheckoutScreen(LaundryPOS parent, ArrayList<LaundryPOS.Item> cart,
            double subtotal, double tax) {
        this.parentFrame = parent;
        this.cartItems = cart;
        this.subtotal = subtotal;
        this.tax = tax;
        this.orderNumber = generateOrderNumber();

        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);

        // ── Top Header (back button + title) ─────────────────────────────────
        add(buildHeader(), BorderLayout.NORTH);

        // ── Two-column body wrapped in vertical-only scroll pane ─────────────
        JPanel body = new JPanel(new BorderLayout(28, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 48, 40, 48));

        // Left: stacked form cards
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(buildContactCard());
        leftCol.add(Box.createVerticalStrut(18));
        leftCol.add(buildInstructionsCard());
        leftCol.add(Box.createVerticalStrut(18));
        leftCol.add(buildPaymentCard());

        body.add(leftCol, BorderLayout.CENTER);
        body.add(buildOrderSummaryCard(), BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HEADER
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 48, 22));
        inner.setOpaque(false);

        // ── Circular back button ──────────────────────────────────────────────
        JButton backBtn = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    // filled blue on hover
                    g2.setColor(UIStyle.PRIMARY);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                } else {
                    // white circle with border idle
                    g2.setColor(Color.WHITE);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                    g2.setColor(UIStyle.TEXT_PRIMARY);
                }

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                // shift arrow 1 px left for optical centre
                int x = (getWidth() - fm.stringWidth(getText())) / 2 - 1;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backBtn.setPreferredSize(new Dimension(42, 42));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null)
                w.dispose();
        });

        // ── CHECKOUT title ────────────────────────────────────────────────────
        JLabel title = new JLabel("CHECKOUT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UIStyle.TEXT_PRIMARY);

        inner.add(backBtn);
        inner.add(title);

        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LEFT COLUMN CARDS
    // ══════════════════════════════════════════════════════════════════════════

    // ── 1. Contact Information ────────────────────────────────────────────────
    private JPanel buildContactCard() {
        ShadowPanel card = new ShadowPanel();
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(26, 26, 26, 26));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets = new Insets(0, 0, 6, 0);

        // Section label
        JLabel sectionLabel = buildSectionLabel("1. Contact Information");
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        inner.add(sectionLabel, g);

        g.gridwidth = 1;
        g.insets = new Insets(14, 0, 0, 12);
        g.gridy = 1;
        g.gridx = 0;
        inner.add(buildFieldGroup("First Name", firstNameField = makeTextField("First name")), g);
        g.gridx = 1;
        g.insets = new Insets(14, 0, 0, 0);
        inner.add(buildFieldGroup("Last Name", lastNameField = makeTextField("Last name")), g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 1;
        g.insets = new Insets(14, 0, 0, 12);
        inner.add(buildFieldGroup("Phone", phoneField = makeTextField("09XX-XXX-XXXX")), g);

        card.add(inner);
        return card;
    }

    // ── 2. Special Instructions ───────────────────────────────────────────────
    private JPanel buildInstructionsCard() {
        ShadowPanel card = new ShadowPanel();
        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(26, 26, 26, 26));

        inner.add(buildSectionLabel("2. Special Instructions"), BorderLayout.NORTH);

        specialInstructionsArea = new JTextArea(5, 20);
        specialInstructionsArea.setFont(UIStyle.REGULAR);
        specialInstructionsArea.setLineWrap(true);
        specialInstructionsArea.setWrapStyleWord(true);
        specialInstructionsArea.setBorder(new EmptyBorder(8, 12, 8, 12));
        specialInstructionsArea.setBackground(new Color(248, 250, 252));
        specialInstructionsArea.setOpaque(true);

        JScrollPane areaScroll = new JScrollPane(specialInstructionsArea);
        areaScroll.setBorder(null);
        areaScroll.setOpaque(false);
        areaScroll.getViewport().setOpaque(false);
        areaScroll.getVerticalScrollBar().setUI(new Scrollbar());
        areaScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Rounded wrapper panel — blue border on focus, same as input fields
        boolean[] areaFocused = { false };
        JPanel areaWrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(areaFocused[0] ? UIStyle.PRIMARY : UIStyle.INPUT_BORDER);
                g2.setStroke(areaFocused[0] ? new BasicStroke(1.8f) : new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        areaWrap.setOpaque(false);
        specialInstructionsArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                areaFocused[0] = true;
                areaWrap.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                areaFocused[0] = false;
                areaWrap.repaint();
            }
        });
        areaWrap.add(areaScroll, BorderLayout.CENTER);

        inner.add(areaWrap, BorderLayout.CENTER);
        card.add(inner);
        return card;
    }

    // ── 3. Payment Method ─────────────────────────────────────────────────────
    private JPanel buildPaymentCard() {
        ShadowPanel card = new ShadowPanel();
        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(26, 26, 26, 26));

        inner.add(buildSectionLabel("3. Payment Method"), BorderLayout.NORTH);

        paymentGroup = new ButtonGroup();
        JPanel optionsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        optionsRow.setOpaque(false);

        cashRadio = buildPaymentOption("Cash", buildCashIcon(), true);
        gcashRadio = buildPaymentOption("G-CASH", buildGcashIcon(), false);

        optionsRow.add(cashRadio.getParent());
        optionsRow.add(gcashRadio.getParent());

        inner.add(optionsRow, BorderLayout.CENTER);
        card.add(inner);
        return card;
    }

    /** Builds one payment tile and returns its JRadioButton. */
    private JRadioButton buildPaymentOption(String label, JComponent iconComp, boolean selected) {
        // Tile panel
        JPanel tile = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(UIStyle.INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        tile.setOpaque(false);
        tile.setBorder(new EmptyBorder(14, 16, 14, 16));
        tile.add(iconComp, BorderLayout.WEST);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UIStyle.TEXT_PRIMARY);
        tile.add(lbl, BorderLayout.CENTER);

        JRadioButton rb = new JRadioButton();
        rb.setSelected(selected);
        rb.setOpaque(false);
        rb.setUI(new BasicRadioButtonUI() {
            @Override
            public synchronized void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = 18, x = (c.getWidth() - sz) / 2, y = (c.getHeight() - sz) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, sz, sz);
                g2.setColor(UIStyle.INPUT_BORDER);
                g2.drawOval(x, y, sz, sz);
                if (((AbstractButton) c).getModel().isSelected()) {
                    g2.setColor(UIStyle.PRIMARY);
                    g2.fillOval(x + 4, y + 4, sz - 8, sz - 8);
                }
                g2.dispose();
            }
        });
        paymentGroup.add(rb);
        tile.add(rb, BorderLayout.EAST);
        return rb;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RIGHT COLUMN – ORDER SUMMARY CARD
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildOrderSummaryCard() {
        ShadowPanel card = new ShadowPanel();
        card.setPreferredSize(new Dimension(340, 0));

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── TOP: Store branding banner ────────────────────────────────────────
        JPanel brandBanner = buildBrandBanner();
        inner.add(brandBanner, BorderLayout.NORTH);

        // ── CENTER: Order number + items + totals ─────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        // Order number chip
        JPanel orderChip = buildOrderChip();
        centerPanel.add(orderChip);
        centerPanel.add(Box.createVerticalStrut(16));

        // Items
        for (LaundryPOS.Item item : cartItems) {
            centerPanel.add(summaryRow(
                    item.name + " (x" + item.qty + "kg)",
                    "₱" + String.format("%.2f", item.price * item.qty),
                    false));
            centerPanel.add(Box.createVerticalStrut(8));
        }
        if (cartItems.isEmpty()) {
            JLabel empty = new JLabel("No items in cart.");
            empty.setFont(UIStyle.SMALL);
            empty.setForeground(UIStyle.TEXT_SECONDARY);
            centerPanel.add(empty);
        }

        centerPanel.add(Box.createVerticalStrut(16));

        // Separator
        centerPanel.add(buildDivider());
        centerPanel.add(Box.createVerticalStrut(14));

        // Subtotal / Tax
        centerPanel.add(summaryRow("Subtotal", "₱" + String.format("%.2f", subtotal), false));
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(summaryRow("Tax (12%)", "₱" + String.format("%.2f", tax), false));
        centerPanel.add(Box.createVerticalStrut(14));

        // Total row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel totalLbl = new JLabel("TOTAL");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalLbl.setForeground(UIStyle.TEXT_SECONDARY);

        JLabel totalAmt = new JLabel("₱" + String.format("%.2f", subtotal + tax));
        totalAmt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalAmt.setForeground(UIStyle.PRIMARY);

        totalRow.add(totalLbl, BorderLayout.WEST);
        totalRow.add(totalAmt, BorderLayout.EAST);

        centerPanel.add(totalRow);
        inner.add(centerPanel, BorderLayout.CENTER);

        // ── BOTTOM: Confirm button ────────────────────────────────────────────
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setBorder(new EmptyBorder(0, 22, 22, 22));

        JButton confirmBtn = new JButton("Confirm Checkout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isRollover() ? new Color(22, 163, 74) : UIStyle.SUCCESS;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        confirmBtn.setPreferredSize(new Dimension(0, 50));
        confirmBtn.setContentAreaFilled(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> processOrder());

        btnWrap.add(confirmBtn, BorderLayout.CENTER);
        inner.add(btnWrap, BorderLayout.SOUTH);

        card.add(inner);
        return card;
    }

    /** Store logo + name + address block at top of summary card. */
    private JPanel buildBrandBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // gentle gradient background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(219, 234, 254),
                        0, getHeight(), new Color(207, 250, 254));
                g2.setPaint(gp);
                // round only the top corners
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 18, 18);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Logo with blue box background
        JPanel logoContainer = new JPanel() {
            private Image brandImg;
            {
                try {
                    java.net.URL url = getClass().getResource("/src/resources/images/machine_wash.png");
                    if (url != null) {
                        brandImg = new ImageIcon(url).getImage();
                    }
                } catch (Exception e) {
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Blue background box
                g2.setColor(UIStyle.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                if (brandImg != null) {
                    int imgW = 28;
                    int imgH = 28;
                    int x = (getWidth() - imgW) / 2;
                    int y = (getHeight() - imgH) / 2;
                    g2.drawImage(brandImg, x, y, imgW, imgH, null);
                }
                g2.dispose();
            }
        };
        logoContainer.setPreferredSize(new Dimension(42, 42));
        logoContainer.setOpaque(false);

        // Name + address
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel nameLabel = new JLabel(STORE_NAME);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel addrLabel = new JLabel(
                "<html><div style='line-height:1.4'>" + STORE_ADDRESS.replace("\n", "<br>") + "</div></html>");
        addrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addrLabel.setForeground(UIStyle.TEXT_SECONDARY);

        textBlock.add(nameLabel);
        textBlock.add(Box.createVerticalStrut(3));
        textBlock.add(addrLabel);

        banner.add(logoContainer, BorderLayout.WEST);
        banner.add(textBlock, BorderLayout.CENTER);
        return banner;
    }

    /** "Order #001" chip displayed just below the brand banner. */
    private JPanel buildOrderChip() {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chip.setOpaque(false);
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel orderLbl = new JLabel(orderNumber);
        orderLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        orderLbl.setForeground(UIStyle.TEXT_PRIMARY);
        chip.add(orderLbl);
        return chip;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PAYMENT ICON HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Peso-sign icon circle (Cash). */
    private JComponent buildCashIcon() {
        JPanel circle = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(40, 40));
        JLabel lbl = new JLabel("₱");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(UIStyle.PRIMARY);
        circle.add(lbl);
        return circle;
    }

    /** G·Cash logo-like icon. */
    private JComponent buildGcashIcon() {
        JPanel box = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // GCash brand blue
                g2.setColor(new Color(0, 119, 196));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(40, 40));
        JLabel lbl = new JLabel("G");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        box.add(lbl);
        return box;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SHARED MINI-COMPONENTS
    // ══════════════════════════════════════════════════════════════════════════

    private JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(UIStyle.TEXT_SECONDARY);
        return lbl;
    }

    private JPanel buildFieldGroup(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UIStyle.TEXT_SECONDARY);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField makeTextField(String placeholder) {
        // Rounded text field via custom paintComponent
        JTextField f = new JTextField() {
            private static final int R = 12;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), R * 2, R * 2);
                // clip text inside rounded rect
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), R * 2, R * 2));
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? UIStyle.PRIMARY : UIStyle.INPUT_BORDER);
                g2.setStroke(isFocusOwner() ? new BasicStroke(1.8f) : new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, R * 2, R * 2);
                g2.dispose();
            }
        };
        f.setFont(UIStyle.REGULAR);
        f.setPreferredSize(new Dimension(0, 44));
        f.setBorder(new EmptyBorder(0, 14, 0, 14));
        f.setBackground(Color.WHITE);
        f.setForeground(UIStyle.TEXT_PRIMARY);
        f.setCaretColor(UIStyle.PRIMARY);
        f.setOpaque(false);
        return f;
    }

    private JPanel summaryRow(String label, String value, boolean bold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel l = new JLabel(label);
        l.setFont(bold
                ? new Font("Segoe UI", Font.BOLD, 13)
                : new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(UIStyle.TEXT_SECONDARY);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(UIStyle.TEXT_PRIMARY);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIStyle.INPUT_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BUSINESS LOGIC
    // ══════════════════════════════════════════════════════════════════════════

    private String generateOrderNumber() {
        int num = 1 + new Random().nextInt(999);
        return String.format("Order #%03d", num);
    }

    private void processOrder() {
        String payment = cashRadio.isSelected() ? "Cash" : "G-CASH";
        String name = firstNameField.getText().trim() + " " + lastNameField.getText().trim();

        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your name.", "Required Field",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String msg = "✅ Order Confirmed!\n\n"
                + orderNumber + "\n"
                + "Customer : " + name + "\n"
                + "Phone    : " + phoneField.getText() + "\n"
                + "Payment  : " + payment + "\n"
                + "Total    : ₱" + String.format("%.2f", subtotal + tax);
        JOptionPane.showMessageDialog(this, msg,
                "Order Placed Successfully", JOptionPane.INFORMATION_MESSAGE);

        if (parentFrame != null)
            parentFrame.clearCart();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null)
            w.dispose();
    }
}
