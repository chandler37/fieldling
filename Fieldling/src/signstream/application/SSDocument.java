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
import org.jdom.Element;
import signstream.utility.XMLConstant;

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
  
  private CodingScheme codingScheme = null;
  /** Holds pairs of SS2Field->SchemeField and SS2Value->SchemeValue. 
   This is used during the import of legacy SS2 files. */
  private Hashtable fieldSpecToCodingScheme = new Hashtable();
  
  /** Holds Segments */
  Vector segments = new Vector();
  
  private boolean dirty = false;
  
  // creates an empty document -- should load an empty "template" doc
  public SSDocument(CodingScheme scheme) {
    codingScheme = scheme;
  }
  public SSDocument(Object xmlNode, CodingScheme scheme) {
    this(scheme);
    fromXMLNode(xmlNode);
  }
  
  // creates a document based on a legacy SignStream file
  public SSDocument(CodingScheme scheme, SS2File ss2file) {
    codingScheme = scheme;
    
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
    filename = ss2file.getSourceFileName();
  }
  
  // returns the object to constrain a sequencer model (e.g. a CodingScheme)
  public Object getDocumentConstraints() {
    return codingScheme;
  }
  
  
  private void mergeFieldSpecIntoCodingScheme(SS2File ss2file) {
    SS2FieldSpec fieldSpec = ss2file.fieldSpec;
    SS2Field[] ss2fields = fieldSpec.getFields();
    for (int i=0; i<ss2fields.length; i++) {
      SS2Field ss2field = ss2fields[i];
      SchemeField field = codingScheme.getSchemeField(ss2field.name);
      
      if (field == null) {
        field = CodingSchemeManager.createField(ss2field.name);
        field.setLabel(ss2field.label);
  //      field.setPrefix(ss2field.prefix);
        //field.setCategory("");
        //field.setDataType("");
        // field.setTimeAlignment("");        
        field = codingScheme.addEquivalentSchemeField(field);
      }
      if (ss2field.color != null) field.setColor(ss2field.color);
      if (ss2field.type == SS2FieldSpec.TYPE_NON_MANUAL) 
        field.setConstraint(SchemeField.CONSTRAINT_VALUE_SET);
      else 
        field.setConstraint(SchemeField.CONSTRAINT_FREE_TEXT);
      fieldSpecToCodingScheme.put(ss2field, field);
      
      if (ss2field.values != null) {
          // ss2field.type == SS2FieldSpec.TYPE_GLOSS | 
          // ss2field.type == SS2FieldSpec.TYPE_NON_MANUAL) {
        SS2Value[] ss2values = ss2field.values;
        for (int j=0; j<ss2values.length; j++) {
          SS2Value ss2value = ss2values[j];
          SchemeValue value = field.getSchemeValue(ss2value.name);
          
          if (value == null) {
            value = CodingSchemeManager.createValue(ss2value.name);
            value.setLabel(ss2value.label);
            value = field.addEquivalentSchemeValue(value);
          } 
          Assert.check(value != null & value instanceof SchemeValue);
          fieldSpecToCodingScheme.put(ss2value, value);
        } // end for each value
      } // end if value-set field
    } // end for each field
  } // end mergeFieldSpecIntoCodingScheme()
  
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
          //track.setType(ss2track.getType());
          //track.setGroup(ss2track.getGroup());
          
          SS2Field ss2field = ss2file.fieldSpec.getField(ss2track.getFieldID());
          // track.setSS2Field(ss2field);
          track.setField((SchemeField) fieldSpecToCodingScheme.get(ss2field));
          // track.setName(ss2track.getName());
          track.setName(ss2field.name);
          
          //       SS2Annotations -> Annotations
          for (int l=0,p=ss2track.annotations.length; l<p; l++) {
            SS2Annotation ss2annotation = ss2track.annotations[l];
            Object ss2value = ss2annotation.value;
            
            Object value = ss2value instanceof String ?
                           ss2value : fieldSpecToCodingScheme.get(ss2value);
            if (value != null) {
            Annotation annotation = 
              new Annotation(value, ss2annotation.startTime, ss2annotation.endTime);
            track.addAnnotation(annotation);
            }
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
  /** Has the document been modified since last save? */
  public boolean isDirty() {
    return dirty;
  }
  /** Sets the dirty state of the document. */
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
  
  public String toString() {
    return filename; // should add some info
  }
  
  public Object toXMLNode() {
    Element el = null;
    Element element = new Element("SIGNSTREAM-DATABASE");
    element.setAttribute(XMLConstant.FILENAME, filename);
    element.setAttribute("SIGNSTREAM-VERSION", "3");
    // element.setAttribute(XMLConstant.SOURCE, sourceLocation);
//    element.setAttribute("DATABASE-VERSION", versionText);
    element.setAttribute(XMLConstant.CODING_SCHEME, codingScheme.getName());
    
    if (distributorText != null && distributorText.length() > 0) {
      el = new Element(XMLConstant.DISTRIBUTOR);
      el.addContent(distributorText);
      element.addContent(el);
    }
    
    if (authorText != null && authorText.length() > 0) {
      el = new Element(XMLConstant.AUTHOR);
      el.addContent(authorText);
      element.addContent(el);
    }
    
    if (citationText != null && citationText.length() > 0) {
      el = new Element(XMLConstant.CITATION);
      el.addContent(citationText);
      element.addContent(el);
    }
    
    if (notesText != null && notesText.length() > 0) {
      el = new Element(XMLConstant.NOTES);
      el.addContent(notesText);
      element.addContent(el);
    }
    
    /* Element participantsEl = new Element(XMLConstant.PARTICIPANTS);
    for (int i=0; i<participants.length; i++) {
      if (participants[i] != null)
        participantsEl.addContent((Element)participants[i].toXMLNode());
    }
    element.addContent(participantsEl); */
    
    // element.addContent((Element)codingScheme.toXMLNode());
    
    /* Element mediaRefsElement = new Element("MEDIA-FILES");
    Hashtable mediaRefsByID = new Hashtable();
    for (int i=0;i<mediaRefs.length; i++) {
      if (mediaRefs[i].isRealRef() &&
      mediaRefsByID.get(new Integer(mediaRefs[i].id)) == null ) {
        mediaRefsElement.addContent((Element)mediaRefs[i].toXMLNode());
        mediaRefsByID.put(new Integer(mediaRefs[i].id), mediaRefs[i]);
      }
    }
    element.addContent(mediaRefsElement); */
    
    Element segmentsEl = new Element(XMLConstant.SEGMENTS);
    for (int i=0;i<segments.size(); i++) {
      Segment segment = (Segment) segments.elementAt(i);
      segmentsEl.addContent((Element)segment.toXMLNode());
    }
    element.addContent(segmentsEl);
    
    return element;
  }
  
  public Object fromXMLNode(Object xmlNode) throws IllegalArgumentException {
    Element element = (Element) xmlNode;
    filename = element.getAttributeValue(XMLConstant.FILENAME);
    // version should be 3
//    versionText = element.getAttributeValue("DATABASE_VERSION");
    codingScheme = CodingSchemeManager
      .getScheme(element.getAttributeValue(XMLConstant.CODING_SCHEME));
    Element el = element.getChild(XMLConstant.AUTHOR);
    if (el != null) authorText = el.getText();
    el = element.getChild(XMLConstant.DISTRIBUTOR);
    if (el != null) distributorText = el.getText();
    el = element.getChild(XMLConstant.CITATION);
    if (el != null) citationText = el.getText();
    el = element.getChild(XMLConstant.NOTES);
    if (el != null) notesText = el.getText();
    
    // participants
    
    // media refs/asset package
    
    el = element.getChild(XMLConstant.SEGMENTS);
    List segmentElements = el.getChildren(XMLConstant.SEGMENT);
    segments = new Vector();
    for (int i=0,n=segmentElements.size(); i<n; i++) {
      Element segmentEl = (Element) segmentElements.get(i);
      segments.addElement(new Segment(segmentEl, codingScheme));
    }
    
    dirty = false;
    return this;
  }
  
}
