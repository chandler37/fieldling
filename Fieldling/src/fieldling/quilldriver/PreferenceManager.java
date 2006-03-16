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
 * ***** END LICENSE BLOCK ***** */
/*******************************************************************************
 * 
 * PreferenceManager.java : This program has all preference related code so that
 * Preferences API continues to function even when Java 1.3 is used
 *  
 ******************************************************************************/

package fieldling.quilldriver;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*; 
import java.lang.reflect.*;
import fieldling.mediaplayer.*;
import fieldling.util.I18n;
import fieldling.util.JdkVersionHacks;
import fieldling.quilldriver.gui.QDShell;

public class PreferenceManager extends JPanel {
	//preference keys
	public static final String WINDOW_MODE_KEY="WINDOW_MODE";
	public static final String BOTH_RESIZE_KEY="BOTH_RESIZE";
	public static final String TRANSCRIPT_X_KEY= "TRANSCRIPT__X";
	public static final String TRANSCRIPT_Y_KEY= "TRANSCRIPT__Y";
	public static final String TRANSCRIPT_WIDTH_KEY = "TRANSCRIPT_WIDTH";
	public static final String TRANSCRIPT_HEIGHT_KEY = "TRANSCRIPT_HEIGHT";
	public static final String VIDEO_X_KEY= "VIDEO__X";
	public static final String VIDEO_Y_KEY= "VIDEO__Y";
	public static final String VIDEO_WIDTH_KEY = "VIDEO_WIDTH";
	public static final String VIDEO_HEIGHT_KEY = "VIDEO_HEIGHT";
	public static final String WINDOW_X_KEY = "WINDOW_X";
	public static final String WINDOW_Y_KEY = "WINDOW_Y";
	public static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT";
	public static final String WORKING_DIRECTORY_KEY = "WORKING_DIRECTORY";
	public static final String RECENT_FILES_KEY = "RECENT_FILES";
	public static final String RECENT_VIDEOS_KEY = "RECENT_VIDEOS";
	public static final String MEDIA_DIRECTORY_KEY = "MEDIA_DIRECTORY";
	public static final String MEDIA_PLAYER_KEY = "MEDIA_PLAYER_KEY";
	public static final String SLOW_ADJUST_KEY = "SLOW_ADJUST";
	public static final String RAPID_ADJUST_KEY = "RAPID_ADJUST";
	public static final String PLAY_MINUS_KEY = "PLAY_MINUS";
	public static final String FONT_FACE_KEY = "FONT_FACE";
	public static final String FONT_SIZE_KEY = "FONT_SIZE";
	public static final String CONFIGURATION_KEY = "CONFIGURATION";
	//public static final String HIGHLIGHT_KEY = "HIGHLIGHT";
	public static final String HIGHLIGHT_RED_KEY = "HIGHLIGHT_RED";
	public static final String HIGHLIGHT_GREEN_KEY = "HIGHLIGHT_GREEN";
	public static final String HIGHLIGHT_BLUE_KEY = "HIGHLIGHT_BLUE";
	
	public static final String TAG_RED_KEY = "TAG_RED";	
	public static final String TAG_GREEN_KEY = "TAG_GREEN";	
	public static final String TAG_BLUE_KEY = "TAG_BLUE";	
	
	public static final String HIGHLIGHT_POSITION_KEY = "HIGHLIGHT_POSITION";
	public static final String SCROLLING_HIGHLIGHT_POLICY_KEY = "SCROLLING_HIGHLIGHT_POLICY";
	public static final String MULTIPLE_HIGHLIGHT_POLICY_KEY = "HIGHLIGHT_POLICY";
	public static final String DEFAULT_LANGUAGE_KEY = "DEFAULT_LANGUAGE";
	public static final String DEFAULT_INTERFACE_FONT_KEY = "DEFAULT_INTERFACE_FONT";
	public static final String USE_WIZARD_KEY = "USE_WIZARD";
	@TIBETAN@public static final String TIBETAN_FONT_SIZE_KEY = "TIBETAN_FONT_SIZE";
	@TIBETAN@public static final String TIBETAN_KEYBOARD_KEY = "TIBETAN_KEYBOARD";
	public static final String SHOW_FILE_NAME_AS_TITLE_KEY = "SHOW_FILE_NAME_AS_TITLE";
	public static final String SHOW_TIME_CODING_KEY = "SHOW_TIME_CODING";
	public static String media_directory;
	public static int slow_adjust;
	public static int rapid_adjust;
	public static int play_minus;
	public static String font_face;
	public static int font_size;
	//public static String highlight_color;
	public static int highlight_color_red, highlight_color_green, highlight_color_blue;
	public static int tag_color_red, tag_color_green, tag_color_blue;	
	public static String highlight_position;
	public static int scrolling_highlight_policy;
	public static int multiple_highlight_policy;
	public static int show_file_name_as_title;
	public static int show_time_coding;
	@TIBETAN@public static int tibetan_font_size;
	public static String working_directory;
	@TIBETAN@public static String tibetan_keyboard;
	public static String recent_files;
	public static String recent_videos;
	public static String configuration;
	public static String media_player;
	public static int default_language;
	public static String default_interface_font;
	public static int use_wizard;
	ResourceBundle messages = null;
	Method getMethodvalue, getMethodint,setMethodvalue, setMethodint;
	public static Object myPrefs = null;
	
