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
 * XMLConstant.java
 *
 * Created on August 8, 2002, 1:11 PM
 */

package signstream.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/** 
 Exports constants for use in XML representations of various SS3 data.
 */
final public class XMLConstant {
  private XMLConstant() {}
  
  // GENERAL
  public static final String ID = "ID";
  public static final String NAME = "NAME";
  public static final String VALUE = "VALUE";
  public static final String NOTES = "NOTES";
  public static final String COMMENTS = "COMMENTS";
  public static final String TEXT = "TEXT";
  public static final String COUNT = "COUNT";
  public static final String SOURCE = "SOURCE";
  public static final DateFormat DATE_FORMAT
      = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  
  // META-DATA for segments (or whole sets)
  public static final String META_DATA = "META-DATA";
  public static final String VERSION = "VERSION";
  public static final String DISTRIBUTOR = "DISTRIBUTOR";
  public static final String AUTHOR = "AUTHOR";
  public static final String CITATION = "CITATION";
  
  // MEDIA PACKAGES -- UNIMPLEMENTED
  public static final String MEDIA_PACKAGE = "MEDIA-PACKAGE";
  public static final String MEDIA_SCENE = "MEDIA-SCENE";
  // public static final String ??? = ""; 
   
  // PARTICIPANTS
  public static final String PARTICIPANTS = "PARTICIPANTS";
  public static final String PARTICIPANT = "PARTICIPANT";
  public static final String GENDER = "GENDER";
  public static final String GENDER_MALE = "male";
  public static final String GENDER_FEMALE = "female";
  public static final String LANGUAGE = "LANGUAGE";
  public static final String AGE = "AGE";
  public static final String PARENTS = "PARENTS";
  public static final String BACKGROUND = "BACKGROUND";
  
  // CODING SCHEMES
  public static final String CODING_SCHEME = "CODING-SCHEME";
  public static final String CS_FIELD = "CS-FIELD";
  public static final String CS_VALUE = "CS-VALUE";
  public static final String CS_PROPERTY = "CS-PROPERTY";
  public static final String PREFIX = "PREFIX";
  public static final String LABEL = "LABEL";
  public static final String TYPE = "TYPE";
  public static final String TYPE_TEXT = "text";
  public static final String TYPE_GRAPHIC = "graphic";
  public static final String TYPE_INTEGER = "integer";
  public static final String TYPE_REAL_NUMBER = "real-number";
  public static final String CONSTRAINT = "CONSTRAINT";
  public static final String CONSTRAINT_FREE_TEXT = "free-text";
  public static final String CONSTRAINT_FIXED_SET = "fixed-set";
  public static final String TIME_ALIGNMENT = "TIME-ALIGNMENT";
  public static final String TIME_ALIGNMENT_ABSOLUTE = "absolute";
  public static final String TIME_ALIGNMENT_LINKED = "linked";
  public static final String TIME_ALIGNMENT_NONE = "none";
  public static final String CATEGORY = "CATEGORY";
  public static final String DESCRIPTION = "DESCRIPTION";
  public static final String ORGANIZATION = "ORGANIZATION";
  
  public static final String SEGMENTS = "SEGMENTS";
  public static final String SEGMENT = "SEGMENT";
  public static final String TRACK = "TRACK";
  public static final String PARTICIPANT_ID = "PARTICIPANT-ID";
  public static final String EXCERPT = "EXCERPT";
  public static final String S = "S";
  public static final String E = "E";
  public static final String LOCAL_CSF = "LOCAL-CSF";
  public static final String LOCAL_CSV = "LOCAL-CSV";
  public static final String VISIBLE = "VISIBLE";
  public static final String ANNOTATION = "A";
  public static final String HOLD = "HOLD";
  
  
  // LEGACY IMPORTING ONLY
  
  public static final String UTTERANCES = "UTTERANCES";
  public static final String PRIMARY = "PRIMARY";
  
  public static final String MEDIA_FILES = "MEDIA-FILES";
  public static final String MEDIA_FILE = "MEDIA-FILE";
  public static final String MEDIA_REF = "MEDIA-REF";
  public static final String PID = "PID";
  public static final String ALT_ID = "ALT-ID";
  public static final String PATHNAME = "PATHNAME";
  public static final String VOLUME = "VOLUME";
  public static final String PATH = "PATH";
  public static final String FILENAME = "FILENAME";
  public static final String TIME_SCALE = "TIME-SCALE"; // ????
  public static final String DURATION = "DURATION";
}
