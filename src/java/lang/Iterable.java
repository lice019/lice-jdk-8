package java.lang;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Iterable：实现此接口允许对象成为“for-each loop”语句的目标。
 * 而iterable接口里定义了返回iterator的方法，相当于对iterator的封装，同时实现了iterable接口的类可以支持for each循环。
 * Iterable接口 (java.lang.Iterable) 是Java集合的顶级接口之一。Collection接口继承Iterable，所以Collection的所有子类也实现了Iterable接口。
 *
 * @since 1.5
 */
public interface Iterable<T> {

    //返回类型为 T元素的迭代器。
    Iterator<T> iterator();

    /**
     * 函数式接口：自jdk1.8开始引入，函数式接口具有继承传递性，任何实现类，实现了该接口，都将具有该方法。
     * 该接口为消费接口
     *
     * @since 1.8
     */
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }

    /**
     * 默认实现从iterable的Iterator创建一个early-binding拼接器。 Spliter继承了iterable的迭代器的fail-fast属性。
     *
     * @since 1.8
     */
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
