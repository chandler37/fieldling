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
 * QuicktimePlayerWindow.java
 *
 * Created on January 17, 2002, 3:10 PM
 */

package signstream.gui.player.quicktime;

import signstream.gui.player.PlayerManager;
import signstream.gui.player.PlayerWindow;
import signstream.exception.Assert;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import quicktime.std.movies.Movie;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.QTException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.app.players.MoviePlayer;
import quicktime.app.display.QTCanvas;


public class QuicktimePlayerWindow implements PlayerWindow {
  PlayerManager theManager = null; 
  File mediaFile = null;
  JFrame frame = null;
  MoviePlayer moviePlayer = null;
   
  double playbackRate = 0.0;
  int timeOffset = 0;
  
  /** Creates new QuicktimePlayerWindow */
  public QuicktimePlayerWindow(PlayerManager manager, File mediaFile, int timeOffset) {
    theManager = manager;
    openMedia(mediaFile, timeOffset);
  }
  
  
  public void addTimingListener(PlayerManager manager) {
    moviePlayer.addDrawingListener((QuicktimePlayerManager) manager);
  }
  
  public void removeTimingListener(PlayerManager manager) {
    try {
      moviePlayer.removeDrawingListener((QuicktimePlayerManager) manager);
    } catch (NullPointerException npe) { System.err.println(npe.getMessage()); }
  }
  
  public void openMedia(File mediaFile, int timeOffset) {
    this.mediaFile = mediaFile;
    this.timeOffset = timeOffset;
    try {
      QTFile qtFile = new QTFile( mediaFile );
      Assert.check(qtFile.exists(), mediaFile.getAbsolutePath()+ "doesn't exist");
      /// open as a movie
      OpenMovieFile omf = OpenMovieFile.asRead( qtFile );
      Movie movie = Movie.fromFile( omf );
      // make all movies loop for quicker testing
      movie.getTimeBase().setFlags( StdQTConstants.loopTimeBase );
      
      /// construct a movie player
      moviePlayer = new MoviePlayer( movie );
      moviePlayer.setRate( 0 );
      moviePlayer.setTime( 0 );
      
      /// construct a window
      QTCanvas qtCanvas = new QTCanvas( QTCanvas.kAspectResize, 0.5f, 0.5f );
      qtCanvas.setClient( moviePlayer, true );
      frame = new JFrame( mediaFile.getName() );
      frame.setSize( 300, 300 );
      
      frame.getContentPane().add( qtCanvas );
      frame.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent we) { 
            try {
              theManager.closeMedia(QuicktimePlayerWindow.this);
            } catch (IOException ioe) { ioe.printStackTrace(); }
          }
        }
      );
      frame.show();
    } catch ( QTException qte ) {
      qte.printStackTrace();
    }
  } // end openMedia()
  
  public void closeMedia() {
    stop();
    try {
       moviePlayer.getMovie().disposeQTObject();
    } catch (QTException qte) {qte.printStackTrace(); }
  }
  
  
  public void play() {
    int scale = 0;
    int time = getCurrentTime();
    try {
      scale = moviePlayer.getScale(); // ticks per second
      moviePlayer.setTime(  scale * time  );
      moviePlayer.setRate( (float) getPlaybackRate() );
    } catch ( QTException qte ) {
      qte.printStackTrace();
    }
  } // end play()
  
  public void stop() {
    try {
      moviePlayer.setRate( 0 );
    } catch ( QTException qte ) {
      qte.printStackTrace();
    }
  } // end stop()
  
  public void rewind() {
    setCurrentTime(0);
  } // end rewind()
  
  /** Should (but doesn't currently) return the current time IN MILLISECONDS. 
   Is actually returning ???
   */
  public int getCurrentTime() {
    int scale = 0, t = 0;
    try {
      scale = moviePlayer.getScale(); // should cache this?
      t = moviePlayer.getTime();
    } catch (QTException qte) {
      qte.printStackTrace();
    }
    return 0;
    //return (int) signstream.io.ss2.MediaRef
    //  .convertValueToMilliseconds(scale, t) + timeOffset;
  }
  
  /** Should (but doesn't currently) treat time as milliseconds and set the 
   QT movie's time accordingly. */ 
  public void setCurrentTime(int time) {
    time -= timeOffset;
    try {
      int scale = moviePlayer.getScale();// ticks per second
      moviePlayer.setTime(  scale * time / 1000 ); // 600u/s * 3000ms / 1000ms/s = 1800u 
    } catch ( QTException qte ) {
      qte.printStackTrace();
    }
  } // end setCurrentTime()
  
  public double getPlaybackRate() {
    return playbackRate;
  }
  public void  setPlaybackRate(double rate) {
    playbackRate = rate;
    try {
      if (moviePlayer.getRate() != 0.0f)
        moviePlayer.setRate( (float) getPlaybackRate() );
    } catch (StdQTException e) { e.printStackTrace(); }
  }
  
  public void show() {
    frame.show();
  }
  public void hide() {
    frame.hide();
    // shouldn't allow playback when hidden, and should not be responsible for
    // timing callbacks...
  }
  
  public void setLocation(int screenX, int screenY) {
    frame.setLocation(screenX, screenY);
  }  
  public java.awt.Point getLocation() {
    return frame.getLocation();
  }
  
  public void setBounds(int x, int y, int width, int height) {
    frame.setBounds(x, y, width, height);
  }
  public java.awt.Rectangle getBounds() {
    return frame.getBounds();
  }  
  
  public void setTimeOffset(int offset) {
    timeOffset = offset;
  }
  
  public int getTimeOffset() {
    return timeOffset;
  }
  
}
