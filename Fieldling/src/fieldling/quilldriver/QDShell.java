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

import fieldling.mediaplayer.*;
import fieldling.util.I18n;
import fieldling.util.JdkVersionHacks;

public class QDShell extends JFrame {

    /** the middleman that keeps code regarding Tibetan keyboards
     *  clean */
     /*
    private final static JskadKeyboardManager keybdMgr
		= new JskadKeyboardManager(JskadKeyboardFactory.getAllAvailableJskadKeyboards());
*/
    /** When opening a file, this is the only extension QuillDriver
        cares about.  This is case-insensitive. */
    protected final static String dotQuillDriver = ".xml";

	ResourceBundle messages = null;
	QD qd = null;

	private static int numberOfQDsOpen = 0;
	public static final String NEW_FILE_ERROR_MESSAGE =
		"This file already exists! Type a new file name\n" +
		"instead of selecting an existing file.";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;

	//preference keys
	public static final String WINDOW_X_KEY = "WINDOW_X";
	public static final String WINDOW_Y_KEY = "WINDOW_Y";
	public static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT";
	public static final String WORKING_DIRECTORY_KEY = "WORKING_DIRECTORY";
	public static final String RECENT_FILES_KEY = "RECENT_FILES";
	public static final String MEDIA_DIRECTORY_KEY = "MEDIA_DIRECTORY";
	public static final String MEDIA_PLAYER_KEY = "MEDIA_PLAYER_KEY";
	public static final String SLOW_ADJUST_KEY = "SLOW_ADJUST";
	public static final String RAPID_ADJUST_KEY = "RAPID_ADJUST";
	public static final String PLAY_MINUS_KEY = "PLAY_MINUS";
	public static final String FONT_FACE_KEY = "FONT_FACE";
	public static final String FONT_SIZE_KEY = "FONT_SIZE";
	public static final String CONFIGURATION_KEY = "CONFIGURATION";
	public static final String TIBETAN_FONT_SIZE_KEY = "TIBETAN_FONT_SIZE";
	public static final String TIBETAN_KEYBOARD_KEY = "TIBETAN_KEYBOARD";


