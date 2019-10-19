
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * ServerSocket用于监听特定端口(也就是客户端的socket的请求)，调用accept()方法会阻塞当前线程，直到接收到一个Socket，而我们需要处理所接收到的Socket。
 */
public class ServerSocket implements java.io.Closeable {
    /**
     * Various states of this socket.
     */
    //socket
    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private Object closeLock = new Object();

    //这个Socket的实现。
    private SocketImpl impl;

    //是否使用旧的socket实现
    private boolean oldImpl = false;

    //通过SocketImpl创建ServerSocket
    ServerSocket(SocketImpl impl) {
        this.impl = impl;
        impl.setServerSocket(this);
    }

    //创建ServerSocket，自动设置socket实现
    public ServerSocket() throws IOException {
        setImpl();
    }

    //创建ServerSocket实例的同时设置服务端socket的端口
    public ServerSocket(int port) throws IOException {
        this(port, 50, null);
    }


    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, null);
    }

    //创建一个具有指定端口的服务器，侦听backlog和本地IP地址绑定。
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        setImpl();
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException(
                    "Port value out of range: " + port);
        if (backlog < 1)
            backlog = 50;
        try {
            bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch (SecurityException e) {
            close();
            throw e;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    //获取Socket实现类
    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl();
        return impl;
    }

    //检查旧的的socket实现
    private void checkOldImpl() {
        if (impl == null)
            return;
        // SocketImpl.connect() is a protected method, therefore we need to use
        // getDeclaredMethod, therefore we need permission to access the member
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws NoSuchMethodException {
                            impl.getClass().getDeclaredMethod("connect",
                                    SocketAddress.class,
                                    int.class);
                            return null;
                        }
                    });
        } catch (java.security.PrivilegedActionException e) {
            oldImpl = true;
        }
    }

    private void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            // No need to do a checkOldImpl() here, we know it's an up to date
            // SocketImpl!
            impl = new SocksSocketImpl();
        }
        if (impl != null)
            impl.setServerSocket(this);
    }

    //创建SocketImpl
    void createImpl() throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(true);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    //将 ServerSocket绑定到特定地址（IP地址和端口号）。
    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, 50);
    }

    //将 ServerSocket绑定到特定地址（IP地址和端口号）。
    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!oldImpl && isBound())
            throw new SocketException("Already bound");
        if (endpoint == null)
            endpoint = new InetSocketAddress(0);
        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if (backlog < 1)
            backlog = 50;
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null)
                security.checkListen(epoint.getPort());
            getImpl().bind(epoint.getAddress(), epoint.getPort());
            getImpl().listen(backlog);
            bound = true;
        } catch (SecurityException e) {
            bound = false;
            throw e;
        } catch (IOException e) {
            bound = false;
            throw e;
        }
    }

    //返回此服务器ServerSocket的本地地址。
    public InetAddress getInetAddress() {
        if (!isBound())
            return null;
        try {
            InetAddress in = getImpl().getInetAddress();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkConnect(in.getHostAddress(), -1);
            return in;
        } catch (SecurityException e) {
            return InetAddress.getLoopbackAddress();
        } catch (SocketException e) {
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return null;
    }

    //返回此ServerSocket绑定到的端点的端口。
    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return -1;
    }


    //返回此ServerSocket绑定到的端点的地址。
    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    //ServerSocket开启对客户端的Socket请求监听
    public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");
        Socket s = new Socket((SocketImpl) null);
        implAccept(s);
        return s;
    }

    //实现服务端Socket的监听客户端的socket
    protected final void implAccept(Socket s) throws IOException {
        SocketImpl si = null;
        try {
            if (s.impl == null)
                s.setImpl();
            else {
                s.impl.reset();
            }
            si = s.impl;
            s.impl = null;
            si.address = new InetAddress();
            si.fd = new FileDescriptor();
            getImpl().accept(si);

            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkAccept(si.getInetAddress().getHostAddress(),
                        si.getPort());
            }
        } catch (IOException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        } catch (SecurityException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        }
        s.impl = si;
        s.postAccept();
    }

    //关闭ServerSocket监听
    public void close() throws IOException {
        synchronized (closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }


    public ServerSocketChannel getChannel() {
        return null;
    }

    //返回ServerSocket的绑定状态。
    public boolean isBound() {
        // Before 1.3 ServerSockets were always bound during creation
        return bound || oldImpl;
    }

    //判断ServerSocket是否为关闭状态
    public boolean isClosed() {
        synchronized (closeLock) {
            return closed;
        }
    }

    //启用/禁用 SO_TIMEOUT带有指定超时，以毫秒为单位。
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }


    public synchronized int getSoTimeout() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* extra type safety */
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }

    //启用/禁用 SO_REUSEADDR的ServerSocket选项。
    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }


    public boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }


    public String toString() {
        if (!isBound())
            return "ServerSocket[unbound]";
        InetAddress in;
        if (System.getSecurityManager() != null)
            in = InetAddress.getLoopbackAddress();
        else
            in = impl.getInetAddress();
        return "ServerSocket[addr=" + in +
                ",localport=" + impl.getLocalPort() + "]";
    }

    void setBound() {
        bound = true;
    }

    void setCreated() {
        created = true;
    }

    //Socket实现类的工厂，可以通过工厂来获取Socket实现类
    private static SocketImplFactory factory = null;

    //注入Socket实现类的工厂
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    //设置从 ServerSocket接受的套接字的 SO_RCVBUF选项的默认建议值。
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
    }


    public synchronized int getReceiveBufferSize()
            throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }


    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth) {
        /* Not implemented yet */
    }

}
