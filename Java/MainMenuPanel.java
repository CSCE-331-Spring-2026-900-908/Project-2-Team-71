
import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel centerPanel = new JPanel();

        JButton openInventory = new JButton("Open Inventory");
        openInventory.addActionListener(e -> gui.showScreen("Inventory"));

        JButton openPurchases = new JButton("Open Purchases");
        openPurchases.addActionListener(e -> gui.showScreen("Purchases"));

        JButton openPOS = new JButton("Open POS");
        openPOS.addActionListener(e -> gui.showScreen("POS"));

        JButton openTransaction = new JButton("Open Transaction");
        openTransaction.addActionListener(e -> gui.showScreen("Transaction"));

        centerPanel.add(openPOS);
        centerPanel.add(openInventory);
        centerPanel.add(openPurchases);
        centerPanel.add(openTransaction);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
