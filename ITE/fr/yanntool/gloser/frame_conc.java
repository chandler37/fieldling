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

/** Fenêtre pour afficher le résultant d'une concordance.
*/
public class frame_conc extends JInternalFrame {

		private org.jdom.Element     corporaElt;
		private options_conc         options;
		private JTable               tableau;
		private TableSorter          sorter;
		private static final String         FILE_CONCXSL = new String("conc.xsl");
		private static final String         RESS_CONCXSL = new String("/styles/conc.xsl");

	/** Crée une fenêtre contenant une concordance
	*
	* @param corporaElt  élément xml simplifié et normalisé contenant juste la liste des phrases, mots, morphèmes avec pour le niveau concerné la transcription et la glose.
	* @param options     les options pour faire la concordance (le pattern recherché, le niveau, la portée, le nombre de mots pour les contextes).
	*/
	public frame_conc(org.jdom.Element corporaElt, options_conc theOptions) {
		super("Concordances", true, true, true, true);
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
		this.corporaElt = corporaElt;
		this.options    = theOptions;

		org.jdom.Element root = doConcordances();

		//chargement de la concordance dans une table
		if (root != null) {
			java.util.List list = root.getChild("resultats").getChildren("item");
			final Object[]   columnNames = new Object[] {"id", "left context", "item", "right context"};
			final Object data[][]        = new Object[list.size()][4];
			for (int i=0; i<list.size(); i++) {
				org.jdom.Element item = (org.jdom.Element)list.get(i);
				data[i][0] = item.getAttributeValue("id");
				data[i][1] = item.getAttributeValue("leftContext");
				data[i][2] = item.getAttributeValue("item");
				data[i][3] = item.getAttributeValue("rightContext");
			}

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
					if (col == 1) { //la deuxième colonne doit être alignée à droite
						return new myString((String)data[row][col]);
					} else {
						return data[row][col];
					}
				}
				public boolean isCellEditable(int row, int col) {
					return false;
				}
				public Class getColumnClass(int col) {
					return getValueAt(0, col).getClass();
				}
			};

