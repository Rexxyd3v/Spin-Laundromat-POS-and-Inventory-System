package application;

import java.io.IOException;
import model.OrderStore;
import persistence.*;

/** Confirmation is delegated to the configured repository, outside Swing. */
public final class CheckoutService {
    public RepositoryProvider.Confirmation confirm(OrderStore.Order order) throws IOException {
        return Repositories.orders().confirm(order);
    }
}
