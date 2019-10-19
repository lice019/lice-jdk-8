
package java.sql;

import java.util.logging.Logger;

/**
 * 每个驱动程序类必须实现的接口。
 * Java SQL框架允许多个数据库驱动程序。
 * 每个驱动程序都应该提供一个实现Driver接口的类。
 * DriverManager将尝试加载尽可能多的驱动程序，然后对于任何给定的连接请求，它会依次要求每个驱动程序尝试连接到目标URL。
 * 强烈建议每个Driver类应该是小型且独立的，以便可以加载和查询Driver类，而不需要大量的支持代码。
 * 当加载一个Driver类时，它应该创建一个自己的实例，并用DriverManager注册它。 这意味着用户可以通过调用以下方式加载和注册驱动程序：
 * Class.forName("foo.bah.Driver")
 * JDBC驱动程序可以创建一个DriverAction才能收到通知执行时DriverManager.deregisterDriver(java.sql.Driver)被调用。
 */
public interface Driver {


    Connection connect(String url, java.util.Properties info)
            throws SQLException;


    boolean acceptsURL(String url) throws SQLException;


    DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info)
            throws SQLException;


    int getMajorVersion();


    int getMinorVersion();


    boolean jdbcCompliant();

    //------------------------- JDBC 4.1 -----------------------------------


    public Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
