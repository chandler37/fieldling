/*
The contents of this file are subject to the THDL Open Community License
Version 1.0 (the "License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License on the THDL web site 
(http://www.thdl.org/).

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the 
License for the specific terms governing rights and limitations under the 
License. 

The Initial Developer of this software is the Tibetan and Himalayan Digital
Library (THDL). Portions created by the THDL are Copyright 2001 THDL.
All Rights Reserved. 

Contributor(s): ______________________________________.
*/

package fieldling.quilldriver;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import java.awt.*;
import java.awt.event.*;  
import java.lang.reflect.*;

import fieldling.mediaplayer.PanelPlayer;
import fieldling.mediaplayer.PanelPlayerException;
import fieldling.util.JdkVersionHacks;
import fieldling.quilldriver.TextHighlightPlayer;
import fieldling.quilldriver.XMLView;
import fieldling.quilldriver.XMLEditor;
import fieldling.quilldriver.XMLUtilities;
import fieldling.util.SimpleSpinner;
import fieldling.util.I18n;
import fieldling.util.StatusBar;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.DOMOutputter;
import org.jdom.transform.JDOMSource;
import org.jdom.transform.JDOMResult;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.Keymap;
import java.util.List;
import java.util.Timer;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

public class QD extends JDesktopPane {
	//TIBETAN-SPECIFIC
	protected org.thdl.tib.input.JskadKeyboard activeKeyboard = null;
	protected PanelPlayer player = null;
	protected XMLEditor editor = null;
	protected JInternalFrame videoFrame = null;
	protected JInternalFrame textFrame = null;
	protected JInternalFrame actionFrame = null;
	protected Map keyActions = null, taskActions = null;
	protected ResourceBundle messages;
	protected TimeCodeModel tcp = null;
	protected Hashtable actions;
	protected Properties config; //xpath based properties
	protected Properties textConfig; //unchangeable properties
	protected JMenu[] configMenus;
	protected XMLView view;
	protected TextHighlightPlayer hp;
	protected DocumentBuilder docBuilder;
	protected File transcriptFile = null;
	protected XMLTagInfo tagInfo = null;
	protected Configuration configuration = null;
	protected String configURL, newURL, editURL, dtdURL, rootElement;
	
	public QD(Configuration configuration) {
		setupGlobals();
		setupGUI();
		configure(configuration);
	}
	public QD() {
		setupGlobals();
		setupGUI();
	}

