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

import org.jdom.Element;
import signstream.utility.Log;
import signstream.utility.XMLConstant;
import signstream.exception.SS2FileFormatException;
import signstream.exception.SS2ImportException;
import signstream.exception.Assert;
import signstream.io.XMLNodeSerializable;
import java.io.*;


import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Iterator;

/** Contains code for parsing legacy SS2 DB files and returning SS3 data structures. */
public final class SS2File implements XMLNodeSerializable {
  // static Hashtable participantNamesByID  = new Hashtable();
  
  private static String          SIGNATURE            = "sASL";
  private static int             RECENT_FILE_VERSION  = 31;
  private static int             CURRENT_FILE_VERSION = 32;
  // see "asl utterance scrap.h"
  static final int UTT_CURR_SCRAP_FORMAT_VERSION    = 14;
  // see "asl MD scrap.h"
  static final int MD_CURR_SCRAP_FORMAT_VERSION     = 1;
  // see "asl MediaRef scrap.h"
  static final int MREF_CURR_SCRAP_FORMAT_VERSION   = 1;
  // see "asl field scrap.h":
  static final int FIELD_CURR_SCRAP_FORMAT_VERSION  = 5;
  // see "asl non-manual data scrap.h":
  static final int NON_MANUAL_DATA_CURR_SCRAP_FORMAT_VERSION = 4;
  // see "asl gloss data scrap.h":
  static final int GLOSS_CURR_SCRAP_FORMAT_VERSION  = 3;
  
  /** Either a local File or remotr URL; either a SIgnStream DB file or its
   XML equivalent (with a "ss2.xml" extension) */
  private Object    sourceData = null;
  /** If true, the import data is in XML format, if false the data is in
   SignStream 2 DB file format. */
  private boolean xmlFormat = false;
  /** A URL or file path of the file opened. This may be a SignStream 2 DB file
   or XML file, either local or remote */
  private String sourceLocation = null;
  /** The name of the original DB file used to create this. This name is preserved
   in the XML. */
  private String sourceDBFilename = null;
  /** Has the file been fully read and parsed -- does not imply that the
   scheme has been normalized or that media files have been migrated
   into SS3 asset packages. */
  boolean fileParsed          = false;
  /** Has the coding scheme been merged with another scheme (typically a local
   scheme) -- when this happens all imported annotations are updated to refer
   to the target scheme's ID #s. */
  boolean schemeNormalized    = false;
  /** Have redundant media refs that refer to actual media refs been culled? */
  boolean mediaRefsNormalized = false;
  /** Have media files not referenced by any segments been culled? */
  boolean mediaRefsCleaned   = false;
  
  /// SignStream 2 legacy classes
  public SS2FieldSpec fieldSpec = null;
  /** Contains the meta-data from SS2's "DB Profile" */
  public SS2DBProfile dbProfile = null;
  /** Contains all importable (not fully implemented) data about media files
   referenced by the legacy file. This data is transient and will be migrated
   into SS3 asset packages, but A) serves to have working media data during
   development and B) is needed to perform some timeline reconstruction and
   media ref normalization during the import process. */
  public SS2MediaRef[]           mediaRefs = null;
  /** Contains all the media refs actually used by one or more utterances,
   for culling the unused from the longer list. Unused media refs are primarily
   due to artifacts when creating new DB files from pre-existing ones, and
   have been decreed superfluous.
   */
  transient Vector        usedMediaRefs = new Vector();
  public SS2Participant[]        participants = null;
  public SS2Utterance[]          utterances = null;
  
  /** Simply caches <code>importFile</code> -- could check for file's existence
   and look at extension, signature, and SS2 file format number here... */
  public SS2File(File importFile) {
    sourceData = importFile;
    xmlFormat = importFile.getName().endsWith(".ss2.xml");
    sourceLocation = importFile.getAbsolutePath();
    sourceDBFilename = importFile.getName();
  }
  
  public SS2File(URL importURL) {
    sourceData = importURL;
    xmlFormat = importURL.getFile().endsWith(".ss2.xml");
    sourceLocation = importURL.toString();
    sourceDBFilename = "URL file"; // should fix
  }
  
  
  
