package fieldling.quilldriver;

import org.jdom.Text;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jaxen.XPath;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import java.util.List;

public class XMLUtilities {
	private XMLUtilities() {}
	
	public static Object findSingleNode(Object jdomNode, String xpathExpression) {
		if (jdomNode == null)
			return null;
		try {
			JDOMXPath path = new JDOMXPath(xpathExpression);
			return path.selectSingleNode(jdomNode);
		} catch (JaxenException je) {
			je.printStackTrace();
			return null;
		}
	}
	public static List findNodeSet(Object jdomNode, String xpathExpression) {
		if (jdomNode == null)
			return null;
		try {
			JDOMXPath path = new JDOMXPath(xpathExpression);
			return path.selectNodes(jdomNode);
		} catch (JaxenException je) {
			je.printStackTrace();
			return null;
		}
	}
	public static String getTextForNode(Object jdomNode) {
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
