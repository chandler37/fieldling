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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur pour l'ensemble des morphèmes d'un mot.
*/
public class panel_word_4_morpheme extends contentPanel {

		private org.jdom.Element wordElt;

	/** Crée un conteneur graphique pour un mot.
	* Les morphèmes sont écrit les uns à la suite des autres.
	*
	* @param word                      Elément XML correspondant au mot.
	* @param fromSentencePanel         Le conteneur graphique de la phrase.
	* @param num                       position du mot dans la phrase.
	*/
	public panel_word_4_morpheme(org.jdom.Element word, panel_level_sentence fromSentencePanel, int num) {
 		super(fromSentencePanel, num, word);
 		this.wordElt           = word;
		setLayout(new FlowLayout());
		try {
			XPath myXpath = new JDOMXPath(getPref("M_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List results = myXpath.selectNodes(wordElt);
			Iterator resultIter = results.iterator();
			int index = 0;
			while ( resultIter.hasNext() ) {
				org.jdom.Element morphemeElt = (org.jdom.Element)resultIter.next();
				panel_morpheme morphemeP = new panel_morpheme(morphemeElt, wordElt, this, index);
				add(morphemeP);
				index++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean autoGlose() {
		boolean stop = false;
		Component[] comps = getComponents();
		for(int i=0; i<comps.length; i++) {
			if (!stop) {
				Component comp = comps[i];
				if (comp instanceof panel_morpheme) {
					panel_morpheme m = (panel_morpheme)comp;
					stop = m.gloseIt();
				}
			}
		}
		return stop;
	}
	public panel_morpheme getMorphemePanel(org.jdom.Element morpheme) {
		Component[] components = getComponents();
		for (int i=0; i<components.length; i++) {
			Component comp = components[i];
			if (comp instanceof panel_morpheme) {
				if (((panel_morpheme)comp).getMorpheme().equals(morpheme)) {
					 return (panel_morpheme)comp;
				}
			}
		}
		return null;
	}
	public void modifMorphemeContent(int morphemeIndex, String s, boolean isTranscription, boolean update) {
		fromSentencePanel.modifMorphemeContent(wordIndex, morphemeIndex, s, isTranscription, update);
	}
	public void deleteMorpheme(int morphemeIndex) {
		fromSentencePanel.deleteMorpheme(wordIndex, morphemeIndex);
	}
	public void concatMorpheme(int morphemeIndex1, int morphemeIndex2) {
		fromSentencePanel.concatMorpheme(wordIndex, morphemeIndex1, morphemeIndex2);
	}
	public void splitMorphemeAt(int morphemeIndex, String before, String after) {
		fromSentencePanel.splitMorphemeAt(wordIndex, morphemeIndex, before, after);
	}
	public void replaceMorpheme(org.jdom.Element old_morpheme, org.jdom.Element new_morpheme) {
		fromSentencePanel.replaceContent(old_morpheme, new_morpheme, "morpheme");
	}
	public void replaceAttributes(org.jdom.Element oldElt, java.util.List attributes, String what)  {
		fromSentencePanel.replaceAttributes(oldElt, attributes, what);
	}
};