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
import java.awt.event.*;
import java.awt.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.function.NormalizeSpaceFunction;

/*-----------------------------------------------------------------------*/

/** Editeur pour la transcription ou la traduction d'un texte.
*/
public class contentTextArea extends JTextArea implements MouseListener {

		private org.jdom.Element    elt;
		private panel_level_text    from;
		private boolean             isTranscription;

	/** Crée un conteneur graphique pour une transcription ou une traduction de texte.
	*
	* @param elt                Element XML correspondant à la transcription ou la traduction d'un texte.
	* @param from               Le conteneur graphique.
	*/
	public contentTextArea(org.jdom.Element elt, String title, panel_level_text from) {
 		super(13, 0);
 		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
		setBorder(new javax.swing.border.TitledBorder(getBorder(), title));
		setBackground((elt == null)? Color.lightGray: Color.white);
 		this.elt    = elt;
 		this.from   = from;
 		this.isTranscription = title.equals("transcription");
		addMouseListener(this);
		try {
			XPath myXpath = new JDOMXPath(".");
			String form = myXpath.stringValueOf(elt);
			setText(form);
			if (form.length() <= 0) setColumns(50);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setTextElt(org.jdom.Element elt) {
 		setElt(elt);
		try {
			XPath myXpath = new JDOMXPath(".");
			String form = myXpath.stringValueOf(elt);
			setText(form);
		} catch (Exception e) {
			System.out.println(e.getMessage());
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
						from.replaceAttributes(elt, attChoice.getResult(), "text "+new String(isTranscription?"transcription":"translation")+" attributes");
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
						from.replaceContent(elt, src.getResult(), "text "+new String(isTranscription?"transcription":"translation"));
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
						from.replaceContent(elt, chooser.getElt(), "text "+new String(isTranscription?"transcription":"translation"));
					}
				}
			});
			popup.add(transform);

			if (isTranscription) {
				popup.addSeparator();
				JMenuItem splitT2S = new JMenuItem("text > sentences");
				splitT2S.setEnabled(elt!=null);
				splitT2S.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						from.splitText2Sentences();
					}
				});
				popup.add(splitT2S);
			}
			JMenuItem getTfromS = new JMenuItem("text < sentence");
			getTfromS.setEnabled(elt!=null);
			getTfromS.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					org.jdom.Element newElt = from.getTextFromSentences(isTranscription);
					if (newElt != null) {
						from.replaceContent(elt, newElt, "text transcription");
					}
				}
			});
			popup.add(getTfromS);
			popup.show(this, e.getX(), e.getY());
		}
	}
};