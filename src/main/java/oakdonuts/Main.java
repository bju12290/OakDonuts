// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.

package oakdonuts;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DB.initialize();

            MenuDAO menu = new MenuDAO();

            // Insert a donut
            menu.insert("Glazed Donut", 150, "Classic glazed donut");

            // Retrieve all donuts
            for (MenuItem item : menu.getAll()) {
                System.out.println(
                        item.getId() + " | " +
                                item.getName() + " | $" +
                                String.format("%.2f", item.getPriceCents() / 100.0)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
