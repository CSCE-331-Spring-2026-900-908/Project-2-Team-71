
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrendsPanel extends JPanel {

    private GUI gui;
    private static Connection conn;


    private static void getConnection() {
        Properties props = new Properties();
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

    public TrendsPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());

        String display = "";
        ResultSet result = GetInventory();

        try {
            while (result.next()) {
                display += result.getString("name") + " " + result.getString("amount") + "\n";
            }
        } catch (Exception e) {
            System.exit(1);
        }

        JTextArea textArea = new JTextArea();
        textArea.setText(display);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        //panel.add(scrollPane);

        CloseConnection();

    }
}
