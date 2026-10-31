package model;

import java.io.IOException;
import java.util.*;
import util.config.*;

public final class TeamStore implements persistence.RepositoryProvider.Team {
    public record Member(String id, String name, String contact, String role, boolean active, String image) {
        public Member(String id, String name, String contact, String role, boolean active) {
            this(id, name, contact, role, active, "");
        }

        public Member {
            image = image == null ? "" : image;
            contact = contact == null ? "" : contact;
            if (id == null || id.isBlank() || id.length() > 128)
                throw new IllegalArgumentException("A staff profile needs a valid identifier.");
            if (name == null || name.isBlank() || name.length() > 80)
                throw new IllegalArgumentException("Enter a name, up to 80 characters.");
            if (!Set.of("Owner", "Manager", "Cashier").contains(role))
                throw new IllegalArgumentException("Choose a role.");
        }
    }

    public java.util.List<Member> read() throws IOException {
        Properties p = AtomicProperties.read(AtomicProperties.dataPath("team.properties"));
        try {
            int count = Integer.parseInt(p.getProperty("count", "0"));
            if (count < 0 || count > 10000)
                throw new IllegalArgumentException();
            java.util.List<Member> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String k = "member." + i + ".";
                result.add(new Member(p.getProperty(k + "id"), p.getProperty(k + "name"),
                        p.getProperty(k + "contact", ""), p.getProperty(k + "role"),
                        Boolean.parseBoolean(p.getProperty(k + "active", "true")), p.getProperty(k + "image", "")));
            }
            for (var a : security.LocalAuth.profiles()) {
                int index = -1;
                for (int j = 0; j < result.size(); j++)
                    if (result.get(j).id().equals(a.profileId()))
                        index = j;
                Member before = index < 0 ? new Member(a.profileId(), a.name(), "", a.role(), a.active(), "")
                        : result.get(index);
                Member linked = new Member(before.id(), before.name(), before.contact(), a.role(), a.active(),
                        before.image());
                if (index < 0)
                    result.add(linked);
                else
                    result.set(index, linked);
            }
            return java.util.List.copyOf(result);
        } catch (RuntimeException ex) {
            throw new IOException("Staff profiles could not be loaded.", ex);
        }
    }

    public void save(java.util.List<Member> members) throws IOException {
        synchronized (persistence.LocalTransactions.LOCK) {
            var actor = security.LocalAuth.current();
            if (members.stream().map(Member::id).distinct().count() != members.size())
                throw new IOException("Team profile IDs must be unique.");
            for (var a : security.LocalAuth.profiles()) {
                Member linked = members.stream().filter(m -> m.id().equals(a.profileId())).findFirst()
                        .orElseThrow(() -> new IOException("Manage linked accounts in Accounts & access."));
                if (!linked.role().equals(a.role()) || linked.active() != a.active())
                    throw new IOException("Change account roles and status in Accounts & access.");
            }
            if (!security.LocalAuth.allowed(security.LocalAuth.Permission.TEAM_EDIT)) {
                var existing = read();
                if (existing.size() != members.size())
                    throw new SecurityException("You can edit only your own profile.");
                for (Member previous : existing) {
                    Member next = members.stream().filter(m -> m.id().equals(previous.id())).findFirst()
                            .orElseThrow(() -> new SecurityException("You can edit only your own profile."));
                    if (!previous.id().equals(actor.profileId()) && !previous.equals(next))
                        throw new SecurityException("You can edit only your own profile.");
                    if (previous.id().equals(actor.profileId())
                            && (!next.role().equals(previous.role()) || next.active() != previous.active()))
                        throw new SecurityException("Your role and status are managed by the Owner.");
                }
            }
            Properties p = new Properties();
            p.setProperty("count", Integer.toString(members.size()));
            for (int i = 0; i < members.size(); i++) {
                Member m = members.get(i);
                String k = "member." + i + ".";
                p.setProperty(k + "id", m.id());
                p.setProperty(k + "name", m.name());
                p.setProperty(k + "contact", m.contact());
                p.setProperty(k + "role", m.role());
                p.setProperty(k + "active", Boolean.toString(m.active()));
                p.setProperty(k + "image", m.image());
            }
            AtomicProperties.write(AtomicProperties.dataPath("team.properties"), p);
            AppEvents.fire("team");

        }
    }
}
