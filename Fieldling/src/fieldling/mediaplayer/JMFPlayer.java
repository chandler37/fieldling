/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2002 Michel Jacobson jacobson@idf.ext.jussieu.fr
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

/*-----------------------------------------------------------------------*/
import java.util.*;
import java.net.*;
import javax.media.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
/*-----------------------------------------------------------------------*/

public class JMFPlayer extends PanelPlayer implements ControllerListener {
		public URL 				mediaURL;

		private Player			player = null;
		private Component 		visualComponent = null;
		private Component		controlComponent = null;
		private Panel 			panel = null;
		private JPanel 			vPanel = null;

		private Container		parent = null;

		private java.util.Timer	timer = null;
		private Time 			stopTime = null;
		private Time			pauseTime = null;

		private boolean 			isMediaAudio = false;
		private boolean			isSized = false;

		private boolean			isRealized = false;
		private boolean			isCached = false;

		private Float			to = null;
/*-----------------------------------------------------------------------*/
	public String getIdentifyingName() {
		return "Java Media Framework";
	}
	public URL getMediaURL() {
		return mediaURL;
	}
	public JMFPlayer() {
		super(new GridLayout());
	}
	public JMFPlayer(Container p, URL sound) throws PanelPlayerException {
		super( new GridLayout() );
		parent = p;
		loadMovie(sound);
	}
	public void loadMovie(URL sound) throws PanelPlayerException {
		if (mediaURL != null) {
			cmd_stop();
			destroy();
		}
		mediaURL = sound;
		start();
	}
	public void setParentContainer(Container c) {
		parent = c;
	}
	public void destroy() throws PanelPlayerException {
		if (false)
			throw new PanelPlayerException();
		removeAllAnnotationPlayers();
		player.close();
		removeAll();
		mediaURL = null;
		isRealized = false;
		isSized = false;
		visualComponent = null;
		controlComponent = null;
		panel = null;
		vPanel = null;
		isMediaAudio = false;
	}
/*-----------------------------------------------------------------------*/
	private void start() {
		try {
			player = Manager.createPlayer(mediaURL);
			player.addControllerListener(this);
		} catch (javax.media.NoPlayerException e) {
			System.err.println("noplayer exception");
			e.printStackTrace();
			return;
		} catch (java.io.IOException ex) {
			System.err.println("IO exception");
			ex.printStackTrace();
			return;
		}
		if (player != null) {
			//player.realize();
			player.prefetch();
		}
	}
/*-----------------------------------------------------------------------*/
	public void displayBorders(boolean borders) throws PanelPlayerException
	{
		if (false)
			throw new PanelPlayerException();
	}
	public void displayController(boolean controller) throws PanelPlayerException
	{
		if (false)
			throw new PanelPlayerException();

	}
	public boolean isInitialized() {
		return isSized;
	}
	//public Dimension getSize() {
	//	return player.getControlPanelComponent().getSize(); //tester avant si player exist
	//}
/*-----------------------------------------------------------------------*/
	private void showMediaComponent() {
		if (isRealized && isCached) {
			if (visualComponent == null) {
				if (panel == null) {
					setLayout(new GridLayout(1,1));
					vPanel = new JPanel();
					vPanel.setLayout( new BorderLayout() );
					if ((visualComponent = player.getVisualComponent())!= null)
						vPanel.add("Center", visualComponent);
					else
						isMediaAudio = true;
					if ((controlComponent = player.getControlPanelComponent()) != null) {
						if (visualComponent == null) //no video
							vPanel.setPreferredSize(new Dimension(400,25));
						vPanel.add("South", controlComponent);
					}
				}
				add(vPanel);
				parent.invalidate();
				parent.validate();
				parent.repaint();
				isSized = true;
			}
		}
	}
	public synchronized void controllerUpdate(ControllerEvent event) {
		if (player == null)
			return;
		if (event instanceof RealizeCompleteEvent) {
			System.out.println("received RealizeCompleteEvent event");
			isRealized = true;
			if (mediaURL.getProtocol().equals("file")) { //if http then wait until entire media is cached
				isCached = true;
				showMediaComponent();
			} else if (isCached) //must be http
				showMediaComponent();
		} else if (event instanceof StartEvent) {
			System.out.println("received StartEvent event");
			launchAnnotationTimer(); //FIXME should have upper limit (stop time)

			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			timer = new java.util.Timer(true);
			timer.schedule(new TimerTask() {
				public void run() {
					//this is specifically for the MPG stop time bug
					if (stopTime != null)
						if (player.getMediaTime().getNanoseconds() > stopTime.getNanoseconds())
							player.stop();
				}}, 0, 15);
		} else if (event instanceof StopEvent) {
			pauseTime = player.getMediaTime();
			cancelAnnotationTimer();

			/*messy problems require messy solutions:
				if the slider is present, dragging it while playing creates
				a RestartingEvent, and if I set the media time here it messes up
				and barely plays at all (maybe because it cancels the previously
				set media time? - I don't know).

				but it seems that if you press the play/pause button on the
				control widget, then you need to set the media time upon stop
				(probably because of problem noted below, namely that you get
				weird results if you do player.start() without setting the media
				time.*/

			if (!(event instanceof RestartingEvent)) {
				//player.setMediaTime(pauseTime);
				//player.prefetch();
			}

			if (event instanceof StopAtTimeEvent) {
				System.out.println("received StopAtTimeEvent");
			} else if (event instanceof StopByRequestEvent) {
				System.out.println("received StopByRequestEvent");
			} else if (event instanceof RestartingEvent) {
				System.out.println("received RestartingEvent");
			} else if (event instanceof DataStarvedEvent) {
				System.out.println("received DataStarvedEvent");
			} else if (event instanceof DeallocateEvent) {
				System.out.println("received DeallocateEvent");
			} else if (event instanceof EndOfMediaEvent) {
				System.out.println("received EndOfMediaEvent");
			}

			stopTime = null;


			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		} else if (event instanceof CachingControlEvent) {
			CachingControlEvent  cce = (CachingControlEvent)event;
			CachingControl cc = cce.getCachingControl();
			System.out.println("still caching at " + String.valueOf(cc.getContentProgress()));
			if (!(cc.getContentLength() > cc.getContentProgress())) {
				System.out.println("caching done!!");
				isCached = true;
				if (isRealized)
					showMediaComponent();
			}
		} else if (event instanceof ControllerErrorEvent) {
			player = null;
			System.err.println("*** ControllerErrorEvent *** " + ((ControllerErrorEvent)event).getMessage());
		} else if (event instanceof PrefetchCompleteEvent) {
			if (panel != null) {
				panel.invalidate();
			}
			parent.invalidate();
			parent.validate();
			parent.repaint();
		}
	}
/*-----------------------------------------------------------------------*/
	public void cmd_stop() throws PanelPlayerException {
		if (player == null)
			throw new PanelPlayerException("no player");
		try {
			player.stop();
		} catch (NotRealizedError err) {
			throw new PanelPlayerException("JMF player not realized");
		}
 	}
	public void cmd_playOn() throws PanelPlayerException {
		if (player == null) {
			throw new PanelPlayerException("no player or video still loading");
		}
		if (player.getState() == Controller.Started)
			return;

		if (pauseTime == null)
			player.setMediaTime(new Time(0.0));
		else
			player.setMediaTime(pauseTime);
		if (player.getTargetState() < Player.Started) {
			player.setStopTime(Clock.RESET);
			player.prefetch();
		}
		player.start();
	}
	public void cmd_playSegment(Integer from, Integer to) throws PanelPlayerException {
		if (from == null || player == null)
			throw new PanelPlayerException("no player or video still loading");

		final Time startTime = new Time(from.longValue() * 1000000);
		try {
			if (player.getState() == Controller.Started)
				player.stop();
			while (player.getState() == Controller.Unrealized)
				;
			if (to == null) {
				stopTime = null;
				player.setStopTime(Clock.RESET);
			} else {
				stopTime = new Time(to.longValue() * 1000000);
				player.setStopTime(stopTime);
			}
			player.setMediaTime(startTime);
			player.prefetch();
			player.start();
		} catch(NotRealizedError err) {
			throw new PanelPlayerException("JMF player not realized");
		}
	}
/*-----------------------------------------------------------------------*/
	public boolean isPlaying() {
		if (player == null)
			return false;
		if (player.getState() == Controller.Started)
			return true;
		return false;
	}
	public int getCurrentTime() {
		if (player == null)
			return -1;
		if (player.getState() < Controller.Realized)
			return -1;
		long currTime = player.getMediaNanoseconds();
		return new Long(currTime / 1000000).intValue();
	}
	public int getEndTime() {
		Time t = player.getDuration();
		long l = t.getNanoseconds();
		return new Long(l / 1000000).intValue();
	}
}
/*-----------------------------------------------------------------------*/

