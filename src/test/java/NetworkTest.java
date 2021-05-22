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


public class NetworkTest {
    static Connection c = null;
    static Statement stmt = null;

    @Test
    void initNetworkTest() throws SQLException {
        CRUM crum = new CRUM();
        Calendar calendar = Calendar.getInstance();
        crum.initOSHI();
        crum.initDB();
        crum.initMachine();
        assertNotNull(crum.networkData.netInterfaces);
        assertNotNull(crum.networkData.baselineBytesIn);
        assertNotNull(crum.networkData.baselineBytesOut);
        c = DriverManager.getConnection("jdbc:sqlite:crum.db");
        crum.networkData.getNetworkData(calendar, crum.SerialNum, c);
        String IPs = "";
        String Macs = "";
        for(int i = 0; i < crum.networkData.netInterfaces.size(); i++){
            NetworkIF netIF = crum.networkData.netInterfaces.get(i);
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
}
