
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * The XReportPanel class provides a graphical user interface for generating X-Reports.
 * An X-Report is a snapshot of the current day's sales at a specific point in time
 * without resetting the register or the daily totals.
 * @author Esteban Di Loreto
 */

public class XReportPanel extends JPanel {

    // ==========================================================
    private final GUI gui;
    private static Connection conn;

    private static class SalesData {

        double grossSales;
        double netSales;
        int totalTransactions;
        double totalFoodSales;
        int totalFoodItems;
        double totalDrinkSales;
        int totalDrinkItems;
    }

    private static class PaymentSummaryData {

        String paymentMethod;
        int totalTransactions;
        double grossSales;
        double netSales;
    }

    /**
     * Constructs a new XReportPanel.
     * This constructor initializes the panel layout and connects it to the main GUI.
     * @param gui The main GUI controller used to transition between screens.
     */

    public XReportPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        getConnection();

        createTopBar();

        JButton generateReportButton = new JButton("Generate X-Report");
        generateReportButton.addActionListener(e -> {
            generateReport();
        });

        add(generateReportButton, BorderLayout.CENTER);
    }

    // ===================== TOP BAR =====================
    private void createTopBar() {

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("Menu");
        backButton.addActionListener(e -> gui.showScreen("MAIN"));
        topBar.add(backButton);

        add(topBar, BorderLayout.NORTH);
    }

    // Get connection to database
    private static void getConnection() {

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

            conn = DriverManager.getConnection(
                    databaseUrl,
                    databaseUser,
                    databasePassword
            );

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Generates the X-Report by querying the database for all sales made during the current shift.
     * The results are displayed in a table within the panel.
     */

    private void generateReport() {
        String fileName = "reports/X-Report.md";

        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // try-with-resources ensures the writer is closed automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("# X-Report");

            writer.newLine();
            writer.newLine();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            writer.write("Generated: " + now.format(formatter));
            writer.newLine();
            writer.newLine();
            writer.write("Business Date: " + now.toLocalDate());
            writer.newLine();

            SalesData sales = getSalesData();

            writer.newLine();
            writer.newLine();
            writer.write("### Gross Sales: " + String.format("%.2f", sales.grossSales));
            writer.newLine();
            writer.write("### Net Sales (Excluding Voids): " + String.format("%.2f", sales.netSales));
            writer.newLine();
            writer.write("### Total Transactions: " + sales.totalTransactions);
            writer.newLine();

            double avgTicket = sales.totalTransactions == 0
                    ? 0
                    : sales.netSales / sales.totalTransactions;

            writer.write("### Average Ticket: " + String.format("%.2f", avgTicket));
            writer.newLine();
            writer.newLine();

            double totalSales = sales.grossSales;

            double foodPercent = totalSales == 0
                    ? 0
                    : (sales.totalFoodSales / totalSales) * 100;

            double drinkPercent = totalSales == 0
                    ? 0
                    : (sales.totalDrinkSales / totalSales) * 100;

            writer.newLine();
            writer.write("### Food Sales: "
                    + String.format("%.2f", sales.totalFoodSales)
                    + " (" + String.format("%.2f", foodPercent) + "%)");
            writer.newLine();

            writer.write("### Drink Sales: "
                    + String.format("%.2f", sales.totalDrinkSales)
                    + " (" + String.format("%.2f", drinkPercent) + "%)");
            writer.newLine();

            List<PaymentSummaryData> payments = getPaymentSummaryData();

            writer.newLine();
            writer.newLine();
            writer.write("# Payment Summary");
            writer.newLine();
            writer.write("| Payment Method | Transactions | % of Txns | Gross Sales | % of Sales |");
            writer.newLine();
            writer.write("|---|---|---|---|---|");

            for (PaymentSummaryData p : payments) {

                double percentOfSales = sales.grossSales == 0
                        ? 0
                        : (p.grossSales / sales.grossSales) * 100;

                double percentOfTransactions = sales.totalTransactions == 0
                        ? 0
                        : ((double) p.totalTransactions / sales.totalTransactions) * 100;

                writer.newLine();
                writer.write(String.format(
                        "| %s | %d | %.2f%% | %.2f | %.2f%% |",
                        p.paymentMethod,
                        p.totalTransactions,
                        percentOfTransactions,
                        p.grossSales,
                        percentOfSales
                ));
            }

            writer.newLine();
            writer.write("# Hourly Sales Breakdown");
            writer.newLine();
            writer.write("| Hour | Total Tx | Valid Tx | Void Tx | Gross Sales | Net Sales |");
            writer.newLine();
            writer.write("|---|---|---|---|---|---|");

            List<double[]> hourlySales = getHourlySales();

            for (double[] hourData : hourlySales) {
                writer.newLine();
                writer.write(String.format(
                        "| %02d:00 | %d | %d | %d | %.2f | %.2f |",
                        (int) hourData[0],
                        (int) hourData[1],
                        (int) hourData[2],
                        (int) hourData[3],
                        hourData[4],
                        hourData[5]
                ));
            }

            JOptionPane.showMessageDialog(this,
                    "X-Report Generated Successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private SalesData getSalesData() {

        String query = """
        WITH receipt_totals AS (
            SELECT 
                r.id,
                r.payment_method,

                COALESCE(SUM(f.price), 0) AS food_total,
                COUNT(f.id) AS food_items,

                COALESCE(SUM(d.price), 0) AS drink_total,
                COUNT(d.id) AS drink_items

            FROM receipt r
            LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
            LEFT JOIN food f ON ftr.food_id = f.id
            LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
            LEFT JOIN drink d ON dtr.drink_id = d.id
            WHERE r.purchase_date::date = CURRENT_DATE
            AND r.z_closed = FALSE
            GROUP BY r.id, r.payment_method
        )

        SELECT
            COUNT(*) AS total_transactions,
            SUM(food_total + drink_total) AS gross_sales,
            SUM(CASE WHEN payment_method != 'Void'
                     THEN food_total + drink_total
                     ELSE 0 END) AS net_sales,
            SUM(food_total) AS total_food_sales,
            SUM(food_items) AS total_food_items,
            SUM(drink_total) AS total_drink_sales,
            SUM(drink_items) AS total_drink_items
        FROM receipt_totals;
    """;

        SalesData data = new SalesData();

        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                data.totalTransactions = rs.getInt("total_transactions");
                data.grossSales = rs.getDouble("gross_sales");
                data.netSales = rs.getDouble("net_sales");
                data.totalFoodSales = rs.getDouble("total_food_sales");
                data.totalFoodItems = rs.getInt("total_food_items");
                data.totalDrinkSales = rs.getDouble("total_drink_sales");
                data.totalDrinkItems = rs.getInt("total_drink_items");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return data;
    }

    private List<double[]> getHourlySales() {
        List<double[]> hourlySales = new ArrayList<>();

        String query = """
        WITH receipt_totals AS (
            SELECT
                EXTRACT(HOUR FROM r.purchase_date) AS hour,
                r.payment_method,
                COALESCE(SUM(f.price),0) AS food_total,
                COALESCE(SUM(d.price),0) AS drink_total
            FROM receipt r
            LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
            LEFT JOIN food f ON ftr.food_id = f.id
            LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
            LEFT JOIN drink d ON dtr.drink_id = d.id
            WHERE r.z_closed = FALSE
            GROUP BY r.id, r.payment_method
        )
        SELECT 
            hour,
            COUNT(*) AS total_transactions,
            COUNT(CASE WHEN payment_method != 'Void' THEN 1 END) AS valid_transactions,
            COUNT(CASE WHEN payment_method = 'Void' THEN 1 END) AS void_transactions,
            SUM(food_total + drink_total) AS gross_sales,
            SUM(CASE WHEN payment_method != 'Void' THEN food_total + drink_total ELSE 0 END) AS net_sales
        FROM receipt_totals
        GROUP BY hour
        ORDER BY hour;
    """;

        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                double hour = rs.getDouble("hour");
                double totalTx = rs.getDouble("total_transactions");
                double validTx = rs.getDouble("valid_transactions");
                double voidTx = rs.getDouble("void_transactions");
                double gross = rs.getDouble("gross_sales");
                double net = rs.getDouble("net_sales");

                hourlySales.add(new double[]{hour, totalTx, validTx, voidTx, gross, net});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving hourly sales: " + e.getMessage());
        }

        return hourlySales;
    }

    private List<PaymentSummaryData> getPaymentSummaryData() {

        String query = """
        WITH receipt_totals AS (
            SELECT 
                r.id,
                r.payment_method,

                COALESCE(SUM(f.price), 0) +
                COALESCE(SUM(d.price), 0) AS receipt_total

            FROM receipt r
            LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
            LEFT JOIN food f ON ftr.food_id = f.id
            LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
            LEFT JOIN drink d ON dtr.drink_id = d.id
            WHERE r.purchase_date::date = CURRENT_DATE
            AND r.z_closed = FALSE
            GROUP BY r.id, r.payment_method
        )

        SELECT
            payment_method,
            COUNT(*) AS total_transactions,
            SUM(receipt_total) AS gross_sales,
            SUM(CASE WHEN payment_method != 'Void'
                     THEN receipt_total
                     ELSE 0 END) AS net_sales
        FROM receipt_totals
        GROUP BY payment_method
        ORDER BY gross_sales DESC;
    """;

        List<PaymentSummaryData> results = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PaymentSummaryData data = new PaymentSummaryData();
                data.paymentMethod = rs.getString("payment_method");
                data.totalTransactions = rs.getInt("total_transactions");
                data.grossSales = rs.getDouble("gross_sales");
                data.netSales = rs.getDouble("net_sales");

                results.add(data);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return results;
    }
}