/*

After pause the MPEG video and playing it again it gets faster
Author: vladshtr
In Reply To: After pause the MPEG video and playing it again it gets faster
Mar 1, 2001 6:25 PM

Reply 1 of 1


The problem is in the setting the Media time.

The safety way is to always set new media time with the
following method: setMediaTime(Time time); .... if you want to
use it after
-player.stop(); used as real stop you can use setMediaTime(new
Time(0.0));
-player.stop(); used as real pause you have to use the
combination:
player.stop();
Time currentTime = player.getMediaTime();
//........
player.setMediaTime(currentTime);
player.start();


Re: (urgent) when you pause and resume, video plays at rate > 1
Author: seertenedos
In Reply To: (urgent) when you pause and resume, video plays at rate > 1
Aug 11, 2002 11:36 PM

Reply 1 of 1


I found a solution for this problem for those that are interested.

what you have to do is store the time just before you pause and then set the
time just before you play. here is a copy of my pause and play methods

// Start the player
private void play() {
if (player != null)
{
if (pauseTime != null)
{
player.setMediaTime(pauseTime);
}
if (player.getTargetState() < Player.Started)
player.prefetch();
player.start();
}
}

// Pause the player
private void pause() {
if (player != null)
pauseTime = player.getMediaTime();
player.stop();
}


that should solve your pause play problems!

> The problem is below. It seems quite a few people are
> having this problem but i have not seen a solution
> around. I really need a solution to this problem as
> the whole point of my application is that it plays
> divx and mpeg videos. At the moment i have divx
> movies playing through the mpeg demuxer as the avi one
> stuffed up the audio. I think that is the reason it
> affects both divx and mpeg. My application is due in
> one week and my client is not going to be very happy
> if this problem happens every time they pause then
> play the video.
> The player is for divx movies. If anyone knows how to
> solve this problem or how to make it so you can pause
> and resume divx movies please respond.
>
> Pause and Resume playback.
> The video plays at a high rate and there is no audio.
> Problem doesn't appear while seeking.
>
>
>
>

*/
/* comments from michel
I change the player.realise() to a player.prefetch() call at the end of the start function.
*/