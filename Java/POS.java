import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class POS {

    private static Connection connectToDatabase() {
        Properties props = new Properties();
        String envFile = Paths.get(".env").toAbsolutePath().toString();

        try (FileInputStream inputStream = new FileInputStream(envFile)) {
            props.load(inputStream);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading .env file: " + e.getMessage());
            return null;
        }

        String databaseUrl = props.getProperty("DATABASE_URL") + props.getProperty("DATABASE_NAME");
        String databaseUser = props.getProperty("DATABASE_USER");
        String databasePassword = props.getProperty("DATABASE_PASSWORD");

        try {
            return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "DB Connection error: " + e.getMessage());
            return null;
        }
    }

    public static JPanel ShowGUI(GUI screen) {
        Connection conn = connectToDatabase(); 
        return new POSScreen(screen, conn);
    }
}