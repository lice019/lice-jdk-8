package java.nio;

import java.util.Spliterator;

/**
 * 特定基本类型数据的容器。
 * 缓冲器是特定原始类型的元素的线性有限序列。 除了其内容，缓冲区的基本属性是其容量，限制和位置。
 * Buffer：
 * (1)、只读缓冲区：
 * 每个缓冲区都是可读的，但并不是每个缓冲区都是可写的。 每个缓冲区类的变异方法被指定为可选操作 ，当在只读缓冲区上调用时，它将抛出一个ReadOnlyBufferException 。 只读缓冲区不允许更改其内容，但其标记，位置和限制值是可变的。 缓冲区是否为只读可以通过调用其isReadOnly方法来确定 。
 * (2)、线程安全
 * 缓冲区不能安全地被多个并发线程使用。 如果一个缓冲区被多个线程使用，则应该通过适当的同步来控制对缓冲区的访问。
 * 特有方法：
 * 1、clear()使缓冲区准备好信道读取或相对放置操作的一个新的序列：它设置了限制的能力和位置为零。
 * 2、flip()使缓冲区准备好新的通道写入或相对获取操作序列：它将限制设置为当前位置，然后将位置设置为零。
 * 3、rewind()使缓冲区准备好重新读取已经包含的数据：它保持限制不变，并将位置设置为零。
 * <p>
 * 注意：Buffer是一个抽象类，用于模板的使用。java中的8种数据类型都有相应的Buffer，用于相应的数据了类型缓冲区
 *
 */

//在NIO中有三个核心的组件：Channels（管道）、Buffers（容器）、Selectors（Selector允许单线程处理多个 Channel。）
//在NIO中管道运输数据的容器就是缓冲区Buffer，Selector主要用于单线程处理Channels管道，从而解决单线程也可以解决高并发
public abstract class Buffer {

    /**
     * 在缓冲区中遍历和拆分元素的spliterator的特性。
     */
    static final int SPLITERATOR_CHARACTERISTICS =
            Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;

    // Invariants: 0<=mark <= position <= limit <= capacity
    /**
     * mark：位置的标记
     * position：buffer当前所在的操作位置
     * limit：buffer最大的操作位置
     * capacity：buffer的最大长度
     */
    private int mark = -1; //位置标记，重置position
    private int position = 0;
    private int limit;
    private int capacity;

    //仅用于直接缓冲区
    //注:在JNI GetDirectBufferAddress中，此处悬挂速度
    long address;

    //Buffer的构造函数，java中抽象不能被new实例化，提供给子类supper来初始化
    Buffer(int mark, int pos, int lim, int cap) {       // package-private
        if (cap < 0)
            throw new IllegalArgumentException("Negative capacity: " + cap);
        //设置Buffer的容量大小
        this.capacity = cap;
        //设置此缓冲区的限制
        limit(lim);
        //设置这个缓冲区的位置。
        position(pos);
        if (mark >= 0) {
            if (mark > pos)
                throw new IllegalArgumentException("mark > position: ("
                        + mark + " > " + pos + ")");
            this.mark = mark;
        }
    }


    public final int capacity() {
        return capacity;
    }


    public final int position() {
        return position;
    }

    //设置这个缓冲区的位置。
    public final Buffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
        if (mark > position) mark = -1;
        return this;
    }


    public final int limit() {
        return limit;
    }

    //设置此缓冲区的限制
    public final Buffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        //将limit设置为形参的，如果limit的大于capacity或小于0，则抛出异常
        limit = newLimit;
        //如果此时的position大于limit，则position为limit
        if (position > limit) position = limit;
        if (mark > limit) mark = -1;
        return this;
    }

    //Mark是位置的标记，为当前操作位置的下标
    public final Buffer mark() {
        mark = position;
        return this;
    }

    //将此缓冲区的位置重置为先前标记的位置。
    public final Buffer reset() {
        int m = mark;
        if (m < 0)
            throw new InvalidMarkException();
        position = m;
        return this;
    }

    //清空缓存区，实际上是将position、limit、mark设置为原来的初始值，缓冲区中的数据是不清除的
    public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    //翻转模式，如果是读模式，则转成写模式；如果是写模式，则转成读模式
    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    //Buffer.rewind()方法将position置为0，这样我们可以重复读取buffer中的数据。limit保持不变。
    public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    //返回当前位置和限制之间的元素数。
    public final int remaining() {
        return limit - position;
    }


    public final boolean hasRemaining() {
        return position < limit;
    }


    public abstract boolean isReadOnly();


    public abstract boolean hasArray();


    public abstract Object array();


    public abstract int arrayOffset();


    public abstract boolean isDirect();


    // -- Package-private methods for bounds checking, etc. --


    final int nextGetIndex() {                          // package-private
        if (position >= limit)
            throw new BufferUnderflowException();
        return position++;
    }

    final int nextGetIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferUnderflowException();
        int p = position;
        position += nb;
        return p;
    }


    final int nextPutIndex() {                          // package-private
        if (position >= limit)
            throw new BufferOverflowException();
        return position++;
    }

    final int nextPutIndex(int nb) {                    // package-private
        if (limit - position < nb)
            throw new BufferOverflowException();
        int p = position;
        position += nb;
        return p;
    }

    //检查下标是否越界
    final int checkIndex(int i) {                       // package-private
        if ((i < 0) || (i >= limit))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int checkIndex(int i, int nb) {               // package-private
        if ((i < 0) || (nb > limit - i))
            throw new IndexOutOfBoundsException();
        return i;
    }

    final int markValue() {                             // package-private
        return mark;
    }

    final void truncate() {                             // package-private
        mark = -1;
        position = 0;
        limit = 0;
        capacity = 0;
    }

    final void discardMark() {                          // package-private
        mark = -1;
    }

    static void checkBounds(int off, int len, int size) { // package-private
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }

}
