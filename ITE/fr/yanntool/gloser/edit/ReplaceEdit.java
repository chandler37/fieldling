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

/** Action reversible: Remplacement d'un element.
*/
public class ReplaceEdit extends AbstractUndoableEdit {

		private org.jdom.Element old_element_, new_element_;
		private int              sentenceNum_;
		private frame_editor     model_;
		private String           what_;
		private boolean          majLexicon_;

	public ReplaceEdit(frame_editor model, org.jdom.Element oldElt, org.jdom.Element newElt, int sentenceNum, String what) {
		model_       = model;
		old_element_ = oldElt;
		new_element_ = newElt;
		what_        = what;
		sentenceNum_ = sentenceNum;
		majLexicon_  = true;

		model_.replaceContent(old_element_, new_element_, sentenceNum_, what_, majLexicon_);
		model_.showSentence(sentenceNum_);
	}
	public ReplaceEdit(frame_editor model, org.jdom.Element oldElt, org.jdom.Element newElt, int sentenceNum, String what, boolean majLexicon) {
		model_       = model;
		old_element_ = oldElt;
		new_element_ = newElt;
		what_        = what;
		sentenceNum_ = sentenceNum;
		majLexicon_  = majLexicon;

		model_.replaceContent(old_element_, new_element_, sentenceNum_, what_, majLexicon_);
		model_.showSentence(sentenceNum_);
	}
	public void undo() throws CannotUndoException {
		model_.replaceContent(new_element_, old_element_, sentenceNum_, what_, majLexicon_);
		model_.showSentence(sentenceNum_);
	}
	public void redo() throws CannotRedoException {
		model_.replaceContent(old_element_, new_element_, sentenceNum_, what_, majLexicon_);
		model_.showSentence(sentenceNum_);
	}
	public boolean canUndo() {
		return true;
	}
	public boolean canRedo() {
		return true;
	}
	public String getPresentationName() {
		return "replace "+what_;
	}

}