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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur pour un mot (composé d'une transcription et d'une glose).
*/
public class panel_word extends contentPanel {

		static  private String   level            = "word";
		private org.jdom.Element gloseElt         = null;
		private org.jdom.Element transcriptionElt = null;
		private org.jdom.Element wordElt;
		private contentTextField form             = null;
		private contentMenu      gls;
		private boolean          caretCondPosInit; //caret position = 0 and no selection
		private String           transcription = "";
		private String           glose = "";
		private boolean          changedTranscription;

		private myActionListener listener;

	/** Crée un conteneur graphique pour un mot.
	* La transcription du mot est écrite dans un éditeur et la glose dans un menu.
	*
	* @param word                      Elément XML correspondant au mot.
	* @param fromSentencePanel         Le conteneur graphique de la phrase.
	* @param num                       position du mot dans la phrase.
	*/
	public panel_word(org.jdom.Element word, panel_level_sentence fromSentencePanel, int num) {
 		super(fromSentencePanel, num, word);
		this.wordElt              = word;
		this.caretCondPosInit     = true;
		this.changedTranscription = false;
		loadElements();

		this.form = new contentTextField(transcriptionElt, this, true);
		form.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setModif(); }
			public void insertUpdate(DocumentEvent e)  { setModif(); }
			public void removeUpdate(DocumentEvent e)  { setModif(); }
		});
		form.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_I) && (e.isControlDown())) {  //ctrl+i = couper un mot en deux
					split();
				} else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					;//nothing
				} else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
					if ((form.getCaretPosition() == 0) && (wordIndex > 0) && (caretCondPosInit)) {  //touche effacer avant le 1er caractere du mot = le coller au precedent si il existe
						concat();
					} else {
						changedTranscription = true;
						if (!form.isActivate()) {
							form.activate();
						}
					}
				} else if (e.getKeyChar() == KeyEvent.VK_DELETE) {
					if (transcription.equals("")) {  //touche supr quand le texte etait deja vide
						changedTranscription = false;
						if (form.isActivate()) {
							form.desactivate();
							deleteTranscription();
						} else {
							deleteWord();
						}
					} else {
						changedTranscription = true;
						if (!form.isActivate()) {
							form.activate();
						}
					}
				} else {
					changedTranscription = true;
					if (!form.isActivate()) {
						form.activate();
					}
				}
			}
			public void keyPressed(KeyEvent e) {
				caretCondPosInit = (form.getCaretPosition() == 0) && (form.getSelectedText() == null);
				transcription = form.getText();
			}
			public void keyTyped(KeyEvent e)   { }
		});
		form.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (changedTranscription) {
					modifTranscription(form.getText());
					changedTranscription = false;
				}
			}
		});
		add("North",  form);

		this.gls = new contentMenu(gloseElt, this);
		listener = new myActionListener();
		gls.addActionListener(listener);
		add("South",  gls);
	}
	public boolean autoGlose() {
		boolean stop = false;
		gls.removeActionListener(listener);
		if ( (glose == null) || (glose.equals("")) ) {
			Vector probables = gls.getProbablesGloses(form.getText());
			if (probables.size() == 1) {
				modifGlose((String)probables.elementAt(0), false); //sans maj
				hilite();
			} else if (probables.size() > 1) {
				int wordPosition = getWordIndex() + 1;
				dialog_gloss gd = new dialog_gloss(level, form.getText(), "w["+wordPosition+"]:"+form.getText(), getWin());
				gd.show();
				if (!gd.isNext()) {
					stop = true;
				} else {
					if(gd.isModif()) {
						String answer = gd.getAnswer();
						modifGlose(answer, false); //sans maj
						hilite();
					}
				}
			}
		}
		gls.addActionListener(listener);
		return stop;
	}
	public void loadElements() {
		try {
			XPath myXpath = new JDOMXPath(getPref("W_FORM_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			this.transcriptionElt = (org.jdom.Element)myXpath.selectSingleNode(wordElt);

			myXpath = new JDOMXPath(".");
			this.transcription = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(transcriptionElt), myXpath.getNavigator());

			myXpath = new JDOMXPath(getPref("W_GLOSE_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			this.gloseElt = (org.jdom.Element)myXpath.selectSingleNode(wordElt);

			myXpath = new JDOMXPath(".");
			this.glose = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(gloseElt), myXpath.getNavigator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Coupe un mot en deux à partir du point d'insertion.
	* Recopie la glose du premier mot dans le nouveau mot crée.
	*/
	public void split() {
		String mot    = form.getText();
		String before = mot.substring(0, form.getCaretPosition());
		String after  = mot.substring(form.getCaretPosition());
		if(transcriptionElt != null) {
			if (frame_editor.ask4replace(transcriptionElt)) {
				fromSentencePanel.splitWordAt(wordIndex, before, after);
			}
		}
	}
	private void concat() {
		fromSentencePanel.concatWord(wordIndex - 1, wordIndex);
	}
	private void modifTranscription(String content) {
		fromSentencePanel.modifWordContent(wordIndex, content, true, true);
	}
	private void deleteTranscription() {
		if (transcriptionElt != null) {
			if (frame_editor.ask4replace(transcriptionElt)) {
				fromSentencePanel.modifWordContent(wordIndex, null, true, true);
			}
		}
	}
	private void deleteWord() {
		if (frame_editor.ask4replace(wordElt)) {
			fromSentencePanel.deleteWord(wordIndex);
		}
	}
	private void deleteGlose() {
		gloseElt = null;
		fromSentencePanel.modifWordContent(wordIndex, null, false, true);
		loadElements();
		setModif();
		gls.reset("", gloseElt);
	}
	private void modifGlose(String gloseChoisie, boolean update) {
		if (gloseElt == null) {
			gloseElt = domOperator.createLevelGlose("word");
			wordElt  = domOperator.embedGlose2level("word", gloseElt, wordElt);
		}
		fromSentencePanel.modifWordContent(wordIndex, gloseChoisie, false, update);
		loadElements();
		setModif();
		gls.reset(gloseChoisie, gloseElt);
	}
	public void hilite() {
		setBackground(getBackground().darker());
	}
	public org.jdom.Element getWord() {
		return wordElt;
	}
    private class myActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (gls.isOther()) {
				Vector all = gls.getAllGloses(form.getText());
				gls.reset(all);
			} else {
				String newGls = ((String)gls.getSelectedItem()).trim();
				if ((e.getActionCommand().equals("comboBoxChanged")) || (e.getActionCommand().equals("comboBoxEdited"))) {
					if (!newGls.equals(glose)) {
						if (newGls.equals("")) {
							deleteGlose();
						} else {
							modifGlose(newGls, true);
						}
						//pour qu'il ne passe pas deux fois par actionPerformed
						gls.removeActionListener(listener);
					} else {
						gls.reset(glose, gloseElt);
					}
				}
			}
		}
    }
};