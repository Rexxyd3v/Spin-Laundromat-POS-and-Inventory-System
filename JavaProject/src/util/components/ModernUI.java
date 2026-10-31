package util.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.config.UIStyle;

public final class ModernUI {
    private ModernUI() {
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JPanel row(int gap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    public static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static JScrollPane scroll(Component content) {
        JScrollPane s = new JScrollPane(content instanceof Scrollable ? content : new WidthPanel(content));
        s.setUI(new javax.swing.plaf.basic.BasicScrollPaneUI());
        s.setBorder(null);
        s.setViewportBorder(null);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUI(new Scrollbar());
        s.getVerticalScrollBar().setOpaque(false);
        s.getHorizontalScrollBar().setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(24);
        s.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if (content instanceof JTable table)
            s.setColumnHeaderView(table.getTableHeader());
        s.addMouseWheelListener(event -> {
            JScrollBar bar = s.getVerticalScrollBar();
            boolean edge = event.getWheelRotation() < 0 ? bar.getValue() <= bar.getMinimum()
                    : bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum();
            if (edge && s.getParent() != null) {
                JScrollPane parent = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, s.getParent());
                if (parent != null) {
                    Point point = SwingUtilities.convertPoint(s, event.getPoint(), parent);
                    parent.dispatchEvent(new java.awt.event.MouseWheelEvent(parent, event.getID(), event.getWhen(),
                            event.getModifiersEx(), point.x, point.y, event.getClickCount(), event.isPopupTrigger(),
                            event.getScrollType(), event.getScrollAmount(), event.getWheelRotation()));
                    event.consume();
                }
            }
        });
        return s;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(UIStyle.REGULAR);
        combo.setBackground(Color.WHITE);
        combo.setForeground(UIStyle.TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER),
                new EmptyBorder(4, 8, 4, 4)));
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton(new AppIcon(AppIcon.Name.CHEVRON, 14, UIStyle.TEXT_SECONDARY));
                arrow.setPreferredSize(new Dimension(28, 28));
                arrow.setBorder(null);
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean focus) {
                g.setColor(Color.WHITE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        combo.setPreferredSize(new Dimension(160, 42));
        return combo;
    }

    public static JSpinner quantity(double value, double minimum, double maximum, double step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
        spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                JButton button = new JButton(new AppIcon(AppIcon.Name.PLUS, 10, UIStyle.TEXT_SECONDARY));
                button.setBorder(null);
                button.setContentAreaFilled(false);
                button.setPreferredSize(new Dimension(22, 16));
                installNextButtonListeners(button);
                return button;
            }

            @Override
            protected Component createPreviousButton() {
                JButton button = new JButton(new AppIcon(AppIcon.Name.MINUS, 10, UIStyle.TEXT_SECONDARY));
                button.setBorder(null);
                button.setContentAreaFilled(false);
                button.setPreferredSize(new Dimension(22, 16));
                installPreviousButtonListeners(button);
                return button;
            }
        });
        spinner.setBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER));
        spinner.setBackground(Color.WHITE);
        return spinner;
    }

    public static JTextField field(String accessibleName) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Object placeholder = getClientProperty("placeholder");
                if (getText().isEmpty() && placeholder != null) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setFont(getFont());
                    g.setColor(UIStyle.TEXT_SECONDARY);
                    g.drawString(placeholder.toString(), getInsets().left,
                            (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
                    g.dispose();
                }
            }
        };
        f.setFont(UIStyle.REGULAR);
        f.setForeground(UIStyle.TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UIStyle.INPUT_BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        f.getAccessibleContext().setAccessibleName(accessibleName);
        return f;
    }

    public static JPanel formField(String title, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = label(title, UIStyle.BOLD, UIStyle.TEXT_PRIMARY);
        l.setLabelFor(field);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static JButton button(String text, AppIcon.Name icon, boolean primary) {
        return new ActionButton(text, icon, primary);
    }

    public static JButton iconButton(String tooltip, AppIcon.Name icon) {
        JButton b = button("", icon, false);
        b.setToolTipText(tooltip);
        b.getAccessibleContext().setAccessibleName(tooltip);
        b.setPreferredSize(new Dimension(36, 36));
        return b;
    }

    private static class WidthPanel extends JPanel implements Scrollable {
        WidthPanel(Component content) {
            super(new BorderLayout());
            setOpaque(false);
            add(content);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }

        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return Math.max(24, r.height - 24);
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getPreferredSize().height < getParent().getHeight();
        }
    }

    public static class Surface extends JPanel {
        public Surface() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(20, 20, 20, 20));
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UIStyle.CARD_BG);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(UIStyle.INPUT_BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.dispose();
        }
    }

    private static class ActionButton extends JButton {
        private final boolean primary;

        ActionButton(String text, AppIcon.Name icon, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(UIStyle.BOLD);
            setIconTextGap(8);
            if (icon != null)
                setIcon(new AppIcon(icon, 18));
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = primary ? UIStyle.PRIMARY : UIStyle.WHITE;
            if (!isEnabled())
                fill = UIStyle.INPUT_BORDER;
            else if (getModel().isPressed())
                fill = primary ? fill.darker() : UIStyle.INPUT_BORDER;
            else if (getModel().isRollover())
                fill = primary ? fill.darker() : UIStyle.BACKGROUND;
            g.setColor(fill);
            g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g.setColor(isFocusOwner() ? UIStyle.PRIMARY : primary ? fill : UIStyle.INPUT_BORDER);
            g.setStroke(new BasicStroke(isFocusOwner() ? 2 : 1));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            setForeground(!isEnabled() ? UIStyle.TEXT_SECONDARY : primary ? Color.WHITE : UIStyle.TEXT_PRIMARY);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
