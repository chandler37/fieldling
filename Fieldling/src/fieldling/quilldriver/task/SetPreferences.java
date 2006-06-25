package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import fieldling.quilldriver.PreferenceSetter;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SetPreferences extends BasicTask {
    
    public void execute(QD qd, String parameters) {
        PreferenceSetter[] prefSetter = new PreferenceSetter[4];
        prefSetter[0] = new SavingPreferenceSetter(qd);
        prefSetter[1] = new TimeCodingPreferenceSetter(qd);
        prefSetter[2] = new HighlightPreferenceSetter(qd);
        prefSetter[3] = new InterfacePreferenceSetter(qd);
        JTabbedPane tabPane = new JTabbedPane();
        for (int i=0; i<prefSetter.length; i++) {
            tabPane.add(prefSetter[i].getDisplayName(), prefSetter[i].getComponent());
        }
        
        JOptionPane pane = new JOptionPane(tabPane);
        pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(qd, QD.messages.getString("Preferences"));
        dialog.show();
        Object selectedValue = pane.getValue();
        if (selectedValue == null)
            return;
        if (selectedValue instanceof Integer) {
            if (((Integer)selectedValue).intValue() == JOptionPane.OK_OPTION) {
                for (int i=0; i<prefSetter.length; i++) {
                    prefSetter[i].setPreferences();
                }
            }
        }
    }
        /*
     //If there is not an array of option buttons:
     if(options == null) {
       if(selectedValue instanceof Integer)
          return ((Integer)selectedValue).intValue();
       return CLOSED_OPTION;
     }
     //If there is an array of option buttons:
     for(int counter = 0, maxCounter = options.length;
        counter < maxCounter; counter++) {
        if(options[counter].equals(selectedValue))
        return counter;
     }
     return CLOSED_OPTION;
	//optionsChanged = false;
	//needsToRestart = false;
		
	// This returns only when the user has closed the dialog
	dialog.setVisible(true);
	Object selectedValue = pane.getValue();
		
	if (!optionsChanged || selectedValue==null || !(selectedValue instanceof Integer))
		return;
		
	Integer selectedInteger = (Integer) selectedValue;
	if (selectedInteger.intValue() != JOptionPane.OK_OPTION)
		return;

		if (needsToRestart)
		{
			JOptionPane.showMessageDialog(this, messages.getString("ChangesToInterface"));
			PreferenceManager.default_interface_font = (String) supportedFonts.getSelectedItem();
			PreferenceManager.default_language = defaultLanguage.getSelectedIndex();
			PreferenceManager.setValue(PreferenceManager.DEFAULT_INTERFACE_FONT_KEY, PreferenceManager.default_interface_font);
			PreferenceManager.setInt(PreferenceManager.DEFAULT_LANGUAGE_KEY, PreferenceManager.default_language);
		}*/
    
    class SavingPreferenceSetter implements PreferenceSetter {
        QD qd;
        JPanel preferencesPanel = new JPanel();
        JSpinner minutesSpinner = new JSpinner();
        int originalMinutesValue = PreferenceManager.getInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, PreferenceManager.AUTO_SAVE_MINUTES_DEFAULT);
        JTextField directoryField = new JTextField(PreferenceManager.getValue(PreferenceManager.BACKUP_DIRECTORY_KEY, PreferenceManager.BACKUP_DIRECTORY_DEFAULT));
        JComboBox normalizerBox = new JComboBox(new String[] {"No", "Yes"});
        
        public SavingPreferenceSetter(QD qd) {
            this.qd = qd;
            
            //auto-save open transcripts every how many minutes?
            JPanel autoSaveMinutesPanel = new JPanel(new BorderLayout());
            autoSaveMinutesPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("AutoSaveMinutesQuestion")));
            minutesSpinner.setValue(new Integer(originalMinutesValue));
            autoSaveMinutesPanel.add(minutesSpinner, BorderLayout.CENTER);
            
            //back transcripts up to what directory?
            JPanel backupDirectoryPanel = new JPanel(new BorderLayout());
            backupDirectoryPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("BackupDirectoryQuestion")));
            JButton browseButton = new JButton(QD.messages.getString("Browse"));
            backupDirectoryPanel.add(directoryField, BorderLayout.CENTER);
            backupDirectoryPanel.add(browseButton, BorderLayout.EAST);
            browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    File f = OpeningTask.selectFileOrDirectory(JFileChooser.DIRECTORIES_ONLY, QD.messages.getString("SelectDirectoryMessage"), SavingPreferenceSetter.this.qd);
                    if (f.isDirectory())
                        directoryField.setText(f.getAbsolutePath());
                }
            });
            
            //normalize namespaces?
            JPanel normalizeNamespacePanel = new JPanel(new BorderLayout());
            normalizeNamespacePanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("NamespaceNormalizerQuestion")));
            int useNormalizer = PreferenceManager.getInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, PreferenceManager.NORMALIZE_NAMESPACES_DEFAULT);
            if (useNormalizer == 1)
                normalizerBox.setSelectedIndex(1);
            else
                normalizerBox.setSelectedIndex(0);
            normalizeNamespacePanel.add(normalizerBox, BorderLayout.CENTER);
            
            //arrange preferences panel
            preferencesPanel.setLayout(new GridLayout(3, 1));
            preferencesPanel.add(autoSaveMinutesPanel);
            preferencesPanel.add(backupDirectoryPanel);
            preferencesPanel.add(normalizeNamespacePanel);
        }
        
        public String getDisplayName() {
            return QD.messages.getString("SavingPreferences");
        }
        
        public JComponent getComponent() {
            return preferencesPanel;
        }
        
        public void setPreferences() {
            //change preferences accordingly
            int n = ((Integer)minutesSpinner.getValue()).intValue();
            if (n != originalMinutesValue) {
                QD.autoSaver.setDelay(0); //stop existing AutoSave thread
                if (n > 0) {
                    PreferenceManager.setInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, n);
                    QD.autoSaver = new fieldling.quilldriver.AutoSave(qd.transcriptToggler, n * 60000);
                    QD.autoSaver.start();
                } else {
                    PreferenceManager.setInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, 0);
                }
            }
            
            PreferenceManager.setValue(PreferenceManager.BACKUP_DIRECTORY_KEY, directoryField.getText());
            
            if (normalizerBox.getSelectedIndex() == 0)
                PreferenceManager.setInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, -1);
            else
                PreferenceManager.setInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, 1);
        }
    }

    class TimeCodingPreferenceSetter implements PreferenceSetter {
        QD qd;
        JPanel preferencesPanel = new JPanel();
        JTextField slowAdjustField = new JTextField(String.valueOf(PreferenceManager.getInt(PreferenceManager.SLOW_ADJUST_KEY, PreferenceManager.SLOW_ADJUST_DEFAULT)));
        JTextField rapidAdjustField = new JTextField(String.valueOf(PreferenceManager.getInt(PreferenceManager.RAPID_ADJUST_KEY, PreferenceManager.RAPID_ADJUST_DEFAULT)));
        JTextField playMinusField = new JTextField(String.valueOf(PreferenceManager.getInt(PreferenceManager.PLAY_MINUS_KEY, PreferenceManager.PLAY_MINUS_DEFAULT)));
        int old_timeCodeBarPolicy = PreferenceManager.getInt(PreferenceManager.SHOW_TIME_CODING_KEY, PreferenceManager.SHOW_TIME_CODING_DEFAULT);
        JComboBox showTimeCoding = new JComboBox(new String[] {QD.messages.getString("ConfigurationDefault"), QD.messages.getString("Never"), QD.messages.getString("Always")});

        public TimeCodingPreferenceSetter(QD qd) {
            this.qd = qd;
            
            //slow adjust
            JPanel slowAdjustPanel = new JPanel(new BorderLayout());
	    slowAdjustPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("SlowIncreaseDecreaseValue")));
            slowAdjustField.setPreferredSize(new Dimension(240, 30));
            slowAdjustPanel.add(slowAdjustField);
            
            //rapid adjust
            JPanel rapidAdjustPanel = new JPanel(new BorderLayout());
            rapidAdjustPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("RapidIncreaseDecreaseValue")));
            rapidAdjustField.setPreferredSize(new Dimension(240, 30));
            rapidAdjustPanel.add(rapidAdjustField);
            
            //play minus parameter
            JPanel playMinusPanel = new JPanel(new BorderLayout());
            playMinusPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("PlayMinusValue")));
            playMinusField.setPreferredSize(new Dimension(240, 30));
            playMinusPanel.add(playMinusField);
            
            //show time coding
            JPanel showTimeCodingPanel = new JPanel(new BorderLayout());
            showTimeCodingPanel.setBorder(BorderFactory.createTitledBorder(QD.messages.getString("ShowTimeCodingBar")));
            showTimeCodingPanel.add(showTimeCoding, BorderLayout.CENTER);
            showTimeCoding.setSelectedIndex(old_timeCodeBarPolicy + 1);
            
            //layout
            preferencesPanel.setLayout(new GridLayout(4, 1));
            preferencesPanel.add(slowAdjustPanel);
            preferencesPanel.add(rapidAdjustPanel);
            preferencesPanel.add(playMinusPanel);
            preferencesPanel.add(showTimeCodingPanel);
        }
        
        public String getDisplayName() {
            return QD.messages.getString("TimeCodingPreferences");
        }
        
        public JComponent getComponent() {
            return preferencesPanel;
        }
        
        public void setPreferences() {
                //set slow adjust
                int new_slow_adjust = -1;
		int old_slow_adjust = PreferenceManager.getInt(PreferenceManager.SLOW_ADJUST_KEY, PreferenceManager.SLOW_ADJUST_DEFAULT);
		try {
			new_slow_adjust = Integer.parseInt(slowAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
                if (new_slow_adjust > -1 && new_slow_adjust != old_slow_adjust)
                    PreferenceManager.setInt(PreferenceManager.SLOW_ADJUST_KEY, new_slow_adjust);
                
                //set rapid adjust
                int new_rapid_adjust = -1;
		int old_rapid_adjust = PreferenceManager.getInt(PreferenceManager.RAPID_ADJUST_KEY, PreferenceManager.RAPID_ADJUST_DEFAULT);
		try {
			new_rapid_adjust = Integer.parseInt(rapidAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
                if (new_rapid_adjust > -1 && new_rapid_adjust != old_rapid_adjust)
                    PreferenceManager.setInt(PreferenceManager.RAPID_ADJUST_KEY, new_rapid_adjust);
                
                //set play minus
                int new_play_minus = -1;  
		int old_play_minus = PreferenceManager.getInt(PreferenceManager.PLAY_MINUS_KEY, PreferenceManager.PLAY_MINUS_DEFAULT);
		try {
			new_play_minus= Integer.parseInt(playMinusField.getText());
		} catch (NumberFormatException ne) {
		}
                if (new_play_minus > -1 && new_play_minus != old_play_minus)
                    PreferenceManager.setInt(PreferenceManager.PLAY_MINUS_KEY, new_play_minus);
                
                int new_timeCodeBarPolicy = showTimeCoding.getSelectedIndex();
                if (new_timeCodeBarPolicy != -1)
                    new_timeCodeBarPolicy--;
                if (new_timeCodeBarPolicy != old_timeCodeBarPolicy) {
                    PreferenceManager.setInt(PreferenceManager.SHOW_TIME_CODING_KEY, new_timeCodeBarPolicy);
                    if (qd.hasContent()) {
                        for (int i=0; i<qd.transcriptToggler.getNumberOfTranscripts(); i++) {
                            QD oneQDAtATime = qd.transcriptToggler.getQDForIndex(i);
                            oneQDAtATime.updateTimeCodeBarVisibility();
                        }
                    }
                }
        }
    }

    class HighlightPreferenceSetter implements PreferenceSetter {
        QD qd;
        JPanel preferencesPanel = new JPanel();
        Color old_highlightColor, new_highlightColor;
        String old_highlightPositionVal;
        JComboBox highlightPosition = new JComboBox(new String[] {QD.messages.getString("Middle"), QD.messages.getString("Bottom")});
        int old_multipleHighlightPolicy;
        JCheckBox multipleHighlightPolicy = new JCheckBox(QD.messages.getString("AllowMultipleHighlighting"));
        int old_scrollingHighlightPolicy;
        JCheckBox scrollingHighlightPolicy = new JCheckBox(QD.messages.getString("AllowScrollHighlighting"));

        public HighlightPreferenceSetter(QD qd) {
            this.qd = qd;
            
            //highlight color
            old_highlightColor = new Color(
                PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_RED_KEY, PreferenceManager.HIGHLIGHT_RED_DEFAULT),
                PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_GREEN_KEY, PreferenceManager.HIGHLIGHT_GREEN_DEFAULT),
                PreferenceManager.getInt(PreferenceManager.HIGHLIGHT_BLUE_KEY, PreferenceManager.HIGHLIGHT_BLUE_DEFAULT));
            new_highlightColor = new Color(old_highlightColor.getRed(), old_highlightColor.getGreen(), old_highlightColor.getBlue());
            final JPanel colorDisplayPanel = new JPanel();
            colorDisplayPanel.setBackground(new_highlightColor);
            final JButton highlightColorButton =new JButton(QD.messages.getString("Change"));
            highlightColorButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev){
			Color newColor = JColorChooser.showDialog(highlightColorButton, "Choose a color", new_highlightColor);
			if(newColor!=null)
			{
				new_highlightColor = newColor;
				colorDisplayPanel.setBackground(new_highlightColor);
				colorDisplayPanel.repaint();
			}
		}
            });	
            JPanel highlightColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            highlightColorPanel.add(new JLabel(QD.messages.getString("HighlightColor")));
            highlightColorPanel.add(colorDisplayPanel);
            highlightColorPanel.add(highlightColorButton);
            colorDisplayPanel.setPreferredSize(highlightColorButton.getPreferredSize());
            
            //highlight position
            JPanel highlightPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            old_highlightPositionVal = PreferenceManager.getValue(PreferenceManager.HIGHLIGHT_POSITION_KEY, PreferenceManager.HIGHLIGHT_POSITION_DEFAULT);
            highlightPosition.setSelectedItem(old_highlightPositionVal);
            highlightPosition.setEditable(true);
            highlightPositionPanel.add(new JLabel(QD.messages.getString("HighlightPosition")));
            highlightPositionPanel.add(highlightPosition);
            
            //multiple highlight policy
            old_multipleHighlightPolicy = PreferenceManager.getInt(PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_KEY, PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_DEFAULT);
            multipleHighlightPolicy.setSelected(old_multipleHighlightPolicy == 0);
            
            //scroll highlighting
            old_scrollingHighlightPolicy = PreferenceManager.getInt(PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_KEY, PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_DEFAULT);
            scrollingHighlightPolicy.setSelected(old_scrollingHighlightPolicy == 0);
            
            //layout
            JPanel policyPanel = new JPanel(new GridLayout(2,1));
            policyPanel.add(multipleHighlightPolicy);
            policyPanel.add(scrollingHighlightPolicy);
            preferencesPanel.setLayout(new GridLayout(3,1));
            preferencesPanel.add(highlightColorPanel);
            preferencesPanel.add(highlightPositionPanel);
            preferencesPanel.add(policyPanel);
        }
        
        public String getDisplayName() {
            return QD.messages.getString("HighlightRelatedPreferences");
        }
        
        public JComponent getComponent() {
            return preferencesPanel;
        }
        
        public void setPreferences() {            
            //set highlight color
            if (!new_highlightColor.equals(old_highlightColor)) {
                PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_RED_KEY, new_highlightColor.getRed());
                PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_GREEN_KEY, new_highlightColor.getGreen());
                PreferenceManager.setInt(PreferenceManager.HIGHLIGHT_BLUE_KEY, new_highlightColor.getBlue());
                if (qd.hasContent()) {
                    qd.hp.setHighlightColor(new_highlightColor);
                }
            }
            
            //set highlight position
            String new_highlightPositionVal = (String)highlightPosition.getSelectedItem();
            if (!old_highlightPositionVal.equals(new_highlightPositionVal)) {
                PreferenceManager.setValue(PreferenceManager.HIGHLIGHT_POSITION_KEY, new_highlightPositionVal);
                if (qd.hasContent()) {
                    qd.hp.setHighlightPosition(new_highlightPositionVal);
                }
            }
            
            //set multiple highlight policy
            int new_multipleHighlightPolicy = multipleHighlightPolicy.getSelectedObjects()!=null?0:1;
            if (old_multipleHighlightPolicy != new_multipleHighlightPolicy) {
                PreferenceManager.setInt(PreferenceManager.MULTIPLE_HIGHLIGHT_POLICY_KEY, new_multipleHighlightPolicy);
                if (qd.hasContent()) {
                    qd.getMediaPlayer().setMultipleAnnotationPolicy(new_multipleHighlightPolicy==0);
                }
            }

            //set scrolling highlight policy
            int new_scrollingHighlightPolicy = scrollingHighlightPolicy.getSelectedObjects()!=null?0:1;
            if (old_scrollingHighlightPolicy != new_scrollingHighlightPolicy) {
                PreferenceManager.setInt(PreferenceManager.SCROLLING_HIGHLIGHT_POLICY_KEY, new_scrollingHighlightPolicy);
                if (qd.hasContent())
                    qd.getMediaPlayer().setAutoScrolling(new_scrollingHighlightPolicy==0);
            }
        }
    }
    
    class InterfacePreferenceSetter implements PreferenceSetter {
        QD qd;
        JPanel preferencesPanel = new JPanel();
        Color old_tagColor, new_tagColor;
        @TIBETAN@int old_tibetanFontSize;
        @TIBETAN@JComboBox tibetanFontSizes;
        String old_fontFace;
        JComboBox romanFontFamilies;
        int old_fontSize;
        JComboBox romanFontSizes;

        public InterfacePreferenceSetter(QD qd) {
            this.qd = qd;
            
            //tag color
            old_tagColor = new Color(PreferenceManager.getInt(PreferenceManager.TAG_RED_KEY, PreferenceManager.TAG_RED_DEFAULT),
                PreferenceManager.getInt(PreferenceManager.TAG_GREEN_KEY, PreferenceManager.TAG_GREEN_DEFAULT),
                PreferenceManager.getInt(PreferenceManager.TAG_BLUE_KEY, PreferenceManager.TAG_BLUE_DEFAULT));
            new_tagColor = new Color(old_tagColor.getRed(), old_tagColor.getGreen(), old_tagColor.getBlue());
            final JPanel tagColorDisplayPanel = new JPanel();
            tagColorDisplayPanel.setBackground(new_tagColor);
            final JButton tagColorButton =new JButton(QD.messages.getString("Change"));
            tagColorButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ev){
			Color newColor = JColorChooser.showDialog(tagColorButton, "Choose a color", new_tagColor);                 
			if(newColor!=null)
			{
                                new_tagColor = newColor;
				tagColorDisplayPanel.setBackground(newColor);
				tagColorDisplayPanel.repaint();
			}
		}
            });	
            JPanel tagColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tagColorDisplayPanel.add(new JLabel(QD.messages.getString("TagColor")));
            tagColorDisplayPanel.setPreferredSize(tagColorButton.getPreferredSize());
            tagColorPanel.add(tagColorDisplayPanel);
            tagColorPanel.add(tagColorButton);
            
            //tibetan font size
            @TIBETAN@old_tibetanFontSize = PreferenceManager.getInt(PreferenceManager.TIBETAN_FONT_SIZE_KEY, PreferenceManager.TIBETAN_FONT_SIZE_DEFAULT);
            @TIBETAN@tibetanFontSizes = new JComboBox(new String[] {"22","24","26","28","30","32","34","36","48","72"});
            @TIBETAN@tibetanFontSizes.setMaximumSize(tibetanFontSizes.getPreferredSize());
            @TIBETAN@tibetanFontSizes.setSelectedItem(String.valueOf(old_tibetanFontSize));
            @TIBETAN@tibetanFontSizes.setEditable(true);
            @TIBETAN@JPanel tibetanFontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            @TIBETAN@tibetanFontSizePanel.add(new JLabel(QD.messages.getString("TibetanFontSize")));
            @TIBETAN@tibetanFontSizePanel.add(tibetanFontSizes);

            //roman font and size            
            GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = genv.getAvailableFontFamilyNames();
            String fontAndSizeLabel;
            @TIBETAN@fontAndSizeLabel = QD.messages.getString("NonTibetanFontAndSize");
            @UNICODE@fontAndSizeLabel = QD.messages.getString("FontAndSize");
            romanFontFamilies = new JComboBox(fontNames);
            romanFontFamilies.setMaximumSize(romanFontFamilies.getPreferredSize());
            old_fontFace = PreferenceManager.getValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.FONT_FACE_DEFAULT);
            romanFontFamilies.setSelectedItem(old_fontFace);
            romanFontFamilies.setEditable(true);
            romanFontSizes = new JComboBox(new String[] {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"});
            romanFontSizes.setMaximumSize(romanFontSizes.getPreferredSize());
            old_fontSize = PreferenceManager.getInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.FONT_SIZE_DEFAULT);
            romanFontSizes.setSelectedItem(String.valueOf(old_fontSize));
            romanFontSizes.setEditable(true);
            JPanel fontAndSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            fontAndSizePanel.add(new JLabel(fontAndSizeLabel));
            fontAndSizePanel.add(romanFontFamilies);
            fontAndSizePanel.add(romanFontSizes);
            
            @UNICODE@preferencesPanel = new JPanel(new GridLayout(2,1));
            @UNICODE@preferencesPanel.add(tagColorPanel);
            @UNICODE@preferencesPanel.add(fontAndSizePanel);
            
            @TIBETAN@preferencesPanel.add(new JPanel(new GridLayout(3,1)));
            @TIBETAN@preferencesPanel.add(tagColorPanel);
            @TIBETAN@preferencesPanel.add(tibetanFontSizePanel);
            @TIBETAN@preferencesPanel.add(fontAndSizePanel);
        }
        
        public String getDisplayName() {
            return QD.messages.getString("InterfacePreferences");
        }
        
        public JComponent getComponent() {
            return preferencesPanel;
        }
        
        public void setPreferences() {
            boolean reRender = false;
            
            //set tagColor
            if (!new_tagColor.equals(old_tagColor)) {
                PreferenceManager.setInt(PreferenceManager.TAG_RED_KEY, new_tagColor.getRed());
                PreferenceManager.setInt(PreferenceManager.TAG_GREEN_KEY, new_tagColor.getGreen());
                PreferenceManager.setInt(PreferenceManager.TAG_BLUE_KEY, new_tagColor.getBlue());
                fieldling.quilldriver.xml.Renderer.setTagColor(new_tagColor);
                reRender = true;
            }
            
            //set tibetan font size
            @TIBETAN@int new_tibetanFontSize;
            @TIBETAN@try {
                    @TIBETAN@new_tibetanFontSize = Integer.parseInt(tibetanFontSizes.getSelectedItem().toString());
            @TIBETAN@}
            @TIBETAN@catch (NumberFormatException ne) {
                @TIBETAN@new_tibetanFontSize = old_tibetanFontSize;
            @TIBETAN@}
            @TIBETAN@if (new_tibetanFontSize != old_tibetanFontSize) {
                @TIBETAN@PreferenceManager.setInt(PreferenceManager.TIBETAN_FONT_SIZE_KEY, new_tibetanFontSize);
                @TIBETAN@for (int i=0; i<qd.transcriptToggler.getNumberOfTranscripts(); i++) {
                    @TIBETAN@QD oneQDAtATime = qd.transcriptToggler.getQDForIndex(i);
                    @TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane) (oneQDAtATime.getEditor().getTextPane());
                    @TIBETAN@dp.setByUserTibetanFontSize(new_tibetanFontSize);
                @TIBETAN@} 
                @TIBETAN@reRender = true;
            @TIBETAN@}
            
            //set roman font and size
            int new_fontSize;
            try {
                new_fontSize = Integer.parseInt(romanFontSizes.getSelectedItem().toString());
            } catch (NumberFormatException nfe) {
                new_fontSize = old_fontSize;
            }
            String new_fontFace = (String)romanFontFamilies.getSelectedItem();
            if (new_fontSize != old_fontSize || !new_fontFace.equals(old_fontFace)) {
                PreferenceManager.setInt(PreferenceManager.FONT_SIZE_KEY, new_fontSize);
                PreferenceManager.setValue(PreferenceManager.FONT_FACE_KEY, new_fontFace);
                @UNICODE@fieldling.quilldriver.xml.Renderer.setFontFamily(new_fontFace);
                @UNICODE@fieldling.quilldriver.xml.Renderer.setFontSize(new_fontSize);
                if (qd.hasContent()) {
                    @TIBETAN@org.thdl.tib.input.DuffPane dp = (org.thdl.tib.input.DuffPane) (qd.getEditor().getTextPane());
                    @TIBETAN@dp.setByUserRomanAttributeSet(new_fontFace, new_fontSize);
                    reRender = true;
                }
            }

            if (reRender) {
                if (qd.hasContent()) {
                    for (int i=0; i<qd.transcriptToggler.getNumberOfTranscripts(); i++) {
                        QD oneQDAtATime = qd.transcriptToggler.getQDForIndex(i);
                        oneQDAtATime.getEditor().render();
                    }                    
                }
            }
        }
    }
}
/*
SHOW FILE NAME AS TITLE!!
	showFileNameAsTitle = new JCheckBox(messages.getString("ShowFileNameAsTitle"));
	showFileNameAsTitle.setSelected(PreferenceManager.show_file_name_as_title!=0);
	interfacePanel.add(showFileNameAsTitle);
        PreferenceManager.show_file_name_as_title = showFileNameAsTitle.getSelectedObjects()!=null ? 1:0;
        PreferenceManager.setInt(PreferenceManager.SHOW_FILE_NAME_AS_TITLE_KEY, PreferenceManager.show_file_name_as_title);
	currentQD.updateTitles();
        */
