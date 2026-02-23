package src.ui.panels.reportPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import src.util.components.Scrollbar;
import src.util.components.ShadowPanel;
import src.util.config.UIStyle;

public class LaundryReportsSystem extends JPanel {
    private String activeTab = "Profitability";
    private JPanel centerArea;
    private JPanel statsPanel;
    private JPanel tablePanel;
    private JLabel cardTitle;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JPanel tabPanel;
    private ReceiptPanel receiptPanel;
    private JPanel masterDetail;

    public LaundryReportsSystem() {
        tableModel = new DefaultTableModel();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        // Header Section
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Generate comprehensive business reports");
        subtitle.setFont(UIStyle.REGULAR);
        subtitle.setForeground(UIStyle.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Center Area (Tabs + Card)
        centerArea = new JPanel(new BorderLayout(0, 25));
        centerArea.setOpaque(false);
        centerArea.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // Tab Bar
        tabPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        tabPanel.setOpaque(false);
        tabPanel.setMaximumSize(new Dimension(800, 45));
        updateTabs();

        JPanel tabContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabContainer.setOpaque(false);
        tabContainer.add(tabPanel);
        centerArea.add(tabContainer, BorderLayout.NORTH);

        // Content Area Wrapper for Cards + Card
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 25));
        contentWrapper.setOpaque(false);

        // Stats Cards (Only for Profitability)
        statsPanel = createStatsPanel();
        contentWrapper.add(statsPanel, BorderLayout.NORTH);

        // Content Card (Shadowed)
        ShadowPanel cardShadow = new ShadowPanel();
        cardShadow.setLayout(new BorderLayout());

        tablePanel = new JPanel(new BorderLayout(0, 0));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        cardTitle = new JLabel("");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTitle.setForeground(UIStyle.TEXT_PRIMARY);
        cardTitle.setBorder(new javax.swing.border.EmptyBorder(16, 20, 12, 20));
        cardTitle.setVisible(false);
        tablePanel.add(cardTitle, BorderLayout.NORTH);

        cardShadow.add(tablePanel);

        // Master-Detail Layout
        masterDetail = new JPanel(new BorderLayout(25, 0));
        masterDetail.setOpaque(false);
        masterDetail.add(cardShadow, BorderLayout.CENTER);

        receiptPanel = new ReceiptPanel(() -> {
            reportTable.clearSelection();
            receiptPanel.setVisible(false);
        });
        receiptPanel.setVisible(false);
        masterDetail.add(receiptPanel, BorderLayout.EAST);

        contentWrapper.add(masterDetail, BorderLayout.CENTER);
        centerArea.add(contentWrapper, BorderLayout.CENTER);
        add(centerArea, BorderLayout.CENTER);

