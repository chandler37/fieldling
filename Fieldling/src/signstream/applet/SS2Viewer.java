/************************************************************************
 
  SignStream is an application for creating and viewing multi-tracked
  annotation transcripts from source video and other media,
  developed primarily for research on ASL and other signed languages.

  Signstream Copyright (C) 1997-2003 Boston University, Dartmouth
  College, and Rutgers the State University of New Jersey.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

  Development of the prototype and versions 1 and 2 was carried out
  by David Greenfield in the Department of Humanities Resources at
  Dartmouth College (Otmar Foelsche, Director).  Other contributors
  included Carol Neidle, Dawn MacLaughlin, and Robert G. Lee
  (Boston University), Benjamin Bahan (Boston University and
  Gallaudet University), and Judy Kegl (Rutgers University).
  Funding was provided by the National Science Foundation (grants
  #SBR-9410562, #SBR-9410562, #IIS-9528985, to Boston University,
  Carol Neidle, PI).

  Development of SignStream version 3 to date has been carried out
  at Boston University by Jason Boyd, with funding from the National
  Science Foundation to Boston University (grants #EIA-9809340,
  #IIS-0012573, and #IIS-0329009), Carol Neidle and Stan Sclaroff,
  co-PIs, with the participation of Robert G. Lee.

  Inquiries about the program should be directed to Prof. Carol Neidle,
  Boston University, Department of Modern Foreign Languages and
  Literatures, 718 Commonwealth Avenue, Boston, MA  02215; 617-353-6218;
  ssdevel@louis-xiv.bu.edu.

**************************************************************************/

/*
 * SS2Viewer.java
 */

package signstream.applet;

import signstream.gui.sequencer.*;
import signstream.io.XMLNodeSerializable;
import signstream.io.ss2.*;
import java.net.URL;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;


public class SS2Viewer extends javax.swing.JApplet {
  
  boolean initialized  = false;
  static final boolean loadedLocally = true; // SHOULD detect whether applet is loaded from BU
  
  Sequencer sequencer = null;
  volatile SS2File   ss2file = null;
  // HACK:
  String currentFilename;
  // stores each package as it's unqualified name (e.g. "ncslgr10a") in a vector
  // keyed by the base package name (e.g. "edu/bu/asllrp/ss2")
  static final Hashtable packages = new Hashtable();
  
  JScrollPane[] scrollPanes = new JScrollPane[]{
    new JScrollPane(), new JScrollPane()
  };
  JDialog utteranceDialog;
  JList   utteranceList = new JList();
  // MultiLabel notesText = new MultiLabel(3,20);
  JTextArea notesText = new JTextArea(3,20);
  
  JLabel packageLabel = new JLabel();
  JLabel utteranceLabel = new JLabel();
  
  // SHOULD only create this once
  JProgressBar progressBar = new JProgressBar(0,100);
  
  Action PACKAGE_INFO = new AbstractAction("Package info...") {
    public void actionPerformed(ActionEvent ae) {
      showPackageInfo();
    }
  };
  Action PARTICIPANT_INFO = new AbstractAction("Participant info...") {
    public void actionPerformed(ActionEvent ae) {
      showParticipantInfo();
    }
  };
  Action BROWSE_UTTERANCES = new AbstractAction("Browse utterances...") {
    public void actionPerformed(ActionEvent ae) {
      showUtteranceDialog();
    }
  };
  
  Action BROWSE_PACKAGES = new AbstractAction("Browse packages...") {
    public void actionPerformed(ActionEvent e) {
      showPackageBrowser();
    }
  };
  
  Action TOGGLE_TIME_SHIFTING = new AbstractAction("Allow time shifting") {
    private boolean allow  = false;
    public void actionPerformed(ActionEvent ae) {
      allow =  !allow;
      SequencerTrack.shiftNeighbors = allow;
    }
  };
  
