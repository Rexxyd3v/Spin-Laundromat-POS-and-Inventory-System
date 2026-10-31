package persistence;

import java.io.IOException;
import java.util.Objects;

/**
 * One replaceable provider, shared by screens. Installation is an
 * application-startup concern.
 */
public final class Repositories {
    private static volatile RepositoryProvider provider = new FileRepositories();

    public static void install(RepositoryProvider replacement) {
        provider = Objects.requireNonNull(replacement);
    }

    public static RepositoryProvider.Catalog catalog() throws IOException {
        return provider.catalog();
    }

    public static RepositoryProvider.Inventory inventory() throws IOException {
        return provider.inventory();
    }

    public static RepositoryProvider.Inventory inventory(String branch) throws IOException {
        return provider.inventory(branch);
    }

    public static RepositoryProvider.Team team() {
        return provider.team();
    }

    public static RepositoryProvider.Orders orders() {
        return provider.orders();
    }

    public static RepositoryProvider.Settings settings() {
        return provider.settings();
    }

    public static RepositoryProvider.Operations operations() {
        return provider.operations();
    }

    public static RepositoryProvider.Assets assets() {
        return provider.assets();
    }

    private Repositories() {
    }
}
