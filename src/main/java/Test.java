import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class Test {

    public static void main(String args[]){
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        System.out.println("si: "+ si);
        System.out.println("hal: " + hal);
        System.out.println("cpu: " + cpu);
    }

}
