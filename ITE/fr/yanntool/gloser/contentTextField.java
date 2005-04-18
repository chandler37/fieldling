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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.function.NormalizeSpaceFunction;

/*-----------------------------------------------------------------------*/

/** Champs texte pour la transcription ou la traduction d'une phrase.
*/
public class contentTextField extends JTextField implements MouseListener {

		private org.jdom.Element elt;
		private contentPanel     from;
		private boolean          isTranscription;

	/** Crée un conteneur graphique pour une transcription de phrase mot ou morpheme.
	*
	* @param elt       Element XML correspondant à la transcription (phrase, mot, morphème) ou traduction de la phrase.
	* @param from      Le conteneur graphique.
	*/
	public contentTextField(org.jdom.Element elt, contentPanel from, boolean isTranscription) {
 		super();
		setBackground((elt == null)? Color.lightGray: Color.white);
 		this.elt    = elt;
 		this.from   = from;
 		this.isTranscription = isTranscription;
		addMouseListener(this);
		try {
			XPath myXpath = new JDOMXPath(".");
			String form = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(elt), myXpath.getNavigator());
			setText(form);
			setColumns((form.length() <= 0)? 20: 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setElt(org.jdom.Element elt) {
 		this.elt    = elt;
		setBackground((elt == null)? Color.lightGray: Color.white);
	}
	public boolean isActivate() {
		return getBackground().equals(Color.white);
	}
	public void activate() {
		setBackground(Color.white);
	}
	public void desactivate() {
		setBackground(Color.lightGray);
	}
	/** popup menu pour inspecter les attributs et le code de l'élément
	*/
	public void mouseClicked(MouseEvent e)  {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}
	public void mousePressed(MouseEvent e)  {
		if (e.getButton() == MouseEvent.BUTTON3) {
			dispatchEvent(new FocusEvent(this, FocusEvent.FOCUS_LOST));
		}
	}
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem showAttribute = new JMenuItem("show attributes");
			showAttribute.setEnabled(elt!=null);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(elt);
					attChoice.show();
					if (attChoice.isModif()) {
						from.setModif();
						from.replaceAttributes(elt, attChoice.getResult(), from.getLevel()+" "+new String(isTranscription?"transcription":"glose")+" attributes");
					}
				}
			});
			popup.add(showAttribute);
			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(elt!=null);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(elt, from);
					src.show();
					if (src.isModif()) {
						from.replaceContent(elt, src.getResult(), from.getLevel()+" "+new String(isTranscription?"transcription":"glose"));
					}
				}
			});
			popup.add(showSrc);
			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(elt!=null);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(elt);
					chooser.show();
					if (chooser.isValidated()) {
						from.replaceContent(elt, chooser.getElt(), from.getLevel()+" "+new String(isTranscription?"transcription":"glose"));
					}
				}
			});
			popup.add(transform);

			popup.addSeparator();
			if (isTranscription) {
				if (from.getLevel().equals("sentence")) {
					JMenuItem getSfromW = new JMenuItem("sentence < words");
					getSfromW.setEnabled(elt!=null);
					getSfromW.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							org.jdom.Element newElt = from.getSentenceFromWords(elt, true, false);
							if (newElt != null) {
								from.replaceContent(elt, newElt, from.getLevel()+" "+new String(isTranscription?"transcription":"glose"));
							}
						}
					});
					popup.add(getSfromW);
					JMenuItem getSfromM = new JMenuItem("sentence < morphemes");
					getSfromM.setEnabled(elt!=null);
					getSfromM.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							org.jdom.Element newElt = from.getSentenceFromWords(elt, false, true);
							if (newElt != null) {
								from.replaceContent(elt, newElt, from.getLevel()+" "+new String(isTranscription?"transcription":"glose"));
							}
						}
					});
					popup.add(getSfromM);
					JMenuItem splitS2W = new JMenuItem("sentence > words");
					splitS2W.setEnabled(elt!=null);
					splitS2W.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							from.splitSentence2Words(elt, true, false);
						}
					});
					popup.add(splitS2W);
					JMenuItem splitS2M = new JMenuItem("sentence > morphemes");
					splitS2M.setEnabled(elt!=null);
					splitS2M.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							from.splitSentence2Words(elt, false, true);
						}
					});
					popup.add(splitS2M);
				} else if (from.getLevel().equals("word")) {
					JMenuItem splitW = new JMenuItem("split the word");
					splitW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
					splitW.setEnabled(elt!=null);
					splitW.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							((panel_word)from).split();
						}
					});
					popup.add(splitW);
					JMenuItem splitW2M = new JMenuItem("word > morphemes");
					splitW2M.setEnabled(elt!=null);
					splitW2M.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							from.splitWord2Morphemes(elt);
						}
					});
					popup.add(splitW2M);
					JMenuItem getWfromM = new JMenuItem("word < morphemes");
					getWfromM.setEnabled(elt!=null);
					getWfromM.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							org.jdom.Element newElt = from.getWordFromMorphemes(elt);
							if (newElt != null) {
								from.replaceContent(elt, newElt, from.getLevel()+" "+new String(isTranscription?"transcription":"glose"));
							}
						}
					});
					popup.add(getWfromM);
				} else if (from.getLevel().equals("morpheme")) {
					JMenuItem splitM = new JMenuItem("split the morpheme");
					splitM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
					splitM.setEnabled(elt!=null);
					splitM.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							((panel_morpheme)getParent()).split();
						}
					});
					popup.add(splitM);
				}
			}
			popup.show(this, e.getX(), e.getY());
		}
	}
};