package ui.panels.posPanel;

import java.awt.*;
import javax.swing.*;
import model.CatalogStore;

public final class CatalogManager extends JDialog {
    public CatalogManager(Window owner, persistence.RepositoryProvider.Catalog store, Runnable changed) {
        super(owner, "Services & products", ModalityType.APPLICATION_MODAL);
        setContentPane(new CatalogPage());
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 580));
        setLocationRelativeTo(owner);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                changed.run();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
