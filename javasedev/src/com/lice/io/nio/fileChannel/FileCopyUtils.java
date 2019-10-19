package com.lice.io.nio.fileChannel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.ByteBuffer.*;

/**
 * description: FileCopyUtils通过使用NIO中的FileChannel进行文件复制 <br>
 * date: 2019/10/5 21:36 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class FileCopyUtils {

    private static final int BUF_SIZE = 1024;
    private static FileChannel readChannel;
    private static FileChannel writeChannel;
    private static FileInputStream readFile;
    private static FileOutputStream writeFile;
    private static ByteBuffer buf = allocate(BUF_SIZE);


    public static boolean copyFile(String from, String to) throws IOException {
        try {
            if (from == null || "".equals(from))
                throw new IOException("源文件名为空");
            if (to == null || "".equals(to))
                throw new IOException("目标文件名为空");
            File fromFile = new File(from);
            if (!fromFile.exists())
                new IOException("源文件不存");
            if (fromFile.isDirectory())
                new IOException(from + "是目录");
            readFile = new FileInputStream(fromFile);
            writeFile = new FileOutputStream(new File(to));
            //获取读的文件通道
            readChannel = readFile.getChannel();
            //获取写的文件通道
            writeChannel = writeFile.getChannel();
            //NIO中FileChannel复制文件
            readChannel.transferTo(0, readChannel.size(), writeChannel);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readChannel != null)
                readChannel.close();
            if (writeChannel != null)
                writeChannel.close();
            if (readFile != null)
                readFile.close();
            if (writeFile != null)
                writeFile.close();
        }
        return false;
    }

    public static void copyFile(File from, File to) throws IOException {
        if (from == null || to == null) {
            throw new IOException("File is null");
        }
        int length = -1; //标记位
        readFile = new FileInputStream(from);
        writeFile = new FileOutputStream(to);
        readChannel = readFile.getChannel();
        writeChannel = writeFile.getChannel();
        try {
            buf.clear();
            //读取文件
            while ((length = readChannel.read(buf)) != -1) {
                //阻塞的方法
                buf.flip();
                writeChannel.write(buf);
                //在这里缓冲区不清除，会一直的读和写
                //原因是使用同一个缓冲区
                buf.clear();
            }
            readChannel.close();
            readFile.close();
            writeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readChannel != null)
                readChannel.close();
            if (writeChannel != null)
                writeChannel.close();
            if (readFile != null)
                readFile.close();
            if (writeFile != null)
                writeFile.close();
        }
    }

    public static void readFile(String path) throws IOException {
        try {
            File file;
            //清空缓冲去
            buf.clear();
            if (path == null || "".equals(path))
                throw new IOException("文件名为空");
            else {
                file = new File(path);
            }
            if (!file.exists())
                throw new IOException("文件不存在");
            readFile = new FileInputStream(file);
            readChannel = readFile.getChannel();
            System.out.println("限制是：" + buf.limit() + ",容量是：" + buf.capacity() + " ,位置是：" + buf.position());
            int length = -1;
            //循环读取
            while ((length = readChannel.read(buf)) != -1) {
                //开辟一个缓冲区大小的字节数组
                byte[] bytes = buf.array();
                String data = new String(bytes, 0, length);
                System.out.println(data);
            }
            readChannel.close();
            readFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readChannel != null)
                readChannel.close();
            if (readFile != null)
                readFile.close();
        }


    }

    public static void main(String[] args) throws Exception {
        //copyFile("D:\\workspace-j\\idea\\source\\jdk-8\\javasedev\\resource\\dom.xml", "E:\\dom1.xml");
        readFile("D:\\workspace-j\\idea\\source\\jdk-8\\javasedev\\resource\\dom.xml");
//        File from = new File("D:\\workspace-j\\idea\\source\\jdk-8\\javasedev\\resource\\dom.xml");
//        File target = new File("E:\\dom1.xml");
//        copyFile(from, target);
    }

}
