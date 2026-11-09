// Project: Oaks Donut Shop
// Course: CMPSC 221
// Author: Brian P. Hartnett Jr.
// File: MongoDB.java

package core;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// Establish and test a connection to MongoDB
public class MongoDB {

    // Default to a local MongoDB server.
    // An environment variable can override this for Atlas.
    private static final String URI = "mongodb://localhost:27017";

    // Shared client used for database operations.
    private static final MongoClient client = MongoClients.create(URI);

    /**
     * Returns the Oak Donuts database.
     *
     * @return the MongoDB database instance
     */
    public static MongoDatabase getDatabase() {
        return client.getDatabase("oaksdonuts");
    }



    public static void testConnection() {

        String configuredURI = System.getenv(("MONGODB_URI"));

        if (configuredURI == null) {
            System.out.println("Using default local MongoDB.");
        } else {
            System.out.println("Using MONGODB_URI env.");
        }

        // Create a new connection using the connection URI
        try (MongoClient client = MongoClients.create(URI)) {

            // Select the apps db
            MongoDatabase database = client.getDatabase("oaksdonuts");

            database.runCommand(new Document("ping", 1));

            System.out.println("Connected to Mongo");

        }
    }
}
