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

package signstream.scheme;

import signstream.exception.*;
import signstream.utility.*;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;


/**
 A facade for the schemes package, responsible for loading and saving coding schemes
 as well as other scheme manipulations. 
 
 <B>NOTE:</B> this class is loaded with lava code. Read the code at your own peril; 
 the API will change as the code is refactored into a simpler design. The following
 description is indicative of the more current design.
 
 One or more coding schemes are stored locally and distiguished from each other
 solely by their unique names. The manager class supports the following operations:
 <UL>
 <LI>Get a named scheme. If the scheme exists locally, it is loaded into memory, 
 otherwise a new in-memory scheme is created.
 <LI>Save a scheme to disk.
 <LI>Discard an in-memory scheme without saving. 
 <LI>Revert an in-memory scheme to the last saved state.
 <LI>Load a scheme from outside the local collection -- e.g. from a URL. If a scheme
 of the same name exists in the local collection, the two schemes are merged.
 <LI>Create a new scheme field or value, not attached to any scheme.
 </UL>
 
 Because the coding scheme implementation is package-private, other packages
 must use the manager to instantiate and manipulate schemes (or violate the 
 design's intent by implementing their own CodingScheme classes).
 
 The "sandbox" is an older idea to be removed, which was designed to allow the 
 user to import schemes from external sources without corrupting any local schemes
 of the same name. Such cases involved creating a new scheme merging the imported 
 and existing schemes, which would then be stored in the "sandbox" until the user
 had approved or rejected any additions to the scheme, at which point they could
 move the changes from the sandbox into the local scheme. This design increases
 complexity more than it reduces complexity, and so was discarded. 
 
 The newer design solution for importing schemes is to timestamp and mark the source 
 of all additions to local schemes, thus allowing the user to audit a scheme
 for changes made since a certain date or from a specific source. A coding scheme
 editor will be incorporated into the application to support such functionality.
 
 Two other functions of the manager may be supported but have not been designed
 completely as the use cases remain unclear. These are merging of schemes
 and diffing of two different schemes. The former would result in a union of 
 the schemes; the latter a difference (used within a single session to highlight 
 changes to a scheme). 
 
 */
public final class CodingSchemeManager
{
  
  ////////////////////
  ////   CONSTANTS
  ////////////////////
  
  private static final File schemesDir 
    = new File(Utilities.getSignStreamDir(), "schemes");
  private static final File sandboxDir 
    = new File(Utilities.getSignStreamDir(), "sandbox");
  private static final String FILE_EXT       = ".xml";
  /** Sandboxed schemes' names are a concatenation of the base name (e.g. ASLLRP),
   the source (e.g. Carol), and import date (YYYY-MM-DD HH:MM:SS). */
  private static final char SANDBOX_NAME_SEPARATOR = '~';
  public static final String DEFAULT_SCHEME = "default";
  
  private static final Hashtable schemes = new Hashtable();
  
  ////////////////////////////////////
  ////   CONSTRUCTION (Singleton)
  ////////////////////////////////////
  
  private static final CodingSchemeManager instance = new CodingSchemeManager();
  private CodingSchemeManager()
  {}
public static CodingSchemeManager getInstance()
  { return instance; }
  
  
  /////////////////////////////////////
  ///    BASIC SCHEME ACCESS
  /////////////////////////////////////
  
  
  /// should combine getLocal, getSandboxed, and getSchemes into one method
  /// with a flag parameter ??
  
  /** Returns the list of all local scheme names stored on disk 
   (excluding backup files) */
  public static List getLocalSchemeNames()
  {
    File schemesDir = getSchemesDir();
    String[] names = schemesDir.list(new FilenameFilter()
    {
      // SHOULD make this less naive; only accept legal scheme names
      public boolean accept(File f, String name)
      {
        if (isLocalName(name) && name.endsWith(FILE_EXT)) return true;
        return false;
      }
    });
    
    for (int i=0; i<names.length; i++)
    {
      names[i] = names[i].substring(0, names[i].indexOf(FILE_EXT));
    }
    return Arrays.asList(names);
  }
  
  /** Returns the list of all sandboxed scheme names stored on disk 
   (excluding backup files) */
  public static List getSandboxedSchemeNames()
  {
    File schemesDir = getSandboxDir();
    String[] names = schemesDir.list(new FilenameFilter()
    {
      // SHOULD make this less naive; only accept legal scheme names
      public boolean accept(File f, String name)
      {
        if (isSandboxedName(name) && name.endsWith(FILE_EXT)) return true;
        return false;
      }
    }); 
    Assert.check(names != null);
    
    for (int i=0; i<names.length; i++)
    {
      names[i] = names[i].substring(0, names[i].lastIndexOf('.'));
    }
    return Arrays.asList(names);
  }
  
