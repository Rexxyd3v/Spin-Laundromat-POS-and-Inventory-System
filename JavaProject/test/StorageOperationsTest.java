import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;
import javax.imageio.ImageIO;
import application.*;
import model.*;
import persistence.*;
import util.config.*;

public class StorageOperationsTest {
    private static int checks;

    private interface Task {
        void run() throws Exception;
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value)
            throw new AssertionError(message);
    }

    private static void reject(Task task, String message) throws Exception {
        try {
            task.run();
            throw new AssertionError(message);
        } catch (IOException | IllegalArgumentException expected) {
            checks++;
        }
    }

    private static BigDecimal number(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal balance(String branch, String item) throws Exception {
        return Repositories.inventory(branch).available(item, Repositories.orders().read().sales());
    }

    private static Map<String, String> hashes(Path folder) throws Exception {
        Map<String, String> result = new TreeMap<>();
        try (var paths = Files.walk(folder)) {
            for (var p : paths.filter(Files::isRegularFile).toList())
                result.put(folder.relativize(p).toString(),
                        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p))));
        }
        return result;
    }

    private static void tamper(Path source, Path target, String name) throws Exception {
        try (ZipFile zip = new ZipFile(source.toFile());
                ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(target))) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                output.putNextEntry(new ZipEntry(e.getName()));
                if (e.getName().equals(name))
                    output.write("tampered".getBytes(StandardCharsets.UTF_8));
                else
                    try (var input = zip.getInputStream(e)) {
                        input.transferTo(output);
                    }
                output.closeEntry();
            }
        }
    }

    private static long concurrent(Callable<Boolean> first, Callable<Boolean> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            var a = pool.submit(() -> {
                start.await();
                return first.call();
            });
            var b = pool.submit(() -> {
                start.await();
                return second.call();
            });
            start.countDown();
            return (a.get() ? 1 : 0) + (b.get() ? 1 : 0);
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        Path sandbox = Files.createTempDirectory("laundry-storage-ops-");
        Path data = sandbox.resolve("data");
        Files.createDirectories(data);
        System.setProperty("laundry.dataDir", data.toString());
        AuthTestSupport.loginOwner();
        System.setProperty("laundry.legacyServices", sandbox.resolve("missing.properties").toString());
        var catalog = Repositories.catalog();
        catalog.save(catalog.items());
        var product = catalog.items().stream().filter(CatalogItem::product).findFirst().orElseThrow();
        var service = catalog.items().stream().filter(i -> !i.product()).findFirst().orElseThrow();
        var stock = Repositories.inventory();
        stock.setCount(product.id(), number("4"), number("3"), "Opening count");
        check(StockAlerts.read().isEmpty(), "No warning above the threshold; untracked products stay excluded");
        stock.setCount(product.id(), number("3"), number("3"), "Equal threshold");
        check(StockAlerts.read().size() == 1, "Equality triggers a low-stock alert");
        stock.setCount(product.id(), number("2"), number("3"), "Below threshold");
        check(StockAlerts.read().get(0).available().compareTo(number("2")) == 0, "Below-threshold quantity is shown");
        stock.setCount(product.id(), number("0"), number("3"), "Sold out");
        check(StockAlerts.read().get(0).severity().equals("Out of stock"), "Zero is classified as out of stock");
        stock.setCount(service.id(), number("1"), number("1"), "Tracked service");
        check(StockAlerts.read().size() == 1, "Service entries do not create product notifications");
        stock.setCount(product.id(), number("20"), number("3"), "Replenished");
        check(StockAlerts.read().isEmpty(), "Replenishment resolves the alert");
        var settings = PosSettings.get();
        var lines = List.of(new OrderPricing.Line(product.id(), product.name(), product.unit().toString(), number("2"),
                product.price()));
        var totals = OrderPricing.calculate(lines, settings.taxEnabled(), settings.taxRate());
        var order = new OrderStore.Order(UUID.randomUUID().toString(), "Customer", "09000000000", "Handle carefully",
                "Cash", "", number("100"), lines, totals, settings);
        var confirmation = new CheckoutService().confirm(order);
        check(confirmation.receipt().contains("Date:") && confirmation.receipt().contains("Branch: Main branch"),
                "Confirmed receipt snapshots include date and source branch");
        check(balance("main", product.id()).compareTo(number("18")) == 0, "Checkout deducts stock once");
        new TeamStore().save(List.of(new TeamStore.Member(ActiveProfile.current().id(), "New operator", "", "Owner", true)));
        var retry = new CheckoutService().confirm(order);
        check(retry.equals(confirmation) && Repositories.orders().read().sales().size() == 1,
                "Retry returns the same confirmation without a second sale");
        check(Repositories.orders().read().sales().get(0).receipt().equals(confirmation.receipt()),
                "Historical receipt keeps original operator and confirmed text");
        var changed = new OrderStore.Order(order.id(), "Different customer", order.phone(), order.notes(),
                order.payment(), order.reference(), order.tendered(), order.lines(), order.totals(), order.settings());
        reject(() -> new CheckoutService().confirm(changed), "Reusing an order ID for a different payload must fail");
        var delegate = new FileRepositories();
        AtomicInteger fakeCalls = new AtomicInteger();
        RepositoryProvider fake = new RepositoryProvider() {
            public Catalog catalog() throws IOException {
                return delegate.catalog();
            }

            public Inventory inventory() throws IOException {
                return delegate.inventory();
            }

            public Team team() {
                return delegate.team();
            }

            public Settings settings() {
                return delegate.settings();
            }

            public Assets assets() {
                return delegate.assets();
            }

            public Operations operations() {
                return delegate.operations();
            }

            public Orders orders() {
                return new Orders() {
                    public Confirmation confirm(OrderStore.Order value) {
                        fakeCalls.incrementAndGet();
                        return new Confirmation(value.id(), "adapter receipt");
                    }

                    public SavedSales.Result read() throws IOException {
                        return delegate.orders().read();
                    }
                };
            }
        };
        Repositories.install(fake);
        try {
            check(new CheckoutService().confirm(order).receipt().equals("adapter receipt") && fakeCalls.get() == 1,
                    "Checkout uses the installed repository contract");
        } finally {
            Repositories.install(delegate);
        }
        var north = Repositories.operations().addBranch("North branch");
        reject(() -> Repositories.operations().addBranch("north branch"),
                "Branch names cannot duplicate case-insensitively");
        var transfer = Repositories.operations().request("main", north.id(), product.id(), number("5"),
                "Restock North");
        check(balance("main", product.id()).compareTo(number("18")) == 0, "A request does not move stock");
        Repositories.operations().advanceTransfer(transfer.id(), OperationsStore.TransferState.DISPATCHED);
        check(balance("main", product.id()).compareTo(number("13")) == 0
                && balance(north.id(), product.id()).signum() == 0,
                "Dispatch deducts source and leaves destination awaiting receipt");
        reject(() -> Repositories.operations().advanceTransfer(transfer.id(), OperationsStore.TransferState.DISPATCHED),
                "Repeated dispatch cannot deduct twice");
        Repositories.operations().advanceTransfer(transfer.id(), OperationsStore.TransferState.RECEIVED);
        check(balance(north.id(), product.id()).compareTo(number("5")) == 0, "Receive credits destination stock");
        check(StockAlerts.read().stream().anyMatch(a -> a.branchId().equals(north.id())),
                "A received product at its default threshold creates a branch alert");
        reject(() -> Repositories.operations().advanceTransfer(transfer.id(), OperationsStore.TransferState.RECEIVED),
                "Repeated receipt cannot credit twice");
        Repositories.inventory(north.id()).setCount(product.id(), number("2"), number("1"), "Recount after receiving");
        check(balance(north.id(), product.id()).compareTo(number("2")) == 0,
                "Recount includes transfer deltas instead of adding them twice");
        var back = Repositories.operations().request(north.id(), "main", product.id(), number("1"), "Return stock");
        Repositories.operations().advanceTransfer(back.id(), OperationsStore.TransferState.DISPATCHED);
        Repositories.operations().advanceTransfer(back.id(), OperationsStore.TransferState.RECEIVED);
        check(balance("main", product.id()).compareTo(number("14")) == 0
                && balance(north.id(), product.id()).compareTo(number("1")) == 0,
                "Reverse-direction transfer reconciles both ledgers");
        reject(() -> Repositories.operations().request("main", "main", product.id(), number("1"), "Invalid"),
                "Same-branch transfer must fail");
        reject(() -> Repositories.operations().request("main", north.id(), product.id(), number("0.5"),
                "Invalid pieces"), "Piece products require whole transfer quantities");
        var cancel = Repositories.operations().request("main", north.id(), product.id(), number("1"),
                "Cancelled request");
        Repositories.operations().advanceTransfer(cancel.id(), OperationsStore.TransferState.CANCELLED);
        check(balance("main", product.id()).compareTo(number("14")) == 0,
                "Cancelling an un-dispatched request leaves stock unchanged");
        reject(() -> Repositories.operations().advanceTransfer(cancel.id(), OperationsStore.TransferState.DISPATCHED),
                "Cancelled transfer cannot be dispatched");
        var excessive = Repositories.operations().request("main", north.id(), product.id(), number("999"),
                "More than available");
        reject(() -> Repositories.operations().advanceTransfer(excessive.id(),
                OperationsStore.TransferState.DISPATCHED), "Insufficient source stock blocks dispatch");
        Repositories.inventory().setCount(product.id(), number("1"), number("3"), "Last unit concurrency");
        var a = Repositories.operations().request("main", north.id(), product.id(), number("1"), "Request A");
        var b = Repositories.operations().request("main", north.id(), product.id(), number("1"), "Request B");
        check(concurrent(() -> {
            try {
                Repositories.operations().advanceTransfer(a.id(), OperationsStore.TransferState.DISPATCHED);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }, () -> {
            try {
                Repositories.operations().advanceTransfer(b.id(), OperationsStore.TransferState.DISPATCHED);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }) == 1, "Only one concurrent transfer can dispatch the last unit");
        check(balance("main", product.id()).signum() == 0, "Concurrent dispatch does not oversell stock");
        var washer = Repositories.operations().addMachine("Washer 1", "Washer");
        var dryer = Repositories.operations().addMachine("Dryer 1", "Dryer");
        Instant start = Instant.now().plusSeconds(3600), end = start.plusSeconds(2700);
        var first = Repositories.operations().reserve(washer.id(), order.id(), start, end, "Wash customer order");
        reject(() -> Repositories.operations().reserve(washer.id(), "", start.plusSeconds(60), end.plusSeconds(60),
                "Overlap"), "Overlapping reservations on the same machine fail");
        var adjacent = Repositories.operations().reserve(washer.id(), "", end, end.plusSeconds(1800), "Next slot");
        check(Repositories.operations().bookings().size() == 2, "Adjacent intervals are allowed");
        var other = Repositories.operations().reserve(dryer.id(), "", start, end, "Other machine");
        check(Repositories.operations().bookings().size() == 3, "Other machines can use the same time window");
        reject(() -> Repositories.operations().setMachineEnabled(washer.id(), false),
                "Maintenance cannot discard active bookings");
        reject(() -> Repositories.operations().advanceBooking(first.id(), OperationsStore.BookingState.RUNNING),
                "Bookings cannot start outside their reserved window");
        Repositories.operations().advanceBooking(first.id(), OperationsStore.BookingState.CANCELLED);
        var current = Repositories.operations().reserve(washer.id(), order.id(), Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(600), "Current booking");
        Repositories.operations().advanceBooking(current.id(), OperationsStore.BookingState.RUNNING);
        Repositories.operations().advanceBooking(current.id(), OperationsStore.BookingState.COMPLETED);
        reject(() -> Repositories.operations().advanceBooking(current.id(), OperationsStore.BookingState.RUNNING),
                "Completed booking cannot restart");
        Repositories.operations().advanceBooking(other.id(), OperationsStore.BookingState.CANCELLED);
        Repositories.operations().setMachineEnabled(dryer.id(), false);
        reject(() -> Repositories.operations().reserve(dryer.id(), "", start, end, "Unavailable"),
                "Maintenance machines cannot be booked");
        Repositories.operations().setMachineEnabled(dryer.id(), true);
        check(concurrent(() -> {
            try {
                Repositories.operations().reserve(dryer.id(), "", start, end, "Concurrent A");
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }, () -> {
            try {
                Repositories.operations().reserve(dryer.id(), "", start, end, "Concurrent B");
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }) == 1, "Concurrent overlapping bookings commit only one reservation");
        reject(() -> Repositories.operations().reserve(washer.id(), "unknown-order", start, end, "Unknown receipt"),
                "Linked receipts must exist");
        var updated = new PosSettings.Values(settings.storeName(), settings.address(), settings.phone(),
                settings.receiptFooter(), settings.accent(), settings.compact(), settings.listView(),
                settings.taxEnabled(), settings.taxRate(), settings.gcashEnabled(), 58);
        Repositories.settings().save(updated);
        check(AtomicProperties.read(DataLocation.resolve("pos-settings.properties")).getProperty("receipt.width")
                .equals("58"), "Receipt paper preference persists");
        Path picture = sandbox.resolve("photo.png");
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", picture.toFile());
        String key = Repositories.assets().importPhoto(picture);
        var photoProduct = new CatalogItem(product.id(), product.name(), product.description(), product.category(),
                product.unit(), product.price(), true, key);
        var items = new ArrayList<>(catalog.items());
        items.set(items.indexOf(product), photoProduct);
        catalog.save(items);
        Map<String, String> before = hashes(data);
        var audit = MigrationAudit.scan();
        check(audit.unreadable() == 0 && audit.items() == items.size() && audit.orders() == 1,
                "Read-only audit counts canonical records");
        check(hashes(data).equals(before), "Audit leaves all source bytes unchanged");
        Path backup = BackupService.create(sandbox.resolve("backup.zip"));
        var verified = BackupService.verify(backup);
        check(verified.files() > 10, "Backup includes raw records, images, migration exports and manifest");
        try (ZipFile zip = new ZipFile(backup.toFile())) {
            check(zip.getEntry("export/inventory.csv") != null && zip.getEntry("export/bookings.csv") != null
                    && zip.getEntry("data/operations.properties") != null,
                    "Backup carries normalized inventory/operations exports and original ledgers");
        }
        check(hashes(data).equals(before), "Backup leaves live records and photos unchanged");
        Path restored = BackupService.restoreCopy(backup, sandbox.resolve("restored"));
        check(hashes(restored.resolve("data")).equals(before), "Isolated restore reproduces every original data byte");
        reject(() -> BackupService.restoreCopy(backup, data), "Restore must never overwrite live data");
        reject(() -> BackupService.restoreCopy(backup, restored), "Restore must never overwrite an existing copy");
        reject(() -> BackupService.create(backup), "Backup cannot replace an existing archive");
        reject(() -> BackupService.create(data.resolve("recursive.zip")),
                "Backup destination cannot be inside live data");
        Path bad = sandbox.resolve("tampered.zip");
        tamper(backup, bad, "data/catalog.properties");
        reject(() -> BackupService.verify(bad), "Tampered file must fail checksum verification");
        reject(() -> BackupService.restoreCopy(bad, sandbox.resolve("tampered-restore")),
                "Corrupt archive cannot be restored");
        check(!Files.exists(sandbox.resolve("tampered-restore")), "Failed verification leaves no partial destination");
        Path unsafe = sandbox.resolve("unsafe.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(unsafe))) {
            zip.putNextEntry(new ZipEntry("manifest.properties"));
            zip.write("format=laundry-snapshot-1\ncount=1\nfile.0.path=data/../../escape.txt\nfile.0.sha256=x\n"
                    .getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("data/../../escape.txt"));
            zip.write(1);
            zip.closeEntry();
        }
        reject(() -> BackupService.restoreCopy(unsafe, sandbox.resolve("unsafe-restore")),
                "Traversal paths cannot escape a restore copy");
        check(!Files.exists(sandbox.resolve("escape.txt")), "Unsafe restore writes no escaped file");
        check(MigrationAudit.csv(" =SUM(A1)", "quoted\"name", "two\nlines").startsWith("\"' =SUM(A1)\""),
                "Export neutralizes spreadsheet formulas and quotes cells");
        Process restart = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-cp", System.getProperty("java.class.path"), "FeatureRestartProbe",
                restored.resolve("data").toString()).redirectErrorStream(true).start();
        check(restart.waitFor(15, TimeUnit.SECONDS), "Restored state loads in a fresh application process");
        String restartOutput = new String(restart.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        check(restart.exitValue() == 0 && restartOutput.contains("RESTORED_READY"),
                "Restored branch/transfer/schedule/receipt settings survive restart: " + restartOutput);
        Files.writeString(DataLocation.resolve("orders/broken.properties"), "id=broken\n");
        check(MigrationAudit.scan().unreadable() == 1, "Audit surfaces unreadable orders rather than inventing values");
        reject(StockAlerts::read, "Stock warnings must fail closed if an order is unreadable");
        Path damagedBackup = BackupService.create(sandbox.resolve("needs-review.zip"));
        check(BackupService.verify(damagedBackup).files() > 0,
                "Damaged business records can still be backed up for recovery");
        check(hashes(data).keySet().containsAll(before.keySet()),
                "All original records remain after restore/error checks");
        System.out.println("PASS: " + checks
                + " repository, backup, restore, migration audit, threshold, transfer and scheduling checks.");
    }
}
