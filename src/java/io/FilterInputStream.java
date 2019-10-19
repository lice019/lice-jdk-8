package java.io;

/**
 * FilterInputStream包含一些其他输入流，它用作其基本的数据源，可能会沿途转换数据或提供附加功能。 FilterInputStream本身简单地覆盖了所有InputStream的方法，
 * InputStream版本将所有请求传递给包含的输入流。 FilterInputStream的FilterInputStream可以进一步覆盖这些方法中的一些，并且还可以提供附加的方法和领域。
 */
public class FilterInputStream extends InputStream {

    //要过滤的输入流。
    protected volatile InputStream in;

    //构造器
    protected FilterInputStream(InputStream in) {
        this.in = in;
    }


    public int read() throws IOException {
        return in.read();
    }


    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }


    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }


    public long skip(long n) throws IOException {
        return in.skip(n);
    }


    public int available() throws IOException {
        return in.available();
    }


    public void close() throws IOException {
        in.close();
    }


    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }


    public synchronized void reset() throws IOException {
        in.reset();
    }


    public boolean markSupported() {
        return in.markSupported();
    }
}
