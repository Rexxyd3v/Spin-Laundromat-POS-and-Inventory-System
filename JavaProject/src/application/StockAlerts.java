package application;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import model.*;
import persistence.*;

/**
 * Active alerts are derived from current tracked balances, including equality
 * with the threshold.
 */
public final class StockAlerts {
    public record Alert(String branchId, String branch, String itemId, String name, String unit, BigDecimal available,
            BigDecimal threshold) {
        public String severity() {
            return available.signum() <= 0 ? "Out of stock" : "Low stock";
        }
    }

    public static List<Alert> read() throws IOException {
        synchronized (LocalTransactions.LOCK) {
            var sales = Repositories.orders().read();
            if (sales.unreadable() > 0)
                throw new IOException("Unreadable orders prevent reliable stock alerts.");
            var products = Repositories.catalog().items().stream().filter(CatalogItem::product).toList();
            List<Alert> alerts = new ArrayList<>();
            for (var branch : Repositories.operations().branches()) {
                var stock = Repositories.inventory(branch.id());
                for (var p : products)
                    if (stock.tracked(p.id())) {
                        BigDecimal available = stock.available(p.id(), sales.sales()),
                                threshold = stock.minimum(p.id());
                        if (available.compareTo(threshold) <= 0)
                            alerts.add(new Alert(branch.id(), branch.name(), p.id(), p.name(), p.unit().toString(),
                                    available, threshold));
                    }
            }
            alerts.sort(Comparator.comparing(Alert::available).thenComparing(Alert::name).thenComparing(Alert::branch));
            return List.copyOf(alerts);
        }
    }

    private StockAlerts() {
    }
}
