
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
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() != -1) {
                int modelRow = inventoryTable.convertRowIndexToModel(inventoryTable.getSelectedRow());
                nameLabel.setText("Name: " + tableModel.getValueAt(modelRow, 1));
                amountLabel.setText("Amount: " + tableModel.getValueAt(modelRow, 2));
                supplierNameLabel.setText("Supplier Name: " + tableModel.getValueAt(modelRow, 3));
                supplierContactLabel.setText("Supplier Contact: " + tableModel.getValueAt(modelRow, 4));
            }
        });

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

        confirmBtn.addActionListener(e -> {
            saveTableChanges();
            dataChanged = false;
            confirmBtn.setVisible(false);
            JOptionPane.showMessageDialog(this, "Database Updated!");
        });

        tableModel.addTableModelListener(e -> {
            if (isEditing && e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                dataChanged = true;
                confirmBtn.setVisible(true);
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                refreshData(searchField.getText());
            }
        });

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

    private void deleteFromDatabase(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
        }
    }

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
