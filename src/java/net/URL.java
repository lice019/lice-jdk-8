
package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.ObjectInputStream.GetField;
import java.util.Hashtable;
import java.util.StringTokenizer;

import sun.net.util.IPAddressUtil;
import sun.security.util.SecurityConstants;

/**
 * Class URL表示统一资源定位符，指向万维网上的“资源”的指针。 资源可以像文件或目录一样简单，或者可以是对更复杂的对象的引用，例如对数据库或搜索引擎的查询。
 */
public final class URL implements java.io.Serializable {

    static final String BUILTIN_HANDLERS_PREFIX = "sun.net.www.protocol";
    static final long serialVersionUID = -7627629688361524110L;


    private static final String protocolPathProp = "java.protocol.handler.pkgs";

    //协议：
    //(ftp, http, nntp, ... etc.) .
    private String protocol;

    //主机ip
    private String host;

    //端口
    private int port = -1;

    //特殊文件路径
    private String file;

    //URL查询部分
    private transient String query;

    //此URL的权威部分。
    private String authority;

    //此URL的路径部分。
    private transient String path;

    //这个URL的用户信息部分。
    private transient String userInfo;

    /**
     * # reference.
     *
     * @serial
     */
    private String ref;

    //主机的IP地址，在equals和hashCode中使用
    //按需计算。未初始化或未知的主机地址为空。
    transient InetAddress hostAddress;

    //URL的流处理器
    transient URLStreamHandler handler;

    /* Our hash code.
     * @serial
     */
    private int hashCode = -1;

    private transient UrlDeserializedState tempState;

    //构造方法
    //协议、主机IP、端口、文件，定位到资源的URL连接
    public URL(String protocol, String host, int port, String file)
            throws MalformedURLException {
        this(protocol, host, port, file, null);
    }


    public URL(String protocol, String host, String file)
            throws MalformedURLException {
        this(protocol, host, -1, file);
    }


    public URL(String protocol, String host, int port, String file,
               URLStreamHandler handler) throws MalformedURLException {
        if (handler != null) {
            //安全管理器，防范未知的程序
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // check for permission to specify a handler
                checkSpecifyHandler(sm);
            }
        }

        protocol = protocol.toLowerCase();
        this.protocol = protocol;
        if (host != null) {

            /**
             * if host is a literal IPv6 address,
             * we will make it conform to RFC 2732
             */
            if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
                host = "[" + host + "]";
            }
            this.host = host;

