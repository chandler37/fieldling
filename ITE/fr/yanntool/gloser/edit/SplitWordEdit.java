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

/** Action reversible: Découpage d'un mot en deux.
*/
public class SplitWordEdit extends AbstractUndoableEdit {

		private org.jdom.Element element_, new_element1_, new_element2_;
		private int              index_, sentenceNum_;
		private frame_editor        model_;

	public SplitWordEdit(frame_editor model, org.jdom.Element element, org.jdom.Element new_element1, org.jdom.Element new_element2, int index, int sentenceNum) {
		model_           = model;
		new_element1_    = new_element1;
		new_element2_    = new_element2;
		element_         = element;
		index_           = index;
		sentenceNum_     = sentenceNum;
		model_.deleteWordAt(index_, sentenceNum_);
		model_.insertWordAt(new_element1_, index_,    sentenceNum_);
		model_.insertWordAt(new_element2_, index_ +1, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void undo() throws CannotUndoException {
		model_.deleteWordAt(index_, sentenceNum_);
		model_.deleteWordAt(index_, sentenceNum_);
		model_.insertWordAt(element_, index_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void redo() throws CannotRedoException {
		model_.deleteWordAt(index_, sentenceNum_);
		model_.insertWordAt(new_element1_, index_,    sentenceNum_);
		model_.insertWordAt(new_element2_, index_ +1, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public boolean canUndo() {
		return true;
	}
	public boolean canRedo() {
		return true;
	}
	public String getPresentationName() {
		return "split words";
	}

}