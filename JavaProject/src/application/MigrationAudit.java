package application;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import model.*;
import persistence.*;

/**
 * Read-only checks and normalized review exports. No import or automatic merge
 * is performed.
 */
public final class MigrationAudit {
    public record Report(int items, int orders, int unreadable, int images, List<String> findings, String text) {
    }

    public static Report scan() throws IOException {
        synchronized (LocalTransactions.LOCK) {
            List<String> findings = new ArrayList<>();
            List<CatalogItem> items = List.of();
            try {
                items = Repositories.catalog().items();
            } catch (IOException ex) {
                findings.add("ERROR: Catalog cannot be decoded: " + ex.getMessage());
            }
            SavedSales.Result orders = Repositories.orders().read();
            if (orders.unreadable() > 0)
                findings.add("ERROR: " + orders.unreadable()
                        + " order files cannot be decoded. Review originals before import.");
            Set<String> referenced = new HashSet<>();
            for (CatalogItem item : items)
                if (!item.image().isBlank())
                    referenced.add(item.image());
            try {
                for (var member : Repositories.team().read())
                    if (!member.image().isBlank())
                        referenced.add(member.image());
            } catch (IOException ex) {
                findings.add("ERROR: Staff profiles cannot be decoded.");
            }
            for (String image : referenced) {
                try {
                    Repositories.assets().validate(Repositories.assets().resolve(image));
                } catch (IOException | RuntimeException ex) {
                    findings.add("ERROR: Missing or invalid referenced photo: " + image);
                }
            }
            int images = 0;
            Path imageRoot = DataLocation.resolve("images");
            if (Files.isDirectory(imageRoot))
                try (var paths = Files.list(imageRoot)) {
                    for (Path p : paths.filter(Files::isRegularFile).toList()) {
                        images++;
                        if (!referenced.contains("images/" + p.getFileName()))
                            findings.add("REVIEW: Unreferenced photo retained: " + p.getFileName());
                    }
                }
            var ids = new HashSet<String>();
            for (var order : orders.sales()) {
                if (!ids.add(order.id()))
                    findings.add("ERROR: Duplicate order ID: " + order.id());
                if (order.subtotal().add(order.tax()).compareTo(order.total()) != 0)
                    findings.add("ERROR: Order totals do not reconcile: " + order.id());
                if (order.lines().stream().map(OrderPricing.Line::total).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .compareTo(order.subtotal()) != 0)
                    findings.add("ERROR: Order lines do not reconcile: " + order.id());
            }
            findings.add(
                    "REVIEW: Catalog IDs are preserved, including legacy IDs. Scope them to the source branch before a multi-branch import.");
            findings.add(
                    "REVIEW: Legacy CSVs remain source documents; incomplete transactions are not converted into invented orders.");
            findings.add("INFO: Profiles and role labels are not authenticated user accounts.");
            String text = "LOCAL DATA AUDIT\nActive folder: " + DataLocation.root() + "\nCatalog items: " + items.size()
                    + "\nReadable orders: " + orders.sales().size() + "\nUnreadable orders: " + orders.unreadable()
                    + "\nImage files: " + images + "\n\n" + String.join("\n", findings)
                    + "\n\nNo files were changed or imported. A backup contains private business data; keep it in a trusted location.";
            return new Report(items.size(), orders.sales().size(), orders.unreadable(), images, List.copyOf(findings),
                    text);
        }
    }

