package model;

import java.io.IOException;
import java.util.*;
import util.config.*;

/**
 * A displayed workspace identity, separate from authentication and permissions.
 */
public final class ActiveProfile {
    public static TeamStore.Member current() {
        try {
            List<TeamStore.Member> active = new TeamStore().read().stream().filter(TeamStore.Member::active).toList();
            String id = AtomicProperties.read(AtomicProperties.dataPath("profile.properties")).getProperty("active.id",
                    "");
            return active.stream().filter(m -> m.id().equals(id)).findFirst()
                    .orElseGet(() -> active.stream().filter(m -> m.role().equals("Owner")).findFirst()
                            .orElseGet(() -> active.isEmpty() ? defaultAdmin() : active.get(0)));
        } catch (IOException ex) {
            return defaultAdmin();
        }
    }

    public static TeamStore.Member defaultAdmin() {
        return new TeamStore.Member("local-admin", "Administrator", "", "Owner", true, "");
    }

    public static void select(String id) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            if (new TeamStore().read().stream().noneMatch(m -> m.id().equals(id) && m.active()))
                throw new IOException("Choose an active team profile.");
            Properties p = new Properties();
            p.setProperty("active.id", id);
            AtomicProperties.write(AtomicProperties.dataPath("profile.properties"), p);
            AppEvents.fire("profile");

        }
    }

    private ActiveProfile() {
    }
}
