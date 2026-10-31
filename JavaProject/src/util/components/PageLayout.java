package util.components;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import util.config.UIStyle;
import static util.components.ModernUI.*;

/** A single page rhythm for headers, filters, data and empty states. */
public class PageLayout extends JPanel {
    protected final JPanel body = new JPanel(new BorderLayout(0, 20));
    protected final JPanel actions = row(8);
    protected final JScrollPane pageScroll;

    public PageLayout(String eyebrow, String title, String description) {
        super(new BorderLayout(0, 24));
        setBackground(UIStyle.BACKGROUND);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            private boolean stacked;

            private void arrange() {
                boolean compact = actions.getComponentCount() > 0 && PageLayout.this.getWidth() > 0
                        && PageLayout.this.getWidth() - 56 < 840;
                if (compact != stacked) {
                    stacked = compact;
                    actions.setBorder(new EmptyBorder(stacked ? 10 : 0, 0, 0, 0));
                    remove(actions);
                    add(actions, stacked ? BorderLayout.SOUTH : BorderLayout.EAST);
                    revalidate();
                }
            }

            @Override
            public Dimension getPreferredSize() {
                arrange();
                return super.getPreferredSize();
            }

            @Override
            public void doLayout() {
                arrange();
                super.doLayout();
            }
        };
        header.setOpaque(false);
        JPanel text = column();
        text.add(label(eyebrow.toUpperCase(), new Font("Segoe UI", Font.BOLD, 11), UIStyle.PRIMARY));
        text.add(Box.createVerticalStrut(6));
        text.add(label(title, UIStyle.TITLE, UIStyle.TEXT_PRIMARY));
        text.add(Box.createVerticalStrut(6));
        text.add(label(description, UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        header.add(text, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        body.setOpaque(false);
        pageScroll = scroll(body);
        pageScroll.setName("page-scroll");
        pageScroll.getVerticalScrollBar().setUnitIncrement(32);
        add(pageScroll, BorderLayout.CENTER);
    }

    public static JPanel metric(String title, String value, String hint, AppIcon.Name icon) {
        Surface card = new Surface();
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel text = column();
        text.add(label(title, UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        text.add(Box.createVerticalStrut(10));
        text.add(label(value, new Font("Segoe UI", Font.BOLD, 27), UIStyle.TEXT_PRIMARY));
        text.add(Box.createVerticalStrut(7));
        text.add(label(hint, new Font("Segoe UI", Font.PLAIN, 11), UIStyle.TEXT_SECONDARY));
        card.add(text, BorderLayout.CENTER);
        card.add(new JLabel(new AppIcon(icon, 23, UIStyle.PRIMARY)), BorderLayout.EAST);
        return card;
    }

    public static JPanel empty(String title, String hint, AppIcon.Name icon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JPanel content = column();
        JLabel mark = new JLabel(new AppIcon(icon, 40, UIStyle.TEXT_SECONDARY));
        mark.setAlignmentX(LEFT_ALIGNMENT);
        content.add(mark);
        content.add(Box.createVerticalStrut(18));
        content.add(label(title, UIStyle.SUBTITLE, UIStyle.TEXT_PRIMARY));
        content.add(Box.createVerticalStrut(8));
        content.add(label(hint, UIStyle.REGULAR, UIStyle.TEXT_SECONDARY));
        panel.add(content);
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(62);
        table.setFont(UIStyle.REGULAR);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.getTableHeader().setUI(new javax.swing.plaf.basic.BasicTableHeaderUI());
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(8, 16, 8, 16));
                setBackground(new Color(248, 250, 252));
                setForeground(UIStyle.TEXT_SECONDARY);
                setFont(new Font("Segoe UI", Font.BOLD, 10));
                return this;
            }
        });
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(100, 44));
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus,
                    int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(10, 16, 10, 16));
                setForeground(selected ? UIStyle.PRIMARY : UIStyle.TEXT_PRIMARY);
                setBackground(selected ? UIStyle.tint() : row % 2 == 0 ? Color.WHITE : new Color(249, 250, 252));
                return this;
            }
        });
    }

    public static JPanel tableCard(String title, JTable table, JComponent footer) {
        Surface card = new Surface();
        card.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel heading = label(title, UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
        heading.setBorder(new EmptyBorder(16, 16, 18, 16));
        card.add(heading, BorderLayout.NORTH);
        card.add(scroll(table), BorderLayout.CENTER);
        if (footer != null) {
            footer.setBorder(new EmptyBorder(12, 16, 12, 16));
            card.add(footer, BorderLayout.SOUTH);
        }
        return card;
    }
}
