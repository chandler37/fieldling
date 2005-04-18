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
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.jaxen.*;
import org.jaxen.function.NormalizeSpaceFunction;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur générique pour un composant de phrase.
*/
public class contentPanel extends JPanel implements MouseListener {

		protected panel_level_sentence   fromSentencePanel;
		protected org.jdom.Element       sentenceElt;
		protected domOperations          domOperator;
		private org.jdom.Element         currentElt;
		private String                   title;
		protected int                    wordIndex;
		private static final String      RESS_W2S_XSL = new String("/styles/embedWord2Sentence.xsl");

	/** Crée un conteneur graphique pour un mot.
	* La transcription du mot est écrite dans un éditeur et la glose dans un menu.
	*
	* @param fromSentencePanel         Le conteneur graphique de la phrase.
	* @param num                       position du mot dans la phrase.
	*/
	public contentPanel(panel_level_sentence fromSentencePanel, int num, org.jdom.Element currElt) {
 		super(new BorderLayout());
		this.title = fromSentencePanel.getLevel();
 		if ((title.equals("word")) || (title.equals("morpheme"))) {
			this.wordIndex = num -1;
		} else {
			this.wordIndex = 0;
		}
 		if (title.equals("morpheme")) {
			title = "word";
		}
 		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		setBorder(new javax.swing.border.TitledBorder(getBorder(), title+"["+num+"]"));
 		this.fromSentencePanel = fromSentencePanel;
 		this.sentenceElt       = fromSentencePanel.getSentence();
 		this.currentElt  = currElt;
 		if (currElt == null) {
			this.currentElt = sentenceElt;
		}
		this.domOperator = getWin().getDomOperator();
		addMouseListener(this);
	}
	public String getLevel() {
		return fromSentencePanel.getLevel();
	}
	public frame_editor getWin() {
		return fromSentencePanel.getWin();
	}
	public String getPref(String s) {
		return getWin().getPref(s);
	}
	/** Déclare une modification dans le document
	*/
	protected void setModif() {
		fromSentencePanel.setModif();
	}

