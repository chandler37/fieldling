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
 * DefaultTrackRenderer.java
 *
 * Created on January 13, 2003, 7:01 PM
 */

package signstream.gui.sequencer;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DefaultTrackRenderer
implements TrackRenderer {
  
  CellRendererPane rendererPane = new CellRendererPane();
  DefaultItemRenderer rendererComp = new DefaultItemRenderer();
  StringBuffer tooltipBuffer = new StringBuffer();
  
  public DefaultTrackRenderer() {
  }
  
  public int getHeight() {
    return 20;
  }
  
  public void paint(Graphics g, SequencerTrack track, int startTime, int endTime, int pixPerSecond, 
    java.util.List selectedItems) {
    int left = startTime * pixPerSecond / 1000;
    int right = endTime * pixPerSecond / 1000;
    int height = getHeight();
    
    paintEmptyTrack(g, left, right, height);
    
    TrackItem itemToRender = track.getItemAt(startTime,true);
    TrackItem nextItem = null; 
    
    while (itemToRender != null) {
      nextItem = track.getItemAt(itemToRender.endTime, true);
      
      int s  = itemToRender.getStartTime();
      int e  = itemToRender.getEndTime();
      int itemDuration = e - s;
      int l = s * pixPerSecond / 1000;
      int w = itemDuration * pixPerSecond / 1000;
      
      rendererComp.durationWidth = w;
      
      if (nextItem != null) {
        w += (nextItem.startTime - e) * pixPerSecond / 1000;
      } else {
        w += (track.duration - e) * pixPerSecond / 1000;
      } 
      boolean selected =  selectedItems.contains(itemToRender);
      
      Graphics cg = g.create(l, 0, w, height);
      rendererComp.setSize(w, height);
      rendererComp.text = itemToRender.value.toString();
      rendererComp.selected = selected;
      rendererComp.paint(cg);
      cg.dispose();
      
      itemToRender = nextItem;
      // rendererPane.paintComponent(g, rendererComp, null, 
      //                            l, 0, w, height, true );
    }
  }
  
  void paintEmptyTrack(Graphics g, int left, int right, int height) {
    Color oldColor = g.getColor();
    g.setColor( UIManager.getColor("SequencerTrack.backgroundColor") );
    g.fillRect( left, 0, right, height  );
    g.setColor( UIManager.getColor("SequencerTrack.outlineColor") );
    g.drawLine(left,0, right, 0);
    g.drawLine(left,height, right, height);
    g.setColor(oldColor);
  }
  
  public String getTooltip(TrackItem item)
  {
    if (item == null) return null;
    tooltipBuffer.setLength(0);
    tooltipBuffer.append('"');
    tooltipBuffer.append(item.value.toString());
    tooltipBuffer.append('"');
    tooltipBuffer.append(' ');
    tooltipBuffer.append((float)(item.getDuration()) / 1000.0f);
    tooltipBuffer.append(" sec");
    return tooltipBuffer.toString();
  }  
  
  public Color getTrackLabelColor(SequencerTrack track) {
    return Color.black;
  }  
  
  public static class DefaultItemRenderer extends JComponent {
    
    public String text;
    public int durationWidth = 0;
    public boolean selected = false;
    
    public DefaultItemRenderer() {
      setFont(new Font("SANS-SERIF", Font.PLAIN, 11));
      setBorder(null);
      setForeground(Color.black);
      setOpaque(false); // should set background to track background color
    }
    
    public void paint(Graphics g) {
      int h = DefaultItemRenderer.this.getHeight();
      int w = getWidth();
      
      // paint underline 
      g.setColor(selected ? Color.red : getForeground());
      g.drawLine(0, h-4, durationWidth-1, h-4);
      if (selected) {
        // paint time drag handles
        g.fillRect(0,   h-4, 4  , h  );
        g.fillRect(durationWidth-4, h-4, 4, h  );
      }
      
      g.setColor(getForeground());
      g.setFont(getFont());
      g.drawString(text, 0, h-6);
      // super.paint(g);
    }
    
  }
}
