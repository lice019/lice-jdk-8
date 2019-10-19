
package java.io;

import java.nio.channels.FileChannel;

import sun.nio.ch.FileChannelImpl;


/**
 * FileInputStream:文件操作字节输入流，在读取文件时，是以字节为单位进行读取的
 */
public class FileInputStream extends InputStream {

    //文件描述类，描述了文件的属性
    private final FileDescriptor fd;

    //要操作输入的文件路径
    private final String path;

    //文件操作管道，是NIO特有的管道
    private FileChannel channel = null;

    //关闭锁对象
    private final Object closeLock = new Object();
    //流是否被关闭
    private volatile boolean closed = false;


    //通过打开与实际文件的连接来创建一个 FileInputStream ，该文件由文件系统中的路径名 name命名。
    public FileInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    //通过打开与实际文件的连接创建一个 FileInputStream ，该文件由文件系统中的 File对象 file命名。
    public FileInputStream(File file) throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        }
        fd = new FileDescriptor();
        fd.attach(this);
        path = name;
        open(name);
    }

    //创建 FileInputStream通过使用文件描述符 fdObj ，其表示在文件系统中的现有连接到一个实际的文件。
    public FileInputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkRead(fdObj);
        }
        fd = fdObj;
        path = null;

        /*
         * FileDescriptor is being shared by streams.
         * Register this stream with FileDescriptor tracker.
         */
        fd.attach(this);
    }

    //通过文件路径名称，开启要操作的文件，open0为本地方法(即为JVM中方法)，JVM通过JNI来调用JVM中C语言的open0方法
    private native void open0(String name) throws FileNotFoundException;

    // wrap native call to allow instrumentation


    private void open(String name) throws FileNotFoundException {
        open0(name);
    }


    public int read() throws IOException {
        return read0();
    }

    //开启读取流，read0为本地方法(即为JVM中方法)，JVM通过JNI来调用JVM中C语言的read0方法
    private native int read0() throws IOException;

    //从该输入流读取最多 len字节的数据为字节数组。
    //将读取到的数据存储到byte b[]数组中，从off开始读，每次读len个字节
    //从输入流中读取最多len个字节到字节数组中(从数组的off位置开始存储字节)，当len为0时则返回0，如果len不为零，则该方法将阻塞，
    private native int readBytes(byte b[], int off, int len) throws IOException;


    public int read(byte b[]) throws IOException {
        return readBytes(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    //跳过n个字节不读
    public long skip(long n) throws IOException {
        return skip0(n);
    }

    private native long skip0(long n) throws IOException;


    public int available() throws IOException {
        return available0();
    }

    private native int available0() throws IOException;


    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        if (channel != null) {
            channel.close();
        }

        fd.closeAll(new Closeable() {
            public void close() throws IOException {
                close0();
            }
        });
    }


    public final FileDescriptor getFD() throws IOException {
        if (fd != null) {
            return fd;
        }
        throw new IOException();
    }


    //获取文件输入流管道，线程同步
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, path, true, false, this);
            }
            return channel;
        }
    }

    private static native void initIDs();

    private native void close0() throws IOException;

    static {
        initIDs();
    }

    //确保当这个文件输入流的 close方法没有更多的引用时被调用。
    protected void finalize() throws IOException {
        if ((fd != null) && (fd != FileDescriptor.in)) {
            /* if fd is shared, the references in FileDescriptor
             * will ensure that finalizer is only called when
             * safe to do so. All references using the fd have
             * become unreachable. We can call close()
             */
            close();
        }
    }
}
