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
 * Asset.java
 *
 * Created on October 30, 2002, 2:01 PM
 */

package signstream.asset;

import java.io.File;

/**
 *
    Asset
       <>Start, End times
       <>Path name (optional, absolute or relative)
       <>Volume name (non-null implies non-local)
       <>Local vs. remote (determined by path, volume)
       <>File size
       <>Quality (can be guesstimated by time/size ratio)
       <>Format (MIME-type? Extension-based...)
       <>Codec (optional)
       <>java.io.File (determined by file name, package name, optional path/volume info) 
 */
public class Asset {

    public Asset(int start, int end, File file, boolean local) {
      
    }
    
    /// public methods

    public File getFile() { return null; }
    public int getStartTime() { return -1; }
    public int getEndTime() { return -1; }
    public int getDuration() { return -1; }
    public boolean isLocal() { return true; }
    public boolean isAvailable() { return false; }
    
    /// package methods
    
    String getVolumeName() { return null; }
    String setVolumeName() { return null; }
    String getPathName() { return null; }
    String setPathName() { return null; }
    String getFileName() { return null; }
    String setFileName() { return null; }
    
    int setStartTime(int s) { return -1; }
    int setEndTime(int e) { return -1; }
    void setLocal(boolean local) { }
    File setFile() { return null; }
    
}
