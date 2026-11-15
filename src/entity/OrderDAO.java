
package entity;

import core.MongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

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


    // READ - Retrieve all completed orders, newest first.
    public List<Order> getAll() {

        List<Order> history = new ArrayList<>();

        // Query MongoDB, sorting by transaction date.
        for (Document doc : orders.find()
                .sort(Sorts.descending("date"))) {

            List<CartItem> items = new ArrayList<>();

            // Reconstruct each purchased item from its stored snapshot.
            for (Document purchased : doc.getList("items", Document.class)) {

                MenuItem snapshot = new MenuItem(
                        purchased.getString("menuItemId"),
                        purchased.getString("name"),
                        purchased.getInteger("priceCents"),
                        ""
                );

                CartItem cartItem = new CartItem(
                        snapshot,
                        purchased.getInteger("quantity"),
                        purchased.getString("icing"),
                        purchased.getString("filling")
                );

                items.add(cartItem);
            }

            // Convert the full MongoDB document into an Order.
            Order order = new Order(
                    doc.getObjectId("_id").toHexString(),
                    doc.getDate("date").toInstant(),
                    items,
                    doc.getInteger("subtotalCents"),
                    doc.getInteger("taxCents")
            );

            history.add(order);
        }

        return history;
    }


    // UPDATE - Change the quantity of an item in a saved order.
    public boolean updateQuantity(
            String transactionId, int itemIndex, int newQuantity) {

        if (newQuantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1."
            );
        }

        ObjectId id = new ObjectId(transactionId);

        // Find the existing transaction.
        Document order = orders.find(
                Filters.eq("_id", id)
        ).first();

        if (order == null) {
            return false;
        }

        List<Document> items = order.getList(
                "items", Document.class
        );

        if (itemIndex < 0 || itemIndex >= items.size()) {
            throw new IllegalArgumentException(
                    "Invalid order item."
            );
        }

        // Change the quantity of the selected item.
        items.get(itemIndex).put("quantity", newQuantity);

        // Recalculate all line totals and the order subtotal.
        int subtotalCents = 0;

        for (Document item : items) {

            int lineTotal = Math.multiplyExact(
                    item.getInteger("priceCents"),
                    item.getInteger("quantity")
            );

            item.put("lineTotalCents", lineTotal);

            subtotalCents = Math.addExact(
                    subtotalCents, lineTotal
            );
        }

        int taxCents = (int) Math.round(subtotalCents * 0.06);
        int totalCents = Math.addExact(subtotalCents, taxCents);

        // Save the revised items and totals together.
        // The transaction ID and original date remain unchanged.
        return orders.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                        Updates.set("items", items),
                        Updates.set("subtotalCents", subtotalCents),
                        Updates.set("taxCents", taxCents),
                        Updates.set("totalCents", totalCents)
                )
        ).getMatchedCount() > 0;
    }


    // DELETE - Remove a completed transaction.
    public boolean delete(String transactionId) {

        ObjectId id = new ObjectId(transactionId);

        return orders.deleteOne(
                Filters.eq("_id", id)
        ).getDeletedCount() > 0;
    }
}
