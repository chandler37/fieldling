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
 * LogManager.java
 *
 * Created on October 2, 2002, 4:08 PM
 */

package signstream.utility;

import org.apache.log.*;
import org.apache.log.filter.*;
import org.apache.log.format.*;
import org.apache.log.output.*;
import org.apache.log.output.io.*;
import org.apache.log.util.*;

import java.io.*;

public class LogManager {
  
  private static final String dateFormat = "yyyy/MM/dd HH:mm:ss.SSS";
  private static final String logFormat =
  "%{time:"+dateFormat+"} %-5.5{priority}: %{message}\n";
  private static final ExtendedPatternFormatter formatter =
  new ExtendedPatternFormatter(logFormat);
  private static final File logfile =
  new File(new File(System.getProperty("user.home"), "signstream"), "sequencer-log.txt");
  
  public static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("sequencer");
  
  public static StringBuffer buffer = new StringBuffer();
  
  static {
    initialize(Priority.WARN);
  }
  
  public static boolean initialize(Priority priority) {
    log.setPriority(priority);
    try {
      FileTarget target = new FileTarget(logfile, false, formatter);
      log.setLogTargets(new LogTarget[] { target });
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return false;
    }
    log.info("Logger initialized\n");
    return true;
  }
  
  public static void debug(String msg) { log.debug(msg); }
  public static void info(String msg) { log.info(msg); }
  public static void warn(String msg) { log.warn(msg); }
  public static void error(String msg) { log.error(msg); }
  public static void fatal(String msg) { log.fatalError(msg); }
  
  public static void debug() {
    log.debug(buffer.toString());
    clear();
  }
  public static void info() {
    log.info(buffer.toString());
    clear();
  }
  public static void warn() {
    log.warn(buffer.toString());
    clear();
  }
  public static void error() {
    log.error(buffer.toString());
    clear();
  }
  public static void fatal() {
    log.fatalError(buffer.toString());
    clear();
  }
  
  public static void clear() {
    buffer.setLength(0);
  }
  public static void append(String msg) {
    buffer.append(msg);
  }
  public static void append(long l) {
    buffer.append(l);
    buffer.append(' ');
  }
  public static void append(Object obj) {
    buffer.append(obj);
    buffer.append(' ');
  }
}
