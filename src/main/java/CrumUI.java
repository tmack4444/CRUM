import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class CrumUI extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JPanel CpuPanel;
    private JPanel RamPanel;
    private JPanel NetworkPanel;
    private JPanel MachinePanel;
    private JLabel titleLabel;
    private JLabel modelLabel;
    private JLabel machineIDLabel;
    private JLabel vendorLabel;
    private JButton NetworkButton;
    private JButton DiskButton;
    private JButton RAMButton;
    private JButton CPUButton;

    // this ArrayList will store our disks, if more than one
    // use this to edit/refresh each DiskPanel component
    // individually
    public ArrayList<DiskPanel> diskList = new ArrayList<>();

    /**
     * This constructor method also handles
     * dynamic tab creation (mainly used for handling
     * multiples of a hardware component, like Disk.
     * Any additional tabs will be added within here,
     * as the constructor has access to tabbedPane1
     * @param title
     */
    public CrumUI(String title){
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(rootPanel);
        // Create and add DiskPanel object for each disk detected
        for(int i=0; i < CRUM.numDisks; i++){
            // +i is added so that we will have disk 0, disk 1, etc
            DiskPanel diskPanel = new DiskPanel();
            this.tabbedPane1.addTab("Disk: "+i, diskPanel);
            diskList.add(diskPanel);
        }
        this.pack();

        // Set CPU main button to switch to CPU tab
        CPUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(1);
            }
        });
        RAMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(2);
            }
        });
        NetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(3);
            }
        });
        // This one worries me as index 4 does not INITIALLY exist
        DiskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedIndex(4);
            }
        });
    }

    /**
     * This method takes in a given frame or
     * extension of JFrame and displays it
     * This method purely exists so I can
     * create a new instance of CrumUI in
     * CRUM.java and call methods from that object
     * @param frame
     */
    public static void createUI(JFrame frame){
        frame.setSize(1000, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * refreshDisks is called to refresh the data on
     * each of the DiskPanels created. By iterating through
     * the diskList, the method will tell the corresponding disk
     * to update its JLabels to whatever the new Disk database
     * values are.
     */
    public void refreshDisks(){
        for (int i=0; i < diskList.size(); i++){
            diskList.get(i).refreshLabels();
        }
    }

}