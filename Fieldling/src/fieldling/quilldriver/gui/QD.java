/******* BEGIN LICENSE BLOCK *****
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
import fieldling.quilldriver.PreferenceManager;
import fieldling.quilldriver.config.*;
import fieldling.quilldriver.xml.*;
import fieldling.quilldriver.xml.View;

public class QD extends JDesktopPane implements DOMErrorHandler {
        public static final int SCROLLING_HIGHLIGHT_IS_ON = 0;
        public static final int SCROLLING_HIGHLIGHT_IS_OFF = 1;
        protected int mode = SCROLLING_HIGHLIGHT_IS_ON;
        
        public static final int HORIZONTAL_WINDOWS_MEDIA_TO_RIGHT=0;
        public static final int HORIZONTAL_WINDOWS_MEDIA_TO_LEFT=1;
        public static final int VERTICAL_WINDOWS_MEDIA_TOP=2;
        public static final int SUBTITLE_BELOW=3;
        public static final int VIDEO_ONLY_FULL_SCREEN=4;
        public static final int VIDEO_ONLY_NORMAL_SIZE=5;
        public static final int TRANSCRIPT_ONLY=6;
      
        protected int windowsMode=0;
       // protected  Dimension initVideoFrameSize=null;
       // protected  Dimension initTextFrameSize=null;
        protected  Dimension defaultVideoFrameSize=null;    
        protected  Dimension defaultTextFrameSize=null;      
        //protected  Dimension videoFrameNormalSize=null;
        
        protected static Color hColor = Color.cyan;
	//TIBETAN-SPECIFIC protected org.thdl.tib.input.JskadKeyboard activeKeyboard = null;
	protected PanelPlayer player = null;
	protected Editor editor = null;
	protected JInternalFrame videoFrame = null;
	protected JInternalFrame textFrame = null;
	protected Map keyActions = null;
	protected ResourceBundle messages;
	protected TimeCodeModel tcp = null;
	protected Hashtable actions;
        static protected Map qdDefaultProperties;
	//protected JMenu[] configMenus;
	protected View view;
	protected TextHighlightPlayer hp;
	protected DocumentBuilder docBuilder;
	protected File transcriptFile = null;
	protected TagInfo currentTagInfo = null;
	protected Configuration configuration = null;
	public Timer checkTimeTimer = null;
        protected PreferenceManager prefmngr;
        protected Action insertTimesAction = null;
        protected boolean firstQDresize = true;
        
        static {
            org.jdom.Namespace[] qdNamespace = {org.jdom.Namespace.getNamespace("qd", "http://altiplano.emich.edu/quilldriver")};
            XPath xpathEnvironment = XPathUtilities.getXPathEnvironmentForDOM(qdNamespace);
            qdDefaultProperties = new HashMap();
            try {
                qdDefaultProperties.put("qd.timealignednodes", xpathEnvironment.compile("//*[@qd:*]"));
                qdDefaultProperties.put("qd.nodebegins", xpathEnvironment.compile("@qd:t1"));
                qdDefaultProperties.put("qd.nodeends", xpathEnvironment.compile("@qd:t2"));
                qdDefaultProperties.put("qd.nearestplayableparent", xpathEnvironment.compile("ancestor-or-self::*[@qd:*]"));
            } catch (XPathExpressionException xpe) {
                xpe.printStackTrace();
            }
        }
        
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
	}
	private void setupGUI() {
 		setBackground(new JFrame().getBackground());
		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		videoFrame = new JInternalFrame(null, true, false, true, true);//title, resizable, closable, maximizable, iconifiable
		videoFrame.setVisible(true);
		videoFrame.setLocation(0,0);
		videoFrame.setSize(0,0);
		add(videoFrame, JLayeredPane.PALETTE_LAYER);
		invalidate();
		validate();
		repaint();
		textFrame = new JInternalFrame(null, true, false, true, true);//title, resizable, closable, maximizable, iconifiable
		textFrame.setVisible(true);
		textFrame.setLocation(0,0);
		textFrame.setSize(0,0);
		add(textFrame, JLayeredPane.DEFAULT_LAYER);
		invalidate();
		validate();
		repaint();
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
                            if (firstQDresize) {
                                        videoFrame.setLocation(prefmngr.getInt(PreferenceManager.VIDEO_X_KEY, 
                                                getSize().width - videoFrame.getSize().width),
                                                prefmngr.getInt(PreferenceManager.VIDEO_Y_KEY, 0));
                                        textFrame.setLocation(prefmngr.getInt(PreferenceManager.TRANSCRIPT_X_KEY, 0),
                                                prefmngr.getInt(PreferenceManager.TRANSCRIPT_Y_KEY, 0));
                                        textFrame.setSize(prefmngr.getInt(PreferenceManager.TRANSCRIPT_WIDTH_KEY, 
                                                getSize().width - videoFrame.getSize().width),
                                                prefmngr.getInt(PreferenceManager.TRANSCRIPT_HEIGHT_KEY, 
                                                getSize().height));
                                      
                                        firstQDresize = false;
                            }
			}
		});
	}
        
         //------setting textframe and videoframe windows mode--------------        
          public void setHorizontalWindowsMediaToRight(){ // it could remember the size and location
                             /**
                               try{
                                if(videoFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }                                
                                 try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
                              
                                textFrame.setSize(getSize().width*3/4,getSize().height);
                                textFrame.setLocation(0,0);
                                videoFrame.setSize(getSize().width - textFrame.getSize().width, getSize().height/3);
                                videoFrame.setLocation(textFrame.getSize().width,0);//????
                                videoFrame.pack();
                                                        
                                windowsMode=HORIZONTAL_WINDOWS_MEDIA_TO_RIGHT;
                                
                               defaultVideoFrameSize=videoFrame.getSize();    
                               defaultTextFrameSize=textFrame.getSize();
                               
                }
                        
         public void setHorizontalWindowsMediaToLeft(){ //it COULD NOT  remember the location of textFrame,but videoFrame is ok                                                       		       
                                //videoFrame.setSize(initVideoFrameSize.width, initVideoFrameSize.height);                        
                               /**  try{
                                if(textFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }
                                try
                                if(textFrame.isIconifiable()) textFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
                             
                                videoFrame.setSize(getSize().width/4,getSize().height/3);
                                videoFrame.setLocation(0,0);
                             
                                textFrame.setSize(getSize().width-videoFrame.getSize().width,getSize().height);
                                textFrame.setLocation(videoFrame.getSize().width,0);  
                                videoFrame.pack();
                                
                                windowsMode=HORIZONTAL_WINDOWS_MEDIA_TO_LEFT;
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();                               
                }   
          
        public void setVerticalWindowsMediaTop(){   // it could remember the size and location                              
                               /**  try{
                                if(textFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }                               
                                try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
                               
                                videoFrame.setLocation(getSize().width/3,0);
                                videoFrame.setSize(getSize().width/2,getSize().height/2);                               
                                videoFrame.pack();
                                textFrame.setLocation(0,videoFrame.getSize().height);
                                textFrame.setSize(getSize().width, getSize().height-videoFrame.getSize().height);
                                
                                windowsMode=VERTICAL_WINDOWS_MEDIA_TOP;  
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();
        }
        
         public void setSubtitleWindows(){  // it could remember the size and location,but some problem when set this mode, it works same as VerticalWindowsMediaTop                      
                             /**
                                 try{
                                if(textFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }
                                                               
                                try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
   
                                videoFrame.setLocation(getSize().width/3,0);
                                videoFrame.setSize(getSize().width*2/3,getSize().height*7/10);
                                //videoFrame.setSize(0,0);
                                videoFrame.pack();
                                textFrame.setLocation(0,videoFrame.getSize().height);
                                textFrame.setSize(getSize().width, getSize().height-videoFrame.getSize().height);
  
                                windowsMode=SUBTITLE_BELOW;  
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();
        }
         
        public void setVideoOnlyFullScreen(){    //it COULD NOT  remember the location of textFrame and videoFrame                     
                                /**
                                try{
                                if(videoFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }
             
                                try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(true); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                } **/  
            
                                
                                videoFrame.setLocation(0,0);                        
                                videoFrame.setSize(getSize().width, getSize().height);
                                //videoFrame.pack();
                                textFrame.setSize(0,0);
                                textFrame.setLocation(0,0);                      
                                                    
                                windowsMode=VIDEO_ONLY_FULL_SCREEN; 
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();
        }
        
         public void setVideoOnlyNormalSize(){ //it COULD NOT  remember the size of textFrame(is visible after reopen),but videoFrame is ok
                            /**
                                try{
                                if(videoFrame.isIconifiable()) videoFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }
                               
                                try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(true); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }                               
                               **/
             
                                textFrame.setSize(0,0);
                                textFrame.setLocation(0,0);
                                videoFrame.setSize(getSize().width/4,getSize().height/3);
                                videoFrame.setLocation(getSize().width/4, getSize().height/4);
                                videoFrame.pack();                              
                                
                                windowsMode=VIDEO_ONLY_NORMAL_SIZE; 
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();
                                
        }
        
          public void setTranscriptOnly(){ //it COULD NOT  remember the size of videoFrame(it is still visible after reopen),but textFrame is ok                       
                                /**
                                try{
                                if(textFrame.isIconifiable()) textFrame.setIcon(false); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
                                videoFrame.setSize(0,0);
                                videoFrame.setLocation(0,0);                              
                                //videoFrame.pack();                               
                                textFrame.setLocation(0,0);                        
                                textFrame.setSize(getSize().width,getSize().height);                              
                                
                                /**
                                try{
                                if(videoFrame.isIconifiable()) videoFrame.setIcon(true); 
                                }catch(Exception e){
                                    e.printStackTrace();
                                    System.exit(0);
                                }**/
                                windowsMode=TRANSCRIPT_ONLY; 
                                defaultVideoFrameSize=videoFrame.getSize();    
                                defaultTextFrameSize=textFrame.getSize();
        }
       
        public void setDefaultSizes(){
                // we have 7 windows modes and each of them has its default size set by set...()method, but when user drap windows and what is the default size???
                   //I am not sure I understand the meaning of defaultsize.
                     videoFrame.setSize(defaultVideoFrameSize.width,defaultVideoFrameSize.height);
                     textFrame.setSize(defaultTextFrameSize.width,defaultTextFrameSize.height);
                     /**
                         switch(windowsMode){                             
                             case 0: setInitialVerticalWindows(); break;
                             case 1: setVerticalWindows(); break;
                             case 2: setSubtitleWindows(); break;
                             case 3: setFullScreenVideo(); break;
                         }  **/                                         
        } 
        
        
            // used for save the current window mode for other files
        /**
            public void setWindows(){                
                         switch(windowsMode){  
                             //case 0: setVerticalWindows(); break;
                             case 0: setVerticalWindows(); break;
                             case 1: setSubtitleWindows(); break;
                             case 2: setFullScreenVideo(); break;
                         } **/                                             
         
        //---------------------------
        
	private void startTimer() {
		final java.util.Timer timer = new java.util.Timer(true);
		timer.schedule(new TimerTask() {
			public void run()
			{
				if (player.isInitialized())
				{
					timer.cancel();
					videoFrame.getContentPane().setLayout(new BorderLayout());
					videoFrame.getContentPane().add("Center", player);
					videoFrame.pack();
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
		}
	}
	
	private void getTimeCodePreferences() {
		//allows user to change slow adjust, rapid adjust, and play minus parameters
		JPanel slowAdjustPanel = new JPanel(new BorderLayout());
		slowAdjustPanel.setBorder(BorderFactory.createTitledBorder(messages
				.getString("SlowIncreaseDecreaseValue")));
		JTextField slowAdjustField = new JTextField(String
				.valueOf(prefmngr.slow_adjust));
		slowAdjustField.setPreferredSize(new Dimension(240, 30));
		slowAdjustPanel.add(slowAdjustField);
		JPanel rapidAdjustPanel = new JPanel(new BorderLayout());
		rapidAdjustPanel.setBorder(BorderFactory.createTitledBorder(messages
				.getString("RapidDncreaseDecreaseValue")));
		JTextField rapidAdjustField = new JTextField(String
				.valueOf(prefmngr.rapid_adjust));
		rapidAdjustField.setPreferredSize(new Dimension(240, 30));
		rapidAdjustPanel.add(rapidAdjustField);
		JPanel playMinusPanel = new JPanel(new BorderLayout());
		playMinusPanel.setBorder(BorderFactory.createTitledBorder(messages
				.getString("PlayVinusValue")));
		JTextField playMinusField = new JTextField(String
				.valueOf(prefmngr.play_minus));
		playMinusField.setPreferredSize(new Dimension(240, 30));
		playMinusPanel.add(playMinusField);
		JPanel preferencesPanel = new JPanel();
		preferencesPanel.setLayout(new GridLayout(3, 1));
		preferencesPanel.add(slowAdjustPanel);
		preferencesPanel.add(rapidAdjustPanel);
		preferencesPanel.add(playMinusPanel);
		JOptionPane pane = new JOptionPane(preferencesPanel);
		JDialog dialog = pane.createDialog(this, messages
				.getString("TimeCodingPreferences"));
		// This returns only when the user has closed the dialog
		dialog.show();
		int old_slow_adjust = prefmngr.slow_adjust;
		try {
			prefmngr.slow_adjust = Integer.parseInt(slowAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
		int old_rapid_adjust = prefmngr.rapid_adjust;
		try {
			prefmngr.rapid_adjust = Integer
					.parseInt(rapidAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
		int old_play_minus = prefmngr.play_minus;
		try {
			prefmngr.play_minus = Integer.parseInt(playMinusField.getText());
		} catch (NumberFormatException ne) {
		}
		// note: if these become negative numbers no error
		if (old_slow_adjust != prefmngr.slow_adjust)
			prefmngr.setInt(prefmngr.SLOW_ADJUST_KEY, prefmngr.slow_adjust);
		if (old_rapid_adjust != prefmngr.rapid_adjust)
			prefmngr.setInt(prefmngr.RAPID_ADJUST_KEY, prefmngr.rapid_adjust);
		if (old_play_minus != prefmngr.play_minus)
			prefmngr.setInt(prefmngr.PLAY_MINUS_KEY, prefmngr.play_minus);
	}	
	
	public PanelPlayer getMediaPlayer() {
		return player;
	}
        
        protected void changeTranscriptFile(File newFile) {
            if (transcriptFile != null)
                transcriptFile = newFile;
        }
    //DOM FIX!!!
	public boolean saveTranscript() {
            if (transcriptFile == null)
		return true;
            try {
                //serialize XML to file, prettified with indents, and encoded as Unicode (UTF-8)
                org.apache.xml.serialize.OutputFormat formatter = new org.apache.xml.serialize.OutputFormat("xml", "utf-8", true);
                formatter.setPreserveSpace(true); //so as not to remove text nodes that consist only of whitespace, which are significant to QD
                formatter.setLineWidth(0); //prevents line-wrapping (so as not to introduce element-internal whitespace)
                org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(formatter);
                FileOutputStream fous = new FileOutputStream(transcriptFile);
                ser.setOutputByteStream(fous);
                org.apache.xml.serialize.DOMSerializer domser = ser.asDOMSerializer();
                domser.serialize(editor.getXMLDocument());
                return true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
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
	public boolean loadTranscript(File file) {
            return loadTranscript(file, null);
        }
	public boolean loadTranscript(File file, String videoUrl) {
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
	public boolean loadTranscript(URL transcriptURL) {
            return loadTranscript(transcriptURL, null);
        }
	public boolean loadTranscript(URL transcriptURL, String videoUrl) {
		String transcriptString = transcriptURL.toString();
		if (player == null) {
			JOptionPane.showConfirmDialog(QD.this, messages.getString("SupportedMediaError"), null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			return false;
		}
		try {
                        org.w3c.dom.Document xmlDoc = null;
                        try {                       
                            /* I use the xerces-2-java parser because, to my knowledge, it is the only parser that supports
                            DOM revalidation as part of DOM Level 3 Core. This allows documents to be revalidated
                            after each call to NodeTransformer, without reloading the entire document. See.
                                     http://xml.apache.org/xerces2-j/faq-dom.html#faq-9*/
                            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
                            DocumentBuilder docBuilder = configuration.getDocumentBuilder(DocumentBuilderFactory.newInstance());
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
			@TIBETAN@final JTextPane t = new org.thdl.tib.input.DuffPane();
			@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)t;
			@TIBETAN@dp.setByUserTibetanFontSize(PreferenceManager.tibetan_font_size);
			@TIBETAN@dp.setByUserRomanAttributeSet(PreferenceManager.font_face, PreferenceManager.font_size);

			@UNICODE@final JTextPane t = new JTextPane();
			@UNICODE@t.setFont(new Font(PreferenceManager.font_face, java.awt.Font.PLAIN, PreferenceManager.font_size));

			editor = new Editor(xmlDoc, t, currentTagInfo);

			Keymap keymap = editor.getTextPane().addKeymap("Config-Bindings", editor.getTextPane().getKeymap());
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
                            hColor = Color.decode("0x"+PreferenceManager.highlight_color);
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                        hp = new TextHighlightPlayer(view, hColor, prefmngr.highlight_position);
                        
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
			tcp = new TimeCodeModel(hp, configuration.getParameters(), configuration.getNamespaces(), insertTimesAction);
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
				value = XPathUtilities.saxonSelectSingleDOMNodeToString(editor.getXMLDocument(), (XPathExpression)(configuration.getParameters().get("qd.mediaurl")));
				if (value!=null) value = value.trim();
			}
			boolean nomedia = true;
			if (value != null && !value.equals("")) {
				try {
					if (value.startsWith("http:"))
					{
						player.loadMovie(new URL(value));
                                                videoFrame.setTitle(value);
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
						if (mediaFile.exists()) { //open the actual file
							player.loadMovie(mediaFile.toURL());
                                                        videoFrame.setTitle(mediaFile.toString());
							nomedia = false;
						} else if (PreferenceManager.media_directory != null) { //otherwise try default media directory
							String mediaName = value.substring(value.lastIndexOf(QDShell.FILE_SEPARATOR)+1);
							mediaFile = new File(PreferenceManager.media_directory, mediaName);
							if (mediaFile.exists()) {
								player.loadMovie(mediaFile.toURL());
                                                                videoFrame.setTitle(mediaFile.toString());
								nomedia = false;
								//INSERT VIDEO NAME INTO DATA FILE
							}
						}
					}  
				} catch (MalformedURLException murle) {murle.printStackTrace();} //do nothing
			}
			if (nomedia) { //can't find movie: open new movie
                if (videoUrl != null) {
                    try {
                        player.loadMovie(new URL(videoUrl));
                        videoFrame.setTitle(videoUrl);
                        nomedia = false;
                    } catch (MalformedURLException murle) {murle.printStackTrace();}
                } else {
					JFileChooser fc = new JFileChooser(PreferenceManager.media_directory);
					if (fc.showDialog(QD.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
						File mediaFile = fc.getSelectedFile();
						try {
							player.loadMovie(mediaFile.toURL());
                                                        videoFrame.setTitle(mediaFile.toString());
							String mediaString = mediaFile.getAbsolutePath();
							PreferenceManager.media_directory = mediaString.substring(0, mediaString.lastIndexOf(QDShell.FILE_SEPARATOR)+1);
							nomedia = false;
							//INSERT VIDEO NAME INTO DATA FILE
						} catch (MalformedURLException murle) {} //do nothing
					}
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
					//LOGGINGSystem.out.println("mouse clicked on player");
					videoFrame.requestFocus();
				}
			});
			startTimer();

                        textFrame.setTitle(transcriptString); //title=name of transcript file
			final TimeCodeView tcv = new TimeCodeView(player, tcp);
			checkTimeTimer = new Timer(true);
			checkTimeTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					tcv.setCurrentTime(player.getCurrentTime());
				}
			}, 0, 50);
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonPanel.add(tcv);
                        JPanel jp = new JPanel(new BorderLayout());
                        jp.add("North", buttonPanel);
			jp.add("Center", hp);
                        //JTabbedPane tabbedPane = new JTabbedPane();
                        //tabbedPane.add(messages.getString("BasicTranscriptViewMode"), jp);
			JComponent c = (JComponent)textFrame.getContentPane();
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
			try
			{
                            //File transcriptFile = new File (transcriptURL.toURI()); //URL.toURI() is only supported in Java 5.0
                            transcriptFile = new File(new URI(transcriptURL.toString())); //URI supported as of Java 1.4
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
			if (mode == SCROLLING_HIGHLIGHT_IS_ON)
                            player.setAutoScrolling(true); //otherwise the first time you press Play you don't get highlights in the text window!!
			//TIBETAN-SPECIFIC if (activeKeyboard != null) changeKeyboard(activeKeyboard); //this means that keyboard was changed before constructing a DuffPane
			return true;
		} catch (PanelPlayerException smpe) {
			smpe.printStackTrace();
			transcriptFile = null;
			return false;
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
			if (mode == SCROLLING_HIGHLIGHT_IS_ON)
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
            if (editor != null) {
                currentTagInfo = newTagInfo;
                editor.setTagInfo(currentTagInfo);
                hp.refresh();
            }
        }
	public boolean configure(Configuration configuration) {
		if (transcriptFile != null) return false;
		try {
                    configuration.configure(qdDefaultProperties);
                    if (configuration.getJMenuBar() != null)
                        textFrame.setJMenuBar(configuration.getJMenuBar());
                    this.configuration = configuration;
                } catch (TransformerException tre) {
                    tre.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (javax.xml.parsers.ParserConfigurationException pce) {
                    pce.printStackTrace();
                } catch (org.xml.sax.SAXException saxe) {
                    saxe.printStackTrace();
                }
                Map parameters = configuration.getParameters();
                Transformer transformer = configuration.getTranscriptTransformer();
                TagInfo[] tagInfo = configuration.getTagInfo();
                Map actionProfiles = configuration.getActionProfiles();
                keyActions = new HashMap(); //maps keys to actions
                Iterator itty = actionProfiles.values().iterator();
                while (itty.hasNext()) {
                    final Configuration.QdActionDescription qdActionDesc = (Configuration.QdActionDescription)itty.next();
                    Action keyAction = Configuration.getActionForActionDescription(qdActionDesc);
                    if (qdActionDesc.getXSLTask() != null && qdActionDesc.getXSLTask().equals("qd.insertTimes"))
                        insertTimesAction = keyAction;
                    keyActions.put(qdActionDesc.getKeyboardShortcut(), keyAction); //eventually to be registered with transcript's JTextPane
                }
                if (tagInfo.length > 1) {
                    for (int i=0; i<tagInfo.length; i++) {
                        KeyStroke key = tagInfo[i].getKeyboardShortcut();
                        Action keyAction = Configuration.getActionForTagInfoChange(tagInfo[i]);
                        if ( !(key == null || keyAction == null) )
                            keyActions.put(key, keyAction);
                    }
                }
                currentTagInfo = tagInfo[0];
                return true;
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
            Iterator itty = configuration.getParameters().keySet().iterator();
            while (itty.hasNext()) {
                String key = (String)itty.next();
                XPathExpression xpath = (XPathExpression)configuration.getParameters().get(key);
                if (!key.startsWith("qd.")) {
                    org.w3c.dom.Node domNodeParam = XPathUtilities.saxonSelectSingleDOMNode(domContextNode, xpath);
                    parameters.put(key, domNodeParam);
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
            float slowInc = (float)PreferenceManager.slow_adjust;
            float rapidInc = (float)PreferenceManager.rapid_adjust;
            parameters.put("qd.slowincrease", new Float(slowInc/1000));
            parameters.put("qd.rapidincrease", new Float(rapidInc/1000));
            //send the name of the current media URL
            parameters.put("qd.mediaurlstring", player.getMediaURL().toString());
            return parameters;
        }
                                                                        
                                                                                      /*The DOM2DTM class serves up a DOM's contents via the DTM API. 
                                                                                      Note that it doesn't necessarily represent a full Document tree. You can 
                                                                                      wrap a DOM2DTM around a specific node and its subtree and the right 
                                                                                      things should happen. (I don't _think_ we currently support DocumentFrgment 
                                                                                      nodes as roots, though that might be worth considering.) Note too that we 
                                                                                      do not currently attempt to track document mutation. If you alter the DOM 
                                                                                      after wrapping DOM2DTM around it, all bets are off.                               
                                                                                        *///LOGGING
                                  /* org.w3c.dom.Element nd = (org.w3c.dom.Element)obj;
                                    try {
                                        System.out.println("\n------OBJ\n");
                                    org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(new org.apache.xml.serialize.OutputFormat("xml", "utf-8", true));
                                    ser.setOutputByteStream(System.out);
                                        org.apache.xml.serialize.DOMSerializer domser = ser.asDOMSerializer();
                                    domser.serialize(nd);
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    }*/
                                    //ENDLOGGING
                
	public void executeCommand(String command) {
		//FIXME: These commands should be defined elsewhere, in programmatically extensible classes
		boolean result=true;
		
        if (command.equals("playNode")) {
            Node nearestParent = XPathUtilities.saxonSelectSingleDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), (XPathExpression)(configuration.getParameters().get("qd.nearestplayableparent")));
            playNode(nearestParent);
        }
        else if (command.equals("playPause")) {
            try {
                /* by transferring focus, we don't have to worry about problems caused by
                the cursor position in the editor being different from the highlight,
                since users will have to click on the editor to get back into editing */
                if (mode == SCROLLING_HIGHLIGHT_IS_ON)
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
                Node nearestParent = XPathUtilities.saxonSelectSingleDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), (XPathExpression)(configuration.getParameters().get("qd.nearestplayableparent")));
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
                Node nearestParent = XPathUtilities.saxonSelectSingleDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), (XPathExpression)(configuration.getParameters().get("qd.nearestplayableparent")));
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
                Node nearestParent = XPathUtilities.saxonSelectSingleDOMNode(editor.getNodeForOffset(editor.getTextPane().getCaret().getMark()), (XPathExpression)(configuration.getParameters().get("qd.nearestplayableparent")));
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
        else if (command.equals("saveTranscript")) {
            if (getEditor().isEditable())
                result = saveTranscript();
            if (!result)
            	JOptionPane.showMessageDialog(null, messages.getString("FileCouldNotBeSaved"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);
        }
        else if (command.equals("timeCode")) {
        	getTimeCodePreferences();
        }
	}

	//TIBETAN-SPECIFIC public org.thdl.tib.input.JskadKeyboard getKeyboard() {
		//TIBETAN-SPECIFIC return activeKeyboard;
	//TIBETAN-SPECIFIC }
	//TIBETAN-SPECIFIC public void changeKeyboard(org.thdl.tib.input.JskadKeyboard kbd) {
		//TIBETAN-SPECIFIC activeKeyboard = kbd;
		//TIBETAN-SPECIFIC if (editor == null || !(editor.getTextPane() instanceof org.thdl.tib.input.DuffPane)) return;
		//TIBETAN-SPECIFIC org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)editor.getTextPane();
		//TIBETAN-SPECIFIC kbd.activate(dp);
	//TIBETAN-SPECIFIC }
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
                

                
                                
 /*               Iterator pitty = keyActions.keySet().iterator();
                while (pitty.hasNext()) {
                    Object next = (pitty.next();
                    
                mItem.setAccelerator(key);
                mItem.addActionListener(new ActionListener() { //so that keystrokes are valid even when transcript is not in focus
                    public void actionPerformed(ActionEvent e) {
                        keyAction.actionPerformed(e);
                    }
                });
                configMenus[xCount].add(mItem);
                */