
package program;

import entity.MenuDAO;
import entity.MenuItem;
import entity.CartItem;
import entity.Order;
import entity.OrderDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main extends JFrame {

    private final List<MenuItem> menuItems;
    private final List<CartItem> cart = new ArrayList<>();

    // Menu components
    private final DefaultListModel<String> menuModel =
            new DefaultListModel<>();

    private final JList<String> menuList =
            new JList<>(menuModel);

    private final JSpinner quantitySpinner =
            new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

    private final JComboBox<String> icingBox =
            new JComboBox<>(new String[]{
                    "None", "Chocolate", "Vanilla"
            });

    private final JComboBox<String> fillingBox =
            new JComboBox<>(new String[]{
                    "None", "Custard", "Jelly"
            });

    // Cart components
    private final DefaultTableModel cartModel =
            new DefaultTableModel(
                    new String[]{"Item", "Options", "Qty", "Price", "Total"},
                    0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable cartTable = new JTable(cartModel);

    private final JLabel subtotalLabel = new JLabel("$0.00");
    private final JLabel taxLabel = new JLabel("$0.00");
    private final JLabel totalLabel = new JLabel("$0.00");

    private final OrderDAO orderDAO = new OrderDAO();
    private final JButton checkoutButton = new JButton("Checkout");

    public Main(List<MenuItem> items) {

        super("Oak Donuts - Ordering");
        this.menuItems = items;

        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JLabel heading = new JLabel("Oak Donuts");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 24f));
        root.add(heading, BorderLayout.NORTH);

        // Populate the menu with items from MongoDB.
        for (MenuItem item : menuItems) {
            menuModel.addElement(
                    item.getName() + " - " + money(item.getPriceCents())
            );
        }

        if (!menuItems.isEmpty()) {
            menuList.setSelectedIndex(0);
        }

        // LEFT: Menu and selection controls
        JPanel menuPanel = new JPanel(new BorderLayout(8, 8));
        menuPanel.add(new JLabel("Menu"), BorderLayout.NORTH);
        menuPanel.add(new JScrollPane(menuList), BorderLayout.CENTER);

        JPanel inputs = new JPanel(new GridLayout(3, 2, 6, 6));
        inputs.add(new JLabel("Quantity:"));
        inputs.add(quantitySpinner);
        inputs.add(new JLabel("Icing:"));
        inputs.add(icingBox);
        inputs.add(new JLabel("Filling:"));
        inputs.add(fillingBox);

        JButton addButton = new JButton("Add to Order");
        addButton.addActionListener(e -> addToOrder());

        JPanel menuBottom = new JPanel(new BorderLayout(6, 6));
        menuBottom.add(inputs, BorderLayout.CENTER);
        menuBottom.add(addButton, BorderLayout.SOUTH);

        menuPanel.add(menuBottom, BorderLayout.SOUTH);

        // RIGHT: Cart table and totals
        JPanel orderPanel = new JPanel(new BorderLayout(8, 8));
        orderPanel.add(new JLabel("Order"), BorderLayout.NORTH);
        orderPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel totals = new JPanel(new GridLayout(3, 2, 6, 6));
        totals.add(new JLabel("Subtotal:"));
        totals.add(subtotalLabel);
        totals.add(new JLabel("Tax (6%):"));
        totals.add(taxLabel);
        totals.add(new JLabel("Total:"));
        totals.add(totalLabel);

        subtotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        taxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelected());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            cart.clear();
            refreshCart();
        });

        checkoutButton.setEnabled(false);
        checkoutButton.addActionListener(e -> checkout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(removeButton);
        buttons.add(clearButton);
        buttons.add(checkoutButton);

        JPanel orderBottom = new JPanel(new BorderLayout(6, 6));
        orderBottom.add(totals, BorderLayout.CENTER);
        orderBottom.add(buttons, BorderLayout.SOUTH);

        orderPanel.add(orderBottom, BorderLayout.SOUTH);

        // Divide the window into menu and order sections.
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                menuPanel,
                orderPanel
        );
        split.setResizeWeight(0.4);

        // Create the application's tab navigation.
        JTabbedPane tabs = new JTabbedPane();

        // Existing ordering interface.
        tabs.addTab("Ordering", split);

        // Temporary placeholder for transaction management.
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(
                new JLabel("Order History", SwingConstants.CENTER),
                BorderLayout.CENTER
        );
        tabs.addTab("Order History", historyPanel);

        // Temporary placeholder for menu administration.
        JPanel managementPanel = new JPanel(new BorderLayout());
        managementPanel.add(
                new JLabel("Menu Management", SwingConstants.CENTER),
                BorderLayout.CENTER
        );
        tabs.addTab("Menu Management", managementPanel);

        // Display the tabbed interface in the main window.
        root.add(tabs, BorderLayout.CENTER);
    }

    // Add the selected donut to the shopping cart.
    private void addToOrder() {

        int index = menuList.getSelectedIndex();

        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a menu item.");
            return;
        }

        MenuItem selected = menuItems.get(index);
        int quantity = (int) quantitySpinner.getValue();

        String icing = (String) icingBox.getSelectedItem();
        String filling = (String) fillingBox.getSelectedItem();

        CartItem item = new CartItem(
                selected, quantity, icing, filling
        );

        cart.add(item);
        refreshCart();
    }

    // Remove the selected row from the cart.
    private void removeSelected() {

        int row = cartTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(row);
        cart.remove(modelRow);
        refreshCart();
    }

    // Rebuild the cart table and recalculate totals.
    private void refreshCart() {

        cartModel.setRowCount(0);

        int subtotalCents = 0;

        for (CartItem item : cart) {

            subtotalCents += item.getLineTotalCents();

            cartModel.addRow(new Object[]{
                    item.getMenuItem().getName(),
                    item.getOptions(),
                    item.getQuantity(),
                    money(item.getMenuItem().getPriceCents()),
                    money(item.getLineTotalCents())
            });
        }

        int taxCents = (int) Math.round(subtotalCents * 0.06);
        int totalCents = subtotalCents + taxCents;

        subtotalLabel.setText(money(subtotalCents));
        taxLabel.setText(money(taxCents));
        totalLabel.setText(money(totalCents));

        checkoutButton.setEnabled(!cart.isEmpty());
    }

    // Convert integer cents into a formatted dollar amount.
    private static String money(int cents) {
        return String.format(Locale.US, "$%.2f", cents / 100.0);
    }

    // Save the current shopping cart as a completed transaction.
    private void checkout() {

        if (cart.isEmpty()) {
            return;
        }

        try {
            Order order = orderDAO.checkout(cart);

            JOptionPane.showMessageDialog(
                    this,
                    "Order completed!\n" +
                            "Transaction ID: " + order.getTransactionId() +
                            "\nTotal: " + money(order.getTotalCents()),
                    "Checkout Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Clear the cart only after a successful database write.
            cart.clear();
            refreshCart();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Checkout failed: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    public static void main(String[] args) {

        MenuDAO menu = new MenuDAO();
        List<MenuItem> items = menu.getAll();

        SwingUtilities.invokeLater(() ->
                new Main(items).setVisible(true)
        );
    }
}
