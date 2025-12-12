package org.vsearch.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataTools {
    private static final Logger logger = LoggerFactory.getLogger(DataTools.class);
    static Boolean checkTableExists(String tableName, Connection connection){
        logger.info("Looking for table {}", tableName );
        try(PreparedStatement st = connection.prepareStatement(
                "select 1 from pg_tables where tablename = ?")){
            st.setObject(1, tableName.toLowerCase());
            ResultSet rs = st.executeQuery();
            if( rs.next() ) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while result checking", e);
        }
        return false;
    }
    static void createTable(String tableName, Connection connection){

        logger.debug("Create tables");
        logger.info(getSQLCreateTable(tableName));
        try(PreparedStatement st = connection.prepareStatement(
                getSQLCreateTable(tableName))){
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while table creation", e);
        }
    }
    private static String getSQLCreateTable(String tablename){
        return "create table " + tablename.toLowerCase() +
                " (" +
                "filename varchar(256),\n" +
                "vector_store_id varchar(32),\n" +
                "file_id varchar(32),\n" +
                "status varchar(24),\n" +
                "modified timestamp,\n" +
                "primary key(filename, vector_store_id));\n" +
                "create unique index idx_" +
                tablename.toLowerCase() +
                "_fileid_vsid on " +
                tablename.toLowerCase() +
                "(file_id, vector_store_id);";
    }
}
