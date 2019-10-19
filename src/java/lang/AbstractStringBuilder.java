package java.lang;

import sun.misc.FloatingDecimal;

import java.util.Arrays;

/**
 * AbstractStringBuilder：jdk1.5的，底层的容器也是使用char []，StringBuffer和StringBuilder继承了它。
 * AbstractStringBuilder为什么为可变的？
 * 因为StringBuffer和StringBuilder的底层容量是char[] value;不是常量的
 */
abstract class AbstractStringBuilder implements Appendable, CharSequence {

    //存储字符的容器
    char[] value;

    //value中的字符数
    int count;

    //这个无参数构造函数是序列化子类所必需的。
    AbstractStringBuilder() {
    }

    //指定大小的AbstractStringBuilder实例初始化
    AbstractStringBuilder(int capacity) {
        value = new char[capacity];
    }

    //value容器的长度
    @Override
    public int length() {
        return count;
    }

    //返回字符数组的容量
    public int capacity() {
        return value.length;
    }

    //确保容器容量
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > 0)
            ensureCapacityInternal(minimumCapacity);
    }

    //确保String内部的容量
    private void ensureCapacityInternal(int minimumCapacity) {
        // overflow-conscious code
        if (minimumCapacity - value.length > 0) {
            value = Arrays.copyOf(value,
                    newCapacity(minimumCapacity));
        }
    }

    //char [] value的最大容器
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    //返回新的容量
    private int newCapacity(int minCapacity) {
        // overflow-conscious code
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0)
                ? hugeCapacity(minCapacity)
                : newCapacity;
    }

    private int hugeCapacity(int minCapacity) {
        if (Integer.MAX_VALUE - minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE)
                ? minCapacity : MAX_ARRAY_SIZE;
    }

    //去掉空格
    public void trimToSize() {
        if (count < value.length) {
            value = Arrays.copyOf(value, count);
        }
    }

    //设置长度
    public void setLength(int newLength) {
        if (newLength < 0)
            throw new StringIndexOutOfBoundsException(newLength);
        ensureCapacityInternal(newLength);

        if (count < newLength) {
            Arrays.fill(value, count, newLength, '\0');
        }

        count = newLength;
    }

    //返回index下标的字符
    @Override
    public char charAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        return value[index];
    }


    public int codePointAt(int index) {
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointAtImpl(value, index, count);
    }


    public int codePointBefore(int index) {
        int i = index - 1;
        if ((i < 0) || (i >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return Character.codePointBeforeImpl(value, index, 0);
    }


    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > count || beginIndex > endIndex) {
            throw new IndexOutOfBoundsException();
        }
        return Character.codePointCountImpl(value, beginIndex, endIndex - beginIndex);
    }


    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException();
        }
        return Character.offsetByCodePointsImpl(value, 0, count,
                index, codePointOffset);
    }


    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (srcBegin < 0)
            throw new StringIndexOutOfBoundsException(srcBegin);
        if ((srcEnd < 0) || (srcEnd > count))
            throw new StringIndexOutOfBoundsException(srcEnd);
        if (srcBegin > srcEnd)
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }


    public void setCharAt(int index, char ch) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        value[index] = ch;
    }

    //在StringBuffer的末尾拼接字符串
    public AbstractStringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }


    public AbstractStringBuilder append(String str) {
        if (str == null)
            return appendNull();
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

    // Documentation in subclasses because of synchro difference
    public AbstractStringBuilder append(StringBuffer sb) {
        if (sb == null)
            return appendNull();
        int len = sb.length();
        ensureCapacityInternal(count + len);
        sb.getChars(0, len, value, count);
        count += len;
        return this;
    }

    /**
     * @since 1.8
     */
    AbstractStringBuilder append(AbstractStringBuilder asb) {
        if (asb == null)
            return appendNull();
        int len = asb.length();
        ensureCapacityInternal(count + len);
        asb.getChars(0, len, value, count);
        count += len;
        return this;
    }

    // Documentation in subclasses because of synchro difference
    @Override
    public AbstractStringBuilder append(CharSequence s) {
        if (s == null)
            return appendNull();
        if (s instanceof String)
            return this.append((String) s);
        if (s instanceof AbstractStringBuilder)
            return this.append((AbstractStringBuilder) s);

        return this.append(s, 0, s.length());
    }

    private AbstractStringBuilder appendNull() {
        int c = count;
        ensureCapacityInternal(c + 4);
        final char[] value = this.value;
        value[c++] = 'n';
        value[c++] = 'u';
        value[c++] = 'l';
        value[c++] = 'l';
        count = c;
        return this;
    }


    @Override
    public AbstractStringBuilder append(CharSequence s, int start, int end) {
        if (s == null)
            s = "null";
        if ((start < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", s.length() "
                            + s.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++)
            value[j] = s.charAt(i);
        count += len;
        return this;
    }


    public AbstractStringBuilder append(char[] str) {
        int len = str.length;
        ensureCapacityInternal(count + len);
        System.arraycopy(str, 0, value, count, len);
        count += len;
        return this;
    }


    public AbstractStringBuilder append(char str[], int offset, int len) {
        if (len > 0)                // let arraycopy report AIOOBE for len < 0
            ensureCapacityInternal(count + len);
        System.arraycopy(str, offset, value, count, len);
        count += len;
        return this;
    }


    public AbstractStringBuilder append(boolean b) {
        if (b) {
            ensureCapacityInternal(count + 4);
            value[count++] = 't';
            value[count++] = 'r';
            value[count++] = 'u';
            value[count++] = 'e';
        } else {
            ensureCapacityInternal(count + 5);
            value[count++] = 'f';
            value[count++] = 'a';
            value[count++] = 'l';
            value[count++] = 's';
            value[count++] = 'e';
        }
        return this;
    }


    @Override
    public AbstractStringBuilder append(char c) {
        ensureCapacityInternal(count + 1);
        value[count++] = c;
        return this;
    }


    public AbstractStringBuilder append(int i) {
        if (i == Integer.MIN_VALUE) {
            append("-2147483648");
            return this;
        }
        int appendedLength = (i < 0) ? Integer.stringSize(-i) + 1
                : Integer.stringSize(i);
        int spaceNeeded = count + appendedLength;
        ensureCapacityInternal(spaceNeeded);
        Integer.getChars(i, spaceNeeded, value);
        count = spaceNeeded;
        return this;
    }


    public AbstractStringBuilder append(long l) {
        if (l == Long.MIN_VALUE) {
            append("-9223372036854775808");
            return this;
        }
        int appendedLength = (l < 0) ? Long.stringSize(-l) + 1
                : Long.stringSize(l);
        int spaceNeeded = count + appendedLength;
        ensureCapacityInternal(spaceNeeded);
        Long.getChars(l, spaceNeeded, value);
        count = spaceNeeded;
        return this;
    }


    public AbstractStringBuilder append(float f) {
        FloatingDecimal.appendTo(f, this);
        return this;
    }


    public AbstractStringBuilder append(double d) {
        FloatingDecimal.appendTo(d, this);
        return this;
    }


    public AbstractStringBuilder delete(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            end = count;
        if (start > end)
            throw new StringIndexOutOfBoundsException();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, count - end);
            count -= len;
        }
        return this;
    }


    public AbstractStringBuilder appendCodePoint(int codePoint) {
        final int count = this.count;

        if (Character.isBmpCodePoint(codePoint)) {
            ensureCapacityInternal(count + 1);
            value[count] = (char) codePoint;
            this.count = count + 1;
        } else if (Character.isValidCodePoint(codePoint)) {
            ensureCapacityInternal(count + 2);
            Character.toSurrogates(codePoint, value, count);
            this.count = count + 2;
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    //移除下标index的字符
    public AbstractStringBuilder deleteCharAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        System.arraycopy(value, index + 1, value, index, count - index - 1);
        count--;
        return this;
    }

    //替代字符
    public AbstractStringBuilder replace(int start, int end, String str) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (start > count)
            throw new StringIndexOutOfBoundsException("start > length()");
        if (start > end)
            throw new StringIndexOutOfBoundsException("start > end");

        if (end > count)
            end = count;
        int len = str.length();
        int newCount = count + len - (end - start);
        ensureCapacityInternal(newCount);

        System.arraycopy(value, end, value, start + len, count - end);
        str.getChars(value, start);
        count = newCount;
        return this;
    }


    public String substring(int start) {
        return substring(start, count);
    }


    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    //获取子串
    public String substring(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            throw new StringIndexOutOfBoundsException(end);
        if (start > end)
            throw new StringIndexOutOfBoundsException(end - start);
        return new String(value, start, end - start);
    }


    public AbstractStringBuilder insert(int index, char[] str, int offset,
                                        int len) {
        if ((index < 0) || (index > length()))
            throw new StringIndexOutOfBoundsException(index);
        if ((offset < 0) || (len < 0) || (offset > str.length - len))
            throw new StringIndexOutOfBoundsException(
                    "offset " + offset + ", len " + len + ", str.length "
                            + str.length);
        ensureCapacityInternal(count + len);
        System.arraycopy(value, index, value, index + len, count - index);
        System.arraycopy(str, offset, value, index, len);
        count += len;
        return this;
    }


    public AbstractStringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }


    public AbstractStringBuilder insert(int offset, String str) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        if (str == null)
            str = "null";
        int len = str.length();
        ensureCapacityInternal(count + len);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        str.getChars(value, offset);
        count += len;
        return this;
    }


    public AbstractStringBuilder insert(int offset, char[] str) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        int len = str.length;
        ensureCapacityInternal(count + len);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        System.arraycopy(str, 0, value, offset, len);
        count += len;
        return this;
    }


    public AbstractStringBuilder insert(int dstOffset, CharSequence s) {
        if (s == null)
            s = "null";
        if (s instanceof String)
            return this.insert(dstOffset, (String) s);
        return this.insert(dstOffset, s, 0, s.length());
    }


    public AbstractStringBuilder insert(int dstOffset, CharSequence s,
                                        int start, int end) {
        if (s == null)
            s = "null";
        if ((dstOffset < 0) || (dstOffset > this.length()))
            throw new IndexOutOfBoundsException("dstOffset " + dstOffset);
        if ((start < 0) || (end < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", s.length() "
                            + s.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        System.arraycopy(value, dstOffset, value, dstOffset + len,
                count - dstOffset);
        for (int i = start; i < end; i++)
            value[dstOffset++] = s.charAt(i);
        count += len;
        return this;
    }


    public AbstractStringBuilder insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    //在offset后插入c字符
    public AbstractStringBuilder insert(int offset, char c) {
        ensureCapacityInternal(count + 1);
        System.arraycopy(value, offset, value, offset + 1, count - offset);
        value[offset] = c;
        count += 1;
        return this;
    }


    public AbstractStringBuilder insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }


    public AbstractStringBuilder insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }


    public AbstractStringBuilder insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }


    public AbstractStringBuilder insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }


    public int indexOf(String str) {
        return indexOf(str, 0);
    }


    public int indexOf(String str, int fromIndex) {
        return String.indexOf(value, 0, count, str, fromIndex);
    }


    public int lastIndexOf(String str) {
        return lastIndexOf(str, count);
    }


    public int lastIndexOf(String str, int fromIndex) {
        return String.lastIndexOf(value, 0, count, str, fromIndex);
    }

    //将字符串反转
    public AbstractStringBuilder reverse() {
        boolean hasSurrogates = false;
        int n = count - 1;
        for (int j = (n - 1) >> 1; j >= 0; j--) {
            int k = n - j;
            char cj = value[j];
            char ck = value[k];
            value[j] = ck;
            value[k] = cj;
            if (Character.isSurrogate(cj) ||
                    Character.isSurrogate(ck)) {
                hasSurrogates = true;
            }
        }
        if (hasSurrogates) {
            reverseAllValidSurrogatePairs();
        }
        return this;
    }

    /**
     * Outlined helper method for reverse()
     */
    private void reverseAllValidSurrogatePairs() {
        for (int i = 0; i < count - 1; i++) {
            char c2 = value[i];
            if (Character.isLowSurrogate(c2)) {
                char c1 = value[i + 1];
                if (Character.isHighSurrogate(c1)) {
                    value[i++] = c1;
                    value[i] = c2;
                }
            }
        }
    }

    //子类实现
    @Override
    public abstract String toString();

    //返回容器char [] value
    final char[] getValue() {
        return value;
    }

}
