import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import java.sql.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {

    public static void main(String[] args) throws InterruptedException {
        c = DriverManager.getConnection("jdbc:sqlite:test.db");
        System.out.println("Opened database successfully");
        stmt = c.createStatement();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
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
                List<HWDiskStore> Disks = hal.getDiskStores();
                HWDiskStore disk = Disks.get(i);
                System.out.println();
                System.out.println(disk.getName());
                System.out.println("Reads: " + disk.getReads());
                System.out.println("Bytes read: " + disk.getReadBytes());
                System.out.println("Writes: " + disk.getReads());
                System.out.println("Bytes written: " + disk.getReadBytes());
                System.out.println("Time in use: " + disk.getTransferTime());
            }
            TimeUnit.SECONDS.sleep(1);
        }

    }
}
