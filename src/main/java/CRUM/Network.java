package CRUM;

import oshi.hardware.NetworkIF;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class Network {
    public static CRUM crum;
    public static long[] baselineBytesIn;
    public static long[] baselineBytesOut;
    public static List<NetworkIF> netInterfaces;

    public static void initNetwork(CRUM crumObj){
        crum = crumObj;
        netInterfaces = crum.hal.getNetworkIFs();
        baselineBytesIn = new long[netInterfaces.size()];
        baselineBytesOut = new long[netInterfaces.size()];
        for(int i = 0; i < netInterfaces.size(); i++){
            NetworkIF netIF = netInterfaces.get(i);
            baselineBytesIn[i] += netIF.getBytesRecv();
            baselineBytesOut[i] += netIF.getBytesSent();
            netIF.updateAttributes();
        }
    }


    public static void getNetworkData(Calendar calendar, String SerialNum, Connection c) throws SQLException {
        java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());
        long totalInbound = 0;
        long totalOutbound = 0;
        long currentIn = 0;
        long currentOut = 0;
        String IPs = "";
        String Macs = "";
        for(int i = 0; i < netInterfaces.size(); i++){
            NetworkIF netIF = netInterfaces.get(i);
            netIF.updateAttributes();
            currentIn += netIF.getBytesRecv() - baselineBytesIn[i];
            currentOut += netIF.getBytesSent() - baselineBytesOut[i];
            baselineBytesIn[i] += currentIn;
            baselineBytesOut[i] += currentOut;
            totalInbound += currentIn;
            totalOutbound += currentOut;
            if(totalInbound < 0){
                totalInbound = 0;
            }
            if(totalOutbound < 0){
                totalOutbound = 0;
            }
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
        String sql_mach_insert = "INSERT INTO Network VALUES(?,?,?,?,?,?)";
        PreparedStatement smi = c.prepareStatement(sql_mach_insert);
        smi.setString(1, IPs);
        smi.setString(2, SerialNum);
        smi.setTimestamp(3, currentTime);
        smi.setLong(4, (totalInbound * 8)/1000000);
        smi.setLong(5, (totalOutbound * 8)/1000000);
        smi.setString(6, Macs);
        smi.execute();
        //LOGGER.info("IPs: {}", IPs);
        //LOGGER.info("Macs:  {}", Macs);
    }
}
