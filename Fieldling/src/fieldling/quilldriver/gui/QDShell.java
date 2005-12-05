/* ***** BEGIN LICENSE BLOCK *****
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

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.event.*;
import fieldling.quilldriver.PreferenceManager;
import fieldling.quilldriver.config.*;
import fieldling.quilldriver.xml.*;
import fieldling.mediaplayer.*;
import fieldling.util.*;
import java.awt.print.*;     
import javax.print.*;        
import javax.print.event.*;  
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.xml.xpath.*;

public class QDShell extends JFrame 
{
	/** the middleman that keeps code regarding Tibetan keyboards
	 *  clean */
	
	/*
	 private final static JskadKeyboardManager keybdMgr
	 = new JskadKeyboardManager(JskadKeyboardFactory.getAllAvailableJskadKeyboards());
	 */
	
	/** When opening a file, this is the only extension QuillDriver
	 cares about.  This is case-insensitive. */
	protected final static String dotQuillDriver = ".xml";
	protected final static String dotQuillDriverTibetan = ".qdt";
	
	ResourceBundle messages = null;
	
	QD qd = null;
	
	QD tempQD=null;//save qd before open another transcript
	public static final int MAXIMUM_NUMBER_OF_OPEN_FILES = 4; 
	private java.util.List openFileList = new ArrayList();
	private java.util.List qdList = new ArrayList();
	private Map openTranscriptToQDMap=new HashMap();
        public static boolean[] requestUniqueIndex=new boolean[4];
        
	Container contentPane;
	private static boolean hasLoadedTranscript = false;
	
	PreferenceManager prefmngr = new fieldling.quilldriver.PreferenceManager();
	
	private static int numberOfQDsOpen = 0;
	
	/* public static final String NEW_FILE_ERROR_MESSAGE =
	 "This file already exists! Type a new file name\n" +
	 "instead of selecting an existing file.";*/
	public static final String FILE_SEPARATOR = System
	.getProperty("file.separator");
	
	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;
	public static final int CLOSE_APPLICATION = 0;
	public static final int CLOSE_WINDOW = 1;
	public static final int CLOSE_TRANSCRIPT = 2;
	private JComboBox defaultLanguage, supportedFonts;
        
	private static void printSyntax()
	{
		System.out.println("Syntax: QDShell [-THDLTranscription | -THDLReadonly  transcript-file]");
	}
	
	public static void main(String[] args)
	{
		/* i have chosen the Metal look and feel to get around one apparent bug with
		 the Windows look and feel (see below). for us, the problem is that if you
		 maximize the transcript, and then click on the video, then the video suddenly
		 maximizes and you can't see the transcript any more--not what we want!
		 
		 http://forum.java.sun.com/thread.jspa?forumID=57&threadID=586119
		 Problem with "Windows Look N Feel"
		 Author: gulshan21  Posts: 63   Registered: 10/1/03
		 Jan 11, 2005 10:43 PM 	
		 Hi, I have a JFrame in which there are several JInternalFrames. 
		 Am actually using the Windows Look N Feel. The problem is that 
		 when I maximised one of those JInternalFrame all of them got 
		 maximised and when I minimised one of them again, all got 
		 minimised. But when using other Look N Feel apart from 
		 Windows, everything works fine. Can anyone please explain 
		 me what's the cause of the problem and if possible send me 
		 the solution.*/
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) { }
		/*try {
		 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 } catch (Exception e) { }*/
		PrintStream ps=null;
		try 
		{
			//ThdlDebug.attemptToSetUpLogFile("qd", ".log");
			/* I am using saxon-b 8.x as my Transformer for two reasons:
			 (1) It supports XSLT 2.0, which is handy
			 (2) Unlike xalan, it has the nice feature that if you transform a node, 
			 you can get access to the entire document connected to that node. As
			 Michael Kay writes, "Generally I"ve followed what Microsoft did (they were 
			 the ones who introduced the idea of starting a transformation at a node 
			 other than the root), which is that the start node is not detached from 
			 the tree it belonged to - you can still do ancestor::x to find its 
			 original ancestors." For discussion, see the following saxon-help post:
			 http://sourceforge.net/mailarchive/message.php?msg_id=2997255*/
			System.setProperty("javax.xml.transform.TransformerFactory",
			"net.sf.saxon.TransformerFactoryImpl");
			//		"org.apache.xalan.processor.TransformerFactoryImpl");
			
			try 
			{
				ps = new PrintStream(new FileOutputStream(System
						.getProperty("user.home")
						+ "/qd.log"));
				System.setOut(ps);
				System.setErr(ps);
			} catch (FileNotFoundException fnfe) {
			}
			if (args.length==0) 
			{
				new QDShell();
				return;
			}
			
			if (args.length==1)
			{
				System.out.println("Syntax error: missing arguments!");
				printSyntax();
				return;
			}
			if (args[0].charAt(0)!='-')
			{
				System.out.println("Syntax error: option expected!");
				printSyntax();
				return;
			}
			
			String option = args[0].substring(1);
			String configName;
			// for now only these two options are available. More to come...
			if (!option.equals("THDLTranscription") && !option.equals("THDLReadonly"))
			{
				System.out.println("Syntax error: invalid option \"" + option + "\"!");
				printSyntax();
				return;
			}
			
			File transcriptFile = new File (args[1]);
			if (!transcriptFile.exists())
			{
				System.out.println("Error reading file!");
				return;
			}
			// if arguments are passed through the command-line default to Quick-time for Java
			new QDShell(transcriptFile, option, "QuicktimeforJava");
			
		} catch (NoClassDefFoundError err) {
		}
	}
	
	public File selectTranscriptFile(String message) 
	{
		JFileChooser fc = new JFileChooser(new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));
		fc.addChoosableFileFilter(new QDFileFilter());
		if (fc.showDialog(QDShell.this, message) == JFileChooser.APPROVE_OPTION) 
		{
			File transcriptFile = fc.getSelectedFile();
			return transcriptFile;
		} 
		else 
		{
			return null;
		}
	}
     /**
      *change title with view  
      */
     public void changeTitle(int i){    
        qd.setCurrentLang(i);
        newTitle();
        }
     /**
      *update title when title changed
      */
      public void updateWindowsTitle(){ 
           if(qd.editor != null){
               newTitle();
           }
       }
      public void newTitle(){
        //String topTitle=qd.getWindowTitle(false, qd.getCurrentLang());         
        String menuTitle=qd.getWindowTitle(qd.getCurrentLang());
        if(openFileList.contains(menuTitle)){              
              if(qd.index==0){
                 QD tempQD=(QD)openTranscriptToQDMap.get(menuTitle);
                 String subStr="["+tempQD.index+"]";
                 String title=menuTitle.concat(subStr);
                 tempQD.setOldTitle(title);
                 openFileList.set(openFileList.indexOf(menuTitle), title);
                 openTranscriptToQDMap.remove(menuTitle);
                 openTranscriptToQDMap.put(title,tempQD);
              }
              else
                {
                 String count=new Integer(qd.index).toString();
                 String subStr="["+count+"]";
                 menuTitle=menuTitle.concat(subStr);
                }
          }   
        String oldTitle=qd.getOldTitle(); 
        qd.setOldTitle(menuTitle);
        //QDShell.this.setTitle(topTitle);
        QDShell.this.setTitle(menuTitle+"-"+qd.productName);
        openFileList.set(openFileList.indexOf(oldTitle), menuTitle);   
        qd=(QD)openTranscriptToQDMap.remove(oldTitle);
        openTranscriptToQDMap.put(menuTitle,qd);     
        setJMenuBar(getQDShellMenu());
	setVisible(true);
      }
	class Wizard extends JDialog {
		public Wizard() {
			//choice of configuration
			super(QDShell.this, messages.getString("Open"), true);
			setSize(new Dimension(600,400));
			addWindowListener(new WindowAdapter () {
				public void windowClosing (WindowEvent e) {
					Wizard.this.dispose();
				}
			});
			
			JPanel configurationChoice = new JPanel();
			configurationChoice.setBorder(BorderFactory.createTitledBorder(messages.getString("SelectConfiguration")));
			final Configuration[] configurations = ConfigurationFactory.getAllQDConfigurations(QDShell.this.getClass().getClassLoader());
			final ButtonGroup configGroup = new ButtonGroup();
			final JRadioButton[] configItems = new JRadioButton[configurations.length];
			for (int i = 0; i < configurations.length; i++) {
				configItems[i] = new JRadioButton(messages.getString(configurations[i].getName()));
				configItems[i].setActionCommand(String.valueOf(i));
				configGroup.add(configItems[i]);
			}
			String configName = prefmngr.getValue(prefmngr.CONFIGURATION_KEY,configurations[0].getName());
			int j = 0;
			for (j = 0; j < configItems.length; j++) {
				if (configName.equals(configurations[j].getName())) {
					configItems[j].setSelected(true);
					break;
				}
			}
			if (j == configItems.length) { // in case the saved configuration doesn't actually exist (e.g. has been renamed)
				configItems[0].setSelected(true);
			}
			configurationChoice.setLayout(new GridLayout(0, 1));
			for (int k = 0; k < configItems.length; k++) {
				configurationChoice.add(configItems[k]);
			}
			
			// load by default?
			boolean useWizard = prefmngr.getInt(prefmngr.USE_WIZARD_KEY, 1)==1;
			JCheckBox openWizardAutomatically = new JCheckBox(messages.getString("AutomaticallyOpenDialogBox"), useWizard);
			openWizardAutomatically.addItemListener(new ItemListener()
					{
				public void itemStateChanged(ItemEvent e) 
				{
					prefmngr.setInt(prefmngr.USE_WIZARD_KEY, e.getStateChange()==e.SELECTED?1:-1);
				}				
					});
			
			//choice of video player
			JPanel moviePlayerChoice = new JPanel();
			moviePlayerChoice
			.setBorder(BorderFactory.createTitledBorder(messages
					.getString("SelectVideoPlayer")));
			java.util.List moviePlayers = PlayerFactory
			.getAllAvailablePlayers();
			ButtonGroup mediaGroup = new ButtonGroup();
			JRadioButton[] mediaItems = new JRadioButton[moviePlayers.size()];
			for (int i = 0; i < moviePlayers.size(); i++) {
				final PanelPlayer mPlayer = (PanelPlayer) moviePlayers.get(i);
				mediaItems[i] = new JRadioButton(messages.getString(mPlayer.getIdentifyingName()));
				mediaItems[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						qd.setMediaPlayer(mPlayer);
						prefmngr.setValue(prefmngr.MEDIA_PLAYER_KEY, mPlayer
								.getIdentifyingName());
					}
				});
				mediaGroup.add(mediaItems[i]);
			}
			if (mediaItems.length > 0) {
				PanelPlayer mPlayer = (PanelPlayer) moviePlayers.get(0);
				String myPlayerName = prefmngr
				.getValue(prefmngr.MEDIA_PLAYER_KEY, mPlayer
						.getIdentifyingName());
				if (myPlayerName.equals(mPlayer.getIdentifyingName())) { //user's player identical to QD's default player
					mediaItems[0].setSelected(true);
					qd.setMediaPlayer(mPlayer);
				} else {
					int i;
					PanelPlayer thisPlayer = null;
					for (i = 0; i < mediaItems.length; i++) {
						thisPlayer = (PanelPlayer) moviePlayers.get(i);
						if (thisPlayer.getIdentifyingName()
								.equals(myPlayerName))
							break;
					}
					if (i == mediaItems.length) { //could not find user's chosen media player
						mediaItems[0].setSelected(true);
						qd.setMediaPlayer(mPlayer); //qd media player defaults to
					} else { //found user's chosen media player
						mediaItems[i].setSelected(true);
						qd.setMediaPlayer(thisPlayer);
					}
				}
			}
			moviePlayerChoice.setLayout(new GridLayout(0, 1));
			for (int i = 0; i < mediaItems.length; i++)
				moviePlayerChoice.add(mediaItems[i]);
			
			//file choice: new transcript, open existing, or open recent
			JPanel dataSourceChoice = new JPanel();
			dataSourceChoice
			.setBorder(BorderFactory.createTitledBorder(messages
					.getString("SelectDataSource")));
			java.util.List recentFileList = new ArrayList();
			java.util.List recentVideoList = new ArrayList();
			String r = prefmngr.getValue(prefmngr.RECENT_FILES_KEY, null);
			String rv = prefmngr.getValue(prefmngr.RECENT_VIDEOS_KEY, null);
			if (r != null) {
				StringTokenizer tok = new StringTokenizer(r, ",");
				while (tok.hasMoreTokens()) {
					recentFileList.add((String) tok.nextToken());
				}
			}
			if (rv != null) {
				StringTokenizer tok = new StringTokenizer(rv, ",");
				while (tok.hasMoreTokens()) {
					recentVideoList.add((String) tok.nextToken());
				}
			}
			String[] recentVideos = (String[]) recentVideoList
			.toArray(new String[0]);
			final ButtonGroup dataSourceGroup = new ButtonGroup();
			JRadioButton[] dataSourceButtons = new JRadioButton[2 + recentFileList
			                                                    .size()];
			dataSourceButtons[0] = new JRadioButton(messages
					.getString("NewTranscriptText"));
			dataSourceButtons[1] = new JRadioButton(messages
					.getString("OpenExisting"));
			dataSourceButtons[0].setActionCommand(messages
					.getString("NewTranscriptText"));
			dataSourceButtons[1].setActionCommand(messages
					.getString("OpenExisting"));
			Iterator itty = recentFileList.iterator();
			int count = 2;
			final Map recentTranscriptToRecentVideoMap = new HashMap();
			while (itty.hasNext()) {
				String recentFile = (String) itty.next();
				dataSourceButtons[count] = new JRadioButton(recentFile);
				dataSourceButtons[count].setActionCommand(recentFile);
				if (recentVideos.length > count - 2)
					recentTranscriptToRecentVideoMap.put(recentFile,
							recentVideos[count - 2]);
				count++;
			}
			dataSourceChoice.setLayout(new GridLayout(0, 1));
			for (int i = 0; i < dataSourceButtons.length; i++) {
				dataSourceGroup.add(dataSourceButtons[i]);
				dataSourceChoice.add(dataSourceButtons[i]);
			}
			if (count > 2)
				dataSourceButtons[2].setSelected(true); //open most recent file by default
			else
				dataSourceButtons[0].setSelected(true); //if no recent files, assume new transcript
			
			//cancel button: quit QuillDriver
			JButton cancelButton = new JButton(messages.getString("Cancel"));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Wizard.this.setVisible(false);
					Wizard.this.dispose();
					if(tempQD!=null){
						qd=tempQD;
						tempQD=null;
						contentPane.add(qd);
						contentPane.repaint();
					}
				}
			});
			
			//ok button: make changes
			JButton okButton = new JButton(messages.getString("Ok"));
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean noProblems = true;
                                        boolean cancelAction=false;
					try {
						
						if(tempQD!=null){
							contentPane.remove(tempQD);
							//tempQD=null;
							contentPane.repaint();  }
						
						ButtonModel selectedConfiguration = configGroup.getSelection();
						String configCommand = selectedConfiguration.getActionCommand();
						int i = Integer.parseInt(configCommand);
						qd.configure(configurations[i]);
						prefmngr.setValue(prefmngr.CONFIGURATION_KEY,configurations[i].getName());
						prefmngr.setValue(prefmngr.MEDIA_PLAYER_KEY, qd.player.getIdentifyingName());
						
						ButtonModel selectedRb = dataSourceGroup.getSelection();
						String command = selectedRb.getActionCommand();
						if (command.equals(messages.getString("NewTranscriptText"))) {
							// File newTemplateFile = new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home")) + qd.newTemplateFileName);
							System.out.println("New Template = " + qd.configuration.getNewTemplate());
							URL newTemplateURL = QDShell.this.getClass().getClassLoader().getResource(qd.configuration.getNewTemplate());
							System.out.println(newTemplateURL.toString());
							if (newTemplateURL == null)
								System.exit(0); //FIX
							/*if (!newTemplateFile.exists())
							 System.exit(0); //FIX*/
							
							 File saveAsFile = selectTranscriptFile(messages.getString("SaveTranscriptAs"));
                                                    
                                                         if(saveAsFile!=null){
								InputStream in = newTemplateURL.openStream();
								OutputStream out = new FileOutputStream(saveAsFile);
								// Transfer bytes from in to out
								byte[] buf = new byte[1024];
								int len;
								while ((len = in.read(buf)) > 0) {
									out.write(buf, 0, len);
								}
								in.close();
								out.close();
								
								noProblems = qd.loadTranscript(saveAsFile);       
								if (noProblems)
								{
									String transcriptString = saveAsFile.getAbsolutePath();
									prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
									makeRecentlyOpened(transcriptString);
									makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());									
								}
							}
							if(saveAsFile==null)                                                             
								cancelAction=true; //when user chooses to cancel create a new transcript  
						} else if (command.equals(messages.getString("OpenExisting"))) {
							File transcriptFile = selectTranscriptFile(messages.getString("OpenTranscript"));
							if (transcriptFile != null) {
								        String transcriptString = transcriptFile.getAbsolutePath();
									prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0,transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
									noProblems = qd.loadTranscript(transcriptFile);
									if (noProblems)
									{
										makeRecentlyOpened(transcriptString);
										makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());                                                                              
									}              								
							}
							else cancelAction=true;// when user choose to cancel open existing
						} 
						else 
						{ //must be recent file
							        File transcriptFile = new File(command);
								Object video = recentTranscriptToRecentVideoMap.get(command);
								if (video == null)
									noProblems = qd.loadTranscript(transcriptFile);
								else
									noProblems = qd.loadTranscript(transcriptFile, (String) video);
								if (noProblems)
								{                                                        
									makeRecentlyOpened(command);
									makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());                                                                    
								}
							
						}
						
						//Wizard.this.hide();
						Wizard.this.dispose();
						
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						noProblems = false;
					}
					catch (Throwable th)
					{
						th.printStackTrace();
						noProblems = false;
					}
					
					if (!cancelAction&&noProblems) 
					{       
						String menuTitle=qd.getWindowTitle(qd.getCurrentLang());
						//String topTitle=qd.getWindowTitle(false,qd.getCurrentLang());                                      
                                                for(int i=0;i<4;i++){
                                                    if (requestUniqueIndex[i]==false){
                                                        qd.index=i;
                                                      requestUniqueIndex[i]=true;
                                                      break;
                                                    }
                                                }
                                                if(openFileList.contains(menuTitle)){                                                    
                                                      if(qd.index==0){
                                                          QD tempQD=(QD)openTranscriptToQDMap.get(menuTitle);
                                                          String subStr="["+tempQD.index+"]";
                                                          String title=menuTitle.concat(subStr);
                                                          tempQD.setOldTitle(title);
                                                          openFileList.set(openFileList.indexOf(menuTitle), title);
                                                          openTranscriptToQDMap.remove(menuTitle);
                                                          openTranscriptToQDMap.put(title,tempQD);
                                                       }
                                                  else
                                                      {
                                                       String count=new Integer(qd.index).toString();
                                                       String subStr="["+count+"]";
                                                       menuTitle=menuTitle.concat(subStr);
                                                       }
                                                    
                                                }                                                  
                                                QDShell.this.setTitle(menuTitle+"-"+qd.productName);
                                                openFileList.add(menuTitle);
						qdList.add(qd);
						openTranscriptToQDMap.put(menuTitle,qd);
                                                qd.setOldTitle(menuTitle);
						contentPane.add(qd);
						qd.videoFrame.pack();
						numberOfQDsOpen++;  
						hasLoadedTranscript = true;
                                                qd.setQDShell(QDShell.this);
					}
					else
					{
						Wizard.this.dispose();
						
						if(tempQD!=null){
							qd=tempQD;                            
							contentPane.add(qd);
							contentPane.repaint();			        
						}						
						if(!noProblems)
							JOptionPane.showMessageDialog(QDShell.this, messages.getString("FileCouldNotBeLoaded"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);                                                                                             
					}
					tempQD=null;
				}
			});
			JPanel northChoices = new JPanel(new GridLayout(1, 0));
			JPanel upperHalfChoices = new JPanel (new BorderLayout());
			JPanel defaultPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
			defaultPanel.add(openWizardAutomatically);
			northChoices.add(configurationChoice);
			northChoices.add(moviePlayerChoice);
			upperHalfChoices.add(BorderLayout.NORTH, defaultPanel);
			upperHalfChoices.add(BorderLayout.CENTER, northChoices);			
			JPanel choices = new JPanel(new GridLayout(0, 1));
			choices.add(upperHalfChoices);
			choices.add(dataSourceChoice);
			JPanel buttons = new JPanel();
			buttons.add(okButton);
			buttons.add(cancelButton);
			JPanel content = new JPanel(new BorderLayout());
			content.add("Center", new JScrollPane(choices));
			content.add("South", buttons);
			getContentPane().add(content);
		}
	}
	
	/** Constructor for generic stuff loaded regardless of how to get initial state (wizard, command-line,
	 * or defaults in pref manager). Not meant to be called directly!!! */
	private void loadGenericInitialState()
	{
		//numberOfQDsOpen++;
		setTitle(QD.productName);
		defaultLanguage = null;
		supportedFonts = null;
		messages = I18n.getResourceBundle();
		setLocation(prefmngr.getInt(prefmngr.WINDOW_X_KEY, 0), prefmngr.getInt(prefmngr.WINDOW_Y_KEY, 0));
		setSize(new Dimension(prefmngr.getInt(prefmngr.WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width), prefmngr.getInt(prefmngr.WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));
		qd = new QD(prefmngr);
                qd.setQDShell(QDShell.this);
		contentPane = getContentPane();
		for (int i=0;i<4;i++)
                    requestUniqueIndex[i]=false;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				closeThisQDFrame(QDShell.CLOSE_WINDOW);
			}
		});
	}
	
	private void loadSpecificInitialStateFromWizard()
	{
		Wizard wiz = new Wizard();
		wiz.setVisible(true);		
	}
	
	
	private void loadSpecificInitialStateFromDefaults() throws Exception
	{
		File transcriptFile;
		String transcriptFileName, s, mediaURL, configName, mediaPlayer;;
		int pos;
		
		s = prefmngr.getValue(prefmngr.RECENT_FILES_KEY, null);
		
		pos = s.indexOf(",");
		if (pos==-1) transcriptFileName = s.trim();
		else transcriptFileName = s.substring(0,pos).trim();
		
		transcriptFile = new File(transcriptFileName);
		if (!transcriptFile.exists()) throw new Exception("Transcription file not found!");
		
		s = prefmngr.getValue(prefmngr.RECENT_VIDEOS_KEY, null);
		pos = s.indexOf(",");
		if (pos==-1) mediaURL = s.trim();
		else mediaURL = s.substring(0,pos).trim();
		
		configName = prefmngr.getValue(prefmngr.CONFIGURATION_KEY, null);
		mediaPlayer = prefmngr.getValue(prefmngr.MEDIA_PLAYER_KEY, null);
		
		loadSpecificInitialState(transcriptFile, mediaURL, configName, mediaPlayer);		
	}
	
	private void loadSpecificInitialState(File transcriptFile, String configName, String mediaName)
	{
		loadSpecificInitialState(transcriptFile, null, configName, mediaName);
	}
	
	private void loadSpecificInitialState(File transcriptFile, String mediaURL, String configName, String mediaName)
	{		
		contentPane.add(qd);
		
		String transcriptString = transcriptFile.getAbsolutePath();
		prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0,transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
		
		final Configuration[] configurations = ConfigurationFactory.getAllQDConfigurations(QDShell.this.getClass().getClassLoader());
		int j;
		for (j = 0; j < configurations.length; j++) {
			if (configurations[j].getName().equals(configName))
			{
				qd.configure(configurations[j]);
				break;
			}
		}
		
		java.util.List moviePlayers = PlayerFactory.getAllAvailablePlayers();
		PanelPlayer mPlayer;
		
		for (j=0; j<moviePlayers.size(); j++)
		{
			mPlayer = (PanelPlayer) moviePlayers.get(j);
			if (mPlayer.getIdentifyingName().equals(mediaName))
			{
				qd.setMediaPlayer(mPlayer);
				break;
			}
		}
		if (mediaURL==null)
		{
			qd.loadTranscript(transcriptFile);
			makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
		}
		else
		{
			qd.loadTranscript(transcriptFile, mediaURL);
			makeRecentlyOpenedVideo(mediaURL);
		}
		makeRecentlyOpened(transcriptString);
		
	}
	
	public QDShell(int useWizard)
	{
		loadGenericInitialState();
		
		try
		{
			if (useWizard==-1)
			{
				loadSpecificInitialStateFromDefaults();
			}
		}
		catch (Exception e)
		{
			useWizard=1;
		}
		
		if (useWizard==1)
		{
			// load wizard
			Wizard wiz = new Wizard();
			wiz.setVisible(true);		
		}
		if (hasLoadedTranscript)
		{
			setJMenuBar(getQDShellMenu());
			setVisible(true);
		}
		else closeThisQDFrame(QDShell.CLOSE_TRANSCRIPT);
	}
	
	public QDShell()
	{
		loadGenericInitialState();
		
		int useWizard = prefmngr.getInt(prefmngr.USE_WIZARD_KEY, 1);
		
		/*try
		 {
		 if (useWizard==-1) loadSpecificInitialStateFromDefaults();
		 }
		 catch (Exception e)
		 {
		 useWizard=1;
		 }*/
		
		setJMenuBar(getQDShellMenu());
		setVisible(true);
		
		if (useWizard==1) // load wizard
		{
			Wizard wiz = new Wizard();
			wiz.setVisible(true);
		}
	}
	
	
	public QDShell(File transcriptFile, String configName, String mediaName)
	{
		loadGenericInitialState();
		// not loading wizard
		
		loadSpecificInitialState(transcriptFile, configName, mediaName);		
		
		setJMenuBar(getQDShellMenu());
		setVisible(true);
	}
	
	private void putPreferences() {
		prefmngr.setInt(prefmngr.WINDOW_X_KEY, getX());
		prefmngr.setInt(prefmngr.WINDOW_Y_KEY, getY());
		prefmngr.setInt(prefmngr.WINDOW_WIDTH_KEY, getWidth());
		prefmngr.setInt(prefmngr.WINDOW_HEIGHT_KEY, getHeight());
		if (qd.getEditor() != null) {
			Rectangle tRec = qd.textFrame.getBounds();
			prefmngr.setInt(prefmngr.TRANSCRIPT_X_KEY, tRec.x);
			prefmngr.setInt(prefmngr.TRANSCRIPT_Y_KEY, tRec.y);
			prefmngr.setInt(prefmngr.TRANSCRIPT_HEIGHT_KEY, tRec.height);
			prefmngr.setInt(prefmngr.TRANSCRIPT_WIDTH_KEY, tRec.width);
			Rectangle vRec = qd.videoFrame.getBounds();
			prefmngr.setInt(prefmngr.VIDEO_X_KEY, vRec.x);
			prefmngr.setInt(prefmngr.VIDEO_Y_KEY, vRec.y);
			prefmngr.setInt(prefmngr.VIDEO_HEIGHT_KEY, vRec.height);
			prefmngr.setInt(prefmngr.VIDEO_WIDTH_KEY, vRec.width);
		}
		prefmngr.setValue(prefmngr.MEDIA_DIRECTORY_KEY, prefmngr.media_directory);
		prefmngr.setInt(prefmngr.WINDOW_MODE_KEY, qd.getWindowsMode());
		if(getQD().getBothResize()) 
			prefmngr.setInt(prefmngr.BOTH_RESIZE_KEY,1);
		else
			prefmngr.setInt(prefmngr.BOTH_RESIZE_KEY,0);
	}
	
	
	private void closeThisQDFrame(int closeMode) {  
		/*i first used dispose() instead of hide(), which should clear up memory,
		 but i got an error: can't dispose InputContext while it's active
		 Note by Andres: This error seems to be fixed on JDK 1.5, so changed back
		 to dispose and things seem fine. */
		putPreferences();
		if(closeMode==QDShell.CLOSE_APPLICATION){
			System.exit(0);
		}
		if (hasLoadedTranscript)
		{
			Editor editor = qd.getEditor();                     
			if (editor != null) //no content in this QD window
			{ //there's a QD editor: save and close
			      
                               if (editor.isEditable() && editor.hasChangedSinceLastSaved())
				{
				       
                                       int option = JOptionPane.showConfirmDialog(this, messages.getString("WantToSaveChanges"), "QuillDriver", JOptionPane.YES_NO_OPTION);					
                                        if (option==JOptionPane.YES_OPTION)
                                           qd.saveTranscript();                                          						
				}
				openFileList.remove(qd.getOldTitle());            
				openTranscriptToQDMap.remove(qd.getOldTitle()); 
				qdList.remove(qd);                          
                                requestUniqueIndex[qd.index]=false;
			}                                                    
			
			numberOfQDsOpen--;
			switch (closeMode)
			{
			case QDShell.CLOSE_WINDOW:                             
				dispose();                                                     
				if (numberOfQDsOpen == 0)
				{       
					System.exit(0);
				}              
				
				break;
			case QDShell.CLOSE_TRANSCRIPT:    
				qd.removeContent();                         
				contentPane.remove(qd); 
				contentPane.repaint();                             
				if (numberOfQDsOpen == 0)
				{				
					hasLoadedTranscript = false;
					QDShell.this.setTitle(qd.productName);
				} else
				{  
					qd=(QD)openTranscriptToQDMap.get(openFileList.get(0));
					contentPane.add(qd);
					QDShell.this.setTitle(qd.getOldTitle()+"-"+qd.productName);
                                        contentPane.repaint();				
				}                                   
				setJMenuBar(getQDShellMenu()); 
				setVisible(true);
				
				break;
			default:
				System.exit(0);
			}
		}             
		
		else
		{       
			System.exit(0);
		}
	}
	
	/** For later opened transcripts */ 
	private void loadState()
	{
		//numberOfQDsOpen++;
		qd = new QD(prefmngr);
		qd.firstQDresize=true;
		contentPane = getContentPane();
		addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				closeThisQDFrame(QDShell.CLOSE_WINDOW);
			}
		});
	}
	
	public QD getQD() {
		return qd;
	}
       //public QDShell getQDShell(){return QDShell.this;}
	public JMenuBar getQDShellMenu() {
		//File menu
		JMenu projectMenu = new JMenu(messages.getString("File"));
		JMenuItem wizardItem = new JMenuItem(messages.getString("Open"));
		wizardItem.setAccelerator(KeyStroke.getKeyStroke("control O"));		
		wizardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {                         
				/*
				 if (hasLoadedTranscript)
				 new QDShell(1);
				 else*/							
				if(numberOfQDsOpen<MAXIMUM_NUMBER_OF_OPEN_FILES){                              
					if (hasLoadedTranscript){                              
						if (qd.getEditor() != null) //no content in this QD window
						{ 
							putPreferences();  //later open trancript has same window size and plancement
							tempQD=qd;      //  save for later remove from contentpane                                        
						}
					}
					loadState();  // prepare load another transcript file				
					Wizard wiz = new Wizard();
					wiz.setVisible(true);
					setJMenuBar(getQDShellMenu());
					setVisible(true);                                       
				}
				else
					JOptionPane.showMessageDialog(null, messages.getString("NumberOfOpenFilesExceed"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);                        
			}
		});
		
		JMenuItem closeItem = new JMenuItem(messages.getString("Close"));
		closeItem.setAccelerator(KeyStroke.getKeyStroke("control W"));		
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Should prompt user to save!!
				//System.exit(tryToQuit());
				closeThisQDFrame(QDShell.CLOSE_TRANSCRIPT);
			}
		});		
		/* Saving is actually context specific. Configurations designed
		 * to be Read-only shouldn't include this option.
		 JMenuItem saveItem = new JMenuItem(messages.getString("Save"));
		 saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
		 saveItem.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
		 if (qd.getEditor().isEditable())
		 qd.saveTranscript();
		 }
		 });*/
		
		JMenuItem printItem = new JMenuItem(messages.getString("Print"));
		printItem.setAccelerator(KeyStroke.getKeyStroke("control P"));		
		printItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {      
			        Thread runner = new Thread() {
				public void run() { 
                                    print();
                                    }};
                              runner.start();               
			}});
                
                JMenu printToFileMenu = new JMenu(messages.getString("PrintToFile"));		
                JMenuItem printToPS=new JMenuItem(messages.getString("PrintToPS")); 
                printToPS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {    		         
                                    printToPS();
                              }});
                JMenuItem printToRTF=new JMenuItem(messages.getString("PrintToRTF")); 
                printToRTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {    		         
                                    printToRTF();
                              }});
                printToFileMenu.add(printToRTF);
                printToFileMenu.add(printToPS);
                
		JMenuItem saveAsItem = new JMenuItem(messages.getString("SaveAs"));
		//saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control S"));		
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                                 if (!qd.getEditor().isEditable())
                                      JOptionPane.showMessageDialog(QDShell.this, messages.getString("OnlyTranscriptionMode"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);        
                                 else
                                      saveTranscriptAs();                           
			}});
                        
		JMenuItem quitItem = new JMenuItem(messages.getString("Exit"));
		//Ed: can't use control X--that's cut: quitItem.setAccelerator(KeyStroke.getKeyStroke("control X"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//NOTE THIS HAS BEEN DISABLED UNTIL USER IS PROMPTED TO SAVE
				//Should prompt user to save!!
				//System.exit(tryToQuit());
				closeThisQDFrame(QDShell.CLOSE_APPLICATION);
			}
		});
		projectMenu.add(wizardItem);
		projectMenu.add(closeItem);
		projectMenu.addSeparator();
		projectMenu.add(saveAsItem);
		projectMenu.addSeparator();
                projectMenu.add(printItem);
                projectMenu.add(printToFileMenu);
                projectMenu.addSeparator();
		projectMenu.add(quitItem);
		
		//Preferences menu
		JMenuItem fontItem = new JMenuItem(messages.getString("Preferences"));
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getDisplayPreferences();
			}
		});
		/* Time coding is also configuration specific.
		 * Read-only configurations won't include it. 
		 * JMenuItem timeCodeItem = new JMenuItem(messages.getString("TimeCoding"));
		 timeCodeItem.addActionListener(new ActionListener() {
		 public void actionPerformed(ActionEvent ae) {
		 getTimeCodePreferences();
		 }
		 }); */
		JMenu preferencesMenu = new JMenu(messages.getString("Tools"));
		preferencesMenu.add(fontItem);
		// preferencesMenu.add(timeCodeItem);
		
		JMenuItem feedbackItem = new JMenuItem(messages.getString("Feedback"));
		feedbackItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new fieldling.quilldriver.UserFeedback(QDShell.this);
			}
		});
		JMenuItem aboutItem = new JMenuItem(messages.getString("AboutQuillDriver"));
		try {
			final JScrollPane sp = GuiUtil.getScrollPaneForTextFile(this.getClass().getClassLoader(), "about.txt");
			aboutItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame f = new JFrame();
					f.setSize(500, 400);
					f.getContentPane().add(sp);
					f.setVisible(true);
				}
			});
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		JMenu betaMenu = new JMenu(messages.getString("About"));
		betaMenu.add(aboutItem);
		betaMenu.add(feedbackItem);
		
		
		//-----------Windows Mode for TextFrame and VideoFrame----------------------------------------
		JMenu windowMenu=new JMenu(messages.getString("Window"));
		final JCheckBoxMenuItem bothResizeSetting=new JCheckBoxMenuItem(messages.getString("BothResizeSetting"));
		bothResizeSetting.setAccelerator(KeyStroke.getKeyStroke("shift F9"));
		bothResizeSetting.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(bothResizeSetting.getState()){ 
					getQD().bothResize=true;
					//prefmngr.setInt(prefmngr.BOTH_RESIZE_KEY, 1);
				}else{
					getQD().bothResize=false;
					//prefmngr.setInt(prefmngr.BOTH_RESIZE_KEY, 0);
				}
			}
		});
		if(getQD().getBothResize())          
			bothResizeSetting.setState(true);
		else 
			bothResizeSetting.setState(false);
		
		windowMenu.add(bothResizeSetting);
		windowMenu.addSeparator();
		JRadioButtonMenuItem item1= new JRadioButtonMenuItem(messages.getString("MediaToRight"));
		item1.setAccelerator(KeyStroke.getKeyStroke("shift F1"));
		item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setHorizontalWindowsMediaToRight();		
			}
		});
		
		JRadioButtonMenuItem item2= new JRadioButtonMenuItem(messages.getString("MediaToLeft"));
		item2.setAccelerator(KeyStroke.getKeyStroke("shift F2"));
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setHorizontalWindowsMediaToLeft();		
			}
		});
		
		JRadioButtonMenuItem item3= new JRadioButtonMenuItem(messages.getString("MediaOnTop"));
		item3.setAccelerator(KeyStroke.getKeyStroke("shift F3"));
		item3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setVerticalWindowsMediaTop();
			}
		});                       
		JRadioButtonMenuItem item4= new JRadioButtonMenuItem(messages.getString("SubtitleBelow"));
		item4.setAccelerator(KeyStroke.getKeyStroke("shift F4"));
		item4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setSubtitleWindows();
			}
		});
		
		JRadioButtonMenuItem item5= new JRadioButtonMenuItem(messages.getString("FullScreenVideoOnly"));
		item5.setAccelerator(KeyStroke.getKeyStroke("shift F5"));
		item5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {                            
				getQD().setVideoOnlyFullScreen();
			}
		});
		
		JRadioButtonMenuItem item6= new JRadioButtonMenuItem(messages.getString("NormalSizeVideoOnly"));
		item6.setAccelerator(KeyStroke.getKeyStroke("shift F6"));
		item6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setVideoOnlyNormalSize();
			}
		});
		
		JRadioButtonMenuItem item7= new JRadioButtonMenuItem(messages.getString("TranscriptOnly"));
		item7.setAccelerator(KeyStroke.getKeyStroke("shift F7"));
		item7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getQD().setTranscriptOnly();
			}
		});
		
		switch(getQD().getWindowsMode()){          
		case 0:item1.setSelected(true);break;
		case 1:item2.setSelected(true);break;
		case 2:item3.setSelected(true);break;
		case 3:item4.setSelected(true);break;
		case 4:item5.setSelected(true);break;
		case 5:item6.setSelected(true);break;
		case 6:item7.setSelected(true);break;                  
		}
		
		ButtonGroup group = new ButtonGroup( );              
		group.add(item1);
		group.add(item2);
		group.add(item3);
		group.add(item4);
		group.add(item5);
		group.add(item6);
		group.add(item7);
		
		windowMenu.add(item1);
		windowMenu.add(item2);
		windowMenu.add(item3);
		windowMenu.add(item4);
		windowMenu.add(item5);
		windowMenu.add(item6);
		windowMenu.add(item7);
		windowMenu.addSeparator();
		
		JMenuItem defaultItem = new JMenuItem(messages.getString("DefaultSizeAndPlacement"));
		defaultItem.setAccelerator(KeyStroke.getKeyStroke("shift F8"));
		defaultItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getQD().setDefaultWindows();
			}
		});

		windowMenu.add(defaultItem);          
		windowMenu.addSeparator();
		
		// the following code for open a few files in same window of qdshell
		Iterator itty = openFileList.iterator();
		final JRadioButtonMenuItem[] fileItem= new JRadioButtonMenuItem[4];        
		int count =0;
		ButtonGroup groupFile = new ButtonGroup( );
		String activeTranscript=null;
		if(qd.transcriptFile!=null)                 
                       activeTranscript=getQD().getOldTitle();
		while (itty.hasNext()) {                     
			final String openFile = (String) itty.next();			
			if(activeTranscript!=null&&openFile.equals(activeTranscript))
				fileItem[count] = new JRadioButtonMenuItem(openFile,true);               
			else 
				fileItem[count] = new JRadioButtonMenuItem(openFile);
			
			final int i=count;
			fileItem[count].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {                                   
					fileItem[i].setSelected(true);
					contentPane = getContentPane();
					if (hasLoadedTranscript){                              
						if (qd.getEditor() != null) //no content in this QD window
						{ 
							//putPreferences();
							contentPane.remove(qd);
							contentPane.repaint();
						}
					}
					qd = (QD)openTranscriptToQDMap.get(openFile);                                     
					contentPane.add(qd);
					QDShell.this.setTitle(qd.getWindowTitle(qd.getCurrentLang())+ " - "+qd.productName);
					contentPane.repaint();
					setJMenuBar(getQDShellMenu()); 
					setVisible(true);
				}
			});
			
			groupFile.add(fileItem[i]);
			windowMenu.add(fileItem[i]);
			count++;
		}
		
		//putting the menus into a menu bar
		JMenuBar bar = new JMenuBar();
		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(projectMenu);
		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(preferencesMenu);
		
		windowMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(windowMenu);
		
		betaMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(betaMenu);
		return bar;
	}
       
         public void saveTranscriptAs(){
             JFileChooser fd=new JFileChooser(new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));
             fd.setDialogTitle(messages.getString("SaveAs"));
             fd.addChoosableFileFilter(new QDFileFilter());
             try{
                   int action=fd.showSaveDialog(QDShell.this);                      
                   if(action==JFileChooser.APPROVE_OPTION){
                   File oldFile=qd.transcriptFile;
                   File newFile=fd.getSelectedFile();
                   openFileList.set(openFileList.indexOf(oldFile.getName()), newFile.getName());
                   qd=(QD)openTranscriptToQDMap.remove(oldFile.getName());
                   openTranscriptToQDMap.put(newFile.getName(),qd);
                   qd.transcriptFile=newFile;                              
                   qd.saveTranscript();
                   qd.textFrame.setTitle(newFile.getAbsolutePath());
                   setJMenuBar(getQDShellMenu());
		   setVisible(true); 
                   makeRecentlyOpened(newFile.getAbsolutePath());
		   makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
                  }
               }catch(Exception ex){
                   System.out.println("Errors are occured when saving: "+ex.toString());
                   JOptionPane.showMessageDialog(this,messages.getString("SaveError"),"Alert",JOptionPane.ERROR_MESSAGE);
               }
          }
   
     public void print( ) {
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        PrintService[  ] services =PrintServiceLookup.lookupPrintServices(flavor, null);
        PrintRequestAttributeSet printAttributes =new HashPrintRequestAttributeSet( );
        PrintService service = ServiceUI.printDialog(null, 100, 100,services, null, null,printAttributes);
        // If the user canceled, don't do anything
        if (service == null) return;
        printDocument(service, printAttributes);
    }
      public void printDocument(PrintService service,PrintRequestAttributeSet printAttributes)
     {
        DocumentRenderer printable = new DocumentRenderer(qd.getEditor().getTextPane().getStyledDocument());
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        Doc doc = new SimpleDoc(printable, flavor, null);
        DocPrintJob job = service.createPrintJob();

        // Set up a dialog box to  monitor printing status
        final JOptionPane pane = new JOptionPane(messages.getString("Printing"),JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = pane.createDialog(this, messages.getString("PrintStatus"));
        // This listener object updates the dialog as the status changes
        job.addPrintJobListener(new PrintJobAdapter( ) {
                public void printJobCompleted(PrintJobEvent e) {
                    pane.setMessage(messages.getString("PrintingComplete"));
                }
                public void printDataTransferCompleted(PrintJobEvent e) {
                    pane.setMessage(messages.getString("DocumentTransferedToPrinter"));
                }
                public void printJobRequiresAttention(PrintJobEvent e) {
                    pane.setMessage(messages.getString("OutOfPaper"));
                }
                public void printJobFailed(PrintJobEvent e) {
                    pane.setMessage(messages.getString("PrintFailed"));
                }
            });

        // Show the dialog, non-modal.
        dialog.setModal(false);
        dialog.setVisible(true);

        // Now print the Doc to the DocPrintJob
        try {
            job.print(doc, printAttributes);
        }
        catch(PrintException e) {
            // Display any errors to the dialog box
            pane.setMessage(e.toString( ));
        }
    }
    
     public void printToPS( ){
        try {
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        String format = "application/postscript";// so far only support ps output
        StreamPrintServiceFactory factory = StreamPrintServiceFactory.
            lookupStreamPrintServiceFactories(flavor, format)[0];
        JFileChooser chooser = new JFileChooser( );
        chooser.setDialogTitle(messages.getString("PrintToPS"));
        chooser.addChoosableFileFilter(new OutputFileFilter());
        if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile( );
        FileOutputStream out = new FileOutputStream(f);
        StreamPrintService service = factory.getPrintService(out);
        printDocument(service, null);
        out.close( );
      }catch(Exception e){
          JOptionPane.showMessageDialog(QDShell.this, messages.getString("PrintFail"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);   
         }
    }
   
      public void printToRTF(){
          try{
          RTFEditorKit rtf=new RTFEditorKit();
          JFileChooser chooser = new JFileChooser( );
          chooser.setDialogTitle(messages.getString("PrintToRTF"));
          chooser.addChoosableFileFilter(new OutputFileFilter());
          if (chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
          File f = chooser.getSelectedFile( );
          FileOutputStream out = new FileOutputStream(f);
          StyledDocument doc=qd.getEditor().getTextPane().getStyledDocument();
          rtf.write(out,(Document)doc,0,doc.getLength());
          }catch(Exception e){
            JOptionPane.showMessageDialog(QDShell.this, messages.getString("PrintFail"), messages.getString("Alert"), JOptionPane.ERROR_MESSAGE); 
          }
      }    
      
	private void makeRecentlyOpened(String s) {
		String r = prefmngr.getValue(prefmngr.RECENT_FILES_KEY, null);
		if (r == null)
			prefmngr.setValue(prefmngr.RECENT_FILES_KEY, s);
		else {
			LinkedList recents = new LinkedList();
			recents.add(s);
			StringTokenizer tok = new StringTokenizer(r, ",");
			while (tok.hasMoreTokens()) {
				String s2 = tok.nextToken();
				if (!s.equals(s2))
					recents.add(s2);
			}
			int k;
			if (recents.size() > MAXIMUM_NUMBER_OF_RECENT_FILES)
				k = MAXIMUM_NUMBER_OF_RECENT_FILES;
			else
				k = recents.size();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < k; i++) {
				sb.append((String) recents.removeFirst());
				sb.append(',');
			}
			prefmngr.setValue(prefmngr.RECENT_FILES_KEY, sb.toString());
		}
	}
	
	private void makeRecentlyOpenedVideo(String s) {
		String r = prefmngr.getValue(prefmngr.RECENT_VIDEOS_KEY, null);
		if (r == null)
			prefmngr.setValue(prefmngr.RECENT_VIDEOS_KEY, s);
		else {
			LinkedList recents = new LinkedList();
			recents.add(s);
			StringTokenizer tok = new StringTokenizer(r, ",");
			while (tok.hasMoreTokens()) {
				String s2 = tok.nextToken();
				recents.add(s2);
			}
			int k;
			if (recents.size() > MAXIMUM_NUMBER_OF_RECENT_FILES)
				k = MAXIMUM_NUMBER_OF_RECENT_FILES;
			else
				k = recents.size();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < k; i++) {
				sb.append((String) recents.removeFirst());
				sb.append(',');
			}
			prefmngr.setValue(prefmngr.RECENT_VIDEOS_KEY, sb.toString());
		}
	}
	
	private void updateSupportedFonts()
	{
		int i;
		String [] fontNames = I18n.getSupportedFonts(defaultLanguage.getSelectedIndex());
		DefaultComboBoxModel model = new DefaultComboBoxModel(fontNames);
		supportedFonts.setModel(model);
		String defaultFont;	
		/* if there is no default language font set for Quilldriver,
		 * use the system default font.
		 */ 
		if (PreferenceManager.default_interface_font==null)
			defaultFont = ((Font)UIManager.get("Label.font")).getFamily();
		else
			defaultFont = PreferenceManager.default_interface_font;
		for (i=0; i<fontNames.length; i++)
			if (fontNames[i].equals(defaultFont))
			{
				supportedFonts.setSelectedIndex(i);
				break;
			}
		if (i>=fontNames.length)
			supportedFonts.setSelectedIndex(0);
	}
	
	/* Declaring the following private instance variables was the only way I figured to share
	 * information between the method itself and its inner classes. */ 
	private boolean optionsChanged, needsToRestart;
	private Color highlightColor, tagColor;
	private JPanel highlightColorPanel, tagColorPanel;
	
	private void getDisplayPreferences()
	{
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames;
		int i;
		highlightColor = new Color(PreferenceManager.highlight_color_red, PreferenceManager.highlight_color_green, PreferenceManager.highlight_color_blue);
		tagColor = new Color(PreferenceManager.tag_color_red, PreferenceManager.tag_color_green, PreferenceManager.tag_color_blue);
		
		fontNames = genv.getAvailableFontFamilyNames();
		
		JPanel hPanel, interfacePanel = new JPanel(new GridLayout(2,2));
		interfacePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("Interface")));
		interfacePanel.add(new JLabel(messages.getString("Language")));
		
		if (supportedFonts==null)
		{
			/* Combo is not field yet, as the supported fonts
			 * depend on the language selected.
			 */
			supportedFonts = new JComboBox();
			supportedFonts.addItemListener(new ItemListener()
					{
				public void itemStateChanged(ItemEvent e) 
				{
					optionsChanged = true;
					needsToRestart = true;
				}				
					});
		}			
		
		if (defaultLanguage==null)
		{
			String[] languageLabels = I18n.getSupportedLanguages();;
			defaultLanguage = new JComboBox(languageLabels);
			defaultLanguage.addItemListener(new ItemListener()
					{
				public void itemStateChanged(ItemEvent e) 
				{
					optionsChanged = true;
					needsToRestart = true;
					updateSupportedFonts();
				}				
					});
			
			/* If there is no default language set for Quilldriver, use the system
			 * default language.
			 */
			if (PreferenceManager.default_language>-1)
			{
				defaultLanguage.setSelectedIndex(PreferenceManager.default_language);
			}
			else
			{
				String defaultLangLabel = I18n.getDefaultDisplayLanguage();
				
				for (i=0; i<languageLabels.length; i++)
					if (languageLabels[i].equals(defaultLangLabel))
					{
						defaultLanguage.setSelectedIndex(i);
						break;
					}
				
				/* if the user's default language is not supported
				 * by Quill-driver, for Quill-driver's sake, english
				 * would be the default language.
				 */
				if (i>=languageLabels.length)
				{
					defaultLanguage.setSelectedIndex(0);
				}
			}
			
			/* calling this directly since itemStateChanged
			 * is not invoked by setSelectedIndex.
			 */
			updateSupportedFonts();
		}
		
		interfacePanel.add(defaultLanguage);
		interfacePanel.add(new JLabel(messages.getString("Font")));
		
		interfacePanel.add(supportedFonts);
		
		@TIBETAN@JPanel tibetanPanel;
		@TIBETAN@JComboBox tibetanFontSizes;
		@TIBETAN@tibetanPanel = new JPanel();
		@TIBETAN@tibetanPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("TibetanFontSize")));
		@TIBETAN@tibetanFontSizes = new JComboBox(new String[] {"22","24","26","28","30","32","34","36","48","72"});
		@TIBETAN@tibetanFontSizes.addItemListener(new ItemListener()
				@TIBETAN@{
			@TIBETAN@public void itemStateChanged(ItemEvent e) 
			@TIBETAN@{
				@TIBETAN@optionsChanged = true;
				@TIBETAN@}				
			@TIBETAN@});
		@TIBETAN@tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());
		@TIBETAN@tibetanFontSizes.setSelectedItem(String.valueOf(PreferenceManager.tibetan_font_size));
		@TIBETAN@tibetanFontSizes.setEditable(true);
		@TIBETAN@tibetanPanel.add(tibetanFontSizes);
		JPanel romanPanel;
		JComboBox romanFontFamilies;
		JComboBox romanFontSizes;
		romanPanel = new JPanel(new GridLayout(1,2));
		romanPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("NonTibetanFontAndSize")));
		romanFontFamilies = new JComboBox(fontNames);
		romanFontFamilies.addItemListener(new ItemListener()
				{
			public void itemStateChanged(ItemEvent e) 
			{
				optionsChanged = true;
			}				
				});
		romanFontFamilies.setMaximumSize(romanFontFamilies.getPreferredSize());
		romanFontFamilies.setSelectedItem(PreferenceManager.font_face);
		romanFontFamilies.setEditable(true);
		romanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
		romanFontSizes.addItemListener(new ItemListener()
				{
			public void itemStateChanged(ItemEvent e) 
			{
				optionsChanged = true;
			}				
				});
		romanFontSizes.setMaximumSize(romanFontSizes.getPreferredSize());
		romanFontSizes.setSelectedItem(String.valueOf(PreferenceManager.font_size));
		romanFontSizes.setEditable(true);
		
		//JPanel romanUpperPanel = new JPanel(new GridLayout(1,2));
		romanPanel.add(romanFontFamilies);
		romanPanel.add(romanFontSizes);
		//romanPanel.add(romanUpperPanel);
		
		JPanel colorPanel = new JPanel(new GridLayout(2,3,10,10));
		colorPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("Colors")));
		
		final JButton tagColorButton =new JButton(messages.getString("Change"));
		tagColorButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				Color newColor = JColorChooser.showDialog(tagColorButton, "Choose a color", tagColor);                 
				if(newColor!=null)
				{
					optionsChanged = true;
					tagColor = newColor;
					tagColorPanel.setBackground(newColor);
					tagColorPanel.repaint();
				}
			}
		});	
		colorPanel.add(new JLabel(messages.getString("TagColor")));		
		tagColorPanel = new JPanel();
		tagColorPanel.setBackground(tagColor);
		colorPanel.add(tagColorPanel);
		colorPanel.add(tagColorButton);		
		
		final JButton highlightColorButton =new JButton(messages.getString("Change"));
		highlightColorButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ev){
				Color newColor = JColorChooser.showDialog(highlightColorButton, "Choose a color", highlightColor);                 
				if(newColor!=null)
				{
					optionsChanged = true;
					highlightColor = newColor;
					highlightColorPanel.setBackground(newColor);
					highlightColorPanel.repaint();
				}
			}
		});	
		colorPanel.add(new JLabel(messages.getString("HighlightColor")));		
		highlightColorPanel = new JPanel();
		highlightColorPanel.setBackground(highlightColor);
		colorPanel.add(highlightColorPanel);
		colorPanel.add(highlightColorButton);
		
		JPanel highlightPanel;
		JComboBox highlightPosition, multipleHighlightPolicy, scrollingHighlightPolicy;  
		JTextField highlightField;
		highlightPanel = new JPanel();                   
		highlightPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("HighlightRelatedPreferences")));      
		highlightPanel.setLayout(new GridLayout(0,1));
		
		highlightPosition = new JComboBox(new String[] {messages.getString("Middle"), messages.getString("Bottom")});
		highlightPosition.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e) 
					{
						optionsChanged = true;
					}				
				});
		highlightPosition.setSelectedItem(PreferenceManager.highlight_position);
		highlightPosition.setEditable(true);
		hPanel = new JPanel();
		hPanel.add(new JLabel(messages.getString("HighlightPosition")));
		hPanel.add(highlightPosition);
		highlightPanel.add(hPanel);
		
		multipleHighlightPolicy = new JComboBox(new String[] {messages.getString("Allowed"), messages.getString("Disallowed")});
		multipleHighlightPolicy.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e) 
					{
						optionsChanged = true;
					}				
				});
		multipleHighlightPolicy.setSelectedItem(PreferenceManager.multiple_highlight_policy);
		multipleHighlightPolicy.setEditable(true);
		scrollingHighlightPolicy = new JComboBox(new String[] {messages.getString("Allowed"), messages.getString("Disallowed")});
		scrollingHighlightPolicy.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e) 
					{
						optionsChanged = true;
					}				
				});
		scrollingHighlightPolicy.setSelectedItem(PreferenceManager.scrolling_highlight_policy);
		scrollingHighlightPolicy.setEditable(true);
		hPanel = new JPanel();
		hPanel.add(new JLabel(messages.getString("MultipleHighlightPolicy")));
		hPanel.add(multipleHighlightPolicy);
		highlightPanel.add(hPanel);
		
		hPanel = new JPanel();
		hPanel.add(new JLabel(messages.getString("ScrollingHighlightPolicy")));
		hPanel.add(scrollingHighlightPolicy);
		highlightPanel.add(hPanel);
		
		JPanel preferencesPanel = new JPanel (new BorderLayout());
		preferencesPanel.add(interfacePanel, BorderLayout.NORTH);
		JPanel middlePanel = new JPanel(new GridLayout(0,1));
		@TIBETAN@middlePanel.add(tibetanPanel);
		middlePanel.add(romanPanel);
		middlePanel.add(colorPanel);
		preferencesPanel.add(middlePanel, BorderLayout.CENTER);
		preferencesPanel.add(highlightPanel, BorderLayout.SOUTH);
		JOptionPane pane = new JOptionPane(preferencesPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = pane.createDialog(this, messages.getString("FontAndStylePreferences"));
		
		optionsChanged = false;
		needsToRestart = false;

		// This returns only when the user has closed the dialog
		dialog.setVisible(true);
		
		Object selectedValue = pane.getValue();
		
		if (!optionsChanged || selectedValue==null || !(selectedValue instanceof Integer))
			return;
		
		Integer selectedInteger = (Integer) selectedValue;
		if (selectedInteger.intValue() != JOptionPane.OK_OPTION)
			return;
		
		@TIBETAN@int old_tibetan_font_size = PreferenceManager.tibetan_font_size;
		@TIBETAN@try {
			@TIBETAN@PreferenceManager.tibetan_font_size = Integer.parseInt(tibetanFontSizes.getSelectedItem().toString());
			@TIBETAN@}
		@TIBETAN@catch (NumberFormatException ne) {
			@TIBETAN@PreferenceManager.tibetan_font_size = old_tibetan_font_size;
			@TIBETAN@}
		
		String old_font_face = new String(PreferenceManager.font_face);
		
		PreferenceManager.font_face = romanFontFamilies.getSelectedItem().toString();
		int old_font_size = PreferenceManager.font_size;
		try {
			PreferenceManager.font_size = Integer.parseInt(romanFontSizes.getSelectedItem().toString());
		}
		catch (NumberFormatException ne) {
			PreferenceManager.font_size = old_font_size;
		}
		
		prefmngr.setValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.font_face);
		prefmngr.setInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.font_size);
		@TIBETAN@prefmngr.setInt(PreferenceManager.TIBETAN_FONT_SIZE_KEY, PreferenceManager.tibetan_font_size);
		Editor editor = qd.getEditor();

		fieldling.quilldriver.xml.Renderer.setTagColor(tagColor);
		PreferenceManager.tag_color_red = tagColor.getRed();
		PreferenceManager.tag_color_green = tagColor.getGreen();
		PreferenceManager.tag_color_blue = tagColor.getBlue();
		prefmngr.setInt(PreferenceManager.TAG_RED_KEY, PreferenceManager.tag_color_red);
		prefmngr.setInt(PreferenceManager.TAG_GREEN_KEY, PreferenceManager.tag_color_green);
		prefmngr.setInt(PreferenceManager.TAG_BLUE_KEY, PreferenceManager.tag_color_blue);		

		String highlightPosVal = (String)highlightPosition.getSelectedItem();
		
		if (editor != null)
		{
			@UNICODE@if (!(old_font_size == PreferenceManager.font_size && old_font_face.equals(PreferenceManager.font_face))){
			@TIBETAN@if (!(old_font_size == PreferenceManager.font_size && old_font_face.equals(PreferenceManager.font_face) && old_tibetan_font_size == PreferenceManager.tibetan_font_size)) {
				@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)editor.getTextPane();
				@TIBETAN@dp.setByUserTibetanFontSize(PreferenceManager.tibetan_font_size);
				@TIBETAN@dp.setByUserRomanAttributeSet(PreferenceManager.font_face, PreferenceManager.font_size);
				@UNICODE@editor.getTextPane().setFont(new Font(PreferenceManager.font_face, Font.PLAIN, PreferenceManager.font_size));
			}
			qd.hp.setHighlightPosition(highlightPosVal);
			if (qd.hp != null) {
				qd.hp.setHighlightColor(highlightColor);
			}
			editor.render();			
		}
		String multipleHighlightPolicyVal = (String)multipleHighlightPolicy.getSelectedItem();
		
		PreferenceManager.multiple_highlight_policy = (String)multipleHighlightPolicy.getSelectedItem();
		PreferenceManager.highlight_position = highlightPosVal;
		
		prefmngr.setValue(PreferenceManager.HIGHLIGHT_POSITION_KEY, highlightPosVal);
		prefmngr.setValue(PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_KEY, PreferenceManager.multiple_highlight_policy);
				
		if (multipleHighlightPolicyVal.equals(messages.getString("Allowed")))
			qd.player.setMultipleAnnotationPolicy(true);
		else
			qd.player.setMultipleAnnotationPolicy(false);
		
		String scrollingHighlightPolicyVal = (String)scrollingHighlightPolicy.getSelectedItem();
		
		PreferenceManager.scrolling_highlight_policy = (String) scrollingHighlightPolicy.getSelectedItem();
		prefmngr.setValue(PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_KEY, PreferenceManager.scrolling_highlight_policy);
		
		if (scrollingHighlightPolicyVal.equals(messages.getString("Allowed"))) {
			if (qd != null) {
				qd.mode = QD.SCROLLING_HIGHLIGHT_IS_ON;
				qd.player.setAutoScrolling(true);
			}
		} else {
			if (qd != null) {
				qd.mode = QD.SCROLLING_HIGHLIGHT_IS_OFF;
				qd.player.setAutoScrolling(false);
			}
		}
		
		if (needsToRestart)
		{
			JOptionPane.showMessageDialog(this, messages.getString("ChangesToInterface"));
			PreferenceManager.default_interface_font = (String) supportedFonts.getSelectedItem();
			PreferenceManager.default_language = defaultLanguage.getSelectedIndex();
			prefmngr.setValue(PreferenceManager.DEFAULT_INTERFACE_FONT_KEY, PreferenceManager.default_interface_font);
			prefmngr.setInt(PreferenceManager.DEFAULT_LANGUAGE_KEY, PreferenceManager.default_language);
		}
	}
	
	private class QDFileFilter extends javax.swing.filechooser.FileFilter {
		// accepts all directories and all savant files
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return f.getName().toLowerCase().endsWith(QDShell.dotQuillDriver) || f.getName().toLowerCase().endsWith(QDShell.dotQuillDriverTibetan);
		}
		//the description of this filter
		public String getDescription() {
			return "QD File Format (" + QDShell.dotQuillDriver + ", " + QDShell.dotQuillDriverTibetan + ")";
		}
	}
        private class OutputFileFilter extends javax.swing.filechooser.FileFilter {
		// accepts all directories and all savant files
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return f.getName().toLowerCase().endsWith(".rtf") || f.getName().toLowerCase().endsWith(".ps");
		}
		//the description of this filter
		public String getDescription() {
			return "QD Output File Format (" + ".rtf" + ", " + ".ps" + ")";
		}
	}
}
		
