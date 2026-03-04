
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
import java.util.Date;
import java.util.List;
import javax.swing.*;

public class SalesReportPanel extends JPanel {

    // ==========================================================
    private final GUI gui;
    private static Connection conn;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;

    private static class SalesData {

        double grossSales;
        double netSales;
        int totalTransactions;
        int validTransactions;
        int voidTransactions;
        double voidAmount;
        double totalFoodSales;
        int totalFoodItems;
        double totalDrinkSales;
        int totalDrinkItems;
        int firstReceipt;
        int lastReceipt;
    }

    private static class PaymentSummaryData {

        String paymentMethod;
        int totalTransactions;
        double grossSales;
        double netSales;
    }

    public SalesReportPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        getConnection();

        createTopBar();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 2, 10, 10));

        centerPanel.add(new JLabel("Start Date:"));
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        centerPanel.add(startDateSpinner);

        centerPanel.add(new JLabel("End Date:"));
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        centerPanel.add(endDateSpinner);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        startDateSpinner.setValue(cal.getTime());

        endDateSpinner.setValue(new Date());

        JButton generateReportButton = new JButton("Generate Sales Report");
        generateReportButton.addActionListener(e -> {
            try {
                generateReport();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        centerPanel.add(generateReportButton);

        add(centerPanel, BorderLayout.CENTER);
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

    private void generateReport() throws SQLException {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Generate Sales Report?",
                "Confirm Sales Report",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String fileName = "reports/Sales Report.md";

        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // try-with-resources ensures the writer is closed automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("# Sales Report");

            writer.newLine();
            writer.newLine();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            writer.write("Generated: " + now.format(formatter));
            writer.newLine();
            writer.newLine();
            writer.write("Business Date: " + now.toLocalDate());

            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            Timestamp startTimestamp = new Timestamp(startDate.getTime());
            Timestamp endTimestamp = new Timestamp(endDate.getTime());

            SalesData sales = getSalesData(startTimestamp, endTimestamp);

            writer.newLine();
            writer.write("### Receipt Range: "
                    + sales.firstReceipt + " - " + sales.lastReceipt);

            writer.newLine();
            writer.write("### Valid Transactions: " + sales.validTransactions);
            writer.newLine();

            writer.write("### Void Transactions: " + sales.voidTransactions);
            writer.newLine();

            writer.write("### Void Amount: " + String.format("%.2f", sales.voidAmount));
            writer.newLine();

            double voidPercent = sales.grossSales == 0
                    ? 0
                    : (sales.voidAmount / sales.grossSales) * 100;

            writer.write("### % of Sales Voided: "
                    + String.format("%.2f", voidPercent) + "%");

            writer.newLine();
            writer.write("### Gross Sales: " + String.format("%.2f", sales.grossSales));

            writer.newLine();
            writer.write("### Net Sales (Excluding Voids): " + String.format("%.2f", sales.netSales));

            writer.newLine();
            writer.write("### Total Transactions: " + sales.totalTransactions);
            writer.newLine();

            double avgTicket = sales.validTransactions == 0
                    ? 0
                    : sales.netSales / sales.validTransactions;

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

            List<PaymentSummaryData> payments
                    = getPaymentSummaryData(startTimestamp, endTimestamp);

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
            };

            JOptionPane.showMessageDialog(this,
                    "Sales Report Generated Successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private SalesData getSalesData(Timestamp start, Timestamp end) {

        String query = """
WITH receipt_totals AS (
    SELECT 
        r.id,
        r.payment_method,

        COALESCE(SUM(f.price), 0) AS food_total,
        COALESCE(SUM(d.price), 0) AS drink_total

    FROM receipt r
    LEFT JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
    LEFT JOIN food f ON ftr.food_id = f.id
    LEFT JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
    LEFT JOIN drink d ON dtr.drink_id = d.id
    WHERE purchase_date >= ?
    AND purchase_date < ?
    GROUP BY r.id, r.payment_method
)

SELECT
    COUNT(*) AS total_transactions,

    COUNT(CASE WHEN payment_method != 'Void' THEN 1 END)
        AS valid_transactions,

    COUNT(CASE WHEN payment_method = 'Void' THEN 1 END)
        AS void_transactions,

    MIN(id) AS first_receipt,
    MAX(id) AS last_receipt,

    SUM(food_total + drink_total) AS gross_sales,

    SUM(CASE WHEN payment_method != 'Void'
             THEN food_total + drink_total
             ELSE 0 END) AS net_sales,

    SUM(CASE WHEN payment_method = 'Void'
             THEN food_total + drink_total
             ELSE 0 END) AS void_amount

FROM receipt_totals;
""";

        SalesData data = new SalesData();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    data.totalTransactions = rs.getInt("total_transactions");
                    data.validTransactions = rs.getInt("valid_transactions");
                    data.voidTransactions = rs.getInt("void_transactions");
                    data.firstReceipt = rs.getInt("first_receipt");
                    data.lastReceipt = rs.getInt("last_receipt");
                    data.grossSales = rs.getDouble("gross_sales");
                    data.netSales = rs.getDouble("net_sales");
                    data.voidAmount = rs.getDouble("void_amount");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return data;
    }

    private List<PaymentSummaryData> getPaymentSummaryData(Timestamp start, Timestamp end) {

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
            WHERE purchase_date >= ?
            AND purchase_date < ?
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

        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PaymentSummaryData data = new PaymentSummaryData();
                    data.paymentMethod = rs.getString("payment_method");
                    data.totalTransactions = rs.getInt("total_transactions");
                    data.grossSales = rs.getDouble("gross_sales");
                    data.netSales = rs.getDouble("net_sales");

                    results.add(data);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return results;
    }
}
