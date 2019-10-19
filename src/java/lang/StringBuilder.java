package java.lang;

/**
 * StringBuilder:一个可变的字符序列。 此类提供与StringBuffer的API，但不保证同步。同样继承AbstractStringBuilder。
 * 底层的容器为父类AbstractStringBuilder的char[] value;可变的数组对象。
 * StringBuilder提供了StringBuffer的所有方法API，线程不安全，由于线程不安全所以效率比StringBuffer高。
 * StringBuilder使用了final修饰不可被继承
 *
 */
public final class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, CharSequence {

    //反序列化的id
    static final long serialVersionUID = 4383685877147921099L;

    //初始化一个StringBuilder的对象，默认底层容器的容量为16
    public StringBuilder() {
        super(16);
    }

    //初始化一个StringBuilder的对象，底层容器的容量自定义
    public StringBuilder(int capacity) {
        super(capacity);
    }


    public StringBuilder(String str) {
        super(str.length() + 16);
        append(str);
    }


    public StringBuilder(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    @Override
    public StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    @Override
    public StringBuilder append(String str) {
        super.append(str);
        return this;
    }


    public StringBuilder append(StringBuffer sb) {
        super.append(sb);
        return this;
    }

    @Override
    public StringBuilder append(CharSequence s) {
        super.append(s);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder append(CharSequence s, int start, int end) {
        super.append(s, start, end);
        return this;
    }

    @Override
    public StringBuilder append(char[] str) {
        super.append(str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder append(char[] str, int offset, int len) {
        super.append(str, offset, len);
        return this;
    }

    @Override
    public StringBuilder append(boolean b) {
        super.append(b);
        return this;
    }

    @Override
    public StringBuilder append(char c) {
        super.append(c);
        return this;
    }

    @Override
    public StringBuilder append(int i) {
        super.append(i);
        return this;
    }

    @Override
    public StringBuilder append(long lng) {
        super.append(lng);
        return this;
    }

    @Override
    public StringBuilder append(float f) {
        super.append(f);
        return this;
    }

    @Override
    public StringBuilder append(double d) {
        super.append(d);
        return this;
    }

    /**
     * @since 1.5
     */
    @Override
    public StringBuilder appendCodePoint(int codePoint) {
        super.appendCodePoint(codePoint);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder delete(int start, int end) {
        super.delete(start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder deleteCharAt(int index) {
        super.deleteCharAt(index);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder replace(int start, int end, String str) {
        super.replace(start, end, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int index, char[] str, int offset,
                                int len) {
        super.insert(index, str, offset, len);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, Object obj) {
        super.insert(offset, obj);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, String str) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, char[] str) {
        super.insert(offset, str);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int dstOffset, CharSequence s) {
        super.insert(dstOffset, s);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int dstOffset, CharSequence s,
                                int start, int end) {
        super.insert(dstOffset, s, start, end);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, boolean b) {
        super.insert(offset, b);
        return this;
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, char c) {
        super.insert(offset, c);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, int i) {
        super.insert(offset, i);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, long l) {
        super.insert(offset, l);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, float f) {
        super.insert(offset, f);
        return this;
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public StringBuilder insert(int offset, double d) {
        super.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(String str) {
        return super.indexOf(str);
    }

    @Override
    public int indexOf(String str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        return super.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    @Override
    public StringBuilder reverse() {
        super.reverse();
        return this;
    }

    @Override
    public String toString() {
        // Create a copy, don't share the array
        return new String(value, 0, count);
    }

    //-----以下方法用于StringBuilder对象的序列化和反序列化


    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(count);
        s.writeObject(value);
    }

    /**
     * readObject is called to restore the state of the StringBuffer from
     * a stream.
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        count = s.readInt();
        value = (char[]) s.readObject();
    }

}
