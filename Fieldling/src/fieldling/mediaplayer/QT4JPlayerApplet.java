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
import java.applet.*;
import java.util.*;
import java.net.*;
import javax.media.*;
import netscape.javascript.JSObject;
import java.awt.*;

/*-----------------------------------------------------------------------*/
public class QT4JPlayerApplet extends Applet implements AnnotationPlayer {

		static public String	FIC_SOUND;
		private QT4JPlayer	myPlayer;

/*-----------------------------------------------------------------------*/
	public void init() {
		FIC_SOUND  	= getParameter("Sound");
		String TAB_STARTS  = getParameter("STARTS");
		String TAB_ENDS  	= getParameter("ENDS");
		String TAB_IDS  	= getParameter("IDS");

		myPlayer = new QT4JPlayer();
		myPlayer.setParentContainer(this);
		myPlayer.initForSavant(convertTimesForPanelPlayer(TAB_STARTS), convertTimesForPanelPlayer(TAB_ENDS), TAB_IDS);
		myPlayer.addAnnotationPlayer(this);
		myPlayer.setAutoScrolling(true);
		setLayout(new BorderLayout());
		add("Center", myPlayer);
	}
    public void destroy() {
		//try {									//why don't you send an exception
			myPlayer.destroy();
		//} catch (PanelPlayerException e) {
		//	//LOGGINGSystem.out.println(e.getMessage());
		//}
	}
	public void start() {
		try {
			myPlayer.loadMovie(new URL(FIC_SOUND));
		} catch (Exception e) {
			//LOGGINGSystem.out.println(e.getMessage());
		}
	}
/*-----------------------------------------------------------------------*/
	public boolean cmd_isRealized() {
		return myPlayer.isInitialized();
	}
	public String cmd_firstS() {
		return myPlayer.cmd_firstS();
	}
	public void cmd_stop() {
		try {
			myPlayer.cmd_stop();
		} catch (PanelPlayerException err) {
			//LOGGINGSystem.out.println(err.getMessage());
		}
 	}
	public boolean cmd_isID(String theID) {
		return myPlayer.cmd_isID(theID);
	}
	public void cmd_playFrom(String fromID) {
		myPlayer.cmd_playFrom(fromID);
	}
	public void cmd_playS(String fromID) {
		myPlayer.cmd_playS(fromID);
	}
/*-----------------------------------------------------------------------*/
	public void startAnnotation(String id) {
		sendMessage("startplay", id);
	}
	public void stopAnnotation(String id) {
		sendMessage("endplay", id);
	}
	private void sendMessage(String method, String mess) {
		Object args[] = { mess };
		try {
			JSObject.getWindow(this).call(method, args);
		} catch (Exception e) {
			//LOGGINGSystem.out.println("Erreur appel javascript: "+e+" "+mess);
		}
	}
/*-----------------------------------------------------------------------*/
	private String convertTimesForPanelPlayer(String s) {
		StringBuffer sBuff = new StringBuffer();
		StringTokenizer sTok = new StringTokenizer(s, ",");
		while (sTok.hasMoreTokens()) {
			sBuff.append(String.valueOf(new Float(Float.parseFloat(sTok.nextToken()) * 1000).intValue()));
			sBuff.append(',');
		}
		return sBuff.toString();
	}
};