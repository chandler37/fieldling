package fieldling.quilldriver;

import org.jdom.Text;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jaxen.XPath;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
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
			XPath path = new JDOMXPath(xpathExpression);
			for (int i=0; i<namespaces.length; i++)
				path.addNamespace(namespaces[i].getPrefix(), namespaces[i].getURI());
			return path.selectNodes(jdomNode);
		} catch (JaxenException je) {
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
		if (jdomNode instanceof Text) {
			Text t = (Text)jdomNode;
			return t.getText();
		} else if (jdomNode instanceof Attribute) {
			Attribute a = (Attribute)jdomNode;
			return a.getValue();
		} else if (jdomNode instanceof Element) {
			Element e = (Element)jdomNode;
			return e.getTextTrim();
		} else return null;
	}
}
