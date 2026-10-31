package application;

import java.awt.*;
import java.awt.print.*;
import java.util.*;
import java.util.List;

/**
 * Text from a confirmed receipt, laid out for 58/80 mm paper with repeatable
 * pagination.
 */
public final class ReceiptPrinter implements Printable {
    private final String text;
    private final int width;
    private final persistence.PrinterPreferences.Config config;

    public ReceiptPrinter(String text, int width) {
        this(text, persistence.PrinterPreferences.defaults(width));
    }

    public ReceiptPrinter(String text, persistence.PrinterPreferences.Config config) {
        this.text = Objects.requireNonNull(text);
        this.config = Objects.requireNonNull(config);
        this.width = config.paperWidth();
    }

    public Font font() {
        return new Font("Monospaced", Font.PLAIN, config.fontSize());
    }

    public PageFormat pageFormat() {
        double scale = 72.0 / 25.4, w = width * scale;
        double left = config.leftMm() * scale, right = config.rightMm() * scale, top = config.topMm() * scale,
                bottom = (config.bottomMm() + config.feedMm()) * scale;
        var image = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        double needed;
        try {
            g.setFont(font());
            var metrics = g.getFontMetrics();
            needed = top + bottom + lines(metrics, w - left - right).size() * metrics.getHeight() + 2;
        } finally {
            g.dispose();
        }
        // Short receipts use only their content height. Long receipts paginate within a
        // standard maximum roll segment.
        double height = Math.max(40 * scale, Math.min(297 * scale, needed));
        Paper paper = new Paper();
        paper.setSize(w, height);
        paper.setImageableArea(left, top, w - left - right, height - top - bottom);
        PageFormat format = new PageFormat();
        format.setPaper(paper);
        return format;
    }

    public List<String> lines(FontMetrics metrics, double available) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\R", -1)) {
            StringBuilder current = new StringBuilder();
            for (int offset = 0; offset < line.length();) {
                int code = line.codePointAt(offset);
                String next = new String(Character.toChars(code));
                if (metrics.stringWidth(current + next) > available && !current.isEmpty()) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                current.append(next);
                offset += Character.charCount(code);
            }
            lines.add(current.toString());
        }
        return List.copyOf(lines);
    }

    @Override
    public int print(Graphics graphics, PageFormat page, int pageIndex) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            Font font = font();
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics();
            double requestedWidth = (width - config.leftMm() - config.rightMm()) * 72.0 / 25.4;
            List<String> lines = lines(metrics, Math.min(page.getImageableWidth(), requestedWidth));
            int perPage = Math.max(1, (int) (page.getImageableHeight() / metrics.getHeight()));
            if (pageIndex < 0 || pageIndex * perPage >= lines.size())
                return NO_SUCH_PAGE;
            g.translate(page.getImageableX(), page.getImageableY());
            g.setColor(Color.BLACK);
            for (int i = pageIndex * perPage,
                    row = 0; i < Math.min(lines.size(), (pageIndex + 1) * perPage); i++, row++)
                g.drawString(lines.get(i), 0, row * metrics.getHeight() + metrics.getAscent());
            return PAGE_EXISTS;
        } finally {
            g.dispose();
        }
    }

}
