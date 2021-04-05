import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;

public class CRUM {

    public static void main(String[] args){
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        List<HWDiskStore> Disks = new List<HWDiskStore>();

        System.out.println("hal: " + hal);
    }
}
