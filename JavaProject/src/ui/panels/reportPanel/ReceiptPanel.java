package src.ui.panels.reportPanel;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import src.util.components.ShadowPanel;
import src.util.config.UIStyle;
import src.util.components.Scrollbar;

public class ReceiptPanel extends JPanel {

    private JLabel transIdLabel;
    private JLabel customerLabel;
    private JLabel processedByLabel;
    private JLabel dateLabel;
    private JPanel servicesContainer;
    private JLabel totalLabel;
    private JLabel paymentSubtext;
    private Runnable onClose;

    public ReceiptPanel(Runnable onClose) {
        this.onClose = onClose;
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(340, 0));
        setMinimumSize(new Dimension(340, 0));
        initComponents();
    }

    private void initComponents() {
        ShadowPanel card = new ShadowPanel();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(Color.WHITE);

        // Header (Always Visible)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("Receipt details");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(30, 58, 138));
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? UIStyle.DANGER : new Color(156, 163, 175));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        closeBtn.setPreferredSize(new Dimension(24, 24));
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> onClose.run());
        header.add(closeBtn, BorderLayout.EAST);

        inner.add(header, BorderLayout.NORTH);

        // Scrollable Content
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);
        scrollContent.setBorder(new EmptyBorder(0, 25, 0, 25));

        // Branding
        JPanel branding = new JPanel();
        branding.setLayout(new GridBagLayout());
        branding.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 12, 0);

        JPanel logoSquare = new JPanel() {
            private Image logoImg;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/src/resources/images/machine_wash.png");
                    if (imgUrl != null) {
                        logoImg = new ImageIcon(imgUrl).getImage();
                    }
                } catch (Exception e) {
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Blue background box
                g2.setColor(new Color(14, 165, 233));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                if (logoImg != null) {
                    int pad = 8;
                    g2.drawImage(logoImg, pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad, null);
                }
                g2.dispose();
            }
        };
        logoSquare.setPreferredSize(new Dimension(50, 50));
        logoSquare.setMinimumSize(new Dimension(50, 50));
        gbc.gridy = 0;
        branding.add(logoSquare, gbc);

        JLabel storeName = new JLabel("Spin Laundromat Service");
        storeName.setFont(new Font("SansSerif", Font.BOLD, 18));
        storeName.setForeground(new Color(31, 41, 55));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        branding.add(storeName, gbc);

        transIdLabel = new JLabel("#1001");
        transIdLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        transIdLabel.setForeground(new Color(107, 114, 128));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        branding.add(transIdLabel, gbc);

        branding.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollContent.add(branding);

        // Details
        scrollContent.add(createDetailRow("CUSTOMER", customerLabel = new JLabel("Jefferson")));
        scrollContent.add(Box.createVerticalStrut(12));
        scrollContent.add(createDetailRow("PROCESSED BY", processedByLabel = new JLabel("Admin 1")));
        scrollContent.add(Box.createVerticalStrut(12));
        scrollContent.add(createDetailRow("DATE", dateLabel = new JLabel("09/02/26")));
        scrollContent.add(Box.createVerticalStrut(12));
        scrollContent.add(createDetailRow("SERVICES", new JLabel("")));

        servicesContainer = new JPanel();
        servicesContainer.setLayout(new BoxLayout(servicesContainer, BoxLayout.Y_AXIS));
        servicesContainer.setOpaque(false);
        servicesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(servicesContainer);

        scrollContent.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new Scrollbar());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        inner.add(scrollPane, BorderLayout.CENTER);

        // Footer (Always Visible)
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 25, 25, 25));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(229, 231, 235));
        footer.add(sep);
        footer.add(Box.createVerticalStrut(15));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalText = new JLabel("TOTAL");
        totalText.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalText.setForeground(new Color(30, 58, 138));
        totalRow.add(totalText, BorderLayout.WEST);

        totalLabel = new JLabel("₱195.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setForeground(new Color(14, 165, 233));
        totalRow.add(totalLabel, BorderLayout.EAST);
        footer.add(totalRow);

        paymentSubtext = new JLabel("Paid via GCash");
        paymentSubtext.setFont(new Font("SansSerif", Font.ITALIC, 11));
        paymentSubtext.setForeground(new Color(14, 165, 233));
        JPanel pSubWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pSubWrapper.setOpaque(false);
        pSubWrapper.add(paymentSubtext);
        footer.add(pSubWrapper);

        footer.add(Box.createVerticalStrut(25));

        JButton printBtn = new JButton("Print") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 165, 233));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = "🖨️ " + getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        printBtn.setPreferredSize(new Dimension(200, 48));
        printBtn.setMaximumSize(new Dimension(200, 48));
        printBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        printBtn.setForeground(Color.WHITE);
        printBtn.setContentAreaFilled(false);
        printBtn.setBorderPainted(false);
        printBtn.setFocusPainted(false);
        printBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pBtnWrapper.setOpaque(false);
        pBtnWrapper.add(printBtn);
        footer.add(pBtnWrapper);

        inner.add(footer, BorderLayout.SOUTH);

        card.add(inner);
        add(card);
    }

    private JPanel createDetailRow(String key, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        keyLabel.setForeground(new Color(59, 130, 246));
        row.add(keyLabel, BorderLayout.WEST);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueLabel.setForeground(new Color(55, 65, 81));
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    public void setData(String id, String customer, String processedBy, String date, String[] services, String total,
            String method) {
        transIdLabel.setText("#" + id);
        customerLabel.setText(customer);
        processedByLabel.setText(processedBy);
        dateLabel.setText(date);
        totalLabel.setText(total);
        paymentSubtext.setText("Paid via " + method);

        servicesContainer.removeAll();
        for (String s : services) {
            JLabel sLabel = new JLabel(s);
            sLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            sLabel.setForeground(new Color(30, 58, 138));
            JPanel sRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
            sRow.setOpaque(false);
            sRow.add(sLabel);
            servicesContainer.add(sRow);
        }
        revalidate();
        repaint();
    }
}
