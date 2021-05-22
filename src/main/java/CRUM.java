import oshi.SystemInfo;
import oshi.hardware.*;
import org.slf4j.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CRUM {
    static Logger LOGGER = LoggerFactory.getLogger(CRUM.class);
    public static SystemInfo si;
    public static HardwareAbstractionLayer hal;
    public static String SerialNum;
    public static Disk diskData;
    public static Memory memoryData;
    public static CPU CPUdata;
    public static List<NetworkIF> netInterfaces;
    public static int numDisks;
    public static long[] baselineBytesIn;
    public static long[] baselineBytesOut;
    static Connection c = null;
    static Statement stmt = null;

    public static void main(String[] args) throws InterruptedException, SQLException {
        Calendar calendar = Calendar.getInstance();
        CRUM crum = new CRUM();
        crum.initDB();
        crum.initOSHI();
        crum.initMachine();
        // Realized I can't manipulate labels accurately
        // unless I do it this way, sorry -Paul
        CrumUI ui = new CrumUI("C.R.U.M", c);
        ui.createUI(ui);
        while(true){
            calendar = Calendar.getInstance();
            diskData.getDiskData(calendar, SerialNum, c);
            CPUdata.getCPUData(calendar, SerialNum, c);
            memoryData.getMemoryData(calendar, SerialNum, c);
            getNetworkData(calendar);
            cullDatabase();
            ui.refresh();
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public void initDB(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:crum.db");
            System.out.println("Opened database successfully");
            stmt = c.createStatement();

            String machine_del = "DROP TABLE IF EXISTS MACHINE";
            stmt.execute(machine_del);

            String disc_del = "DROP TABLE IF EXISTS DISC";
            stmt.execute(disc_del);

            String user_del = "DROP TABLE IF EXISTS USER";
            stmt.execute(user_del);

            String cpu_del = "DROP TABLE IF EXISTS CPU";
            stmt.execute(cpu_del);

            String network_del = "DROP TABLE IF EXISTS NETWORK";
            stmt.execute(network_del);

            String ram_del = "DROP TABLE IF EXISTS RAM";
            stmt.execute(ram_del);


            String sql_machine = "CREATE TABLE IF NOT EXISTS MACHINE " +
                    "(MACHINE_ID TEXT      NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    " MACHINE_MODEL           TEXT    NOT NULL, " +
                    " MACHINE_VENDOR      TEXT     NOT NULL, " +
                    "PRIMARY KEY(MACHINE_ID, TIMESTAMP))";

            stmt.executeUpdate(sql_machine);

            String sql_disc = "CREATE TABLE IF NOT EXISTS DISC " +
                    "(DISC_ID INT      NOT NULL," +
                    "MACHINE_ID TEXT      NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    " DISC_NAME           TEXT    NOT NULL, " +
                    " DISC_MODEL          TEXT     NOT NULL, " +
                    " DISC_SIZE        INT    NOT NULL, " +
                    "DISC_USED        INT   NOT NULL," +
                    "DISC_SPEED INT NOT NULL, " +
                    "PRIMARY KEY(DISC_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_disc);

            String sql_cpu = "CREATE TABLE IF NOT EXISTS CPU " +
                    "(CPU_ID TEXT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL, " +
                    "CPU_MODEL TEXT NOT NULL, " +
                    "CLOCK_SPEED INT NOT NULL, " +
                    "CORE_PHYSICAL INT NOT NULL, " +
                    "CORE_LOGICAL INT NOT NULL, " +
                    "CORE_USAGE INT NOT NULL, " +
                    "NUM_PROCESS INT NOT NULL, " +
                    "PRIMARY KEY(CPU_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_cpu);

            String sql_network = "CREATE TABLE IF NOT EXISTS NETWORK " +
                    "(NETWORK_ID TEXT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "INBOUND_TRAFFIC INT NOT NULL," +
                    "OUTBOUND_TRAFFIC INT NOT NULL," +
                    "MAC_ADDRESS TEXT NOT NULL," +
                    "PRIMARY KEY(NETWORK_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_network);

            String sql_ram = "CREATE TABLE IF NOT EXISTS RAM " +
                    "(RAM_ID INT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "TOTAL_SPACE INT NOT NULL," +
                    "TOTAL_PHYSICAL INT NOT NULL," +
                    "TOTAL_VIRTUAL INT NOT NULL," +
                    "USED_SPACE INT NOT NULL," +
                    "PRIMARY KEY(RAM_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_ram);

            System.out.println("Tables created successfully");

        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Initializes the various OSHI objects that will be used to gather information.
     * We initialize them here rather than in their own method in order to simplify
     * the gathering steps, and ensure that these objects all exist before we attempt to gather data
     */
    public void initOSHI(){
        si = new SystemInfo();
        hal = si.getHardware();
        SerialNum = hal.getComputerSystem().getSerialNumber();
        CPUdata = new CPU();
        memoryData = new Memory();
        diskData = new Disk();
        CPUdata.initCPU(this);
        memoryData.initMemory(this);
        numDisks = diskData.initDisk(this);
        netInterfaces = hal.getNetworkIFs();
        baselineBytesIn = new long[netInterfaces.size()];
        baselineBytesOut = new long[netInterfaces.size()];
        for(int i = 0; i < netInterfaces.size(); i++){
            NetworkIF netIF = netInterfaces.get(i);
            baselineBytesIn[i] += netIF.getBytesRecv();
            baselineBytesOut[i] += netIF.getBytesSent();
            netIF.updateAttributes();
        }

    }

    /**
     * Since the Machine table is static in our application, we only need to pass
     * values to it once. As such, we only need to gather some basic information
     * like Serial Number, Model, and Manufacturer at the start of CRUM. These
     * values will not change during operation, so there is no need to call this function
     * more than once at the start
     *
     */
    public void initMachine() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        String sql_mach_insert = "INSERT INTO MACHINE VALUES(?,?,?,?)";
        PreparedStatement smi = c.prepareStatement(sql_mach_insert);
        smi.setString(1, SerialNum);
        smi.setTimestamp(2, currentTime);
        smi.setString(3, hal.getComputerSystem().getModel());
        smi.setString(4, hal.getComputerSystem().getManufacturer());
        smi.execute();
    }



    public static void getNetworkData(Calendar calendar) throws SQLException {
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        long totalInbound = 0;
        long totalOutbound = 0;
        long currentIn = 0;
        long currentOut = 0;
        String IPs = "";
        String Macs = "";
        for(int i = 0; i < netInterfaces.size(); i++){
            NetworkIF netIF = netInterfaces.get(i);
            netIF.updateAttributes();
            currentIn += netIF.getBytesRecv() - baselineBytesIn[i];
            currentOut += netIF.getBytesSent() - baselineBytesOut[i];
            baselineBytesIn[i] += currentIn;
            baselineBytesOut[i] += currentOut;
            totalInbound += currentIn;
            totalOutbound += currentOut;
            if(totalInbound < 0){
                totalInbound = 0;
            }
            if(totalOutbound < 0){
                totalOutbound = 0;
            }
            String[] currIP = netIF.getIPv4addr();
            for(int j = 0; j < currIP.length; j++){
                IPs += currIP[j];
                if(j < currIP.length-1){
                    IPs += ".";
                }
            }
            IPs += " ";
            Macs += netIF.getMacaddr() + " ";
            //LOGGER.info("Total In {}", totalInbound);
            //LOGGER.info("Total Out {} ", totalOutbound);
            //LOGGER.info("baseLine in {}", baselineBytesIn);
            //LOGGER.info("baseline out {} \n", baselineBytesOut);
        }
        String sql_mach_insert = "INSERT INTO Network VALUES(?,?,?,?,?,?)";
        PreparedStatement smi = c.prepareStatement(sql_mach_insert);
        smi.setString(1, IPs);
        smi.setString(2, SerialNum);
        smi.setTimestamp(3, currentTime);
        smi.setLong(4, (totalInbound * 8)/1000000);
        smi.setLong(5, (totalOutbound * 8)/1000000);
        smi.setString(6, Macs);
        smi.execute();
        //LOGGER.info("IPs: {}", IPs);
        //LOGGER.info("Macs:  {}", Macs);
    }


    public static void cullDatabase() throws SQLException {
        Calendar tempCalendar = Calendar.getInstance();

        int tempDay = tempCalendar.get(Calendar.DAY_OF_YEAR);
        if(tempDay == 0) {
            int tempYear = tempCalendar.get(Calendar.YEAR);
            tempCalendar.set(Calendar.YEAR,tempYear-1);
            tempCalendar.set(Calendar.DAY_OF_YEAR,tempCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        }
        else
            tempCalendar.set(Calendar.DAY_OF_YEAR,tempDay-1);
        java.sql.Timestamp lastMonth = new java.sql.Timestamp(tempCalendar.getTime().getTime());
        LOGGER.info("TimeStamp:  {}", lastMonth.toString());
        String timeSearch = lastMonth.toString();
        String[] timeSplitTemp = timeSearch.split(Pattern.quote("."));
        timeSearch = timeSplitTemp[0];

        String machineDeleteStatement = "DELETE FROM MACHINE WHERE DATETIME(TIMESTAMP)<='"+timeSearch+"'";
        stmt.execute(machineDeleteStatement);
        String diskDeleteStatement = "DELETE FROM DISC WHERE DATETIME(TIMESTAMP)<='"+timeSearch+"'";
        stmt.execute(diskDeleteStatement);
        String cpuDeleteStatement = "DELETE FROM CPU WHERE DATETIME(TIMESTAMP)<='"+timeSearch+"'";
        stmt.execute(cpuDeleteStatement);
        String ramDeleteStatement = "DELETE FROM RAM WHERE DATETIME(TIMESTAMP)<='"+timeSearch+"'";
        stmt.execute(ramDeleteStatement);
        String netDeleteStatement = "DELETE FROM NETWORK WHERE DATETIME(TIMESTAMP)<='"+timeSearch+"'";
        stmt.execute(netDeleteStatement);
    }

}
