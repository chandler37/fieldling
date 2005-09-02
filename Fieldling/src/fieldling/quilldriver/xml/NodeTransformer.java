package fieldling.quilldriver.xml;

import java.util.Map;
import java.util.Iterator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.*;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMConfiguration;

public class NodeTransformer {
    private NodeTransformer() {}
    
    public static DocumentFragment transformNode(Node sourceNode, Transformer transformer, Map parameters) throws TransformerException {
        //DocumentFragment sourceFrag = sourceNode.getOwnerDocument().createDocumentFragment();
        DocumentFragment replaceFrag = sourceNode.getOwnerDocument().createDocumentFragment();
        //sourceFrag.appendChild(sourceNode.cloneNode(true)); //make deep clone of transform node, add to sourceFrag
        Iterator mapIterator = parameters.keySet().iterator();
        while (mapIterator.hasNext()) {
            String key = (String)mapIterator.next();
            transformer.setParameter(key, parameters.get(key));
        }
        transformer.transform(new DOMSource(sourceNode), new DOMResult(replaceFrag));
        //transformer.transform(new DOMSource(sourceFrag), new DOMResult(replaceFrag));
        //transformer.reset();
        transformer.clearParameters();
        /*LOGGING
        try {
            System.out.println("\n------SOURCE\n");
            org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(new org.apache.xml.serialize.OutputFormat("xml", "utf-8", true));
            ser.setOutputByteStream(System.out);
            org.apache.xml.serialize.DOMSerializer domser = ser.asDOMSerializer();
            domser.serialize((org.w3c.dom.Element)sourceNode);
            System.out.println("\n------RESULT\n");
            domser.serialize(replaceFrag);
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        //ENDLOGGING*/
        return replaceFrag;
    }
    public static Node transformAndReplaceNode(Node sourceNode, Transformer transformer, Map parameters) throws TransformerException {
        DocumentFragment replaceFrag = transformNode(sourceNode, transformer, parameters);
        Node firstReplacementNode = replaceFrag.getFirstChild();
        Node parentNode = sourceNode.getParentNode();
        parentNode.replaceChild(replaceFrag, sourceNode);
        return firstReplacementNode;
    }
    public static void revalidate(Document doc, DOMErrorHandler handler) {
            //note: DOM3 revalidation not possible against DTDs
            DOMConfiguration config = doc.getDomConfig();
            config.setParameter("error-handler", handler);
            config.setParameter("schema-type", "http://www.w3.org/2001/XMLSchema");
            config.setParameter("validate", Boolean.TRUE);
            doc.normalizeDocument(); //revalidate against schema
    }
}
