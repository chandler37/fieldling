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
 * Track.java
 */

package signstream.transcript;

import signstream.scheme.*;
import signstream.io.XMLNodeSerializable;
import org.jdom.Element;
import signstream.utility.XMLConstant;

import java.util.*;

public class Track implements XMLNodeSerializable {
  
  String name;
  //int group;
  //int type;
  SchemeField field;
  /** Temporary until SS3 CodingSchemes are re-integrated;
   for now we're using SS2FieldSpec classes. */
  // SS2Field ss2field;
  boolean visible = true;
  /** Used when reading in from an XML node, since the 
   calling code must tell the track which scheme to use. */
  CodingScheme scheme = null;
  
  /// should be linked list
  public Vector annotations = new Vector();
  
  
  
  public Track() {}
  public Track(Object xmlNode, CodingScheme scheme) {
    this.scheme = scheme;
    fromXMLNode(xmlNode);
    this.scheme = null;
  }
  
  public void setName(String n) {
    if (n == null || n.equals("")) name = "EMPTY NAME";
    else name = n;
  }
  public String getName() {
    return name;
  }
  
    /* 
     This stuff pertains to SchemeFields in SignStream 3:
     public void setGroup(int g) {
      group = g;
    }
    public int getGroup() {
      return group;
    }
     
    public void setType(int t) {
      type = t;
    }
    public int getType() {
      return type;
    } */
  
  public Annotation[] getAnnotations() {
    Annotation[] array = new Annotation[annotations.size()];
    annotations.copyInto(array);
    return array;
  }
  public void addAnnotation(Annotation anno) {
    annotations.addElement(anno);
  }
  
  public void setField(SchemeField field) {
    this.field = field;
  }
  public SchemeField getField() {
    return field;
  }
  
  
  public String toString() {
    return name;
  }
  
  public Object toXMLNode() {
    Element element = new Element(XMLConstant.TRACK);
    element.setAttribute(XMLConstant.NAME, name);
    element.setAttribute("FID", ""+field.getID());
    if (!visible) element.setAttribute(XMLConstant.VISIBLE, ""+visible);
    // should include type, possibly order
    
    for (int i=0, n=annotations.size();i<n; i++) {
      Element annoEl = new Element(XMLConstant.ANNOTATION);
      Annotation annotation = (Annotation)annotations.elementAt(i);
      annoEl.setAttribute(XMLConstant.S, ""+annotation.startTime);
      annoEl.setAttribute(XMLConstant.E, ""+annotation.endTime);
      
      Object value = annotation.value;
      if (annotation.value instanceof SchemeValue)
        annoEl.setAttribute("VID", ""+((SchemeValue)value).getID());
      else
        annoEl.addContent(value.toString());
      
      element.addContent(annoEl);
    }
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    Element element = (Element) xmlNode;
    name = element.getAttributeValue(XMLConstant.NAME);
    field = scheme.getSchemeField(
      Integer.parseInt(element.getAttributeValue("FID")));
    String visText = element.getAttributeValue(XMLConstant.VISIBLE);
    visible = visText == null; // only present in XML when visibility=false
    annotations = new Vector();
    
    List annoElements = element.getChildren(XMLConstant.ANNOTATION);
    
    for (int i=0, n=annoElements.size();i<n;i++) {
      Element annoEl = (Element) annoElements.get(i);
      
      int start = Integer.parseInt(annoEl.getAttributeValue(XMLConstant.S));
      int end = Integer.parseInt(annoEl.getAttributeValue(XMLConstant.E));
      Object value = null; 
      String vidText = annoEl.getAttributeValue("VID");
      if (vidText == null || vidText.equals(""))
        value = annoEl.getText();
      else 
        value = field.getSchemeValue(Integer.parseInt(vidText));
      Annotation annotation = new Annotation(value, start, end);
      addAnnotation(annotation);
    }
    return this;
  }
  
}
