package persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.List;
import model.*;
import util.config.PosSettings;

/**
 * The contracts consumed by screens and use cases; file storage is one
 * implementation.
 */
public interface RepositoryProvider {
    Catalog catalog() throws IOException;

    Inventory inventory() throws IOException;

    default Inventory inventory(String branch) throws IOException {
        if (!"main".equals(branch))
            throw new IOException("Branch inventory adapter is unavailable.");
        return inventory();
    }

    Team team();

    Orders orders();

    Settings settings();

    Assets assets();

    Operations operations();

    interface Operations {
        List<OperationsStore.Branch> branches() throws IOException;

        OperationsStore.Branch addBranch(String name) throws IOException;

        List<OperationsStore.Transfer> transfers() throws IOException;

        OperationsStore.Transfer request(String from, String to, String item, BigDecimal quantity, String note)
                throws IOException;

        void advanceTransfer(String id, OperationsStore.TransferState next) throws IOException;

        List<OperationsStore.Machine> machines() throws IOException;

        OperationsStore.Machine addMachine(String name, String type) throws IOException;

        void setMachineEnabled(String id, boolean enabled) throws IOException;

        List<OperationsStore.Booking> bookings() throws IOException;

        OperationsStore.Booking reserve(String machine, String order, java.time.Instant start, java.time.Instant end,
                String note) throws IOException;

        void advanceBooking(String id, OperationsStore.BookingState next) throws IOException;
    }

    interface Catalog {
        List<CatalogItem> items();

        void save(List<CatalogItem> items) throws IOException;
    }

    interface Inventory {
        boolean tracked(String id);

        BigDecimal minimum(String id);

        BigDecimal available(String id, List<SavedSales.Sale> sales);

        void setCount(String id, BigDecimal count, BigDecimal minimum, String note) throws IOException;
    }

    interface Team {
        List<TeamStore.Member> read() throws IOException;

        void save(List<TeamStore.Member> members) throws IOException;
    }

    record Confirmation(String id, String receipt) {
    }

    interface Orders {
        Confirmation confirm(OrderStore.Order order) throws IOException;

        SavedSales.Result read() throws IOException;
    }

    interface Settings {
        PosSettings.Values get();

        PosSettings.Values defaults();

        String loadError();

        void save(PosSettings.Values values) throws IOException;

        void addListener(PropertyChangeListener listener);

        void removeListener(PropertyChangeListener listener);
    }

    interface Assets {
        void validate(Path file) throws IOException;

        String importPhoto(Path file) throws IOException;

        Path resolve(String key) throws IOException;

        BufferedImage read(Path file) throws IOException;
    }
}
