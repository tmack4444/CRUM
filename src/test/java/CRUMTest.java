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
      //assertNotNull(crum.disks);
      assertNotNull(crum.SerialNum);
      //assertNotNull(crum.fs);
      assertNotNull(crum.numDisks);
      //assertNotNull(crum.cpu);
      //assertNotNull(crum.prevLoadTicks);
      //assertNotNull(crum.memory);
      //assertNotNull(crum.numMemModules);
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

  /*
  @Test
  public void initRAMTest() throws SQLException {
      CRUM crum = new CRUM();
      crum.initOSHI();
      crum.initDB();
      crum.initMachine();
      c = DriverManager.getConnection("jdbc:sqlite:crum.db");
      long usedSpace = (crum.memory.getTotal() - crum.memory.getAvailable())/1000000;
      Calendar calendar = Calendar.getInstance();
      crum.getMemoryData(calendar);
      stmt = c.createStatement();
      String sql_Search = "SELECT * FROM RAM ";
      ResultSet rs = stmt.executeQuery(sql_Search);
      assertEquals(crum.numMemModules, rs.getInt("RAM_ID"));
      assertEquals(crum.memory.getTotal()/1000000, rs.getLong("TOTAL_SPACE"));
      assertEquals(usedSpace, rs.getLong("USED_SPACE"));
      c.close();
  }

   */

    /*
    @Test
    public void initCPUTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        Calendar calendar = Calendar.getInstance();
        crum.getCPUData(calendar);
        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM CPU ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(crum.cpu.getProcessorIdentifier().getProcessorID(), rs.getString("CPU_ID"));
        assertEquals(crum.cpu.getProcessorIdentifier().getName(), rs.getString("CPU_MODEL"));
        assertEquals(crum.cpu.getPhysicalProcessorCount(), rs.getLong("CORE_PHYSICAL"));
        assertEquals(crum.cpu.getLogicalProcessorCount(), rs.getLong("CORE_LOGICAL"));
        c.close();
    }
     */

/*    @Test
    public void initDiscTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        Calendar calendar = Calendar.getInstance();
        crum.getDiskData(calendar);
        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM DISC ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(0, rs.getInt("DISC_ID"));
        assertEquals(crum.fileStores.get(0).getMount(), rs.getString("DISC_NAME"));
        assertEquals((crum.fileStores.get(0).getTotalSpace()/1000000000), rs.getLong("DISC_SIZE"));
        c.close();
    }
 */

    /*
    @Test
    public void initNetworkTest() throws SQLException {
        CRUM crum = new CRUM();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        Calendar calendar = Calendar.getInstance();
        crum.getNetworkData(calendar);

        String IPs = "";
        String Macs = "";

        for(int i = 0; i < crum.netInterfaces.size(); i++){
            NetworkIF netIF = crum.netInterfaces.get(i);
            netIF.updateAttributes();

            String[] currIP = netIF.getIPv4addr();
            for(int j = 0; j < currIP.length; j++){
                IPs += currIP[j];
                if(j < currIP.length-1){
                    IPs += ".";
                }
            }
            IPs += " ";
            Macs += netIF.getMacaddr() + " ";
            //LOGGER.info("Total In {}", totalInbound);
            //LOGGER.info("Total Out {} ", totalOutbound);
            //LOGGER.info("baseLine in {}", baselineBytesIn);
            //LOGGER.info("baseline out {} \n", baselineBytesOut);
        }

        stmt = c.createStatement();
        String sql_Search = "SELECT * FROM NETWORK ";
        ResultSet rs = stmt.executeQuery(sql_Search);
        assertEquals(IPs, rs.getString("NETWORK_ID"));
        assertEquals(Macs, rs.getString("MAC_ADDRESS"));
        c.close();
    }
    
     */

 /*    @Test
    void getDiskDataTest(){
        CRUM crum = new CRUM();
        Calendar calendar = Calendar.getInstance();
        crum.initOSHI();
        crum.initDB();
        crum.getDiskData(calendar);
        String machineID = crum.hal.getComputerSystem().getSerialNumber();
        String sourceFile = "test.txt";
        try {
            c = DriverManager.getConnection("jdbc:sqlite:crum.db");
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
            c = DriverManager.getConnection("jdbc:sqlite:crum.db");
            stmt = c.createStatement();
            java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
            String timeSearch = currentTime.toString();
            String[] timeSplitTemp = timeSearch.split(".");
            timeSearch = timeSplitTemp[0];
            String secondSearch = "SELECT * FROM DISC WHERE DATETIME(TIMESTAMP) >= '" + timeSearch + "'";
            rs = stmt.executeQuery(secondSearch);
            int finalUsed = rs.getInt("DISC_USED");
            assertTrue((finalUsed > initUsed));
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

  */

}