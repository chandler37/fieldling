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
 * SS2Value.java
 *
 * Created on March 5, 2003, 4:54 PM
 */

package signstream.io.ss2;

import signstream.utility.XMLConstant;
import org.jdom.Element;
import signstream.io.XMLNodeSerializable;

public final class SS2Value implements XMLNodeSerializable {

    public static final SS2Value HOLD   = new SS2Value("HOLD", null, 400000);
    public static final SS2Value ONSET  = new SS2Value("ONSET", null, 400001);
    public static final SS2Value OFFSET = new SS2Value("OFFSET", null, 400002);
    public static final SS2Value EMPTY  = new SS2Value("", null, 400003);
    
    public boolean isSpecialValue() {
      if (this == HOLD || this == ONSET || this == OFFSET) {
        return true;
      }
      return false;
    }
      
    
    public final String name;
    public final String label;
    public final int id;
    private final int hashCode;
    
    public SS2Value(String name, String label, int id) {
      this.name = name;
      this.label = label;
      this.id = id;
      int seed = 17;
      seed = 37*seed + name.hashCode();
      seed = 37*seed + (label != null ? label.hashCode() : 0);
      hashCode = seed;
      // id is not considered significant for equals(), so is not used for hashcode
    } 
  
    public String toString() {
      return label == null ? name : label;
    }
    
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SS2Value)) return false;
      SS2Value v2 = (SS2Value) o;
      if ( name.equals( v2.name ) &&
        (label == v2.label || (label != null && label.equals(v2.label)))) 
        return true;
      // id is not considered significant
      return false;
    }
    public int hashCode() {
      return hashCode;
    }
    
    public Object toXMLNode() {
      Element element = new Element("VALUE");
      element.setAttribute(XMLConstant.ID, ""+id);
      element.setAttribute(XMLConstant.NAME, name);
      if (label != null && label.length() > 0 && !label.equals(name)) 
        element.setAttribute(XMLConstant.LABEL, label);
      
      return element;
    }
    
    public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
      if (!(xmlNode instanceof Element)) throw new IllegalArgumentException();
      Element element = (Element) xmlNode;
      if (!(element.getName().equals("VALUE"))) throw new IllegalArgumentException();
      
      int id = Integer.parseInt(element.getAttributeValue(XMLConstant.ID));
      String name = element.getAttributeValue(XMLConstant.NAME);
      String label = element.getAttributeValue(XMLConstant.LABEL);
      
      return new SS2Value(name, label, id);
    }
    
}
