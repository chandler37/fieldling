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
				qd.saveTranscript();
				try {
					if (qd.checkTimeTimer != null) qd.checkTimeTimer.cancel();
					qd.getMediaPlayer().destroy();
				} catch (PanelPlayerException ppe) {
					ppe.printStackTrace();
				}
				System.exit(0);
			}
		});
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
				JFileChooser fc = new JFileChooser();
				if (fc.showDialog(QDShell.this, messages.getString("SelectMedia")) == JFileChooser.APPROVE_OPTION) {
					File mediaFile = fc.getSelectedFile();
					try {
						JFileChooser fc2 = new JFileChooser();
						fc2.addChoosableFileFilter(new QDFileFilter());
						if (fc2.showDialog(QDShell.this, messages.getString("SaveTranscript")) == JFileChooser.APPROVE_OPTION) {
							File transcriptFile = fc2.getSelectedFile();
							String mediaString = mediaFile.toURL().toString();
							qd.newTranscript(transcriptFile, mediaString);
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
				JFileChooser fc = new JFileChooser();
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
				qd.saveTranscript();
				System.exit(0);
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
			mediaItems[0].setSelected(true);
			qd.setMediaPlayer((PanelPlayer)moviePlayers.get(0)); //set qd media player to default
		}

		@TIBETAN@org.thdl.tib.input.JskadKeyboardManager keybdMgr = new org.thdl.tib.input.JskadKeyboardManager(org.thdl.tib.input.JskadKeyboardFactory.getAllAvailableJskadKeyboards());
		@TIBETAN@ButtonGroup keyboardGroup = new ButtonGroup();
		@TIBETAN@JMenuItem[] keyboardItems = new JRadioButtonMenuItem[keybdMgr.size()];
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
		JMenu preferencesMenu = new JMenu(messages.getString("Preferences"));
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
		@TIBETAN@keyboardItems[0].setSelected(true); //set keyboard to Wylie
		@TIBETAN@for (int i=0; i<keyboardItems.length; i++)
			@TIBETAN@preferencesMenu.add(keyboardItems[i]);
		//preferencesMenu.addSeparator();
			
		JMenuBar bar = new JMenuBar();
		projectMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(projectMenu);
		final JMenu[] configMenus = qd.getConfiguredMenus();
		for (int i=0; i<configMenus.length; i++) {
			configMenus[i].getPopupMenu().setLightWeightPopupEnabled(false);
			bar.add(configMenus[i]);
		}
		preferencesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(preferencesMenu);
		return bar;
		} catch (SecurityException se) {
			se.printStackTrace();
			return null;
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
