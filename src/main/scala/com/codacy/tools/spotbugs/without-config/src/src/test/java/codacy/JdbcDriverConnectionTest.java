//#Patterns: HARD_CODE_PASSWORD
package codacy;

import junit.framework.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcDriverConnectionTest extends TestCase {
    protected int value1, value2;

    // assigning the values
    protected void setUp() {
        value1 = 3;
        value2 = 3;
    }

    // test method to add two values
    public void testAdd() throws SQLException {
        try {
            //#Err: HARD_CODE_PASSWORD
            Connection conn = DriverManager.getConnection("jdbc:mysql://prod.company.com/production", "root", "lamepassword");
        } catch (Exception ex) {
            // ignore
        }
        double result = value1 + value2;
        assertTrue(result == 6);
    }
}
