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
import java.util.regex.*;
/*-----------------------------------------------------------------------*/

/** Fenêtre de dialogue pour choisir les parametres d'une concordance.
*/
public class options_conc {

		String level, scope, nbWord, pattern, xpathPredicate;
		int valid;

	public options_conc() {
		Object[]      message = new Object[9];

		message[0] = "Pattern's form (regular expression)";
		JTextField regex = new JTextField();
		message[1] = regex;

		message[2] = "Predicate XPath expression";
		JTextField predicate = new JTextField();
		message[3] = predicate;

		message[4] = "Level";
		JComboBox cbLevel = new JComboBox(new String[] {"word","morpheme"});
		message[5] = cbLevel;

		message[6] = "Scope";
		JComboBox cbScope = new JComboBox(new String[] {"text","sentence"});
		message[7] = cbScope;

		JPanel nbPanel = new JPanel();
		nbPanel.add(new JLabel("Number of words by context"));
		JComboBox cbNbItems = new JComboBox(new String[] {"1","2","3","4","5","6","7","8","9","10"});
		nbPanel.add(cbNbItems);
		message[8] = nbPanel;

		this.valid = JOptionPane.showOptionDialog(null, message, "Concordance options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		this.level = (String)cbLevel.getSelectedItem();
		this.scope = (String)cbScope.getSelectedItem();
		this.nbWord = (String)cbNbItems.getSelectedItem();
		this.pattern = regex.getText();
		this.xpathPredicate = predicate.getText();
		try {
			Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			JOptionPane.showMessageDialog(null, "Bad expression: "+pattern);
			valid = JOptionPane.CANCEL_OPTION;
		}
	}
	/**
	* @return <code>String</code> le niveau d'analyse (word ou morpheme).
	*/
	public String getLevel() {
		return level;
	}
	/**
	* @return <code>String</code> la portée de l'analyse (sentence ou text).
	*/
	public String getScope() {
		return scope;
	}
	/**
	* @return <code>String</code> le pattern recherché (expression régulière).
	* Les caractères réservés en XML sont remplacés par les entités caractères correspondants
	* pour une utilisation dans une valeur d'attribut
	*/
	public String getPattern() {
		String forXMLattribut = pattern;
		forXMLattribut = forXMLattribut.replaceAll("\\\"", "&quot;");
		forXMLattribut = forXMLattribut.replaceAll("<", "&lt;");
		forXMLattribut = forXMLattribut.replaceAll(">", "&gt;");
		//forXMLattribut = forXMLattribut.replaceAll("\\\'", "&apos;");
		forXMLattribut = forXMLattribut.replaceAll("@", "&amp;");
		return forXMLattribut;
	}
	/**
	* @return <code>String</code> le prédicat xpath recherché.
	*/
	public String getPredicate() {
		return xpathPredicate;
	}
	/**
	* @return <code>String</code> le nombre de mot conservés pour les contexte droite et gauche.
	*/
	public String getNbWord() {
		return nbWord;
	}
	/**
	* @return <code>boolean</code> 'true' si le dialogue a été validé.
	*/
	public boolean isValidated() {
		return (valid == JOptionPane.OK_OPTION);
	}
};