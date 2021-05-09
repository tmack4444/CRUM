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
      crum.initDB();
      Calendar calendar = Calendar.getInstance();
      crum.getDiskData(calendar);
      String sourceFile = "test.txt";
      try {
          String sql_Search = "SELECT * FROM DISC";
          ResultSet rs = stmt.executeQuery(sql_Search);
          long initReads = rs.getLong("DISC_SPEED");
          OutputStream os = new FileOutputStream("test2.txt");
          InputStream is = new FileInputStream(sourceFile);
          os.write(is.read());
          is.close();
          os.close();
          sql_Search = "SELECT * FROM DISC";
          rs = stmt.executeQuery(sql_Search);
          long finalReads = rs.getLong("DISC_SPEED");
          assertTrue((finalReads > initReads));
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