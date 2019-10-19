
package java.nio.file.spi;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.channels.*;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 文件系统的服务提供者类。 Files类定义的方法通常会委托给这个类的一个实例。
 * 文件系统提供程序是实现由该类定义的抽象方法的该类的具体实现。 提供者由URI scheme标识 。 默认提供程序由URI方案“文件”标识。 它创建FileSystem ，提供了访问给Java虚拟机访问的文件系统。 FileSystems类定义了文件系统提供程序的位置和加载方式。 默认提供程序通常是系统默认提供程序，但如果设置了系统属性java.nio.file.spi.DefaultFileSystemProvider则可能会被覆盖。 在这种情况下，提供程序具有一个参数构造函数，其形式参数类型为FileSystemProvider 。 所有其他提供程序都有一个零参数构造函数来初始化提供程序。
 * <p>
 * 提供者是一个或多个FileSystem实例的工厂。 每个文件系统由URI ，其URI的方案与提供者的scheme匹配。 例如，默认文件系统由URI "file:///" 。 例如，基于存储器的文件系统可以由诸如"memory:///?name=logfs"的URI来"memory:///?name=logfs" 。 newFileSystem方法可用于创建文件系统，并且可以使用getFileSystem方法来获得对由提供者创建的现有文件系统的引用。 如果提供商是单个文件系统的工厂，那么如果在提供程序初始化时创建文件系统，或稍后当调用newFileSystem方法时， newFileSystem于提供程序。 在默认提供程序的情况下， FileSystem是在提供程序FileSystem时创建的。
 * <p>
 * 此类中的所有方法都可以安全地被多个并发线程使用。
 */

public abstract class FileSystemProvider {
    // lock using when loading providers
    private static final Object lock = new Object();

    // installed providers
    private static volatile List<FileSystemProvider> installedProviders;

    // used to avoid recursive loading of instaled providers
    private static boolean loadingProviders = false;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("fileSystemProvider"));
        return null;
    }

    private FileSystemProvider(Void ignore) {
    }


    protected FileSystemProvider() {
        this(checkPermission());
    }

    // loads all installed providers
    private static List<FileSystemProvider> loadInstalledProviders() {
        List<FileSystemProvider> list = new ArrayList<FileSystemProvider>();

        ServiceLoader<FileSystemProvider> sl = ServiceLoader
                .load(FileSystemProvider.class, ClassLoader.getSystemClassLoader());

        // ServiceConfigurationError may be throw here
        for (FileSystemProvider provider : sl) {
            String scheme = provider.getScheme();

            // add to list if the provider is not "file" and isn't a duplicate
            if (!scheme.equalsIgnoreCase("file")) {
                boolean found = false;
                for (FileSystemProvider p : list) {
                    if (p.getScheme().equalsIgnoreCase(scheme)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.add(provider);
                }
            }
        }
        return list;
    }


    public static List<FileSystemProvider> installedProviders() {
        if (installedProviders == null) {
            // ensure default provider is initialized
            FileSystemProvider defaultProvider = FileSystems.getDefault().provider();

            synchronized (lock) {
                if (installedProviders == null) {
                    if (loadingProviders) {
                        throw new Error("Circular loading of installed providers detected");
                    }
                    loadingProviders = true;

                    List<FileSystemProvider> list = AccessController
                            .doPrivileged(new PrivilegedAction<List<FileSystemProvider>>() {
                                @Override
                                public List<FileSystemProvider> run() {
                                    return loadInstalledProviders();
                                }
                            });

                    // insert the default provider at the start of the list
                    list.add(0, defaultProvider);

                    installedProviders = Collections.unmodifiableList(list);
                }
            }
        }
        return installedProviders;
    }

    /**
     * Returns the URI scheme that identifies this provider.
     *
     * @return The URI scheme
     */
    public abstract String getScheme();


    public abstract FileSystem newFileSystem(URI uri, Map<String, ?> env)
            throws IOException;


    public abstract FileSystem getFileSystem(URI uri);


    public abstract Path getPath(URI uri);

    //构造一个新的 FileSystem以访问文件的内容作为文件系统。
    public FileSystem newFileSystem(Path path, Map<String, ?> env)
            throws IOException {
        throw new UnsupportedOperationException();
    }


    public InputStream newInputStream(Path path, OpenOption... options)
            throws IOException {
        if (options.length > 0) {
            for (OpenOption opt : options) {
                // All OpenOption values except for APPEND and WRITE are allowed
                if (opt == StandardOpenOption.APPEND ||
                        opt == StandardOpenOption.WRITE)
                    throw new UnsupportedOperationException("'" + opt + "' not allowed");
            }
        }
        return Channels.newInputStream(Files.newByteChannel(path, options));
    }


    public OutputStream newOutputStream(Path path, OpenOption... options)
            throws IOException {
        int len = options.length;
        Set<OpenOption> opts = new HashSet<OpenOption>(len + 3);
        if (len == 0) {
            opts.add(StandardOpenOption.CREATE);
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            for (OpenOption opt : options) {
                if (opt == StandardOpenOption.READ)
                    throw new IllegalArgumentException("READ not allowed");
                opts.add(opt);
            }
        }
        opts.add(StandardOpenOption.WRITE);
        return Channels.newOutputStream(newByteChannel(path, opts));
    }

    //创建文件通道
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
            throws IOException {
        throw new UnsupportedOperationException();
    }


    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
                                                              Set<? extends OpenOption> options,
                                                              ExecutorService executor,
                                                              FileAttribute<?>... attrs)
            throws IOException {
        throw new UnsupportedOperationException();
    }


    public abstract SeekableByteChannel newByteChannel(Path path,
                                                       Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;


    public abstract DirectoryStream<Path> newDirectoryStream(Path dir,
                                                             DirectoryStream.Filter<? super Path> filter) throws IOException;


    public abstract void createDirectory(Path dir, FileAttribute<?>... attrs)
            throws IOException;


    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs)
            throws IOException {
        throw new UnsupportedOperationException();
    }


    public void createLink(Path link, Path existing) throws IOException {
        throw new UnsupportedOperationException();
    }


    public abstract void delete(Path path) throws IOException;


    public boolean deleteIfExists(Path path) throws IOException {
        try {
            delete(path);
            return true;
        } catch (NoSuchFileException ignore) {
            return false;
        }
    }


    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException();
    }


    public abstract void copy(Path source, Path target, CopyOption... options)
            throws IOException;


    public abstract void move(Path source, Path target, CopyOption... options)
            throws IOException;


    public abstract boolean isSameFile(Path path, Path path2)
            throws IOException;


    public abstract boolean isHidden(Path path) throws IOException;


    public abstract FileStore getFileStore(Path path) throws IOException;


    public abstract void checkAccess(Path path, AccessMode... modes)
            throws IOException;


    public abstract <V extends FileAttributeView> V
    getFileAttributeView(Path path, Class<V> type, LinkOption... options);


    public abstract <A extends BasicFileAttributes> A
    readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException;


    public abstract Map<String, Object> readAttributes(Path path, String attributes,
                                                       LinkOption... options)
            throws IOException;


    public abstract void setAttribute(Path path, String attribute,
                                      Object value, LinkOption... options)
            throws IOException;
}
