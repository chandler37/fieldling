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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class CodingSchemeAdapter implements CodingSchemeListener {

  public void propertyChange(PropertyChangeEvent event) {

    String propertyName = event.getPropertyName();
    Object source = event.getSource();
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
    
    if (source instanceof CodingScheme) 
    {
      if (propertyName == FIELD_PROPERTY) {

        if (oldValue == null && newValue != null) 
          fieldAdded((CodingScheme)source, (SchemeField)newValue);
        else if (oldValue != null && newValue == null) 
          fieldRemoved((CodingScheme)source, (SchemeField)oldValue);
        else 
          throw new RuntimeException("OOPS");
      } 
      else if (propertyName == PROPERTY_PROPERTY) {
        schemePropertyChanged(event);
      }
      else {
        schemeAttributeChanged(event);
      }
    } 
    else if (source instanceof SchemeField) 
    {
      if (propertyName == VALUE_PROPERTY) {

        if (oldValue == null && newValue != null)
          valueAdded((SchemeField)source, (SchemeValue)newValue);
        else if (oldValue != null && newValue == null)
          valueRemoved((SchemeField)source, (SchemeValue)oldValue);
        else
          throw new RuntimeException("D'oh!");
      } 
      else if (propertyName == PROPERTY_PROPERTY) {
        fieldPropertyChanged(event);
      }
      else {
        fieldAttributeChanged(event);
      }
    } 
    else if (source instanceof SchemeValue) {

      if (propertyName == PROPERTY_PROPERTY)
        valuePropertyChanged(event);
      else 
        valueAttributeChanged(event);
    } 
    else {
      throw new RuntimeException("BAD PROGRAMMER! BAD!");
    }
  } // end propertyChange()
  
  public void schemeAttributeChanged(PropertyChangeEvent event) {}
  public void schemePropertyChanged(PropertyChangeEvent event) {}
  public void schemeInternalPropertyChanged(PropertyChangeEvent event) {}
  
  public void fieldAdded(CodingScheme addedTo, SchemeField added) {}
  public void fieldRemoved(CodingScheme removedFrom, SchemeField removed) {}

  public void fieldAttributeChanged(PropertyChangeEvent event) {}
  public void fieldPropertyChanged(PropertyChangeEvent event) {}
  public void fieldInternalPropertyChanged(PropertyChangeEvent event) {}
  
  public void valueAdded(SchemeField addedTo, SchemeValue added) {}
  public void valueRemoved(SchemeField removedFrom, SchemeValue removed) {}

  public void valueAttributeChanged(PropertyChangeEvent event) {}
  public void valuePropertyChanged(PropertyChangeEvent event) {}
  public void valueInternalPropertyChanged(PropertyChangeEvent event) {}
}
