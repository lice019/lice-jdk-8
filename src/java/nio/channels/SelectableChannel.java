
package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;


/**
 * 可通过Selector复用的通道 。
 * 为了与一个选择器被使用，这个类的一个实例必须首先经由注册 register方法。 此方法返回一个新的SelectionKey对象，表示通道与选择器的注册。
 * <p>
 * 一旦注册了选择器，通道保持注册，直到它被注销 。 这包括通过选择器取消分配给频道的任何资源。
 * <p>
 * 渠道不能直接注销; 而是代表其注册的密钥必须被取消 。 取消密钥请求在选择器的下一个选择操作期间注销通道。 可以通过调用其cancel方法来明确地取消密钥。 无论通过调用其close方法还是通过中断通道上的I / O操作中阻塞的线程，通道关闭时，所有通道的密钥都将被隐式取消。
 * <p>
 * 如果选择器本身被关闭，那么该通道将被注销，并且表示其注册的密钥将被无效，而不再延迟。
 * <p>
 * 任何特定选择器最多可以注册一个通道。
 * <p>
 * 可以通过调用isRegistered方法来确定信道是否被注册到一个或多个选择器。
 * <p>
 * 可选择的通道可安全使用多个并发线程。
 * <p>
 * 阻塞模式
 * 可选通道处于阻塞模式或非阻塞模式。 在阻塞模式下，通道上调用的每个I / O操作将阻塞直到完成。 在非阻塞模式下，I / O操作将永远不会阻塞，并且可能会传输比所请求的字节更少的字节，或者根本没有字节。 可选通道的阻塞模式可以通过调用其isBlocking方法来确定 。
 * 新创建的可选通道始终处于阻止模式。 非阻塞模式与基于选择器的复用最为有用。 在注册选择器之前，必须将通道置于非阻塞模式，并且在注销之前可能不会返回到阻止模式。
 */

public abstract class SelectableChannel
        extends AbstractInterruptibleChannel
        implements Channel {

    //初始化一个可以多路复用的通道
    protected SelectableChannel() {
    }

    //获取一个多路选择权的提供者
    public abstract SelectorProvider provider();


    public abstract int validOps();

    // Internal state:
    //   keySet, may be empty but is never null, typ. a tiny array
    //   boolean isRegistered, protected by key set
    //   regLock, lock object to prevent duplicate registrations
    //   boolean isBlocking, protected by regLock


    public abstract boolean isRegistered();
    //
    // sync(keySet) { return isRegistered; }


    public abstract SelectionKey keyFor(Selector sel);
    //
    // sync(keySet) { return findKey(sel); }

    //使用给定的选择器注册此频道，返回一个选择键。
    public abstract SelectionKey register(Selector sel, int ops, Object att)
            throws ClosedChannelException;
    //
    // sync(regLock) {
    //   sync(keySet) { look for selector }
    //   if (channel found) { set interest ops -- may block in selector;
    //                        return key; }
    //   create new key -- may block somewhere in selector;
    //   sync(keySet) { add key; }
    //   attach(attachment);
    //   return key;
    // }

    //使用给定的选择器注册此频道，返回一个选择键。
    public final SelectionKey register(Selector sel, int ops)
            throws ClosedChannelException {
        return register(sel, ops, null);
    }

    //调整此频道的屏蔽模式。 切换阻塞模式
    public abstract SelectableChannel configureBlocking(boolean block)
            throws IOException;
    //
    // sync(regLock) {
    //   sync(keySet) { throw IBME if block && isRegistered; }
    //   change mode;
    // }


    public abstract boolean isBlocking();


    public abstract Object blockingLock();

}
