/*
The contents of this file are subject to the THDL Open Community License
Version 1.0 (the "License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License on the THDL web site 
(http://www.thdl.org/).

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the 
License for the specific terms governing rights and limitations under the 
License. 

The Initial Developer of this software is the Tibetan and Himalayan Digital
Library (THDL). Portions created by the THDL are Copyright 2001 THDL.
All Rights Reserved. 

Contributor(s): ______________________________________.
*/

package fieldling.mediaplayer;

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;

import fieldling.util.*;

public class PlayerFactory {
    //public static List mediaPlayers;

    /** You cannot instantiate this class. */
    private PlayerFactory() { }
    
    public static List getAllAvailablePlayers() {
        List mediaPlayers;
        String defaultPlayer, player;
      
        switch (OperatingSystemUtils.getOSType()) {
            case OperatingSystemUtils.MAC: //macs default to org.thdl.media.SmartQT4JPlayer
	        defaultPlayer = "fieldling.mediaplayer.QT4JPlayer";
                break;
            case OperatingSystemUtils.WIN32: //windows defaults to SmartJMFPlayer
		defaultPlayer = "fieldling.mediaplayer.JMFPlayer";
                break;
            default: //put linux etc. here
                defaultPlayer = "fieldling.mediaplayer.JMFPlayer";
                break;
       }

       // player  = ThdlOptions.getStringOption("thdl.media.player", defaultPlayer);
       player = "fieldling.mediaplayer.JMFPlayer";
       String[] possiblePlayers;
       if (player.equals("fieldling.mediaplayer.JMFPlayer"))
           possiblePlayers = new String[] {"fieldling.mediaplayer.JMFPlayer", "fieldling.mediaplayer.QT4JPlayer"};
	else
            possiblePlayers = new String[] {"fieldling.mediaplayer.QT4JPlayer", "fieldling.mediaplayer.JMFPlayer"};

	mediaPlayers = new ArrayList();
	for (int i=0; i<possiblePlayers.length; i++) {
            try {
		Class mediaClass = Class.forName(possiblePlayers[i]);
                //FIXME:				playerClasses.add(mediaClass);
		PanelPlayer smp = (PanelPlayer)mediaClass.newInstance();
                mediaPlayers.add(smp);
            } catch (ClassNotFoundException cnfe) {
		//LOGGINGSystem.out.println("No big deal: class " + possiblePlayers[i] + " not found.");
            } catch (LinkageError lie) {
		//LOGGINGSystem.out.println("No big deal: class " + possiblePlayers[i] + " not found.");
            } catch (InstantiationException ie) {
		ie.printStackTrace();
            } catch (SecurityException se) {
		se.printStackTrace();
            } catch (IllegalAccessException iae) {
		iae.printStackTrace();
                //ThdlDebug.noteIffyCode();
            }
	}
	return mediaPlayers;
    }
    public static PanelPlayer getPlayerForClass(String className) throws PanelPlayerException {
            try {
		Class mediaClass = Class.forName(className);
		PanelPlayer smp = (PanelPlayer)mediaClass.newInstance();
                return smp;
            } catch (Exception e) {
                throw new PanelPlayerException("player " + className + " doesn't exist");
            }
    }
}
