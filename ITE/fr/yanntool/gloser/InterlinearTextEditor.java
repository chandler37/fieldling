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
import java.awt.event.*;

import javax.swing.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;

import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

/*-----------------------------------------------------------------------*/

/** Editeur de texte interlinéaire.
*/
public class InterlinearTextEditor extends JFrame implements WindowListener, DropTargetListener {

		static public InterlinearTextEditor 			      win;
		static public Hashtable                               lexiconWord;
		static public Hashtable                               lexiconMorpheme;
		static private Hashtable                              corpusLexiconWord;
		static private Hashtable                              corpusLexiconMorpheme;

		private JDesktopPane                                  desktop;
		private JMenu                                         menuWindows;
		private int                                           tempNum;

	public static void main(String [] args) {
		win = new InterlinearTextEditor(args);
		win.show();
	}

	/** Crée la fenêtre principale.
	*/
	public InterlinearTextEditor(String [] args) {
		String titre = "Interlinear Text Editor";
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.corpusLexiconWord     = new Hashtable();
		this.corpusLexiconMorpheme = new Hashtable();
		this.lexiconWord           = new Hashtable();
		this.lexiconMorpheme       = new Hashtable();
		this.menuWindows           = new JMenu();
		this.tempNum               = 0;

		DropTarget dropTarget = new DropTarget (this, this);

		String listMenu = new String("File/Tools/Windows/Help/");
		Hashtable ListMenuItems = new Hashtable();
		ListMenuItems.put("File",    "New/Open.../Load lexicon from file.../-/Quit");
		ListMenuItems.put("Windows", "");
		ListMenuItems.put("Tools",   "Concordances/Lexicon");
		ListMenuItems.put("Help",    "About...");

		setTitle(titre);
		setBackground(Color.white);
      	getContentPane().setLayout(new BorderLayout());

		JMenuBar mb = createMenubar(listMenu, ListMenuItems);
		desktop = new JDesktopPane();
		setJMenuBar(mb);
		getContentPane().add(desktop);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(dim.width-20, dim.height-60);
		dim = getSize();
		for (int i=0; i<args.length; i++) {
			addCorpusFrame(args[i]);
		}
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}
	private void quit() {
		boolean ok = true;
		Vector editors = getCorpora();
		for (int i=0; i<editors.size(); i++) {
			frame_editor editor = (frame_editor)editors.elementAt(i);
			if (editor.isModif()) {
				int answer = editor.askSave();
				if (answer == JOptionPane.CANCEL_OPTION) {
					ok = false;
				}
			}
		}
		if (ok) {
			dispose();
			System.exit(0);
		}
	}
	private JMenuBar createMenubar(String liste, Hashtable hash) {
		JMenuItem mi;
		JMenuBar mb = new JMenuBar();

		StringTokenizer menuKeys = new StringTokenizer(liste, "/");
		while (menuKeys.hasMoreTokens()) {
			String name = menuKeys.nextToken();
			JMenu m = createMenu(name, hash);
			if (name.equals("Windows")) menuWindows = m;
			if (m != null) {
				mb.add(m);
			}
		}
		return mb;
	}
	private JMenu createMenu(String key, Hashtable hash) {
		JMenu menu = new JMenu(key);
		String s = (String)hash.get(key);
		StringTokenizer menuItems = new StringTokenizer(s, "/");
		while (menuItems.hasMoreTokens()) {
			String name = menuItems.nextToken();
			if (name.equals("-")) {
				menu.addSeparator();
			} else if (name.equals("Open...")) {
				final JMenuItem mi = createMenuItem(name);
				mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						openCorpus();
					}
				});
				menu.add(mi);
			} else if (name.equals("About...")) {
				final JMenuItem mi = createMenuItem(name);
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						JDialog about = new JDialog(win, "About Interlinear Text Editor", true);
						String s = "<html><head></head><body>"
						+"<p>Interlinear Text Editor: An XML editor for glossing linguistic field data. Copyright 2003 Michel Jacobson jacobson@idf.ext.jussieu.fr"
						+"<p>This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version."
						+"<p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details."
						+"<p>You should have received a copy of the GNU General Public License along with this program; if not, write to the "
						+"<p>Free Software  Foundation,"
						+"<br>Inc., 59 Temple Place, Suite 330,"
						+"<br>Boston, MA  02111-1307"
						+"<br>USA"
						+"</body></html>";
						JEditorPane html = new JEditorPane("text/html", s);
						html.setEditable(false);
						about.getContentPane().add(new JScrollPane(html));
						about.setSize(450, 450);
						about.show();
					}
				});
				menu.add(mi);
			} else if (name.equals("New")) {
				final JMenuItem mi = createMenuItem(name);
				mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						newCorpus();
					}
				});
				menu.add(mi);
			} else if (name.equals("Load lexicon from file...")) {
				JMenu subMenu = new JMenu(name);
				final JMenuItem smiW = new JMenuItem("words lexicon");
				smiW.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						lexiconWord = openLexique();
					}
				});
				final JMenuItem smiM = new JMenuItem("morphemes lexicon");
				smiM.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						lexiconMorpheme = openLexique();
					}
				});
				subMenu.add(smiW);
				subMenu.add(smiM);
				menu.add(subMenu);
			} else if (name.equals("Concordances")) {
				final JMenuItem mi = createMenuItem(name);
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						options_conc concOpt = new options_conc();
						if (concOpt.isValidated()) {
							frame_conc conc = new frame_conc(prepareConc(concOpt.getLevel()), concOpt);
							desktop.add(conc);
							try {
								conc.setVisible(true);
								conc.setSelected(true);
							} catch (java.beans.PropertyVetoException err) {
								System.err.println(err.getMessage());
							}
						}
					}
				});
				menu.add(mi);
			} else if (name.equals("Lexicon")) {
				final JMenuItem mi = createMenuItem(name);
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						String level = (String)JOptionPane.showInputDialog(null, null, "Lexicon", JOptionPane.OK_CANCEL_OPTION, null, new String[] {"word","morpheme"}, "word");
						if (level != null) {
							frame_lex lex = new frame_lex(getLexicon(level), getCorpusLexicon(level), level);
							desktop.add(lex);
							try {
								lex.setVisible(true);
								lex.setSelected(true);
							} catch (java.beans.PropertyVetoException err) {
								System.err.println(err.getMessage());
							}
						}
					}
				});
				menu.add(mi);
			} else if (name.equals("Quit")) {
				final JMenuItem mi = createMenuItem(name);
				mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						quit();
					}
				});
				menu.add(mi);
			}
		}
		return menu;
	}
	private JMenuItem createMenuItem(String cmd) {
		JMenuItem mi = new JMenuItem(cmd);
		return mi;
	}
	public Vector getCorpora() {
		Vector response = new Vector();
		JInternalFrame[] frames = desktop.getAllFrames();
		for (int i=0; i<frames.length; i++) {
			JInternalFrame frame = frames[i];
			if (frame instanceof frame_editor) {
				response.addElement(frame);
			}
		}
		return response;
	}
	private org.jdom.Element prepareConc(String level) {
		org.jdom.Element corpus = new org.jdom.Element("corpus");
		Vector editors = getCorpora();
		for (int i=0; i<editors.size(); i++) {
			frame_editor win = (frame_editor)editors.elementAt(i);
			org.jdom.Document docCorpus = win.getData();
			try {
				XPath xpathS = new JDOMXPath(win.getT()+"/"+win.getS());
				xpathS.setFunctionContext(new myXPathFunctionContext());
				java.util.List listS = xpathS.selectNodes(docCorpus);
				for (int numS=0; numS<listS.size(); numS++) {
					org.jdom.Element sElt = (org.jdom.Element)listS.get(numS);
					int pos = numS+1;
					org.jdom.Element sentence = new org.jdom.Element("S");
					{
						java.util.List atts = sElt.getAttributes();
						for (int x=0; x<atts.size(); x++) {
							org.jdom.Attribute att = (org.jdom.Attribute)atts.get(x);
							sentence.setAttribute(att.getName(), att.getValue(), att.getNamespace());
						}
					}
					sentence.setAttribute("win", String.valueOf(win.hashCode()));
					sentence.setAttribute("pos", String.valueOf(pos));
					sentence.setAttribute("id", win.getTitle()+":"+String.valueOf(pos)+":"+win.hashCode());
					XPath xpathW = new JDOMXPath(win.getW());
					xpathW.setFunctionContext(new myXPathFunctionContext());
					java.util.List listW = xpathW.selectNodes(sElt);
					for (int numW=0; numW<listW.size(); numW++) {
						org.jdom.Element wElt = (org.jdom.Element)listW.get(numW);
						org.jdom.Element word = new org.jdom.Element("W");
						if (level.equals("word")) {
							XPath xpathWF = new JDOMXPath(win.getWF());
							xpathWF.setFunctionContext(new myXPathFunctionContext());
							XPath xpathWG = new JDOMXPath(win.getWG());
							xpathWG.setFunctionContext(new myXPathFunctionContext());
							{
								java.util.List atts = wElt.getAttributes();
								for (int x=0; x<atts.size(); x++) {
									org.jdom.Attribute att = (org.jdom.Attribute)atts.get(x);
									word.setAttribute(att.getName(), att.getValue(), att.getNamespace());
								}
							}
							word.setAttribute("form",   xpathWF.stringValueOf(wElt));
							word.setAttribute("transl", xpathWG.stringValueOf(wElt));
						} else {
							XPath xpathM = new JDOMXPath(win.getM());
							xpathM.setFunctionContext(new myXPathFunctionContext());
							java.util.List listM = xpathM.selectNodes(wElt);
							for (int numM=0; numM<listM.size(); numM++) {
								org.jdom.Element mElt = (org.jdom.Element)listM.get(numM);
								org.jdom.Element morpheme = new org.jdom.Element("M");
								XPath xpathMF = new JDOMXPath(win.getMF());
								xpathMF.setFunctionContext(new myXPathFunctionContext());
								XPath xpathMG = new JDOMXPath(win.getMG());
								xpathMG.setFunctionContext(new myXPathFunctionContext());
								{
									java.util.List atts = mElt.getAttributes();
									for (int x=0; x<atts.size(); x++) {
										org.jdom.Attribute att = (org.jdom.Attribute)atts.get(x);
										morpheme.setAttribute(att.getName(), att.getValue(), att.getNamespace());
									}
								}
								morpheme.setAttribute("form",   xpathMF.stringValueOf(mElt));
								morpheme.setAttribute("transl", xpathMG.stringValueOf(mElt));
								word.addContent(morpheme);
							}
						}
						sentence.addContent(word);
					}
					corpus.addContent(sentence);
				}
			} catch (Exception err) {
				System.out.println("prepareConc:"+err.getMessage());
			}
		}
		return corpus;
	}
	private void addCorpusFrame(String ficname) {
		try {
			File fic = new File(ficname);
			addCorpusFrame(fic, fic.getName(), false);
		} catch (NullPointerException e) {
			System.err.println(e.getMessage());
		}
	}
	private void addCorpusFrame(File fic, String ficname, boolean newone) {
		final JMenuItem mi = createMenuItem(ficname);
		final frame_editor sentence = new frame_editor(this, fic, ficname, menuWindows, mi, newone);
		if (!sentence.isClosed()) {
			desktop.add(sentence);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						sentence.setIcon(false);
						sentence.setSelected(true);
					} catch(Exception err) {
						System.out.println(err.getMessage());
					}
				}
			});
			menuWindows.add(mi);
			try {
				sentence.setVisible(true);
				sentence.setSelected(true);
			} catch (java.beans.PropertyVetoException err) {
				System.err.println(err.getMessage());
			}
		}
	}
	private void newCorpus() {
		String ficname = "Document"+tempNum;
		tempNum++;
		addCorpusFrame(null, ficname, true);
	}
	private void openCorpus() {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		filterExtension filter = new filterExtension(".xml", "XML files");
		chooser.setFileFilter(filter);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File fic = chooser.getSelectedFile();
			addCorpusFrame(fic, fic.getName(), false);
		}
	}

	private Hashtable addLexique(File fic) {
		Hashtable hGloses = new Hashtable();
		try {
			String filename = fic.getAbsolutePath();
			org.jdom.Document lexique = null;
			SAXBuilder builder = new SAXBuilder(false);
			try {
				lexique = builder.build(fic.getAbsolutePath());
				System.out.println(filename + " is well formed.");
				XPath myXpath = new JDOMXPath("//item");
				java.util.List results = myXpath.selectNodes(lexique);
				Iterator resultIter = results.iterator();
				while ( resultIter.hasNext() ) {
					org.jdom.Element item = (org.jdom.Element)resultIter.next();
					int nb = item.getAttribute("nb").getIntValue();
					XPath aXpath = new JDOMXPath("transcription");
					String form = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(item), aXpath.getNavigator());
					aXpath = new JDOMXPath("glose");
					String gls = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(item), aXpath.getNavigator());
					if (!gls.equals("")) {
						if (!hGloses.containsKey(form)) {
							Hashtable h = new Hashtable();
							h.put(gls, new Integer(nb));
							hGloses.put(form, h);
						} else {
							Hashtable h = (Hashtable)hGloses.get(form);
							if (!h.containsKey(gls)) {
								h.put(gls, new Integer(nb));
							} else {
								Integer num = (Integer)h.get(gls);
								int i = num.intValue() + nb;
								h.put(gls, new Integer(i));
							}
							hGloses.put(form, h);
						}
					}
				}
			} catch (JDOMException e) {
				JOptionPane.showMessageDialog(null, filename + " is not well formed.\n"+e.getMessage());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "IOException.\n"+e.getMessage());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Exception.\n"+e.getMessage());
			}
		} catch (Exception e) {
				System.err.println(e.getMessage());
		}
		return hGloses;
	}
	/** Construit une hashtable a partir d'un fichier lexique.
	 */
	private Hashtable openLexique() {
		Hashtable hGloses = new Hashtable();
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		filterExtension filter = new filterExtension(".xml", "XML files");
		chooser.setFileFilter(filter);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File fic = chooser.getSelectedFile();
			hGloses = addLexique(fic);
		}
		return hGloses;
	}
	static public Hashtable getLexicon(String myLevel) {
		if (myLevel.equals("word")) {
			return lexiconWord;
		} else if (myLevel.equals("morpheme")) {
			return lexiconMorpheme;
		} else {
			return null;
		}
	}
	static public Hashtable getCorpusLexicon(String myLevel) {
		if (myLevel.equals("word")) {
			return corpusLexiconWord;
		} else if (myLevel.equals("morpheme")) {
			return corpusLexiconMorpheme;
		} else {
			return null;
		}
	}
	/** Met à jour la hashtable de toutes les gloses des textes (ajoute ou augmente).
	 */
	public void addCorpusLexique(String forme, String glose, String myLevel) {
		Hashtable gloses = getCorpusLexicon(myLevel);
		if (gloses != null) {
			if (!glose.equals("")) {
				if (!gloses.containsKey(forme)) {
					Hashtable h = new Hashtable();
					h.put(glose, new Integer(1));
					gloses.put(forme, h);
				} else {
					Hashtable h = (Hashtable)gloses.get(forme);
					if (!h.containsKey(glose)) {
						h.put(glose, new Integer(1));
					} else {
						Integer num = (Integer)h.get(glose);
						int i = num.intValue() + 1;
						h.put(glose, new Integer(i));
					}
					gloses.put(forme, h);
				}
			}
		}
	}
	/** Met à jour la hashtable de toutes les gloses des textes (supprime ou diminu).
	 */
	public void delCorpusLexique(String forme, String glose, String myLevel) {
		Hashtable gloses = getCorpusLexicon(myLevel);
		if (gloses != null) {
			if (!glose.equals("")) {
				if (gloses.containsKey(forme)) {
					Hashtable h = (Hashtable)gloses.get(forme);
					if (h.containsKey(glose)) {
						Integer num = (Integer)h.get(glose);
						int i = num.intValue() - 1;
						if (i > 0) {
							h.put(glose, new Integer(i));
						} else {
							h.remove(glose);
						}
					}
					gloses.put(forme, h);
				}
			}
		}
	}
	/** Construit une hashtable de toutes les gloses du texte.
	 */
	public void addGloses(org.jdom.Element inputDoc, String path2level, String path2forms, String path2gloses, String myLevel) {
		if (inputDoc != null) {
			try {
				if ((myLevel.equals("word")) || (myLevel.equals("morpheme"))) {
					XPath myXpath = new JDOMXPath(path2level);
					myXpath.setFunctionContext(new myXPathFunctionContext());
					java.util.List results = myXpath.selectNodes(inputDoc);
					Iterator resultIter = results.iterator();
					while ( resultIter.hasNext() ) {
						org.jdom.Element w = (org.jdom.Element)resultIter.next();
						XPath aXpath = new JDOMXPath(path2forms);
						aXpath.setFunctionContext(new myXPathFunctionContext());
						String form = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
						aXpath = new JDOMXPath(path2gloses);
						aXpath.setFunctionContext(new myXPathFunctionContext());
						String gls = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
						addCorpusLexique(form, gls, myLevel);
					}
				}
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}
	public void delGloses(org.jdom.Element inputDoc, String path2level, String path2forms, String path2gloses, String myLevel) {
		if (inputDoc != null) {
			try {
				if ((myLevel.equals("word")) || (myLevel.equals("morpheme"))) {
					XPath myXpath = new JDOMXPath(path2level);
					myXpath.setFunctionContext(new myXPathFunctionContext());
					java.util.List results = myXpath.selectNodes(inputDoc);
					Iterator resultIter = results.iterator();
					while ( resultIter.hasNext() ) {
						org.jdom.Element w = (org.jdom.Element)resultIter.next();
						XPath aXpath = new JDOMXPath(path2forms);
						aXpath.setFunctionContext(new myXPathFunctionContext());
						String form = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
						aXpath = new JDOMXPath(path2gloses);
						aXpath.setFunctionContext(new myXPathFunctionContext());
						String gls = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
						delCorpusLexique(form, gls, myLevel);
					}
				}
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}

	public void windowClosing(WindowEvent e) {
		quit();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	public void dragEnter (DropTargetDragEvent dropTargetDragEvent) {}
	public void dragExit (DropTargetEvent dropTargetEvent) {}
	public void dragOver (DropTargetDragEvent dropTargetDragEvent) {}
	public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent){}
	public synchronized void drop (DropTargetDropEvent dropTargetDropEvent) {
		try {
			Transferable tr = dropTargetDropEvent.getTransferable();
			if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
				dropTargetDropEvent.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
				java.util.List fileList = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator iterator = fileList.iterator();
				while (iterator.hasNext()) {
				  File file = (File)iterator.next();
				  addCorpusFrame(file.getAbsolutePath());
				}
				dropTargetDropEvent.getDropTargetContext().dropComplete(true);
		  } else {
			System.err.println("Rejected");
			dropTargetDropEvent.rejectDrop();
		  }
		} catch (IOException e) {
			e.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		}
	}
};
