package application;

import java.awt.print.*;
import java.util.*;
import java.util.List;
import javax.print.*;
import javax.print.attribute.standard.*;
import persistence.PrinterPreferences;

/**
 * Windows-installed 2D printer queues. Discovery and job preparation run away
 * from Swing.
 */
public final class PrinterServiceManager {
    public record Device(String name, String status, boolean systemDefault, boolean accepting) {
        public String toString() {
            return name;
        }
    }

    public record Prepared(PrinterJob job, PageFormat page) {
    }

    public static List<Device> discover() {
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        String defaultName = defaultService == null ? "" : defaultService.getName();
        List<Device> devices = new ArrayList<>();
        for (PrintService service : PrinterJob.lookupPrintServices()) {
            var accepting = service.getAttribute(PrinterIsAcceptingJobs.class);
            var state = service.getAttribute(PrinterState.class);
            var queued = service.getAttribute(QueuedJobCount.class);
            String status = state == null ? "Status not reported"
                    : state == PrinterState.IDLE ? "Idle"
                            : state == PrinterState.PROCESSING ? "Processing jobs"
                                    : state == PrinterState.STOPPED ? "Stopped — check printer" : "Unknown status";
            if (PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(accepting))
                status = "Not accepting jobs — check connection or queue";
            if (queued != null && queued.getValue() > 0)
                status += " · " + queued.getValue() + " queued";
            devices.add(new Device(service.getName(), status, service.getName().equals(defaultName),
                    !PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(accepting)));
        }
        devices.sort(Comparator.comparing(Device::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(devices);
    }

    /**
     * An explicitly selected missing printer never silently falls back to a
     * different queue.
     */
    public static PrintService choose(String selected, List<PrintService> services, PrintService defaultService)
            throws PrinterException {
        PrintService result = selected.isBlank() ? defaultService
                : services.stream().filter(s -> s.getName().equals(selected)).findFirst().orElse(null);
        if (result == null)
            throw new PrinterException(selected.isBlank()
                    ? "No Windows default printer is available. Install or select a printer in Receipt printer settings."
                    : "The saved printer is not available: " + selected
                            + ". Reconnect it or choose another printer in settings.");
        if (PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS.equals(result.getAttribute(PrinterIsAcceptingJobs.class)))
            throw new PrinterException(
                    "The selected printer is not accepting jobs. Check its connection, paper and Windows queue.");
        return result;
    }

    public static Prepared prepare(String receipt, PrinterPreferences.Config config) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        PrintService service = choose(config.printerName(), Arrays.asList(PrinterJob.lookupPrintServices()),
                PrintServiceLookup.lookupDefaultPrintService());
        job.setPrintService(service);
        job.setJobName("Laundry receipt");
        job.setCopies(1);
        ReceiptPrinter document = new ReceiptPrinter(receipt, config);
        PageFormat page = job.validatePage(document.pageFormat());
        if (page.getImageableWidth() < 20 || page.getImageableHeight() < 20)
            throw new PrinterException(
                    "Printer driver has no usable print area. Check the paper size in Windows printer preferences.");
        job.setPrintable(document, page);
        return new Prepared(job, page);
    }

    private PrinterServiceManager() {
    }
}
