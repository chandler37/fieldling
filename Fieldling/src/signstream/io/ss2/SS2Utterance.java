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
 
 /* SS2Utterance.java
 */

package signstream.io.ss2;

import org.jdom.Element;
import signstream.utility.XMLConstant;
import signstream.io.XMLNodeSerializable;

public final class SS2Utterance implements XMLNodeSerializable {

  int id;
  String excerpt;
  public String notes;
  public SS2MediaRef[] mediaRefs;
  public long startTime;
  public long endTime;
  
  public SS2ParticipantSegment[] participantSegments;
  
  
  public String toString() {
    // HACK for displaying utterance lists
   return ""+(id+1)+"  "+excerpt;
  }
  
  public String getExcerpt() { return excerpt; }
  public String getNotes() {  return notes; }
  
  public Object toXMLNode() {
    Element element = new Element("UTTERANCE");
    element.setAttribute(XMLConstant.ID, ""+id);
    element.setAttribute(XMLConstant.EXCERPT, excerpt);
    element.setAttribute(XMLConstant.S, ""+startTime);
    element.setAttribute(XMLConstant.E, ""+endTime);
    if (notes != null && notes.length() > 0) {
      Element notesEl = new Element(XMLConstant.NOTES);
      notesEl.addContent(notes);
      element.addContent(notesEl);
    } 
    for (int i=0; i<mediaRefs.length; i++) {
      Element el = new Element(XMLConstant.MEDIA_REF);
      el.setAttribute(XMLConstant.ID, ""+mediaRefs[i].getID());
      element.addContent(el);
    }
    for (int i=0; i<participantSegments.length; i++) {
      element.addContent((Element)participantSegments[i].toXMLNode());
    }
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
  
}
