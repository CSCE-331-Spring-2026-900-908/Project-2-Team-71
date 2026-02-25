import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class POSScreen extends JFrame {
    private final Connection conn;

    public POSScreen(Connection conn) {
        this.conn = conn;

        setTitle("POS - Cashier");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        add(new JLabel("Cashier Screen goes here", SwingConstants.CENTER), BorderLayout.CENTER);

        setVisible(true);
    }
}