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
 * PlayerWindow.java
 *
 * Created on January 17, 2002, 2:46 PM
 */

package signstream.gui.player;

import java.io.File;
import java.io.IOException;
import java.awt.Dimension;


public interface PlayerWindow {
  
  void openMedia(File mediaFile, int timeOffset) throws IOException;
  void closeMedia() throws IOException;
  
  /** Should make the window visible, reopening the media stream if needed. */
  void show();
  /** Should make the window invisible, closing the media stream as appropriate. */
  void hide();
  /** Should move the window's upper left corner to the specified screen coordinate. */
  void setLocation(int screenX, int screenY);  
  /** Should return the window's upper-left corner as a screen coordinate. */
  java.awt.Point getLocation(); 
  /** Should set the window's upper-left corner, width, and height to the specified bounds. */
  void setBounds(int x, int y, int width, int height);
  /** Should return a Rectangle containing the window's current screen bounds. */
  java.awt.Rectangle getBounds();
  
  /** Should begin/resume playback at the component's currently cached playback rate. */
  void play();
  /** Should halt playback. Note that this should <i>not</i> reset the playback rate
   to 0.0 */
  void stop();
  /** Should set the player's time to 0 + any time offset as specified when the 
   * player was created. This is important, for example, if the current timeline
   * for a SS3 document begins somewhere other than the beginning of this player's
   * media file. */
  void rewind();
  /** Should return the current position in the media stream + any time offset. */
  int getCurrentTime();
  /** Should set the position in the media stream to <code>time</code> minus
   any time offset. */
  void setCurrentTime(int time);
  
  /** Should return the current playback state 
   *@see #setPlaybackRate(double)
   */
  double getPlaybackRate();
  /** Should set the player's playback rate, where 1.0 = normal, 0.0 = halted. Note 
   * that merely setting the rate should <i>not</i> change the current playback state;
   * the rate is simply cached and used when playback is initiated. */
  void setPlaybackRate(double rate);
   
  /** Should return the current time offset.
   * @see #setTimeOffset(int) */
  int getTimeOffset();
  /** Should set the current time offset, which is the time in the current timeline
   * corresponding to time 0.0 for the opened media stream. */
  void setTimeOffset(int offset);
  
  /** Optional method -- may be used to make this player the timing master for 
   *<code>manager</code>. This must of course also be implemented by the particular
   *{@link MediaManager} class, and creates a tight coupling between the two classes,
   *although in practice this should be fine as MediaManagers will be distributed
   *with specific compatible PlayerWindows. */
  void addTimingListener(PlayerManager manager);
  /** @see #addTimingListener(MediaManager) */
  void removeTimingListener(PlayerManager manager);
}