	public PreferenceManager()
	{
		try {
			Class qdClass = Class.forName("fieldling.quilldriver.PreferenceManager");
			Class prefClass = Class.forName("java.util.prefs.Preferences");
			Class[] userNodeArgTypes = new Class[] {Class.class};
			Object[] userNodeArgs = new Object[] {qdClass};
			Method getUserNodeMethod = prefClass.getMethod("userNodeForPackage", userNodeArgTypes);
			myPrefs = getUserNodeMethod.invoke(null, userNodeArgs);
			
			// All Get Functionalities
			Class[] setParameters = new Class[] {Object.class};
			Object[] argument = new Object[] {this};
			getMethodvalue = prefClass.getMethod("get", new Class[]{String.class,String.class});
			getMethodint = prefClass.getMethod("getInt", new Class[]{String.class,int.class});
			setMethodint = prefClass.getMethod("putInt", new Class[]{String.class,int.class});
			setMethodvalue = prefClass.getMethod("put", new Class[]{String.class,String.class});
			
		} catch (NoSuchMethodException nsme)
		{
			nsme.printStackTrace();
		} catch (InvocationTargetException ite)
		{
			ite.printStackTrace();
		} catch (IllegalAccessException e) {
			//LOGGINGSystem.out.println(e);
		} catch (ClassNotFoundException e)
		{
			//LOGGINGSystem.out.println(e);
		}
		
		media_directory=getValue(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));
		slow_adjust = getInt(SLOW_ADJUST_KEY, 25); //in milliseconds
		rapid_adjust =getInt(RAPID_ADJUST_KEY, 250); //in milliseconds
		play_minus = getInt(PLAY_MINUS_KEY, 1000); // milliseconds
		font_face = getValue(FONT_FACE_KEY, "Courier");
		font_size = getInt(FONT_SIZE_KEY, 14);
		//highlight_color = getValue(HIGHLIGHT_KEY,"FFCCFF");
		highlight_color_red = getInt(HIGHLIGHT_RED_KEY,204);// RGB color value
		highlight_color_green = getInt(HIGHLIGHT_GREEN_KEY,102);
		highlight_color_blue = getInt(HIGHLIGHT_BLUE_KEY,255);
		
		// Royal blue !
		tag_color_red = getInt(TAG_RED_KEY,65);
		tag_color_green = getInt(TAG_GREEN_KEY,105);
		tag_color_blue = getInt(TAG_BLUE_KEY,225);
		
		highlight_position = getValue(HIGHLIGHT_POSITION_KEY, "Middle");
		scrolling_highlight_policy = getInt(SCROLLING_HIGHLIGHT_POLICY_KEY, 0);
		multiple_highlight_policy = getInt(MULTIPLE_HIGHLIGHT_POLICY_KEY, 0);
		default_language = getInt(DEFAULT_LANGUAGE_KEY, -1);
		default_interface_font = getValue(DEFAULT_INTERFACE_FONT_KEY, null);
		use_wizard = getInt(USE_WIZARD_KEY, 1);
		@TIBETAN@tibetan_font_size =getInt(TIBETAN_FONT_SIZE_KEY, 36);
		show_file_name_as_title = getInt(SHOW_FILE_NAME_AS_TITLE_KEY, -1);
		show_time_coding = getInt(SHOW_TIME_CODING_KEY, -1);
		configuration = getValue(CONFIGURATION_KEY, null);
		media_player = getValue(MEDIA_PLAYER_KEY, null);
	}
	
	
	
	public String getValue(String key,String defvalue)
	{
		if (myPrefs != null)
		{
			try
			{
				Object[] argument = new Object[] {key, defvalue};
				
				if((String)getMethodvalue.invoke(myPrefs, argument) == null)
					return defvalue;
				else
					return (String)getMethodvalue.invoke(myPrefs, argument);
			} catch (IllegalAccessException illae)
			{
				illae.printStackTrace();
			} catch (InvocationTargetException ite)
			{
				ite.printStackTrace();
			}
		}
		return defvalue;
	}
	
	
	public int getInt(String key,int defvalue)
	{
		if (myPrefs != null)
		{
			try {
				Object[] argument = new Object[] {key, new Integer(defvalue)};
				if(((Integer)getMethodint.invoke(myPrefs, argument)).intValue() == 0)
					return defvalue;
				else
					return ((Integer)getMethodint.invoke(myPrefs, argument)).intValue();
			} catch (IllegalAccessException illae)
			{
				illae.printStackTrace();
			} catch (InvocationTargetException ite)
			{
				ite.printStackTrace();
			}
		}
		return defvalue;
	}
	
//	function to set integer value
	
	public void setInt(String key, int setvalue)
	{
		if (myPrefs == null)
		{
			// do nothing
		} else
		{
			Object[] argument = new Object[] {key, new Integer(setvalue)};
			try
			{
				setMethodint.invoke(myPrefs, argument);
			} catch (IllegalAccessException illae)
			{
				illae.printStackTrace();
			} catch (InvocationTargetException ite)
			{
				ite.printStackTrace();
			}
		}
	}
	
	// function to set String value
	public void setValue(String key, String setvalue) {
		if (myPrefs == null)
		{
			// do nothing
		} else
		{
			Object[] argument = new Object[] {key, setvalue};
			try {
				setMethodvalue.invoke(myPrefs, argument);
			} catch (IllegalAccessException illae)
			{
				illae.printStackTrace();
			} catch (InvocationTargetException ite)
			{
				ite.printStackTrace();
			}
		}
	}
}

