package com.leonlee.windplayer.util;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;

import com.leonlee.windplayer.po.OnlineVideo;

public class XmlReaderHelper {
    
    /** get all category */
    public static ArrayList<OnlineVideo> getAllCategory(final Context context) {
        ArrayList<OnlineVideo> result = new ArrayList<OnlineVideo>();
        DocumentBuilderFactory docBuilderFactory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;
        
        try {
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            
            //load online.xml
            doc = docBuilder.parse(context.getResources().getAssets().open("online.xml"));
            
            //parse xml
            Element root = doc.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName("category");
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                NamedNodeMap attr = node.getAttributes();
                OnlineVideo onlineVideo = new OnlineVideo();
                onlineVideo.title = attr.getNamedItem("name").getNodeValue();
                onlineVideo.id = attr.getNamedItem("id").getNodeValue();
                onlineVideo.category = 1;
                onlineVideo.level = 2;
                onlineVideo.is_category = true;
                result.add(onlineVideo);
            }
        } catch (IOException e) {
        } catch (SAXException e) {
        } catch (ParserConfigurationException e) {
        } finally {
            doc = null;
            docBuilder = null;
            docBuilderFactory = null;
        }
        
        return result;
    }
    
    /** get all tv urls on one category */
    public static ArrayList<OnlineVideo> getVideoUrls(final Context context, String categoryId) {
        ArrayList<OnlineVideo> result = new ArrayList<OnlineVideo>();
        DocumentBuilderFactory docBuilderFactory = null;
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            
            //load xml file
            doc = docBuilder.parse(context.getResources().getAssets().open("online.xml"));
            
            //parse element
            Element root = doc.getElementById(categoryId);
            if (root != null) {
                NodeList nodeList = root.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node baseNode = nodeList.item(i);
                    
                    if (! "item".equals(baseNode.getNodeName()))
                        continue;
                    String id = baseNode.getFirstChild().getNodeValue();
                    if (id == null)
                        continue;
                    OnlineVideo onlineValue = new OnlineVideo();
                    onlineValue.id = id;
                    
                    Element tvElement = doc.getElementById(id);
                    if (tvElement != null) {
                        onlineValue.title = tvElement.getAttribute("title");
                        onlineValue.icon_url = tvElement.getAttribute("image");
                        onlineValue.level = 3;
                        onlineValue.category = 1;
                        NodeList nodes = tvElement.getChildNodes();
                        for (int m = 0; m < nodes.getLength(); ++m) {
                            Node node = nodes.item(m);
                            if (!"ref".equals(node.getNodeName()))
                                continue;
                            String url = node.getAttributes().getNamedItem("href").getNodeValue();
                            if (onlineValue.url == null) {
                                onlineValue.url = url;
                            } else {
                                if (onlineValue.backup_url == null)
                                    onlineValue.backup_url = new ArrayList<String>();
                                onlineValue.backup_url.add(url);
                            }
                        }
                        
                        if (onlineValue.url != null)
                            result.add(onlineValue);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            doc = null;
            docBuilder = null;
            docBuilderFactory = null;
        }
        return result;
    }
}
