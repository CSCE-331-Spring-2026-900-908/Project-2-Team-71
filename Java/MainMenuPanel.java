
import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openInventory = new JButton("Open Inventory");
        openInventory.addActionListener(e -> gui.showScreen("INVENTORY"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openInventory);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
