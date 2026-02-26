
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
        MainMenuPanel mainMenuPanel = new MainMenuPanel(this);
        InventoryPanel inventoryPanel = new InventoryPanel(this);
        PurchasesPanel purchasesPanel = new PurchasesPanel(this);
        TransactionsPanel transactionsPanel = new TransactionsPanel(this);
        POSPanel posPanel = new POSPanel(this);
        TrendsPanel trendsPanel = new TrendsPanel(this);
        MenuPanel menuPanel = new MenuPanel(this);

        // Add screens to container
        container.add(mainMenuPanel, "MAIN");
        container.add(purchasesPanel, "Purchases");
        container.add(menuPanel, "Menu");
        container.add(transactionsPanel, "Transaction");
        container.add(inventoryPanel, "Inventory");
        container.add(posPanel, "POS");
        container.add(trendsPanel, "Trends");

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