			//affichage de la table
			if(list.size()!=0) {
				sorter = new TableSorter(model);
				tableau = new JTable(sorter);
				tableau.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							String winPos = (String)sorter.getValueAt(tableau.getSelectedRow(), 0);
							String[] all = winPos.split(":");
							int pos = Integer.valueOf((String)all[1]).intValue();
							int win = Integer.valueOf((String)all[2]).intValue();
							Vector editors = InterlinearTextEditor.win.getCorpora();
							for (int i=0; i<editors.size(); i++) {
								frame_editor editor = (frame_editor)editors.elementAt(i);
								if (editor.hashCode() == win) {
									editor.showSentence(pos);
									editor.showLevel(options.getLevel());
									editor.toFront();
									try {editor.setSelected(true);} catch (Exception err){}
								}
							}
						}
					}
				});
				sorter.setTableHeader(tableau.getTableHeader());
				tableau.getTableHeader().setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");
				tableau.setDefaultRenderer(myString.class, new myAfficheur());
				JScrollPane scroller = new JScrollPane(tableau, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				setContentPane(scroller);
			} else {
				JLabel message = new JLabel("Number of items = 0");
				setContentPane(message);
			}
		}
	}
    public static Boolean matches(String str, String match) {
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(match);
		java.util.regex.Matcher m = p.matcher(str);
        return ( m.find()
                 ? Boolean.TRUE
                 : Boolean.FALSE
                 );
    }
	/** Calcule les concordances par application d'une feuille de styles xslt.
	*/
	private org.jdom.Element doConcordances() {
		String leftContext;
		String rightContext;

		String concXsl = getClass().getResource(RESS_CONCXSL).toString();
		File conFile = new File(FILE_CONCXSL);
		if (conFile.exists()) {
			concXsl = FILE_CONCXSL;
		}
		leftContext  = "<xsl:attribute name='leftContext'><xsl:call-template name=\"leftContext\"><xsl:with-param name=\"scope\">"+options.getScope()+"</xsl:with-param><xsl:with-param name=\"nbWords\" select=\""+options.getNbWord()+"\"/></xsl:call-template></xsl:attribute>";
		rightContext = "<xsl:attribute name='rightContext'><xsl:call-template name=\"rightContext\"><xsl:with-param name=\"scope\">"+options.getScope()+"</xsl:with-param><xsl:with-param name=\"nbWords\" select=\""+options.getNbWord()+"\"/></xsl:call-template></xsl:attribute>";
		String stylesheet =
		"<?xml version='1.0' encoding='iso-8859-1'?>"
		+"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:java='http://xml.apache.org/xslt/java' version='1.0'>"
		+"<xsl:import href='"+concXsl+"'/>"
		+"<xsl:output method='xml' indent='yes' encoding='utf-8'/>"
		+""
		+"<xsl:template match='/'>"
		+"	<resultats>"
		+"		<xsl:for-each select=\".//"+new String(options.getLevel().equals("word")? "W": "M")+new String(options.getPredicate().equals("")? "": "["+options.getPredicate()+"]")+"[java:fr.yanntool.gloser.frame_conc.matches(java:java.lang.String.new(@form), '"+options.getPattern()+"')]\">"
		+"			<xsl:variable name='myid'><xsl:value-of select='ancestor::S/@id'/></xsl:variable>"
		+"			<xsl:variable name='mywin'><xsl:value-of select='ancestor::S/@win'/></xsl:variable>"
		+"			<xsl:variable name='mypos'><xsl:value-of select='ancestor::S/@pos'/></xsl:variable>"
		+"			<item id='{$myid}' win='{$mywin}' pos='{$mypos}'>"
		+"				<xsl:attribute name='item'><xsl:call-template name='itemForm'/></xsl:attribute>"
		+				leftContext
		+				rightContext
		+"			</item>"
		+"		</xsl:for-each>"
		+"	</resultats>"
		+"</xsl:template>"
		+""
		+"</xsl:stylesheet>";
		org.jdom.Element res = new org.jdom.Element("res");
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Reader reader = new StringReader(stylesheet);
			org.jdom.Document xsltSource = null;
			xsltSource = builder.build(reader);
			TransformerFactory transformerfactory = TransformerFactory.newInstance();
			Transformer transformer = transformerfactory.newTransformer(new JDOMSource(xsltSource));
			JDOMResult out = new JDOMResult();
			JDOMSource in  = new JDOMSource(corporaElt);
			transformer.transform(in, out);
			org.jdom.Element elt = out.getDocument().getRootElement();
			res.addContent((org.jdom.Element)elt.clone());
		} catch (Exception err) {
			String mess = err.getLocalizedMessage().replaceAll("^.*Exception", "");
			JOptionPane.showMessageDialog(null, mess);
		}
		return res;
	}
	/** Alignement à droite du contexte gauche.
	*/
	private class myAfficheur extends JLabel implements TableCellRenderer {
		public myAfficheur() {
			this.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			setOpaque(true);
			Font font = getFont();
			setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
			setText(value.toString());
			if (isSelected) {
				this.setBackground(table.getSelectionBackground());
			} else {
				this.setBackground(table.getBackground());
			}
			return this;
		}
	}
	/** Objet spécifique pour l'affichage du contexte gauche qui doit être aligné à droite.
	*/
    private class myString extends Object {
			String s = "";
		public myString(String val) {
			this.s = val;
		}
		public String toString() {
			return s;
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
					org.jdom.Element root = new org.jdom.Element("conc");
					org.jdom.Document doc = new org.jdom.Document(root);
					for (int row=0; row<tableau.getRowCount(); row++) {
						org.jdom.Element rowElt = new org.jdom.Element("row");
						for (int col=0; col<tableau.getColumnCount(); col++) {
							org.jdom.Element colElt = new org.jdom.Element("col");
							colElt.setAttribute("name", tableau.getColumnName(col));
							colElt.setText(tableau.getValueAt(row, col).toString());
							rowElt.addContent(colElt);
						}
						root.addContent(rowElt);
					}
					outputter.output(doc, writer);
				} catch (Exception err) {
					System.out.println(err.getMessage());
				}
			}
		}
	}
};