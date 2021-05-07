import javax.swing.*;
import java.awt.*;

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
    private JLabel readSpeed = new JLabel("Read Speed:");
    private JLabel writeSpeed = new JLabel("Write Speed");

    /**
     * The constructor method
     * Since this is a dynamically created
     * JPanel and IntelliJ limitations, I cannot
     * simply open this JPanel in the GUI Designer
     * and manipulate the components there. What I am
     * about to make in this following method is an
     * ungodly mess, but at least the data will be
     * in an easy to read format when it is finished
     */
    DiskPanel(){
        // Set layout and constraints
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5,5,5,5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        this.add(diskName, constraints);
        constraints.gridy = 1;
        this.add(readSpeed, constraints);
        constraints.gridy = 2;
        this.add(writeSpeed, constraints);
    }
}
