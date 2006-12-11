/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Edward Garrett
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
 ****** END LICENSE BLOCK ***** */
/*******************************************************************************
 * 
 * PreferenceManager.java : This program has all preference related code.
 *  
 ******************************************************************************/

package fieldling.quilldriver;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*; 
import java.lang.reflect.*;
import fieldling.mediaplayer.*;
import fieldling.util.I18n;
import fieldling.util.JdkVersionHacks;
import fieldling.quilldriver.gui.QDShell;

public class PreferenceManager {
	//input-output preferences
	public static final String WORKING_DIRECTORY_KEY = "WORKING_DIRECTORY";
	public static final String WORKING_DIRECTORY_DEFAULT = System.getProperty("user.home");
	
	public static final String MEDIA_DIRECTORY_KEY = "MEDIA_DIRECTORY";
	public static final String MEDIA_DIRECTORY_DEFAULT = System.getProperty("user.home");
	
	public static final String NORMALIZE_NAMESPACES_KEY = "NORMALIZE_NAMESPACES";
	public static final int NORMALIZE_NAMESPACES_DEFAULT = -1;
	
	public static final String AUTO_SAVE_MINUTES_KEY = "AUTO_SAVE_MINUTES";
	public static final int AUTO_SAVE_MINUTES_DEFAULT = 0;
	
	public static final String BACKUP_DIRECTORY_KEY = "BACKUP_DIRECTORY";
	public static final String BACKUP_DIRECTORY_DEFAULT = "";
	
	public static final String NEXT_BACKUP_ID_KEY = "NEXT_BACKUP_ID";
	public static final int NEXT_BACKUP_ID_DEFAULT = 0;
	
	public static final String RECENT_FILES_KEY = "RECENT_FILES";
	public static final String RECENT_VIDEOS_KEY = "RECENT_VIDEOS";
	
	//configuration preferences
	public static final String MEDIA_PLAYER_KEY = "MEDIA_PLAYER_KEY";
	public static final String MEDIA_PLAYER_DEFAULT = "fieldling.mediaplayer.QT4JPlayer";
	
	public static final String CONFIGURATION_KEY = "CONFIGURATION";
	@TIBETAN@public static final String CONFIGURATION_DEFAULT = "THDLTranscription";
	@UNICODE@public static final String CONFIGURATION_DEFAULT = "TranscribeQuechua";
	
	public static final String SHOW_FILE_NAME_AS_TITLE_KEY = "SHOW_FILE_NAME_AS_TITLE";
	public static final int SHOW_FILE_NAME_AS_TITLE_DEFAULT = -1;              
	
	public static final String DEFAULT_LANGUAGE_KEY = "DEFAULT_LANGUAGE";
	public static final int DEFAULT_LANGUAGE_DEFAULT = -1;
	
	public static final String DEFAULT_INTERFACE_FONT_KEY = "DEFAULT_INTERFACE_FONT";
	public static final String DEFAULT_INTERFACE_FONT_DEFAULT = null;
	
	//window positioning preferences
	public static final String WINDOW_MODE_KEY="WINDOW_MODE";
	public static final String WINDOW_MODE_DEFAULT = "fieldling.quilldriver.task.MediaToRight";
	
	public static final String WINDOW_X_KEY = "WINDOW_X";
	public static final int WINDOW_X_DEFAULT = 0;
	
	public static final String WINDOW_Y_KEY = "WINDOW_Y";
	public static final int WINDOW_Y_DEFAULT = 0;
	
	public static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH";
	public static final int WINDOW_WIDTH_DEFAULT = Toolkit.getDefaultToolkit().getScreenSize().width;
	
	public static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT";
	public static final int WINDOW_HEIGHT_DEFAULT = Toolkit.getDefaultToolkit().getScreenSize().height;
	
	public static final String VIDEO_HAS_BORDER_KEY = "VIDEO_HAS_BORDER";
	public static final int VIDEO_HAS_BORDER_DEFAULT = 1;
	
	//font and style preferences
	public static final String FONT_FACE_KEY = "FONT_FACE";
	public static final String FONT_FACE_DEFAULT = "Courier";
	
