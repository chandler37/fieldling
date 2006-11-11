/* ****** BEGIN LICENSE BLOCK *****
 *
 *    Copyright 2003 Edward Garrett
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

package fieldling.quilldriver.gui;

import java.util.List; //preferred over java.awt.List
import java.util.Timer; //preferred over javax.swing.Timer
import java.net.*;
import java.awt.*;
import java.awt.font.*;//
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
import fieldling.mediaplayer.*;
import fieldling.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.xpath.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import fieldling.quilldriver.*;
import fieldling.quilldriver.config.*;
import fieldling.quilldriver.xml.*;
import fieldling.quilldriver.xml.View;
import fieldling.quilldriver.task.*;

public class QD extends JDesktopPane implements DOMErrorHandler {
    private boolean firstResize = true;
    
	//CLASS CONSTANTS
	protected static TagInfo currentTagInfo = null;
	protected static Color hColor = Color.cyan;
	@UNICODE@public static final String PRODUCT_NAME = "QuillDriver";
	@TIBETAN@public static final String PRODUCT_NAME = "QuillDriver-TIBETAN";
	public static final String SHOW_FILENAME_AS_TITLE_BY_DEFAULT_NAME = "qd.showfilenameastitlebydefault";
	
	//CLASS ACTION
	public static TranscriptToggler transcriptToggler;
	public static AutoSave autoSaver;
	static {
		transcriptToggler = new TranscriptToggler();
		autoSaver = new AutoSave(transcriptToggler, PreferenceManager.getInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, PreferenceManager.AUTO_SAVE_MINUTES_DEFAULT) * 60000);
		autoSaver.start();
	}
	
	//MORE CLASS FIELDS
	public static QD lastQD = null;
	public static Configuration configuration = null;
	public static DocumentBuilder docBuilder = null;
	public static ResourceBundle messages = I18n.getResourceBundle();
	
	//INSTANCE FIELDS
	private QDShell qdShell=null;
	private String title = "";
	private boolean hasVideoFrameBeenResizedByDragging = false;
	@TIBETAN@protected org.thdl.tib.input.JskadKeyboard activeKeyboard = null;
	protected PanelPlayer player = null;
	protected Editor editor = null;
	protected TimeCodeModel tcp = null;
	protected Hashtable actions;
	protected View view;
	protected org.w3c.dom.Document xmlDoc = null;
	protected TimeCodeView tcv;
	protected JPanel buttonPanel = null;
	protected JComboBox togglerComboBox;
	public JInternalFrame videoFrame = null;
	public JInternalFrame textFrame = null;	
	public TextHighlightPlayer hp;
	public File transcriptFile = null;
	public Timer checkTimeTimer = null;
	
	public QD(Configuration configuration, PanelPlayer player) {
		setConfiguration(configuration);
		setMediaPlayer(player);
		setupGUI();
	}
	
	private void setupGUI() {
		setBackground(new JFrame().getBackground());
		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		videoFrame = new JInternalFrame(null, true, false, true, true);//title, resizable, closable, maximizable, iconifiable
        //videoFrame = new JInternalFrame();
		//videoFrame.setBorder(null);
		//((javax.swing.plaf.basic.BasicInternalFrameUI) videoFrame.getUI()).setNorthPane(null);
		//videoFrame.getContentPane().setLayout(new BorderLayout());
		videoFrame.setVisible(true);
		videoFrame.setLocation(0,0);
		videoFrame.setSize(0,0);
		videoFrame.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent me) {
				hasVideoFrameBeenResizedByDragging = true;
			}
		});
		videoFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
				if (hasVideoFrameBeenResizedByDragging) {
					//player.setPreferredSize(new Dimension(player.getComponent(0).getSize()));
					//videoFrame.pack();
					hasVideoFrameBeenResizedByDragging = false;
					WindowPositioningTask.repositionWithActiveWindowPositioner(QD.this);
				}
			}
		});
		add(videoFrame, JLayeredPane.PALETTE_LAYER);
		invalidate();
		validate();
		repaint();
		textFrame = new JInternalFrame();             
		textFrame.setBorder(null);
		((javax.swing.plaf.basic.BasicInternalFrameUI) textFrame.getUI()).setNorthPane(null);
		//textFrame = new JInternalFrame(null, true, false, true, true);//title, resizable, closable, maximizable, iconifiable
		textFrame.setVisible(true);
		textFrame.setLocation(0,0);
		textFrame.setSize(0,0);
		add(textFrame, JLayeredPane.DEFAULT_LAYER);
		invalidate();
		validate();
		repaint();
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
                if (firstResize) {
                    videoFrame.pack();
                    firstResize = false;
                }
				WindowPositioningTask.repositionWithActiveWindowPositioner(QD.this);
			}
		});
	}
	
	public String getWindowTitle(String lang)
	{       
		String titleName="qd.title";
		/*String titleName=null;
		 if(lang=="English") titleName="qd.title";
		 else if(lang=="Chinese")titleName="qd.title_zh";
		 else if(lang=="Tibetan"||lang=="Wylie") titleName="qd.title_tib";*/
		XPathExpression title = (XPathExpression) configuration.getParameters().get(titleName);
		
		if (title !=null)
		{
			String transcriptTitle;
			transcriptTitle = XPathUtilities.saxonSelectSingleDOMNodeToString(getEditor().getXMLDocument(), title);			                                                               
			transcriptTitle =transcriptTitle.trim();
			if (transcriptTitle!=null && !transcriptTitle.equals("")){		                                
				//@TIBETAN@if(lang=="Tibetan"){ }//rendering the tibetan string from Wylie
				//@UNICODE@if(lang=="Tibetan"){transcriptTitle=org.thdl.tib.text.ttt.EwtsToUnicodeForXslt.convertEwtsTo(transcriptTitle);} //rendering the unicode tibetan string                           
				return transcriptTitle; 
			}
			else
				return transcriptFile.getName();
		}
		//return transcriptFile.getName() + " - " + QD.PRODUCT_NAME;
		return transcriptFile.getName();
	}
	
	public void setQDShell(QDShell shell) {
		qdShell=shell;
	}
	
	private void startTimer() {
		final java.util.Timer timer = new java.util.Timer(true);
		timer.schedule(new TimerTask() {
			public void run()
			{
				if (player.isInitialized())
				{
					timer.cancel();
                    //Panel vidP = new Panel();
                    //vidP.add(player);
					//videoFrame.getContentPane().add(BorderLayout.CENTER, vidP);
					videoFrame.getContentPane().add(BorderLayout.CENTER, player);
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
	public void setMediaPlayer(PanelPlayer smp) {
		if (transcriptFile != null) return;
		if (smp == null)
			player = null;
		else if (player == null || !player.equals(smp)) {
			player = smp;
			player.setParentContainer(QD.this);
			player.setMultipleAnnotationPolicy(PreferenceManager.getInt(PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_KEY, PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_DEFAULT)==0);
			player.setAutoScrolling(PreferenceManager.getInt(PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_KEY, PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_DEFAULT) == 0);
		}
	}
	
	public PanelPlayer getMediaPlayer() {
		return player;
	}
	
	public boolean hasContent() {
		return editor != null;
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
		transcriptToggler.remove(this);
		removeAll(); //remove sub-windows
		editor = null;
		setupGUI(); //set up GUI for another transcript
	}
	
	public boolean loadTranscript(File file)
	{
		return loadTranscript(file, null);
	}
	
	public boolean loadTranscript(File file, String videoUrl)
	{
		try 
		{
			return loadTranscript(file.toURI().toURL(), videoUrl);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			return false;
		}
	}
	
	public boolean loadTranscript(URL transcriptURL)
	{
		return loadTranscript(transcriptURL, null);
	}
	
	public boolean loadTranscript(URL transcriptURL, String videoUrl)
	{
		String transcriptString = transcriptURL.toString();
		if (player == null) {
			JOptionPane.showConfirmDialog(QD.this, messages.getString("SupportedMediaError"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			return false;
		}
		try
		{
			try { //load xml document                       
				/* I use the xerces-2-java parser because, to my knowledge, it is the only parser that supports
				 DOM revalidation as part of DOM Level 3 Core. This allows documents to be revalidated
				 after each call to NodeTransformer, without reloading the entire document. See.
				 http://xml.apache.org/xerces2-j/faq-dom.html#faq-9*/
				if (docBuilder == null) {
					System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
					docBuilder = configuration.getDocumentBuilder(DocumentBuilderFactory.newInstance());
				}
				xmlDoc = docBuilder.parse(transcriptString);
				/* handler = new SAXValidator();
				 docBuilder.setErrorHandler(handler);*/
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (org.xml.sax.SAXException saxe) {
				saxe.printStackTrace();
				return false;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return false;
			}
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
			}
			String value;
			if (configuration.getParameters().get("qd.mediaurl") == null) value = null;
			else {
				value = XPathUtilities.saxonSelectSingleDOMNodeToString(xmlDoc, (XPathExpression)(configuration.getParameters().get("qd.mediaurl")));
				if (value!=null) value = value.trim();
			}
			boolean nomedia = true;
			if (value != null && !value.equals("")) {
				try {
					if (value.startsWith("http:"))
					{
						player.loadMovie(new URL(value));
						//videoFrame.setTitle(value);
						nomedia = false;
					}
					else
					{ //it's a file, so try to load
						File mediaFile=null;
						if (value.startsWith("file:")) mediaFile = new File(value.substring(5));
						else
						{
							try
							{
								//File transcriptFile = new File (transcriptURL.toURI()); //URL.toURI() is only supported in Java 5.0
								transcriptFile = new File(new URI(transcriptURL.toString())); //URI supported as of Java 1.4
								String transcriptAbs = transcriptFile.getAbsolutePath();
								mediaFile = new File(transcriptAbs.substring(0,transcriptAbs.lastIndexOf(QDShell.FILE_SEPARATOR) + 1), value);
							}
							catch (Exception e)
							{
								e.printStackTrace(System.err);
							}
						}
						try
						{
							if (mediaFile.exists()) { //open the actual file
								player.loadMovie(mediaFile);
								//player.loadMovie(mediaFile.toURL());
								nomedia = false;
							} else {
								String mediaDirectory = PreferenceManager.getValue(PreferenceManager.MEDIA_DIRECTORY_KEY, PreferenceManager.MEDIA_DIRECTORY_DEFAULT);
								if (mediaDirectory != null) { //otherwise try default media directory
									String mediaName = value.substring(value.lastIndexOf(QDShell.FILE_SEPARATOR)+1);
									mediaFile = new File(mediaDirectory, mediaName);
									if (mediaFile.exists()) {
										player.loadMovie(mediaFile);
										//player.loadMovie(mediaFile.toURL());
										nomedia = false;
									}
								}
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}  
				} catch (MalformedURLException murle) {murle.printStackTrace();} //do nothing
			}
			if (nomedia) { //can't find movie: open new movie
				if (videoUrl != null) {
					try {
						player.loadMovie(new URL(videoUrl));
						nomedia = false;
					} catch (MalformedURLException murle) {murle.printStackTrace();}
				} else {
					JFileChooser fc = new JFileChooser(PreferenceManager.getValue(PreferenceManager.MEDIA_DIRECTORY_KEY, PreferenceManager.MEDIA_DIRECTORY_DEFAULT));
					if (fc.showDialog(QD.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
						File mediaFile = fc.getSelectedFile();
						//try {
							player.loadMovie(mediaFile);
							//player.loadMovie(mediaFile.toURL());
							String mediaString = mediaFile.getAbsolutePath();
							PreferenceManager.setValue(PreferenceManager.MEDIA_DIRECTORY_KEY, mediaString.substring(0, mediaString.lastIndexOf(QDShell.FILE_SEPARATOR)+1));
							nomedia = false;
						//} catch (MalformedURLException murle) {} //do nothing
					}
				}
			}
			if (nomedia) { //user did not open a valid movie; therefore transcript will not be opened
				transcriptFile = null;
				return false;
			}
			try
			{
				//File transcriptFile = new File (transcriptURL.toURI()); //URL.toURI() is only supported in Java 5.0
				transcriptFile = new File(new URI(transcriptURL.toString())); //URI supported as of Java 1.4
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
			transcriptToggler.add(this);
			layoutTranscript();
			return true;
		} catch (PanelPlayerException smpe) {
			smpe.printStackTrace();
			transcriptFile = null;
			return false;
		}
	}
	
	public void layoutTranscript() {
		@TIBETAN@final JTextPane t = new org.thdl.tib.input.DuffPane();
		@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)t;
		@TIBETAN@dp.setByUserTibetanFontSize(PreferenceManager.getInt(PreferenceManager.TIBETAN_FONT_SIZE_KEY, PreferenceManager.TIBETAN_FONT_SIZE_DEFAULT));
		@TIBETAN@dp.setByUserRomanAttributeSet(PreferenceManager.getValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.FONT_FACE_DEFAULT), PreferenceManager.getInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.FONT_SIZE_DEFAULT));
		
		@UNICODE@final JTextPane t = new JTextPane();
		@UNICODE@t.setFont(new Font(PreferenceManager.getValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.FONT_FACE_DEFAULT), java.awt.Font.PLAIN, PreferenceManager.getInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.FONT_SIZE_DEFAULT)));
		
		editor = new Editor(xmlDoc, t, currentTagInfo);
		
		Keymap keymap = editor.getTextPane().addKeymap("Config-Bindings", editor.getTextPane().getKeymap());
		Map keyActions = configuration.getKeyActions();
		Set keys = keyActions.keySet();
		Iterator keyIter = keys.iterator();
		while (keyIter.hasNext()) {
			KeyStroke key = (KeyStroke)keyIter.next();
			Action action = (Action)keyActions.get(key);
			keymap.addActionForKeyStroke(key, action);
		}
		editor.getTextPane().setKeymap(keymap);
		view = new View(editor, editor.getXMLDocument(), (XPathExpression)configuration.getParameters().get("qd.timealignednodes"), (XPathExpression)configuration.getParameters().get("qd.nodebegins"), (XPathExpression)configuration.getParameters().get("qd.nodeends"));          
		try {
			hColor=new Color(
					PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_RED_KEY, PreferenceManager.HIGHLIGHT_RED_DEFAULT),
					PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_GREEN_KEY, PreferenceManager.HIGHLIGHT_GREEN_DEFAULT),
					PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_BLUE_KEY, PreferenceManager.HIGHLIGHT_BLUE_DEFAULT)
			);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		hp = new TextHighlightPlayer(view, hColor, PreferenceManager.getValue(PreferenceManager.HIGHLIGHT_POSITION_KEY, PreferenceManager.HIGHLIGHT_POSITION_DEFAULT));
		
		/*LOGGING
		 for (int ok=0; ok<namespaces.length; ok++)
		 System.out.println(namespaces[ok].toString());*/
		
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
		tcp = new TimeCodeModel(hp, configuration.getParameters(), configuration.getNamespaces());
		//tcp = new TimeCodeModel(hp, configuration.getParameters(), configuration.getNamespaces(), insertTimesAction);
		player.removeAllAnnotationPlayers();
		player.addAnnotationPlayer(hp);
		player.initForSavant(convertTimesForPanelPlayer(view.getT1s()), convertTimesForPanelPlayer(view.getT2s()), view.getIDs());
		videoFrame.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//LOGGINGSystem.out.println("mouse clicked on player");
				videoFrame.requestFocus();
			}
		});
		startTimer();
		
		//textFrame.setTitle(transcriptString); //title=name of transcript file
		
		tcv = new TimeCodeView(player, tcp);
		updateTimeCodeBarVisibility();
		checkTimeTimer = new Timer(true);
		checkTimeTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				tcv.setCurrentTime(player.getCurrentTime());
			}
		}, 0, 50);
		buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(tcv, BorderLayout.SOUTH);
		//JPanel buttonPanel = new JPanel(new Layout(FlowLayout.LEFT,0,0));
		//buttonPanel.add(tcv);
		JPanel jp = new JPanel(new BorderLayout());
		jp.add("North", buttonPanel);
		jp.add("Center", hp);
		//JTabbedPane tabbedPane = new JTabbedPane();
		//tabbedPane.add(messages.getString("BasicTranscriptViewMode"), jp);
		JComponent c = (JComponent)textFrame.getContentPane();
		c.removeAll();
		c.setLayout(new BorderLayout());
		//c.add("Center", tabbedPane);
		c.add("Center", jp);
		textFrame.setSize(textFrame.getSize().width, getSize().height);
		textFrame.invalidate();
		textFrame.validate();
		textFrame.repaint();
		editor.addNodeEditListener(new Editor.NodeEditListener() {
			public void nodeEditPerformed(Editor.NodeEditEvent ned) {
				if (ned instanceof Editor.StartEditEvent) {
					if (tcp != null) tcp.setNode(ned.getNode());
				} else if (ned instanceof Editor.EndEditEvent) {
					Editor.EndEditEvent eee = (Editor.EndEditEvent)ned;
					if (eee.hasBeenEdited()) hp.refresh();
				} else if (ned instanceof Editor.CantEditEvent) {
					//if this node can't be edited, maybe it can be played!
					Object node = ned.getNode();
					if (node != null) {
						editor.getTextPane().setCaretPosition(editor.getStartOffsetForNode(node));
						if (tcp != null) tcp.setNode(node);
						playNode(node);
					}
				}
			}
		});
		if (!configuration.canEdit()) editor.setEditable(false);
		else editor.setEditabilityTracker(true);
		togglerComboBox = new JComboBox();
		buttonPanel.add(togglerComboBox, BorderLayout.CENTER);
		//transcriptToggler.add(this);
		//register();			
		@TIBETAN@if (activeKeyboard != null) changeKeyboard(activeKeyboard); //this means that keyboard was changed before constructing a DuffPane
	}
	
	public void updateTimeCodeBarVisibility()
	{
		int showTimeCoding = PreferenceManager.getInt(PreferenceManager.SHOW_TIME_CODING_KEY, PreferenceManager.SHOW_TIME_CODING_DEFAULT);
		if (showTimeCoding == -1)
		{
			// if preferences have not been set, get the default from the configuration file.
			Boolean showTimeCodingBoolean = (Boolean) configuration.getParameters().get("qd.showtimecodingbydefault");
			if (showTimeCodingBoolean!=null)
				showTimeCoding = showTimeCodingBoolean.booleanValue()?1:0;
		}
		if (showTimeCoding == 0) tcv.setVisible(false);
		else tcv.setVisible(true);
	}
	
	public void register()
	{
		XPathExpression showFileNameXPath;
		Object obj = configuration.getParameters().get(SHOW_FILENAME_AS_TITLE_BY_DEFAULT_NAME);
		if (obj != null) {
			Boolean bool = (Boolean) obj;
			PreferenceManager.setInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, bool.booleanValue()?1:-1);
		}
		/*if (PreferenceManager.getInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, PreferenceManager.SHOW_FILE_NAME_AS_TITLE_DEFAULT)==-1)
		{
			//showFileNameXPath = (XPathExpression) configuration.getParameters().get(SHOW_FILENAME_AS_TITLE_BY_DEFAULT_NAME);
			if (showFileNameXPath==null)
			{
				PreferenceManager.setInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, 1);
			}
			else
			{
				PreferenceManager.setInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, Boolean.getBoolean(XPathUtilities.saxonSelectSingleDOMNodeToString(editor.getXMLDocument(), showFileNameXPath))?1:0);
			}
		}*/
		if (PreferenceManager.getInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, PreferenceManager.SHOW_FILE_NAME_AS_TITLE_DEFAULT)==0 || PreferenceManager.getInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, PreferenceManager.SHOW_FILE_NAME_AS_TITLE_DEFAULT) == 1)
		{
			if (transcriptToggler.getNumberOfTranscripts() == 0)
				title = new String(PRODUCT_NAME + " (" + messages.getString(configuration.getName()) + ")");
			else { //qd has content
				title = new String(getWindowTitle(I18n.getLocale().getLanguage()) + " (" + PRODUCT_NAME + ": " + messages.getString(configuration.getName()) + ")");
				buttonPanel.remove(togglerComboBox);
				togglerComboBox = transcriptToggler.getToggler(this);
				togglerComboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JComboBox box = (JComboBox)e.getSource();
						try {
							QD switchTo = transcriptToggler.getQDForIndex(box.getSelectedIndex());
							if (QD.this != switchTo) { //switch to different QD
								QDShell qdShell = QD.this.getParentQDShell();
								qdShell.deActivateQD(QD.this);
								lastQD = QD.this;
								qdShell.activateQD(switchTo, true);
							}
						} catch (IndexOutOfBoundsException iobe) {
							iobe.printStackTrace();
						}
					}
				});
				buttonPanel.add(togglerComboBox, BorderLayout.CENTER);
				buttonPanel.validate();
			}
		}
	}
	
	public void playNode(Object node) {
		Node playableparent = XPathUtilities.saxonSelectSingleDOMNode(node, (XPathExpression)(configuration.getParameters().get("qd.nearestplayableparent")));
		if (playableparent == null) return;
		String nodeid = String.valueOf(playableparent.hashCode());
		if (player.cmd_isID(nodeid)) {
			/* FIXME: by transferring focus, we don't have to worry about problems caused by
			 the cursor position in the editor being different from the highlight,
			 since users will have to click on the editor to get back into editing
			 FIXME here's a problem, though: if there is a scrollbar for the JTextPane, then
			 focus transfers to this scrollbar. if not, then it transfers back to itself, in other
			 words the desired effect is not achieved! */
			if (player.getAutoScrolling())
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
		//LOGGINGSystem.out.println ("Constructor: " + constructor.toString());
		Object object = null;
		try {
			object = constructor.newInstance(arguments);
			//LOGGINGSystem.out.println ("Object: " + object.toString());
			return object;
		} catch (InstantiationException e) {
			//LOGGINGSystem.out.println(e);
		} catch (IllegalAccessException e) {
			//LOGGINGSystem.out.println(e);
		} catch (IllegalArgumentException e) {
			//LOGGINGSystem.out.println(e);
		} catch (InvocationTargetException e) {
			//LOGGINGSystem.out.println(e);
			//LOGGINGSystem.out.println(e.getTargetException());
		}
		return object;
	}
	
	public TimeCodeModel getTimeCodeModel() {
		return tcp;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public QDShell getParentQDShell() {
		return qdShell;
	}
	
	public ResourceBundle getMessages() {
		return messages;
	}
	
	public Editor getEditor() {
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
	
	public void changeTagInfo(TagInfo newTagInfo) {
		if (hasContent()) {
			currentTagInfo = newTagInfo;
			editor.setTagInfo(currentTagInfo);
			hp.refresh();
		}
	}
	
	public void changeTagInfo(TagInfo newTagInfo,int i) {
		if (hasContent()) {
			currentTagInfo = newTagInfo;
			editor.setTagInfo(currentTagInfo);
			hp.refresh();
		}
	}
	
	public void setConfiguration(Configuration configuration) {
		if (QD.configuration == configuration)
			return; //do nothing--not a change
		
		//to switch back, uncomment B and comment A
		TagInfo[] tagInfo = configuration.getTagInfo();
		currentTagInfo = tagInfo[0]; //line A
		//changeViewWithInterfaceLanguage(configuration,tagInfo); //line B
		
		if (!hasContent()) {
			QD.configuration = configuration; //no content in QD, so effortless to change
		} else { //must re-configure currently loaded transcript(s)
			QD.configuration = configuration;
			for (int k=0; k<transcriptToggler.getNumberOfTranscripts(); k++) {
				QD qd = transcriptToggler.getQDForIndex(k);
				qd.layoutTranscript();
			}
		}
	}
	
	public void transformTranscript(Object domContextNode, XPathExpression nodeSelector, String task) {
		editor.fireEndEditEvent();
		editor.setEditabilityTracker(false);
		try {
			org.w3c.dom.Node domNode = (org.w3c.dom.Node)nodeSelector.evaluate(domContextNode, XPathConstants.NODE);
			try {
				Map xslParameters = getParametersForTransform(domContextNode);
				xslParameters.put("qd.task", task);
				Node firstNodeAfterReplacement = domNode.getNextSibling();
				Node firstNodeInReplacement = NodeTransformer.transformAndReplaceNode(domNode, configuration.getTranscriptTransformer(), xslParameters);
				NodeTransformer.revalidate(domNode.getOwnerDocument(), configuration.getSchemaListAsString(), this);
				editor.replaceNode(domNode, firstNodeInReplacement, firstNodeAfterReplacement);
				hp.refresh();
				player.initForSavant(convertTimesForPanelPlayer(view.getT1s()), convertTimesForPanelPlayer(view.getT2s()), view.getIDs());
			} catch (TransformerException tre) {
				tre.printStackTrace();
			}
		} catch (javax.xml.xpath.XPathExpressionException xpee) {
			xpee.printStackTrace();
		}
		editor.setEditabilityTracker(true);
	}
	
	public boolean handleError(DOMError error) {
		System.out.println(error.getMessage());
		return true;
	}
	
	private Map getParametersForTransform(Object domContextNode) {
		Map parameters = new HashMap();
		XPathExpression xpath;
		Object obj;
		Iterator itty = configuration.getParameters().keySet().iterator();
		while (itty.hasNext()) {
			String key = (String)itty.next();
			if (!key.startsWith("qd.")) {
				obj = configuration.getParameters().get(key);
				if (obj instanceof XPathExpression)
				{
					xpath = (XPathExpression) obj;
					org.w3c.dom.Node domNodeParam = XPathUtilities.saxonSelectSingleDOMNode(domContextNode, xpath);
					parameters.put(key, domNodeParam);
				}
			}
		}
		float inSeconds = tcp.getInTime().floatValue() / 1000; //convert from milliseconds
		float outSeconds = tcp.getOutTime().floatValue() / 1000; //convert from milliseconds
		if (outSeconds >= inSeconds) { //time parameters will not be passed if out precedes in
			parameters.put("qd.start", String.valueOf(inSeconds));
			parameters.put("qd.end", String.valueOf(outSeconds));
			//LOGGINGSystem.out.println("Start="+String.valueOf(inSeconds)+" & End="+String.valueOf(outSeconds));
		} else {
			parameters.put("qd.start", "");
			parameters.put("qd.end", "");
		}
		float now = (float)player.getCurrentTime();
		float endoftime = (float)player.getEndTime();
		float currentSeconds = now / 1000; //convert from milliseconds
		float endTime = endoftime / 1000; //convert from milliseconds
		String cS = String.valueOf(currentSeconds);
		String eT = String.valueOf(endTime);
		//LOGGINGSystem.out.println("Current = " + cS + "\nEnd = " + eT + "\n\n");
		parameters.put("qd.currentmediatime", String.valueOf(currentSeconds));
		parameters.put("qd.mediaduration", String.valueOf(endTime));
		float slowInc = (float)PreferenceManager.getInt(PreferenceManager.SLOW_ADJUST_KEY, PreferenceManager.SLOW_ADJUST_DEFAULT);
		float rapidInc = (float)PreferenceManager.getInt(PreferenceManager.SLOW_ADJUST_KEY, PreferenceManager.SLOW_ADJUST_DEFAULT);
		parameters.put("qd.slowincrease", new Float(slowInc/1000));
		parameters.put("qd.rapidincrease", new Float(rapidInc/1000));
		//send the name of the current media URL
		parameters.put("qd.mediaurlstring", player.getMediaURL().toString());
		return parameters;
	}
	
	@TIBETAN@public org.thdl.tib.input.JskadKeyboard getKeyboard() {
		@TIBETAN@	return activeKeyboard;
		@TIBETAN@}
	@TIBETAN@public void changeKeyboard(org.thdl.tib.input.JskadKeyboard kbd) {
		@TIBETAN@	activeKeyboard = kbd;
		@TIBETAN@	if (editor == null || !(editor.getTextPane() instanceof org.thdl.tib.input.DuffPane)) return;
		@TIBETAN@	org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)editor.getTextPane();
		@TIBETAN@	kbd.activate(dp);
		@TIBETAN@}
}

/*
 XMLSerialiser: how to keep new/empty lines?
 Author: MartinHilpert                       
 Aug 11, 2003 10:29 AM                        
 
 
 
 
 how can i tell XMLSerializer to preserve empty lines (and spaces) within elements? i write a DOM object via
 
 
 FileWriter fw = new FileWriter(file);
 OutputFormat of = new OutputFormat(document, "iso-8859-1", true);
 of.setLineWidth(0); //turn off automatic line wrapping
 of.setPreserveSpace(false); //true preserves new lines but does not nice XML formatting
 XMLSerializer xmls = new XMLSerializer(fw,of);
 xmls.serialize(document);
 
 
 and empty lines within text nodes are just ignored or a space is inserted instead. but when i set outputFormat.setPreserveSpace(true), the new lines are preserved and printed to the XML file. however, with setPreserveSpace(true) all the nice XML formatting is gone (all XML is printed to to one single line - just the explicit new lines within elements (text nodes) create new lines), too! :-(
 
 how can i preserve space in element (nodes) but still keep the nice XML formatting (with new lines for each element and indentation)?              
 
 
 
 
 
 
 
 
 
 
 Re: XMLSerialiser: how to keep new/empty lines?                 
 Author: doffoel                              
 In Reply To:             XMLSerialiser: how to keep new/empty lines?                                  
 Aug 12, 2003 1:34 PM                               
 Reply 1 of 4                 
 
 
 
 
 
 
 
 
 Don't you need to specify an XML schema for doing that ?
 
 PA
 http://www.doffoel.com                  
 
 
 
 
 
 
 
 
 
 
 Re: XMLSerialiser: how to keep new/empty lines?                 
 Author: bangz                              
 In Reply To:             Re: XMLSerialiser: how to keep new/empty lines?                                  
 Aug 12, 2003 4:24 PM                               
 Reply 2 of 4                 
 
 
 
 
 
 
 
 
 I tried what you mentioned, I want to achieve the same thing. In my case the XML was formatted fine but all the blank spaces were not retained, only the first one was retained.
 Did you find any other solution?                  
 
 
 
 
 
 
 
 
 
 
 Re: XMLSerialiser: how to keep new/empty lines?                 
 Author: bangz                              
 In Reply To:             Re: XMLSerialiser: how to keep new/empty lines?                                  
 Aug 12, 2003 8:24 PM                               
 Reply 3 of 4                 
 
 
 
 
 
 
 
 
 Hi
 Sorry for the last post. I am getting the same result as yours. I  lose all the formatting in the XML.
 So I instead of setting the attribute in the output format class for the whole XML, I just set the attribute xml:space=preserve in the element for which I need whitespaces(tabs, newline) to be preserved. 
 It worked!                  
 
 
 
 
 
 
 
 
 
 
 Re: XMLSerialiser: how to keep new/empty lines?                 
 Author: MartinHilpert                              
 In Reply To:             Re: XMLSerialiser: how to keep new/empty lines?                                  
 Aug 14, 2003 2:54 AM                               
 Reply 4 of 4                 
 
 
 
 
 
 
 
 
 the xml:space preserve didn't work for me, but i found another solution: instead of using XMLSerializer, I use the standard Java XML API:
 
 
 /**
  * Write document (DOM object) to a file (as XML text). Default settings: encoding is "iso-8859-1",
  * no automatic line breaks, automatic indentation.
  * 
  * @param document Document to write.
  * @param file File to write to.
  * @throws IOException
  /
   public static void writeDocument(Document document, File file) throws IOException {
   if (document != null && file != null) {
   //Java API:
    FileOutputStream fos = new FileOutputStream(file);
    TransformerFactory tf = TransformerFactory.newInstance();
    try {
    Transformer t = tf.newTransformer();
    t.setOutputProperty("encoding", "iso-8859-1");
    t.setOutputProperty("indent", "yes");
    t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    DOMSource domsource = new DOMSource(document);
    StreamResult sr = new StreamResult(fos);
    t.transform(domsource, sr); 
    } catch (TransformerConfigurationException tce) {
    tce.printStackTrace(System.err);
    } catch (TransformerException te) {
    te.printStackTrace(System.err);
    }
    }//else: no input values available
    }//writeDocument()
    
    
    and in the XSL definition, I use the white-space-collapse and linefeed-treatment attributes to preserve the spaces and linefeeds of the selected value:
    
    
    <fo:block white-space-collapse="false" linefeed-treatment="preserve">
    <xsl:value-of select="/Description/text()"/>
    </fo:block>
    */



/*public void  changeViewWithInterfaceLanguage(Configuration configuration,TagInfo[] tagInfo){
 JMenu viewMenu=null;
 String configName=configuration.getName();
 int currentConfig=0;
 if(configName.equals("TranscribeQuechua")){
 currentConfig=0; 
 }
 else 
 if (configName.equals("THDLTranscription")){
 @UNICODE@viewMenu=configuration.getJMenuBar().getMenu(5);
 @TIBETAN@viewMenu=configuration.getJMenuBar().getMenu(6);
 currentConfig=1;                   
 }
 else 
 if (configName.equals("THDLReadonly")){ 
 currentConfig=2;
 viewMenu=configuration.getJMenuBar().getMenu(1);
 }
 switch (currentConfig){
 case 0:                     
 currentTagInfo = tagInfo[0];     
 break;
 case 1:
 case 2:                       
 if(language=="English"){ //if interface is english, set view to 'English only'
 currentTagInfo = tagInfo[5];
 viewMenu.getItem(5).setSelected(true);
 }
 else 
 if(language=="Chinese"){ //if interface is chinese, set view to 'Chinese only'
 currentTagInfo = tagInfo[6];
 viewMenu.getItem(6).setSelected(true);
 }
 else 
 if(language=="tibetan") {//if interface is tibetan, set view to 'Tibetan only'
 currentTagInfo = tagInfo[0];
 viewMenu.getItem(0).setSelected(true);
 }
 break;
 }        
 }*/

/* EDGE FIX ME! gotta get this out!! what if order in config file changes?? this doesn't belong here
 public void setCurrentLang(int newTagInfo){
 switch(newTagInfo){
 case 0://"TranscriptionOnly"
 language="Tibetan"; break;
 case 1://"TranscriptionPlusEnglish"
 language="Tibetan"; break;  
 case 2://"TranscriptionPlusChinese"
 language="Tibetan"; break;
 case 3://"TranscriptionPlusEnglishPlusChinese"
 language="Tibetan"; break;
 case 4://"EnglishPlusChinese"
 language="Chinese"; break;
 case 5://"Translation_ENOnly"
 language="English"; break;
 case 6://"Translation_ZHOnly"
 language="Chinese"; break;
 case 7://"WylieOnly"
 language="Wylie"; break;
 case 8://"WylieAndEnglish"
 language="Wylie"; break;
 case 9://"NotesOnly"
 language="Tibetan"; break;
 case 10://"TranscriptionPlusNotes"
 language="Tibetan"; break;
 case 11://"EnglishPlusNotes"
 language="English"; break;
 case 12://"ShowEverything"
 language="Tibetan"; break;
 }       
 }*/