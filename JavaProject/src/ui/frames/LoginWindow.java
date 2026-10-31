package ui.frames;

import java.awt.*;
import javax.swing.*;
import util.components.*;
import util.config.*;

/**
 * One native window for unlock, staff sign-in, workspace, and returning to
 * login.
 */
public class LoginWindow extends JFrame {
    private final TransitionPane screens;

    protected LoginWindow(boolean startWithStaff) {
        UIStyle.installDefaults();
        setTitle(persistence.Repositories.settings().get().storeName() + " · Welcome");
        setIconImages(BrandMark.windowIcons());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        screens = new TransitionPane(welcome(startWithStaff));
        setContentPane(screens);
        setMinimumSize(new Dimension(940, 680));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
    }

    private WelcomePanel welcome(boolean staff) {
        return new WelcomePanel(staff, () -> {
            if (staff)
                showWorkspace();
            else
                showWelcome(true);
        });
    }

    private void showWelcome(boolean staff) {
        screens.transitionTo(() -> {
            setTitle(persistence.Repositories.settings().get().storeName() + " · Welcome");
            return welcome(staff);
        });
    }

    private void showWorkspace() {
        screens.transitionTo(() -> {
            setTitle(persistence.Repositories.settings().get().storeName() + " · Laundry workspace");
            return new WorkspacePanel(() -> showWelcome(false));
        });
    }

    public void display() {
        setVisible(true);
    }
}
