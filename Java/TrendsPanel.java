
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.data.general.DefaultPieDataset;



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
        ResultSet orderCount = GetDrinksAndFoodCount();

        DefaultPieDataset orderDefaultPieDataset = loadOrderData(orderCount);




        // Bar chart for showing monthly revenue

        // line chart to show monthly number of sales

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


    private static ResultSet GetDrinksAndFoodCount() {
        try {
            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT COUNT(drink_id) AS number_of_orders, name
                FROM (
                    SELECT drink_to_receipt.drink_id, drink.name
                    FROM drink
                    INNER JOIN drink_to_receipt ON drink.id = drink_to_receipt.drink_id
                )
                GROUP BY name
                UNION
                SELECT COUNT(food_id) AS number_of_orders, name
                FROM (
                    SELECT food_to_receipt.food_id, food.name
                    FROM food
                    INNER JOIN food_to_receipt ON food.id = food_to_receipt.food_id
                )
                GROUP BY name;
            """;
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
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

    private DefaultPieDataset loadOrderData(ResultSet orderCount) {
        // create dataset for pi graph
        DefaultPieDataset orderPiGraphData = new DefaultPieDataset();

        // while loop through result set and input values into dataset
        try {
            while (orderCount != null && orderCount.next()) {
                orderPiGraphData.setValue(orderCount.getString("name"), orderCount.getInt("number_of_orders"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        
        // return!
        return orderPiGraphData;
    }

    
}
