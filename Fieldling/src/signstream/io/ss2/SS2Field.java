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
 * SS2Field.java
 *
 * Created on March 5, 2003, 4:53 PM
 */

package signstream.io.ss2;

import org.jdom.Element;
import signstream.utility.Log;
import signstream.utility.XMLConstant;
import signstream.io.XMLNodeSerializable;

import java.awt.Color;

// SHOULD implement equals() and hashCode()
public final class SS2Field implements XMLNodeSerializable {
  
  public static final SS2Field DUMMY = new SS2Field(
    null, null, null, null, -1, -1, -1, false, null, -1, -1
  );
  
    public final String name;
    public final String label;
    public final String prefix;
    
    public final SS2Value[] values;
    /** Used to cache the "s" and "e" values used in SS2 to represent onsets and
     offsets. This allows the field to return the unique SS2Value for annotations
     referring to these logical values, and not store them in <code>values</code>. */
    final int legacyOnsetID, legacyOffsetID;
    
    public final int group;
    public final int type;
    public final int id;
    // public final boolean predefined;
    
    public final boolean visible;
    public final Color color;
    
    public SS2Field(
      String name, String label, String prefix,
      SS2Value[] values,
      int group, int type, int id,
      boolean visible, Color color,
      int legacyOnsetID, int legacyOffsetID
    ) {
      this.name = name;
      this.label = label;
      this.prefix = prefix;
      this.values = values;
      this.group = group;
      this.type = type;
      this.id = id;
      this.visible = visible;
      this.color = color;
      this.legacyOnsetID = legacyOnsetID;
      this.legacyOffsetID = legacyOffsetID;
    }
    
  public SS2Value getValue(int id) {
    
    if (id == SS2Value.HOLD.id)  return SS2Value.HOLD;
    if (id == SS2Value.ONSET.id || id == legacyOnsetID) return SS2Value.ONSET;
    if (id == SS2Value.OFFSET.id || id == legacyOffsetID) return SS2Value.OFFSET;
    
    for (int i=0; i<values.length; i++) {
      if (values[i].id == id) return values[i];
    }
    Log.error("No value with ID "+id+" for field "+this.id);
    return null;
  }
    
    public Object toXMLNode() {
      Element element = new Element("FIELD");
      element.setAttribute(XMLConstant.ID, ""+id);
      // element.setAttribute("PREDEFINED", ""+predefined);
      element.setAttribute(XMLConstant.NAME, name);
      if (label != null && label.length() > 0 && !label.equals(name)) 
        element.setAttribute(XMLConstant.LABEL, label);
      if (prefix != null && prefix.length() > 0) 
        element.setAttribute(XMLConstant.PREFIX, prefix);
      // element.setAttribute("GROUP", ""+group);
      if (type != SS2FieldSpec.TYPE_NON_MANUAL) {
        element.setAttribute(XMLConstant.CONSTRAINT, XMLConstant.CONSTRAINT_FREE_TEXT);
        if (type == SS2FieldSpec.TYPE_FREE_TEXT) 
          element.setAttribute(XMLConstant.TIME_ALIGNMENT, XMLConstant.TIME_ALIGNMENT_NONE);
      }
      /* if (red != 0 || green != 0 || blue != 0) {
      } */
      if (!(color.equals(java.awt.Color.black))) 
        element.setAttribute("COLOR", 
          toHexString(color.getRed())+
          toHexString(color.getGreen())+
          toHexString(color.getBlue()));
      
      if (values != null) {
        for (int i=0; i<values.length; i++) {
          element.addContent((Element)values[i].toXMLNode());
        }
        element.addContent((Element)SS2Value.HOLD.toXMLNode());
        element.addContent((Element)SS2Value.ONSET.toXMLNode());
        element.addContent((Element)SS2Value.OFFSET.toXMLNode());
      }
      
      
      return element;
    }
    
    public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
      Element element = (Element) xmlNode;
      int id = Integer.parseInt(element.getAttributeValue(XMLConstant.ID));
      String name = element.getAttributeValue(XMLConstant.NAME);
      String label = element.getAttributeValue(XMLConstant.LABEL);
      String prefix = element.getAttributeValue(XMLConstant.PREFIX);
      int type = -1; // SHOULD
      int group = -1; // SHOULD
      Color color = Color.black;
      
      java.util.List valueElements = element.getChildren();
      int numValues = valueElements.size();
      SS2Value[] values = new SS2Value[numValues];
      for (int i=0;i<numValues;i++) {
        values[i] = new SS2Value(null, null, -1);
        values[i].fromXMLNode(valueElements.get(i));
      }
      return new SS2Field(
        name, label, prefix, values, group, type, id, visible, color, -1, -1
      );
    }
    
    
    String toHexString(int i) {
      if (i > 0xFF || i < 0) throw new RuntimeException("Color component is outside expected range: "+i);
      if (i <= 0x0F) return "0"+Integer.toHexString(i);
      else return Integer.toHexString(i);
    }
    
}
