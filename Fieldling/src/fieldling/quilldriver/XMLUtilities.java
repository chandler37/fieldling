package fieldling.quilldriver;

import org.jdom.Namespace;
import java.util.List;

public class XMLUtilities {
	private XMLUtilities() {}

	public static List selectJDOMNodes(Object jdomNode, String xpathExpression) {
		return selectJDOMNodes(jdomNode, xpathExpression, new Namespace[0]);
	}
	public static List selectJDOMNodes(Object jdomNode, String xpathExpression, Namespace namespace) {
		if (namespace == null)
			return selectJDOMNodes(jdomNode, xpathExpression, new Namespace[0]);
		else {
			Namespace[] ns = new Namespace[1];
			ns[0] = namespace;
			return selectJDOMNodes(jdomNode, xpathExpression, ns);
		}
	}
	public static List selectJDOMNodes(Object jdomNode, String xpathExpression, Namespace[] namespaces) {
		if (jdomNode == null)
			return null;
		try {
			org.jdom.xpath.XPath path = org.jdom.xpath.XPath.newInstance(xpathExpression);
			for (int i=0; i<namespaces.length; i++)
				path.addNamespace(namespaces[i].getPrefix(), namespaces[i].getURI());
			return path.selectNodes(jdomNode);
		} catch (org.jdom.JDOMException je) {
			je.printStackTrace();
			return null;
		}
	}
	public static Object selectSingleJDOMNode(Object jdomNode, String xpathExpression) {
        List nodes = selectJDOMNodes(jdomNode, xpathExpression);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
	}
    public static Object selectSingleJDOMNode(Object jdomNode, String xpathExpression, Namespace namespace) {
        List nodes = selectJDOMNodes(jdomNode, xpathExpression, namespace);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
    }
    public static Object selectSingleJDOMNode(Object jdomNode, String xpathExpression, Namespace[] namespaces) {
        List nodes = selectJDOMNodes(jdomNode, xpathExpression, namespaces);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
    }
	public static String getTextForJDOMNode(Object jdomNode) {
		if (jdomNode instanceof org.jdom.Text) {
			org.jdom.Text t = (org.jdom.Text)jdomNode;
			return t.getText();
		} else if (jdomNode instanceof org.jdom.Attribute) {
			org.jdom.Attribute a = (org.jdom.Attribute)jdomNode;
			return a.getValue();
		} else if (jdomNode instanceof org.jdom.Element) {
			org.jdom.Element e = (org.jdom.Element)jdomNode;
			return e.getTextTrim();
		} else if (jdomNode instanceof String) {
            return (String)jdomNode;
        }
        return null;
	}
	public static List selectDOMNodes(Object domNode, String xpathExpression) {
		return selectDOMNodes(domNode, xpathExpression, new Namespace[0]);
	}
	public static List selectDOMNodes(Object domNode, String xpathExpression, Namespace namespace) {
		if (namespace == null)
			return selectDOMNodes(domNode, xpathExpression, new Namespace[0]);
		else {
			Namespace[] ns = new Namespace[1];
			ns[0] = namespace;
			return selectDOMNodes(domNode, xpathExpression, ns);
		}
	}
	public static List selectDOMNodes(Object domNode, String xpathExpression, Namespace[] namespaces) {
		if (domNode == null)
			return null;
		try {
			org.jaxen.XPath path = new org.jaxen.dom.DOMXPath(xpathExpression);
			for (int i=0; i<namespaces.length; i++)
				path.addNamespace(namespaces[i].getPrefix(), namespaces[i].getURI());
			return path.selectNodes(domNode);
		} catch (org.jaxen.JaxenException je) {
			je.printStackTrace();
			return null;
		}
	}
	public static Object selectSingleDOMNode(Object domNode, String xpathExpression) {
        List nodes = selectDOMNodes(domNode, xpathExpression);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
	}
    public static Object selectSingleDOMNode(Object domNode, String xpathExpression, Namespace namespace) {
        List nodes = selectDOMNodes(domNode, xpathExpression, namespace);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
    }
    public static Object selectSingleDOMNode(Object domNode, String xpathExpression, Namespace[] namespaces) {
        List nodes = selectDOMNodes(domNode, xpathExpression, namespaces);
        if (nodes == null || nodes.size() == 0)
            return null;
        return nodes.get(0);
    }
	public static String getTextForDOMNode(Object domNode) {
		if (domNode instanceof org.w3c.dom.Text) {
			org.w3c.dom.Text t = (org.w3c.dom.Text)domNode;
			return t.getData();
		} else if (domNode instanceof org.w3c.dom.Attr) {
			org.w3c.dom.Attr a = (org.w3c.dom.Attr)domNode;
			return a.getValue();
		} else if (domNode instanceof org.w3c.dom.Element) {
            org.w3c.dom.Node n = (org.w3c.dom.Node)domNode;
			org.w3c.dom.NodeList nl = n.getChildNodes();
            StringBuffer buffy = new StringBuffer();
            for (int z=0; z<nl.getLength(); z++) {
                if (nl.item(z) instanceof org.w3c.dom.Text)
                    buffy.append(nl.item(z).getNodeValue());
            }
			return buffy.toString();
		} else if (domNode instanceof String) {
            return (String)domNode;
        }
        return null;
	}    
}
