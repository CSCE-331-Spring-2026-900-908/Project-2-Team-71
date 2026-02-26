
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MenuPanel extends JPanel {

    private final GUI gui;
    private Connection conn;

    private JTabbedPane tabbedPane;

    // ===== Drink Tab Components =====
    private DefaultTableModel drinkModel;
    private JTable drinkTable;
    private JButton addDrinkButton;
    private JButton editDrinkButton;
    private JButton deleteDrinkButton;

    // ===== Food Tab Components =====
    private DefaultTableModel foodModel;
    private JTable foodTable;
    private JButton addFoodButton;
    private JButton editFoodButton;
    private JButton deleteFoodButton;

    public MenuPanel(GUI gui) {
        this.gui = gui;
        getConnection();

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topPanel.add(backButton);

        add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Drinks", buildDrinkPanel());
        tabbedPane.addTab("Food", buildFoodPanel());

        add(tabbedPane, BorderLayout.CENTER);
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

            conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ================================================
    // ================== Drink Tab ===================
    // ================================================
    private JPanel buildDrinkPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Table
        drinkModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        drinkTable = new JTable(drinkModel);
        JScrollPane scroll = new JScrollPane(drinkTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        addDrinkButton = new JButton("Add Drink");
        editDrinkButton = new JButton("Edit Drink");
        deleteDrinkButton = new JButton("Delete Drink");

        btnPanel.add(addDrinkButton);
        btnPanel.add(editDrinkButton);
        btnPanel.add(deleteDrinkButton);

        panel.add(btnPanel, BorderLayout.SOUTH);

        // Load data
        loadDrinks();

        // Button Actions
        addDrinkButton.addActionListener(e -> openDrinkDialog(null));
        editDrinkButton.addActionListener(e -> {
            int row = drinkTable.getSelectedRow();
            if (row != -1) {
                int id = (int) drinkModel.getValueAt(row, 0);
                openDrinkDialog(id);
            }
        });
        deleteDrinkButton.addActionListener(e -> {
            int row = drinkTable.getSelectedRow();
            if (row != -1) {
                int id = (int) drinkModel.getValueAt(row, 0);
                deleteDrink(id);
            }
        });

        return panel;
    }

    private void loadDrinks() {
        drinkModel.setRowCount(0);
        if (conn == null) {
            return;
        }
        String sql = "SELECT id, name, price FROM drink ORDER BY id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                drinkModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading drinks: " + ex.getMessage());
        }
    }

    private void openDrinkDialog(Integer drinkId) {
        boolean editing = drinkId != null;
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        // Ingredient table
        DefaultTableModel recipeModel = new DefaultTableModel(new Object[]{"Inventory ID", "Amount"}, 0);
        JTable recipeTable = new JTable(recipeModel);

        if (editing) {
            // Load drink info
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, price FROM drink WHERE id = ?")) {
                ps.setInt(1, drinkId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    priceField.setText(String.valueOf(rs.getDouble("price")));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading drink: " + ex.getMessage());
                return;
            }

            // Load existing recipe
            try (PreparedStatement ps = conn.prepareStatement("SELECT inventory_id, quantity_used FROM drink_recipe WHERE drink_id = ?")) {
                ps.setInt(1, drinkId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    recipeModel.addRow(new Object[]{rs.getInt("inventory_id"), rs.getDouble("quantity_used")});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading recipe: " + ex.getMessage());
            }
        }

        // Ingredient buttons
        JButton addIngredientButton = new JButton("Add Ingredient");
        JButton removeIngredientButton = new JButton("Remove Selected");

        addIngredientButton.addActionListener(e -> recipeModel.addRow(new Object[]{0, 0.0}));
        removeIngredientButton.addActionListener(e -> {
            int row = recipeTable.getSelectedRow();
            if (row != -1) {
                recipeModel.removeRow(row);
            }
        });

        JPanel ingredientPanel = new JPanel(new BorderLayout());
        ingredientPanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);
        JPanel ingredientBtnPanel = new JPanel();
        ingredientBtnPanel.add(addIngredientButton);
        ingredientBtnPanel.add(removeIngredientButton);
        ingredientPanel.add(ingredientBtnPanel, BorderLayout.SOUTH);

        // Main panel
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(new JLabel("Price:"));
        topPanel.add(priceField);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(ingredientPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel,
                (editing ? "Edit Drink" : "Add Drink"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            try {
                conn.setAutoCommit(false);

                int id;
                if (editing) {
                    // Update drink
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE drink SET name = ?, price = ? WHERE id = ?")) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.setInt(3, drinkId);
                        ps.executeUpdate();
                    }
                    id = drinkId;

                    // Delete old recipe
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM drink_recipe WHERE drink_id = ?")) {
                        ps.setInt(1, drinkId);
                        ps.executeUpdate();
                    }
                } else {
                    // Insert drink
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO drink (name, price) VALUES (?, ?) RETURNING id")) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        id = rs.getInt(1);
                    }
                }

                // Insert new recipe
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO drink_recipe (drink_id, inventory_id, quantity_used) VALUES (?, ?, ?)")) {
                    for (int i = 0; i < recipeModel.getRowCount(); i++) {
                        int invId = Integer.parseInt(recipeModel.getValueAt(i, 0).toString());
                        double amt = Double.parseDouble(recipeModel.getValueAt(i, 1).toString());
                        ps.setInt(1, id);
                        ps.setInt(2, invId);
                        ps.setDouble(3, amt);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                loadDrinks();
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                JOptionPane.showMessageDialog(this, "Error saving drink: " + ex.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void insertDrink(String name, double price) {
        String sql = "INSERT INTO drink (name, price) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
            loadDrinks();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding drink: " + ex.getMessage());
        }
    }

    private void updateDrink(int id, String name, double price) {
        String sql = "UPDATE drink SET name = ?, price = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, id);
            ps.executeUpdate();
            loadDrinks();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating drink: " + ex.getMessage());
        }
    }

    private void deleteDrink(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete drink and its recipe?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM drink_recipe WHERE drink_id = ?")) {
                    ps1.setInt(1, id);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM drink WHERE id = ?")) {
                    ps2.setInt(1, id);
                    ps2.executeUpdate();
                }
                conn.commit();
                loadDrinks();
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                JOptionPane.showMessageDialog(this, "Error deleting drink: " + ex.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ================================================
    // ================== Food Tab ====================
    // ================================================
    private JPanel buildFoodPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        foodModel = new DefaultTableModel(new Object[]{"ID", "Name", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        foodTable = new JTable(foodModel);
        JScrollPane scroll = new JScrollPane(foodTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        addFoodButton = new JButton("Add Food");
        editFoodButton = new JButton("Edit Food");
        deleteFoodButton = new JButton("Delete Food");

        btnPanel.add(addFoodButton);
        btnPanel.add(editFoodButton);
        btnPanel.add(deleteFoodButton);

        panel.add(btnPanel, BorderLayout.SOUTH);

        loadFoods();

        addFoodButton.addActionListener(e -> openFoodDialog(null));
        editFoodButton.addActionListener(e -> {
            int row = foodTable.getSelectedRow();
            if (row != -1) {
                int id = (int) foodModel.getValueAt(row, 0);
                openFoodDialog(id);
            }
        });
        deleteFoodButton.addActionListener(e -> {
            int row = foodTable.getSelectedRow();
            if (row != -1) {
                int id = (int) foodModel.getValueAt(row, 0);
                deleteFood(id);
            }
        });

        return panel;
    }

    private void loadFoods() {
        foodModel.setRowCount(0);
        if (conn == null) {
            return;
        }
        String sql = "SELECT id, name, price FROM food ORDER BY id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                foodModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading food: " + ex.getMessage());
        }
    }

    private void openFoodDialog(Integer foodId) {
        boolean editing = foodId != null;
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();

        // Recipe table
        DefaultTableModel recipeModel = new DefaultTableModel(new Object[]{"Inventory ID", "Amount"}, 0);
        JTable recipeTable = new JTable(recipeModel);

        if (editing) {
            // Load food info
            try (PreparedStatement ps = conn.prepareStatement("SELECT name, price FROM food WHERE id = ?")) {
                ps.setInt(1, foodId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    priceField.setText(String.valueOf(rs.getDouble("price")));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading food: " + ex.getMessage());
                return;
            }

            // Load existing recipe
            try (PreparedStatement ps = conn.prepareStatement("SELECT inventory_id, quantity_used FROM food_recipe WHERE food_id = ?")) {
                ps.setInt(1, foodId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    recipeModel.addRow(new Object[]{rs.getInt("inventory_id"), rs.getDouble("quantity_used")});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading recipe: " + ex.getMessage());
            }
        }

        // Ingredient buttons
        JButton addIngredientButton = new JButton("Add Ingredient");
        JButton removeIngredientButton = new JButton("Remove Selected");

        addIngredientButton.addActionListener(e -> recipeModel.addRow(new Object[]{0, 0.0}));
        removeIngredientButton.addActionListener(e -> {
            int row = recipeTable.getSelectedRow();
            if (row != -1) {
                recipeModel.removeRow(row);
            }
        });

        JPanel ingredientPanel = new JPanel(new BorderLayout());
        ingredientPanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);
        JPanel ingredientBtnPanel = new JPanel();
        ingredientBtnPanel.add(addIngredientButton);
        ingredientBtnPanel.add(removeIngredientButton);
        ingredientPanel.add(ingredientBtnPanel, BorderLayout.SOUTH);

        // Main panel
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        topPanel.add(new JLabel("Price:"));
        topPanel.add(priceField);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(ingredientPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel,
                (editing ? "Edit Food" : "Add Food"), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            try {
                conn.setAutoCommit(false);

                int id;
                if (editing) {
                    // Update food
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE food SET name = ?, price = ? WHERE id = ?")) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.setInt(3, foodId);
                        ps.executeUpdate();
                    }
                    id = foodId;

                    // Delete old recipe
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM food_recipe WHERE food_id = ?")) {
                        ps.setInt(1, foodId);
                        ps.executeUpdate();
                    }
                } else {
                    // Insert food
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO food (name, price) VALUES (?, ?) RETURNING id")) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        id = rs.getInt(1);
                    }
                }

                // Insert new recipe
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO food_recipe (food_id, inventory_id, quantity_used) VALUES (?, ?, ?)")) {
                    for (int i = 0; i < recipeModel.getRowCount(); i++) {
                        int invId = Integer.parseInt(recipeModel.getValueAt(i, 0).toString());
                        double amt = Double.parseDouble(recipeModel.getValueAt(i, 1).toString());
                        ps.setInt(1, id);
                        ps.setInt(2, invId);
                        ps.setDouble(3, amt);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                loadFoods(); // reload table/list in the UI
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                JOptionPane.showMessageDialog(this, "Error saving food: " + ex.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private void insertFood(String name, double price) {
        String sql = "INSERT INTO food (name, price) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.executeUpdate();
            loadFoods();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding food: " + ex.getMessage());
        }
    }

    private void updateFood(int id, String name, double price) {
        String sql = "UPDATE food SET name = ?, price = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, id);
            ps.executeUpdate();
            loadFoods();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating food: " + ex.getMessage());
        }
    }

    private void deleteFood(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete food and its recipe?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM food_recipe WHERE food_id = ?")) {
                    ps1.setInt(1, id);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM food WHERE id = ?")) {
                    ps2.setInt(1, id);
                    ps2.executeUpdate();
                }
                conn.commit();
                loadFoods();
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                JOptionPane.showMessageDialog(this, "Error deleting food: " + ex.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
