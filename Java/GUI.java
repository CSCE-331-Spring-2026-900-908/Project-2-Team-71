
import java.awt.*;
import javax.swing.*;
import java.sql.*;

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
        PanelTemplate template = new PanelTemplate(this);
        POSScreen posScreen = new POSScreen(this, conn); //assumoing coonection obj is created elsewhere

        // Add screens to container
        container.add(menuPanel, "MAIN");
        container.add(template, "TEMP");
        container.add(posScreen, "POS");

        add(container);

        setLocationRelativeTo(null); // center window
        setVisible(true);

        showScreen("POS"); // open to the POS page
    }

    // Method to switch screens
    public void showScreen(String name) {
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}