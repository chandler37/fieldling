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

import fieldling.quilldriver.QDShell;



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



		//public static Field md,sa,ra,pm,ff,fs,tfs,wxk,wyk,wwk,whk,wd,rfk,ck,tkk,mpk;



		public static String media_directory;

		public static int slow_adjust;

		public static int rapid_adjust;

		public static int play_minus;

		public static String font_face;

		public static int font_size;

		public static int tibetan_font_size;



		public static String working_directory;

		public static String tibetan_keyboard;

		public static String recent_files;

		public static String configuration_key;

		public static String media_player;
		
		ResourceBundle messages = null;
		Method getMethodvalue, getMethodint,setMethodvalue, setMethodint;
		public static Object myPrefs = null;

		public PreferenceManager()

		{
			try {
				Class qdClass = Class.forName("fieldling.quilldriver.QDShell");
				Class prefClass = Class.forName("java.util.prefs.Preferences");
				Class[] userNodeArgTypes = new Class[] {Class.class};
				Object[] userNodeArgs = new Object[] {qdClass};
				Method getUserNodeMethod = prefClass.getMethod("userNodeForPackage", userNodeArgTypes);
				Object myPrefs = getUserNodeMethod.invoke(null, userNodeArgs);

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
				System.out.println(e);
			 } catch (ClassNotFoundException e)
			 {

				          System.out.println(e);

      		 }

			
			media_directory=getValue(MEDIA_DIRECTORY_KEY, System.getProperty("user.home"));

			slow_adjust = getInt(SLOW_ADJUST_KEY, 25); //in milliseconds

			rapid_adjust =getInt(RAPID_ADJUST_KEY, 250); //in milliseconds

			play_minus = getInt(PLAY_MINUS_KEY, 1000); // milliseconds

			font_face = getValue(FONT_FACE_KEY, "Courier");

			font_size = getInt(FONT_SIZE_KEY, 14);

			tibetan_font_size =getInt(TIBETAN_FONT_SIZE_KEY, 36);





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
		}



/***



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

***/









public String getValue(String key,String defvalue)

{

	if (myPrefs == null)

	{

		try {



			// do nothing



		} catch (NumberFormatException nfe)

		{

			nfe.printStackTrace();

		}

	}

	else

	{

		try

		{

			Object[] argument = new Object[] {key};



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

	return null;



}





public int getInt(String key,int defvalue)

{

	if (myPrefs == null)

	{

		try {



			// do nothing



		} catch (NumberFormatException nfe)

		{

			nfe.printStackTrace();

		}

	} else

	{

		try {



			Object[] argument = new Object[] {key};



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

	return 0;

}







// function to set integer value

	public void setInt(String key, int setvalue)

	{

		if (myPrefs == null)

		{

			// do nothing

		} else

		{

			Object[] argument = new Object[] {key};

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

				Object[] argument = new Object[] {key};

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