  private byte[] readIntoMemory()
  throws IOException {
    if (javax.swing.SwingUtilities.isEventDispatchThread()) 
      //LOGGINGSystem.out.println("SS2File readIntoMemory() called from event dispatch thread");
    byte[] buffer = null;
    InputStream is = null;
    if (sourceData instanceof URL && ((URL)sourceData).getProtocol().equals("file"))
      sourceData = new File(((URL)sourceData).getFile());
    if (sourceData instanceof File) {
      File inFile = (File) sourceData;
      is = new FileInputStream( inFile );
      buffer = new byte[( int ) inFile.length()];
      is.read( buffer );
      is.close();
    } else if (sourceData instanceof URL) {
      URLConnection connection = ((URL)sourceData).openConnection();
      buffer = new byte[connection.getContentLength()];
      is = connection.getInputStream();
      int offset = 0;
      int ret = 0;
      while (ret != -1) {
        /// artificially stagger reading
        int bytesToRead = buffer.length - offset; 
        // Math.min(1024, buffer.length-offset);
        ret = is.read( buffer, offset, bytesToRead);
        offset += ret;
        percentStreamRead = (int) (((float) offset) * 100f / ((float) buffer.length));
        // try {Thread.sleep(20); } catch (InterruptedException ie) {}
      }
      is.read( buffer );
      is.close();
    }
    percentStreamRead = 100;
    return buffer;
  } // end readIntoMemory()
  
  
  public void parseData()
  throws SS2ImportException {
    if (xmlFormat) {
      readFromXMLStream();
    } else {
      parseDBFileData();
    }
    fileParsed = true;
  } // end parseData()
  
  
  private void readFromXMLStream()
  throws SS2ImportException {
    InputStream is = null;
    try {
      if (sourceData instanceof URL)
        is = ((URL)sourceData).openStream();
      else
        is = new FileInputStream((File)sourceData);
    } catch (IOException ioe) {
      throw new SS2ImportException("Error reading stream", ioe);
    }
    
    org.jdom.Document document = null;
    try {
      org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
      document = builder.build(is);
    } catch (org.jdom.JDOMException je) {
      throw new SS2ImportException("Error building Document", je);
    }
    
    fromXMLNode(document.getRootElement());
  } // readFromXMLStream()
  
  public static int NOT_STARTED           = 0;
  public static int READING_STREAM        = 1;
  public static int PARSING_HEADER        = 2;
  public static int PARSING_FIELD_SPEC    = 3;
  public static int PARSING_PARTICIPANTS  = 4;
  public static int PARSING_DB_PROFILE    = 5;
  public static int PARSING_MEDIA_REFS    = 6;
  public static int PARSING_UTTERANCES    = 7;
  public static int CLEANING_UP           = 8;
  public static int LOADED                = 9;
  public static int ERROR                 = 10;
  private volatile int loadProgress = NOT_STARTED;
  private String[] loadMessages = new String[] {
    "Not started",
    "Reading data from stream...",
    "Parsing file header...",
    "Parsing field specification...",
    "Parsing participant info...",
    "Parsing DB profile...",
    "Parsing media references...",
    "Parsing utterance data...",
    "Cleaning up data...",
    "Loading complete",
    "Error during parsing",
  };
  private volatile int percentStreamRead = 0;
  
  // SHOULD make these methods one atomic operation somehow
  public int getLoadProgress() {
    return loadProgress;
  }
  public String getLoadMessage() {
    return loadMessages[loadProgress];
  }
  public int getPercentStreamRead() { return percentStreamRead; }
  
