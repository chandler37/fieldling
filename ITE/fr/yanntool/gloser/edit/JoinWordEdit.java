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

/** Action reversible: Concaténation de deux mots.
*/
public class JoinWordEdit extends AbstractUndoableEdit {

		private org.jdom.Element element1_, element2_, old_element1_, old_element2_;
		private int              index1_, index2_, sentenceNum_;
		private frame_editor        model_;

	public JoinWordEdit(frame_editor model, org.jdom.Element element1, org.jdom.Element element2, int index1, int index2, int sentenceNum) {
		model_        = model;
		old_element1_     = (org.jdom.Element)element1.clone();
		old_element2_     = (org.jdom.Element)element2.clone();
		element1_     = element1;
		element2_     = element2;
		index1_       = index1;
		index2_       = index2;
		sentenceNum_  = sentenceNum;
		model_.joinWordAt(element1_, element2_, index1_, index2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void undo() throws CannotUndoException {
		element1_     = (org.jdom.Element)old_element1_.clone();
		element2_     = (org.jdom.Element)old_element2_.clone();
		model_.deleteWordAt(index1_, sentenceNum_);
		model_.insertWordAt(old_element1_, index1_, sentenceNum_);
		model_.insertWordAt(old_element2_, index2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void redo() throws CannotRedoException {
		model_.joinWordAt(element1_, element2_, index1_, index2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public boolean canUndo() {
		return true;
	}
	public boolean canRedo() {
		return true;
	}
	public String getPresentationName() {
		return "join words";
	}

}