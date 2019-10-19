package com.lice.io.nio.socketChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * description: NIOSocketServer <br>
 * date: 2019/10/8 21:36 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class NIOSocketServer {


    private int bufSize = 1024;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(bufSize);


    //通过传入服务端端口，初始化服务端，并监听客户端
    public void init(int port) {
        try {
            //开启多路复用器
            selector = Selector.open();
            //开服务端通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //绑定IP和端口
            serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", port));
            //将服务端的通道注册到Selector中，以监听客户端向服务端发起的事件（以客户端连接服务端的连接事件为主）
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("ServerSocket is started...");
            handleKey();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    //服务端不停地监听客户端的事件
    public void handleKey() {
        try {
            while (true) {
                //selector.select(2000)返回int类型的(事件key)
                //非阻塞式
                int selected = selector.select(2000);
                //如果selected==0，说明客户端没有与服务端发起事件,大于0，则有客户端向服务端发起事件
                if (selected == 0) {
                    System.out.println("有" + selected + "客户端发起事件");
                    //如果selected==0，使用continue跳出循环，继续循环监听客户端
                    continue;
                }
                //如果有客户端向服务端发起事件,则取出相应客户端的事件key，进行相应的处理
                //返回客户端事件key集合，并一一遍历取出进行处理
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    //判断通道是否有效
                    if (key.isValid()) {
                        //判断客户端通道的事件类型
                        try {
                            //1、客户端连接事件
                            if (key.isAcceptable()) {
                                accept(key);
                            }
                        } catch (CancelledKeyException e) {
                            //断开连接
                            key.cancel();
                        }
                        //2、读取客户端数据事件
                        try {
                            if (key.isReadable()) {
                                read(key);
                            }
                        } catch (CancelledKeyException e) {
                            key.cancel();
                        }
                        //3、向客户端写数据事件
                        try {
                            if (key.isWritable()) {
                                write(key);
                            }
                        } catch (CancelledKeyException e) {
                            key.cancel();
                        }

                    }
                    //移除处理过的key(已经对某个客户端的向服务端发起事件的key)，如果不移除，则会一直被循环处理
                    it.remove();
                }

            }
        } catch (Exception e) {

        }

    }

    public void accept(SelectionKey key) throws Exception {
        //获取与客户端通信的SocketChannel
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.configureBlocking(false);
        //清空字节缓冲区
        buffer.clear();
        //将客户端连接到服务端的通道注册到Selector，用于监听后续读写的事件，如果有读取客户端的事件，则为其分配buffer缓冲区接收
        socketChannel.register(selector, SelectionKey.OP_READ, buffer);
    }

    public void read(SelectionKey key) throws Exception {
        //获取与客户端通信的SocketChannel
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.configureBlocking(false);
        //清空字节缓冲区
        buffer.clear();
        int readLength = socketChannel.read(buffer);
        if (readLength == -1) {
            key.channel().close();
            //关闭连接
            key.cancel();
            return;
        }
        this.buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println("form" + socketChannel.getRemoteAddress() + "client:" + new String(data, "UTF-8"));
        //将客户端连接到服务端的通道注册到Selector，用于监听后续读写的事件，如果有读取客户端的事件，则为其分配buffer缓冲区接收
        socketChannel.register(selector, SelectionKey.OP_READ, buffer);

    }

    public void write(SelectionKey key) throws Exception {
        //获取与客户端通信的SocketChannel
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.configureBlocking(false);
        //清空字节缓冲区
        buffer.clear();
        Scanner scanner = new Scanner(System.in);
        System.out.println("response data for client:");
        String line = scanner.nextLine();
        buffer.put(line.getBytes("UTF-8"));
        //转换缓存区的状态
        buffer.flip();
        //向客户端的通道发送数据
        socketChannel.write(buffer);
        //注册通道，标记下一个状态标记
        socketChannel.register(this.selector, SelectionKey.OP_READ, buffer);
    }


    public static void main(String[] args) throws Exception {

        NIOSocketServer socketServer = new NIOSocketServer();
        socketServer.init(8888);

    }
}


