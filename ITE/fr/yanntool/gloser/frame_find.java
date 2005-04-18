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

/** Dialogue pour le choix des parametres du Find.
*/
public class frame_find extends JInternalFrame implements InternalFrameListener {

		protected final frame_editor        win;
		protected JPanel                 globalPanel;
		protected JPanel                 actionPanel;
		protected org.jdom.Element       find;
		protected JButton                nextButton;
		protected options_find           options;
		protected org.jdom.Document      data;

		private java.util.ListIterator   searchList;
		private JCheckBox                regex, fromBegining;
		private JRadioButton             contains, exact;
		private JTextField               what;
		private JTextField               condition;
		private JComboBox                where;

	public frame_find(frame_editor prov) {
		super("Find", false, true, false, false);
		this.searchList   = null;
		this.win          = prov;
		this.data         = win.getData();
		this.find         = null;
		addInternalFrameListener(this);

		options      = win.getFindReplaceOptions();
		globalPanel = new JPanel();

		JPanel whatPanel = new JPanel();
		what = new JTextField(options.getSearchString(), 10);
		what.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { options.setSearchString(what.getText());reset(); }
			public void insertUpdate(DocumentEvent e)  { options.setSearchString(what.getText());reset(); }
			public void removeUpdate(DocumentEvent e)  { options.setSearchString(what.getText());reset(); }
		});
		condition = new JTextField(options.getPredicate(), 15);
		condition.setBackground((options.getPredicate().equals(""))? getBackground(): Color.white);
		condition.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { options.setPredicate(condition.getText());reset(); }
			public void insertUpdate(DocumentEvent e)  { options.setPredicate(condition.getText());reset(); }
			public void removeUpdate(DocumentEvent e)  { options.setPredicate(condition.getText());reset(); }
		});
		whatPanel.add(new JLabel("Find what:"));
		whatPanel.add(what);
		where = new JComboBox(new String[] {"word transcription","word gloss","morpheme transcription","morpheme gloss"});
		where.setSelectedItem(options.getSearchWhere());
		where.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.setSearchWhere((String)where.getSelectedItem());
				reset();
			}
		});
		whatPanel.add(new JLabel("In:"));
		whatPanel.add(where);

		globalPanel.add(whatPanel);

		JPanel xpathPanel = new JPanel();
		xpathPanel.add(new JLabel("xpath predicate:"));
		xpathPanel.add(condition);

		JPanel condPanel = new JPanel(new GridLayout(0, 1));
 		condPanel.setBorder(new javax.swing.border.TitledBorder(condPanel.getBorder(), "conditions"));
		regex = new JCheckBox("Regular expression", options.isRegex());
		regex.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (regex.isSelected()) {
					options.setRegex(true);
					contains.setEnabled(false);
					exact.setEnabled(false);
				} else {
					options.setRegex(false);
					contains.setEnabled(true);
					exact.setEnabled(true);
				}
				reset();
			}
		});
		JPanel testPanel = new JPanel();
		exact = new JRadioButton("exact match", options.isExact());
		exact.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setExact(exact.isSelected());
				reset();
			}
		});
		contains = new JRadioButton("contains", !options.isExact());
		contains.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setExact(!contains.isSelected());
				reset();
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(contains);
		group.add(exact);
		testPanel.add(contains);
		testPanel.add(exact);
		fromBegining = new JCheckBox("Start searching at the begining of the document", options.isFromStart());
		fromBegining.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				options.setFromStart(fromBegining.isSelected());
				reset();
			}
		});
		condPanel.add(regex);
		condPanel.add(testPanel);
		condPanel.add(xpathPanel);
		condPanel.add(fromBegining);

		globalPanel.add(condPanel);

		actionPanel = new JPanel(new GridLayout(0, 1));
		nextButton      = new JButton("Next");
		nextButton.setEnabled(!options.getSearchString().equals(""));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (options.isModif()) {
					searchList = getSearchList();
					options.resetModif();
				}
				boolean oldFind = (find != null);
				find = doNext();
				nextButton.firePropertyChange("find", oldFind, (find != null));
			}
		});
		JButton closeButton      = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		actionPanel.add(nextButton);
		actionPanel.add(closeButton);

		globalPanel.add(actionPanel);

		setContentPane(globalPanel);
		setBounds(375, 20, 410, 250);
	}
	private java.util.ListIterator getSearchList() {
		if (!options.parseRegex()) {
			return null;
		}
		String searchWhere  = (String)where.getSelectedItem();
		if (data != null) {
			try {
				XPath myXpath = new JDOMXPath(options.getFindXpath(win, false));
				myXpath.setFunctionContext(new myXPathFunctionContext());
				java.util.List res = myXpath.selectNodes(data);
				return res.listIterator();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	private org.jdom.Element doNext() {
		if ((searchList != null) && (data != null)) {
			try {
				XPath sXpath = new JDOMXPath(win.getT()+"/"+win.getS());
				java.util.List listS = sXpath.selectNodes(data);
				if (searchList.hasNext()) {
					org.jdom.Element elt = (org.jdom.Element)searchList.next();
					XPath sentenceXpath = new JDOMXPath(win.getSancestor());
					org.jdom.Element sentence = (org.jdom.Element)sentenceXpath.selectSingleNode(elt);
					for (int i=0; i<listS.size() ;i++) {
						org.jdom.Element eltS = (org.jdom.Element)listS.get(i);
						if (eltS.equals(sentence)) {
							win.showSentence(i+1);
							win.hilite(options.getLevel(), elt);
							return elt;
						}
					}
					return null;
				} else {
					JOptionPane.showMessageDialog(null, "Cannot find "+new String((options.isRegex())?"regular expression":"literal string")+": "+options.getSearchString());
					return null;
				}
			} catch (Exception e) {
			    e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	private void reset () {
		nextButton.firePropertyChange("find", (find != null), false);
		condition.setBackground((options.getPredicate().equals(""))? getBackground(): Color.white);
		nextButton.setEnabled(!options.getSearchString().equals(""));
		find = null;
	}
	//fonctions pour la gestion des evenements de la fenetre
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameClosed(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {
		what.setText(options.getSearchString());
		where.setSelectedItem(options.getSearchWhere());
		condition.setText(options.getPredicate());
		regex.setSelected(options.isRegex());
		contains.setSelected(!options.isExact());
		exact.setSelected(options.isExact());
		fromBegining.setSelected(options.isFromStart());
	}
	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
};