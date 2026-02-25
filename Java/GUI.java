
import java.awt.*;
import javax.swing.*;
import java.sql.*;

public class GUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel container;
    ///////////////////// login //////////////////////////////////////////
    private Connection conn;              // set this to your real DB connection
    private Integer cashierId = null;
    private String cashierName = null;

    public Integer getCashierId() { return cashierId; }
    public String getCashierName() { return cashierName; }
    ////////////////////////////////////////////////////////////////////////

    public GUI() {
        setTitle("DB GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);

        // Initialize layout
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        // Create screens
//        MainMenuPanel menuPanel = new MainMenuPanel(this);
//        PanelTemplate template = new PanelTemplate(this);
        POSScreen posScreen = new POSScreen(this, conn); //assumoing coonection obj is created elsewhere

        // Add screens to container
//        container.add(menuPanel, "MAIN");
//        container.add(template, "TEMP");
        container.add(posScreen, "POS");

        add(container);

        setLocationRelativeTo(null); // center window
        setVisible(true);

        /////////////////// login //////////////////////////////////////////////
        while (!promptCashierLoginById()) {
            // keep asking until valid cashier ID
        }
        ////////////////////////////////////////////////////////////////////////

        showScreen("POS"); // open to the POS page
    }

    private boolean promptCashierLoginById() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No DB connection yet.");
            return false;
        }

        String input = JOptionPane.showInputDialog(this, "Enter Cashier ID:", "Cashier Login",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null) return false; // cancel

        input = input.trim();
        if (!input.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Cashier ID must be a number.");
            return false;
        }

        int id = Integer.parseInt(input);

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name FROM cashiers WHERE id = ? LIMIT 1")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cashierId = rs.getInt("id");
                    cashierName = rs.getString("name");
                    JOptionPane.showMessageDialog(this,
                            "Logged in as: " + cashierName + " (ID " + cashierId + ")");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "No cashier found with ID " + id);
                    return false;
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage());
            return false;
        }
    }

    // Method to switch screens
    public void showScreen(String name) {
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}