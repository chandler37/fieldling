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
 * SS2Participant.java
 *
 * Created on March 5, 2003, 5:02 PM
 */

package signstream.io.ss2;

import org.jdom.Element;
import java.io.IOException;
import java.io.DataInputStream;

import signstream.utility.Log;
import signstream.utility.XMLConstant;
import signstream.exception.Assert;
import signstream.io.XMLNodeSerializable;


public final class SS2Participant implements XMLNodeSerializable {
  
  static public final int FEMALE = 1;
  static public final int MALE   = 2;
      
  int id;
  int gender;
  String language;
  String label;
  String name;
  int age;
  String comments;
  String parents;
  String background;
  
  
  
  static SS2Participant[] parseParticipants(DataInputStream dis)
    throws IOException {
    int version = SS2File.readU16(dis);
    Assert.check(version == 2);
    
    int languageListVersion = SS2File.readU16(dis);
    Assert.check(languageListVersion == 0);
    
    int numLanguages = SS2File.readU16(dis);
    String[] languages = new String[numLanguages];
    for (int i=0; i<numLanguages; i++) {
      String name = SS2File.readPascalString(dis);
      languages[i] = name;
    }
    int nextParticipantID = SS2File.readU16(dis); // discard
    int numParticipants = SS2File.readU16(dis);

    SS2Participant[] participants = new SS2Participant[numParticipants];

    int offset = 0; // for when participant IDs are not sequential from 0
    for (int i=0; i<numParticipants; i++) {
      int participantScrapVersion = SS2File.readU16(dis);
      Assert.check(participantScrapVersion == 2);
      
      SS2Participant participant = new SS2Participant();
      participants[i+offset] = participant;
      
      participant.id = SS2File.readU16(dis);
      if (participant.id != i+offset) {
        Log.warn("Participant #"+i+" has ID "+participant.id);
        SS2Participant[] newArray = new SS2Participant[participants.length+1];
        participants[i+offset] = null;
        for (int p=0;p<participants.length; p++) {
          newArray[p] = participants[p];
        }
        participants = newArray;
        offset++;
        participants[i+offset] = participant;
      }
      boolean everPaned = SS2File.readBoolean(dis); // discard
      participant.gender = SS2File.readU16(dis) == FEMALE ? SS2Participant.FEMALE : SS2Participant.MALE;
      participant.language = languages[SS2File.readU16(dis)];
      participant.label    = SS2File.readPascalString(dis);
      participant.name     = SS2File.readPascalString(dis);
      if (participant.name.length() == 0) participant.name = participant.label;
      String ageStr   = SS2File.readPascalString(dis);
      participant.age = extractAge(ageStr);
  
      participant.comments = SS2File.readLongPascalString(dis);
      participant.parents  = SS2File.readLongPascalString(dis);
      participant.background = SS2File.readLongPascalString(dis);
    }
    return participants;
  }
  
  static int extractAge(String ageStr) {
    int age = 0;
    if (ageStr != null && ageStr.length() > 0) {
      char[] chars = ageStr.toCharArray();
      for (int c=0; c<chars.length; c++) {
        if (chars[c] >= '0' && chars[c] <= '9') {
          age *= 10;
          age += Integer.parseInt(""+chars[c]);
        }
      }
    }
    // Log.info("extracted age from \""+ageStr+"\" => "+age);
    return age;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String nl = System.getProperty("line.separator");
    
    buffer.append(name+"("+label+") age "+age+(gender == FEMALE ? " female" : " male") +nl);
    buffer.append("Language: "+language+nl);
    buffer.append("Background: "+background+nl);
    buffer.append("Parent info: "+parents+nl);
    buffer.append("Comments: "+comments+nl);
    return buffer.toString();
  }
  
  public int getGender() { return gender; }
  public int getAge() { return age; }
  public String getLanguage() { return language; }
  public String getName() { return name; }
  public String getLabel() { return label; }
  public String getComments() { return comments; }
  public String getParentInfo() { return parents; }
  public String getBackground() { return background; }
  
  public Object toXMLNode() {
    Element element = new Element("PARTICIPANT");
    element.setAttribute(XMLConstant.ID, ""+id);
    element.setAttribute(XMLConstant.NAME, name);
    element.setAttribute(XMLConstant.LABEL, label);
    element.setAttribute(XMLConstant.AGE, ""+age);
    element.setAttribute(XMLConstant.GENDER, (gender == FEMALE ? "female" : "male"));
    element.setAttribute(XMLConstant.LANGUAGE, language);
    if (comments != null && comments.length() > 0) {
      Element commentsEl = new Element(XMLConstant.NOTES);
      commentsEl.addContent(comments);
      element.addContent(commentsEl);
    }
    if (parents != null && parents.length() > 0) {
      Element parentsEl = new Element(XMLConstant.PARENTS);
      parentsEl.addContent(parents);
      element.addContent(parentsEl);
    }
    if (background != null && background.length() > 0) {
      Element backgroundEl = new Element(XMLConstant.BACKGROUND);
      backgroundEl.addContent(background);
      element.addContent(backgroundEl);
    }
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
}
