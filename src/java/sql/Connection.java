
package java.sql;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 与特定数据库的连接（会话）。 执行SQL语句并在连接的上下文中返回结果。
 */
public interface Connection extends Wrapper, AutoCloseable {

    //创建一个Statement对象，用于操作SQL语句
    Statement createStatement() throws SQLException;

    //创建一个 PreparedStatement对象，用于将参数化的SQL语句发送到数据库。
    //具有预编译功能
    PreparedStatement prepareStatement(String sql)
            throws SQLException;

    //建一个调用数据库存储过程的 CallableStatement对象。
    CallableStatement prepareCall(String sql) throws SQLException;


    String nativeSQL(String sql) throws SQLException;

    //设置自动提交事务
    void setAutoCommit(boolean autoCommit) throws SQLException;


    boolean getAutoCommit() throws SQLException;


    void commit() throws SQLException;

    //事务回滚
    void rollback() throws SQLException;


    void close() throws SQLException;


    boolean isClosed() throws SQLException;

    //======================================================================
    // Advanced features:

    //检索 DatabaseMetaData对象包含有关哪个这个数据库的元数据 Connection对象表示的连接。
    DatabaseMetaData getMetaData() throws SQLException;


    void setReadOnly(boolean readOnly) throws SQLException;


    boolean isReadOnly() throws SQLException;


    void setCatalog(String catalog) throws SQLException;


    String getCatalog() throws SQLException;

    /**
     * A constant indicating that transactions are not supported.
     */
    int TRANSACTION_NONE = 0;

    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read
     * by another transaction before any changes in that row have been
     * committed (a "dirty read").  If any of the changes are rolled back,
     * the second transaction will have retrieved an invalid row.
     */
    int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     * A constant indicating that
     * dirty reads are prevented; non-repeatable reads and phantom
     * reads can occur.  This level only prohibits a transaction
     * from reading a row with uncommitted changes in it.
     */
    int TRANSACTION_READ_COMMITTED = 2;

    /**
     * A constant indicating that
     * dirty reads and non-repeatable reads are prevented; phantom
     * reads can occur.  This level prohibits a transaction from
     * reading a row with uncommitted changes in it, and it also
     * prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
     */
    int TRANSACTION_REPEATABLE_READ = 4;

    /**
     * A constant indicating that
     * dirty reads, non-repeatable reads and phantom reads are prevented.
     * This level includes the prohibitions in
     * <code>TRANSACTION_REPEATABLE_READ</code> and further prohibits the
     * situation where one transaction reads all rows that satisfy
     * a <code>WHERE</code> condition, a second transaction inserts a row that
     * satisfies that <code>WHERE</code> condition, and the first transaction
     * rereads for the same condition, retrieving the additional
     * "phantom" row in the second read.
     */
    int TRANSACTION_SERIALIZABLE = 8;


    //设置事务隔离级别
    void setTransactionIsolation(int level) throws SQLException;


    int getTransactionIsolation() throws SQLException;


    SQLWarning getWarnings() throws SQLException;


    void clearWarnings() throws SQLException;


    //--------------------------JDBC 2.0-----------------------------


    Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException;


    PreparedStatement prepareStatement(String sql, int resultSetType,
                                       int resultSetConcurrency)
            throws SQLException;


    CallableStatement prepareCall(String sql, int resultSetType,
                                  int resultSetConcurrency) throws SQLException;


    java.util.Map<String, Class<?>> getTypeMap() throws SQLException;


    void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException;

    //--------------------------JDBC 3.0-----------------------------


    void setHoldability(int holdability) throws SQLException;


    int getHoldability() throws SQLException;


    Savepoint setSavepoint() throws SQLException;


    Savepoint setSavepoint(String name) throws SQLException;


    void rollback(Savepoint savepoint) throws SQLException;


    void releaseSavepoint(Savepoint savepoint) throws SQLException;


    Statement createStatement(int resultSetType, int resultSetConcurrency,
                              int resultSetHoldability) throws SQLException;


    PreparedStatement prepareStatement(String sql, int resultSetType,
                                       int resultSetConcurrency, int resultSetHoldability)
            throws SQLException;


    CallableStatement prepareCall(String sql, int resultSetType,
                                  int resultSetConcurrency,
                                  int resultSetHoldability) throws SQLException;


    PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException;


    PreparedStatement prepareStatement(String sql, int columnIndexes[])
            throws SQLException;


    PreparedStatement prepareStatement(String sql, String columnNames[])
            throws SQLException;


    Clob createClob() throws SQLException;


    Blob createBlob() throws SQLException;


    NClob createNClob() throws SQLException;


    SQLXML createSQLXML() throws SQLException;


    boolean isValid(int timeout) throws SQLException;


    void setClientInfo(Properties properties)
            throws SQLClientInfoException;


    String getClientInfo(String name)
            throws SQLException;


    Properties getClientInfo()
            throws SQLException;


    Array createArrayOf(String typeName, Object[] elements) throws
            SQLException;


    Struct createStruct(String typeName, Object[] attributes)
            throws SQLException;

    //--------------------------JDBC 4.1 -----------------------------


    void setSchema(String schema) throws SQLException;


    String getSchema() throws SQLException;


    void abort(Executor executor) throws SQLException;


    void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException;


    int getNetworkTimeout() throws SQLException;
}
