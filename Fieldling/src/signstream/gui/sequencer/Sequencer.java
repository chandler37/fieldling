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

/*
 * Sequencer.java
 *
 * Created on January 8, 2003, 4:30 PM
 */

package signstream.gui.sequencer;

import signstream.utility.LogManager;
import signstream.timing.TimeSlave;
import signstream.timing.TimeSlaveHelper;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.plaf.*;

public class Sequencer extends JComponent
implements
Scrollable
, CellEditorListener
, SequencerModelListener
, TimeSlave {
  
  
  public Sequencer(SequencerModel[] models) {
    super();
    
    setModels(models);
    
    ruler = new Ruler();
    header = new Header();
    mouseHandler = new MouseHandler();
    addMouseMotionListener(mouseHandler);
    addMouseListener(mouseHandler);
    
    setLayout(null);
    // setOpaque(true); // COULD BE CAUSING OS X BLINKING BUG
    setAutoscrolls(true);
    
    ToolTipManager.sharedInstance().setEnabled(true);
    setToolTipText("Generic tooltip text");
    
    preferredSize = new Dimension(200,200);
  }
  
  public Sequencer(SequencerModel model) {
    this(new SequencerModel[] { model });
  }
  
  
  public void addNotify() {
    super.addNotify();
    if (UIManager.getColor("Sequencer.backgroundColor") == null) {
      updateUI();
    }
    configureEnclosingScrollPane();
    fitToView();
  }
  public void removeNotify() {
    unconfigureEnclosingScrollPane();
    super.removeNotify();
  }
  
  
  //// SCROLLABLE
  
  protected void configureEnclosingScrollPane() {
    Container container = getParent();
    if (container instanceof JViewport) {
      Container container2 = container.getParent();
      if (container2 instanceof JScrollPane) {
        scrollPane = (JScrollPane) container2;
        viewport = scrollPane.getViewport();
        if (viewport == null || viewport.getView() != this) {
          return;
        }
        Color backgroundColor = UIManager.getColor("Sequencer.backgroundColor");
        viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        viewport.setBackground(backgroundColor);
        viewport.setOpaque(true);
        scrollPane.setRowHeaderView(header);
        scrollPane.setColumnHeaderView(ruler);
        scrollPane.setBackground(backgroundColor);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new Scaler());
        // JTable changes the scroll pane's border
      }// end if
    } // end if
  }// end configureEnclosingScrollPane()
  
  
  protected void unconfigureEnclosingScrollPane() {
    Container container = getParent();
    if (container instanceof JViewport) {
      Container container2 = container.getParent();
      if (container2 instanceof JScrollPane) {
        JScrollPane sPane = (JScrollPane) container2;
        viewport = sPane.getViewport();
        if (viewport == null || viewport.getView() != this) {
          return;
        }
        sPane.setColumnHeaderView(null);
        sPane.setRowHeaderView(null);
        viewport = null;
        scrollPane = null;
      }// end if
    }// end if
  }// end unconfigureEnclosingScrollPane()
  
  
  public boolean getScrollableTracksViewportWidth()
  { return false;
  }
  
  public int getScrollableBlockIncrement(java.awt.Rectangle rectangle, int param, int param2)
  { return pixPerSecond;
  }
  
  public boolean getScrollableTracksViewportHeight()
  { return false;
  }
  
  public java.awt.Dimension getPreferredScrollableViewportSize()
  {    return preferredSize;
  }
  
  public int getScrollableUnitIncrement(java.awt.Rectangle rectangle, int param, int param2)
  { return pixPerSecond;
  }
  
  
  ////////// MODEL
  
  public void setModels(SequencerModel[] models) {
    if (sequencerModels != null) {
      for (int i=0;i<sequencerModels.length; i++) {
        sequencerModels[i].removeModelListener(this);
      }
      
    }
    
    sequencerModels = models;
    for (int i=0;i<sequencerModels.length; i++) {
      sequencerModels[i].addModelListener(this);
    }
  }
  public SequencerModel[] getModels() {
    return sequencerModels;
  }
  
  
  ////////// SELECTION
  
  public void clearSelection() {
    editingCanceled(null);
    if (selectedItems.size() == 0) return;
    selectedItems.clear();
    repaint();
  }
  
  private void addToSelection(TrackItem item) {
    if (item == null) throw new IllegalArgumentException();
    selectedItems.addElement(item);
    repaint();
  }
  
  private void removeFromSelection(TrackItem item) {
    if (item == null) throw new IllegalArgumentException();
    selectedItems.remove(item);
    repaint();
  }
  
  
  /////// LAYOUT
  
  void calculateDimensions() {
    /// VERTICAL DIMENSIONS
    int currentY = UIManager.getInt("Sequencer.topMargin");
    int bottomMargin = UIManager.getInt("Sequencer.bottomMargin");
    int leftMargin = UIManager.getInt("Sequencer.leftMargin");
    int rightMargin = UIManager.getInt("Sequencer.rightMargin");
    int trackGap = UIManager.getInt("SequencerTrack.trackGap");
    
    trackEdgeCache = new Vector();
    Iterator it = sequencerModels[0].getTrackIterator();
    
    while (it.hasNext()) {
      trackEdgeCache.addElement(new Integer(currentY));
      SequencerTrack track = (SequencerTrack) it.next();
      TrackRenderer trackRenderer = getTrackRenderer(track.constraints);
      int trackHeight = trackRenderer.getHeight();
      trackEdgeCache.addElement(track);
      currentY += trackHeight;
      trackEdgeCache.addElement(new Integer(currentY));
      
      currentY += trackGap;
    } // end while tracks
    
    currentY += bottomMargin;
    height = currentY;
    
    /// HORIZONTAL DIMENSIONS
    
    width = sequencerModels[0].getDuration() * pixPerSecond / 1000 + (leftMargin+rightMargin);
    preferredSize = new Dimension(width, height);
    preferredViewportSize = preferredSize;
    
    // renderSequence(null);
    
    revalidate();
    ruler.invalidate();
    repaint();
    ruler.repaint();
  }
  
  public Dimension getPreferredSize() {
    return preferredSize;
  }
  
  
  public void scaleHorizontal(float scale) {
    if (scale <= 0f) throw new IllegalArgumentException("non-positive scale: "+scale);
    if (Math.abs(scale - 1f) < 0.01f) return;
    
    pixPerSecond = (int) ( ((float) pixPerSecond) * scale);
    calculateDimensions();
  }
  
  void setWidth(int pixels) {
    if (width == pixels || width == 0) return;
    
    // SHOULD ensure actual width is used; may need floating point pixPerSecond and/or width
    // //LOGGINGSystem.out.println("setWidth("+pixels+")");
    pixPerSecond = (int) ((float)pixels * 1000f / (float)sequencerModels[0].getDuration());
    calculateDimensions();
    // //LOGGINGSystem.out.println("resulting width: "+width);
  }
  
  public void fitToView() {
    Thread t = new Thread() {
      public void run() {
        while( scrollPane != null && scrollPane.getWidth() <= 0 ) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException ie) {}
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            setWidth(scrollPane.getWidth()
            - scrollPane.getInsets().left - scrollPane.getInsets().right
            - UIManager.getInt("Sequencer.leftMargin") 
            - UIManager.getInt("Sequencer.rightMargin")
            - header.getWidth());
          }
        });
      }
    };
    t.start();
  }
  
  // SHOULD add getVisibleItemRect(), getTrackRect(), getItemRect() (this method)
  public Rectangle getTrackItemRect(SequencerTrack track, TrackItem item) {
    int index = trackEdgeCache.indexOf(track);
    if (index < 0) {
      return null;
    }
    int top = ((Integer)trackEdgeCache.elementAt(index-1)).intValue();
    int bottom = ((Integer)trackEdgeCache.elementAt(index+1)).intValue();
    int left = xAtTime(item.getStartTime());
    int right = xAtTime(item.getEndTime());
    
    return new Rectangle(left, top, right-left, bottom-top);
  } // end getTrackItemAt()
  
  // if we need this, SHOULD have setter
  public int getPixPerSecond()
  { return pixPerSecond; }
  
  
  public SequencerTrack trackAtY(int y, boolean includeGap) {
    int trackGap = UIManager.getInt("SequencerTrack.trackGap");
    
    for (int i=0, n=trackEdgeCache.size(); i<n; i+=3) {
      int topEdge = ((Integer)trackEdgeCache.elementAt(i)).intValue();
      int bottomEdge = ((Integer)trackEdgeCache.elementAt(i+2)).intValue();
      if (includeGap) bottomEdge += trackGap;
      
      if (y < topEdge) // y is above top-most track
        return includeGap ? (SequencerTrack) trackEdgeCache.elementAt(1) : null;
        if (y < bottomEdge)
          return (SequencerTrack) trackEdgeCache.elementAt(i+1);
    }
    return null;
  }
  
  int timeAtX(int xCoordinate) {
    return xCoordinate * 1000 / pixPerSecond;
   }
  int xAtTime(int time) {
    return time * pixPerSecond / 1000;
  }
  
  public TrackItem itemAt(int x, int y) {
    SequencerTrack track = trackAtY(y, false);
    if (track == null) return null;
    return track.getItemAt(timeAtX(x), false);
  }
  
  /** UNIMPLEMENTED */
  public void setTrackVisible(int t)
  {}
  
  /** Partially IMPLEMENTED -- all tracks are visible */
  public int getVisibleTrackCount() {
    return sequencerModels[0].getTrackCount();
  }
  
  
  Object getRegisteredObject(Map map, Object key) {
    if (map == null) throw new NullPointerException();
    if (map.size() == 0) throw new IllegalArgumentException("null Map argument");
    
    Class objectClass = (key != null) ? key.getClass() : Object.class;
    Object obj;
    do {
      obj = map.get(objectClass);
      objectClass = objectClass.getSuperclass();
    } while ( obj == null);
    
    return obj;
  }
  
  
  ////// EDITING
  
  
  public TrackEditor   getTrackEditor(Object foo) {
    return (TrackEditor) getRegisteredObject(trackEditors, foo);
  }
  
  public TrackChooser getTrackChooser(Object foo) {
    return (TrackChooser) getRegisteredObject(trackChoosers, foo);
  }
  
  public boolean isEditing() {
    return editor != null;
  }
  
  protected void editItemAt(Point p) { 
    int time = timeAtX(p.x);
    SequencerTrack track = trackAtY(p.y, false);
    TrackItem item = track.getItemAt(time, false);
    editItem(track, item);
  }
  
  protected void editItem(SequencerTrack track, TrackItem item) {
    if (item == null || track == null) {
      //LOGGINGSystem.out.println("Attempt to edit null item or null track");
      return;
    }
    if (isEditing() && !editor.stopCellEditing()) {
      //LOGGINGSystem.out.println("Couldn't stop edit in progress");
      return;
    }
    editingTrack = track;
    editingItem = item;
    editor = getTrackEditor(editingTrack.constraints);
    if (!editor.isItemEditable(editingItem.getValue(), editingTrack.constraints)) {
      //LOGGINGSystem.out.println("Can't edit item value: "+editingItem.getValue());
      return;
    }
    editorComp = editor.getEditorComponent(editingItem, editingTrack);
    installEditor(); 
  } /// editItemAt()
  
  
  private void installEditor() { 
    editor.addCellEditorListener(this);
    
    /// Handle a few basic types of editor components:
    /// 1. JOptionPanes which should be displayed in a dialog attached to the
    /// Frame at the root of this Sequencer and can remain displayed even
    /// when not editing (floating "palettes")
    /// 2. Simple dialogs which are displayed modally per edit
    /// 3. All other Components, which should be sized and added to the
    /// Sequencer for "in-place" editing
    
    /// Case 1: JOptionPane
    /* if (editorComp instanceof JOptionPane)
    {
      JOptionPane palette = (JOptionPane) editorComp;
      /// check to see if palette is already on-screen, and attached to same
      // window hierarchy
      if (!palette.isShowing())
      {
        if (palette.getParent() != JOptionPane.getFrameForComponent(this))
        {
          Frame paletteFrame = (Frame) palette.getParent();
          paletteFrame.dispose();
          palette.setVisible(false);
        }
      }
     
      /// Case 2: JDialog
    } else if (editorComp instanceof JDialog)
    {
     
     
     
      /// Case 3: Any other (J)Component:
    } else
    { */
    
    
    Rectangle itemBounds = getTrackItemRect(editingTrack, editingItem);
    Rectangle editorBounds = new Rectangle(itemBounds);
    Dimension preferredSize = editorComp.getPreferredSize();
    editorBounds.width =
    Math.max((int) preferredSize.getWidth(), editorBounds.width);
    // don't allow editor comp to be out of bounds
    if (editorBounds.x + editorBounds.width > getWidth())
      editorBounds.x = getWidth() - editorBounds.width;
    editorComp.setBounds(editorBounds);
    scrollRectToVisible(editorBounds);
    add(editorComp);
    editorComp.validate();
    // }
  } // end installEditor()
  
  private void uninstallEditor() {
    if (editor != null) editor.removeCellEditorListener(this);
    if (editorComp != null) remove(editorComp);
    editor = null;
    repaint();
  }
  
  public void editingStopped(javax.swing.event.ChangeEvent changeEvent) {
    editingItem.setValue(editor.getCellEditorValue());
    uninstallEditor();
    Rectangle dirtyRect = getTrackItemRect(editingTrack, editingItem);
    // HACK:
    dirtyRect.x = 0; dirtyRect.width = width;
    // renderSequence(new Rectangle[] { dirtyRect });
    repaint(dirtyRect);
  }
  
  public void editingCanceled(javax.swing.event.ChangeEvent changeEvent) {
    if (isEditing()) uninstallEditor();
  }
  
  
  /////  RENDERING
  
  
  public TrackRenderer getTrackRenderer(Object foo) {
    return (TrackRenderer) getRegisteredObject(trackRenderers, foo);
  }
  
  
  public void paintComponent(Graphics g) {
    
    Rectangle[] dirtyRects = new Rectangle[] {g.getClipBounds()};
      
      Graphics2D offscreen = (Graphics2D) g;
      
      Color backgroundColor = UIManager.getColor("Sequencer.backgroundColor");
      int trackGap = UIManager.getInt("SequencerTrack.trackGap");
      int topMargin = UIManager.getInt("Sequencer.topMargin");
      int bottomMargin = UIManager.getInt("Sequencer.bottomMargin");
      int leftMargin = UIManager.getInt("Sequencer.leftMargin");
      int rightMargin = UIManager.getInt("Sequencer.rightMargin");
      
      for (int r=0; r<dirtyRects.length; r++) {
        Rectangle clip = dirtyRects[r];
        // offscreen.setClip(clip);
        offscreen.setColor( backgroundColor );
        offscreen.fillRect(clip.x,clip.y,clip.width-clip.x,clip.height-clip.y);
        
        //  calc time for left and right of dirty rect
        int minTime = timeAtX(clip.x);
        int maxTime = timeAtX(clip.x + clip.width - 1);
        
        int currentY = topMargin; // keep track of how far down we've rendered
        offscreen.translate(0, topMargin);
        Iterator it = sequencerModels[0].getTrackIterator();
        
        while (it.hasNext()) {
          // offscreen.translate(0, trackGap);
          if (currentY > clip.y + clip.height) break; // we've renderered all visible tracks
          SequencerTrack track = (SequencerTrack) it.next();
          
          TrackRenderer trackRenderer = getTrackRenderer(track.constraints);
          int trackHeight = trackRenderer.getHeight();
          
          if (currentY + trackHeight > clip.y) { // track is within clipping rect
            // should translate Graphics or pass additional Y param to TrackRenderer.paint()?
            trackRenderer.paint(offscreen, track, minTime, maxTime, pixPerSecond, selectedItems);
          }
          offscreen.translate(0,trackHeight+trackGap);
          currentY += trackHeight + trackGap;
        } // end while tracks
        offscreen.translate(0, bottomMargin-height);
      }// end for dirty rects
  } 
  
  
  
  public String getToolTipText(MouseEvent me) {
    Point p = me.getPoint();
    SequencerTrack track = trackAtY(p.y, false);
    if (track == null) return null;
    TrackItem item = track.getItemAt(timeAtX(p.x), false);
    if (item == null) return null;
    
    TrackRenderer renderer = getTrackRenderer(track.constraints);
    return renderer.getTooltip(item);
    
  } // end getToolTipText()
  
  
  /// SequencerModelListener
  
  
  public void trackAdded(SequencerTrack track) {
    calculateDimensions();
    header.repaint();
  }
  public void trackRemoved(SequencerTrack track) {
    calculateDimensions();
    header.repaint();
  }
  public void tracksReordered() {
    calculateDimensions();
    header.repaint();
  }
  
  public void trackUpdated(SequencerTrack track) {
    // renderSequence(null);
    repaint();
  }  
  
  
  private int currentTime;
  
  public void timeChanged(int time) {
    if (time != currentTime) {
      currentTime = time;
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            repaint(); // MUST optimize for playback 
         }
      });
    }
    timeSlaveHelper.broadcastTimeChanged(time, this);
  }
  
  ////
  //// DATA
  ////
  
  //// LOGICAL DATA
  
  // SequencerModel seqModel;
  SequencerModel[] sequencerModels;
  
  /// ASSOCIATED GUI COMPONENTS
  
  protected JScrollPane		scrollPane;
  protected JViewport		viewport;
  Header header;
  Ruler ruler;
  
  // SHOULD rethink this...
  static final ImageIcon zoomOutIcon = new ImageIcon(
    Sequencer.class.getClassLoader().getResource(
        "signstream/gui/sequencer/images/ZoomOut24.gif"));
  static final ImageIcon zoomInIcon = new ImageIcon(
    Sequencer.class.getClassLoader().getResource(
        "signstream/gui/sequencer/images/ZoomIn24.gif"));
  static final ImageIcon fitViewIcon = new ImageIcon(
    Sequencer.class.getClassLoader().getResource(
        "signstream/gui/sequencer/images/fit-view.gif"));
  
  /* The buffer used to store the current rendered sequence, which is only repainted when the sequence
   data changes and is blit to the enclosing viewport as a GUI optimization. */
  // transient BufferedImage sequenceImage;
  
  protected MouseHandler		mouseHandler;
  
  static Map trackRenderers = new Hashtable();
  static Map trackEditors = new Hashtable();
  static Map trackChoosers = new Hashtable();
  
  /// GUI VARIABLES
  
  /** used to sychronize renderSequence() calls */
  protected transient Object          renderLock = new Object();
  boolean shouldRerender = true;
  
  /** Stores the vertical layout of currently visible tracks for efficient lookup by pixel coordinates.
   Each visible track is stored as a triplet of an Integer for the track's top edge, the SequencerTrack
   itself, and another Integer with the track's lower edge.
   */
  protected Vector trackEdgeCache;
  protected Dimension           preferredSize;
  protected Dimension           preferredViewportSize;
  
  protected Color playheadColor;
  protected Color backgroundColor; // SHOULD simply use JComponent's background property
  protected int topMargin;
  protected int bottomMargin;
  protected int leftMargin; 
  protected int rightMargin;
  
  public void updateUI() {
    playheadColor = UIManager.getColor("Sequencer.playheadColor");
    backgroundColor = UIManager.getColor("Sequencer.backgroundColor");
    setBackground(backgroundColor);
    topMargin = UIManager.getInt("Sequencer.topMargin");
    bottomMargin = UIManager.getInt("Sequencer.bottomMargin");
    leftMargin = UIManager.getInt("Sequencer.leftMargin");
    rightMargin = UIManager.getInt("Sequencer.rightMargin");
    
    if (scrollPane != null) 
      scrollPane.setBackground(backgroundColor);
    if (viewport != null) 
      viewport.setBackground(backgroundColor);
    
    calculateDimensions();
  }
  
  static {
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
    
    trackRenderers.put(Object.class, new DefaultTrackRenderer());
    trackEditors.put(Object.class, new DefaultTrackEditor());
    // trackChoosers.put(Object.class, new DefaultTrackChooser());
  }
  
  protected int pixPerSecond = 200;
  protected int height = -1;
  protected int width = -1;
  
  
  //////////// EDITING
  
  public static void registerTrackEditor(Class c, Object editor) {
    trackEditors.put(c, editor);
  }
  public static void registerTrackRenderer(Class c, Object renderer) {
    trackRenderers.put(c, renderer);
  }
  public static void registerTrackChooser(Class c, Object chooser) {
    trackChoosers.put(c, chooser);
  }
  
  
  transient Vector  selectedItems = new Vector();
  protected SequencerTrack focusedTrack;
  protected TrackItem focusedItem;
  
  protected TrackEditor          editor;
  protected Component         editorComp;
  protected transient SequencerTrack editingTrack;
  protected transient TrackItem	 editingItem;
  
  // private Vector slaves = new Vector();
  private TimeSlaveHelper timeSlaveHelper;
  public void setTimeSlaveHelper(TimeSlaveHelper master) {
    timeSlaveHelper = master;
  }
  
  /**
    Handles mouse input for a Sequencer.
   */
  final class MouseHandler extends MouseInputAdapter {
    
    private volatile SequencerTrack mouseOverTrack;
    private volatile TrackItem mouseOverItem;
    private volatile int mouseOverTime;
    private volatile int mouseModifiers;
    
    synchronized private void getMouseOverData(MouseEvent me) {
      mouseModifiers = me.getModifiers();
      mouseOverTrack = trackAtY(me.getY(), false);
      mouseOverTime = timeAtX(me.getX());
      
      mouseOverItem = mouseOverTrack != null ? 
        mouseOverTrack.getItemAt(mouseOverTime, false) : null;
    }
    
    public void mouseClicked(MouseEvent me) {
      getMouseOverData(me);
      
      if (mouseOverTrack == null) {
        // move playback head
        editingCanceled(null);
      } else {
        if (mouseOverItem == null) {
          clearSelection();
          editItem(mouseOverTrack, mouseOverItem); 
        } else {
          if (me.isShiftDown()) {
            if (selectedItems.contains(mouseOverItem)) 
              removeFromSelection(mouseOverItem);
            else 
              addToSelection(mouseOverItem);
          } else if (me.isControlDown()) {
            // calc which endpoint is being [un]locked
            int itemMidTime = mouseOverItem.getEndTime() - (mouseOverItem.getDuration()/2);
            boolean previous = itemMidTime > mouseOverTime;
            mouseOverTrack.toggleItemLock(mouseOverItem, previous);
          } else {
            if (selectedItems.contains(mouseOverItem)) {
              editItem(mouseOverTrack, mouseOverItem);
            } else {
              focusedTrack = mouseOverTrack;
              focusedItem = mouseOverItem;
              clearSelection();
              addToSelection(mouseOverItem); // SHOULD have setSelection()
            }
          }
        }
      }
    } // end mouseClicked()
   
    public void mousePressed(MouseEvent me) {
      getMouseOverData(me);
      
      if (mouseOverTrack == null) return; 
      
      if (mouseOverItem == null) {
        if (me.getClickCount() != 2) return;// Robert requested less immediate editing
        // add a new item of zero duration
        mouseOverItem = new TrackItem(mouseOverTime, mouseOverTime+33, "");
        mouseOverTrack.addItem(mouseOverItem);
        clearSelection();
        addToSelection(mouseOverItem);
        beginTimeDrag(mouseOverTrack, mouseOverItem, mouseOverTime);
      } else {
        if ( (Math.abs(mouseOverItem.getStartTime() - mouseOverTime) < 33) ||
             (Math.abs(mouseOverItem.getEndTime() - mouseOverTime) < 33) )
          beginTimeDrag(mouseOverTrack, mouseOverItem, mouseOverTime);
      }
    } // end mousePressed()
    
    public void mouseMoved(MouseEvent me) {
      if (draggingItem != null) return;
      
      getMouseOverData(me); 
      
      if (  mouseOverItem == null )
        Sequencer.this.setCursor(Cursor.getDefaultCursor());
      else if (mouseOverTime - mouseOverItem.startTime < 10 || 
               mouseOverItem.endTime - mouseOverTime < 10  )
        Sequencer.this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
      else 
        Sequencer.this.setCursor(Cursor.getDefaultCursor());
    } // end mouseMoved()
    
    public void mouseReleased(MouseEvent me) {
      if (draggingItem != null) endTimeDrag();
    }
    
    public void mouseDragged(MouseEvent me) {
      if (draggingItem == null) return;
      dragTime(timeAtX(me.getX()), me.isControlDown());
    }
    
    private TrackItem draggingItem;
    private SequencerTrack draggingTrack;
    private int lastDragTime = -1;
    /** If true, current time drag operation is on item start point(s), if false
     operation is on item end point(s). */
    private boolean draggingStart;
    
    void beginTimeDrag(SequencerTrack track, TrackItem item, int time) {
      draggingTrack = track;
      draggingItem = item;
      lastDragTime = time;
    }
    void dragTime(int time, boolean unLock) {
      if (time == lastDragTime) return;
      lastDragTime = time;
      // calc which endpoint(s) are being dragged
      int itemMidTime = draggingItem.getEndTime() - (draggingItem.getDuration()/2);
      draggingStart = itemMidTime > time;
      if (draggingStart) 
        draggingTrack.changeItemStart( draggingItem, time, unLock);
      else 
        draggingTrack.changeItemEnd(draggingItem, time, unLock);
    }
    void endTimeDrag() {
      TrackItem item = draggingItem;
      SequencerTrack track = draggingTrack;
      draggingTrack = null;
      draggingItem = null;
      lastDragTime = -1;
      if (item != null && item.getValue().equals(""))
        editItem(track, item);
    }
    
  } // end MouseHandler
  
  
  
  /**
   A simple component providing zoom buttons, added to the Sequencer's enclosing
   {@link JScrollPane}.
  */
  class Scaler extends JPanel {
    
    JButton zoomOutButton = new JButton();
    JButton zoomInButton  = new JButton();
    JButton fitViewButton = new JButton();
    EmptyBorder border = new EmptyBorder(0,0,0,0);
    
    public void updateUI() {
      Color color = UIManager.getColor("Sequencer.backgroundColor");
      setBorder(null);
      setBackground(color);
      if (zoomOutButton != null) {
        zoomOutButton.setBackground(color);
        zoomOutButton.setBorder(border);
        zoomOutButton.setBounds(120,1,20,20); // MAJOR HACK
      }
      if (fitViewButton != null) {
        fitViewButton.setBackground(color);
        fitViewButton.setBorder(border);
        fitViewButton.setBounds(100,1,20,20); // MAJOR HACK
      }
      if (zoomInButton != null) {
        zoomInButton.setBackground(color);
        zoomInButton.setBorder(border);
        zoomInButton.setBounds(80,1,20,20); // MAJOR HACK
      }
    } 
    
    
    Scaler() {
      setLayout(null);
      
      zoomOutButton.setAction(
      new AbstractAction(null, zoomOutIcon) {
        public void actionPerformed(ActionEvent ae) {
          scaleHorizontal(0.84f); // SHOULD make convenience methods
        }
      });
      //zoomOutButton.setRolloverIcon(zoomOutOverIcon);
      zoomOutButton.setToolTipText("Zoom out");
      
      fitViewButton.setAction(
      new AbstractAction(null, fitViewIcon) {
        public void actionPerformed(ActionEvent ae) {
          fitToView();
        }
      });
      //fitViewButton.setRolloverIcon(fitViewOverIcon);
      fitViewButton.setToolTipText("Fit to view");
      
      zoomInButton.setAction(
      new AbstractAction(null, zoomInIcon) {
        public void actionPerformed(ActionEvent ae) {
          scaleHorizontal(1.19f); // SHOULD make convenience methods
        }
      });
      //zoomInButton.setRolloverIcon(zoomInOverIcon);
      zoomInButton.setToolTipText("Zoom in");
      
      add(zoomInButton);
      add(fitViewButton);
      add(zoomOutButton);
      updateUI();
    }
  }
  
  
  
  /**
    Displays a time ruler, showing seconds and frames, set as the top header
   of the enclosing {@link JScrollPane}.
   */
  class Ruler extends JComponent {
    // int lastWidth = -1;
    
    // BufferedImage rulerImage;
    
    public Dimension getPreferredSize() {
      return new Dimension(width, 20);
    }
    
    public void paintComponent(Graphics g) {
      g.setColor(Color.white);
      int w = Math.max(viewport.getSize().width, Sequencer.this.width);
      g.fillRect(0,0,w,20);
      
      g.setColor(Color.black);
      int half = pixPerSecond/2, quarter = pixPerSecond/4, s = 0;
      int fps = 30; // should be property of sequencer
      float pixPerFrame = (float) pixPerSecond / (float) 30;
      
      for (int x = 0; x<w; x+= pixPerSecond) {
        g.drawLine(x+half, 0, x+half, 5);
        g.drawLine(x, 0, x, 19);
        g.drawString(""+s, x+3, 10);
        
        for (int f = 1; f<fps; f++) {
          int fx = x + (int) ( (float) pixPerSecond * (float) f / (float) fps );
          g.drawLine(fx, 13, fx, 19);
        }
        
        s++;
      }
      
      g.drawLine(0,19,w,19);
    }
    
  }
  
  
  
  /**
   Displays and manipulates the Sequencer's {@link SequencerTrack}s, added as the 
   left header to the enclosing JScrollPane.
   */
  class Header extends JComponent
  implements MouseListener, MouseMotionListener {
    
    Header() {
      addMouseListener(this);
      addMouseMotionListener(this);
    }
    
    public void paintComponent(Graphics g) {
      g.setColor(UIManager.getColor("Sequencer.backgroundColor"));
      Rectangle clip = g.getClipBounds();
      g.fillRect(clip.x, clip.y, clip.width, clip.height);
      
      int y = UIManager.getInt("Sequencer.topMargin");
      int trackGap = UIManager.getInt("SequencerTrack.trackGap");
      
      Iterator it = sequencerModels[0].getTrackIterator();
      
      while (it.hasNext()) {
        if (y > clip.y + clip.height) break; // we've renderered all visible tracks
        SequencerTrack track = (SequencerTrack) it.next();
        
        TrackRenderer trackRenderer = getTrackRenderer(track.constraints);
        int trackHeight = trackRenderer.getHeight();
        
        if (track == mouseOverTrack) {
          g.setColor(Color.lightGray);
          g.fillRect(4,y, getWidth()-4, trackHeight);
        }
        
        y += trackHeight;
        if (y > clip.y) { // track is within clipping rect
          g.setColor(trackRenderer.getTrackLabelColor(track));
          g.drawString(track.name, 8, y-4);
        }
        y += trackGap;
      } // end while tracks
      
      if (draggingTrack != null) {
        g.setColor(Color.black);
        // SHOULD be caching these with mouse dragging
        int dragTrackIndex = trackEdgeCache.indexOf(draggingTrack);
        int index = trackEdgeCache.indexOf(trackAtY(lastPoint.y, true));
        if (index <0 ) return;
        int lineY;
        if (dragTrackIndex > index) {
          lineY = ((Integer)trackEdgeCache.elementAt(index-1)).intValue() -3;
        } else {
          lineY = ((Integer)trackEdgeCache.elementAt(index+1)).intValue() + 1;
        }
        g.fillRect(4, lineY, getWidth()-8,2);
      }
      
    }
    
    
    public Dimension getPreferredSize() {
      return new Dimension(150, height);
    }
    
    private Point lastPoint;
    private SequencerTrack draggingTrack;
    private SequencerTrack mouseOverTrack; 
    
    public void mousePressed(MouseEvent e) {
      lastPoint = e.getPoint();
      draggingTrack = trackAtY(e.getY(), false);
      if (draggingTrack != null) 
        setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    }
    public void mouseReleased(MouseEvent e) {
      if (draggingTrack != null) {
        
        int currentIndex = getModels()[0].getTrackIndex(draggingTrack);
        int newIndex = getModels()[0].getTrackIndex(trackAtY(e.getY(), true));
        if (newIndex >=0) getModels()[0].moveTrack(currentIndex, newIndex);
      }
      draggingTrack = null;
      lastPoint = null;
      setCursor(Cursor.getDefaultCursor());
      repaint();
    }
    public void mouseClicked(MouseEvent e) {
      
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
      mouseOverTrack = null;
      repaint();
    }
    
    public void mouseMoved(MouseEvent e) {
      if (draggingTrack == null) {
        mouseOverTrack = trackAtY(e.getY(), false);
        repaint();
      } 
    }
    public void mouseDragged(MouseEvent e) {
      if (draggingTrack != null) {
        lastPoint = e.getPoint();
        Header.this.repaint();
      }
    }
    
  }
  
}

