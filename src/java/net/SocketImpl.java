
package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;

/**
 * SocketImpl:抽象类SocketImpl是实际实现socket的所有类的公共超类。 它用于创建客户端和服务器socket
 */
public abstract class SocketImpl implements SocketOptions {

    //客户端的Socket请求对象
    Socket socket = null;
    //服务端的ServerSocket的监听客户端对象
    ServerSocket serverSocket = null;

    //文件描述类
    protected FileDescriptor fd;

    //此套接字的远程端IP地址。
    protected InetAddress address;

    //此套接字连接到的远程主机上的端口号。
    //服务端的端口号
    protected int port;

    //本地端口
    protected int localport;


    //创建一个datagram socket的流
    protected abstract void create(boolean stream) throws IOException;

    //连接的主机和端口
    protected abstract void connect(String host, int port) throws IOException;

    //通过InetAddress对象来连接
    protected abstract void connect(InetAddress address, int port) throws IOException;


    protected abstract void connect(SocketAddress address, int timeout) throws IOException;

    //客户端绑定服务端的IP和端口
    protected abstract void bind(InetAddress host, int port) throws IOException;


    protected abstract void listen(int backlog) throws IOException;


    //监听一个客户端连接请求
    protected abstract void accept(SocketImpl s) throws IOException;

    //开启一个输入流
    protected abstract InputStream getInputStream() throws IOException;

    //开启一个输出流
    protected abstract OutputStream getOutputStream() throws IOException;

    //返回可以从该socket读取而不阻塞的字节数。
    protected abstract int available() throws IOException;

    //关闭socket流
    protected abstract void close() throws IOException;


    protected void shutdownInput() throws IOException {
      throw new IOException("Method not implemented!");
    }


    protected void shutdownOutput() throws IOException {
      throw new IOException("Method not implemented!");
    }


    protected FileDescriptor getFileDescriptor() {
        return fd;
    }


    protected InetAddress getInetAddress() {
        return address;
    }


    protected int getPort() {
        return port;
    }


    protected boolean supportsUrgentData () {
        return false; // must be overridden in sub-class
    }


    protected abstract void sendUrgentData (int data) throws IOException;


    protected int getLocalPort() {
        return localport;
    }

    void setSocket(Socket soc) {
        this.socket = soc;
    }

    Socket getSocket() {
        return socket;
    }

    void setServerSocket(ServerSocket soc) {
        this.serverSocket = soc;
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Returns the address and port of this socket as a {@code String}.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
        return "Socket[addr=" + getInetAddress() +
            ",port=" + getPort() + ",localport=" + getLocalPort()  + "]";
    }

    void reset() throws IOException {
        address = null;
        port = 0;
        localport = 0;
    }

    protected void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth)
    {
        /* Not implemented yet */
    }

    <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            setOption(SocketOptions.SO_KEEPALIVE, value);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(SocketOptions.SO_REUSEADDR, value);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            setOption(SocketOptions.SO_LINGER, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            setOption(SocketOptions.TCP_NODELAY, value);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            return (T)getOption(SocketOptions.SO_KEEPALIVE);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            return (T)getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T)getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T)getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            return (T)getOption(SocketOptions.SO_LINGER);
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T)getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            return (T)getOption(SocketOptions.TCP_NODELAY);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }
}
