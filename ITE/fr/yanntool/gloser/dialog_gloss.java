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
/*-----------------------------------------------------------------------*/

/** Fen�tre de dialogue permetant de choisir la valeur d'une glose parmis
 * l'ensemble des gloses deja utilis�es pour gloser la forme demand�e.
*/
public class dialog_gloss extends JDialog {

		private boolean           modif;
		private boolean           next;
		private String            response;


	/** Cr�e une fen�tre de dialog modal pour choisir la glose dans une liste ferm�e.
	* La liste ne pr�sente que les seules gloses du corpus et du lexique,
	* d�j� utilis�es pour gloser la forme demand�e.
	*
	* @param level           le niveau <code>word</code> ou <code>morpheme</code>
	* @param forme           la transcription dont on cherche la glose
	* @param message         le message affich� dans la fen�tre permettant d'identifier le mot ou morph�me sur lequel on travaille
	* @param win             la fen�tre de l'�diteur
	*/
	public dialog_gloss(String level, String forme, String message, frame_editor win) {
 		super(InterlinearTextEditor.win, true);
 		this.modif     = false;
 		this.next      = false;
 		this.response  = null;

		final JComboBox glsMenu = new JComboBox();
		Vector all = listOfGloses(forme, level);
		for (int i=0; i< all.size(); i++) {
			glsMenu.addItem(all.get(i));
		}
		glsMenu.setEditable(true);//c'est pas sur que ce soit une bonne chose!
		glsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modif = true;
				response = (String)glsMenu.getSelectedItem();
				next = true;
				dispose();
			}
		});

		JButton skipButton = new JButton("Skip");
		skipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next = true;
				dispose();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		setBackground(Color.white);
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(new JLabel(message));
		getContentPane().add(glsMenu);
		getContentPane().add(skipButton);
		getContentPane().add(closeButton);
		pack();
	}
	public String getAnswer() {
		return response;
	}
	public boolean isModif() {
		return modif;
	}
	public boolean isNext() {
		return next;
	}
	/** Construit une liste de toutes les glose pr�sentes
	*  dans le corpus ou dans le lexique utilis�es pour gloser la forme demand�e
	*  puis les trie par ordre de frequence.
	*/
	private Vector listOfGloses (String forme, String level) {

		Vector all = new Vector();
		Hashtable gloses  = InterlinearTextEditor.getCorpusLexicon(level);
		Hashtable lexicon = InterlinearTextEditor.getLexicon(level);

		Vector vect = new Vector();
 		// Ajoute les gloses possibles pour la forme donn�e (en cherchant en premier dans le lexique puis dans le corpus)
		if (lexicon.containsKey(forme)) {
			Hashtable values = (Hashtable)lexicon.get(forme);
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
		if (gloses.containsKey(forme)) {
			Hashtable values = (Hashtable)gloses.get(forme);
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
};