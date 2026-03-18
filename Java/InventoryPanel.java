
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * InventoryPanel.java
 * 
 * This panel allows users to view, add, edit, and delete inventory items.
 * It features a search bar, a table with item details, and a side panel showing selected item information.
 * The table supports inline editing and highlights low stock items in red.
 */
public class InventoryPanel extends JPanel {

    private static Connection conn;
    private static DefaultTableModel tableModel;
    private static JTable inventoryTable;

    private boolean isEditing = false;
    private boolean dataChanged = false;

    private JButton editBtn, addBtn, confirmBtn;
    private JPanel buttonPanel;
    private JScrollPane scrollPane;
    private JLabel nameLabel = new JLabel("Name: -");
    private JLabel amountLabel = new JLabel("Amount: -");
    private JLabel supplierNameLabel = new JLabel("Supplier Name: -");
    private JLabel supplierContactLabel = new JLabel("Supplier Contact: -");
    private JLabel totalStatsLabel = new JLabel("Total Items: 0");

    /**
     * @author Qayyum alli and ethan nguyen
     * @version 1.0
     * @throws SQLException if database connection fails
     * gets the database connection using credentials from the .env file
     */
    private static void GetConnection() {
        Properties props = new Properties();
        var envFile = Paths.get(".env").toAbsolutePath().toString();
        try (FileInputStream inputStream = new FileInputStream(envFile)) {
            props.load(inputStream);
            String dbName = props.getProperty("DATABASE_NAME");
            String dbUser = props.getProperty("DATABASE_USER");
            String dbPass = props.getProperty("DATABASE_PASSWORD");
            String dbUrl = props.getProperty("DATABASE_URL") + dbName;
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed: " + e.getMessage());
        }
    }

    /**
     * @author Qayyum alli
     * @param screen the main GUI screen
     * @version 1.0
     * sets up the inventory panel with a table, search functionality, and buttons for adding/editing items.
     * The table supports inline editing and highlights low stock items. Changes can be confirmed to update the database.
     */
    public InventoryPanel(GUI screen) {
        GetConnection();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> screen.showScreen("MAIN"));
        topPanel.add(backButton);

        JTextField searchField = new JTextField(15);
        JButton refreshBtn = new JButton("Refresh");
        addBtn = new JButton("Add New Row");
        editBtn = new JButton("Edit Table");

        topPanel.add(new JLabel("Search Name:"));
        topPanel.add(searchField);
        topPanel.add(refreshBtn);
        topPanel.add(addBtn);
        topPanel.add(editBtn);

        // --- 2. Center (Table with Hidden ID) ---
        String[] columns = {"ID", "Item Name", "Quantity", "Supplier Name", "Supplier Contact"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2) {
                    return Integer.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // ID is index 0, Item Name is index 1. Both now editable during Edit mode!
                return isEditing && column != 0;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(30);
        inventoryTable.setAutoCreateRowSorter(true);

        // Hide the ID column from view
        inventoryTable.removeColumn(inventoryTable.getColumnModel().getColumn(0));

        // Low Stock Highlighter (Red if <= 5)
        /**
         * @author Qayyum alli
         * This custom cell renderer checks the quantity of each item and highlights the row in soft red if the quantity is 5 or less.
         * It also ensures that the selection background color is preserved when a row is selected, and defaults to white for normal rows.
         */
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                int qty = (int) table.getModel().getValueAt(modelRow, 2);

                if (qty <= 5) {
                    c.setBackground(new Color(255, 210, 210)); // Soft Red
                } else if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        scrollPane = new JScrollPane(inventoryTable);
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        scrollPane.setRowHeaderView(buttonPanel);

        // --- 3. Right Panel (Details) ---
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(250, 0));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Selected Item Details"));
        detailPanel.add(nameLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(amountLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(supplierNameLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(supplierContactLabel);

        // --- 4. Bottom Panel ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        confirmBtn = new JButton("Confirm Changes");
        confirmBtn.setBackground(new Color(46, 204, 113));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setVisible(false);
        bottomPanel.add(totalStatsLabel, BorderLayout.WEST);
        bottomPanel.add(confirmBtn, BorderLayout.EAST);

        // --- Events ---
        /**
         * @author Qayyum alli
         * This listener updates the right-side detail panel whenever a new row is selected in the inventory table. It retrieves the item details from the selected row and displays them in the labels. If no row is selected, it defaults to showing "-" for all fields.
         * @param e the list selection event triggered by changing the selected row in the inventory table
         */
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() != -1) {
                int modelRow = inventoryTable.convertRowIndexToModel(inventoryTable.getSelectedRow());
                nameLabel.setText("Name: " + tableModel.getValueAt(modelRow, 1));
                amountLabel.setText("Amount: " + tableModel.getValueAt(modelRow, 2));
                supplierNameLabel.setText("Supplier Name: " + tableModel.getValueAt(modelRow, 3));
                supplierContactLabel.setText("Supplier Contact: " + tableModel.getValueAt(modelRow, 4));
            }
        });

