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
 * SS2Track.java
 @author Jason Boyd
 */

package signstream.io.ss2;

import org.jdom.Element;
import signstream.utility.XMLConstant;
import signstream.io.XMLNodeSerializable;

public final class SS2Track implements XMLNodeSerializable {
  String name;
  int group;
  int type;
  int fieldID;
  boolean visible;
  int order;
  
  public SS2Annotation[] annotations;
  
  public int getFieldID() { return fieldID; }
  public String getName() { return name; }
  public int getGroup() { return group; }
  public int getType() { return type; }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(fieldID+": ");
    buffer.append(name+" ");
    for (int i=0;i<annotations.length;i++)
      buffer.append(annotations[i].toString());
    
    return buffer.toString();
  }
  
  public Object toXMLNode() {
    Element element = new Element(XMLConstant.TRACK);
    // element.setAttribute(XMLConstant.NAME, name);
    element.setAttribute("FID", ""+fieldID);
    if (!visible) element.setAttribute(XMLConstant.VISIBLE, ""+visible);
    // should include type, possibly order
    StringBuffer buffer = new StringBuffer();
    for (int i=0;i<annotations.length; i++) {
      Element anno = new Element(XMLConstant.ANNOTATION);
      Object value = annotations[i].value;
      anno.setAttribute(XMLConstant.S, ""+annotations[i].startTime);
      anno.setAttribute(XMLConstant.E, ""+annotations[i].endTime);
      
      if (annotations[i].value instanceof SS2Value)
        anno.setAttribute("VID", ""+((SS2Value)value).id);
      else
        anno.addContent(value.toString());
      
      element.addContent(anno);
      
      buffer.append(annotations[i].toString());
    }
    // element.addContent(buffer.toString());
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
}
