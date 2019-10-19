package java.io;


/**
 * 用于写入字符流的抽象类。 子类必须实现的唯一方法是write（char []，int，int），flush（）和close（）。
 * 然而，大多数子类将覆盖这里定义的一些方法，以便提供更高的效率，附加的功能或两者。
 */

public abstract class Writer implements Appendable, Closeable, Flushable {

    //用于保存字符串和单个字符写入的临时缓冲区
    private char[] writeBuffer;

    /**
     * Size of writeBuffer, must be >= 1
     */
    private static final int WRITE_BUFFER_SIZE = 1024;

    //对象锁
    protected Object lock;


    protected Writer() {
        this.lock = this;
    }


    protected Writer(Object lock) {
        if (lock == null) {
            throw new NullPointerException();
        }
        this.lock = lock;
    }


    public void write(int c) throws IOException {
        synchronized (lock) {
            if (writeBuffer == null) {
                writeBuffer = new char[WRITE_BUFFER_SIZE];
            }
            writeBuffer[0] = (char) c;
            write(writeBuffer, 0, 1);
        }
    }


    public void write(char cbuf[]) throws IOException {
        write(cbuf, 0, cbuf.length);
    }


    abstract public void write(char cbuf[], int off, int len) throws IOException;


    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }


    public void write(String str, int off, int len) throws IOException {
        synchronized (lock) {
            char cbuf[];
            if (len <= WRITE_BUFFER_SIZE) {
                if (writeBuffer == null) {
                    writeBuffer = new char[WRITE_BUFFER_SIZE];
                }
                cbuf = writeBuffer;
            } else {    // Don't permanently allocate very large buffers.
                cbuf = new char[len];
            }
            str.getChars(off, (off + len), cbuf, 0);
            write(cbuf, 0, len);
        }
    }

    //将指定的字符附加到此作者。
    public Writer append(CharSequence csq) throws IOException {
        if (csq == null)
            write("null");
        else
            write(csq.toString());
        return this;
    }


    public Writer append(CharSequence csq, int start, int end) throws IOException {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }


    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    //刷新流
    abstract public void flush() throws IOException;


    abstract public void close() throws IOException;

}
