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

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/** Constructs a database of critical information on
 * all IPA characters in Unicode.
 * <P>This database is drawn from data files from two
 * sources:
 * <OL>
 * <LI><a href="http://www.unicode.org/ucd/">The Unicode Character Database</a></LI>
 * <LI><a href="http://www.phon.ucl.ac.uk/home/wells/ipa-unicode.htm">The International Phonetic
 * Alphabet in Unicode</a>, by John Wells</LI>
 * </OL>
 * Note that this database includes data on both precomposed characters as well as
 * decomposed character sequences.
 */
public class IPACharacterDatabase extends JPanel {
	Map decomposedIPAtoPrecomposed, precomposedIPAtoData;
	String[][] IPACharacterData;
	private JTable table = null, table2 = null;
    
    static final int CODE_POINT = 0;
    static final int CHARACTER = 1;
    static final int IPA_NAME = 2;
    static final int UNICODE_NAME = 3;
    static final int UNICODE_BLOCK = 4;
    static final int UNICODE_CATEGORY = 5;
    static final int COMBINING_CLASS = 6;
    
	static final String[] COLUMN_NAMES = {
			"Code",
			"Character",
			"IPA Name",  
			"Unicode Name", 
			"Block",
			"Category", 
			"Combining Class", 
			"Bidirectional Class", 
			"Decomposition", 
			"Decimal", 
			"Digit", 
			"Numeric", 
			"Mirrored", 
			"v1.0 Name",
			"ISO 10646", 
			"Upper Case", 
			"Lower Case", 
			"Title Case"};
	static final String[] COLUMN_NAMES_2 = {"Code", "Character", "Name", "Block", "Decomposition"};
	
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("init")) createDatabase();
			return;
		}
		JFrame f = new JFrame();
		f.setSize(new Dimension(800,600));
		f.setLocation(0,0);
		f.setTitle("IPA Character Database");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		f.getContentPane().add(new IPACharacterDatabase());
		f.setVisible(true);
	}
    /** Creates the database.
    */
	public IPACharacterDatabase() {
		loadDatabase();
	}
    /** Gets the default, table-based GUI for 
    * displaying the database.
    * @return a table-based view of the IPA character data
    */
    public JPanel getGUI() {
        table = new JTable();
		table2 = new JTable();
		createTable(IPACharacterData);
		System.out.println("arranging layout");
        JPanel gui = new JPanel();
		gui.setLayout(new GridLayout(0,1));
		gui.add(new JScrollPane(table));
		gui.add(new JScrollPane(table2));
		gui.invalidate();
		gui.validate();
		gui.repaint();
        return gui;
    }
	private void loadDatabase() {
		BufferedReader in;
		List lines;
		Iterator iter;
		String line;
		
		try {
			in = new BufferedReader(new InputStreamReader(
				IPACharacterDatabase.class.getResource("IPAdecomposed.txt").openStream()));

			decomposedIPAtoPrecomposed = new HashMap();

			lines = new LinkedList();
			while ((line = in.readLine()) != null)
				lines.add(line);
				
			IPACharacterData = new String[lines.size()][18];
			iter = lines.iterator();
			int k = 0;
			while (iter.hasNext()) {
				String nextLine = (String)iter.next();
				String forTable = nextLine.substring(0, nextLine.lastIndexOf(";"));
				String[] stuff = forTable.split(";");
				IPACharacterData[k][0] = stuff[0];
				IPACharacterData[k][1] = UnicodeUtils.getStringForCodePoint(stuff[0]);
				System.arraycopy(stuff, 1, IPACharacterData[k], 2, stuff.length-1);

				for (int m=stuff.length; m<18; m++)
					IPACharacterData[k][m] = "";

				String precompInfo = nextLine.substring(nextLine.lastIndexOf(";")+1);
				if (precompInfo.length() != 0)
					decomposedIPAtoPrecomposed.put(IPACharacterData[k][0], precompInfo);
				
				k+=1;
			}

			in = new BufferedReader(new InputStreamReader(
				IPACharacterDatabase.class.getResource("IPAprecomposed.txt").openStream()));
			precomposedIPAtoData = new HashMap();
			while ((line = in.readLine()) != null) 
				precomposedIPAtoData.put(line.substring(0, line.indexOf(";")), line.split(";"));
		} catch (MalformedURLException murle) {
		} catch (IOException ioe) {
		}
	}
	private static void createDatabase() {
			String[][] decomposedIPAData;
			String[][] precomposedIPAData;
			String line;
			BufferedReader in;
			Map decomposedIPAtoPrecomposed = new TreeMap();
		try {	
				in = new BufferedReader(new InputStreamReader(
					IPACharacterDatabase.class.getResource("Chars-IPA.txt").openStream()));
			
			//created sorted Map of IPA Characters with IPA Names as key values
			Map ipaMap = new TreeMap();
				while ((line = in.readLine()) != null) {
					String[] data = line.split(";");
					ipaMap.put(data[0], data[1]);
				}
			
			decomposedIPAData = new String[ipaMap.keySet().size()][18];
			Iterator iter = ipaMap.keySet().iterator();
			for (int i=0; i<ipaMap.keySet().size(); i++) {
				String nextIPAChar = (String)iter.next();
				decomposedIPAData[i][0] = nextIPAChar;
				decomposedIPAData[i][1] = (String)ipaMap.get(nextIPAChar);
			}
			Set ipaChars = new HashSet();
			for (int i=0; i<decomposedIPAData.length; i++) 
				ipaChars.add(decomposedIPAData[i][0]);

				in = new BufferedReader(new InputStreamReader(
					IPACharacterDatabase.class.getResource("UnicodeData.txt").openStream()));
			
			List precomposedIPA = new LinkedList();
			List ipaUnicodeDataList = new LinkedList();

				while ((line = in.readLine()) != null) {
					String[] data = line.split(";");
					if (ipaChars.contains(line.substring(0, line.indexOf(";"))))
						ipaUnicodeDataList.add(data);
					else {
						boolean flag = false;
						if (!data[5].equals("")) {
							StringTokenizer tok = new StringTokenizer(data[5]);
							flag = true;
							while (tok.hasMoreTokens()) {
								if (!ipaChars.contains(tok.nextToken())) {
									flag = false;
									break;
								}
							}
						}
						if (flag) {
							precomposedIPA.add(data);
							String[] decomp = data[5].split(" ");
							for (int i=0; i<decomp.length; i++) {
								if (decomposedIPAtoPrecomposed.containsKey(decomp[i])) {
									String val = (String)decomposedIPAtoPrecomposed.get(decomp[i]);
									val += " ";
									val += data[0];
									decomposedIPAtoPrecomposed.put(decomp[i], val);
								} else
									decomposedIPAtoPrecomposed.put(decomp[i], data[0]);
							}
						}
					}
				}
			
			iter = ipaUnicodeDataList.iterator();
			for (int i=0; i<decomposedIPAData.length; i++) {
				String[] next = (String[])iter.next();
				decomposedIPAData[i][2] = next[1]; //Unicode Name
				System.arraycopy(next, 2, decomposedIPAData[i], 4, next.length-2);
				for (int k=next.length; k<15; k++)
					decomposedIPAData[i][k+2] = new String("");
				if (decomposedIPAtoPrecomposed.containsKey(decomposedIPAData[i][0]))
					decomposedIPAData[i][17] = (String)decomposedIPAtoPrecomposed.get(decomposedIPAData[i][0]);
				else
					decomposedIPAData[i][17] = new String("");
			}
			
			precomposedIPAData = new String[precomposedIPA.size()][16];
			iter = precomposedIPA.iterator();
			for (int i=0; i<precomposedIPAData.length; i++) {
				String[] next = (String[])iter.next();
				precomposedIPAData[i][0] = next[0]; //Unicode code point
				precomposedIPAData[i][1] = next[1]; //Unicode name
				System.arraycopy(next, 2, precomposedIPAData[i], 3, next.length-2);
				for (int k=next.length; k<16; k++)
					precomposedIPAData[i][k] = new String("");
			}

			in = new BufferedReader(new InputStreamReader(
				IPACharacterDatabase.class.getResource("Blocks.txt").openStream()));
			do {
				line = in.readLine();
			} while (line.startsWith("#"));

			int kD = 0;
			int kP = 0;
			do {
				String blockName = line.substring(line.indexOf(";")+2);
				String codeRange = line.substring(0, line.indexOf(";"));
				int periodIndex = codeRange.indexOf(".");
				String startCode = codeRange.substring(0, periodIndex);
				String endCode = codeRange.substring(periodIndex+2);
				for (; kD < decomposedIPAData.length && 
					decomposedIPAData[kD][0].compareTo(startCode) > -1 &&
					decomposedIPAData[kD][0].compareTo(endCode) < 1 ; 
					kD++)
						decomposedIPAData[kD][3] = blockName;
				for (; kP < precomposedIPAData.length &&
					precomposedIPAData[kP][0].compareTo(startCode) > -1 &&
					precomposedIPAData[kP][0].compareTo(endCode) < 1; 
					kP++)
						precomposedIPAData[kP][2] = blockName;
			} while ((line = in.readLine()) != null);
			
			String temp = IPACharacterDatabase.class.getResource("Chars-IPA.txt").getFile();
			String outputDir = temp.substring(0, temp.lastIndexOf("/")+1);
			
			PrintWriter pw;
			pw = new PrintWriter(new FileWriter(outputDir+"IPAdecomposed.txt"));
			for (int i=0; i<decomposedIPAData.length; i++) {
				StringBuffer sb = new StringBuffer();
				for (int j=0; j<17; j++) {
					sb.append(decomposedIPAData[i][j]);
					sb.append(';');
				}
				sb.append(decomposedIPAData[i][17]);
				pw.println(sb.toString());
			}
			pw.close();

			pw = new PrintWriter(new FileWriter(outputDir+"IPAprecomposed.txt"));
			for (int i=0; i<precomposedIPAData.length; i++) {
				StringBuffer sb = new StringBuffer();
				for (int j=0; j<15; j++) {
					sb.append(precomposedIPAData[i][j]);
					sb.append(';');
				}
				sb.append(precomposedIPAData[i][15]);
				pw.println(sb.toString());
			}
			pw.close();
		} catch (MalformedURLException murle) {
			murle.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	private void createTable(final String[][] IPACharacterData) {
		table.setModel(new DefaultTableModel(IPACharacterData, COLUMN_NAMES));
		
		//on single-select, bring up table of precomposed characters
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages.
				if (e.getValueIsAdjusting()) return;
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (lsm.isSelectionEmpty()) {
					//no rows are selected
				} else { 
					//selectedRow is selected: show table of precomposed IPA characters
					updateTable2(IPACharacterData[lsm.getMinSelectionIndex()][0]);
				}
			}
		});
	}
	private void updateTable2(String codePoint) {
		if (decomposedIPAtoPrecomposed.containsKey(codePoint)) {
			//there are precomposed characters that utilize this code point
			String precomposedList = (String)decomposedIPAtoPrecomposed.get(codePoint);
			StringTokenizer tok = new StringTokenizer(precomposedList);
			String[][] precomposedIPACharacterData = new String[tok.countTokens()][5];
			int j = 0;
			while (tok.hasMoreTokens()) {
				String s = tok.nextToken();
				String[] codeData = (String[])precomposedIPAtoData.get(s);
				precomposedIPACharacterData[j][0] = codeData[0]; //Unicode code point
				precomposedIPACharacterData[j][1] = UnicodeUtils.getStringForCodePoint(codeData[0]); //Unicode character
				precomposedIPACharacterData[j][2] = codeData[1]; //Unicode name
				precomposedIPACharacterData[j][3] = codeData[2]; //Block name
				precomposedIPACharacterData[j++][4] = codeData[6]; //Decomposition
			}
			table2.setModel(new DefaultTableModel(precomposedIPACharacterData, COLUMN_NAMES_2));
		} else {
			table2.setModel(new DefaultTableModel(new String[0][5], COLUMN_NAMES_2));
		}
	}
    /** Sets the font for the data table.
    * @param font the font
    */
	public void setFont(Font font) {
		if (font == null || table == null || table2 == null)
			return;
		else {
			table.setFont(font);
			table2.setFont(font);
		}
	}
	/*
	public String[] getTokensAsArray(String s) {
		StringTokenizer tok = new StringTokenizer(s, ";", true);
		List list = new ArrayList();
		int delimCount = 0;
		int dataCount = 0;
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			if (t.equals(";")) {
				delimCount += 1;
				if (delimCount > dataCount) {
					list.add(new String(""));
					dataCount += 1;
				}
			}
			else {
				dataCount += 1;
				list.add(t);
			}
		}
		if (dataCount == 14) 
			list.add(new String(""));
		return (String[])list.toArray(new String[0]);
	}*/	
};
