package model;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.List;
import persistence.LocalTransactions;
import util.config.*;

/**
 * Local branch-transfer and machine-booking ledgers. Each state change is one
 * atomic file replacement.
 */
public final class OperationsStore {
    public record Branch(String id, String name) {
        public String toString() {
            return name;
        }
    }

    public enum TransferState {
        REQUESTED, DISPATCHED, RECEIVED, CANCELLED
    }

    public record Transfer(String id, String from, String to, String itemId, String itemName, String unit,
            BigDecimal quantity, TransferState state, String created, String updated, String actor, String note) {
    }

    public record Machine(String id, String name, String type, boolean enabled) {
        public String toString() {
            return name + " · " + type;
        }
    }

    public enum BookingState {
        RESERVED, RUNNING, COMPLETED, CANCELLED
    }

    public record Booking(String id, String machineId, String orderId, Instant start, Instant end, BookingState state,
            String actor, String note) {
    }

    private static Properties load() throws IOException {
        return AtomicProperties.read(AtomicProperties.dataPath("operations.properties"));
    }

    private static void save(Properties p, String topic) throws IOException {
        AtomicProperties.write(AtomicProperties.dataPath("operations.properties"), p);
        AppEvents.fire(topic);
    }

    private static List<String> ids(Properties p, String group, String field) {
        return p.stringPropertyNames().stream().filter(k -> k.startsWith(group + ".") && k.endsWith("." + field))
                .map(k -> k.substring(group.length() + 1, k.length() - field.length() - 1)).sorted().toList();
    }

    private static void text(String value, String label, int length) {
        if (value == null || value.isBlank() || value.length() > length)
            throw new IllegalArgumentException(label + " must contain 1–" + length + " characters.");
    }

    private static String actor() {
        return ActiveProfile.current().name();
    }

    public static List<Branch> branches() throws IOException {
        var p = load();
        List<Branch> result = new ArrayList<>();
        result.add(new Branch("main", p.getProperty("branch.main.name", "Main branch")));
        for (String id : ids(p, "branch", "name"))
            if (!id.equals("main"))
                result.add(new Branch(id, p.getProperty("branch." + id + ".name")));
        return List.copyOf(result);
    }

    public static Branch addBranch(String name) throws IOException {
        text(name, "Branch name", 80);
        synchronized (LocalTransactions.LOCK) {
            if (branches().stream().anyMatch(b -> b.name().equalsIgnoreCase(name.trim())))
                throw new IllegalArgumentException("A branch already uses that name.");
            var p = load();
            String id = UUID.randomUUID().toString();
            p.setProperty("branch." + id + ".name", name.trim());
            save(p, "branches");
            return new Branch(id, name.trim());
        }
    }

    private static void branch(String id) throws IOException {
        if (branches().stream().noneMatch(b -> b.id().equals(id)))
            throw new IllegalArgumentException("Choose a known branch.");
    }

    public static List<Transfer> transfers() throws IOException {
        Properties p = load();
        List<Transfer> result = new ArrayList<>();
        try {
            for (String id : ids(p, "transfer", "state")) {
                String k = "transfer." + id + ".";
                result.add(new Transfer(id, p.getProperty(k + "from"), p.getProperty(k + "to"),
                        p.getProperty(k + "item"), p.getProperty(k + "name"), p.getProperty(k + "unit"),
                        new BigDecimal(p.getProperty(k + "quantity")),
                        TransferState.valueOf(p.getProperty(k + "state")), p.getProperty(k + "created"),
                        p.getProperty(k + "updated"), p.getProperty(k + "actor"), p.getProperty(k + "note", "")));
            }
        } catch (RuntimeException ex) {
            throw new IOException("Transfer records need attention. No stock was changed.", ex);
        }
        result.sort(Comparator.comparing(Transfer::created).reversed());
        return List.copyOf(result);
    }

