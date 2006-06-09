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
        public static final String DEFAULT_MEDIA_PLAYER = "fieldling.mediaplayer.QT4JPlayer";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final int MAXIMUM_NUMBER_OF_RECENT_FILES = 4;
	public static final int CLOSE_APPLICATION = 0;
	public static final int CLOSE_WINDOW = 1;
	public static final int CLOSE_TRANSCRIPT = 2;
	public static boolean hasLoadedTranscript = false;	
	protected ResourceBundle messages = null;
	private JComboBox defaultLanguage, supportedFonts;
	private QD qd = null;
	private Container contentPane;

	/* Declaring the following private instance variables was the only way I figured to share
	 * information between the method itself and its inner classes. */ 
	private boolean optionsChanged, needsToRestart;
	private Color highlightColor, tagColor;
	private JPanel highlightColorPanel, tagColorPanel;
	
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
			if (!option.equals("THDLTranscription") && !option.equals("THDLReadonly"))
			{
				System.out.println("Syntax error: invalid option \"" + option + "\"!");
				printSyntax();
				return;
			}
			
 			if (args.length==1)
 			{
 				new QDShell(option, DEFAULT_MEDIA_PLAYER);
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
			new QDShell(transcriptFile[0], option, DEFAULT_MEDIA_PLAYER);
		} catch (NoClassDefFoundError err) {
		}
	}

        public QDShell()
        {
            this(PreferenceManager.getValue(PreferenceManager.CONFIGURATION_KEY, null), DEFAULT_MEDIA_PLAYER);
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
		PreferenceManager.setValue(PreferenceManager.MEDIA_DIRECTORY_KEY, PreferenceManager.media_directory);
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
		defaultLanguage = null;
		supportedFonts = null;
		contentPane = getContentPane();
		messages = I18n.getResourceBundle();
		setLocation(PreferenceManager.getInt(PreferenceManager.WINDOW_X_KEY, 0), PreferenceManager.getInt(PreferenceManager.WINDOW_Y_KEY, 0));
		setSize(new Dimension(PreferenceManager.getInt(PreferenceManager.WINDOW_WIDTH_KEY, getToolkit().getScreenSize().width), PreferenceManager.getInt(PreferenceManager.WINDOW_HEIGHT_KEY, getToolkit().getScreenSize().height)));
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
		configName = PreferenceManager.getValue(PreferenceManager.CONFIGURATION_KEY, null);
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
			qd.loadTranscript(transcriptFile);
			//makeRecentlyOpened(transcriptString, qd.player.getMediaURL().toString());
		}
		else
		{
			qd.loadTranscript(transcriptFile, mediaURL);
			//makeRecentlyOpened(transcriptString, mediaURL);
		}
		qd.setQDShell(QDShell.this);
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
	
        
        
        
        
        
        //GET ALL OF THE FOLLOWING INTO A SEPARATE FILE!!
        
        
        
        
        

	
	public void getDisplayPreferences()
	{
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames;
		int i;
		highlightColor = new Color(PreferenceManager.highlight_color_red, PreferenceManager.highlight_color_green, PreferenceManager.highlight_color_blue);
		tagColor = new Color(PreferenceManager.tag_color_red, PreferenceManager.tag_color_green, PreferenceManager.tag_color_blue);
		
		fontNames = genv.getAvailableFontFamilyNames();
		
		// main panels to be used
		JPanel interfacePanel, transcriptPanel, highlightPanel;
		
		JComboBox highlightPosition, showTimeCoding;
		JCheckBox multipleHighlightPolicy, scrollingHighlightPolicy, showFileNameAsTitle;  		
		
		// intermediate panels for rendering
		JPanel hPanel;
		
		interfacePanel = new JPanel(new GridLayout(4,2));
		interfacePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("Interface")));
		interfacePanel.add(new JLabel(messages.getString("Language")));
		
		if (supportedFonts==null)
		{
			/* Combo is not filled yet, as the supported fonts
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
			String[] languageLabels = I18n.getSupportedLanguages();
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
		hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hPanel.add(defaultLanguage);
		interfacePanel.add(hPanel);
		interfacePanel.add(new JLabel(messages.getString("Font")));
		hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hPanel.add(supportedFonts);
		interfacePanel.add(hPanel);
 		showTimeCoding = new JComboBox();
 		showTimeCoding.addItem(messages.getString("ConfigurationDefault"));
 		showTimeCoding.addItem(messages.getString("Never"));
 		showTimeCoding.addItem(messages.getString("Always"));
 		showTimeCoding.setSelectedIndex(PreferenceManager.show_time_coding + 1);
 		showTimeCoding.addItemListener(new ItemListener()
 				{
 			public void itemStateChanged(ItemEvent e) 
 			{
 				optionsChanged = true;
 			}				
 				});
 
 		interfacePanel.add(new JLabel(messages.getString("ShowTimeCodingBar")));
 		hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		hPanel.add(showTimeCoding);
 		interfacePanel.add(hPanel);		
		showFileNameAsTitle = new JCheckBox(messages.getString("ShowFileNameAsTitle"));
		showFileNameAsTitle.setSelected(PreferenceManager.show_file_name_as_title!=0);
		showFileNameAsTitle.addItemListener(new ItemListener()
				{
			public void itemStateChanged(ItemEvent e) 
			{
				optionsChanged = true;
			}				
				});
		interfacePanel.add(showFileNameAsTitle);
		
		transcriptPanel = new JPanel(new GridLayout(0, 2, 10, 10));
		
		transcriptPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("Transcript")));
		
		@TIBETAN@JComboBox tibetanFontSizes;
		@TIBETAN@transcriptPanel.add(new JLabel(messages.getString("TibetanFontSize")));
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
		@TIBETAN@hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		@TIBETAN@hPanel.add(tibetanFontSizes);
		@TIBETAN@transcriptPanel.add(hPanel);
		JComboBox romanFontFamilies;
		JComboBox romanFontSizes;
		String fontAndSizeLabel;
		@TIBETAN@fontAndSizeLabel = messages.getString("NonTibetanFontAndSize");
		@UNICODE@fontAndSizeLabel = messages.getString("FontAndSize");
		transcriptPanel.add(new JLabel(fontAndSizeLabel));
		
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
		
		hPanel = new JPanel(new BorderLayout(10,10));
		hPanel.add(romanFontFamilies, BorderLayout.CENTER);
		hPanel.add(romanFontSizes, BorderLayout.EAST);
		transcriptPanel.add(hPanel);
		
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
		transcriptPanel.add(new JLabel(messages.getString("TagColor")));		
		tagColorPanel = new JPanel();
		tagColorPanel.setBackground(tagColor);
		hPanel = new JPanel(new BorderLayout(10,10));
		hPanel.add(tagColorPanel, BorderLayout.CENTER);
		hPanel.add(tagColorButton, BorderLayout.EAST);
		transcriptPanel.add(hPanel);
		
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
		transcriptPanel.add(new JLabel(messages.getString("HighlightColor")));		
		highlightColorPanel = new JPanel();
		highlightColorPanel.setBackground(highlightColor);
		hPanel = new JPanel(new BorderLayout(10,10));		
		hPanel.add(highlightColorPanel, BorderLayout.CENTER);
		hPanel.add(highlightColorButton, BorderLayout.EAST);
		transcriptPanel.add(hPanel);
		
		JTextField highlightField;
		highlightPanel = new JPanel(new GridLayout(2,2));                   
		highlightPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("HighlightRelatedPreferences")));      
		
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
		highlightPanel.add(new JLabel(messages.getString("HighlightPosition")));
		hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hPanel.add(highlightPosition);
		highlightPanel.add(hPanel);
		
		multipleHighlightPolicy = new JCheckBox(messages.getString("AllowMultipleHighlighting"));
		multipleHighlightPolicy.addItemListener(new ItemListener()
				{
			public void itemStateChanged(ItemEvent e) 
			{
				optionsChanged = true;
			}				
				});
		multipleHighlightPolicy.setSelected(PreferenceManager.multiple_highlight_policy==0);
		
		scrollingHighlightPolicy = new JCheckBox(messages.getString("AllowScrollHighlighting"));
		scrollingHighlightPolicy.addItemListener(new ItemListener()
				{
			public void itemStateChanged(ItemEvent e) 
			{
				optionsChanged = true;
			}				
				});
		scrollingHighlightPolicy.setSelected(PreferenceManager.scrolling_highlight_policy==0);
		
		/*hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		 hPanel.add(multipleHighlightPolicy);*/
		highlightPanel.add(multipleHighlightPolicy);
		
		/*hPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		 hPanel.add(scrollingHighlightPolicy);*/
		highlightPanel.add(scrollingHighlightPolicy);
		
		JPanel preferencesPanel = new JPanel (new BorderLayout());
		preferencesPanel.add(interfacePanel, BorderLayout.NORTH);
		preferencesPanel.add(transcriptPanel, BorderLayout.CENTER);
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
		
		PreferenceManager.setValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.font_face);
		PreferenceManager.setInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.font_size);
		@TIBETAN@PreferenceManager.setInt(PreferenceManager.TIBETAN_FONT_SIZE_KEY, PreferenceManager.tibetan_font_size);
 		Editor editor;
 		if (qd!=null) editor = qd.getEditor();
 		else editor = null;		
		fieldling.quilldriver.xml.Renderer.setTagColor(tagColor);
		PreferenceManager.tag_color_red = tagColor.getRed();
		PreferenceManager.tag_color_green = tagColor.getGreen();
		PreferenceManager.tag_color_blue = tagColor.getBlue();
		PreferenceManager.setInt(PreferenceManager.TAG_RED_KEY, PreferenceManager.tag_color_red);
		PreferenceManager.setInt(PreferenceManager.TAG_GREEN_KEY, PreferenceManager.tag_color_green);
		PreferenceManager.setInt(PreferenceManager.TAG_BLUE_KEY, PreferenceManager.tag_color_blue);		
 		PreferenceManager.highlight_color_red = highlightColor.getRed();
 		PreferenceManager.highlight_color_green = highlightColor.getGreen();
 		PreferenceManager.highlight_color_blue = highlightColor.getBlue();
 		PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_RED_KEY, PreferenceManager.highlight_color_red);
 		PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_GREEN_KEY, PreferenceManager.highlight_color_green);
 		PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_BLUE_KEY, PreferenceManager.highlight_color_blue);			
		String highlightPosVal = (String)highlightPosition.getSelectedItem();
		
		if (getQD().hasContent())
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
			int multipleHighlightPolicyVal = multipleHighlightPolicy.getSelectedObjects()!=null?0:1;
			
			PreferenceManager.multiple_highlight_policy = multipleHighlightPolicyVal;
			PreferenceManager.highlight_position = highlightPosVal;
			
			PreferenceManager.setValue(PreferenceManager.HIGHLIGHT_POSITION_KEY, highlightPosVal);
			PreferenceManager.setInt(PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_KEY, PreferenceManager.multiple_highlight_policy);
			
			qd.player.setMultipleAnnotationPolicy(multipleHighlightPolicyVal==0);
			
			int scrollingHighlightPolicyVal = scrollingHighlightPolicy.getSelectedObjects()!=null?0:1;
			
			PreferenceManager.scrolling_highlight_policy = scrollingHighlightPolicyVal;
			PreferenceManager.setInt(PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_KEY, PreferenceManager.scrolling_highlight_policy);
			
			if (scrollingHighlightPolicyVal==0) {
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
			
			PreferenceManager.show_file_name_as_title = showFileNameAsTitle.getSelectedObjects()!=null ? 1:0;
			PreferenceManager.setInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, PreferenceManager.show_file_name_as_title);
 			i = showTimeCoding.getSelectedIndex();
 			if (i==-1) PreferenceManager.show_time_coding =  - 1;
 			else PreferenceManager.show_time_coding = i - 1;
 			PreferenceManager.setInt(PreferenceManager.SHOW_TIME_CODING_KEY, PreferenceManager.show_time_coding);
			/*Iterator iterator = qdList.iterator();
			QD currentQD;
			while (iterator.hasNext())
			{
				currentQD = (QD) iterator.next();
				currentQD.updateTitles();
                                currentQD.updateTimeCodeBarVisibility();
			}*/
			
			if (needsToRestart)
			{
				JOptionPane.showMessageDialog(this, messages.getString("ChangesToInterface"));
				PreferenceManager.default_interface_font = (String) supportedFonts.getSelectedItem();
				PreferenceManager.default_language = defaultLanguage.getSelectedIndex();
				PreferenceManager.setValue(PreferenceManager.DEFAULT_INTERFACE_FONT_KEY, PreferenceManager.default_interface_font);
				PreferenceManager.setInt(PreferenceManager.DEFAULT_LANGUAGE_KEY, PreferenceManager.default_language);
			}
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

