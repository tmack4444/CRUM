import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Memory {
    public static List<HWDiskStore> disks;
    static Logger LOGGER = LoggerFactory.getLogger(Memory.class);
    public static CRUM crum;
    public static GlobalMemory memory;
    public static int numMemModules;
    static Statement stmt = null;

    public static void initMemory(CRUM crumObj){
        crum = crumObj;
        memory = crum.hal.getMemory();
        numMemModules = memory.getPhysicalMemory().size();
    }

    /**
     * Gathers information about the Memory. This means simply the total space, and the amount being used.
     * We use the number of physical modules of RAM as the key, since that should not change during use
     * (and if it does, there are much bigger problems than CRUM not functioning correctly, such as windows crashing)
     * Other than that, we just grab some basic memory metrics from OSHI.
     *
     * @param @param calendar We pass a calendar into each data gathering method to ensure timestamps are the same for each round of collection
     * @throws SQLException
     */
    public static void getMemoryData(Calendar calendar, String SerialNum, Connection c) throws SQLException {
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        long usedMemory = memory.getTotal() - memory.getAvailable();
        long totalPhysMem = 0;
        for(int i = 0; i < memory.getPhysicalMemory().size(); i++){
            //LOGGER.info("Memory Bank: {}", i);
            //LOGGER.info("Memory in that module:  {}", memory.getPhysicalMemory().get(i).getCapacity());
            totalPhysMem += memory.getPhysicalMemory().get(i).getCapacity();
        }
        String sql_mach_insert = "INSERT INTO RAM VALUES(?,?,?,?,?,?,?)";
        PreparedStatement smi = c.prepareStatement(sql_mach_insert);
        smi.setLong(1, numMemModules);
        smi.setString(2, SerialNum);
        smi.setTimestamp(3, currentTime);
        smi.setLong(4, memory.getTotal()/1000000);
        smi.setLong(5, totalPhysMem/1000000);
        smi.setLong(6, memory.getVirtualMemory().getVirtualMax()/1000000);
        smi.setLong(7, usedMemory/1000000);
        smi.execute();
        //LOGGER.info("Num Mem Modules: {}", numMemModules);
        //LOGGER.info("Total Memory:  {}", memory.getTotal());
        //LOGGER.info("Total Physical:  {}", totalPhysMem);
        //LOGGER.info("Total Virtual:  {}", memory.getVirtualMemory().getVirtualMax());
        //LOGGER.info("Used Memory {} \n", usedMemory);

    }
}
