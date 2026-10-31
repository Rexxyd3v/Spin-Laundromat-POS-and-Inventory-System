package model;

import java.math.BigDecimal;

public record CatalogItem(String id, String name, String description, String category, Unit unit, BigDecimal price,
        boolean product, String image) {
    public CatalogItem(String id, String name, String description, String category, Unit unit, BigDecimal price,
            boolean product) {
        this(id, name, description, category, unit, price, product, "");
    }

    public enum Unit {
        KG("kg"), PIECE("pc"), LOAD("load"), SERVICE("service");

        private final String label;

        Unit(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public CatalogItem {
        image = image == null ? "" : image;
        if (id == null || id.isBlank() || name == null || name.isBlank() || name.length() > 80)
            throw new IllegalArgumentException("Enter an item name (up to 80 characters).");
        if (category == null || category.isBlank() || category.length() > 40)
            throw new IllegalArgumentException("Enter a category (up to 40 characters).");
        if (unit == null || price == null || price.signum() < 0 || price.scale() > 2
                || price.compareTo(new BigDecimal("9999999.99")) > 0)
            throw new IllegalArgumentException("Enter a valid price with up to two decimal places.");
    }
}
