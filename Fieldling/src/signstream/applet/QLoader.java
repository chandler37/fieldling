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

package signstream.applet;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.*;
import javax.swing.JApplet;
import javax.swing.UIManager;
import java.net.URLClassLoader;
import java.net.URL;

public class QLoader extends JApplet
implements Runnable, AppletStub {
  String appletToLoad;
  Thread appletThread;
  URLClassLoader loader;
  
  static String[] classNames = new String[] {
    "signstream.utility.Log",
    "signstream.exception.Assert",
    "signstream.exception.SignStreamException",
    "signstream.exception.SS2ImportException",
    "signstream.exception.SS2FileFormatException",
    "signstream.timing.TimeSlave",
    "signstream.gui.sequencer.TrackItem",
    "signstream.gui.sequencer.TrackRenderer",
    "signstream.gui.sequencer.TrackEditor",
    "signstream.gui.sequencer.TrackChooser",
    "signstream.gui.sequencer.SequencerTrack",
    "signstream.gui.sequencer.SequencerModel",
    "signstream.gui.sequencer.SequencerModelListener",
    "signstream.gui.sequencer.Sequencer$1",
    "signstream.gui.sequencer.Sequencer$2",
    "signstream.gui.sequencer.Sequencer$3",
    "signstream.gui.sequencer.Sequencer$4",
    "signstream.gui.sequencer.Sequencer$5",
    "signstream.gui.sequencer.Sequencer$Header",
    "signstream.gui.sequencer.Sequencer$Ruler",
    "signstream.gui.sequencer.Sequencer$Scaler",
    "signstream.gui.sequencer.Sequencer$MouseHandler",
    "signstream.gui.sequencer.Sequencer",
    "signstream.gui.sequencer.DefaultTrackEditor",
    "signstream.gui.sequencer.DefaultTrackRenderer",
    "signstream.gui.SS2TrackEditor$1",
    "signstream.gui.SS2TrackEditor$2",
    "signstream.gui.SS2TrackEditor$3",
    "signstream.gui.SS2TrackEditor$4",
    "signstream.gui.SS2TrackEditor$5",
    "signstream.gui.SS2TrackEditor$SS2ItemRenderer",
    "signstream.gui.SS2TrackEditor",
    "signstream.io.XMLNodeSerializable",
    "signstream.io.ss2.SS2Value",
    "signstream.io.ss2.SS2Field",
    "signstream.io.ss2.SS2FieldSpec",
    "signstream.io.ss2.SS2Annotation",
    "signstream.io.ss2.SS2Track",
    "signstream.io.ss2.SS2ParticipantSegment",
    "signstream.io.ss2.SS2Utterance",
    "signstream.io.ss2.SS2MediaRef",
    "signstream.io.ss2.SS2Participant",
    "signstream.io.ss2.SS2DBProfile",
    "signstream.io.ss2.SS2File$1",
    "signstream.io.ss2.SS2File",
    "signstream.applet.SS2Viewer$MultiLabel",
    "signstream.applet.SS2Viewer$1",
    "signstream.applet.SS2Viewer$2",
    "signstream.applet.SS2Viewer$3",
    "signstream.applet.SS2Viewer$4",
    "signstream.applet.SS2Viewer$5",
    "signstream.applet.SS2Viewer$6",
    "signstream.applet.SS2Viewer$7",
    "signstream.applet.SS2Viewer$8",
    "signstream.applet.SS2Viewer$9",
    "signstream.applet.SS2Viewer$10",
    "signstream.applet.SS2Viewer$11",
    "signstream.applet.SS2Viewer$12",
    "signstream.applet.SS2Viewer$13",
    "signstream.applet.SS2Viewer$14",
    "signstream.applet.SS2Viewer$15",
    "signstream.applet.SS2Viewer$16",
    "signstream.applet.SS2Viewer$17",
    "signstream.applet.SS2Viewer$18",
    "signstream.applet.SS2Viewer$19",
    "signstream.applet.SS2Viewer$20",
    "signstream.applet.SS2Viewer$21",
    "signstream.applet.SS2Viewer$22",
    "signstream.applet.SS2Viewer"
  };
  
  public void init() {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e)
    { e.printStackTrace(); }
    
    appletToLoad = getParameter("appletToLoad");
    
    setBackground(Color.black);
  }
  
  String message = new String("Please wait while applet loads.");
  
  public void paint(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(100,40, 300,100);
    
    g.setColor(Color.white);
    g.setFont(new Font(getFont().getName(), Font.BOLD, 12));
    g.drawString(message, 100,40);
    int percent = (int) ( ((float)numClassesLoaded) / ((float)classNames.length) * 100f);
    g.drawRect(100,50,percent*2,20);
    g.setColor(Color.gray);
    g.fillRect(100,50,percent*2,20);
    g.setColor(Color.white);
    g.drawString(""+percent+" %", 110, 65);
    String category = null;
    if (percent < 10) category = "utility classes";
    else if (percent < 47) category = "GUI classes";
    else if (percent < 66) category = "SignStream DB file classes";
    else category = "main application";
    g.drawString("Loading "+category+"...", 100, 90);
  }
  
 // private volatile boolean appletStarted = false;
  private volatile boolean shouldRun = true;
  private volatile int numClassesLoaded = 0;
  
  public void run() {
    synchronized(QLoader.this) {
      while (shouldRun && numClassesLoaded < classNames.length) {
        try {
          Class.forName(classNames[numClassesLoaded], 
          true, this.getClass().getClassLoader());
          repaint();
          // Thread.sleep(10); // simulate network lag
        } catch (ClassNotFoundException cnfe) { 
          cnfe.printStackTrace(); 
          shouldRun = false;
        } // catch(InterruptedException ie) {} // simulate network lag
        finally {
          if (numClassesLoaded++ >= classNames.length) {
            shouldRun = false;
            try { Thread.sleep(300); } // allow 100% loaded message to linger for a moment
            catch (InterruptedException ie) {}
          }
        }
      }
    }
    
    try {
      Class appletClass = Class.forName(appletToLoad);
      realApplet = (Applet)appletClass.newInstance();
      realApplet.setStub(this);
      getContentPane().setLayout( new GridLayout(1,0));
      getContentPane().add(realApplet);
      realApplet.init();
      realApplet.start();
   //   appletStarted = true;
    }
    catch (Exception e) {
      System.out.println( e );
    }
    validate();
  }
  
  public void start(){
    appletThread = new Thread(this);
    appletThread.start();
  }
  
  public void stop() {
    shouldRun = false;
  }
  
  
  // SHOULD register to listen to browser resizing
  public void appletResize( int width, int height ){
    resize( width, height );
    if (realApplet != null) 
      realApplet.resize(width, height);
  } 
  
  private Applet realApplet = null;
}