  /** Returns the default (aka "open") scheme */
  public static CodingScheme getDefaultScheme()
  {
    CodingScheme defaultScheme = getScheme(DEFAULT_SCHEME); 
    Assert.check(defaultScheme != null);
    return defaultScheme;
  }
  
  /** Loads and caches scheme (local or sandboxed) if not loaded,
   and returns cached scheme
   */
  public static CodingScheme getScheme(String name) 
  {
    Assert.check(name != null && name.length() > 0);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    CodingScheme cs = (CodingScheme) schemes.get(name);
    if (cs == null)
      cs = loadScheme(name);

    if (cs == null) {
      cs = createScheme(name);
    }
    cacheScheme(cs, true); // clobber should be irrelevant here
    
    return cs;
  }
  
  
  /** Returns all cached local schemes -- does not load anything */
  public static List getCachedLocalSchemes()
  {
    List localSchemes = new Vector();
    /// iterate thru cache and grab only local
    Iterator iterator = schemes.values().iterator();
    while (iterator.hasNext())
    {
      CodingScheme cs = (CodingScheme) iterator.next();
      if (isLocalName(cs.getName())) localSchemes.add(cs);
    }
    return Collections.unmodifiableList(localSchemes);
  }
  
  /** Returns all cached sandboxed schemes -- does not load anything */
  public static List getCachedSandboxedSchemes()
  {
    List sandboxedSchemes = new Vector();
    /// iterate thru cache and grab only sandboxed
    Iterator iterator = schemes.values().iterator();
    while (iterator.hasNext())
    {
      CodingScheme cs = (CodingScheme) iterator.next();
      if (isSandboxedName(cs.getName())) sandboxedSchemes.add(cs);
    }
    return Collections.unmodifiableList(sandboxedSchemes);
  }
  
  /** Returns all cached schemes */
  public static List getCachedSchemes()
  {
    return Collections.unmodifiableList(new Vector(schemes.values()));
  }
  
  public static void saveCachedSchemes() {
    Iterator it = schemes.keySet().iterator();
    while (it.hasNext()) {
      String schemeName = (String) it.next();
      saveScheme(schemeName);
    }
  }
  
  /** Saves the cached scheme either locally or in sandbox depending on its name */
  public static void saveScheme(String name)
  {
    Assert.check(name != null && name.length() >0);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    CodingScheme cachedScheme = (CodingScheme) schemes.get(name);
    Assert.check(cachedScheme != null);
    
    File directory = isLocalName(name) ? getSchemesDir() : getSandboxDir();
    String saveFileName = name + FILE_EXT;
    String backupFileName = saveFileName + "~";
    File saveFile = new File(directory, saveFileName);
    if (saveFile.exists())
    {
      // try {
      // SHOULD do this in platform independent way...
      // Runtime.getRuntime().exec("cp "+saveFileName + " "+backupFileName);
      // } catch (IOException ioe) { ioe.printStackTrace(); }
    }
    saveScheme(cachedScheme, saveFile);
  }
  
  
  ///////////////////////////////////
  ///    CACHE/DISK MANAGEMENT
  ///////////////////////////////////
  
  
  /** Removes any matching scheme from the cache */
  public static void expungeScheme(String name)
  {
    Assert.check(name != null && name.length() >0);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    schemes.remove(name);
  }
  /** Discards any existing matching scheme from the cache and reloads the
   scheme from disk */
  public static void reloadScheme(String name)
  {
    Assert.check(name != null && name.length() >0);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    expungeScheme(name);
    getScheme(name); //, true); // should not return this; client code must
    // make another call to getScheme with destructive set
  }
  /** Removes any matching scheme from disk and removes from the cache if
   loaded -- retains backup file */
  public static void deleteScheme(String name)
  {
    Assert.check(name != null && name.length() >0);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    boolean local = isLocalName(name);
    expungeScheme(name);
    File directory  = local ? getSchemesDir() : getSandboxDir();
    File schemeFile = new File(directory, name+FILE_EXT);
    // Utilities.backupFile(schemeFile);
    schemeFile.delete();
  }
  
