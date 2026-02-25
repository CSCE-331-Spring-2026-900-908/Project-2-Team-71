
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import javax.swing.*;



//JMJT

public class TrendsPanel extends JPanel {

    private GUI gui;
    private static Connection conn;

    public TrendsPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());

        // Create panel to view graphs

        JPanel trends = new JPanel();
        trends.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        trends.setLayout(new GridLayout(2,2));

        add(trends, BorderLayout.CENTER);
        
        // add four different graphs

        // Pie chart for showing most popular drinks

        // display panel

        //JFreeChart revenueChart = ChartFactory.createBarChart();
        

        
        
        
        
        
        /* 
        String display = "";
        ResultSet result = GetInventory();

        try {
            while (result.next()) {
                display += result.getString("name") + " " + result.getString("amount") + "\n";
            }
        } catch (Exception e) {
            System.exit(1);
        }
        */
        
    }

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

    private static ResultSet GetRevenue() {
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

    
}
