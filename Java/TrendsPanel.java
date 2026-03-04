import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

    public TrendsPanel(GUI gui) {
        //this.gui = gui;
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel graphPanel = new JPanel();
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Create button to navigate back to main menu
        // make menu button like the rest of the pages
        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topBar.add(backButton);

        // Create a button to display graphs for all time data
        // it will populate the date fields to include all the data
        JButton allTimeButton = new JButton("All Time");
        allTimeButton.addActionListener(e -> LoadAllTime(graphPanel, bottomBar));
        topBar.add(allTimeButton);


        // Create field to enter time frame of interest
        // This information will be fed into the graphs
        JTextField fromDateField = new JTextField(8);
        JTextField toDateField = new JTextField(8);
        topBar.add(new JLabel("From (YYYY-MM-DD):"));
        topBar.add(fromDateField);
        topBar.add(new JLabel("To:"));
        topBar.add(toDateField);

        // add refresh button to refresh graphs to corresponding time frame
        JButton refreshButton = new JButton("Refresh");
        topBar.add(refreshButton);

        // add to frame!
        add(topBar, BorderLayout.NORTH);

        // generate graphs to display all time data
        LoadAllTime(graphPanel, bottomBar);
        add(graphPanel, BorderLayout.CENTER);


        // create bottom bar to display current graph time frame
        String timeString = "January 21, 2025 - March 21, 2025";
        JLabel timeFrame = new JLabel(timeString);
        timeFrame.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
        timeFrame.setFont(new Font(timeFrame.getName(), Font.BOLD, 16));
        bottomBar.add(timeFrame);
        add(bottomBar, BorderLayout.SOUTH);
    }




    

    private static ChartPanel SetUpPiChart(String[] startDate, String[] endDate) {
        ResultSet orderCount = GetDrinksAndFoodCount(startDate, endDate);
        DefaultPieDataset orderPieDataset = LoadOrderData(orderCount);

        JFreeChart ordersPiChart = ChartFactory.createPieChart(
            "Sales Per Item", // Title
            orderPieDataset, // Dataset
            true, // Legend?
            true, // Tooltip?
            false // URLS?
        );

        ChartPanel piChart = new ChartPanel(ordersPiChart);
        return piChart;
    }

    private static ChartPanel SetUpBarChart(String[] startDate, String[] endDate) {
        ResultSet incomeData = GetIncome(startDate, endDate);
        DefaultCategoryDataset incomeDataset = LoadBarData(incomeData, "Income");

        ResultSet lossData = GetExpenses(startDate, endDate);
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

    private static ChartPanel SetUpLineChart(String[] startDate, String[] endDate) {
        // line chart will display monthly amount of customer orders by tracking receipts per month
        ResultSet receiptData = GetReceipts(startDate, endDate);
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

    private static ChartPanel SetUpTimeChart(String[] startDate, String[] endDate) {
        ResultSet timeData = GetTimes(startDate, endDate);
        DefaultCategoryDataset timeDataset = LoadTimeData(timeData);
        

        JFreeChart timeLineChart = ChartFactory.createLineChart(
            "Average Hourly Business",
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


    private static void redrawGraphs(JPanel graphPanel, String[] startDate, String[] endDate) {
        // This function replaces the graphs with the data contained in the 
        // specified time frame
        graphPanel.removeAll();

        // add four different graphs //
        graphPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Pie chart for showing most popular drinks
        ChartPanel piChart = SetUpPiChart(startDate, endDate);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(piChart, constraints);

        // Bar chart for showing monthly revenue
        ChartPanel barChart = SetUpBarChart(startDate, endDate);
        constraints.gridx = 200;
        constraints.gridy = 0;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(barChart, constraints);

        // line chart to show monthly number of sales
        ChartPanel lineChart = SetUpLineChart(startDate, endDate);
        constraints.gridx = 0;
        constraints.gridy = 100;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(lineChart, constraints);

        // Show busy time trends
        ChartPanel timeChart = SetUpTimeChart(startDate, endDate);
        constraints.gridx = 200;
        constraints.gridy = 100;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(timeChart, constraints);

        
        graphPanel.revalidate();
        graphPanel.repaint();
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


    private static ResultSet GetDrinksAndFoodCount(String[] startDate, String[] endDate) {
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = 
                " SELECT COUNT(drink_id) AS number_of_orders, name " 
                + "FROM ( "
                    + "SELECT "
                        + "drink_to_receipt.drink_id, "
                        + "drink.name, " 
                        + "DATE_PART('month', receipt.purchase_date) AS month, "
                        + "DATE_PART('day', receipt.purchase_date) AS day, "
                        + "DATE_PART('year', receipt.purchase_date) AS year "
                    + "FROM drink "
                    + "INNER JOIN drink_to_receipt ON drink.id = drink_to_receipt.drink_id "
                    + "INNER JOIN receipt ON receipt.id = drink_to_receipt.receipt_id "
                    + "WHERE ( "
                            + "(DATE_PART('month', receipt.purchase_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', receipt.purchase_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', receipt.purchase_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY name "
                + "UNION "
                + "SELECT COUNT(food_id) AS number_of_orders, name "
                + "FROM ( "
                    + "SELECT "
                        + "food_to_receipt.food_id, "
                        + "food.name, " 
                        + "DATE_PART('month', receipt.purchase_date) AS month, "
                        + "DATE_PART('day', receipt.purchase_date) AS day, "
                        + "DATE_PART('year', receipt.purchase_date) AS year "
                    + "FROM food "
                    + "INNER JOIN food_to_receipt ON food.id = food_to_receipt.food_id "
                    + "INNER JOIN receipt ON receipt.id = food_to_receipt.receipt_id "
                    + "WHERE ( "
                            + "(DATE_PART('month', receipt.purchase_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', receipt.purchase_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', receipt.purchase_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY name";
                
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetIncome(String[] startDate, String[] endDate) {
        // finds total income from sales for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = 
                "SELECT SUM(sale) AS income, month, year " 
                + "FROM ( "
                    + "SELECT "
                        + "drink.price AS sale, "
                        + "DATE_PART('month', receipt.purchase_date) AS month, "
                        + "DATE_PART('day', receipt.purchase_date) AS day, "
                        + "DATE_PART('year', receipt.purchase_date) AS year "
                    + "FROM ((drink "
                        + "INNER JOIN drink_to_receipt ON drink.id = drink_to_receipt.drink_id) "
                        + "INNER JOIN receipt ON receipt.id = drink_to_receipt.receipt_id) "
                    + "WHERE ( "
                            + "(DATE_PART('month', receipt.purchase_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', receipt.purchase_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', receipt.purchase_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                    + "UNION ALL "
                    + "SELECT "
                        + "food.price AS sale, "
                        + "DATE_PART('month', receipt.purchase_date) AS month, "
                        + "DATE_PART('day', receipt.purchase_date) AS day, "
                        + "DATE_PART('year', receipt.purchase_date) AS year "
                    + "FROM ((food"
                        + "INNER JOIN food_to_receipt ON food.id = food_to_receipt.food_id)"
                        + "INNER JOIN receipt ON receipt.id = food_to_receipt.receipt_id)"
                    + "WHERE ( "
                            + "(DATE_PART('month', receipt.purchase_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', receipt.purchase_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', receipt.purchase_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY year, month "
                + "ORDER BY year, month ASC";
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetExpenses(String[] startDate, String[] endDate) {
        // finds total expenses for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = 
                "SELECT SUM(expense) AS loss, month, year " 
                + "FROM ( "
                    + "SELECT "
                        + "supplier_price AS expense, "
                        + "DATE_PART('month', buy_date) AS month, "
                        + "DATE_PART('day', buy_date) AS day, "
                        + "DATE_PART('year', buy_date) AS year "
                    + "FROM purchase "
                    + "WHERE ( "
                            + "(DATE_PART('month', buy_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', buy_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', buy_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY year, month "
                + "ORDER BY year, month ASC";
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetReceipts(String[] startDate, String[] endDate) {
        // finds total number of receipts for each month
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = 
                "SELECT COUNT(id) AS receipts, month, year " 
                + "FROM ( "
                    + "SELECT "
                        + "id, "
                        + "DATE_PART('month', purchase_date) AS month, "
                        + "DATE_PART('day', purchase_date) AS day, "
                        + "DATE_PART('year', purchase_date) AS year "
                    + "FROM receipt "
                    + "WHERE ( "
                            + "(DATE_PART('month', buy_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', buy_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', buy_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY year, month "
                + "ORDER BY year, month ASC";
            
            // """
            //     SELECT COUNT(id) AS receipts, month
            //     FROM (
            //         SELECT id, DATE_PART('month', purchase_date) AS month 
            //         FROM receipt
            //     )
            //     GROUP BY month
            //     ORDER BY month ASC;
            // """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetTimes(String[] startDate, String[] endDate) {
        // finds avg number of receipts for each hour
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = 
                "SELECT AVG(orders) AS avg_receipts, hour " 
                + "FROM ( "
                    + "SELECT "
                        + "COUNT(id) AS orders, "
                        + "DATE_PART('hour', purchase_date) AS hour, "
                        + "DATE_PART('day', purchase_date) AS day,"
                        + "DATE_PART('month', purchase_date) AS month, "
                        + "DATE_PART('year', purchase_date) AS year "
                    + "FROM receipt "
                    + "WHERE ( "
                            + "(DATE_PART('month', buy_date) BETWEEN " + startDate[0] + " AND " + endDate[0] + ") "
                        + "AND "
                            + "(DATE_PART('day', buy_date) BETWEEN " + startDate[1] + " AND " + endDate[1] + ") "
                        + "AND "
                            + "(DATE_PART('year', buy_date) BETWEEN " + startDate[2] + " AND " + endDate[2] + ") "
                    + ") "
                + ") "
                + "GROUP BY hour "
                + "ORDER BY hour ASC";
            
            
            // """
            //     SELECT AVG(orders) AS avg_receipts, hour
            //     FROM (
            //         SELECT COUNT(id) AS orders, 
            //         DATE_PART('hour', purchase_date) AS hour, 
            //         DATE_PART('month', purchase_date) AS month, 
            //         DATE_PART('day', purchase_date) AS day
            //         FROM receipt
            //         GROUP BY hour, day, month
            //     )
            //     GROUP BY hour
            //     ORDER BY hour ASC;
            // """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private static ResultSet GetAllTime() {
        // finds avg number of receipts for each hour
        try {
            GetConnection();

            //create a statement object
            Statement stmt = conn.createStatement();

            //create a SQL statement
            String sqlStatement = """
                SELECT 
                    DATE_PART('month', MIN(purchase_date)) AS month,
                    DATE_PART('day', MIN(purchase_date)) AS day, 
                    DATE_PART('year', MIN(purchase_date)) AS year
                FROM receipt
                UNION ALL
                SELECT 
                    DATE_PART('month', MAX(purchase_date)) AS month, 
                    DATE_PART('day', MAX(purchase_date)) AS day, 
                    DATE_PART('year', MAX(purchase_date)) AS year
                FROM receipt;
            """;
            
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }


    private static void LoadAllTime(JPanel graphPanel, JPanel bottomPanel) {
        // get oldest receipt and newest receipt dates
        ResultSet allTimeSet = GetAllTime();
        boolean oldestDate = true;
        String[] startDate = new String[3];
        String[] endDate = new String[3];
        
        try {
            while (allTimeSet != null && allTimeSet.next()) {
                if (oldestDate) {
                    startDate[0] = allTimeSet.getString("month");
                    startDate[1] = allTimeSet.getString("day");
                    startDate[2] = allTimeSet.getString("year");
                    oldestDate = false;
                }
                else {
                    endDate[0] = allTimeSet.getString("month");
                    endDate[1] = allTimeSet.getString("day");
                    endDate[2] = allTimeSet.getString("year");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        redrawGraphs(graphPanel, startDate, endDate);

        JTextField timeFrameField = (JTextField) bottomPanel.getComponent(0);
        timeFrameField.setText("All Time Data");

        bottomPanel.revalidate();
        bottomPanel.repaint();
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