  /** Shouldn't be used directly by client code except for schemes loaded from
   external URLs  Conditionally puts cs in the cache, unless clobber is false and there
   exists a scheme with the same logical name as cs (as returned by getName()) */
  public static void cacheScheme(CodingScheme cs, boolean clobber)
  {
    Assert.check(cs != null);
    Assert.check(isLocalName(cs.getName()) || isSandboxedName(cs.getName()));
    
    CodingScheme cachedScheme = (CodingScheme) schemes.get(cs.getName());
    if (cachedScheme == null || clobber == true) {
      // if (cs instanceof NonDestructiveScheme) 
      //  cs = ((NonDestructiveScheme)cs).getDestructiveScheme();
      schemes.put(cs.getName(), cs);
      
    }
  }
  /** Should not be used directly by client code unless importing a scheme from
   outside the local or sandbox areas.
   This implementation does *not* cache the loaded scheme.
   */
  public static CodingScheme loadScheme(URL schemeUrl)
  {
    Assert.check(schemeUrl != null);
    
    CodingScheme cs = null;
    /// build a JDOM document from the XML file
    org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
    org.jdom.Document xmlDoc = null;
    builder.setValidation(false);
    try
    {
      xmlDoc = builder.build(schemeUrl);
    } catch (org.jdom.JDOMException jde) {
      jde.printStackTrace();
      return null;
    } 
    return new ConcreteCodingScheme(xmlDoc.getRootElement());
  }
  
  /** UNIMPLEMENTED Writes the scheme to a URL for cases when schemes need to be exported
   outside the local and sandboxed sets */
  public static void saveScheme(CodingScheme cs, URL saveUrl)
  {
  
  }
  
  
  ////////////////////////
  ////   INTERNAL
  ////////////////////////
  
  
  static File getSchemesDir()
  {
    return schemesDir;
  }
  static File getSandboxDir()
  {
    return sandboxDir;
  }
  
  private static CodingScheme loadScheme(String name)
  {
    Assert.check(name != null);
    Assert.check(isLocalName(name) || isSandboxedName(name));
    
    CodingScheme cs = null;
    boolean local = isLocalName(name);
    try
    {
      File directory = local ? getSchemesDir() : getSandboxDir();
      URL schemeURL = new File(directory, name+FILE_EXT).toURL();
      cs = loadScheme(schemeURL);
    } catch (java.net.MalformedURLException mue)
    {
      mue.printStackTrace();
    }
    
    if (cs != null)
      cs.setName(name); // filename overrides internally stored name
    return cs;
  }
  
  private static void saveScheme(CodingScheme cs, File saveFile)
  {
    Assert.check(cs != null);
    Assert.check(saveFile != null);
     
    try {
      FileOutputStream fos = new FileOutputStream(saveFile);
      org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(" ", true);
      org.jdom.Element root 
        = (org.jdom.Element) ((ConcreteCodingScheme)cs).toXMLNode();
      outputter.output(new org.jdom.Document(root), fos); 
    } catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  } // end saveScheme(CodingScheme, File)
  
  
  ////////////////////////////////
  ///   SCHEME NAME STUFF (Manipulating local vs. sandboxed names)
  ////////////////////////////////
  
  
  public static boolean isLocalName(String name)
  {
    Assert.check(name != null && name.length() > 0);
    
    // naive: if it doesn't have the special sandbox char, it's a local name
    return name.indexOf(SANDBOX_NAME_SEPARATOR) < 0;
  }
  
  public static boolean isSandboxedName(String name)
  {
    Assert.check(name != null && name.length() > 0);
    
    if (isLocalName(name)) return false;
    char[] chars = name.toCharArray();
    int separatorCount = 0;
    for (int i=0; i<chars.length; i++)
    {
      if (chars[i] == SANDBOX_NAME_SEPARATOR) separatorCount++;
    }
    return separatorCount == 2;
  } 
  
  public static String nameFromSandboxedName(String name)
  {
    Assert.check(isSandboxedName(name)); // COULD allow a local name here
    
    return name.substring(0,name.indexOf(SANDBOX_NAME_SEPARATOR));
  }
  public static String sourceFromSandboxedName(String name)
  {
    Assert.check(isSandboxedName(name)); // COULD NOT allow a local name here
    
    return name.substring(
      name.indexOf(SANDBOX_NAME_SEPARATOR),
      name.lastIndexOf(SANDBOX_NAME_SEPARATOR));
  }
  public static Date dateFromSandboxedName(String name)
  {
    Assert.check(isSandboxedName(name)); // COULD allow a local name here
    
    String dateString = name.substring(name.lastIndexOf(SANDBOX_NAME_SEPARATOR));
    Date date = null;
    if (dateString.length() > 0) {
      try
      {
        date = XMLConstant.DATE_FORMAT.parse(dateString);
      } catch (java.text.ParseException pe)
      {
        pe.printStackTrace();
      }
    }
    return date;
  }

