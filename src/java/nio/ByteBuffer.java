package java.nio;


/**
 * 一个字节缓冲区。
 * 这个类在字节缓冲区中定义了六类操作：
 * 绝对和相对get和put读写单字节的方法;
 * 相对bulk get方法将连续的字节序列从该缓冲区传输到数组中;
 * 相对bulk put方法，将字节数组或其他字节缓冲区的连续字节序列传输到此缓冲区中;
 * 绝对和相对get和put方法，读取和写入其他原始类型的值，将它们转换为特定字节顺序的字节序列;
 * 用于创建view buffers的方法，其允许将字节缓冲器视为包含某些其他原始类型的值的缓冲器; 和
 * 方法compacting ， duplicating和slicing一个字节的缓冲区。
 * 字节缓冲区可以由allocation创建，它为缓冲区的内容分配空间，或者通过wrapping将现有的字节数组分配到缓冲区中。
 * <p>
 * 直接与非直接缓冲区
 * (1)、非直接缓冲区：
 * 创建的缓冲区，在JVM中内存中创建，在每次调用基础操作系统的一个本机IO之前或者之后，虚拟机都会将缓冲区的内容复制到中间缓冲区（或者从中间缓冲区复制内容），缓冲区的内容驻留在JVM内，因此销毁容易，但是占用JVM内存开销，处理过程中有复制操作。
 * (2)、直接缓冲区：
 * 创建的缓冲区，在JVM内存外开辟内存，在每次调用基础操作系统的一个本机IO之前或者之后，虚拟机都会避免将缓冲区的内容复制到中间缓冲区（或者从中间缓冲区复制内容），缓冲区的内容驻留在物理内存内，会少一次复制过程，如果需要循环使用缓冲区，用直接缓冲区可以很大地提高性能。
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {

    // 为了减少访问这些值所需的虚拟方法调用的数量，这里声明了这些字段，
    // 而不是在堆- x - buffer中声明这些字段，这在编写小缓冲区时特别昂贵。

    //非空仅用于堆缓冲区，字节数组容器
    final byte[] hb;
    final int offset;
    //是否为仅读，只对堆缓冲区有效
    boolean isReadOnly;

    //ByteBuffer为抽象类，不能使用new来创建，只需使用时，使用静态方法来分配空间即可
    ByteBuffer(int mark, int pos, int lim, int cap,   // package-private
               byte[] hb, int offset) {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    ByteBuffer(int mark, int pos, int lim, int cap) { // package-private
        this(mark, pos, lim, cap, null, 0);
    }


    //分配直接缓冲区，通过映射直接操作物理内存
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }


    //分配非直接缓冲区，通过将物理内存中数据读取到jvm的内存中，再进行操作
    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }

    //将一个字节数组包装到缓冲区中。
    public static ByteBuffer wrap(byte[] array,
                                  int offset, int length) {
        try {
            return new HeapByteBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    //将一个字节数组包装到缓冲区中。
    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    //由子类HeapByteBuffer来实现
    //创建一个新的字节缓冲区，其内容是此缓冲区内容的共享子序列。
    public abstract ByteBuffer slice();

    //
    public abstract ByteBuffer duplicate();


    public abstract ByteBuffer asReadOnlyBuffer();


    // -- Singleton get/put methods --  单例的get获取和put添加数据到缓冲区的方法


    public abstract byte get();


    public abstract ByteBuffer put(byte b);

    //根据下标index来获取字节数据
    public abstract byte get(int index);


    //根据数组下标来添加字节数据
    public abstract ByteBuffer put(int index, byte b);


    // -- Bulk get operations -- 块级的get操作

    //将参数dst的字节数组中从offset的下标开始，添加length长度的字节到缓存区中
    public ByteBuffer get(byte[] dst, int offset, int length) {
        //将offset下标和dst的长度是否越界
        //父类方法
        checkBounds(offset, length, dst.length);
        //获取的字节数不能大于当前位置和最大限制的差
        if (length > remaining())
            throw new BufferUnderflowException();
        //得出最后元素的下标
        int end = offset + length;
        //
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }


    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }


    // -- Bulk put operations -- 块级的put方法

    //将一个缓存区数据添加到该缓存区中
    public ByteBuffer put(ByteBuffer src) {
        if (src == this)
            throw new IllegalArgumentException();
        if (isReadOnly())
            throw new ReadOnlyBufferException();
        int n = src.remaining();
        if (n > remaining())
            throw new BufferOverflowException();
        for (int i = 0; i < n; i++)
            put(src.get());
        return this;
    }


    public ByteBuffer put(byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            this.put(src[i]);
        return this;
    }


    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }


    // -- Other stuff --


    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }


    public final byte[] array() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return hb;
    }


    public final int arrayOffset() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return offset;
    }


    public abstract ByteBuffer compact();


    public abstract boolean isDirect();


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }


    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--)


            h = 31 * h + (int) get(i);

        return h;
    }


    public boolean equals(Object ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof ByteBuffer))
            return false;
        ByteBuffer that = (ByteBuffer) ob;
        if (this.remaining() != that.remaining())
            return false;
        int p = this.position();
        for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--)
            if (!equals(this.get(i), that.get(j)))
                return false;
        return true;
    }

    private static boolean equals(byte x, byte y) {


        return x == y;

    }


    public int compareTo(ByteBuffer that) {
        int n = this.position() + Math.min(this.remaining(), that.remaining());
        for (int i = this.position(), j = that.position(); i < n; i++, j++) {
            int cmp = compare(this.get(i), that.get(j));
            if (cmp != 0)
                return cmp;
        }
        return this.remaining() - that.remaining();
    }

    private static int compare(byte x, byte y) {


        return Byte.compare(x, y);

    }

    // -- Other char stuff --


    // -- Other byte stuff: Access to binary data --


    boolean bigEndian                                   // package-private
            = true;
    boolean nativeByteOrder                             // package-private
            = (Bits.byteOrder() == ByteOrder.BIG_ENDIAN);


    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }


    public final ByteBuffer order(ByteOrder bo) {
        bigEndian = (bo == ByteOrder.BIG_ENDIAN);
        nativeByteOrder =
                (bigEndian == (Bits.byteOrder() == ByteOrder.BIG_ENDIAN));
        return this;
    }

    // Unchecked accessors, for use by ByteBufferAs-X-Buffer classes
    //
    abstract byte _get(int i);                          // package-private

    abstract void _put(int i, byte b);                  // package-private


    public abstract char getChar();


    public abstract ByteBuffer putChar(char value);


    public abstract char getChar(int index);


    public abstract ByteBuffer putChar(int index, char value);


    public abstract CharBuffer asCharBuffer();


    public abstract short getShort();


    public abstract ByteBuffer putShort(short value);


    public abstract short getShort(int index);


    public abstract ByteBuffer putShort(int index, short value);


    public abstract ShortBuffer asShortBuffer();


    public abstract int getInt();


    public abstract ByteBuffer putInt(int value);


    public abstract int getInt(int index);


    public abstract ByteBuffer putInt(int index, int value);


    public abstract IntBuffer asIntBuffer();


    public abstract long getLong();


    public abstract ByteBuffer putLong(long value);


    public abstract long getLong(int index);


    public abstract ByteBuffer putLong(int index, long value);


    public abstract LongBuffer asLongBuffer();


    public abstract float getFloat();


    public abstract ByteBuffer putFloat(float value);


    public abstract float getFloat(int index);

    public abstract ByteBuffer putFloat(int index, float value);


    public abstract FloatBuffer asFloatBuffer();


    public abstract double getDouble();


    public abstract ByteBuffer putDouble(double value);


    public abstract double getDouble(int index);


    public abstract ByteBuffer putDouble(int index, double value);


    public abstract DoubleBuffer asDoubleBuffer();

}
