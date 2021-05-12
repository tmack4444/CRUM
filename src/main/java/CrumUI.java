import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
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
    private JLabel physicalCoresLabel;
    private JLabel logicalCoresLabel;
    private JPanel cpuGraphPanel;
    private JLabel cpuModelLabel;
    private JLabel usageLabel;
    private JLabel dummylabel;
    private JLabel processesLabel;
    private JLabel clockSpeedLabel;

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
        // Luckily the disk tabs are added before actual frame creation
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
     * @param c the Database Connection object so we are on the
     *          correct DB
     */
    public void refreshDisks(Connection c) throws SQLException {
        for (int i=0; i < diskList.size(); i++){
            diskList.get(i).refreshLabels(c, i);
        }
    }

    /**
     * This method is basically the same as refreshDisks, but for
     * all the other UI components created by CrumUI.
     * Disk is separate because they are dynamically added later
     * @param c the database connection from CRUM.java
     */
    public void refreshUILabels(Connection c) throws SQLException {
        Statement stmt = c.createStatement();
        // Get and Display Machine data and info from Machine table
        String sqlGetMachineData = "SELECT MACHINE_ID, MACHINE_MODEL, MACHINE_VENDOR FROM MACHINE";
        ResultSet rs = stmt.executeQuery(sqlGetMachineData);
        while (rs.next()){
            modelLabel.setText("Machine Model: " +rs.getString("MACHINE_MODEL"));
            machineIDLabel.setText("Machine ID: " +rs.getString("MACHINE_ID"));
            vendorLabel.setText("Machine Vendor: " +rs.getString("MACHINE_VENDOR"));
        }
        stmt.close();
    }

    /**
     * This method calls all other refresh methods, this way
     * CRUM.java only has to call one method for each update
     * @param c
     * @throws SQLException
     */
    public void refresh(Connection c) throws SQLException {
        refreshUILabels(c);
        refreshDisks(c);
        refreshCPU(c);
    }

    /**
     * Refreshes all JLabels in CPU
     * @param c
     * @throws SQLException
     */
    public void refreshCPU(Connection c) throws SQLException {

        // Get and display CPU data
        String sqlGetCPUData = "SELECT * FROM CPU";
        Statement cpuStmt = c.createStatement();
        ResultSet cpuRS = cpuStmt.executeQuery(sqlGetCPUData);
        while(cpuRS.next()){
            cpuModelLabel.setText(cpuRS.getString("CPU_MODEL"));
            clockSpeedLabel.setText("Clock Speed: " + cpuRS.getInt("CLOCK_SPEED"));
            physicalCoresLabel.setText("Physical Cores: " + cpuRS.getInt("CORE_PHYSICAL"));
            logicalCoresLabel.setText("Logical Cores: " + cpuRS.getInt("CORE_LOGICAL"));
            usageLabel.setText("Usage: "+ cpuRS.getInt("CORE_USAGE"));
            processesLabel.setText("Processes: " + cpuRS.getInt("NUM_PROCESSES"));
        }
    }

}