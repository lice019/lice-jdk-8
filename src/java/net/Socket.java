
package java.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;

/**
 * Socket是应用层与TCP/IP协议族通信的中间软件抽象层，它是一组接口。在设计模式中，Socket其实就是一个门面模式，它把复杂的TCP/IP协议族隐藏在Socket接口后面，
 * 对用户来说，一组简单的接口就是全部，让Socket去组织数据，以符合指定的协议。
 * Socket：实际上就是对TCP/IP协议请求的封装，将这些请求的信息都封装到socket中，socket是扮演客户端请求的角色
 */
public class Socket implements java.io.Closeable {
    /**
     * Various states of this socket.
     */
    //socket请求服务端的状态
    private boolean created = false;
    private boolean bound = false;
    private boolean connected = false;
    private boolean closed = false;
    private Object closeLock = new Object();
    private boolean shutIn = false;
    private boolean shutOut = false;

    //由SocketImpl来实现
    SocketImpl impl;


    private boolean oldImpl = false;


    public Socket() {
        setImpl();
    }

    //创建一个未连接的socket，指定应该使用的代理类型（如果有的话），无论其他任何设置如何。
    public Socket(Proxy proxy) {
        // Create a copy of Proxy as a security measure
        if (proxy == null) {
            throw new IllegalArgumentException("Invalid Proxy");
        }
        Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY
                : sun.net.ApplicationProxy.create(proxy);
        Proxy.Type type = p.type();
        if (type == Proxy.Type.SOCKS || type == Proxy.Type.HTTP) {
            SecurityManager security = System.getSecurityManager();
            InetSocketAddress epoint = (InetSocketAddress) p.address();
            if (epoint.getAddress() != null) {
                checkAddress(epoint.getAddress(), "Socket");
            }
            if (security != null) {
                if (epoint.isUnresolved())
                    epoint = new InetSocketAddress(epoint.getHostName(), epoint.getPort());
                if (epoint.isUnresolved())
                    security.checkConnect(epoint.getHostName(), epoint.getPort());
                else
                    security.checkConnect(epoint.getAddress().getHostAddress(),
                            epoint.getPort());
            }
            impl = type == Proxy.Type.SOCKS ? new SocksSocketImpl(p)
                    : new HttpConnectSocketImpl(p);
            impl.setSocket(this);
        } else {
            if (p == Proxy.NO_PROXY) {
                if (factory == null) {
                    impl = new PlainSocketImpl();
                    impl.setSocket(this);
                } else
                    setImpl();
            } else
                throw new IllegalArgumentException("Invalid Proxy");
        }
    }


    protected Socket(SocketImpl impl) throws SocketException {
        this.impl = impl;
        if (impl != null) {
            checkOldImpl();
            this.impl.setSocket(this);
        }
    }

   //创建流socket（客户端请求socket）并将其连接到指定主机上的指定端口号。
    public Socket(String host, int port)
            throws UnknownHostException, IOException {
        this(host != null ? new InetSocketAddress(host, port) :
                        new InetSocketAddress(InetAddress.getByName(null), port),
                (SocketAddress) null, true);
    }