  public static String constructSandboxedName(CodingScheme localScheme)
  {
    Assert.check(localScheme != null);
    
    String schemeName = localScheme.getName();
    if (isSandboxedName(schemeName)) return schemeName;
    
    String source = localScheme.getProperty(SchemeConstant.SOURCE);
    if (source == null) source = "Jason";
    String dateString = localScheme.getProperty(SchemeConstant.IMPORT_DATE);
    if (dateString == null) 
      dateString = XMLConstant.DATE_FORMAT.format(new Date());
    
    return 
     schemeName+SANDBOX_NAME_SEPARATOR+source+SANDBOX_NAME_SEPARATOR+dateString;
  }
  
  
  /////////////////////////////////////
  ////   STAND-ALONE FACTORY METHODS
  /////////////////////////////////////
  
  
  public static CodingScheme createScheme(String schemeName)
  {
    CodingScheme scheme = new ConcreteCodingScheme(schemeName);
    // cacheScheme(scheme, false); 
    return scheme;
  }
  
  /**
   * Creates an empty SchemeField with this name.
   */
  public static SchemeField createField(String name)
  {
    return new ConcreteField(name);
  }
  
  /**
   * Creates a deep copy of the SchemeField.
   */
  public static SchemeField cloneField(SchemeField f)
  {
    return ((ConcreteField) f).deepCopy();
  }
  
  public static SchemeValue createValue(String name)
  {
    return new ConcreteValue(name);
  }
  
  public static SchemeValue cloneValue(SchemeValue v)
  {
    return ((ConcreteValue) v).deepCopy();
  }
  
  
  
  
  ////////////////////////////////
  ///    MERGING/DIFFING
  ////////////////////////////////
  
  
  /**
   * Adds fields that the 'from' scheme contains that are missing
   * in the target and updates Fields that have been changed.
   */
  public static void mergeSchemes(CodingScheme target, CodingScheme from)
  {
    
    for (Iterator it = from.getSchemeFields().iterator(); it.hasNext();)
    {
      
      SchemeField f = (SchemeField)it.next();
      SchemeField t = target.getSchemeField(f.getName());
      
      if (t != null && !t.equals(f))
      {
        
        // TODO - Make this less painful!
        target.removeSchemeField(f.getName());
        target.addEquivalentSchemeField(f);
      }
      else
        target.addEquivalentSchemeField(f);
    }
  }
  
  
  /**
   * Returns a CodingScheme which is a Field-Wise union of the
   * base scheme compared against the compareTo scheme.  The returning
   * scheme adds CS-Field properties that are named _diff
   * with possible values:
   *
   *  missing - A field that exists in the compareTo scheme but missing
   *            from the base scheme.
   *  new - A field that exists in the base scheme that is missing from the
   *        compareTo scheme.
   *  changed - There are attribute/value differences between the two schemes.
   *  copy - An duplicate
   */
  public static void diffSchemes(CodingScheme base, CodingScheme compareTo)
  {
    
    List missingFields = new LinkedList();
    
    // TODO
    //
    // Tagging the CodingScheme VALUE=Name,author,date  of
    // the compareTo scheme:
    //
    // diff.addProperty(DIFF, compareTo.getName());
    
    // MISSING or CHANGED
    //
    for (Iterator it = compareTo.getSchemeFields().iterator(); it.hasNext();)
    {
      
      SchemeField f = (SchemeField)it.next();
      SchemeField b = base.getSchemeField(f.getName());
      
      // Missing
      //
      if (b == null)
      {
        
        SchemeField clone = cloneField(f);
        clone.removeProperty(SchemeConstant.DIFF);
        clone.addProperty(SchemeConstant.DIFF, SchemeConstant.DIFF_MISSING);
        tagValues(clone, SchemeConstant.DIFF_MISSING);
        missingFields.add(clone);
      }
      else
        diffFields(b, f); // Changed
    }
    
    // NEW fields
    //
    for (Iterator it = base.getSchemeFields().iterator(); it.hasNext();)
    {
      
      SchemeField f = (SchemeField)it.next();
      
      if (compareTo.getSchemeField(f.getName()) == null)
      {
        
        f.removeProperty(SchemeConstant.DIFF);
        f.addProperty(SchemeConstant.DIFF, SchemeConstant.DIFF_NEW);
        tagValues(f, SchemeConstant.DIFF_NEW);
      }
    }
    
    // Put in missing fields
    for (Iterator it = missingFields.iterator(); it.hasNext();)
    {
      SchemeField f = (SchemeField)it.next();
      base.addEquivalentSchemeField(f);
    }
  }
  
