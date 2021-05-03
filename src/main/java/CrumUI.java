import javax.swing.*;

public class CrumUI {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JPanel CpuPanel;
    private JPanel RamPanel;
    private JPanel NetworkPanel;
    private JPanel DiskPanel;
    private JLabel readMetrics;
    private JLabel readLabel;
    private JLabel writeLabel;
    private JLabel writeMetrics;
    private JLabel bytesRead;
    private JLabel readBytesNum;
    private JLabel bytesWritten;
    private JLabel writeBytesNum;

    public static void main(String[] args){
        JFrame frame = new JFrame("C.R.U.M");

        frame.setSize(700,700);
        frame.setContentPane(new CrumUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        //centers the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}