
package java.nio.channels;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;


/**
 * 用于读取，写入，映射和操作文件的通道。
 * 文件通道可以安全使用多个并发线程。 close方法可以随时调用，由Channel接口指定。
 */
public abstract class FileChannel extends AbstractInterruptibleChannel
        implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {


    protected FileChannel() {
    }

    //打开或创建文件，返回文件通道以访问该文件。
    public static FileChannel open(Path path,
                                   Set<? extends OpenOption> options,
                                   FileAttribute<?>... attrs)
            throws IOException {
        FileSystemProvider provider = path.getFileSystem().provider();
        return provider.newFileChannel(path, options, attrs);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // generic array construction
    private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

    //打开或创建文件，返回文件通道以访问该文件。
    public static FileChannel open(Path path, OpenOption... options)
            throws IOException {
        Set<OpenOption> set = new HashSet<OpenOption>(options.length);
        Collections.addAll(set, options);
        return open(path, set, NO_ATTRIBUTES);
    }

    // -- Channel operations --

    //从该通道读取数据到给定缓冲区容器的字节序列。
    public abstract int read(ByteBuffer dst) throws IOException;


    public abstract long read(ByteBuffer[] dsts, int offset, int length)
            throws IOException;


    public final long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    //将缓冲区的数据写入到通道中
    public abstract int write(ByteBuffer src) throws IOException;


    public abstract long write(ByteBuffer[] srcs, int offset, int length)
            throws IOException;


    public final long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }


    // -- Other operations --

    //返回此频道的文件位置。
    public abstract long position() throws IOException;

    //设置此通道的文件位置。
    public abstract FileChannel position(long newPosition) throws IOException;

    //返回此通道文件的当前大小。
    public abstract long size() throws IOException;

    //将此频道的文件截断为给定大小。
    public abstract FileChannel truncate(long size) throws IOException;

    //强制将此通道文件的任何更新写入包含该通道的存储设备。
    public abstract void force(boolean metaData) throws IOException;


    public abstract long transferTo(long position, long count,
                                    WritableByteChannel target)
            throws IOException;


    public abstract long transferFrom(ReadableByteChannel src,
                                      long position, long count)
            throws IOException;


    public abstract int read(ByteBuffer dst, long position) throws IOException;


    public abstract int write(ByteBuffer src, long position) throws IOException;


    // -- Memory-mapped buffers --

    /**
     * A typesafe enumeration for file-mapping modes.
     *
     * @see java.nio.channels.FileChannel#map
     * @since 1.4
     */
    public static class MapMode {

        /**
         * Mode for a read-only mapping.
         */
        public static final MapMode READ_ONLY
                = new MapMode("READ_ONLY");

        /**
         * Mode for a read/write mapping.
         */
        public static final MapMode READ_WRITE
                = new MapMode("READ_WRITE");

        /**
         * Mode for a private (copy-on-write) mapping.
         */
        public static final MapMode PRIVATE
                = new MapMode("PRIVATE");

        private final String name;

        private MapMode(String name) {
            this.name = name;
        }

        /**
         * Returns a string describing this file-mapping mode.
         *
         * @return A descriptive string
         */
        public String toString() {
            return name;
        }

    }

    //将此频道文件的区域直接映射到内存中。
    public abstract MappedByteBuffer map(MapMode mode,
                                         long position, long size)
            throws IOException;


    // -- Locks --


    public abstract FileLock lock(long position, long size, boolean shared)
            throws IOException;


    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }


    public abstract FileLock tryLock(long position, long size, boolean shared)
            throws IOException;


    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

}