	public static final String FONT_SIZE_KEY = "FONT_SIZE";
	public static final int FONT_SIZE_DEFAULT = 14;
	
	public static final String TAG_RED_KEY = "TAG_RED";
	public static final int TAG_RED_DEFAULT = 65;
	
	public static final String TAG_GREEN_KEY = "TAG_GREEN";
	public static final int TAG_GREEN_DEFAULT = 105;
	
	public static final String TAG_BLUE_KEY = "TAG_BLUE";
	public static final int TAG_BLUE_DEFAULT = 225;
	
	//highlighting preferences
	public static final String HIGHLIGHT_RED_KEY = "HIGHLIGHT_RED";
	public static final int HIGHLIGHT_RED_DEFAULT = 204;
	
	public static final String HIGHLIGHT_GREEN_KEY = "HIGHLIGHT_GREEN";
	public static final int HIGHLIGHT_GREEN_DEFAULT = 102;
	
	public static final String HIGHLIGHT_BLUE_KEY = "HIGHLIGHT_BLUE";
	public static final int HIGHLIGHT_BLUE_DEFAULT = 255;
	
	public static final String HIGHLIGHT_POSITION_KEY = "HIGHLIGHT_POSITION";
	public static final String HIGHLIGHT_POSITION_DEFAULT = "Middle";
	
	public static final String SCROLLING_HIGHLIGHT_POLICY_KEY = "SCROLLING_HIGHLIGHT_POLICY";
	public static final int SCROLLING_HIGHLIGHT_POLICY_DEFAULT = 1;
	
	public static final String MULTIPLE_HIGHLIGHT_POLICY_KEY = "HIGHLIGHT_POLICY";
	public static final int MULTIPLE_HIGHLIGHT_POLICY_DEFAULT = 0;        
	
	//time coding preferences
	public static final String SHOW_TIME_CODING_KEY = "SHOW_TIME_CODING";
	public static final int SHOW_TIME_CODING_DEFAULT = 1;
	
	public static final String SLOW_ADJUST_KEY = "SLOW_ADJUST";
	public static final int SLOW_ADJUST_DEFAULT = 25;
	
	public static final String RAPID_ADJUST_KEY = "RAPID_ADJUST";
	public static final int RAPID_ADJUST_DEFAULT = 250;
	
	public static final String PLAY_MINUS_KEY = "PLAY_MINUS";
	public static final int PLAY_MINUS_DEFAULT = 1000;
	
	//Tibetan specific preferences	
	@TIBETAN@public static final String TIBETAN_FONT_SIZE_KEY = "TIBETAN_FONT_SIZE";
	@TIBETAN@public static final int TIBETAN_FONT_SIZE_DEFAULT = 36;
	@TIBETAN@public static final String TIBETAN_KEYBOARD_KEY = "TIBETAN_KEYBOARD";
	@TIBETAN@public static final String SAVE_TIBETAN_AS_UNICODE_KEY = "SAVE_TIBETAN_AS_UNICODE";
	@TIBETAN@public static final int SAVE_TIBETAN_AS_UNICODE_DEFAULT = -1;

	
	
	private PreferenceManager() {} //do not call constructor, class should not be instantiated
	
	public static Preferences myPrefs = null;
	
	static {
		myPrefs = Preferences.userNodeForPackage(PreferenceManager.class);
	}
	
	public static String getValue(String key,String defvalue)
	{
		if (myPrefs != null)
			return myPrefs.get(key, defvalue);
		return defvalue;
	}
	
	public static int getInt(String key,int defvalue)
	{
		if (myPrefs != null)
			return myPrefs.getInt(key, defvalue);
		return defvalue;
	}
	
	public static void setInt(String key, int setvalue)
	{
		if (myPrefs == null)
		{
			// do nothing
		} else
		{
			myPrefs.putInt(key, setvalue);
		}
	}
	
	public static void setValue(String key, String setvalue) {
		if (myPrefs == null)
		{
			// do nothing
		} else
		{
			myPrefs.put(key, setvalue);
		}
	}
}
