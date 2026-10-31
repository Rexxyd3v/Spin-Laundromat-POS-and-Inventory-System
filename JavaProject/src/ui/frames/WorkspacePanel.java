package ui.frames;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.panels.BusinessPages;
import ui.panels.AnalyticsDashboard;
import model.*;
import ui.panels.posPanel.*;
import ui.panels.settingsPanel.SettingsPanel;
import util.components.*;
import util.config.*;
import static util.components.ModernUI.*;

/**
 * The same complete workspace can be rendered and tested without a native
 * window.
 */
public final class WorkspacePanel extends JPanel {
    private final JPanel sidebar = new JPanel(new BorderLayout(0, 24)), content = new JPanel(new CardLayout());
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final List<SidebarButton> buttons = new ArrayList<>();
    private final JLabel brand = label("", UIStyle.BOLD, Color.WHITE),
            tagline = label("LAUNDRY WORKSPACE", new Font("Segoe UI", Font.PLAIN, 9), new Color(148, 163, 184));
    private final JLabel breadcrumb = label("Workspace / Point of sale", UIStyle.SMALL, UIStyle.TEXT_SECONDARY);
    private final JPanel brandText = column();
    private SidebarButton active;
    private boolean compact;
    private Boolean manualCompact;
    private String current = "Overview";
    private final JButton profileButton = new ProfileButton();
    private final JLabel topProfile = label("", UIStyle.SMALL, UIStyle.TEXT_PRIMARY);
    private final JLabel topAvatar = new JLabel();
    private final PropertyChangeListener dataListener = e -> {
        String destination = switch (e.getPropertyName()) {
        case "navigate-pos" -> "Point of sale";
        case "navigate-reports" -> "Reports";
        case "navigate-catalog" -> "Catalog";
        case "navigate-inventory" -> "Inventory";
        case "navigate-team" -> "Team";
        case "navigate-operations" -> "Operations";
        default -> null;
        };
        if (destination != null) {
            showPage(destination);
            return;
        }
        if (e.getPropertyName().equals("profile") || e.getPropertyName().equals("team"))
            updateProfile();
        refreshPages();
    };
    private final PropertyChangeListener settingsListener = e -> {
        brand.setText(persistence.Repositories.settings().get().storeName());
        repaint();
    };

    private final Runnable exitAction;

    public WorkspacePanel() {
        this(null);
    }

    public WorkspacePanel(Runnable onExit) {
        super(new BorderLayout());
        exitAction = onExit;
        UIStyle.installDefaults();
        setBackground(UIStyle.BACKGROUND);
        pages.put("Overview", new AnalyticsDashboard());
        pages.put("Point of sale", new LaundryPOS());
        pages.put("Catalog", new CatalogPage());
        pages.put("Inventory", new BusinessPages.Inventory());
        pages.put("Operations", new ui.panels.OperationsPage());
        pages.put("Reports", new BusinessPages.Reports());
        pages.put("Team", new BusinessPages.Team());
        pages.put("Settings", new SettingsPanel());
        for (var page : pages.entrySet())
            content.add(page.getValue(), page.getKey());
        content.setOpaque(false);
        buildSidebar();
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIStyle.BACKGROUND);
        main.add(topbar(), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
        showPage("Overview");
        updateProfile();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        AppEvents.add(dataListener);
        persistence.Repositories.settings().addListener(settingsListener);
    }

    @Override
    public void removeNotify() {
        AppEvents.remove(dataListener);
        persistence.Repositories.settings().removeListener(settingsListener);
        super.removeNotify();
    }

    private void refreshPages() {
        JComponent page = pages.get(current);
        if (page instanceof BusinessPages.Refreshable live)
            live.refresh();
        if (page instanceof CatalogPage catalog)
            catalog.refresh();
        repaint();
    }

    public JComponent page(String name) {
        return pages.get(name);
    }

    public void showPage(String name) {
        if (!pages.containsKey(name))
            return;
        current = name;
        ((CardLayout) content.getLayout()).show(content, name);
        breadcrumb.setText("Workspace / " + name);
        for (SidebarButton b : buttons)
            b.setActive(b.getText().equals(name));
        JComponent page = pages.get(name);
        if (page instanceof BusinessPages.Refreshable live)
            live.refresh();
        if (page instanceof CatalogPage catalog)
            catalog.refresh();
    }

