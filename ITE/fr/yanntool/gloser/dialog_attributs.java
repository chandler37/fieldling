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

/** Fenêtre de dialogue pour changer la valeur des attributs.
*/
public class dialog_attributs extends JDialog implements ActionListener {

		private org.jdom.Element elt;
		private org.jdom.Element result;
		private Hashtable        names, attribs;
		private boolean          modif;

	public dialog_attributs(org.jdom.Element elt) {
 		super(InterlinearTextEditor.win, true);
 		this.elt         = elt;
 		this.names       = new Hashtable();
 		this.attribs     = new Hashtable();
 		this.modif       = false;
 		this.result      = new org.jdom.Element("vide");

		JButton okButton           = new JButton("Ok");
		JButton cancelButton       = new JButton("Cancel");
		JButton addButton          = new JButton("Add an attribute");
		JButton delButton          = new JButton("Delete an attribute");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		addButton.addActionListener(this);
		delButton.addActionListener(this);

		setBackground(Color.white);
 		getContentPane().setLayout(new GridLayout(0, 2));

 		if (elt != null) {
			java.util.List list = elt.getAttributes();
			for (int i=0; i<list.size() ;i++) {
				org.jdom.Attribute att = (org.jdom.Attribute)list.get(i);

				String key    = att.getName();
				String prefix  = att. getNamespacePrefix();
				JTextField textVal = new JTextField(att.getValue());
				if (!prefix.equals("")) {
					key = prefix+":"+key;
				}
				Container cont = new JLabel(key);
				cont.setName(key);
				getContentPane().add(key, cont);
				getContentPane().add(textVal);
				names.put(key, textVal);
				attribs.put(key, att);
			}
		}
		getContentPane().add(addButton);
		getContentPane().add(delButton);
		getContentPane().add(okButton);
		getContentPane().add(cancelButton);
		pack();
	}
	public boolean isModif() {
		return modif;
	}
	public java.util.List getResult() {
		return result.getAttributes();
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Ok")) {
			for (Enumeration enum = names.keys() ; enum.hasMoreElements() ;) {
				String key = (String)enum.nextElement();
				String val = ((JTextField)names.get(key)).getText();
				org.jdom.Namespace ns = org.jdom.Namespace.NO_NAMESPACE;
				int pos = key.lastIndexOf(':');
				if (pos != -1) {
					String prefix    = key.substring(0, pos);
					String reste = key.substring(pos+1);
					if (prefix.equals("xml")) {
						 ns = org.jdom.Namespace.XML_NAMESPACE;
						 key = reste;
					 } else {
						 key = reste;
					 }
				}
				result.setAttribute(key, val, ns);
			}
			modif = true;
			dispose();
		} else if (e.getActionCommand().equals("Cancel")) {
			dispose();
		} else if (e.getActionCommand().equals("Add an attribute")) {
			String key = JOptionPane.showInputDialog(null, "Enter the name attribut", "Titre", JOptionPane.QUESTION_MESSAGE);
			if ((key != null) && (!key.equals(""))) {
				if (names.containsKey(key)) {
					JOptionPane.showMessageDialog(null, key+" attribut name already exist!");
				} else {
					org.jdom.Namespace ns = org.jdom.Namespace.NO_NAMESPACE;
					int pos = key.lastIndexOf(':');
					if (pos != -1) {
						String prefix    = key.substring(0, pos);
						String reste = key.substring(pos+1);
						if (prefix.equals("xml")) {
							 ns = org.jdom.Namespace.XML_NAMESPACE;
							 key = reste;
						 } else {
							 key = reste;
						 }
					}
					if (!(org.jdom.Verifier.checkAttributeName(key) == null)) {
						JOptionPane.showMessageDialog(null, key+" is an illegal name for an attribute!");
					} else {
						String val = JOptionPane.showInputDialog(null, "Enter the value for the "+key+" attribut", "Titre", JOptionPane.QUESTION_MESSAGE);
						if ((val != null) && (!val.equals(""))) {
							if (elt != null) {
								org.jdom.Attribute att = new org.jdom.Attribute(key, val, ns);
								String prefix  = att. getNamespacePrefix();
								JTextField textVal = new JTextField(att.getValue());
								if (!prefix.equals("")) {
									key = prefix+":"+key;
								}
								names.put(key, textVal);
								attribs.put(key, att);
								Container cont = new JLabel(key);
								cont.setName(key);
								getContentPane().add(cont, 0);
								getContentPane().add(textVal, 1);
							}
						}
					}
				}
			}
			validate();
			pack();
		} else if (e.getActionCommand().equals("Delete an attribute")) {
			Object[] possibleValues = names.keySet().toArray();
			if (possibleValues.length > 0) {
				String key = (String)JOptionPane.showInputDialog(null, "Choose one attribut", "Input",
							JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
				if (key != null) {
					for (int i=0; i<getContentPane().getComponents().length; i++) {
						Component comp = getContentPane().getComponent(i);
						if ((comp.getName() != null) && (comp.getName().equals(key))) {
							getContentPane().remove(i+1);
							getContentPane().remove(i);
							names.remove(key);
							attribs.remove(key);
						}
					}
					validate();
					pack();
				}
			}
		}
	}
};