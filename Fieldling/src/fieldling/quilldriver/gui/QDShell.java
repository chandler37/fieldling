/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2006-, Edward Garrett
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
import java.awt.event.*;
import fieldling.quilldriver.*;
import fieldling.quilldriver.PreferenceManager;
import fieldling.quilldriver.config.*;
import fieldling.quilldriver.xml.*;
import fieldling.quilldriver.task.*;
import fieldling.mediaplayer.*;
import fieldling.util.*;
import javax.xml.xpath.*;

public class QDShell extends JFrame 
{
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;
	public static final int CLOSE_APPLICATION = 0;
	public static final int CLOSE_WINDOW = 1;
	public static final int CLOSE_TRANSCRIPT = 2;
	public static boolean hasLoadedTranscript = false;	
	protected ResourceBundle messages = null;
	private QD qd = null;
	private Container contentPane;
	
	/* Declaring the following private instance variables was the only way I figured to share
	 * information between the method itself and its inner classes. */ 
	private boolean optionsChanged;
	
	private static void printSyntax()
	{
		System.out.println("Syntax: QDShell [-THDLTranscription | -THDLReadonly  transcript-file-list]");
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
		PrintStream ps=null;
		try 
		{
			/* We use saxon-b 8.x as our Transformer for two reasons:
			 (1) It supports XSLT 2.0, which is handy
			 (2) Unlike xalan, it has the nice feature that if you transform a node, 
			 you can get access to the entire document connected to that node. As
			 Michael Kay writes, "Generally I"ve followed what Microsoft did (they were 
			 the ones who introduced the idea of starting a transformation at a node 
			 other than the root), which is that the start node is not detached from 
			 the tree it belonged to - you can still do ancestor::x to find its 
			 original ancestors." For discussion, see the following saxon-help post:
			 http://sourceforge.net/mailarchive/message.php?msg_id=2997255*/
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			
			try 
			{
				ps = new PrintStream(new FileOutputStream(System.getProperty("user.home") + "/qd.log"));
				System.setOut(ps);
				System.setErr(ps);
			} catch (FileNotFoundException fnfe) {
			}
			
			if (args.length==0) 
			{
				new QDShell();
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
			if (!option.equals("THDLTranscription") && !option.equals("THDLReadonly") && !option.equals("TranscribeQuechua"))
			{
				System.out.println("Syntax error: invalid option \"" + option + "\"!");
				printSyntax();
				return;
			}
			
			if (args.length==1)
			{
				new QDShell(option, PreferenceManager.MEDIA_PLAYER_DEFAULT);
				return;
			}
			
			File[] transcriptFile = new File[args.length-1];
			for (int i=1; i<args.length; i++) {
				transcriptFile[i-1] = new File (args[i]);
				if (!transcriptFile[i-1].exists())
				{
					System.out.println("Error reading file!");
					return;
				}
			}
			// if arguments are passed through the command-line default to Quick-time for Java
			new QDShell(transcriptFile[0], option, PreferenceManager.MEDIA_PLAYER_DEFAULT);
		} catch (NoClassDefFoundError err) {
		}
	}
	
	public QDShell()
	{
		this(PreferenceManager.getValue(PreferenceManager.CONFIGURATION_KEY, PreferenceManager.CONFIGURATION_DEFAULT), 
				PreferenceManager.MEDIA_PLAYER_DEFAULT);
	}
	
	public QDShell(String configName, String mediaPlayer)
	{
		loadGenericInitialState(new QD(ConfigurationFactory.getConfiguration(configName), getMediaPlayer(mediaPlayer)));
		setVisible(true);
	}
	
	public QDShell(File transcriptFile, String configName, String mediaPlayer)
	{
		loadGenericInitialState(new QD(ConfigurationFactory.getConfiguration(configName), getMediaPlayer(mediaPlayer)));
		loadSpecificInitialState(transcriptFile);
		setVisible(true);
	}
	
	public QDShell(QD qd) {
		loadGenericInitialState(qd);
		setVisible(true);
	}
	
	public PanelPlayer getMediaPlayer(String mediaPlayer) {
		try {
			return PlayerFactory.getPlayerForClass(mediaPlayer);
		} catch (PanelPlayerException ppe) {
			return null;
		}
	}
	
	public void putPreferences() {
		PreferenceManager.setInt(PreferenceManager.WINDOW_X_KEY, getX());
		PreferenceManager.setInt(PreferenceManager.WINDOW_Y_KEY, getY());
		PreferenceManager.setInt(PreferenceManager.WINDOW_WIDTH_KEY, getWidth());
		PreferenceManager.setInt(PreferenceManager.WINDOW_HEIGHT_KEY, getHeight());
	}
	
	public QD getQD() {
		return qd;
	}
	
	public void activateQD(QD qd, boolean hasLoadedTranscript) {
		this.qd = qd;
		qd.register();
		qd.setQDShell(this);
		setTitle(qd.getTitle());
		if (hasLoadedTranscript) {
			this.hasLoadedTranscript = true;
			//qd.setSize(0,0);
			//qd.setSize(contentPane.getSize());
			contentPane.add(qd);
			WindowPositioningTask.repositionWithActiveWindowPositioner(qd);
			qd.requestFocus();
			/*contentPane.validate();
			 contentPane.repaint();*/
		}
		invalidate();
		validate();
		repaint();
	}
	
	public void deActivateQD(QD qd) {
		contentPane.remove(qd);
		qd.setQDShell(null);
		contentPane.validate();
		contentPane.repaint();
		QD.lastQD = qd;
		//this.qd = null;
	}
	
	/** Constructor for generic stuff loaded regardless of how to get initial state (wizard, command-line,
	 * or defaults in pref manager). Not meant to be called directly!!! */
	private void loadGenericInitialState(QD qd)
	{
		contentPane = getContentPane();
		messages = I18n.getResourceBundle();
		setLocation(PreferenceManager.getInt(PreferenceManager.WINDOW_X_KEY, PreferenceManager.WINDOW_X_DEFAULT), PreferenceManager.getInt(PreferenceManager.WINDOW_Y_KEY, PreferenceManager.WINDOW_Y_DEFAULT));
		setSize(new Dimension(PreferenceManager.getInt(PreferenceManager.WINDOW_WIDTH_KEY, PreferenceManager.WINDOW_WIDTH_DEFAULT), PreferenceManager.getInt(PreferenceManager.WINDOW_HEIGHT_KEY, PreferenceManager.WINDOW_HEIGHT_DEFAULT)));
		setJMenuBar(QD.configuration.getJMenuBar());
		activateQD(qd, false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e1) {
				try {
					BasicTask exitTask = BasicTask.getTaskForClass("fieldling.quilldriver.task.ExitQD");
					exitTask.execute(QDShell.this.getQD(), null);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
	}
	
	private void loadSpecificInitialStateFromDefaults() throws Exception
	{
		File transcriptFile;
		String transcriptFileName, s, mediaURL, configName, mediaPlayer;
		int pos;
		s = PreferenceManager.getValue(PreferenceManager.RECENT_FILES_KEY, null);
		pos = s.indexOf(",");
		if (pos==-1) transcriptFileName = s.trim();
		else transcriptFileName = s.substring(0,pos).trim();
		transcriptFile = new File(transcriptFileName);
		if (!transcriptFile.exists()) throw new Exception("Transcription file not found!");
		s = PreferenceManager.getValue(PreferenceManager.RECENT_VIDEOS_KEY, null);
		pos = s.indexOf(",");
		if (pos==-1) mediaURL = s.trim();
		else mediaURL = s.substring(0,pos).trim();
		configName = PreferenceManager.getValue(PreferenceManager.CONFIGURATION_KEY, PreferenceManager.CONFIGURATION_DEFAULT);
		loadSpecificInitialState(transcriptFile, mediaURL);
	}
	
	private void loadSpecificInitialState(File transcriptFile)
	{
		loadSpecificInitialState(transcriptFile, null);
	}
	
	private void loadSpecificInitialState(File transcriptFile, String mediaURL)
	{
		String transcriptString = transcriptFile.getAbsolutePath();
		PreferenceManager.setValue(PreferenceManager.WORKING_DIRECTORY_KEY,transcriptString.substring(0,transcriptString.lastIndexOf(FILE_SEPARATOR) + 1));
		if (mediaURL==null)
		{
			if (qd.loadTranscript(transcriptFile)) activateQD(qd, true);
			//makeRecentlyOpened(transcriptString, qd.player.getMediaURL().toString());
		}
		else
		{
			if (qd.loadTranscript(transcriptFile, mediaURL)) activateQD(qd, true);
			//makeRecentlyOpened(transcriptString, mediaURL);
		}
		// qd.setQDShell(QDShell.this);
	}
}
/*
 
 java.util.List recentFileList = new ArrayList();
 java.util.List recentVideoList = new ArrayList();
 String r = PreferenceManager.getValue(PreferenceManager.RECENT_FILES_KEY, null);
 String rv = PreferenceManager.getValue(PreferenceManager.RECENT_VIDEOS_KEY, null);
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
 
 /*protected void makeRecentlyOpened(String s, String t) {
  String r = PreferenceManager.getValue(PreferenceManager.RECENT_FILES_KEY, null);
  String q = PreferenceManager.getValue(PreferenceManager.RECENT_VIDEOS_KEY, null);
  if (r == null) {
  PreferenceManager.setValue(PreferenceManager.RECENT_FILES_KEY, s);
  PreferenceManager.setValue(PreferenceManager.RECENT_VIDEOS_KEY, s);
  }
  else {
  LinkedList recentTs = new LinkedList();
  LinkedList recentVs = new LinkedList();
  recentTs.add(s);
  recentVs.add(t);
  String[] recentTranscripts = r.split(",");
  String[] recentVideos = q.split(",");
  for (int j=0; j<recentTranscripts.length; j++) {
  if (!recentTranscripts[j].equals(s)) {
  recentTs.add(recentTranscripts[j]);
  recentVs.add(recentVideos[j]);
  }
  }
  int k;
  if (recentTs.size() > MAXIMUM_NUMBER_OF_RECENT_FILES)
  k = MAXIMUM_NUMBER_OF_RECENT_FILES;
  else
  k = recentTs.size();
  StringBuffer sb = new StringBuffer();
  StringBuffer sb2 = new StringBuffer();
  for (int i = 0; i < k; i++) {
  sb.append((String) recentTs.removeFirst());
  sb.append(',');
  sb2.append((String)recentVs.removeFirst());
  sb2.append(',');
  }
  PreferenceManager.setValue(PreferenceManager.RECENT_FILES_KEY, sb.toString());
  PreferenceManager.setValue(PreferenceManager.RECENT_VIDEOS_KEY, sb2.toString());
  */

