import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import org.slf4j.*;

import javax.swing.*;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {
    static Logger LOGGER = LoggerFactory.getLogger(CRUM.class);
    public static List<HWDiskStore> disks;
    public static SystemInfo si;
    public static HardwareAbstractionLayer hal;
    public static String SerialNum;
    public static int numDisks;
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
            CrumUI ui = new CrumUI("C.R.U.M");
            ui.createUI(ui);
            for(int i = 0; i < disks.size(); i++){
                HWDiskStore disk = disks.get(i);
                System.out.println();
                System.out.println("disk name: " + disk.getName());
                System.out.println("disk model: " + disk.getModel());
                System.out.println("disk size: " + (disk.getSize() / 1073741824) + " GB");
            }
            while(true){
                calendar = Calendar.getInstance();
                getDiskData(calendar);
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
                    "PASSWORD HASH TEXT NOT NULL," +
                    "PRIMARY KEY(USER_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES MACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_user);

            System.out.println("Tables created successfully");

        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public void initOSHI(){
        si = new SystemInfo();
        hal = si.getHardware();
        SerialNum = hal.getComputerSystem().getSerialNumber();
        disks = hal.getDiskStores();
        numDisks = disks.size();
    }

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

    public static void getDiskData(Calendar calendar){
        try {
          for(int i = 0; i < disks.size(); i++) {
            java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
            List<HWDiskStore> Disks = hal.getDiskStores();
            HWDiskStore disk = Disks.get(i);
            LOGGER.info("Disk:  {}", disk.getName());
            LOGGER.info("Reads:  {}", disk.getReads());
            LOGGER.info("Bytes read: {} GB", disk.getReadBytes());
            LOGGER.info("Writes:  {}", disk.getWrites());
            LOGGER.info("Bytes written: {} GB", disk.getWriteBytes());
            LOGGER.info("Time in use: {} \n", disk.getTransferTime());
            int usedSpace = (int) (disk.getSize() - disk.getWriteBytes());
            String sql_mach_insert = "INSERT INTO DISC VALUES(?,?,?,?,?,?,?,?)";
            PreparedStatement smi = c.prepareStatement(sql_mach_insert);
            smi.setInt(1, i);
            smi.setString(2, SerialNum);
            smi.setTimestamp(3, currentTime);
            smi.setString(4, disk.getName());
            smi.setString(5, disk.getModel());
            smi.setLong(6, disk.getSize());
            smi.setInt(7, usedSpace);
            smi.setLong(8, disk.getTransferTime());
            smi.execute();
          }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}
