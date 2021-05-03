import javax.swing.*;

public class CrumUI {
    private JTabbedPane tabbedPane1;
    private JPanel rootPanel;
    private JPanel CpuPanel;
    private JPanel RamPanel;
    private JPanel NetworkPanel;
    private JPanel DiskPanel;
    private JLabel readSpeed;
    private JLabel writeSpeed;
    private JLabel bytesRead;
    private JLabel bytesWritten;

    public static void main(String[] args){
        JFrame frame = new JFrame("C.R.U.M");

        //frame.setSize(500,500);
        frame.setContentPane(new CrumUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //centers the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}