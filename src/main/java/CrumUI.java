import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.jdbc.JDBCCategoryDataset;

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
    private JLabel processesLabel;
    private JLabel clockSpeedLabel;

    // this ArrayList will store our disks, if more than one
    // use this to edit/refresh each DiskPanel component
    // individually
    public ArrayList<DiskPanel> diskList = new ArrayList<>();
    // Database Connection object, assigned to c from CRUM.java
    private Connection c;
    private ChartPanel cpuChartPanel;

    /**
     * This constructor method also handles
     * dynamic tab creation (mainly used for handling
     * multiples of a hardware component, like Disk.
     * Any additional tabs will be added within here,
     * as the constructor has access to tabbedPane1
     * @param title title of Frame
     * @param c Database Connection
     */
    public CrumUI(String title, Connection c) throws SQLException {
        super(title);

        // set c for this instance to whatever c was passed
        this.c = c;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(rootPanel);

        // Create and add DiskPanel object for each disk detected
        for(int i=0; i < CRUM.numDisks; i++){
            // +i is added so that we will have disk 0, disk 1, etc
            DiskPanel diskPanel = new DiskPanel();
            this.tabbedPane1.addTab("Disk: "+i, diskPanel);
            diskList.add(diskPanel);
        }

        // create CPU chart and add it to cpuGraphPanel
        String sql = "SELECT TIMESTAMP, CORE_USAGE FROM CPU";
        JDBCCategoryDataset dataset = new JDBCCategoryDataset(c, sql);
        JFreeChart cpuChart = ChartFactory.createLineChart("CPU Usage", "Time",
                "Utilization", dataset, PlotOrientation.VERTICAL, false, false, false);
        cpuChartPanel = new ChartPanel(cpuChart);
        this.cpuGraphPanel.add(cpuChartPanel, BorderLayout.CENTER);

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
        // Also yes, this button only goes to Disk:0
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
    public void refreshDisks() throws SQLException {
        for (int i=0; i < diskList.size(); i++){
            diskList.get(i).refreshLabels(c, i);
        }
    }

    /**
     * This method is basically the same as refreshDisks, but for
     * all the other UI components created by CrumUI.
     * Disk is separate because they are dynamically added later
     *
     */
    public void refreshUILabels() throws SQLException {
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
     *
     * @throws SQLException
     */
    public void refresh() throws SQLException {
        refreshUILabels();
        refreshDisks();
        refreshCPU();
    }

    /**
     * Refreshes all JLabels in CPU
     * @throws SQLException
     */
    public void refreshCPU() throws SQLException {

        // Get and display CPU data
        String sqlGetCPUData = "SELECT CPU_MODEL, CLOCK_SPEED, CORE_PHYSICAL, " +
                "CORE_LOGICAL, CORE_USAGE, NUM_PROCESS FROM CPU";
        Statement cpuStmt = c.createStatement();
        ResultSet cpuRS = cpuStmt.executeQuery(sqlGetCPUData);
        while(cpuRS.next()){
            cpuModelLabel.setText(cpuRS.getString("CPU_MODEL"));
            clockSpeedLabel.setText("Clock Speed: " + cpuRS.getDouble("CLOCK_SPEED") / 1000000000 + "GHz");
            physicalCoresLabel.setText("Physical Cores: " + cpuRS.getInt("CORE_PHYSICAL"));
            logicalCoresLabel.setText("Logical Cores: " + cpuRS.getInt("CORE_LOGICAL"));
            usageLabel.setText("Usage: "+ cpuRS.getInt("CORE_USAGE") + "%");
            processesLabel.setText("Processes: " + cpuRS.getLong("NUM_PROCESS"));
        }

        cpuStmt.close();
    }

}