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
 * SS2ParticipantSegment.java
 *
 * Created on March 5, 2003, 5:33 PM
 */

package signstream.io.ss2;

import org.jdom.Element;
import signstream.utility.XMLConstant;
import signstream.io.XMLNodeSerializable;

public final class SS2ParticipantSegment implements XMLNodeSerializable {

  SS2Participant participant;
  boolean primary;
  boolean visible;
  
  public SS2Track[] tracks;
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String nl = System.getProperty("line.separator");
    
    buffer.append("Participant "+participant.name+nl);
    if (tracks != null) {
      for (int i=0;i<tracks.length; i++) {
        buffer.append(tracks[i].toString());
        buffer.append(nl);
      }
    } else {
      buffer.append("No tracks");
    }
    return buffer.toString();
  }
  
  public Object toXMLNode() {
    Element element = new Element(XMLConstant.SEGMENT);
    element.setAttribute(XMLConstant.PARTICIPANT_ID, ""+participant.id);
    if (!primary) element.setAttribute("PRIMARY", ""+primary);
    // should include visible
    if (tracks != null) {
      for (int i=0; i<tracks.length; i++) {
        element.addContent((Element)tracks[i].toXMLNode());
      }
    } 
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
}
