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
 * SequencerTester.java
 *
 * Created on January 13, 2003, 6:09 PM
 */

package signstream.gui.sequencer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class SequencerTester 
{
  
  public static void main(String[] args)
  {
    
    try
    {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e)
    { e.printStackTrace(); }
    
    final SequencerModel model = new SequencerModel(null);
    
     SequencerTrack track = new SequencerTrack("Some fairly long field name", null);
     track.addItem(new TrackItem(200, 500, "Rumpelstiltsken"));
     track.addItem(new TrackItem(600, 900, "Fred"));
     model.addTrack(0, track);
     track = new SequencerTrack("BAR", null);
     track.addItem(new TrackItem(100, 450, "Hello Goodbye"));
     track.addItem(new TrackItem(500, 800, "Fred"));
     model.addTrack(1, track);
    
    final Sequencer sequencer = new Sequencer(model);
    
    JFrame frame = new JFrame("Sequencer test driver");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    
    JMenuBar menubar = new JMenuBar();
    
    JMenu testMenu = new JMenu("Test");
    JMenuItem menuItem = new JMenuItem(
      new AbstractAction("Add track") {
        public void actionPerformed(ActionEvent event) {
          SequencerTrack track = new SequencerTrack("Added track", null);
          track.addItem(new TrackItem(1200, 4000, "Kwijibo"));
          model.addTrack(2,  track);
        }
      }
    );
    testMenu.add(menuItem);
    
    menuItem = new JMenuItem(
    new AbstractAction("Zoom Out") {
      public void actionPerformed(ActionEvent event) {
        sequencer.scaleHorizontal(110);
      }
    });
    testMenu.add(menuItem);
    menuItem = new JMenuItem(
    new AbstractAction("Zoom In") {
      public void actionPerformed(ActionEvent event) {
        sequencer.scaleHorizontal(91);
      }
    });
    testMenu.add(menuItem);
    menuItem = new JMenuItem(
    new AbstractAction("Fit to Window") {
      public void actionPerformed(ActionEvent event) {
        sequencer.fitToView();
      }
    });
    testMenu.add(menuItem);
    
    menubar.add(testMenu);
    
    
    frame.setJMenuBar(menubar);
    
    JScrollPane scrollPane = new JScrollPane(sequencer,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setSize(screenSize.width - 200, 400);
    frame.setLocation(2,345);
    frame.show();
    frame.invalidate();
  }
  
}