  Action ABOUT = new AbstractAction("About SignStream Viewer") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
    }
  };
  Action HELP = new AbstractAction("Help") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
    }
  };
  
  
  Action UNDO = new AbstractAction("Undo") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
    }
  };
  Action REDO = new AbstractAction("Redo") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
    }
  };
  Action DELETE = new AbstractAction("Delete selected items") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
    }
  };
  Action CLEAR_SELECTION = new AbstractAction("Clear selection") {
    public void actionPerformed(ActionEvent ae) {
      sequencer.clearSelection();
    }
  };
  Action GOTO_WEBSITE = new AbstractAction("Visit SignStream website") {
    public void actionPerformed(ActionEvent ae) {
      try {
        getAppletContext().showDocument(
        new URL("http://www.bu.edu/asllrp/signstream/"));
        
      } catch (java.net.MalformedURLException mue) {
        mue.printStackTrace();
      }
    }
  };
  
  public void init() {
    try {
      String testing = getParameter("TESTING");
      // SHOULD detect whether applet is loaded from BU
      // loadedLocally = (testing != null && testing.equals("true"));
      
      int index = 1;
      while (true) {
        String packageName = getParameter("package"+index);
        if (packageName == null) break;
        int lastSlashPosition = packageName.lastIndexOf('/');
        String baseName = packageName.substring(0,lastSlashPosition);
        String dirName = packageName.substring(lastSlashPosition+1);
        Object obj = packages.get(baseName);
        Vector packageNames = null;
        if (obj == null) {
            packageNames = new Vector();
            packages.put(baseName, packageNames);
        } else {
            packageNames = (Vector) obj;
        }
        packageNames.addElement(dirName);
        index++;
      }
      Object[] defaults = new Object[] {
        "Sequencer.playheadColor",        new Color(0x00, 0x99, 0x33),
        "Sequencer.backgroundColor",      new Color(0xFF, 0xFF, 0xCC),
        "Sequencer.topMargin",            new Integer(10),
        "Sequencer.bottomMargin",         new Integer(10),
        "Sequencer.leftMargin",           new Integer(5), // SHOULD be using this
        "Sequencer.rightMargin",          new Integer(5),
        
        "SequencerTrack.outlineColor",    new Color(0xff, 0xcc, 0x99),
        "SequencerTrack.backgroundColor", new Color(0xff, 0xff, 0xff),
        "SequencerTrack.trackGap",        new Integer(4), // space between aggregate tracks
        "SequencerTrack.height",          new Integer(20),
      };
      for (int i=0;i<defaults.length;i+=2) {
        UIManager.put(defaults[i], defaults[i+1]);
      }
      try {
        signstream.gui.SS2TrackEditor editor = new signstream.gui.SS2TrackEditor();
        Sequencer.registerTrackEditor(
        Class.forName("signstream.io.ss2.SS2Field"),editor);
        Sequencer.registerTrackRenderer(
        Class.forName("signstream.io.ss2.SS2Field"),editor);
        
      } catch (ClassNotFoundException cnfe) {
        cnfe.printStackTrace();
      }
      initializeComponents();
      initialized = true;
    } catch (Throwable t) {
      t.printStackTrace();
    }
  } // end init()
  
  
  public void start() {
    
    try {
      
      String uttID = getParameter("initial_utterance");
      int utteranceID = uttID == null ? 0 : Integer.parseInt(uttID);
      String initPackage = getParameter("initial_package");
      initPackage = initPackage == null ? "edu/bu/asllrp/ss2/ncslgr10a" : initPackage;
      openFile(initPackage, utteranceID);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  } // end start()
  
  
  void initializeComponents() {
    
    showStatus("Initializing GUI");
    
    getContentPane().setLayout(new BorderLayout());
    
    // JSplitPane splitpane
    // = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,scrollPanes[0], scrollPanes[1]);
    // splitpane.setDividerSize(4);
    getContentPane().add(scrollPanes[0]);
    
    JPanel southPanel = new JPanel(new BorderLayout());
    
    progressBar.setStringPainted(true);
    southPanel.add(progressBar, BorderLayout.SOUTH);
    utteranceList.setVisibleRowCount(6);
    southPanel.add(new JScrollPane(utteranceList), BorderLayout.WEST);
    // JPanel notesPanel = new JPanel();
    notesText.setWrapStyleWord(true);
    notesText.setLineWrap(true);
    // notesPanel.add(utteranceLabel);
    // notesPanel.add(notesText);
    southPanel.add(notesText, BorderLayout.CENTER);
    getContentPane().add(southPanel, BorderLayout.SOUTH);
    
    // utteranceList.setSize(300,200);
    utteranceList.addMouseListener(
    new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int index = utteranceList.locationToIndex(e.getPoint());
          utteranceList.setSelectedIndex(index);
          SS2Utterance utt = (SS2Utterance)utteranceList.getSelectedValue();
          displayUtterance(utt);
        }
      }
    });
    utteranceList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        Object obj = utteranceList.getSelectedValue();
        if (obj == null) return;
        notesText.setText( ((SS2Utterance)obj).getNotes() );
      }
    });
    
    JPanel statusPanel = new JPanel() {
      public void updateUI() {
        super.updateUI();
        setBackground(UIManager.getColor("Sequencer.backgroundColor").darker());
      }
    };
    statusPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    statusPanel.add(new JLabel("Current package:"));
    statusPanel.add(packageLabel);
    // statusPanel.add(new JLabel("            Utterance:"));
    // statusPanel.add(utteranceLabel);
    getContentPane().add(statusPanel, BorderLayout.NORTH);
    
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Package");
    menu.add(PACKAGE_INFO);
    menu.add(PARTICIPANT_INFO);
    // menu.add(BROWSE_UTTERANCES);
    menu.addSeparator();
    menu.add(BROWSE_PACKAGES);
    menuBar.add(menu);
    
    menu = new JMenu("Edit");
    menu.add(UNDO);
    menu.add(REDO);
    menu.addSeparator();
    menu.add(DELETE);
    menu.addSeparator();
    menu.add(CLEAR_SELECTION);
    menuBar.add(menu);
    
    menu = new JMenu("Options");
    menu.add(new JCheckBoxMenuItem(TOGGLE_TIME_SHIFTING));
    menuBar.add(menu);
    
    menu = new JMenu("Help");
    menu.add(ABOUT);
    menu.add(HELP);
    menu.add(GOTO_WEBSITE);
    menuBar.add(menu);
    
    menu = new JMenu("Test");
    /* menu.add(new AbstractAction("Test track reorder") {
      public void actionPerformed(ActionEvent ae) {
        sequencer.getModels()[0]
        .reorderTracks(new int[] {13,12,11,10,9,8,7,6,5,4,3,2,1,0,14});
      }
    }); */
    
    menu.add(new AbstractAction("Change background") {
      public void actionPerformed(ActionEvent ae) {
        Color newColor = JColorChooser.showDialog(
        SwingUtilities.getWindowAncestor(SS2Viewer.this),
        "Choose background color", UIManager.getColor("Sequencer.backgroundColor"));
        UIManager.put("Sequencer.backgroundColor", newColor);
        SwingUtilities.updateComponentTreeUI(SS2Viewer.this);
      }
    });
    menu.add(new AbstractAction("Change track outlines") {
      public void actionPerformed(ActionEvent ae) {
        Color newColor = JColorChooser.showDialog(
        SwingUtilities.getWindowAncestor(SS2Viewer.this),
        "Choose background color", null);
        UIManager.put("SequencerTrack.outlineColor", newColor);
        SwingUtilities.updateComponentTreeUI(SS2Viewer.this);
      }
    });
    menu.add(new AbstractAction("Change track background") {
      public void actionPerformed(ActionEvent ae) {
        Color newColor = JColorChooser.showDialog(
        SwingUtilities.getWindowAncestor(SS2Viewer.this),
        "Choose background color", null);
        UIManager.put("SequencerTrack.backgroundColor", newColor);
        SwingUtilities.updateComponentTreeUI(SS2Viewer.this);
      }
    });
    menu.add(new AbstractAction("Change track gap") {
      public void actionPerformed(ActionEvent ae) {
        JSlider slider = new JSlider(0,10,UIManager.getInt("SequencerTrack.trackGap"));
        slider.setMajorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintTrack(true);
        slider.setPaintLabels(true);
        JOptionPane.showInputDialog(
        SwingUtilities.getWindowAncestor(SS2Viewer.this), slider);
        
        UIManager.put("SequencerTrack.trackGap", new Integer(slider.getValue()));
        SwingUtilities.updateComponentTreeUI(SS2Viewer.this);
      }
    });
    menu.add(new AbstractAction("Change minimum track height") {
      public void actionPerformed(ActionEvent ae) {
        JSlider slider = new JSlider(15,30,UIManager.getInt("SequencerTrack.height"));
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTrack(false);
        slider.setSnapToTicks(true);
        JOptionPane.showInputDialog(
        SwingUtilities.getWindowAncestor(SS2Viewer.this), slider);
        
        UIManager.put("SequencerTrack.height", new Integer(slider.getValue()));
        SwingUtilities.updateComponentTreeUI(SS2Viewer.this);
      }
    });
    if (loadedLocally)
      menuBar.add(menu);
    
    setJMenuBar(menuBar);
  } // end initializeComponents()
  
  
  
  void showPackageInfo() {
    SS2DBProfile profile = ss2file.dbProfile;
    JLabel      dbFilenameLabel    = new JLabel("Original DB file:");
    
    String version = profile.getVersionText();
    if (version == null || version.equals("")) version = "1";
    JLabel      dbFilenameText     = new JLabel(currentFilename+", v. "+version);
    
    JLabel      authorLabel        = new JLabel("Author:");
    JLabel      citationLabel      = new JLabel("Citation:");
    JLabel      distributorLabel   = new JLabel("Distributor:");
    JLabel      notesLabel         = new JLabel("Notes:");
    
    JTextArea   authorText         = new MultiLabel(profile.getAuthor());
    JTextArea   citationText       = new MultiLabel(profile.getDistributor());
    JTextArea   distributorText    = new MultiLabel(profile.getCitation());
    JTextArea   notesText          = new MultiLabel(profile.getNotes());
    
    
    GridBagLayout layout = new GridBagLayout();
    JPanel infoPanel = new JPanel(layout);
    infoPanel.setBorder(new TitledBorder("Package Information"));
    GridBagConstraints gbc = new GridBagConstraints();
    
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3,3,3,3);
    
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
    infoPanel.add(dbFilenameLabel, gbc);
    infoPanel.add(authorLabel, gbc);
    infoPanel.add(distributorLabel, gbc);
    infoPanel.add(citationLabel, gbc);
    infoPanel.add(notesLabel, gbc);
    
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx = 1;
    gbc.gridy = 0;
    infoPanel.add(dbFilenameText, gbc);
    gbc.gridy = GridBagConstraints.RELATIVE;
    infoPanel.add(authorText, gbc);
    infoPanel.add(distributorText, gbc);
    infoPanel.add(citationText, gbc);
    infoPanel.add(notesText, gbc);
    JOptionPane.showMessageDialog(SS2Viewer.this, infoPanel);
  }
  
  void showUtteranceDialog() {
    if (utteranceDialog == null) {
      JScrollPane listScrollPane = new JScrollPane(utteranceList);
      // listScrollPane.setPreferredSize(new Dimension(200,150));
      
      JPanel panel = new JPanel();
      panel.add(listScrollPane);
      panel.add(notesText);
      utteranceDialog = new JDialog(
      (Frame) SwingUtilities.getWindowAncestor(SS2Viewer.this),
      "Utterances", false);
      utteranceDialog.getContentPane().add(panel);
      utteranceDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
      utteranceDialog.setLocation(400,400);
    }
    utteranceDialog.pack();
    utteranceDialog.setVisible(true);
  }
  
  
  
  void showPackageBrowser() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        
        JTree       packageExplorer = new JTree(packages);
        packageExplorer.expandRow(0);
        JScrollPane packageScrollPane = new JScrollPane(packageExplorer);
        JPanel      packageBrowser = new JPanel();
        
        packageExplorer.getSelectionModel().setSelectionMode(
        javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        packageScrollPane.setPreferredSize(new Dimension(150,200));
        packageBrowser.setLayout(new BorderLayout());
        packageBrowser.add(new JLabel("Select a package"), BorderLayout.NORTH);
        packageBrowser.add(packageScrollPane);
        
        TreeNode selectedNode = null;
        while (selectedNode == null || !selectedNode.isLeaf()) {
          int option = JOptionPane.showConfirmDialog(
          SS2Viewer.this, packageBrowser, "Package Browser",
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (option != JOptionPane.OK_OPTION) return;
          selectedNode = (TreeNode)packageExplorer.getLastSelectedPathComponent();
        }
        
        String filename = (String)
        ((DefaultMutableTreeNode)selectedNode).getUserObject();
        String packageName = (String)
        ((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
        final String url = packageName + '/' + filename; 
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            openFile(url, 0);
          }
        });
      }
    });
  }
  
  
  void showParticipantInfo() {
    SS2Participant[] participants = ss2file.participants;
    GridBagLayout layout = new GridBagLayout();
    JPanel participantsPanel = new JPanel(layout);
    participantsPanel.setBorder(new TitledBorder("Participants"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(3,3,3,3);
    
    int lastRow = 0;
    for (int i=0;i<participants.length; i++) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(participants[i].getName());
      buffer.append("  (");
      buffer.append(participants[i].getLabel());
      buffer.append("),");
      int age = participants[i].getAge();
      if (age > 0 ) {
        buffer.append("  Age: ");
        buffer.append(age);
        buffer.append(",");
      }
      buffer.append(participants[i].getGender() == SS2Participant.FEMALE ? "  Female," : "  Male,");
      buffer.append("  Language: ");
      buffer.append(participants[i].getLanguage());
      
      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx = 0;
      gbc.gridy = lastRow++;
      gbc.gridwidth = 2;
      
      participantsPanel.add(new JLabel(buffer.toString()), gbc);
      
      String background = participants[i].getBackground();
      String parentInfo = participants[i].getParentInfo();
      String comments = participants[i].getComments();
      
      gbc.anchor = GridBagConstraints.NORTHEAST;
      gbc.gridx = 0;
      gbc.gridy = GridBagConstraints.RELATIVE;
      gbc.gridwidth = 1;
      if (background.length() > 0)
        participantsPanel.add(new JLabel("Background:"), gbc);
      if (parentInfo.length() > 0)
        participantsPanel.add(new JLabel("Parent info:"), gbc);
      if (comments.length() > 0)
        participantsPanel.add(new JLabel("Comments:"), gbc);
      
      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridx = 1;
      gbc.gridy = lastRow;
      if (background.length() > 0) {
        gbc.gridy = lastRow++;
        participantsPanel.add(new MultiLabel(background), gbc);
      }
      if (parentInfo.length() > 0) {
        gbc.gridy = lastRow++;
        participantsPanel.add(new MultiLabel(parentInfo), gbc);
      }
      if (comments.length() > 0) {
        gbc.gridy = lastRow++;
        participantsPanel.add(new MultiLabel(comments), gbc);
      }
    }
    
    JOptionPane.showMessageDialog(SS2Viewer.this, participantsPanel);
  } // end showParticipantInfo()
  
  
  static class MultiLabel extends JTextArea {
    MultiLabel(int rows, int cols) {
      super(rows, cols);
      UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
      Color background = (Color) uiDefaults.get("Panel.background");
      setSelectionColor(background);
      setBackground(background);
      setForeground((Color) uiDefaults.get("Panel.foreground"));
      setLineWrap(true);
      setWrapStyleWord(true);
      setEditable(false);
    }
    MultiLabel(String text) {
      this(3, 40);
      setText(text);
    }
  }
  
  static final String basePackageURL = "http://www.bu.edu/av/asllrp/assets/";
  synchronized void openFile(final String packageName, final int utteranceID) {
    final String filename = packageName.substring(packageName.lastIndexOf('/')+1);
    String packageURL = basePackageURL + packageName + '/' + filename;
    
    showStatus("Loading data for "+filename);
    
    URL url = null;
    try {
      url = new URL(getDocumentBase(), packageURL);
      ss2file = new SS2File(url);
    } catch (Throwable e) {
      e.printStackTrace();
      return;
    }
    
    
    Thread t = new Thread() {
      public void run() {
        try {
          ss2file.parseData();
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
        
        currentFilename = filename;
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            utteranceList.setListData(ss2file.utterances);
            utteranceList.setSelectedIndex(0);
            
            showStatus("");
            packageLabel.setText("edu.bu.asllrp.ss2."+currentFilename);
            utteranceList.setSelectedIndex(utteranceID);
            displayUtterance(ss2file.utterances[utteranceID]);
            // showUtteranceDialog();
          }
        });
      }
    };
    t.start();
    
    
    javax.swing.Timer timer = new javax.swing.Timer(100, new ActionListener() {
      {
        progressBar.setValue(0);
        progressBar.setMaximum(100);
      }
      public void actionPerformed(ActionEvent ae) {
        int status = ss2file.getLoadProgress();
        int percent = ss2file.getPercentStreamRead();
        
        if (status <= SS2File.READING_STREAM) {
          progressBar.setValue(percent);
          progressBar.setString("Loaded "+percent+"%");
        } else {
          progressBar.setMaximum(ss2file.LOADED);
          progressBar.setValue(status);
          progressBar.setString(ss2file.getLoadMessage());
        }
        
        if (status == SS2File.LOADED) {
          stop();
          progressBar.setString(currentFilename + " loaded");
        }
      }
    });
    timer.start();
  } // end openFile()
  
  
  
  void displayUtterance(Object obj) {
    SS2Utterance utterance  = (SS2Utterance) obj;
    utteranceLabel.setText(utterance.getExcerpt());
    
    //LOGGING//LOGGINGSystem.out.println("Participants: "+utterance.participantSegments.length);
    
    for (int s=0; s < 1; s++) {
      SequencerModel model = new SequencerModel(ss2file.fieldSpec);
      SS2ParticipantSegment segment = utterance.participantSegments[0];
      //LOGGING//LOGGINGSystem.out.println(utterance.startTime);
      
      SS2Track[] tracks = segment.tracks;
      for (int i=0;i<tracks.length; i++) {
        SS2Track ss2track = tracks[i];
        SS2Field ss2field = ss2file.fieldSpec.getField(ss2track.getFieldID());
        SequencerTrack seqTrack = new SequencerTrack(ss2field.name, ss2field);
        
        SS2Annotation[] annotations = ss2track.annotations;
        for (int j=0;j<annotations.length; j++) {
          SS2Annotation anno = annotations[j];
          seqTrack.addItem(new TrackItem(anno.startTime, anno.endTime,anno.value));
        } // end annotations
        model.addTrack(i, seqTrack);
      } // end tracks
      
      sequencer = new Sequencer(model);
      scrollPanes[s].getViewport().setView(sequencer);
      
    } // end for
  } // end displayUtterance()
  
  
} // end SS2Viewer


