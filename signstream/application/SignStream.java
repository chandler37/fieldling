/************************************************************************
 
 SignStream is an application for creating and viewing multi-tracked
 annotation transcripts from source video and other media,
 developed primarily for research on ASL and other signed languages.
 
 Signstream Copyright (C) 1997-2003 Boston University, Dartmouth
 College, and Rutgers the State University of New Jersey.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
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

package signstream.application;

import signstream.transcript.*;
import signstream.gui.sequencer.*;
import signstream.io.XMLNodeSerializable;
import signstream.io.ss2.*;
import signstream.scheme.*;

import java.net.URL;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;


/** The main executable application. 
 
  As transitional steps towards full functionality:
 
  For now SignStream is a single-document application, meaning it can have
 only one "document" open (analogous to SS2's DB file) and can view only one
 segment within the document at a time. However it should not be difficult to
 modify it for multiple open segments and then multiple documents. The primary
 constraints for now involve the unknown ultimate on-disk organization of
 documents, and the GUI issues involved with multiple open segments. 
 
 For now, although the legacy SS2 DB files are mostly converted into analogous
 SS3 data structures (which can then be altered without affecting any legacy 
 support code), the SS2FieldSpec is still being used to constrain annotations. 
 In order to migrate to SS3's CodingScheme, the SSDocument will need to merge
 each imported FieldSpec into the document's coding scheme, and at minimum
 a new SchemedTrackEditor/Renderer class must be created, which can initially
 be a near clone of the SS2TrackEditor. 
 
 Media playback functionality was stifled for many many months while Apple made
 Quicktime for Java unsupported on their JVM (and OS X was our development
 platform). Apple has just released (10/23/2003) a new version of Quicktime(6.4)
 intended to support QTJava on Windows and OS X, but this leaves me almost no 
 time to investigate the new APIs, reintegrate the gui.player package, and 
 contend with the presumed level of bugginess of this new platform. 
 
 Finally, media assets (primarily video files), formally hard-coded by 
 file path in SS2 (giving rise to several different end-user problems, will 
 first be transitioned into a dummy implementation of the asset package scheme, 
 which will simply maintain the original file paths. Beyond this it should be 
 possible to implement true asset packaging transparently to the application 
 classes.
 
 */

public class SignStream  {
  
  // private static Vector ssDocuments = new Vector();
  private static SSDocument document = 
    new SSDocument(CodingSchemeManager.getDefaultScheme());
  
  /// GUI components
  private static JFrame mainFrame = null;
  private static JMenuBar mainMenuBar = null;
  private static JMenuBar sequencerMenuBar = null;
  private static JDialog segmentDialog = null;
  private static JTree segmentList = null;
  private static JTextArea notesText = new JTextArea(3,20);
  private static JLabel packageLabel = new JLabel();
  private static JLabel utteranceLabel = new JLabel();
  private static JProgressBar progressBar = new JProgressBar(0,100);
  
  // will eventually be multiple Sequencers, to handle multiple open documents
  // private static Sequencer sequencer = null;
  private static JFrame sequencerFrame = null;
  private static JPanel sequencerPanel = new JPanel();
  private static JPanel segmentsPanel  = new JPanel();
  private static JSplitPane sequencerSplitPane;
  
  private static JFileChooser fileChooser = new JFileChooser();
  private static javax.swing.filechooser.FileFilter importFileFilter 
    = new ImportFileFilter();
  
  /// Actions
  