  public void parseDBFileData()
  throws SS2ImportException {
    /// TRACK TIME SPENT PARSING FOR FUTURE OPTIMIZATION
    
    loadProgress = READING_STREAM;
    
    long startTime = System.currentTimeMillis();
    long lastTime = startTime;
    long currentTime = lastTime;
    long parseTime = 0;
    Log.info("Loading "+sourceLocation);
    
    DataInputStream dis = null;
    ByteArrayInputStream bais = null;
    
    try {
      byte[] ramFile = readIntoMemory();
      bais = new ByteArrayInputStream( ramFile );
      dis = new DataInputStream( bais );
      
      String signature = readSignature(dis);
      if (!(signature.equals(SIGNATURE))) {
        loadProgress = ERROR;
        throw new SS2FileFormatException(signature); //sourceLocation);
      }
      
      int version = readU16(dis);
      if (version != RECENT_FILE_VERSION && version != CURRENT_FILE_VERSION) {
        loadProgress = ERROR;
        throw new SS2FileFormatException(sourceLocation, version);
      }
      if (version == RECENT_FILE_VERSION) {
        Log.warn("Using file format 31");
      }
      
      /// READ FILE HEADER
      
      loadProgress = PARSING_HEADER;
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Data read:       "+parseTime);
      lastTime = currentTime;
      
      int fileSize = readU32(dis); // COULD assert this
      int nextSegmentID = readU32(dis);
      int indexOffset = readU32(dis);
      int indexSize = readU32(dis);
      int fieldSpecOffset = readU32(dis);
      int fieldSpecSize = readU32(dis);
      int utteranceOffset = readU32(dis);
      int utteranceSize = readU32(dis);
      int fontListOffset = readU32(dis);
      int fontListSize = readU32(dis);
      int openSegmentsOffset = readU32(dis);
      int openSegmentsSize = readU32(dis);
      int participantsOffset = readU32(dis);
      int participantsSize = readU32(dis);
      int dbProfileOffset = readU32(dis);
      int dbProfileSize = readU32(dis);
      int windowLocationOffset = readU32(dis);
      int windowLocationSize = readU32(dis);
      int mediaRefsOffset = readU32(dis);
      int mediaRefsSize = readU32(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Header:          "+parseTime);
      lastTime = currentTime;
      
      /// READ THE CODING SCHEME
      
      loadProgress = PARSING_FIELD_SPEC;
      jumpToFilePosition(dis, fieldSpecOffset);
      fieldSpec = SS2FieldSpec.parseFieldSpec(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  FieldSpec:       "+parseTime);
      lastTime = currentTime;
      
      //// READ PARTICIPANT DATA
      
      loadProgress = PARSING_PARTICIPANTS;
      jumpToFilePosition(dis, participantsOffset);
      participants = SS2Participant.parseParticipants(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Participants:    "+parseTime+" ("+participants.length+" participants)");
      lastTime = currentTime;
      
      //// READ "DB Profile" META-DATA
      
      loadProgress = PARSING_DB_PROFILE;
      jumpToFilePosition(dis, dbProfileOffset);
      dbProfile = SS2DBProfile.parseDBProfile(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  DB Profile:      "+parseTime);
      lastTime = currentTime;
      
      /// READ MEDIA REF DATA
      
      loadProgress = PARSING_MEDIA_REFS;
      jumpToFilePosition(dis, mediaRefsOffset);
      mediaRefs = SS2MediaRef.parseMediaRefs(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Media:           "+parseTime);
      lastTime = currentTime;
      
      /// READ UTTERANCES
      
      loadProgress = PARSING_UTTERANCES;
      jumpToFilePosition(dis, utteranceOffset);
      parseUtterances(dis);
      
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Utterances:     "+parseTime);
      lastTime = currentTime;
      
      
      loadProgress = CLEANING_UP;
      removeUnusedMediaRefs();
      currentTime = System.currentTimeMillis();
      parseTime = currentTime-lastTime;
      Log.info("  Cull media refs:"+parseTime);
      lastTime = currentTime;
      
      
      dis.close();
      bais.close();
    } catch (IOException ioe) {
      // SHOULD throw more useful exceptions at higher granularity
      Log.info("  I/O error reading "+sourceLocation);
      loadProgress = ERROR;
      try {
        dis.close();
        bais.close();
      } catch (IOException ioe2)
      { ioe2.printStackTrace(); }
      
      throw new SS2ImportException("I/O Error occured", ioe);
    }
    
    currentTime = System.currentTimeMillis();
    parseTime = currentTime - startTime;
    Log.info("Total load time: "+parseTime);
    
    loadProgress = LOADED;
  } // end parseFile()
  
  /** Checks the first 4 bytes of <code>file</code> to determine
   if it is a SignStream 2 DB file. */
  public static boolean isSS2File(File file) {
    FileInputStream fis = null;
    DataInputStream dis = null;
    try {
       fis = new FileInputStream(file);
       dis = new DataInputStream(fis);
      if (readSignature(dis).equals(SIGNATURE)) return true;
      return false;
    } catch (IOException ioe) {
      Log.error(ioe.getMessage());
      return false;
    } finally {
      try {
        if (dis != null) dis.close();
        if (fis != null) fis.close();
      } catch(IOException ioe) {
        Log.error(ioe.getMessage());
      }
    }
    
  }
  
  
  public String getSourceFileName() {
    return sourceDBFilename;
  }
  
  public int getUtteranceCount() {
    return utterances.length;
  }
  
  SS2Utterance UTTERANCE = null;
  int FIELD = -1;
  int currentUtteranceDuration = 0;
  
  void parseUtterances(DataInputStream dis)
  throws IOException {
    Assert.check(participants != null && participants.length >0);
    Assert.check(dbProfile != null);
    Assert.check(mediaRefs != null && mediaRefs.length > 0);
    Assert.check(utterances == null);
    
    int numUtterances = readU16(dis);
    utterances = new SS2Utterance[numUtterances];
    
    // Since an utterance maps to 1 or more segments (per participant), data
    // common to each segment must be read before calling parseParticipantSegment()
    
    for (int u=0; u<numUtterances; u++) {
      SS2Utterance utterance = new SS2Utterance();
      utterances[u] = utterance;
      
      UTTERANCE = utterance;
      int fileOffset  = readU32(dis);
      int size    = readU32(dis); // discard
      
      utterance.id      = readU32(dis);
      utterance.excerpt = parseUtteranceExcerpt(dis);
      
      jumpToFilePosition(dis, fileOffset);
      
      int utteranceVersion =  readU16(dis);
      Assert.check(utteranceVersion == UTT_CURR_SCRAP_FORMAT_VERSION);
      int id2 = readU32(dis);
      Assert.check(utterance.id == id2); // I think these are redundant
      boolean overrideFieldOrder      = readBoolean(dis);
      boolean overrideFieldVisibility = readBoolean(dis);
      boolean overrideFieldColor      = readBoolean(dis);
      // NOTES scrap
      readWindowStateInfo(dis); // discard window state info
      utterance.notes = readNotes(dis);
      
      utterance.mediaRefs = parseMediaRefRefs(dis);
      
      long[] times = readClipTimes(dis, false);
      utterance.startTime = times[0];
      utterance.endTime   = times[1];
      currentUtteranceDuration = (int) (utterance.endTime - utterance.startTime);
      
      readWindowStateInfo(dis); // discard
      
      int numParticipants = readU16(dis);
      utterance.participantSegments = new SS2ParticipantSegment[numParticipants];
      
      for (int i=0; i<numParticipants; i++) {
        SS2ParticipantSegment segment = parseParticipantSegment(dis);
        // if (endTime >= 0) ...
        utterance.participantSegments[i] = segment;
        // segment.removeEmptyStart();
        
      }
      
      restoreLastFilePosition(dis);
    } // end for utterances
  } // end parseTranscriptSegments()
  
  
  static final String parseUtteranceExcerpt(DataInputStream dis)
  throws IOException {
    // SS2 code comments say excerpt length is 1 byte, but we need to
    // right-shift read length value by one byte for unknown reasons
    int excerptLength = readU16(dis) >> 8;
    String excerpt = readString24Byte(dis);
    if (excerptLength > 0 && excerptLength <= 24 )
      excerpt = excerpt.substring(0, excerptLength-1);
    else if (excerptLength == 0)
      excerpt = "";
    
    return excerpt;
  }
  
  /**
   Returns the SS2MediaRefs that are <i>referenced</I> by this utterance.
   */
  SS2MediaRef[] parseMediaRefRefs(DataInputStream dis)
  throws IOException {
    int mediaDispatchScrapVersion = readU16(dis);
    Assert.check(mediaDispatchScrapVersion == MD_CURR_SCRAP_FORMAT_VERSION);
    
    readWindowStateInfo(dis); // discard
    boolean displayMD = readBoolean(dis); // discard
    
    int numVideoRefs = readU16(dis);
    int numAudioRefs = readU16(dis);
    int numRefs = numVideoRefs + numAudioRefs; // should keep this distinction?
    
    Vector refs = new Vector();
    for (int i=0; i<numRefs; i++) {
      int mRefVersion = readU16(dis);
      Assert.check(mRefVersion == MREF_CURR_SCRAP_FORMAT_VERSION);
      
      int mediaType = readU16(dis);    // VIDEO = 1; AUDIO = 2
      boolean selected = readBoolean(dis);
      int mediaID = -1;
      boolean visible = true;
      if (selected) {
        mediaID = readU16(dis);
        boolean volumeSet = readBoolean(dis);
        if (volumeSet) {
          int volume = readU16(dis); // discard
        }
        readWindowStateInfo(dis); // discard
        visible = readBoolean(dis);
      }
      
      if (mediaID != -1) {
        SS2MediaRef mr = SS2MediaRef.findRef(mediaRefs, mediaID);
        Assert.check(mr != null);
        // SHOULD implement MediaRef.setType()
        refs.addElement(mr);
        usedMediaRefs.addElement(mr);
      }
    } // end for media ref refs
    
    SS2MediaRef[] mrs = new SS2MediaRef[refs.size()];
    Iterator it = refs.iterator();
    int index = 0;
    while (it.hasNext()) {
      mrs[index++] = (SS2MediaRef) it.next();
    }
    return mrs;
  }
  
  
  void removeUnusedMediaRefs() {
    if (this.mediaRefsCleaned) return;
    mediaRefs = new SS2MediaRef[usedMediaRefs.size()];
    for (int i=0,n=usedMediaRefs.size(); i<n; i++) {
      mediaRefs[i] = (SS2MediaRef) usedMediaRefs.elementAt(i);
    }
    usedMediaRefs = null;
    mediaRefsCleaned = true;
  }
  
  /** Corresponds to a ParticipantPane in the DB file */
  SS2ParticipantSegment parseParticipantSegment(DataInputStream dis)
  throws IOException {
    int trackNumber = 0; // used to set track's ordering to match SS2 display
    int participantID = readU16(dis);
    boolean primary = readBoolean(dis);
    boolean visible = readBoolean(dis); // discard
    if (visible) {
      int height = readU16(dis);        // discard
      int upperHeight = readU16(dis);   // discard
      int fixedHeight = readU16(dis);   // discard
      int lowerHeight = readU16(dis);   // discard
    }
    
    SS2ParticipantSegment segment = new SS2ParticipantSegment();
    
    segment.participant = participants[participantID];
    
    if (!primary) {
      segment.primary = false;
    }
    
    int numTracks = readU16(dis);
    SS2Track[] tracks = new SS2Track[numTracks];
    segment.tracks = tracks;
    // need to cache free text tracks so we can make their end time
    // equal to the segment's end time -- a slight hack
    Vector freeTextAnnotations = new Vector();
    
    for (int i=0, n=numTracks; i<n; i++) {
      SS2Track track = new SS2Track();
      tracks[i] = track;
      
      int version = readU16(dis);
      Assert.check(version == FIELD_CURR_SCRAP_FORMAT_VERSION);
      
      track.group  = readU16(dis);
      track.fieldID = readU16(dis);
      FIELD = track.fieldID;
      track.order = trackNumber++;
      if (track.group == SS2FieldSpec.GROUP_FIXED) {
        track.fieldID += SS2FieldSpec.GROUP_FIXED_ID_OFFSET;
        track.order   += SS2FieldSpec.GROUP_FIXED_ID_OFFSET;
      }
      else if (track.group == SS2FieldSpec.GROUP_LOWER) {
        track.fieldID += SS2FieldSpec.GROUP_LOWER_ID_OFFSET;
        track.order   += SS2FieldSpec.GROUP_LOWER_ID_OFFSET;
      }
      
      track.visible = readBoolean(dis);
      int dataLength = readU32(dis);
      
      SS2Annotation[] items = null;
      SS2Field field = fieldSpec.getField(track.fieldID);
      track.type = field.type;
      track.name = field.name;
      
      if (dataLength > 0) {
        switch (track.type) {
          case SS2FieldSpec.TYPE_NON_MANUAL:
            items = readNonManualTrackData(dis, field);
            break;
          case SS2FieldSpec.TYPE_GLOSS:
            items = readGlossTrackData(dis, track.fieldID);
            break;
          case SS2FieldSpec.TYPE_FREE_TEXT:
            SS2Annotation annotation = new SS2Annotation(
              readFreeTextTrackData(dis), 0, currentUtteranceDuration
              );
            items = new SS2Annotation[] { annotation };
            break;
          default:
            throw new RuntimeException("Track doesn't match any known type");
        } // end switch(group)
        
        track.annotations = items;
      } else {
        track.annotations = new SS2Annotation[0];
      }
    }
    java.util.Arrays.sort(tracks, new java.util.Comparator() {
      public int compare(Object a, Object b) {
        return ((SS2Track)a).order - ((SS2Track)b).order;
      }
    });
    return segment;
  }
  
  // returns an array of items referencing scheme fields *and* values
  SS2Annotation[] readNonManualTrackData(DataInputStream dis, SS2Field field)
  throws IOException {
    int version = readU16(dis);
    Assert.check(version == NON_MANUAL_DATA_CURR_SCRAP_FORMAT_VERSION);
    
    boolean invalidAnnotations = false;
    
    Vector items = new Vector(); // many annotations in the DB file are empty placeholders
    int numAnnotations = readU16(dis);
    for (int i=0; i<numAnnotations; i++) {
      int startTime = (int) (readTimePoint(dis) - UTTERANCE.startTime);
      int endTime   = (int) (readTimePoint(dis) - UTTERANCE.startTime);
      int valueID = readU16(dis);
      SS2Value value = field.getValue(valueID);
      
      if (value == null) {
        invalidAnnotations = true;
        // SHOULD log and identify
        continue;
      }
      /* if ( value.label.equals("e") ) {
        value = SS2Value.OFFSET; 
      } else if (value.label.equals("s") ) {
        value = SS2Value.ONSET; 
      } else */ 
      if (startTime == endTime) {
        endTime += 33; // assume a frame rate of 30fps
      }
      currentUtteranceDuration = Math.max(currentUtteranceDuration, endTime);
      
      SS2Annotation annotation = new SS2Annotation(
        value, startTime, endTime
      );
      items.addElement(annotation);
    }
    
    SS2Annotation[] annotations = new SS2Annotation[items.size()];
    for (int i=0, n=items.size(); i<n; i++) {
      SS2Annotation anno = (SS2Annotation) items.elementAt(i);
      
      if (anno.value == SS2Value.ONSET && items.size() > i+1) {
          anno = new SS2Annotation(
            SS2Value.ONSET, 
            anno.startTime, ((SS2Annotation) items.elementAt(i+1)).startTime);
      }    
      else if (anno.value == SS2Value.OFFSET && i > 0) {
        anno = new SS2Annotation(
            SS2Value.OFFSET, 
            ((SS2Annotation) items.elementAt(i-1)).endTime, anno.endTime);
      }     
      annotations[i] = anno;
    } 
    
    return annotations;
  } // end readNonManualTrackData()
  
  
  // returns an array of items referencing fields but with free text, not scheme values
  SS2Annotation[] readGlossTrackData(DataInputStream dis, int fieldID)
  throws IOException {
    int version = readU16(dis);
    Assert.check(version == GLOSS_CURR_SCRAP_FORMAT_VERSION);
    
    Vector items = new Vector(); // many annotations in the DB file are empty placeholders
    int numAnnotations = readU16(dis);
    for (int i=0; i<numAnnotations; i++) {
      
      Object value = readLongPascalString(dis);
      
      if (value != null && ((String)value).length() > 0 ) {
        String styleScrap = readLongPascalString(dis); // discard
          
        boolean hold = false;
        long[] times = readClipTimes(dis, false);
        
        int startTime = (int) times[0] - (int) UTTERANCE.startTime;
        int endTime =   (int) times[1] - (int) UTTERANCE.startTime;
        if (endTime >= 0)
          hold = readBoolean(dis);
        
        if (startTime >= 0 || endTime >= 0) {
          if (startTime < 0) startTime = endTime;
          else if (endTime < 0) endTime = startTime;
          if (hold) {
            // HACK: set start time to end time of last annotation
            startTime = ((SS2Annotation)items.lastElement()).endTime;
            value = SS2Value.HOLD;
          } else if (startTime == endTime) {
            endTime += 33; //assume 30 fps
          }
          currentUtteranceDuration = Math.max(currentUtteranceDuration, endTime);
          SS2Annotation annotation = new SS2Annotation(
            value, startTime, endTime);
          items.addElement(annotation);
        }
      }
    }
    SS2Annotation[] annotations = new SS2Annotation[items.size()];
    Iterator it = items.iterator();
    int index = 0;
    while (it.hasNext()) {
      annotations[index++] = (SS2Annotation) it.next();
    }
    return annotations;
  } // end readTrack()
  
  
  // returns a single String of text for the track
  static String readFreeTextTrackData(DataInputStream dis)
  throws IOException {
    int version = readU16(dis);
    Assert.check(version == 0); // This seems to be only version in DB files.
    
    String text = readLongPascalString(dis);
    String styleScrap = readLongPascalString(dis); // discard
    
    return text;
  } // end readTrack()
  
  
  static String readNotes(DataInputStream dis)
  throws IOException {
    String text = readLongPascalString(dis);
    if (text != null && text.length() > 0) {
      String styleScrap = readLongPascalString(dis); // discard
      int selStart  = readU16(dis); // discard
      int selEnd    = readU16(dis); // discard
    }
    return text;
  } // end readNotes()
  
  
  
  static long[] readClipTimes(DataInputStream dis, boolean unconditional)
  throws IOException {
    long[] times = new long[2];
    times[0] = -1;
    times[1] = -1;
    if (unconditional || readBoolean(dis)) times[0] = readTimePoint(dis);
    if (unconditional || readBoolean(dis)) times[1] = readTimePoint(dis);
    return times;
  }
  
  
  /** Reads a Quicktime time-stamp and returns the value in milliseconds.
   @see MediaRef
   */
  
  static long readTimePoint(DataInputStream dis)
  throws IOException {
    int frame = readU32(dis);
    long value = readU64(dis);
    Assert.check(value <= Integer.MAX_VALUE);
    Assert.check(frame == value);
    int scale = readU32(dis);
    int base = readU32(dis);
    return SS2MediaRef.convertValueToMilliseconds(scale, value);
  } // end readTimePoint()
  
  
  static void readWindowStateInfo(DataInputStream dis)
  throws IOException {
    // FROM "Window State scrap.h":
    // #define  CURRENT_WIN_STATE_SCRAP_VERSION  ( 0 )
    int version = readU16(dis);
    Assert.check(version == 0);
    
    int size = readU32(dis);
    boolean windowSized = readBoolean(dis);
    int left, top, right, bottom;
    boolean maximized;
    if (windowSized) {
      maximized = readBoolean(dis);
      left = readU16(dis);
      top = readU16(dis);
      right = readU16(dis);
      bottom = readU16(dis);
    }
  }
  
  public Object toXMLNode() {
    Element el = null;
    Element element = new Element("SIGNSTREAM-DATABASE");
    element.setAttribute("SIGNSTREAM-VERSION", "2");
    element.setAttribute(XMLConstant.SOURCE, sourceLocation);
    if (!dbProfile.defaultFilename)
      element.setAttribute(XMLConstant.NAME, dbProfile.filename);
    element.setAttribute("DATABASE-VERSION", dbProfile.getVersionText());
    
    if (dbProfile.distributor != null && dbProfile.distributor.length() > 0) {
      el = new Element(XMLConstant.DISTRIBUTOR);
      el.addContent(dbProfile.distributor);
      element.addContent(el);
    }
    
    if (dbProfile.author != null && dbProfile.author.length() > 0) {
      el = new Element(XMLConstant.AUTHOR);
      el.addContent(dbProfile.author);
      element.addContent(el);
    }
    
    if (dbProfile.citation != null && dbProfile.citation.length() > 0) {
      el = new Element(XMLConstant.CITATION);
      el.addContent(dbProfile.citation);
      element.addContent(el);
    }
    
    if (dbProfile.notes != null && dbProfile.notes.length() > 0) {
      el = new Element(XMLConstant.NOTES);
      el.addContent(dbProfile.notes);
      element.addContent(el);
    }
    
    Element participantsEl = new Element(XMLConstant.PARTICIPANTS);
    for (int i=0; i<participants.length; i++) {
      if (participants[i] != null)
        participantsEl.addContent((Element)participants[i].toXMLNode());
    }
    element.addContent(participantsEl);
    
    element.addContent((Element)fieldSpec.toXMLNode());
    
    Element mediaRefsElement = new Element("MEDIA-FILES");
    Hashtable mediaRefsByID = new Hashtable();
    for (int i=0;i<mediaRefs.length; i++) {
      if (mediaRefs[i].isRealRef() &&
      mediaRefsByID.get(new Integer(mediaRefs[i].id)) == null ) {
        mediaRefsElement.addContent((Element)mediaRefs[i].toXMLNode());
        mediaRefsByID.put(new Integer(mediaRefs[i].id), mediaRefs[i]);
      }
    }
    element.addContent(mediaRefsElement);
    
    Element utterancesEl = new Element(XMLConstant.UTTERANCES);
    for (int i=0;i<utterances.length; i++) {
      utterancesEl.addContent((Element)utterances[i].toXMLNode());
    }
    element.addContent(utterancesEl);
    
    return element;
  }
  
  
  public Object fromXMLNode(Object xmlNode)
  throws IllegalArgumentException {
    if (!(xmlNode instanceof Element))
      throw new IllegalArgumentException();
    
    Element element = (Element) xmlNode;
    String elementName = element.getName();
    if (!elementName.equals("SIGNSTREAM-DATABASE"))
      throw new IllegalArgumentException();
    
    sourceLocation = element.getAttributeValue(XMLConstant.SOURCE);
    
    dbProfile = new SS2DBProfile();
    dbProfile.versionText = element.getAttributeValue(XMLConstant.VERSION);
    dbProfile.author = element.getChildText(XMLConstant.AUTHOR);
    dbProfile.citation = element.getChildText(XMLConstant.CITATION);
    dbProfile.distributor = element.getChildText(XMLConstant.DISTRIBUTOR);
    dbProfile.notes = element.getChildText(XMLConstant.NOTES);
    // dbProfile.filename = element.getAttributeValue(XMLConstant.NAME);
    
    fieldSpec = new SS2FieldSpec();
    fieldSpec.fromXMLNode(element.getChild("FIELD-SPEC"));
    
    participants = new SS2Participant[0];
    mediaRefs = new SS2MediaRef[0];
    
    utterances = new SS2Utterance[0];
    return this;
  }
  
  
  
  
  /** maintains the current position in the {@link DataInputStream} passed
   to {@link #readFromStream} */
  static int                       filePointer = 0;
  /** maintains a stack of file markers in the {@link DataInputStream} passed
   to {@link #readFromStream}, for cases of multiply nested {@link #REFERENCED_CHUNK}s */
  static Stack                     fileMarkers = new Stack();
  
  ////// PRIMITIVE READ METHODS
  
  /** Reads a 4-character ASCII string, used as the file signature in SS2 files
   (and many other binary files). For SignStream 2 files, this string must be
   "sASL".
   @see SS2File
   */
  static final String readSignature( DataInputStream dis )
  throws IOException {
    filePointer += 4;
    byte[] buffer = new byte[4];
    dis.readFully( buffer );
    return new String( buffer );
  }
  
  /** Reads 1 byte from the supplied stream, returning an unsigned int. */
  static final int readU8( DataInputStream dis )
  throws IOException {
    filePointer += 1;
    return dis.readByte();
  }
  
  /** Reads 2 bytes from the supplied stream, returning an unsigned int. */
  static final int readU16( DataInputStream dis )
  throws IOException {
    filePointer += 2;
    return ( int ) dis.readShort();
  }
  
  /** Reads 4 bytes from the supplied stream, returning an unsigned int. */
  static final int readU32( DataInputStream dis )
  throws IOException {
    filePointer += 4;
    return dis.readInt();
  }
  
  /** Reads 8 bytes from the supplied stream, returning an unsigned long. */
  static final long readU64( DataInputStream dis )
  throws IOException {
    filePointer += 8;
    return dis.readLong();
  }
  
  /** Reads 2 bytes into a boolean value. */
  static final boolean readBoolean( DataInputStream dis )
  throws IOException {
    filePointer += 2;
    return dis.readShort() != 0;
  }
  
  /** Reads a 1 byte length and then the corresponding number of ASCII characters,
   returning these as a String. */
  static final String readPascalString( DataInputStream dis )
  throws IOException {
    int length = ( int ) dis.readByte();
    if (length < 0)
      throw new IOException("Read length byte of " + length);
    byte[] buffer = new byte[length];
    dis.readFully( buffer );
    filePointer += length + 1;
    return new String( buffer );
  }// end readPascalString()
  
  
  /** Same as {@link #readPascalString}, only uses a 4 byte length header. */
  static final String readLongPascalString( DataInputStream dis )
  throws IOException {
    int length = dis.readInt();
    try {
      byte[] buffer = new byte[length];
      dis.readFully( buffer );
      filePointer += length + 4;
      return new String( buffer );
      
    } catch (EOFException eofe) {
      Log.error("readLongPascalString() read length "+length);
      throw eofe;
    }
    
  }// end readPascalString()
  
  
  /** Reads 24 ASCII characters from the supplied stream and returns a String. */
  static final String readString24Byte( DataInputStream dis )
  throws IOException {
    byte[] buffer = new byte[24];
    dis.readFully( buffer );
    String b = new String( buffer );
    filePointer += 24;
    return b;
  }
  
  
  
  static final void jumpToFilePosition(DataInputStream dis, int offset)
  throws IOException {
    fileMarkers.push( new Integer( filePointer ) );
    // move to new offset in stream
    dis.reset();
    dis.skipBytes( offset );
    filePointer = offset;
  } // end jumpToFilePosition()
  
  static final void restoreLastFilePosition(DataInputStream dis)
  throws IOException {
    filePointer = ( ( Integer ) fileMarkers.pop() ).intValue();
    dis.reset();
    dis.skipBytes( filePointer );
  } // end restoreLastFilePosition()
  
  
}

