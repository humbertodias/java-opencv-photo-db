package opencv.photo.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionFactory {

    private static String JDBC_DRIVER;
    private static String JDBC_URL;
    private static String JDBC_USER;
    private static String JDBC_PASS;

    private static String TABLE_PHOTO;
    private static String TABLE_PHOTO_SQL_CREATE;
    private static String TABLE_PHOTO_SQL_INSERT;

    static {
        loadConfiguration("connection");

        if (getTables(getConnection()).stream().noneMatch(name -> TABLE_PHOTO.equalsIgnoreCase(name))) createDB();
    }

    private ConnectionFactory(){

    }

    public static void loadConfiguration(String resourceName) {
        try {

            InputStream resource = ConnectionFactory.class.getResourceAsStream("/" + resourceName + ".properties");
            if (resource != null) {
                Properties properties = new Properties();
                properties.load(resource);

                JDBC_DRIVER = properties.getProperty("jdbc.driver");
                JDBC_URL = properties.getProperty("jdbc.url");
                JDBC_USER = properties.getProperty("jdbc.user");
                JDBC_PASS = properties.getProperty("jdbc.pass");
                TABLE_PHOTO_SQL_CREATE = properties.getProperty("table.photo.sql.create");
                TABLE_PHOTO = TABLE_PHOTO_SQL_CREATE.split(" ")[2];
                TABLE_PHOTO_SQL_INSERT = properties.getProperty("table.photo.sql.insert");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() {
        try {

            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            //autocommit doesn't work very well with mysql
            conn.setAutoCommit(false);

            return conn;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void createDB() {
        try {
            try (PreparedStatement ps = getConnection().prepareStatement(TABLE_PHOTO_SQL_CREATE)) {
                ps.execute();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void insertImage(String type, byte [] imageData) throws IOException, SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(TABLE_PHOTO_SQL_INSERT)) {

            // Create an InputStream from the byte array
            try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
                preparedStatement.setString(1, type);
                preparedStatement.setBinaryStream(2, inputStream, imageData.length);
                preparedStatement.setInt(3, imageData.length);

                // Execute the query
                preparedStatement.executeUpdate();
            }
        }

    }

    public static List<String> getTables(Connection conn) {
        List<String> tables = new ArrayList<>();
        try {

            DatabaseMetaData dbmd = conn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tables;
    }

}
