
import java.awt.*;
import javax.swing.*;

public class GUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel container;

    public GUI() {
        setTitle("DB GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);

        // Initialize layout
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        // Create screens
        MainMenuPanel menuPanel = new MainMenuPanel(this);
        Inventory inventoryPanel = new Inventory(this);

        // Add screens to container
        container.add(menuPanel, "MAIN");
        container.add(inventoryPanel, "INVENTORY");

        add(container);

        setLocationRelativeTo(null); // center window
        setVisible(true);
    }

    // Method to switch screens
    public void showScreen(String name) {
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
