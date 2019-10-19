package com.lice.w3c.dom;

import com.lice.w3c.dom.entity.Book;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * description: 使用Dom操作XML <br>
 * date: 2019/8/21 20:26 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class DomApp2 {

    public static void main(String[] args) throws Exception {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dom.xml");
        //将XML加载到内存中，Document就已经是整个XML文档，并且代表着根节点
        Document document = builder.parse(inputStream);
        //获取跟节点的所有子节点，即book节点，多个的。
        NodeList nodeList = document.getElementsByTagName("book");
        ArrayList<Book> arrayList = new ArrayList<Book>();
        Book book = null;
        //循环，继续取book内的节点内容
        for (int i = 0; i < nodeList.getLength(); i++) {
            //创建book对象，存储每一个book节点的内容
            book = new Book();
            //每一个book节点的全部内容
            Node node = nodeList.item(i);
            //获取id属性中的值
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            book.setbId(id);
            //获取book标签节点的子节点，是一个NodeList集合
            NodeList childNodes = node.getChildNodes();
            //循环book子节点
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node item = childNodes.item(j);
                String name = item.getNodeName();
                if ("name".equals(name)) {
                    book.setbName(item.getFirstChild().getNodeValue());
                } else if ("author".equals(name)) {
                    book.setAuthor(item.getFirstChild().getNodeValue());
                } else if ("year".equals(name)) {
                    book.setYear(item.getFirstChild().getNodeValue());
                } else if ("price".equals(name)) {
                    book.setPrice(item.getFirstChild().getNodeValue());
                }
            }
            arrayList.add(book);
        }

        System.out.println(Arrays.toString(arrayList.toArray()));
    }
}
