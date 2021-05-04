import javax.swing.*;

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

    // Defualt constrctor
    DiskPanel(){
        this.add(diskName);
        this.add(readSpeed);
        this.add(writeSpeed);
    }
}
