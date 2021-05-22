import CRUM.CRUM;

import java.sql.*;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class MemoryTest {
    static Connection c = null;
    static Statement stmt = null;

    @Test
    public void initMemoryTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        assertNotNull(crum.memoryData.memory);
        assertNotNull(crum.memoryData.numMemModules);
    }

    @Test
    public void getMemoryDataTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        long usedSpace = (crum.memoryData.memory.getTotal() - crum.memoryData.memory.getAvailable())/1000000;
        Calendar calendar = Calendar.getInstance();
        crum.memoryData.getMemoryData(calendar, crum.SerialNum, c);
        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM RAM ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(crum.memoryData.numMemModules, rs.getInt("RAM_ID"));
        assertEquals(crum.memoryData.memory.getTotal()/1000000, rs.getLong("TOTAL_SPACE"));
        assertEquals(usedSpace, rs.getLong("USED_SPACE"));
        c.close();
    }

}
