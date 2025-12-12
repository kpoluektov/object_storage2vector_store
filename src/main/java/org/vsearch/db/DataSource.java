package org.vsearch.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
    private static HikariDataSource ds;
    public static synchronized void init(Map<String, Object> settings, String tableName){
        Properties props = new Properties();
        props.putAll(settings);
        HikariConfig config = new HikariConfig(props);
        ds = new HikariDataSource( config );
        try (Connection connection = ds.getConnection()) {
            if ( !DataTools.checkTableExists(tableName, connection)){
                DataTools.createTable(tableName, connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open connection " , e);
        }
    }
    private DataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    public static void close(){
        ds.close();
    }
}
