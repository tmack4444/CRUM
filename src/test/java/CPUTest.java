import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import oshi.hardware.NetworkIF;


public class CPUTest {
    static Connection c = null;
    static Statement stmt = null;

    @Test
    public void initCPU() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        assertNotNull(crum.CPUdata.cpu);
        assertNotNull(crum.CPUdata.prevLoadTicks);
    }

    @Test
    public void getCPUData() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        Calendar calendar = Calendar.getInstance();
        crum.CPUdata.getCPUData(calendar, crum.SerialNum, c);
        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM CPU ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(crum.CPUdata.cpu.getProcessorIdentifier().getProcessorID(), rs.getString("CPU_ID"));
        assertEquals(crum.CPUdata.cpu.getProcessorIdentifier().getName(), rs.getString("CPU_MODEL"));
        assertEquals(crum.CPUdata.cpu.getPhysicalProcessorCount(), rs.getLong("CORE_PHYSICAL"));
        assertEquals(crum.CPUdata.cpu.getLogicalProcessorCount(), rs.getLong("CORE_LOGICAL"));
        c.close();
    }
}
