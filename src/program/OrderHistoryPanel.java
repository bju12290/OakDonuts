
package program;

import entity.CartItem;
import entity.Order;
import entity.OrderDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private final List<Order> history = new ArrayList<>();

    private final DefaultTableModel ordersModel =
            readOnlyModel("Transaction ID", "Date", "Items", "Total");

    private final DefaultTableModel itemsModel =
            readOnlyModel("Donut", "Options", "Qty", "Price", "Total");

    private final JTable ordersTable = new JTable(ordersModel);
    private final JTable itemsTable = new JTable(itemsModel);

    private final JLabel detailsLabel =
            new JLabel("Select a transaction to view its items.");

    private final JSpinner quantitySpinner =
            new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());

    public OrderHistoryPanel() {

        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ordersTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        itemsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        // Selecting an order displays its purchased items.
        ordersTable.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        showSelectedOrder();
                    }
                });

        // Selecting an item loads its current quantity.
        itemsTable.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        showSelectedQuantity();
                    }
                });

        JPanel ordersPanel = new JPanel(new BorderLayout(5, 5));
        ordersPanel.add(
                new JLabel("Completed Transactions"),
                BorderLayout.NORTH
        );
        ordersPanel.add(
                new JScrollPane(ordersTable),
                BorderLayout.CENTER
        );

        JPanel itemsPanel = new JPanel(new BorderLayout(5, 5));
        itemsPanel.add(detailsLabel, BorderLayout.NORTH);
        itemsPanel.add(
                new JScrollPane(itemsTable),
                BorderLayout.CENTER
        );

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ordersPanel,
                itemsPanel
        );

        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // Controls for editing and deleting saved transactions.
        JButton updateButton = new JButton("Update Quantity");
        JButton deleteButton = new JButton("Delete Order");
        JButton refreshButton = new JButton("Refresh");

        updateButton.addActionListener(
                e -> runAction(this::updateQuantity)
        );

        deleteButton.addActionListener(
                e -> runAction(this::deleteOrder)
        );

        refreshButton.addActionListener(
                e -> runAction(this::refreshOrders)
        );

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        controls.add(new JLabel("Item Quantity:"));
        controls.add(quantitySpinner);
        controls.add(updateButton);
        controls.add(deleteButton);
        controls.add(refreshButton);

        add(controls, BorderLayout.SOUTH);

        refreshOrders();
    }

    // Create a JTable model that does not allow direct editing.
    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // READ - Retrieve transactions and rebuild the history table.
    public void refreshOrders() {

        ordersTable.clearSelection();
        itemsModel.setRowCount(0);
        ordersModel.setRowCount(0);

        history.clear();
        history.addAll(orderDAO.getAll());

        detailsLabel.setText(
                "Select a transaction to view its items."
        );

        for (Order order : history) {

            int itemCount = 0;

            for (CartItem item : order.getItems()) {
                itemCount += item.getQuantity();
            }

            ordersModel.addRow(new Object[]{
                    order.getTransactionId(),
                    DATE_FORMAT.format(order.getDate()),
                    itemCount,
                    money(order.getTotalCents())
            });
        }
    }

    // Display every purchased item in the selected order.
    private void showSelectedOrder() {

        itemsModel.setRowCount(0);

        int row = ordersTable.getSelectedRow();

        if (row < 0) {
            detailsLabel.setText("Select a transaction.");
            return;
        }

        Order order = history.get(
                ordersTable.convertRowIndexToModel(row)
        );

        detailsLabel.setText(
                "Transaction: " + order.getTransactionId()
                        + "   |   Total: " + money(order.getTotalCents())
        );

        for (CartItem item : order.getItems()) {

            itemsModel.addRow(new Object[]{
                    item.getMenuItem().getName(),
                    item.getOptions(),
                    item.getQuantity(),
                    money(item.getMenuItem().getPriceCents()),
                    money(item.getLineTotalCents())
            });
        }
    }

    // Show the stored quantity of the selected purchased item.
    private void showSelectedQuantity() {

        int orderRow = ordersTable.getSelectedRow();
        int itemRow = itemsTable.getSelectedRow();

        if (orderRow < 0 || itemRow < 0) {
            return;
        }

        Order order = history.get(
                ordersTable.convertRowIndexToModel(orderRow)
        );

        CartItem item = order.getItems().get(
                itemsTable.convertRowIndexToModel(itemRow)
        );

        quantitySpinner.setValue(item.getQuantity());
    }

    // Return the currently selected order.
    private Order selectedOrder() {

        int row = ordersTable.getSelectedRow();

        if (row < 0) {
            throw new IllegalArgumentException(
                    "Select a transaction first."
            );
        }

        return history.get(
                ordersTable.convertRowIndexToModel(row)
        );
    }

    // UPDATE - Modify the quantity of a purchased item.
    private void updateQuantity() {

        Order order = selectedOrder();

        int row = itemsTable.getSelectedRow();

        if (row < 0) {
            throw new IllegalArgumentException(
                    "Select an item within the transaction."
            );
        }

        int itemIndex = itemsTable.convertRowIndexToModel(row);
        int quantity = (int) quantitySpinner.getValue();

        boolean updated = orderDAO.updateQuantity(
                order.getTransactionId(),
                itemIndex,
                quantity
        );

        if (!updated) {
            throw new IllegalStateException(
                    "Transaction no longer exists."
            );
        }

        refreshOrders();
        selectTransaction(order.getTransactionId());
    }

    // DELETE - Remove the selected transaction.
    private void deleteOrder() {

        Order order = selectedOrder();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete transaction " + order.getTransactionId() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = orderDAO.delete(
                order.getTransactionId()
        );

        if (!deleted) {
            throw new IllegalStateException(
                    "Transaction no longer exists."
            );
        }

        refreshOrders();
    }

    // Restore transaction selection after refreshing the table.
    private void selectTransaction(String transactionId) {

        for (int i = 0; i < history.size(); i++) {

            if (history.get(i).getTransactionId()
                    .equals(transactionId)) {

                ordersTable.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    // Report errors without closing the application.
    private void runAction(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Operation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String money(int cents) {
        return String.format(
                Locale.US, "$%.2f", cents / 100.0
        );
    }
}
