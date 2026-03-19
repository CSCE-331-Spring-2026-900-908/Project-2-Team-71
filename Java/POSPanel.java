import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Creates the POS panel they system to place orders
 *
 * <p>Creates the panel cashiers can access and adds the 
 * ordering sytems, custom id function, cashier id funtion, 
 * drnk sleesion and item search bar</p>
 *
 * @author Julia Street
 * @version 1.0
 */
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

    /**
     * Populates all the object ont the point of sale page and creates the page layout
     *
     * <p>The interface is created and each section 
     * needed for a POS operateion is added in the correct place</p>
     *
     * @param gui the gui object all panel componenetsa are attached to
     */
    public POSPanel(GUI gui) {
        this.gui = gui;
        getConnection();

        setLayout(new BorderLayout(10, 10));

        // Example: left panel contains customer area + cart + checkout
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    /**
     * Establishes a connection to the database using credentials stored in a .env file.
     *
     * <p>This method reads database login info from
     * a .env file and uses them to create a connection </p>
     *
     * @throws IOException if there is an issue reading the .env file (handled internally)
     * @throws SQLException if a database access error occurs (handled internally)
     */
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

    /**
     * Check if the database is connected and runs get connection if not
     */
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

    /**
     * Builds and returns the left panel of the POS interface.
     *
     * <p>Organizes teh panel into three main sections:
     * a customer information section at the top, a cart display in the center,
     * and a checkout section at the bottom.</p>
     *
     * @return an item representing the fully constructed left panel
     */
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

    /**
     * Builds and returns the customer information section of the POS interface.
     *
     * <p>This section includes a search field and button for searching customers,
     * and labels to display customer info: ID, name, phone number,
     * and reward points.</p>
     *
     * @return a item representing the customer section panel
     */
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

    /**
     * Searches for a customer in the database using the input from the search field.
     *
     * <p>If there is a connection to the database. then the custoer info
     *  is reterived from the database to be display i the customer section</p>
     */
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

    /**
     * Sets the cutomer display back t null characters
     */
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

    /**
     * Builds and returns the cart/checkout section of the POS interface.
     *
     * <p>This section contains a table that displays items currently in the order,
     * including their type, item ID, name, selected options, and price.</p>
     *
     * @return an item representing the cart section panel
     */
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

    /**
     * Builds and returns the checkout section of the POS interface.
     *
     * <p>This section includes components for displaying the total cost,
     * applying discounts, completing the checkout process, setting the cashier,
     * and returning to the main menu. The layout is organized vertically using
     * a BoxLayout for clear separation of each functional area.</p>
     *
     * <p>The checckout has components:
     * <ul>
     *   <li>A display label showing the order total</li>
     *   <li>A button to apply discounts</li>
     *   <li>A checkout button to finalize the transaction</li>
     *   <li>A cashier input field</li>
     *   <li>A button to return to the main menu</li>
     * </ul>
     * </p>
     *
     * @return a {@link JComponent} representing the checkout section panel
     */
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

    /**
     * Accesses the database to fins and get the attributes for a spesific discout
     *
     * <p>Searches fo attrbute associated with 
     * the discount name searched and applied that 
     * to the checkout toals</p>
     */
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

    /**
     * Recalculates the total cost of the current order and updates the total label.
     *
     * <p>This is used whn discount are apllied or items are in chackout are changed.</p>
     */
    private void recalcAndUpdateTotalLabel() {
        // 'total' is your running subtotal from cart items
        double subtotal = total;

        double discountAmount = subtotal * discountRate;
        double discountedSubtotal = subtotal - discountAmount;

        taxAmount = round2(discountedSubtotal * TAX_RATE);
        finalTotal = round2(discountedSubtotal + taxAmount);

        totalLabel.setText(String.format("$%.2f", finalTotal));
    }

    /**
     * Adds an item to the cart and updates the total cost.
     *
     * <p>This method inserts a new row into the checkout cart table with the given item
     * details, including type, ID, name, options, and price. It then
     * updates the total.</p>
     *
     * @param type the category or type of the item
     * @param id the unique identifier of the item
     * @param itemName the name of the item
     * @param options any selected options or customizations for the item
     * @param price the price of the item
     */
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

    /**
     * Inserts a new receipt record into the database and returns its ID.
     *
     * <p>This method creates a receipt entry using the current transaction details.
     * The purchase date is set to the current timestamp. If no valid
     * customer or cashier ID is available, a default value of 0 is used.</p>
     *
     * @return the ID of the newly inserted receipt
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Inserts all items from the cart into the database for a given receipt.
     *
     * <p>This method iterates through each item in the cart and inserts it into
     * the appropriate database table based on its type.</p>
     *
     * @param receiptId the ID of the receipt to associate the cart items with
     * @throws SQLException if a database access error occurs during insertion
     */
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

    /**
     * Extracts the value associated with a specific key from an options string.
     *
     * <p>This method searches the 'options' string for a specified key and
     * returns the corresponding value. The value is assumed to follow the key
     * and end at the next comma or the end of the string. If the key is not found,
     * an empty string is returned.</p>
     *
     * @param options the full options string containing key value pairs
     * @param key the key to search for within the options string
     * @return the extracted value associated with the key, or an empty string if not found
     */
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

    /**
     * Validates and sets the cashier based on the entered cashier ID.
     *
     * <p>This method retrieves the cashier I D from the input field, verifies that it
     * is a valid numeric value, and queries the database to find a matching cashier.
     * If a valid cashier is found, the cashier ID and name are stored and displayed
     * in the UI. If the ID is invalid or not found, the cashier information is cleared
     * and an error message is shown.</p>
     *
     * <p>The method ensures a valid database connection before performing the lookup
     * and handles any SQL errors by displaying an error dialog.</p>
     */
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

    /**
     * Updates the total label to display the current subtotal with standard dollar/cent format>
     */
    private void updateTotalLabel() {
        totalLabel.setText(String.format("$%.2f", total));
    }

    /**
     * Processes the checkout for the current order.
     *
     * <p>This method validates that the cart is not empty, calculates the subtotal,
     * discount, tax, and final total, and prompts the user to select a payment method.
     * It then displays a summary of the transaction to the user. After a successful 
     * checkout, the cart, totals, discount information, and payment method
     * are reset, and the UI is updated accordingly.</p>
     */
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

    /**
     * Prompts the user to select or enter a payment method
     *
     * <p>This method displays a dialog with payment options:
     * Cash, Card, Other, Cancel. If the user selects "Other", they are
     * prompted to enter a custom payment method. If the user cancels the
     * dialog or provides invalid input, null is returned.</p>
     *
     * @return the selected or entered payment method as a string,
     *         or null if the user cancels or provides invalid input
     */
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
    
    /**
     * Builds and returns the right panel of the POS interface.
     *
     * <p>This panel contains the main item selection components, including
     * a grid of available drinks in the center and a search bar feild at the bottom.</p>
     *
     * @return a item representing the panel
     */
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

    /**
     * Builds and returns the search panel for the POS interface.
     *
     * <p>This panel provides functionality for searching menu items. It includes
     * a search bar, as well as a table that displays matching items.
     * Each result includes an 'add' button that allows items to be added directly
     * to the cart.</p>
     *
     * @return an item representing the search panel
     */
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

    /**
     * Searches the menu database for items matching the user's input and displays the results.
     *
     * <p>This method retrieves the search key from the user and determines if
     * it represents an item ID or a name. If the input is numeric, it searches for items
     * by ID; otherwise, it performs a case-insensitive search by name across both drink
     * and food tables.</p>
     */
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

    /**
     * Retrieves the base price of a drink from the database by its name.
     *
     * <p>This method queries the drink table to find the price associated
     * with the specified drink name.</p>
     *
     * @param drinkName the name of the drink to look up
     * @return the base price of the drink, or 0.0
     */
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

    /**
     * Custom table cell renderer that displays a button in a JTable cell.
     *
     * <p>This renderer extends button and implements
     * table renderer to display a button (e.g., "Add") in table cells.</p>
     * 
     * @author Julia Street
     * @version 1.0
     */
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        /**
     * Constructs a ButtonRenderer and ensures the button is opaque
     * so it is properly displayed in table cells.
     */
        public ButtonRenderer() {
            setOpaque(true);
        }

        /**
         * Returns the component used to render a table cell as a button.
         *
         * <p>This method sets the button text based on the cell value,
         * defaulting to 'add' if the value is null, and returns the button
         * component for display in the table.</p>
         *
         * @param table the JTable requesting the renderer
         * @param value the value of the cell to render
         * @param isSelected whether the cell is selected
         * @param hasFocus whether the cell has focus
         * @param row the row index of the cell
         * @param column the column index of the cell
         * @return the component used to render the cell
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText(value == null ? "Add" : value.toString());
            return this;
        }
    }

    /**
     * Custom table cell editor that allows button interaction within a JTable cell.
     *
     * <p>The editor tracks the clicked state and the row being edited to handle
     * user interactions appropriately.</p>
     * 
     * @author Julia Street
     */
    private class ButtonEditor extends DefaultCellEditor {

        private final JButton button = new JButton();
        private boolean clicked;
        private int row;

        /**
         * Constructs a ButtonEditor using the specified checkbox component.
         * 
         * @param checkBox the checkbox component required by DefaultCellEditor
         */
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        /**
         * Returns the component used for editing a table cell as a button.
         *
         * @param table the JTable requesting the editor
         * @param value the value of the cell being edited
         * @param isSelected whether the cell is selected
         * @param row the row index of the cell
         * @param column the column index of the cell
         * @return the button component used for editing the cell
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            button.setText("Add");
            clicked = true;
            return button;
        }

        /**
         * Returns the value of the cell editor and performs the associated action.
         *
         * @return the value of the cell editor, typically 'add'
         */
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
    
    /**
     * Builds and returns a grid of drink selection buttons for the POS interface.
     *
     * <p>This method queries the database for all available drinks and
     * creates a button for each one. Each button has the drink name and price
     * and allows the user to select and customize the drink.</p>
     * 
     * @return a item containing the drink selection grid
     */
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

    /**
     * Displays a popup for customizing a selected drink and adds it to the cart.
     *
     * <p>This method presents a user interface allowing the user to choose drink
     * customization options such as ice level, sweetness, milk type, and optional
     * add-ons like boba or popping boba. </p>
     *
     * @param drinkId the unique identifier of the selected drink
     * @param drinkName the name of the selected drink
     * @param basePrice the base price of the drink before add-ons
     */
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
