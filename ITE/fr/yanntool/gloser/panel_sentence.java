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

/** Conteneur pour une phrase (composée d'une transcription et d'une traduction).
*/
public class panel_sentence extends contentPanel {

		static  private String   level               = "sentence";
		private org.jdom.Element transcriptionElt  = null;
		private org.jdom.Element translationElt    = null;
		private contentTextField form, transl;
		private boolean          caretCondPosInitForm, caretCondPosInitTransl; //caret position = 0 and no selection
		private boolean          changedTranscription, changedTranslation;

	/** Crée un conteneur graphique pour une phrase.
	* La transcription et la traduction sont écrite dans des éditeurs.
	*
	* @param fromSentencePanel         Le conteneur graphique de la phrase.
	* @param num                       Numéro de la phrase.
	*/
	public panel_sentence(panel_level_sentence fromSentencePanel,int num) {
 		super(fromSentencePanel, num, null);
 		this.caretCondPosInitForm   = true;
 		this.caretCondPosInitTransl = true;
 		this.changedTranscription   = false;
 		this.changedTranslation     = false;
		try {
			XPath myXpath = new JDOMXPath(getPref("S_FORM_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			transcriptionElt = (org.jdom.Element)myXpath.selectSingleNode(sentenceElt);

			myXpath = new JDOMXPath(getPref("S_GLOSE_XPATH"));
			myXpath.setFunctionContext(new myXPathFunctionContext());
			translationElt = (org.jdom.Element)myXpath.selectSingleNode(sentenceElt);

			int winWidth = (int)fromSentencePanel.getSize().getWidth();

			this.form = new contentTextField(transcriptionElt, this, true);
			form.setPreferredSize(new Dimension(winWidth - 40, (int)form.getPreferredSize().getHeight()));
			form.moveCaretPosition(0);
			form.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) { setModif(); }
				public void insertUpdate(DocumentEvent e)  { setModif(); }
				public void removeUpdate(DocumentEvent e)  { setModif(); }
			});
			form.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					caretCondPosInitForm = (form.getCaretPosition() == 0) && (form.getSelectedText() == null);
				}
				public void keyTyped(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {
					if ((e.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (e.getKeyChar() == KeyEvent.VK_DELETE)){
						if ((form.getText().equals("")) && (form.getCaretPosition() == 0) && (caretCondPosInitForm)) {  //touche effacer avant le 1er caractere
							changedTranscription = false;
							form.desactivate();
							deleteTranscription();
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

			this.transl = new contentTextField(translationElt, this, false);
			transl.setPreferredSize(new Dimension(winWidth - 40, (int)transl.getPreferredSize().getHeight()));
			transl.moveCaretPosition(0);
			transl.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) { setModif(); }
				public void insertUpdate(DocumentEvent e)  { setModif(); }
				public void removeUpdate(DocumentEvent e)  { setModif(); }
			});
			transl.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					caretCondPosInitTransl = (transl.getCaretPosition() == 0) && (transl.getSelectedText() == null);
				}
				public void keyTyped(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {
					if ((e.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (e.getKeyChar() == KeyEvent.VK_DELETE)){
						if ((transl.getText().equals("")) && (transl.getCaretPosition() == 0) && (caretCondPosInitTransl)) {  //touche effacer avant le 1er caractere
							changedTranslation = false;
							transl.desactivate();
							deleteTranslation();
						} else {
							changedTranslation = true;
							if (!transl.isActivate()) {
								transl.activate();
							}
						}
					} else {
						changedTranslation = true;
						if (!transl.isActivate()) {
							transl.activate();
						}
					}
				}
			});
			transl.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {}
				public void focusLost(FocusEvent e) {
					if (changedTranslation) {
						modifTranslation(transl.getText());
						changedTranslation = false;
					}
				}
			});
			add("North",  form);
			add("South",  transl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void modifTranscription(String content) {
		fromSentencePanel.modifSentenceContent(content, true);
	}
	private void deleteTranscription() {
		if (transcriptionElt != null) {
			if (frame_editor.ask4replace(transcriptionElt)) {
				fromSentencePanel.modifSentenceContent(null, true);
			}
		} else {
			fromSentencePanel.modifSentenceContent(null, true);
		}
	}
	private void modifTranslation(String content) {
		fromSentencePanel.modifSentenceContent(content, false);
	}
	private void deleteTranslation() {
		if (translationElt != null) {
			if (frame_editor.ask4replace(translationElt)) {
				fromSentencePanel.modifSentenceContent(null, false);
			}
		} else {
			fromSentencePanel.modifSentenceContent(null, false);
		}
	}
};