            if (port < -1) {
                throw new MalformedURLException("Invalid port number :" +
                        port);
            }
            this.port = port;
            authority = (port == -1) ? host : host + ":" + port;
        }

        Parts parts = new Parts(file);
        path = parts.getPath();
        query = parts.getQuery();

        if (query != null) {
            this.file = path + "?" + query;
        } else {
            this.file = path;
        }
        ref = parts.getRef();

        // Note: we don't do full validation of the URL here. Too risky to change
        // right now, but worth considering for future reference. -br
        if (handler == null &&
                (handler = getURLStreamHandler(protocol)) == null) {
            throw new MalformedURLException("unknown protocol: " + protocol);
        }
        this.handler = handler;
        if (host != null && isBuiltinStreamHandler(handler)) {
            String s = IPAddressUtil.checkExternalForm(this);
            if (s != null) {
                throw new MalformedURLException(s);
            }
        }
    }

    //从 String表示形成一个 URL对象。
    public URL(String spec) throws MalformedURLException {
        this(null, spec);
    }

    //通过在指定的上下文中解析给定的规范来创建一个URL。
    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, null);
    }

    //通过在指定上下文中使用指定的处理程序解析给定规范来创建URL。
    public URL(URL context, String spec, URLStreamHandler handler)
            throws MalformedURLException {
        String original = spec;
        int i, limit, c;
        int start = 0;
        String newProtocol = null;
        boolean aRef = false;
        boolean isRelative = false;

        // Check for permission to specify a handler
        if (handler != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                checkSpecifyHandler(sm);
            }
        }

        try {
            limit = spec.length();
            while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
                limit--;        //eliminate trailing whitespace
            }
            while ((start < limit) && (spec.charAt(start) <= ' ')) {
                start++;        // eliminate leading whitespace
            }

            if (spec.regionMatches(true, start, "url:", 0, 4)) {
                start += 4;
            }
            if (start < spec.length() && spec.charAt(start) == '#') {
                /* we're assuming this is a ref relative to the context URL.
                 * This means protocols cannot start w/ '#', but we must parse
                 * ref URL's like: "hello:there" w/ a ':' in them.
                 */
                aRef = true;
            }
            for (i = start; !aRef && (i < limit) &&
                    ((c = spec.charAt(i)) != '/'); i++) {
                if (c == ':') {

                    String s = spec.substring(start, i).toLowerCase();
                    if (isValidProtocol(s)) {
                        newProtocol = s;
                        start = i + 1;
                    }
                    break;
                }
            }

            // Only use our context if the protocols match.
            protocol = newProtocol;
            if ((context != null) && ((newProtocol == null) ||
                    newProtocol.equalsIgnoreCase(context.protocol))) {
                // inherit the protocol handler from the context
                // if not specified to the constructor
                if (handler == null) {
                    handler = context.handler;
                }

                // If the context is a hierarchical URL scheme and the spec
                // contains a matching scheme then maintain backwards
                // compatibility and treat it as if the spec didn't contain
                // the scheme; see 5.2.3 of RFC2396
                if (context.path != null && context.path.startsWith("/"))
                    newProtocol = null;

                if (newProtocol == null) {
                    protocol = context.protocol;
                    authority = context.authority;
                    userInfo = context.userInfo;
                    host = context.host;
                    port = context.port;
                    file = context.file;
                    path = context.path;
                    isRelative = true;
                }
            }

            if (protocol == null) {
                throw new MalformedURLException("no protocol: " + original);
            }

            // Get the protocol handler if not specified or the protocol
            // of the context could not be used
            if (handler == null &&
                    (handler = getURLStreamHandler(protocol)) == null) {
                throw new MalformedURLException("unknown protocol: " + protocol);
            }

            this.handler = handler;

            i = spec.indexOf('#', start);
            if (i >= 0) {
                ref = spec.substring(i + 1, limit);
                limit = i;
            }

            /*
             * Handle special case inheritance of query and fragment
             * implied by RFC2396 section 5.2.2.
             */
            if (isRelative && start == limit) {
                query = context.query;
                if (ref == null) {
                    ref = context.ref;
                }
            }

            handler.parseURL(this, spec, start, limit);

        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            MalformedURLException exception = new MalformedURLException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /*
     * Returns true if specified string is a valid protocol name.
     */
    private boolean isValidProtocol(String protocol) {
        int len = protocol.length();
        if (len < 1)
            return false;
        char c = protocol.charAt(0);
        if (!Character.isLetter(c))
            return false;
        for (int i = 1; i < len; i++) {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' &&
                    c != '-') {
                return false;
            }
        }
        return true;
    }

    /*
     * Checks for permission to specify a stream handler.
     */
    private void checkSpecifyHandler(SecurityManager sm) {
        sm.checkPermission(SecurityConstants.SPECIFY_HANDLER_PERMISSION);
    }


    void set(String protocol, String host, int port,
             String file, String ref) {
        synchronized (this) {
            this.protocol = protocol;
            this.host = host;
            authority = port == -1 ? host : host + ":" + port;
            this.port = port;
            this.file = file;
            this.ref = ref;
            /* This is very important. We must recompute this after the
             * URL has been changed. */
            hashCode = -1;
            hostAddress = null;
            int q = file.lastIndexOf('?');
            if (q != -1) {
                query = file.substring(q + 1);
                path = file.substring(0, q);
            } else
                path = file;
        }
    }


    void set(String protocol, String host, int port,
             String authority, String userInfo, String path,
             String query, String ref) {
        synchronized (this) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.file = query == null ? path : path + "?" + query;
            this.userInfo = userInfo;
            this.path = path;
            this.ref = ref;
            /* This is very important. We must recompute this after the
             * URL has been changed. */
            hashCode = -1;
            hostAddress = null;
            this.query = query;
            this.authority = authority;
        }
    }


    public String getQuery() {
        return query;
    }

    //获取URL的路径
    public String getPath() {
        return path;
    }

    //获取用户信息
    public String getUserInfo() {
        return userInfo;
    }

    //获取相应的权威
    public String getAuthority() {
        return authority;
    }

    //获取服务器端口
    public int getPort() {
        return port;
    }

    //获取服务器默认端口
    public int getDefaultPort() {
        return handler.getDefaultPort();
    }

    //获取服务器协议
    public String getProtocol() {
        return protocol;
    }

    //获取服务器主机
    public String getHost() {
        return host;
    }


    public String getFile() {
        return file;
    }


    public String getRef() {
        return ref;
    }


    public boolean equals(Object obj) {
        if (!(obj instanceof URL))
            return false;
        URL u2 = (URL) obj;

        return handler.equals(this, u2);
    }


    public synchronized int hashCode() {
        if (hashCode != -1)
            return hashCode;

        hashCode = handler.hashCode(this);
        return hashCode;
    }


    public boolean sameFile(URL other) {
        return handler.sameFile(this, other);
    }


    public String toString() {
        return toExternalForm();
    }


    public String toExternalForm() {
        return handler.toExternalForm(this);
    }


    public URI toURI() throws URISyntaxException {
        URI uri = new URI(toString());
        if (authority != null && isBuiltinStreamHandler(handler)) {
            String s = IPAddressUtil.checkAuthority(this);
            if (s != null) throw new URISyntaxException(authority, s);
        }
        return uri;
    }

    //开启与服务器的连接
    public URLConnection openConnection() throws java.io.IOException {
        return handler.openConnection(this);
    }

    //通过代理对象开启对服务器的连接
    public URLConnection openConnection(Proxy proxy)
            throws java.io.IOException {
        //代理对象抛出异常
        if (proxy == null) {
            throw new IllegalArgumentException("proxy can not be null");
        }

        // Create a copy of Proxy as a security measure
        Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY : sun.net.ApplicationProxy.create(proxy);
        SecurityManager sm = System.getSecurityManager();
        if (p.type() != Proxy.Type.DIRECT && sm != null) {
            InetSocketAddress epoint = (InetSocketAddress) p.address();
            if (epoint.isUnresolved())
                sm.checkConnect(epoint.getHostName(), epoint.getPort());
            else
                sm.checkConnect(epoint.getAddress().getHostAddress(),
                        epoint.getPort());
        }
        return handler.openConnection(this, p);
    }

    //URL网络输入流
    public final InputStream openStream() throws java.io.IOException {
        //通过URLConnection来获取网络输入流
        return openConnection().getInputStream();
    }

    //获取连接通道的content
    public final Object getContent() throws java.io.IOException {
        //通过URLConnection
        return openConnection().getContent();
    }


    public final Object getContent(Class[] classes)
            throws java.io.IOException {
        return openConnection().getContent(classes);
    }

    //URLStreamHandler工厂。
    static URLStreamHandlerFactory factory;


    public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
        synchronized (streamHandlerLock) {
            if (factory != null) {
                throw new Error("factory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSetFactory();
            }
            handlers.clear();
            factory = fac;
        }
    }

    /**
     * A table of protocol handlers.
     */
    static Hashtable<String, URLStreamHandler> handlers = new Hashtable<>();
    private static Object streamHandlerLock = new Object();

    /**
     * Returns the Stream Handler.
     *
     * @param protocol the protocol to use
     */
    static URLStreamHandler getURLStreamHandler(String protocol) {

        URLStreamHandler handler = handlers.get(protocol);
        if (handler == null) {

            boolean checkedWithFactory = false;

            // Use the factory (if any)
            if (factory != null) {
                handler = factory.createURLStreamHandler(protocol);
                checkedWithFactory = true;
            }

            // Try java protocol handler
            if (handler == null) {
                String packagePrefixList = null;

                packagePrefixList
                        = java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction(
                                protocolPathProp, ""));
                if (packagePrefixList != "") {
                    packagePrefixList += "|";
                }

                // REMIND: decide whether to allow the "null" class prefix
                // or not.
                packagePrefixList += "sun.net.www.protocol";

                StringTokenizer packagePrefixIter =
                        new StringTokenizer(packagePrefixList, "|");

                while (handler == null &&
                        packagePrefixIter.hasMoreTokens()) {

                    String packagePrefix =
                            packagePrefixIter.nextToken().trim();
                    try {
                        String clsName = packagePrefix + "." + protocol +
                                ".Handler";
                        Class<?> cls = null;
                        try {
                            cls = Class.forName(clsName);
                        } catch (ClassNotFoundException e) {
                            ClassLoader cl = ClassLoader.getSystemClassLoader();
                            if (cl != null) {
                                cls = cl.loadClass(clsName);
                            }
                        }
                        if (cls != null) {
                            handler =
                                    (URLStreamHandler) cls.newInstance();
                        }
                    } catch (Exception e) {
                        // any number of exceptions can get thrown here
                    }
                }
            }

            synchronized (streamHandlerLock) {

                URLStreamHandler handler2 = null;

                // Check again with hashtable just in case another
                // thread created a handler since we last checked
                handler2 = handlers.get(protocol);

                if (handler2 != null) {
                    return handler2;
                }

                // Check with factory if another thread set a
                // factory since our last check
                if (!checkedWithFactory && factory != null) {
                    handler2 = factory.createURLStreamHandler(protocol);
                }

                if (handler2 != null) {
                    // The handler from the factory must be given more
                    // importance. Discard the default handler that
                    // this thread created.
                    handler = handler2;
                }

                // Insert this handler into the hashtable
                if (handler != null) {
                    handlers.put(protocol, handler);
                }

            }
        }

        return handler;

    }

    /**
     * @serialField protocol String
     * @serialField host String
     * @serialField port int
     * @serialField authority String
     * @serialField file String
     * @serialField ref String
     * @serialField hashCode int
     */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("protocol", String.class),
            new ObjectStreamField("host", String.class),
            new ObjectStreamField("port", int.class),
            new ObjectStreamField("authority", String.class),
            new ObjectStreamField("file", String.class),
            new ObjectStreamField("ref", String.class),
            new ObjectStreamField("hashCode", int.class),};

    /**
     * WriteObject is called to save the state of the URL to an
     * ObjectOutputStream. The handler is not saved since it is
     * specific to this system.
     *
     * @serialData the default write object value. When read back in,
     * the reader must ensure that calling getURLStreamHandler with
     * the protocol variable returns a valid URLStreamHandler and
     * throw an IOException if it does not.
     */
    private synchronized void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        s.defaultWriteObject(); // write the fields
    }

    /**
     * readObject is called to restore the state of the URL from the
     * stream.  It reads the components of the URL and finds the local
     * stream handler.
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        GetField gf = s.readFields();
        String protocol = (String) gf.get("protocol", null);
        if (getURLStreamHandler(protocol) == null) {
            throw new IOException("unknown protocol: " + protocol);
        }
        String host = (String) gf.get("host", null);
        int port = gf.get("port", -1);
        String authority = (String) gf.get("authority", null);
        String file = (String) gf.get("file", null);
        String ref = (String) gf.get("ref", null);
        int hashCode = gf.get("hashCode", -1);
        if (authority == null
                && ((host != null && host.length() > 0) || port != -1)) {
            if (host == null)
                host = "";
            authority = (port == -1) ? host : host + ":" + port;
        }
        tempState = new UrlDeserializedState(protocol, host, port, authority,
                file, ref, hashCode);
    }

    /**
     * Replaces the de-serialized object with an URL object.
     *
     * @return a newly created object from the deserialzed state.
     * @throws ObjectStreamException if a new object replacing this
     *                               object could not be created
     */

    private Object readResolve() throws ObjectStreamException {

        URLStreamHandler handler = null;
        // already been checked in readObject
        handler = getURLStreamHandler(tempState.getProtocol());

        URL replacementURL = null;
        if (isBuiltinStreamHandler(handler.getClass().getName())) {
            replacementURL = fabricateNewURL();
        } else {
            replacementURL = setDeserializedFields(handler);
        }
        return replacementURL;
    }

    private URL setDeserializedFields(URLStreamHandler handler) {
        URL replacementURL;
        String userInfo = null;
        String protocol = tempState.getProtocol();
        String host = tempState.getHost();
        int port = tempState.getPort();
        String authority = tempState.getAuthority();
        String file = tempState.getFile();
        String ref = tempState.getRef();
        int hashCode = tempState.getHashCode();


        // Construct authority part
        if (authority == null
                && ((host != null && host.length() > 0) || port != -1)) {
            if (host == null)
                host = "";
            authority = (port == -1) ? host : host + ":" + port;

            // Handle hosts with userInfo in them
            int at = host.lastIndexOf('@');
            if (at != -1) {
                userInfo = host.substring(0, at);
                host = host.substring(at + 1);
            }
        } else if (authority != null) {
            // Construct user info part
            int ind = authority.indexOf('@');
            if (ind != -1)
                userInfo = authority.substring(0, ind);
        }

        // Construct path and query part
        String path = null;
        String query = null;
        if (file != null) {
            // Fix: only do this if hierarchical?
            int q = file.lastIndexOf('?');
            if (q != -1) {
                query = file.substring(q + 1);
                path = file.substring(0, q);
            } else
                path = file;
        }

        // Set the object fields.
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.file = file;
        this.authority = authority;
        this.ref = ref;
        this.hashCode = hashCode;
        this.handler = handler;
        this.query = query;
        this.path = path;
        this.userInfo = userInfo;
        replacementURL = this;
        return replacementURL;
    }

    private URL fabricateNewURL()
            throws InvalidObjectException {
        // create URL string from deserialized object
        URL replacementURL = null;
        String urlString = tempState.reconstituteUrlString();

        try {
            replacementURL = new URL(urlString);
        } catch (MalformedURLException mEx) {
            resetState();
            InvalidObjectException invoEx = new InvalidObjectException(
                    "Malformed URL: " + urlString);
            invoEx.initCause(mEx);
            throw invoEx;
        }
        replacementURL.setSerializedHashCode(tempState.getHashCode());
        resetState();
        return replacementURL;
    }

    boolean isBuiltinStreamHandler(URLStreamHandler handler) {
        return isBuiltinStreamHandler(handler.getClass().getName());
    }

    private boolean isBuiltinStreamHandler(String handlerClassName) {
        return (handlerClassName.startsWith(BUILTIN_HANDLERS_PREFIX));
    }

    private void resetState() {
        this.protocol = null;
        this.host = null;
        this.port = -1;
        this.file = null;
        this.authority = null;
        this.ref = null;
        this.hashCode = -1;
        this.handler = null;
        this.query = null;
        this.path = null;
        this.userInfo = null;
        this.tempState = null;
    }

    private void setSerializedHashCode(int hc) {
        this.hashCode = hc;
    }
}

