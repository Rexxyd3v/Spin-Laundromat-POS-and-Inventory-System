package model;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.List;

/**
 * All dashboard metrics share the same paid-order window and Manila calendar.
 */
public final class SalesAnalytics {
    private static final ZoneId ZONE = ZoneId.of("Asia/Manila");

    public record Day(LocalDate date, BigDecimal revenue, int orders) {
    }

    public record RankedItem(String id, String name, BigDecimal quantity, BigDecimal revenue, String unit) {
    }

    public record Window(LocalDate start, LocalDate end, List<SavedSales.Sale> orders, List<SavedSales.Sale> previous,
            BigDecimal revenue, BigDecimal previousRevenue, BigDecimal average, BigDecimal previousAverage,
            int customers, int returning, List<Day> days, List<Day> previousDays, Map<String, BigDecimal> payments,
            List<RankedItem> topItems) {
    }

    public static Window calculate(List<SavedSales.Sale> all, int days, LocalDate end) {
        if (days < 1 || days > 366)
            throw new IllegalArgumentException("Invalid analytics period.");
        LocalDate start = end.minusDays(days - 1), prior = start.minusDays(days);
        List<SavedSales.Sale> orders = range(all, start, end), previous = range(all, prior, start.minusDays(1));
        Map<String, Long> lifetime = new HashMap<>();
        for (var sale : all)
            if (!date(sale).isAfter(end)) {
                String key = customerKey(sale);
                if (key != null)
                    lifetime.merge(key, 1L, Long::sum);
            }
        Set<String> customers = new HashSet<>();
        orders.forEach(s -> {
            String key = customerKey(s);
            if (key != null)
                customers.add(key);
        });
        int returning = (int) customers.stream().filter(key -> lifetime.getOrDefault(key, 0L) > 1).count();
        Map<String, BigDecimal> payments = new LinkedHashMap<>();
        for (var order : orders)
            payments.merge(order.payment(), order.total(), BigDecimal::add);
        Map<String, RankedItem> ranked = new HashMap<>();
        for (var sale : orders)
            for (var line : sale.lines())
                ranked.merge(line.id(),
                        new RankedItem(line.id(), line.name(), line.quantity(), line.total(), line.unit()),
                        (a, b) -> new RankedItem(a.id(), b.name(), a.quantity().add(b.quantity()),
                                a.revenue().add(b.revenue()), a.unit()));
        return new Window(start, end, List.copyOf(orders), List.copyOf(previous), total(orders), total(previous),
                average(orders), average(previous), customers.size(), returning, daily(orders, start, days),
                daily(previous, prior, days), Collections.unmodifiableMap(payments),
                ranked.values().stream()
                        .sorted(Comparator.comparing(RankedItem::revenue).reversed().thenComparing(RankedItem::name))
                        .limit(5).toList());
    }

    private static String customerKey(SavedSales.Sale sale) {
        String digits = sale.phone().replaceAll("\\D", "");
        if (!digits.isBlank())
            return digits;
        String name = sale.customer().trim().toLowerCase(Locale.ROOT);
        return name.equals("walk-in customer") || name.isBlank() ? null : name;
    }

    public static LocalDate date(SavedSales.Sale sale) {
        return sale.created().atZoneSameInstant(ZONE).toLocalDate();
    }

    private static List<SavedSales.Sale> range(List<SavedSales.Sale> all, LocalDate start, LocalDate end) {
        return all.stream().filter(s -> !date(s).isBefore(start) && !date(s).isAfter(end))
                .sorted(Comparator.comparing(SavedSales.Sale::created).reversed()).toList();
    }

    private static BigDecimal total(List<SavedSales.Sale> sales) {
        return sales.stream().map(SavedSales.Sale::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    private static BigDecimal average(List<SavedSales.Sale> sales) {
        return sales.isEmpty() ? new BigDecimal("0.00")
                : total(sales).divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
    }

    private static List<Day> daily(List<SavedSales.Sale> orders, LocalDate start, int count) {
        List<Day> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDate day = start.plusDays(i);
            List<SavedSales.Sale> onDay = orders.stream().filter(s -> date(s).equals(day)).toList();
            result.add(new Day(day, total(onDay), onDay.size()));
        }
        return List.copyOf(result);
    }

    public static String change(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0)
            return current.signum() == 0 ? "No change" : "No earlier sales";
        BigDecimal percent = current.subtract(previous).multiply(new BigDecimal("100")).divide(previous, 1,
                RoundingMode.HALF_UP);
        return (percent.signum() > 0 ? "+" : "") + percent.stripTrailingZeros().toPlainString()
                + "% vs previous period";
    }

    private SalesAnalytics() {
    }
}