        /**
         * @author Qayyum alli
         * This listener handles the edit button action, toggling the editing mode and updating the button text accordingly.
         * @param e the action event triggered by clicking the edit button
         */
        editBtn.addActionListener(e -> {
            if (isEditing && dataChanged) {
                JOptionPane.showMessageDialog(this, "Confirm changes or refresh first.");
                return;
            }
            isEditing = !isEditing;
            editBtn.setText(isEditing ? "Finish Editing" : "Edit Table");
            updateActionButtons();
        });

        addBtn.addActionListener(e -> showAddItemDialog("", 0, "", ""));
        
        /**
         * @author Qayyum alli
         * This listener handles the confirm button action, saving the table changes and updating the UI.
         * @param e the action event triggered by clicking the confirm button
         */
        confirmBtn.addActionListener(e -> {
            saveTableChanges();
            dataChanged = false;
            confirmBtn.setVisible(false);
            JOptionPane.showMessageDialog(this, "Database Updated!");
        });

        /**
         * @author Qayyum alli
         * This listener updates the confirm button visibility based on the table model changes.
         * @param e the table model event triggered by changes in the inventory table
         */
        tableModel.addTableModelListener(e -> {
            if (isEditing && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                dataChanged = true;
                confirmBtn.setVisible(true);
            }
        });

