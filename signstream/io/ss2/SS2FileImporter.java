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

package signstream.io.ss2;

import signstream.utility.Log;
import signstream.utility.Utilities;
import signstream.exception.SignStreamException;
import java.text.CharacterIterator;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

/** A testing class */
public class SS2FileImporter {

  static int filesProcessed = 0;
  static final float bytesPerMega = 1048576f;
  
    // SS2MediaRef => File (DB file);
    // looking for instances of media files shared by multiple DB files
  static Hashtable dbFilesByMediaRef = new Hashtable();
    // store total bytes (Integers) with DB file names as keys
  static Hashtable mediaPackageSizes = new Hashtable();
  static PrintWriter report = null; 
  
  public SS2FileImporter(File inFile) {
    if (!(inFile.exists())) {
      System.err.println("SS2FileImporter: "+inFile.getName()+" does not exist");
      return;
    }
    
    importFile(inFile);
  } // end constructor(File)
  
  private void importFile(File inFile) {
    String filename = inFile.getName();
    if (filename.startsWith(".") || 
        filename.endsWith(".xml") ) 
      return;
    
    SS2File ss2file = new SS2File(inFile);
        
    Log.info("Importing \""+inFile.getName()+"\"");
    
    try {
      ss2file.parseData();
    } catch (SignStreamException sse) { 
      sse.printStackTrace();
      return;
    } 
    
    
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
      
    Log.info(inFile.getName()+" uses "+(((float)totalMediaFileBytes)/bytesPerMega)+" MB");
      
    report.println(inFile.getName()+"  "+((float)totalMediaFileBytes)/bytesPerMega);
    Object[] refs = mediaRefsCache.keySet().toArray();
    for (int i=0; i<refs.length; i++) {
      report.println("  "+((SS2MediaRef)refs[i]).getFileName());
    }  
    /* File saveFile = new File(inFile.getAbsolutePath() + ".xml");
    Log.info("Writing "+saveFile.getName());
    
    org.jdom.Element element = (org.jdom.Element) ss2file.toXMLNode();
    org.jdom.Document doc = new org.jdom.Document(element);
    
    Log.info("Document finished");
    
    try {
      FileOutputStream fos = new FileOutputStream(saveFile);
      org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(" ", true);
      outputter.output(doc, fos); 
    } catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    
    Log.info("Document written to disk"); */
    
    // ss2file.printUtterance(System.out, 0);
    
    filesProcessed++;
  } // end importFile()
  
  
  static String makeSafeFilename(String filename) {
    // chop off file extension
    int extPosition = filename.lastIndexOf('.');
    if (extPosition > 0) filename = filename.substring(0,extPosition);
    CharacterIterator iter = new java.text.StringCharacterIterator(filename);
    StringBuffer buffer = new StringBuffer();
     
    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
      if (c == ' ') c = '_';   // replace spaces with underscores
      buffer.append(c);
    }
    
    return buffer.toString();
  } // end makeSafeFilename()
  
  
  public static void main( String[] args ) {
    /* JFileChooser chooser = new JFileChooser(new File(Utilities.getSignStreamDir(), "legacy"));
    File file = null;
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      file = chooser.getSelectedFile();
      chooser = null;
    } else {
      System.exit(0); 
    } */
    
    File file = new File(Utilities.getSignStreamDir(), "NCSLGR(1)"); 
    try { 
      report = 
        new PrintWriter(
          new FileOutputStream(
            new File(Utilities.getSignStreamDir(), "media-report.txt")));
    } catch (IOException ioe) { ioe.printStackTrace(); }
      
    long startTime = System.currentTimeMillis();
    
    
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (int i=0; i<files.length; i++) {
        if (!(files[i].isDirectory())) {
          SS2FileImporter importer = new SS2FileImporter(files[i]); 
        }
      }
    } else {
      SS2FileImporter importer = new SS2FileImporter(file);
    }
    
    
    report.close();
    
    long totalTime = System.currentTimeMillis() - startTime;
    long averageTime = totalTime / filesProcessed;
    Log.info("Total processing time: "+totalTime+" for "+filesProcessed+" files, average time: "+averageTime);
    
    System.exit(0);
  }// end main()

  
}
