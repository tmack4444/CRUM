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


public class CRUMTest {
    static Connection c = null;
    static Statement stmt = null;

  @Test
  void initOSHITest(){
      CRUM crum = new CRUM();
      Calendar calendar = Calendar.getInstance();
      crum.initOSHI();
      assertNotNull(crum.si);
      assertNotNull(crum.hal);
      assertNotNull(crum.disks);
      assertNotNull(crum.SerialNum);
  }

  @Test
  void getDiskDataTest(){
      CRUM crum = new CRUM();
      Calendar calendar = Calendar.getInstance();
      crum.initOSHI();
      crum.initDB();
      crum.getDiskData(calendar);
      String machineID = crum.hal.getComputerSystem().getSerialNumber();
      String sourceFile = "test.txt";
      try {
          c = DriverManager.getConnection("jdbc:sqlite:test.db");
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
          crum.getDiskData(calendar);
          c = DriverManager.getConnection("jdbc:sqlite:test.db");
          stmt = c.createStatement();
          java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
          String timeSearch = currentTime.toString();
          String[] timeSplitTemp = timeSearch.split(".");
          timeSearch = timeSplitTemp[0];
          String secondSearch = "SELECT * FROM DISC WHERE DATETIME(TIMESTAMP) >= '" + timeSearch + "'";
          rs = stmt.executeQuery(secondSearch);
          int finalUsed = rs.getInt("DISC_USED");
          assertTrue((finalUsed > initUsed));
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (SQLException throwables) {
          throwables.printStackTrace();
      } catch (Exception e){

      }
  }

  @Test
  public void initDBTest() throws SQLException {
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      stmt = c.createStatement();
      CRUM crum = new CRUM();
      crum.initDB();
      //Somehow check that each table exists here
      DatabaseMetaData meta = c.getMetaData();
      ResultSet res = meta.getTables(null, null, "MACHINE", new String[] {"TABLE"});
      assertEquals("MACHINE", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "DISC", new String[] {"TABLE"});
      assertEquals("DISC", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "USER", new String[] {"TABLE"});
      assertEquals("USER", res.getString("TABLE_NAME"));

  }
}