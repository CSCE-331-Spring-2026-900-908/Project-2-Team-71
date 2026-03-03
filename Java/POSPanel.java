
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class POSPanel extends JPanel {

    private final GUI gui;
    private Connection conn;

    // customer lookup UI
    private JTextField customerLookupField;
    private JButton customerFindButton;

    // customer display labels (values that update)
    private JLabel idValue;
    private JLabel nameValue;
    private JLabel phoneValue;
    private JLabel pointsValue;

    // cashier sign-in (not a login)
    private JTextField cashierIdField;
    private JButton cashierSetButton;
    private JLabel cashierStatusLabel;

    //mani meun button
    private JButton returnToMainButton;

    private Integer cashierId = null;
    private String cashierName = null;
    private String paymentMethod = null;
    private static final double TAX_RATE = 0.0825; // 8.25%
    private double taxAmount = 0.0;

    // discount
    private double discountRate = 0.0;     // stored as fraction: 0.25 = 25%
    private String discountType = null;    // e.g., "Student"
    private JLabel discountStatusLabel;    // shows applied discount in UI
    private JButton applyDiscountButton;

    // total display
    private double finalTotal = 0.0;

    public POSPanel(GUI gui) {
        this.gui = gui;
        getConnection();

        setLayout(new BorderLayout(10, 10));

        // Example: left panel contains customer area + cart + checkout
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    private void getConnection() {

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

    /////////////  db connection  /////////////////////////////////////////////////////////
    private void ensureConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                getConnection();
            }
        } catch (SQLException e) {
            getConnection();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    /////////  top left cusomer look up  //////////////////////////////////////////////////
    private JComponent buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setPreferredSize(new Dimension(350, 0));

        // Customer section on top (if you already have it)
        left.add(buildCustomerSection(), BorderLayout.NORTH);

        // Cart table in center
        left.add(buildCartSection(), BorderLayout.CENTER);

        // Total + checkout at bottom
        left.add(buildCheckoutSection(), BorderLayout.SOUTH);

        return left;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    //////////////  top right customer info //////////////////////////////////////////////////
    private JComponent buildCustomerSection() {
        JPanel customer = new JPanel();
        customer.setLayout(new BoxLayout(customer, BoxLayout.Y_AXIS));
        customer.setBorder(BorderFactory.createTitledBorder("Customer"));

        // Lookup row
        JPanel lookupRow = new JPanel(new BorderLayout(5, 5));
        customerLookupField = new JTextField();
        customerFindButton = new JButton("Find");

        lookupRow.add(new JLabel("Lookup:"), BorderLayout.WEST);
        lookupRow.add(customerLookupField, BorderLayout.CENTER);
        lookupRow.add(customerFindButton, BorderLayout.EAST);

        // Value labels (start as "-")
        idValue = new JLabel("-");
        nameValue = new JLabel("-");
        phoneValue = new JLabel("-");
        pointsValue = new JLabel("-");

        // Layout
        customer.add(lookupRow);
        customer.add(Box.createVerticalStrut(8));

        customer.add(new JLabel("ID"));
        customer.add(idValue);

        customer.add(new JLabel("Customer Name"));
        customer.add(nameValue);

        customer.add(new JLabel("Phone number"));
        customer.add(phoneValue);

        customer.add(new JLabel("Points"));
        customer.add(pointsValue);

        // Events: click Find or press Enter in the box
        customerFindButton.addActionListener(e -> lookupCustomer());
        customerLookupField.addActionListener(e -> lookupCustomer());

        return customer;
    }

    private void lookupCustomer() {
        String key = customerLookupField.getText().trim();
        if (key.isEmpty()) {
            return;
        }

        ensureConnection();

        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection (conn is null).");
            return;
        }

        String sql = """
            SELECT id, name, phone, points
            FROM customer
            WHERE phone LIKE ?
               OR name LIKE ?
            ORDER BY id
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idValue.setText(String.valueOf(rs.getInt("id")));
                    nameValue.setText(rs.getString("name"));
                    phoneValue.setText(rs.getString("phone"));
                    pointsValue.setText(String.valueOf(rs.getInt("points")));
                } else {
                    clearCustomerDisplay();
                    JOptionPane.showMessageDialog(this, "Customer not found.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }

    private void clearCustomerDisplay() {
        idValue.setText("-");
        nameValue.setText("-");
        phoneValue.setText("-");
        pointsValue.setText("-");
    }
    //////////////////////////////////////////////////////////////////////////////////////////

    /////////////  left checkout section  ////////////////////////////////////////////////
    // Cart table
    private DefaultTableModel cartModel;
    private JTable cartTable;

    // Total + checkout
    private JLabel totalLabel;
    private JButton checkoutButton;

    // Running total
    private double total = 0.0;

    private JComponent buildCartSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Order"));

        cartModel = new DefaultTableModel(
                new Object[]{"Type", "Item ID", "Item", "Options", "Price"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        cartTable = new JTable(cartModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildCheckoutSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.add(new JLabel("Total:"), BorderLayout.WEST);

        totalLabel = new JLabel("$0.00", SwingConstants.RIGHT);
        totalRow.add(totalLabel, BorderLayout.EAST);

        //////////// disocunt logic ///////////////////////
        applyDiscountButton = new JButton("Apply Discount");
        applyDiscountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyDiscountButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        applyDiscountButton.addActionListener(e -> applyDiscount());

        discountStatusLabel = new JLabel("Discount: (none)");
        discountStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(applyDiscountButton);
        panel.add(Box.createVerticalStrut(5));
        panel.add(discountStatusLabel);
        panel.add(Box.createVerticalStrut(10));
        //////////////////////////////////////////////

        checkoutButton = new JButton("Check out");
        checkoutButton.setFont(checkoutButton.getFont().deriveFont(20f));
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        checkoutButton.addActionListener(e -> onCheckout());

        panel.add(totalRow);
        panel.add(Box.createVerticalStrut(10));
        panel.add(checkoutButton);

        /////// chashie login///////
        panel.add(Box.createVerticalStrut(10));

        JPanel cashierRow = new JPanel(new BorderLayout(5, 5));
        cashierIdField = new JTextField();
        cashierSetButton = new JButton("Set");

        cashierRow.add(new JLabel("Cashier ID:"), BorderLayout.WEST);
        cashierRow.add(cashierIdField, BorderLayout.CENTER);
        cashierRow.add(cashierSetButton, BorderLayout.EAST);

        cashierStatusLabel = new JLabel("Cashier: (not set)");
        cashierStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(cashierRow);
        panel.add(Box.createVerticalStrut(5));
        panel.add(cashierStatusLabel);

        // Enter key or button click
        cashierSetButton.addActionListener(e -> setCashierFromId());
        cashierIdField.addActionListener(e -> setCashierFromId());
        /////////////////////
        
        //////////// main meu button /////
        panel.add(Box.createVerticalStrut(10));

        returnToMainButton = new JButton("Return to Main Menu");
        returnToMainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnToMainButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(returnToMainButton);
        // Action
        returnToMainButton.addActionListener(e -> {
            cartModel.setRowCount(0);
            total = 0.0;
            updateTotalLabel();
            gui.showScreen("MAIN");
        });
        ///////////////////////////////////

        return panel;
    }

    private void applyDiscount() {
        String typed = JOptionPane.showInputDialog(this, "Enter discount type (e.g., Student):");
        if (typed == null) {
            return;
        }
        typed = typed.trim();
        if (typed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Discount type cannot be empty.");
            return;
        }

        ensureConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection.");
            return;
        }

        String sql = "SELECT type, amount FROM discount WHERE type ILIKE ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typed);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "No discount found for type: " + typed);
                    return;
                }

                String dbType = rs.getString("type");
                double amt = rs.getDouble("amount");

                // Robust handling:
                // if DB stores 25 => treat as 25% => 0.25
                // if DB stores 0.25 => treat as 25% already
                double rate = (amt >= 1.0) ? (amt / 100.0) : amt;

                // Clamp to sane range
                if (rate < 0.0) {
                    rate = 0.0;
                }
                if (rate > 1.0) {
                    rate = 1.0;
                }

                discountType = dbType;
                discountRate = rate;

                discountStatusLabel.setText(
                        "Discount: " + discountType + " (" + String.format("%.0f", discountRate * 100) + "%)"
                );

                recalcAndUpdateTotalLabel();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Discount lookup error: " + ex.getMessage());
        }
    }

    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private void recalcAndUpdateTotalLabel() {
        // 'total' is your running subtotal from cart items
        double subtotal = total;

        double discountAmount = subtotal * discountRate;
        double discountedSubtotal = subtotal - discountAmount;

        taxAmount = round2(discountedSubtotal * TAX_RATE);
        finalTotal = round2(discountedSubtotal + taxAmount);

        totalLabel.setText(String.format("$%.2f", finalTotal));
    }

    public void addToCartWithId(String type, int id, String itemName, String options, double price) {

        cartModel.addRow(new Object[]{
            type,
            id,
            itemName,
            options,
            String.format("$%.2f", price)
        });

        total += price;
        recalcAndUpdateTotalLabel();
    }

    private int insertReceiptRow() throws SQLException {
        String sql = """
        INSERT INTO receipt (purchase_date, customer_id, cashier_id, tax, payment_method, discount)
        VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)
        RETURNING id
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int customerId = 0;
            String custText = idValue.getText().trim();
            if (!custText.equals("-") && custText.matches("\\d+")) {
                customerId = Integer.parseInt(custText);
            }

            ps.setInt(1, customerId);
            ps.setInt(2, cashierId != null ? cashierId : 0);
            ps.setDouble(3, taxAmount);
            ps.setString(4, paymentMethod);
            ps.setDouble(5, discountRate);

            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt("id");
        }
    }

    private void insertCartItems(int receiptId) throws SQLException {

        String foodSql = "INSERT INTO food_to_receipt (receipt_id, food_id) VALUES (?, ?)";
        String drinkSql = """
        INSERT INTO drink_to_receipt 
        (receipt_id, drink_id, ice, sweetness, milk, boba, popping_boba)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        for (int i = 0; i < cartModel.getRowCount(); i++) {

            String type = (String) cartModel.getValueAt(i, 0);
            int itemId = (int) cartModel.getValueAt(i, 1);
            String options = (String) cartModel.getValueAt(i, 3);

            if (type.equals("Food")) {

                try (PreparedStatement ps = conn.prepareStatement(foodSql)) {
                    ps.setInt(1, receiptId);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                }

            } else if (type.equals("Drink")) {

                // Extract options
                String ice = extractOption(options, "Ice:");
                int sweet = Integer.parseInt(extractOption(options, "Sweet:").replace("%", ""));
                String milk = extractOption(options, "Milk:");
                boolean boba = options.contains("Boba: Yes");
                boolean popping = options.contains("Pop: Yes");

                try (PreparedStatement ps = conn.prepareStatement(drinkSql)) {
                    ps.setInt(1, receiptId);
                    ps.setInt(2, itemId);
                    ps.setString(3, ice);
                    ps.setInt(4, sweet);
                    ps.setString(5, milk);
                    ps.setBoolean(6, boba);
                    ps.setBoolean(7, popping);
                    ps.executeUpdate();
                }
            }
        }
    }

    private String extractOption(String options, String key) {
        int start = options.indexOf(key);
        if (start == -1) {
            return "";
        }
        start += key.length();
        int end = options.indexOf(",", start);
        if (end == -1) {
            end = options.length();
        }
        return options.substring(start, end).trim();
    }

    private void setCashierFromId() {
        String text = cashierIdField.getText().trim();

        if (!text.matches("\\d+")) {
            cashierId = null;
            cashierName = null;
            cashierStatusLabel.setText("Invalid cashier ID");
            JOptionPane.showMessageDialog(this, "Invalid cashier ID (must be a number).");
            return;
        }

        int id = Integer.parseInt(text);

        ensureConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection.");
            return;
        }

        String sql = "SELECT id, name FROM cashier WHERE id = ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cashierId = rs.getInt("id");
                    cashierName = rs.getString("name");
                    cashierStatusLabel.setText("Cashier: " + cashierName + " (ID " + cashierId + ")");
                } else {
                    cashierId = null;
                    cashierName = null;
                    cashierStatusLabel.setText("Invalid cashier ID");
                    JOptionPane.showMessageDialog(this, "No cashier found with ID " + id);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cashier lookup error: " + ex.getMessage());
        }
    }

    private void updateTotalLabel() {
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void onCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        recalcAndUpdateTotalLabel(); //ensure ttoal is most recpet update

        // Calculate tax
        taxAmount = total * TAX_RATE;
        double finalTotal = total + taxAmount;

        String method = promptForPaymentMethod();
        if (method == null) {
            return; // user cancelled / closed / invalid
        }

        this.paymentMethod = method;

        double subtotal = total;
        double discountAmount = subtotal * discountRate;
        double discountedSubtotal = subtotal - discountAmount;
        taxAmount = round2(discountedSubtotal * TAX_RATE);
        finalTotal = round2(discountedSubtotal + taxAmount);

        JOptionPane.showMessageDialog(this,
                "Subtotal: $" + String.format("%.2f", subtotal)
                + "\nDiscount: $" + String.format("%.2f", discountAmount)
                + (discountType != null ? " (" + discountType + ")" : "")
                + "\nTax: $" + String.format("%.2f", taxAmount)
                + "\nTotal: $" + String.format("%.2f", finalTotal)
                + "\n\nPayment method: " + method
        );

        try {
            conn.setAutoCommit(false);

            int receiptId = insertReceiptRow();
            insertCartItems(receiptId);

            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }

        // Clear cart after payment
        cartModel.setRowCount(0);
        total = 0.0;
        discountRate = 0.0;
        discountType = null;
        taxAmount = 0.0;
        finalTotal = 0.0;
        this.paymentMethod = null;

        if (discountStatusLabel != null) {
            discountStatusLabel.setText("Discount: (none)");
        }
        totalLabel.setText("$0.00");
    }

    private String promptForPaymentMethod() {
        String[] options = {"Cash", "Card", "Other", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Select a payment method:",
                "Payment Method",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1] // default = Card
        );

        if (choice == 3 || choice == JOptionPane.CLOSED_OPTION) { // Cancel or X
            return null;
        }

        if (choice == 2) { // Other
            String typed = JOptionPane.showInputDialog(this, "Enter payment method:");
            if (typed == null) {
                return null; // user canceled

            }
            typed = typed.trim();
            if (typed.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Payment method cannot be empty.");
                return null;
            }
            return typed;
        }

        // Cash or Card
        return options[choice];
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /////////////////////// right section //////////////////////////////////////////////////
    private JComponent buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout(10, 10));

        right.add(buildDrinkGrid(), BorderLayout.CENTER);
        right.add(buildSearchPanel(), BorderLayout.SOUTH);

        return right;
    }
    ///////////////////////////////////////////////////////////////////////////////////////

    /////////////////  middle/right bottom item search bar  ////////////////////////////////
    // Search UI
    private JTextField searchField;
    private JButton searchButton;

    private DefaultTableModel resultsModel;
    private JTable resultsTable;

    private JComponent buildSearchPanel() {
        //test line
        System.out.println("buildSearchPanel() running");

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setPreferredSize(new Dimension(0, 280));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(5, 5));
        searchBar.add(new JLabel("Search:"), BorderLayout.WEST);

        searchField = new JTextField();
        searchButton = new JButton("🔍");

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);

        // Results table
        resultsModel = new DefaultTableModel(new Object[]{"Type", "ID", "Name", "Price", "Add"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // only Add is clickable
            }
        };

        resultsTable = new JTable(resultsModel);
        resultsTable.getColumn("Add").setCellRenderer(new ButtonRenderer());
        resultsTable.getColumn("Add").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(resultsTable);

        right.add(searchBar, BorderLayout.NORTH);
        right.add(scroll, BorderLayout.CENTER);

        // Actions
        searchButton.addActionListener(e -> searchMenu());
        searchField.addActionListener(e -> searchMenu()); // Enter key

        return right;
    }

    private void searchMenu() {
        String key = searchField.getText().trim();
        if (key.isEmpty()) {
            return;
        }

        ensureConnection();

        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection.");
            return;
        }

        resultsModel.setRowCount(0);

        boolean isNumber = key.matches("\\d+");

        // --- Change these table names if yours are different ---
        String sqlById = """
            SELECT 'Drink' AS type, id, name, price FROM drink WHERE id = ?
            UNION ALL
            SELECT 'Food'  AS type, id, name, price FROM food   WHERE id = ?
            ORDER BY type, name
            LIMIT 50
        """;

        String sqlByName = """
            SELECT 'Drink' AS type, id, name, price FROM drink WHERE name ILIKE ?
            UNION ALL
            SELECT 'Food'  AS type, id, name, price FROM food WHERE name ILIKE ?
            ORDER BY type, name
            LIMIT 50
        """;

        try (PreparedStatement ps = conn.prepareStatement(isNumber ? sqlById : sqlByName)) {

            if (isNumber) {
                int id = Integer.parseInt(key);
                ps.setInt(1, id);
                ps.setInt(2, id);
            } else {
                String pat = "%" + key + "%";
                ps.setString(1, pat);
                ps.setString(2, pat);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type"); // Drink/Food
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");

                    resultsModel.addRow(new Object[]{
                        type,
                        id,
                        name,
                        String.format("$%.2f", price),
                        "Add"
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage());
        }
    }

    private double getDrinkBasePrice(String drinkName) {
        ensureConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection.");
            return 0.0;
        }

        String sql = "SELECT price FROM drink WHERE name = ? LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, drinkName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble("price");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Price lookup error: " + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, "No price found for: " + drinkName);
        return 0.0;
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value == null ? "Add" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {

        private final JButton button = new JButton();
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            button.setText("Add");
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                int id = (int) resultsModel.getValueAt(row, 1);
                String name = (String) resultsModel.getValueAt(row, 2);
                String priceText = (String) resultsModel.getValueAt(row, 3);
                double price = Double.parseDouble(priceText.replace("$", ""));

                addToCartWithId("Food", id, name, "", price);
            }
            clicked = false;
            return "Add";
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// top right drink icons ///////////////////////////////
    private JComponent buildDrinkGrid() {

        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setBorder(BorderFactory.createTitledBorder("Drinks"));

        ensureConnection();
        if (conn == null) {
            grid.add(new JLabel("No DB connection"));
            return grid;
        }

        String sql = "SELECT id, name, price FROM drink ORDER BY name";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int drinkId = rs.getInt("id");
                String drinkName = rs.getString("name");
                double price = rs.getDouble("price");

                JButton btn = new JButton(
                        "<html><center>" + drinkName + "<br>$" + String.format("%.2f", price) + "</center></html>"
                );

                btn.setPreferredSize(new Dimension(150, 100));

                // IMPORTANT: pass ID instead of name
                btn.addActionListener(e -> openCustomizeDialog(drinkId, drinkName, price));

                grid.add(btn);
            }

        } catch (SQLException e) {
            grid.add(new JLabel("Error loading drinks: " + e.getMessage()));
        }

        return grid;
    }

    private void openCustomizeDialog(int drinkId, String drinkName, double basePrice) {

        JComboBox<String> iceBox = new JComboBox<>(new String[]{"No Ice", "Less Ice", "Normal Ice"});
        JComboBox<String> sweetBox = new JComboBox<>(new String[]{"0%", "50%", "100%"});
        JComboBox<String> milkBox = new JComboBox<>(new String[]{"Cow", "Oat", "Almond"});

        JCheckBox bobaBox = new JCheckBox("Boba");
        JCheckBox poppingBox = new JCheckBox("Popping Boba");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Ice:"));
        panel.add(iceBox);
        panel.add(new JLabel("Sweetness:"));
        panel.add(sweetBox);
        panel.add(new JLabel("Milk:"));
        panel.add(milkBox);
        panel.add(new JLabel(""));
        panel.add(bobaBox);
        panel.add(new JLabel(""));
        panel.add(poppingBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Customize: " + drinkName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {

            String ice = (String) iceBox.getSelectedItem();
            String sweet = (String) sweetBox.getSelectedItem();
            String milk = (String) milkBox.getSelectedItem();
            boolean boba = bobaBox.isSelected();
            boolean popping = poppingBox.isSelected();

            String options = String.format(
                    "Ice: %s, Sweet: %s, Milk: %s, Boba: %s, Pop: %s",
                    ice, sweet, milk,
                    boba ? "Yes" : "No",
                    popping ? "Yes" : "No"
            );

            double finalPrice = basePrice
                    + (boba ? 0.50 : 0.0)
                    + (popping ? 0.75 : 0.0);

            // IMPORTANT: store drinkId for later DB insert
            addToCartWithId("Drink", drinkId, drinkName, options, finalPrice);
        }
    }


////////////////////////////////////////////////////////////////////////////////////////


} //// end of POSScreen
