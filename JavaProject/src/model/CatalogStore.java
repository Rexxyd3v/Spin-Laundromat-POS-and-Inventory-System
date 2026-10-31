package model;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import util.config.AtomicProperties;

/** The original services file is imported without changing or deleting it. */
public final class CatalogStore implements persistence.RepositoryProvider.Catalog {
    private final Path file = AtomicProperties.dataPath("catalog.properties");
    private List<CatalogItem> items;

    public CatalogStore() throws IOException {
        if (Files.exists(file)) {
            try {
                items = decode(AtomicProperties.read(file));
            } catch (IllegalArgumentException ex) {
                throw new IOException("The saved catalog contains invalid data. It was not overwritten.", ex);
            }
        } else {
            items = initialItems();
        }
    }

    public List<CatalogItem> items() {
        return List.copyOf(items);
    }

    public void save(List<CatalogItem> replacement) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            Properties p = new Properties();
            p.setProperty("count", Integer.toString(replacement.size()));
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < replacement.size(); i++) {
                CatalogItem item = replacement.get(i);
                if (!ids.add(item.id()))
                    throw new IllegalArgumentException("Duplicate catalog identifier.");
                String key = "item." + i + ".";
                p.setProperty(key + "id", item.id());
                p.setProperty(key + "name", item.name());
                p.setProperty(key + "description", item.description());
                p.setProperty(key + "category", item.category());
                p.setProperty(key + "unit", item.unit().name());
                p.setProperty(key + "price", item.price().toPlainString());
                p.setProperty(key + "product", Boolean.toString(item.product()));
                p.setProperty(key + "image", item.image());
            }
            AtomicProperties.write(file, p);
            items = List.copyOf(replacement);
            util.config.AppEvents.fire("catalog");

        }
    }

    private List<CatalogItem> decode(Properties p) {
        int count = Integer.parseInt(p.getProperty("count", "-1"));
        if (count < 0 || count > 10000)
            throw new IllegalArgumentException("Invalid catalog count.");
        List<CatalogItem> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String k = "item." + i + ".";
            CatalogItem item = new CatalogItem(p.getProperty(k + "id"), p.getProperty(k + "name"),
                    p.getProperty(k + "description", ""), p.getProperty(k + "category"),
                    CatalogItem.Unit.valueOf(p.getProperty(k + "unit")), new BigDecimal(p.getProperty(k + "price")),
                    Boolean.parseBoolean(p.getProperty(k + "product")), p.getProperty(k + "image", ""));
            if (!ids.add(item.id()))
                throw new IllegalArgumentException("Duplicate catalog identifier.");
            result.add(item);
        }
        return result;
    }

    private List<CatalogItem> initialItems() throws IOException {
        List<CatalogItem> result = new ArrayList<>();
        Path old = Path.of(System.getProperty("laundry.legacyServices", "services.properties"));
        if (Files.exists(old)) {
            try {
                Properties p = AtomicProperties.read(old);
                int count = Integer.parseInt(p.getProperty("count", "0"));
                if (count < 0 || count > 10000)
                    throw new IllegalArgumentException();
                for (int i = 0; i < count; i++) {
                    String k = "service." + i + ".";
                    result.add(new CatalogItem("legacy-service-" + i, p.getProperty(k + "name"),
                            p.getProperty(k + "desc", "Per kg"), "Laundry", CatalogItem.Unit.KG,
                            new BigDecimal(p.getProperty(k + "price", "0")), false));
                }
            } catch (IllegalArgumentException ex) {
                throw new IOException("Existing services could not be imported. The original file was not changed.",
                        ex);
            }
        } else {
            result.add(item("wash", "Wash only", "A fresh start for everyday laundry", "Wash", "10.00", false));
            result.add(item("dry", "Dry only", "Gentle tumble drying", "Dry", "10.00", false));
            result.add(item("full", "Wash, dry & fold", "Fresh, clean and ready to wear", "Packages", "24.29", false));
            result.add(item("fold", "Fold only", "Neatly folded and packed", "Extras", "4.29", false));
        }
        result.add(item("surf", "Surf powder", "Rose Fresh · 75 g sachet", "Products", "7.00", true));
        result.add(item("wings", "Wings detergent", "White · 40 g sachet", "Products", "7.00", true));
        result.add(item("breeze", "Breeze liquid", "Detergent · 60 ml sachet", "Products", "15.00", true));
        result.add(item("downy", "Downy Sunrise", "Fabric conditioner · 60 ml", "Products", "6.00", true));
        return result;
    }

    private CatalogItem item(String id, String name, String desc, String category, String price, boolean product) {
        return new CatalogItem(id, name, desc, category, product ? CatalogItem.Unit.PIECE : CatalogItem.Unit.KG,
                new BigDecimal(price), product);
    }
}