  Action OPEN_DOCUMENT = new AbstractAction("Open Document...") {
    public void actionPerformed(ActionEvent ae) {
      loadDocument(null);
    }
  };
  Action IMPORT_FILE = new AbstractAction("Import...") {
    public void actionPerformed(ActionEvent ae) {
      // should present file chooser
      fileChooser.setFileFilter(importFileFilter);
      int ret = fileChooser.showDialog(null, "Import");
      if (ret == JFileChooser.APPROVE_OPTION)
      
      // should determine whether this is a portable SS3 doc, or legacy
      // DB file
      importSS2File(fileChooser.getSelectedFile());
    }
  };
  
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
      showSegmentDialog();
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
      // sequencer.clearSelection();
    }
  };
  Action GOTO_WEBSITE = new AbstractAction("Visit SignStream website") {
    public void actionPerformed(ActionEvent ae) {
      try {
        //getAppletContext().showDocument(
        new URL("http://www.bu.edu/asllrp/signstream/");//);
        
      } catch (java.net.MalformedURLException mue) {
        mue.printStackTrace();
      }
    }
  };
  
  
  public static void main(String args[]) {
    new SignStream();
  }
  
  private SignStream() {
    loadPreferences();
    initGUI();
    connectGUIActions();
    
    mainFrame.setVisible(true);
    sequencerFrame.setVisible(true);
    
    // get initial document OR package, segment from prefs and if any load:
    // if recent document and pref, open
    // else new document
  }
  
  
  private boolean loadPreferences() {
    return true;
  }
  
  
  private boolean initGUI() {
    // make these persistent preferences...
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
    
    
    /// create main frame,
    mainFrame = new JFrame("SignStream");
    ///   set closing operations -- should actually handle this
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ///   size fullscreen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    mainFrame.setBounds(0,0,screenSize.width, 60);
    mainFrame.setResizable(false);
    
    createSequencerWindow();
    
    // create singleton dialogs, components
    // segment dialog
    segmentsPanel.setMinimumSize(new Dimension(400, 200));
    segmentsPanel.setPreferredSize(new Dimension(400, 200));
    segmentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
    //   segment list/tree
    // package explorer (or create on request? -- available packages may change)
    // "About" dialog
    // Copyright/license dialog
    
     
    return true;
  }
  
  
  private void createSequencerWindow() {
    // assemble, do not make visible
      sequencerFrame = new JFrame("Sequencer");
      
      // should use screen characteristics, should consolidate into layoutGUI()
      sequencerFrame.setBounds(10,240,1000,500);
      sequencerFrame.setJMenuBar(sequencerMenuBar);
      // separator pane for multi-participants? .... need more info in data
      sequencerPanel.setLayout(new BorderLayout());
      
      sequencerSplitPane = 
        new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      sequencerSplitPane.setOneTouchExpandable(true);
      sequencerSplitPane.setResizeWeight(1.0);
      sequencerSplitPane.setTopComponent(sequencerPanel);
      sequencerSplitPane.setBottomComponent(segmentsPanel);
      
      sequencerFrame.getContentPane().add(sequencerSplitPane);
  } // end createSequencerWindow()
  
  
  
  private void connectGUIActions() {
    // create main menu bar
    //   later can adapt OS X code to use system menu bar
    mainMenuBar = new JMenuBar();
    mainFrame.setJMenuBar(mainMenuBar);
    sequencerMenuBar = new JMenuBar();
    
    JMenu menu = new JMenu("File");
    menu.add(IMPORT_FILE);
    menu.add(PACKAGE_INFO);
    menu.add(PARTICIPANT_INFO);
    // menu.add(BROWSE_UTTERANCES);
    menu.addSeparator();
    menu.add(BROWSE_PACKAGES);
    mainMenuBar.add(menu);
    mainMenuBar.add(menu);
    
    menu = new JMenu("Edit");
    menu.add(UNDO);
    menu.add(REDO);
    menu.addSeparator();
    menu.add(DELETE);
    menu.addSeparator();
    menu.add(CLEAR_SELECTION);
    mainMenuBar.add(menu);
    
    menu = new JMenu("Options");
    menu.add(new JCheckBoxMenuItem(TOGGLE_TIME_SHIFTING));
    mainMenuBar.add(menu);
    
    menu = new JMenu("Help");
    menu.add(ABOUT);
    menu.add(HELP);
    menu.add(GOTO_WEBSITE);
    mainMenuBar.add(menu);
    
    
  } // end connectGUIActions
  
  
  /** Open a sequencer window for the given segments. 
   For now, in the SDI implementation, only one sequencer window can be open,
   so the contents will simply be updated if a window is already open. When
   multiple segments are passed, this (for now) implies a multiple-participant
   situation, where segments overlap on the timeline and should be 
   displayed in vertical panes using a split window.
   */
  private void showSegments(Segment[] segments) {
    
    for (int i=0; i<segments.length; i++) {
      Segment segment = segments[i];
      SequencerModel seqModel = new SequencerModel(document.getDocumentConstraints());
      
      Track[] tracks = segment.getTracks();
      
      for (int j=0; j<tracks.length; j++) {
        Track track = tracks[j];
        SequencerTrack seqTrack = 
          new SequencerTrack(track.getName(), track.getSS2Field());
        Annotation[] annotations = track.getAnnotations();
        for (int k=0; k<annotations.length; k++) {
          seqTrack.addItem(new TrackItem(
            annotations[k].startTime, annotations[k].endTime, annotations[k].value));
        }
        seqModel.addTrack(j, seqTrack);
      }
      
      Sequencer sequencer = new Sequencer(seqModel);
      JScrollPane scrollPane = new JScrollPane(sequencer);
      if (i == 0) {
        sequencerPanel.removeAll();
        sequencerPanel.add(scrollPane);
      }
    }
    sequencerFrame.setVisible(true);
    
  } // end openSequencerWindow()
  
  
  
  /** Open a SignStream 3 or later XML file into an SSDocument. */
  private void loadDocument(File file) {
    String filename = file.getName();
    //if (filename.endsWith(".xml") {
    //  if (filename.endsWith(".ss2.xml");
      
  } // end loadDocument()
  
  
  /** Locate and open any corresponding SignStream XML file (including
   legacy .ss2.xml files) and open. If any transcriber or version are null or 
   non-positive, and multiple files are found, provide a chooser dialog. 
   @return true if a document was found and loaded, false if no doc found
   */
  private boolean loadDocument(String packageName, String transcriber, int version) {
    return false;
  }
  
  
  /** Open a SignStream 2 DB file or .ss2.xml equivalent, 
   and convert into an SSDocument. */
  private void importSS2File(File legacyFile) {
    final SS2File ss2file;
    // must read and parse file data

    try {
      ss2file = new SS2File(legacyFile);
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
        
    document = new SSDocument(ss2file);
    // System.out.println(document.toString());
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            /*utteranceList.setListData(ss2file.utterances);
            utteranceList.setSelectedIndex(0);
            
            showStatus("");
            packageLabel.setText("edu.bu.asllrp.ss2."+currentFilename);
            utteranceList.setSelectedIndex(utteranceID);
            displayUtterance(ss2file.utterances[utteranceID]);
            // showUtteranceDialog(); */
            
            showSegments(new Segment[] {(Segment) document.segments.firstElement()});
          }
        });
      }
    };
    t.start();
    //   display progress dialog
    
  } // end importSS2File()
  
  
  private boolean loadSegment(String docName) {
      /* document descriptor?
          package, scene, time, transcriber, version
       */
    
    return true;
  }
  
  
  private void showPackageBrowser() {}
  private void showPackageInfo() {}
  private void showParticipantInfo() {}
  private void showSegmentDialog() {}
  
}

class ImportFileFilter extends javax.swing.filechooser.FileFilter {
  public boolean accept(File file) {
    if (file.isDirectory()) return true;
    if (file.getName().endsWith(".ss3.xml")) return true;
    if (file.getName().endsWith(".ss2.xml")) return true;
    if (file.getName().indexOf('.') < 0) // no file extension
      if (SS2File.isSS2File(file)) return true;
    return false;
  }
  
  public String getDescription() {
    return ".ss3.xml or SignStream 2 DB files";
  }
}