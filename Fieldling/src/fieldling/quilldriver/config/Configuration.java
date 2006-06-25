package fieldling.quilldriver.config;

import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import javax.swing.KeyStroke;
import org.jdom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import fieldling.util.GuiUtil;
import fieldling.util.I18n;
import fieldling.quilldriver.*;
import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.QDShell;
import fieldling.quilldriver.xml.*;
import fieldling.quilldriver.task.BasicTask;
import org.jdom.*;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.validation.*;

public class Configuration
{
	static final String XML_SCHEMA_ELEMENT_NAME = "xmlschema";
	static final String NAMESPACES_ELEMENT_NAME = "namespaces";
	static final String NEW_TEMPLATE_ELEMENT_NAME = "newtemplate";
	static final String XSL_TRANSFORM_ELEMENT_NAME = "xsltransform";
	static final String ALL_PARAMETERS_ELEMENT_NAME = "parameters";
	static final String ONE_PARAMETER_ELEMENT_NAME = "parameter";
	static final String RENDERING_ROOT_ELEMENT_NAME = "rendering-instructions";
	static final String ALL_MENUS_ELEMENT_NAME = "menus";
	static final String ONE_MENU_ELEMENT_NAME = "menu";
	
	static Map qdDefaultProperties;
	static Map actionNameToActionDescription = new HashMap();
        static Map tagNameToTag = new HashMap();
	static String newTagInfo=null;
	
        private org.xhtmlrenderer.simple.XHTMLPanel helpPanel;
	JMenuBar jBar = null;
	String name = null;
	org.jdom.Document configDoc = null;
	URL helpURL = null;
	URL editURL = null;
	String newTemplate;
	String[] schemaList = null;
	String schemaListAsString = null;
	TagInfo[] tagInfo;
	Map parameters;
	Map actionProfiles;
        Map keyActions;
	Transformer transformer;
	org.jdom.Namespace[] namespaces;
	org.w3c.dom.Document docDoc = null;
        JScrollPane helpScrollPane = null;
        
        private static TransformerFactory transformerFactory = null;
        
	static {
		org.jdom.Namespace[] qdNamespace = {org.jdom.Namespace.getNamespace("qd", "http://altiplano.emich.edu/quilldriver")};
		javax.xml.xpath.XPath xpathEnvironment = fieldling.quilldriver.xml.XPathUtilities.getXPathEnvironmentForDOM(qdNamespace);
		qdDefaultProperties = new HashMap();
		try {
			qdDefaultProperties.put("qd.timealignednodes", xpathEnvironment.compile("//*[@qd:*]"));
			qdDefaultProperties.put("qd.nodebegins", xpathEnvironment.compile("@qd:t1"));
			qdDefaultProperties.put("qd.nodeends", xpathEnvironment.compile("@qd:t2"));
			qdDefaultProperties.put("qd.nearestplayableparent", xpathEnvironment.compile("ancestor-or-self::*[@qd:*]"));
		} catch (javax.xml.xpath.XPathExpressionException xpe) {
			xpe.printStackTrace();
		}
                transformerFactory = TransformerFactory.newInstance();
	}

        public static TransformerFactory getTransformerFactory() {
            return transformerFactory;
        }
        
