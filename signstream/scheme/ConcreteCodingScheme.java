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
 * ConcreteCodingScheme.java
 *
 * Created on November 12, 2002, 8:09 PM
 */

package signstream.scheme;

import signstream.exception.*;
import signstream.utility.*;

import org.jdom.Element;

import java.util.*;
import java.io.Serializable;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 *
 */
class ConcreteCodingScheme implements CodingScheme {
  private String name = null;
  private int version = -1;
  private String description = "";
  private String notes = "";
  private String organization = "";
  private Date versionDate = null;
  private Properties properties = new Properties();
  PropertyChangeSupport propertyChangeSupport = null;
  
  private Vector schemeFields    = new Vector();
  private Hashtable fieldsByName = new Hashtable();
  
  
  ConcreteCodingScheme(String name) {
    this.name = name;
    propertyChangeSupport = new PropertyChangeSupport(this);
  }
  
  /// SHOULD be replaced with fromXMLNode()
  ConcreteCodingScheme(Object xmlNode) {
    this(((Element)xmlNode).getAttributeValue(XMLConstant.NAME));
    
    Element element = (Element) xmlNode;
    Assert.check(element.getName().equals(XMLConstant.CODING_SCHEME));
    Iterator fields = element.getChildren(XMLConstant.CS_FIELD).iterator();
    while (fields.hasNext()) {
      Element fieldEl = (Element) fields.next();
      SchemeField field = new ConcreteField(fieldEl);
      schemeFields.addElement(field);
      fieldsByName.put(field.getName(), field);
    }
  }
  
  Object toXMLNode() {
    Element element = new Element(XMLConstant.CODING_SCHEME);
    element.setAttribute(XMLConstant.NAME, name);
    Iterator it = schemeFields.iterator();
    while (it.hasNext()) {
      ConcreteField field = (ConcreteField) it.next();
      element.addContent((Element) field.toXMLNode());
    }
    return element;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    propertyChangeSupport.firePropertyChange(XMLConstant.NAME, this.name, name);
    this.name = name;
  }
  public void setVersion(int version) {
    propertyChangeSupport
    .firePropertyChange(XMLConstant.VERSION, this.version, version);
    this.version = version;
  }
  public int getVersion() {
    return version;
  }
  public Date getVersionDate() {
    return versionDate;
  }
  public void setVersionDate(Date date) {
    versionDate = date;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    propertyChangeSupport
    .firePropertyChange(XMLConstant.DESCRIPTION, this.description, description);
    this.description = description;
  }
  public String getOrganization() {
    return organization;
  }
  public void setOrganization(String organization) {
    propertyChangeSupport
    .firePropertyChange(XMLConstant.ORGANIZATION, this.organization, organization);
    this.organization = organization;
  }
  public String getNotes() {
    return notes;
  }
  public void setNotes(String notes) {
    propertyChangeSupport
    .firePropertyChange(XMLConstant.NOTES, this.notes, notes);
    this.notes = notes;
  }
  
  public String getProperty(String propertyName) {
    return (String) properties.get(propertyName);
  }
  public Map getProperties() {
    return Collections.unmodifiableMap(properties);
  }
  public void addProperty(String name, String value) {
    Assert.check(name != null);
    properties.setProperty(name, value);
    propertyChangeSupport.firePropertyChange(XMLConstant.CS_PROPERTY, null, null);
  }
  public void removeProperty(String propertyName) {
    properties.remove(propertyName);
    propertyChangeSupport.firePropertyChange(XMLConstant.CS_PROPERTY, null, null);
  }
  
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }
  
  
  
  public SchemeField getSchemeField(String fieldName) {
    return (SchemeField) fieldsByName.get(fieldName);
  }
  public List getSchemeFields() {
    return Collections.unmodifiableList(schemeFields);
  }
  public SchemeField getSchemeField(int fieldID) {
    Iterator it = schemeFields.iterator();
    while (it.hasNext()) {
      SchemeField field = (SchemeField) it.next();
      if (field.getID() == fieldID) return field;
    }
    return null;
  }
  public SchemeField addEquivalentSchemeField(SchemeField sf) {
    Object existingField = fieldsByName.get(sf.getName());
    if (existingField != null) return (SchemeField) existingField;
    
    ConcreteField field = (ConcreteField) ((ConcreteField)sf).deepCopy();
    schemeFields.addElement(field);
    fieldsByName.put(field.getName(), field);
    field.parentScheme = this;
    field.setID(getNextFieldID());
    propertyChangeSupport
    .firePropertyChange(CodingSchemeListener.FIELD_PROPERTY, null, field);
    return field;
  }
  public void removeSchemeField(String fieldName) {
    ConcreteField field = (ConcreteField) fieldsByName.get(fieldName);
    if (field == null) return; // SHOULD log
    fieldsByName.remove(fieldName);
    schemeFields.remove(field);
    field.parentScheme = null;
    propertyChangeSupport
    .firePropertyChange(CodingSchemeListener.FIELD_PROPERTY, field, null);
  }
  
  
  private int getNextFieldID() {
    int maxID = -1;
    Iterator iterator = schemeFields.iterator();
    while (iterator.hasNext()) {
      SchemeField sf = (SchemeField) iterator.next();
      maxID = Math.max(maxID, sf.getID());
    }
    return maxID +1;
  }
  
  
  
  /// Object methods
  
  public String toString() {
    return getName();
  }
  
  public int hashCode() {
    return getName().hashCode();
  }
  
  /// equals()
}



