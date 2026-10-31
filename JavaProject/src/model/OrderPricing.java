package model;

import java.math.*;
import java.util.List;

public final class OrderPricing {
    public record Line(String id, String name, String unit, BigDecimal quantity, BigDecimal unitPrice) {
        public Line {
            if (quantity.signum() <= 0 || quantity.compareTo(new BigDecimal("99999")) > 0 || unitPrice.signum() < 0)
                throw new IllegalArgumentException("Invalid order quantity or price.");
        }

        public BigDecimal total() {
            return unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public static Totals calculate(List<Line> lines, boolean enabled, BigDecimal rate) {
        BigDecimal subtotal = lines.stream().map(Line::total).reduce(new BigDecimal("0.00"), BigDecimal::add);
        BigDecimal tax = enabled ? subtotal.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : new BigDecimal("0.00");
        return new Totals(subtotal, tax, subtotal.add(tax));
    }

    public static String money(BigDecimal amount) {
        return "₱" + String.format(java.util.Locale.US, "%,.2f", amount);
    }

    private OrderPricing() {
    }
}
