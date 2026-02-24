
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;

public class PurchasesPanel extends JPanel {

    private GUI gui;
    static Connection conn;

    private static void GetConnection() {
        //Building the connection
        Properties props = new Properties();

        // Specify the path to your .env file
        var envFile = Paths.get(".env").toAbsolutePath().toString();

        try (FileInputStream inputStream = new FileInputStream(envFile)) {
            props.load(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            return;
        }

        String databaseName = props.getProperty("DATABASE_NAME");
        String databaseUser = props.getProperty("DATABASE_USER");
        String databasePassword = props.getProperty("DATABASE_PASSWORD");
        String databaseUrl = String.format(props.getProperty("DATABASE_URL") + "%s", databaseName);

        try {
            conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private static void CloseConnection() {
        //closing the connection
        try {
            conn.close();
            JOptionPane.showMessageDialog(null, "Connection Closed.");
        } catch (HeadlessException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }

    private static ResultSet GetInventory() {
        try {
            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = "SELECT * FROM inventory";

            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    public PurchasesPanel(GUI gui) {
        this.gui = gui;

        setLayout(new BorderLayout());

        // Top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));

        topBar.add(backButton);

        add(topBar, BorderLayout.NORTH);

        // Main content
        JTextArea textArea = new JTextArea("Purchases Screen");
        textArea.setEditable(false);

        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }
}
