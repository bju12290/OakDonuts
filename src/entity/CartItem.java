
package entity;

public class CartItem {

    private final MenuItem menuItem;
    private final int quantity;
    private final String icing;
    private final String filling;

    public CartItem(MenuItem menuItem, int quantity,
                    String icing, String filling) {

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1."
            );
        }

        this.menuItem = menuItem;
        this.quantity = quantity;
        this.icing = icing;
        this.filling = filling;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getIcing() {
        return icing;
    }

    public String getFilling() {
        return filling;
    }

    // Calculate the total cost of this cart entry.
    public int getLineTotalCents() {
        return menuItem.getPriceCents() * quantity;
    }

    // Format the selected options for display in the GUI.
    public String getOptions() {
        String result = "";

        if (!icing.equals("None")) {
            result += "Icing: " + icing;
        }

        if (!filling.equals("None")) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            result += "Filling: " + filling;
        }

        return result.isEmpty() ? "-" : result;
    }
}
