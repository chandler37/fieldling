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
import javax.swing.event.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur graphique pour un texte.
*/
public class panel_level_text extends JPanel implements MouseListener {

		private org.jdom.Element		textElt          = null;
		private org.jdom.Element        transcriptionElt = null;
		private org.jdom.Element        translationElt   = null;
		private frame_editor            from;
		private contentTextArea         transcription, translation;
		private boolean                 caretCondPosInitForm, caretCondPosInitTransl; //caret position = 0 and no selection
		private boolean                 changedTranscription, changedTranslation;


	/** Crée un conteneur graphique contenant la présentation du texte.
	*
	* @param frame_editor         fenêtre dans laquelle le texte est.
	*/
	public panel_level_text(frame_editor from) {
		super(new FlowLayout(FlowLayout.LEFT));
 		this.caretCondPosInitForm   = true;
 		this.caretCondPosInitTransl = true;
		this.from    = from;
		this.changedTranscription   = false;
		this.changedTranslation     = false;
		loadElements();

		transcription = new contentTextArea(transcriptionElt, "transcription", this);
		transcription.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setModif(); }
			public void insertUpdate(DocumentEvent e)  { setModif(); }
			public void removeUpdate(DocumentEvent e)  { setModif(); }
		});
		transcription.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e)    {}
			public void keyPressed(KeyEvent e)  {
				caretCondPosInitForm = (transcription.getCaretPosition() == 0) && (transcription.getSelectedText() == null);
			}
			public void keyReleased(KeyEvent e) {
				if ((e.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (e.getKeyChar() == KeyEvent.VK_DELETE)){
					if ((transcription.getText().equals("")) && (transcription.getCaretPosition() == 0) && (caretCondPosInitForm)) {  //touche effacer avant le 1er caractere
						changedTranscription = false;
						transcription.desactivate();
						deleteTranscription();
					} else {
						changedTranscription = true;
						if (!transcription.isActivate()) {
							transcription.activate();
						}
					}
				} else {
					changedTranscription = true;
					if (!transcription.isActivate()) {
						transcription.activate();
					}
				}
			}
		});
		transcription.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (changedTranscription) {
					modifTranscription(transcription.getText());
					changedTranscription = false;
				}
			}
		});
        JScrollPane transcriptionScroller = new JScrollPane(transcription, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(transcriptionScroller);

		translation = new contentTextArea(translationElt, "translation", this);
		translation.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { setModif(); }
			public void insertUpdate(DocumentEvent e)  { setModif(); }
			public void removeUpdate(DocumentEvent e)  { setModif(); }
		});
		translation.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)  {
				caretCondPosInitTransl = (translation.getCaretPosition() == 0) && (translation.getSelectedText() == null);
			}
			public void keyTyped(KeyEvent e)    {}
			public void keyReleased(KeyEvent e) {
				if ((e.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (e.getKeyChar() == KeyEvent.VK_DELETE)){
					if ((translation.getText().equals("")) &&(translation.getCaretPosition() == 0) && (caretCondPosInitTransl)) {  //touche effacer avant le 1er caractere
						changedTranslation = false;
						translation.desactivate();
						deleteTranslation();
					} else {
						changedTranslation = true;
						if (!translation.isActivate()) {
							translation.activate();
						}
					}
				} else {
					changedTranslation = true;
					if (!translation.isActivate()) {
						translation.activate();
					}
				}
			}
		});
		translation.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (changedTranslation) {
					modifTranslation(translation.getText());
					changedTranslation = false;
				}
			}
		});
        JScrollPane translationScroller = new JScrollPane(translation, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(translationScroller);
		addMouseListener(this);
	}
	private void modifTranscription(String content) {
		from.doModifTextContent(content, true);
	}
	private void deleteTranscription() {
		if (transcriptionElt != null) {
			if (frame_editor.ask4replace(transcriptionElt)) {
				from.doModifTextContent(null, true);
			}
		} else {
			from.doModifTextContent(null, true);
		}
	}
	private void modifTranslation(String content) {
		from.doModifTextContent(content, false);
	}
	private void deleteTranslation() {
		if (translationElt != null) {
			if (frame_editor.ask4replace(translationElt)) {
				from.doModifTextContent(null, false);
			}
		} else {
			from.doModifTextContent(null, false);
		}
	}
	private void loadElements() {
		try {
			XPath myXpath = new JDOMXPath(from.getT());
			myXpath.setFunctionContext(new myXPathFunctionContext());
			textElt = (org.jdom.Element)myXpath.selectSingleNode(from.getData());
			myXpath = new JDOMXPath(from.getTF());
			myXpath.setFunctionContext(new myXPathFunctionContext());
			transcriptionElt = (org.jdom.Element)myXpath.selectSingleNode(textElt);
			myXpath = new JDOMXPath(from.getTG());
			myXpath.setFunctionContext(new myXPathFunctionContext());
			translationElt = (org.jdom.Element)myXpath.selectSingleNode(textElt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void reloadElements() {
		loadElements();
		transcription.setTextElt(transcriptionElt);
		translation.setTextElt(translationElt);
	}
	public void setModif()  {
		from.setModif();
	}
	protected void splitText2Sentences() {
		try {
			String form = transcription.getText();
			String[] sentences = form.split(from.getPref("regexS"));
			XPath myXpath = new JDOMXPath(from.getS());
			myXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List list = myXpath.selectNodes(textElt);
			if (list.size() != 0) {
				int answer = JOptionPane.showConfirmDialog(null, "Do you really want to replace all the sentences?", "Alert", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					org.jdom.Element newTextElt = ((org.jdom.Element)textElt.clone());

					//suppression des anciennes phrases
					java.util.List newlist = myXpath.selectNodes(newTextElt);
					for (int i=0; i<newlist.size(); i++) {
						org.jdom.Element elt = (Element)newlist.get(i);
						elt.getParent().removeContent(elt);
					}

					//creation des nouvelles phrases
					for (int i=0; i<sentences.length; i++) {
						String sentence = sentences[i];
						org.jdom.Element newS   = from.getDomOperator().createSentence();
						org.jdom.Element newS_F = from.getDomOperator().createLevelTranscr("sentence");
						newS_F.setText(sentence);
						newS = from.getDomOperator().embedTranscr2level("sentence", newS_F, newS);
						newTextElt = from.getDomOperator().embedSentence2text(newS, newTextElt);
					}
					replaceContent(textElt, newTextElt, "text");
				}
			} else {
					org.jdom.Element newTextElt = ((org.jdom.Element)textElt.clone());
					//creation des nouvelles phrases
					for (int i=0; i<sentences.length; i++) {
						String sentence = sentences[i];
						org.jdom.Element newS   = from.getDomOperator().createSentence();
						org.jdom.Element newS_F = from.getDomOperator().createLevelTranscr("sentence");
						newS_F.setText(sentence);
						newS = from.getDomOperator().embedTranscr2level("sentence", newS_F, newS);
						newTextElt = from.getDomOperator().embedSentence2text(newS, newTextElt);
					}
					replaceContent(textElt, newTextElt, "text");
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
	protected org.jdom.Element getTextFromSentences(boolean isTranscription) {
		try {
			String s = "";
			XPath mXpath = new JDOMXPath(from.getS());
			mXpath.setFunctionContext(new myXPathFunctionContext());
			java.util.List mList = mXpath.selectNodes(textElt);
			for (int j=0; j<mList.size(); j++) {
				org.jdom.Element m = (org.jdom.Element)mList.get(j);
				XPath contentXpath = new JDOMXPath(isTranscription?from.getSF():from.getSG());
				contentXpath.setFunctionContext(new myXPathFunctionContext());
				s += contentXpath.stringValueOf(m);
				if (j < (mList.size()-1)) s += "\n";
			}
			org.jdom.Element newElt;
			if (isTranscription) {
				newElt = ((org.jdom.Element)transcriptionElt.clone()).setText(s);
			} else {
				newElt = ((org.jdom.Element)translationElt.clone()).setText(s);
			}
			return newElt;
		} catch (Exception err) {
			System.out.println(err.getMessage());
			return null;
		}
	}
	public void replaceAttributes(org.jdom.Element oldElt, java.util.List attributes, String what)  {
		from.doReplaceAttributes(oldElt, attributes, what);
	}
	public void replaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, String what)  {
		from.doReplaceContent(oldElt, newElt, what);
	}
	/** popup menu pour inspecter les attributs et le code de l'élément
	*/
	public void mouseClicked(MouseEvent e)  {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}
	public void mousePressed(MouseEvent e)  {}
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem showAttribute = new JMenuItem("show the text attributes");
			showAttribute.setEnabled(textElt!=null);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(textElt);
					attChoice.show();
					if (attChoice.isModif()) {
						replaceAttributes(textElt, attChoice.getResult(), "text attributes");
						setModif();
					}
				}
			});
			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(textElt!=null);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(textElt, null);
					src.show();
					if (src.isModif()) {
						replaceContent(textElt, src.getResult(), "text");
					}
				}
			});
			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(textElt!=null);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(textElt);
					chooser.show();
					if (chooser.isValidated()) {
						replaceContent(textElt, chooser.getElt(), "text");
					}
				}
			});

			popup.add(showAttribute);
			popup.add(showSrc);
			popup.add(transform);
			popup.show(this, e.getX(), e.getY());
		}
	}
};