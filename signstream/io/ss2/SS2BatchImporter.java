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
 * SS2BatchImporter.java
 *
 * Created on March 20, 2003, 4:12 PM
 */

package signstream.io.ss2;

import signstream.utility.Log;
import signstream.utility.Utilities;
import signstream.exception.SignStreamException;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;



public class SS2BatchImporter {
  
  static int filesProcessed = 0;
  static final float bytesPerMega = 1048576f;
  static Hashtable dbFilesByMediaRef = new Hashtable();
  static File   assetsDir = Utilities.getSignStreamDir();
  static String packageBaseName = "edu.bu.asllrp.ss2";
  static String importVolumeName = null;
  
  static SS2BatchImporter importer = new SS2BatchImporter();
  
  SS2BatchImporter() {
    createPackageDirectory(packageBaseName);
  }
  
  File createPackageDirectory(String packageName) {
    if (packageName.indexOf('.') != -1) {
      StringBuffer buffer = new StringBuffer(packageName);
      int dotIndex = packageName.indexOf('.');
      while (dotIndex != -1) {
        buffer.setCharAt(dotIndex, File.separatorChar);
        dotIndex = packageName.indexOf('.', dotIndex+1);
      }
      packageName = buffer.toString();
    }
    File packageDir = new File(assetsDir, packageName);
    if ((!packageDir.exists()) && (!packageDir.mkdirs())) {
      Log.error("Couldn't make dir "+packageDir.getAbsolutePath());
      System.exit(0); // should throw exception
    }
    return packageDir;
  } // end createPackageDirectory()
  
  void importFile(File inFile) {
    String filename = inFile.getName();
    if (filename.indexOf('.') != -1) return;
    
    Log.info("Importing "+filename);
    SS2File ss2file = new SS2File(inFile);
    
    try {
      ss2file.parseData();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    
    File packageDir 
      = createPackageDirectory(packageBaseName+"."+filename);
    File reportFile = null;
    
    //// log some info about the media files
    SS2Utterance[] utterances = ss2file.utterances;
    int totalMediaFileBytes = 0;
    // store each mediaRef once only per DB file
    Hashtable mediaRefsCache = new Hashtable();
    
    for (int u=0;u<utterances.length;u++) {
      SS2Utterance utt = utterances[u];
      
      SS2MediaRef[] mediaRefs = utt.mediaRefs;
      for (int i=0;i<mediaRefs.length;i++) {
        SS2MediaRef mr = mediaRefs[i];
        Object obj = mediaRefsCache.get(mediaRefs[i]);
        if (obj == null) {
          mediaRefsCache.put(mr, "FOO");
          totalMediaFileBytes += mr.fileBytes;
          obj = dbFilesByMediaRef.get(mr);
          if (obj != null)
            Log.info(mr+" reffed by "+inFile+" and "+(File)obj);
          else dbFilesByMediaRef.put(mr, inFile);
        } else {
          // Log.info(inFile.getName()+" dupes "+mr.getFileName()+" in utt# "+u);
        }
      } // end mediaRefs
    } // end utterances
    
    float mb = ((float)totalMediaFileBytes)/bytesPerMega;
    
    try {
      reportFile = new File(packageDir, "media-files-"+(int)mb+".txt");
      PrintWriter reporter = new PrintWriter(new FileOutputStream(reportFile));
      reporter.println(mb);
      
      Object[] refs = mediaRefsCache.keySet().toArray();
      for (int i=0; i<refs.length; i++) {
        String mediaFile = ((SS2MediaRef)refs[i]).getFileName();
        mediaFile = mediaFile.substring(0,mediaFile.indexOf('.'));
        reporter.println(mediaFile);
      } 
      reporter.close();
     
      org.jdom.Element element = (org.jdom.Element) ss2file.toXMLNode();
      org.jdom.Document doc = new org.jdom.Document(element);
      org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(" ", true);
      
      File saveFile = new File(packageDir, filename+".ss2.xml");
      FileOutputStream fos = new FileOutputStream(saveFile);
      outputter.output(doc, fos);
      fos.close();
      
      Log.info("Finished "+saveFile.getName());
        
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.exit(0);
    }
    
  } // end importFile()
  
  
  public static void main(String args[]) {
    File importDir = (args.length > 0) ? 
      new File(args[0]) :
      new File(Utilities.getSignStreamDir(), "legacy"); 
      
      importVolumeName = importDir.getName();
      Log.info("Importing "+importDir.getAbsolutePath());
      
      File[] files = importDir.listFiles();
      for (int i=0; i<files.length; i++) {
        importer.importFile(files[i]);
      }
      System.exit(0);
  }
  
}
