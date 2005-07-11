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

package fieldling.quilldriver;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import java.util.prefs.*;
import fieldling.quilldriver.PreferenceManager;
import fieldling.mediaplayer.*;
import fieldling.util.I18n;
import fieldling.util.JdkVersionHacks;

public class QDShell extends JFrame implements ItemListener
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

	PreferenceManager prefmngr = new fieldling.quilldriver.PreferenceManager();

	private static int numberOfQDsOpen = 0;

	/* public static final String NEW_FILE_ERROR_MESSAGE =
	 "This file already exists! Type a new file name\n" +
	 "instead of selecting an existing file.";*/
	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;
	
	/**
	 * Used in the Preference window. Need to access them from itemHasChanged
	 */
	private JComboBox defaultLanguage, supportedFonts;
	
	/**
	 * Marks if preference changes that require restart took place.
	 */
	private boolean needsToRestart;

	private static void printSyntax()
	{
		System.out.println("Syntax: QDShell [-edit | -read  transcript-file]");
	}
	
	public static void main(String[] args)
	{
		PrintStream ps=null;
		try 
		{
			//ThdlDebug.attemptToSetUpLogFile("qd", ".log");
			Locale locale;
			//note: by default Java 1.5 (and 1.4??) uses a buggy transformer based on an
			//earlier version of Xalan-Java. so we need to set this system property
			System.setProperty("javax.xml.transform.TransformerFactory",
					"org.apache.xalan.processor.TransformerFactoryImpl");
			try 
			{
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
			}
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
			boolean readOnly;
			
			if (option.equals("edit"))
				readOnly = false;
			else if (option.equals("read"))
				readOnly = true;
			else
			{
				System.out.println("Syntax error: invalid option!");
				printSyntax();
				return;
			}
			
			File transcriptFile = new File (args[1]);
			if (!transcriptFile.exists())
			{
				System.out.println("Error reading file!");
				return;
			}
			new QDShell(transcriptFile, readOnly);
			
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

	class Wizard extends JDialog {
		public Wizard() {
			//choice of configuration
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
					Wizard.this.hide();
					System.exit(0);
				}
			});

			//ok button: make changes
			JButton okButton = new JButton(messages.getString("Ok"));
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ButtonModel selectedConfiguration = configGroup.getSelection();
					String configCommand = selectedConfiguration.getActionCommand();
					try {
						int i = Integer.parseInt(configCommand);
						qd.configure(configurations[i]);
						prefmngr.setValue(prefmngr.CONFIGURATION_KEY,configurations[i].getName());
						prefmngr.setValue(prefmngr.MEDIA_PLAYER_KEY, qd.player.getIdentifyingName());
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						System.exit(0);
					}
					ButtonModel selectedRb = dataSourceGroup.getSelection();
					String command = selectedRb.getActionCommand();
					if (command.equals(messages.getString("NewTranscriptText"))) {
						// File newTemplateFile = new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home")) + qd.newTemplateFileName);
						URL newTemplateURL = QDShell.this.getClass().getClassLoader().getResource(qd.newTemplateFileName);
						if (newTemplateURL == null)
							System.exit(0); //FIX
						/*if (!newTemplateFile.exists())
							System.exit(0); //FIX*/
                                                        
						File saveAsFile = selectTranscriptFile(messages.getString("SaveTranscriptAs"));
                                                
                                                try {
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
                                                } catch (IOException ioe) {
                                                    ioe.printStackTrace();
                                                }
                                                
                                                qd.loadTranscript(saveAsFile);
						String transcriptString = saveAsFile.getAbsolutePath();
						prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
						makeRecentlyOpened(transcriptString);
						makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
					} else if (command.equals(messages.getString("OpenExisting"))) {
						File transcriptFile = selectTranscriptFile(messages.getString("OpenTranscript"));
						if (transcriptFile != null) {
							String transcriptString = transcriptFile.getAbsolutePath();
							prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0,transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
							qd.loadTranscript(transcriptFile);
							makeRecentlyOpened(transcriptString);
							makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
						}
					} else { //must be recent file
						File transcriptFile = new File(command);
						Object video = recentTranscriptToRecentVideoMap.get(command);
						if (video == null)
							qd.loadTranscript(transcriptFile);
						else
							qd.loadTranscript(transcriptFile, (String) video);
						makeRecentlyOpened(command);
						makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
					}
					Wizard.this.hide();
				}
			});
			JPanel northChoices = new JPanel(new GridLayout(1, 0));
			northChoices.add(configurationChoice);
			northChoices.add(moviePlayerChoice);
			JPanel choices = new JPanel(new GridLayout(0, 1));
			choices.add(northChoices);
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
	
	public QDShell()
	{
		this (null, false);
	}

	public QDShell(File transcriptFile, boolean readOnly) {
		numberOfQDsOpen++;
		@UNICODE@setTitle("QuillDriver");
		@TIBETAN@setTitle("QuillDriver-TIBETAN");
		defaultLanguage = null;
		supportedFonts = null;
		needsToRestart = false;
		messages = I18n.getResourceBundle();
		setLocation(prefmngr.getInt(prefmngr.WINDOW_X_KEY, 0), prefmngr.getInt(prefmngr.WINDOW_Y_KEY, 0));
		setSize(new Dimension(prefmngr.getInt(prefmngr.WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width),
		prefmngr.getInt(prefmngr.WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));
		qd = new QD(prefmngr);
		if (transcriptFile==null)
		{
			Wizard wiz = new Wizard();
			wiz.setModal(true);
			wiz.setSize(new Dimension(600,400));
			wiz.addWindowListener(new WindowAdapter () {
				public void windowClosing (WindowEvent e) {
	                            System.exit(0);
				}
			});
			wiz.show();
		}
		else
		{
			String transcriptString = transcriptFile.getAbsolutePath(), configName;
			prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY,transcriptString.substring(0,transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
			if (readOnly)
				configName = "THDLReadonly";
			else
				configName = "THDLTranscription";
			
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
				if (mPlayer.getIdentifyingName().equals("QuicktimeforJava"))
				{
					qd.setMediaPlayer(mPlayer);
					break;
				}
			}
			
			qd.loadTranscript(transcriptFile);
			makeRecentlyOpened(transcriptString);
			makeRecentlyOpenedVideo(qd.player.getMediaURL().toString());
		}
		getContentPane().add(qd);
		setJMenuBar(getQDShellMenu());
		// setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				closeThisQDFrame();
				if (numberOfQDsOpen == 0) {
					putPreferences();
					System.exit(0);
				}
			}
		});
		setVisible(true);
	}

	private void putPreferences() {
		prefmngr.setInt(prefmngr.WINDOW_X_KEY, getX());
		prefmngr.setInt(prefmngr.WINDOW_Y_KEY, getY());
		prefmngr.setInt(prefmngr.WINDOW_WIDTH_KEY, getWidth());
		prefmngr.setInt(prefmngr.WINDOW_HEIGHT_KEY, getHeight());
		prefmngr.setValue(prefmngr.MEDIA_DIRECTORY_KEY,
				prefmngr.media_directory);
	}

	private void closeThisQDFrame() {
		/*i first used dispose() instead of hide(), which should clear up memory,
		 but i got an error: can't dispose InputContext while it's active*/
		if (qd.getEditor() == null) { //no content in this QD window
			hide();
			numberOfQDsOpen--;
		} else { //there's a QD editor: save and close
			if (qd.getEditor().isEditable())
				qd.saveTranscript();
			qd.removeContent();
			hide();
			numberOfQDsOpen--;
		}
	}

	public QD getQD() {
		return qd;
	}

	public JMenuBar getQDShellMenu() {
		//File menu
		JMenu projectMenu = new JMenu(messages.getString("File"));
		JMenuItem wizardItem = new JMenuItem(messages.getString("NewWizard"));
		wizardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new QDShell();
			}
		});
		JMenuItem saveItem = new JMenuItem(messages.getString("Save"));
		saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                            if (qd.getEditor().isEditable())
                                qd.saveTranscript();
			}
		});
		JMenuItem quitItem = new JMenuItem(messages.getString("Quit"));
		quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Should prompt user to save!!
				//System.exit(tryToQuit());
				putPreferences();
				System.exit(0);
			}
		});
		projectMenu.add(wizardItem);
		projectMenu.addSeparator();
		projectMenu.add(saveItem);
		projectMenu.addSeparator();
		projectMenu.add(quitItem);

		//Preferences menu
		JMenuItem fontItem = new JMenuItem(messages.getString("Display"));
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getDisplayPreferences();
			}
		});
		JMenuItem timeCodeItem = new JMenuItem(messages.getString("TimeCoding"));
		timeCodeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getTimeCodePreferences();
			}
		});
		JMenu preferencesMenu = new JMenu(messages.getString("Preferences"));
		preferencesMenu.add(fontItem);
		preferencesMenu.add(timeCodeItem);

		//Help menu
		JMenuItem feedbackItem = new JMenuItem(messages.getString("Feedback"));
		feedbackItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new UserFeedback(QDShell.this);
			}
		});
		JMenuItem aboutItem = new JMenuItem(messages
				.getString("AboutQuillDriver"));
		try {
			final JScrollPane sp = getScrollPaneForTextFile(this.getClass()
					.getClassLoader(), "about.txt");
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
		JMenu betaMenu = new JMenu(messages.getString("Help"));
		betaMenu.add(aboutItem);
		betaMenu.add(feedbackItem);

		//putting the menus into a menu bar
		JMenuBar bar = new JMenuBar();
		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(projectMenu);
		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(preferencesMenu);
		betaMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(betaMenu);
		JMenu[] configMenus = qd.getConfiguredMenus();
		for (int k = 0; k < configMenus.length; k++) {
			configMenus[k].getPopupMenu().setLightWeightPopupEnabled(false);
			bar.add(configMenus[k]);
		}
		return bar;
		/*
		 @TIBETAN@org.thdl.tib.input.JskadKeyboardManager keybdMgr = null;
		 @TIBETAN@JMenuItem[] keyboardItems = null;
		 @TIBETAN@try {
		 @TIBETAN@keybdMgr = new org.thdl.tib.input.JskadKeyboardManager(org.thdl.tib.input.JskadKeyboardFactory.getAllAvailableJskadKeyboards());
		 @TIBETAN@}catch (Exception e) {}
		 @TIBETAN@if (keybdMgr != null) {
		 @TIBETAN@ButtonGroup keyboardGroup = new ButtonGroup();
		 @TIBETAN@keyboardItems = new JRadioButtonMenuItem[keybdMgr.size()];
		 @TIBETAN@for (int i=0; i<keybdMgr.size(); i++) {
		 @TIBETAN@final org.thdl.tib.input.JskadKeyboard kbd = keybdMgr.elementAt(i);
		 //if (kbd.hasQuickRefFile()) {
		 @TIBETAN@keyboardItems[i] = new JRadioButtonMenuItem(kbd.getIdentifyingString());
		 @TIBETAN@keyboardItems[i].addActionListener(new ActionListener() {
		 @TIBETAN@public void actionPerformed(ActionEvent e) {
		 @TIBETAN@qd.changeKeyboard(kbd);
		 @TIBETAN@prefmngr.setValue(prefmngr.TIBETAN_KEYBOARD_KEY, kbd.getIdentifyingString());
		 @TIBETAN@}
		 @TIBETAN@});
		 @TIBETAN@keyboardGroup.add(keyboardItems[i]);
		 @TIBETAN@}
		 @TIBETAN@}
		 @TIBETAN@if (keybdMgr != null) {
		 @TIBETAN@String userKeyboard = prefmngr.getValue(prefmngr.TIBETAN_KEYBOARD_KEY, keybdMgr.elementAt(0).getIdentifyingString());
		 @TIBETAN@int i;
		 @TIBETAN@for (i=0; i<keybdMgr.size(); i++)
		 @TIBETAN@if (userKeyboard.equals(keybdMgr.elementAt(i).getIdentifyingString())) break;
		 @TIBETAN@if (i == 0 || i == keybdMgr.size()) //keyboard either can't be found or is default Wylie
		 @TIBETAN@keyboardItems[0].setSelected(true);
		 @TIBETAN@else { //keyboard is other than default Wylie keyboard: must explicitly change keyboard
		 @TIBETAN@keyboardItems[i].setSelected(true);
		 @TIBETAN@qd.changeKeyboard(keybdMgr.elementAt(i));
		 @TIBETAN@}
		 @TIBETAN@for (int k=0; k<keyboardItems.length; k++)
		 @TIBETAN@preferencesMenu.add(keyboardItems[k]);
		 @TIBETAN@}
		 */
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
	
	public void itemStateChanged(ItemEvent e)
	{
		needsToRestart = true;
		if (e.getSource()==defaultLanguage)
			updateSupportedFonts();
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

	private void getDisplayPreferences() {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames;
		int i;
		
		fontNames = genv.getAvailableFontFamilyNames();
		
		JPanel interfacePanel = new JPanel(new GridLayout(2,2));
		interfacePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("Interface")));
		interfacePanel.add(new JLabel(messages.getString("Language")));
		
		if (supportedFonts==null)
		{
			/* Combo is not field yet, as the supported fonts
			 * depend on the language selected.
			 */
			supportedFonts = new JComboBox();
			supportedFonts.addItemListener(this);
		}			
		
		if (defaultLanguage==null)
		{
			String[] languageLabels = I18n.getSupportedLanguages();;
			defaultLanguage = new JComboBox(languageLabels);
			defaultLanguage.addItemListener(this);
			
			/* If there is no default language set for Quilldriver, use the system
			 * default language.
			 */
			if (PreferenceManager.default_language>-1)
				defaultLanguage.setSelectedIndex(PreferenceManager.default_language);
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
					defaultLanguage.setSelectedIndex(0);
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
		@TIBETAN@tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());
		@TIBETAN@tibetanFontSizes.setSelectedItem(String.valueOf(prefmngr.tibetan_font_size));
		@TIBETAN@tibetanFontSizes.setEditable(true);
		@TIBETAN@tibetanPanel.add(tibetanFontSizes);
		JPanel romanPanel;
		JComboBox romanFontFamilies;
		JComboBox romanFontSizes;
		romanPanel = new JPanel();
		romanPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("NonTibetanFontAndSize")));
		romanFontFamilies = new JComboBox(fontNames);
		romanFontFamilies.setMaximumSize(romanFontFamilies.getPreferredSize());
		romanFontFamilies.setSelectedItem(prefmngr.font_face);
		romanFontFamilies.setEditable(true);
		romanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
		romanFontSizes.setMaximumSize(romanFontSizes.getPreferredSize());
		romanFontSizes.setSelectedItem(String.valueOf(prefmngr.font_size));
		romanFontSizes.setEditable(true);
		romanPanel.setLayout(new GridLayout(1,2));
		romanPanel.add(romanFontFamilies);
		romanPanel.add(romanFontSizes);
        JPanel highlightPanel;
        JComboBox highlightPosition, multipleHighlightPolicy;  
        JTextField highlightField;
        JLabel hColorLabel, hPositionLabel, hMultipleLabel;
        JPanel h1Panel, h2Panel, h3Panel;
        highlightPanel = new JPanel();
        highlightPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("HighlightRelatedPreferences")));
        hColorLabel = new JLabel(messages.getString("ColorInHex"));
        highlightField = new JTextField(prefmngr.highlight_color);
        h1Panel = new JPanel();
        h1Panel.add(hColorLabel);
        h1Panel.add(highlightField);
        hPositionLabel = new JLabel(messages.getString("HighlightPosition"));
        highlightPosition = new JComboBox(new String[] {messages.getString("Middle"), messages.getString("Bottom")});
        highlightPosition.setSelectedItem(prefmngr.highlight_position);
        highlightPosition.setEditable(true);
        h2Panel = new JPanel();
        h2Panel.add(hPositionLabel);
        h2Panel.add(highlightPosition);
        hMultipleLabel = new JLabel(messages.getString("MultipleHighlightPolicy"));
        multipleHighlightPolicy = new JComboBox(new String[] {messages.getString("Allowed"), messages.getString("Disallowed")});
        multipleHighlightPolicy.setSelectedItem(prefmngr.multiple_highlight_policy);
        multipleHighlightPolicy.setEditable(true);
        h3Panel = new JPanel();
        h3Panel.add(hMultipleLabel);
        h3Panel.add(multipleHighlightPolicy);
        highlightPanel.setLayout(new GridLayout(0,1));
        highlightPanel.add(h1Panel);
        highlightPanel.add(h2Panel);
        highlightPanel.add(h3Panel);
		JPanel preferencesPanel = new JPanel();
		preferencesPanel.setLayout(new GridLayout(0,1));
		preferencesPanel.add(interfacePanel);
		@TIBETAN@preferencesPanel.add(tibetanPanel);
		preferencesPanel.add(romanPanel);
		preferencesPanel.add(highlightPanel);
		JOptionPane pane = new JOptionPane(preferencesPanel);
		JDialog dialog = pane.createDialog(this, messages.getString("FontAndStylePreferences"));
		
		// This returns only when the user has closed the dialog
		dialog.show();
		
		@TIBETAN@int old_tibetan_font_size = prefmngr.tibetan_font_size;
		@TIBETAN@try {
			@TIBETAN@prefmngr.tibetan_font_size = Integer.parseInt(tibetanFontSizes.getSelectedItem().toString());
		@TIBETAN@} catch (NumberFormatException ne) {
			@TIBETAN@prefmngr.tibetan_font_size = old_tibetan_font_size;
		@TIBETAN@}
		String old_font_face = new String(prefmngr.font_face);
		prefmngr.font_face = romanFontFamilies.getSelectedItem().toString();
		int old_font_size = prefmngr.font_size;
		try {
			prefmngr.font_size = Integer.parseInt(romanFontSizes.getSelectedItem().toString());
		}
		catch (NumberFormatException ne) {
			prefmngr.font_size = old_font_size;
		}
		prefmngr.setValue(prefmngr.FONT_FACE_KEY, prefmngr.font_face);
		prefmngr.setInt(prefmngr.FONT_SIZE_KEY, prefmngr.font_size);
		@TIBETAN@prefmngr.setInt(prefmngr.TIBETAN_FONT_SIZE_KEY, prefmngr.tibetan_font_size);
		if (qd.getEditor() != null) {
			@UNICODE@if (!(old_font_size == prefmngr.font_size && old_font_face.equals(prefmngr.font_face))){
			@TIBETAN@if (!(old_font_size == prefmngr.font_size && old_font_face.equals(prefmngr.font_face) && old_tibetan_font_size == prefmngr.tibetan_font_size)) {
				@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)qd.getEditor().getTextPane();
				@TIBETAN@dp.setByUserTibetanFontSize(prefmngr.tibetan_font_size);
				@TIBETAN@dp.setByUserRomanAttributeSet(prefmngr.font_face, prefmngr.font_size);
				@UNICODE@qd.getEditor().getTextPane().setFont(new Font(prefmngr.font_face, Font.PLAIN, prefmngr.font_size));
				qd.getEditor().render();
			}
		}
        String highlightPosVal = (String)highlightPosition.getSelectedItem();
        String multipleHighlightPolicyVal = (String)multipleHighlightPolicy.getSelectedItem();
        prefmngr.setValue(prefmngr.HIGHLIGHT_POSITION_KEY, highlightPosVal);
        if (qd.getEditor() != null) {
            qd.hp.setHighlightPosition(highlightPosVal);
        }
        if (multipleHighlightPolicyVal.equals(messages.getString("Allowed")))
            qd.player.setMultipleAnnotationPolicy(true);
        else
            qd.player.setMultipleAnnotationPolicy(false);
        String hexColor = highlightField.getText();
        try {
            Color c = Color.decode("0x"+hexColor);
            prefmngr.setValue(prefmngr.HIGHLIGHT_KEY, hexColor);
            if (qd.getEditor() != null) {
                if (qd.hp != null) {
                    qd.hp.setHighlightColor(c);
                }
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        if (needsToRestart)
        {
        	JOptionPane.showMessageDialog(this, messages.getString("ChangesToInterface"));
        	prefmngr.setValue(prefmngr.DEFAULT_INTERFACE_FONT_KEY, (String) supportedFonts.getSelectedItem());
        	prefmngr.setInt(prefmngr.DEFAULT_LANGUAGE_KEY, defaultLanguage.getSelectedIndex());
        	needsToRestart=false;
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

	}	private JScrollPane getScrollPaneForTextFile(ClassLoader resourceLoader,
			String textFileName)

	throws IOException, FileNotFoundException

	{

		InputStream in = resourceLoader.getResourceAsStream(textFileName);

		if (in == null) {

			throw new FileNotFoundException(textFileName);

		}

		try {

			BufferedReader changeReader = new BufferedReader(
					new InputStreamReader(in));

			StringBuffer concat = new StringBuffer();

			String line;

			while (null != (line = changeReader.readLine())) {

				concat.append(line);

				concat.append('\n');

			}

			JTextArea changeText = new JTextArea(concat.toString());

			changeText.setEditable(false);

			JScrollPane sp = new JScrollPane();

			sp.setViewportView(changeText);

			return sp;

		} catch (IOException ioe) {

			throw ioe;

		}

	}
}
/*JMenuItem closeItem = new JMenuItem(messages.getString("Close"));
 closeItem.setAccelerator(KeyStroke.getKeyStroke("control W"));
 closeItem.addActionListener(new ActionListener() {
 public void actionPerformed(ActionEvent e) {
 if (numberOfQDsOpen > 1) closeThisQDFrame();
 else {
 if (qd.getEditor().isEditable()) qd.saveTranscript();
 qd.removeContent();
 }
 }
 });
 */
