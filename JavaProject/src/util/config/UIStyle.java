package util.config;

import java.awt.*;
import javax.swing.*;

public final class UIStyle {
    public static final Color YELLOW = Color.decode("#F4C542");
    public static Color PRIMARY = accent(PosSettings.get().accent());
    public static final Color BACKGROUND = Color.decode("#F5F7FB"), SIDEBAR_BG = Color.decode("#111827"),
            SIDEBAR_DIVIDER = Color.decode("#263244"), CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = Color.decode("#111827"), TEXT_SECONDARY = Color.decode("#64748B"),
            SUCCESS = Color.decode("#15803D"), WARNING = Color.decode("#B45309"), DANGER = Color.decode("#B91C1C"),
            WHITE = Color.WHITE;
    public static final Color INPUT_BORDER = Color.decode("#E2E8F0");
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 28),
            SUBTITLE = new Font("Segoe UI", Font.BOLD, 18), REGULAR = new Font("Segoe UI", Font.PLAIN, 14),
            SMALL = new Font("Segoe UI", Font.PLAIN, 12), BOLD = new Font("Segoe UI", Font.BOLD, 14),
            BRAND = new Font("Segoe UI", Font.BOLD, 20);
    static {
        PosSettings.addListener(event -> {
            Color old = PRIMARY;
            PRIMARY = accent(PosSettings.get().accent());
            installDefaults();
            for (Window w : Window.getWindows()) {
                recolor(w, old);
                w.repaint();
            }
        });
    }
    private static boolean installed;

    private UIStyle() {
    }

    public static Color accent(String name) {
        return Color.decode(switch (name) {
        case "Blue" -> "#1D4ED8";
        case "Violet" -> "#6D28D9";
        default -> "#0F766E";
        });
    }

    public static Color tint() {
        return new Color((PRIMARY.getRed() + 255 * 9) / 10, (PRIMARY.getGreen() + 255 * 9) / 10,
                (PRIMARY.getBlue() + 255 * 9) / 10);
    }

    public static void installDefaults() {
        if (!installed) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
            }
            installed = true;
        }
        UIManager.put("defaultFont", REGULAR);
        UIManager.put("control", CARD_BG);
        UIManager.put("nimbusBase", PRIMARY);
        UIManager.put("nimbusFocus", PRIMARY);
        UIManager.put("nimbusSelectionBackground", PRIMARY);
        UIManager.put("nimbusBlueGrey", INPUT_BORDER);
        UIManager.put("Separator.foreground", INPUT_BORDER);
        for (String key : new String[] { "Label.font", "Button.font", "TextField.font", "TextArea.font",
                "ComboBox.font", "CheckBox.font", "Table.font", "TabbedPane.font", "OptionPane.messageFont",
                "OptionPane.buttonFont", "Spinner.font" })
            UIManager.put(key, REGULAR);
        UIManager.put("Panel.background", CARD_BG);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.selectionBackground", PRIMARY);
        UIManager.put("TextField.selectionForeground", WHITE);
        UIManager.put("Table.selectionBackground", tint());
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.errorIcon",
                new util.components.AppIcon(util.components.AppIcon.Name.WARNING, 28, DANGER));
        UIManager.put("OptionPane.warningIcon",
                new util.components.AppIcon(util.components.AppIcon.Name.WARNING, 28, WARNING));
        UIManager.put("OptionPane.informationIcon",
                new util.components.AppIcon(util.components.AppIcon.Name.CHECK, 28, PRIMARY));
        UIManager.put("OptionPane.questionIcon",
                new util.components.AppIcon(util.components.AppIcon.Name.BELL, 28, PRIMARY));
    }

    private static void recolor(Component c, Color previous) {
        if (previous.equals(c.getForeground()))
            c.setForeground(PRIMARY);
        if (previous.equals(c.getBackground()))
            c.setBackground(PRIMARY);
        if (c instanceof Container container)
            for (Component child : container.getComponents())
                recolor(child, previous);
    }
}
