import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CRUMTest {
    private static List<HWDiskStore> disks;
    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static String SerialNum;
    public static int numDisks;
  public static void initOSHItest(){
      CRUM.initOSHI();
      assertNotNull(si);
      assertNotNull(hal);
      assertNotNull(disks);
      assertNotNull(SerialNum);
  }

}