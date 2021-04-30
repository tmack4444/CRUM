import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import org.slf4j.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CRUM {
    static Logger LOGGER = LoggerFactory.getLogger(CRUM.class);
    public static void main(String[] args) throws InterruptedException {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<HWDiskStore> disks = hal.getDiskStores();
        for(int i = 0; i < disks.size(); i++){
            LOGGER.info("Initializing new Disk {}", i);
            HWDiskStore disk = disks.get(i);
            LOGGER.info("disk name:  {}", disk.getName());
            LOGGER.info("disk model: {}", disk.getModel());
            LOGGER.info("disk size:  {} GB", (disk.getSize() / 1073741824));
            /*
            System.out.println();
            System.out.println("disk name: " + disk.getName());
            System.out.println("disk model: " + disk.getModel());
            System.out.println("disk size: " + (disk.getSize() / 1073741824) + " GB");
            */
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
            }
            TimeUnit.SECONDS.sleep(1);
        }

    }
}
