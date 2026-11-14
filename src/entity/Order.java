
package entity;

import java.time.Instant;
import java.util.List;

public class Order {

    private final String transactionId;
    private final Instant date;
    private final List<CartItem> items;
    private final int subtotalCents;
    private final int taxCents;

    public Order(String transactionId, Instant date,
                 List<CartItem> items, int subtotalCents,
                 int taxCents) {

        this.transactionId = transactionId;
        this.date = date;
        this.items = List.copyOf(items);
        this.subtotalCents = subtotalCents;
        this.taxCents = taxCents;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getDate() {
        return date;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getSubtotalCents() {
        return subtotalCents;
    }

    public int getTaxCents() {
        return taxCents;
    }

    public int getTotalCents() {
        return subtotalCents + taxCents;
    }
}
