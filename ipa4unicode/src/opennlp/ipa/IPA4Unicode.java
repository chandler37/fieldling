/*
Copyright (c) 2003, Edward Garrett

    This file is part of larkpie.

    larkpie is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    larkpie is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with larkpie; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package opennlp.ipa;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.io.*;
import weka.core.Instances;
import weka.associations.Apriori;

/** A wrapper application to demonstrate EMU's tools for 
 * working with the International Phonetic Alphabet in Unicode.
 */
public class IPA4Unicode extends JFrame {
	IPACharacterDatabase ipaDatabase;
	IPARegex ipaRegex;
	public static Font DOULOS_SIL = new Font("Doulos SIL", Font.PLAIN, 24);
	public static final String[][] TEXTS = {
                {"American English", "AmericanEnglish.txt"},
                {"Amharic", "Amharic.txt"},
		{"Arabic", "Arabic.txt"},
                {"Catalan", "Catalan.txt"},
		{"French", "French.txt"},
		{"Hindi", "Hindi.txt"},
		{"Swedish", "Swedish.txt"},
		{"Turkish", "Turkish.txt"},
                {"TEST", "TEST.txt"}
	};
	
    /** Gateway into the IPA4Unicode demonstration application.
    */
	public static void main(String[]args) {
		new IPA4Unicode();
	}
    /** Creates a window which includes tools for examining IPA
    characters and their properties, as well as searching texts based
    using phonological feature bundles.
    */
	public IPA4Unicode() {
		JPanel p = new JPanel(new GridLayout(0,1));
		ipaDatabase = new IPACharacterDatabase();
		ipaRegex = new IPARegex();
		ipaDatabase.setFont(DOULOS_SIL);
		ipaRegex.setFont(DOULOS_SIL);
		p.add(ipaRegex);
		p.add(ipaDatabase.getGUI());
		getContentPane().add(p);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		setTitle("IPA4Unicode");
		setLocation(0,0);
		setSize(new Dimension(800,600));
		setJMenuBar(getMenus());
		setVisible(true);
		ipaRegex.setIPANetwork(new IPANetwork(IPASymbolLoader.readIPASymbols()));
	}
	private JMenuBar getMenus() {
		/*final GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] fontNames = genv.getAvailableFontFamilyNames();
		final String[] fontSizes = {"8","10","12","14","16","18","20","22","24","26","28","30","32","34","36","48","72"};*/
		JMenuBar mb = new JMenuBar();
		/*JMenu viewMenu = new JMenu("View");
		JMenuItem fontItem = new JMenuItem("Fonts");
		fontItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//font face and size for the text panel
				JPanel textPanel = new JPanel();
				JComboBox textFontFamilies = new JComboBox(fontNames);
				textPanel.setBorder(BorderFactory.createTitledBorder("Set font face and size for text"));
				textFontFamilies.setMaximumSize(textFontFamilies.getPreferredSize());
				//textFontFamilies.setSelectedItem(dp.getRomanFontFamily());
				textFontFamilies.setEditable(true);
				JComboBox textFontSizes = new JComboBox(fontSizes);
				textFontSizes.setMaximumSize(textFontSizes.getPreferredSize());
				//textFontSizes.setSelectedItem(String.valueOf(dp.getRomanFontSize()));
				textFontSizes.setEditable(true);
				textPanel.setLayout(new GridLayout(1,2));
				textPanel.add(textFontFamilies);
				textPanel.add(textFontSizes);
				
				//font face and size for the table
				JPanel tablePanel = new JPanel();
				JComboBox tableFontFamilies = new JComboBox(fontNames);
				tablePanel.setBorder(BorderFactory.createTitledBorder("Set font face and size for table text"));
				tableFontFamilies.setMaximumSize(tableFontFamilies.getPreferredSize());
				//textFontFamilies.setSelectedItem(dp.getRomanFontFamily());
				tableFontFamilies.setEditable(true);
				JComboBox tableFontSizes = new JComboBox(fontSizes);
				tableFontSizes.setMaximumSize(tableFontSizes.getPreferredSize());
				//textFontSizes.setSelectedItem(String.valueOf(dp.getRomanFontSize()));
				tableFontSizes.setEditable(true);
				tablePanel.setLayout(new GridLayout(1,2));
				tablePanel.add(tableFontFamilies);
				tablePanel.add(tableFontSizes);
				JPanel preferencesPanel = new JPanel(new GridLayout(0,1));
				preferencesPanel.add(textPanel);
				preferencesPanel.add(tablePanel);
				JOptionPane pane = new JOptionPane(preferencesPanel);
				JDialog dialog = pane.createDialog(IPA4Unicode.this, "");
				// This returns only when the user has closed the dialog:
				dialog.show();
				
				//change font face and size for text
				int textSize;
				String textFont = textFontFamilies.getSelectedItem().toString();
				try {
					textSize = Integer.parseInt(textFontSizes.getSelectedItem().toString());
				}
				catch (NumberFormatException ne) {
					textSize = 12;
				}
				Font newTextFont = new Font(textFont, Font.PLAIN, textSize);
				ipaRegex.setFont(newTextFont);
				
				//change font face and size for table
				int tableSize;
				String tableFont = tableFontFamilies.getSelectedItem().toString();
				try {
					tableSize = Integer.parseInt(tableFontSizes.getSelectedItem().toString());
				}
				catch (NumberFormatException ne) {
					tableSize = 12;
				}
				Font newTableFont = new Font(tableFont, Font.PLAIN, tableSize);
				ipaDatabase.setFont(newTableFont);
			}
		});
		viewMenu.add(fontItem);*/
		JMenu openMenu = new JMenu("Open");
		for (int m=0; m<TEXTS.length; m++) {
			final int n = m;
			JMenuItem tItem = new JMenuItem(TEXTS[m][0]);
			tItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ipaRegex.setText(IPA4Unicode.class.getResource(TEXTS[n][1]));
				}
			});
			openMenu.add(tItem);
		}
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem generalizeItem = new JMenuItem("Generalize");
		toolsMenu.add(generalizeItem);
		generalizeItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			if (!ipaRegex.getText().equals("")) {
			    try {
				BufferedWriter out = new BufferedWriter(new FileWriter("temp.arff"));
				ARFF.setIPANetwork(ipaRegex.getIPANetwork());
				ARFF.writeARFF(ipaRegex.getText(), out);
				out.close();
				Instances instances = new Instances(new FileReader("temp.arff"));
				Apriori apriori = new Apriori();
				apriori.buildAssociations(instances);
				JFrame f = new JFrame("Generalizations (courtesy of Weka)");
				f.getContentPane().add(new JScrollPane(new JTextArea(apriori.toString())));
				f.setSize(600,350);
				f.setLocation(75,75);
				f.setVisible(true);
			    } catch (IOException ioe) {
				 ioe.printStackTrace();
			    } catch (Exception exc) {
				 exc.printStackTrace();
			    }
			}
		    }
		});
		mb.add(openMenu);
		//mb.add(viewMenu);
		mb.add(toolsMenu);
		return mb;
	}
}
