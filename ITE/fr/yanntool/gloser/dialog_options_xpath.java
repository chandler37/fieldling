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
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;

import org.jaxen.*;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;

/*-----------------------------------------------------------------------*/
public class dialog_options_xpath extends JDialog implements ActionListener {

		private static final String         RESS_PROFILES     = new String("/profiles/prof.xml");
		public  static final String         RESS_DEFAULT_PROF = new String("/profiles/default.xml");

		private Hashtable newPref;
		private boolean   valid;

		final JTextField textFromRoot, sentenceFromText, wordFromSentence, morphemeFromWord,
				sentenceToText, wordToSentence, morphemeToWord,
				textFormXpath,  sentenceFormXpath,  wordFormXpath,  morphemeFormXpath,
				textFromFormXpath,  sentenceFromFormXpath,  wordFromFormXpath,  morphemeFromFormXpath,
				textGloseXpath, sentenceGloseXpath, wordGloseXpath, morphemeGloseXpath,
				textFromGloseXpath, sentenceFromGloseXpath, wordFromGloseXpath, morphemeFromGloseXpath,
				xslt_modif,
				startText, startSentence, startWord, startMorpheme,
				endText, endSentence, endWord, endMorpheme;
		JComboBox profs;

	public dialog_options_xpath(Hashtable currentPref, frame_editor win) {
 		super(InterlinearTextEditor.win, "Xpath definitions for "+win.getTitle(), true);
 		this.valid   = false;
 		this.newPref = new Hashtable(currentPref);

		JButton okButton           = new JButton("Ok");
		JButton cancelButton       = new JButton("Cancel");

		textFromRoot       = new JTextField((String)newPref.get("T_XPATH"),            10);
		sentenceFromText   = new JTextField((String)newPref.get("S_XPATH"),            10);
		wordFromSentence   = new JTextField((String)newPref.get("W_XPATH"),            10);
		morphemeFromWord   = new JTextField((String)newPref.get("M_XPATH"),            10);

		sentenceToText     = new JTextField((String)newPref.get("S2T_XPATH"),          10);
		wordToSentence     = new JTextField((String)newPref.get("W2S_XPATH"),          10);
		morphemeToWord     = new JTextField((String)newPref.get("M2W_XPATH"),          10);

		textFormXpath      = new JTextField((String)newPref.get("T_FORM_XPATH"),       10);
		sentenceFormXpath  = new JTextField((String)newPref.get("S_FORM_XPATH"),       10);
		wordFormXpath      = new JTextField((String)newPref.get("W_FORM_XPATH"),       10);
		morphemeFormXpath  = new JTextField((String)newPref.get("M_FORM_XPATH"),       10);

		textFromFormXpath      = new JTextField((String)newPref.get("FORM2T_XPATH"),   10);
		sentenceFromFormXpath  = new JTextField((String)newPref.get("FORM2S_XPATH"),   10);
		wordFromFormXpath      = new JTextField((String)newPref.get("FORM2W_XPATH"),   10);
		morphemeFromFormXpath  = new JTextField((String)newPref.get("FORM2M_XPATH"),   10);

		textGloseXpath     = new JTextField((String)newPref.get("T_GLOSE_XPATH"),      10);
		sentenceGloseXpath = new JTextField((String)newPref.get("S_GLOSE_XPATH"),      10);
		wordGloseXpath     = new JTextField((String)newPref.get("W_GLOSE_XPATH"),      10);
		morphemeGloseXpath = new JTextField((String)newPref.get("M_GLOSE_XPATH"),      10);

		textFromGloseXpath     = new JTextField((String)newPref.get("GLOSE2T_XPATH"),  10);
		sentenceFromGloseXpath = new JTextField((String)newPref.get("GLOSE2S_XPATH"),  10);
		wordFromGloseXpath     = new JTextField((String)newPref.get("GLOSE2W_XPATH"),  10);
		morphemeFromGloseXpath = new JTextField((String)newPref.get("GLOSE2M_XPATH"),  10);

		startText             = new JTextField((String)newPref.get("STARTT_XPATH"),   10);
		startSentence         = new JTextField((String)newPref.get("STARTS_XPATH"),   10);
		startWord             = new JTextField((String)newPref.get("STARTW_XPATH"),   10);
		startMorpheme         = new JTextField((String)newPref.get("STARTM_XPATH"),   10);
		endText               = new JTextField((String)newPref.get("ENDT_XPATH"),     10);
		endSentence           = new JTextField((String)newPref.get("ENDS_XPATH"),     10);
		endWord               = new JTextField((String)newPref.get("ENDW_XPATH"),     10);
		endMorpheme           = new JTextField((String)newPref.get("ENDM_XPATH"),     10);

		xslt_modif              = new JTextField((String)newPref.get("XSLT_modifications"),      10);

		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		setBackground(Color.white);
 		getContentPane().setLayout(new GridLayout(0, 5));

		addSeparator("Xpath definitions");
		getContentPane().add(new JLabel("Text node"));
		getContentPane().add(new JLabel("from root"));
		getContentPane().add(textFromRoot);
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));

		getContentPane().add(new JLabel("Sentence nodeset"));
		getContentPane().add(new JLabel("from text node"));
		getContentPane().add(sentenceFromText);
		getContentPane().add(new JLabel("to text node"));
		getContentPane().add(sentenceToText);

		getContentPane().add(new JLabel("Word nodeset"));
		getContentPane().add(new JLabel("from sentence node"));
		getContentPane().add(wordFromSentence);
		getContentPane().add(new JLabel("to sentence node"));
		getContentPane().add(wordToSentence);

		getContentPane().add(new JLabel("Morpheme nodeset"));
		getContentPane().add(new JLabel("from word node"));
		getContentPane().add(morphemeFromWord);
		getContentPane().add(new JLabel("to word node"));
		getContentPane().add(morphemeToWord);

		addSeparator();
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel("\u2026text level"));
		getContentPane().add(new JLabel("\u2026sentence level"));
		getContentPane().add(new JLabel("\u2026word level"));
		getContentPane().add(new JLabel("\u2026morpheme level"));
		getContentPane().add(new JLabel("transcription from\u2026"));
		getContentPane().add(textFormXpath);
		getContentPane().add(sentenceFormXpath);
		getContentPane().add(wordFormXpath);
		getContentPane().add(morphemeFormXpath);
		getContentPane().add(new JLabel("transcription to\u2026"));
		getContentPane().add(textFromFormXpath);
		getContentPane().add(sentenceFromFormXpath);
		getContentPane().add(wordFromFormXpath);
		getContentPane().add(morphemeFromFormXpath);
		getContentPane().add(new JLabel("translation/gloss from\u2026"));
		getContentPane().add(textGloseXpath);
		getContentPane().add(sentenceGloseXpath);
		getContentPane().add(wordGloseXpath);
		getContentPane().add(morphemeGloseXpath);
		getContentPane().add(new JLabel("translation/gloss to\u2026"));
		getContentPane().add(textFromGloseXpath);
		getContentPane().add(sentenceFromGloseXpath);
		getContentPane().add(wordFromGloseXpath);
		getContentPane().add(morphemeFromGloseXpath);
		getContentPane().add(new JLabel("start time ofset from\u2026"));
		getContentPane().add(startText);
		getContentPane().add(startSentence);
		getContentPane().add(startWord);
		getContentPane().add(startMorpheme);
		getContentPane().add(new JLabel("end time ofset from\u2026"));
		getContentPane().add(endText);
		getContentPane().add(endSentence);
		getContentPane().add(endWord);
		getContentPane().add(endMorpheme);

		addSeparator();
		addSeparator("Stylesheets:");
		getContentPane().add(new JLabel("xslt modifications"));
		getContentPane().add(xslt_modif);
		profs = new JComboBox();
		final Hashtable profHash = getProfiles();
		for (Enumeration e = profHash.keys() ; e.hasMoreElements() ;) {
			profs.addItem(e.nextElement());
		}
		profs.setSelectedItem("Default values");
		profs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.println("load:"+profHash.get(profs.getSelectedItem()));
				Hashtable newValues = loadValues((String)profHash.get(profs.getSelectedItem()));
				if (newValues != null) {
					newPref = newValues;
					changeValues(newPref);
				}
			}
		});
		getContentPane().add(profs);
		getContentPane().add(cancelButton);
		getContentPane().add(okButton);
		setSize(850, 450);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(dim.width/15*10, dim.height/2);
	}
	private void addTitle (String title) {
		JLabel label = new JLabel(title);
		label.setForeground(Color.BLUE);
		getContentPane().add(label);
	}
	private void addSeparator (String s) {
		addTitle(s);
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
	}
	private void addSeparator () {
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
		getContentPane().add(new JLabel(""));
	}
	static public Hashtable loadValues (String ficName) {
		Hashtable hash = new Hashtable();
		SAXBuilder builder = new SAXBuilder(false);
		try {
			org.jdom.Document doc = builder.build(ficName);
			org.jdom.Element root = doc.getRootElement();
			java.util.List list = root.getChildren("option");
			for (int i=0; i<list.size(); i++) {
				org.jdom.Element elt = (org.jdom.Element)list.get(i);
				String name = elt.getAttributeValue("name");
				String value = elt.getAttributeValue("value");
				hash.put(name, value);
			}
			return hash;
		} catch (JDOMException e) {
			JOptionPane.showMessageDialog(null, ficName + " is not well formed.\n"+e.getMessage());
			return null;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception.\n"+e.getMessage());
			return null;
		}
	}

	private void changeValues (Hashtable pref) {
		textFromRoot.setText((String)pref.get("T_XPATH"));
		sentenceFromText.setText((String)pref.get("S_XPATH"));
		wordFromSentence.setText((String)pref.get("W_XPATH"));
		morphemeFromWord.setText((String)pref.get("M_XPATH"));
		sentenceToText.setText((String)pref.get("S2T_XPATH"));
		wordToSentence.setText((String)pref.get("W2S_XPATH"));
		morphemeToWord.setText((String)pref.get("M2W_XPATH"));

		textFormXpath.setText((String)pref.get("T_FORM_XPATH"));
		sentenceFormXpath.setText((String)pref.get("S_FORM_XPATH"));
		wordFormXpath.setText((String)pref.get("W_FORM_XPATH"));
		morphemeFormXpath.setText((String)pref.get("M_FORM_XPATH"));

		textFromFormXpath.setText((String)pref.get("FORM2T_XPATH"));
		sentenceFromFormXpath.setText((String)pref.get("FORM2S_XPATH"));
		wordFromFormXpath.setText((String)pref.get("FORM2W_XPATH"));
		morphemeFromFormXpath.setText((String)pref.get("FORM2M_XPATH"));

		textGloseXpath.setText((String)pref.get("T_GLOSE_XPATH"));
		sentenceGloseXpath.setText((String)pref.get("S_GLOSE_XPATH"));
		wordGloseXpath.setText((String)pref.get("W_GLOSE_XPATH"));
		morphemeGloseXpath.setText((String)pref.get("M_GLOSE_XPATH"));

		textFromGloseXpath.setText((String)pref.get("GLOSE2T_XPATH"));
		sentenceFromGloseXpath.setText((String)pref.get("GLOSE2S_XPATH"));
		wordFromGloseXpath.setText((String)pref.get("GLOSE2W_XPATH"));
		morphemeFromGloseXpath.setText((String)pref.get("GLOSE2M_XPATH"));

		startText.setText((String)pref.get("STARTT_XPATH"));
		startSentence.setText((String)pref.get("STARTS_XPATH"));
		startWord.setText((String)pref.get("STARTW_XPATH"));
		startMorpheme.setText((String)pref.get("STARTM_XPATH"));
		endText.setText((String)pref.get("ENDT_XPATH"));
		endSentence.setText((String)pref.get("ENDS_XPATH"));
		endWord.setText((String)pref.get("ENDW_XPATH"));
		endMorpheme.setText((String)pref.get("ENDM_XPATH"));

		xslt_modif.setText((String)pref.get("XSLT_modifications"));
	}
	public void actionPerformed(ActionEvent e){
		if ((e.getSource() instanceof JButton) && (e.getActionCommand().equals("Ok"))) {
			validation();
		} else if ((e.getSource() instanceof JButton) && (e.getActionCommand().equals("Cancel"))) {
			dispose();
		}
	}
	private void validation() {
		newPref.put("T_XPATH",       textFromRoot.getText());
		newPref.put("S_XPATH",       sentenceFromText.getText());
		newPref.put("W_XPATH",       wordFromSentence.getText());
		newPref.put("M_XPATH",       morphemeFromWord.getText());
		newPref.put("S2T_XPATH",     sentenceToText.getText());
		newPref.put("W2S_XPATH",     wordToSentence.getText());
		newPref.put("M2W_XPATH",     morphemeToWord.getText());

		newPref.put("T_FORM_XPATH",  textFormXpath.getText());
		newPref.put("S_FORM_XPATH",  sentenceFormXpath.getText());
		newPref.put("W_FORM_XPATH",  wordFormXpath.getText());
		newPref.put("M_FORM_XPATH",  morphemeFormXpath.getText());

		newPref.put("FORM2T_XPATH",  textFromFormXpath.getText());
		newPref.put("FORM2S_XPATH",  sentenceFromFormXpath.getText());
		newPref.put("FORM2W_XPATH",  wordFromFormXpath.getText());
		newPref.put("FORM2M_XPATH",  morphemeFromFormXpath.getText());

		newPref.put("T_GLOSE_XPATH", textGloseXpath.getText());
		newPref.put("S_GLOSE_XPATH", sentenceGloseXpath.getText());
		newPref.put("W_GLOSE_XPATH", wordGloseXpath.getText());
		newPref.put("M_GLOSE_XPATH", morphemeGloseXpath.getText());

		newPref.put("GLOSE2T_XPATH", textFromGloseXpath.getText());
		newPref.put("GLOSE2S_XPATH", sentenceFromGloseXpath.getText());
		newPref.put("GLOSE2W_XPATH", wordFromGloseXpath.getText());
		newPref.put("GLOSE2M_XPATH", morphemeFromGloseXpath.getText());

		newPref.put("STARTT_XPATH",  startText.getText());
		newPref.put("STARTS_XPATH",  startSentence.getText());
		newPref.put("STARTW_XPATH",  startWord.getText());
		newPref.put("STARTM_XPATH",  startMorpheme.getText());
		newPref.put("ENDT_XPATH",    endText.getText());
		newPref.put("ENDS_XPATH",    endSentence.getText());
		newPref.put("ENDW_XPATH",    endWord.getText());
		newPref.put("ENDM_XPATH",    endMorpheme.getText());

		newPref.put("XSLT_modifications", xslt_modif.getText());

		valid = true;
		dispose();
	}
	public Hashtable getPrefs() {
		return newPref;
	}
	public boolean isValidated() {
		return valid;
	}
	private Hashtable getProfiles() {
		boolean inResource = true;
		Hashtable res = new Hashtable();
		res.put("Default values", getClass().getResource(RESS_DEFAULT_PROF).toString());

		String profiles = getClass().getResource(RESS_PROFILES).toString();
		File profilesFile = new File("prof.xml");
		if (profilesFile.exists()) {
			profiles = "prof.xml";
			inResource = false;
		}
		SAXBuilder builder = new SAXBuilder(false);
		try {
			org.jdom.Document doc = builder.build(profiles);
			org.jdom.Element root = doc.getRootElement();
			java.util.List list = root.getChildren("item");
			for (int i=0; i<list.size(); i++) {
				org.jdom.Element elt = (org.jdom.Element)list.get(i);
				String name = elt.getAttributeValue("name");
				String value = elt.getAttributeValue("value");
				if (inResource) {
					value = getClass().getResource(value).toString();
				}
				res.put(name, value);
			}
		} catch (JDOMException e) {
			JOptionPane.showMessageDialog(null, profiles + " is not well formed.\n"+e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception.\n"+e.getMessage());
		}
		return res;
	}
};