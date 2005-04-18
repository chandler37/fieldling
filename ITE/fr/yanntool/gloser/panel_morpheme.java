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
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur pour un morphème (composé d'une transcription et d'une glose).
*/
public class panel_morpheme extends JPanel implements MouseListener {

		private org.jdom.Element       wordElt;
		private org.jdom.Element       morphemeElt;
		private org.jdom.Element       morphemeFormeElt = null;
		private org.jdom.Element       morphemeGloseElt = null;
		private panel_word_4_morpheme  from;
		private contentTextField       form             = null;
		private contentMenu            gls;
		private int                    morphemeIndex;
		private boolean                caretCondPosInit; //caret position = 0 and no selection
		private String                 transcription = "";
		private String                 glose = "";
		private boolean                changedTranscription;
		private myActionListener       listener;

		static private String          level            = "morpheme";

	/** Crée un conteneur graphique pour un morphème.
	* La transcription du morphème est écrite dans un éditeur et la glose dans un menu.
	*
	* @param morpheme                  Elément XML correspondant au morpheme.
	*/
	public panel_morpheme(org.jdom.Element morpheme, org.jdom.Element word, panel_word_4_morpheme fromPanel, int mIndex) {
 		super(new BorderLayout());
 		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
 		this.wordElt              = word;
 		this.morphemeElt          = morpheme;
		this.from                 = fromPanel;
		this.morphemeIndex        = mIndex;
		this.caretCondPosInit     = true;
		this.changedTranscription = false;
		loadElements();
		addMouseListener(this);

		this.form = new contentTextField(morphemeFormeElt, this.from, true);
		form.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { from.setModif(); }
			public void insertUpdate(DocumentEvent e)  { from.setModif(); }
			public void removeUpdate(DocumentEvent e)  { from.setModif(); }
		});
		form.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_I) && (e.isControlDown())) {  //ctrl+i = couper un mot en deux
					split();
				} else if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					;//nothing
				} else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
					if ((form.getCaretPosition() == 0) && (morphemeIndex > 0) && (caretCondPosInit)) {  //touche effacer avant le 1er caractere du mot = le coller au precedent si il existe
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
							deleteMorpheme();
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
		add("North", form);

		this.gls = new contentMenu(morphemeGloseElt, from);
		listener = new myActionListener();
		gls.addActionListener(listener);
		add("South", gls);
	}
	public int getMorphemeIndex() {
		return morphemeIndex;
	}
	public boolean gloseIt() {
		boolean stop = false;
		gls.removeActionListener(listener);
		if ( (glose == null) || (glose.equals("")) ) {
			Vector probables = gls.getProbablesGloses(form.getText());
			if (probables.size() == 1) {
				modifGlose((String)probables.elementAt(0), false); //sans maj
				hilite();
			} else if (probables.size() > 1) {
				int wordPosition = from.getWordIndex() + 1;
				int morphemePosition = getMorphemeIndex() + 1;
				dialog_gloss gd = new dialog_gloss(level, form.getText(), "w["+wordPosition+"]m["+morphemePosition+"]:"+form.getText(), from.getWin());
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
			XPath aXpath = new JDOMXPath(from.getPref("M_FORM_XPATH"));
			aXpath.setFunctionContext(new myXPathFunctionContext());
			this.morphemeFormeElt = (org.jdom.Element)aXpath.selectSingleNode(morphemeElt);
			aXpath = new JDOMXPath(".");
			this.transcription = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(morphemeFormeElt), aXpath.getNavigator());

			aXpath = new JDOMXPath(from.getPref("M_GLOSE_XPATH"));
			aXpath.setFunctionContext(new myXPathFunctionContext());
			this.morphemeGloseElt = (org.jdom.Element)aXpath.selectSingleNode(morphemeElt);
			aXpath = new JDOMXPath(".");
			this.glose = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(morphemeGloseElt), aXpath.getNavigator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void split() {
		String morpheme    = form.getText();
		String before = morpheme.substring(0, form.getCaretPosition());
		String after  = morpheme.substring(form.getCaretPosition());
		if(morphemeFormeElt != null) {
			if (frame_editor.ask4replace(morphemeFormeElt)) {
				from.splitMorphemeAt(morphemeIndex, before, after);
			}
		}
	}
	private void concat() {
		from.concatMorpheme(morphemeIndex - 1, morphemeIndex);
	}
	private void modifTranscription(String content) {
		from.modifMorphemeContent(morphemeIndex, content, true, true);
	}
	private void deleteTranscription() {
		if (morphemeFormeElt != null) {
			if (frame_editor.ask4replace(morphemeFormeElt)) {
				from.modifMorphemeContent(morphemeIndex, null, true, true);
			}
		}
	}
	private void deleteMorpheme() {
		if (frame_editor.ask4replace(morphemeElt)) {
			from.deleteMorpheme(morphemeIndex);
		}
	}
	private void modifGlose(String gloseChoisie, boolean update) {
		if (morphemeGloseElt == null) {
			morphemeGloseElt = from.domOperator.createLevelGlose("morpheme");
			morphemeElt      = from.domOperator.embedGlose2level("morpheme", morphemeGloseElt, morphemeElt);
		}
		from.modifMorphemeContent(morphemeIndex, gloseChoisie, false, update);
		loadElements();
		from.setModif();
		gls.reset(gloseChoisie, morphemeGloseElt);
	}
	private void deleteGlose() {
		morphemeGloseElt = null;
		from.modifMorphemeContent(morphemeIndex, null, false, true);
		loadElements();
		from.setModif();
		gls.reset("", morphemeGloseElt);
	}

	public void hilite() {
		setBackground(Color.red);
	}
	public org.jdom.Element getMorpheme() {
		return morphemeElt;
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
			JMenuItem showAttribute = new JMenuItem("show the morpheme attributes");
			showAttribute.setEnabled(morphemeElt!=null);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(morphemeElt);
					attChoice.show();
					if (attChoice.isModif()) {
						from.replaceAttributes(morphemeElt, attChoice.getResult(), "morpheme attributes");
						from.setModif();
					}
				}
			});
			popup.add(showAttribute);

			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(morphemeElt!=null);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(morphemeElt,from);
					src.show();
					if (src.isModif()) {
						from.replaceMorpheme(morphemeElt, src.getResult());
					}
				}
			});
			popup.add(showSrc);

			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(morphemeElt!=null);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(morphemeElt);
					chooser.show();
					if (chooser.isValidated()) {
						from.replaceMorpheme(morphemeElt, chooser.getElt());
					}
				}
			});
			popup.add(transform);

			if (from.getWin().isMediaTag("morpheme", morphemeElt)) {
				JMenuItem play = new JMenuItem("play morpheme");
				play.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						from.getWin().playMedia("morpheme", morphemeElt);
					}
				});
				popup.add(play);
			}

			popup.show(this, e.getX(), e.getY());
		}
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
						gls.reset(glose, morphemeGloseElt);
					}
				}
			}
		}
    }
};