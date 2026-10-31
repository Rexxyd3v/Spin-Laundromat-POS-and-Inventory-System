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

    public ReceiptPrinter(String text, int width) {
        if (width != 58 && width != 80)
            throw new IllegalArgumentException("Choose 58 or 80 mm paper.");
        this.text = Objects.requireNonNull(text);
        this.width = width;
    }

    public PageFormat pageFormat() {
        Paper paper = new Paper();
        double w = width * 72.0 / 25.4, h = 297 * 72.0 / 25.4, margin = 3 * 72.0 / 25.4;
        paper.setSize(w, h);
        paper.setImageableArea(margin, margin, w - margin * 2, h - margin * 2);
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
            Font font = new Font("Monospaced", Font.PLAIN, width == 58 ? 8 : 9);
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics();
            List<String> lines = lines(metrics, page.getImageableWidth());
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