	public Configuration(Element e, ClassLoader loader) throws JDOMException, IOException, TransformerException, ParserConfigurationException, SAXException {
		name = e.getAttributeValue("name");
		String configString = e.getAttributeValue("href");
		org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		configDoc = builder.build(loader.getResource(configString));
		org.jdom.Element parameterElement = configDoc.getRootElement().getChild(ALL_PARAMETERS_ELEMENT_NAME);
		org.jdom.Element newTemplateParam = parameterElement.getChild(NEW_TEMPLATE_ELEMENT_NAME);
		if (newTemplateParam == null) newTemplate = new String();
		else newTemplate = newTemplateParam.getAttributeValue("val");
		String prefix = configString.substring(0, configString.length()-4);
		Locale[] localesToTry = fieldling.util.I18n.getLocalesUpToFallback(fieldling.util.I18n.getLocale(), new Locale("en"));
		for (int i=0; i<localesToTry.length && helpURL == null; i++) {
			String urlToTry = prefix + "_" + localesToTry[i].toString() + ".html";
			helpURL = loader.getResource(urlToTry);
			System.out.println(urlToTry); //LOGGING
		}
		org.jdom.Element editElem = parameterElement.getChild(XSL_TRANSFORM_ELEMENT_NAME);
		if (editElem != null)
			editURL = loader.getResource(editElem.getAttributeValue("val"));
                configure();
	}
	private void configure() throws IOException, TransformerException, ParserConfigurationException, SAXException
	{
		docDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(helpURL.openStream());
                helpPanel = new org.xhtmlrenderer.simple.XHTMLPanel();
                helpPanel.setDocument(getHelpDocument());
                helpScrollPane = GuiUtil.getScrollPaneForJPanel(helpPanel);
                helpScrollPane.setPreferredSize(new Dimension(500,400));
                /*helpPanel.addDocumentListener(new org.xhtmlrenderer.event.DocumentListener() {
                    private boolean run = false;
                    public void documentLoaded() {
                        if (!run) {
                            run = true;
                            helpPanel.relayout();
                        }
                    }
                });*/
		org.jdom.Element cRoot = configDoc.getRootElement();
		tagInfo = TagInfo.getTagInfoFromXMLConfiguration(cRoot.getChild(RENDERING_ROOT_ELEMENT_NAME));
                for (int i=0; i<tagInfo.length; i++) {
                    tagNameToTag.put(tagInfo[i].getIdentifyingName(), tagInfo[i]);
                }
		org.jdom.Element parameterSet = cRoot.getChild(ALL_PARAMETERS_ELEMENT_NAME);
		org.jdom.Element schemaParam = parameterSet.getChild(XML_SCHEMA_ELEMENT_NAME);
				
		if (schemaParam != null)
			schemaList = schemaParam.getAttributeValue("val").split("\\s");
		org.jdom.Element namespaceParam = parameterSet.getChild(NAMESPACES_ELEMENT_NAME);
		String nsValue;
		if (namespaceParam == null) nsValue = new String();
		else nsValue = namespaceParam.getAttributeValue("val");
		List nsList = parseNamespaces(nsValue);
		nsList.add(org.jdom.Namespace.getNamespace("qd", "http://altiplano.emich.edu/quilldriver"));
		namespaces = (org.jdom.Namespace[])nsList.toArray(new org.jdom.Namespace[0]);
		for (int i=0; i<tagInfo.length; i++)
			tagInfo[i].useNamespaces(namespaces);
		XPath xpathEnvironment = XPathUtilities.getXPathEnvironmentForDOM(namespaces);
		parameters = new HashMap(qdDefaultProperties);
		List parameterList = parameterSet.getChildren(ONE_PARAMETER_ELEMENT_NAME);
		Iterator it = parameterList.iterator();
		String type;
		while (it.hasNext()) {
			org.jdom.Element e = (org.jdom.Element)it.next();
			type = e.getAttributeValue("type");
			if (type.equals("XPathExpression"))
			{
				try {
					parameters.put(e.getAttributeValue("name"), xpathEnvironment.compile(e.getAttributeValue("val")));
				} catch (XPathExpressionException xpe) {
					xpe.printStackTrace();
				}
			}
			else if (type.equals("Boolean"))
			{
				parameters.put(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("val")));
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
                keyActions = new HashMap();
		Iterator itty = actionProfiles.values().iterator();
		while (itty.hasNext()) {
			QdActionDescription qdActionDesc = (QdActionDescription)itty.next();
                        Action keyAction = getActionForActionDescription(qdActionDesc);
                        /*if (qdActionDesc.getXSLTask() != null && qdActionDesc.getXSLTask().equals("qd.insertTimes"))
                            insertTimesAction = keyAction;*/
                        if (qdActionDesc.getKeyboardShortcut() != null) {
                            keyActions.put(qdActionDesc.getKeyboardShortcut(), keyAction); //eventually to be registered with transcript's JTextPane
                        }
		}
		if (tagInfo.length > 1) {
			for (int i=0; i<tagInfo.length; i++) {
				KeyStroke key = tagInfo[i].getKeyboardShortcut();
				Action keyAction = getActionForTagInfoChange(tagInfo[i]);
				if ( !(key == null || keyAction == null) )
					keyActions.put(key, keyAction);
			}
		}
		setJMenuBar(cRoot.getChild(ALL_MENUS_ELEMENT_NAME));
		//setJMenuBar(cRoot.getChild(ALL_MENUS_ELEMENT_NAME), qd, prefmngr);
		if (canEdit())
			transformer = transformerFactory.newTransformer(new StreamSource(editURL.openStream()));
		else
			transformer = null;
	}
	public String getName() {
		return name;
	}
	public String getSchemaListAsString() {
		return schemaListAsString;
	}
	public DocumentBuilder getDocumentBuilder(DocumentBuilderFactory factory) throws ParserConfigurationException {
		/* Using JAXP you can instruct the parser to validate against XML Schema only. 
		 The JAXP 1.3 Validation API allows you to build an in-memory representation 
		 of an XML Schema which you can then set on a parser factory. Parsers created 
		 from the factory will validate documents using the schema object you specified.
		 It is also possible to configure a SAX parser or DocumentBuilder to validate 
		 against XML Schema only (http://xml.apache.org/xerces2-j/faq-pcfp.html#faq-4*).
		 However, if you do this, at least with Xerces-2 Java, XML ids are not properly
		 recognized, with the effect that the id() and getElementsById() functions won't work. 
		 For these functions to work, make sure the validation feature and the schema feature are 
		 turned on before you parse a document. And, note that setting these features is
		 incompatible with setting a Schema for a document. See
		 http://xml.apache.org/xerces2-j/faq-dom.html#faq-13*/
		factory.setNamespaceAware(true);
		factory.setFeature("http://xml.org/sax/features/validation", true);
		factory.setFeature("http://apache.org/xml/features/validation/schema", true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		/* The syntax for the http://apache.org/xml/properties/schema/external-schemaLocation 
		 property is the same as for schemaLocation attributes in instance 
		 documents: e.g, "http://www.example.com file_name.xsd". The user 
		 can specify more than one XML Schema in the list. The 
		 http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation 
		 property allows the user to specify an XML Schema with no namespace. 
		 The syntax is a same as for the noNamespaceSchemaLocation attribute 
		 that may occur in an instance document: e.g."file_name.xsd". The user
		 may specify only one XML Schema. For more information see:
		 http://xml.apache.org/xerces2-j/properties.html */
		if (schemaList != null) {
			if (schemaList.length==1) { //then dealing with external-noNamespaceSchemaLocation
				URL schemaUrl = Configuration.this.getClass().getClassLoader().getResource(schemaList[0]);
				schemaListAsString = schemaUrl.toString();
				factory.setAttribute("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schemaListAsString);
			} else { //dealing with external-schemaLocation
				schemaListAsString = new String();
				StringBuffer property = new StringBuffer();
				for (int i=0; i<schemaList.length; i=i+2) {
					if (i>0) {
						property.append(' ');
						schemaListAsString += " ";
					}
					property.append(schemaList[i]);
					property.append(' ');
					URL schemaUrl = Configuration.this.getClass().getClassLoader().getResource(schemaList[i+1]);
					property.append(schemaUrl.toString());
					schemaListAsString += schemaUrl.toString();
				}
				factory.setAttribute("http://apache.org/xml/properties/schema/external-schemaLocation", property.toString());
			}
		}
		return factory.newDocumentBuilder();
	}
	public String getNewTemplate() {
		return newTemplate;
	}
	public boolean canEdit() {
		if (editURL == null) return false;
		else return true;
	}
	public org.w3c.dom.Document getHelpDocument() {
		return docDoc;
	}
        public JScrollPane getHelpScrollPane() {
                return helpScrollPane;
        }
        public java.awt.print.Printable getHelpAsPrintable() {
            return new org.xhtmlrenderer.simple.XHTMLPrintable(helpPanel);
        }
	public Transformer getTranscriptTransformer() {
                return transformer;
	}
	public TagInfo[] getTagInfo() {
		return tagInfo;
	}
	public Map getParameters() {
		return parameters;
	}
	public Map getActionProfiles() {
		return actionProfiles;
	}
	public org.jdom.Namespace[] getNamespaces() {
		return namespaces;
	}
        public Map getKeyActions() {
                return keyActions;
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
	private void setJMenuBar(org.jdom.Element allMenus)
	//private void setJMenuBar(org.jdom.Element allMenus, QD qd, PreferenceManager prefmngr)
	{
		String menuName;
		if (allMenus == null) return;
		ResourceBundle messages = fieldling.util.I18n.getResourceBundle();
		jBar = new JMenuBar();
		List menuElems = allMenus.getChildren(ONE_MENU_ELEMENT_NAME);
		JMenu jMenu;
		Iterator itty = menuElems.iterator();
		while (itty.hasNext()) {
			org.jdom.Element elem = (org.jdom.Element)itty.next();
			menuName = elem.getAttributeValue("name");
                        jMenu = new JMenu(messages.getString(menuName));
                        String[] menuItems = elem.getAttributeValue("contains").split(" ");
                        for (int j=0; j<menuItems.length; j++) {
                            if (menuItems[j].equals("|")) jMenu.addSeparator();
                            else {
                            QdActionDescription actDesc = getActionDescriptionForActionName(menuItems[j]);
                            if (actDesc != null) { //it's an action, so execute action
                                final Action menuAction = getActionForActionDescription(actDesc);
                                JMenuItem jItem = new JMenuItem(messages.getString(menuItems[j]));
                                jItem.setAccelerator(actDesc.getKeyboardShortcut());
                                jItem.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent ae) {
                                        menuAction.actionPerformed(ae);
                                    }
                                });
                                jMenu.add(jItem);
                            } else if (tagNameToTag.containsKey(menuItems[j])) { //it's a tag view, so change tag view
                                TagInfo tagView = (TagInfo)tagNameToTag.get(menuItems[j]);
                                final Action tagViewAction = getActionForTagInfoChange(tagView);
                                //final Action tagViewAction = getActionForTagInfoChange(qd, tagView);
                                JMenuItem tagItem = new JMenuItem(messages.getString(tagView.getIdentifyingName()));
                                tagItem.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        tagViewAction.actionPerformed(e);  
                                    }
                                });
                                tagItem.setAccelerator(tagView.getKeyboardShortcut());
                                jMenu.add(tagItem);
                            }
                            }
                        }
                        jMenu.getPopupMenu().setLightWeightPopupEnabled(false);
                        jBar.add(jMenu);
		}
	}
	public JMenuBar getJMenuBar() {
		return jBar;
	}
	public static Action getActionForTagInfoChange(final TagInfo tagInfo) {
	//public static Action getActionForTagInfoChange(final QD qd, final TagInfo tagInfo) {
		return new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (!(source instanceof Component))
				{
					System.out.println("no component for event--what to do?");
					return;
				}
				QD qd = getQDShellParentForComponent((Component)source).getQD();
				if (qd == null) {
					System.out.println("can't find any QD parent");
					return;
				}
                                qd.changeTagInfo(tagInfo);
				//qd.changeTagInfo(tagInfo,i);
			}
		};
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
		//private String name, command, task;
                private String name, task;
                private BasicTask command;
		private KeyStroke keyStroke;
		private XPathExpression nodeSelector;
		private boolean move;
		
		public QdActionDescription(XPath xpathEnvironment, String name, String keyStroke, String nodeSelector, String move, String command, String task) {
			this.name = name;;
			this.task = task;
			this.move = Boolean.valueOf(move).booleanValue();
			this.keyStroke = KeyStroke.getKeyStroke(keyStroke);
			//this.command = command
                        try {
                            if (command == null) this.command = null;
                            else this.command =BasicTask.getTaskForClass(command);
                        } catch (ClassNotFoundException cnfe) {
                            cnfe.printStackTrace();
                            this.command = null;
                        } catch (InstantiationException ie) {
                            ie.printStackTrace();
                            this.command = null;
                        } catch (IllegalAccessException iae) {
                            iae.printStackTrace();
                            this.command = null;
                        }
			try {
				this.nodeSelector = xpathEnvironment.compile(nodeSelector);
			} catch (XPathExpressionException xpe) {
				xpe.printStackTrace();
				this.nodeSelector = null;
			}
			actionNameToActionDescription.put(this.name, this);
		}
		public String getName() {
			return name;
		}
		public BasicTask getCommand() {
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
	public static QdActionDescription getActionDescriptionForActionName(String name) {
		return (QdActionDescription)actionNameToActionDescription.get(name);
	}
	public static Action getActionForActionDescription(final QdActionDescription qdActionDesc) {
		Action keyAction;
		if (qdActionDesc.getXSLTask() == null) { //no xsl transform
			keyAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					Object source = e.getSource();
					if (!(source instanceof Component)) {
						System.out.println("no component for event--what to do?");
						return;
					}
                                        QD qd = getQDShellParentForComponent((Component)source).getQD();
					if (qd == null) {
						System.out.println("can't find any QD parent");
						return;
					}
					if (qd.getEditor() != null && qdActionDesc.getNodeSelector() != null) {
						qd.getEditor().fireEndEditEvent();
						Object moveTo = qd.getEditor().getNextVisibleNode(qd.getEditor().getTextPane().getCaret().getMark(), qdActionDesc.getNodeSelector());
						qd.getEditor().getTextPane().requestFocus();
						if (qdActionDesc.shouldMove())
							qd.getEditor().getTextPane().setCaretPosition(qd.getEditor().getStartOffsetForNode(moveTo));
                                        }
					if (qdActionDesc.getCommand() != null) qdActionDesc.getCommand().execute(qd, "");
				}
			};
		} else { //xsl transform
			keyAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					Object source = e.getSource();
					if (!(source instanceof Component)) {
						System.out.println("no component for event--what to do?");
						return;
					}
                                        QD qd = getQDShellParentForComponent((Component)source).getQD();
					if (qd == null) {
						System.out.println("can't find any QD parent");
						return;
					}
                                        if (qd.getEditor() != null) {
                                            qd.transformTranscript(qd.getEditor().getNodeForOffset(qd.getEditor().getTextPane().getCaret().getMark()), qdActionDesc.getNodeSelector(), qdActionDesc.getXSLTask());
                                            if (qdActionDesc.getCommand() != null) qdActionDesc.getCommand().execute(qd, "");
                                        } 
				}
			};
		}
		return keyAction;
	}
	/*
	 * Get the top most component for a given component
	 */
	public static QDShell getQDShellParentForComponent(Component comp) {
		if (comp instanceof QDShell) return (QDShell)comp;
		Component tcomp = comp;
		MenuElement mi;
		if (comp instanceof MenuElement) {
			if (comp instanceof JPopupMenu) {
				tcomp = ((JPopupMenu)comp).getInvoker();
			} else {
				tcomp = ((MenuElement)comp).getComponent();
			}
		}
		Component parent = tcomp.getParent();
		if (parent instanceof QDShell) return (QDShell)parent;
		if (parent instanceof MenuElement) {
			if (parent instanceof JPopupMenu) {
				parent = ((JPopupMenu)parent).getInvoker();
			} else {
				parent = ((MenuElement)parent).getComponent();
			}
		}
		if (parent instanceof QDShell) return (QDShell)parent;
		while (parent != null) {
			tcomp = parent;
			parent = tcomp.getParent();
			if (parent instanceof QDShell) return (QDShell)parent;
			if (parent instanceof MenuElement) {
				if (parent instanceof JPopupMenu) {
					parent = ((JPopupMenu)parent).getInvoker();
				} else {
					parent = ((MenuElement)parent).getComponent();
				}
			}
		}
		if (tcomp instanceof QDShell) return (QDShell)tcomp;
		else return null;
	}
}