        updateView();
    }

    private void updateTabs() {
        tabPanel.removeAll();
        String[] tabs = { "Profitability", "Cashier Performance", "Transaction History" };
        for (String tab : tabs) {
            tabPanel.add(createTabButton(tab, tab.equals(activeTab)));
        }
        tabPanel.revalidate();
        tabPanel.repaint();
    }

    private void updateView() {
        boolean isProfitability = activeTab.equals("Profitability");
        statsPanel.setVisible(isProfitability);
        cardTitle.setVisible(!isProfitability);
        if (!isProfitability) {
            cardTitle.setText(activeTab);
        }
        if (receiptPanel != null) {
            receiptPanel.setVisible(false);
        }
        refreshTable();
        revalidate();
        repaint();
    }

    private JButton createTabButton(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.BOLD);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) {
            btn.setBackground(UIStyle.ACCENT_BLUE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setForeground(UIStyle.TEXT_SECONDARY);
        }

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected) {
                    g2.setColor(c.getBackground());
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 30, 30);
                }

                FontMetrics fm = g2.getFontMetrics();
                int x = (c.getWidth() - fm.stringWidth(btn.getText())) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(c.getForeground());
                g2.drawString(btn.getText(), x, y);
                g2.dispose();
            }
        });

        btn.addActionListener(e -> {
            activeTab = text;
            updateTabs();
            updateView();
        });

        return btn;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 140));

        panel.add(createMetricCard("Total Gross Profit", "₱7,300.00", "src/resources/images/grossprofit.png",
                UIStyle.SUCCESS, null));
        panel.add(createMetricCard("Average Margin", "15.9%", "src/resources/images/average_margin.png",
                UIStyle.TEXT_PRIMARY, null));
        panel.add(createMetricCard("Best Margin", "36.4%", "src/resources/images/best_margin.png", UIStyle.SUCCESS,
                "Full Service"));

        return panel;
    }

    private JPanel createMetricCard(String title, String value, String iconSource, Color valueColor, String subtitle) {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(15, 5));
        inner.setBackground(Color.WHITE);
        inner.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIStyle.SMALL);
        titleLabel.setForeground(UIStyle.TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(valueColor);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        if (subtitle != null) {
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            subLabel.setForeground(UIStyle.TEXT_SECONDARY);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(subLabel);
        }

        JLabel iconLabel = new JLabel();
        if (iconSource.endsWith(".png")) {
            try {
                String[] attempts = {
                        iconSource,
                        iconSource.startsWith("/src") ? iconSource.substring(4) : "/src" + iconSource,
                        iconSource.startsWith("/") ? iconSource.substring(1) : iconSource
                };

                java.net.URL imgUrl = null;
                for (String path : attempts) {
                    imgUrl = getClass().getResource(path);
                    if (imgUrl != null)
                        break;
                    String noSlash = path.startsWith("/") ? path.substring(1) : path;
                    imgUrl = getClass().getClassLoader().getResource(noSlash);
                    if (imgUrl != null)
                        break;
                }

                if (imgUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(imgUrl);
                    Image img = originalIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(new ImageIcon(img));
                } else {
                    iconLabel.setText("📈");
                }
            } catch (Exception e) {
                iconLabel.setText("📈");
            }
        } else {
            iconLabel.setText(iconSource);
            iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        }

        inner.add(textPanel, BorderLayout.CENTER);
        inner.add(iconLabel, BorderLayout.EAST);

        card.add(inner);
        return card;
    }

    private void refreshTable() {
        String[] columns;
        Object[][] data;

        if (activeTab.equals("Profitability")) {
            columns = new String[] { "RANK", "PRODUCT", "COST PRICE", "SELLING PRICE", "GROSS PROFIT", "MARGIN",
                    "STATUS" };
            data = new Object[][] {
                    { "#1", "Full Service", "₱60.00", "₱195.00", "₱135.00", "35.0%", "Excellent" },
                    { "#2", "Detergent (Powder)", "₱6.00", "₱18.00", "₱12.00", "20.0%", "Poor" }
            };
        } else if (activeTab.equals("Cashier Performance")) {
            columns = new String[] { "CASHIER", "TRANSACTIONS", "TOTAL SALES", "AVERAGE TRANSACTION", "PERFORMANCE" };
            data = new Object[][] {
                    { "Admin 1", "3", "₱12,000.00", "₱5,400.00", "Good" }
            };
        } else {
            columns = new String[] { "TRANSACTION ID", "DATE & TIME", "CASHIER", "ITEMS SOLD", "PAYMENT METHOD",
                    "TOTAL", "ACTIONS" };
            data = new Object[][] {
                    { "1", "09/02/26, 1:06:55 PM", "Admin 1", "Full Service", "GCash", "₱195.00", "Delete" },
                    { "2", "09/02/26, 1:06:55 PM", "Admin 1", "Full Service", "Cash", "₱195.00", "Delete" }
            };
        }

        tableModel.setDataVector(data, columns);

        // Re-apply renderers since columns changed
        tablePanel.removeAll();
        tablePanel.add(createReportTable(), BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private JScrollPane createReportTable() {
        reportTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Keep Transaction History delete column editable
                return activeTab.equals("Transaction History") && col == 6;
            }
        };

        // ── Table appearance ─────────────────────────────────────────────────
        reportTable.setRowHeight(52);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTable.setForeground(new Color(55, 65, 81));
        reportTable.setShowGrid(false);
        reportTable.setIntercellSpacing(new Dimension(0, 0));
        reportTable.setFillsViewportHeight(true);
        reportTable.setBackground(Color.WHITE);
        reportTable.setBorder(null);
        reportTable.setSelectionBackground(new Color(239, 246, 255));

        // ── Header ────────────────────────────────────────────────────────────
        JTableHeader th = reportTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setForeground(new Color(100, 116, 139));
        th.setBackground(new Color(248, 250, 252));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        th.setPreferredSize(new Dimension(0, 38));
        th.setReorderingAllowed(false);

        // Column 0 LEFT, all others CENTER
        DefaultTableCellRenderer hLeft = new DefaultTableCellRenderer();
        hLeft.setHorizontalAlignment(JLabel.LEFT);
        hLeft.setBackground(new Color(248, 250, 252));
        hLeft.setForeground(new Color(30, 41, 59));
        hLeft.setBorder(new javax.swing.border.EmptyBorder(0, 12, 0, 12));
        hLeft.setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer hCenter = new DefaultTableCellRenderer();
        hCenter.setHorizontalAlignment(JLabel.CENTER);
        hCenter.setBackground(new Color(248, 250, 252));
        hCenter.setForeground(new Color(30, 41, 59));
        hCenter.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        hCenter.setFont(new Font("Segoe UI", Font.BOLD, 11));

        th.getColumnModel().getColumn(0).setHeaderRenderer(hLeft);
        for (int i = 1; i < reportTable.getColumnCount(); i++) {
            th.getColumnModel().getColumn(i).setHeaderRenderer(hCenter);
        }

        // ── Default cell renderer — alternating rows + LEFT padding + CENTER ─
        DefaultTableCellRenderer cellLeft = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(col == 0 ? JLabel.LEFT : JLabel.CENTER);
                setBorder(new javax.swing.border.EmptyBorder(0, col == 0 ? 12 : 0, 0, 0));
                setBackground(rowBg(t, sel, row));
                setForeground(new Color(55, 65, 81));
                setOpaque(true);
                return this;
            }
        };

        int cols = reportTable.getColumnCount();
        // Apply default renderer first, then override last col if needed
        for (int i = 0; i < cols; i++) {
            reportTable.getColumnModel().getColumn(i).setCellRenderer(cellLeft);
        }

        // ── Special column renderers ──────────────────────────────────────────
        if (activeTab.equals("Profitability")) {
            reportTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        } else if (activeTab.equals("Cashier Performance")) {
            reportTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        } else if (activeTab.equals("Transaction History")) {
            reportTable.getColumnModel().getColumn(6).setCellRenderer(new TableButtonRenderer());
            reportTable.getColumnModel().getColumn(6).setCellEditor(new TableButtonEditor(new JCheckBox()));
        }

        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.getVerticalScrollBar().setUI(new Scrollbar());
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(new Color(248, 250, 252));
        scroll.setBackground(Color.WHITE);

        // Selection Listener for Transaction History
        if (activeTab.equals("Transaction History")) {
            reportTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = reportTable.getSelectedRow();
                    if (row != -1) {
                        showTransactionDetail(row);
                    }
                }
            });
        }

        return scroll;
    }

    private void showTransactionDetail(int row) {
        String id = tableModel.getValueAt(row, 0).toString();
        String date = tableModel.getValueAt(row, 1).toString();
        String cashier = tableModel.getValueAt(row, 2).toString();
        String items = tableModel.getValueAt(row, 3).toString();
        String method = tableModel.getValueAt(row, 4).toString();
        String total = tableModel.getValueAt(row, 5).toString();

        // Sample data for details (Mocked for now)
        String customer = (id.equals("1") || id.equals("1001")) ? "Jefferson" : "Cash Customer";
        String[] services = items.split(", ");

        receiptPanel.setData(id, customer, cashier, date.split(",")[0], services, total, method);
        receiptPanel.setVisible(true);
        revalidate();
        repaint();
    }

    /** Helper for alternating row background */
    private Color rowBg(JTable t, boolean selected, int row) {
        if (selected)
            return new Color(239, 246, 255);
        return row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252);
    }

    // Status Badge Renderer — pill-painted JLabel, centred, alternating rows
    class StatusCellRenderer extends DefaultTableCellRenderer {

        StatusCellRenderer() {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            final Color bg;
            final Color fg = Color.WHITE;
            String txt = value == null ? "" : value.toString();

            if (txt.equals("Excellent") || txt.equals("Good")) {
                bg = new Color(34, 197, 94); // green
            } else {
                bg = new Color(239, 68, 68); // red
            }

            // Wrapper so we can vertically centre + set row background
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(true);
            wrapper.setBackground(rowBg(table, isSelected, row));

            JLabel pill = new JLabel(txt, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pill.setOpaque(false);
            pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pill.setForeground(fg);
            pill.setBackground(bg);
            pill.setBorder(new javax.swing.border.EmptyBorder(4, 14, 4, 14));

            int vGap = Math.max(0, (table.getRowHeight(row) - pill.getPreferredSize().height) / 2);
            ((FlowLayout) wrapper.getLayout()).setVgap(vGap);

            wrapper.add(pill);
            return wrapper;
        }
    }

    class TableButtonRenderer extends JButton implements TableCellRenderer {
        public TableButtonRenderer() {
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(null);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value.toString());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = 10;
            int h = 28;
            int y = (getHeight() - h) / 2;
            g2.setColor(UIStyle.DANGER);
            g2.fillRoundRect(pad, y, getWidth() - pad * 2, h, h, h);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, ty);
            g2.dispose();
        }
    }

    class TableButtonEditor extends DefaultCellEditor {
        private String label;
        private JButton button;

        public TableButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorder(null);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            // Implement delete logic here if needed
            int row = reportTable.getSelectedRow();
            if (row != -1) {
                int confirm = JOptionPane.showConfirmDialog(LaundryReportsSystem.this,
                        "Are you sure you want to delete this transaction?", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    ((DefaultTableModel) reportTable.getModel()).removeRow(row);
                }
            }
            return super.stopCellEditing();
        }
    }
}
