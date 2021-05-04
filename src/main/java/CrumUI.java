import javax.swing.*;
import java.awt.*;

public class CrumUI extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JPanel CpuPanel;
    private JPanel RamPanel;
    private JPanel NetworkPanel;
    private JPanel DiskPane;
    private JLabel readSpeed;
    private JLabel writeSpeed;
    private JLabel bytesRead;
    private JLabel bytesWritten;

    /**
     * This constructor method also handles
     * dynamic tab creation (mainly used for handling
     * multiples of a hardware component, like Disk.
     * Any additional tabs will be added within here,
     * as the constructor has access to tabbedPane1
     * @param title
     */
    public CrumUI(String title){
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(rootPanel);
        this.tabbedPane1.addTab("Disk:0", new DiskPanel());
        this.pack();
    }
    
    public static void createUI(){
        JFrame frame = new CrumUI("C.R.U.M");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}