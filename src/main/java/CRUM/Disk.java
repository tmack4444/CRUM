package CRUM;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Disk {
    //static Logger LOGGER = LoggerFactory.getLogger(Disk.class);
    public static FileSystem fs;
    public static List<HWDiskStore> disks;
    public static List<OSFileStore> fileStores;
    public static long[] prevFreeSpace;
    public static long[] prevTransferTime;
    public static long[] prevBytesWritten;
    public static long[] prevBytesRead;
    public static Map<Integer, Integer> FileStoresToDisks;
    public static CRUM crum;
    static Statement stmt = null;

    public static int initDisk(CRUM crumObj){
        crum = crumObj;
        fs = crum.si.getOperatingSystem().getFileSystem();
        fileStores = fs.getFileStores();
        disks = crum.hal.getDiskStores();
        prevFreeSpace = new long[fileStores.size()];
        FileStoresToDisks = new HashMap<>();
        prevTransferTime = new long[fileStores.size()];
        prevBytesRead = new long[fileStores.size()];
        prevBytesWritten = new long[fileStores.size()];
        for(int j = 0; j < fileStores.size(); j++){
            OSFileStore currStore = fileStores.get(j);
            prevFreeSpace[j] = currStore.getFreeSpace();
        }
        for(int j = 0; j < disks.size(); j++){
            HWDiskStore disk = disks.get(j);
            prevTransferTime[j] = disk.getTransferTime();
            prevBytesRead[j] = disk.getReadBytes();
            prevBytesWritten[j] = disk.getWriteBytes();
            List <HWPartition> Partitions = disk.getPartitions();
            for(int k = 0; k < Partitions.size(); k++){
                HWPartition partition = Partitions.get(k);
                System.out.println("Partition ID: " + partition.getUuid());
                for(int l = 0; l < fileStores.size(); l++){
                    OSFileStore currStore = fileStores.get(l);
                    System.out.println("Filestore ID: " + currStore.getUUID());
                    if(currStore.getUUID().equals(partition.getUuid())){
                        FileStoresToDisks.put(l, j);
                    }
                }
                //LOGGER.info("Partition:  {}", partition.getMountPoint());
                //LOGGER.info("UUID:  {}", partition.getUuid());
            }
        }
        return fileStores.size();
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
    public static void getDiskData(Calendar calendar, String SerialNum, Connection c){
        try {
            for(int i = 0; i < fileStores.size(); i++) {
                System.out.println("fileStores: " + fileStores.size());
                System.out.println("i: " + i);
                java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
                OSFileStore currStore = fileStores.get(i);
                HWDiskStore currDisk = disks.get(FileStoresToDisks.get(i));
                prevFreeSpace[i] = currStore.getFreeSpace();
                String sql_mach_insert = "INSERT INTO DISC VALUES(?,?,?,?,?,?,?,?)";
                PreparedStatement smi = c.prepareStatement(sql_mach_insert);
                long transferTime = currDisk.getTransferTime() - prevTransferTime[i];
                long writesPerSec = currDisk.getWriteBytes() - prevBytesWritten[i];
                long readsPerSec = currDisk.getReadBytes() - prevBytesRead[i];
                long totalBytesPerSec = writesPerSec + readsPerSec;
                prevTransferTime[i] = currDisk.getTransferTime();
                prevBytesRead[i] = currDisk.getReadBytes();
                prevBytesWritten[i] = currDisk.getWriteBytes();
                currStore.updateAttributes();
                currDisk.updateAttributes();
                smi.setInt(1, i);
                smi.setString(2, SerialNum);
                smi.setTimestamp(3, currentTime);
                smi.setString(4, currStore.getMount());
                smi.setString(5, currDisk.getModel());
                smi.setLong(6, currStore.getTotalSpace() / (1024 * 1024 * 1024));
                smi.setLong(7,  (currStore.getTotalSpace() - currStore.getFreeSpace()) / (1024 * 1024 * 1024));
                smi.setLong(8, totalBytesPerSec / (1024));
                smi.execute();
                //LOGGER.info("Disk:  {}", currStore.getName());
                //LOGGER.info("Description:  {}", currStore.getDescription());
                //LOGGER.info("Label:  {}", currStore.getLabel());
                //LOGGER.info("Logical Volume:  {}", currStore.getLogicalVolume());
                //LOGGER.info("Mount Volume:  {}", currStore.getMount());
                //LOGGER.info("UUID:  {}", currStore.getUUID());
                //LOGGER.info("Reads:  {}", disk.getReads());
                //LOGGER.info("Bytes read: {}", disk.getReadBytes());
                //LOGGER.info("Writes:  {}", disk.getWrites());
                //LOGGER.info("Bytes written: {}", disk.getWriteBytes());
                //LOGGER.info("Writes per second: {} ", writesPerSec);
                //LOGGER.info("Reads per second: {} ", readsPerSec);
                //LOGGER.info("Total bytes per second: {} ", totalBytesPerSec);
                //LOGGER.info("Transfer time: {} ", transferTime);
                //LOGGER.info("usedSpace: {}", currStore.getFreeSpace());
                //LOGGER.info("Total Space in GB: {}", currStore.getTotalSpace() / (1024 * 1024 * 1024));
                //LOGGER.info("usedSpace in GB: {}", (currStore.getTotalSpace() - currStore.getFreeSpace()) / (1024 * 1024 * 1024));
                //LOGGER.info("usable space in GB: {} \n", currStore.getFreeSpace() / (1024 * 1024 * 1024));
                currDisk.updateAttributes();
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
