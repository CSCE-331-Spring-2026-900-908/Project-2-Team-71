
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class PurchasesPanel extends JPanel {

    private static Connection conn;

    // Table and model
    private DefaultTableModel model;
    private JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    // Summary labels
    private final JLabel overallSummaryLabel;
    private JLabel selectedItemSummaryLabel;

    public PurchasesPanel(GUI gui) {
        setLayout(new BorderLayout());

        // ===== Top Bar =====
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topBar.add(backButton);

        JTextField searchField = new JTextField(10);
        JTextField fromDateField = new JTextField(8);
        JTextField toDateField = new JTextField(8);
        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);
        topBar.add(new JLabel("From (YYYY-MM-DD):"));
        topBar.add(fromDateField);
        topBar.add(new JLabel("To:"));
        topBar.add(toDateField);

        JButton refreshButton = new JButton("Refresh");
        topBar.add(refreshButton);

        add(topBar, BorderLayout.NORTH);

        // ===== Table Setup =====
        String[] columns = {"Item Name", "Amount", "Supplier Price", "Buy Date", "Supplier Name", "Supplier Contact"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        table.setFillsViewportHeight(
                true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);

        table.setRowSorter(sorter);

        table.getColumnModel()
                .getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);
        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setWidth(0);

        // Ensure numeric sorting works
        sorter.setComparator(1, (o1, o2) -> Integer.compare((Integer) o1, (Integer) o2));
        sorter.setComparator(2, (o1, o2) -> Double.compare((Double) o1, (Double) o2));

        // Default sort by Buy Date descending
        sorter.setSortKeys(List.of(new RowSorter.SortKey(3, SortOrder.DESCENDING)));

        JScrollPane tableScrollPane = new JScrollPane(table);

        // ===== Selected Item Panel =====
        JPanel selectedPanel = new JPanel();
        selectedPanel.setLayout(new BoxLayout(selectedPanel, BoxLayout.Y_AXIS));
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Selected Item"));

        JLabel selectedName = new JLabel("Name: ");
        JLabel selectedAmount = new JLabel("Amount: ");
        JLabel selectedPrice = new JLabel("Price: ");
        JLabel selectedDate = new JLabel("Buy Date: ");
        JLabel selectedSupplierName = new JLabel("Supplier: ");
        JLabel selectedSupplierContact = new JLabel("Contact: ");

        selectedPanel.add(selectedName);
        selectedPanel.add(selectedAmount);
        selectedPanel.add(selectedPrice);
        selectedPanel.add(selectedSupplierName);
        selectedPanel.add(selectedSupplierContact);
        selectedPanel.add(selectedDate);

        // Create a horizontal split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, selectedPanel);

        // Set initial divider location (pixels from left)
        splitPane.setDividerLocation(600);
        splitPane.setOneTouchExpandable(true);

        // Set sizes
        tableScrollPane.setMinimumSize(new Dimension(400, 200));
        selectedPanel.setMinimumSize(new Dimension(200, 200));
        selectedPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        // Add the split pane to the main panel
        add(splitPane, BorderLayout.CENTER);

        JButton newPurchaseButton = new JButton("Add Purchase");
        topBar.add(newPurchaseButton);

        newPurchaseButton.addActionListener(e -> showAddPurchaseDialog());

        // ===== Bottom Summary Panel =====
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));
        overallSummaryLabel = new JLabel();
        selectedItemSummaryLabel = new JLabel();
        summaryPanel.add(overallSummaryLabel);
        summaryPanel.add(selectedItemSummaryLabel);
        add(summaryPanel, BorderLayout.SOUTH);

        // ===== Load initial data =====
        loadTableData();
        updateOverallSummary();

        // ===== Refresh button action =====
        refreshButton.addActionListener(e -> {
            loadTableData();
            applyFilter(searchField, fromDateField, toDateField);
            updateOverallSummary();
        });

        // ===== Live filtering =====
        Runnable applyFilterRunnable = () -> {
            applyFilter(searchField, fromDateField, toDateField);
            updateOverallSummary();
        };

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(applyFilterRunnable));
        fromDateField.getDocument().addDocumentListener(new SimpleDocumentListener(applyFilterRunnable));
        toDateField.getDocument().addDocumentListener(new SimpleDocumentListener(applyFilterRunnable));

        // ===== Table selection listener =====
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                selectedName.setText("Name: " + model.getValueAt(modelRow, 0));
                selectedAmount.setText("Amount: " + model.getValueAt(modelRow, 1));
                selectedPrice.setText("Price: $" + model.getValueAt(modelRow, 2));
                selectedDate.setText("Buy Date: " + model.getValueAt(modelRow, 3));
                selectedSupplierName.setText("Supplier: " + model.getValueAt(modelRow, 4));
                selectedSupplierContact.setText("Contact: " + model.getValueAt(modelRow, 5));
            } else {
                selectedName.setText("Name: ");
                selectedAmount.setText("Amount: ");
                selectedPrice.setText("Price: ");
                selectedDate.setText("Buy Date: ");
            }
        });

        // ===== Selected Item Summary =====
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                String name = (String) model.getValueAt(modelRow, 0);
                int amount = (Integer) model.getValueAt(modelRow, 1);
                double price = (Double) model.getValueAt(modelRow, 2);
                Date date = (Date) model.getValueAt(modelRow, 3);
                selectedItemSummaryLabel.setText(
                        "Selected: " + name + " | Amount: " + amount + " | Price: $" + price + " | Date: " + date
                );
            } else {
                selectedItemSummaryLabel.setText("");
            }
        });
    }

    // ===== Load table data from DB =====
    private void loadTableData() {
        model.setRowCount(0); // clear table

        ResultSet result = getPurchases();

        try {
            while (result != null && result.next()) {
                // Add hidden columns
                Object[] row = {
                    result.getString("name"),
                    result.getInt("amount"),
                    result.getDouble("supplier_price"),
                    result.getDate("buy_date"),
                    result.getString("supplier_name"), // hidden
                    result.getString("supplier_contact") // hidden
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== Apply search + date filter =====
    private void applyFilter(JTextField searchField, JTextField fromDateField, JTextField toDateField) {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        String searchText = searchField.getText();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 0)); // Item Name column
        }

        String fromText = fromDateField.getText();
        String toText = toDateField.getText();
        if (!fromText.isEmpty() && !toText.isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    String date = (String) entry.getStringValue(3);
                    return date.compareTo(fromText) >= 0 && date.compareTo(toText) <= 0;
                }
            });
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // ===== Overall Summary =====
    private void updateOverallSummary() {
        int totalAmount = 0;
        double totalSpent = 0;

        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i); // map visible row to model
            int amount = (Integer) model.getValueAt(modelRow, 1);
            double price = (Double) model.getValueAt(modelRow, 2);
            totalAmount += amount;
            totalSpent += amount * price;
        }

        overallSummaryLabel.setText("Total Items: " + totalAmount + " | Total Spent: $" + totalSpent);
    }

    // ===== Database Access =====
    private static void getConnection() {
        Properties props = new Properties();
        var envFile = Paths.get(".env").toAbsolutePath().toString();
        try (FileInputStream inputStream = new FileInputStream(envFile)) {
            props.load(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            return;
        }
        String databaseName = props.getProperty("DATABASE_NAME");
        String databaseUser = props.getProperty("DATABASE_USER");
        String databasePassword = props.getProperty("DATABASE_PASSWORD");
        String databaseUrl = String.format(props.getProperty("DATABASE_URL") + "%s", databaseName);

        try {
            conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private static ResultSet getPurchases() {
        getConnection();
        try {
            Statement stmt = conn.createStatement();
            String sql = """
                    SELECT i.name, p.amount, p.supplier_price, p.buy_date, p.supplier_name, p.supplier_contact
                    FROM purchase p
                    JOIN inventory i ON p.item_id = i.id;
                    """;
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    // ===== SimpleDocumentListener Helper =====
    private static class SimpleDocumentListener implements DocumentListener {

        private final Runnable runnable;

        public SimpleDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }
    }

    private void showAddPurchaseDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField itemIdField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField buyDateField = new JTextField(); // YYYY-MM-DD
        JTextField supplierNameField = new JTextField();
        JTextField supplierContactField = new JTextField();

        panel.add(new JLabel("Item ID:"));
        panel.add(itemIdField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Buy Date (YYYY-MM-DD):"));
        panel.add(buyDateField);
        panel.add(new JLabel("Supplier Name:"));
        panel.add(supplierNameField);
        panel.add(new JLabel("Supplier Contact:"));
        panel.add(supplierContactField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add New Purchase", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int itemId = Integer.parseInt(itemIdField.getText());
                int amount = Integer.parseInt(amountField.getText());
                double price = Double.parseDouble(priceField.getText());
                String buyDate = buyDateField.getText();
                String supplierName = supplierNameField.getText();
                String supplierContact = supplierContactField.getText();

                insertPurchase(itemId, amount, price, buyDate, supplierName, supplierContact);

                // Reload table and update summary
                loadTableData();
                updateOverallSummary();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount, Price, and Item ID must be numeric.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void insertPurchase(int itemId, int amount, double price, String buyDate, String supplierName, String supplierContact) {
        try {
            getConnection(); // open DB connection

            // Start transaction
            conn.setAutoCommit(false);

            // 1️⃣ Insert into purchases table
            String insertSql = """
            INSERT INTO purchase
                (item_id, amount, supplier_price, buy_date, supplier_name, supplier_contact)
                VALUES (?, ?, ?, ?, ?, ?)
        """;
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, itemId);
                insertStmt.setInt(2, amount);
                insertStmt.setDouble(3, price);
                LocalDate localDate = LocalDate.parse(buyDate); // "YYYY-MM-DD"
                LocalDateTime localDateTime = localDate.atStartOfDay(); // 00:00 time
                Timestamp timestamp = Timestamp.valueOf(localDateTime);
                insertStmt.setTimestamp(4, timestamp);
                insertStmt.setString(5, supplierName);
                insertStmt.setString(6, supplierContact);
                insertStmt.executeUpdate();
            }

            // 2️⃣ Update inventory table
            String updateInventorySql = """
            UPDATE inventory
            SET amount = amount + ?
            WHERE id = ?
        """;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateInventorySql)) {
                updateStmt.setInt(1, amount);
                updateStmt.setInt(2, itemId);
                int rows = updateStmt.executeUpdate();

                // Optional: if inventory item doesn't exist, alert user
                if (rows == 0) {
                    JOptionPane.showMessageDialog(this, "Warning: Inventory item not found. Purchase added but inventory not updated.", "Inventory Missing", JOptionPane.WARNING_MESSAGE);
                }
            }

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Purchase added and inventory updated successfully!");
            loadTableData();
            updateOverallSummary();
        } catch (SQLException e) {
            try {
                conn.rollback(); // rollback if any error
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error adding purchase: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error closing connection: " + ex.getMessage());
            }
        }
    }
}