class Parts {
    String path, query, ref;

    Parts(String file) {
        int ind = file.indexOf('#');
        ref = ind < 0 ? null : file.substring(ind + 1);
        file = ind < 0 ? file : file.substring(0, ind);
        int q = file.lastIndexOf('?');
        if (q != -1) {
            query = file.substring(q + 1);
            path = file.substring(0, q);
        } else {
            path = file;
        }
    }

    String getPath() {
        return path;
    }

    String getQuery() {
        return query;
    }

    String getRef() {
        return ref;
    }
}

final class UrlDeserializedState {
    private final String protocol;
    private final String host;
    private final int port;
    private final String authority;
    private final String file;
    private final String ref;
    private final int hashCode;

    public UrlDeserializedState(String protocol,
                                String host, int port,
                                String authority, String file,
                                String ref, int hashCode) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.authority = authority;
        this.file = file;
        this.ref = ref;
        this.hashCode = hashCode;
    }

    String getProtocol() {
        return protocol;
    }

    String getHost() {
        return host;
    }

    String getAuthority() {
        return authority;
    }

    int getPort() {
        return port;
    }

    String getFile() {
        return file;
    }

    String getRef() {
        return ref;
    }

    int getHashCode() {
        return hashCode;
    }

    String reconstituteUrlString() {

        // pre-compute length of StringBuilder
        int len = protocol.length() + 1;
        if (authority != null && authority.length() > 0)
            len += 2 + authority.length();
        if (file != null) {
            len += file.length();
        }
        if (ref != null)
            len += 1 + ref.length();
        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(":");
        if (authority != null && authority.length() > 0) {
            result.append("//");
            result.append(authority);
        }
        if (file != null) {
            result.append(file);
        }
        if (ref != null) {
            result.append("#");
            result.append(ref);
        }
        return result.toString();
    }
}