    public static Map<String, byte[]> exports() throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        var items = Repositories.catalog().items();
        var sales = Repositories.orders().read();
        StringBuilder catalog = new StringBuilder("SourceBranch,ItemId,Name,Type,Category,Unit,Price,ImageKey\n");
        for (var i : items)
            catalog.append(csv("main", i.id(), i.name(), i.product() ? "Product" : "Service", i.category(),
                    i.unit().name(), i.price().toPlainString(), i.image()));
        StringBuilder orders = new StringBuilder(
                "SourceBranch,OrderId,Created,Customer,Phone,Payment,Subtotal,Tax,Total\n");
        StringBuilder lines = new StringBuilder("OrderId,LineIndex,ItemId,Name,Unit,Quantity,Price,Total\n");
        for (var s : sales.sales()) {
            orders.append(csv("main", s.id(), s.created().toString(), s.customer(), s.phone(), s.payment(),
                    s.subtotal().toPlainString(), s.tax().toPlainString(), s.total().toPlainString()));
            int n = 0;
            for (var l : s.lines())
                lines.append(csv(s.id(), Integer.toString(n++), l.id(), l.name(), l.unit(),
                        l.quantity().toPlainString(), l.unitPrice().toPlainString(), l.total().toPlainString()));
        }
        files.put("export/catalog.csv", catalog.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        files.put("export/orders.csv", orders.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        files.put("export/order-lines.csv", lines.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        files.put("export/audit.txt", scan().text().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder team = new StringBuilder("ProfileId,Name,Contact,RoleLabel,Active,ImageKey\n");
        for (var m : Repositories.team().read())
            team.append(csv(m.id(), m.name(), m.contact(), m.role(), Boolean.toString(m.active()), m.image()));
        files.put("export/team.csv", team.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder inventory = new StringBuilder("BranchId,BranchName,ItemId,Tracked,Available,Threshold\n");
        for (var branch : Repositories.operations().branches()) {
            var stock = Repositories.inventory(branch.id());
            for (var item : items)
                if (item.product())
                    inventory.append(csv(branch.id(), branch.name(), item.id(),
                            Boolean.toString(stock.tracked(item.id())),
                            stock.tracked(item.id()) && sales.unreadable() == 0
                                    ? stock.available(item.id(), sales.sales()).toPlainString()
                                    : "",
                            stock.tracked(item.id()) ? stock.minimum(item.id()).toPlainString() : ""));
        }
        files.put("export/inventory.csv", inventory.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder transfers = new StringBuilder(
                "TransferId,FromBranch,ToBranch,ItemId,Quantity,Unit,State,Created,Updated,LastActor,Reason\n");
        for (var t : Repositories.operations().transfers())
            transfers.append(csv(t.id(), t.from(), t.to(), t.itemId(), t.quantity().toPlainString(), t.unit(),
                    t.state().name(), t.created(), t.updated(), t.actor(), t.note()));
        files.put("export/transfers.csv", transfers.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder machines = new StringBuilder("MachineId,Name,Type,Enabled\n");
        for (var m : Repositories.operations().machines())
            machines.append(csv(m.id(), m.name(), m.type(), Boolean.toString(m.enabled())));
        files.put("export/machines.csv", machines.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder bookings = new StringBuilder(
                "BookingId,MachineId,OrderId,StartUTC,EndUTC,State,Actor,Instructions\n");
        for (var b : Repositories.operations().bookings())
            bookings.append(csv(b.id(), b.machineId(), b.orderId(), b.start().toString(), b.end().toString(),
                    b.state().name(), b.actor(), b.note()));
        files.put("export/bookings.csv", bookings.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder assets = new StringBuilder("StorageKey,Bytes\n");
        Path imageRoot = DataLocation.resolve("images");
        if (Files.isDirectory(imageRoot))
            try (var paths = Files.list(imageRoot)) {
                for (var image : paths.filter(Files::isRegularFile).sorted().toList())
                    assets.append(csv("images/" + image.getFileName(), Long.toString(Files.size(image))));
            }
        files.put("export/assets.csv", assets.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return files;
    }

    public static String csv(String... values) {
        return Arrays.stream(values).map(v -> v == null ? "" : v)
                .map(v -> v.matches("(?s)^[\\s]*[=+@\\-].*") ? "'" + v : v)
                .map(v -> "\"" + v.replace("\"", "\"\"") + "\"").collect(java.util.stream.Collectors.joining(","))
                + "\n";
    }

    private MigrationAudit() {
    }
}
