import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.awt.event.HierarchyEvent;

public class MainMenuPanel extends JPanel {

    private final GUI gui;
    private Connection conn;
    private static final String MANAGER_PIN = "manager"; // or "mm", still undecided
    private boolean loginInProgress = false;

    public MainMenuPanel(GUI gui) {

        this.gui = gui;

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openTrends = new JButton("Open Trends");
        openTrends.addActionListener(e -> gui.showScreen("Trends"));

        JButton openPurchases = new JButton("Open Purchases");
        openPurchases.addActionListener(e -> gui.showScreen("Purchases"));

        JButton openTransaction = new JButton("Open Transaction");
        openTransaction.addActionListener(e -> gui.showScreen("Transaction"));

        JButton openInventory = new JButton("Open Inventory");
        openInventory.addActionListener(e -> gui.showScreen("Inventory"));

        JButton openPOS = new JButton("Open POS");
        openPOS.addActionListener(e -> gui.showScreen("POS"));

        JButton openMenu = new JButton("Open Menu");
        openMenu.addActionListener(e -> gui.showScreen("Menu"));

        JButton openXReport = new JButton("Open X Report");
        openXReport.addActionListener(e -> gui.showScreen("XReport"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openTrends);
        centerPanel.add(openPurchases);
        centerPanel.add(openTransaction);
        centerPanel.add(openInventory);
        centerPanel.add(openPOS);
        centerPanel.add(openMenu);
        centerPanel.add(openXReport);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> {
                    loginInProgress = false;   // allow popup again
                    forceLoginPopup();
                });
            }
        });
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        // When this panel is shown again, force login
        if (aFlag) {
            SwingUtilities.invokeLater(() -> {
                // allow popup to run again
                loginInProgress = false;
                forceLoginPopup();
            });
        }
    }

    private void forceLoginPopup() {
        if (loginInProgress) return;
        loginInProgress = true;

        JTextField input = new JTextField(15);

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(new JLabel("Enter Cashier ID (number) OR Manager PIN:"), BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    p,
                    "Login Required",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                // If they cancel, don't allow access to menu
                // Send them back to POS or exit — pick one:
                // gui.showScreen("POS");
                System.exit(0);
                return;
            }

            String typed = input.getText().trim();

            // Manager path
            if (typed.equalsIgnoreCase(MANAGER_PIN)) {
                loginInProgress = false;
                return; // allow menu access
            }

            // Cashier login (validated)
            if (typed.matches("\\d+")) {
                int id = Integer.parseInt(typed);
                if (cashierExists(id)) {
                    loginInProgress = false;
                    gui.showScreen("POS");
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "No cashier found with ID " + id);
                    input.setText("");
                    input.requestFocusInWindow();
                    continue;
                }
            }

            JOptionPane.showMessageDialog(this, "Invalid login. Enter a numeric cashier ID or the manager PIN.");
            input.setText("");
            input.requestFocusInWindow();
        }
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

    private void ensureConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                getConnection();
            }
        } catch (SQLException e) {
            getConnection();
        }
    }

    private boolean cashierExists(int cashierId) {
        ensureConnection();
        if (conn == null) return false;

        String sql = "SELECT 1 FROM cashier WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cashierId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cashier lookup error: " + ex.getMessage());
            return false;
        }
    }

}