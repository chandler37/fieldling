/************************************************************************
 
 SignStream is an application for creating and viewing multi-tracked
 annotation transcripts from source video and other media,
 developed primarily for research on ASL and other signed languages.
 
 Signstream Copyright (C) 1997-2003 Boston University, Dartmouth
 College, and Rutgers the State University of New Jersey.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.
 
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

package signstream.application;

import signstream.asset.*;
import signstream.transcript.*;
import signstream.exception.*;
import signstream.io.ss2.*;
import signstream.scheme.*;
import signstream.io.XMLNodeSerializable;

import java.util.*;

/** The in-memory document of SignStream 3. For now corresponds to a single
 XML file on disk, though an SSDocument can also be created from a legacy
 SignStream 2 DB file.
 
 The document contains meta-data as well as a graph of segments, that contain
 tracks, that contain annotations, all of which reference a single coding
 scheme. 
 
 */
public class SSDocument implements XMLNodeSerializable {
  
  private String authorText = "";
  private String citationText = "";
  private String distributorText = "";
  private String notesText = "";
  private String versionText = "";
  private String filename = "";
  
  /** Will eventually be a CodingScheme instance; transitionally will be
   a SS2FieldSpec instance for imported legacy files. */
  private Object documentConstraints = new Object();
  
  /** Holds Segments */
  Vector segments = new Vector();
  
  // creates an empty document -- should load an empty "template" doc
  public SSDocument(CodingScheme scheme) {
    documentConstraints = scheme;
  }
  
  // creates a document based on a legacy SignStream file
  public SSDocument(SS2File ss2file) {
    mergeFieldSpecIntoCodingScheme(ss2file);
    
    importMediaRefsIntoPackage(ss2file);
    
    importSS2Utterances(ss2file);
    
    /// grab all meta-data
    SS2DBProfile dbProfile = ss2file.dbProfile;
    authorText = dbProfile.getAuthor();
    citationText = dbProfile.getCitation();
    distributorText = dbProfile.getDistributor();
    notesText = dbProfile.getNotes();
    versionText = dbProfile.getVersionText();
    filename = dbProfile.getFilename();
  }
  
  // returns the object to constrain a sequencer model (e.g. a CodingScheme)
  public Object getDocumentConstraints() {
    return documentConstraints;
  }
  
  
  private void mergeFieldSpecIntoCodingScheme(SS2File ss2file) {
    // merge field spec into local coding scheme
    //  merge/convert SS2Fields -> SchemeFields
    //    store original field ID in SchemeField
    //  merge/convert SS2Values -> SchemeValues
    //    store original value ID in SchemeValue
  }
  
  private void importMediaRefsIntoPackage(SS2File ss2file) {}
  
  private void importSS2Utterances(SS2File ss2file) {
    //   SS2Participants -> Participants
    
    
    //   SS2Utterances -> Segments
    for (int i=0, n=ss2file.getUtteranceCount(),
    segmentID=1; i<n; i++, segmentID++) {
      SS2Utterance utterance = ss2file.utterances[i];
      
      long startTime = utterance.startTime;
      long endTime = utterance.endTime;
      String notes = utterance.notes;
      
      // split multi-participant utterances into multiple segments
      for (int j=0, m=utterance.participantSegments.length; j<m; j++) {
        SS2ParticipantSegment ss2segment = utterance.participantSegments[j];
       
        Segment segment = new Segment();
        
        // should calculate full gloss excerpt
        segment.setGlossExcerpt(utterance.getExcerpt());
        segment.setNotes(utterance.getNotes()); // primary participant only?
        segment.setStartTime(startTime);
        segment.setEndTime(endTime);
        // participant
        
        //     SS2Tracks -> Tracks
        for (int k=0, o=ss2segment.tracks.length; k<o; k++) {
          SS2Track ss2track = ss2segment.tracks[k];
          Track track = new Track();
          track.setName(ss2track.getName());
          //track.setType(ss2track.getType());
          //track.setGroup(ss2track.getGroup());
          // field (for now, SS2Field, will be a SchemeField)
          track.setSS2Field(ss2file.fieldSpec.getField(ss2track.getFieldID()));
          
          //       SS2Annotations -> Annotations
          for (int l=0,p=ss2track.annotations.length; l<p; l++) {
            SS2Annotation ss2annotation = ss2track.annotations[l];
            Annotation annotation = new Annotation(ss2annotation.value, 
                               ss2annotation.startTime, ss2annotation.endTime);
            track.addAnnotation(annotation);
          } // end for each annotation
          segment.addTrack(track);
        } // end for each track
        segments.addElement(segment);
      } // end for each participant segment
    } // end for each utterance
  }
  
  public String getAuthorText() {
    return authorText;
  }
  public String getCitationText() {
    return citationText;
  }
  public String getDistributorText() {
    return distributorText;
  }
  public String getNotesText() {
    return notesText;
  }
  public String getFilename() {
    return filename;
  }
  public String getVersionText() {
    return versionText;
  }
  
  /** Transitionally used to test successful import of SS2File data. */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String nl = System.getProperty("line.separator");
    buffer.append("Author: "+nl);
    buffer.append(authorText+nl);
    buffer.append("Citation: "+nl);
    buffer.append(citationText+nl);
    
    for (int i=0,n=segments.size(); i<n; i++) {
      buffer.append(segments.elementAt(i).toString());
    }
    return buffer.toString();
  }
  
  public Object toXMLNode() {
    return null;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    return null;
  }
  
}
