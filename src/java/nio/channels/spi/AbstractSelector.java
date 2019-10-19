
package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

import sun.nio.ch.Interruptible;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 选择器的基本实现类。
 * 该类封装了实施选择操作中断所需的低级机器。 具体的选择器类必须分别调用begin和end方法，调用可能无限期阻塞的I / O操作。 为了确保总是调用end方法，这些方法应该在一个try ... finally块内使用：
 * <p>
 * try {
 * begin();
 * // Perform blocking I/O operation here
 * ...
 * } finally {
 * end();
 * }该类还定义了用于维护选择器的取消密钥集合并从其信道的密钥集中去除密钥的方法，并声明由可选择的信道的register方法调用的抽象register方法，以便执行注册信道的实际工作。
 */

public abstract class AbstractSelector
        extends Selector {

    private AtomicBoolean selectorOpen = new AtomicBoolean(true);

    // The provider that created this selector
    private final SelectorProvider provider;


    protected AbstractSelector(SelectorProvider provider) {
        this.provider = provider;
    }

    private final Set<SelectionKey> cancelledKeys = new HashSet<SelectionKey>();

    void cancel(SelectionKey k) {                       // package-private
        synchronized (cancelledKeys) {
            cancelledKeys.add(k);
        }
    }


    public final void close() throws IOException {
        boolean open = selectorOpen.getAndSet(false);
        if (!open)
            return;
        implCloseSelector();
    }


    protected abstract void implCloseSelector() throws IOException;

    public final boolean isOpen() {
        return selectorOpen.get();
    }


    public final SelectorProvider provider() {
        return provider;
    }


    protected final Set<SelectionKey> cancelledKeys() {
        return cancelledKeys;
    }


    protected abstract SelectionKey register(AbstractSelectableChannel ch,
                                             int ops, Object att);


    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel) key.channel()).removeKey(key);
    }


    // -- Interruption machinery --

    private Interruptible interruptor = null;


    protected final void begin() {
        if (interruptor == null) {
            interruptor = new Interruptible() {
                public void interrupt(Thread ignore) {
                    AbstractSelector.this.wakeup();
                }
            };
        }
        AbstractInterruptibleChannel.blockedOn(interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted())
            interruptor.interrupt(me);
    }


    protected final void end() {
        AbstractInterruptibleChannel.blockedOn(null);
    }

}
