package src.ui.panels.posPanel;

import java.awt.*;
import javax.swing.*;


public class CheckoutScreen extends JPanel {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextArea specialInstructionsArea;
    private ButtonGroup paymentGroup;
    private JRadioButton cashRadio;
    private JRadioButton gcashRadio;
    private java.util.ArrayList<LaundryPOS.Item> cartItems;
    private double subtotal;
    private double tax;
    private JLabel orderSummaryLabel;
    private LaundryPOS parentFrame;

    public CheckoutScreen() {
        this(null, new java.util.ArrayList<>(), 0, 0);
    }

    public CheckoutScreen(LaundryPOS parent, java.util.ArrayList<LaundryPOS.Item> cart, double subtotal, double tax) {
        this.parentFrame = parent;
        this.cartItems = cart;
        this.subtotal = subtotal;
        this.tax = tax;

        // Main panel with light blue background
        setLayout(new BorderLayout());
        setBackground(new Color(175, 238, 238));

        // Add left and right panels
        add(createLeftPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(175, 238, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 20));

        // Header with back button
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(175, 238, 238));
        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.setBackground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(CheckoutScreen.this);
            if (window != null) {
                window.dispose();
            }
        });
        JLabel titleLabel = new JLabel("CHECKOUT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(backButton);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(titleLabel);
        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(30));

        // 1. Contact Information
        JLabel contactLabel = new JLabel("1. Contact Information");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(contactLabel);
        panel.add(Box.createVerticalStrut(10));

        // First Name and Last Name
        JPanel namePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        namePanel.setBackground(new Color(175, 238, 238));

        JPanel firstNamePanel = new JPanel();
        firstNamePanel.setLayout(new BoxLayout(firstNamePanel, BoxLayout.Y_AXIS));
        firstNamePanel.setBackground(new Color(175, 238, 238));
        JLabel firstNameLabel = new JLabel("First Name");
        firstNameLabel.setForeground(new Color(100, 200, 200));
        firstNameField = new JTextField();
        firstNameField.setPreferredSize(new Dimension(200, 35));
        firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        firstNamePanel.add(firstNameLabel);
        firstNamePanel.add(firstNameField);

        JPanel lastNamePanel = new JPanel();
        lastNamePanel.setLayout(new BoxLayout(lastNamePanel, BoxLayout.Y_AXIS));
        lastNamePanel.setBackground(new Color(175, 238, 238));
        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameLabel.setForeground(new Color(100, 200, 200));
        lastNameField = new JTextField();
        lastNameField.setPreferredSize(new Dimension(200, 35));
        lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        lastNamePanel.add(lastNameLabel);
        lastNamePanel.add(lastNameField);

        namePanel.add(firstNamePanel);
        namePanel.add(lastNamePanel);
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(15));

        // Phone
        JPanel phonePanel = new JPanel();
        phonePanel.setLayout(new BoxLayout(phonePanel, BoxLayout.Y_AXIS));
        phonePanel.setBackground(new Color(175, 238, 238));
        phonePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel phoneLabel = new JLabel("Phone");
        phoneLabel.setForeground(new Color(100, 200, 200));
        phoneField = new JTextField();
        phoneField.setPreferredSize(new Dimension(250, 35));
        phoneField.setMaximumSize(new Dimension(250, 35));
        phonePanel.add(phoneLabel);
        phonePanel.add(phoneField);
        panel.add(phonePanel);
        panel.add(Box.createVerticalStrut(25));

        // 2. Special Instructions
        JLabel specialLabel = new JLabel("2. Special Instructions");
        specialLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(specialLabel);
        panel.add(Box.createVerticalStrut(10));

        specialInstructionsArea = new JTextArea(6, 40);
        specialInstructionsArea.setLineWrap(true);
        specialInstructionsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(specialInstructionsArea);
        scrollPane.setPreferredSize(new Dimension(450, 130));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(25));

        // 3. Payment Method
        JLabel paymentLabel = new JLabel("3. Payment Method");
        paymentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(paymentLabel);
        panel.add(Box.createVerticalStrut(10));

        paymentGroup = new ButtonGroup();

        // Cash option
        JPanel cashPanel = new JPanel(new BorderLayout());
        cashPanel.setBackground(Color.WHITE);
        cashPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        cashPanel.setPreferredSize(new Dimension(450, 50));
        cashPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel cashIcon = new JLabel("₱");
        cashIcon.setFont(new Font("Arial", Font.BOLD, 24));
        cashIcon.setPreferredSize(new Dimension(40, 40));
        cashIcon.setHorizontalAlignment(SwingConstants.CENTER);
        cashIcon.setBackground(Color.WHITE);
        cashIcon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        cashIcon.setOpaque(true);

        JLabel cashLabel = new JLabel("Cash");
        cashLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        cashRadio = new JRadioButton();
        cashRadio.setSelected(true);
        cashRadio.setBackground(Color.WHITE);

        JPanel cashLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cashLeftPanel.setBackground(Color.WHITE);
        cashLeftPanel.add(cashIcon);
        cashLeftPanel.add(cashLabel);

        cashPanel.add(cashLeftPanel, BorderLayout.WEST);
        cashPanel.add(cashRadio, BorderLayout.EAST);
        paymentGroup.add(cashRadio);

        panel.add(cashPanel);
        panel.add(Box.createVerticalStrut(15));

        // GCash option
        JPanel gcashPanel = new JPanel(new BorderLayout());
        gcashPanel.setBackground(Color.WHITE);
        gcashPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        gcashPanel.setPreferredSize(new Dimension(450, 50));
        gcashPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel gcashIcon = new JLabel("G");
        gcashIcon.setFont(new Font("Arial", Font.BOLD, 18));
        gcashIcon.setForeground(Color.WHITE);
        gcashIcon.setPreferredSize(new Dimension(40, 40));
        gcashIcon.setHorizontalAlignment(SwingConstants.CENTER);
        gcashIcon.setBackground(new Color(0, 122, 255));
        gcashIcon.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 255), 1));
        gcashIcon.setOpaque(true);

        JLabel gcashLabel = new JLabel("G-CASH");
        gcashLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        gcashRadio = new JRadioButton();
        gcashRadio.setBackground(Color.WHITE);

        JPanel gcashLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        gcashLeftPanel.setBackground(Color.WHITE);
        gcashLeftPanel.add(gcashIcon);
        gcashLeftPanel.add(gcashLabel);

        gcashPanel.add(gcashLeftPanel, BorderLayout.WEST);
        gcashPanel.add(gcashRadio, BorderLayout.EAST);
        paymentGroup.add(gcashRadio);

        panel.add(gcashPanel);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Order summary content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Service header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setBackground(new Color(230, 240, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel iconLabel = new JLabel("🧺");
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(230, 240, 255));

        JLabel serviceLabel = new JLabel("Spin Laundromat Service");
        serviceLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel addressLabel = new JLabel(
                "<html>148 Garrick st., Brgy. 165, Zone 20,<br>Rivera Village NAA Pasay City</html>");
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        addressLabel.setForeground(Color.GRAY);

        textPanel.add(serviceLabel);
        textPanel.add(addressLabel);

        headerPanel.add(iconLabel);
        headerPanel.add(textPanel);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Order number
        JLabel orderLabel = new JLabel("Order #001");
        orderLabel.setFont(new Font("Arial", Font.BOLD, 18));
        orderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(orderLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Quantity and items in cart
        orderSummaryLabel = new JLabel("x" + cartItems.size());
        orderSummaryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        orderSummaryLabel.setForeground(Color.GRAY);
        contentPanel.add(orderSummaryLabel);

        contentPanel.add(Box.createVerticalStrut(15));

        // Cart items list
        for (LaundryPOS.Item item : cartItems) {
            double itemTotal = item.price * (item.qty / 7.0);

            JLabel itemLabel = new JLabel(item.name + " x" + item.qty + "kg - ₱" + String.format("%.2f", itemTotal));
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(itemLabel);
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // Totals
        JLabel subtotalLabel = new JLabel("Subtotal: ₱" + String.format("%.2f", subtotal));
        subtotalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subtotalLabel);

        JLabel taxLabel = new JLabel("Tax(12%): ₱" + String.format("%.2f", tax));
        taxLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        taxLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(taxLabel);

        double total = subtotal + tax;
        JLabel totalLabel = new JLabel("Total: ₱" + String.format("%.2f", total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(totalLabel);

        panel.add(contentPanel, BorderLayout.NORTH);

        // Confirm button at bottom
        JButton confirmButton = new JButton("Confirm Checkout");
        confirmButton.setBackground(new Color(34, 197, 94));
        confirmButton.setForeground(Color.BLACK);
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        confirmButton.setPreferredSize(new Dimension(260, 45));
        confirmButton.addActionListener(e -> {
            String paymentMethod = cashRadio.isSelected() ? "Cash" : "G-CASH";

            // Show confirmation message
            String message = """
                    Order confirmed!
                    Name: """ + firstNameField.getText() + " " + lastNameField.getText() + """

                    Phone: """ + phoneField.getText() + """

                    Payment: """ + paymentMethod + """

                    Total: ₱""" + String.format("%.2f", subtotal + tax);

            JOptionPane.showMessageDialog(CheckoutScreen.this,
                    message,
                    "Checkout Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear cart in parent POS
            if (parentFrame != null) {
                parentFrame.clearCart();
            }

            // Close the checkout dialog
            Window window = SwingUtilities.getWindowAncestor(CheckoutScreen.this);
            if (window != null) {
                window.dispose();
            }
        });

        panel.add(confirmButton, BorderLayout.SOUTH);

        return panel;
    }
}