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
 * SS2MediaRef.java
 *
 * Created on March 5, 2003, 5:14 PM
 */

package signstream.io.ss2;

import org.jdom.Element;


import signstream.utility.XMLConstant;
import signstream.exception.Assert;
import signstream.io.XMLNodeSerializable;
import java.io.IOException;
import java.io.DataInputStream;


public final class SS2MediaRef implements XMLNodeSerializable {
  static final int MEDIA_REFS_SCRAP_VERSION = 3;
  static final int MEDIA_REF_SCRAP_VERSION = 16;
  
  static final int TYPE_VIDEO = 1;
  static final int TYPE_AUDIO = 2;
  
  int id;
  int numUsers;
  int pid;
  String fullPath;
  int created, modified, fileBytes;
  int trackCount, timeScale, duration;
  int prefWidth, prefHeight;
  
  String volumeName, pathName, fileName;
  
  int mediaType;
  
  /** If this is a bogus ref, store the ref it points to */
  private SS2MediaRef realRef = null;

  
  
    /** Reads the entire set of SS2MediaRefs from the input stream. The function
   assumes the stream pointer is correctly set and will almost certainly throw an
   IOException otherwise.
   */
  static SS2MediaRef[] parseMediaRefs(DataInputStream dis)
  throws IOException
  {
    int version = SS2File.readU16(dis);
    Assert.check(version == 3);
    
    int nextID  = SS2File.readU16(dis);
    int numRefs = SS2File.readU16(dis);
    
    SS2MediaRef[] mediaRefs = new SS2MediaRef[numRefs];
    
    for (int i=0; i<numRefs; i++)
    {
      SS2MediaRef mediaRef = new SS2MediaRef();
      mediaRefs[i] = mediaRef;
      
      int mediaScrapVersion = SS2File.readU16(dis);
      Assert.check(mediaScrapVersion == 3);
      
      mediaRef.id = SS2File.readU16(dis);
      mediaRef.numUsers = SS2File.readU16(dis); // discard
      mediaRef.pid = SS2File.readU16(dis);
      
      boolean gotStats = SS2File.readBoolean(dis);
      
      // A little confusing here: either this is a bogus ref which refers
      // to another ref with its parent ID or it has file data (gotStats)
      // Asserting this because I think this is always true.
      Assert.check((mediaRef.pid != -1) ^ // ^ = Exclusive OR; one but not both true
                    (gotStats));
      if (gotStats)
      {
        mediaRef.fullPath  = SS2File.readPascalString(dis);
        Assert.check(mediaRef.fullPath.length() > 0 && mediaRef.fullPath.indexOf(':') != -1);
        
        StringBuffer pathBuffer = new StringBuffer(mediaRef.fullPath);
        int firstDelimiter = mediaRef.fullPath.indexOf(':');
        int lastDelimiter = mediaRef.fullPath.lastIndexOf(':');
        
        mediaRef.volumeName = pathBuffer.substring(0, firstDelimiter);
        mediaRef.fileName = pathBuffer.substring(lastDelimiter+1); 
        
        if (lastDelimiter == firstDelimiter) 
          mediaRef.pathName = "";
        else {
          int delim = firstDelimiter;
          while (true) {
            delim = mediaRef.fullPath.indexOf(':', delim+1);
            if (delim == -1) break;
            pathBuffer.setCharAt(delim, '/');
          }
          mediaRef.pathName = pathBuffer.substring(firstDelimiter+1, lastDelimiter);
        }
        
        mediaRef.created   = SS2File.readU32(dis); // SHOULD figure out format or ignore
        mediaRef.modified  = SS2File.readU32(dis); // ditto
        mediaRef.fileBytes = SS2File.readU32(dis);
        mediaRef.trackCount = SS2File.readU16(dis); // discard
        mediaRef.timeScale = SS2File.readU32(dis); 
        mediaRef.duration  = SS2File.readU32(dis); 
        mediaRef.prefWidth = SS2File.readU16(dis);  // discard
        mediaRef.prefHeight = SS2File.readU16(dis); // discard
      }
    } // end for files
    
    linkBogusRefsToRealRefs(mediaRefs);
    return mediaRefs;
  } 
  
    
  static void linkBogusRefsToRealRefs(SS2MediaRef[] mediaRefs) {
    SS2MediaRef[] mediaRefsCopy = new SS2MediaRef[mediaRefs.length];
    // System.arraycopy(mediaRefs,0, mediaRefsCopy, 0, mediaRefs.length);
    
    for (int i=0; i<mediaRefs.length; i++) {
      SS2MediaRef mr = mediaRefs[i];
      mr.realRef = mr;
      
      int parentID = mr.pid;
      while (parentID != -1) {
        mr.realRef = findRef(mediaRefs, parentID);
        parentID = mr.realRef.pid;
      }
    }
  }
  
  
  static SS2MediaRef findRef(SS2MediaRef[] mediaRefs, int id) {
    for (int i=0; i<mediaRefs.length; i++) {
      if (mediaRefs[i].id == id) return mediaRefs[i];
    }
    throw new RuntimeException("No media ref found matching ID "+id);
  }
  
  boolean isRealRef() { return realRef == this; }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String nl = System.getProperty("line.separator");
    buffer.append("Full path: "+fullPath+nl);
      
    return buffer.toString();
  }
  
  int getID() { return realRef.id; }
  public int getMediaType() { return realRef.mediaType; }
  public String getFullPath() { return realRef.fullPath; }
  public String getVolumeName() { return realRef.volumeName; }
  public String getPathname() { return realRef.pathName; }
  public String getFileName() { return realRef.fileName; }
  
  int getTimeScale() { return realRef.timeScale; }
  int getDuration() { return realRef.duration; }
  public long getDurationInMilleseconds() {
    return SS2MediaRef.convertValueToMilliseconds(getTimeScale(), getDuration());
  }
  static long convertValueToMilliseconds(long unitsPerSecond, long value) {
    // E.g. 1500u  *  ms/s  /  600u/s         = 2,500 ms 
    return  value  *  1000  /  unitsPerSecond; 
  }
  
  public Object toXMLNode() {
    Element element = new Element("MEDIA-FILE");
    element.setAttribute(XMLConstant.ID, ""+realRef.id);
    element.setAttribute("LEGACY-PATH", realRef.fullPath);
    /* element.setAttribute(XMLConstant.VOLUME, realRef.volumeName);
    element.setAttribute(XMLConstant.PATHNAME, realRef.pathName);
    element.setAttribute(XMLConstant.FILENAME, realRef.fileName); */
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
}
