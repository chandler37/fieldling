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
import quicktime.*;
import quicktime.app.*;
import quicktime.app.display.*;
import quicktime.app.players.*;
//import quicktime.app.image.*; //required for the QT4JAVA test
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.DataRef;
import quicktime.io.*;
import quicktime.app.time.*;
import quicktime.std.clocks.*;
import quicktime.app.QTFactory;

public class QT4JPlayer extends PanelPlayer {
	private myJumpCallBack               theJumper = null;
	private myRateCallBack               theRater = null;
	private URL                          mediaUrl = null;
	private TimeBase                     myMoviesTimeBase;
	private QTCanvas                     canvas;
	private QTPlayer                     player;
	private Container		parent = null;
	private static int	numberOfPlayersOpen = 0;
	
//constructor
	public QT4JPlayer(Container cont, URL mediaURL) {
		super(new BorderLayout());
		//super( new GridLayout() );
		parent = cont;
		try {
			loadMovie(mediaURL);
		} catch (PanelPlayerException smpe) {
			smpe.printStackTrace();
		}
	}
	public QT4JPlayer() {
		super(new BorderLayout());
		//super( new GridLayout() );
	}
//destructor
	public void destroy() {
		if (theJumper != null)
			theJumper.cancelAndCleanup();
		if(theRater != null)
			theRater.cancelAndCleanup();
		cancelAnnotationTimer();
		removeAllAnnotationPlayers();
		if (numberOfPlayersOpen == 1) QTSession.close();
		removeAll();
		mediaUrl = null;
		//LOGGINGSystem.out.println("Clean up performed.");
		numberOfPlayersOpen--;
	}

//accessors
	public String getIdentifyingName() {
		return "Quicktime for Java";
	}
	public URL getMediaURL() {
		return mediaUrl;
	}
	public void setParentContainer(Container c) {
		parent = c;
	}
	public void setPlayer(QTDrawable player) {
		this.player = (QTPlayer)player;
	}
	public QTPlayer getPlayer() {
		return player;
	}
	/*
	public void setCanvas(QTCanvas canvas) {
		this.canvas = canvas;
	}
	public QTCanvas getCanvas() {
		return canvas;
	}
	*/


//contract methods - initialize
	public void displayBorders(boolean borders) throws PanelPlayerException {
	}
	public void displayController(boolean controller) throws PanelPlayerException {
	}

