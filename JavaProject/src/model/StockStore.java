package model;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import util.config.*;

/**
 * Stock is a counted baseline minus saved sales, avoiding a second checkout
 * write.
 */
public final class StockStore implements persistence.RepositoryProvider.Inventory {
    private final Properties values;
    private final String branch;

    private String key(String id, String suffix) {
        return (branch.equals("main") ? "" : "branch." + branch + ".") + id + suffix;
    }

    private Map<String, BigDecimal> deltas = Map.of();
    private Set<String> received = Set.of();

    private BigDecimal delta(String id) {
        return deltas.getOrDefault(id, BigDecimal.ZERO);
    }

    private void refreshDeltas() throws IOException {
        Map<String, BigDecimal> next = new HashMap<>();
        Set<String> delivered = new HashSet<>();
        for (var t : OperationsStore.transfers()) {
            if ((t.state() == OperationsStore.TransferState.DISPATCHED
                    || t.state() == OperationsStore.TransferState.RECEIVED) && t.from().equals(branch))
                next.merge(t.itemId(), t.quantity().negate(), BigDecimal::add);
            if (t.state() == OperationsStore.TransferState.RECEIVED && t.to().equals(branch)) {
                next.merge(t.itemId(), t.quantity(), BigDecimal::add);
                delivered.add(t.itemId());
            }
        }
        deltas = Map.copyOf(next);
        received = Set.copyOf(delivered);
    }

    public StockStore() throws IOException {
        this("main");
    }

    public StockStore(String branch) throws IOException {
        if (OperationsStore.branches().stream().noneMatch(b -> b.id().equals(branch)))
            throw new IllegalArgumentException("Unknown stock branch.");
        this.branch = branch;
        values = AtomicProperties.read(AtomicProperties.dataPath("stock.properties"));
        refreshDeltas();
    }

    public boolean tracked(String id) {
        return values.containsKey(key(id, ".base")) || received.contains(id);
    }

    public BigDecimal minimum(String id) {
        return new BigDecimal(values.getProperty(key(id, ".minimum"), "5"));
    }

    public BigDecimal available(String id, java.util.List<SavedSales.Sale> sales) {
        return new BigDecimal(values.getProperty(key(id, ".base"), "0")).subtract(sold(id, sales)).add(delta(id));
    }

    private BigDecimal sold(String id, java.util.List<SavedSales.Sale> sales) {
        if (!branch.equals("main"))
            return BigDecimal.ZERO;
        return sales.stream().flatMap(s -> s.lines().stream()).filter(l -> l.id().equals(id))
                .map(OrderPricing.Line::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setCount(String id, BigDecimal count, BigDecimal minimum, String note) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            refreshDeltas();
            if (count == null || minimum == null || count.signum() < 0 || minimum.signum() < 0)
                throw new IllegalArgumentException("Stock quantities cannot be negative.");
            java.util.List<SavedSales.Sale> sales = checkedSales();
            Properties next = AtomicProperties.read(AtomicProperties.dataPath("stock.properties"));
            next.setProperty(key(id, ".base"), count.add(sold(id, sales)).subtract(delta(id)).toPlainString());
            next.setProperty(key(id, ".minimum"), minimum.toPlainString());
            next.setProperty(key(id, ".note"), note);
            next.setProperty(key(id, ".updated"), java.time.OffsetDateTime.now().toString());
            AtomicProperties.write(AtomicProperties.dataPath("stock.properties"), next);
            values.clear();
            values.putAll(next);
            AppEvents.fire("stock");

        }
    }

    private static java.util.List<SavedSales.Sale> checkedSales() throws IOException {
        SavedSales.Result result = SavedSales.read();
        if (result.unreadable() > 0)
            throw new IOException("Some saved orders need attention before stock can be updated.");
        return result.sales();
    }

    public static void ensureAvailable(java.util.List<OrderPricing.Line> lines) throws IOException {
        StockStore stock = new StockStore();
        java.util.List<SavedSales.Sale> sales = checkedSales();
        Map<String, BigDecimal> requested = new HashMap<>();
        for (OrderPricing.Line l : lines)
            requested.merge(l.id(), l.quantity(), BigDecimal::add);
        for (var entry : requested.entrySet())
            if (stock.tracked(entry.getKey()) && stock.available(entry.getKey(), sales).compareTo(entry.getValue()) < 0)
                throw new IOException(
                        "Insufficient stock for an item in this order. Update the order before retrying.");
    }
}
