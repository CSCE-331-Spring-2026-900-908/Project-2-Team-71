
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;

public class XReportPanel extends JPanel {

    // ==========================================================
    private GUI gui;
    private static Connection conn;

    public XReportPanel(GUI gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        getConnection();

        createTopBar();

        JButton generateReportButton = new JButton("Generate X-Report");
        generateReportButton.addActionListener(e -> {
            generateReport();
        });

        add(generateReportButton);
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

    private void generateReport() {
        String fileName = "reports/X-Report.md";

        // try-with-resources ensures the writer is closed automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("# X-Report");
            writer.newLine();
            writer.write("## Sales");

            ResultSet sales = getSales();
            try {
                while (sales.next()) {
                    writer.newLine();
                    writer.write("Total Transactions: " + sales.getString("total_transactions"));
                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Food Sales: " + sales.getString("total_food_sales"));
                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Food Items: " + sales.getString("total_food_items"));
                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Drink Sales: " + sales.getString("total_drink_sales"));
                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Drink Items: " + sales.getString("total_drink_items"));
                    writer.newLine();
                    writer.newLine();
                    writer.write("Total Sales: " + sales.getString("total_sales"));
                }
            } catch (SQLException e) {
                writer.newLine();
                writer.write("Error" + e.getMessage());
            }

            ResultSet paymentSummary = getPaymentSummary();
            try {
                writer.newLine();
                writer.write("## Payment Summary");
                writer.newLine();
                writer.write("| Payment Method | Total Transactions | Total Sales |");
                writer.newLine();
                writer.write("|---|---|---|");
                while (sales.next()) {
                    writer.newLine();
                    writer.write(
                            String.format(
                                    "| %s | %s | %s |",
                                    sales.getString("payment_method"),
                                    sales.getString("total_transactions"),
                                    sales.getString("total_sales")
                            )
                    );
                }
            } catch (SQLException e) {
                writer.newLine();
                writer.write("Error" + e.getMessage());
            }

            System.out.println("Successfully wrote to the file: " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ResultSet getSales() {
        String salesQuery = """
SELECT
    -- Total transactions today
    (SELECT COUNT(*) 
     FROM receipt
     WHERE purchase_date::date >= CURRENT_DATE) AS total_transactions,

    -- Food totals
    (SELECT COALESCE(SUM(f.price),0)
     FROM receipt r
     JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
     JOIN food f ON ftr.food_id = f.id
     WHERE r.purchase_date::date >= CURRENT_DATE) AS total_food_sales,

    (SELECT COUNT(*)
     FROM receipt r
     JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
     WHERE r.purchase_date::date >= CURRENT_DATE) AS total_food_items,

    -- Drink totals
    (SELECT COALESCE(SUM(d.price),0)
     FROM receipt r
     JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
     JOIN drink d ON dtr.drink_id = d.id
     WHERE r.purchase_date::date >= CURRENT_DATE) AS total_drink_sales,

    (SELECT COUNT(*)
     FROM receipt r
     JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
     WHERE r.purchase_date::date >= CURRENT_DATE) AS total_drink_items,

    -- Total combined sales
    (
        (SELECT COALESCE(SUM(f.price),0)
         FROM receipt r
         JOIN food_to_receipt ftr ON r.id = ftr.receipt_id
         JOIN food f ON ftr.food_id = f.id
         WHERE r.purchase_date::date >= CURRENT_DATE)

        +

        (SELECT COALESCE(SUM(d.price),0)
         FROM receipt r
         JOIN drink_to_receipt dtr ON r.id = dtr.receipt_id
         JOIN drink d ON dtr.drink_id = d.id
         WHERE r.purchase_date::date >= CURRENT_DATE)
    ) AS total_sales;
""";
        try {

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);

            PreparedStatement stmt = conn.prepareStatement(salesQuery);

            return stmt.executeQuery();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return null;
    }

    private ResultSet getPaymentSummary() {
        String query = """
WITH receipt_totals AS (
    SELECT 
        r.id,
        r.payment_method,
        r.purchase_date,

        COALESCE(
            (SELECT SUM(f.price)
             FROM food_to_receipt ftr
             JOIN food f ON ftr.food_id = f.id
             WHERE ftr.receipt_id = r.id), 0
        )

        +

        COALESCE(
            (SELECT SUM(d.price)
             FROM drink_to_receipt dtr
             JOIN drink d ON dtr.drink_id = d.id
             WHERE dtr.receipt_id = r.id), 0
        ) AS receipt_total

    FROM receipt r
    WHERE r.purchase_date::date = CURRENT_DATE
)

SELECT 
    payment_method,
    COUNT(*) AS total_transactions,
    SUM(receipt_total) AS total_sales
FROM receipt_totals
GROUP BY payment_method;
        """;

        try {
            PreparedStatement stmt = conn.prepareStatement(query);

            return stmt.executeQuery();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return null;

    }
}
