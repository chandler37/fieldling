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
 * SequencerModel.java
 *
 * Created on January 8, 2003, 4:33 PM
 */

package signstream.gui.sequencer;

// import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class SequencerModel {

  /** Creates new SequencerModel */
    public SequencerModel(Object constraints) {
      tracks = new Vector();
      modelListeners = new Vector();
      duration = 1000; 
    }
    
  public List getTracks() {
    return tracks;
  }
  public Iterator getTrackIterator() {
    return tracks.iterator();
  }
  public int getTrackCount () {
    return tracks.size();
  }
  
  public int getDuration() {
    return duration;
  }
  
  public void addTrack (int index, SequencerTrack track) {
    tracks.add(index, track);
    duration = Math.max(duration, track.getDuration()); 
    track.model = this;
    fireTrackAdded(track);
  } 
  /** UNIMPLEMENTED */
  public SequencerTrack removeTrack (SequencerTrack track) { 
    return null; 
  } 
  
  int getTrackIndex(SequencerTrack track) {
    return tracks.indexOf(track);
  }
  
  /** Reorders the tracks in the model according to <code>oldIndices</code>.
   For example, if <code>oldIndices</code> == {1,2,0} the tracks will be reordered
   such that current track #1 is moved to #0, #2 to #1, and #0 to #2. Once the tracks
   are reordered registered SequencerModelListeners are notified.
   @throws IllegalArgumentException if the passed array doesn't match the number
   of tracks.
   */
  public void reorderTracks (int[] oldIndices) {
    if (oldIndices.length != tracks.size()) 
      throw new IllegalArgumentException("Index array must match number of tracks");
    
    Vector newTracks = new Vector(tracks.size());
    for (int i=0;i<oldIndices.length;i++) {
      newTracks.addElement(tracks.get(oldIndices[i]));
    }
    tracks = newTracks;
    fireTracksReordered();
  }
  
  public void moveTrack(int currentIndex, int newIndex) {
    if (currentIndex == newIndex) return;
    if (currentIndex < 0 || currentIndex >= tracks.size()) 
      throw new IllegalArgumentException("Invalid current index: "+currentIndex);
    if (newIndex < 0 || newIndex >= tracks.size()) 
      throw new IllegalArgumentException("Invalid new index: "+newIndex);
    
    SequencerTrack track = (SequencerTrack) tracks.elementAt(currentIndex);
    tracks.remove(currentIndex);
    //LOGGINGSystem.out.println(track.name);
    tracks.insertElementAt(track, newIndex);
    
    // if (currentIndex > newIndex) currentIndex++;
    
    fireTracksReordered();
  }
  
  void addModelListener(SequencerModelListener listener) {
    if (modelListeners.contains(listener)) return;
    modelListeners.addElement(listener);
  }
  void removeModelListener(SequencerModelListener listener) {
    modelListeners.remove(listener);
  }
  
  void fireTrackAdded(SequencerTrack track) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      //LOGGINGSystem.out.println("calling trackAdded()");
     ((SequencerModelListener) modelListeners.elementAt(n)).trackAdded(track);
    }
  }
  /** UNIMPLEMENTED */
  void fireTrackRemoved(SequencerTrack track) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      
    }
  }
  // void fireTrackUpdated(SequencerTrack track) {}
  
  
  void fireTracksReordered() {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      ((SequencerModelListener)modelListeners.elementAt(n)).tracksReordered();
    }
  }
  
  void fireTrackUpdated(SequencerTrack track) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      ((SequencerModelListener)modelListeners.elementAt(n)).trackUpdated(track); 
    }
  }
  
  /*
  void fireTrackItemUpdated(TrackItem oldItem, TrackItem newItem) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      // ((SequencerModelListener)modelListeners.elementAt(n)).trackItemUpdated(oldItem, newItem); 
    }
  }
  
  void fireTrackItemAdded(TrackItem item) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      
    }
  }
  void fireTrackItemRemoved(TrackItem item) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      
    }
  }
  void fireTrackItemTimeChanged(SequencerTrack track, TrackItem newItem) {
    for (int n=modelListeners.size()-1; n>=0; n--) {
      ((SequencerModelListener)modelListeners.elementAt(n)).trackItemTimeChanged(track, newItem); 
    }
  } */
  
  
  /** Will always be a CodingScheme, but Object is used to avoid importing
   from scheme package. */
  Object constraints;
    /** SequencerTracks */
    Vector tracks;
    /** the sequences duration in milliseconds, cached for convenience */
    int duration;
    Vector modelListeners;
}
