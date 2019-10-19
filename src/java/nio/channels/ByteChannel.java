
package java.nio.channels;

/**
 * 一个可以读写字节的通道。
 * 这个界面简单地统一了ReadableByteChannel和WritableByteChannel ; 它没有指定任何新的操作。
 */

public interface ByteChannel
        extends ReadableByteChannel, WritableByteChannel {

}
