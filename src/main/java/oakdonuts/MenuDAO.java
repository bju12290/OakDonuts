// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.

package oakdonuts;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    // CREATE
    public void insert(String name, int priceCents, String description)
            throws SQLException {

        String sql = "INSERT INTO OD_MENU (NAME, PRICE_CENTS, DESCRIPTION) VALUES (?, ?, ?)";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, priceCents);
            stmt.setString(3, description);

            stmt.executeUpdate();
        }
    }

    // READ
    public List<MenuItem> getAll() throws SQLException {
        List<MenuItem> items = new ArrayList<>();

        String sql = "SELECT * FROM OD_MENU ORDER BY ID";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getInt("PRICE_CENTS"),
                        rs.getString("DESCRIPTION")
                );

                items.add(item);
            }
        }

        return items;
    }
}
