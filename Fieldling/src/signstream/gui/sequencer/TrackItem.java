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
 * TrackItem.java
 *
 * Created on January 13, 2003, 4:12 PM
 */

package signstream.gui.sequencer;

public class TrackItem 
implements Comparable {

  int startTime;
  int endTime;
  Object value;

    public TrackItem(int s, int e, Object v) {
      startTime = s;
      endTime = e;
      value = v;
    }
    
    public TrackItem() {}
  
    /** Getter for property startTime.
     * @return Value of property startTime.
     */
    public int getStartTime()
    {
      return startTime;
    }
    
    /** Setter for property startTime.
     * @param startTime New value of property startTime.
     */
    public void setStartTime(int startTime)
    {
      this.startTime = startTime;
    }
    
    /** Getter for property endTime.
     * @return Value of property endTime.
     */
    public int getEndTime()
    {
      return endTime;
    }
    
    /** Setter for property endTime.
     * @param endTime New value of property endTime.
     */
    public void setEndTime(int endTime)
    {
      this.endTime = endTime;
    }
    
    public int getDuration() {
      return endTime - startTime;
    }
    
    /** Getter for property value.
     * @return Value of property value.
     */
    public java.lang.Object getValue()
    {
      return value;
    }
    
    /** Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(java.lang.Object value)
    {
      this.value = value;
    }
    
    public int compareTo(Object obj)
    {
      TrackItem item = (TrackItem) obj;
      if (this == item) return 0;
      if (startTime < item.startTime) return -1;
      if (startTime > item.startTime) return 1;
      if (endTime < item.endTime) return -1;
      if (endTime > item.endTime) return 1;
      return 0;
    }
    
}
