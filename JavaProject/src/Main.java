package src;

import src.ui.frames.MasterKey;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MasterKey().display();
        });
    }
}
