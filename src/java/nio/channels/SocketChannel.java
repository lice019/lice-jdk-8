
package java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;


/**
 * 客户端的Socket通道，用于连接ServerSocket的通道
 */
public abstract class SocketChannel extends AbstractSelectableChannel
        implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {


    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    //获得一个Socket通道
    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    //开启以客户端的Socket，绑定服务端ServerSocket的IP和端口
    public static SocketChannel open(SocketAddress remote)
            throws IOException {
        SocketChannel sc = open();
        try {
            sc.connect(remote);
        } catch (Throwable x) {
            try {
                sc.close();
            } catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        assert sc.isConnected();
        return sc;
    }


    public final int validOps() {
        return (SelectionKey.OP_READ
                | SelectionKey.OP_WRITE
                | SelectionKey.OP_CONNECT);
    }


    // -- Socket-specific operations --


    @Override
    public abstract SocketChannel bind(SocketAddress local)
            throws IOException;


    @Override
    public abstract <T> SocketChannel setOption(SocketOption<T> name, T value)
            throws IOException;


    public abstract SocketChannel shutdownInput() throws IOException;


    public abstract SocketChannel shutdownOutput() throws IOException;

    //返回客户端的Socket
    public abstract Socket socket();


    public abstract boolean isConnected();


    public abstract boolean isConnectionPending();

    // 根据ip和port连接到对应服务器端
    public abstract boolean connect(SocketAddress remote) throws IOException;


    public abstract boolean finishConnect() throws IOException;


    public abstract SocketAddress getRemoteAddress() throws IOException;

    // -- ByteChannel operations --

    //读取服务端socket返回的数据
    public abstract int read(ByteBuffer dst) throws IOException;


    public abstract long read(ByteBuffer[] dsts, int offset, int length)
            throws IOException;

    /**
     * @throws NotYetConnectedException If this channel is not yet connected
     */
    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    //向服务端socket写数据
    public abstract int write(ByteBuffer src) throws IOException;


    public abstract long write(ByteBuffer[] srcs, int offset, int length)
            throws IOException;


    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }


    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
