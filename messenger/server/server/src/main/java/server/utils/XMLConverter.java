package server.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLConverter {
    public static String toXml(JsonObject response) {
        String type = response.get("type").getAsString();
        String status = response.get("status").getAsString();
        JsonObject content = response.getAsJsonObject("content");

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<response>");
        xml.append("<type>").append(escapeXml(type)).append("</type>");
        xml.append("<status>").append(escapeXml(status)).append("</status>");
        xml.append("<content>");
        
        for (Map.Entry<String, JsonElement> entry : content.entrySet()) {
            xml.append("<").append(escapeXml(entry.getKey())).append(">");
            xml.append(escapeXml(entry.getValue().toString())).append("</").append(escapeXml(entry.getKey())).append(">");
        }
        
        xml.append("</content>");
        xml.append("</response>");
        
        return xml.toString();
    }

    private static String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static JsonObject parseXmlToJson(String xml) {
        JsonObject response = new JsonObject();
        JsonObject content = new JsonObject();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();
            
            NodeList typeList = doc.getElementsByTagName("type");
            if (typeList.getLength() > 0) {
                response.addProperty("type", typeList.item(0).getTextContent());
            }
            
            NodeList statusList = doc.getElementsByTagName("status");
            if (statusList.getLength() > 0) {
                response.addProperty("status", statusList.item(0).getTextContent());
            }
            
            NodeList contentList = doc.getElementsByTagName("content");
            if (contentList.getLength() > 0) {
                Node contentNode = contentList.item(0);
                NodeList childNodes = contentNode.getChildNodes();
                
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String key = node.getNodeName();
                        String value = node.getTextContent();
                        try {
                            JsonElement jsonElement = JsonParser.parseString(value);
                            content.add(key, jsonElement);
                        } catch (JsonSyntaxException e) {
                            content.addProperty(key, value);
                        }
                    }
                }
                response.add("content", content);
            }
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
        
        return response;
    }
}
