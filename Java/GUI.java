
import java.awt.*;
import javax.swing.*;

public class GUI extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel container;

    public GUI() {
        setTitle("DB GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);

        // Initialize layout
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        // Create screens
        MainMenuPanel menuPanel = new MainMenuPanel(this);
        PurchasesPanel purchasesPanel = new PurchasesPanel(this);
        TransactionsPanel transactionsPanel = new TransactionsPanel(this);
        POSScreen posScreen = new POSScreen(this);

        // Add screens to container
        container.add(menuPanel, "MAIN");
        container.add(purchasesPanel, "Purchases");
        container.add(transactionsPanel, "Transactions");
        container.add(posScreen, "POS");

        add(container);

        setLocationRelativeTo(null); // center window
        setVisible(true);
    }

    public void showScreen(String name) {
        cardLayout.show(container, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
