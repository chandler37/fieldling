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


/****
*
*	PreferenceManager.java :   This program has all preference related code so that Preferences API
*	continues to function even when Java 1.3 is used
*
***/

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
import fieldling.quilldriver.*;

public class PreferenceManager extends JPanel {



		//preference keys
		public static final String WINDOW_X_KEY = "WINDOW_X";
		public static final String WINDOW_Y_KEY = "WINDOW_Y";
		public static final String WINDOW_WIDTH_KEY = "WINDOW_WIDTH";
		public static final String WINDOW_HEIGHT_KEY = "WINDOW_HEIGHT";
		public static final String WORKING_DIRECTORY_KEY = "WORKING_DIRECTORY";
		public static final String RECENT_FILES_KEY = "RECENT_FILES";
		public static final String MEDIA_DIRECTORY_KEY = "MEDIA_DIRECTORY";
		public static final String MEDIA_PLAYER_KEY = "MEDIA_PLAYER_KEY";
		public static final String SLOW_ADJUST_KEY = "SLOW_ADJUST";
		public static final String RAPID_ADJUST_KEY = "RAPID_ADJUST";
		public static final String PLAY_MINUS_KEY = "PLAY_MINUS";
		public static final String FONT_FACE_KEY = "FONT_FACE";
		public static final String FONT_SIZE_KEY = "FONT_SIZE";
		public static final String CONFIGURATION_KEY = "CONFIGURATION";
		public static final String TIBETAN_FONT_SIZE_KEY = "TIBETAN_FONT_SIZE";
		public static final String TIBETAN_KEYBOARD_KEY = "TIBETAN_KEYBOARD";

		public static Field md,sa,ra,pm,ff,fs,tfs,wxk,wyk,wwk,whk,wd,rfk,ck,tkk,mpk;
/*
		public static final String media_directory;
		public static final int slow_adjust;
		public static final int rapid_adjust;
		public static final int play_minus;
		public static final String font_face;
		public static final int font_size;
		public static final int tibetan_font_size;
*/
		ResourceBundle messages = null;
				QD qd = null;


				QDShell qdshellclass = new QDShell();



		public static Class myPrefs;


		public PreferenceManager()
		{

			//preference defaults and values
	/***		private static Preferences myPrefs = Preferences.userNodeForPackage(QDShell.class);
			public static String media_directory = myPrefs.get(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));
			public static int slow_adjust = myPrefs.getInt(SLOW_ADJUST_KEY, 25); //in milliseconds
			public static int rapid_adjust = myPrefs.getInt(RAPID_ADJUST_KEY, 250); //in milliseconds
			public static int play_minus = myPrefs.getInt(PLAY_MINUS_KEY, 1000); // milliseconds
			public static String font_face = myPrefs.get(FONT_FACE_KEY, "Courier");
			public static int font_size = myPrefs.getInt(FONT_SIZE_KEY, 14);
			public static int tibetan_font_size = myPrefs.getInt(TIBETAN_FONT_SIZE_KEY, 36);

****/
			PrefManagerMethod(qdshellclass);



		}

		void PrefManagerMethod(Object o)
		{
			myPrefs = o.getClass();

			//Field f[]=myPrefs.getFields();



			 try
			 {
				// All Get Functionalities

				md= myPrefs.getField(MEDIA_DIRECTORY_KEY);
				qdshellclass.media_directory = (md.get(o)).toString();

				sa= myPrefs.getField(SLOW_ADJUST_KEY);
				qdshellclass.slow_adjust = (int) sa.getInt(o);

				ra= myPrefs.getField(RAPID_ADJUST_KEY);
				qdshellclass.rapid_adjust = (int) ra.getInt(o);

				pm= myPrefs.getField(PLAY_MINUS_KEY);
				qdshellclass.play_minus = (int) pm.getInt(o);

				ff= myPrefs.getField(FONT_FACE_KEY);
				qdshellclass.font_face = (ff.get(o)).toString();

				fs= myPrefs.getField(FONT_SIZE_KEY);
				qdshellclass.font_size = (int) fs.getInt(o);

				tfs= myPrefs.getField(TIBETAN_FONT_SIZE_KEY);
				qdshellclass.tibetan_font_size = (int) tfs.getInt(o);

				wxk= myPrefs.getField(WINDOW_X_KEY);
				qdshellclass.xl = (int) wxk.getInt(o);

				wyk= myPrefs.getField(WINDOW_Y_KEY);
				qdshellclass.yl = (int) wyk.getInt(o);

				wwk= myPrefs.getField(WINDOW_WIDTH_KEY);
				qdshellclass.ww = (int) wwk.getInt(o);

				whk= myPrefs.getField(WINDOW_HEIGHT_KEY);
				qdshellclass.wh = (int) whk.getInt(o);

				wd= myPrefs.getField(WORKING_DIRECTORY_KEY);
				qdshellclass.working_directory = (wd.get(o)).toString();

				rfk= myPrefs.getField(RECENT_FILES_KEY);
				qdshellclass.recent_files = (rfk.get(o)).toString();

				ck= myPrefs.getField(CONFIGURATION_KEY);
				qdshellclass.configuration_key = (ck.get(o)).toString();

				tkk= myPrefs.getField(TIBETAN_KEYBOARD_KEY);
				qdshellclass.tibetan_keyboard = (tkk.get(o)).toString();

				mpk= myPrefs.getField(MEDIA_PLAYER_KEY);
				qdshellclass.media_player = (mpk.get(o)).toString();




			 } catch (NoSuchFieldException e)
			 {
				  System.out.println(e);
			 } catch (SecurityException e)
			 {
				  System.out.println(e);
			 } catch (IllegalAccessException e)
			 {
			 	 System.out.println(e);
			 }



		}

		// All Set Functionalities

		void SetPreferenceInt(Object o,Field f,int value)
		{
			myPrefs = o.getClass();



		}

}
