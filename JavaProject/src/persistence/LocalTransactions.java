package persistence;

/**
 * Coordinates local file transactions and snapshots within this JVM, not across
 * PCs.
 */
public final class LocalTransactions {
    public static final Object LOCK = new Object();

    private LocalTransactions() {
    }
}
