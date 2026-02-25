
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PanelTemplate extends JPanel {

    // ===================== CONFIG SECTION =====================
    // 🔹 EDIT THESE FOR EACH PANEL
    private static final String PANEL_TITLE = "Panel Name";

    // Reference line 259 for what this does
    private static final String TABLE_QUERY = """
        SELECT id, column1, column2
        FROM your_table;
    """;

    // Reference line 92 for what this does
    private static final String[] COLUMNS = {
        "Column 1",
        "Column 2",
        "Column 3"
    };

    // ==========================================================
    private GUI gui;
    private static Connection conn;

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel overallSummaryLabel;
    private JLabel selectedItemSummaryLabel;

    public PanelTemplate(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());

        createTopBar();
        createTableSection();
        createSummarySection();

        loadTableData();
        updateOverallSummary();
    }

    // ===================== TOP BAR =====================
    private void createTopBar() {

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topBar.add(backButton);

        JTextField searchField = new JTextField(12);
        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);

        JButton refreshButton = new JButton("Refresh");
        topBar.add(refreshButton);

        JButton addButton = new JButton("Add");
        topBar.add(addButton);

        add(topBar, BorderLayout.NORTH);

        refreshButton.addActionListener(e -> {
            loadTableData();
            applyFilter(searchField);
            updateOverallSummary();
        });

        searchField.getDocument().addDocumentListener(
                new SimpleDocumentListener(() -> applyFilter(searchField))
        );

        addButton.addActionListener(e -> showAddDialog());
    }

    // ===================== TABLE =====================
    private void createTableSection() {

        model = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel selectedPanel = new JPanel();
        selectedPanel.setLayout(new BoxLayout(selectedPanel, BoxLayout.Y_AXIS));
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Selected Item"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollPane,
                selectedPanel
        );

        splitPane.setDividerLocation(600);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                selectedItemSummaryLabel.setText("Selected row: " + modelRow);
            }
        });
    }

    // ===================== SUMMARY =====================
    private void createSummarySection() {

        JPanel summaryPanel = new JPanel(new GridLayout(2, 1));

        overallSummaryLabel = new JLabel();
        selectedItemSummaryLabel = new JLabel();

        summaryPanel.add(overallSummaryLabel);
        summaryPanel.add(selectedItemSummaryLabel);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    private void updateOverallSummary() {

        int totalRows = table.getRowCount();
        overallSummaryLabel.setText("Total Records: " + totalRows);
    }

    // ===================== FILTER =====================
    private void applyFilter(JTextField searchField) {

        String text = searchField.getText();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    // ===================== LOAD DATA =====================
    private void loadTableData() {

        model.setRowCount(0);

        ResultSet result = getData();

        try {
            while (result != null && result.next()) {

                // 🔹 EDIT THIS MAPPING FOR YOUR TABLE
                Object[] row = {
                    result.getObject(1),
                    result.getObject(2),
                    result.getObject(3)
                };

                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===================== ADD DIALOG =====================
    private void showAddDialog() {

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();

        panel.add(new JLabel("Field 1:"));
        panel.add(field1);
        panel.add(new JLabel("Field 2:"));
        panel.add(field2);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Record",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            runTransaction(connection -> {

                // 🔹 EDIT THIS INSERT QUERY
                String insertSql = """
                    INSERT INTO your_table (column1, column2)
                    VALUES (?, ?)
                """;

                try (PreparedStatement stmt
                        = connection.prepareStatement(insertSql)) {

                    stmt.setString(1, field1.getText());
                    stmt.setString(2, field2.getText());
                    stmt.executeUpdate();
                }
            });

            loadTableData();
            updateOverallSummary();
        }
    }

    // ===================== DATABASE =====================
    private static void getConnection() {

        Properties props = new Properties();
        var envFile = Paths.get(".env").toAbsolutePath().toString();

        try (FileInputStream inputStream = new FileInputStream(envFile)) {
            props.load(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            return;
        }

        try {
            String databaseUrl = props.getProperty("DATABASE_URL") + props.getProperty("DATABASE_NAME");
            String databaseUser = props.getProperty("DATABASE_USER");
            String databasePassword = props.getProperty("DATABASE_PASSWORD");

            conn = DriverManager.getConnection(
                    databaseUrl,
                    databaseUser,
                    databasePassword
            );

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ResultSet getData() {

        getConnection();

        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(TABLE_QUERY);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return null;
    }

    private void runTransaction(SQLRunnable runnable) {

        try {
            getConnection();
            conn.setAutoCommit(false);

            runnable.run(conn);

            conn.commit();
            conn.setAutoCommit(true);

        } catch (Exception e) {

            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }

            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface SQLRunnable {

        void run(Connection conn) throws Exception;
    }

    // ===================== DOCUMENT LISTENER =====================
    private static class SimpleDocumentListener implements DocumentListener {

        private Runnable runnable;

        public SimpleDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }
    }
}
