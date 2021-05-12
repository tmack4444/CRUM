import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * File: DiskPanel.java
 * @author Paul Ippolito
 *
 * This class creates a new JPanel which contains
 * the collected disk metrics. The purpose of
 * this class is to account for multiple Disks
 * in the user's machine. If the main CRUM API
 * detects multiple Disks, it will create a new Disk tab for
 * each and add them to the main JTabbedPane located in
 * CrumUI.java
 */
public class DiskPanel extends JPanel {
    private JLabel diskName = new JLabel("Disk:0");
    private JLabel reads = new JLabel("Read Speed:");
    private JLabel writes = new JLabel("Write Speed");
    private JLabel model = new JLabel("Disk Model: ");
    private JLabel diskSize = new JLabel("Size available: ");
    private JLabel amountUsed = new JLabel("Amount used: ");
    private JLabel speed = new JLabel("Disk speed: ");


    /**
     * The constructor method
     * Since this is a dynamically created
     * JPanel and IntelliJ limitations, I cannot
     * simply open this JPanel in the GUI Designer
     * and manipulate the components there. What I am
     * about to make in this following method is an
     * ungodly mess, but at least the data will be
     * in an easy to read format when it is finished.
     * I doubt it will be in a nice format, but it will
     * be readable
     */
    DiskPanel(){
        // Set preferred sizes so that the JLabels actually display in full
        Dimension dim = new Dimension(10000, 10);
        diskName.setPreferredSize(dim);
        reads.setPreferredSize(dim);
        writes.setPreferredSize(dim);
        model.setPreferredSize(dim);
        diskSize.setPreferredSize(dim);
        amountUsed.setPreferredSize(dim);
        speed.setPreferredSize(dim);

        // Set layout
        // yes, null is generally bad, however, I tried to use the layout
        // managers nicely in code. It did not want to cooperate, so now
        // we have this. It looks how I want it to and that's what matters
        this.setLayout(null);
        // add diskName
        this.add(diskName);
        diskName.setBounds(new Rectangle(new Point(10, 0), diskName.getPreferredSize()));

        // add reads and writes
        this.add(reads);
        reads.setBounds(new Rectangle(new Point(10, 15), reads.getPreferredSize()));
        this.add(writes);
        writes.setBounds(new Rectangle(new Point(10, 35), writes.getPreferredSize()));

        // add model
        this.add(model);
        model.setBounds(new Rectangle(new Point(10, 55), model.getPreferredSize()));

        // add disk size
        this.add(diskSize);
        diskSize.setBounds(new Rectangle(new Point(10, 75), diskSize.getPreferredSize()));

        // add amount of disk being used
        this.add(amountUsed);
        amountUsed.setBounds(new Rectangle(new Point(10, 95), amountUsed.getPreferredSize()));

        // add speed of disk
        this.add(speed);
        speed.setBounds(new Rectangle(new Point(10, 115), speed.getPreferredSize()));
    }

    /**
     * This method will change the
     * JLabels contained within the corresponding
     * DiskPanel object to the correct/updated
     * database value
     * @param c Connection: Database Connection Object
     * @param i Int: Desired ID of Disk
     */
    public void refreshLabels(Connection c, int i) throws SQLException {
        Statement stmt = c.createStatement();
        // We pass i in so it gets the correct Disk, that way Disk 0 and Disk 1
        // won't have the same data
        String sqlGetDiskData = "SELECT * FROM DISC WHERE DISC_ID = " + i;
        ResultSet rs = stmt.executeQuery(sqlGetDiskData);
        while(rs.next()){
            diskName.setText("Disk Name: " + rs.getString("DISC_NAME"));
            model.setText("Disk Model: " + rs.getString("DISC_MODEL"));
            diskSize.setText("Disk Size: " + rs.getLong("DISC_SIZE") + " GB");
            amountUsed.setText("Disk Used: " +rs.getLong("DISC_USED") + " GB");
            speed.setText("Disk Speed: " + rs.getInt("DISC_SPEED"));
        }
    }
}
