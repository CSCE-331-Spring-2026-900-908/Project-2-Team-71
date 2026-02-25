
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

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;


//JMJT

public class TrendsPanel extends JPanel {

    //private GUI gui;
    private static Connection conn;

    public static JPanel ShowGUI(GUI gui) {
        //this.gui = gui;
        gui.setLayout(new BorderLayout());

        // Create panel to view graphs

        JPanel trends = new JPanel();
        //trends.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        trends.setLayout(new GridLayout(2, 2, 20, 20));


        // add four different graphs //


        // Pie chart for showing most popular drinks
        ChartPanel piChart = SetUpPiChart();
        trends.add(piChart);

        // Bar chart for showing monthly revenue
        ChartPanel barChart = SetUpBarChart();
        trends.add(barChart);

        // line chart to show monthly number of sales
        ChartPanel lineChart = SetUpLineChart();
        trends.add(lineChart);

        // Show busy time trends
        ChartPanel timeChart = SetUpTimeChart();
        trends.add(timeChart);
        

        return trends;
    }



    private static ChartPanel SetUpPiChart() {
        ResultSet orderCount = GetDrinksAndFoodCount();
        DefaultPieDataset orderPieDataset = LoadOrderData(orderCount);

        JFreeChart ordersPiChart = ChartFactory.createPieChart(
            "All Time Sales Per Item", // Title
            orderPieDataset, // Dataset
            true, // Legend?
            true, // Tooltip?
            false // URLS?
        );

        ChartPanel piChart = new ChartPanel(ordersPiChart);
        return piChart;
    }

    private static ChartPanel SetUpBarChart() {
        ResultSet incomeData = GetIncome();
        DefaultCategoryDataset incomeDataset = LoadBarData(incomeData, "Income");

        ResultSet lossData = GetExpenses();
        DefaultCategoryDataset lossDataset = LoadBarData(lossData, "Loss");

        // Create combination bar plot. One bar will be loss and other on top will be income.
        CategoryPlot revenuePlot = new CategoryPlot();
        revenuePlot.setDataset(1,incomeDataset);
        revenuePlot.setRenderer(1, new BarRenderer());

        revenuePlot.setDataset(0,lossDataset);
        revenuePlot.setRenderer(0, new BarRenderer());

        revenuePlot.setDomainAxis(new CategoryAxis("Month"));
        revenuePlot.setRangeAxis(new NumberAxis("Money"));

        revenuePlot.setOrientation(PlotOrientation.VERTICAL);
        revenuePlot.setRangeGridlinesVisible(true);
        revenuePlot.setDomainGridlinesVisible(true);

        // make plot into chart for adding to JPanel
        JFreeChart revenueBarChart = new JFreeChart(
            "Monthly Revenue", // Title
            null, // null if default font
            revenuePlot, // Combination bar graph plot
            true // Legend
        );

        ChartPanel barChart = new ChartPanel(revenueBarChart);
        return barChart;
    }

    private static ChartPanel SetUpLineChart() {
        // line chart will display monthly amount of customer orders by tracking receipts per month
        ResultSet receiptData = GetReceipts();
        DefaultCategoryDataset receiptDataset = LoadReceiptData(receiptData);

        // Create the line chart
        JFreeChart receiptLineChart = ChartFactory.createLineChart(
            "Monthly Receipt Count", 
            "Month",
            "Receipts", 
            receiptDataset,
            PlotOrientation.VERTICAL,
            false, // Legend?
            true, // Tooltips?
            false // urls?
        );

        // Convert chart into a panel and return!
        ChartPanel lineChart = new ChartPanel(receiptLineChart);
        return lineChart;
    }

    private static ChartPanel SetUpTimeChart() {
        ResultSet timeData = GetTimes();
        DefaultCategoryDataset timeDataset = LoadTimeData(timeData);
        

        JFreeChart timeLineChart = ChartFactory.createLineChart(
            "Hourly Business",
            "Time", 
            "Receipts", 
            timeDataset, 
            PlotOrientation.VERTICAL,
            false, // Legend?
            true, // Tooltips?
            false // URLs?
        );

        // Convert chart into a panel and return!
        ChartPanel timeChart = new ChartPanel(timeLineChart);
        return timeChart;
    }



    private static void GetConnection() {
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
            GetConnection();

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

    private static ResultSet GetIncome() {
        // finds total income from sales for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT SUM(sale) AS income, month
                FROM (
                    SELECT drink.price AS sale, DATE_PART('month', receipt.purchase_date) AS month 
                    FROM ((drink
                    INNER JOIN drink_to_receipt ON drink.id = drink_to_receipt.drink_id)
                    INNER JOIN receipt ON receipt.id = drink_to_receipt.receipt_id)
                    UNION ALL
                    SELECT food.price AS sale, DATE_PART('month',receipt.purchase_date) AS month 
                    FROM ((food
                    INNER JOIN food_to_receipt ON food.id = food_to_receipt.food_id)
                    INNER JOIN receipt ON receipt.id = food_to_receipt.receipt_id)
                )
                GROUP BY month
                ORDER BY month ASC;
            """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetExpenses() {
        // finds total expenses for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT SUM(expense) AS loss, month
                FROM (
                    SELECT supplier_price AS expense, DATE_PART('month', buy_date) AS month 
                    FROM purchase
                )
                GROUP BY month
                ORDER BY month ASC;
            """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetReceipts() {
        // finds total number of receipts for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT COUNT(id) AS receipts, month
                FROM (
                    SELECT id, DATE_PART('month', purchase_date) AS month 
                    FROM receipt
                )
                GROUP BY month
                ORDER BY month ASC;
            """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetTimes() {
        // finds avg number of receipts for each hour
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT AVG(orders) AS avg_receipts, hour
                FROM (
                    SELECT COUNT(id) AS orders, 
                    DATE_PART('hour', purchase_date) AS hour, 
                    DATE_PART('month', purchase_date) AS month, 
                    DATE_PART('day', purchase_date) AS day
                    FROM receipt
                    GROUP BY hour, day, month
                )
                GROUP BY hour
                ORDER BY hour ASC;
            """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }



    private static DefaultPieDataset LoadOrderData(ResultSet orderCount) {

        // create dataset for pi graph
        DefaultPieDataset piDataset = new DefaultPieDataset();

        // while loop through result set and input values into dataset
        try {
            while (orderCount != null && orderCount.next()) {
                piDataset.setValue(orderCount.getString("name"), orderCount.getInt("number_of_orders"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return piDataset;
    }

    private static DefaultCategoryDataset LoadBarData(ResultSet barData, String saleType) {
        // create dataset for bar graph
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        // while loop through result set and input values into dataset
        try {
            while (barData != null && barData.next()) {
                barDataset.addValue(barData.getInt(1), saleType, barData.getString(2));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return barDataset;
    }

    private static DefaultCategoryDataset LoadReceiptData(ResultSet receiptData) {
        // create dataset for line graph
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();

        // while loop through result set and input values into dataset
        try {
            while (receiptData != null && receiptData.next()) {
                lineDataset.addValue(receiptData.getInt("receipts"), "Receipts", receiptData.getString("month"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return lineDataset;
    }

    private static DefaultCategoryDataset LoadTimeData(ResultSet timeData) {
        // create dataset for line graph
        DefaultCategoryDataset timeDataset = new DefaultCategoryDataset();

        // while loop through result set and input values into dataset
        try {
            while (timeData != null && timeData.next()) {
                timeDataset.addValue(timeData.getInt("avg_receipts"), "Avg Receipts", timeData.getString("hour"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return timeDataset;
    }
}
