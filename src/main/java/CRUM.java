import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import org.slf4j.*;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {
    static Logger LOGGER = LoggerFactory.getLogger(CRUM.class);
    public static void main(String[] args) throws InterruptedException {
        //
        Connection c = null;
        Statement stmt = null;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            String SerialNum = hal.getComputerSystem().getSerialNumber();
            List<HWDiskStore> disks = hal.getDiskStores();
            for(int i = 0; i < disks.size(); i++){
                HWDiskStore disk = disks.get(i);
                System.out.println();
                System.out.println("disk name: " + disk.getName());
                System.out.println("disk model: " + disk.getModel());
                System.out.println("disk size: " + (disk.getSize() / 1073741824) + " GB");
            }
            while(true){
                for(int i = 0; i < disks.size(); i++) {
                    Calendar calendar = Calendar.getInstance();
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
                    String sql_mach_insert = "INSERT INTO TESTDISC VALUES(?,?,?,?,?,?,?,?)";
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
                TimeUnit.SECONDS.sleep(1);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}

/*

                    String sql_Search = "SELECT * FROM TESTDISC";
                    ResultSet rs = stmt.executeQuery(sql_Search);
                    while(rs.next()) {
                        System.out.println("ID: "+rs.getInt("MACHINE_ID"));
                        System.out.println("TIMESTAMP: "+rs.getTimestamp("TIMESTAMP"));
                        System.out.println("DISC_SIZE: "+rs.getString("DISC_SIZE"));
                        System.out.println("DISC_SPEED: "+rs.getString("DISC_SPEED"));
                        System.out.println();
                    }
        for(int i = 0; i < disks.size(); i++){
            LOGGER.info("Initializing new Disk {}", i);
            HWDiskStore disk = disks.get(i);
            LOGGER.info("disk name:  {}", disk.getName());
            LOGGER.info("disk model: {}", disk.getModel());
            LOGGER.info("disk size:  {} GB", (disk.getSize() / 1073741824));

            System.out.println();
            System.out.println("disk name: " + disk.getName());
            System.out.println("disk model: " + disk.getModel());
            System.out.println("disk size: " + (disk.getSize() / 1073741824) + " GB");

        }
        while(true){
            for(int i = 0; i < disks.size(); i++){
                List<HWDiskStore> Disks = hal.getDiskStores();
                HWDiskStore disk = Disks.get(i);
                LOGGER.info("Disk:  {}", disk.getName());
                LOGGER.info("Reads:  {}", disk.getReads());
                LOGGER.info("Bytes read: {} GB", disk.getReadBytes());
                LOGGER.info("Writes:  {}", disk.getWrites());
                LOGGER.info("Bytes written: {} GB", disk.getWriteBytes());
                LOGGER.info("Time in use: {} \n", disk.getTransferTime());

        Connection c = null;
        Statement stmt = null;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");
            stmt = c.createStatement();
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            String SerialNum = hal.getComputerSystem().getSerialNumber();
            List<HWDiskStore> disks = hal.getDiskStores();
            for(int i = 0; i < disks.size(); i++){
                HWDiskStore disk = disks.get(i);
                System.out.println();
                System.out.println("disk name: " + disk.getName());
                System.out.println("disk model: " + disk.getModel());
                System.out.println("disk size: " + (disk.getSize() / 1073741824) + " GB");
            }
            while(true){
                for(int i = 0; i < disks.size(); i++){
                    Calendar calendar = Calendar.getInstance();
                    java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());

                    List<HWDiskStore> Disks = hal.getDiskStores();
                    HWDiskStore disk = Disks.get(i);
                    int usedSpace = (int) (disk.getSize() - disk.getWriteBytes());
                    String sql_mach_insert = "INSERT INTO TESTDISC VALUES(?,?,?,?,?,?,?,?)";
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
                    /*
                    String sql_Search = "SELECT * FROM TESTDISC";
                    ResultSet rs = stmt.executeQuery(sql_Search);
                    while(rs.next()) {
                        System.out.println("ID: "+rs.getInt("MACHINE_ID"));
                        System.out.println("TIMESTAMP: "+rs.getTimestamp("TIMESTAMP"));
                        System.out.println("DISC_SIZE: "+rs.getString("DISC_SIZE"));
                        System.out.println("DISC_SPEED: "+rs.getString("DISC_SPEED"));
                        System.out.println();
                    }
                    */