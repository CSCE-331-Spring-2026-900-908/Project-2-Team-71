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

    // Reference line 259 for what this does
    private static final String TABLE_QUERY = """
        SELECT * FROM INVENTORY;
    """;

    // Reference line 92 for what this does
    private static final String[] COLUMNS = {
        "Column 1",
        "Column 2",
        "Column 3"
    };

    static Connection conn;
    private static DefaultTableModel tableModel;
    private static JTable inventoryTable;
    
    // Right panel labels
    private static JLabel nameLabel = new JLabel("Name: -");
    private static JLabel amountLabel = new JLabel("Amount: -");
    private static JLabel priceLabel = new JLabel("Price: -");
    private static JLabel dateLabel = new JLabel("Date: -");
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

        // --- 1. Top Panel (Search & Filters) ---
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
        String[] columns = {"Item Name", "Quantity", "Unit Price", "Date Added"};
        tableModel = new DefaultTableModel(columns, 0);
        inventoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        // --- 3. Right Panel (Details) ---
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(250, 0));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Selected Item Details"));

        detailPanel.add(nameLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(amountLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(priceLabel);
        detailPanel.add(Box.createVerticalStrut(10));
        detailPanel.add(dateLabel);

        // --- 4. Bottom Panel (Status/Totals) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalStatsLabel);

        // ===================== LOGIC & EVENTS =====================

        // Search as you type
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() { refreshData(searchField.getText()); }
        });

        // Manual Refresh
        refreshBtn.addActionListener(e -> refreshData(searchField.getText()));

        // Table Click Selection
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() != -1) {
                int row = inventoryTable.getSelectedRow();
                nameLabel.setText("Name: " + tableModel.getValueAt(row, 0));
                amountLabel.setText("Amount: " + tableModel.getValueAt(row, 1));
                priceLabel.setText("Price: $" + tableModel.getValueAt(row, 2));
                dateLabel.setText("Date: " + tableModel.getValueAt(row, 3));
            }
        });

        // Initial Load
        refreshData("");

        // Final Assembly
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
        splitPane.setDividerLocation(800);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        
    }
    private ResultSet getData() {

        GetConnection();

        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(TABLE_QUERY);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return null;
    }

    private static void refreshData(String filter) {
        if (conn == null) return;
        
        tableModel.setRowCount(0);
        double totalValue = 0;
        int totalQty = 0;

        // Uses ILIKE for case-insensitive partial name matching
        String query = "SELECT * FROM INVENTORY;";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + filter + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("amount");
                double price = rs.getDouble("price");
                Date date = rs.getDate("buy_date");

                tableModel.addRow(new Object[]{name, qty, price, date});
                
                totalQty += qty;
                totalValue += (qty * price);
            }

            totalStatsLabel.setText(String.format("Total Items: %d | Total Value: $%.2f", totalQty, totalValue));

        } catch (SQLException e) {
            System.err.println("Query Error: " + e.getMessage());
        }
    }
}