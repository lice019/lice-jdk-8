package com.lice.io.nio.socketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * description: NIOServer <br>
 * date: 2019/10/4 23:55 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class NIOServer implements Runnable {

    private int bufSize = 1024;
    /*
     * Selector:selector存储的是服务端为每个客户端的开启的SocketChannel，给SocketChannel是服务端
     * 开通与客户端通信的，Selector不用存储客户端的SocketChannel的，因为每一个客户端的socket通道都是独立的
     */
    //多路复用器，用于注册通道
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(bufSize);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(bufSize);

    public NIOServer(int port) {
        init(port);
    }

    private void init(int port) {
        try {
            //开启多路复用器
            this.selector = Selector.open();
            //开启服务端通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
            //服务器通道注册到Selector中，并标记通道的状态，一旦客户端发生事件，Selector会通知服务端
            /*
             * 通道状态：
             * OP_ACCEPT：连接成功的标记位
             * OP_READ: 可以读取数据的标记位
             * OP_WRITE: 可以写入数据的标记位
             * OP_CONNECT: 连接建立后的标记
             */
            //设置客户端连接服务端的事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server is started....");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //线程实现方法
    @Override
    public void run() {
        while (true) {
            try {
                //阻塞方法，当至少一个通道被选中，此方法返回的是SelectedKey的数量，可以除了一个是服务端的，其他的都是客户端的
                //通道是否被选中，由注册到多路复用器中的通道标记决定
                int select = this.selector.select();
                //返回选中的通道标记的集合
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                //遍历多路复用器中由标记的通道
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    //将本次要处理的通道从集合中删除，下次循环根据新的通道列表再次执行必要的逻辑业务
                    //下次是根据serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);改变的状态来重新循环
                    it.remove();
                    //通道是否有效
                    if (key.isValid()) {
                        //阻塞状态
                        try {
                            if (key.isAcceptable()) {
                                //监听客户端的连接
                                accept(key);
                            }
                        } catch (CancelledKeyException e) {
                            //断开连接
                            key.cancel();
                        }
                        //可读状态
                        try {
                            if (key.isReadable()) {
                                read(key);
                            }
                        } catch (CancelledKeyException e) {
                            key.cancel();
                        }
                        //可写状态
                        try {
                            if (key.isWritable()) {
                                write(key);
                            }
                        } catch (CancelledKeyException e) {
                            key.cancel();
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //阻塞状态，一旦连接成功，就等着
    private void accept(SelectionKey selectionKey) {
        try {
            //此通道是init方法中注册到Selector上的ServerSocketChannel
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            //阻塞方法，当前客户端发起请求后返回
            //监听客户端连接，客户端一旦连接上服务端，返回一个SocketChannel，用于与客户端进行通信
            //与客户端建立连接的通道
            SocketChannel channel = serverSocketChannel.accept();
            //设置为非阻塞
            channel.configureBlocking(false);
            //注册到多路复用器中,设置为可读标记，此通道用于读取数据用的
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            //关闭通道
            selectionKey.cancel();
        }


    }

    private void read(SelectionKey selectionKey) {
        try {
            //清除读的缓存区
            this.readBuffer.clear();
            //获取客户端的通道，因为客户端是通过客户端通道发送数据的，想要读取客户端的发来的数据，需要获取客户端的通道
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            int readLength = channel.read(readBuffer);
            //检查客户端是否写入数据，如果没有把通道关闭
            if (readLength == -1) {
                selectionKey.channel().close();
                //关闭连接
                selectionKey.cancel();
                return;
            }
            //NIO中最复杂的操作，Buffer的控制
            this.readBuffer.flip();
            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);
            System.out.println("form" + channel.getRemoteAddress() + "client:" + new String(data, "UTF-8"));
            //注册通道，标记为写的操作
            channel.register(this.selector, SelectionKey.OP_WRITE);

        } catch (IOException e) {
            //关闭通道
            selectionKey.cancel();
        }

    }

    private void write(SelectionKey selectionKey) {
        try {
            this.writeBuffer.clear();
            //通过SelectionKey获取客户端的通道，因为服务端需要跟客户端通信写数据
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            Scanner scanner = new Scanner(System.in);
            System.out.println("response data for client:");
            String line = scanner.nextLine();
            writeBuffer.put(line.getBytes("UTF-8"));
            //转换缓存区的状态
            writeBuffer.flip();
            //向客户端的通道发送数据
            channel.write(writeBuffer);
            //注册通道，标记下一个状态标记
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            selectionKey.cancel();
        }


    }

    public static void main(String[] args) {


    }


}
