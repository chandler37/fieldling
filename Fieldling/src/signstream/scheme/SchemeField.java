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

package signstream.scheme;

import signstream.exception.*;

public interface SchemeField 
{
  // SHOULD add prefix, constraint (,??) bean properties
  
  static String CONSTRAINT_VALUE_SET = "value set";
  static String CONSTRAINT_FREE_TEXT = "free text";
  
  public int getID();
  public CodingScheme getCodingScheme();
  public String getName();
  public String getLabel();
  public String getCategory();
  public String getNotes();
  public String getDataType();
  public String getConstraint();
  public String getTimeAlignment();
  public java.awt.Color getColor();
  
  public SchemeValue addEquivalentSchemeValue(SchemeValue sv);
  public java.util.List getSchemeValues();
  public SchemeValue getSchemeValue(int valueID);
  public SchemeValue getSchemeValue(String valueName);

  public void addProperty(String name, String value);
  public java.util.Map getProperties();
  public String getProperty(String propertyName); 
  public void removeProperty(String propertyName);
  
  //// DESTRUCTIVE METHODS
  
  public void setName(String name);
  public void setID(int id);
  public void setLabel(String label);
  public void setCategory(String cat);
  public void setNotes(String notes);
  public void setDataType(String type);
  public void setConstraint(String constraint);
  public void setTimeAlignment(String timing); 
  public void setColor(java.awt.Color color);

  public void removeSchemeValue(String valueName);
  // public void removeSchemeValue(SchemeValue sv);

}