	private void setupGlobals() {
		messages = I18n.getResourceBundle();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			docBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
	}	
	private void setupGUI() {
 		setBackground(new JFrame().getBackground());
		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	
		//(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
		videoFrame = new JInternalFrame(null, false, false, false, false);
		videoFrame.setVisible(true);
		videoFrame.setLocation(0,0);
		videoFrame.setSize(0,0);
		add(videoFrame, JLayeredPane.POPUP_LAYER);
		invalidate();
		validate();
		repaint();
	
		textFrame = new JInternalFrame(null, false, false, false, true);
		textFrame.setVisible(true);
		textFrame.setLocation(0,0);
		textFrame.setSize(0,0);
		add(textFrame, JLayeredPane.DEFAULT_LAYER);
		invalidate();
		validate();
		repaint();
	
	
		actionFrame = new JInternalFrame(null, false, false, false, true);
		actionFrame.setVisible(true);
		actionFrame.setLocation(0,0);
		actionFrame.setSize(0,0);
		add(actionFrame, JLayeredPane.DEFAULT_LAYER);
		invalidate();
		validate();
		repaint();
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
				Dimension d = videoFrame.getSize();
				if (d.width == 0 && d.height == 0)
					videoFrame.setSize(getSize().width / 2, 0);
				textFrame.setLocation(videoFrame.getSize().width, 0);
				textFrame.setSize(getSize().width - videoFrame.getSize().width, getSize().height);
				actionFrame.setLocation(0, videoFrame.getSize().height);
				actionFrame.setSize(videoFrame.getSize().width, getSize().height - videoFrame.getSize().height);
			}
		});
	}
		
	private void startTimer() {
		final java.util.Timer timer = new java.util.Timer(true);
		timer.schedule(new TimerTask() {
			public void run()
			{
				if (player.isInitialized())
				{
					timer.cancel();
					final TimeCodeView tcv = new TimeCodeView(player, tcp);
					actionFrame.setContentPane(tcv);
					Timer checkTimeTimer = new Timer(true);
					checkTimeTimer.scheduleAtFixedRate(new TimerTask() {
						public void run() {
							tcv.setCurrentTime(player.getCurrentTime());
						}
					}, 0, 50);
					actionFrame.pack();
					invalidate();
					validate();
					repaint();
					videoFrame.setContentPane(player);
					videoFrame.pack();
					videoFrame.setMaximumSize(videoFrame.getSize());
					invalidate();
					validate();
					repaint();
					textFrame.setLocation(videoFrame.getSize().width, 0);
					textFrame.setSize(getSize().width - videoFrame.getSize().width, getSize().height);
					invalidate();
					validate();
					repaint();
					actionFrame.setLocation(0, videoFrame.getSize().height);
					actionFrame.setSize(videoFrame.getSize().width, getSize().height - videoFrame.getSize().height);
					invalidate();
					validate();
					repaint();
				}
			}}, 0, 50);
	}

	private void createActionTable(JTextComponent textComponent) {
	    actions = new Hashtable();
	    Action[] actionsArray = textComponent.getActions();
	    for (int i = 0; i < actionsArray.length; i++) {
		Action a = actionsArray[i];
		actions.put(a.getValue(Action.NAME), a);
	    }
	}    
	
	private Action getActionByName(String name) {
	    return (Action)(actions.get(name));
	}

	
	class TimeCodeView extends JPanel implements TimeCodeModelListener {
		TimeCodeModel tcm;
		JTextField currentTimeField;
		SimpleSpinner startSpinner, stopSpinner;
		int currentTime=-1, startTime=-1, stopTime=-1;
		final int TEXT_WIDTH, TEXT_HEIGHT;
		
		TimeCodeView(final PanelPlayer player, TimeCodeModel tcm) {
			this.tcm = tcm;
			
			JLabel clockLabel = new JLabel(new ImageIcon(QD.class.getResource("clock.gif")));
			JButton inButton = new JButton(new ImageIcon(QD.class.getResource("right-arrow.jpg")));
			JButton outButton = new JButton(new ImageIcon(QD.class.getResource("left-arrow.jpg")));
			JButton playButton = new JButton(new ImageIcon(QD.class.getResource("play.gif")));
			
			inButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int t = player.getCurrentTime();
					if (t != -1) setStartTime(t);
				}
			});
			outButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int t = player.getCurrentTime();
					if (t != -1) {
						setStopTime(t);
						try {
							player.cmd_stop();
						} catch (PanelPlayerException smpe) {
							smpe.printStackTrace();
						}
					}
				}
			});
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (stopTime > startTime && startTime > -1) {
						try {
							//automatic highlighting & scrolling interferes with time-coding
							//player.cancelAnnotationTimer();
							player.setAutoScrolling(false);
							player.cmd_playSegment(new Integer(startTime), new Integer(stopTime));
						} catch (PanelPlayerException smpe) {
							smpe.printStackTrace();
						}
					}
				}
			});
			TEXT_WIDTH = 60;
			TEXT_HEIGHT = inButton.getPreferredSize().height / 2;
			
			currentTimeField = new JTextField();
			currentTimeField.setEditable(false);
			currentTimeField.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
			
			startSpinner = new fieldling.util.SimpleSpinner();
			stopSpinner = new fieldling.util.SimpleSpinner();
			startSpinner.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
			stopSpinner.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
			
			
			
			setCurrentTime(0);
			setStartTime(0);
			setStopTime(0);
			setLayout(new BorderLayout());
			JPanel jp_top = new JPanel(new FlowLayout());
			JPanel jp_center = new JPanel(new FlowLayout());
			jp_top.add(clockLabel);
			jp_top.add(currentTimeField);
			jp_center.add(inButton);
			jp_center.add(startSpinner);
			jp_center.add(playButton);
			jp_center.add(stopSpinner);
			jp_center.add(outButton);
			add("North", jp_top);
			add("Center", jp_center);
			tcm.addTimeCodeModelListener(this);
		}
		void changeTimeCodesInXML() {
			if (taskActions != null) {
				AbstractAction action = (AbstractAction)taskActions.get("qd.insertTimes");
				if (action != null && tcm.getCurrentNode() != null)
					action.actionPerformed(new ActionEvent(TimeCodeView.this, 0, "no.command"));
			}
		}
		String getTimeAsString(int t) {
			return String.valueOf((new Integer(t)).floatValue() / 1000);
		}
		void setCurrentTime(int t) {
			if (t != currentTime) {
				currentTime = t;
				currentTimeField.setText(String.valueOf(new Integer(t)));
				//currentTimeLabel.setText(getTimeAsString(currentTime));
			}
		}
		public void setStartTime(int t) {
			if (t != startTime) {
				startTime = t;
				startSpinner.setValue(new Integer(t));
				changeTimeCodesInXML();
				//startTimeLabel.setText(getTimeAsString(startTime));
			}
		}
		public void setStopTime(int t) {
			if (t != stopTime) {
				stopTime = t;
				stopSpinner.setValue(new Integer(t));
				changeTimeCodesInXML();
				//stopTimeLabel.setText(getTimeAsString(stopTime));
			}
		}
	}
	class TimeCodeModel {
		private EventListenerList listenerList; 
		int t1, t2; //start and stop times in milliseconds
		private TextHighlightPlayer thp;
		private Object currentNode = null;
		
		TimeCodeModel(TextHighlightPlayer thp) {
			listenerList = new EventListenerList();
			this.thp = thp;
			t1 = 0;
			t2 = 0;
		} 
		public void addTimeCodeModelListener(TimeCodeModelListener tcml) {
			listenerList.add(TimeCodeModelListener.class, tcml);
		}
		public void removeTimeCodeModelListener(TimeCodeModelListener tcml) {
			listenerList.remove(TimeCodeModelListener.class, tcml);
		}
		public void removeAllTimeCodeModelListeners() {
			listenerList = new EventListenerList();
		}
		public Integer getInTime() {
			return new Integer(t1);
		}
		public Integer getOutTime() {
			return new Integer(t2);
		}
		public Object getCurrentNode() {
			return currentNode;
		}
		public void setInTime(int k) {
			t1 = k;
			//see javadocs on EventListenerList for how following array is structured
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==TimeCodeModelListener.class)
					((TimeCodeModelListener)listeners[i+1]).setStartTime(t1);
			}
		}
		public void setOutTime(int k) {
			t2 = k;
			//see javadocs on EventListenerList for how following array is structured
			Object[] listeners = listenerList.getListenerList();
			for (int i = listeners.length-2; i>=0; i-=2) {
				if (listeners[i]==TimeCodeModelListener.class)
					((TimeCodeModelListener)listeners[i+1]).setStopTime(t2);
			}
		}
		public void setTimes(Object node) {
			currentNode = node;
			Object playableparent = XMLUtilities.findSingleNode(node, config.getProperty("qd.nearestplayableparent"));
			if (playableparent == null) {
				setInTime(-1);
				setOutTime(-1);
				thp.unhighlightAll();
				return;
			} else {
				String t1 = XMLUtilities.getTextForNode(XMLUtilities.findSingleNode(playableparent, config.getProperty("qd.nodebegins")));
				String t2 = XMLUtilities.getTextForNode(XMLUtilities.findSingleNode(playableparent, config.getProperty("qd.nodeends")));
				float f1 = new Float(t1).floatValue()*1000;
				float f2 = new Float(t2).floatValue()*1000;
				setInTime(new Float(f1).intValue());
				setOutTime(new Float(f2).intValue());
				thp.unhighlightAll();
				thp.highlight(editor.getStartOffsetForNode(playableparent), editor.getEndOffsetForNode(playableparent));
			}
		}
	}
	
	public void setMediaPlayer(PanelPlayer smp) {
		if (smp == null)
			player = null;
		else if (player == null || !player.equals(smp)) {
			player = smp;
			player.setParentContainer(QD.this);
		}
	}
	public PanelPlayer getMediaPlayer() {
		return player;
	}

	public boolean saveTranscript() {
		if (transcriptFile == null)
			return true;
		
		XMLOutputter xmlOut = new XMLOutputter();
		try {
			FileOutputStream fous = new FileOutputStream(transcriptFile);
			xmlOut.output(editor.getXMLDocument(), fous);
			return true;
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			//ThdlDebug.noteIffyCode();
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			//ThdlDebug.noteIffyCode();
			return false;
		}
	}
	
	public boolean newTranscript(File file, String mediaURL) {
			try {
				if (configuration == null) return false;
				final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(configuration.getNewURL().openStream()));
				transformer.setParameter("qd.mediaURL", mediaURL);
				javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
				javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
				org.w3c.dom.Document d = db.newDocument();
				d.appendChild(d.createElement("ROOT-ELEMENT"));
				transformer.transform(new javax.xml.transform.dom.DOMSource(d), new StreamResult(file)); //no args constructor implies default tree with empty root
				transformer.clearParameters();
				if (loadTranscript(file)) {	
					if (dtdURL != null && rootElement != null) editor.getXMLDocument().setDocType(new DocType(rootElement,dtdURL));
					return true;
				}
			} catch (TransformerException tre) {
				tre.printStackTrace();
			} catch (javax.xml.parsers.ParserConfigurationException pce) {
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			return false;
	}
	
	public boolean loadTranscript(File file) {
			if (!file.exists())
				return false;
		
			if (player == null) {
				JOptionPane.showConfirmDialog(QD.this, messages.getString("SupportedMediaError"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				return false;
			}
				
			try {
				//FIXME?? don't validate, since user could be offline and dtd online
				final SAXBuilder builder = new SAXBuilder(false); 
				
				//TIBETAN-SPECIFIC
				final JTextPane t = new org.thdl.tib.input.DuffPane();
				//IF NOT TIBETAN, THEN USE:
				//final JTextPane t = new JTextPane();

				editor = new XMLEditor(builder.build(file), t, tagInfo);

				Keymap keymap = editor.getTextPane().addKeymap("Config-Bindings", editor.getTextPane().getKeymap());
				Set keys = keyActions.keySet();
				Iterator keyIter = keys.iterator();
				while (keyIter.hasNext()) {
					KeyStroke key = (KeyStroke)keyIter.next(); 
					Action action = (Action)keyActions.get(key);
					keymap.addActionForKeyStroke(key, action);
				}
				editor.getTextPane().setKeymap(keymap);
				
				view = new XMLView(editor, editor.getXMLDocument(), config.getProperty("qd.timealignednodes"), config.getProperty("qd.nodebegins"), config.getProperty("qd.nodeends"));
				hp = new TextHighlightPlayer(view, Color.cyan);
				tcp = new TimeCodeModel(hp);
				
				if (player.getMediaURL() != null) {
					try {
						player.cmd_stop();
						player.destroy();
					} catch (PanelPlayerException smpe) {
						smpe.printStackTrace();
						return false;
					}
					videoFrame.getContentPane().remove(player);
					videoFrame.getContentPane().invalidate();
					videoFrame.getContentPane().validate();
					videoFrame.getContentPane().repaint();
					videoFrame.setSize(new Dimension(QD.this.getSize().width / 2, 0));
					actionFrame.setLocation(0,0);
					actionFrame.setSize(new Dimension(actionFrame.getSize().width, QD.this.getSize().height));
				}

				Object mediaURL = XMLUtilities.findSingleNode(editor.getXMLDocument(), config.getProperty("qd.mediaurl"));
				String value = XMLUtilities.getTextForNode(mediaURL);
				boolean nomedia = true;
				if (value != null) {
					try {
						player.loadMovie(new URL(value));
						nomedia = false;
					} catch (MalformedURLException murle) {} //do nothing 
				}
				if (nomedia) { //can't find movie: open new movie
					JFileChooser fc = new JFileChooser();
					if (fc.showDialog(QD.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
						File mediaFile = fc.getSelectedFile();
						try {
							player.loadMovie(mediaFile.toURL());
							nomedia = false;
						} catch (MalformedURLException murle) {} //do nothing
					}
				}
				if (nomedia) { //user did not open a valid movie; therefore transcript will not be opened
					transcriptFile = null;
					return false;
				}
				player.addAnnotationPlayer(hp);
				player.initForSavant(convertTimesForPanelPlayer(view.getT1s()), convertTimesForPanelPlayer(view.getT2s()), view.getIDs());
				videoFrame.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						System.out.println("mouse clicked on player");
						//PanelPlayer smp = (PanelPlayer)e.getSource();
						videoFrame.requestFocus();
					}
				});
				startTimer();
				
				JComponent c = (JComponent)textFrame.getContentPane();
				c.setLayout(new BorderLayout());
				c.add("Center", hp);
				textFrame.setSize(textFrame.getSize().width, getSize().height);
				textFrame.invalidate();
				textFrame.validate();
				textFrame.repaint();
				
				editor.addNodeEditListener(new XMLEditor.NodeEditListener() {
					public void nodeEditPerformed(XMLEditor.NodeEditEvent ned) {
						if (ned instanceof XMLEditor.StartEditEvent) { 
							//stop the automatic highlighting and scrolling
							//since it would interfere with editing.
							//player.cancelAnnotationTimer();
							player.setAutoScrolling(false);
							if (tcp != null) tcp.setTimes(ned.getNode());
						} else if (ned instanceof XMLEditor.EndEditEvent) {
							//turn auto-scrolling and highlighting back on
							XMLEditor.EndEditEvent eee = (XMLEditor.EndEditEvent)ned;
							if (eee.hasBeenEdited()) {
								view.refresh();
								hp.refresh();
							}
							player.setAutoScrolling(true);
						} else if (ned instanceof XMLEditor.CantEditEvent) {
							//if this node can't be edited, maybe it can be played!
							System.out.println(ned.getNode().toString());
							Object node = ned.getNode();
							if (node != null) {
								editor.getTextPane().setCaretPosition(editor.getStartOffsetForNode(node));
								playNode(node);
								if (tcp != null) tcp.setTimes(node);
							}
						}
					}
				});
				editor.setEditabilityTracker(true);
				transcriptFile = file;
				
				//TIBETAN-SPECIFIC!!
				if (activeKeyboard != null) changeKeyboard(activeKeyboard); //this means that keyboard was changed before constructing a DuffPane
				
				return true;
			} catch (JDOMException jdome) {
				jdome.printStackTrace();
				transcriptFile = null;
				return false;
			} catch (PanelPlayerException smpe) {
				smpe.printStackTrace();
				transcriptFile = null;
				return false;
			}
	}

	public void playNode(Object node) {
		Object playableparent = XMLUtilities.findSingleNode(node, config.getProperty("qd.nearestplayableparent"));
		if (playableparent == null) return;
		String nodeid = String.valueOf(playableparent.hashCode());
		if (player.cmd_isID(nodeid)) {
			editor.getTextPane().transferFocus();
			player.cmd_playS(nodeid);
		}
	}
	public void setEditable(boolean bool) {
	}

    /* FIXME: needs better error handling */
    /** Creates an object via reflection.
     *  @return nonnull on success, null on error */
	public static Object createObject(Constructor constructor, Object[] arguments) {
		System.out.println ("Constructor: " + constructor.toString());
		Object object = null;
		try {
			object = constructor.newInstance(arguments);
			System.out.println ("Object: " + object.toString());
			return object;
		} catch (InstantiationException e) {
			System.out.println(e);
			//ThdlDebug.noteIffyCode();
		} catch (IllegalAccessException e) {
			System.out.println(e);
			//ThdlDebug.noteIffyCode();
		} catch (IllegalArgumentException e) {
			System.out.println(e);
			//ThdlDebug.noteIffyCode();
		} catch (InvocationTargetException e) {
			System.out.println(e);
			System.out.println(e.getTargetException());
			//ThdlDebug.noteIffyCode();
		}
		return object;
	}
	
  	public static String convertTimesForPanelPlayer(String s) {
		StringBuffer sBuff = new StringBuffer();
		StringTokenizer sTok = new StringTokenizer(s, ",");
		while (sTok.hasMoreTokens()) {
			sBuff.append(String.valueOf(new Float(Float.parseFloat(sTok.nextToken()) * 1000).intValue()));
			sBuff.append(',');
		}
		return sBuff.toString();
	}

	public void configure(Configuration configuration) {
		try {
			this.configuration = configuration;
			SAXBuilder builder = new SAXBuilder();
			Document cDoc = builder.build(configuration.getConfigURL());
			Element cRoot = cDoc.getRootElement();
			Iterator it;
			List tagOptions = cRoot.getChildren("tag");
			if (tagOptions.size() > 0) {
				tagInfo = new XMLTagInfo();
				it = tagOptions.iterator();
				while (it.hasNext()) {
					Element e = (Element)it.next();
					
					//TIBETAN-SPECIFIC
					tagInfo.addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")), 
						new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
						new Boolean(e.getAttributeValue("tibetan")));
					List atts = e.getChildren("attribute");
					Iterator it2 = atts.iterator();
					while (it2.hasNext()) {
						Element eAtt = (Element)it2.next();
						tagInfo.addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
							new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"));
					}
				}
			}	
			List parameters = cRoot.getChildren("parameter");
			textConfig = new Properties();
			config = new Properties();
			it = parameters.iterator();
			while (it.hasNext()) {
				Element e = (Element)it.next();
				String type = e.getAttributeValue("type");
				if (type == null || type.equals("xpath"))
					config.put(e.getAttributeValue("name"), e.getAttributeValue("val"));
				else if (type.equals("text"))
					textConfig.put(e.getAttributeValue("name"), e.getAttributeValue("val"));
			}
			rootElement = textConfig.getProperty("qd.root.element");
			List navigations = cRoot.getChildren("navigation");
			final String[] navigXPath = new String[navigations.size()];
			configMenus = new JMenu[2];
			configMenus[1] = new JMenu(messages.getString("View"));
			keyActions = new HashMap(); //maps keys to actions
			taskActions = new HashMap(); //maps task names to same actions
			it = navigations.iterator();
			while (it.hasNext()) {
				Element e = (Element)it.next();
				final JMenuItem mItem = new JMenuItem(e.getAttributeValue("name"));
				final String xpathExpression = e.getAttributeValue("val");
				final String command = e.getAttributeValue("command");
				mItem.setToolTipText(e.getChildTextNormalize("desc"));
				KeyStroke key = KeyStroke.getKeyStroke(e.getAttributeValue("keystroke"));
				final Action keyAction = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						if (xpathExpression != null) {
							editor.fireEndEditEvent();
							boolean keepSearching;
							JTextPane t = editor.getTextPane();
							int offset = t.getCaret().getMark();
							Object context = editor.getNodeForOffset(offset);
							System.out.println("xpath--"+String.valueOf(offset)+": "+context.toString());
							do {
								keepSearching = false;
								if (context != null) {
									Object moveTo = XMLUtilities.findSingleNode(context, xpathExpression);
									int newStartOffset = editor.getStartOffsetForNode(moveTo);
									if (newStartOffset > -1) {
										t.requestFocus();
										t.setCaretPosition(newStartOffset);
									} else {
										keepSearching = true; //search again
										context = moveTo;
									}
								}
							} while (keepSearching);
							if (command.equals("playNode")) {
								Object nearestParent = XMLUtilities.findSingleNode(editor.getNodeForOffset(t.getCaret().getMark()), config.getProperty("qd.nearestplayableparent"));
								playNode(nearestParent);
							}
						}
					}				
				};
				keyActions.put(key, keyAction);	//eventually to be registered with transcript's JTextPane
				mItem.setAccelerator(key);
				mItem.addActionListener(new ActionListener() { //so that keystrokes are valid even when transcript is not in focus
					public void actionPerformed(ActionEvent e) {
						keyAction.actionPerformed(e);
					}
				});
				configMenus[1].add(mItem);
			}
			configMenus[0] = new JMenu(messages.getString("Edit"));
			/*if (editURL != null)*/ try {
				final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(configuration.getEditURL().openStream()));				
				List transformations = cRoot.getChildren("transformation");
				it = transformations.iterator();
				while (it.hasNext()) {
					Element e = (Element)it.next();
					final DOMOutputter domOut = new DOMOutputter();
					final DOMBuilder jdomBuild = new DOMBuilder();
					final JMenuItem mItem = new JMenuItem(e.getAttributeValue("name"));
					final String tasks = e.getAttributeValue("tasks");
					final String nodeSelector = e.getAttributeValue("node");
					mItem.setToolTipText(e.getChildTextNormalize("desc"));
					KeyStroke key = KeyStroke.getKeyStroke(e.getAttributeValue("keystroke")); 
					mItem.setAccelerator(key);
					final Action keyAction = new AbstractAction() {
						public void actionPerformed(ActionEvent e) {
							try {
								editor.fireEndEditEvent();
								editor.setEditabilityTracker(false);
								int offset = editor.getTextPane().getCaret().getMark();
								Object context = editor.getNodeForOffset(offset);
								Object transformNode = XMLUtilities.findSingleNode(context, nodeSelector);
								if (!(transformNode instanceof Element)) return;
								Element jdomEl = (Element)transformNode;
								Element clone = (Element)jdomEl.clone();
								Element cloneOwner = new Element("CloneOwner");
								cloneOwner.addContent(clone);
								org.w3c.dom.Element transformElement = domOut.output((Element)cloneOwner);
								//String refreshID = XMLUtilities.getTextForNode(XMLUtilities.findSingleNode(context, config.getProperty("qd.refreshid")));
								Enumeration enum = config.propertyNames();
								while (enum.hasMoreElements()) {
									String key = (String)enum.nextElement();
									String val = config.getProperty(key);
									if (!key.startsWith("qd.")) {
										Object obj = XMLUtilities.findSingleNode(context, val);
										if (obj != null) {
											String id = XMLUtilities.getTextForNode(obj);
											transformer.setParameter(key, id);
											System.out.println("key="+key+" & id="+id);
										}
									}
								}
								enum = textConfig.propertyNames();
								while (enum.hasMoreElements()) {
									String key = (String)enum.nextElement();
									transformer.setParameter(key, textConfig.getProperty(key));
								}
								transformer.setParameter("qd.task", tasks);
								
								// THIS CODE HANDLED OLD TIME-CODING PANEL WHICH I AM TRYING TO GET RID OF
								float inSeconds = tcp.getInTime().floatValue() / 1000; //convert from milliseconds
								float outSeconds = tcp.getOutTime().floatValue() / 1000; //convert from milliseconds
								if (outSeconds >= inSeconds) { //time parameters will not be passed if out precedes in
									transformer.setParameter("qd.start", String.valueOf(inSeconds));
									transformer.setParameter("qd.end", String.valueOf(outSeconds));
									System.out.println("Start="+String.valueOf(inSeconds)+" & End="+String.valueOf(outSeconds));
								} else {
									transformer.setParameter("qd.start", "");
									transformer.setParameter("qd.end", "");
								}
								
								float currentSeconds = (new Integer(player.getCurrentTime())).floatValue() / 1000; //convert from milliseconds
								float endTime = (new Integer(player.getEndTime())).floatValue() / 1000; //convert from milliseconds
								String cS = String.valueOf(currentSeconds);
								String eT = String.valueOf(endTime);
								System.out.println("Current = " + cS + "\nEnd = " + eT + "\n\n");
								transformer.setParameter("qd.currentmediatime", String.valueOf(currentSeconds));
								transformer.setParameter("qd.mediaduration", String.valueOf(endTime));
								
								
								JDOMResult jdomResult = new JDOMResult();
								//DOMSource domSource = new DOMSource(docBuilder.newDocument());
								//domSource.getNode().appendChild(transformElement);
								//if (transformElement.getParentNode() == null)
								//	System.out.println("null parent");
								DOMSource domSource = new DOMSource(transformElement);
								//if (domSource == null)
								//	System.out.println("dom source is null");
								transformer.transform(domSource, jdomResult);
								Element transformedRoot = jdomResult.getDocument().getRootElement();
								List replaceNodeWith = transformedRoot.getContent();
								
								XMLOutputter xmlOut = new XMLOutputter();
								try {
									Element ee = (Element)cloneOwner;
									xmlOut.output(ee, System.out);
									xmlOut.output(jdomResult.getDocument(), System.out);
								} catch (IOException ioe) {ioe.printStackTrace();}
								
								int start = editor.getStartOffsetForNode(transformNode);
								int end = editor.getEndOffsetForNode(transformNode);
								StyledDocument tDoc = editor.getTextPane().getStyledDocument();
								AttributeSet attSet = tDoc.getCharacterElement(start).getAttributes();
								float indent = StyleConstants.getLeftIndent(attSet);
								try {
									tDoc.insertString(end, "\n", null);
									tDoc.remove(start, end-start);
									//tDoc.insertString(PUT A CARRIAGE RETURN HERE... FOR TEXT)
								} catch (BadLocationException ble) {
									ble.printStackTrace();
									return;
								}
								
								int insertPos = start;
								Element el = (Element)transformNode;
								if (el.isRootElement()) {
									Iterator replaceIter = replaceNodeWith.iterator();
									while (replaceIter.hasNext()) {
										Object o = replaceIter.next();
										if (o instanceof Element) {
											replaceIter.remove();
											Document d = el.getDocument();
											d.detachRootElement();
											Element er = (Element)o;
											d.setRootElement(er);
											editor.removeNode(er);
											insertPos = editor.renderElement(er, indent, insertPos);
											break;
										}
									}
								} else {
									List parentContent = el.getParent().getContent(); //this is a live list
									int elPos = parentContent.indexOf(el);
									parentContent.remove(elPos);
									editor.removeNode(transformNode);
									Iterator replaceIter = replaceNodeWith.iterator();
									while (replaceIter.hasNext()) {
										Object next = replaceIter.next();
										replaceIter.remove();
										if (next instanceof Element) {
											Element elem = (Element)next;
											insertPos = editor.renderElement(elem, indent, insertPos);
										} else if (next instanceof Text) {
											Text text = (Text)next;
											if (text.getTextTrim().length() > 0)
												insertPos = editor.renderText(text, indent, insertPos);
										}
										parentContent.add(elPos, next);
										elPos++;
									}
								}

								try {
									tDoc.remove(insertPos, 1); //removes extra dummy new line inserted above to protect indentation
									String s = tDoc.getText(insertPos-1, 2);
									if (s.charAt(1)=='\n') {
										if (s.charAt(0)=='\n') {
											tDoc.remove(insertPos-1, 1); //if two newlines, delete first
											AttributeSet attSet2 = tDoc.getCharacterElement(insertPos-2).getAttributes();
											tDoc.setCharacterAttributes(insertPos-1, 1, attSet2, false);
										} else {
											AttributeSet attSet2 = tDoc.getCharacterElement(insertPos-1).getAttributes();
											tDoc.setCharacterAttributes(insertPos, 1, attSet2, false);
										}
										System.out.println("carriage return detected");
									}
								} catch (BadLocationException ble) {
									ble.printStackTrace();
									return;
								}
								
								editor.fixOffsets();
								view.refresh();
								hp.refresh();
								player.initForSavant(convertTimesForPanelPlayer(view.getT1s()), convertTimesForPanelPlayer(view.getT2s()), view.getIDs());
								
								transformer.clearParameters();
								
								editor.setEditabilityTracker(true);
							} catch (TransformerException tre) {
								tre.printStackTrace();
							} catch (JDOMException jdome) {
								jdome.printStackTrace();
							}
						}
					};
					keyActions.put(key, keyAction);	//eventually to be registered with transcript's JTextPane
					taskActions.put(tasks, keyAction); //to be used to call actions by task name rather than by key press
					mItem.setAccelerator(key);
					mItem.addActionListener(new ActionListener() { //so that keystrokes are valid even when transcript is not in focus
						public void actionPerformed(ActionEvent e) {
							keyAction.actionPerformed(e);
						}
					});
					configMenus[0].add(mItem);
				}
			} catch (TransformerException tre) {
				tre.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} catch (JDOMException jdome) {
			jdome.printStackTrace();
		}
	}

	public JMenu[] getConfiguredMenus() {
		return configMenus;
	}
	
	//TIBETAN-SPECIFIC!!
	public void changeKeyboard(org.thdl.tib.input.JskadKeyboard kbd) {
		activeKeyboard = kbd;
		if (editor == null || !(editor.getTextPane() instanceof org.thdl.tib.input.DuffPane)) return;
		org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)editor.getTextPane();
		kbd.activate(dp);
	}
}



