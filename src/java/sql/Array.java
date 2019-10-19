
package java.sql;


/**
 * Java编程语言中映射的SQL类型为ARRAY 。 默认情况下， Array值是对SQL ARRAY值的事务持续时间引用。 默认情况下， Array对象在内部使用SQL LOCATOR（数组）实现，这意味着Array对象包含一个逻辑指针，指向SQL ARRAY值中的数据，而不是包含ARRAY值的数据。
 * Array接口提供了将SQL ARRAY值的数据作为数组或ResultSet对象提供给客户端的方法。 如果SQL ARRAY的元素是UDT，则它们可能是自定义映射的。 要创建自定义映射，程序员必须做两件事情：
 * <p>
 * 创建一个实现用于定制映射的UDT的SQLData接口的类。
 * 在包含的类型映射中创建一个条目
 * UDT的完全限定的SQL类型名称
 * 类对象为类实现SQLData
 * 当具有基本类型的条目的类型映射被提供给方法getArray和getResultSet时，其包含的映射将用于映射ARRAY值的元素。 如果没有提供类型图，通常情况下，默认情况下会使用连接的类型映射。 如果连接的类型映射或提供给方法的类型映射没有基本类型的条目，则会根据标准映射映射元素。
 * <p>
 * 所有的方法Array接口必须如果JDBC驱动程序支持的数据类型得到充分执行。
 */
public interface Array {


    String getBaseTypeName() throws SQLException;


    int getBaseType() throws SQLException;


    Object getArray() throws SQLException;


    Object getArray(java.util.Map<String, Class<?>> map) throws SQLException;


    Object getArray(long index, int count) throws SQLException;


    Object getArray(long index, int count, java.util.Map<String, Class<?>> map)
            throws SQLException;


    ResultSet getResultSet() throws SQLException;


    ResultSet getResultSet(java.util.Map<String, Class<?>> map) throws SQLException;


    ResultSet getResultSet(long index, int count) throws SQLException;


    ResultSet getResultSet(long index, int count,
                           java.util.Map<String, Class<?>> map)
            throws SQLException;

    void free() throws SQLException;

}
