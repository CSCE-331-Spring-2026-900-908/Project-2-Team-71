import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TransactionsPanel extends JPanel {

    // ===================== CONFIG SECTION =====================
    private static final String PANEL_TITLE = "Transactions";

    // Query to get the list of transactions for the left table
    private static final String TABLE_QUERY = """
        SELECT id, customer_name, total_price 
        FROM transactions 
        ORDER BY id DESC;
    """;

    private static final String[] COLUMNS = {
        "Transaction ID", "Customer Name", "Total"
    };

    // ==========================================================
    private GUI gui;
    private static Connection conn;

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    // Right side components (Detail View)
    private JLabel customerLabel;
    private JLabel pointsLabel;
    private JTextArea receiptArea;

    public TransactionsPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());

        createTopBar();
        createMainContent(); 
        
        loadTableData();
    }

    private void createTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topBar.add(backButton);

        JTextField searchField = new JTextField(15);
        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);

        add(topBar, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(
            (SimpleDocumentListener) () -> applyFilter(searchField)
        );
    }

    private void createMainContent() {
        // Left Table
        model = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane tableScroll = new JScrollPane(table);

        // Right Detail Panel (Figma Gray Area)
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailPanel.setBackground(Color.LIGHT_GRAY);

        customerLabel = new JLabel("Customer Name: -");
        pointsLabel = new JLabel("Points: -");
        receiptArea = new JTextArea(15, 20);
        receiptArea.setEditable(false);

        detailPanel.add(customerLabel);
        detailPanel.add(pointsLabel);
        detailPanel.add(new JScrollPane(receiptArea));
        
        JButton printButton = new JButton("Print Receipt");
        detailPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailPanel.add(printButton);

        // Split Pane to hold both
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detailPanel);
        splitPane.setDividerLocation(400);
        add(splitPane, BorderLayout.CENTER);

        // Selection Logic: Update right side when a row is clicked
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                Object transactionId = model.getValueAt(modelRow, 0);
                loadTransactionDetails(transactionId);
            }
        });
    }

    private void loadTransactionDetails(Object transactionId) {
        // Here you would run a query like: 
        // SELECT * FROM transaction_items WHERE transaction_id = ?
        customerLabel.setText("Customer Name: " + model.getValueAt(table.getSelectedRow(), 1));
        receiptArea.setText("Items for Transaction #" + transactionId + "\n------------------\nItem 1... $0.00\nAdd-on...");
    }

    private void applyFilter(JTextField searchField) {
        String text = searchField.getText();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private void loadTableData() {
        model.setRowCount(0);
        getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(TABLE_QUERY)) {
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getObject(1), rs.getObject(2), rs.getObject(3) });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private static void getConnection() {
        if (conn != null) return; 
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(".env")) {
            props.load(in);
            String url = props.getProperty("DATABASE_URL") + props.getProperty("DATABASE_NAME");
            conn = DriverManager.getConnection(url, props.getProperty("DATABASE_USER"), props.getProperty("DATABASE_PASSWORD"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }
}
