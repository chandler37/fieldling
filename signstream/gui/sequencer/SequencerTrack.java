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
 * SequencerTrack.java
 *
 * Created on January 8, 2003, 4:33 PM
 */

package signstream.gui.sequencer;

import java.util.*;

public class SequencerTrack {

    public SequencerTrack(String name, Object constraints) {
      this.name = name;
      this.constraints = constraints;
      trackData = new Vector();
    }

    public void addItem(TrackItem item) { 
      TrackItem existingItem = getItemAt(item.startTime, true);
      if (existingItem != null && existingItem.startTime < item.endTime) 
        throw new IllegalArgumentException("Attempt to insert item overlapping"+
         " existing item");
      
      trackData.addElement(item);
      Collections.sort(trackData);
      duration = Math.max(duration, item.endTime + 66);
    }
    
    /** QUESTIONABLE */
    public TrackItem updateItem(TrackItem oldItem, TrackItem newItem) {
      int index = trackData.indexOf(oldItem);
      trackData.set(index, newItem);
      if (model != null) model.fireTrackUpdated(this);
      return newItem;
    }
    
    
    public static boolean shiftNeighbors = false;
    /// SHOULD make chaining an atomic operation ???
    
    public TrackItem getNextItem(TrackItem item) {
      int index = trackData.indexOf(item);
      if (index >= 0 && index < trackData.size()-1) 
        return (TrackItem) trackData.elementAt(index+1);
      return null;
    }
    
    public TrackItem getPreviousItem(TrackItem item) {
      int index = trackData.indexOf(item);
      if (index > 0 && index < trackData.size()) 
        return (TrackItem) trackData.elementAt(index-1);
      return null;
    }
    
    void toggleItemLock(TrackItem item, boolean previous) {
      TrackItem previousItem = getPreviousItem(item);
      TrackItem nextItem = getNextItem(item);
      
      if (previous && previousItem != null) {
        if (previousItem.endTime == item.startTime) 
          previousItem.endTime -= 16;
        else 
          previousItem.endTime = item.startTime;
      }
      if ((!previous) && nextItem != null) {
        if (nextItem.startTime == item.endTime) 
          nextItem.startTime += 16;
        else
          nextItem.startTime = item.endTime;
      }
      if (model != null) model.fireTrackUpdated(this);
    }
    
    void changeItemStart(TrackItem item, int time, boolean unLock) {
      int index = trackData.indexOf(item);
      if (time == item.startTime) return;
      
      int start = Math.max(0,time);
      boolean movingEarlier = start < item.startTime;
      
      if ( index > 0 ) { // has an earlier neighbor
        TrackItem neighbor = (TrackItem)trackData.elementAt(index-1);
        boolean overlapping = neighbor.endTime >= start;
        boolean alreadyChained = neighbor.endTime == item.startTime;
        
        boolean chained = movingEarlier ?
           (alreadyChained || (unLock && overlapping)) : 
           (alreadyChained && !unLock);
        
        if ( overlapping || chained )
            if (shiftNeighbors || chained) {
              neighbor.setEndTime(chained ? start : start-16);
            } else {
              start = chained ? neighbor.endTime : neighbor.endTime+16;
            }
        }
      
      item.startTime = start;
      
      if (model != null) model.fireTrackUpdated(this);
    } // end changeItemStart()
    
    void changeItemEnd(TrackItem item, int time, boolean unLock) {
      int index = trackData.indexOf(item);
      if (time == item.endTime) return;
      
      int end = Math.min(getDuration(),time);
      boolean movingLater = end > item.endTime;
      
      if ( index < trackData.size()-1 ) { // has a later neighbor
        TrackItem neighbor = (TrackItem)trackData.elementAt(index+1);
        boolean overlapping = neighbor.startTime <= end;
        boolean alreadyChained = neighbor.startTime == item.endTime;
        
        boolean chained = movingLater ?
           (alreadyChained || (unLock && overlapping)) : 
           (alreadyChained && !unLock);
        
        if ( overlapping || chained )
            if (shiftNeighbors || chained) {
              neighbor.setStartTime(chained ? end : end+16);
            } else {
              end = chained ? neighbor.startTime : neighbor.startTime-16;
            }
        }
      
      item.endTime = end;
      
      if (model != null) model.fireTrackUpdated(this);
    } // end changeItemStart()
    
    
    public TrackItem removeItem(int time) {
      TrackItem item = getItemAt(time, false);
      trackData.remove(item);
      return item;
    }
    
    public TrackItem getItemAt(int time, boolean seekAhead) { 
      Iterator it = getItemIterator();
      TrackItem last = null;
      while (it.hasNext()) {
        TrackItem item = (TrackItem) it.next();
        if (time >= item.endTime) {
          continue;
        } 
        if (seekAhead || time >= item.startTime) return item;
      }
      return null;
    }
    
    public Iterator getItemIterator() {
      return trackData.iterator();
    }
    /** BUGGY */
    /* public Iterator getItemIterator(int startTime, int endTime) {
      return getTimeRangeItems(startTime, endTime).iterator();
    } */
    
    List getTimeRangeItems(int startTime, int endTime) {
      TrackItem startItem = getItemAt(startTime, true);
      TrackItem endItem = getItemAt(endTime, true);
      
      int index1 = 0, index2 = 0;
      if (startItem != null) {
        index1 = trackData.indexOf(startItem);
        if (endItem != null) {
          index2 = trackData.indexOf(endItem);
          if (endItem.startTime < endTime) index2++;
        } else {
          index2 = trackData.size();
        }
      }
      
      return trackData.subList(index1, index2);
    }
    
    public int getDuration() {
      if (model != null) return model.getDuration();
      return duration; 
    }
    void setDuration(int d) {
      duration = Math.max(duration, d);
      if (model != null) model.duration = Math.max(model.duration, duration);
    }
    public Object getConstraints() { return constraints; }
    
    /** Could be a SchemeField, or a StreamDescriptor for sample-based tracks */
    Object constraints;
    /** TrackItems */
    Vector trackData; 
    int duration;
    String name;
    
    SequencerModel model;
    
    boolean visible = true;
}