// Do gestalt check that QT is present and initialize QT
    /* try {
      QTSession.open();
      quicktimePresent = true;
      //LOGGINGSystem.out.println("Quicktime for Java opened");
    } catch ( Throwable t ) {
      t.printStackTrace();
      //LOGGINGSystem.out.println("Quicktime for Java not opened");
      try {
        QTSession.close();
        //LOGGINGSystem.out.println("Quicktime for Java shutdown");
      } catch (Throwable t2)
      { t2.printStackTrace(); }
    } */


  /*
  public boolean openMedia(File mediaFile, int timeOffset)
  {
    if (!quicktimePresent) return false;
   
    // this.mediaFile = mediaFile;
    // this.timeOffset = timeOffset;
    try
    {
      QTFile qtFile = new QTFile( mediaFile );
      if(qtFile.exists()) {
        //LOGGINGSystem.out.println(mediaFile.getAbsolutePath()+ "doesn't exist");
        return false;
      }
      /// open as a movie
      OpenMovieFile omf = OpenMovieFile.asRead( qtFile );
      Movie movie = Movie.fromFile( omf );
   
      /// construct a movie player
      MoviePlayer moviePlayer = new MoviePlayer( movie );
      moviePlayer.setRate( 0 );
      moviePlayer.setTime( 0 );
   
      /// construct a window
      QTCanvas qtCanvas = new QTCanvas( QTCanvas.kAspectResize, 0.5f, 0.5f );
      qtCanvas.setClient( moviePlayer, true );
   
      mediaPanel.removeAll();
      mediaPanel.add( qtCanvas );
    } catch ( QTException qte )
    {
      qte.printStackTrace();
    }
    return true;
  } // end openMedia() */


/*  public void destroy() {
    if (quicktimePresent) {
      try {
        QTSession.close();
        //LOGGINGSystem.out.println("Quicktime for Java shutdown");
      } catch ( Throwable t ) {
        //LOGGINGSystem.out.println("Quicktime for Java not shutdown properly");
 
      }
    }
  } // end destroy() */