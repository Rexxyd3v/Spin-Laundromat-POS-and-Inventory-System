package ui.frames;

import javax.swing.SwingUtilities;

public class MasterKey extends LoginWindow {
    public MasterKey() {
        super(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MasterKey().display());
    }
}
