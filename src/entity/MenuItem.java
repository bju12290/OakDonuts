// Project: Oaks Donut Shop
// Course: CMPCS 221
// Author: Brian P. Hartnett Jr.
// File: MenuItem - Menu Item Class

package entity;

public class MenuItem {
    private String id;
    private String name;
    private int priceCents;
    private String description;

    public MenuItem(String id, String name, int priceCents, String description) {
        this.id = id;
        this.name = name;
        this.priceCents = priceCents;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public String getDescription() {
        return description;
    }
}
