package com.lice.io.bio;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * description: 模拟Tomcat <br>
 * date: 2019/10/24 11:47 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class Tomcat {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            byte[] len = new byte[in.available()];
            int count = 0;
            if (count == 0) {
                count = in.read(len);
            }
            System.out.println(new String(len, 0, count));
            OutputStream out = socket.getOutputStream();
            out.write("hello client".getBytes());
            out.flush();
            out.close();


        }


    }


}
