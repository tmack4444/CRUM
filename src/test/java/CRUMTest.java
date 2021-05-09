import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.*;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class CRUMTest {
    static Connection c = null;
    static Statement stmt = null;

  @Test
  void initOSHITest(){
      CRUM crum = new CRUM();
      Calendar calendar = Calendar.getInstance();
      crum.initOSHI(calendar);
      assertNotNull(crum.si);
      assertNotNull(crum.hal);
      assertNotNull(crum.disks);
      assertNotNull(crum.SerialNum);
  }

  @Test
  void getDiskDataTest(){
      CRUM crum = new CRUM();
      Calendar calendar = Calendar.getInstance();
      crum.initOSHI(calendar);
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
          is.close();
          os.close();
          crum.getDiskData(calendar);
          java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
          sql_Search = "SELECT * FROM DISC WHERE TIMESTAMP >= " + currentTime;
          rs = stmt.executeQuery(sql_Search);
          int finalUsed = rs.getInt("DISC_USED");
          assertTrue((finalUsed > initUsed));
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (SQLException throwables) {
          throwables.printStackTrace();
      }
  }
}