        /**
         * @author Qayyum alli
         * This listener updates the inventory data based on the search field input.
         * @param e the document event triggered by changes in the search field
         */
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }
        });
        
        /**
         * @author Qayyum alli
         * This listener refreshes the inventory data when the refresh button is clicked, resetting the search field and hiding the confirm button.
         * @param e the action event triggered by clicking the refresh button
         */
        refreshBtn.addActionListener(e -> {
            dataChanged = false;
            confirmBtn.setVisible(false);
            refreshData(searchField.getText());
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
        splitPane.setDividerLocation(700);
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData("");
    }


    /**
     * @author Qayyum alli
     * This method updates the action buttons displayed for each row in the inventory table based on the editing mode.
     */
    private void updateActionButtons() {
        buttonPanel.removeAll();
        if (isEditing) {
            for (int i = 0; i < inventoryTable.getRowCount(); i++) {
                JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                rowActions.setMaximumSize(new Dimension(70, 30));
                JButton del = new JButton("X");
                del.setForeground(Color.RED);
                JButton duo = new JButton("D");
                del.setPreferredSize(new Dimension(30, 30));
                duo.setPreferredSize(new Dimension(30, 30));

                final int viewRow = i;
                del.addActionListener(e -> {
                    int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                    int id = (int) tableModel.getValueAt(modelRow, 0);
                    String name = (String) tableModel.getValueAt(modelRow, 1);
                    if (JOptionPane.showConfirmDialog(this, "Delete " + name + "?") == JOptionPane.YES_OPTION) {
                        deleteFromDatabase(id);
                        refreshData("");
                    }
                });

                duo.addActionListener(e -> {
                    int mRow = inventoryTable.convertRowIndexToModel(viewRow);
                    showAddItemDialog((String) tableModel.getValueAt(mRow, 1), (int) tableModel.getValueAt(mRow, 2), (String) tableModel.getValueAt(mRow, 3), (String) tableModel.getValueAt(mRow, 4));
                });

                rowActions.add(del);
                rowActions.add(duo);
                buttonPanel.add(rowActions);
            }
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }


    /**
     * @author Qayyum alli
     * This method displays a dialog for adding a new inventory item or duplicating an existing one. It takes the item details as parameters and allows the user to input or modify them. Upon confirmation, it inserts the new item into the database and refreshes the inventory data.
     * @param dName the default name of the item to be added or duplicated
     * @param dQty the default quantity of the item to be added or duplicated
     * @param dSupp the default supplier name of the item to be added or duplicated
     * @param dCont the default supplier contact of the item to be added or duplicated
     */
    private void showAddItemDialog(String dName, int dQty, String dSupp, String dCont) {
        JTextField f1 = new JTextField(dName);
        JTextField f2 = new JTextField(String.valueOf(dQty));
        JTextField f3 = new JTextField(dSupp);
        JTextField f4 = new JTextField(dCont);
        Object[] m = {"Item Name:", f1, "Qty:", f2, "Supplier:", f3, "Contact:", f4};

        if (JOptionPane.showConfirmDialog(null, m, "Add/Duplicate Item", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // FIX: Explicitly name columns so 'id' can be auto-generated by the DB
            String sql = "INSERT INTO inventory (name, amount, supplier_name, supplier_contact) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, f1.getText());
                ps.setInt(2, Integer.parseInt(f2.getText()));
                ps.setString(3, f3.getText());
                ps.setString(4, f4.getText());
                ps.executeUpdate();
                refreshData("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding item: " + ex.getMessage());
            }
        }
    }


    /**
     * @author Qayyum alli
     * This method saves the changes made in the inventory table to the database. It iterates through each row of the table model, retrieves the updated values, and executes an update query for each item. If any errors occur during the database update, it displays an error message.
     */
    private void saveTableChanges() {
        String sql = "UPDATE inventory SET name = ?, amount = ?, supplier_name = ?, supplier_contact = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pstmt.setString(1, (String) tableModel.getValueAt(i, 1));
                pstmt.setInt(2, (int) tableModel.getValueAt(i, 2));
                pstmt.setString(3, (String) tableModel.getValueAt(i, 3));
                pstmt.setString(4, (String) tableModel.getValueAt(i, 4));
                pstmt.setInt(5, (int) tableModel.getValueAt(i, 0));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save Error: " + ex.getMessage());
        }
    }

    /**
     * @author Qayyum alli
     * This method deletes an inventory item from the database based on the provided item ID. It executes a delete query and handles any SQL exceptions that may occur during the operation.
     * @param id the unique identifier of the inventory item to be deleted
     */
    private void deleteFromDatabase(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    /**
     * @author Qayyum alli
     * This method refreshes the inventory data displayed in the table based on the provided filter. It executes a SQL query to retrieve items that match the filter criteria and updates the table model accordingly. It also calculates and displays the total quantity of items and updates the action buttons for each row.
     * @param filter the search term used to filter inventory items by name
     */
    private void refreshData(String filter) {
        if (conn == null) {
            return;
        }
        tableModel.setRowCount(0);
        int total = 0;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id, name, amount, supplier_name, supplier_contact FROM inventory WHERE name ILIKE ? ORDER BY id ASC")) {
            pstmt.setString(1, "%" + filter + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int q = rs.getInt("amount");
                tableModel.addRow(new Object[]{id, name, q, rs.getString("supplier_name"), rs.getString("supplier_contact")});
                total += q;
            }
            totalStatsLabel.setText("Total Items: " + total);
            updateActionButtons();
        } catch (SQLException e) {
        }
    }
}