	protected org.jdom.Element getSentenceFromWords(org.jdom.Element transcriptionElt, boolean wordLevel, boolean morphemeLevel) {
		try {
			String form = "";
			XPath wXpath = new JDOMXPath(getPref("W_XPATH"));
			wXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List list = wXpath.selectNodes(sentenceElt);
			for (int i=0; i<list.size(); i++) {
				org.jdom.Element w = (org.jdom.Element)list.get(i);
				if (wordLevel) {
					XPath formXpath = new JDOMXPath(getPref("W_FORM_XPATH"));
					formXpath.setFunctionContext(new myXPathFunctionContext());
					form += formXpath.stringValueOf(w);
				} else {
					XPath mXpath = new JDOMXPath(getPref("M_XPATH"));
					mXpath.setFunctionContext(new myXPathFunctionContext());
					java.util.List mList = mXpath.selectNodes(w);
					for (int j=0; j<mList.size(); j++) {
						org.jdom.Element m = (org.jdom.Element)mList.get(j);
						XPath formXpath = new JDOMXPath(getPref("M_FORM_XPATH"));
						formXpath.setFunctionContext(new myXPathFunctionContext());
						form += formXpath.stringValueOf(m);
						if (j < (mList.size()-1)) form += "-";
					}
				}
				if (i < (list.size()-1)) form += " ";
			}
			org.jdom.Element newElt = ((org.jdom.Element)transcriptionElt.clone()).setText(form);
			return newElt;
		} catch (Exception err) {
			System.out.println(err.getMessage());
			return null;
		}
	}
	protected org.jdom.Element getWordFromMorphemes(org.jdom.Element transcriptionElt) {
		try {
			String form = "";
			XPath mXpath = new JDOMXPath(getPref("M_XPATH"));
			mXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List mList = mXpath.selectNodes(currentElt);
			for (int j=0; j<mList.size(); j++) {
				org.jdom.Element m = (org.jdom.Element)mList.get(j);
				XPath formXpath = new JDOMXPath(getPref("M_FORM_XPATH"));
				formXpath.setFunctionContext(new myXPathFunctionContext());
				form += formXpath.stringValueOf(m);
				if (j < (mList.size()-1)) form += "-";
			}
			org.jdom.Element newElt = ((org.jdom.Element)transcriptionElt.clone()).setText(form);
			return newElt;
		} catch (Exception err) {
			System.out.println(err.getMessage());
			return null;
		}
	}
	protected void splitSentence2Words(org.jdom.Element sentenceTrancriptionElt, boolean wordLevel, boolean morphemeLevel) {
		try {
			XPath myXpath = new JDOMXPath(".");
			String form = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(sentenceTrancriptionElt), myXpath.getNavigator());
			String[] words = form.split(getPref("regexW"));
			myXpath = new JDOMXPath(getPref("W_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List list = myXpath.selectNodes(sentenceElt);
			if (list.size() != 0) {
				int answer = JOptionPane.showConfirmDialog(null, "Do you really want to replace all the words?", "Alert", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					org.jdom.Element newSentenceElt = ((org.jdom.Element)sentenceElt.clone());
					//suppression des anciens mots
					java.util.List newlist = myXpath.selectNodes(newSentenceElt);
					for (int i=0; i<newlist.size(); i++) {
						org.jdom.Element elt = (Element)newlist.get(i);
						elt.getParent().removeContent(elt);
					}
					//creation des nouveaux mots
					for (int i=0; i<words.length; i++) {
						String word = words[i];
						org.jdom.Element newW   = domOperator.createWord();
						if (wordLevel) {
							org.jdom.Element newW_F = domOperator.createLevelTranscr("word");
							newW_F.setText(word);
							newW = domOperator.embedTranscr2level("word", newW_F, newW);
						}
						if (morphemeLevel) {
							String[] morphemes = word.split(getPref("regexM"));
							for (int j=0; j<morphemes.length; j++) {
								String morpheme = morphemes[j];
								org.jdom.Element newM_F = domOperator.createLevelTranscr("morpheme");
								newM_F.setText(morpheme);
								org.jdom.Element newM   = domOperator.createMorpheme();
								newM = domOperator.embedTranscr2level("morpheme", newM_F, newM);
								newW = domOperator.embedMorpheme2word(newM, newW);
							}
						}
						newSentenceElt = domOperator.embedWord2sentence(newW, newSentenceElt);
					}
					replaceContent(sentenceElt, newSentenceElt, "sentence");
				}
			} else {
					org.jdom.Element newSentenceElt = ((org.jdom.Element)sentenceElt.clone());
					//creation des nouveaux mots
					for (int i=0; i<words.length; i++) {
						String word = words[i];
						org.jdom.Element newW   = domOperator.createWord();
						if (wordLevel) {
							org.jdom.Element newW_F = domOperator.createLevelTranscr("word");
							newW_F.setText(word);
							newW = domOperator.embedTranscr2level("word", newW_F, newW);
						}
						if (morphemeLevel) {
							String[] morphemes = word.split(getPref("regexM"));
							for (int j=0; j<morphemes.length; j++) {
								String morpheme = morphemes[j];
								org.jdom.Element newM_F = domOperator.createLevelTranscr("morpheme");
								newM_F.setText(morpheme);
								org.jdom.Element newM   = domOperator.createMorpheme();
								newM = domOperator.embedTranscr2level("morpheme", newM_F, newM);
								newW = domOperator.embedMorpheme2word(newM, newW);
							}
						}
						newSentenceElt = domOperator.embedWord2sentence(newW, newSentenceElt);
					}
					replaceContent(sentenceElt, newSentenceElt, "sentence");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
	protected void splitWord2Morphemes(org.jdom.Element wordTranscrptionElt) {
		try {
			XPath myXpath = new JDOMXPath(".");
			String form = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(wordTranscrptionElt), myXpath.getNavigator());
			String[] morphemes = form.split(getPref("regexM"));
			myXpath = new JDOMXPath(getPref("M_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List list = myXpath.selectNodes(currentElt);
			if (list.size() != 0) {
				int answer = JOptionPane.showConfirmDialog(null, "Do you really want to replace all the morphemes?", "Alert", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					org.jdom.Element newWordElt = ((org.jdom.Element)currentElt.clone());
					//suppression des anciens morphemes
					java.util.List newlist = myXpath.selectNodes(newWordElt);
					for (int i=0; i<newlist.size(); i++) {
						org.jdom.Element elt = (Element)newlist.get(i);
						elt.getParent().removeContent(elt);
					}
					//creation des nouveaux morphemes
					for (int i=0; i<morphemes.length; i++) {
						String morpheme = morphemes[i];
						org.jdom.Element newM   = domOperator.createMorpheme();
						org.jdom.Element newM_F = domOperator.createLevelTranscr("morpheme");
						newM_F.setText(morpheme);
						newM = domOperator.embedTranscr2level("morpheme", newM_F, newM);
						newWordElt = domOperator.embedMorpheme2word(newM, newWordElt);
					}
					replaceContent(currentElt, newWordElt, "word");
				}
			} else {
					org.jdom.Element newWordElt = ((org.jdom.Element)currentElt.clone());
					//creation des nouveaux morphemes
					for (int i=0; i<morphemes.length; i++) {
						String morpheme = morphemes[i];
						org.jdom.Element newM   = domOperator.createMorpheme();
						org.jdom.Element newM_F = domOperator.createLevelTranscr("morpheme");
						newM_F.setText(morpheme);
						newM = domOperator.embedTranscr2level("morpheme", newM_F, newM);
						newWordElt = domOperator.embedMorpheme2word(newM, newWordElt);
					}
					replaceContent(currentElt, newWordElt, "word");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	public void replaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, String what) {
		fromSentencePanel.replaceContent(oldElt, newElt, what);
	}
	public void replaceAttributes(org.jdom.Element oldElt, java.util.List attributes, String what)  {
		fromSentencePanel.replaceAttributes(oldElt, attributes, what);
	}
	/** popup menu pour inspecter les attributs et le code de l'élément de ce niveau
	*/
	public void mouseClicked(MouseEvent e)  {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}
	public void mousePressed(MouseEvent e)  {}
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem showAttribute = new JMenuItem("show the "+title+" attributes");
			showAttribute.setEnabled(currentElt!=null);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(currentElt);
					attChoice.show();
					if (attChoice.isModif()) {
						setModif();
						replaceAttributes(currentElt, attChoice.getResult(), title+" attributes");
					}
				}
			});
			popup.add(showAttribute);

			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(currentElt!=null);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(currentElt, null);
					src.show();
					if (src.isModif()) {
						replaceContent(currentElt, src.getResult(), title);
					}
				}
			});
			popup.add(showSrc);

			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(currentElt!=null);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(currentElt);
					chooser.show();
					if (chooser.isValidated()) {
						replaceContent(currentElt, chooser.getElt(), title);
					}
				}
			});
			popup.add(transform);

			if (getWin().isMediaTag(fromSentencePanel.getLevel(), currentElt)) {
				JMenuItem play = new JMenuItem("play "+title);
				play.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getWin().playMedia(fromSentencePanel.getLevel(), currentElt);
					}
				});
				popup.add(play);
			}

			if (fromSentencePanel.getLevel().equals("morpheme")) {
				JMenuItem appendMorpheme = new JMenuItem("append morpheme");
				appendMorpheme.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						fromSentencePanel.appendMorpheme(wordIndex);
					}
				});
				popup.add(appendMorpheme);
			}
			popup.show(this, e.getX(), e.getY());
		}
	}
	public int getWordIndex() {
		return wordIndex;
	}
};