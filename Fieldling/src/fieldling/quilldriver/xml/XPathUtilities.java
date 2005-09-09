package fieldling.quilldriver.xml;

import org.jdom.Namespace;
import javax.xml.namespace.NamespaceContext;
import java.util.*;
import javax.xml.xpath.*;

public class XPathUtilities {
        private static XPathFactory xpathFactoryForDOM = null;
        
	private XPathUtilities() {}

        public static XPathFactory getXPathFactoryForDOM() {
            if (xpathFactoryForDOM == null) {
                System.setProperty("javax.xml.xpath.XPathFactory", "net.sf.saxon.xpath.XPathFactoryImpl");
                /* The following more general code should work, but it doesn't.
                See the code within the following post:
                        http://sourceforge.net/forum/message.php?msg_id=2957596
                try {
                    javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL); //DOESN'T LOCATE SAXON!!
                } catch (javax.xml.xpath.XPathFactoryConfigurationException xpfce) {
                    xpfce.printStackTrace();
                }*/
                try {
                    xpathFactoryForDOM = net.sf.saxon.xpath.XPathFactoryImpl.newInstance(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL);
                } catch (javax.xml.xpath.XPathFactoryConfigurationException xpfce) {
                    xpfce.printStackTrace();
                }
            }
            return xpathFactoryForDOM;
        }
        public static XPath getXPathEnvironmentForDOM(Namespace[] namespaces) {
            return getXPathEnvironment(getXPathFactoryForDOM().newXPath(), namespaces);
        }
        private static XPath getXPathEnvironment(XPath xpath, final Namespace[] namespaces) {
            xpath.setNamespaceContext(
                    new NamespaceContext() {
                        public String getNamespaceURI(String s) {
                            for (int i=0; i<namespaces.length; i++) {
                                if (s.equals(namespaces[i].getPrefix()))
                                    return namespaces[i].getURI();
                            }
                            return null;
                        }
                        public String getPrefix(String s) { return null; }
                        public Iterator getPrefixes(String s) { return null; }
                    }
                );
           return xpath;
        }
        public static List saxonSelectMultipleDOMNodes(Object domNode, XPathExpression xpe) {
            if (domNode == null)
                return null;
            try { /* see note below about casting to VirtualNode and getting the underlying node */
                org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList)xpe.evaluate(domNode, XPathConstants.NODESET);
                List nodesAsList = new LinkedList();
                for (int i=0; i<nodeList.getLength(); i++)
                    nodesAsList.add(nodeList.item(i));
                return nodesAsList;
                //return (List)xpe.evaluate(domNode, XPathConstants.NODESET);
                /*List foundNodes = new LinkedList();
                Iterator itty = ((List)xpe.evaluate(domNode, XPathConstants.NODESET)).iterator();
                while (itty.hasNext()) {
                    net.sf.saxon.om.VirtualNode virtualNode = (net.sf.saxon.om.VirtualNode)itty.next();
                    foundNodes.add(virtualNode.getUnderlyingNode());
                }
                return foundNodes;*/
            } catch (XPathExpressionException xpee) {
                xpee.printStackTrace();
                return null;
            }
        }
        public static org.w3c.dom.Node saxonSelectSingleDOMNode(Object domNode, XPathExpression xpe) {
            if (domNode == null)
                return null;
            try {
                /* We'd like to just evaluate the xpath expression and get a DOM node back. However, unfortunately,
                in saxon-b 8.5, you've got to first first cast to a VirtualNode and then get the underlying DOM node
                from that. I guess this is a bug that will be fixed--see the thread at
                      http://sourceforge.net/mailarchive/message.php?msg_id=12547183 */ 
                /*net.sf.saxon.om.VirtualNode vNode = (net.sf.saxon.om.VirtualNode)xpe.evaluate(domNode, XPathConstants.NODE);
                if (vNode == null) //if the node isn't found, i guess
                    return null;
                return (org.w3c.dom.Node)vNode.getUnderlyingNode();*/
                /* Update (9-9-05): I just upgraded to Saxon 8.5.1, and now this problem has gone away. */
                org.w3c.dom.Node vNode = (org.w3c.dom.Node)xpe.evaluate(domNode, XPathConstants.NODE);
                return vNode;
            } catch (XPathExpressionException xpee) {
                xpee.printStackTrace();
                return null;
            }
        }
        public static String saxonSelectSingleDOMNodeToString(Object domNode, XPathExpression xpe) {
            if (domNode == null)
                return null;
            try {
                return xpe.evaluate(domNode);
            } catch (XPathExpressionException xpee) {
                xpee.printStackTrace();
                return null;
            }
        }
        public static String saxonSelectSingleDOMNodeToString(Object domNode, String xpathExpression, Namespace[] namespaces) {
            if (domNode == null)
                return null;
            try {
                XPathExpression xpe = getXPathEnvironmentForDOM(namespaces).compile(xpathExpression);
                return xpe.evaluate(domNode);
            } catch (javax.xml.xpath.XPathExpressionException xpee) {
                xpee.printStackTrace();
            }
            return null;
	}
}
