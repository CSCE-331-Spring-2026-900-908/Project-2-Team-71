import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class Inventory extends JPanel {
    private static final String PANEL_TITLE = "Inventory";
    private static final String TABLE_QUERY = "SELECT * FROM INVENTORY;";

    private static Connection conn;
    private static DefaultTableModel tableModel;
    private static JTable inventoryTable;
    
    private static JLabel nameLabel = new JLabel("Name: -");
    private static JLabel amountLabel = new JLabel("Amount: -");
    private static JLabel supplierNameLabel = new JLabel("Supplier Name: -");
    private static JLabel supplierContactLabel = new JLabel("Supplier Contact: -");
    private static JLabel totalStatsLabel = new JLabel("Total Items: 0 | Total Value: $0.00");

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

    public Inventory(GUI screen) {
        GetConnection();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(15);
        JTextField fromDate = new JTextField("2024-01-01", 8);
        JTextField toDate = new JTextField("2024-12-31", 8);
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(new JLabel("Search Name:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("From:"));
        topPanel.add(fromDate);
        topPanel.add(new JLabel("To:"));
        topPanel.add(toDate);
        topPanel.add(refreshBtn);

        // --- 2. Center (The Table) ---
        String[] columns = {"Item Name", "Quantity", "Supplier Name", "Supplier Contact"};
        
        // MODIFIED: Override getColumnClass so Quantity (Index 1) sorts numerically
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Integer.class; 
                return String.class;
            }
        };

        inventoryTable = new JTable(tableModel);
        
        // ADDED: This enables the clickable header arrows
        inventoryTable.setAutoCreateRowSorter(true); 
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        // --- 3. Right Panel ---
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
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalStatsLabel);

        // --- LOGIC & EVENTS ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() { refreshData(searchField.getText()); }
        });

        refreshBtn.addActionListener(e -> refreshData(searchField.getText()));

        // MODIFIED: Added convertRowIndexToModel so selection works after sorting
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() != -1) {
                int viewRow = inventoryTable.getSelectedRow();
                int modelRow = inventoryTable.convertRowIndexToModel(viewRow);
                
                nameLabel.setText("Name: " + tableModel.getValueAt(modelRow, 0));
                amountLabel.setText("Amount: " + tableModel.getValueAt(modelRow, 1));
                supplierNameLabel.setText("Supplier Name: " + tableModel.getValueAt(modelRow, 2));
                supplierContactLabel.setText("Supplier Contact: " + tableModel.getValueAt(modelRow, 3));
            }
        });

        refreshData("");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
        splitPane.setDividerLocation(800);
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private static void refreshData(String filter) {
        if (conn == null) return;
        
        tableModel.setRowCount(0);
        int totalQty = 0;
        String query = "SELECT * FROM inventory WHERE name ILIKE ?;";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + filter + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("amount");
                String supplierName = rs.getString("supplier_name");
                String supplierContact = rs.getString("supplier_contact");

                // Note: The 'qty' is passed as an Integer object so it sorts correctly
                tableModel.addRow(new Object[]{name, qty, supplierName, supplierContact});
                totalQty += qty;
            }
            totalStatsLabel.setText(String.format("Total Items: %d | Total Value: $0.00", totalQty));
        } catch (SQLException e) {
            System.err.println("Query Error: " + e.getMessage());
        }
    }
}