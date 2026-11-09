// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.
// File: MenuDAO: Oaks Donut Database Access Object

package oakdonuts;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Database operations for the donut shop menu
public class MenuDAO {

    // CREATE
    // Insert a new menu item into the db
    public void insert(String name, int priceCents, String description)
            throws SQLException {

        // SQL Insert
        String sql = "INSERT INTO OD_MENU (NAME, PRICE_CENTS, DESCRIPTION) VALUES (?, ?, ?)";

        // Open a connection, prepare the SQL statement
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Bind to the corresponding placeholders
            stmt.setString(1, name);
            stmt.setInt(2, priceCents);
            stmt.setString(3, description);

            // Execute the statement
            stmt.executeUpdate();
        }
    }

    // READ
    // Retrieve all items currently stored in the db ordered by unique IDs
    public List<MenuItem> getAll() throws SQLException {

        // Create a list to store the items in
        List<MenuItem> items = new ArrayList<>();

        // Select all menu items in ascending order by ID
        String sql = "SELECT * FROM OD_MENU ORDER BY ID";

        // Execute the query
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Process each row returned by the db query
            while (rs.next()) {

                // Convert the current db row into a MenuItem object
                MenuItem item = new MenuItem(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getInt("PRICE_CENTS"),
                        rs.getString("DESCRIPTION")
                );

                // Add the menu item to the list of resuls
                items.add(item);
            }
        }

        // Return all retrieved items to the caller
        return items;
    }
}
