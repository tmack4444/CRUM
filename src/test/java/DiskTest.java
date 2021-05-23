import CRUM.CRUM;

import java.io.*;
import java.sql.*;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class DiskTest {
    static Connection c = null;
    static Statement stmt = null;

    @Test
    public void initDiskTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        assertNotNull(crum.diskData.fs);
        assertNotNull(crum.diskData.fileStores);
        assertNotNull(crum.diskData.disks);
        assertNotNull(crum.diskData.prevFreeSpace);
        assertNotNull(crum.diskData.FileStoresToDisks);
        assertNotNull(crum.diskData.prevTransferTime);
        assertNotNull(crum.diskData.prevBytesRead);
        assertNotNull(crum.diskData.prevBytesWritten);
        c = DriverManager.getConnection("jdbc:sqlite:C:/tmp/crum.db");
        Calendar calendar = Calendar.getInstance();
        crum.diskData.getDiskData(calendar, crum.SerialNum, c);
        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM DISC ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(0, rs.getInt("DISC_ID"));
        assertEquals(crum.diskData.fileStores.get(0).getMount(), rs.getString("DISC_NAME"));
        assertEquals((crum.diskData.fileStores.get(0).getTotalSpace()/ (1024 * 1024 * 1024)), rs.getLong("DISC_SIZE"));
        c.close();
    }

    @Test
    void getDiskDataTest(){;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:C:/tmp/crum.db");
            CRUM crum = new CRUM();
            Calendar calendar = Calendar.getInstance();
            crum.initOSHI();
            crum.initDB();
            crum.diskData.getDiskData(calendar, crum.SerialNum, c);
            String machineID = crum.hal.getComputerSystem().getSerialNumber();
            String sourceFile = "test.txt";
            stmt = c.createStatement();
            String sql_Search = "SELECT * FROM DISC ";
            ResultSet rs = stmt.executeQuery(sql_Search);
            int initUsed = rs.getInt("DISC_USED");
            OutputStream os = new FileOutputStream("test2.txt");
            InputStream is = new FileInputStream(sourceFile);
            os.write(is.read());
            c.close();
            is.close();
            os.close();
            calendar = Calendar.getInstance();
            c = DriverManager.getConnection("jdbc:sqlite:C:/tmp/crum.db");
            crum.diskData.getDiskData(calendar, crum.SerialNum, c);
            stmt = c.createStatement();
            java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
            String timeSearch = currentTime.toString();
            String[] timeSplitTemp = timeSearch.split(".");
            timeSearch = timeSplitTemp[0];
            String secondSearch = "SELECT * FROM DISC WHERE DATETIME(TIMESTAMP) >= '" + timeSearch + "'";
            rs = stmt.executeQuery(secondSearch);
            int finalUsed = rs.getInt("DISC_USED");
            assertTrue((finalUsed > initUsed));
            System.out.println("CLOSE 2");
            c.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e){

        }
    }
}
