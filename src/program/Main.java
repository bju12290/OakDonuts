
package program;

import entity.MenuDAO;
import entity.MenuItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Main extends JFrame {

    public Main(List<MenuItem> items) {

        // Configure the window
        setTitle("Oak Donuts");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Define the columns of our menu table
        String[] columns = {"Name", "Price", "Description"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate the table using menu items from MongoDB
        for (MenuItem item : items) {
            model.addRow(new Object[] {
                item.getName(),
                String.format("$%.2f", item.getPriceCents() / 100.00),
                item.getDescription()
            });
        }

        JTable table = new JTable(model);

        // Add components to the window
        add(new JLabel("Oak Donuts - Menu"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public static void main(String[] args) {

        MenuDAO menu = new MenuDAO();
        List<MenuItem> items = menu.getAll();

        // Create and display the GUI on Swing's event thread
        SwingUtilities.invokeLater(() -> {
            new Main(items).setVisible(true);
        });
    }
}
