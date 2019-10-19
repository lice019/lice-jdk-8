
package java.io;

import java.lang.annotation.Native;

/**
 * Package-private abstract class for the local filesystem abstraction.
 * 本地文件系统抽象的包私有抽象类。
 */

abstract class FileSystem {

    /* -- Normalization and construction-规范化建设 -- */

    //返回本地文件系统的名称分隔符。
    public abstract char getSeparator();

    //返回本地文件系统的路径分隔符。
    public abstract char getPathSeparator();

    //将给定的路径名字符串转换为普通形式。如果字符串已经处于正常形式，那么它将被简单地返回。
    public abstract String normalize(String path);

    //计算这个路径名字符串前缀的长度。路径名字符串必须是标准格式的。
    public abstract int prefixLength(String path);

    //根据父路径名解析子路径名字符串。
    //两个字符串都必须是标准形式的，结果也必须是标准形式的。
    public abstract String resolve(String parent, String child);

    //返回父路径名字符串，当两个参数的文件构造函数中的父目录参数为空路径名时使用该字符串。
    public abstract String getDefaultParent();

    //如果需要，对给定的URI路径字符串进行后处理。这个用于
    //例如，将“/c:/foo”转换成“c:/foo”。路径字符串
    //仍然有斜杠分隔符;文件类中的代码将翻译它们
    //在此方法返回之后。
    public abstract String fromURIPath(String path);


    /* -- Path operations -路径操作-- */

    //File对象的f是否为绝对路径路径文件
    public abstract boolean isAbsolute(File f);

    //将给定的抽象路径名解析为绝对形式。调用的
    //文件类中的getAbsolutePath和getCanonicalPath方法。
    public abstract String resolve(File f);

    public abstract String canonicalize(String path) throws IOException;


    /* -- Attribute accessors -- */

    /* Constants for simple boolean attributes 用于简单布尔属性的常量*/
    @Native public static final int BA_EXISTS    = 0x01;
    @Native public static final int BA_REGULAR   = 0x02;
    @Native public static final int BA_DIRECTORY = 0x04;
    @Native public static final int BA_HIDDEN    = 0x08;

    /**
     * Return the simple boolean attributes for the file or directory denoted
     * by the given abstract pathname, or zero if it does not exist or some
     * other I/O error occurs.
     */
    public abstract int getBooleanAttributes(File f);

    @Native public static final int ACCESS_READ    = 0x04;
    @Native public static final int ACCESS_WRITE   = 0x02;
    @Native public static final int ACCESS_EXECUTE = 0x01;

    /**
     * Check whether the file or directory denoted by the given abstract
     * pathname may be accessed by this process.  The second argument specifies
     * which access, ACCESS_READ, ACCESS_WRITE or ACCESS_EXECUTE, to check.
     * Return false if access is denied or an I/O error occurs
     */
    public abstract boolean checkAccess(File f, int access);
    /**
     * Set on or off the access permission (to owner only or to all) to the file
     * or directory denoted by the given abstract pathname, based on the parameters
     * enable, access and oweronly.
     */
    public abstract boolean setPermission(File f, int access, boolean enable, boolean owneronly);

    /**
     * Return the time at which the file or directory denoted by the given
     * abstract pathname was last modified, or zero if it does not exist or
     * some other I/O error occurs.
     */
    public abstract long getLastModifiedTime(File f);

    /**
     * Return the length in bytes of the file denoted by the given abstract
     * pathname, or zero if it does not exist, is a directory, or some other
     * I/O error occurs.
     */
    public abstract long getLength(File f);


    /* -- File operations -- */

    /**
     * Create a new empty file with the given pathname.  Return
     * <code>true</code> if the file was created and <code>false</code> if a
     * file or directory with the given pathname already exists.  Throw an
     * IOException if an I/O error occurs.
     */
    public abstract boolean createFileExclusively(String pathname)
        throws IOException;

    /**
     * Delete the file or directory denoted by the given abstract pathname,
     * returning <code>true</code> if and only if the operation succeeds.
     */
    public abstract boolean delete(File f);

    /**
     * List the elements of the directory denoted by the given abstract
     * pathname.  Return an array of strings naming the elements of the
     * directory if successful; otherwise, return <code>null</code>.
     */
    public abstract String[] list(File f);

    /**
     * Create a new directory denoted by the given abstract pathname,
     * returning <code>true</code> if and only if the operation succeeds.
     */
    public abstract boolean createDirectory(File f);

    /**
     * Rename the file or directory denoted by the first abstract pathname to
     * the second abstract pathname, returning <code>true</code> if and only if
     * the operation succeeds.
     */
    public abstract boolean rename(File f1, File f2);

    /**
     * Set the last-modified time of the file or directory denoted by the
     * given abstract pathname, returning <code>true</code> if and only if the
     * operation succeeds.
     */
    public abstract boolean setLastModifiedTime(File f, long time);

    /**
     * Mark the file or directory denoted by the given abstract pathname as
     * read-only, returning <code>true</code> if and only if the operation
     * succeeds.
     */
    public abstract boolean setReadOnly(File f);


    /* -- Filesystem interface -- */

    /**
     * List the available filesystem roots.
     */
    public abstract File[] listRoots();

    /* -- Disk usage -- */
    @Native public static final int SPACE_TOTAL  = 0;
    @Native public static final int SPACE_FREE   = 1;
    @Native public static final int SPACE_USABLE = 2;

    public abstract long getSpace(File f, int t);

    /* -- Basic infrastructure -- */

    /**
     * Compare two abstract pathnames lexicographically.
     */
    public abstract int compare(File f1, File f2);

    /**
     * Compute the hash code of an abstract pathname.
     */
    public abstract int hashCode(File f);

    // Flags for enabling/disabling performance optimizations for file
    // name canonicalization
    static boolean useCanonCaches      = true;
    static boolean useCanonPrefixCache = true;

    private static boolean getBooleanProperty(String prop, boolean defaultVal) {
        String val = System.getProperty(prop);
        if (val == null) return defaultVal;
        if (val.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    static {
        useCanonCaches      = getBooleanProperty("sun.io.useCanonCaches",
                                                 useCanonCaches);
        useCanonPrefixCache = getBooleanProperty("sun.io.useCanonPrefixCache",
                                                 useCanonPrefixCache);
    }
}
