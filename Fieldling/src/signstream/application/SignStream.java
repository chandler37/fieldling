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
import signstream.io.*;
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

import org.jdom.Element;
import signstream.utility.*;

public class SignStream  {
  
  // private static Vector ssDocuments = new Vector();
  private static SSDocument document =
  new SSDocument(CodingSchemeManager.getDefaultScheme());
  
  /// GUI components
  private static JFrame mainFrame = null;
  private static JMenuBar mainMenuBar = null;
  private static JTree segmentTree = null;
  private static JScrollPane segmentTreeScrollPane = null;
  private static JTextArea notesText = new JTextArea(3,20);
  private static JLabel participantLabel = new JLabel("Participant: ");
  private static JLabel packageLabel = new JLabel("Package: ");
  private static JLabel segmentLabel = new JLabel("Gloss Excerpt: ");
  private static JLabel codingSchemeLabel = new JLabel("Coding Scheme: ");
  // private static JProgressBar progressBar = new JProgressBar(0,100);
  
  private static JPanel sequencerPanel = new JPanel();
  private static JPanel segmentsPanel  = new JPanel();
  private static JSplitPane sequencerSplitPane;
  
  private static JFileChooser fileChooser = new JFileChooser();
  private static javax.swing.filechooser.FileFilter importFileFilter
  = new ImportFileFilter();
  
  
  /** Maps all created SequencerModels -> Segments. The SequencerModel
   can be edited without affecting the original Segment.
   @see #commitEdits()
   */
  private Hashtable openedSegments = new Hashtable();
  