/// old code for optimizing rendering, unfortunately very buggy under certain
// OS X JVMs. Some sort of buffered image will be necessary to optimize 
// rendering during playback.

  /* public void paintComponent(Graphics g) {
    if (shouldRerender) {
      renderSequence(null);
      return;
    }
    Rectangle clip = g.getClipBounds();
    g.drawImage(sequenceImage,  clip.x,clip.y,clip.x + clip.width,clip.y + clip.height,
    clip.x,clip.y,clip.x + clip.width,clip.y + clip.height, null);
    
    // paintPlayhead(g);
  } // end paintComponent() */
  
  
  
  /* public void renderSequence(final Rectangle[] rects) {
    
    Rectangle[] dirtyRects = rects;
    if (dirtyRects == null || dirtyRects.length == 0)
      dirtyRects = new Rectangle[] { new Rectangle(0,0,width,height) };
      
      resizeImageBuffer();
      Graphics2D offscreen = sequenceImage.createGraphics();
      
      Color backgroundColor = UIManager.getColor("Sequencer.backgroundColor");
      int trackGap = UIManager.getInt("SequencerTrack.trackGap");
      int topMargin = UIManager.getInt("Sequencer.topMargin");
      int bottomMargin = UIManager.getInt("Sequencer.bottomMargin");
      int leftMargin = UIManager.getInt("Sequencer.leftMargin");
      int rightMargin = UIManager.getInt("Sequencer.rightMargin");
      
      for (int r=0; r<dirtyRects.length; r++) {
        Rectangle clip = dirtyRects[r];
        offscreen.setClip(clip);
        offscreen.setColor( backgroundColor );
        offscreen.fillRect(clip.x,clip.y,clip.width-clip.x,clip.height-clip.y);
        
        //  calc time for left and right of dirty rect
        int minTime = timeAtX(clip.x);
        int maxTime = timeAtX(clip.x + clip.width - 1);
        
        int currentY = topMargin; // keep track of how far down we've rendered
        offscreen.translate(0, topMargin);
        Iterator it = sequencerModels[0].getTrackIterator();
        
        while (it.hasNext()) {
          // offscreen.translate(0, trackGap);
          if (currentY > clip.y + clip.height) break; // we've renderered all visible tracks
          SequencerTrack track = (SequencerTrack) it.next();
          
          TrackRenderer trackRenderer = getTrackRenderer(track.constraints);
          int trackHeight = trackRenderer.getHeight();
          
          if (currentY + trackHeight > clip.y) { // track is within clipping rect
            // should translate Graphics or pass additional Y param to TrackRenderer.paint()?
            trackRenderer.paint(offscreen, track, minTime, maxTime, pixPerSecond, selectedItems);
          }
          offscreen.translate(0,trackHeight+trackGap);
          currentY += trackHeight + trackGap;
        } // end while tracks
        offscreen.translate(0, bottomMargin-height);
      }// end for dirty rects
      
      offscreen.dispose();
      
      shouldRerender = false;
      
      if (isVisible()) {
        for (int i=0; i<dirtyRects.length; i++) {
          repaint(dirtyRects[i]);
        }
      }
  } // end renderSequence()
  
  private void resizeImageBuffer() {
    
    if (sequenceImage == null ||
    sequenceImage.getHeight() < height ||
    sequenceImage.getWidth()  < width  ||
    sequenceImage.getHeight() - height > 200 ||
    sequenceImage.getWidth()  - width  > 200) {
      
      sequenceImage = null;
      sequenceImage = new BufferedImage(width + 100, height + 100, // not much room for expanding tracks...
      BufferedImage.TYPE_USHORT_565_RGB);
      int bytes = sequenceImage.getHeight() * sequenceImage.getWidth() * 2;
      float MB = bytes / 1048576.0f; // SHOULD account for non-linear buffer; this is bogus
      //LOGGINGSystem.out.println(MB +" MB used for new image buffer");
      //LOGGINGSystem.out.println(getVisibleTrackCount() + " visible tracks, "+
      (float)(sequencerModels[0].getDuration()) /1000.0f+ " seconds");
    }
    
  } // end resizeImageBuffer() */


    /* void renderRuler() {
      resizeBuffer();
      Graphics offscreen = rulerImage.createGraphics();
      offscreen.setColor(Color.white);
      offscreen.fillRect(0,0,rulerImage.getWidth(),rulerImage.getHeight());
      
      offscreen.setColor(Color.black);
      int half = pixPerSecond/2, quarter = pixPerSecond/4, s = 0;
      int fps = 30; // should be property of sequencer
      float pixPerFrame = (float) pixPerSecond / (float) 30;
      
      for (int x = 0, n=rulerImage.getWidth(); x<n; x+= pixPerSecond) {
        offscreen.drawLine(x+half, 0, x+half, 5);
        offscreen.drawLine(x, 0, x, 19);
        offscreen.drawString(""+s, x+3, 10);
        
        for (int f = 1; f<fps; f++) {
          int fx = x + (int) ( (float) pixPerSecond * (float) f / (float) fps );
          offscreen.drawLine(fx, 13, fx, 19);
        }
        
        s++;
      }
      
      offscreen.drawLine(0,19,rulerImage.getWidth(),19);
    }
    
    void resizeBuffer() {
      if (rulerImage == null || rulerImage.getWidth() < width) {
        rulerImage = null; // help the garbage collector
        rulerImage = new BufferedImage(width+100, 20, BufferedImage.TYPE_USHORT_565_RGB);
      }
    }
    
    public void paintComponent(Graphics g) {
      if (lastWidth != width) renderRuler();
      
      Rectangle clip = g.getClipBounds();
      g.drawImage(rulerImage,  clip.x,clip.y,clip.x + clip.width,clip.y + clip.height,
      clip.x,clip.y,clip.x + clip.width,clip.y + clip.height, null);
    }  */