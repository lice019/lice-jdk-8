
package java.io;

/**
 * 这个抽象类是表示输入字节流的所有类的超类。-----用于操作节点的输入流
 * 需要定义InputStream子类的应用InputStream必须始终提供一种返回输入的下一个字节的方法。
 * 传统的BIO的输入流的读取方法是阻塞的
 */
public abstract class InputStream implements Closeable {

    // MAX_SKIP_BUFFER_SIZE is used to determine the maximum buffer size to
    // use when skipping.
    //最大跳过缓冲区大小
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    //InputStream输入流读取方法,该方法是BIO中阻塞的，在还没读完前，程序将会等待
    public abstract int read() throws IOException;

    //从输入流读取一些字节数，并将它们存储到缓冲区 b 。该方法是BIO中阻塞的，在还没读完前，程序将会等待
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * byte b[] : 读取数据的缓冲区
     * int off ：  byte b[]中的起始偏移量
     * int len： 读取内容的最大长度
     */
    //从输入流读取最多 len字节的数据到一个字节数组。该方法是BIO中阻塞的，在还没读完前，程序将会等待
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    //跳过并丢弃来自此输入流的 n字节数据。
    //跳过不读取的n个字节数据
    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }

        int size = (int) Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            nr = read(skipBuffer, 0, (int) Math.min(size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    //返回从该输入流中可以读取（或跳过）的字节数的估计值，而不会被下一次调用此输入流的方法阻塞。
    public int available() throws IOException {
        return 0;
    }

    //关闭流
    public void close() throws IOException {
    }

    //标记此输入流中的当前位置。
    public synchronized void mark(int readlimit) {
    }

   //将此流重新定位到上次在此输入流上调用 mark方法时的位置。
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    //测试这个输入流是否支持 mark和 reset方法。
    public boolean markSupported() {
        return false;
    }

}
