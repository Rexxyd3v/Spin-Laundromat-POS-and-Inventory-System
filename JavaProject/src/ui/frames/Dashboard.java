package src.ui.frames;

import java.awt.*;
import javax.swing.*;
import src.model.Staff;
import src.util.components.BrandIcons;
import src.util.components.SidebarButton;
import src.ui.panels.dashboardPanel.*;
import src.ui.panels.inventoryPanel.LaundryInventorySystem;
import src.ui.panels.posPanel.LaundryPOS;
import src.ui.panels.reportPanel.LaundryReportsSystem;
import src.ui.panels.settingsPanel.SettingsPanel;
import src.ui.panels.userPanel.UserManagementPanel;
import src.util.config.*;

public class Dashboard extends JFrame {
    private final Staff staff;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private SidebarButton currentActiveBtn;

    public Dashboard() {
        // Mock staff (Correct signature from Staff.java)
        this.staff = new Staff("0", "Admin", "password123");

        setTitle("Spin Laundromat Service - POS & Inventory System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());

        // Set Window Icon
        try {
            java.net.URL iconUrl = getClass().getResource("/src/resources/images/desktoplogo.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
        }

        initSidebar();
        initMainContent();
    }

    public void display() {
        setVisible(true);
    }

    private void initSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(241, 245, 249)));

        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(Color.WHITE);
        brandPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 20));
        brandPanel.setMaximumSize(new Dimension(240, 90));
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconContainer = new JPanel() {
            private Image brandImg;
            {
                try {
                    java.net.URL imgUrl = getClass().getResource("/src/resources/images/machine_wash.png");
                    if (imgUrl != null) {
                        brandImg = new ImageIcon(imgUrl).getImage();
                    }
                } catch (Exception e) {
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIStyle.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                if (brandImg != null) {
                    int imgW = 30;
                    int imgH = 30;
                    int x = (getWidth() - imgW) / 2;
                    int y = (getHeight() - imgH) / 2;
                    g2.drawImage(brandImg, x, y, imgW, imgH, null);
                }
                g2.dispose();
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(45, 45));
        iconContainer.setMaximumSize(new Dimension(45, 45));

        JPanel brandTextPanel = new JPanel();
        brandTextPanel.setBackground(Color.WHITE);
        brandTextPanel.setLayout(new BoxLayout(brandTextPanel, BoxLayout.Y_AXIS));

        JLabel brandName = new JLabel("Machine Wash");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 12));
        brandName.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel brandSubtitleLabel = new JLabel("POS & Inventory");
        brandSubtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        brandSubtitleLabel.setForeground(UIStyle.TEXT_SECONDARY);

        brandTextPanel.add(brandName);
        brandTextPanel.add(brandSubtitleLabel);
        brandPanel.add(iconContainer);
        brandPanel.add(brandTextPanel);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(10));

        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(249, 250, 251));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        userPanel.setMaximumSize(new Dimension(240, 100));
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userName = new JLabel(staff.getUsername());
        userName.setFont(new Font("SansSerif", Font.BOLD, 16));
        userName.setForeground(UIStyle.TEXT_PRIMARY);

        JLabel userRole = new JLabel("Admin");
        userRole.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userRole.setForeground(UIStyle.TEXT_SECONDARY);

        userPanel.add(userName);
        userPanel.add(Box.createVerticalStrut(5));
        userPanel.add(userRole);

        sidebar.add(userPanel);
        sidebar.add(Box.createVerticalStrut(20));

        Object[][] menus = {
                { "Dashboard", BrandIcons.DASHBOARD },
                { "Point of Sale", BrandIcons.POS },
                { "Inventory", BrandIcons.INVENTORY },
                { "Reports", BrandIcons.REPORTS },
                { "Users", BrandIcons.USERS },
                { "Settings", BrandIcons.SETTINGS }
        };

        for (Object[] menu : menus) {
            String title = (String) menu[0];
            SidebarButton.IconPainter painter = (SidebarButton.IconPainter) menu[1];
            SidebarButton btn = new SidebarButton(title, painter);
            btn.setMaximumSize(new Dimension(240, 45));
            btn.addActionListener(e -> {
                if (currentActiveBtn != null)
                    currentActiveBtn.setActive(false);
                btn.setActive(true);
                currentActiveBtn = btn;
                cardLayout.show(contentPanel, title);
            });
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(5));

            if (title.equals("Dashboard")) {
                btn.setActive(true);
                currentActiveBtn = btn;
            }
        }

        sidebar.add(Box.createVerticalGlue());

        SidebarButton logoutBtn = new SidebarButton("Logout", BrandIcons.LOGOUT);
        logoutBtn.setMaximumSize(new Dimension(240, 45));
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new MasterKey().display();
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(20));

        add(sidebar, BorderLayout.WEST);
    }

    private void initMainContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIStyle.BACKGROUND);

        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(new LaundryPOS(), "Point of Sale");
        contentPanel.add(new LaundryInventorySystem(), "Inventory");
        contentPanel.add(new LaundryReportsSystem(), "Reports");
        contentPanel.add(new UserManagementPanel(), "Users");
        contentPanel.add(new SettingsPanel(), "Settings");

        add(contentPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().display());
    }
}
