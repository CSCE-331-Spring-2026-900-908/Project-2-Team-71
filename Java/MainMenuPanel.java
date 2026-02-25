
import java.awt.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GUI gui) {

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton openTemplate = new JButton("Open Template");
        openTemplate.addActionListener(e -> gui.showScreen("TEMP"));

        JPanel centerPanel = new JPanel();
        centerPanel.add(openTemplate);

        add(title, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
