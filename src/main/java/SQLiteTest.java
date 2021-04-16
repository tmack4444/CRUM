import java.sql.*;
import java.util.Calendar;

public class SQLiteTest {
    public static void main( String args[] ) {
        Connection c = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql_machine = "CREATE TABLE IF NOT EXISTS TESTMACHINE " +
                    "(MACHINE_ID INT      NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    " MACHINE_MODEL           TEXT    NOT NULL, " +
                    " MACHINE_VENDOR      TEXT     NOT NULL, " +
                    "PRIMARY KEY(MACHINE_ID, TIMESTAMP))";

            stmt.executeUpdate(sql_machine);

            String sql_disc = "CREATE TABLE IF NOT EXISTS TESTDISC " +
                    "(DISC_ID INT      NOT NULL," +
                    "MACHINE_ID INT      NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    " DISC_NAME           TEXT    NOT NULL, " +
                    " DISC_MODEL          TEXT     NOT NULL, " +
                    " DISC_SIZE        INT    NOT NULL, " +
                    "DISC_USED        INT   NOT NULL," +
                    "DISC_SPEED INT NOT NULL, " +
                    "PRIMARY KEY(DISC_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES TESTMACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_disc);

            String sql_user = "CREATE TABLE IF NOT EXISTS TESTUSER " +
                    "(USER_ID INT NOT NULL, " +
                    "MACHINE_ID INT NOT NULL," +
                    "TIMESTAMP TIMESTAMP NOT NULL," +
                    "PASSWORD HASH TEXT NOT NULL," +
                    "PRIMARY KEY(USER_ID, MACHINE_ID, TIMESTAMP)," +
                    "FOREIGN KEY(MACHINE_ID) REFERENCES TESTMACHINE(MACHINE_ID))";
            stmt.executeUpdate(sql_user);

            System.out.println("Table created successfully");

            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp currentTime = new java.sql.Timestamp(calendar.getTime().getTime());

            String sql_mach_insert = "INSERT INTO TESTMACHINE VALUES(?,?,?,?)";
            PreparedStatement smi = c.prepareStatement(sql_mach_insert);
            smi.setInt(1, 2);
            smi.setTimestamp(2,currentTime);
            smi.setString(3, "XPS143");
            smi.setString(4, "Dell");
            smi.execute();

            String sql_Search = "SELECT * FROM TESTMACHINE";
            ResultSet rs = stmt.executeQuery(sql_Search);
            //Retrieving values
            while(rs.next()) {
                System.out.println("ID: "+rs.getInt("MACHINE_ID"));
                System.out.println("TIMESTAMP: "+rs.getTimestamp("TIMESTAMP"));
                System.out.println("Model: "+rs.getString("MACHINE_MODEL"));
                System.out.println("Vendor: "+rs.getString("MACHINE_VENDOR"));
                System.out.println();
            }
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
