// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.
// File: DB

package oakdonuts;

import java.sql.*;

// Manages the Derby db connection and init
public class DB {

    // Connection URL for the local db
    private static final String URL =
            "jdbc:derby:oakdonutsdb;create=true";

    // Open and return a connection to the derby db.
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Initialize the database tables
    public static void initialize() throws SQLException {

        // Open a connection that automatically closes when finished
        try (Connection conn = connect()) {

            // Check the metadata to see if the OD_MENU table already exists
            ResultSet tables = conn.getMetaData()
                    .getTables(null, null, "OD_MENU", null);

            boolean exists = tables.next();
            tables.close();

            if (!exists) {

                // Create the menu table with a generated ID, item name, price in cents, and optional description.
                String sql = """
                        CREATE TABLE OD_MENU (
                            ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            NAME VARCHAR(100) NOT NULL,
                            PRICE_CENTS INT NOT NULL,
                            DESCRIPTION VARCHAR(255)
                        )
                        """;

                // Execute the SQL statement to create the table
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }

                System.out.println("Menu table created!");
            } else {

                // Skip table creation if it already exists.
                System.out.println("Menu table already exists.");
            }
        }
    }
}
