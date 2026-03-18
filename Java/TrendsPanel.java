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

/**
 * This is the class to create the "Trends" page. It contains four different graphs representing the data
 * collected in the database. The four graphs are number of products sold, income and loss, 
 * average time of sales, and monthly receipt count
 * 
 * @author Matthew Hebert
 */
public class TrendsPanel extends JPanel {

    //private GUI gui;
    private static Connection conn;

    /**
     * This is the constructor for the TrendsPanel class. The gui is passed in and the page is created.
     * This is the only constructor.
     * 
     * @param gui This is the base JPanel that the Trends page is built off of.
     * @author Matthew Hebert
     */
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
        allTimeButton.addActionListener(e -> TrendsPanel.LoadAllTime(graphPanel, bottomBar));
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
        refreshButton.addActionListener(e -> TrendsPanel.RefreshGraphs(topBar, graphPanel, bottomBar));
        topBar.add(refreshButton);

        // add to frame!
        add(topBar, BorderLayout.NORTH);

        // generate graphs to display all time data
        LoadAllTime(graphPanel, bottomBar);
        add(graphPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }




    
    /**
     * This function is handels the creation of the first graph which is a pi chart. It displays how much of each item was
     * purchased in a given time period
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return The returned value is a set up ChartPanel that can then be added to the Trends page in the JPanel graphPanel section
     * @author Matthew Hebert
     */
    private static ChartPanel SetUpPiChart(String startDateString, String endDateString) {
        ResultSet orderCount = GetDrinksAndFoodCount(startDateString, endDateString);
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

    /**
     * This function is handels the creation of the second graph which is a bar chart. It displays how much income was made
     * and loss of money each month in the given time period.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return The returned value is a set up ChartPanel that can then be added to the Trends page in the JPanel graphPanel section
     * @author Matthew Hebert
     */
    private static ChartPanel SetUpBarChart(String startDateString, String endDateString) {
        ResultSet incomeData = GetIncome(startDateString, endDateString);
        DefaultCategoryDataset incomeDataset = LoadBarData(incomeData, "Income");

        ResultSet lossData = GetExpenses(startDateString, endDateString);
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

    /**
     * This function is handels the creation of the third graph which is a pi chart. It displays the number of receipts 
     * per month in the given time period.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return The returned value is a set up ChartPanel that can then be added to the Trends page in the JPanel graphPanel section
     * @author Matthew Hebert
     */
    private static ChartPanel SetUpLineChart(String startDateString, String endDateString) {
        // line chart will display monthly amount of customer orders by tracking receipts per month
        ResultSet receiptData = GetReceipts(startDateString, endDateString);
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

    /**
     * This function is handels the creation of the fourth graph which is a pi chart. 
     * It displays the average number of receipts per hour in the given time period.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return The returned value is a set up ChartPanel that can then be added to the Trends page in the JPanel graphPanel section
     * @author Matthew Hebert
     */
    private static ChartPanel SetUpTimeChart(String startDateString, String endDateString) {
        ResultSet timeData = GetTimes(startDateString, endDateString);
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


    /**
     * This function handels calling all of the functions to redo the graphs for an updated giventime period
     * 
     * @param graphPanel The JPanel that holds all of the graphs is passed into the function to be cleared and updated.
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return Returns nothing. Void.
     * @author Matthew Hebert
     */
    private static void RedrawGraphs(JPanel graphPanel, String startDateString, String endDateString) {
        // This function replaces the graphs with the data contained in the 
        // specified time frame
        graphPanel.removeAll();
        graphPanel.revalidate();

        // add four different graphs //
        graphPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Pie chart for showing most popular drinks
        ChartPanel piChart = SetUpPiChart(startDateString, endDateString);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(piChart, constraints);

        // Bar chart for showing monthly revenue
        ChartPanel barChart = SetUpBarChart(startDateString, endDateString);
        constraints.gridx = 200;
        constraints.gridy = 0;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(barChart, constraints);

        // line chart to show monthly number of sales
        ChartPanel lineChart = SetUpLineChart(startDateString, endDateString);
        constraints.gridx = 0;
        constraints.gridy = 100;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(lineChart, constraints);

        // Show busy time trends
        ChartPanel timeChart = SetUpTimeChart(startDateString, endDateString);
        constraints.gridx = 200;
        constraints.gridy = 100;
        constraints.gridheight = 100;
        constraints.gridwidth = 200;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        graphPanel.add(timeChart, constraints);

        graphPanel.repaint();
    }

    /**
     * This function handels refreshing the time frame displayed on the bottom on the "Trends" 
     * page with a given updated time frame. 
     * 
     * @param bottomBar The JPanel that displays the current time frame the graphs 
     * represent is passed into the function to be cleared and updated.
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @param allTime This is a boolean that is true if the desired time frame is all recorded history. Else, false.
     * @return Returns nothing. Void.
     * @author Matthew Hebert
     */
    private static void RedrawTimeFrame(JPanel bottomBar, String startDateString, String endDateString, Boolean allTime) {
        bottomBar.removeAll();
        bottomBar.revalidate();

        // create bottom bar to display current graph time frame
        String timeString = startDateString + " To " + endDateString;
        
        if (allTime) {
            timeString += " (All Time Data)";
        }

        JLabel timeFrame = new JLabel(timeString);
        timeFrame.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
        timeFrame.setFont(new Font(timeFrame.getName(), Font.BOLD, 16));
        bottomBar.add(timeFrame);

        bottomBar.repaint();
    }

    /**
     * This function is the higher level function that receives the desired time frame from the user and calls the respective
     * functions to update the "Trends" page accordingly.
     * 
     * @param topBar The JPanel that contains the text box for the user to input the desired time frame.
     * @param graphPanel The JPanel that holds all of the graphs that need to be updated.
     * @param bottomBar The JPanel that displays the current time frame the graphs represent.
     * @return Returns nothing. Void.
     * @author Matthew Hebert
     */
    private static void RefreshGraphs(JPanel topBar, JPanel graphPanel, JPanel bottomBar) {
        JTextField startField = (JTextField) topBar.getComponent(3);
        JTextField endField = (JTextField) topBar.getComponent(5);

        String startDateString = startField.getText();
        String endDateString = endField.getText();

        RedrawGraphs(graphPanel, startDateString, endDateString);
        RedrawTimeFrame(bottomBar, startDateString, endDateString, false);
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

    /**
     * This function queries the database for the amount of each item sold in a given time frame.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
    private static ResultSet GetDrinksAndFoodCount(String startDateString, String endDateString) {
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
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
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
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
                + ") "
                + "GROUP BY name";
                
            //send statement to DBMS
            return stmt.executeQuery(sqlStatement);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    /**
     * This function queries the database for the amount income gained each month in a given time frame.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
    private static ResultSet GetIncome(String startDateString, String endDateString) {
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
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
                    + "UNION ALL "
                    + "SELECT "
                        + "food.price AS sale, "
                        + "DATE_PART('month', receipt.purchase_date) AS month, "
                        + "DATE_PART('day', receipt.purchase_date) AS day, "
                        + "DATE_PART('year', receipt.purchase_date) AS year "
                    + "FROM ((food "
                        + "INNER JOIN food_to_receipt ON food.id = food_to_receipt.food_id) "
                        + "INNER JOIN receipt ON receipt.id = food_to_receipt.receipt_id) "
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
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

    /**
     * This function queries the database for the amount loss each month in a given time frame.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
    private static ResultSet GetExpenses(String startDateString, String endDateString) {
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
                    + "WHERE purchase.buy_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
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

    /**
     * This function queries the database for the amount of receipts each month in a given time frame.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
    private static ResultSet GetReceipts(String startDateString, String endDateString) {
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
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
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

    /**
     * This function queries the database for the average number of receipts sold each hour in a given time frame.
     * 
     * @param startDateString This is a string that contains the starting date for the desired time period. YYYY-MM-DD
     * @param endDateString This is a string that contains the ending date for the desired time period. YYYY-MM-DD
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
    private static ResultSet GetTimes(String startDateString, String endDateString) {
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
                        + "DATE_PART('day', purchase_date) AS day, "
                        + "DATE_PART('month', purchase_date) AS month, "
                        + "DATE_PART('year', purchase_date) AS year "
                    + "FROM receipt "
                    + "WHERE receipt.purchase_date BETWEEN '" + startDateString + "' AND '" + endDateString + "' "
                    + "GROUP BY hour, day, month, year "
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

    /** 
     * This function finds and returns the eailiers purchase date and the latest purchase date. This information
     * is useful for finding the startDateString and the endDateString
     * 
     * @return It returns a ResultSet that contains the data received back from the database query. Can then be parsed.
     * @author Matthew Hebert
     */
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

    /** 
     * This function resets the graphs to display the data for all purchase history.
     * 
     * @param graphPanel The JPanel that holds all of the graphs that need to be updated.
     * @param bottomBar The JPanel that displays the current time frame the graphs represent.
     * @return Returns nothing. Void.
     * @author Matthew Hebert
     */
    private static void LoadAllTime(JPanel graphPanel, JPanel bottomPanel) {
        // get oldest receipt and newest receipt dates
        ResultSet allTimeSet = GetAllTime();
        boolean oldestDate = true;
        String startDateString = "";
        String endDateString = "";
        
        try {
            while (allTimeSet != null && allTimeSet.next()) {
                if (oldestDate) {
                    startDateString = allTimeSet.getString("year") + "-" + allTimeSet.getString("month") + "-" + allTimeSet.getString("day");
                    oldestDate = false;
                }
                else {
                    endDateString = allTimeSet.getString("year") + "-" + allTimeSet.getString("month") + "-" + allTimeSet.getString("day");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        RedrawGraphs(graphPanel, startDateString, endDateString);
        RedrawTimeFrame(bottomPanel, startDateString, endDateString, true);
    }

    /** 
     * This function converts the result from a database query and turns it into data ready to be used in creating the pi
     *  chart for number of items sold in a time period.
     * 
     * @param orderCount This is the ResultSet from the database query for the number of each item sold in a time frame.
     * @return Returns a DefaultPieDataset. This can then be used to supply the data when creating a pi chart.
     * @author Matthew Hebert
     */
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

    /** 
     * This function converts the result from a database query and turns it into data ready to be used in creating the bar
     * chart for income and loss each month in a time period.
     * 
     * @param barData This is the ResultSet from the database query for the income or loss each month during a time period.
     * @param saleType This is a String that is used to indicate if the barData is income or loss.
     * @return Returns a DefaultCategoryDataset. This can then be used to supply the data when creating the bar chart.
     * @author Matthew Hebert
     */
    private static DefaultCategoryDataset LoadBarData(ResultSet barData, String saleType) {
        // create dataset for bar graph
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        String dateEntry;

        // while loop through result set and input values into dataset
        try {
            while (barData != null && barData.next()) {
                dateEntry = barData.getString(2) + "/" + barData.getString(3);
                barDataset.addValue(barData.getInt(1), saleType, dateEntry);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return barDataset;
    }

    /** 
     * This function converts the result from a database query and turns it into data ready to be used in creating the line
     * chart for number of receipts per month in a time period.
     * 
     * @param receiptData This is the ResultSet from the database query for the number of receipts per month during a time period.
     * @return Returns a DefaultCategoryDataset. This can then be used to supply the data when creating the line chart.
     * @author Matthew Hebert
     */
    private static DefaultCategoryDataset LoadReceiptData(ResultSet receiptData) {
        // create dataset for line graph
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        String dateEntry;

        // while loop through result set and input values into dataset
        try {
            while (receiptData != null && receiptData.next()) {
                dateEntry = receiptData.getString("month") + "/" + receiptData.getString("year");
                lineDataset.addValue(receiptData.getInt("receipts"), "Receipts", dateEntry);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
        // return!
        return lineDataset;
    }

    /** 
     * This function converts the result from a database query and turns it into data ready to be used in creating the line
     * chart for average receipts per hour sampled across a time period.
     * 
     * @param timeData This is the ResultSet from the database query for the average number of receipts per hour sampled across a time period.
     * @return Returns a DefaultCategoryDataset. This can then be used to supply the data when creating the line chart.
     * @author Matthew Hebert
     */
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
