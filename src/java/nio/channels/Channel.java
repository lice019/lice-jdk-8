/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.nio.channels;

import java.io.IOException;
import java.io.Closeable;


/**
 * A nexus for I/O operations.
 *
 *
 * <p> A channel represents an open connection to an entity such as a hardware
 * device, a file, a network socket, or a program component that is capable of
 * performing one or more distinct I/O operations, for example reading or
 * writing.
 *
 * <p> A channel is either open or closed.  A channel is open upon creation,
 * and once closed it remains closed.  Once a channel is closed, any attempt to
 * invoke an I/O operation upon it will cause a {@link ClosedChannelException}
 * to be thrown.  Whether or not a channel is open may be tested by invoking
 * its {@link #isOpen isOpen} method.
 *
 * <p> Channels are, in general, intended to be safe for multithreaded access
 * as described in the specifications of the interfaces and classes that extend
 * and implement this interface.
 * 以上的翻译：
 * 用于I/O操作的连接。
 * 通道表示到实体(如硬件设备、文件、网络套接字或程序组件)的开放连接，该实体能够执行一个或多个不同的I/O操作，例如读或写。
 * 一个通道要么打开要么关闭。通道在创造时打开，
 * 一旦关闭，它仍然关闭。一旦通道关闭，对其调用I/O操作的任何尝试都会引发{@link ClosedChannelException}。通道是否打开可以通过调用它的{@link #isOpen isOpen}方法来测试。
 * 通道，一般来说，打算是安全的多线程访问中描述的接口和类的规范，扩展和实现这个接口。
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

/**
 * Channel：NIO的管道根接口,管道是流的一个通道，通过Buffer缓存区容器来配合对数据进行输入输出，管道既可以输入也可以输出
 * 管道可以通过Selectors来解决单线程的并发，这也是NIO的设计理念和处出发点
 */
public interface Channel extends Closeable {

    /**
     * 是否开启管道，true：开启中；false：关闭中
     *
     * @return
     */
    public boolean isOpen();

    //关闭管道
    public void close() throws IOException;

}