    //创建流套接字并将其连接到指定IP地址的指定端口号。
    public Socket(InetAddress address, int port) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null,
                (SocketAddress) null, true);
    }

    //创建套接字并将其连接到指定的远程端口上指定的远程地址。
    public Socket(String host, int port, InetAddress localAddr,
                  int localPort) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) :
                        new InetSocketAddress(InetAddress.getByName(null), port),
                new InetSocketAddress(localAddr, localPort), true);
    }

    //创建套接字并将其连接到指定的远程端口上指定的远程地址。
    public Socket(InetAddress address, int port, InetAddress localAddr,
                  int localPort) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null,
                new InetSocketAddress(localAddr, localPort), true);
    }


    @Deprecated
    public Socket(String host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) :
                        new InetSocketAddress(InetAddress.getByName(null), port),
                (SocketAddress) null, stream);
    }


    @Deprecated
    public Socket(InetAddress host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) : null,
                new InetSocketAddress(0), stream);
    }

    private Socket(SocketAddress address, SocketAddress localAddr,
                   boolean stream) throws IOException {
        setImpl();

        // backward compatibility
        if (address == null)
            throw new NullPointerException();

        try {
            createImpl(stream);
            if (localAddr != null)
                bind(localAddr);
            connect(address);
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            try {
                close();
            } catch (IOException ce) {
                e.addSuppressed(ce);
            }
            throw e;
        }
    }


    void createImpl(boolean stream) throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(stream);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    private void checkOldImpl() {
        if (impl == null)
            return;
        // SocketImpl.connect() is a protected method, therefore we need to use
        // getDeclaredMethod, therefore we need permission to access the member

        oldImpl = AccessController.doPrivileged
                (new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        Class<?> clazz = impl.getClass();
                        while (true) {
                            try {
                                clazz.getDeclaredMethod("connect", SocketAddress.class, int.class);
                                return Boolean.FALSE;
                            } catch (NoSuchMethodException e) {
                                clazz = clazz.getSuperclass();
                                // java.net.SocketImpl class will always have this abstract method.
                                // If we have not found it by now in the hierarchy then it does not
                                // exist, we are an old style impl.
                                if (clazz.equals(java.net.SocketImpl.class)) {
                                    return Boolean.TRUE;
                                }
                            }
                        }
                    }
                });
    }


    void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            // No need to do a checkOldImpl() here, we know it's an up to date
            // SocketImpl!
            impl = new SocksSocketImpl();
        }
        if (impl != null)
            impl.setSocket(this);
    }



    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl(true);
        return impl;
    }

    //socket连接到服务端的ServerSocket
    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }


    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null)
            throw new IllegalArgumentException("connect: The address can't be null");

        if (timeout < 0)
            throw new IllegalArgumentException("connect: timeout can't be negative");

        if (isClosed())
            throw new SocketException("Socket is closed");

        if (!oldImpl && isConnected())
            throw new SocketException("already connected");

        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");

        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        InetAddress addr = epoint.getAddress();
        int port = epoint.getPort();
        checkAddress(addr, "connect");

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (epoint.isUnresolved())
                security.checkConnect(epoint.getHostName(), port);
            else
                security.checkConnect(addr.getHostAddress(), port);
        }
        if (!created)
            createImpl(true);
        if (!oldImpl)
            impl.connect(epoint, timeout);
        else if (timeout == 0) {
            if (epoint.isUnresolved())
                impl.connect(addr.getHostName(), port);
            else
                impl.connect(addr, port);
        } else
            throw new UnsupportedOperationException("SocketImpl.connect(addr, timeout)");
        connected = true;
        /*
         * If the socket was not bound before the connect, it is now because
         * the kernel will have picked an ephemeral port & a local address
         */
        bound = true;
    }


    public void bind(SocketAddress bindpoint) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!oldImpl && isBound())
            throw new SocketException("Already bound");

        if (bindpoint != null && (!(bindpoint instanceof InetSocketAddress)))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) bindpoint;
        if (epoint != null && epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if (epoint == null) {
            epoint = new InetSocketAddress(0);
        }
        InetAddress addr = epoint.getAddress();
        int port = epoint.getPort();
        checkAddress(addr, "bind");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(port);
        }
        getImpl().bind(addr, port);
        bound = true;
    }

    private void checkAddress(InetAddress addr, String op) {
        if (addr == null) {
            return;
        }
        if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
            throw new IllegalArgumentException(op + ": invalid address type");
        }
    }

    /**
     * set the flags after an accept() call.
     */
    final void postAccept() {
        connected = true;
        created = true;
        bound = true;
    }

    void setCreated() {
        created = true;
    }

    void setBound() {
        bound = true;
    }

    void setConnected() {
        connected = true;
    }


    public InetAddress getInetAddress() {
        if (!isConnected())
            return null;
        try {
            return getImpl().getInetAddress();
        } catch (SocketException e) {
        }
        return null;
    }


    public InetAddress getLocalAddress() {
        // This is for backward compatibility
        if (!isBound())
            return InetAddress.anyLocalAddress();
        InetAddress in = null;
        try {
            in = (InetAddress) getImpl().getOption(SocketOptions.SO_BINDADDR);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkConnect(in.getHostAddress(), -1);
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
        } catch (SecurityException e) {
            in = InetAddress.getLoopbackAddress();
        } catch (Exception e) {
            in = InetAddress.anyLocalAddress(); // "0.0.0.0"
        }
        return in;
    }


    public int getPort() {
        if (!isConnected())
            return 0;
        try {
            return getImpl().getPort();
        } catch (SocketException e) {
            // Shouldn't happen as we're connected
        }
        return -1;
    }


    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
            // shouldn't happen as we're bound
        }
        return -1;
    }


    public SocketAddress getRemoteSocketAddress() {
        if (!isConnected())
            return null;
        return new InetSocketAddress(getInetAddress(), getPort());
    }



    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getLocalAddress(), getLocalPort());
    }

    //获取NIO的socket管道
    public SocketChannel getChannel() {
        return null;
    }

    //获取网络的输入流
    public InputStream getInputStream() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isInputShutdown())
            throw new SocketException("Socket input is shutdown");
        final Socket s = this;
        InputStream is = null;
        try {
            is = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            return impl.getInputStream();
                        }
                    });
        } catch (java.security.PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        return is;
    }

    //获取网络的输出流
    public OutputStream getOutputStream() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isOutputShutdown())
            throw new SocketException("Socket output is shutdown");
        final Socket s = this;
        OutputStream os = null;
        try {
            os = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<OutputStream>() {
                        public OutputStream run() throws IOException {
                            return impl.getOutputStream();
                        }
                    });
        } catch (java.security.PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        return os;
    }

    //设置TCP协议
    public void setTcpNoDelay(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.TCP_NODELAY, Boolean.valueOf(on));
    }


    public boolean getTcpNoDelay() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.TCP_NODELAY)).booleanValue();
    }


    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!on) {
            getImpl().setOption(SocketOptions.SO_LINGER, new Boolean(on));
        } else {
            if (linger < 0) {
                throw new IllegalArgumentException("invalid value for SO_LINGER");
            }
            if (linger > 65535)
                linger = 65535;
            getImpl().setOption(SocketOptions.SO_LINGER, new Integer(linger));
        }
    }


    public int getSoLinger() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_LINGER);
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return -1;
        }
    }


    public void sendUrgentData(int data) throws IOException {
        if (!getImpl().supportsUrgentData()) {
            throw new SocketException("Urgent data not supported");
        }
        getImpl().sendUrgentData(data);
    }


    public void setOOBInline(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_OOBINLINE, Boolean.valueOf(on));
    }


    public boolean getOOBInline() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.SO_OOBINLINE)).booleanValue();
    }


    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (timeout < 0)
            throw new IllegalArgumentException("timeout can't be negative");

        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }


    public synchronized int getSoTimeout() throws SocketException {
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

    //设置发送缓冲区大小
    public synchronized void setSendBufferSize(int size)
            throws SocketException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative send size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_SNDBUF, new Integer(size));
    }


    public synchronized int getSendBufferSize() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_SNDBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }


    public synchronized void setReceiveBufferSize(int size)
            throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
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

    //设置活跃检查
    public void setKeepAlive(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_KEEPALIVE, Boolean.valueOf(on));
    }


    public boolean getKeepAlive() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) getImpl().getOption(SocketOptions.SO_KEEPALIVE)).booleanValue();
    }


    public void setTrafficClass(int tc) throws SocketException {
        if (tc < 0 || tc > 255)
            throw new IllegalArgumentException("tc is not in range 0 -- 255");

        if (isClosed())
            throw new SocketException("Socket is closed");
        try {
            getImpl().setOption(SocketOptions.IP_TOS, tc);
        } catch (SocketException se) {
            // not supported if socket already connected
            // Solaris returns error in such cases
            if (!isConnected())
                throw se;
        }
    }


    public int getTrafficClass() throws SocketException {
        return ((Integer) (getImpl().getOption(SocketOptions.IP_TOS))).intValue();
    }


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


    public synchronized void close() throws IOException {
        synchronized (closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }


    public void shutdownInput() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isInputShutdown())
            throw new SocketException("Socket input is already shutdown");
        getImpl().shutdownInput();
        shutIn = true;
    }


    public void shutdownOutput() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isConnected())
            throw new SocketException("Socket is not connected");
        if (isOutputShutdown())
            throw new SocketException("Socket output is already shutdown");
        getImpl().shutdownOutput();
        shutOut = true;
    }


    public String toString() {
        try {
            if (isConnected())
                return "Socket[addr=" + getImpl().getInetAddress() +
                        ",port=" + getImpl().getPort() +
                        ",localport=" + getImpl().getLocalPort() + "]";
        } catch (SocketException e) {
        }
        return "Socket[unconnected]";
    }


    public boolean isConnected() {
        // Before 1.3 Sockets were always connected during creation
        return connected || oldImpl;
    }


    public boolean isBound() {
        // Before 1.3 Sockets were always bound during creation
        return bound || oldImpl;
    }


    public boolean isClosed() {
        synchronized (closeLock) {
            return closed;
        }
    }


    public boolean isInputShutdown() {
        return shutIn;
    }


    public boolean isOutputShutdown() {
        return shutOut;
    }

    /**
     * The factory for all client sockets.
     */
    private static SocketImplFactory factory = null;


    public static synchronized void setSocketImplFactory(SocketImplFactory fac)
            throws IOException {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }


    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth) {
        /* Not implemented yet */
    }
}
