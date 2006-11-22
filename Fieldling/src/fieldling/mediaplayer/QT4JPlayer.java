/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Edward Garrett, Michel Jacobson, Travis McCauley
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ***** END LICENSE BLOCK ***** */

package fieldling.mediaplayer;

import java.awt.*;
import java.net.*;
import java.io.*;
import quicktime.*;
import quicktime.qd.QDRect;
import quicktime.app.view.*;
import quicktime.app.display.*;
import quicktime.app.players.*;
//import quicktime.app.image.*; //required for the QT4JAVA test
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.DataRef;
import quicktime.io.*;
import quicktime.app.time.*;
import quicktime.std.clocks.*;

public class QT4JPlayer extends PanelPlayer {
	private static boolean hasQTSessionBeenOpened = false;
	private static Thread shutdownHook;
	private StopAtTimeCallBack 			theStopper = null;
	private ScrubberMovedCallBack               theJumper = null;
	private PlayingNotPlayingCallBack               theRater = null;
	private Movie			movie;
    
    public Component       movieViewComp;
	
    private MovieController		controller = null;
	private Container		parent = null;
	private static int	numberOfPlayersOpen = 0;
	
	//constructor
	public static void checkQTSession() throws QTException {
		if (!hasQTSessionBeenOpened) {
			QTSession.open();
			shutdownHook = new Thread() {
				public void run() {
					QTSession.close();
				}
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			hasQTSessionBeenOpened = true;
		}
	}
    //cribbed from Adamson 2005, p. 45
    /*public Dimension getPreferredSize() {
        if (controller == null)
            return new Dimension(0,0);
        try {
            QDRect contRect = controller.getBounds();
            Dimension compDim = movieViewComp.getPreferredSize();
            if (contRect.getHeight() > compDim.height) {
                return new Dimension(contRect.getWidth() + getInsets().left + getInsets().right, contRect.getHeight() + getInsets().top + getInsets().bottom);
            } else {
                return new Dimension(compDim.width + getInsets().left + getInsets().right, compDim.height + getInsets().top + getInsets().bottom);
            }
        } catch (QTException qte) {
            return new Dimension(0,0);
        }
        
    }*/
	public QT4JPlayer(Container cont, MovieController mc) {
        //super(new FlowLayout());
        super(new BorderLayout());
		try {
			checkQTSession();
		} catch (QTException qte) {
			qte.printStackTrace();
		}
		parent = cont;
		try {
            controller = mc;
            movie = controller.getMovie();
			QTComponent qtComp = QTFactory.makeQTComponent(controller);
            movieViewComp = qtComp.asComponent(); 
			this.add(movieViewComp);
			/*try {
				this.mediaURL = mediaFile.toURL();
			} catch (MalformedURLException murle) {
				murle.printStackTrace();
			}*/
			//LOGGINGSystem.out.println("loadMovie:"+mediaURL.toString());
			numberOfPlayersOpen++;
			theJumper = new ScrubberMovedCallBack(movie.getTimeBase());
			theRater = new PlayingNotPlayingCallBack(movie.getTimeBase(), 0, StdQTConstants.triggerRateChange);
		} catch(QTException qte) {
			//LOGGINGSystem.out.println("loadMovie failed");
			qte.printStackTrace();
		}        
    }
	public QT4JPlayer(Container cont, URL mediaURL) {
		//super(new FlowLayout());
        super(new BorderLayout());
		try {
			checkQTSession();
		} catch (QTException qte) {
			qte.printStackTrace();
		}
		parent = cont;
		try {
			loadMovie(mediaURL);
		} catch (PanelPlayerException smpe) {
			smpe.printStackTrace();
		}
	}
	public QT4JPlayer(Container cont, File mediaFile) {
		//super(new FlowLayout());
        super(new BorderLayout());
		try {
			checkQTSession();
		} catch (QTException qte) {
			qte.printStackTrace();
		}
		parent = cont;
		try {
			loadMovie(mediaFile);
		} catch (PanelPlayerException smpe) {
			smpe.printStackTrace();
		}
	}
	public QT4JPlayer() {
		//super(new FlowLayout());
        super(new BorderLayout());
		try {
			checkQTSession();
		} catch (QTException qte) {
			qte.printStackTrace();
		}
		//super( new GridLayout() );
	}
	
	//destructor
	public void destroy() {
		if (theJumper != null)
			theJumper.cancelAndCleanup();
		if (theRater != null)
			theRater.cancelAndCleanup();
		if (theStopper != null)
			theStopper.cancelAndCleanup();
		cancelAnnotationTimer();
		removeAllAnnotationPlayers();
		removeAll();
		this.mediaURL = null;
		//LOGGINGSystem.out.println("Clean up performed.");
		numberOfPlayersOpen--;
	}

	//accessors
	public String getIdentifyingName() {
		return "QuicktimeforJava";
	}
	public void setParentContainer(Container c) {
		parent = c;
	}


	//contract methods - initialize
	public void displayBorders(boolean borders) throws PanelPlayerException {
	}
	public void displayController(boolean controller) throws PanelPlayerException {
	}

	public void loadMovie(File mediaFile) throws PanelPlayerException {
		try {
			QTFile qtFile = new QTFile(mediaFile);
			OpenMovieFile omFile = OpenMovieFile.asRead(qtFile);
			movie = Movie.fromFile(omFile);
			controller = new MovieController(movie);
			QTComponent qtComp = QTFactory.makeQTComponent(controller);
			this.add(qtComp.asComponent());
			try {
				this.mediaURL = mediaFile.toURL();
			} catch (MalformedURLException murle) {
				murle.printStackTrace();
			}
			//LOGGINGSystem.out.println("loadMovie:"+mediaURL.toString());
			numberOfPlayersOpen++;
			theJumper = new ScrubberMovedCallBack(movie.getTimeBase());
			theRater = new PlayingNotPlayingCallBack(movie.getTimeBase(), 0, StdQTConstants.triggerRateChange);
		} catch(QTException qte) {
			//LOGGINGSystem.out.println("loadMovie failed");
			qte.printStackTrace();
		}
	}
	public void loadMovie(URL mediaURL) throws PanelPlayerException {
		try {
			String url = mediaURL.toString();
			DataRef dr = new DataRef(url);
			movie = Movie.fromDataRef(dr, StdQTConstants.newMovieActive);
			controller = new MovieController(movie);
			QTComponent qtComp = QTFactory.makeQTComponent(controller);
			this.add(qtComp.asComponent());
			this.mediaURL = mediaURL;
			//LOGGINGSystem.out.println("loadMovie:"+mediaURL.toString());
			numberOfPlayersOpen++;
			theJumper = new ScrubberMovedCallBack(movie.getTimeBase());
			theRater = new PlayingNotPlayingCallBack(movie.getTimeBase(), 0, StdQTConstants.triggerRateChange);
		} catch(QTException qte) {
			//LOGGINGSystem.out.println("loadMovie failed");
			qte.printStackTrace();
		}
	}

	//contract methods - control media
	public void cmd_playOn() throws PanelPlayerException {
		try {
			controller.play(1);
		} catch(QTException qte) {
			qte.printStackTrace();
		}
	}
	public void cmd_playSegment(Long startTime, Long stopTime) throws PanelPlayerException {
		try {
			cmd_stop();
			//int myScale = movie.getTimeScale();
			int myScale = controller.getTimeScale();
            long t1 = startTime.longValue() * myScale / 1000;
			controller.goToTime(new TimeRecord(myScale, t1));
            //movie.setTime(new TimeRecord(myScale, (int)t1));
			if (stopTime != null) {
				long t2 = stopTime.longValue() * myScale / 1000;
				theStopper = new StopAtTimeCallBack((int)t2);
			}
			cmd_playOn();
		}
		catch (PanelPlayerException smpe) {
		}
		catch (StdQTException sqte) {
		}
		catch (QTException qte) {
		}
	}
	public void cmd_stop() throws PanelPlayerException {
		try {
			controller.play(0);
		} catch(QTException qte) {
			qte.printStackTrace();
		}
	}

	//contract methods - media status
	public boolean isInitialized() {
		return true; //FIXME what should this do?
	}
	public boolean isPlaying() {
		try {
			if (controller.getPlayRate() > 0)
				return true;
		} catch (StdQTException stdqte) {
			stdqte.printStackTrace();
		}
		return false;
	}
	public long getCurrentTime() {
		try {
            return (controller.getCurrentTime() * 1000 / controller.getTimeScale());
			//long myScale = movie.getTimeScale();
            //long now = movie.getTime();
			//return (now*1000)/myScale;
		} catch (StdQTException stqte) {
			stqte.printStackTrace();
			return 0;
		} catch (QTException qte) {
			//LOGGINGSystem.out.println("getCurrentTimeErr");
			return 0;
		}
	}
	public long getEndTime() {
		try {
			long myScale = movie.getTimeScale();
			long duration = movie.getDuration();
			return duration * 1000 / myScale;
		} catch (StdQTException stqte) {
			stqte.printStackTrace();
			return 0;
		} catch (QTException qte) {
			//LOGGINGSystem.out.println("getCurrentTimeErr");
			return 0;
		}
	}
	public void setCurrentTime(long t) {
		try {
            controller.goToTime(new TimeRecord(1000, t));
			//int myScale = controller.getTimeScale();
            //long t1 = t * myScale / 1000;
            //controller.goToTime(new TimeRecord(myScale, t1));
			//movie.setTimeValue((int)t1);
		} catch (StdQTException stqte) {
			stqte.printStackTrace();
		} catch (QTException qte) {
			//LOGGINGSystem.out.println("getCurrentTimeErr");
		}
	}
	
	// inner classes
	class StopAtTimeCallBack extends quicktime.std.clocks.TimeCallBack {
		public StopAtTimeCallBack(int time) throws QTException {
			super(movie.getTimeBase(), movie.getTimeScale(), time, StdQTConstants.triggerTimeFwd);
			callMeWhen();
		}
		public void execute() {
			try {
				cmd_stop();     
			} catch (PanelPlayerException ppe) {
				ppe.printStackTrace();
			}
		}
	}
	class PlayingNotPlayingCallBack extends quicktime.std.clocks.RateCallBack {
		public PlayingNotPlayingCallBack(TimeBase tb, int scale, int flag) throws QTException {
			super(tb, scale, flag);
			callMeWhen();
		}
		public void execute() {
			try {
				//LOGGINGSystem.out.println("PlayingNotPlayingCallBack: " + String.valueOf(rateWhenCalled));
				/* rateWhenCalled has the following value:
					1: when the media starts playing
					0: when the media stops
				*/
				if (rateWhenCalled > 0) {
					launchAnnotationTimer();
				} else {
                    cueAnnotationTimer();
					if (theStopper != null) {
						theStopper.cancelAndCleanup();
						theStopper = null;
					}
				}
				callMeWhen();
			} catch (Exception e) {
				//LOGGINGSystem.out.println("PlayingNotPlayingCallBack err: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	class ScrubberMovedCallBack extends quicktime.std.clocks.TimeJumpCallBack {
		public ScrubberMovedCallBack(TimeBase tb) throws QTException {
			super(tb);
			callMeWhen();
		}
		public void execute() {
			try {
				//LOGGINGSystem.out.println("ScrubberMovedCallBack: " + String.valueOf(rateWhenCalled));
				if (rateWhenCalled > 0) {
                    cueAnnotationTimer();
					launchAnnotationTimer();
					if (theStopper != null) {
						theStopper.cancelAndCleanup();
						theStopper = null;
					}
				} else {             
                    cueAnnotationTimer();
                }
				callMeWhen();
			} catch (Exception e) {
				//LOGGINGSystem.out.println("ScrubberMovedCallBack err: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
