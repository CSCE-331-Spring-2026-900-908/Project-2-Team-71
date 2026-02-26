
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openTrends = new JButton("Open Trends");
        openTrends.addActionListener(e -> gui.showScreen("Trends"));

        JButton openPurchases = new JButton("Open Purchases");
        openPurchases.addActionListener(e -> gui.showScreen("Purchases"));

        JButton openTransaction = new JButton("Open Transaction");
        openTransaction.addActionListener(e -> gui.showScreen("Transaction"));

        JButton openInventory = new JButton("Open Inventory");
        openInventory.addActionListener(e -> gui.showScreen("Inventory"));

        JButton openPOS = new JButton("Open POS");
        openPOS.addActionListener(e -> gui.showScreen("POS"));

        JButton openMenu = new JButton("Open Menu");
        openMenu.addActionListener(e -> gui.showScreen("Menu"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openTrends);
        centerPanel.add(openPurchases);
        centerPanel.add(openTransaction);
        centerPanel.add(openInventory);
        centerPanel.add(openPOS);
        centerPanel.add(openMenu);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
