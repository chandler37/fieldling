/* Configuration - Decompiled by JCavaj
 * Visit http://www.bysoft.se/sureshot/jcavaj/
 */
package fieldling.quilldriver.config;

import java.net.*;
import java.util.*;
import java.io.IOException;
import javax.swing.KeyStroke;
import org.jdom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import fieldling.quilldriver.xml.*;
import org.jdom.*;
import org.xml.sax.SAXException;

public class Configuration
{
    String name = null;
    URL configURL = null;
    URL helpURL = null;
    URL editURL = null;
    URL newURL = null;
    String newTemplate;
    boolean isConfigured = false;
    TagInfo[] tagInfo;
    Map parameters;
    Map actionProfiles;
    Transformer transformer;
    org.jdom.Namespace[] namespaces;
    org.w3c.dom.Document docDoc = null;
    
    Configuration(Element e, ClassLoader loader) {
	name = e.getAttributeValue("menu-name");
	Element configElem = e.getChild("config");
	Element editElem = e.getChild("edit");
	Element newElem = e.getChild("new");
	if (configElem != null) {
            String configString = configElem.getAttributeValue("href");
	    configURL = loader.getResource(configString);
            String suffix;
            String lang = fieldling.util.I18n.getLocale().getLanguage();
            if (lang.equals(""))
                suffix = new String();
            else
                suffix = "_" +lang;
            String prefix = configString.substring(0, configString.length()-4);
            helpURL = loader.getResource(prefix + suffix + ".html");
        }
	if (editElem != null)
	    editURL = loader.getResource(editElem.getAttributeValue("href"));
	if (newElem != null)
	    newURL = loader.getResource(newElem.getAttributeValue("href"));
    }
    public void configure(Map defaultProperties) throws TransformerException, IOException, JDOMException, ParserConfigurationException, SAXException {
        docDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(helpURL.openStream());
	org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
	org.jdom.Document cDoc = builder.build(configURL);
	org.jdom.Element cRoot = cDoc.getRootElement();
        tagInfo = TagInfo.getTagInfoFromXMLConfiguration(cRoot.getChild("rendering-instructions"));
	org.jdom.Element parameterSet = cRoot.getChild("parameters");
        org.jdom.Element newTemplateParam = cRoot.getChild("newtemplate");
        if (newTemplateParam == null)
            newTemplate = new String();
        else
            newTemplate = newTemplateParam.getAttributeValue("val");
        org.jdom.Element namespaceParam = parameterSet.getChild("namespaces");
        String nsValue;
        if (namespaceParam == null) nsValue = new String();
        else nsValue = namespaceParam.getAttributeValue("val");
        List nsList = parseNamespaces(nsValue);
        nsList.add(org.jdom.Namespace.getNamespace("qd", "http://altiplano.emich.edu/quilldriver"));
        namespaces = (org.jdom.Namespace[])nsList.toArray(new org.jdom.Namespace[0]);
        for (int i=0; i<tagInfo.length; i++)
            tagInfo[i].useNamespaces(namespaces);
        XPath xpathEnvironment = XPathUtilities.getXPathEnvironmentForDOM(namespaces);
        parameters = new HashMap(defaultProperties);
	List parameterList = parameterSet.getChildren("parameter");
        Iterator it = parameterList.iterator();
        while (it.hasNext()) {
            org.jdom.Element e = (org.jdom.Element)it.next();
            try {
                parameters.put(e.getAttributeValue("name"), xpathEnvironment.compile(e.getAttributeValue("val")));
            } catch (XPathExpressionException xpe) {
                xpe.printStackTrace();
            }
        }
        actionProfiles = new HashMap();
        java.util.List actions = cRoot.getChild("actions").getChildren("action");
        Iterator actionIter = actions.iterator();
        while (actionIter.hasNext()) {
            Element e = (org.jdom.Element)actionIter.next();
            actionProfiles.put(e.getAttributeValue("name"), new QdActionDescription(xpathEnvironment, 
                e.getAttributeValue("name"), e.getAttributeValue("keystroke"), 
                e.getAttributeValue("node"), e.getAttributeValue("move"), 
                e.getAttributeValue("qd-command"), e.getAttributeValue("xsl-task")));
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (canEdit())
            transformer = transformerFactory.newTransformer(new StreamSource(editURL.openStream()));
        else
            transformer = null;
        isConfigured = true;
    }
    public String getName() {
	return name;
    }
    public String getNewTemplate() {
        if (isConfigured)
            return newTemplate;
        else
            return new String();
    }
    public boolean canEdit() {
        if (editURL == null) return false;
        else return true;
    }
    public org.w3c.dom.Document getHelpDocument() {
        return docDoc;
    }
    public Transformer getTranscriptTransformer() {
        if (isConfigured)
            return transformer;
        else
            return null;
    }
    public TagInfo[] getTagInfo() {
        if (isConfigured)
            return tagInfo;
        else
            return new TagInfo[0];
    }
    public Map getParameters() {
        if (isConfigured)
            return parameters;
        else
            return new HashMap();
    }
    public Map getActionProfiles() {
        if (isConfigured)
            return actionProfiles;
        else
            return new HashMap();
    }
    public org.jdom.Namespace[] getNamespaces() {
        if (isConfigured)
            return namespaces;
        else
            return new org.jdom.Namespace[0];
    }
    private List parseNamespaces(String nsList) {
        List namespaces = new ArrayList();
        StringTokenizer tok = new StringTokenizer(nsList, ",");
        while (tok.hasMoreTokens()) {
            String nextNs = tok.nextToken();
            namespaces.add(org.jdom.Namespace.getNamespace(nextNs.substring(0, nextNs.indexOf(' ')), nextNs.substring(nextNs.indexOf(' ')+1)));
        }
        return namespaces;
    }
                                     /*I added the boolean move parameter to actions in the
                                    configuration files because while for some actions, like 
                                    "Go to Next", you want the cursor to move (say, to the next
                                    line), in other cases, like "Play End of Current",
                                    really you'd rather have the cursor stay where it is so you 
                                    don't have to reposition it for editing. unfortunately this 
                                    hack won't work below for those actions involving xsl-
                                    transforms, since these transforms actually change the data.*/
    public class QdActionDescription {
        private String name, command, task;
        private KeyStroke keyStroke;
        private XPathExpression nodeSelector;
        private boolean move;
        
        public QdActionDescription(XPath xpathEnvironment, String name, String keyStroke, String nodeSelector, String move, String command, String task) {
            this.name = name;
            this.command = command;
            this.task = task;
            this.move = Boolean.valueOf(move).booleanValue();
            this.keyStroke = KeyStroke.getKeyStroke(keyStroke);
            try {
                this.nodeSelector = xpathEnvironment.compile(nodeSelector);
            } catch (XPathExpressionException xpe) {
                xpe.printStackTrace();
                this.nodeSelector = null;
            }
        }
        public String getName() {
            return name;
        }
        public String getCommand() {
            return command;
        }
        public String getXSLTask() {
            return task;
        }
        public KeyStroke getKeyboardShortcut() {
            return keyStroke;
        }
        public XPathExpression getNodeSelector() {
            return nodeSelector;
        }
        public boolean shouldMove() {
            return move;
        }
    }
}

