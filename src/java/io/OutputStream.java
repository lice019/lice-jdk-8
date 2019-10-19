
package java.io;

/**
 * 这个抽象类是表示字节输出流的所有类的超类。 输出流接收输出字节并将其发送到某个接收器。
 * 需要定义OutputStream子类的应用OutputStream必须至少提供一个写入一个字节输出的方法
 */
public abstract class OutputStream implements Closeable, Flushable {

    public abstract void write(int b) throws IOException;


    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }


    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    //将数据刷新到目标接收器
    public void flush() throws IOException {
    }


    public void close() throws IOException {
    }

}