/*
SUPPORTED FONTS!!
            if (supportedFonts==null)
            {
                    /* Combo is not filled yet, as the supported fonts
                     * depend on the language selected.
                     */
   /*                 supportedFonts = new JComboBox();
                    supportedFonts.addItemListener(new ItemListener()
                    {
                            public void itemStateChanged(ItemEvent e) 
                            {
                                    optionsChanged = true;
                                    needsToRestart = true;
                            }				
                    });
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
       /*         String dFont = PreferenceManager.getValue(PreferenceManager.DEFAULT_INTERFACE_FONT_KEY, null);
		if (dFont==null)
			defaultFont = ((Font)UIManager.get("Label.font")).getFamily();
		else
			defaultFont = dFont;
		for (i=0; i<fontNames.length; i++)
			if (fontNames[i].equals(defaultFont))
			{
				supportedFonts.setSelectedIndex(i);
				break;
			}
		if (i>=fontNames.length)
			supportedFonts.setSelectedIndex(0);
	}
DEFAULT LANGUAGE!!
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
	/*	if (PreferenceManager.default_language>-1)
		{
			defaultLanguage.setSelectedIndex(PreferenceManager.default_language);
		}
		else
		{
                        Locale loc = I18n.getLocale();
			String defaultLangLabel = loc.getDisplayName(loc);
			
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
	/*		if (i>=languageLabels.length)
			{
				defaultLanguage.setSelectedIndex(0);
			}
		}
			
		/* calling this directly since itemStateChanged
		 * is not invoked by setSelectedIndex.
		 */
	/*	updateSupportedFonts();
*/   

