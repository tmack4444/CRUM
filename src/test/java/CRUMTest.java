import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
      crum.initOSHI();
      assertNotNull(crum.si);
      assertNotNull(crum.hal);
      assertNotNull(crum.disks);
      assertNotNull(crum.SerialNum);
  }

  @Test
  void getDiskDataTest(){
      CRUM crum = new CRUM();
      crum.initOSHI();
      Calendar calendar = Calendar.getInstance();
      crum.getDiskData(calendar);
      String sourceFile = "test.txt";
      crum.getDiskData(calendar);
      try {
          String sql_Search = "SELECT * FROM TESTMACHINE";
          ResultSet rs = stmt.executeQuery(sql_Search);
          int length;
          byte[] buffer = new byte[1024];
          OutputStream os = new FileOutputStream("test2.txt");
          InputStream is = new FileInputStream(sourceFile);
          while((length = is.read(buffer)) > 0) {
              os.write(buffer, 0, length);
          }
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (SQLException throwables) {
          throwables.printStackTrace();
      }
      crum.getDiskData(calendar);
  }
}