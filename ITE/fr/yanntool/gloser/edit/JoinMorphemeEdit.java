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

/** Action reversible: Concatenation de deux morphèmes.
*/
public class JoinMorphemeEdit extends AbstractUndoableEdit {

		private org.jdom.Element element1_, element2_, old_element1_, old_element2_;
		private int              indexW_, indexM1_, indexM2_, sentenceNum_;
		private frame_editor        model_;

	public JoinMorphemeEdit(frame_editor model, org.jdom.Element element1, org.jdom.Element element2, int indexW, int indexM1, int indexM2, int sentenceNum) {
		model_        = model;
		old_element1_     = (org.jdom.Element)element1.clone();
		old_element2_     = (org.jdom.Element)element2.clone();
		element1_         = element1;
		element2_         = element2;
		indexM1_          = indexM1;
		indexM2_          = indexM2;
		indexW_           = indexW;
		sentenceNum_      = sentenceNum;
		model_.joinMorphemeAt(element1_, element2_, indexW_, indexM1_, indexM2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void undo() throws CannotUndoException {
		element1_     = (org.jdom.Element)old_element1_.clone();
		element2_     = (org.jdom.Element)old_element2_.clone();
		model_.deleteMorphemeAt(indexW_, indexM1_, sentenceNum_);
		model_.insertMorphemeAt(old_element1_, indexW_, indexM1_, sentenceNum_);
		model_.insertMorphemeAt(old_element2_, indexW_, indexM2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public void redo() throws CannotRedoException {
		model_.joinMorphemeAt(element1_, element2_, indexW_, indexM1_, indexM2_, sentenceNum_);
		model_.showSentence(sentenceNum_);
	}
	public boolean canUndo() {
		return true;
	}
	public boolean canRedo() {
		return true;
	}
	public String getPresentationName() {
		return "join morphemes";
	}

}