  /// Actions
  
  
  Action NEW_DOCUMENT = new AbstractAction("New Document...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
    }
  };
  Action OPEN_DOCUMENT = new AbstractAction("Open Document...") {
    public void actionPerformed(ActionEvent ae) {
      int ret = fileChooser.showOpenDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION)
        loadDocument(fileChooser.getSelectedFile());
    }
  };
  Action SAVE_DOCUMENT = new AbstractAction("Save Document...") {
    public void actionPerformed(ActionEvent ae) {
      int ret = fileChooser.showSaveDialog(null);
      if (ret == JFileChooser.APPROVE_OPTION)
        saveDocument(document, fileChooser.getSelectedFile());
    }
  };
  Action SAVE_TEMPLATE = new AbstractAction("Save As Template...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
    }
  };
  Action IMPORT_SS2_FILE = new AbstractAction("Import SignStream 2 File...") {
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
  Action IMPORT_PORTABLE_FILE = new AbstractAction("Import Portable Document...") {
    {
      setEnabled(false);
    }
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
  Action EXPORT_PORTABLE_FILE = new AbstractAction("Export Portable Document...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      loadDocument(null);
    }
  };
  
  Action PACKAGE_INFO = new AbstractAction("Package info...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      showPackageInfo();
    }
  };
  Action PARTICIPANT_INFO = new AbstractAction("Participant info...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      showParticipantInfo();
    }
  };
  
  Action BROWSE_PACKAGES = new AbstractAction("Browse packages...") {
    {
      setEnabled(false);
    }
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
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      // sequencer.clearSelection();
    }
  };
  Action GOTO_WEBSITE = new AbstractAction("Visit SignStream website") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      try {
        //getAppletContext().showDocument(
        new URL("http://www.bu.edu/asllrp/signstream/");//);
        
      } catch (java.net.MalformedURLException mue) {
        mue.printStackTrace();
      }
    }
  };
  Action EDIT_NOTES = new AbstractAction("Notes...") {
    {
      setEnabled(false);
    }
    public void actionPerformed(ActionEvent ae) {
      
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
      signstream.gui.SS2TrackEditor ss2editor = new signstream.gui.SS2TrackEditor();
      Sequencer.registerTrackEditor(
      Class.forName("signstream.io.ss2.SS2Field"),ss2editor);
      Sequencer.registerTrackRenderer(
      Class.forName("signstream.io.ss2.SS2Field"),ss2editor);
      signstream.gui.BasicSchemeTrackEditor editor =
      new signstream.gui.BasicSchemeTrackEditor();
      Sequencer.registerTrackEditor(
      Class.forName("signstream.scheme.ConcreteField"),editor);
      Sequencer.registerTrackRenderer(
      Class.forName("signstream.scheme.ConcreteField"),editor);
      
    } catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
    }
    
    
    
    mainFrame = new JFrame("SignStream");
    ///   should actually handle this for dirty documents
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ///   size fullscreen; should figure out Taskbar/Dock/Apple menu/etc.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    mainFrame.setBounds(0,0,screenSize.width, screenSize.height);
    
    // create singleton dialogs, components
    createSegmentsPanel();
    
    // separator pane for multi-participants? .... need more info in data
    sequencerPanel.setLayout(new BorderLayout());
    
    sequencerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sequencerSplitPane.setOneTouchExpandable(true);
    sequencerSplitPane.setResizeWeight(1.0);
    sequencerSplitPane.setTopComponent(sequencerPanel);
    sequencerSplitPane.setBottomComponent(segmentsPanel);
    
    mainFrame.getContentPane().add(sequencerSplitPane);
    
    // package explorer (or create on request? -- available packages may change)
    // "About" dialog
    // Copyright/license dialog
    
    return true;
  }
  
  
  private void createSegmentsPanel() {
    /// segment selection component
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    segmentTree = new JTree(new DefaultTreeModel(root));
    segmentTree.setRootVisible(false);
    segmentTree.setEditable(false);
    segmentTree.setVisibleRowCount(6);
    segmentTree.getSelectionModel().setSelectionMode(
    TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    segmentTreeScrollPane = new JScrollPane(segmentTree);
    segmentTreeScrollPane.setViewportView(segmentTree);
    segmentTreeScrollPane.setPreferredSize(new Dimension(300,180));
    
    /// selected segment meta-data
    JButton openSegmentButton   = new JButton("Open");
    JButton deleteSegmentButton = new JButton("Delete");
    deleteSegmentButton.setEnabled(false);
    
    openSegmentButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Object obj = segmentTree.getLastSelectedPathComponent();
        if (obj == null) return;
        obj = ((DefaultMutableTreeNode)obj).getUserObject();
        if (obj instanceof Segment)
          showSegments(new Segment[] {(Segment) obj});
      }
    });
    
    
    /// layout the panel
    segmentsPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    segmentsPanel.setMinimumSize(new Dimension(400, 200));
    segmentsPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));
    
    JPanel infoPanel = new JPanel(new GridBagLayout());
    infoPanel.setBorder(new TitledBorder("Segment Info"));
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(4,4,4,4);
    gbc.gridy = 0;
    infoPanel.add(segmentLabel, gbc);
    gbc.gridy = 1;
    infoPanel.add(participantLabel, gbc);
    gbc.gridy = 2;
    infoPanel.add(packageLabel, gbc);
    gbc.gridy = 3;
    infoPanel.add(codingSchemeLabel, gbc);
    
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    infoPanel.add(new JLabel("khbfdvjhbfavjwhbfv"), gbc);
    gbc.gridy = 1;
    infoPanel.add(new JLabel("khbfdvjhbfavjwhbfv"), gbc);
    gbc.gridy = 2;
    infoPanel.add(new JLabel("khbfdvjhbfavjwhbfv"), gbc);
    gbc.gridy = 3;
    infoPanel.add(new JLabel("khbfdvjhbfavjwhbfv"), gbc);
    
    gbc.gridx = 2;
    gbc.gridy = 0;
    infoPanel.add(new JLabel("Notes:"), gbc);
    gbc.gridy = 1;
    gbc.gridheight = 3;
    infoPanel.add(notesText, gbc);
    
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.gridx = 0;
    gbc.gridy = 0;
    segmentsPanel.add(segmentTreeScrollPane, gbc);
    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    segmentsPanel.add(infoPanel, gbc);
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    segmentsPanel.add(openSegmentButton, gbc);
    gbc.gridx = 2;
    segmentsPanel.add(deleteSegmentButton, gbc);
    
  } // end createSegmentsPanel()
  
  
  
  
  
  private void connectGUIActions() {
    // create main menu bar
    //   later can adapt OS X code to use system menu bar
    mainMenuBar = new JMenuBar();
    mainFrame.setJMenuBar(mainMenuBar);
    
    JMenu menu = new JMenu("File");
    menu.add(NEW_DOCUMENT);
    menu.add(OPEN_DOCUMENT);
    menu.add(SAVE_DOCUMENT);
    menu.add(SAVE_TEMPLATE);
    menu.addSeparator();
    menu.add(IMPORT_SS2_FILE);
    menu.add(IMPORT_PORTABLE_FILE);
    menu.add(EXPORT_PORTABLE_FILE);
    mainMenuBar.add(menu);
    
    menu = new JMenu("Transcript");
    menu.add(PACKAGE_INFO);
    menu.add(PARTICIPANT_INFO);
    menu.add(EDIT_NOTES);
    mainMenuBar.add(menu);
    
    menu = new JMenu("Edit");
    menu.add(UNDO);
    menu.add(REDO);
    menu.addSeparator();
    menu.add(CLEAR_SELECTION);
    menu.add(DELETE);
    menu.addSeparator();
    menu.add(new JCheckBoxMenuItem(TOGGLE_TIME_SHIFTING));
    mainMenuBar.add(menu);
    
    menu = new JMenu("Media");
    mainMenuBar.add(menu);
    
    menu = new JMenu("Options");
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
        new SequencerTrack(track.getName(), track.getField());
        Annotation[] annotations = track.getAnnotations();
        for (int k=0; k<annotations.length; k++) {
          seqTrack.addItem(new TrackItem(
          annotations[k].startTime, annotations[k].endTime, annotations[k].value));
        }
        seqModel.addTrack(j, seqTrack);
      } // end for each track
      
      Sequencer sequencer = new Sequencer(seqModel);
      JScrollPane scrollPane = new JScrollPane(sequencer);
      if (i == 0) {
        sequencerPanel.removeAll();
        sequencerPanel.add(scrollPane);
        sequencerPanel.validate();
      }
      openedSegments.put(seqModel, segment);
    } // end for each segment
  } // end showSegments()
  
  
  /** Repopulate a Segment's track data from the SequencerModel data.
   Before calling this method, the Segment retains the data as of initialization
   or the last commit, allowing for simple in-memory "revert" operations.
   */
  public void commitEdits(SequencerModel seqModel) {
    Segment segment = (Segment) openedSegments.get(seqModel);
    segment.clearTrackData();
    Iterator it = seqModel.getTrackIterator();
    while (it.hasNext()) {
      SequencerTrack seqTrack = (SequencerTrack) it.next();
      Track track = new Track();
      track.setField((SchemeField) seqTrack.getConstraints());
      track.setName(seqTrack.getName()); // should add method
      Iterator it2 = seqTrack.getItemIterator();
      while (it2.hasNext()) {
        TrackItem trackItem = (TrackItem) it2.next();
        Annotation annotation =
        new Annotation(trackItem.getValue(),
        trackItem.getStartTime(),
        trackItem.getEndTime());
        track.addAnnotation(annotation);
      } // end for each TrackItem
      segment.addTrack(track);
      // segment should already be marked dirty, and should remain so
    } // end for each sequencer track
  } // end commitEdits()
  
  
  /** Open a SignStream 3 or later document into an SSDocument. */
  private void loadDocument(final File file) {
    
    Thread t = new Thread() {
      public void run() {
        
        Object xmlNode = IOManager.loadXMLFile(file);
        Element element = (Element) xmlNode;
        String codingSchemeName = element.getAttributeValue(XMLConstant.CODING_SCHEME);
        CodingScheme scheme = CodingSchemeManager.getScheme(codingSchemeName);
        document = new SSDocument(xmlNode, scheme);
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            displayDocument();
          }
        });
      }
    };
    t.start();
    //   should display progress dialog
  } // end loadDocument()
  
  private void saveDocument(SSDocument doc, File saveFile) {
    Enumeration enum = openedSegments.keys();
    while (enum.hasMoreElements()) {
      SequencerModel seqModel = (SequencerModel) enum.nextElement();
      commitEdits(seqModel);
    }
    CodingSchemeManager.saveScheme(
    ((CodingScheme)doc.getDocumentConstraints()).getName());
    boolean success = IOManager.saveXMLFile(saveFile, doc, false);
  } // end saveDocument()
  
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
    try {
      ss2file = new SS2File(legacyFile);
    } catch (Throwable e) { // should handle SignStreamExceptions
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
        
        document = new SSDocument(CodingSchemeManager.getDefaultScheme(), ss2file);
        
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            displayDocument();
          }
        });
      }
    };
    t.start();
    //   should display progress dialog
    
  } // end importSS2File()
  
  private void displayDocument() {
    String docName = document.getFilename();
    java.util.List segs = document.segments;
    DefaultMutableTreeNode root =
    (DefaultMutableTreeNode) segmentTree.getModel().getRoot();
    DefaultMutableTreeNode docNode = new DefaultMutableTreeNode(docName);
    
    for (int i=0, n=segs.size(); i<n; i++ ) {
      docNode.add(new DefaultMutableTreeNode(segs.get(i)));
    }
    
    root.add(docNode);
    ((DefaultTreeModel)segmentTree.getModel())
    .insertNodeInto(docNode, root, 0);
    segmentTree.expandPath(new TreePath(new Object[]{root,docNode}));
    
    showSegments(new Segment[] {(Segment) document.segments.firstElement()});
  }
  
  private void showPackageBrowser() {}
  private void showPackageInfo() {}
  private void showParticipantInfo() {}
  
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