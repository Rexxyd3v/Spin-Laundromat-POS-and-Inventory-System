package persistence;

import java.io.IOException;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import model.*;
import util.config.*;

/**
 * Local adapters preserve the existing file formats for a reversible cloud
 * migration.
 */
public final class FileRepositories implements RepositoryProvider {
    public Catalog catalog() throws IOException {
        return new CatalogStore();
    }

    public Inventory inventory() throws IOException {
        return new StockStore();
    }

    public Inventory inventory(String branch) throws IOException {
        return new StockStore(branch);
    }

    public Team team() {
        return new TeamStore();
    }

    public Orders orders() {
        return new Orders() {
            public Confirmation confirm(OrderStore.Order order) throws IOException {
                synchronized (LocalTransactions.LOCK) {
                    java.util.UUID.fromString(order.id());
                    Path file = DataLocation.resolve("orders/" + order.id() + ".properties");
                    try {
                        OrderStore.save(order);
                    } catch (FileAlreadyExistsException alreadySaved) {
                        if (!sameRequest(AtomicProperties.read(file), order))
                            throw new IOException("Order ID is already used by a different request.");
                    }
                    var saved = AtomicProperties.read(file);
                    return new Confirmation(order.id(), saved.getProperty("receipt.snapshot",
                            OrderStore.receipt(order, saved.getProperty("cashier.name", "Not recorded"))));
                }
            }

            public SavedSales.Result read() throws IOException {
                return SavedSales.read();
            }
        };
    }

    private static boolean sameRequest(java.util.Properties p, OrderStore.Order o) {
        try {
            if (!o.customer().equals(p.getProperty("customer")) || !o.phone().equals(p.getProperty("phone"))
                    || !o.notes().equals(p.getProperty("notes")) || !o.payment().equals(p.getProperty("payment"))
                    || !o.reference().equals(p.getProperty("reference"))
                    || o.lines().size() != Integer.parseInt(p.getProperty("line.count")))
                return false;
            if (o.tendered().compareTo(new java.math.BigDecimal(p.getProperty("tendered"))) != 0
                    || o.totals().total().compareTo(new java.math.BigDecimal(p.getProperty("total"))) != 0
                    || o.totals().subtotal().compareTo(new java.math.BigDecimal(p.getProperty("subtotal"))) != 0
                    || o.totals().tax().compareTo(new java.math.BigDecimal(p.getProperty("tax"))) != 0)
                return false;
            if (o.settings().taxEnabled() != Boolean.parseBoolean(p.getProperty("tax.enabled"))
                    || o.settings().taxRate().compareTo(new java.math.BigDecimal(p.getProperty("tax.rate"))) != 0
                    || !o.settings().storeName().equals(p.getProperty("store.name"))
                    || !o.settings().address().equals(p.getProperty("store.address"))
                    || !o.settings().phone().equals(p.getProperty("store.phone"))
                    || !o.settings().receiptFooter().equals(p.getProperty("receipt.footer")))
                return false;
            for (int i = 0; i < o.lines().size(); i++) {
                var l = o.lines().get(i);
                String k = "line." + i + ".";
                if (!l.id().equals(p.getProperty(k + "id")) || !l.name().equals(p.getProperty(k + "name"))
                        || !l.unit().equals(p.getProperty(k + "unit"))
                        || l.quantity().compareTo(new java.math.BigDecimal(p.getProperty(k + "quantity"))) != 0
                        || l.unitPrice().compareTo(new java.math.BigDecimal(p.getProperty(k + "price"))) != 0)
                    return false;
            }
            return true;
        } catch (RuntimeException invalid) {
            return false;
        }
    }

    public Operations operations() {
        return new Operations() {
            public java.util.List<OperationsStore.Branch> branches() throws IOException {
                return OperationsStore.branches();
            }

            public OperationsStore.Branch addBranch(String name) throws IOException {
                return OperationsStore.addBranch(name);
            }

            public java.util.List<OperationsStore.Transfer> transfers() throws IOException {
                return OperationsStore.transfers();
            }

            public OperationsStore.Transfer request(String from, String to, String item, java.math.BigDecimal quantity,
                    String note) throws IOException {
                return OperationsStore.request(from, to, item, quantity, note);
            }

            public void advanceTransfer(String id, OperationsStore.TransferState next) throws IOException {
                OperationsStore.advanceTransfer(id, next);
            }

            public java.util.List<OperationsStore.Machine> machines() throws IOException {
                return OperationsStore.machines();
            }

            public OperationsStore.Machine addMachine(String name, String type) throws IOException {
                return OperationsStore.addMachine(name, type);
            }

            public void setMachineEnabled(String id, boolean enabled) throws IOException {
                OperationsStore.setMachineEnabled(id, enabled);
            }

            public java.util.List<OperationsStore.Booking> bookings() throws IOException {
                return OperationsStore.bookings();
            }

            public OperationsStore.Booking reserve(String machine, String order, java.time.Instant start,
                    java.time.Instant end, String note) throws IOException {
                return OperationsStore.reserve(machine, order, start, end, note);
            }

            public void advanceBooking(String id, OperationsStore.BookingState next) throws IOException {
                OperationsStore.advanceBooking(id, next);
            }
        };
    }

    public Settings settings() {
        return new Settings() {
            public PosSettings.Values get() {
                return PosSettings.get();
            }

            public PosSettings.Values defaults() {
                return PosSettings.defaults();
            }

            public String loadError() {
                return PosSettings.loadError();
            }

            public void save(PosSettings.Values values) throws IOException {
                PosSettings.save(values);
            }

            public void addListener(PropertyChangeListener listener) {
                PosSettings.addListener(listener);
            }

            public void removeListener(PropertyChangeListener listener) {
                PosSettings.removeListener(listener);
            }
        };
    }

    public Assets assets() {
        return new Assets() {
            public void validate(Path file) throws IOException {
                ItemImages.validate(file);
            }

            public String importPhoto(Path file) throws IOException {
                return ItemImages.importPhoto(file);
            }

            public Path resolve(String key) throws IOException {
                return ItemImages.resolve(key);
            }

            public BufferedImage read(Path file) throws IOException {
                return ItemImages.read(file);
            }
        };
    }
}
