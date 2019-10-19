
package java.io;

import java.nio.channels.FileChannel;

import sun.nio.ch.FileChannelImpl;


/**
 * 文件输出流是用于将数据写入到输出流File或一个FileDescriptor 。 文件是否可用或可能被创建取决于底层平台。 特别是某些平台允许一次只能打开一个文件来写入一个FileOutputStream （或其他文件写入对象）。 在这种情况下，如果所涉及的文件已经打开，则此类中的构造函数将失败。
 * FileOutputStream用于写入诸如图像数据的原始字节流。 对于写入字符流，请考虑使用FileWriter 。
 */
public class FileOutputStream extends OutputStream {

    //文件描述类
    private final FileDescriptor fd;

    //如果文件被打开用于追加，则为True。
    private final boolean append;

    //NIO的文件管道
    private FileChannel channel;

    //文件路径名称
    private final String path;

    //关闭锁
    private final Object closeLock = new Object();
    private volatile boolean closed = false;


    public FileOutputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null, false);
    }


    public FileOutputStream(String name, boolean append)
            throws FileNotFoundException {
        this(name != null ? new File(name) : null, append);
    }


    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }


    public FileOutputStream(File file, boolean append)
            throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkWrite(name);
        }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        }
        this.fd = new FileDescriptor();
        fd.attach(this);
        this.append = append;
        this.path = name;

        open(name, append);
    }


    public FileOutputStream(FileDescriptor fdObj) {
        SecurityManager security = System.getSecurityManager();
        if (fdObj == null) {
            throw new NullPointerException();
        }
        if (security != null) {
            security.checkWrite(fdObj);
        }
        this.fd = fdObj;
        this.append = false;
        this.path = null;

        fd.attach(this);
    }


    private native void open0(String name, boolean append)
            throws FileNotFoundException;

    // wrap native call to allow instrumentation


    private void open(String name, boolean append)
            throws FileNotFoundException {
        open0(name, append);
    }


    private native void write(int b, boolean append) throws IOException;


    public void write(int b) throws IOException {
        write(b, append);
    }


    private native void writeBytes(byte b[], int off, int len, boolean append)
            throws IOException;


    public void write(byte b[]) throws IOException {
        writeBytes(b, 0, b.length, append);
    }


    public void write(byte b[], int off, int len) throws IOException {
        writeBytes(b, off, len, append);
    }


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


    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelImpl.open(fd, path, false, true, append, this);
            }
            return channel;
        }
    }


    protected void finalize() throws IOException {
        if (fd != null) {
            if (fd == FileDescriptor.out || fd == FileDescriptor.err) {
                flush();
            } else {
                /* if fd is shared, the references in FileDescriptor
                 * will ensure that finalizer is only called when
                 * safe to do so. All references using the fd have
                 * become unreachable. We can call close()
                 */
                close();
            }
        }
    }

    private native void close0() throws IOException;

    private static native void initIDs();

    static {
        initIDs();
    }

}
