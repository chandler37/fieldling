/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Edward Garrettc
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ***** END LICENSE BLOCK ***** */

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
import fieldling.util.SimpleSpinnerListener;
import fieldling.util.I18n;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.Namespace;
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
    protected static Color hColor = Color.cyan;
    
	@TIBETAN@protected org.thdl.tib.input.JskadKeyboard activeKeyboard = null;
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
    protected Namespace[] namespaces;
	protected JMenu[] configMenus;
	protected XMLView view;
	protected TextHighlightPlayer hp;
	protected DocumentBuilder docBuilder;
	protected File transcriptFile = null;
	protected XMLTagInfo currentTagInfo = null;
	protected Configuration configuration = null;
	protected String configURL, newURL, editURL, dtdURL, rootElement;
	public Timer checkTimeTimer = null;
	protected Transformer transformer = null; //this has no place here
    protected PreferenceManager prefmngr;

	public QD(Configuration configuration, PreferenceManager prefs) {
        prefmngr = prefs;
		setupGlobals();
		setupGUI();
		configure(configuration);
	}
	public QD(PreferenceManager prefs) {
        prefmngr = prefs;
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
				textFrame.setLocation(0,0);
				textFrame.setSize(getSize().width - videoFrame.getSize().width, getSize().height);
				videoFrame.setLocation(textFrame.getSize().width, 0);
				actionFrame.setLocation(textFrame.getSize().width, videoFrame.getSize().height);
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
					checkTimeTimer = new Timer(true);
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
					videoFrame.setLocation(getSize().width - videoFrame.getSize().width, 0);
					invalidate();
					validate();
					repaint();
					textFrame.setLocation(0, 0);
					textFrame.setSize(getSize().width - videoFrame.getSize().width, getSize().height);
					invalidate();
					validate();
					repaint();
					actionFrame.setLocation(textFrame.getSize().width, videoFrame.getSize().height);
					actionFrame.setSize(videoFrame.getSize().width, getSize().height - videoFrame.getSize().height);
					invalidate();
					validate();
					repaint();
System.out.println("DURATION = " + String.valueOf(player.getEndTime()));
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


	class TimeCodeView extends JPanel implements TimeCodeModelListener, SimpleSpinnerListener {
		TimeCodeModel tcm;
		JTextField currentTimeField;
		SimpleSpinner startSpinner, stopSpinner;
		long currentTime=-1, startTime=-1, stopTime=-1;
		//int currentTime=-1, startTime=-1, stopTime=-1;
		final int TEXT_WIDTH, TEXT_HEIGHT;

		TimeCodeView(final PanelPlayer player, TimeCodeModel time_model) {
			tcm = time_model;

			JLabel clockLabel = new JLabel(new ImageIcon(QD.class.getResource("clock.gif")));
			JButton inButton = new JButton(new ImageIcon(QD.class.getResource("right-arrow.jpg")));
			JButton outButton = new JButton(new ImageIcon(QD.class.getResource("left-arrow.jpg")));
			JButton playButton = new JButton(new ImageIcon(QD.class.getResource("play.gif")));
			inButton.setBorder(null);
			outButton.setBorder(null);
			playButton.setBorder(null);
			inButton.setPreferredSize(new Dimension(inButton.getIcon().getIconWidth(), inButton.getIcon().getIconHeight()));
			outButton.setPreferredSize(new Dimension(outButton.getIcon().getIconWidth(), outButton.getIcon().getIconHeight()));
			playButton.setPreferredSize(new Dimension(playButton.getIcon().getIconWidth(), playButton.getIcon().getIconHeight()));

			inButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					long t = player.getCurrentTime();
					//int t = player.getCurrentTime();
					if (t != -1) {
						setStartTime(t);
						tcm.setTimeCodes(t, stopTime, tcm.getCurrentNode());
					}
				}
			});
			outButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					long t = player.getCurrentTime();
					//int t = player.getCurrentTime();
					if (t != -1) {
						setStopTime(t);
						tcm.setTimeCodes(startTime, t, tcm.getCurrentNode());
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
							player.cmd_playSegment(new Long(startTime), new Long(stopTime));
							//player.cmd_playSegment(new Integer(startTime), new Integer(stopTime));
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
			startSpinner.addSimpleSpinnerListener(this);
			stopSpinner.addSimpleSpinnerListener(this);
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
		public void valueChanged(ChangeEvent e) {
			Object obj = e.getSource();
			if (obj == startSpinner) startTime = startSpinner.getValue().longValue();
			else if (obj == stopSpinner) stopTime = stopSpinner.getValue().longValue();
			//if (obj == startSpinner) startTime = startSpinner.getValue().intValue();
			//else if (obj == stopSpinner) stopTime = stopSpinner.getValue().intValue();
			tcm.setTimeCodes(startTime, stopTime, tcm.getCurrentNode());
		}
		//void setCurrentTime(int t) {
		void setCurrentTime(long t) {
			if (t != currentTime) {
				currentTime = t;
				currentTimeField.setText(String.valueOf(new Long(t)));
				//currentTimeField.setText(String.valueOf(new Integer(t)));
			}
		}
		//public void setStartTime(int t) {
		public void setStartTime(long t) {
			if (t != startTime) {
				startTime = t;
				startSpinner.setValue(new Long(t));
				//startSpinner.setValue(new Integer(t));
			}
		}
		//public void setStopTime(int t) {
		public void setStopTime(long t) {
			if (t != stopTime) {
				stopTime = t;
				stopSpinner.setValue(new Long(t));
				//stopSpinner.setValue(new Integer(t));
			}
		}
	}
	class TimeCodeModel {
		private EventListenerList listenerList;
		long t1, t2; //start and stop times in milliseconds
		//int t1, t2; //start and stop times in milliseconds
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
		//public Integer getInTime() {
		//	return new Integer(t1);
		public Long getInTime() {
			return new Long(t1);
		}
		//public Integer getOutTime() {
		//	return new Integer(t2);
		public Long getOutTime() {
			return new Long(t2);
		}
		public Object getCurrentNode() {
			return currentNode;
		}
		private void changeTimeCodesInXML() {
			if (taskActions != null) {
				AbstractAction action = (AbstractAction)taskActions.get("qd.insertTimes");
				if (action != null && getCurrentNode() != null)
					action.actionPerformed(new ActionEvent(TimeCodeModel.this, 0, "no.command"));
			}
		}
		//public void setTimeCodes(int t1, int t2, Object node) {
		public void setTimeCodes(long t1, long t2, Object node) {
			Object oldNode = currentNode;
			currentNode = node;
			if (!(this.t1 == t1 && this.t2 == t2)) {
				this.t1 = t1;
				this.t2 = t2;
				//see javadocs on EventListenerList for how following array is structured
				Object[] listeners = listenerList.getListenerList();
				for (int i = listeners.length-2; i>=0; i-=2) {
					if (listeners[i]==TimeCodeModelListener.class) {
						((TimeCodeModelListener)listeners[i+1]).setStartTime(t1);
						((TimeCodeModelListener)listeners[i+1]).setStopTime(t2);
					}
				}
				if (currentNode == oldNode) {
					if ((t2 >= t1 && t1 > -1) || (t1 > -1 && t2 == -1)) changeTimeCodesInXML(); //update the XML file
				}
			}
		}
		public void setNode(Object node) {
			Object playableparent = XMLUtilities.selectSingleJDOMNode(node, config.getProperty("qd.nearestplayableparent"), namespaces);
			if (playableparent == null) {
				setTimeCodes(-1, -1, node);
				thp.unhighlightAll();
				return;
			} else {
				String t1 = XMLUtilities.getTextForJDOMNode(XMLUtilities.selectSingleJDOMNode(playableparent, config.getProperty("qd.nodebegins"), namespaces));
				String t2 = XMLUtilities.getTextForJDOMNode(XMLUtilities.selectSingleJDOMNode(playableparent, config.getProperty("qd.nodeends"), namespaces));
				float f1, f2;
				if (t1 == null) f1 = -1;
				else f1 = new Float(t1).floatValue()*1000;
				if (t2 == null) f2 = -1;
				else f2 = new Float(t2).floatValue()*1000;
				setTimeCodes(new Float(f1).longValue(), new Float(f2).longValue(), node);
				//setTimeCodes(new Float(f1).intValue(), new Float(f2).intValue(), node);
				thp.unhighlightAll();
				//FIXME FIXME FIXME should not be making reference to player in this class
				//need better communication about highlights between PanelPlayer and TextHighlightPlayer
				//if (!thp.getView().getTextComponent().hasFocus()) //only highlight if line is NOT selected
				player.fireStartAnnotation(String.valueOf(playableparent.hashCode()));
				//thp.highlight(String.valueOf(playableparent.hashCode()));
				//thp.highlight(editor.getStartOffsetForNode(playableparent), editor.getEndOffsetForNode(playableparent));
			}
		}
	}
	public void setMediaPlayer(PanelPlayer smp) {
		if (transcriptFile != null) return;

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
	public void removeContent() {
		if (transcriptFile == null) return;
		try { //dispose of media player
			if (checkTimeTimer != null) checkTimeTimer.cancel();
			getMediaPlayer().destroy();
		} catch (PanelPlayerException ppe) {
			ppe.printStackTrace();
		}
		transcriptFile = null;
		removeAll(); //remove sub-windows
		editor = null;
		setupGUI(); //set up GUI for another transcript
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

				@TIBETAN@final JTextPane t = new org.thdl.tib.input.DuffPane();
				@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)t;
				@TIBETAN@dp.setByUserTibetanFontSize(PreferenceManager.tibetan_font_size);
				@TIBETAN@dp.setByUserRomanAttributeSet(PreferenceManager.font_face, PreferenceManager.font_size);

				@UNICODE@final JTextPane t = new JTextPane();
				@UNICODE@t.setFont(new Font(PreferenceManager.font_face, java.awt.Font.PLAIN, PreferenceManager.font_size));

				editor = new XMLEditor(builder.build(file), t, currentTagInfo);

				Keymap keymap = editor.getTextPane().addKeymap("Config-Bindings", editor.getTextPane().getKeymap());
				Set keys = keyActions.keySet();
				Iterator keyIter = keys.iterator();
				while (keyIter.hasNext()) {
					KeyStroke key = (KeyStroke)keyIter.next();
					Action action = (Action)keyActions.get(key);
					keymap.addActionForKeyStroke(key, action);
				}
				editor.getTextPane().setKeymap(keymap);

                
				view = new XMLView(editor, editor.getXMLDocument(), config.getProperty("qd.timealignednodes"), config.getProperty("qd.nodebegins"), config.getProperty("qd.nodeends"), namespaces);
                
                try {
                    hColor = Color.decode("0x"+PreferenceManager.highlight_color);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
				hp = new TextHighlightPlayer(view, hColor, prefmngr.highlight_position);
                
                for (int ok=0; ok<namespaces.length; ok++)
                    System.out.println(namespaces[ok].toString());

				//FIXME: otherwise JScrollPane's scrollbar will intercept key codes like
				//Ctrl-Page_Down and so on... surely there is a better way to do this....
				JScrollBar sb = hp.getScroller().getVerticalScrollBar();
				if (sb != null) {
					keyIter = keys.iterator();
					while (keyIter.hasNext()) {
						KeyStroke key = (KeyStroke)keyIter.next();
						Action action = (Action)keyActions.get(key);
						sb.getInputMap().put(key, action);
						sb.getActionMap().put(action, action);
					}
				}

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

				String value;
				if (config.getProperty("qd.mediaurl") == null) value = null;
				else {
					Object mediaURL = XMLUtilities.selectSingleJDOMNode(editor.getXMLDocument(), config.getProperty("qd.mediaurl"), namespaces);
					value = XMLUtilities.getTextForJDOMNode(mediaURL);
				}
				boolean nomedia = true;
				if (value != null) {
					try {
						if (value.startsWith("file:")) { //it's a file, so try to load
							File mediaFile = new File(value.substring(5));
							if (mediaFile.exists()) { //open the actual file
								player.loadMovie(mediaFile.toURL());
								nomedia = false;
							} else if (PreferenceManager.media_directory != null) { //otherwise try default media directory
								String mediaName = value.substring(value.lastIndexOf(QDShell.FILE_SEPARATOR)+1);
								mediaFile = new File(PreferenceManager.media_directory, mediaName);
								if (mediaFile.exists()) {
									player.loadMovie(mediaFile.toURL());
									nomedia = false;
									//INSERT VIDEO NAME INTO DATA FILE
								}
							}
						} else {
							player.loadMovie(new URL(value));
							nomedia = false;
						}
					} catch (MalformedURLException murle) {murle.printStackTrace();} //do nothing
				}
				if (nomedia) { //can't find movie: open new movie
					JFileChooser fc = new JFileChooser(PreferenceManager.media_directory);
					if (fc.showDialog(QD.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
						File mediaFile = fc.getSelectedFile();
						try {
							player.loadMovie(mediaFile.toURL());
							String mediaString = mediaFile.getAbsolutePath();
							PreferenceManager.media_directory = mediaString.substring(0, mediaString.lastIndexOf(QDShell.FILE_SEPARATOR)+1);
							nomedia = false;
							//INSERT VIDEO NAME INTO DATA FILE
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

				//give text frame title of video/transcript
				if (config.getProperty("qd.title") != null) {
					Object obj = XMLUtilities.selectSingleJDOMNode(editor.getXMLDocument().getRootElement(), config.getProperty("qd.title"), namespaces);
					textFrame.setTitle(XMLUtilities.getTextForJDOMNode(obj));
				}

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
							if (tcp != null) tcp.setNode(ned.getNode());
						} else if (ned instanceof XMLEditor.EndEditEvent) {
							//turn auto-scrolling and highlighting back on
							XMLEditor.EndEditEvent eee = (XMLEditor.EndEditEvent)ned;
							if (eee.hasBeenEdited()) hp.refresh();
							player.setAutoScrolling(true);
						} else if (ned instanceof XMLEditor.CantEditEvent) {
							//if this node can't be edited, maybe it can be played!
							Object node = ned.getNode();
							if (node != null) {
								editor.getTextPane().setCaretPosition(editor.getStartOffsetForNode(node));
								if (tcp != null) tcp.setNode(node);
								player.setAutoScrolling(true);
								playNode(node);
							}
						}
					}
				});
				editor.getTextPane().addMouseMotionListener(new MouseMotionAdapter() {
					/* Turns off highlight if mouse is pressed, since
					user is most likely selecting a block of text */
					public void mouseDragged(MouseEvent e) {
						//hp.unhighlightAll();
					}
				});

				if (configuration.getEditURL() == null) editor.setEditable(false);
				else editor.setEditabilityTracker(true);

				transcriptFile = file;
				player.setAutoScrolling(true); //otherwise the first time you press Play you don't get highlights in the text window!!

				@TIBETAN@if (activeKeyboard != null) changeKeyboard(activeKeyboard); //this means that keyboard was changed before constructing a DuffPane

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
		Object playableparent = XMLUtilities.selectSingleJDOMNode(node, config.getProperty("qd.nearestplayableparent"), namespaces);
		if (playableparent == null) return;
		String nodeid = String.valueOf(playableparent.hashCode());
		if (player.cmd_isID(nodeid)) {
			/* FIXME: by transferring focus, we don't have to worry about problems caused by
			the cursor position in the editor being different from the highlight,
			since users will have to click on the editor to get back into editing
			FIXME here's a problem, though: if there is a scrollbar for the JTextPane, then
			focus transfers to this scrollbar. if not, then it transfers back to itself, in other
			words the desired effect is not achieved! */
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

	public XMLEditor getEditor() {
		return editor;
	}

  	public static String convertTimesForPanelPlayer(String s) {
		StringBuffer sBuff = new StringBuffer();
		StringTokenizer sTok = new StringTokenizer(s, ",");
		while (sTok.hasMoreTokens()) {
			sBuff.append(String.valueOf(new Float(Float.parseFloat(sTok.nextToken()) * 1000).longValue()));
			sBuff.append(',');
		}
		return sBuff.toString();
	}
	public boolean configure(Configuration configuration) {
		if (transcriptFile != null) return false;
		try {
			this.configuration = configuration;
			SAXBuilder builder = new SAXBuilder();
			Document cDoc = builder.build(configuration.getConfigURL());
			Element cRoot = cDoc.getRootElement();
			Iterator it;

			//tag rendering
			Element renderingInstructions = cRoot.getChild("rendering-instructions");
			List sharedInstructions = renderingInstructions.getChildren("tag");
			List tagViews = renderingInstructions.getChildren("tagview");
			XMLTagInfo[] tagInfo;
			Map tagShortcuts = null;
			if (tagViews.size() == 0) { //only one set of rendering instructions
				tagInfo = new XMLTagInfo[1];
				tagInfo[0] = new XMLTagInfo();
				if (sharedInstructions.size() > 0) {
					it = sharedInstructions.iterator();
					while (it.hasNext()) {
						Element e = (Element)it.next();
						@TIBETAN@tagInfo[0].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@TIBETAN@new Boolean(e.getAttributeValue("tibetan")), e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						@UNICODE@tagInfo[0].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						List atts = e.getChildren("attribute");
						Iterator it2 = atts.iterator();
						while (it2.hasNext()) {
							Element eAtt = (Element)it2.next();
							tagInfo[0].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
						}
					}
				}
			} else {
				tagShortcuts = new HashMap();
				tagInfo = new XMLTagInfo[tagViews.size()];
				int count = 0;
				Iterator tagViewIter = tagViews.iterator();
				while (tagViewIter.hasNext()) {
					Element tagView = (Element)tagViewIter.next();
					tagInfo[count] = new XMLTagInfo(tagView.getAttributeValue("name"));
					KeyStroke key = KeyStroke.getKeyStroke(tagView.getAttributeValue("keystroke"));
					tagShortcuts.put(tagInfo[count], key);
					if (sharedInstructions.size() > 0) {
						it = sharedInstructions.iterator();
						while (it.hasNext()) {
							Element e = (Element)it.next();
							@TIBETAN@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
								@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
								@TIBETAN@new Boolean(e.getAttributeValue("tibetan")), e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
							@UNICODE@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
								@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
								@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
							List atts = e.getChildren("attribute");
							Iterator it2 = atts.iterator();
							while (it2.hasNext()) {
								Element eAtt = (Element)it2.next();
								tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
									new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
									eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
							}
						}
					}

					List tagOptions = tagView.getChildren("tag");
					it = tagOptions.iterator();
					while (it.hasNext()) {
						Element e = (Element)it.next();
						@TIBETAN@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@TIBETAN@new Boolean(e.getAttributeValue("tibetan")), e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						@UNICODE@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						List atts = e.getChildren("attribute");
						Iterator it2 = atts.iterator();
						while (it2.hasNext()) {
							Element eAtt = (Element)it2.next();
							tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								e.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
						}
					}
					count++;
				}
			}

			//parameters
			Element parameterSet = cRoot.getChild("parameters");
			List parameters = parameterSet.getChildren("parameter");
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
            List allNamespaces = new ArrayList();
            if (config.getProperty("qd.namespaces") != null) {
                String nsList = config.getProperty("qd.namespaces");
                StringTokenizer tok = new StringTokenizer(nsList, ",");
                while (tok.hasMoreTokens()) {
                    String nextNs = tok.nextToken();
                    allNamespaces.add(Namespace.getNamespace(nextNs.substring(0, nextNs.indexOf(' ')), nextNs.substring(nextNs.indexOf(' ')+1)));
                }
            }
            if (config.getProperty("qd.timealignednodes") == null) {
                allNamespaces.add(Namespace.getNamespace("qd", "http://altiplano.emich.edu/quilldriver"));
                config.setProperty("qd.timealignednodes", "//*[@qd:*]");
                config.setProperty("qd.nodebegins", "@qd:t1");
                config.setProperty("qd.nodeends", "@qd:t2");
                config.setProperty("qd.nearestplayableparent", "ancestor-or-self::*[@qd:*]");
            }
            namespaces = (Namespace[])allNamespaces.toArray(new Namespace[0]);
            
			//configuration-defined actions
			Element allActions = cRoot.getChild("actions");
			List actionSets = allActions.getChildren("action-set");
			keyActions = new HashMap(); //maps keys to actions
			taskActions = new HashMap(); //maps task names to same actions

			int xCount = 0;
			if (tagInfo.length < 2)
				configMenus = new JMenu[actionSets.size()]; //no need for extra "View" menu
			else {
				configMenus = new JMenu[actionSets.size()+1]; //need extra "View" menu
				configMenus[xCount] = new JMenu("View");
					ButtonGroup tagGroup = new ButtonGroup();
					for (int z=0; z<tagInfo.length; z++) {
						final XMLTagInfo zTagInfo = tagInfo[z];
						final Action changeViewAction = new AbstractAction() {
							public void actionPerformed(ActionEvent e) {
								currentTagInfo = zTagInfo;
								if (editor != null) {
									editor.setTagInfo(currentTagInfo);
									hp.refresh();
								}
							}
						};
						JRadioButtonMenuItem tagItem = new JRadioButtonMenuItem(tagInfo[z].getIdentifyingName());
						tagItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								changeViewAction.actionPerformed(e);
							}
						});
						KeyStroke k = (KeyStroke)tagShortcuts.get(tagInfo[z]);
						if (k != null) {
							tagItem.setAccelerator(k);
							keyActions.put(k, changeViewAction);
						}
						tagGroup.add(tagItem);
						if (z == 0) tagItem.setSelected(true);
						configMenus[xCount].add(tagItem);
					}
				xCount++;
			}
            currentTagInfo = tagInfo[0];

			Iterator actionSetIter = actionSets.iterator();
			try {
			//stupid: I just made Transformer global for no good reason
			if (configuration.getEditURL() != null)
				transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(configuration.getEditURL().openStream()));
			final DOMOutputter domOut = new DOMOutputter();
			final DOMBuilder jdomBuild = new DOMBuilder();

			while (actionSetIter.hasNext()) {
				Element thisSet = (Element)actionSetIter.next();
				configMenus[xCount] = new JMenu(thisSet.getAttributeValue("name"));
				List actions = thisSet.getChildren("action");
				it = actions.iterator();
				while (it.hasNext()) {
					Element e = (Element)it.next();
					final JMenuItem mItem = new JMenuItem(e.getAttributeValue("name"));
					KeyStroke key = KeyStroke.getKeyStroke(e.getAttributeValue("keystroke"));
					final String nodeSelector = e.getAttributeValue("node");
					final String command = e.getAttributeValue("qd-command");
					final String tasks = e.getAttributeValue("xsl-task");
					if (tasks == null) { //no need for xsl transform
						final Action keyAction = new AbstractAction() {
							public void actionPerformed(ActionEvent e) {
								if (nodeSelector != null) {
									editor.fireEndEditEvent();
									boolean keepSearching;
									JTextPane t = editor.getTextPane();
									int offset = t.getCaret().getMark();
									Object context = editor.getNodeForOffset(offset);
									System.out.println("xpath--"+String.valueOf(offset)+": "+context.toString());
									do {
										keepSearching = false;
										if (context != null) {
											Object moveTo = XMLUtilities.selectSingleJDOMNode(context, nodeSelector, namespaces);
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
									if (command != null) executeCommand(command);
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
						configMenus[xCount].add(mItem);
					} else { //need for xsl transform
						final Action keyAction = new AbstractAction() {
							public void actionPerformed(ActionEvent e) {
								try {
									if (command != null) executeCommand(command);
									editor.fireEndEditEvent();
									editor.setEditabilityTracker(false);
									int offset = editor.getTextPane().getCaret().getMark();
									Object context = editor.getNodeForOffset(offset);
									Object transformNode = XMLUtilities.selectSingleJDOMNode(context, nodeSelector, namespaces);
									if (!(transformNode instanceof Element)) return;
									Element jdomEl = (Element)transformNode;
									Element clone = (Element)jdomEl.clone();
									Element cloneOwner = new Element("CloneOwner");
									cloneOwner.addContent(clone);
									org.w3c.dom.Element transformElement = domOut.output((Element)cloneOwner);
									//String refreshID = XMLUtilities.getTextForJDOMNode(XMLUtilities.selectSingleJDOMNode(context, config.getProperty("qd.refreshid"), namespaces));
									Enumeration enum = config.propertyNames();
									while (enum.hasMoreElements()) {
										String key = (String)enum.nextElement();
										String val = config.getProperty(key);
										if (!key.startsWith("qd.")) {
											Object obj = XMLUtilities.selectSingleJDOMNode(context, val, namespaces);
											if (obj != null) {
												String id = XMLUtilities.getTextForJDOMNode(obj);
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
									float now = (float)player.getCurrentTime();
									float endoftime = (float)player.getEndTime();
									float currentSeconds = now / 1000; //convert from milliseconds
									float endTime = endoftime / 1000; //convert from milliseconds
									//float currentSeconds = (new Integer(player.getCurrentTime())).floatValue() / 1000; //convert from milliseconds
									//float endTime = (new Integer(player.getEndTime())).floatValue() / 1000; //convert from milliseconds
									String cS = String.valueOf(currentSeconds);
									String eT = String.valueOf(endTime);
									System.out.println("Current = " + cS + "\nEnd = " + eT + "\n\n");
									transformer.setParameter("qd.currentmediatime", String.valueOf(currentSeconds));
									transformer.setParameter("qd.mediaduration", String.valueOf(endTime));
									float slowInc = (float)PreferenceManager.slow_adjust;
									float rapidInc = (float)PreferenceManager.rapid_adjust;
									transformer.setParameter("qd.slowincrease", String.valueOf(slowInc/1000));
									transformer.setParameter("qd.rapidincrease", String.valueOf(rapidInc/1000));
									//send the name of the current media URL
									transformer.setParameter("qd.mediaurlstring", player.getMediaURL().toString());

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

									/*XMLOutputter xmlOut = new XMLOutputter();
									try {
										Element ee = (Element)cloneOwner;
										xmlOut.output(ee, System.out);
										xmlOut.output(jdomResult.getDocument(), System.out);
									} catch (IOException ioe) {ioe.printStackTrace();}*/

									int start = editor.getStartOffsetForNode(transformNode);
									int end = editor.getEndOffsetForNode(transformNode);

									//FIXME: this whole section needs to be sensitive to whether or
									//not node is visible on screen, and whether or not what replaces
									//it should be visible or not. right now, the following code won't
									//work right if the transformed node is not visible

									//boolean redraw = true;
									//if (start == -1 || end == -1) redraw = false; //node is visible, so redraw transformed text segment
									//if (redraw) {
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
														insertPos = editor.renderText(text, insertPos);
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
						configMenus[xCount].add(mItem);
					}
				}
				xCount++;
			}
				} catch (TransformerException tre) {
					tre.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
		} catch (JDOMException jdome) {
			jdome.printStackTrace();
		}
		return true;
	}

	public void executeCommand(String command) {
		//FIXME: These commands should be defined elsewhere, in programmatically extensible classes
							if (command.equals("playNode")) {
								Object nearestParent = XMLUtilities.selectSingleJDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), config.getProperty("qd.nearestplayableparent"), namespaces);
								playNode(nearestParent);
							}
							else if (command.equals("playPause")) {
								try {
									/* by transferring focus, we don't have to worry about problems caused by
									the cursor position in the editor being different from the highlight,
									since users will have to click on the editor to get back into editing */
									editor.getTextPane().transferFocus();
									if (player.isPlaying()) player.cmd_stop();
									else player.cmd_playOn();
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
							else if (command.equals("playBack")) {
								try {
									long t = player.getCurrentTime() - PreferenceManager.play_minus;
									if (t < 0) t = 0;
									if (player.isPlaying()) player.cmd_stop();
									player.setCurrentTime(t);
									player.cmd_playOn();
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
							else if (command.equals("playEdge")) {
								try {
									Object nearestParent = XMLUtilities.selectSingleJDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), config.getProperty("qd.nearestplayableparent"), namespaces);
									tcp.setNode(nearestParent);
									Long t2 = tcp.getOutTime();
									long t1 = t2.longValue() - PreferenceManager.play_minus;
									if (t1 < 0) t1 = 0;
									player.cmd_playSegment(new Long(t1), t2);
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
							else if (command.equals("seekStart")) {
								try {
									Object nearestParent = XMLUtilities.selectSingleJDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), config.getProperty("qd.nearestplayableparent"), namespaces);
									tcp.setNode(nearestParent);
									Long t = tcp.getInTime();
									if (player.isPlaying()) player.cmd_stop();
									player.setCurrentTime(t.longValue());
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
							else if (command.equals("seekEnd")) {
								try {
									Object nearestParent = XMLUtilities.selectSingleJDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), config.getProperty("qd.nearestplayableparent"), namespaces);
									tcp.setNode(nearestParent);
									Long t = tcp.getOutTime();
									if (player.isPlaying()) player.cmd_stop();
									player.setCurrentTime(t.longValue());
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
							else if (command.equals("stopMedia")) {
								try {
									if (player.isPlaying()) player.cmd_stop();
								} catch (PanelPlayerException ppe) {
									ppe.printStackTrace();
								}
							}
	}
	public JMenu[] getConfiguredMenus() {
		return configMenus;
	}

	@TIBETAN@public org.thdl.tib.input.JskadKeyboard getKeyboard() {
		@TIBETAN@return activeKeyboard;
	@TIBETAN@}
	@TIBETAN@public void changeKeyboard(org.thdl.tib.input.JskadKeyboard kbd) {
		@TIBETAN@activeKeyboard = kbd;
		@TIBETAN@if (editor == null || !(editor.getTextPane() instanceof org.thdl.tib.input.DuffPane)) return;
		@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)editor.getTextPane();
		@TIBETAN@kbd.activate(dp);
	@TIBETAN@}
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
