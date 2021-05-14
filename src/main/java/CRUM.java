import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import org.slf4j.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import javax.swing.*;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {
    static Logger LOGGER = LoggerFactory.getLogger(CRUM.class);
    public static List<HWDiskStore> disks;
    public static FileSystem fs;
    public static List<OSFileStore> fileStores;
    public static SystemInfo si;
    public static HardwareAbstractionLayer hal;
    public static String SerialNum;
    public static int numDisks;
    public static CentralProcessor cpu;
    public static long[][] prevLoadTicks;
    public static double[] currLoadTicks;
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
                getDiskData(calendar);
                getCPUData(calendar);
                ui.refresh();
                TimeUnit.SECONDS.sleep(1);
            }
    }

    public void initDB(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");
            stmt = c.createStatement();

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

            String sql_user = "CREATE TABLE IF NOT EXISTS USER " +
                    "(USER_ID INT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "PASSWORD_HASH TEXT NOT NULL," +
                    "PRIMARY KEY(USER_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_user);

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
                    "(NETWORK_ID INT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "INBOUND_WIFI INT NOT NULL," +
                    "OUTBOUND_WIFI INT NOT NULL," +
                    "INBOUND_ETHERNET INT NOT NULL," +
                    "OUTBOUND_ETHERNET INT NOT NULL," +
                    "MAC_ADDRESS TEXT NOT NULL," +
                    "PRIMARY KEY(NETWORK_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_network);

            String sql_ram = "CREATE TABLE IF NOT EXISTS RAM " +
                    "(RAM_ID INT NOT NULL, " +
                    "MACHINE_ID TEXT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "TOTAL_SPACE INT NOT NULL," +
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
        fs = si.getOperatingSystem().getFileSystem();
        fileStores = fs.getFileStores();
        disks = hal.getDiskStores();
        numDisks = disks.size();
        cpu = hal.getProcessor();
        prevLoadTicks = cpu.getProcessorCpuLoadTicks();
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

    /**
     * This function gathers usage statistics from disk and inserts them into the disk table.
     * We make use of the OSHI disks to gather some basic information like name and model,
     * however other details use an OSHI object called a file store
     * While disk is the hardware specifications of each storage device, this means that certain
     * values are not stored in a disk, for example the total size of a disk is the hardware specified
     * size, where 1 KB = 1000 Bytes, instead of 1 KB = 1024 bytes as reported by the operating system and software
     *
     * To get the software values and some other statistics we use something called an OSFileStore, another OSHI
     * object that uses the software reported values for some statistics like correct amount of total space, or
     * amount of space remaining on the disk.
     *
     * @param calendar We pass a calendar into each data gathering method to ensure timestamps are the same for each round of collection
     */
    public static void getDiskData(Calendar calendar){
        try {
          for(int i = 0; i < disks.size(); i++) {
            java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
            HWDiskStore disk = disks.get(i);
            List<OSFileStore> fileStores = fs.getFileStores();
            OSFileStore currStore = fileStores.get(i);
            disk.updateAttributes();
            String sql_mach_insert = "INSERT INTO DISC VALUES(?,?,?,?,?,?,?,?)";
            PreparedStatement smi = c.prepareStatement(sql_mach_insert);
            smi.setInt(1, i);
            smi.setString(2, SerialNum);
            smi.setTimestamp(3, currentTime);
            smi.setString(4, disk.getName());
            smi.setString(5, disk.getModel());
            smi.setLong(6, disk.getSize());
            smi.setLong(7,  (currStore.getTotalSpace() - currStore.getFreeSpace()));
            smi.setLong(8, disk.getTransferTime());
            smi.execute();
              LOGGER.info("Disk:  {}", disk.getName());
              LOGGER.info("Reads:  {}", disk.getReads());
              LOGGER.info("Bytes read: {}", disk.getReadBytes());
              LOGGER.info("Writes:  {}", disk.getWrites());
              LOGGER.info("Bytes written: {}", disk.getWriteBytes());
              LOGGER.info("usedSpace: {}", currStore.getFreeSpace());
              LOGGER.info("Total Space in GB: {}", currStore.getTotalSpace() / (1024 * 1024 * 1024));
              LOGGER.info("usedSpace in GB: {}", (currStore.getTotalSpace() - currStore.getFreeSpace()) / (1024 * 1024 * 1024));
              LOGGER.info("usable space in GB: {}", currStore.getFreeSpace() / (1024 * 1024 * 1024));
              LOGGER.info("Time in use: {} \n", disk.getTransferTime());
          }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Gathers usage statistics from the CPU using an OSHI CPU object. We also make use of a processor
     * Identifier to gather some static information, such as processor ID and name. Most values are fairly standard
     * Except for currentload, a percentage of the total amount of the CPU in use.
     * This uses an OSHI method that returns the percentage load for each logical core on a processor, compared to the
     * Previous load on that processor. We use this information to gather the average amount of usage as a percentage,
     * However in order to break this down to one total CPU average, we have to sum the values returned and divide by the
     * number of cores, resulting in one value for the entire CPU's average usage.
     *
     * @param @param calendar We pass a calendar into each data gathering method to ensure timestamps are the same for each round of collection
     * @throws SQLException
     */
    public static void getCPUData(Calendar calendar) throws SQLException {
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        currLoadTicks = cpu.getProcessorCpuLoadBetweenTicks(prevLoadTicks);          //Returns the percentage of load for each logical processor
        prevLoadTicks = cpu.getProcessorCpuLoadTicks();
        double currentLoad = 0.0;
        for(int i = 0; i  < cpu.getLogicalProcessorCount(); i++){
            currentLoad += currLoadTicks[i];
        }
        currentLoad = (currentLoad / cpu.getLogicalProcessorCount()) * 100;
        String sql_mach_insert = "INSERT INTO CPU VALUES(?,?,?,?,?,?,?,?,?)";
        PreparedStatement smi = c.prepareStatement(sql_mach_insert);
        smi.setString(1, cpu.getProcessorIdentifier().getProcessorID());
        smi.setString(2, SerialNum);
        smi.setTimestamp(3, currentTime);
        smi.setString(4, cpu.getProcessorIdentifier().getName());
        smi.setLong(5, cpu.getMaxFreq());
        smi.setInt(6, cpu.getPhysicalProcessorCount());
        smi.setLong(7,  cpu.getLogicalProcessorCount());
        smi.setDouble(8, currentLoad);
        smi.setLong(9,10);
        smi.execute();
        //LOGGER.info("Context Switches:  {}", cpu.getContextSwitches());
        //LOGGER.info("Curr Load Ticks:  {}", currLoadTicks);
        //LOGGER.info("Prev Load Ticks: {}", prevLoadTicks);
        //LOGGER.info("Current Load: {}", currentLoad);
        //LOGGER.info("Load over 1 Minute:  {}", cpu.getSystemLoadAverage(3));
        //LOGGER.info("Frequency {}", cpu.getCurrentFreq());
        //LOGGER.info("Max Frequency {} \n", cpu.getMaxFreq());
    }

    /**
     * Gathers information about the Memory. This means simply the total space, and the amount being used.
     * Not much here right now, as we are sorting out the RAM_ID value
     *
     * @param @param calendar We pass a calendar into each data gathering method to ensure timestamps are the same for each round of collection
     * @throws SQLException
     */
    public static void getMemoryData(Calendar calendar) throws SQLException {
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        /*
        String sql_ram = "CREATE TABLE IF NOT EXISTS RAM " +
                "(RAM_ID INT NOT NULL, " +
                "MACHINE_ID TEXT NOT NULL," +
                "TIMESTAMP TIMESTAMP NOT NULL," +
                "TOTAL_SPACE INT NOT NULL," +
                "USED_SPACE INT NOT NULL," +
                "PRIMARY KEY(RAM_ID, MACHINE_ID, TIMESTAMP)," +
                "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
        stmt.executeUpdate(sql_ram);
         */
    }



}
