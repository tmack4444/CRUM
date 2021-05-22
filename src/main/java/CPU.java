import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class CPU {
    public static CRUM crum;
    public static SystemInfo si;
    public static HardwareAbstractionLayer hal;
    public static CentralProcessor cpu;
    public static long[][] prevLoadTicks;
    public static double[] currLoadTicks;

    public static void initCPU(CRUM crumObj){
        crum = crumObj;
        si = new SystemInfo();
        hal = si.getHardware();
        cpu = hal.getProcessor();
        prevLoadTicks = cpu.getProcessorCpuLoadTicks();
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
    public static void getCPUData(Calendar calendar, String SerialNum, Connection c) throws SQLException {
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
}