	//preference defaults and values
	private static Preferences myPrefs = Preferences.userNodeForPackage(QDShell.class);
	public static String media_directory = myPrefs.get(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));
	public static int slow_adjust = myPrefs.getInt(SLOW_ADJUST_KEY, 25); //in milliseconds
	public static int rapid_adjust = myPrefs.getInt(RAPID_ADJUST_KEY, 250); //in milliseconds
	public static int play_minus = myPrefs.getInt(PLAY_MINUS_KEY, 1000); // milliseconds
	public static String font_face = myPrefs.get(FONT_FACE_KEY, "Courier");
	public static int font_size = myPrefs.getInt(FONT_SIZE_KEY, 14);
	public static int tibetan_font_size = myPrefs.getInt(TIBETAN_FONT_SIZE_KEY, 36);


	public static void main(String[] args) {
		try {
			//ThdlDebug.attemptToSetUpLogFile("qd", ".log");

			Locale locale;
/*
			try {
	PrintStream ps =  new PrintStream(new FileOutputStream("qd.log"));
			System.setOut(ps);
			System.setErr(ps);
} catch (FileNotFoundException fnfe) {}
*/
/*
			if (args.length == 3) {
				locale = new Locale(new String(args[1]), new String(args[2]));
				I18n.setLocale(locale);
			}
*/

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {
			}
			new QDShell();
		} catch (NoClassDefFoundError err) {
			System.out.println(err.toString());
			//ThdlDebug.handleClasspathError("QuillDriver's CLASSPATH", err);
		}
	}
	public QDShell() {
		numberOfQDsOpen++;

		/*
		String configURL = null;
	String newURL = null;
	String editURL = null;
	String dtdURL = null;

		switch (args.length) {
			case 4:	dtdURL = new String(args[3]);
			case 3: newURL = new String(args[2]);
			case 2: editURL = new String(args[1]);
			case 1: configURL = new String(args[0]);
		}
		*/
		//NON-TIBETAN setTitle("QuillDriver");
		setTitle("QuillDriver-TIBETAN");
		messages = I18n.getResourceBundle();

		/*myPrefs = Preferences.userNodeForPackage(QDShell.class);
		working_directory = myPrefs.get(WORKING_DIRECTORY_KEY, System.getProperty("user.home"));
		media_directory = myPrefs.get(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));
		font_face = myPrefs.get(FONT_FACE_KEY, "Serif");
		font_size = myPrefs.getInt(FONT_SIZE_KEY, 14);
		tibetan_font_size = myPrefs.getInt(TIBETAN_FONT_SIZE_KEY, 36);*/

		setLocation(myPrefs.getInt(WINDOW_X_KEY, 0), myPrefs.getInt(WINDOW_Y_KEY, 0));
		setSize(new Dimension(myPrefs.getInt(WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width),
			myPrefs.getInt(WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));

		/*
		// Code for Merlin
		if (JdkVersionHacks.maximizedBothSupported(getToolkit())) {
			setLocation(0,0);
			setSize(getToolkit().getScreenSize().width,getToolkit().getScreenSize().height);
			setVisible(true);

			// call setExtendedState(Frame.MAXIMIZED_BOTH) if possible:
			if (!JdkVersionHacks.maximizeJFrameInBothDirections(this)) {
				throw new Error("badness at maximum: the frame state is supported, but setting that state failed.  JdkVersionHacks has a bug.");
			}
		} else {
			Dimension gs = getToolkit().getScreenSize();
			setLocation(0,0);
			setSize(new Dimension(gs.width, gs.height));
			setVisible(true);
		}
		*/

		/*if (args.length == 4) {
			qd = new QD();
			getContentPane().add(qd);
			setJMenuBar(getQDShellMenu());
		} else {*/
			/*try {
				String home = System.getProperty("user.home");
				String sep = System.getProperty("file.separator");
				String path = "file:" + home + sep + "put-in-home-directory" + sep;
				qd = new QD(path+"config.xml", path+"edit.xsl", path+"new.xsl", path+"dtd.dtd");*/

				//FIXME! deal with no DTD problem!!!
				/*ClassLoader cl = this.getClass().getClassLoader();
				qd = new QD(	cl.getResource("config.xml").toString(),
						cl.getResource("edit.xsl").toString(),
						cl.getResource("new.xsl").toString(),
						null);
				*/
				qd = new QD();
				getContentPane().add(qd);
				setJMenuBar(getQDShellMenu());
			/*} catch (SecurityException se) {
				se.printStackTrace();
			}*/
		//}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
		myPrefs.putInt(WINDOW_X_KEY, getX());
		myPrefs.putInt(WINDOW_Y_KEY, getY());
		myPrefs.putInt(WINDOW_WIDTH_KEY, getWidth());
		myPrefs.putInt(WINDOW_HEIGHT_KEY, getHeight());
		myPrefs.put(MEDIA_DIRECTORY_KEY, media_directory);
	}
	private void closeThisQDFrame() {
		/*i first used dispose() instead of hide(), which should clear up memory,
		but i got an error: can't dispose InputContext while it's active*/
		if (qd.getEditor() == null) { //no content in this QD window
			hide();
			numberOfQDsOpen--;
		} else { //there's a QD editor: save and close
			if (qd.getEditor().isEditable()) qd.saveTranscript();
			qd.removeContent();
			hide();
			numberOfQDsOpen--;
		}
	}
	public QD getQD() {
		return qd;
	}
	public JMenuBar getQDShellMenu() {
		JMenu projectMenu = new JMenu(messages.getString("File"));

		JMenuItem newItem = new JMenuItem(messages.getString("New"));
		newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = "To start a new annotation, first open a video, " +
							"and then create and save an empty annotation file.";
				JFileChooser fc = new JFileChooser(new File(media_directory));
				if (fc.showDialog(QDShell.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
					File mediaFile = fc.getSelectedFile();
					try {
						JFileChooser fc2 = new JFileChooser(new File(myPrefs.get(WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));
						fc2.addChoosableFileFilter(new QDFileFilter());
						if (fc2.showDialog(QDShell.this, messages.getString("SaveTranscript")) == JFileChooser.APPROVE_OPTION) {
							File transcriptFile = fc2.getSelectedFile();
							if (transcriptFile.exists()) { //error message: cannot make new file from existing file
								JOptionPane.showMessageDialog(QDShell.this, NEW_FILE_ERROR_MESSAGE, "No can do!", JOptionPane.WARNING_MESSAGE);
							} else {
								String transcriptString = transcriptFile.getAbsolutePath();
								int i = transcriptString.lastIndexOf('.');
								if (i<0) transcriptString += dotQuillDriver;
								else transcriptString = transcriptString.substring(0, i) + dotQuillDriver;
								transcriptFile = new File(transcriptString);
								fc2.rescanCurrentDirectory();
								myPrefs.put(WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR)+1));
								String mediaString = mediaFile.getAbsolutePath();
								media_directory = mediaString.substring(0, mediaString.lastIndexOf(FILE_SEPARATOR)+1);
								String mediaString2 = mediaFile.toURL().toString();
								if (qd.getEditor() == null) { //nothing in this QD
									//qd.saveTranscript();
									qd.newTranscript(transcriptFile, mediaString2);
								} else { //open new QDShell window
									QDShell qdsh = new QDShell();
									qdsh.getQD().newTranscript(transcriptFile, mediaString2);
								}
								makeRecentlyOpened(transcriptString);
							}
						}
					} catch (MalformedURLException murle) {
						murle.printStackTrace();
						//ThdlDebug.noteIffyCode();
					}
				}
			}
		});

		// Added a few lines - shreya
		JMenuItem UserInfo = new JMenuItem("User Info");
		UserInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateFrame();
			}
		});


		JMenuItem Feedback = new JMenuItem("Feedback");

		JMenuItem openItem = new JMenuItem(messages.getString("Open"));
		openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(new File(myPrefs.get(WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));
				fc.addChoosableFileFilter(new QDFileFilter());
				if (fc.showDialog(QDShell.this, messages.getString("OpenTranscript")) == JFileChooser.APPROVE_OPTION) {
					File transcriptFile = fc.getSelectedFile();
					String transcriptString = transcriptFile.getAbsolutePath();
					myPrefs.put(WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR)+1));
					if (qd.getEditor() == null) { //nothing in this QD
						//qd.saveTranscript();
						qd.loadTranscript(transcriptFile);
					} else { //open new QDShell window
						QDShell qdsh = new QDShell();
						qdsh.getQD().loadTranscript(transcriptFile);
					}
					makeRecentlyOpened(transcriptString);
				}
			}
		});

		JMenuItem closeItem = new JMenuItem(messages.getString("Close"));
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

		JMenuItem saveItem = new JMenuItem(messages.getString("Save"));
		saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (qd.getEditor().isEditable()) qd.saveTranscript();
			}
		});

		/*
		JMenuItem quitItem = new JMenuItem(messages.getString("Quit"));
		quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				System.exit(tryToQuit());

					putPreferences();
					System.exit(0);
				}
			}
		});*/

		projectMenu.add(newItem);
		projectMenu.addSeparator();
		projectMenu.add(openItem);
		projectMenu.add(UserInfo);
		projectMenu.add(Feedback);
		projectMenu.add(closeItem);
		projectMenu.addSeparator();
		projectMenu.add(saveItem);

		String r = myPrefs.get(RECENT_FILES_KEY, null);
		if (r != null) {
			projectMenu.addSeparator();
			StringTokenizer tok = new StringTokenizer(r, ",");
			int count = 1;
			while (tok.hasMoreTokens()) {
				final String fileName = (String)tok.nextToken();
				String menuName = String.valueOf(count) + ") " + fileName;
				JMenuItem openRecentItem = new JMenuItem(menuName);
				openRecentItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (qd.getEditor() == null) { //nothing in this QD
							//qd.saveTranscript();
							qd.loadTranscript(new File(fileName));
						} else { //open new QDShell window
							QDShell qdsh = new QDShell();
							qdsh.getQD().loadTranscript(new File(fileName));
						}
						makeRecentlyOpened(fileName);
					}
				});
				projectMenu.add(openRecentItem);
				count++;
			}
		}

		//projectMenu.addSeparator();
		//projectMenu.add(quitItem);

		try {
		final Configuration[] configurations = ConfigurationFactory.getAllQDConfigurations(this.getClass().getClassLoader());

		ButtonGroup configGroup = new ButtonGroup();
		JMenuItem[] configItems = new JRadioButtonMenuItem[configurations.length];
		for (int i=0; i<configurations.length; i++) {
			final int k=i;
			configItems[i] = new JRadioButtonMenuItem(configurations[i].getName());
			configItems[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myPrefs.put(CONFIGURATION_KEY, configurations[k].getName());
					if (qd.configure(configurations[k])) { //cannot re-configure if transcript is already loaded
						JMenuBar bar = QDShell.this.getJMenuBar();
						JMenu fileMenu = bar.getMenu(0);
						JMenu prefMenu = bar.getMenu(1);
						JMenuBar newBar = new JMenuBar();
						newBar.add(fileMenu);
						newBar.add(prefMenu);
						JMenu[] configMenus = qd.getConfiguredMenus();
						for (int z=0; z<configMenus.length; z++) {
							configMenus[z].getPopupMenu().setLightWeightPopupEnabled(false);
							newBar.add(configMenus[z]);
						}
						QDShell.this.setJMenuBar(newBar);
						QDShell.this.invalidate();
						QDShell.this.validate();
						QDShell.this.repaint();
					}
				}
			});
			configGroup.add(configItems[i]);
		}
		if (configItems.length > 0) {
			String configName = myPrefs.get(CONFIGURATION_KEY, configurations[0].getName());
			for (int j=0; j<configItems.length; j++) {
				if (configName.equals(configurations[j].getName())) {
					configItems[j].setSelected(true);
					qd.configure(configurations[j]);
					break;
				}
			}
		}

		java.util.List moviePlayers = PlayerFactory.getAllAvailablePlayers();
		ButtonGroup mediaGroup = new ButtonGroup();
		JMenuItem[] mediaItems = new JRadioButtonMenuItem[moviePlayers.size()];
		for (int i=0; i<moviePlayers.size(); i++) {
			final PanelPlayer mPlayer = (PanelPlayer)moviePlayers.get(i);
			mediaItems[i] = new JRadioButtonMenuItem(mPlayer.getIdentifyingName());
			mediaItems[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					qd.setMediaPlayer(mPlayer);
					myPrefs.put(MEDIA_PLAYER_KEY, mPlayer.getIdentifyingName());
				}
			});
			mediaGroup.add(mediaItems[i]);
		}
		if (mediaItems.length > 0) {
			PanelPlayer mPlayer = (PanelPlayer)moviePlayers.get(0);
			String myPlayerName = myPrefs.get(MEDIA_PLAYER_KEY, mPlayer.getIdentifyingName());
			if (myPlayerName.equals(mPlayer.getIdentifyingName())) { //user's player identical to QD's default player
				mediaItems[0].setSelected(true);
				qd.setMediaPlayer(mPlayer);
			} else {
				int i;
				PanelPlayer thisPlayer = null;
				for (i=0; i<mediaItems.length; i++) {
					thisPlayer = (PanelPlayer)moviePlayers.get(i);
					if (thisPlayer.getIdentifyingName().equals(myPlayerName)) break;
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

		JMenuItem fontItem = new JMenuItem("Fonts and styles...");
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getFontPreferences();
			}
		});

		JMenuItem timeCodeItem = new JMenuItem("Time coding...");
		timeCodeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getTimeCodePreferences();
			}
		});

		org.thdl.tib.input.JskadKeyboardManager keybdMgr = null;
		JMenuItem[] keyboardItems = null;
		try {
		keybdMgr = new org.thdl.tib.input.JskadKeyboardManager(org.thdl.tib.input.JskadKeyboardFactory.getAllAvailableJskadKeyboards());
		}catch (Exception e) {}
		if (keybdMgr != null) {
		ButtonGroup keyboardGroup = new ButtonGroup();
		keyboardItems = new JRadioButtonMenuItem[keybdMgr.size()];
		for (int i=0; i<keybdMgr.size(); i++) {
		    final org.thdl.tib.input.JskadKeyboard kbd = keybdMgr.elementAt(i);
		    //if (kbd.hasQuickRefFile()) {
			keyboardItems[i] = new JRadioButtonMenuItem(kbd.getIdentifyingString());
			keyboardItems[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				//    qd.changeKeyboard(kbd);
				    myPrefs.put(TIBETAN_KEYBOARD_KEY, kbd.getIdentifyingString());
				}
			    });
			keyboardGroup.add(keyboardItems[i]);
		}
		}



		JMenu preferencesMenu = new JMenu(messages.getString("Preferences"));
		preferencesMenu.add(fontItem);
		preferencesMenu.add(timeCodeItem);
		preferencesMenu.addSeparator();
		if (configItems.length > 0) {
			for (int i=0; i<configItems.length; i++)
				preferencesMenu.add(configItems[i]);
			preferencesMenu.addSeparator();
		}
		if (mediaItems.length > 0) {
			for (int i=0; i<mediaItems.length; i++)
				preferencesMenu.add(mediaItems[i]);
			preferencesMenu.addSeparator();
		}

		if (keybdMgr != null) {
		String userKeyboard = myPrefs.get(TIBETAN_KEYBOARD_KEY, keybdMgr.elementAt(0).getIdentifyingString());
		int i;
		for (i=0; i<keybdMgr.size(); i++)
			if (userKeyboard.equals(keybdMgr.elementAt(i).getIdentifyingString())) break;
		if (i == 0 || i == keybdMgr.size()) //keyboard either can't be found or is default Wylie
			keyboardItems[0].setSelected(true);
		else { //keyboard is other than default Wylie keyboard: must explicitly change keyboard
			keyboardItems[i].setSelected(true);
			//qd.changeKeyboard(keybdMgr.elementAt(i));
		}
		for (int k=0; k<keyboardItems.length; k++)
			preferencesMenu.add(keyboardItems[k]);
		}

		JMenuBar bar = new JMenuBar();
		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(projectMenu);
		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(preferencesMenu);
		final JMenu[] configMenus = qd.getConfiguredMenus();
		for (int k=0; k<configMenus.length; k++) {
			configMenus[k].getPopupMenu().setLightWeightPopupEnabled(false);
			bar.add(configMenus[k]);
		}
		return bar;
		} catch (SecurityException se) {
			se.printStackTrace();
			return null;
		}
	}

	private void makeRecentlyOpened(String s) {
		String r = myPrefs.get(RECENT_FILES_KEY, null);
		if (r == null) myPrefs.put(RECENT_FILES_KEY, s);
		else {
			LinkedList recents = new LinkedList();
			recents.add(s);
			StringTokenizer tok = new StringTokenizer(r, ",");
			while (tok.hasMoreTokens()) {
				String s2 = tok.nextToken();
				if (!s.equals(s2)) recents.add(s2);
			}
			int k;
			if (recents.size() > MAXIMUM_NUMBER_OF_RECENT_FILES) k = MAXIMUM_NUMBER_OF_RECENT_FILES;
			else k = recents.size();
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<k; i++) {
				sb.append((String)recents.removeFirst());
				sb.append(',');
			}
			myPrefs.put(RECENT_FILES_KEY, sb.toString());
		}
	}
	private void getTimeCodePreferences() {
		 //allows user to change slow adjust, rapid adjust, and play minus parameters
		 JPanel slowAdjustPanel = new JPanel(new BorderLayout());
		 slowAdjustPanel.setBorder(BorderFactory.createTitledBorder("Slow increase/decrease value (in milliseconds)"));
		 JTextField slowAdjustField = new JTextField(String.valueOf(slow_adjust));
		 slowAdjustField.setPreferredSize(new Dimension(240,30));
		 slowAdjustPanel.add(slowAdjustField);
		 JPanel rapidAdjustPanel = new JPanel(new BorderLayout());
		 rapidAdjustPanel.setBorder(BorderFactory.createTitledBorder("Rapid increase/decrease value (in milliseconds)"));
		 JTextField rapidAdjustField = new JTextField(String.valueOf(rapid_adjust));
		 rapidAdjustField.setPreferredSize(new Dimension(240,30));
		 rapidAdjustPanel.add(rapidAdjustField);
		 JPanel playMinusPanel = new JPanel(new BorderLayout());
		 playMinusPanel.setBorder(BorderFactory.createTitledBorder("Play minus value (in milliseconds)"));
		 JTextField playMinusField = new JTextField(String.valueOf(play_minus));
		 playMinusField.setPreferredSize(new Dimension(240,30));
		 playMinusPanel.add(playMinusField);

		 JPanel preferencesPanel = new JPanel();
		preferencesPanel.setLayout(new GridLayout(3,1));
		preferencesPanel.add(slowAdjustPanel);
		preferencesPanel.add(rapidAdjustPanel);
		preferencesPanel.add(playMinusPanel);

		 JOptionPane pane = new JOptionPane(preferencesPanel);
		 JDialog dialog = pane.createDialog(this, "Time Coding Preferences");
		 // This returns only when the user has closed the dialog
		 dialog.show();
		 int old_slow_adjust = slow_adjust;
		 try {
			slow_adjust = Integer.parseInt(slowAdjustField.getText());
		} catch (NumberFormatException ne) {
		}

		 int old_rapid_adjust = rapid_adjust;
		 try {
			rapid_adjust = Integer.parseInt(rapidAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
		 int old_play_minus = play_minus;
		 try {
			play_minus = Integer.parseInt(playMinusField.getText());
		} catch (NumberFormatException ne) {
		}
		// note: if these become negative numbers no error
		if (old_slow_adjust != slow_adjust) myPrefs.putInt(SLOW_ADJUST_KEY, slow_adjust);
		if (old_rapid_adjust != rapid_adjust) myPrefs.putInt(RAPID_ADJUST_KEY, rapid_adjust);
		if (old_play_minus != play_minus) myPrefs.putInt(PLAY_MINUS_KEY, play_minus);
	}
	private void getFontPreferences() {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = genv.getAvailableFontFamilyNames();

		JPanel tibetanPanel;
		JComboBox tibetanFontSizes;
		tibetanPanel = new JPanel();
		tibetanPanel.setBorder(BorderFactory.createTitledBorder("Tibetan Font Size"));
		tibetanFontSizes = new JComboBox(new String[] {"22","24","26","28","30","32","34","36","48","72"});
		tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());
		tibetanFontSizes.setSelectedItem(String.valueOf(tibetan_font_size));
		tibetanFontSizes.setEditable(true);
		tibetanPanel.add(tibetanFontSizes);

		JPanel romanPanel;
		JComboBox romanFontFamilies;
		JComboBox romanFontSizes;
		romanPanel = new JPanel();
		romanPanel.setBorder(BorderFactory.createTitledBorder("Non-Tibetan Font and Size"));
		romanFontFamilies = new JComboBox(fontNames);
		romanFontFamilies.setMaximumSize(romanFontFamilies.getPreferredSize());
		romanFontFamilies.setSelectedItem(font_face);
		romanFontFamilies.setEditable(true);
		romanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
		romanFontSizes.setMaximumSize(romanFontSizes.getPreferredSize());
		romanFontSizes.setSelectedItem(String.valueOf(font_size));
		romanFontSizes.setEditable(true);
		romanPanel.setLayout(new GridLayout(1,2));
		romanPanel.add(romanFontFamilies);
		romanPanel.add(romanFontSizes);

		JPanel preferencesPanel = new JPanel();
		//NON-TIBETAN preferencesPanel.setLayout(new BorderLayout());
		preferencesPanel.setLayout(new GridLayout(2,1));
		preferencesPanel.add(tibetanPanel);
		preferencesPanel.add(romanPanel);

		JOptionPane pane = new JOptionPane(preferencesPanel);
		JDialog dialog = pane.createDialog(this, "Font and Style Preferences");

        // This returns only when the user has closed the dialog
		dialog.show();

		int old_tibetan_font_size = tibetan_font_size;
		try {
			tibetan_font_size = Integer.parseInt(tibetanFontSizes.getSelectedItem().toString());
		} catch (NumberFormatException ne) {
			tibetan_font_size = old_tibetan_font_size;
		}

		String old_font_face = new String(font_face);
		font_face = romanFontFamilies.getSelectedItem().toString();
		int old_font_size = font_size;
		try {
			font_size = Integer.parseInt(romanFontSizes.getSelectedItem().toString());
		}
		catch (NumberFormatException ne) {
			font_size = old_font_size;
		}

		myPrefs.put(FONT_FACE_KEY, font_face);
		myPrefs.putInt(FONT_SIZE_KEY, font_size);
		myPrefs.putInt(TIBETAN_FONT_SIZE_KEY, tibetan_font_size);

		if (qd.getEditor() != null) {
			//NON-TIBETAN if (!(old_font_size == font_size && old_font_face.equals(font_face))) {
			if (!(old_font_size == font_size && old_font_face.equals(font_face) && old_tibetan_font_size == tibetan_font_size)) {
				org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)qd.getEditor().getTextPane();
				dp.setByUserTibetanFontSize(tibetan_font_size);
				dp.setByUserRomanAttributeSet(font_face, font_size);
				//NON-TIBETAN qd.getEditor().getTextPane().setFont(new Font(font_face, Font.PLAIN, font_size));
				qd.getEditor().render();
			}
		}
	}

	private class QDFileFilter extends javax.swing.filechooser.FileFilter {
		// accepts all directories and all savant files

		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			return f.getName().toLowerCase().endsWith(QDShell.dotQuillDriver);
		}

		//the description of this filter
		public String getDescription() {
			return "QD File Format (" + QDShell.dotQuillDriver + ")";
		}

	}

	void CreateFrame()
  {
	  Userinfo ui= new Userinfo();
	  ui.show();
  }
}
