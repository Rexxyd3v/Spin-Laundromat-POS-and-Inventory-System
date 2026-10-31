package model;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;
import util.config.*;

/**
 * A complete order is committed as one file, including its pricing snapshot.
 */
public final class OrderStore {
    public record Order(String id, String customer, String phone, String notes, String payment, String reference,
            BigDecimal tendered, List<OrderPricing.Line> lines, OrderPricing.Totals totals,
            PosSettings.Values settings) {
        public Order {
            lines = List.copyOf(lines);
            if (lines.isEmpty() || customer.isBlank())
                throw new IllegalArgumentException("An order needs a customer and at least one item.");
        }
    }

    public static Path save(Order order) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            UUID.fromString(order.id());
            Path file = AtomicProperties.dataPath("orders").resolve(order.id() + ".properties");
            if (java.nio.file.Files.exists(file))
                throw new java.nio.file.FileAlreadyExistsException(file.toString());
            OrderPricing.Totals expected = OrderPricing.calculate(order.lines(), order.settings().taxEnabled(),
                    order.settings().taxRate());
            if (!expected.equals(order.totals()))
                throw new IllegalArgumentException("Order totals do not match the line items.");
            StockStore.ensureAvailable(order.lines());
            Properties p = new Properties();
            p.setProperty("id", order.id());
            p.setProperty("created", OffsetDateTime.now().toString());
            p.setProperty("customer", order.customer());
            p.setProperty("cashier.id", ActiveProfile.current().id());
            p.setProperty("cashier.name", ActiveProfile.current().name());
            p.setProperty("phone", order.phone());
            p.setProperty("notes", order.notes());
            p.setProperty("payment", order.payment());
            p.setProperty("reference", order.reference());
            p.setProperty("tendered", order.tendered().toPlainString());
            p.setProperty("subtotal", order.totals().subtotal().toPlainString());
            p.setProperty("tax", order.totals().tax().toPlainString());
            p.setProperty("total", order.totals().total().toPlainString());
            p.setProperty("tax.rate", order.settings().taxRate().toPlainString());
            p.setProperty("tax.enabled", Boolean.toString(order.settings().taxEnabled()));
            p.setProperty("store.name", order.settings().storeName());
            p.setProperty("store.address", order.settings().address());
            p.setProperty("store.phone", order.settings().phone());
            p.setProperty("receipt.footer", order.settings().receiptFooter());
            p.setProperty("line.count", Integer.toString(order.lines().size()));
            for (int i = 0; i < order.lines().size(); i++) {
                OrderPricing.Line l = order.lines().get(i);
                String k = "line." + i + ".";
                p.setProperty(k + "id", l.id());
                p.setProperty(k + "name", l.name());
                p.setProperty(k + "unit", l.unit());
                p.setProperty(k + "quantity", l.quantity().toPlainString());
                p.setProperty(k + "price", l.unitPrice().toPlainString());
                p.setProperty(k + "total", l.total().toPlainString());
            }
            p.setProperty("branch.id", "main");
            p.setProperty("branch.name", OperationsStore.branches().stream().filter(b -> b.id().equals("main"))
                    .findFirst().orElseThrow().name());
            p.setProperty("receipt.snapshot", receipt(order, p.getProperty("cashier.name"), p.getProperty("created"),
                    p.getProperty("branch.name")));
            AtomicProperties.write(file, p);
            AppEvents.fire("orders");
            return file;

        }
    }

    public static String receipt(Order o) {
        return receipt(o, ActiveProfile.current().name());
    }

    public static String receipt(Order o, String cashierName) {
        return receipt(o, cashierName, "", "");
    }

    public static String receipt(Order o, String cashierName, String created, String branchName) {
        StringBuilder s = new StringBuilder(
                o.settings().storeName() + "\n" + o.settings().address() + "\n" + o.settings().phone() + "\n\nORDER "
                        + o.id() + "\nCustomer: " + o.customer() + "\nPhone: " + o.phone() + "\n\n");
        if (!created.isBlank())
            s.append("Date: ")
                    .append(java.time.OffsetDateTime.parse(created)
                            .atZoneSameInstant(java.time.ZoneId.of("Asia/Manila"))
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")))
                    .append("\n");
        if (!branchName.isBlank())
            s.append("Branch: ").append(branchName).append("\n");
        s.append("Processed by: ").append(cashierName).append("\n\n");
        for (OrderPricing.Line l : o.lines())
            s.append(l.name()).append("\n  ").append(l.quantity().stripTrailingZeros().toPlainString()).append(' ')
                    .append(l.unit()).append(" x ").append(OrderPricing.money(l.unitPrice())).append("   ")
                    .append(OrderPricing.money(l.total())).append('\n');
        s.append("\nSubtotal: ").append(OrderPricing.money(o.totals().subtotal())).append("\nTax: ")
                .append(OrderPricing.money(o.totals().tax())).append("\nTOTAL: ")
                .append(OrderPricing.money(o.totals().total())).append("\nPayment: ").append(o.payment());
        if (o.payment().equals("Cash"))
            s.append("\nReceived: ").append(OrderPricing.money(o.tendered())).append("\nChange: ")
                    .append(OrderPricing.money(o.tendered().subtract(o.totals().total())));
        else
            s.append("\nReference: ").append(o.reference());
        if (!o.notes().isBlank())
            s.append("\nNotes: ").append(o.notes());
        return s.append("\n\n").append(o.settings().receiptFooter()).append('\n').toString();
    }

    private OrderStore() {
    }
}
