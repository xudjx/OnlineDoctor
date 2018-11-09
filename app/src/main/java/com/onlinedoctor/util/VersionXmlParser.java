package com.onlinedoctor.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by wds on 15/11/28.
 */
public class VersionXmlParser {
    public static final String TAG_ROOT = "update";
    public static final String TAG_VERSION = "version";
    public static final String TAG_NAME = "name";
    public static final String TAG_URL = "url";
    public static final String TAG_DISPLAY_MSG = "displayMessage";

    public static HashMap<String, String> parseXml(InputStream inStream) throws Exception {
        HashMap<String, String> hashMap = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inStream);
        Element root = document.getDocumentElement();

        NodeList childNodes = root.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++){
            Node childNode = childNodes.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE){
                Element childElement = (Element) childNode;
                if(TAG_VERSION.equals(childElement.getNodeName())){
                    hashMap.put(TAG_VERSION, childElement.getFirstChild().getNodeValue());
                }else if(TAG_NAME.equals(childElement.getNodeName())){
                    hashMap.put(TAG_NAME, childElement.getFirstChild().getNodeValue());
                }else if(TAG_URL.equals(childElement.getNodeName())){
                    hashMap.put(TAG_URL, childElement.getFirstChild().getNodeValue());
                }else if(TAG_DISPLAY_MSG.equals(childElement.getNodeName())){
                    hashMap.put(TAG_DISPLAY_MSG, childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;
    }
}
