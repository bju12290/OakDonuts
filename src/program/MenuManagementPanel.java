
package program;

import entity.MenuDAO;
import entity.MenuItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuManagementPanel extends JPanel {

    private final MenuDAO menuDAO = new MenuDAO();
    private final List<MenuItem> displayedItems = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Price", "Description"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JTextField nameField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextArea descriptionField = new JTextArea(3, 20);

    public MenuManagementPanel() {

        super(new BorderLayout(10, 10));

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Menu Management"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate the edit fields when a row is selected.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedItem();
            }
        });

        // Build the item editor.
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Price ($):"));
        form.add(priceField);

        form.add(new JLabel("Description:"));
        form.add(new JScrollPane(descriptionField));

        // Create CRUD controls.
        JButton addButton = new JButton("Add New");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> runAction(this::addItem));
        updateButton.addActionListener(e -> runAction(this::updateItem));
        deleteButton.addActionListener(e -> runAction(this::deleteItem));
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> runAction(this::refreshMenu));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);
        buttons.add(refreshButton);

        JPanel editor = new JPanel(new BorderLayout(8, 8));
        editor.add(form, BorderLayout.CENTER);
        editor.add(buttons, BorderLayout.SOUTH);

        add(editor, BorderLayout.SOUTH);

        refreshMenu();
    }

    // READ - Load menu items from MongoDB into the table.
    public void refreshMenu() {

        List<MenuItem> latestItems = menuDAO.getAll();

        displayedItems.clear();
        displayedItems.addAll(latestItems);

        model.setRowCount(0);

        for (MenuItem item : displayedItems) {
            model.addRow(new Object[]{
                    item.getName(),
                    money(item.getPriceCents()),
                    item.getDescription()
            });
        }
    }

    // Copy the selected row's values into the editor.
    private void showSelectedItem() {

        int row = table.getSelectedRow();

        if (row < 0) {
            return;
        }

        MenuItem item = displayedItems.get(
                table.convertRowIndexToModel(row)
        );

        nameField.setText(item.getName());
        priceField.setText(String.format(
                Locale.US, "%.2f", item.getPriceCents() / 100.0
        ));
        descriptionField.setText(item.getDescription());
    }

    // Return the selected menu item or report an error.
    private MenuItem getSelectedItem() {

        int row = table.getSelectedRow();

        if (row < 0) {
            throw new IllegalArgumentException(
                    "Select a menu item first."
            );
        }

        return displayedItems.get(
                table.convertRowIndexToModel(row)
        );
    }

    // CREATE - Insert a new donut into MongoDB.
    private void addItem() {

        menuDAO.insert(
                getItemName(),
                getPriceCents(),
                getDescription()
        );

        refreshMenu();
        clearForm();
    }

    // UPDATE - Modify the selected donut.
    private void updateItem() {

        MenuItem selected = getSelectedItem();

        boolean updated = menuDAO.update(
                selected.getId(),
                getItemName(),
                getPriceCents(),
                getDescription()
        );

        if (!updated) {
            throw new IllegalStateException("Menu item no longer exists.");
        }

        refreshMenu();
        clearForm();
    }

    // DELETE - Remove the selected donut.
    private void deleteItem() {

        MenuItem selected = getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selected.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = menuDAO.delete(selected.getId());

        if (!deleted) {
            throw new IllegalStateException("Menu item no longer exists.");
        }

        refreshMenu();
        clearForm();
    }

    // Validate the menu item name.
    private String getItemName() {
        String name = nameField.getText().trim();

        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException(
                    "Name must contain 1-100 characters."
            );
        }

        return name;
    }

    // Convert a dollar amount into integer cents.
    private int getPriceCents() {
        try {
            BigDecimal dollars = new BigDecimal(
                    priceField.getText().trim()
            );

            int cents = dollars.movePointRight(2).intValueExact();

            if (cents <= 0) {
                throw new IllegalArgumentException(
                        "Price must be greater than zero."
                );
            }

            return cents;

        } catch (NumberFormatException | ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Enter a valid price, such as 1.49."
            );
        }
    }

    private String getDescription() {
        String description = descriptionField.getText().trim();

        if (description.length() > 255) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 255 characters."
            );
        }

        return description;
    }

    private void clearForm() {
        table.clearSelection();
        nameField.setText("");
        priceField.setText("");
        descriptionField.setText("");
    }

    // Show database or input errors without crashing the GUI.
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
        return String.format(Locale.US, "$%.2f", cents / 100.0);
    }
}
