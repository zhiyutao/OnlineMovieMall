import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;
import java.util.ArrayList;

public class UpdateSecurePassword {

    /*
     * 
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     * 
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     * 
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=false";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128) NOT NULL";
        String query = "SELECT email, password from employees";
        String update = "UPDATE employees SET password='%s' WHERE email='%s';";
        updatePassword(connection, alterQuery, query, update);
        alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128) NOT NULL";
        query = "SELECT email, password from customers";
        update = "UPDATE customers SET password='%s' WHERE email='%s';";
        updatePassword(connection, alterQuery, query, update);
        connection.close();

        System.out.println("finished");

    }

    private static void updatePassword(Connection connection, String alterQuery, String query, String update) throws SQLException {
        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)

        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();

        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            // get the ID and plain text password from current table
            String id = rs.getString("email");
            String password = rs.getString("password");

            // encrypt the password using StrongPasswordEncryptor
            String encryptedPassword = passwordEncryptor.encryptPassword(password);

            // generate the update query
            String updateQuery = String.format(update, encryptedPassword,
                    id);
            updateQueryList.add(updateQuery);
            // System.out.println(updateQuery);
        }
        rs.close();

        // execute the update queries to update the password
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");

        statement.close();
    }

}
