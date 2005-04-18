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
package fr.yanntool.gloser;

import javax.swing.*;
import java.util.regex.*;
/*-----------------------------------------------------------------------*/

/** Parametres du Find/Replace.
*/
public class options_find {

		private String           searchString,
		                         replaceString,
		                         searchWhere,
		                         predicate;
		private boolean          isRegex,
		                         isExact,
		                         isFromStart,
		                         modif;

	public options_find() {
		this.searchString  = "";
		this.replaceString = "";
		this.predicate     = "";
		this.searchWhere   = "word transcription";
		this.isRegex       = false;
		this.isExact       = true;
		this.isFromStart   = false;
		this.modif         = false;
	}
	/** Retourne le chemin à partir du noeud trouvé (mot ou morphème) jusqu'au noeud
	*  sur lequel la recherche se fait (transcription ou traduction) sous forme de xpath.
	* @param win             fenêtre de l'editeur
	*/
	public String getContentXpath(frame_editor win) {
		String xpath = "";
		if (getLevel().equals("word")) {
			xpath = new String(isTranscription()? win.getWF():win.getWG());
		} else if (getLevel().equals("morpheme")) {
			xpath = new String(isTranscription()? win.getMF():win.getMG());
		}
		return xpath;
	}
	/** Retourne le chemin jusqu'aux noeuds recherchés sous forme de xpath.
	* @param win             fenêtre de l'editeur
	* @param everyWhere      la recherche s'effectue sans égard à la position de départ (changeAll)
	*/
	public String getFindXpath(frame_editor win, boolean everyWhere) {
		String xpathFromRootToSentence = win.getT()+"/"+win.getS();
		String xpathFromSentencetoLevel = "";
		if (getLevel().equals("word")) {
			xpathFromSentencetoLevel = "/"+win.getW();
		} else if (getLevel().equals("morpheme")) {
			xpathFromSentencetoLevel = "/"+win.getW()+"/"+win.getM();
		}
		String positionPredicat = "";
		if ((!everyWhere) && (!isFromStart())) {
			positionPredicat = "[position() >= "+win.getCurrentS()+"]";
		}
		String patternMatchingPredicat = "";
		if (searchWhere.equals("word transcription")) {
			patternMatchingPredicat = getPatternMatchingPredicat(win.getWF());
		} else if (searchWhere.equals("morpheme transcription")) {
			patternMatchingPredicat = getPatternMatchingPredicat(win.getMF());
		} else if (searchWhere.equals("word gloss")) {
			patternMatchingPredicat = getPatternMatchingPredicat(win.getWG());
		} else if (searchWhere.equals("morpheme gloss")) {
			patternMatchingPredicat = getPatternMatchingPredicat(win.getMG());
		}
		return xpathFromRootToSentence+positionPredicat+xpathFromSentencetoLevel+patternMatchingPredicat;
	}
	private String getPatternMatchingPredicat(String param) {
		String cond2 = "";
		if (!predicate.equals("")) {
			cond2 = "["+predicate+"]";
		}
		if (isRegex()) {
			return "[matches("+param+",'"+searchString+"')]"+cond2;
		} else {
			if (isExact) {
				return "["+param+" = '"+searchString+"']"+cond2;
			} else {
				return "[contains("+param+", '"+searchString+"')]"+cond2;
			}
		}
	}
	public String getSearchString () {
		return searchString;
	}
	public void setSearchString(String s) {
		searchString = s;
		modif = true;
	}
	public String getPredicate () {
		return predicate;
	}
	public void setPredicate(String s) {
		predicate = s;
		modif = true;
	}
	public String getReplaceString () {
		return replaceString;
	}
	public void setReplaceString(String s) {
		replaceString = s;
		modif = true;
	}
	public String getSearchWhere () {
		return searchWhere;
	}
	public void setSearchWhere (String s) {
		searchWhere = s;
		modif = true;
	}
	public boolean isRegex () {
		return isRegex;
	}
	public void setRegex (boolean b) {
		if (isRegex != b) modif = true;
		isRegex = b;
	}
	public boolean isExact () {
		return isExact;
	}
	public void setExact (boolean b) {
		if (isExact != b) modif = true;
		isExact = b;
	}
	public boolean isFromStart () {
		return isFromStart;
	}
	public void setFromStart (boolean b) {
		if (isFromStart != b) modif = true;
		isFromStart = b;
	}
	public boolean isModif () {
		return modif;
	}
	public void resetModif () {
		modif = false;
	}
	public String getLevel() {
		if ( (searchWhere.equals("word transcription")) || (searchWhere.equals("word gloss")) ) {
			return "word";
		} else {
			return "morpheme";
		}
	}
	public boolean isTranscription() {
		if ( (searchWhere.equals("word transcription")) || (searchWhere.equals("morpheme transcription")) ) {
			return true;
		} else {
			return false;
		}
	}
	public boolean parseRegex() {
		if (isRegex) {
			try {
				Pattern.compile(searchString);
			} catch (PatternSyntaxException e) {
				JOptionPane.showMessageDialog(null, searchString+": is not a valid regular expression.");
				return false;
			}
		}
		return true;
	}
};