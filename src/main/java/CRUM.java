import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {

    public static void main(String[] args) throws InterruptedException {
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
                    int usedSpace = disk.getSize() - disk.getWriteBytes();
                    String sql_mach_insert = "INSERT INTO TESTDISC VALUES(?,?,?,?,?,?,?,?,?,?");
                    String sql_disc = "CREATE TABLE IF NOT EXISTS TESTDISC " +
                            "(DISC_ID INT      NOT NULL," +
                            "MACHINE_ID INT      NOT NULL," +
                            "TIMESTAMP TIMESTAMP NOT NULL," +
                            " DISC_NAME           TEXT    NOT NULL, " +
                            " DISC_MODEL          TEXT     NOT NULL, " +
                            " DISC_SIZE        INT    NOT NULL, " +
                            "DISC_USED        INT   NOT NULL," +
                            "DISC_SPEED INT NOT NULL, " +
                            "PRIMARY KEY(DISC_ID, MACHINE_ID, TIMESTAMP)," +
                            "FOREIGN KEY(MACHINE_ID) REFERENCES TESTMACHINE(MACHINE_ID))";
                    stmt.executeUpdate(sql_disc);
                    PreparedStatement smi = c.prepareStatement(sql_mach_insert);
                    smi.setInt(1, i);
                    smi.setInt(2, SerialNum);
                    smi.setTimestamp(3, calendar.getTime().getTime());
                    smi.setString(4, disk.getName());
                    smi.setString(5, disk.getModel());
                    smi.setInt(6, disk.getSize());
                    smi.setInt(7, usedSpace);
                    smi.setInt(8, disk.getTransferTime());
                    smi.execute();
                    /*
                    System.out.println();
                    System.out.println(disk.getName());
                    System.out.println("Reads: " + disk.getReads());
                    System.out.println("Bytes read: " + disk.getReadBytes());
                    System.out.println("Writes: " + disk.getReads());
                    System.out.println("Bytes written: " + disk.getReadBytes());
                    System.out.println("Time in use: " + disk.getTransferTime());
                     */
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}
