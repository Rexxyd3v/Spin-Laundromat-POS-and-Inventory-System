package ui.panels.posPanel;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import model.*;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

public class CheckoutScreen extends JPanel {
    private final LaundryPOS parent;
    private final List<OrderPricing.Line> lines;
    private final OrderPricing.Totals totals;
    private final PosSettings.Values settings;
    private final String orderId = UUID.randomUUID().toString();
    private final JTextField customer = field("Customer name"), phone = field("Phone number"),
            cash = field("Cash received"), reference = field("Payment reference");
    private final JTextArea notes = new JTextArea(3, 24);
    private final JComboBox<String> method;
    private final JLabel feedback = label("", UIStyle.SMALL, UIStyle.DANGER),
            change = label("", UIStyle.BOLD, UIStyle.PRIMARY);
    private final JButton confirm = button("Confirm & save order", AppIcon.Name.CHECK, true);
    private final JPanel paymentFields = new JPanel(new CardLayout());

    public CheckoutScreen() {
        this(null, List.of(), new OrderPricing.Totals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                persistence.Repositories.settings().get());
    }

    public CheckoutScreen(LaundryPOS parent, List<OrderPricing.Line> lines, OrderPricing.Totals totals,
            PosSettings.Values settings) {
        this.parent = parent;
        this.lines = List.copyOf(lines);
        this.totals = totals;
        this.settings = settings;
        method = combo(settings.gcashEnabled() ? new String[] { "Cash", "GCash" } : new String[] { "Cash" });
        setLayout(new BorderLayout(0, 22));
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        JPanel header = column();
        header.add(label("Checkout", UIStyle.TITLE, UIStyle.TEXT_PRIMARY));
        header.add(Box.createVerticalStrut(6));
        header.add(label("Review the order and record payment.", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        add(header, BorderLayout.NORTH);
        JPanel form = column();
        form.setBorder(new EmptyBorder(0, 0, 0, 18));
        addField(form, formField("Customer name", customer));
        addField(form, formField("Phone (optional)", phone));
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(UIStyle.REGULAR);
        notes.setBorder(new EmptyBorder(10, 10, 10, 10));
        notes.getAccessibleContext().setAccessibleName("Order notes");
        addField(form, formField("Special instructions (optional)", new JScrollPane(notes)));
        addField(form, formField("Payment method", method));
        paymentFields.setOpaque(false);
        paymentFields.setAlignmentX(LEFT_ALIGNMENT);
        JPanel cashPanel = column();
        addField(cashPanel, formField("Cash received", cash));
        addField(cashPanel, change);
        JPanel refPanel = column();
        addField(refPanel, formField("GCash reference", reference));
        addField(refPanel,
                label("Verify the payment in your GCash account first.", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        paymentFields.add(cashPanel, "Cash");
        paymentFields.add(refPanel, "GCash");
        paymentFields.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        form.add(paymentFields);
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.add(scroll(form));
        body.add(summary());
        add(body, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        footer.add(feedback, BorderLayout.NORTH);
        footer.add(confirm, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        method.addActionListener(
                e -> ((CardLayout) paymentFields.getLayout()).show(paymentFields, (String) method.getSelectedItem()));
        cash.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateChange();
            }

            public void removeUpdate(DocumentEvent e) {
                updateChange();
            }

            public void changedUpdate(DocumentEvent e) {
                updateChange();
            }
        });
        cash.setText(totals.total().toPlainString());
        confirm.setEnabled(!lines.isEmpty());
        confirm.addActionListener(e -> processOrder());
    }

    public void setOrderDetails(String name, String instructions) {
        customer.setText(name);
        notes.setText(instructions);
    }

    private void addField(JPanel p, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        p.add(c);
        p.add(Box.createVerticalStrut(16));
    }

    private JPanel summary() {
        Surface surface = new Surface();
        JPanel p = column();
        p.add(label(settings.storeName(), UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        p.add(Box.createVerticalStrut(6));
        p.add(label("ORDER SUMMARY", UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        p.add(Box.createVerticalStrut(20));
        for (OrderPricing.Line l : lines) {
            p.add(label(l.name(), UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
            p.add(Box.createVerticalStrut(4));
            p.add(label(
                    l.quantity().stripTrailingZeros().toPlainString() + " " + l.unit() + " · "
                            + OrderPricing.money(l.unitPrice()) + " / " + l.unit(),
                    UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
            p.add(Box.createVerticalStrut(4));
            p.add(label(OrderPricing.money(l.total()), UIStyle.BOLD, UIStyle.TEXT_PRIMARY));
            p.add(Box.createVerticalStrut(18));
        }
        surface.add(scroll(p), BorderLayout.CENTER);
        JPanel total = column();
        total.add(new JSeparator());
        total.add(Box.createVerticalStrut(15));
        total.add(
                label("Subtotal   " + OrderPricing.money(totals.subtotal()), UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        total.add(label("Tax   " + OrderPricing.money(totals.tax()), UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        total.add(Box.createVerticalStrut(12));
        total.add(label(OrderPricing.money(totals.total()), UIStyle.TITLE, UIStyle.PRIMARY));
        surface.add(total, BorderLayout.SOUTH);
        return surface;
    }

    private BigDecimal cashAmount() {
        BigDecimal value = new BigDecimal(cash.getText().trim());
        if (value.signum() < 0 || value.scale() > 2 || value.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new NumberFormatException();
        return value;
    }

    private void updateChange() {
        try {
            BigDecimal c = cashAmount().subtract(totals.total());
            change.setText(
                    c.signum() < 0 ? "Remaining: " + OrderPricing.money(c.abs()) : "Change: " + OrderPricing.money(c));
        } catch (NumberFormatException ex) {
            change.setText("Enter an amount with up to two decimal places.");
        }
    }

    private void processOrder() {
        if (customer.getText().isBlank()) {
            feedback.setText("Enter the customer name.");
            customer.requestFocusInWindow();
            return;
        }
        String selected = (String) method.getSelectedItem();
        BigDecimal tendered = totals.total();
        if (selected.equals("Cash")) {
            try {
                tendered = cashAmount();
                if (tendered.compareTo(totals.total()) < 0) {
                    feedback.setText("Cash received is less than the order total.");
                    return;
                }
            } catch (NumberFormatException ex) {
                feedback.setText("Enter a valid cash amount with up to two decimal places.");
                return;
            }
        } else if (reference.getText().isBlank()) {
            feedback.setText("Enter the verified payment reference.");
            reference.requestFocusInWindow();
            return;
        }
        OrderStore.Order order = new OrderStore.Order(orderId, customer.getText().trim(), phone.getText().trim(),
                notes.getText().trim(), selected, reference.getText().trim(), tendered, lines, totals, settings);
        setClosingEnabled(false);
        confirm.setEnabled(false);
        feedback.setForeground(UIStyle.TEXT_SECONDARY);
        feedback.setText("Saving your order…");
        new SwingWorker<persistence.RepositoryProvider.Confirmation, Void>() {
            protected persistence.RepositoryProvider.Confirmation doInBackground() throws Exception {
                return new application.CheckoutService().confirm(order);
            }

            protected void done() {
                try {
                    var confirmed = get();
                    setClosingEnabled(true);
                    if (parent != null)
                        parent.clearCart();
                    showReceipt(confirmed.receipt());
                } catch (Exception ex) {
                    setClosingEnabled(true);
                    confirm.setEnabled(true);
                    feedback.setForeground(UIStyle.DANGER);
                    feedback.setText("Order could not be saved. Your cart is kept. Check stock and try again.");
                }
            }
        }.execute();
    }

    private void setClosingEnabled(boolean enabled) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JDialog dialog)
            dialog.setDefaultCloseOperation(enabled ? JDialog.DISPOSE_ON_CLOSE : JDialog.DO_NOTHING_ON_CLOSE);
    }

    private void showReceipt(String savedReceipt) {
        removeAll();
        JPanel title = column();
        JLabel success = label("Order saved", UIStyle.TITLE, UIStyle.SUCCESS);
        success.setIcon(new AppIcon(AppIcon.Name.CHECK, 28, UIStyle.SUCCESS));
        title.add(success);
        title.add(label("Your receipt is ready.", UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        add(title, BorderLayout.NORTH);
        add(new ReceiptView(savedReceipt, false), BorderLayout.CENTER);
        JPanel actions = row(10);
        JButton done = button("New order", AppIcon.Name.PLUS, false);
        actions.add(done);
        add(actions, BorderLayout.SOUTH);
        done.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null)
                w.dispose();
        });
        revalidate();
        repaint();
    }
}
