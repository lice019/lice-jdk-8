package java.io;


/**
 * 将文本写入字符输出流，缓冲字符，以提供单个字符，数组和字符串的高效写入。
 * 可以指定缓冲区大小，或者可以接受默认大小。 默认值足够大，可用于大多数用途。
 * <p>
 * 提供了一个newLine（）方法，它使用平台自己的系统属性line.separator定义的行分隔符概念。 并非所有平台都使用换行符（'\ n'）来终止行。 因此，调用此方法来终止每个输出行，因此优选直接写入换行符。
 * <p>
 * 一般来说，Writer将其输出立即发送到底层字符或字节流。 除非需要提示输出，否则建议将BufferedWriter包装在其write（）操作可能很昂贵的Writer上，例如FileWriters和OutputStreamWriters。 例如，
 * <p>
 * PrintWriter out
 * = new PrintWriter(new BufferedWriter(new FileWriter("foo.out"))); 将缓冲PrintWriter的输出到文件。 没有缓冲，每次调用print（）方法都会使字符转换为字节，然后立即写入文件，这可能非常低效。
 */

public class BufferedWriter extends Writer {

    private Writer out;

    private char cb[];
    private int nChars, nextChar;

    //默认字符缓冲区的大小
    private static int defaultCharBufferSize = 8192;

    //文本行的分割器
    private String lineSeparator;


    public BufferedWriter(Writer out) {
        this(out, defaultCharBufferSize);
    }


    public BufferedWriter(Writer out, int sz) {
        super(out);
        if (sz <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.out = out;
        cb = new char[sz];
        nChars = sz;
        nextChar = 0;

        lineSeparator = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
    }

    /**
     * Checks to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }


    void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar == 0)
                return;
            out.write(cb, 0, nextChar);
            nextChar = 0;
        }
    }


    public void write(int c) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar >= nChars)
                flushBuffer();
            cb[nextChar++] = (char) c;
        }
    }


    private int min(int a, int b) {
        if (a < b) return a;
        return b;
    }


    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }

            if (len >= nChars) {
                /* If the request length exceeds the size of the output buffer,
                   flush the buffer and then write the data directly.  In this
                   way buffered streams will cascade harmlessly. */
                flushBuffer();
                out.write(cbuf, off, len);
                return;
            }

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                System.arraycopy(cbuf, b, cb, nextChar, d);
                b += d;
                nextChar += d;
                if (nextChar >= nChars)
                    flushBuffer();
            }
        }
    }


    public void write(String s, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                s.getChars(b, b + d, cb, nextChar);
                b += d;
                nextChar += d;
                if (nextChar >= nChars)
                    flushBuffer();
            }
        }
    }


    public void newLine() throws IOException {
        write(lineSeparator);
    }


    public void flush() throws IOException {
        synchronized (lock) {
            flushBuffer();
            out.flush();
        }
    }

    @SuppressWarnings("try")
    public void close() throws IOException {
        synchronized (lock) {
            if (out == null) {
                return;
            }
            try (Writer w = out) {
                flushBuffer();
            } finally {
                out = null;
                cb = null;
            }
        }
    }
}
