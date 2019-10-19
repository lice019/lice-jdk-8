package com.lice.w3c.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * description: 使用java的org.w3c.dom 操作XML文件<br>
 * date: 2019/8/18 23:18 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class DomApp {
    public static void main(String[] args) throws Exception {
        String path = "javasedev/resource/dom.xml";
        String name = "name";
        String author = "author";
        String year = "year";
        String price = "price";
        Document document = parseXML(path);
        insertDom(document, name, author, year, price);
        domToXML(document, path);

    }

    //添加dom节点
    public static void insertDom(Document document, String... args) {
        int i = 0;
        //获取根节点
        Element root = document.getDocumentElement();
        //创建firstDom
        Element firstElement = document.createElement("book");
        //设置firstElement属性
        firstElement.setAttribute("id", i++ + "");
        Element element;
        //遍历可变参数，创建节点
        for (String arg : args) {
            element = document.createElement(arg);
            element.setTextContent("insertElement");
            firstElement.appendChild(element);
        }
        //根节点添加节点
        root.appendChild(firstElement);
    }

    //XML解析，返回Document
    public static Document parseXML(String path) {
        //创建DocumentBuilder工厂实例
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document = null;
        try {
            //创建DocumentBuilder对象
            documentBuilder = builderFactory.newDocumentBuilder();
            document = documentBuilder.parse(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    //document输出到xml文件中
    public static void domToXML(Document document, String targetPath) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(targetPath);
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
