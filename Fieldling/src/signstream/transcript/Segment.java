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

package signstream.transcript;

import java.util.*;
import signstream.scheme.*;
import signstream.io.XMLNodeSerializable;
import org.jdom.Element;
import signstream.utility.XMLConstant;

import signstream.gui.sequencer.*;

/**
 *
 * @author  Jason Boyd
 */
public class Segment implements XMLNodeSerializable, SequencerModelListener {

  Participant participant;
  boolean primary;
  // lava?: boolean visible;
  
  String glossExcerpt = "";
  String notes = "";
  long startTime;
  long endTime;
    
  Vector tracks = new Vector();
  
  private boolean dirty = false;
 
  transient CodingScheme scheme = null;
  
  public Segment() {}
  public Segment(Object xmlNode, CodingScheme scheme) {
    this.scheme = scheme;
    fromXMLNode(xmlNode);
    this.scheme = null;
  }

  public Track[] getTracks() {
    Track[] array = new Track[tracks.size()];
    tracks.copyInto(array);
    return array;
  }
  
  public void setGlossExcerpt(String text) {
    glossExcerpt = text;
  }
  public void setNotes(String text) {
    notes = text;
  }
  public void setStartTime(long s) {
    startTime = s;
  }
  public void setEndTime(long e) {
    endTime = e;
  }
  
  public void addTrack(Track track) {
    tracks.addElement(track);
  }
  public void clearTrackData() {
    tracks = new Vector();
    dirty = true;
  }
  
  /** Has the segment been modified since last save? */
  public boolean isDirty() {
    return dirty;
  }
  /** Sets the dirty state of the segment. */
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
  
  /** Returns the gloss excerpt */
  public String toString() {
    return glossExcerpt;
  }
    
  public Object toXMLNode() {
    Element element = new Element(XMLConstant.SEGMENT);
    element.setAttribute(XMLConstant.EXCERPT, glossExcerpt);
    element.setAttribute(XMLConstant.S, ""+startTime);
    element.setAttribute(XMLConstant.E, ""+endTime);
    if (notes != null && notes.length() > 0) {
      Element notesEl = new Element(XMLConstant.NOTES);
      notesEl.addContent(notes);
      element.addContent(notesEl);
    } 
   // element.setAttribute(XMLConstant.PARTICIPANT_ID, ""+participant.id);
    if (!primary) element.setAttribute(XMLConstant.PRIMARY, ""+primary);
    // should include visible
    
    /* for (int i=0; i<mediaRefs.length; i++) {
      Element el = new Element(XMLConstant.MEDIA_REF);
      el.setAttribute(XMLConstant.ID, ""+mediaRefs[i].getID());
      element.addContent(el);
    } */
    
    if (tracks != null) {
      for (int i=0, n=tracks.size(); i<n; i++) {
        Track track = (Track) tracks.elementAt(i);
        element.addContent((Element)track.toXMLNode());
      }
    } 
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    Element element = (Element) xmlNode;
    glossExcerpt = element.getAttributeValue(XMLConstant.EXCERPT);
    startTime = Integer.parseInt(element.getAttributeValue(XMLConstant.S));
    endTime   = Integer.parseInt(element.getAttributeValue(XMLConstant.E));
    // notes
    // participant
    String primaryText = element.getAttributeValue(XMLConstant.PRIMARY);
    primary = primaryText == null; // default is primary=true
    // visible
    // media refs/asset package
    
    tracks = new Vector();
    List trackElements = element.getChildren(XMLConstant.TRACK);
    for (int i=0,n=trackElements.size(); i<n; i++) {
      Element trackEl = (Element) trackElements.get(i);
      addTrack(new Track(trackEl, scheme));
    }
    
    dirty = false;
    return this;
  } // end fromXMLNode()
  
  public void tracksReordered() {
    setDirty(true);
  }
  
  public void trackUpdated(SequencerTrack track) {
    setDirty(true);
  }
  
  public void trackAdded(SequencerTrack track) {
    setDirty(true);
  }
  
  public void trackRemoved(SequencerTrack track) {
    setDirty(true);
  }
  
}