  public static void diffFields(SchemeField base, SchemeField compareTo)
  {
    
    if (base != null && compareTo == null)
    {
      
      // VALUE=new
      //
      base.removeProperty(SchemeConstant.DIFF);
      base.addProperty(SchemeConstant.DIFF, SchemeConstant.DIFF_NEW);
      tagValues(base, SchemeConstant.DIFF_NEW);
    }
    else if (!base.equals(compareTo))
    {
      
      // VALUE=changed
      //
      base.removeProperty(SchemeConstant.DIFF);
      base.addProperty(SchemeConstant.DIFF, SchemeConstant.DIFF_CHANGED);
      tagValues(compareTo, base);
    }
    else
    {
      
      // Copy
      base.removeProperty(SchemeConstant.DIFF);
      base.addProperty(SchemeConstant.DIFF, SchemeConstant.DIFF_COPIED);
      tagValues(base, SchemeConstant.DIFF_COPIED);
    }
  }
  
  
  public static void tagValues(SchemeField f, String tag)
  {
    
    // Mark all Values as copies
    //
    for (Iterator vi = f.getSchemeValues().iterator(); vi.hasNext();)
    {
      
      SchemeValue v = (SchemeValue)vi.next();
      v.removeProperty(SchemeConstant.DIFF);
      v.addProperty(SchemeConstant.DIFF, tag);
    }
  }
  
  /**
   * Marks each SchemeValue a value according to its
   * apparent difference with a SchemeValue that the
   * comparedTo Value has.
   */
  private static void tagValues(SchemeField comparedTo, SchemeField base)
  {
    
    // MISSING, CHANGED, COPY
    //
    for (Iterator it = comparedTo.getSchemeValues().iterator(); it.hasNext();)
    {
      
      SchemeValue v = (SchemeValue)it.next();
      SchemeValue l = base.getSchemeValue(v.getName());
      
      if (l == null)
      {
        
        // missing
        //
        SchemeValue clone = cloneValue(v);
        clone.removeProperty(SchemeConstant.DIFF);
        clone.addProperty(SchemeConstant.DIFF,
        SchemeConstant.DIFF_MISSING);
        base.addEquivalentSchemeValue(clone);
      }
      else if (!l.equals(v))
      {
        
        // changed
        //
        l.removeProperty(SchemeConstant.DIFF);
        l.addProperty(SchemeConstant.DIFF,
        SchemeConstant.DIFF_CHANGED);
      }
      else
      {
        
        // Copy
        l.removeProperty(SchemeConstant.DIFF);
        l.addProperty(SchemeConstant.DIFF,
        SchemeConstant.DIFF_COPIED);
      }
    }
    
    // NEW
    //
    for (Iterator it = base.getSchemeValues().iterator(); it.hasNext();)
    {
      
      SchemeValue v = (SchemeValue)it.next();
      
      if (comparedTo.getSchemeValue(v.getName()) == null)
      {
        
        v.removeProperty(SchemeConstant.DIFF);
        v.addProperty(SchemeConstant.DIFF,
        SchemeConstant.DIFF_NEW);
      }
    }
  }
  
  ///////////////////////////////
  ////   JDOM CONVERSION
  ///////////////////////////////
  
  /*
  public static org.jdom.Element convertToElement(CodingScheme cs)
  {
    /* if (cs instanceof JDOMCodingScheme)
    {
      return ((JDOMCodingScheme)cs).copyJDOMTree();
    }
    // should implement this ??:
    // throw new RuntimeException("Only JDOMCodingScheme is supported"); 
    return null;
  }
  
  public static CodingScheme convertToScheme(org.jdom.Element schemeElement)
  {
    // return new JDOMCodingScheme(schemeElement); // SHOULD not be a public constructor;
    // SHOULD move JDOMXXX into this package
    return null;
  } 
  */  
}