    public static Transfer request(String from, String to, String itemId, BigDecimal quantity, String note)
            throws IOException {
        synchronized (LocalTransactions.LOCK) {
            branch(from);
            branch(to);
            if (from.equals(to))
                throw new IllegalArgumentException("Choose different source and destination branches.");
            CatalogItem item = persistence.Repositories.catalog().items().stream()
                    .filter(i -> i.id().equals(itemId) && i.product()).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Choose a current retail product."));
            if (quantity == null || quantity.signum() <= 0 || quantity.compareTo(new BigDecimal("999999")) > 0
                    || (item.unit() != CatalogItem.Unit.KG && quantity.stripTrailingZeros().scale() > 0)
                    || quantity.scale() > 3)
                throw new IllegalArgumentException("Enter a positive quantity using the product's unit.");
            if (note == null || note.isBlank() || note.length() > 300)
                throw new IllegalArgumentException("Enter a transfer reason, up to 300 characters.");
            String id = UUID.randomUUID().toString(), now = Instant.now().toString(), k = "transfer." + id + ".";
            var p = load();
            p.setProperty(k + "from", from);
            p.setProperty(k + "to", to);
            p.setProperty(k + "item", itemId);
            p.setProperty(k + "name", item.name());
            p.setProperty(k + "unit", item.unit().toString());
            p.setProperty(k + "quantity", quantity.toPlainString());
            p.setProperty(k + "state", "REQUESTED");
            p.setProperty(k + "created", now);
            p.setProperty(k + "requested.by", actor());
            p.setProperty(k + "updated", now);
            p.setProperty(k + "actor", actor());
            p.setProperty(k + "note", note.trim());
            save(p, "transfers");
            return transfers().stream().filter(t -> t.id().equals(id)).findFirst().orElseThrow();
        }
    }

