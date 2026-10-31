package ui.panels;

import java.awt.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;
import persistence.Repositories;
import util.components.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/**
 * Transfer and scheduling controls share the same local repository and atomic
 * state transitions.
 */
public final class OperationsPage extends PageLayout implements BusinessPages.Refreshable {
    private static final ZoneId ZONE = ZoneId.of("Asia/Manila");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT),
            DISPLAY = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZONE);
    private final JLabel status = label("Loading operations…", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final DefaultTableModel transfers = model("PRODUCT", "FROM → TO", "QUANTITY", "STATE", "UPDATED BY"),
            machines = model("MACHINE", "TYPE", "AVAILABILITY"),
            bookings = model("MACHINE", "START", "END", "ORDER", "STATE");
    private final JTable transferTable = new JTable(transfers), machineTable = new JTable(machines),
            bookingTable = new JTable(bookings);
    private final JButton request = button("Request transfer", AppIcon.Name.PLUS, true),
            branch = button("Add branch", AppIcon.Name.STORE, false),
            dispatch = button("Dispatch", AppIcon.Name.UPLOAD, false),
            receive = button("Receive", AppIcon.Name.DOWNLOAD, false),
            cancelTransfer = button("Cancel request", AppIcon.Name.CLOSE, false),
            addMachine = button("Add machine", AppIcon.Name.PLUS, true),
            maintenance = button("Toggle maintenance", AppIcon.Name.SETTINGS, false),
            reserve = button("Reserve machine", AppIcon.Name.PLUS, true),
            start = button("Start", AppIcon.Name.ARROW, false),
            complete = button("Complete", AppIcon.Name.CHECK, false),
            cancelBooking = button("Cancel booking", AppIcon.Name.CLOSE, false);
    private final List<JButton> controls = List.of(request, branch, dispatch, receive, cancelTransfer, addMachine,
            maintenance, reserve, start, complete, cancelBooking);
    private Snapshot data = new Snapshot(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    private boolean mutating, loading;
    private long generation;

    private record Snapshot(List<OperationsStore.Branch> branches, List<OperationsStore.Transfer> transfers,
            List<OperationsStore.Machine> machines, List<OperationsStore.Booking> bookings, List<CatalogItem> products,
            List<SavedSales.Sale> sales) {
    }

    public OperationsPage() {
        super("BRANCH & LAUNDRY OPERATIONS", "Operations",
                "Move tracked stock and schedule washer/dryer time in this local workspace.");
        JTabbedPane tabs = new ModernTabs();
        tabs.setName("operationsTabs");
        tabs.setFont(UIStyle.REGULAR);
        tabs.addTab("Stock transfers", section("Transfer history", transferTable,
                new JButton[] { request, branch, dispatch, receive, cancelTransfer }));
        tabs.addTab("Schedule", section("Machine reservations · Manila time", bookingTable,
                new JButton[] { reserve, start, complete, cancelBooking }));
        tabs.addTab("Machines",
                section("Machines & maintenance", machineTable, new JButton[] { addMachine, maintenance }));
        body.add(tabs, BorderLayout.CENTER);
        body.add(status, BorderLayout.SOUTH);
        int[] widths = { 180, 260, 100, 140, 150 };
        for (int i = 0; i < widths.length; i++)
            transferTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        transferTable.setName("transfersTable");
        bookingTable.setName("bookingsTable");
        machineTable.setName("machinesTable");
        for (JTable table : new JTable[] { transferTable, machineTable, bookingTable })
            table.getSelectionModel().addListSelectionListener(e -> updateControls());
        branch.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New branch name", "Add branch", JOptionPane.PLAIN_MESSAGE);
            if (name != null)
                mutate(() -> Repositories.operations().addBranch(name),
                        "Branch created. Record its opening stock in Inventory.");
        });
        request.addActionListener(e -> requestTransfer());
        dispatch.addActionListener(e -> transfer(OperationsStore.TransferState.DISPATCHED));
        receive.addActionListener(e -> transfer(OperationsStore.TransferState.RECEIVED));
        cancelTransfer.addActionListener(e -> transfer(OperationsStore.TransferState.CANCELLED));
        addMachine.addActionListener(e -> {
            JTextField name = field("Machine name");
            JComboBox<String> type = combo(new String[] { "Washer", "Dryer" });
            if (form("Add machine", formField("Machine name", name), formField("Type", type))) {
                String n = name.getText(), t = (String) type.getSelectedItem();
                mutate(() -> Repositories.operations().addMachine(n, t), "Machine added.");
            }
        });
        maintenance.addActionListener(e -> {
            int row = machineTable.getSelectedRow();
            if (row >= 0) {
                var m = data.machines().get(row);
                mutate(() -> Repositories.operations().setMachineEnabled(m.id(), !m.enabled()),
                        m.enabled() ? "Machine marked for maintenance." : "Machine available for bookings.");
            }
        });
        reserve.addActionListener(e -> reserveMachine());
        start.addActionListener(e -> booking(OperationsStore.BookingState.RUNNING));
        complete.addActionListener(e -> booking(OperationsStore.BookingState.COMPLETED));
        cancelBooking.addActionListener(e -> booking(OperationsStore.BookingState.CANCELLED));
        refresh();
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel section(String title, JTable table, JButton[] buttons) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        styleTable(table);
        table.setRowHeight(54);
        JPanel card = tableCard(title, table, null);
        card.setPreferredSize(new Dimension(580, 390));
        panel.add(card, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, Math.min(3, buttons.length), 8, 8));
        actions.setOpaque(false);
        for (JButton b : buttons)
            actions.add(b);
        panel.add(actions, BorderLayout.SOUTH);
        panel.setBorder(new javax.swing.border.EmptyBorder(14, 0, 0, 0));
        return panel;
    }

    private boolean form(String title, JComponent... fields) {
        JPanel content = column();
        for (JComponent field : fields) {
            field.setAlignmentX(LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
            content.add(field);
            content.add(Box.createVerticalStrut(14));
        }
        JScrollPane scroller = scroll(content);
        scroller.setPreferredSize(new Dimension(390, Math.min(360, content.getPreferredSize().height)));
        return JOptionPane.showConfirmDialog(this, scroller, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private void requestTransfer() {
        if (data.branches().size() < 2 || data.products().isEmpty()) {
            status.setText("Add a destination branch and a retail product before requesting a transfer.");
            return;
        }
        JComboBox<OperationsStore.Branch> from = combo(data.branches().toArray(OperationsStore.Branch[]::new)),
                to = combo(data.branches().toArray(OperationsStore.Branch[]::new));
        to.setSelectedIndex(1);
        JComboBox<CatalogItem> product = combo(data.products().toArray(CatalogItem[]::new));
        product.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                    boolean focus) {
                return super.getListCellRendererComponent(list,
                        value instanceof CatalogItem item ? item.name() + " / " + item.unit() : value, index, selected,
                        focus);
            }
        });
        JTextField quantity = field("Quantity"), note = field("Reason");
        quantity.setText("1");
        if (form("Request stock transfer", formField("From branch", from), formField("To branch", to),
                formField("Product", product), formField("Quantity", quantity), formField("Reason", note))) {
            try {
                var f = (OperationsStore.Branch) from.getSelectedItem();
                var t = (OperationsStore.Branch) to.getSelectedItem();
                var p = (CatalogItem) product.getSelectedItem();
                BigDecimal q = new BigDecimal(quantity.getText().trim());
                String n = note.getText();
                mutate(() -> Repositories.operations().request(f.id(), t.id(), p.id(), q, n),
                        "Transfer requested. Dispatch deducts source stock; receipt credits the destination.");
            } catch (RuntimeException ex) {
                status.setText("Enter a valid quantity.");
            }
        }
    }

    private void transfer(OperationsStore.TransferState state) {
        int row = transferTable.getSelectedRow();
        if (row < 0)
            return;
        var t = data.transfers().get(row);
        if (JOptionPane.showConfirmDialog(this, "Confirm " + state.name().toLowerCase(Locale.ROOT) + " for "
                + t.quantity().stripTrailingZeros().toPlainString() + " " + t.unit() + " of " + t.itemName() + "?",
                "Stock transfer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
            mutate(() -> Repositories.operations().advanceTransfer(t.id(), state), "Transfer updated: " + state);
    }

    private record OrderChoice(String id, String title) {
        public String toString() {
            return title;
        }
    }

    private void reserveMachine() {
        var available = data.machines().stream().filter(OperationsStore.Machine::enabled).toList();
        if (available.isEmpty()) {
            status.setText("Add an available machine first.");
            return;
        }
        JComboBox<OperationsStore.Machine> machine = combo(available.toArray(OperationsStore.Machine[]::new));
        List<OrderChoice> orders = new ArrayList<>();
        orders.add(new OrderChoice("", "No linked order"));
        for (var s : data.sales())
            orders.add(new OrderChoice(s.id(),
                    "#" + s.id().substring(0, Math.min(8, s.id().length())) + " · " + s.customer()));
        JComboBox<OrderChoice> order = combo(orders.toArray(OrderChoice[]::new));
        JTextField time = field("Start time"), minutes = field("Duration in minutes"), note = field("Instructions");
        time.setText(INPUT.format(LocalDateTime.now(ZONE).plusMinutes(5)));
        minutes.setText("45");
        if (form("Reserve machine", formField("Machine", machine), formField("Linked receipt (optional)", order),
                formField("Start · yyyy-MM-dd HH:mm (Manila)", time), formField("Duration in minutes", minutes),
                formField("Instructions", note))) {
            try {
                Instant begin = LocalDateTime.parse(time.getText().trim(), INPUT).atZone(ZONE).toInstant();
                int duration = Integer.parseInt(minutes.getText().trim());
                if (duration < 1 || duration >= 1440)
                    throw new IllegalArgumentException();
                Instant end = begin.plusSeconds(duration * 60L);
                String m = ((OperationsStore.Machine) machine.getSelectedItem()).id(),
                        o = ((OrderChoice) order.getSelectedItem()).id(), n = note.getText();
                mutate(() -> Repositories.operations().reserve(m, o, begin, end, n),
                        "Machine reserved. Conflicting times are rejected.");
            } catch (RuntimeException ex) {
                status.setText("Enter a valid future date/time and 1–1439 minutes.");
            }
        }
    }

    private void booking(OperationsStore.BookingState state) {
        int row = bookingTable.getSelectedRow();
        if (row < 0)
            return;
        var b = data.bookings().get(row);
        if (state == OperationsStore.BookingState.CANCELLED
                && JOptionPane.showConfirmDialog(this, "Cancel this booking? Its history will remain.",
                        "Cancel booking", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        mutate(() -> Repositories.operations().advanceBooking(b.id(), state), "Booking updated: " + state);
    }

    private interface Task {
        void run() throws Exception;
    }

    private void mutate(Task task, String message) {
        if (mutating || loading)
            return;
        mutating = true;
        generation++;
        updateControls();
        status.setText("Saving…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            protected void done() {
                mutating = false;
                try {
                    get();
                    load(message);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    status.setText(cause.getMessage());
                    updateControls();
                }
            }
        }.execute();
    }

    public boolean isLoading() {
        return loading || mutating;
    }

    @Override
    public void refresh() {
        load(null);
    }

    private void load(String message) {
        if (mutating)
            return;
        long request = ++generation;
        loading = true;
        updateControls();
        new SwingWorker<Snapshot, Void>() {
            protected Snapshot doInBackground() throws Exception {
                var ops = Repositories.operations();
                return new Snapshot(ops.branches(), ops.transfers(), ops.machines(), ops.bookings(),
                        Repositories.catalog().items().stream().filter(CatalogItem::product).toList(),
                        Repositories.orders().read().sales());
            }

            protected void done() {
                if (request != generation)
                    return;
                loading = false;
                try {
                    data = get();
                    render();
                    status.setText(message == null
                            ? "Local branch ledger · Request → Dispatch → Receive · Machine times use Asia/Manila. No cross-PC synchronization yet."
                            : message);
                } catch (Exception ex) {
                    status.setText("Operations could not be loaded. Review the saved data before retrying.");
                }
                updateControls();
            }
        }.execute();
    }

    private String branchName(String id) {
        return data.branches().stream().filter(b -> b.id().equals(id)).map(OperationsStore.Branch::name).findFirst()
                .orElse(id);
    }

    private String machineName(String id) {
        return data.machines().stream().filter(b -> b.id().equals(id)).map(OperationsStore.Machine::name).findFirst()
                .orElse(id);
    }

    private void render() {
        transfers.setRowCount(0);
        for (var t : data.transfers())
            transfers.addRow(new Object[] { t.itemName(), branchName(t.from()) + " → " + branchName(t.to()),
                    t.quantity().stripTrailingZeros().toPlainString() + " " + t.unit(), t.state(), t.actor() });
        machines.setRowCount(0);
        for (var m : data.machines())
            machines.addRow(new Object[] { m.name(), m.type(), m.enabled() ? "Available" : "Maintenance" });
        bookings.setRowCount(0);
        for (var b : data.bookings())
            bookings.addRow(new Object[] { machineName(b.machineId()), DISPLAY.format(b.start()),
                    DISPLAY.format(b.end()),
                    b.orderId().isBlank() ? "—" : "#" + b.orderId().substring(0, Math.min(8, b.orderId().length())),
                    b.state() });
    }

    private void updateControls() {
        boolean ready = !mutating && !loading;
        for (var b : controls)
            b.setEnabled(ready);
        int row = transferTable.getSelectedRow();
        var transfer = row >= 0 && row < data.transfers().size() ? data.transfers().get(row) : null;
        dispatch.setEnabled(ready && transfer != null && transfer.state() == OperationsStore.TransferState.REQUESTED);
        receive.setEnabled(ready && transfer != null && transfer.state() == OperationsStore.TransferState.DISPATCHED);
        cancelTransfer
                .setEnabled(ready && transfer != null && transfer.state() == OperationsStore.TransferState.REQUESTED);
        row = bookingTable.getSelectedRow();
        var booking = row >= 0 && row < data.bookings().size() ? data.bookings().get(row) : null;
        start.setEnabled(ready && booking != null && booking.state() == OperationsStore.BookingState.RESERVED);
        complete.setEnabled(ready && booking != null && booking.state() == OperationsStore.BookingState.RUNNING);
        cancelBooking.setEnabled(ready && booking != null && (booking.state() == OperationsStore.BookingState.RESERVED
                || booking.state() == OperationsStore.BookingState.RUNNING));
        maintenance.setEnabled(ready && machineTable.getSelectedRow() >= 0);
    }
}
