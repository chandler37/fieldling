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
import java.util.*;

import org.jdom.*;
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.function.NormalizeSpaceFunction;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
/*-----------------------------------------------------------------------*/

/** Menu pour la glose d'un mot ou d'un morphème.
*/
public class contentMenu extends JComboBox implements MouseListener {

		private org.jdom.Element  gloseElt;
		private contentPanel      from;
		private static String     others = "other gloss...";

	/** Crée un menu déroulant pour lister la glose et un item pour choisir une autre glose.
	*
	* @param gloseElt     Element XML correspondant à la glose (du mot ou du morphème).
	* @param from         Le conteneur graphique.
	*/
	public contentMenu(org.jdom.Element gloseElt, contentPanel from) {
 		super();
 		this.gloseElt = gloseElt;
 		this.from     = from;
		Component[] comps = getComponents();
		for(int i=0; i<comps.length; i++) {
			comps[i].addMouseListener(this);
		}
		try {
			XPath myXpath = new JDOMXPath(".");
			String glose = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(gloseElt), myXpath.getNavigator());
			addItem(glose);
			addItem(others);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void reset(Vector all)  {
		removeAllItems();
		for (int i=0; i< all.size(); i++) {
			addItem(all.get(i));
		}
		setEditable(true);
		getEditor().selectAll();
	}
	public void reset(String newValue, org.jdom.Element  newGloseElt)  {
		gloseElt = newGloseElt;
		removeAllItems();
		addItem(newValue);
		addItem(others);
		setEditable(false);
	}
	public boolean isOther()  {
		return (getSelectedItem() == others);
	}

	/** Construit une liste de toutes les glose présentes
	*  dans le corpus ou dans le lexique.
	*/
	public Vector getAllGloses(String transcription) {
		Hashtable gloses = InterlinearTextEditor.getCorpusLexicon(from.getLevel());
		Hashtable lexicon = InterlinearTextEditor.getLexicon(from.getLevel());
		Vector all = getProbablesGloses(transcription);
		all.addElement("");
 		// Ajoute les autres gloses du texte et du lexique par ordre alphabétique
		Vector values = new Vector();
		for (Enumeration e = gloses.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			if (!key.equals(transcription)) {
				Hashtable v = (Hashtable)gloses.get(key);
				for (Enumeration enum = v.keys() ; enum.hasMoreElements() ;) {
					String val = (String)enum.nextElement();
					if ((!val.equals("")) && (!all.contains(val)) && (!values.contains(val))) {
						values.addElement(val);
					}
				}
			}
		}
		for (Enumeration e = lexicon.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			if (!key.equals(transcription)) {
				Hashtable v = (Hashtable)lexicon.get(key);
				for (Enumeration enum = v.keys() ; enum.hasMoreElements() ;) {
					String val = (String)enum.nextElement();
					if ((!val.equals("")) && (!all.contains(val)) && (!values.contains(val))) {
						values.addElement(val);
					}
				}
			}
		}
		Collections.sort(values);
		for (int i=0; i< values.size(); i++) {
			all.addElement(values.get(i));
		}
		return all;
	}

	/** Construit une liste de toutes les glose déjà rencontrées
	*  dans le corpus ou dans le lexique pour gloser la transcription 'forme'.
	*/
	public Vector getProbablesGloses(String transcription) {
		Vector all = new Vector();
		Hashtable gloses = InterlinearTextEditor.getCorpusLexicon(from.getLevel());
		Hashtable lexicon = InterlinearTextEditor.getLexicon(from.getLevel());

		Vector vect = new Vector();
 		// Ajoute les gloses possibles pour la forme donnée (en cherchant en premier dans le lexique puis dans le corpus)
		if (lexicon.containsKey(transcription)) {
			Hashtable values = (Hashtable)lexicon.get(transcription);
			for (Enumeration e = values.keys() ; e.hasMoreElements() ;) {
				String val = (String)e.nextElement();
				Integer nb = (Integer)values.get(val);
				if (!val.equals("")) {
					int pos = -1;
					for (int i=0;i<vect.size();i++) {
						gloseFreq gf = (gloseFreq)vect.get(i);
						if (gf.getGlose().equals(val)) {
							pos = i;
						}
					}
					if (pos != -1) {
						gloseFreq gf = (gloseFreq)vect.get(pos);
						gf.addFreq(nb.intValue());
					} else {
						vect.addElement(new gloseFreq(val, nb.intValue()));
					}
				}
			}
		}
		if (gloses.containsKey(transcription)) {
			Hashtable values = (Hashtable)gloses.get(transcription);
			for (Enumeration e = values.keys() ; e.hasMoreElements() ;) {
				String val = (String)e.nextElement();
				Integer nb = (Integer)values.get(val);
				if (!val.equals("")) {
					int pos = -1;
					for (int i=0;i<vect.size();i++) {
						gloseFreq gf = (gloseFreq)vect.get(i);
						if (gf.getGlose().equals(val)) {
							pos = i;
						}
					}
					if (pos != -1) {
						gloseFreq gf = (gloseFreq)vect.get(pos);
						gf.addFreq(nb.intValue());
					} else {
						vect.addElement(new gloseFreq(val, nb.intValue()));
					}
				}
			}
		}
		Collections.sort(vect); //trier par ordre de frequence
		for (int i=0;i<vect.size();i++) {
			gloseFreq gf = (gloseFreq)vect.get(i);
			all.addElement(gf.getGlose());
		}
		return all;
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
			JMenuItem showAttribute = new JMenuItem("show attributes");
			showAttribute.setEnabled(gloseElt!=null);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(gloseElt);
					attChoice.show();
					if (attChoice.isModif()) {
						from.setModif();
						from.replaceAttributes(gloseElt, attChoice.getResult(), from.getLevel()+" glose attributes");
					}
				}
			});
			popup.add(showAttribute);

			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(gloseElt!=null);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(gloseElt, from);
					src.show();
					if (src.isModif()) {
						from.replaceContent(gloseElt, src.getResult(), from.getLevel()+" glose");
					}
				}
			});
			popup.add(showSrc);

			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(gloseElt!=null);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(gloseElt);
					chooser.show();
					if (chooser.isValidated()) {
						from.replaceContent(gloseElt, chooser.getElt(), from.getLevel()+" glose");
					}
				}
			});
			popup.add(transform);

			popup.show(this, e.getX(), e.getY());
		}
	}
};