    public static void advanceTransfer(String id, TransferState next) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            var transfer = transfers().stream().filter(t -> t.id().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Transfer was not found."));
            if (!((transfer.state() == TransferState.REQUESTED
                    && (next == TransferState.DISPATCHED || next == TransferState.CANCELLED))
                    || (transfer.state() == TransferState.DISPATCHED && next == TransferState.RECEIVED)))
                throw new IllegalArgumentException("That transfer transition is not allowed.");
            if (next == TransferState.DISPATCHED) {
                var result = persistence.Repositories.orders().read();
                if (result.unreadable() > 0)
                    throw new IOException("Review unreadable orders before dispatching stock.");
                var stock = new StockStore(transfer.from());
                if (!stock.tracked(transfer.itemId())
                        || stock.available(transfer.itemId(), result.sales()).compareTo(transfer.quantity()) < 0)
                    throw new IllegalArgumentException(
                            "Insufficient tracked source stock. Record a count or reduce the transfer.");
            }
            var p = load();
            String k = "transfer." + id + ".";
            p.setProperty(k + "state", next.name());
            p.setProperty(k + next.name().toLowerCase(Locale.ROOT) + ".at", Instant.now().toString());
            p.setProperty(k + next.name().toLowerCase(Locale.ROOT) + ".by", actor());
            p.setProperty(k + "updated", Instant.now().toString());
            p.setProperty(k + "actor", actor());
            save(p, "transfers");
        }
    }

    public static BigDecimal stockDelta(String branch, String item) throws IOException {
        BigDecimal delta = BigDecimal.ZERO;
        for (var t : transfers()) {
            if (!t.itemId().equals(item))
                continue;
            if ((t.state() == TransferState.DISPATCHED || t.state() == TransferState.RECEIVED)
                    && t.from().equals(branch))
                delta = delta.subtract(t.quantity());
            if (t.state() == TransferState.RECEIVED && t.to().equals(branch))
                delta = delta.add(t.quantity());
        }
        return delta;
    }

    public static boolean receivedStock(String branch, String item) throws IOException {
        return transfers().stream()
                .anyMatch(t -> t.to().equals(branch) && t.itemId().equals(item) && t.state() == TransferState.RECEIVED);
    }

    public static List<Machine> machines() throws IOException {
        var p = load();
        return ids(p, "machine", "name").stream().map(id -> {
            String k = "machine." + id + ".";
            return new Machine(id, p.getProperty(k + "name"), p.getProperty(k + "type"),
                    Boolean.parseBoolean(p.getProperty(k + "enabled", "true")));
        }).toList();
    }

    public static Machine addMachine(String name, String type) throws IOException {
        text(name, "Machine name", 60);
        if (!Set.of("Washer", "Dryer").contains(type))
            throw new IllegalArgumentException("Choose Washer or Dryer.");
        synchronized (LocalTransactions.LOCK) {
            if (machines().stream().anyMatch(m -> m.name().equalsIgnoreCase(name.trim())))
                throw new IllegalArgumentException("A machine already uses that name.");
            var p = load();
            String id = UUID.randomUUID().toString(), k = "machine." + id + ".";
            p.setProperty(k + "name", name.trim());
            p.setProperty(k + "type", type);
            p.setProperty(k + "enabled", "true");
            save(p, "machines");
            return new Machine(id, name.trim(), type, true);
        }
    }

    public static void setMachineEnabled(String id, boolean enabled) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            if (machines().stream().noneMatch(m -> m.id().equals(id)))
                throw new IllegalArgumentException("Machine not found.");
            if (!enabled && bookings().stream().anyMatch(b -> b.machineId().equals(id)
                    && (b.state() == BookingState.RESERVED || b.state() == BookingState.RUNNING)))
                throw new IllegalArgumentException("Finish or cancel active bookings before maintenance.");
            var p = load();
            p.setProperty("machine." + id + ".enabled", Boolean.toString(enabled));
            save(p, "machines");
        }
    }

    public static List<Booking> bookings() throws IOException {
        var p = load();
        List<Booking> result = new ArrayList<>();
        try {
            for (String id : ids(p, "booking", "state")) {
                String k = "booking." + id + ".";
                result.add(new Booking(id, p.getProperty(k + "machine"), p.getProperty(k + "order", ""),
                        Instant.parse(p.getProperty(k + "start")), Instant.parse(p.getProperty(k + "end")),
                        BookingState.valueOf(p.getProperty(k + "state")), p.getProperty(k + "actor"),
                        p.getProperty(k + "note", "")));
            }
        } catch (RuntimeException ex) {
            throw new IOException("Machine bookings need attention.", ex);
        }
        result.sort(Comparator.comparing(Booking::start));
        return List.copyOf(result);
    }

    public static Booking reserve(String machine, String order, Instant start, Instant end, String note)
            throws IOException {
        synchronized (LocalTransactions.LOCK) {
            if (machines().stream().noneMatch(m -> m.id().equals(machine) && m.enabled()))
                throw new IllegalArgumentException("Choose an available machine.");
            if (start == null || end == null || !end.isAfter(start) || Duration.between(start, end).toHours() >= 24
                    || start.isBefore(Instant.now().minusSeconds(60)))
                throw new IllegalArgumentException("Choose a future start and an end within 24 hours.");
            if (order == null || note == null || note.isBlank() || note.length() > 300)
                throw new IllegalArgumentException("Enter booking instructions, up to 300 characters.");
            if (!order.isBlank()
                    && persistence.Repositories.orders().read().sales().stream().noneMatch(s -> s.id().equals(order)))
                throw new IllegalArgumentException("Linked order was not found.");
            if (bookings().stream()
                    .anyMatch(b -> b.machineId().equals(machine)
                            && (b.state() == BookingState.RESERVED || b.state() == BookingState.RUNNING)
                            && start.isBefore(b.end()) && end.isAfter(b.start())))
                throw new IllegalArgumentException("This machine already has a booking during that time.");
            var p = load();
            String id = UUID.randomUUID().toString(), k = "booking." + id + ".";
            p.setProperty(k + "machine", machine);
            p.setProperty(k + "order", order);
            p.setProperty(k + "start", start.toString());
            p.setProperty(k + "end", end.toString());
            p.setProperty(k + "state", "RESERVED");
            p.setProperty(k + "actor", actor());
            p.setProperty(k + "note", note.trim());
            save(p, "machines");
            return new Booking(id, machine, order, start, end, BookingState.RESERVED, actor(), note.trim());
        }
    }

    public static void advanceBooking(String id, BookingState next) throws IOException {
        synchronized (LocalTransactions.LOCK) {
            var b = bookings().stream().filter(v -> v.id().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
            if (!((b.state() == BookingState.RESERVED
                    && (next == BookingState.RUNNING || next == BookingState.CANCELLED))
                    || (b.state() == BookingState.RUNNING
                            && (next == BookingState.COMPLETED || next == BookingState.CANCELLED))))
                throw new IllegalArgumentException("That booking transition is not allowed.");
            if (next == BookingState.RUNNING
                    && (Instant.now().isBefore(b.start().minusSeconds(60)) || !Instant.now().isBefore(b.end())))
                throw new IllegalArgumentException(
                        "Start a booking within its reserved time window; otherwise cancel and reserve a new time.");
            if (next == BookingState.RUNNING && bookings().stream().anyMatch(v -> !v.id().equals(id)
                    && v.machineId().equals(b.machineId()) && v.state() == BookingState.RUNNING))
                throw new IllegalArgumentException("Another booking is still running on this machine.");
            var p = load();
            String k = "booking." + id + ".";
            p.setProperty(k + "state", next.name());
            p.setProperty(k + "actor", actor());
            p.setProperty(k + next.name().toLowerCase(Locale.ROOT) + ".at", Instant.now().toString());
            p.setProperty(k + next.name().toLowerCase(Locale.ROOT) + ".by", actor());
            save(p, "machines");
        }
    }

    private OperationsStore() {
    }
}
