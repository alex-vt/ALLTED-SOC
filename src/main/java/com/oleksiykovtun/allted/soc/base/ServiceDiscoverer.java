package com.oleksiykovtun.allted.soc.base;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.*;

/**
 * Service discoverer based on the application.wadl document
 */
public class ServiceDiscoverer {

    public static List<String> getServiceLinkList(String urlString) throws Throwable {
        List<String> serviceLinks = new ArrayList<>();
        for (String serviceName : getServiceNameList(urlString)) {
            serviceLinks.add(urlString + serviceName);
        }
        return serviceLinks;
    }

    public static List<String> getServiceNameList(String urlString) throws Throwable {
        Document wadlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new URL(urlString + "application.wadl").openStream());
        NodeList resourceNodeList = wadlDocument.getElementsByTagName("resource");
        Set<String> serviceNames = new TreeSet<>();
        for (int i = 0; i < resourceNodeList.getLength(); ++i) {
            String path = ((Element) resourceNodeList.item(i)).getAttribute("path");
            if (path.matches("/.+/")) {
                serviceNames.add(path.replaceAll("/", ""));
            }
        }
        return new ArrayList<>(serviceNames);
    }

}

