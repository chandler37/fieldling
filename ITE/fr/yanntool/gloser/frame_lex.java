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
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.transform.*;
import org.jdom.output.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
/*-----------------------------------------------------------------------*/

/** Fenêtre pour afficher un lexique.
*/
public class frame_lex extends JInternalFrame {

		private Hashtable          lex;
		private Object             data[][];

	/** Crée une fenêtre contenant le lexique
	*
	* @param lexicon   <code>Hashtable</code> lexique (transcription, glose, occurences).
	*/
	public frame_lex(Hashtable lexicon, Hashtable lexiconCorpus, String level) {
		super(level+" lexicon", true, true, true, true);
		setFrameIcon( (Icon)UIManager.get("Tree.openIcon"));
		JMenu menuFile = new JMenu("File");
		JMenuItem save = new JMenuItem("Save as...");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveToFile();
			}
		});
		menuFile.add(save);
		JMenuBar mb = new JMenuBar();
		mb.add(menuFile);
		setJMenuBar(mb);
		setBackground(Color.white);
		setBounds( 25, 25, 600, 400);

		this.lex = new Hashtable(lexiconCorpus); //on met tout le lexique du corpus dedans

		//on regarde pour le lexique from file
		for (Enumeration enum = lexicon.keys(); enum.hasMoreElements();) {
			String form = (String)enum.nextElement();
			Hashtable h = (Hashtable)lexicon.get(form);
			if (!lex.containsKey(form)) { 			//l'entree n'existe pas encore dans lex: on copie tout
				lex.put(form, h);
			} else {							//l'entree existe
				Hashtable hLex = new Hashtable((Hashtable)lex.get(form));
				for (Enumeration enum2 = h.keys(); enum2.hasMoreElements();) {
					String gls = (String)enum2.nextElement();
					Integer num = (Integer)h.get(gls);
					if (!hLex.containsKey(gls)) {
						hLex.put(gls, num);
					} else {
						Integer num2 = (Integer)hLex.get(gls);
						int i = num.intValue() + num2.intValue();
						hLex.put(gls, new Integer(i));
					}
					lex.put(form, hLex);
				}
			}
		}

		int nbItems = 0;
		for (Enumeration enum = lex.keys(); enum.hasMoreElements();) {
			String form = (String)enum.nextElement();
			Hashtable h = (Hashtable)lex.get(form);
			nbItems += h.size();
		}
		final Object[]   columnNames = new Object[] {"transcription", "gloss", "occurrences"};
		data = new Object[nbItems][3];
		int i = 0;
		for (Enumeration enum = lex.keys(); enum.hasMoreElements();) {
			String form = (String)enum.nextElement();
			Hashtable h = (Hashtable)lex.get(form);
			for (Enumeration enum2 = h.keys(); enum2.hasMoreElements();) {
				String gls = (String)enum2.nextElement();
				Integer num = (Integer)h.get(gls);
				data[i][0] = form;
				data[i][1] = gls;
				data[i][2] = num;
				i++;
			}
		}

		//chargement du lexique dans une table
		TableModel model = new AbstractTableModel() {
			public int getColumnCount() {
				return data[0].length;
			}
			public int getRowCount() {
				return data.length;
			}
			public String getColumnName(int col) {
				return (String)columnNames[col];
			}
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
			public boolean isCellEditable(int row, int col) {
				return false;
			}
			public Class getColumnClass(int col) {
				return getValueAt(0, col).getClass();
			}
		};

		//affichage de la table
		if(nbItems!=0) {
			TableSorter sorter = new TableSorter(model);
			JTable tableau = new JTable(sorter);
			sorter.setTableHeader(tableau.getTableHeader());
			tableau.getTableHeader().setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
			JScrollPane scroller = new JScrollPane(tableau, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			setContentPane(scroller);
		} else {
			JLabel message = new JLabel("Number of items = 0");
			setContentPane(message);
		}
	}
	void saveToFile() {
		XMLOutputter outputter = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		filterExtension filter = new filterExtension(".xml", "XML files");
		chooser.setFileFilter(filter);
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File fic = chooser.getSelectedFile();
			int answer = JOptionPane.OK_OPTION;
			if (fic.exists()) {
				answer = JOptionPane.showConfirmDialog(null, fic.getAbsolutePath()+"\" already exist. Do you want to replace it?", "Replace", JOptionPane.YES_NO_OPTION);
			}
			if (answer == JOptionPane.OK_OPTION) {
				File inputFile = chooser.getSelectedFile();
				try {
					FileOutputStream fos = new FileOutputStream(inputFile);
					OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
					org.jdom.Element root = new org.jdom.Element("lexique");
					org.jdom.Document doc = new org.jdom.Document(root);
					for (int i=0; i<data.length ; i++) {
						org.jdom.Element item = new org.jdom.Element("item");
						item.setAttribute("nb", String.valueOf((Integer)data[i][2]));
						org.jdom.Element transcription = new org.jdom.Element("transcription");
						transcription.setText((String)data[i][0]);
						org.jdom.Element glose = new org.jdom.Element("glose");
						glose.setText((String)data[i][1]);
						item.addContent(transcription);
						item.addContent(glose);
						root.addContent(item);
					}
					outputter.output(doc, writer);
				} catch (Exception err) {
					System.out.println(err.getMessage());
				}
			}
		}
	}
};