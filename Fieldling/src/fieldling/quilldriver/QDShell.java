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
	Preferences myPrefs = null;
	Preferences sysPrefs = null;

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	//preference keys
	public static final String WINDOW_X_KEY = "WINDOW_X";
	public static final String WINDOW_Y_KEY = "WINDOW_Y";
	public static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT";
	public static final String WORKING_DIRECTORY_KEY = "WORKING_DIRECTORY";
	public static final String MEDIA_DIRECTORY_KEY = "MEDIA_DIRECTORY";
	public static final String MEDIA_PLAYER_KEY = "MEDIA_PLAYER_KEY";
	public static final String FONT_FACE_KEY = "FONT_FACE";
	public static final String FONT_SIZE_KEY = "FONT_SIZE";
	@TIBETAN@public static final String TIBETAN_FONT_SIZE_KEY = "TIBETAN_FONT_SIZE";
	@TIBETAN@public static final String TIBETAN_KEYBOARD_KEY = "TIBETAN_KEYBOARD";
	
	//preference defaults and values
	public static String working_directory;
	public static String media_directory;
	public static String font_face = "Serif";
	public static int font_size = 14;
	@TIBETAN@public static int tibetan_font_size = 36;
	

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

			QDShell qdsh = new QDShell(args);
			qdsh.setVisible(true);
		} catch (NoClassDefFoundError err) {
			System.out.println(err.toString());
			//ThdlDebug.handleClasspathError("QuillDriver's CLASSPATH", err);
		}
	}

	public QDShell(String[] args) {	
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
		
		setTitle("QuillDriver");
		messages = I18n.getResourceBundle();

		myPrefs = Preferences.userNodeForPackage(QDShell.class);
		working_directory = myPrefs.get(WORKING_DIRECTORY_KEY, System.getProperty("user.home"));
		media_directory = myPrefs.get(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));
		font_face = myPrefs.get(FONT_FACE_KEY, "Serif");
		font_size = myPrefs.getInt(FONT_SIZE_KEY, 14);
		@TIBETAN@tibetan_font_size = myPrefs.getInt(TIBETAN_FONT_SIZE_KEY, 36); 
		
		setLocation(myPrefs.getInt(WINDOW_X_KEY, 0), myPrefs.getInt(WINDOW_Y_KEY, 0));
		setSize(new Dimension(myPrefs.getInt(WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width), 
			myPrefs.getInt(WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));
		setVisible(true);
		
		
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

		if (args.length == 4) {
			qd = new QD();
			getContentPane().add(qd);
			setJMenuBar(getQDShellMenu());
		} else {
			try {
				/*String home = System.getProperty("user.home");
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
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				System.exit(tryToQuit());
			}
		});
	}
	private int tryToQuit() {
		qd.saveTranscript();
		myPrefs.putInt(WINDOW_X_KEY, getX());
		myPrefs.putInt(WINDOW_Y_KEY, getY());
		myPrefs.putInt(WINDOW_WIDTH_KEY, getWidth());
		myPrefs.putInt(WINDOW_HEIGHT_KEY, getHeight());
		myPrefs.put(WORKING_DIRECTORY_KEY, working_directory);
		myPrefs.put(MEDIA_DIRECTORY_KEY, media_directory);
		myPrefs.put(FONT_FACE_KEY, font_face);
		myPrefs.putInt(FONT_SIZE_KEY, font_size);
		if (qd.getMediaPlayer() != null) myPrefs.put(MEDIA_PLAYER_KEY, qd.getMediaPlayer().getIdentifyingName());
		
		@TIBETAN@myPrefs.putInt(TIBETAN_FONT_SIZE_KEY, tibetan_font_size);
		@TIBETAN@if (qd.getKeyboard() != null) myPrefs.put(TIBETAN_KEYBOARD_KEY, qd.getKeyboard().getIdentifyingString());
		
		try {
			if (qd.checkTimeTimer != null) qd.checkTimeTimer.cancel();
			qd.getMediaPlayer().destroy();
		} catch (PanelPlayerException ppe) {
			ppe.printStackTrace();
			return 1;
		}
		return 0;
	}
	public JMenuBar getQDShellMenu() {
		JMenu projectMenu = new JMenu(messages.getString("File"));

		JMenuItem newItem = new JMenuItem(messages.getString("New"));
		newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				qd.saveTranscript();
				String s = "To start a new annotation, first open a video, " +
							"and then create and save an empty annotation file.";
				JFileChooser fc = new JFileChooser(new File(media_directory));
				if (fc.showDialog(QDShell.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
					File mediaFile = fc.getSelectedFile();
					try {
						JFileChooser fc2 = new JFileChooser(new File(working_directory));
						fc2.addChoosableFileFilter(new QDFileFilter());
						if (fc2.showDialog(QDShell.this, messages.getString("SaveTranscript")) == JFileChooser.APPROVE_OPTION) {
							File transcriptFile = fc2.getSelectedFile();
							String transcriptString = transcriptFile.getAbsolutePath();
							int i = transcriptString.lastIndexOf('.');
							if (i<0) transcriptString += dotQuillDriver;
							else transcriptString = transcriptString.substring(0, i) + dotQuillDriver;
							transcriptFile = new File(transcriptString);
							fc2.rescanCurrentDirectory();
							working_directory = transcriptString.substring(0, transcriptString.lastIndexOf(FILE_SEPARATOR)+1);
							String mediaString = mediaFile.getAbsolutePath();
							media_directory = mediaString.substring(0, mediaString.lastIndexOf(FILE_SEPARATOR)+1);
							String mediaString2 = mediaFile.toURL().toString();
							qd.newTranscript(transcriptFile, mediaString2);
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
				qd.saveTranscript();
				JFileChooser fc = new JFileChooser(new File(working_directory));
				fc.addChoosableFileFilter(new QDFileFilter());
				if (fc.showDialog(QDShell.this, messages.getString("OpenTranscript")) == JFileChooser.APPROVE_OPTION) {
					File transcriptFile = fc.getSelectedFile();
					qd.loadTranscript(transcriptFile);
				}
			}
		});
/*
		JMenuItem closeItem = new JMenuItem(messages.getString("Close"));
		closeItem.setAccelerator(KeyStroke.getKeyStroke("control W"));
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		*/
		JMenuItem saveItem = new JMenuItem(messages.getString("Save"));
		saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				qd.saveTranscript();
			}
		});

		JMenuItem quitItem = new JMenuItem(messages.getString("Quit"));
		quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(tryToQuit());
			}
		});
		
		projectMenu.add(newItem);
		projectMenu.addSeparator(); 
		projectMenu.add(openItem);
		//projectMenu.add(closeItem);
		projectMenu.addSeparator();
		projectMenu.add(saveItem);
		projectMenu.addSeparator();
		projectMenu.add(quitItem);
		
		try {
		final Configuration[] configurations = ConfigurationFactory.getAllQDConfigurations(this.getClass().getClassLoader());

		ButtonGroup configGroup = new ButtonGroup();
		JMenuItem[] configItems = new JRadioButtonMenuItem[configurations.length];
		for (int i=0; i<configurations.length; i++) {
			final int k=i;
			configItems[i] = new JRadioButtonMenuItem(configurations[i].getName());
			configItems[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					qd.configure(configurations[k]);
					JMenu[] configMenus = qd.getConfiguredMenus();
					configMenus[0].getPopupMenu().setLightWeightPopupEnabled(false);
					configMenus[1].getPopupMenu().setLightWeightPopupEnabled(false);
					JMenuBar bar = QDShell.this.getJMenuBar();
					JMenu fileMenu = bar.getMenu(0);
					JMenu prefMenu = bar.getMenu(3);
					JMenuBar newBar = new JMenuBar();
					newBar.add(fileMenu);
					newBar.add(configMenus[0]);
					newBar.add(configMenus[1]);
					newBar.add(prefMenu);
					QDShell.this.setJMenuBar(newBar);
					QDShell.this.invalidate();
					QDShell.this.validate();
					QDShell.this.repaint();
				}
			});
			configGroup.add(configItems[i]);
		}
		if (configItems.length > 0) {
			configItems[0].setSelected(true);
			qd.configure(configurations[0]);
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
				@TIBETAN@}
			    @TIBETAN@});
			@TIBETAN@keyboardGroup.add(keyboardItems[i]);
		@TIBETAN@}
		@TIBETAN@}

		JMenu preferencesMenu = new JMenu(messages.getString("Preferences"));
		preferencesMenu.add(fontItem);
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
		@TIBETAN@String userKeyboard = myPrefs.get(TIBETAN_KEYBOARD_KEY, keybdMgr.elementAt(0).getIdentifyingString());
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
			
		JMenuBar bar = new JMenuBar();
		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(projectMenu);
		final JMenu[] configMenus = qd.getConfiguredMenus();
		for (int k=0; k<configMenus.length; k++) {
			configMenus[k].getPopupMenu().setLightWeightPopupEnabled(false);
			bar.add(configMenus[k]);
		}
		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(preferencesMenu);
		return bar;
		} catch (SecurityException se) {
			se.printStackTrace();
			return null;
		}
	}

	private void getFontPreferences() {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = genv.getAvailableFontFamilyNames();

		@TIBETAN@JPanel tibetanPanel;
		@TIBETAN@JComboBox tibetanFontSizes;
		@TIBETAN@tibetanPanel = new JPanel();
		@TIBETAN@tibetanPanel.setBorder(BorderFactory.createTitledBorder("Set Tibetan Font Size"));
		@TIBETAN@tibetanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
		@TIBETAN@tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());
		@TIBETAN@tibetanFontSizes.setEditable(true);
		@TIBETAN@tibetanPanel.add(tibetanFontSizes);

		JPanel romanPanel;
		JComboBox romanFontFamilies;
		JComboBox romanFontSizes;
		romanPanel = new JPanel();
		romanPanel.setBorder(BorderFactory.createTitledBorder("Set non-Tibetan Font and Size"));
		romanFontFamilies = new JComboBox(fontNames);
		romanFontFamilies.setMaximumSize(romanFontFamilies.getPreferredSize());
		romanFontFamilies.setEditable(true);
		romanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
		romanFontSizes.setMaximumSize(romanFontSizes.getPreferredSize());
		romanFontSizes.setEditable(true);
		romanPanel.setLayout(new GridLayout(1,2));
		romanPanel.add(romanFontFamilies);
		romanPanel.add(romanFontSizes);

		JPanel preferencesPanel = new JPanel();
		@UNICODE@preferencesPanel.setLayout(new BorderLayout());
		@TIBETAN@preferencesPanel.setLayout(new GridLayout(2,1));
		@TIBETAN@preferencesPanel.add(tibetanPanel);
		preferencesPanel.add(romanPanel);

		JOptionPane pane = new JOptionPane(preferencesPanel);
		JDialog dialog = pane.createDialog(this, "Preferences");

        // This returns only when the user has closed the dialog:
		dialog.show();
		
		@TIBETAN@int old_tibetan_font_size = tibetan_font_size;
		@TIBETAN@try {
			@TIBETAN@tibetan_font_size = Integer.parseInt(tibetanFontSizes.getSelectedItem().toString());
		@TIBETAN@} catch (NumberFormatException ne) {
			@TIBETAN@tibetan_font_size = old_tibetan_font_size;
		@TIBETAN@}

		font_face = romanFontFamilies.getSelectedItem().toString();
		int old_font_size = font_size;
		try {
			font_size = Integer.parseInt(romanFontSizes.getSelectedItem().toString());
		}
		catch (NumberFormatException ne) {
			font_size = old_font_size;
		}
		
		if (qd.getEditor() != null) {
			@TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane)qd.getEditor().getTextPane();
			@TIBETAN@dp.setByUserTibetanFontSize(tibetan_font_size);
			@TIBETAN@dp.setByUserRomanAttributeSet(font_face, font_size);
			@UNICODE@qd.getEditor().getTextPane().setFont(new Font(font_face, Font.PLAIN, font_size));
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
}
