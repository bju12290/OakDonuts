// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.

package oakdonuts;

import java.sql.*;

public class DB {

    private static final String URL =
            "jdbc:derby:oakdonutsdb;create=true";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() throws SQLException {
        try (Connection conn = connect()) {

            ResultSet tables = conn.getMetaData()
                    .getTables(null, null, "OD_MENU", null);

            boolean exists = tables.next();
            tables.close();

            if (!exists) {
                String sql = """
                        CREATE TABLE OD_MENU (
                            ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            NAME VARCHAR(100) NOT NULL,
                            PRICE_CENTS INT NOT NULL,
                            DESCRIPTION VARCHAR(255)
                        )
                        """;

                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }

                System.out.println("Menu table created!");
            } else {
                System.out.println("Menu table already exists.");
            }
        }
    }
}
