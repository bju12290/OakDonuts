
package entity;

import core.MongoDB;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDAO {

    private final MongoCollection<Document> orders;

    public OrderDAO() {
        orders = MongoDB.getDatabase().getCollection("orders");
    }

    // CREATE - Save a completed shopping cart as a transaction.
    public Order checkout(List<CartItem> cart) {

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        List<Document> purchasedItems = new ArrayList<>();
        int subtotalCents = 0;

        // Convert cart items into MongoDB documents.
        for (CartItem cartItem : cart) {

            MenuItem item = cartItem.getMenuItem();

            int lineTotal = Math.multiplyExact(
                    item.getPriceCents(),
                    cartItem.getQuantity()
            );

            subtotalCents = Math.addExact(
                    subtotalCents, lineTotal
            );

            Document purchasedItem = new Document(
                    "menuItemId", item.getId()
            )
                    .append("name", item.getName())
                    .append("priceCents", item.getPriceCents())
                    .append("quantity", cartItem.getQuantity())
                    .append("icing", cartItem.getIcing())
                    .append("filling", cartItem.getFilling())
                    .append("lineTotalCents", lineTotal);

            purchasedItems.add(purchasedItem);
        }

        // Calculate the final transaction totals.
        int taxCents = (int) Math.round(subtotalCents * 0.06);
        int totalCents = subtotalCents + taxCents;

        // Generate an ID and timestamp for the transaction.
        ObjectId id = new ObjectId();
        Instant now = Instant.now();

        // Construct the complete order document.
        Document orderDocument = new Document("_id", id)
                .append("transactionId", id.toHexString())
                .append("date", Date.from(now))
                .append("items", purchasedItems)
                .append("subtotalCents", subtotalCents)
                .append("taxCents", taxCents)
                .append("totalCents", totalCents);

        // Persist the order to MongoDB.
        orders.insertOne(orderDocument);

        // Return the completed order to the application.
        return new Order(
                id.toHexString(),
                now,
                cart,
                subtotalCents,
                taxCents
        );
    }
}
