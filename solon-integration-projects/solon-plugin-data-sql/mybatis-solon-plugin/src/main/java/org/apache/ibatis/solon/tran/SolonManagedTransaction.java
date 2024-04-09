package org.apache.ibatis.solon.tran;

import org.apache.ibatis.transaction.Transaction;
import org.noear.solon.data.tran.TranUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author noear
 * @since 1.6
 */
public class SolonManagedTransaction implements Transaction {
    DataSource dataSource;
    Connection connection;

    public SolonManagedTransaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = TranUtils.getConnectionProxy(dataSource);
        }

        return connection;
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null) {
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null) {
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return null;
    }
}
