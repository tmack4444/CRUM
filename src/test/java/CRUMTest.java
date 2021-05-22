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
      assertNotNull(crum.diskData);
      assertNotNull(crum.SerialNum);
      assertNotNull(crum.numDisks);
      assertNotNull(crum.CPUdata);
      assertNotNull(crum.memoryData);
  }

  @Test
  public void initDBTest() throws SQLException {
      c = DriverManager.getConnection("jdbc:sqlite:crum.db");
      stmt = c.createStatement();
      CRUM crum = new CRUM();
      crum.initDB();
      //Somehow check that each table exists here
      DatabaseMetaData meta = c.getMetaData();
      ResultSet res = meta.getTables(null, null, "MACHINE", new String[] {"TABLE"});
      assertEquals("MACHINE", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "DISC", new String[] {"TABLE"});
      assertEquals("DISC", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "CPU", new String[] {"TABLE"});
      assertEquals("CPU", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "RAM", new String[] {"TABLE"});
      assertEquals("RAM", res.getString("TABLE_NAME"));
      res = meta.getTables(null, null, "NETWORK", new String[] {"TABLE"});
      assertEquals("NETWORK", res.getString("TABLE_NAME"));
      c.close();
  }

  @Test
  public void initMachineTest() throws SQLException {
      CRUM crum = new CRUM();
      crum.initOSHI();
      crum.initDB();
      crum.initMachine();
      c = DriverManager.getConnection("jdbc:sqlite:crum.db");
      stmt = c.createStatement();
      String sql_Search = "SELECT * FROM MACHINE ";
      ResultSet rs = stmt.executeQuery(sql_Search);
      assertEquals(crum.SerialNum, rs.getString("MACHINE_ID"));
      assertEquals(crum.hal.getComputerSystem().getModel(), rs.getString("MACHINE_MODEL"));
      assertEquals(crum.hal.getComputerSystem().getManufacturer(), rs.getString("MACHINE_VENDOR"));
      c.close();
  }

}