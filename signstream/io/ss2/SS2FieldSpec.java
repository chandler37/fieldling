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
 * SS2FieldSpec.java
 *
 * Created on March 5, 2003, 4:19 PM
 */

package signstream.io.ss2;

import org.jdom.Element;
import signstream.utility.Log;
import signstream.exception.Assert;
import signstream.io.XMLNodeSerializable;
import java.io.IOException;
import java.io.DataInputStream;
import java.util.List;
import java.util.Vector;
import java.awt.Color;


public final class SS2FieldSpec implements XMLNodeSerializable {

  public static final int              TYPE_NON_MANUAL = 0;
  public static final int              TYPE_GLOSS = 1;
  public static final int              TYPE_POS_TAG = 2; // unused in SS2
  public static final int              TYPE_FREE_TEXT = 3;
  
  static final int              GROUP_UPPER = 0;
  static final int              GROUP_FIXED = 1;
  static final int              GROUP_LOWER = 2;
  
  // ID offsets, since each group starts fields at zero in DB files
  static final int              GROUP_FIXED_ID_OFFSET = 10000;
  static final int              GROUP_LOWER_ID_OFFSET = 20000;
  
  SS2Field[] fields;
  
  
  static SS2FieldSpec parseFieldSpec(DataInputStream dis) 
  throws IOException {
    int version = SS2File.readU16(dis);
    Assert.check(version == 5, "Version is "+version);
    
    SS2FieldSpec fieldSpec = new SS2FieldSpec();
    
    int numFields = SS2File.readU16(dis);
    int nextUpperID = SS2File.readU16(dis); // discard
    int nextFixedID = SS2File.readU16(dis); // discard
    int nextLowerID = SS2File.readU16(dis); // discard
    
    fieldSpec.fields = new SS2Field[numFields];
    
    for (int i=0; i<numFields; i++) {
      int fieldChunkVersion = SS2File.readU16(dis);
      Assert.check(version == 5);
      
      int group     = SS2File.readU16(dis);
      int type      = SS2File.readU16(dis);
      Assert.check(type != TYPE_POS_TAG); // this was never implemented
      int id        = SS2File.readU16(dis);
      // predefined could be used to speed up importing; all predefined fields
      // could ship as default ASLLRP scheme, if built-in...
      boolean predefined = SS2File.readBoolean(dis); // discard??

      boolean visible    = SS2File.readBoolean(dis); 
      int red     = SS2File.readU16(dis) & 0x00FF;
      int green   = SS2File.readU16(dis) & 0x00FF;
      int blue    = SS2File.readU16(dis) & 0x00FF;
      Color color = new Color(red, green, blue);
      String name = SS2File.readPascalString(dis);
      String label = SS2File.readPascalString(dis);
      String prefix = "";

      // SHOULD remove this -- may be incorrect assumption
      if ((group == GROUP_UPPER && type != TYPE_NON_MANUAL) ||
          (group == GROUP_FIXED && type != TYPE_GLOSS) || 
          (group == GROUP_LOWER && type != TYPE_FREE_TEXT)) {
          Log.error("Field group and type don't match: "
            +name+" group "+group+" type "+type);
      }
      
      switch (group) {
        case GROUP_FIXED:
          id += GROUP_FIXED_ID_OFFSET;
          break;
        case GROUP_LOWER:
          id += GROUP_LOWER_ID_OFFSET;
          break;
      }
      
      int numValues = 0;
      if (type == TYPE_NON_MANUAL) { 
        prefix = SS2File.readPascalString(dis);
        numValues = SS2File.readU16(dis);
        int nextValueID = SS2File.readU16(dis); // discard
      }
    
      Vector vector = new Vector(numValues);
      int legacyOnsetID = -1, legacyOffsetID = -1;
      for (int v=0; v<numValues; v++) { 
        
        int valueID = SS2File.readU16(dis);
        String valueName = SS2File.readPascalString(dis);
        String valueLabel = SS2File.readPascalString(dis);
        
        if (valueLabel.equals("s")) {
          legacyOnsetID = valueID;
        } else if (valueLabel.equals("e")) {
          legacyOffsetID = valueID;
        } else {
          SS2Value value = new SS2Value(valueName, valueLabel, valueID);
          vector.addElement(value);
        }    
      } // end values
      
      SS2Value[] values = new SS2Value[vector.size()];
      vector.copyInto(values);
      
      SS2Field field = new SS2Field(
        name, label, prefix, values, 
        group, type, id, visible, color, legacyOnsetID, legacyOffsetID
      );
      fieldSpec.fields[i] = field;
    } // end fields
    
    return fieldSpec;
  } // end parseFieldSpec()
  
 
  public SS2Field getField(int id) {
    for (int i=0; i<fields.length; i++) {
      if (fields[i].id == id) return fields[i];
    }
    return null;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String nl = System.getProperty("line.separator");
    for(int i=0;i<fields.length;i++) {
      buffer.append(fields[i].toString());
      buffer.append(nl);
    }
    
    return buffer.toString();
  }
  
  // should make defensive copy if performance isn't hit too badly
  public SS2Field[] getFields() { return fields; }
  
    public Object toXMLNode() {
      Element element = new Element("CODING-SCHEME");
      
      if (fields != null) {
        for (int i=0; i<fields.length; i++) {
          element.addContent((Element)fields[i].toXMLNode());
        }
      }
      
      return element;
    }
  
    public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    Element element = (Element) xmlNode;
    
    List fieldElements = element.getChildren();
    int numFields = fieldElements.size();
    fields = new SS2Field[numFields];
    
    for (int i=0;i<numFields;i++) {
      Element fieldEl = (Element) fieldElements.get(i);
      fields[i] = (SS2Field) SS2Field.DUMMY.fromXMLNode(fieldEl);
    }
    return this; 
  }
  
}
