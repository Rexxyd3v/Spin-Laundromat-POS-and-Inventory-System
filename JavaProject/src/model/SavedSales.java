package model;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import util.config.*;

/** Read-only projection of completed POS orders for reports and inventory. */
public final class SavedSales {
    public record Sale(String id, String customer, String phone, OffsetDateTime created, String payment,
            BigDecimal subtotal, BigDecimal tax, BigDecimal total, java.util.List<OrderPricing.Line> lines,
            String receipt) {
    }

    public record Result(java.util.List<Sale> sales, int unreadable) {
    }

    public static Result read() throws IOException {
        Path folder = AtomicProperties.dataPath("orders");
        if (!Files.exists(folder))
            return new Result(java.util.List.of(), 0);
        java.util.List<Sale> result = new ArrayList<>();
        int unreadable = 0;
        try (var paths = Files.list(folder)) {
            for (Path path : paths.filter(p -> p.getFileName().toString().endsWith(".properties")).toList()) {
                try {
                    Properties p = AtomicProperties.read(path);
                    int count = Integer.parseInt(p.getProperty("line.count"));
                    if (count < 1 || count > 10000)
                        throw new IllegalArgumentException();
                    java.util.List<OrderPricing.Line> lines = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        String k = "line." + i + ".";
                        lines.add(new OrderPricing.Line(p.getProperty(k + "id"), p.getProperty(k + "name"),
                                p.getProperty(k + "unit"), new BigDecimal(p.getProperty(k + "quantity")),
                                new BigDecimal(p.getProperty(k + "price"))));
                    }
                    OrderPricing.Totals totals = new OrderPricing.Totals(new BigDecimal(p.getProperty("subtotal")),
                            new BigDecimal(p.getProperty("tax")), new BigDecimal(p.getProperty("total")));
                    PosSettings.Values s = new PosSettings.Values(p.getProperty("store.name", "Laundry"),
                            p.getProperty("store.address", ""), p.getProperty("store.phone", ""),
                            p.getProperty("receipt.footer", ""), "Teal", false, false,
                            Boolean.parseBoolean(p.getProperty("tax.enabled", "true")),
                            new BigDecimal(p.getProperty("tax.rate", "0")), true);
                    OrderStore.Order order = new OrderStore.Order(p.getProperty("id"), p.getProperty("customer"),
                            p.getProperty("phone", ""), p.getProperty("notes", ""), p.getProperty("payment"),
                            p.getProperty("reference", ""), new BigDecimal(p.getProperty("tendered")), lines, totals,
                            s);
                    result.add(new Sale(order.id(), order.customer(), order.phone(),
                            OffsetDateTime.parse(p.getProperty("created")), order.payment(), totals.subtotal(),
                            totals.tax(), totals.total(), java.util.List.copyOf(lines),
                            p.getProperty("receipt.snapshot",
                                    OrderStore.receipt(order, p.getProperty("cashier.name", "Not recorded")))));
                } catch (RuntimeException | IOException ex) {
                    unreadable++;
                }
            }
        }
        result.sort(Comparator.comparing(Sale::created).reversed());
        return new Result(java.util.List.copyOf(result), unreadable);
    }

    private SavedSales() {
    }
}
