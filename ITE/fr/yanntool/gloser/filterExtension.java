/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Michel Jacobson jacobson@idf.ext.jussieu.fr
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ***** END LICENSE BLOCK ***** */

/*-----------------------------------------------------------------------*/
package fr.yanntool.gloser;

import java.io.*;

public class filterExtension extends javax.swing.filechooser.FileFilter {
   String extension;
   String description;

   public filterExtension(String extension, String description) {
	   this.extension = extension;
	   this.description = description;
   }

   public boolean accept(File fic) {
	   if (fic.getName().endsWith(extension))
		   return true;
	   else if (fic.isDirectory())
		   return true;
	   return false;
   }

   public String getDescription() {
	   return this.description + "(*" + extension + ")";
   }
}
