import ui.frames.MasterKey;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            util.config.UIStyle.installDefaults();
            new MasterKey().display();
        });
    }
}
