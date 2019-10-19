
package java.io;


/**
 * Reader：用于读取字符流的抽象类。 子类必须实现的唯一方法是read（char []，int，int）和close（）。
 * 然而，大多数子类将覆盖这里定义的一些方法，以便提供更高的效率，附加的功能或两者。
 * <p>
 * 注意：
 * 1、Reader可以操作中文的，一个字符=两个字节，一个字节=8bit。
 * 2、Reader底层用的是字符缓冲区来操作数据。
 */

public abstract class Reader implements Readable, Closeable {

    //对象锁
    protected Object lock;

    //构造器创建Reader实例，同时将该对象设置为对象锁
    protected Reader() {
        this.lock = this;
    }

    //可以传入锁对象，来创建Reader对象
    protected Reader(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }

    //通过NIO的字符缓冲区来读取字符
    public int read(java.nio.CharBuffer target) throws IOException {
        int len = target.remaining();
        char[] cbuf = new char[len];
        int n = read(cbuf, 0, len);
        if (n > 0)
            target.put(cbuf, 0, n);
        return n;
    }


    public int read() throws IOException {
        char cb[] = new char[1];
        if (read(cb, 0, 1) == -1)
            return -1;
        else
            return cb[0];
    }


    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }


    abstract public int read(char cbuf[], int off, int len) throws IOException;

    /**
     * Maximum skip-buffer size
     */
    private static final int maxSkipBufferSize = 8192;

    /**
     * Skip buffer, null until allocated
     */
    private char skipBuffer[] = null;


    public long skip(long n) throws IOException {
        if (n < 0L)
            throw new IllegalArgumentException("skip value is negative");
        int nn = (int) Math.min(n, maxSkipBufferSize);
        synchronized (lock) {
            if ((skipBuffer == null) || (skipBuffer.length < nn))
                skipBuffer = new char[nn];
            long r = n;
            while (r > 0) {
                int nc = read(skipBuffer, 0, (int) Math.min(r, nn));
                if (nc == -1)
                    break;
                r -= nc;
            }
            return n - r;
        }
    }


    public boolean ready() throws IOException {
        return false;
    }


    public boolean markSupported() {
        return false;
    }


    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }


    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }


    abstract public void close() throws IOException;

}
