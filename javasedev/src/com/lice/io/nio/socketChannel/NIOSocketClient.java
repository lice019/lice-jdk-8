package com.lice.io.nio.socketChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * description: NIOSocketClient <br>
 * date: 2019/10/8 22:50 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class NIOSocketClient {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8888);

        //连接服务端
        if (!socketChannel.connect(inetSocketAddress)) {
            //如果上面连接超时，则调用finishConnect一直连，直到连接上
            while (!socketChannel.finishConnect()) {

            }
        }

        //向服务端发送数据
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("send data to server:");
//        String line = scanner.nextLine();
        String line = "hello server";
        ByteBuffer byteBuffer = ByteBuffer.wrap(line.getBytes());
        socketChannel.write(byteBuffer);
        //阻止控制结束。因为客户端连接断开服务端会抛异常
        System.in.read();


    }
}
