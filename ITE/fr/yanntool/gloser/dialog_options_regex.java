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

import org.jaxen.*;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.saxpath.SAXPathException;

/*-----------------------------------------------------------------------*/
public class dialog_options_regex extends JDialog {

		private Hashtable newPref;
		private boolean   valid;
		private frame_editor   win;

		final JTextField regexS, regexW, regexM;

	public dialog_options_regex(Hashtable currentPref, frame_editor win) {
 		super(InterlinearTextEditor.win, "options", true);
 		this.win     = win;
 		this.valid   = false;
 		this.newPref = new Hashtable(currentPref);

		regexS             = new JTextField((String)newPref.get("regexS"), 10);
		regexW             = new JTextField((String)newPref.get("regexW"), 10);
		regexM             = new JTextField((String)newPref.get("regexM"), 10);

		JButton okButton           = new JButton("Ok");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				newPref.put("regexS",        regexS.getText());
				newPref.put("regexW",        regexW.getText());
				newPref.put("regexM",        regexM.getText());
				valid = true;
				dispose();
			}
		});
		JButton cancelButton       = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		setBackground(Color.white);
 		getContentPane().setLayout(new GridLayout(0, 2));
		getContentPane().add(new JLabel("regex (text > sentences)"));
		getContentPane().add(regexS);
		getContentPane().add(new JLabel("regex (sentence > words)"));
		getContentPane().add(regexW);
		getContentPane().add(new JLabel("regex (word > morphemes)"));
		getContentPane().add(regexM);
		getContentPane().add(okButton);
		getContentPane().add(cancelButton);

		pack();
	}
	public Hashtable getPrefs() {
		return newPref;
	}
	public boolean isValidated() {
		return valid;
	}
};