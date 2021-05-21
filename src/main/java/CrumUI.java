import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.jfree.data.jdbc.JDBCXYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

/**
 * =======================
 *      CrumUI.java
 * =======================
 * @author Paul Ippolito
 * @version 0.8.9
 *
 * This class is the main UI code for the
 * CRUM application. It contains/connects to
 * the CrumUI.form file, which is the base UI.
 * Within this class, all static components are added
 * to the rootPanel and subsequent panels contained
 * within root's JTabbedPane. Throughout its methods,
 * it will retrieve and display the data within our
 * database for the user. Some data will be displayed
 * as a JFreeChart Line chart.
 */
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
    private JLabel RAMUsedLabel;
    private JLabel RAMSizeLabel;
    private JPanel RAMGraphPanel;
    private JLabel macAddrLabel;
    private JLabel outBoundLabel;
    private JLabel inboundLabel;
    private JPanel netGraphPanel;

    // this ArrayList will store our disks, if more than one
    // use this to edit/refresh each DiskPanel component
    // individually
    public ArrayList<DiskPanel> diskList = new ArrayList<>();
    // Database Connection object, assigned to c from CRUM.java
    private Connection c;

    // SQL Strings and JDBCCategoryDataSets
    public final String sql = "SELECT TIMESTAMP, CORE_USAGE FROM CPU";
    JDBCCategoryDataset dataset;

    public final String ramSql = "SELECT TIMESTAMP, USED_SPACE FROM RAM";
    JDBCCategoryDataset ramDS;

    public final String netSQL = "SELECT TIMESTAMP, INBOUND_TRAFFIC, OUTBOUND_TRAFFIC FROM NETWORK";
    JDBCCategoryDataset netDS;

    // ChartPanels for JFreeChart usage
    private ChartPanel cpuChartPanel;
    private ChartPanel ramChartPanel;
    private ChartPanel netChartPanel;

    // JFreeCharts
    JFreeChart ramChart;
    JFreeChart cpuChart;
    JFreeChart netChart;


    /**
     * This constructor method also handles
     * dynamic tab creation (mainly used for handling
     * multiples of a hardware component, like Disk.
     * Any additional tabs will be added within here,
     * as the constructor has access to tabbedPane1
     * Constructor also handles initial JFreeChart creation
     * for the graphs
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

        /**
         * Initialize JFreeCharts throughout the tabs
         */
        // create CPU chart and add it to cpuGraphPanel
        dataset = new JDBCCategoryDataset(c, sql);
        cpuChart = ChartFactory.createLineChart("CPU Usage", "Time",
                "Utilization", dataset, PlotOrientation.VERTICAL, false, false, false);
        cpuChartPanel = new ChartPanel(cpuChart);
        this.cpuGraphPanel.add(cpuChartPanel, BorderLayout.CENTER);

        // create and add ramChart to ramGraphPanel

        ramDS = new JDBCCategoryDataset(c, ramSql);
        ramChart = ChartFactory.createLineChart("RAM Usage", "Time", "Usage",
                ramDS, PlotOrientation.VERTICAL, false, false, false);
        ramChartPanel = new ChartPanel(ramChart);
        this.RAMGraphPanel.add(ramChartPanel, BorderLayout.CENTER);


        // Network ChartPanel, graph, pray that two lines display
        netDS = new JDBCCategoryDataset(c, netSQL);
        netChart = ChartFactory.createLineChart("Network Traffic", "Time",
                "Traffic", netDS, PlotOrientation.VERTICAL, true, true, true);
        netChartPanel = new ChartPanel(netChart);
        this.netGraphPanel.add(netChartPanel, BorderLayout.CENTER);



        this.pack();

        /**
         * ActionListeners for any JButtons (mostly on Main tab)
         */
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
        // make it full screen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
     * main Tab
     * Disk is separate because they are dynamically added later
     *
     */
    public void refreshMain() throws SQLException {
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
        refreshMain();
        refreshDisks();
        refreshCPU();
        refreshRAM();
        refreshNetwork();
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
            clockSpeedLabel.setText("Clock Speed: " + cpuRS.getDouble("CLOCK_SPEED")  + "GHz");
            physicalCoresLabel.setText("Physical Cores: " + cpuRS.getInt("CORE_PHYSICAL"));
            logicalCoresLabel.setText("Logical Cores: " + cpuRS.getInt("CORE_LOGICAL"));
            usageLabel.setText("Usage: "+ cpuRS.getInt("CORE_USAGE") + "%");
            processesLabel.setText("Processes: " + cpuRS.getLong("NUM_PROCESS"));
        }

        // Re-execute query to change dataset, automatically redraws graph
        dataset.executeQuery(sql);

        cpuStmt.close();
    }

    /**
     * Refreshes the JLabels of RAM
     * @throws SQLException
     */
    public void refreshRAM() throws SQLException {

        // Get and Display RAM total size and usage
        String getRAMData = "SELECT TOTAL_PHYSICAL, USED_SPACE FROM RAM";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(getRAMData);
        while (rs.next()){
            RAMUsedLabel.setText("In Use: " + rs.getLong("USED_SPACE")  + "GB");
            RAMSizeLabel.setText("Total RAM: " + rs.getLong("TOTAL_PHYSICAL") + "GB");
        }

        // Re-execute query to change dataset, automatically redraws graph
        ramDS.executeQuery(ramSql);

        stmt.close();

    }

    /**
     * Refresh the JLabels and ChartPanel for the
     * Network tab
     */
    private void refreshNetwork() throws SQLException {
        String getNetData = "SELECT * FROM NETWORK";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(getNetData);
        while (rs.next()){
            macAddrLabel.setText("MAC Address: "+ rs.getString("MAC_ADDRESS"));
            inboundLabel.setText("Inbound: "+ rs.getInt("INBOUND_TRAFFIC")  + "Mbps");
            outBoundLabel.setText("Outbound: "+rs.getInt("OUTBOUND_TRAFFIC") + "Mbps");
        }

        // re execute query, redraw graph
        netDS.executeQuery(netSQL);

        stmt.close();
    }

}