    private JPanel topbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.INPUT_BORDER), new EmptyBorder(12, 28, 12, 28)));
        JPanel left = row(12);
        JButton toggle = iconButton("Expand or collapse navigation", AppIcon.Name.LIST);
        toggle.addActionListener(e -> {
            manualCompact = !compact;
            setCompact(manualCompact);
            revalidate();
        });
        left.add(toggle);
        left.add(breadcrumb);
        bar.add(left, BorderLayout.WEST);
        JPanel right = row(14);
        right.add(label(LocalDate.now(ZoneId.of("Asia/Manila")).format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                UIStyle.SMALL, UIStyle.TEXT_SECONDARY));
        right.add(new StockNotificationButton());
        right.add(topAvatar);
        right.add(topProfile);
        topProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topProfile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openProfiles();
            }
        });
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private void buildSidebar() {
        sidebar.setBackground(UIStyle.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(28, 0, 20, 0));
        sidebar.setPreferredSize(new Dimension(224, 0));
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 20, 0, 16));
        JLabel logo = new JLabel(new BrandMark(40));
        header.add(logo, BorderLayout.WEST);
        brandText.setOpaque(false);
        brand.setText(persistence.Repositories.settings().get().storeName());
        brand.setToolTipText(persistence.Repositories.settings().get().storeName());
        brandText.add(brand);
        brandText.add(Box.createVerticalStrut(5));
        brandText.add(tagline);
        header.add(brandText, BorderLayout.CENTER);
        sidebar.add(header, BorderLayout.NORTH);
        JPanel navHolder = new JPanel(new BorderLayout());
        navHolder.setOpaque(false);
        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 6));
        nav.setOpaque(false);
        String[] names = pages.keySet().toArray(String[]::new);
        AppIcon.Name[] icons = { AppIcon.Name.DASHBOARD, AppIcon.Name.POS, AppIcon.Name.BOX, AppIcon.Name.LIST,
                AppIcon.Name.REFRESH, AppIcon.Name.CHART, AppIcon.Name.USERS, AppIcon.Name.SETTINGS };
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            AppIcon.Name icon = icons[i];
            SidebarButton b = new SidebarButton(name,
                    (g, x, y, size, color) -> AppIcon.draw(icon, g, x, y, size, color));
            b.setPreferredSize(new Dimension(224, 48));
            b.addActionListener(e -> showPage(name));
            buttons.add(b);
            nav.add(b);
            if (i == 0)
                b.setActive(true);
        }
        JPanel navStack = new JPanel(new BorderLayout());
        navStack.setOpaque(false);
        navStack.add(nav, BorderLayout.NORTH);
        JScrollPane navigationScroll = scroll(navStack);
        navigationScroll.setName("sidebar-scroll");
        navigationScroll.getVerticalScrollBar()
                .setUI(new util.components.Scrollbar(new Color(65, 81, 105), new Color(100, 116, 139)));
        navigationScroll.getVerticalScrollBar().setOpaque(false);
        navHolder.add(navigationScroll, BorderLayout.CENTER);
        sidebar.add(navHolder, BorderLayout.CENTER);
        SidebarButton exit = new SidebarButton("Exit workspace",
                (g, x, y, size, color) -> AppIcon.draw(AppIcon.Name.LOGOUT, g, x, y, size, color));
        exit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit this workspace? An unfinished cart will be discarded.",
                    "Exit workspace", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (exitAction != null) {
                    exitAction.run();
                    return;
                }
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null)
                    w.dispose();
                new MasterKey().display();
            }
        });
        buttons.add(exit);
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        profileButton.setFont(UIStyle.BOLD);
        profileButton.setForeground(Color.WHITE);
        profileButton.setBackground(new Color(31, 46, 64));
        profileButton.setOpaque(false);
        profileButton.setBorder(new EmptyBorder(14, 14, 14, 10));
        profileButton.setFocusPainted(false);
        profileButton.setHorizontalAlignment(SwingConstants.LEFT);
        profileButton.setIconTextGap(10);
        profileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileButton.addActionListener(e -> openProfiles());
        JPanel profileWrap = new JPanel(new BorderLayout());
        profileWrap.setOpaque(false);
        profileWrap.setBorder(new EmptyBorder(0, 10, 0, 10));
        profileWrap.add(profileButton);
        footer.add(profileWrap, BorderLayout.NORTH);
        footer.add(exit, BorderLayout.SOUTH);
        sidebar.add(footer, BorderLayout.SOUTH);
    }

    private void setCompact(boolean value) {
        if (compact == value)
            return;
        compact = value;
        sidebar.setPreferredSize(new Dimension(value ? 76 : 224, 0));
        brandText.setVisible(!value);
        buttons.forEach(b -> b.setCompact(value));
        updateProfile();
    }

    public void updateProfile() {
        TeamStore.Member person = ActiveProfile.current();
        Avatar sidebarAvatar = new Avatar(person, 36);
        profileButton.setIcon(sidebarAvatar);
        profileButton.setHorizontalAlignment(compact ? SwingConstants.CENTER : SwingConstants.LEFT);
        profileButton.setText(compact ? ""
                : "<html><b>" + html(shortName(person.name(), 23)) + "</b><br><font color='#B7C8D9'>"
                        + html(person.role()) + "</font></html>");
        profileButton.setToolTipText(person.name() + " · " + person.role() + " · Switch or edit profile");
        profileButton.getAccessibleContext().setAccessibleName("Workspace profile: " + person.name());
        topAvatar.setIcon(new Avatar(person, 30));
        topProfile.setText(shortName(person.name(), 24));
        topProfile.setToolTipText(person.name() + " · " + person.role());
        revalidate();
        repaint();
    }

    private static String shortName(String text, int max) {
        return text.length() > max ? text.substring(0, max - 1) + "…" : text;
    }

    private static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void openProfiles() {
        new ProfileChooser(SwingUtilities.getWindowAncestor(this), this::updateProfile).setVisible(true);
    }

    @Override
    public void doLayout() {
        setCompact(manualCompact != null ? manualCompact : getWidth() < 1180);
        super.doLayout();
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintChildren(g);
        g.dispose();
    }
}
