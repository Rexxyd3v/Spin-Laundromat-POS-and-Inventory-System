package src;

import src.ui.frames.MasterKeyPreview;
import javax.swing.SwingUtilities;
import src.ui.panels.posPanel.LaundryPOS;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MasterKeyPreview().display();
           // new LaundryPOS();
        });
    }
}