	public void loadMovie(URL mediaURL) throws PanelPlayerException {
		//Initialize a QT session and add a test image

		//These three try/catch blocks come from PlayMovie.java copyright
		// Apple Co. I'm using it to test that QT and QT4Java exist
		
		//LOGGINGSystem.out.println("Trying to load: "+ mediaURL.toString());
		
		try {
			if (QTSession.isInitialized() == false) { //{
				QTSession.open();
				//LOGGINGSystem.out.println("initializing");
				
				/*
				try {
					setCanvas( new QTCanvas(QTCanvas.kInitialSize, 0.5F, 0.5F) );
					this.add( getCanvas() );
					getCanvas().setClient(ImageDrawer.getQTLogo(), true);
				} catch(QTException qte) {
					qte.printStackTrace();
				}*/
			}
		} catch (NoClassDefFoundError er) {
			//LOGGINGSystem.out.println("Can't Find QTJava classes. Check install and try again.");
			er.printStackTrace();
		} catch (SecurityException se) {
			// this is thrown by MRJ trying to find QTSession class
			//LOGGINGSystem.out.println("Can't Find QTJava classes. Check install and try again.");
			se.printStackTrace();
		} catch (Exception e) {
			// do a dynamic test for QTException
			//so the QTException class is not loaded unless
			// an unknown exception is thrown by the runtime
			
			if (e instanceof ClassNotFoundException || e instanceof java.io.FileNotFoundException) {
				//LOGGINGSystem.out.println("Can't Find QTJava classes. Check install and try again.");
			} else if (e instanceof QTException) {
				//LOGGINGSystem.out.println("Problem with QuickTime install.");
			}
			e.printStackTrace();
		}

		try {
			//LOGGINGSystem.out.println("Removing canvas, etc.");

			canvas = new QTCanvas(QTCanvas.kAspectResize, 0.5F, 0.5F);
			setPlayer(QTFactory.makeDrawable(mediaURL.toString()));
			canvas.setClient(getPlayer(), true);
			this.add("Center", canvas);
			mediaUrl = mediaURL;
			
			/*
			this.remove( getCanvas() );
			setPlayer(QTFactory.makeDrawable(mediaURL.toString()));
			getCanvas().setClient( getPlayer(), true );
			this.add( getCanvas() );
			*/
			
			//LOGGINGSystem.out.println("loadMovie:"+mediaURL.toString());
			numberOfPlayersOpen++;
			
			myMoviesTimeBase = getPlayer().getTimeBase();
			theJumper = new myJumpCallBack(myMoviesTimeBase);
			theJumper.callMeWhen();
			theRater = new myRateCallBack(myMoviesTimeBase, 0, StdQTConstants.triggerRateChange);
			theRater.callMeWhen();
			//Timer timer = new Timer(10,1,new Tickler(), getPlayer().getMovieController().getMovie());
			//timer.setActive(true);
		} catch(QTException qte) {
			//LOGGINGSystem.out.println("loadMovie failed");
			qte.printStackTrace();
		}
	}

//contract methods - control media
	public void cmd_playOn() throws PanelPlayerException {
		try {
			getPlayer().setRate(1.0F);
		} catch(QTException qte) {
			qte.printStackTrace();
		}
	}
	//public void cmd_playSegment(Integer startTime, Integer stopTime) throws PanelPlayerException {
	public void cmd_playSegment(Long startTime, Long stopTime) throws PanelPlayerException {
		try {
			cmd_stop();
			
			int myScale = getPlayer().getScale();
			long t1 = startTime.longValue() * myScale / 1000;
			
			getPlayer().setTime((int)t1);
			//getPlayer().setTime( (startTime.intValue() * myScale) / 1000 );

			if (stopTime == null) {
				myMoviesTimeBase.setStopTime(new TimeRecord(myScale, player.getDuration()));
				//LOGGING//LOGGINGSystem.out.println("startTime:"+(startTime.intValue()*myScale)/1000+" stopTime: to the End" );
			} else {
				long t2 = stopTime.longValue() * myScale / 1000;
				myMoviesTimeBase.setStopTime(new TimeRecord(myScale, (int)t2));
				//LOGGING//LOGGINGSystem.out.println("startTime:"+(startTime.intValue()*myScale)/1000+" stopTime:"+(stopTime.intValue()*myScale)/1000 );
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
			//it seems that if theRater is not cancelled and recalled then a threading problem arises
			theRater.cancelAndCleanup();
			theJumper.cancelAndCleanup();
			getPlayer().setRate(0.0F);
			int myScale = getPlayer().getScale();
			myMoviesTimeBase.setStopTime(new TimeRecord(myScale, player.getDuration())); //default behavior is to play to the end of the video
			theRater.callMeWhen();
			theJumper.callMeWhen();
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
			if (player.getRate() > 0)
				return true;
		} catch (StdQTException stdqte) {
			stdqte.printStackTrace();
		}
		return false;
	}
	//doit envoyer le temps en sec  fois 1000
	public long getCurrentTime() {
		try {
			long myScale = getPlayer().getScale();
			long now = player.getTime();
			return (now*1000)/myScale;
			//int myScale = getPlayer().getScale();
			//LOGGING//LOGGINGSystem.out.println("getCurrentTime():"+(player.getTime()*1000)/myScale);
			//return (player.getTime()*1000)/myScale;
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
			long myScale = getPlayer().getScale();
			long duration = player.getDuration();
			return duration * 1000 / myScale;
			//int myScale = getPlayer().getScale();
			//return (player.getDuration()*1000)/myScale; caused problems for really long files: i guess ints can only be so big
			//return (player.getDuration()/myScale)*1000; //this is therefore an interim solution: should use longs instead!!
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
			int myScale = getPlayer().getScale();
			long t1 = t * myScale / 1000;
			getPlayer().setTime((int)t1);
		} catch (StdQTException stqte) {
			stqte.printStackTrace();
		} catch (QTException qte) {
			//LOGGINGSystem.out.println("getCurrentTimeErr");
		}
	}
// inner classes


	class myRateCallBack extends quicktime.std.clocks.RateCallBack {
		public myRateCallBack(TimeBase tb, int scale, int flag) throws QTException {
			super(tb, scale, flag);
		}
		public void execute() {
			try {
				//LOGGINGSystem.out.println("myRateCallBack: " + String.valueOf(rateWhenCalled));
				/* rateWhenCalled has the following value:
					1: when the media starts playing
					0: when the media stops
				*/
				if (rateWhenCalled > 0) launchAnnotationTimer();
				else { 
					int myScale = getPlayer().getScale();
					//needed to ensure that stop time does not stick (and interfere with behavior of slider)
					myMoviesTimeBase.setStopTime(new TimeRecord(myScale, player.getDuration()));
					//not needed since launchAnnotationTimer() cancels automatically
					//cancelAnnotationTimer();
				}
				cancelAndCleanup();
				callMeWhen();
			} catch (Exception e) {
				//LOGGINGSystem.out.println("myRateCallBack err: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	class myJumpCallBack extends quicktime.std.clocks.TimeJumpCallBack {
		public myJumpCallBack(TimeBase tb) throws QTException {
			super(tb);
		}
		public void execute() {
			try {
				//LOGGINGSystem.out.println("myJumpCallBack: " + String.valueOf(rateWhenCalled));
				if (rateWhenCalled > 0) launchAnnotationTimer();
				
				/*
				//Just added this: ok??
				int myScale = getPlayer().getScale();
				//needed to ensure that stop time does not stick (and interfere with behavior of slider)
				myMoviesTimeBase.setStopTime(new TimeRecord(myScale, getEndTime()));
				*/
				
				cancelAndCleanup();
				callMeWhen();
			} catch (Exception e) {
				//LOGGINGSystem.out.println("myJumpCallBack err: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public class Tickler implements Ticklish {
		public void timeChanged(int newTime) throws QTException {
			//LOGGING//LOGGINGSystem.out.println("*** TimerClass *** timeChanged at:"+newTime);
		}
		public boolean tickle(float er,int time) throws QTException {
			//LOGGING//LOGGINGSystem.out.println("*** TimerClass *** tickle at:"+time);
			return true;
		}
	}
}

/*
This class is the equivalent of sun.awt.DrawingSurface in 1.3. From 1.4 onwards the DrawingSurface of awt component cannot be accessed via java awt classes. This class makes calls to the native WinNativeHelper library using JNI , that returns the DrawingSurface for the component . This implementation is just for Windows operating system. 



The bugtraq report that corresponds to this change is: 4293646. 

The sun.awt.DrawingSurface API has been removed. It was never made public, but some developers have been using it. The functionality has been replaced by the JAWT. For more information, see the AWT Native Interface description in the 1.3 release notes for AWT. 


Java Applets cannot choose which version of Java they invoke. This choice is made instead by the browser. Microsoft�s Internet Explorer uses Java 1.3.1. Apple�s Safari uses Java 1.4.1. (Applet Launcher also uses Java 1.4.1.)
*/

