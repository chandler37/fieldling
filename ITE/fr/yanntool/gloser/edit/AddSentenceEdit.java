/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2004 Michel Jacobson jacobson@idf.ext.jussieu.fr
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
package fr.yanntool.gloser.edit;

import fr.yanntool.gloser.*;
import javax.swing.undo.*;
import javax.swing.*;
import org.jdom.*;

/*-----------------------------------------------------------------------*/

/** Action reversible: Ajout d'une phrase.
*/
public class AddSentenceEdit extends AbstractUndoableEdit {

		private org.jdom.Element element_;
		private int              index_;
		private frame_editor        model_;

	public AddSentenceEdit(frame_editor model, org.jdom.Element element, int index) {
		model_   = model;
		element_ = element;
		index_   = index;
		model_.insertSentenceAt(element_,  index_);
		model_.showSentence(index_+1);
	}
	public void undo() throws CannotUndoException {
		model_.deleteSentenceAt( index_ );
		model_.showSentence(index_);
	}
	public void redo() throws CannotRedoException {
		model_.insertSentenceAt( element_, index_ );
		model_.showSentence(index_+1);
	}
	public boolean canUndo() {
		return true;
	}
	public boolean canRedo() {
		return true;
	}
	public String getPresentationName() {
		return "insert sentence";
	}

}