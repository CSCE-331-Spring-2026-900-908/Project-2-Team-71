import javax.swing.*;
import java.sql.Connection;

public class POS {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // For now: pass null until we wire DB back in
            new POSScreen((Connection) null);
        });
    }
}