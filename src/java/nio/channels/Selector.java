
package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;


/**
 * SelectableChannel对象的多路复用器 。
 * 可以通过调用此类的open方法来创建选择器，该方法将使用系统的默认值selector provider创建一个新的选择器。 还可以通过调用自定义选择器提供程序的openSelector方法来创建选择器。 选择器保持打开，直到通过其close方法关闭。
 * <p>
 * 选择器的可选频道注册由SelectionKey对象表示。 选择器保存三组选择键：
 * <p>
 * 密钥集包含表示此选择器当前通道注册的键。 该集合由keys方法返回。
 * <p>
 * 所选择的密钥集是一组密钥，使得每个密钥的信道被检测为在先前选择操作期间的密钥的兴趣集中识别的至少一个操作准备好。 该集合由selectedKeys方法返回。 所选密钥集始终是密钥集的子集。
 * <p>
 * 取消的密钥集是已经被取消但其通道尚未被注销的密钥的集合。 此集不能直接访问。 取消的密钥集始终是密钥集的子集。
 * <p>
 * 新创建的选择器中的所有三组都为空。
 * <p>
 * 通过频道的register方法，将一个键添加到选择器的按键集中，作为注册频道的副作用 。 在选择操作期间，从键集中删除取消的键。 密钥集本身不能直接修改。
 * <p>
 * 无论是通过关闭其通道还是调用其cancel方法，都会将其添加到其选择器的取消密钥集中。 取消键将导致其通道在下一次选择操作期间被取消注册，此时该键将从所有选择器的键集中移除。
 * <p>
 * 键通过选择操作被添加到所选择的键集中。 一个关键的可直接从通过调用该组的设定的选择键被移除remove方法或通过调用remove一个的方法iterator从集合获得的。 密钥永远不会以任何其他方式从所选密钥集中删除; 特别是作为选择操作的副作用而不被删除。 键可能不会直接添加到选定的键集。
 * <p>
 * 选择
 * 在每个选择操作期间，键可以被添加到选择器的选择键集合并从其中移除，并且可以从其键和取消的键集合中移除。 选择由执行select() ， select(long)和selectNow()方法，以及包括三个步骤：
 * <p>
 * 取消密钥集中的每个密钥从其作为其成员的每个密钥集中移除，并且其信道被注销。 此步骤将取消的密钥设置为空。
 * <p>
 * 查询底层操作系统，以便更新每个剩余通道的准备状态，以便在选择操作开始的时刻执行由其密钥的兴趣集确定的任何操作。 对于准备进行至少一个此类操作的通道，执行以下两个操作之一：
 * <p>
 * 如果频道的密钥尚未在选择的密钥集中，则将其添加到该集合中，并且其即时操作集被修改，以便准确地标识该信道现在被报告准备好的那些操作。 先前记录在就绪集中的任何就绪信息都被丢弃。
 * <p>
 * 否则，频道的密钥已经在选择密钥集中，因此其即时操作集被修改，以识别通道报告准备就绪的任何新操作。 以前记录在就绪集中的任何准备信息都将被保留; 换句话说，由底层系统返回的准备集被按位分离到键的当前准备集中。
 * <p>
 * 如果在此步骤开始时设置的密钥中的所有密钥都具有空的兴趣集，那么所选密钥集和任何密钥的即用操作集都将不被更新。
 * 如果在步骤（2）进行中将任何键添加到取消的键集合，则它们将如步骤（1）中那样处理。
 * <p>
 * 选择操作是否阻止等待一个或多个通道准备就绪，如果是这样，多长时间是三种选择方法之间唯一的本质区别。
 * <p>
 * 并发性
 * 选择器本身可以安全地被多个并发线程使用; 不过，他们的钥匙不是。
 * <p>
 * 选择操作在选择器本身，按键集合和所选键集上以该顺序同步。 它们还在上述步骤（1）和（3）中的取消键集上进行同步。
 * <p>
 * 在选择操作正在进行时，对选择器的键的兴趣集进行的更改对该操作没有影响; 他们将在下一个选择操作中看到。
 * <p>
 * 钥匙可能会被取消，渠道可能随时关闭。 因此，在一个或多个选择器的密钥组中存在密钥并不意味着密钥是有效的或其信道是打开的。 如果有可能另一个线程将取消键或关闭通道，则应用代码应小心同步并根据需要检查这些条件。
 * <p>
 * select()或select(long)方法之一中阻塞的线程可能会被其他线程以三种方式之一中断：
 * <p>
 * 通过调用选择器的wakeup方法，
 * <p>
 * 通过调用选择器的close方法，或
 * <p>
 * 通过调用阻塞线程的interrupt方法，在这种情况下，它的中断状态将被设置，并且将调用选择器的wakeup方法。
 * <p>
 * close方法以与选择操作相同的顺序在选择器和所有三个按键组上同步。
 * <p>
 * 选择器的密钥和选择密钥集合通常不会被多个并发线程安全使用。 如果这样的线程可以直接修改这些集合中的一个，则应该通过在集合本身上进行同步来控制访问。 由这些集合iterator方法返回的迭代器是故障快速的：如果在迭代器创建之后修改集合，除了通过调用迭代器自己的remove方法之外，以任何方式都将被修改，那么将抛出一个ConcurrentModificationException 。
 */

public abstract class Selector implements Closeable {

    /**
     * Initializes a new instance of this class.
     */
    protected Selector() {
    }

    //打开选择器。
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    //告诉这个选择器是否打开。
    public abstract boolean isOpen();

    //返回创建此通道的提供程序。
    public abstract SelectorProvider provider();

    //返回此选择器的键集。
    public abstract Set<SelectionKey> keys();

    //选择一组其相应通道准备好进行I / O操作的键。
    public abstract Set<SelectionKey> selectedKeys();

    //选择一组其相应通道准备好进行I / O操作的键。
    public abstract int selectNow() throws IOException;

    //选择一组其相应通道准备好进行I / O操作的键。
    public abstract int select(long timeout)
            throws IOException;

    //选择一组其相应通道准备好进行I / O操作的键。
    public abstract int select() throws IOException;

    //导致尚未返回的第一个选择操作立即返回。
    public abstract Selector wakeup();

    //关闭此选择器。
    public abstract void close() throws IOException;

}
