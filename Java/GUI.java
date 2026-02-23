
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class GUI extends JFrame implements ActionListener {

    static JFrame f;

    public static void main(String[] args) {
        //Building the connection
        Connection conn = null;
        //TODO STEP 1 (see line 7)
        String databaseName = "team_71_db";
        String databaseUser = "team_71";
        String databasePassword = "QayyumIsAB1tch";
        String databaseUrl = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", databaseName);
        try {
            conn = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "Opened database successfully");

        String display = "";
        try {
            //create a statement object
            Statement stmt = conn.createStatement();
            //create a SQL statement
            //TODO Step 2 (see line 8)
            String sqlStatement = "SELECT * FROM customers";
            //send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next()) {
                // TODO you probably need to change the column name tat you are retrieving
                //      this command gets the data from the "name" attribute
                display += result.getString("name") + " " + result.getString("phone") + "\n";
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        // create a new frame
        f = new JFrame("DB GUI");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

// create a object
        GUI screen = new GUI();

        // create a panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton button = new JButton("Close");

        button.addActionListener(screen);

        JTextArea textArea = new JTextArea();
        textArea.setText(display);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane);

        // add panel to frame
        f.add(panel);
        // set the size of frame
        f.setSize(400, 400);

        f.setVisible(true);

        //closing the connection
        try {
            conn.close();
            JOptionPane.showMessageDialog(null, "Connection Closed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }

    // if button is pressed
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Close")) {
            f.dispose();
        }
    }
}
