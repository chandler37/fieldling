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

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

import fieldling.mediaplayer.*;
//import org.thdl.util.ThdlDebug;
//import org.thdl.util.ActionListener;
//import org.thdl.util.ThdlOptions;
//import org.thdl.tib.input.JskadKeyboardManager;
//import org.thdl.tib.input.JskadKeyboardFactory;
//import org.thdl.tib.input.JskadKeyboard;
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
try {
	PrintStream ps =  new PrintStream(new FileOutputStream("qd.log"));
			System.setOut(ps);
			System.setErr(ps);
} catch (FileNotFoundException fnfe) {}
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
			qd = new QD(configURL, editURL, newURL, dtdURL);
			getContentPane().add(qd);
			setJMenuBar(getQDShellMenu());
		} else {
			try {
				String home = System.getProperty("user.home");
				String sep = System.getProperty("file.separator");
				String path = "file:" + home + sep + "put-in-home-directory" + sep;
				qd = new QD(path+"config.xml", path+"edit.xsl", path+"new.xsl", path+"dtd.dtd");
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
				System.exit(0);
			}
		});
	}

	public JMenuBar getQDShellMenu() {
		JMenu projectMenu = new JMenu(messages.getString("File"));

		JMenuItem newItem = new JMenuItem(messages.getString("New"));
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

		JMenuItem saveItem = new JMenuItem(messages.getString("Save"));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				qd.saveTranscript();
			}
		});

		JMenuItem quitItem = new JMenuItem(messages.getString("Quit"));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				qd.saveTranscript();
				System.exit(0);
			}
		});
		
		projectMenu.add(newItem);
		projectMenu.addSeparator(); 
		projectMenu.add(openItem);
		projectMenu.add(saveItem);
		projectMenu.addSeparator();
		projectMenu.add(quitItem);

	/*
	JMenu keyboardMenu = new JMenu(messages.getString("Keyboard"));

        for (int i = 0; i < keybdMgr.size(); i++) {
            final JskadKeyboard kbd = keybdMgr.elementAt(i);
            if (kbd.hasQuickRefFile()) {
                JMenuItem keybdItem = new JMenuItem(kbd.getIdentifyingString());
                keybdItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            qd.changeKeyboard(kbd);
                        }
                    });
                keyboardMenu.add(keybdItem);
            }
        }*/

		JMenu mediaPlayerMenu = new JMenu(messages.getString("MediaPlayer"));
		java.util.List moviePlayers = PlayerFactory.getAllAvailablePlayers();
		for (int i=0; i<moviePlayers.size(); i++) {
			final PanelPlayer mPlayer = (PanelPlayer)moviePlayers.get(i);
			JMenuItem mItem = new JMenuItem(mPlayer.getIdentifyingName());
			mItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					qd.setMediaPlayer(mPlayer);
				}
			});
			mediaPlayerMenu.add(mItem);
		}

		JMenu preferencesMenu = new JMenu(messages.getString("Preferences"));
	//	preferencesMenu.add(keyboardMenu);
		if (moviePlayers.size() > 0) {
			PanelPlayer defaultPlayer = (PanelPlayer)moviePlayers.get(0);
			qd.setMediaPlayer(defaultPlayer); //set qd media player to default
			if (moviePlayers.size() > 1)
				preferencesMenu.add(mediaPlayerMenu);
		}

		JMenu[] configMenus = qd.getConfiguredMenus();
		JMenuBar bar = new JMenuBar();
		bar.add(projectMenu);
		for (int i=0; i<configMenus.length; i++) bar.add(configMenus[i]);
		bar.add(preferencesMenu);
		return bar;
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
