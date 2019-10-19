package java.nio.channels;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * ServerSocketChannel：网络服务端的Socket的通道。
 * SelectorProvider的实现类：
 */
public abstract class ServerSocketChannel extends AbstractSelectableChannel implements NetworkChannel {


    //该包的类和ServerSocketChannel的子类才能创建ServerSocketChannel的实例对象。并传入SelectorProvider对象去获取管道
    protected ServerSocketChannel(SelectorProvider provider) {
        super(provider);
    }


    //开启ServerSocketChannel管道---使用的多
    public static ServerSocketChannel open() throws IOException {
        //实际是返回SelectorProvider提供的管道
        return SelectorProvider.provider().openServerSocketChannel();
    }


    //返回确定此频道支持的操作的操作集。
    public final int validOps() {
        return SelectionKey.OP_ACCEPT;
    }


    // -- ServerSocket-specific operations --ServerSocket特有的接口


    //通过SocketAddress对象绑定IP地址
    public final ServerSocketChannel bind(SocketAddress local)
            throws IOException {
        return bind(local, 0);
    }



    public abstract ServerSocketChannel bind(SocketAddress local, int backlog)
            throws IOException;

    //设置套接字选项的值。
    public abstract <T> ServerSocketChannel setOption(SocketOption<T> name, T value)
            throws IOException;

    //从ServerSocketChannel通道对象中获取一个ServerSocket对象
    public abstract ServerSocket socket();


    //监听新进来的连接
    //ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。
    public abstract SocketChannel accept() throws IOException;


    //返回此通道的套接字所绑定的套接字地址。
    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;

}
