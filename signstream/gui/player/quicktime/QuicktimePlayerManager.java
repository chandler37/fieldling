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

package signstream.gui.player.quicktime;

import signstream.gui.player.PlayerWindow;
import signstream.gui.player.PlayerManager;

import signstream.timing.TimeSlave;
import signstream.timing.TimeSlaveHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.awt.Rectangle;

import quicktime.app.display.DrawingListener;
import quicktime.app.display.QTDrawable;
import quicktime.app.QTFactory;
import quicktime.app.players.MoviePlayer;
import quicktime.QTException;
import quicktime.QTSession;


public class QuicktimePlayerManager
implements PlayerManager
, DrawingListener {
  static private int PLAYING_STATE = 1;
  static private int STOPPED_STATE = 2;
    
  protected Vector                  playerWindows = new Vector( 4 );
  // Vector                            slaves = new Vector( 1 );
  TimeSlaveHelper timeSlaveHelper;
  public void setTimeSlaveHelper(TimeSlaveHelper master) {
    timeSlaveHelper = master;
  }
  /**  The rate of playback when we are playing, where 1.0 == normal speed. */
  double                            playbackRate = 0.0;
  /** The current time in milliseconds. */
  protected int                     currentTime = 0;
  protected int                     state = STOPPED_STATE;
  
  PlayerWindow                      timingMaster = null;
  
  public QuicktimePlayerManager() {
    // Do gestalt check that QT is present and initialize QT
    try {
      QTSession.open();
    } catch ( QTException qte ) {
      qte.printStackTrace();
      try {
        QTSession.close();
      } catch (Exception e) { e.printStackTrace(); }
    }
  } // end constructor()
  
  
  public void finalize() {
    QTSession.close();
  }
  
  // called for each frame drawn by the one register Drawable, the QT MoviePlayer
  // instance held by PlayerWindow timingMaster
  public void drawingComplete(QTDrawable drawable ) {
    if (state == STOPPED_STATE) return;
    
    MoviePlayer player = ( MoviePlayer )drawable;
    try {
      /// is this as efficient as poossible?
      setCurrentTime((int) ((player.getTime() * 1000.0f) / player.getScale()));
    } catch ( QTException qte ) {
      qte.printStackTrace();
    }
  }// end drawingComplete()
  
  
  //// PlayerManager //////
  
  public PlayerWindow openMedia(String fileName, int timeOffset) throws IOException {
    return openMedia(new File(fileName), timeOffset);
  }
  
  public PlayerWindow openMedia(File file, int timeOffset) throws IOException {
    PlayerWindow player = new QuicktimePlayerWindow(this, file, 0);
    player.setPlaybackRate(getPlaybackRate());
    player.setCurrentTime(getCurrentTime()); // SHOULD account for time offset
    
    playerWindows.addElement(player);
    
    player.setLocation( (playerWindows.size()-1)*300,20 );
    return player;
  } // end openMedia(File)
  
  
  public void closeMedia(PlayerWindow player) throws IOException {
    synchronized(playerWindows) {
      playerWindows.removeElement(player);
    }
    
    synchronized(playerWindows) {
      if (playerWindows.size() > 0) {
        QuicktimePlayerWindow qtpw
        = (QuicktimePlayerWindow) playerWindows.firstElement();
        setTimingMaster(qtpw);
      } else {
        setTimingMaster(null);
        stop();
      }
    }
    ((QuicktimePlayerWindow)player).frame.dispose();
    player.closeMedia();
  } // end closeMedia()
  
  
  public void closeAllMedia() throws IOException {
    stop();
    for (int i=0, n=playerWindows.size(); i<n; i++) {
      closeMedia((PlayerWindow) playerWindows.firstElement());
    }
  }
  
  private void setTimingMaster(QuicktimePlayerWindow player) {
    if (timingMaster == player) return;
    if (timingMaster != null)
      timingMaster.removeTimingListener(this);
    if (player != null) {
      player.addTimingListener(this);
    }
    timingMaster = player;
  }
  
  public void printTimingMaster() {
    System.err.println(timingMaster);
  }
  
  
  ///// SynchedPlayer //////
  
  /**
   *  Redundant -- passes thru to {@link #timeChanged(int)}.
   *
   * @param  time  the new time in milliseconds.
   */
  public void setCurrentTime( int time ) {
    timeChanged(time);
  }// end setCurrentTime()
  
  /**
   * @return    the current time in milliseconds.
   */
  public int getCurrentTime() {
    return currentTime;
  }
  
  /**
   *  Sets the current playback rate for this component and all player windows.
   *
   * @param  rate  1.0 = normal speed, 0.0 = halted
   */
  public void setPlaybackRate( double rate ) {
    playbackRate = rate;
    Iterator it = playerWindows.iterator(); 
    while (it.hasNext()) {
      PlayerWindow player = (PlayerWindow) it.next();
      player.setPlaybackRate(rate);
    }
  }// end setPlaybackRate()
  
  /**
   * @return    the current playback rate, where 1.0 = normal, 0.0 = halted
   */
  public double getPlaybackRate() {
    return playbackRate;
  }
  
  /**
   *  Sets the current time to zero; the final API will need to deal with time
   *  offset. Note that this method pays no attention to playback state.
   */
  public void rewind() {
    setCurrentTime( 0 );
  }
  
  /**
   *  Sets all player windows playing with the current playback rate. Sets state
   *  to {@link #PLAYING_STATE} and calls {@link #fireStateChanged(int) }
   */
  public void play() {
    if (state == PLAYING_STATE || playerWindows.size() == 0) return;
    
    state = PLAYING_STATE;
    
    if (timingMaster == null) {
      setTimingMaster((QuicktimePlayerWindow)playerWindows.firstElement());
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final Iterator it = playerWindows.iterator();
        while (it.hasNext()) {
          PlayerWindow player = (PlayerWindow) it.next();
          player.play();
        }
      }
    });
    // fireStateChanged( PLAYING_STATE );
  }// end play()
  
  /**
   *  Stops playback of all player windows. Sets state to {@link #STOPPED_STATE} and
   *  calls {@link #fireStateChanged(int) }
   */
  public void stop() {
    if (state == STOPPED_STATE) return;
    
    Iterator it = playerWindows.iterator();
    while (it.hasNext()) {
      PlayerWindow player = (PlayerWindow) it.next();
      player.stop();
    }
    state = STOPPED_STATE;
    // fireStateChanged( STOPPED_STATE );
    timeSlaveHelper.broadcastTimeChanged(currentTime, this);
  }// end stop()
  
  
  ///// TimeSlave //////
  
  public void timeChanged( int time ) {
    if ( time < 0 || time == currentTime ) {
      return;
    }
    currentTime = time;
    
    // SHOULD figure out what threading makes sense here --
    // assuming QTJava passes control from AWT event thread to
    // native threads "appropriately"
    
    // javax.swing.SwingUtilities.invokeLater(new Runnable() {
    //   public void run() {
   //     if ( state != PLAYING_STATE ) {
          for (Enumeration enumeration = playerWindows.elements(); enumeration.hasMoreElements(); ) {
            PlayerWindow player = (PlayerWindow) enumeration.nextElement();
            player.setCurrentTime(currentTime);
          }
   //     }
    //   }
    // });
    
    timeSlaveHelper.broadcastTimeChanged(time, this);
  }// end timeChanged()
  
  /**
   *  Lava code
   */
  public void stateChanged( int state ) {
    if ( state == STOPPED_STATE ) {
      stop();
    } else if ( state == PLAYING_STATE ) {
      play();
    }
  }// end stateChanged()
  
  
  public void layout(Rectangle suggestedBounds) {
    // reposition frames as well as we can inside suggestedBounds
  }
  
  
  
}


