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

package signstream.gui.player;

import signstream.timing.TimeSlave;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

/****************************************************************************
This component is responsible for opening and
closing media files or streams and managing individual playe windows for each
media stream. 
 Each SignStream 3 document (SS is a multiple document application) has exactly
one instance of a MediaManager. 
 <P>Note that since a MediaManager can manage
 several media files at once, which themselves may have differing frame/sample 
 rates, it is up to this component to determine how best to handle its own internal
 timing. 

@see TimeSlave
@see TimeSlaveHelper
****************************************************************************/
public interface PlayerManager extends TimeSlave {
  
  /** Should create a new PlayerWindow instance for the given media file, of an 
   * appropriate subclass for the given media. It is undecided how different
   * PlayerWindow subclasses should be instantiated based on file type. 
   * @param timeOffset  The logical time within the current timeline which is the start time of the given media file
   * @return A reference to the created PlayerWindow
   * @see #closeMedia(PlayerWindow)
   */
  PlayerWindow openMedia(File mediaFile, int timeOffset) throws IOException;
  /** A convenience method which should call {@link #openMedia(File, int)} */
  PlayerWindow openMedia(String fileName, int timeOffset) throws IOException; 
  /** Should close the player window and free all resources associated with it. */
  void closeMedia(PlayerWindow player) throws IOException; 
  void closeAllMedia() throws IOException;
  /** Should attempt to layout all visible player windows within 
  the specified area of the screen. */
  void layout(Rectangle suggestedBounds);
  
} // end interface
