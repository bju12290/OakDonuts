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
import org.bson.Document;

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
}
