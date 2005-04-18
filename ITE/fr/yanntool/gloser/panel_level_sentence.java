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
import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Conteneur graphique pour une phrase sous forme interlinéaire.
*/
public class panel_level_sentence extends JPanel implements MouseListener {

		private org.jdom.Element		sentence;
		private frame_editor            from;
		private String                  level;

	/** Crée un conteneur graphique contenant la présentation interlinéaire d'une phrase.
	*
	* @param sentenceElt         Elément XML correspondant à la phrase.
	* @param contentLevel        le niveau de représentation de la phrase (sentence, word, morpheme).
	*/
	public panel_level_sentence(frame_editor from, String contentLevel) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.level           = contentLevel;
		this.from            = from;
		this.sentence        = from.getSentence(1);
		addMouseListener(this);
	}
	/** Retourne le composant mot s'il est dans le panel phrase, sinon retourne null.
	*/
	public panel_word getWordComponent(org.jdom.Element word) {
		Component[] components = getComponents();
		for (int i=0; i<components.length; i++) {
			Component comp = components[i];
			if (comp instanceof panel_word) {
				if (((panel_word)comp).getWord().equals(word)) {
					return (panel_word)comp;
				}
			}
		}
		return null;
	}
	/** Retourne le composant morpheme s'il est dans le panel phrase, sinon retourne null.
	*/
	public panel_morpheme getMorphemeComponent(org.jdom.Element morpheme) {
		Component[] components = getComponents();
		for (int i=0; i<components.length; i++) {
			Component comp = components[i];
			if (comp instanceof panel_word_4_morpheme) {
				panel_morpheme mp = ((panel_word_4_morpheme)comp).getMorphemePanel(morpheme);
				if (mp!=null) {
					return mp;
				}
			}
		}
		return null;
	}
	/** Retourne le composant mot si le morpheme est dans le panel phrase, sinon retourne null.
	*/
	public panel_word_4_morpheme getWordComponentFromMorpheme(org.jdom.Element morpheme) {
		Component[] components = getComponents();
		for (int i=0; i<components.length; i++) {
			Component comp = components[i];
			if (comp instanceof panel_word_4_morpheme) {
				panel_morpheme mp = ((panel_word_4_morpheme)comp).getMorphemePanel(morpheme);
				if (mp!=null) {
					return (panel_word_4_morpheme)comp;
				}
			}
		}
		return null;
	}
	public String getLevel() {
		return level;
	}
	public frame_editor getWin() {
		return from;
	}

	//actions sur les elements
	public void replaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, String what) {
		from.doReplaceContent(oldElt, newElt, what);
	}
	public void replaceAttributes(org.jdom.Element oldElt, java.util.List attributes, String what)  {
		from.doReplaceAttributes(oldElt, attributes, what);
	}
	//actions sur les phrases
	public void deleteSentence() {
		from.doDeleteSentence();
	}
	public void modifSentenceContent(String s, boolean isTranscription) {
		from.doModifSentenceContent(s, isTranscription);
	}

	//actions sur les mots
	public void modifWordContent(int index, String s, boolean isTranscription, boolean update) {
		from.doModifWordContent(index, s, isTranscription, update);
	}
	public void deleteWord(int index) {
		from.doDeleteWord(index);
	}
	public void insertWordAt(org.jdom.Element w, int index) {
		from.doInsertWordAt(w, index);
	}
	public void concatWord(int index1, int index2) {
		from.doJoinWord(index1, index2);
	}
	public void splitWordAt(int index, String form1, String form2) {
		from.doSplitWord(index, form1, form2);
	}

	//actions sur les morphemes
	public void appendMorpheme(int wordIndex) {
		from.doAppendMorpheme(wordIndex);
	}
	public void modifMorphemeContent(int wordIndex, int morphemeIndex, String s, boolean isTranscription, boolean update) {
		from.doModifMorphemeContent(wordIndex, morphemeIndex, s, isTranscription, update);
	}
	public void deleteMorpheme(int wordIndex, int morphemeIndex) {
		from.doDeleteMorpheme(wordIndex, morphemeIndex);
	}
	public void concatMorpheme(int wordIndex, int index1, int index2) {
		from.doJoinMorpheme(wordIndex, index1, index2);
	}
	public void splitMorphemeAt(int wordIndex, int morphemeIndex, String form1, String form2) {
		from.doSplitMorpheme(wordIndex, morphemeIndex, form1, form2);
	}

	/** transmet la valeur de modif à l'éditeur.
	*/
	public void setModif() {
		from.setModif();
	}

	/** Fixe la valeur des gloses de chaque mot ou morphème non déjà glosé de la phrase.
	* S'il n'y en a qu'une glose trouvée dans le lexique ou dans le corpus: la glose est écrite;
	* S'il y a plusieurs gloses trouvées: propose un menu pour choisir celle qu'on veut;
	*/
	public void autoGlose() {
		boolean stop = false;
		Component[] comps = getComponents();
		for(int i=0; i<comps.length; i++) {
			if (!stop) {
				Component comp = comps[i];
				if (comp instanceof panel_word) {
					panel_word w = (panel_word)comp;
					stop = w.autoGlose();
				} else if (comp instanceof panel_word_4_morpheme) {
					panel_word_4_morpheme w = (panel_word_4_morpheme)comp;
					stop = w.autoGlose();
				}
			}
		}
	}

	public org.jdom.Element getSentence() {
		return sentence;
	}
	private void createAudioButton() {
		if (from.isMediaTag("sentence", sentence)) {
			ImageIcon playImg = new ImageIcon(getClass().getResource("/icons/sound1.gif"));
			JButton playButton      = new JButton(playImg);
			playButton.setPreferredSize(new Dimension(playImg.getIconWidth(), playImg.getIconHeight()));
			playButton.setBorder(new javax.swing.border.EmptyBorder(playButton.getBorder().getBorderInsets(playButton)));
			playButton.setToolTipText("play sentence");
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					from.playMedia("sentence", sentence);
				}
			});
			add(playButton);
		}
	}

	public void reload(org.jdom.Element sentenceElt) {
		this.sentence = sentenceElt;
	}
	public void loadSentence(org.jdom.Element sentenceElt) {
		this.sentence = sentenceElt;
		if (sentence != null) {
			try {
				removeAll();
				updateUI();
				/* debut ajout pour player audio */
				createAudioButton();
				/* fin ajout pour player audio */
				if (level.equals("sentence")) {
					contentPanel content = new panel_sentence(this, from.getCurrentS());
					add(content);
				} else {
					XPath myXpath = new JDOMXPath(from.getW());
					myXpath.setFunctionContext(new myXPathFunctionContext());
					java.util.List results = myXpath.selectNodes(sentence);
					Iterator resultIter = results.iterator();
					int i = 0;
					while ( resultIter.hasNext() ) {
						Element w = (Element)resultIter.next();
						i++;
						contentPanel content;
						if (level.equals("word")) {
							content = new panel_word(w, this, i);
						} else {
							content = new panel_word_4_morpheme(w, this, i);
						}
						add(content);
					}
				}
				validate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			removeAll();
			updateUI();
		}
		myResize();
	}
	/** Ajuste la taille en hauteur du panel afin qu'on puisse scroller jusqu'au dernier element
	*/
	private void myResize() {
		int nbComp = getComponentCount();
		if (nbComp != 0) {
			JComponent last   = (JComponent)getComponent(nbComp-1);
			double y          = last.getLocation(null).getY();
			double height     = last.getSize().getHeight();
			Dimension dimPref = getPreferredSize();
			int hauteur       = (int)(y + height);
			int largeur       = (int)dimPref.getWidth();
			setPreferredSize(new Dimension(largeur, hauteur));
		}
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
			JMenuItem showAttribute = new JMenuItem("show the sentence attributes");
			showAttribute.setEnabled(from.getCurrentS()!=0);
			showAttribute.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_attributs attChoice = new dialog_attributs(sentence);
					attChoice.show();
					if (attChoice.isModif()) {
						replaceAttributes(sentence, attChoice.getResult(), "sentence attributes");
						from.setModif();
					}
				}
			});
			JMenuItem showSrc = new JMenuItem("show the source code");
			showSrc.setEnabled(from.getCurrentS()!=0);
			showSrc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_source src = new dialog_source(sentence, null);
					src.show();
					if (src.isModif()) {
						replaceContent(sentence, src.getResult(), "sentence");
					}
				}
			});
			JMenuItem transform = new JMenuItem("transform...");
			transform.setEnabled(from.getCurrentS()!=0);
			transform.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					dialog_apply_xslt chooser = new dialog_apply_xslt(sentence);
					chooser.show();
					if (chooser.isValidated()) {
						replaceContent(sentence, chooser.getElt(), "sentence");
					}
				}
			});
			JMenuItem playSentence = null;
			if (from.isMediaTag("sentence", sentence)) {
				playSentence = new JMenuItem("play sentence");
				playSentence.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						from.playMedia("sentence", sentence);
					}
				});
			}
			JMenuItem insertSentence = new JMenuItem("insert sentence");
			insertSentence.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					from.doAddSentence();
				}
			});
			JMenuItem duplicateSentence = new JMenuItem("duplicate sentence");
			duplicateSentence.setEnabled(from.getCurrentS()!=0);
			duplicateSentence.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					from.doDuplicateSentence();
				}
			});
			JMenuItem deleteSentence = new JMenuItem("delete sentence");
			deleteSentence.setEnabled(from.getMaxS()>0);
			deleteSentence.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					from.doDeleteSentence();
				}
			});
			popup.add(showAttribute);
			popup.add(showSrc);
			popup.add(transform);
			if (playSentence!=null) popup.add(playSentence);
			popup.addSeparator();
			popup.add(insertSentence);
			popup.add(duplicateSentence);
			popup.add(deleteSentence);

			if (level.equals("word") || level.equals("morpheme")) {
				JMenuItem append_W = new JMenuItem("append word");
				append_W.setEnabled(from.getCurrentS()!=0);
				append_W.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						from.doAppendWord();
					}
				});
				popup.add(insertSentence);
				popup.add(append_W);
			}

			popup.show(this, e.getX(), e.getY());
		}
	}
};