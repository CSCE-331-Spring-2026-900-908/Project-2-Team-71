
import java.awt.*;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TransactionsPanel extends JPanel {

    // ===================== CONFIG SECTION =====================
    // Query to get the list of transactions for the left table
    private static final String TABLE_QUERY = """
        SELECT id, customer_name, total_price 
        FROM transactions 
        ORDER BY id DESC;
    """;

    private static final String RECEIPT_DETAIL_QUERY = """
    SELECT f.name AS item_name,
           f.price AS price,
           ftr.modifiers AS details
    FROM food_to_receipt ftr
    JOIN food f ON ftr.food_id = f.id
    WHERE ftr.receipt_id = ?

    UNION ALL

    SELECT d.name AS item_name,
           d.price AS price,
           CONCAT(
               'Ice: ', dtr.ice,
               ', Sweetness: ', dtr.sweetness,
               ', Milk: ', dtr.milk,
               ', Boba: ', dtr.boba,
               ', Popping: ', dtr.popping_boba
           ) AS details
    FROM drink_to_receipt dtr
    JOIN drink d ON dtr.drink_id = d.id
    WHERE dtr.receipt_id = ?
""";

    private static final String[] COLUMNS = {
        "Transaction ID", "Customer Name", "Total"
    };

    // ==========================================================
    private final GUI gui;
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

        int receiptId = (int) transactionId;

        // Update customer label
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        customerLabel.setText("Customer Name: " + model.getValueAt(modelRow, 1));

        receiptArea.setText("");

        try (PreparedStatement stmt = conn.prepareStatement(RECEIPT_DETAIL_QUERY)) {

            stmt.setInt(1, receiptId);
            stmt.setInt(2, receiptId);

            ResultSet rs = stmt.executeQuery();

            receiptArea.append("Receipt #" + receiptId + "\n");
            receiptArea.append("----------------------------------\n");

            while (rs.next()) {
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");

                String details = rs.getString("details");

                receiptArea.append(String.format("%-20s $%.2f\n", name, price));

                if (details != null && !details.isBlank()) {
                    receiptArea.append("   -> " + details + "\n");
                }

                receiptArea.append(String.format("%-20s $%.2f\n", name, price));
            }

            receiptArea.append("----------------------------------\n");
            receiptArea.append(String.format("TOTAL: $%.2f\n", model.getValueAt(modelRow, 2)));

        } catch (SQLException e) {
            receiptArea.setText("Error loading receipt: " + e.getMessage());
        }
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
                model.addRow(new Object[]{rs.getObject(1), rs.getObject(2), rs.getObject(3)});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private static void getConnection() {
        if (conn != null) {
            return;
        }
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

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update();
        }
    }
}