class ConcreteField implements SchemeField {
  
  private int ID;
  CodingScheme parentScheme;
  private String name;
  private String label;
  private String category;
  private String notes;
  private String dataType; // candidate for type-safe enum ?
  private String timeAlignment;
  
  private Properties properties = new Properties();
  private Vector    schemeValues = new Vector();
  private Hashtable valuesByName = new Hashtable();
  
  ConcreteField(String name) {
    this.name = name;
  }
  
  ConcreteField(Object xmlNode) {
    Element element = (Element) xmlNode;
    Assert.check(element.getName().equals(XMLConstant.CS_FIELD));
    name = element.getAttributeValue(XMLConstant.NAME);
    label = element.getAttributeValue(XMLConstant.LABEL);
    dataType = element.getAttributeValue(XMLConstant.TYPE);
    timeAlignment = element.getAttributeValue(XMLConstant.TIME_ALIGNMENT);
    ID = Integer.parseInt(element.getAttributeValue(XMLConstant.ID));
    Iterator values = element.getChildren(XMLConstant.CS_VALUE).iterator();
    while (values.hasNext()) {
      Element valueEl = (Element) values.next();
      SchemeValue value = new ConcreteValue(valueEl);
      schemeValues.addElement(value);
      valuesByName.put(value.getName(), value);
    }
  }
  
  Object toXMLNode() {
    Element element = new Element(XMLConstant.CS_FIELD);
    element.setAttribute(XMLConstant.NAME, name);
    element.setAttribute(XMLConstant.ID, ""+ID);
    if (label != null) element.setAttribute(XMLConstant.LABEL, label);
    if (dataType != null && dataType.length() > 0) 
      element.setAttribute(XMLConstant.TYPE, dataType);
    if (timeAlignment != null && timeAlignment.length() > 0) 
      element.setAttribute(XMLConstant.TIME_ALIGNMENT, timeAlignment);
    
    // Iterator it = schemeValues.iterator();
    // while (it.hasNext()) {
    //  ConcreteValue value = (ConcreteValue) it.next();
    //  element.addContent((Element) value.toXMLNode());
    //}
    return element;
  }
  
  public int getID() {
    return ID;
  }
  public CodingScheme getCodingScheme() {
    return parentScheme;
  }
  
  public String getName() {
    return name;
  }
  public String getLabel() {
    return label == null ? name : label;
  }
  public String getCategory() {
    return category == null ? "" : category;
  }
  public String getNotes() {
    return notes == null ? "" : notes;
  }
  public String getDataType() {
    return dataType == null ? XMLConstant.TYPE_TEXT : dataType;
  }
  public String getTimeAlignment() {
    return timeAlignment == null ? XMLConstant.TIME_ALIGNMENT_ABSOLUTE : timeAlignment;
  }
  
  public SchemeValue addEquivalentSchemeValue(SchemeValue sv) {
    Object existingValue = valuesByName.get(sv.getName());
    if (existingValue != null) return (SchemeValue) existingValue;
    
    ConcreteValue value = (ConcreteValue) ((ConcreteValue)sv).deepCopy();
    schemeValues.addElement(value);
    valuesByName.put(value.getName(), value);
    value.parentField = this;
    value.setID(getNextValueID());
    //    getCodingScheme().propertyChangeSupport
    //      .firePropertyChange(CodingSchemeListener.VALUE_PROPERTY, null, value);
    return value;
  }
  
  public List getSchemeValues() {
    return Collections.unmodifiableList(schemeValues);
  }
  public SchemeValue getSchemeValue(int valueID) {
    Iterator it = schemeValues.iterator();
    while (it.hasNext()) {
      SchemeValue value = (SchemeValue) it.next();
      if (value.getID() == valueID) return value;
    }
    return null;
    
  }
  public SchemeValue getSchemeValue(String valueName) {
    return (SchemeValue) valuesByName.get(valueName);
  }
  
