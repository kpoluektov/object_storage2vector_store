package org.vsearch.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.document.Document;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DBConnection implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static final String tableName = "vector_store";
    public DBConnection(Map<String, Object> settings){
        logger.info("Opening PG connection to {}", settings.get("jdbcUrl"));
        DataSource.init(settings, tableName);
    }
    public void saveRecord(Document doc) {
        logger.debug("Saving document {} as {} by store {} in status {}",
                doc.getKey(), doc.getFileId(), doc.getIndex(), doc.getStatus());
        String query = "with new_entity as (select ? as filename, " +
                "? as vector_store_id, ? as file_id, ? as status)\n" +
                "merge into " + tableName + " a using new_entity ns on (a.filename = ns.filename " +
                "and a.vector_store_id = ns.vector_store_id)\n" +
                "when matched then update set file_id = ns.file_id, status = ns.status, modified = now()\n" +
                "when not matched then insert (filename, vector_store_id, file_id, status, modified)\n"+
                "values (ns.filename, ns.vector_store_id, ns.file_id, ns.status, now())";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     query)){
            st.setString(1, doc.getKey());
            st.setString(2, doc.getIndex());
            st.setString(3, doc.getFileId());
            st.setString(4, doc.getStatus().name());
            logger.trace("execite sql: {}", query);
            if (st.executeUpdate() != 1) throw new RuntimeException("Entity update missing for id " + doc.getKey()) ;
        } catch (SQLException e) {
            throw new RuntimeException("Error when updating entity", e);
        }
    }
    public void syncStatus(Document doc){
        String query = "select file_id, status from " + tableName +
                " where filename = ? and vector_store_id = ?";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     query)){
            st.setString(1, doc.getKey());
            st.setString(2, doc.getIndex());
            logger.trace("execite sql: {}", query);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                doc.setFileObject(rs.getString(1));
                doc.setStatus(rs.getString(2));
                logger.info("Document {} found for index {}. Proceed from status {}",
                        doc.getKey(), doc.getIndex(), doc.getStatus());
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error when looking for entity", e);
        }

    }
    @Override
    public void close() {
        DataSource.close();
    }
}
