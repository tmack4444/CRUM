import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class CRUMTest {
    private static List<HWDiskStore> disks;
    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static String SerialNum;
    public static int numDisks;

  @Test
  void initOSHITest(){
      CRUM crum = new CRUM();
      crum.initOSHI();
      assertNotNull(crum.si);
      assertNotNull(crum.hal);
      assertNotNull(crum.disks);
      assertNotNull(crum.SerialNum);
  }

}