/*
public int findNextText(int startPos, StyledDocument sourceDoc, StyledDocument findDoc) {
	if (startPos<0 || startPos>sourceDoc.getLength()-1)
		return -1;

try {

	AttributeSet firstAttr = findDoc.getCharacterElement(0).getAttributes();
	String firstFontName = StyleConstants.getFontFamily(firstAttr);
	char firstSearchChar = findDoc.getText(0,1).charAt(0);

	boolean match = false;
	for (int i=startPos; i<sourceDoc.getLength()-findDoc.getLength(); i++) {
			AttributeSet attr = sourceDoc.getCharacterElement(i).getAttributes();
			String fontName = StyleConstants.getFontFamily(attr);
			char c = sourceDoc.getText(i,1).charAt(0);

			if (c == firstSearchChar && fontName.equals(firstFontName)) { //found first character, now check subsequents
				match = true;
				for (int k=1; k<findDoc.getLength() && match; k++) {
					if (findDoc.getText(k,1).charAt(0) == sourceDoc.getText(i+k,1).charAt(0)) {
	 					AttributeSet	attr2 = findDoc.getCharacterElement(k).getAttributes();
									attr = sourceDoc.getCharacterElement(i+k).getAttributes();
						if (!StyleConstants.getFontFamily(attr).equals(StyleConstants.getFontFamily(attr2)))
							match = false;
					} else
						match = false;
				}
				if (match)
					return i;
			}
	}
	} catch (BadLocationException ble) {
		ble.printStackTrace();
		//ThdlDebug.noteIffyCode();
	}

	return -1; //not found
}

public void findText() {
	//get input from user on what to search for
	JPanel p0 = new JPanel(new GridLayout(0,1));
	JPanel p2 = new JPanel();
	p2.add(new JLabel(messages.getString("FindWhat") + " "));
	if (findDoc == null) {
		sharedDP.setText("");
		findDoc = (TibetanDocument)sharedDP.getDocument();
	} else
		sharedDP.setDocument(findDoc);
	sharedDP.setPreferredSize(new Dimension(250,50));
	p2.add(sharedDP);
	p0.add(p2);

	if (JOptionPane.showConfirmDialog(textFrame, p0, messages.getString("FindHeading"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {

	try {
		TibetanDocument doc = (TibetanDocument)pane.getDocument();
		Position pos = doc.createPosition(pane.getCaretPosition());

		boolean startingOver = false;
		int i=pos.getOffset();
		while (true) {
			i = findNextText(i, doc, findDoc);
			if (i>0) { //found!! so highlight text and seek video
				if (startingOver && i>pos.getOffset()-1)
					break;
				pane.setSelectionStart(i);
				pane.setSelectionEnd(i+findDoc.getLength());
				if (JOptionPane.showInternalConfirmDialog(textFrame, messages.getString("FindNextYesNo"), null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.YES_OPTION)
					break;
				i++;
			}
			if (i<0 || i==doc.getLength() && pos.getOffset()>0) { //reached end of document - start over?
				if (startingOver || pos.getOffset()==0)
					break;
				if (JOptionPane.showInternalConfirmDialog(textFrame, messages.getString("StartFromBeginning"), null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.YES_OPTION)
					break;
				i=0;
				startingOver = true;
			}
		}
	} catch (BadLocationException ble) {
		ble.printStackTrace();
		//ThdlDebug.noteIffyCode();
	}

	}
}

public void replaceText() {
	//get input from user on what to search for
	JPanel p0 = new JPanel(new GridLayout(0,1));
	JPanel p2 = new JPanel();
	p2.add(new JLabel(messages.getString("FindWhat") + " "));
	if (findDoc == null) {
		sharedDP.setText("");
		findDoc = (TibetanDocument)sharedDP.getDocument();
	} else
		sharedDP.setDocument(findDoc);
	sharedDP.setPreferredSize(new Dimension(250,50));
	p2.add(sharedDP);
	JPanel p3 = new JPanel();
	p3.add(new JLabel(messages.getString("ReplaceWith") + " "));
	if (replaceDoc == null) {
		sharedDP2.setText("");
		replaceDoc = (TibetanDocument)sharedDP2.getDocument();
	} else
		sharedDP2.setDocument(replaceDoc);
	sharedDP2.setPreferredSize(new Dimension(250,50));
	p3.add(sharedDP2);
	
	p0.add(p2);
	p0.add(p3);

	if (JOptionPane.showConfirmDialog(textFrame, p0, messages.getString("FindReplaceHeading"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION) {

	try {
		java.util.List replaceDCs = new ArrayList();
		for (int k=0; k<replaceDoc.getLength(); k++)
			replaceDCs.add(new DuffCode(TibetanMachineWeb.getTMWFontNumber(StyleConstants.getFontFamily(replaceDoc.getCharacterElement(k).getAttributes())), replaceDoc.getText(k,1).charAt(0)));
		DuffData[] dd = TibTextUtils.convertGlyphs(replaceDCs);

		TibetanDocument doc = (TibetanDocument)pane.getDocument();
		Position pos = doc.createPosition(pane.getCaretPosition());

		String[] replaceOptions = new String[3];
		replaceOptions[0] = new String(messages.getString("Replace"));
		replaceOptions[1] = new String(messages.getString("ReplaceAll"));
		replaceOptions[2] = new String(messages.getString("NoReplace"));

		boolean startingOver = false;
		boolean replaceAll = false;
		int i=pos.getOffset();
		outer:
		while (true) {
			i = findNextText(i, doc, findDoc);
			if (i>0) { //found!! so highlight text and seek video
				if (startingOver && i>pos.getOffset()-1)
					break;
				if (replaceAll) {
					doc.remove(i,findDoc.getLength());
					doc.insertDuff(i,dd);
				} else {
					pane.setSelectionStart(i);
					pane.setSelectionEnd(i+findDoc.getLength());
					String choice = (String)JOptionPane.showInternalInputDialog(textFrame, messages.getString("ReplaceQ"), null, JOptionPane.PLAIN_MESSAGE, null, replaceOptions, replaceOptions[0]);
					if (choice == null) //cancel, so stop searching
						break outer;
					if (choice.equals(replaceOptions[0])) { //replace
						doc.remove(i,findDoc.getLength());
						doc.insertDuff(i,dd);
					}
					else if (choice.equals(replaceOptions[1])) { //replace all
						doc.remove(i,findDoc.getLength());
						doc.insertDuff(i,dd);
						replaceAll = true;
					}
				}
				i++;
			}
			if (i<0 || i==doc.getLength() && pos.getOffset()>0) { //reached end of document - start over?
				if (startingOver || pos.getOffset()==0)
					break;
				if (JOptionPane.showInternalConfirmDialog(textFrame, messages.getString("StartFromBeginning"), null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.YES_OPTION)
					break;
				i=0;
				startingOver = true;
			}
		}
	} catch (BadLocationException ble) {
		ble.printStackTrace();
		//ThdlDebug.noteIffyCode();
	}

	}
}
*/
