/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2004 Michel Jacobson jacobson@idf.ext.jussieu.fr
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
import javax.swing.event.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;

import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;
/*-----------------------------------------------------------------------*/

/** Dialogue pour le choix des parametres du Replace.
*/
public class frame_replace extends frame_find {

		private JTextField             by;
		private JButton                replaceButton, replaceAllButton;

	public frame_replace(frame_editor prov) {
		super(prov);
		setTitle("Replace");

		JPanel byPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		by = new JTextField(options.getReplaceString(), 10);
		by.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { options.setReplaceString(by.getText()); }
			public void insertUpdate(DocumentEvent e)  { options.setReplaceString(by.getText()); }
			public void removeUpdate(DocumentEvent e)  { options.setReplaceString(by.getText()); }
		});
		byPanel.add(new JLabel("Replace by:"));
		byPanel.add(by);
		globalPanel.add(byPanel, 1);

		nextButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) {
				if (e.getPropertyName().equals("find")) {
					replaceButton.setEnabled(e.getNewValue().equals(new Boolean(true)));
				}
			}
		});

		replaceButton      = new JButton("Replace");
		replaceButton.setEnabled(false);
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (find != null) {
					int wordIndex = -1;
					if (options.getLevel().equals("word")) {
						wordIndex = win.getWordIndex(find);
					} else if (options.getLevel().equals("morpheme")) {
						wordIndex = win.getWordIndexFromMorpheme(find);
					}
					if (wordIndex != -1) {
						try {
							XPath myXpath = new JDOMXPath(options.getContentXpath(win));
							myXpath.setFunctionContext(new myXPathFunctionContext());
							org.jdom.Element elt = (org.jdom.Element)myXpath.selectSingleNode(find);
							myXpath = new JDOMXPath(".");
							String s = NormalizeSpaceFunction.evaluate(myXpath.stringValueOf(elt), myXpath.getNavigator());
							String newS = replace(s);
							if (options.getLevel().equals("word")) {
								win.doModifWordContent(wordIndex, newS, options.isTranscription(), true);
							} else if (options.getLevel().equals("morpheme")) {
								int morphemeIndex = win.getMorphemeIndex(find);
								win.doModifMorphemeContent(wordIndex, morphemeIndex, newS, options.isTranscription(), true);
							}
						} catch (Exception err) {
							err.printStackTrace();
						}
					}
				}
			}
		});
		replaceAllButton      = new JButton("Replace All");
		replaceAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				org.jdom.Document doc = ((org.jdom.Document)data.clone());
				try {
					XPath myXpath = new JDOMXPath(options.getFindXpath(win, true));
					myXpath.setFunctionContext(new myXPathFunctionContext());
					java.util.List res = myXpath.selectNodes(doc);
					java.util.ListIterator iter = res.listIterator();
					if (res.size() > 0) {
						int answer = JOptionPane.showConfirmDialog(null, res.size() +" occurences are found. Do you really want to replace all of them?", "Alert", JOptionPane.YES_NO_OPTION);
						if (answer == JOptionPane.OK_OPTION) {
							while (iter.hasNext()) {
								org.jdom.Element item = (org.jdom.Element)iter.next();
								XPath aXpath = new JDOMXPath(options.getContentXpath(win));
								aXpath.setFunctionContext(new myXPathFunctionContext());
								org.jdom.Element elt = (org.jdom.Element)aXpath.selectSingleNode(item);
								aXpath = new JDOMXPath(".");
								String s = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(elt), aXpath.getNavigator());
								String newS = replace(s);
								elt.setText(newS);
							}
							XPath textXpath = new JDOMXPath(win.getT());
							textXpath.setFunctionContext(new myXPathFunctionContext());
							org.jdom.Element textElt = (org.jdom.Element)textXpath.selectSingleNode(data);
							org.jdom.Element newTextElt = (org.jdom.Element)textXpath.selectSingleNode(doc);
							win.doReplaceContent(textElt, (org.jdom.Element)newTextElt.detach(), "text");
						}
					}
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		});

		actionPanel.add(replaceAllButton, 0);
		actionPanel.add(replaceButton, 0);
		setBounds(375, 20, 430, 290);
	}
	private String replace (String s) {
		String response;
		if (options.isRegex()) {
			response = s.replaceAll(options.getSearchString(), by.getText());
		} else {
			String reste = s;
			response     = "";
			int pos = reste.indexOf(options.getSearchString());
			while (pos != -1) {
				response += reste.substring(0, pos) + by.getText();
				reste = reste.substring(pos+options.getSearchString().length());
				pos = reste.indexOf(options.getSearchString());
			}
			response += reste;
		}
		return response;
	}
};