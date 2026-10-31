package model;

import java.io.IOException;
import security.LocalAuth;
import util.config.AppEvents;

/**
 * Display identity always follows the authenticated account; profile.properties
 * cannot grant access.
 */
public final class ActiveProfile {
    public static TeamStore.Member current() {
        try {
            var account = LocalAuth.current();
            return new TeamStore().read().stream().filter(m -> m.id().equals(account.profileId())).findFirst()
                    .orElse(new TeamStore.Member(account.profileId(), account.name(), "", account.role(), true, ""));
        } catch (SecurityException ex) {
            return defaultAdmin();
        } catch (IOException ex) {
            try {
                var a = LocalAuth.current();
                return new TeamStore.Member(a.profileId(), a.name(), "", a.role(), true, "");
            } catch (SecurityException denied) {
                return defaultAdmin();
            }
        }
    }

    /** Safe signed-out display only. Never an Owner fallback. */
    public static TeamStore.Member defaultAdmin() {
        return new TeamStore.Member("guest", "Not signed in", "", "Cashier", false, "");
    }

    public static void select(String id) throws IOException {
        var account = LocalAuth.current();
        if (!account.profileId().equals(id))
            throw new IOException("Sign out and sign in to use another account.");
        AppEvents.fire("profile");
    }

    private ActiveProfile() {
    }
}
