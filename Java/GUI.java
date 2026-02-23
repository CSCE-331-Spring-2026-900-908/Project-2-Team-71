
import java.awt.event.*;
import javax.swing.*;

public class GUI extends JFrame implements ActionListener {

    static JFrame frame;

    public static void main(String[] args) {

        frame = new JFrame("DB GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a object
        GUI screen = new GUI();

        JPanel panel = POS.ShowGUI(screen);

        JButton button = new JButton("Close");

        button.addActionListener(screen);
        panel.add(button);

        // add panel to frame
        frame.add(panel);
        // set the size of frame
        frame.setSize(400, 400);

        frame.setVisible(true);
    }

    // if button is pressed
    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            frame.removeAll();
        }
    }
}