  public void addProperty(String name, String value) {
    Assert.check(name != null);
    properties.put(name, value);
    ///    getCodingScheme().propertyChangeSupport
    //      .firePropertyChange(XMLConstant.CS_PROPERTY, null, null);
  }
  public Map getProperties() {
    return Collections.unmodifiableMap(properties);
  }
  public String getProperty(String propertyName) {
    return (String) properties.getProperty(propertyName);
  }
  public void removeProperty(String propertyName) {
    properties.remove(propertyName);
    ///    getCodingScheme().propertyChangeSupport
    //      .firePropertyChange(XMLConstant.CS_PROPERTY, null, null);
  }
  
  //// DESTRUCTIVE METHODS
  
  public void setName(String name) {
    this.name = name;
  }
  public void setID(int id) {
    this.ID = id;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public void setCategory(String cat) {
    this.category = cat;
  }
  public void setNotes(String notes) {
    this.notes = notes;
  }
  public void setDataType(String type) {
    this.dataType = type;
  }
  public void setTimeAlignment(String timing){
    this.timeAlignment = timing;
  }
  
  public void removeSchemeValue(String valueName) {
    SchemeValue sv = (SchemeValue) valuesByName.remove(valueName);
    schemeValues.remove(sv);
  }
  
  
  SchemeField deepCopy() {
    ConcreteField clone = new ConcreteField(getName());
    clone.setName(name);
    // clone.setID(id); // SHOULD ???
    clone.setCategory(category);
    clone.setDataType(dataType);
    clone.setLabel(label);
    clone.setNotes(notes);
    clone.setTimeAlignment(timeAlignment);
    Iterator it = schemeValues.iterator();
    while (it.hasNext()) {
      ConcreteValue sv = (ConcreteValue) it.next();
      sv = (ConcreteValue) sv.deepCopy();
      clone.schemeValues.addElement(sv); // keep identical IDs?...
      clone.valuesByName.put(sv.getName(), sv);
    }
    it = properties.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      clone.properties.put(key, properties.getProperty(key));
    }
    return clone;
  }
  
  private int getNextValueID() {
    int maxID = -1;
    Iterator iterator = schemeValues.iterator();
    while (iterator.hasNext()) {
      SchemeValue sv = (SchemeValue) iterator.next();
      maxID = Math.max(maxID, sv.getID());
    }
    return maxID +1;
  }
  
  
  public String toString() {
    return getName();
  }
  
  public int hashCode() {
    return getName().hashCode();
  }
  
  /// equals()
}


class ConcreteValue implements SchemeValue, Serializable {
  
  private int ID;
  SchemeField parentField;
  private String name;
  private String label;
  private Object value;
  private Properties properties = new Properties();
  
  ConcreteValue(String name) {
    this.name = name;
    ID = -1;
  }
  
  ConcreteValue(Object xmlNode) {
    Element element = (Element) xmlNode;
    Assert.check(element.getName().equals(XMLConstant.CS_VALUE));
    name = element.getAttributeValue(XMLConstant.NAME);
    ID = Integer.parseInt(element.getAttributeValue(XMLConstant.ID));
    label = element.getAttributeValue(XMLConstant.LABEL);
  }
  
  Object toXMLNode() {
    Element element = new Element(XMLConstant.CS_VALUE);
    element.setAttribute(XMLConstant.NAME, name);
    element.setAttribute(XMLConstant.ID, ""+ID);
    if (label != null) element.setAttribute(XMLConstant.LABEL, label);
    return element;
  }
  
  SchemeValue deepCopy() {
    ConcreteValue clone = new ConcreteValue(name);
    clone.setID(ID);
    clone.setLabel(label);
    clone.setValue(value);
    Iterator it = properties.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      clone.properties.put(key, properties.getProperty(key));
    }
    return clone;
  }
  
  public int getID() {
    return ID;
  }
  public CodingScheme getCodingScheme() {
    if (parentField != null) return parentField.getCodingScheme();
    return null;
  }
  public SchemeField getSchemeField() {
    return parentField;
  }
  
  public String getName() {
    return name;
  }
  public String getLabel() {
    return label == null ? name : label;
  }
  public Object getValue() { // could be String, Icon, Number...
    return value == null ? name : value;
  }
  /** Should not clobber any existing property with the same name */
  public void addProperty(String name, String value) {
    properties.setProperty(name, value);
  }
  public Map getProperties() {
    return Collections.unmodifiableMap(properties);
  }
  public String getProperty(String propertyName) {
    return properties.getProperty(propertyName);
  }
  public void removeProperty(String name) {
    properties.remove(name);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  public void setID(int id) {
    this.ID = id;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public void setValue(Object value) { // could be String, Icon, Number...
    this.value = value;
  }
  
  
  
  
  public String toString() {
    return getName();
  }
  
  public int hashCode() {
    return getName().hashCode();
  }
  
  /// equals()
}