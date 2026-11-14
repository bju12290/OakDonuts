// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.
// File: MenuDAO: Oaks Donut Database Access Object

package entity;


import core.MongoDB;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

// Database operations for the donut shop menu
public class MenuDAO {

    private final MongoCollection<Document> menu;

    public MenuDAO() {
        menu = MongoDB.getDatabase().getCollection("menu");
    }

    // CREATE - Insert a new donut into MongoDB.
    public void insert(String name, int priceCents, String description) {

        // Construct a document representing the donut

        Document donut = new Document("name", name)
                .append("priceCents", priceCents)
                .append("description", description);

        menu.insertOne(donut);
    }

    // READ
    // Retrieve all items currently stored in the db
    public List<MenuItem> getAll() {

        // Create a list to store the items in
        List<MenuItem> items = new ArrayList<>();

        FindIterable<Document> iterable = menu.find();

        for (Document doc : iterable) {
            // Convert the current db row into a MenuItem object
            MenuItem item = new MenuItem(
                    doc.getObjectId("_id").toHexString(),
                    doc.getString("name"),
                    doc.getInteger("priceCents"),
                    doc.getString("description")
            );

            // Add the menu item to the list of resuls
            items.add(item);
        }

        // Return all retrieved items to the caller
        return items;
    }

    //UPDATE - Modify an existing menu item.
    public boolean update(String id, String name, int priceCents, String description) {

        // Locate the donut by its unique MongoDB ID.
        ObjectId objectId = new ObjectId(id);

        // Update the specified fields without changing the ID.
        UpdateResult result = menu.updateOne(
                Filters.eq("_id", objectId),
                Updates.combine(
                        Updates.set("name", name),
                        Updates.set("priceCents", priceCents),
                        Updates.set("description", description)
                )
        );

        // True if the donut was found.
        return result.getMatchedCount() > 0;
    }

    // DELETE - Remove a menu item by its unique ID.
    public boolean delete(String id) {
        ObjectId objectId = new ObjectId(id);

        DeleteResult result = menu.deleteOne(
                Filters.eq("_id", objectId)
        );

        return result.getDeletedCount() > 0;
    }
}
