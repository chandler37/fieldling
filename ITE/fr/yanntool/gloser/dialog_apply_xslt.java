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
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
/*-----------------------------------------------------------------------*/

/** Application d'une transformation XSL à un document ou à un élément XML.
*/
public class dialog_apply_xslt extends JDialog {

		private boolean           valid;
		private JCheckBox         replace, srcView, htmlView;
		private org.jdom.Element  newElt = null;
		final JTextField  filename;


	public dialog_apply_xslt(final org.jdom.Element elt) {
 		super(InterlinearTextEditor.win, "apply Stylesheet", true);
 		this.valid   = false;
		filename           = new JTextField("", 20);
		JButton choose     = new JButton("Choose...");
		choose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				filterExtension filter = new filterExtension(".xsl", "XML Stylesheet");
				chooser.setFileFilter(filter);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					filename.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		JButton okButton           = new JButton("Ok");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				valid = true;
				try {
					TransformerFactory transformerfactory = TransformerFactory.newInstance();
					Transformer transformer = transformerfactory.newTransformer(new StreamSource(getFile()));
					JDOMResult out = new JDOMResult();
					JDOMSource in  = new JDOMSource(elt);
					transformer.transform(in, out);
					if (out.getDocument() != null) {
						newElt = (org.jdom.Element)out.getDocument().getRootElement().detach();
					}
					dispose();
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, err.getMessage());
				}
			}
		});
		JButton cancelButton       = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		JPanel cmds = new JPanel();
		cmds.add(filename);
		cmds.add(choose);
		cmds.add(okButton);
		cmds.add(cancelButton);

		setBackground(Color.white);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("North", cmds);

		pack();
	}
	public dialog_apply_xslt(final org.jdom.Document doc) {
 		super(InterlinearTextEditor.win, "apply Stylesheet", true);
 		this.valid   = false;

		filename           = new JTextField("", 20);
		JButton choose     = new JButton("Choose...");
		choose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				filterExtension filter = new filterExtension(".xsl", "XML Stylesheet");
				chooser.setFileFilter(filter);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					filename.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		JButton okButton           = new JButton("Ok");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				valid = true;
				try {
					TransformerFactory transformerfactory = TransformerFactory.newInstance();
					Transformer transformer = transformerfactory.newTransformer(new StreamSource(getFile()));
					JDOMResult out = new JDOMResult();
					JDOMSource in  = new JDOMSource(doc);
					transformer.transform(in, out);
					if (out.getDocument() != null) {
						newElt = (org.jdom.Element)out.getDocument().getRootElement().detach();
					}
					dispose();
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, err.getMessage());
				}
				dispose();
			}
		});
		JButton cancelButton       = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel cmds = new JPanel();
		cmds.add(filename);
		cmds.add(choose);
		cmds.add(okButton);
		cmds.add(cancelButton);

		JPanel view = new JPanel();
		ButtonGroup group = new ButtonGroup();
		srcView = new JCheckBox("view source", true);
		htmlView = new JCheckBox("view as html (3.2)");
		group.add(srcView);
		group.add(htmlView);
		replace = new JCheckBox("Replace document");
		replace.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (isReplace()) {
					srcView.setEnabled(false);
					htmlView.setEnabled(false);
				} else {
					srcView.setEnabled(true);
					htmlView.setEnabled(true);
				}
			}
		});
		view.add(replace);
		view.add(srcView);
		view.add(htmlView);

		setBackground(Color.white);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("North", cmds);
		getContentPane().add("South", view);


		pack();
	}
	public org.jdom.Element getElt() {
		return newElt;
	}
	public String getFile() {
		return filename.getText();
	}
	public boolean isReplace() {
		return replace.isSelected();
	}
	public String getMimeType() {
		if (srcView.isSelected()) {
			return "text/plain";
		} else if (htmlView.isSelected()) {
			return "text/html";
		} else {
			return "text/plain";
		}
	}
	public boolean isValidated() {
		return valid;
	}
};