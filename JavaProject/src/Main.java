package src;

import src.ui.frames.MasterKeyPreview;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MasterKeyPreview().display();
        });
    }
}
