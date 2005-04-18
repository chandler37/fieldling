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
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
/*-----------------------------------------------------------------------*/

/** Fenêtre de dialogue pour changer le code d'un élément.
*/
public class dialog_source extends JDialog implements ActionListener {

		private org.jdom.Element eltIn;
		private boolean          modif;
		private JTextPane        tp;
		private org.jdom.Element eltOut;

	public dialog_source(org.jdom.Element elt, Component comp) {
		this(elt, comp, true);
	}
	public dialog_source(org.jdom.Element elt, Component comp, boolean edit) {
 		super(InterlinearTextEditor.win, true);
 		this.eltIn       = elt;
 		this.eltOut      = null;
 		this.modif       = false;

		JButton okButton           = new JButton("Ok");
		JButton cancelButton       = new JButton("Cancel");
		JPanel  cmds               = new JPanel();
		cmds.add(okButton);
		cmds.add(cancelButton);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		setBackground(Color.white);
 		getContentPane().setLayout(new BorderLayout());

 		if (eltIn != null) {
			XMLOutputter outputter = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
			String s = outputter.outputString(eltIn);
			tp = new JTextPane();
			tp.setText(s);
			JScrollPane scroller = new JScrollPane(tp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			getContentPane().add("Center", scroller);
		}
		if (edit) getContentPane().add("South", cmds);
		if (comp!=null) setLocationRelativeTo(comp);
		pack();
	}
	public boolean isModif() {
		return modif;
	}
	public org.jdom.Element getResult() {
		return (org.jdom.Element)eltOut.detach();
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Ok")) {
			SAXBuilder builder = new SAXBuilder(false);
			Reader reader = new StringReader(tp.getText());
			try {
				//parse le code source
				org.jdom.Document doc = builder.build(reader);
				eltOut = doc.getRootElement();
				modif = true;
				dispose();
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}
		} else if (e.getActionCommand().equals("Cancel")) {
			dispose();
		}
	}
};