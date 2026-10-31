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
    private boolean workspaceOpen;
    private final Timer sessionWatch = new Timer(2000, e -> {
        if (workspaceOpen && !security.LocalAuth.isSignedIn())
            logout();
    });

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
        sessionWatch.start();
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

    private void logout() {
        workspaceOpen = false;
        for (Window child : getOwnedWindows())
            child.dispose();
        security.LocalAuth.signOut();
        showWelcome(false);
    }

    private void showWorkspace() {
        security.LocalAuth.current();
        workspaceOpen = true;
        screens.transitionTo(() -> {
            setTitle(persistence.Repositories.settings().get().storeName() + " · Laundry workspace");
            return new WorkspacePanel(this::logout);
        });
    }

    @Override
    public void dispose() {
        sessionWatch.stop();
        super.dispose();
    }

    public void display() {
        if (!security.LocalAuth.configured() && !AuthDialogs.setup(this)) {
            dispose();
            return;
        }
        setVisible(true);
    }
}
