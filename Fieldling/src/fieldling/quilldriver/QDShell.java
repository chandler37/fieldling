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
	PreferenceManager prefmngr= new fieldling.quilldriver.PreferenceManager();;
	private static int numberOfQDsOpen = 0;
	public static final String NEW_FILE_ERROR_MESSAGE =
		"This file already exists! Type a new file name\n" +
		"instead of selecting an existing file.";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;

	public static void main(String[] args) {
		try {
			//ThdlDebug.attemptToSetUpLogFile("qd", ".log");
			Locale locale;
			//note: by default Java 1.5 (and 1.4??) uses a buggy transformer based on an
			//earlier version of Xalan-Java. so we need to set this system property
			System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
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
			//LOGGINGSystem.out.println(err.toString());
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
		@UNICODE@setTitle("QuillDriver");
		@TIBETAN@setTitle("QuillDriver-TIBETAN");
		messages = I18n.getResourceBundle();
		setLocation(prefmngr.getInt(prefmngr.WINDOW_X_KEY, 0), prefmngr.getInt(prefmngr.WINDOW_Y_KEY, 0));
		setSize(new Dimension(prefmngr.getInt(prefmngr.WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width),
			prefmngr.getInt(prefmngr.WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));
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
				qd = new QD(prefmngr);
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

		prefmngr.setInt(prefmngr.WINDOW_X_KEY, getX());

		prefmngr.setInt(prefmngr.WINDOW_Y_KEY, getY());

		prefmngr.setInt(prefmngr.WINDOW_WIDTH_KEY, getWidth());

		prefmngr.setInt(prefmngr.WINDOW_HEIGHT_KEY, getHeight());

		prefmngr.setValue(prefmngr.MEDIA_DIRECTORY_KEY, prefmngr.media_directory);

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

				JFileChooser fc = new JFileChooser(new File(prefmngr.media_directory));

				if (fc.showDialog(QDShell.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {

					File mediaFile = fc.getSelectedFile();

					try {

						JFileChooser fc2 = new JFileChooser(new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));

						fc2.addChoosableFileFilter(new QDFileFilter());



						//Font ipaFont = new Font("SILDoulosUnicodeIPA",Font.PLAIN,10);

						//fc2.setFont(ipaFont);

						fc2.updateUI();





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

								prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR)+1));

								String mediaString = mediaFile.getAbsolutePath();

								prefmngr.media_directory = mediaString.substring(0, mediaString.lastIndexOf(FILE_SEPARATOR)+1);

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



		JMenuItem openItem = new JMenuItem(messages.getString("Open"));

		openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));

		openItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser(new File(prefmngr.getValue(prefmngr.WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));

				fc.addChoosableFileFilter(new QDFileFilter());

				if (fc.showDialog(QDShell.this, messages.getString("OpenTranscript")) == JFileChooser.APPROVE_OPTION) {

					File transcriptFile = fc.getSelectedFile();

					String transcriptString = transcriptFile.getAbsolutePath();

					prefmngr.setValue(prefmngr.WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR)+1));

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

		projectMenu.add(closeItem);

		projectMenu.addSeparator();

		projectMenu.add(saveItem);



		String r = prefmngr.getValue(prefmngr.RECENT_FILES_KEY, null);

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

					prefmngr.setValue(prefmngr.CONFIGURATION_KEY, configurations[k].getName());

					if (qd.configure(configurations[k])) { //cannot re-configure if transcript is already loaded

						JMenuBar bar = QDShell.this.getJMenuBar();

						JMenu fileMenu = bar.getMenu(0);

						JMenu prefMenu = bar.getMenu(1);

						JMenu betaMenu = bar.getMenu(2);

						JMenuBar newBar = new JMenuBar();

						newBar.add(fileMenu);

						newBar.add(prefMenu);

						newBar.add(betaMenu);

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

			String configName = prefmngr.getValue(prefmngr.CONFIGURATION_KEY, configurations[0].getName());

            int j=0;

			for (j=0; j<configItems.length; j++) {

				if (configName.equals(configurations[j].getName())) {

					configItems[j].setSelected(true);

					qd.configure(configurations[j]);

					break;

				}

			}

            if (j == configItems.length) { // in case the saved configuration doesn't actually exist (e.g. has been renamed)

                configItems[0].setSelected(true);

                qd.configure(configurations[0]);

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

					prefmngr.setValue(prefmngr.MEDIA_PLAYER_KEY, mPlayer.getIdentifyingName());

				}

			});

			mediaGroup.add(mediaItems[i]);

		}

		if (mediaItems.length > 0) {

			PanelPlayer mPlayer = (PanelPlayer)moviePlayers.get(0);

			String myPlayerName = prefmngr.getValue(prefmngr.MEDIA_PLAYER_KEY, mPlayer.getIdentifyingName());

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



		JMenuItem fontItem = new JMenuItem("Display...");

		fontItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				getDisplayPreferences();

			}

		});



		JMenuItem timeCodeItem = new JMenuItem("Time coding...");

		timeCodeItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {

				getTimeCodePreferences();

			}

		});



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



		//Beta menu for beta-only menu-options:

		JMenuItem feedbackItem = new JMenuItem("Feedback");

		feedbackItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				new UserFeedback(QDShell.this);

			}

		});



        //about menu item

        JMenuItem aboutItem = new JMenuItem("About QuillDriver");

        try {

            final JScrollPane sp = getScrollPaneForTextFile(this.getClass().getClassLoader(), "about.txt");

            aboutItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    JFrame f = new JFrame();

                    f.setSize(500,400);

                    f.getContentPane().add(sp);

                    f.setVisible(true);

                }

            }); 

        } catch (IOException ioe ) {

            ioe.printStackTrace();

        }

        

		JMenu betaMenu =  new JMenu("Help");

        betaMenu.add(aboutItem);

		betaMenu.add(feedbackItem);



		JMenuBar bar = new JMenuBar();

		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);

		bar.add(projectMenu);

		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);

		bar.add(preferencesMenu);

		betaMenu.getPopupMenu().setLightWeightPopupEnabled(false);

		bar.add(betaMenu);

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

		String r = prefmngr.getValue(prefmngr.RECENT_FILES_KEY, null);

		if (r == null) prefmngr.setValue(prefmngr.RECENT_FILES_KEY, s);

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

			prefmngr.setValue(prefmngr.RECENT_FILES_KEY, sb.toString());

		}

	}

	private void getTimeCodePreferences() {

		 //allows user to change slow adjust, rapid adjust, and play minus parameters

		 JPanel slowAdjustPanel = new JPanel(new BorderLayout());

		 slowAdjustPanel.setBorder(BorderFactory.createTitledBorder("Slow increase/decrease value (in milliseconds)"));

		 JTextField slowAdjustField = new JTextField(String.valueOf(prefmngr.slow_adjust));

		 slowAdjustField.setPreferredSize(new Dimension(240,30));

		 slowAdjustPanel.add(slowAdjustField);

		 JPanel rapidAdjustPanel = new JPanel(new BorderLayout());

		 rapidAdjustPanel.setBorder(BorderFactory.createTitledBorder("Rapid increase/decrease value (in milliseconds)"));

		 JTextField rapidAdjustField = new JTextField(String.valueOf(prefmngr.rapid_adjust));

		 rapidAdjustField.setPreferredSize(new Dimension(240,30));

		 rapidAdjustPanel.add(rapidAdjustField);

		 JPanel playMinusPanel = new JPanel(new BorderLayout());

		 playMinusPanel.setBorder(BorderFactory.createTitledBorder("Play minus value (in milliseconds)"));

		 JTextField playMinusField = new JTextField(String.valueOf(prefmngr.play_minus));

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

		 int old_slow_adjust = prefmngr.slow_adjust;

		 try {

			prefmngr.slow_adjust = Integer.parseInt(slowAdjustField.getText());

		} catch (NumberFormatException ne) {

		}



		 int old_rapid_adjust = prefmngr.rapid_adjust;

		 try {

			prefmngr.rapid_adjust = Integer.parseInt(rapidAdjustField.getText());

		} catch (NumberFormatException ne) {

		}

		 int old_play_minus = prefmngr.play_minus;

		 try {

			prefmngr.play_minus = Integer.parseInt(playMinusField.getText());

		} catch (NumberFormatException ne) {

		}

		// note: if these become negative numbers no error

		if (old_slow_adjust != prefmngr.slow_adjust) prefmngr.setInt(prefmngr.SLOW_ADJUST_KEY, prefmngr.slow_adjust);

		if (old_rapid_adjust != prefmngr.rapid_adjust) prefmngr.setInt(prefmngr.RAPID_ADJUST_KEY, prefmngr.rapid_adjust);

		if (old_play_minus != prefmngr.play_minus) prefmngr.setInt(prefmngr.PLAY_MINUS_KEY, prefmngr.play_minus);

	}

	private void getDisplayPreferences() {

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();

		String[] fontNames = genv.getAvailableFontFamilyNames();



		@TIBETAN@JPanel tibetanPanel;

		@TIBETAN@JComboBox tibetanFontSizes;

		@TIBETAN@tibetanPanel = new JPanel();

		@TIBETAN@tibetanPanel.setBorder(BorderFactory.createTitledBorder("Tibetan Font Size"));

		@TIBETAN@tibetanFontSizes = new JComboBox(new String[] {"22","24","26","28","30","32","34","36","48","72"});

		@TIBETAN@tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());

		@TIBETAN@tibetanFontSizes.setSelectedItem(String.valueOf(prefmngr.tibetan_font_size));

		@TIBETAN@tibetanFontSizes.setEditable(true);

		@TIBETAN@tibetanPanel.add(tibetanFontSizes);



		JPanel romanPanel;

		JComboBox romanFontFamilies;

		JComboBox romanFontSizes;

		romanPanel = new JPanel();

		romanPanel.setBorder(BorderFactory.createTitledBorder("Non-Tibetan Font and Size"));

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

        highlightPanel.setBorder(BorderFactory.createTitledBorder("Highlight-Related Preferences"));

        

        hColorLabel = new JLabel("Color in hex [c.f. http://www.hypersolutions.org/pages/rgbhex.html]: ");

        highlightField = new JTextField(prefmngr.highlight_color);

        h1Panel = new JPanel();

        h1Panel.add(hColorLabel);

        h1Panel.add(highlightField);

        

        hPositionLabel = new JLabel("Highlight position: ");

        highlightPosition = new JComboBox(new String[] {"Middle", "Bottom"});

        highlightPosition.setSelectedItem(prefmngr.highlight_position);

        highlightPosition.setEditable(true);

        h2Panel = new JPanel();

        h2Panel.add(hPositionLabel);

        h2Panel.add(highlightPosition);

        

        hMultipleLabel = new JLabel("Multiple highlight policy: ");

        multipleHighlightPolicy = new JComboBox(new String[] {"Allowed", "Disallowed"});

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

		@TIBETAN@preferencesPanel.add(tibetanPanel);

		preferencesPanel.add(romanPanel);

        preferencesPanel.add(highlightPanel);



		JOptionPane pane = new JOptionPane(preferencesPanel);

		JDialog dialog = pane.createDialog(this, "Font and Style Preferences");



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

			@UNICODE@if (!(old_font_size == prefmngr.font_size && old_font_face.equals(prefmngr.font_face))) {

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

        if (multipleHighlightPolicyVal.equals("Allowed"))

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

  

    private JScrollPane getScrollPaneForTextFile(ClassLoader resourceLoader, String textFileName) 

        throws IOException, FileNotFoundException

    {

        InputStream in = resourceLoader.getResourceAsStream(textFileName);

        if (in == null) {

            throw new FileNotFoundException(textFileName);

        }



        try {

            BufferedReader changeReader = new BufferedReader(new InputStreamReader(in));

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

