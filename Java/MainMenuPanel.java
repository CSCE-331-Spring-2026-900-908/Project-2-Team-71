
import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openPOS = new JButton("Open POS");
        openPOS.addActionListener(e -> gui.showScreen("POS"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openPOS);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
