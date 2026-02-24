
import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openPurchases = new JButton("Open Purchases");
        openPurchases.addActionListener(e -> gui.showScreen("Purchases"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openPurchases);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
