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

import java.util.*;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.transform.*;
import org.jdom.filter.ContentFilter;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.jaxen.*;
import org.jaxen.jdom.JDOMXPath;
import org.jaxen.XPathSyntaxException;
import org.jaxen.JaxenException;
import org.jaxen.function.NormalizeSpaceFunction;
import org.jaxen.function.CountFunction;
import org.saxpath.SAXPathException;

import org.yanntool.mediaplayer.*;
import fr.yanntool.gloser.edit.*;

/*-----------------------------------------------------------------------*/

/** Fenêtre d'édition du document.
*/
public class frame_editor extends JInternalFrame implements ActionListener, InternalFrameListener {

		private File                    inputFile;
		private String                  filename;
		private boolean                 newone;
		final private JMenu             fromMenu;
		final private JMenuItem         fromItem;

		private panel_level_sentence    viewS, viewW, viewM;
		private panel_level_text        viewT;
		private int                     currentS;
		private int                     maxS;
		private JTabbedPane             tabbedPane;
		private JTextField              afficheNum;
		private	org.jdom.Document       data = null;
		private boolean                 modif;
		private domOperations           domOperator;
		private Hashtable               preferences;
		private PanelPlayer             player;
		private JPanel                  cmdsPanel;

		private frame_find              findTool;
		private frame_replace           replaceTool;

		private UndoManager             undoManager;
		private UndoableEditSupport     undoSupport;
		private JMenuItem               undoMenuItem, redoMenuItem;
		private options_find            optionsFindReplace;
		private InterlinearTextEditor   ite;

	/** Crée une fenêtre contenant un onglet pour chaque niveau de présentation du texte (texte, phrase, mot et morphemes)
	* Pour les niveaux mot et morpheme la représentation intelinéaire se fait phrase par phrase
	* La fenêtre possède ses propres menus pour sauvegarder le document, afficher les préférences.
	*
	* @param inputFile   le fichier corpus (null si nouveau document).
	* @param fromMenu    le menu contenant la liste de tous les noms des fichiers en cours.
	* @param fromItem    l'option dans le menu fromMenu pour ce fichier corpus.
	* @param newone      si le fichier corpus est un nouveau document: <code>true</code> sinon <code>false</code>.
	*/
	public frame_editor(InterlinearTextEditor ITE, File inputFile, String filename, JMenu fromMenu, JMenuItem fromItem, boolean newone) {
		super(filename, true, true, true, true);
		addInternalFrameListener(this);
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		this.preferences           = dialog_options_xpath.loadValues(getClass().getResource(dialog_options_xpath.RESS_DEFAULT_PROF).toString());
		this.inputFile = inputFile;
		this.filename  = filename;
		this.fromMenu  = fromMenu;
		this.fromItem  = fromItem;
		this.newone    = newone;
		this.player    = null;
		this.currentS  = 1;
		this.maxS      = 0;
		this.ite       = ITE;
		if (newone) {
			if (setXpathPreferences()) {
				this.domOperator = new domOperations(this);
				this.data = domOperator.createDocument();
			}
		} else {
			if (setXpathPreferences()) {
				this.domOperator = new domOperations(this);
				this.data = readFile(inputFile);
			}
		}
		optionsFindReplace = new options_find();
		this.findTool    = new frame_find(this);
		this.replaceTool = new frame_replace(this);
		if (data != null) {
			loadMaxS();
			this.modif          = false;
			ite.addGloses(data.getRootElement(), getT()+"/"+getS()+"/"+getW()+"/"+getM(), getMF(), getMG(), "morpheme");
			ite.addGloses(data.getRootElement(), getT()+"/"+getS()+"/"+getW(),            getWF(), getWG(), "word");
			setFrameIcon( (Icon)UIManager.get("Tree.openIcon"));
			setBounds( 25, 25, 600, 400);


			undoManager = new UndoManager();
			undoSupport = new UndoableEditSupport();
			undoSupport.addUndoableEditListener(new UndoAdapter());

			JMenu menuFile = new JMenu("File");

			JMenuItem loadMedia = new JMenuItem("Load media file...");
			loadMedia.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					loadMedia();
				}
			});
			menuFile.add(loadMedia);

			JMenuItem save = new JMenuItem("Save");
			save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
			save.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			menuFile.add(save);
			JMenuItem saveAs = new JMenuItem("Save as...");
			saveAs.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveAs();
				}
			});
			menuFile.add(saveAs);
			JMenuItem close = new JMenuItem("Close");
			close.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					doDefaultCloseAction();
				}
			});
			menuFile.add(close);

			JMenu menuEdit = new JMenu("Edit");
			JMenuItem find = new JMenuItem("Find...");
			find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
			find.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					findShow();
				}
			});
			JMenuItem replace = new JMenuItem("Replace...");
			replace.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					replaceShow();
				}
			});
			undoMenuItem = new JMenuItem("Undo");
			undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
			undoMenuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt ) {
					undoManager.undo();
					refreshUndoRedo();
				}
			});
			redoMenuItem = new JMenuItem("Redo");
			redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
			redoMenuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt ) {
					undoManager.redo();
					refreshUndoRedo();
				}
			});
			menuEdit.add(undoMenuItem);
			menuEdit.add(redoMenuItem);
			menuEdit.add(find);
			menuEdit.add(replace);
			refreshUndoRedo();

			JMenu menuTools = new JMenu("Tools");
			JMenuItem applyStylesheet = new JMenuItem("Apply stylesheet...");
			applyStylesheet.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					applyStylesheet();
				}
			});
			menuTools.add(applyStylesheet);
			JMenuItem pref = new JMenuItem("Options...");
			pref.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					setOptions();
				}
			});
			menuTools.add(pref);

			JMenuBar mb = new JMenuBar();
			mb.add(menuFile);
			mb.add(menuEdit);
			mb.add(menuTools);
			setJMenuBar(mb);

			JPanel myPanel = new JPanel(new BorderLayout());

			this.data = data;
			viewS = new panel_level_sentence(this, "sentence");
			viewW = new panel_level_sentence(this, "word");
			viewM = new panel_level_sentence(this, "morpheme");
			viewT = new panel_level_text(this);
			ImageIcon prevImg = new ImageIcon(getClass().getResource("/icons/left.gif"));
			JButton prevButton      = new JButton(prevImg);
			prevButton.setPreferredSize(new Dimension(prevImg.getIconWidth(), prevImg.getIconHeight()));
			prevButton.setBorder(new javax.swing.border.EmptyBorder(prevButton.getBorder().getBorderInsets(prevButton)));
			prevButton.setToolTipText("previous sentence");
			prevButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ((!getActiveLevel().equals("text")) && ((currentS-1) > 0)) {
						currentS--;
						showCurrent();
					}
				}
			});
			ImageIcon nexImg = new ImageIcon(getClass().getResource("/icons/right.gif"));
			JButton nextButton      = new JButton(nexImg);
			nextButton.setPreferredSize(new Dimension(nexImg.getIconWidth(), nexImg.getIconHeight()));
			nextButton.setBorder(new javax.swing.border.EmptyBorder(nextButton.getBorder().getBorderInsets(nextButton)));
			nextButton.setToolTipText("next sentence");
			nextButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if ((!getActiveLevel().equals("text")) && (currentS < maxS)) {
						currentS++;
						showCurrent();
					}
				}
			});

			JButton autoGloseButton = new JButton("Glossing");
			afficheNum              = new JTextField(String.valueOf(currentS), 3);

			cmdsPanel     = new JPanel(new FlowLayout(FlowLayout.LEFT));
			cmdsPanel.add(prevButton);
			cmdsPanel.add(afficheNum);
			cmdsPanel.add(nextButton);
			cmdsPanel.add(autoGloseButton);

			setBackground(Color.white);

			JScrollPane scrollerT = new JScrollPane(viewT, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JScrollPane scrollerS = new JScrollPane(viewS, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JScrollPane scrollerW = new JScrollPane(viewW, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JScrollPane scrollerM = new JScrollPane(viewM, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			//viewT.setPreferredSize(new Dimension((int)(getWidth()-48), (int)(getHeight()-125)));
			viewT.setPreferredSize(new Dimension((int)(getWidth()-22), (int)(getHeight()+75)));
			viewS.setPreferredSize(new Dimension((int)(getWidth()-28), (int)(getHeight()-95)));
			viewW.setPreferredSize(new Dimension((int)(getWidth()-28), (int)(getHeight()-95)));
			viewM.setPreferredSize(new Dimension((int)(getWidth()-28), (int)(getHeight()-95)));
			//double widthScroller = scrollerS.getVerticalScrollBar().getMinimumSize().getWidth(); // = 15.0 + les bords peut etre?
			//double heigth = cmdsPanel.getMinimumSize().getHeight();// = 36.0

			afficheNum.addActionListener(this);
			autoGloseButton.addActionListener(this);


			this.tabbedPane = new JTabbedPane(SwingConstants.TOP);
			tabbedPane.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					showCurrent();
				}
			});
			tabbedPane.add("text",     scrollerT);
			tabbedPane.add("sentence", scrollerS);
			tabbedPane.add("word",     scrollerW);
			tabbedPane.add("morpheme", scrollerM);

			myPanel.add("South",  cmdsPanel);
			myPanel.add("Center", tabbedPane);

			setContentPane(myPanel);
		} else {
			dispose();
		}
	}

	public domOperations getDomOperator() {
		return domOperator;
	}
	public String getTitleName() {
		return filename;
	}
	public options_find getFindReplaceOptions() {
		return optionsFindReplace;
	}
	public org.jdom.Document getData() {
		return data;
	}
	public int getCurrentS() {
		return currentS;
	}
	private XPath getXpath(String xpath) throws org.jaxen.JaxenException{
		XPath mXpath = new JDOMXPath(xpath);
		mXpath.setFunctionContext(new myXPathFunctionContext());
		return mXpath;
	}
	public void loadMaxS() {
		try {
			XPath myXpath = getXpath(getT()+"/"+getS());
			java.util.List res = myXpath.selectNodes(data);
			maxS = res.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getMaxS() {
		return maxS;
	}
	public void incrMaxS() {
		maxS++;
	}
	public void decrMaxS() {
		maxS--;
	}
	public void findShow() {
		replaceTool.dispose();
		ite.win.getDesktop().remove(replaceTool);
		if (!ite.win.getDesktop().isAncestorOf(findTool)) ite.win.getDesktop().add(findTool);
		findTool.show();
	}
	public void replaceShow() {
		findTool.dispose();
		ite.win.getDesktop().remove(findTool);
		if (!ite.win.getDesktop().isAncestorOf(replaceTool)) ite.win.getDesktop().add(replaceTool);
		replaceTool.show();
	}
	public int getWordIndex(org.jdom.Element word) {
		panel_word compW = viewW.getWordComponent(word);
		if (compW!=null) {
			return compW.getWordIndex();
		} else {
			return -1;
		}
	}
	public int getMorphemeIndex(org.jdom.Element morpheme) {
		panel_morpheme compM = viewM.getMorphemeComponent(morpheme);
		if (compM != null) {
			return compM.getMorphemeIndex();
		} else {
			return -1;
		}
	}
	public int getWordIndexFromMorpheme(org.jdom.Element morpheme) {
		panel_word_4_morpheme compM = viewM.getWordComponentFromMorpheme(morpheme);
		if (compM != null) {
			return compM.getWordIndex();
		} else {
			return -1;
		}
	}
	public void showLevel(String mylevel) {
		tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(mylevel));
	}
	public void hilite(String mylevel, org.jdom.Element elt) {
		showLevel(mylevel);
		if (mylevel.equals("word")) {
			Component[] components = viewW.getComponents();
			for (int j=0; j<components.length; j++) {
				Component comp = components[j];
				if (comp instanceof panel_word) {
					if (((panel_word)comp).getWord().equals(elt)) {
						((panel_word)comp).hilite();
					}
				}
			}
		} else {
			Component[] components = viewM.getComponents();
			for (int j=0; j<components.length; j++) {
				Component comp = components[j];
				if (comp instanceof panel_word_4_morpheme) {
					panel_word_4_morpheme compM = (panel_word_4_morpheme)comp;
					panel_morpheme mp = compM.getMorphemePanel(elt);
					if (mp != null) {
						mp.hilite();
					}
				}
			}
		}
	}

	//---------------------------------------------------------------------
	//acces aux texte
	//---------------------------------------------------------------------
	private org.jdom.Element getText() {
		if (data != null) {
			try {
				XPath myXpath = getXpath(getT());
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(data);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	private org.jdom.Element getTextContent(boolean isTranscription) {
		if (data != null) {
			try {
				XPath myXpath = getXpath(isTranscription?getT()+"/"+getTF():getT()+"/"+getTG());
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(data);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	//---------------------------------------------------------------------
	//acces aux phrases
	//---------------------------------------------------------------------
	public org.jdom.Element getSentence(int num) {
		if (data != null) {
			try {
				XPath myXpath = getXpath(getT()+"/"+getS()+"["+num+"]");
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(data);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	private org.jdom.Element getSentenceContentAt(int numS, boolean isTranscription) {
		org.jdom.Element s = getSentence(numS);
		if (s != null) {
			try {
				XPath myXpath = getXpath(isTranscription?getSF():getSG());
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(s);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	//---------------------------------------------------------------------
	//acces aux mots
	//---------------------------------------------------------------------
	private org.jdom.Element getWordAt(int numS, int indexW) {
		if (data != null) {
			try {
				int numW = indexW +1;
				XPath myXpath = getXpath(getT()+"/"+getS()+"["+numS+"]/"+getW()+"["+numW+"]");
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(data);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	private int getWordCount(int numS) {
		if (data != null) {
			try {
				XPath myXpath = getXpath(getT()+"/"+getS()+"["+numS+"]/"+getW());
				java.util.List list = myXpath.selectNodes(data);
				return list.size();
			} catch (Exception e) {
			    e.printStackTrace();
			    return 0;
			}
		} else {
			return 0;
		}
	}
	private org.jdom.Element getWordContentAt(int numS, int indexW, boolean isTranscription) {
		org.jdom.Element w = getWordAt(numS, indexW);
		if (w != null) {
			try {
				XPath myXpath = getXpath(isTranscription?getWF():getWG());
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(w);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	//---------------------------------------------------------------------
	//acces aux morphemes
	//---------------------------------------------------------------------
	private org.jdom.Element getMorphemeAt(int numS, int indexW, int indexM) {
		if (data != null) {
			try {
				int numW = indexW +1;
				int numM = indexM +1;
				XPath myXpath = getXpath(getT()+"/"+getS()+"["+numS+"]/"+getW()+"["+numW+"]/"+getM()+"["+numM+"]");
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(data);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	private int getMorphemeCount(int numS, int indexW) {
		if (data != null) {
			try {
				int numW = indexW +1;
				XPath myXpath = getXpath(getT()+"/"+getS()+"["+numS+"]/"+getW()+"["+numW+"]/"+getM());
				java.util.List list = myXpath.selectNodes(data);
				return list.size();
			} catch (Exception e) {
			    e.printStackTrace();
			    return 0;
			}
		} else {
			return 0;
		}
	}
	private org.jdom.Element getMorphemeContentAt(int numS, int indexW, int indexM, boolean isTranscription) {
		org.jdom.Element m = getMorphemeAt(numS, indexW, indexM);
		if (m != null) {
			try {
				XPath myXpath = getXpath(isTranscription?getMF():getMG());
				org.jdom.Element res = (org.jdom.Element)myXpath.selectSingleNode(m);
				return res;
			} catch (Exception e) {
			    e.printStackTrace();
			    return null;
			}
		} else {
			return null;
		}
	}
	//---------------------------------------------------------------------
	private Component getView() {
		Component selectedComp = tabbedPane.getSelectedComponent();
		if (selectedComp instanceof JScrollPane) {
			Component comp = ((JScrollPane)selectedComp).getViewport().getView();
			return comp;
		} else {
			return null;
		}
	}
	private String getActiveLevel() {
		Component comp = getView();
		if (comp instanceof panel_level_sentence) {
			return ((panel_level_sentence)comp).getLevel();
		} else if (comp instanceof panel_level_text) {
			return "text";
		} else {
			return "";
		}
	}
	public boolean isModif() {
		return modif;
	}
	private void setModif(boolean b) {
		modif = b;
		if (b) {
			setTitle(filename+"*");
		} else {
			setTitle(filename);
		}
	}
	public void setModif() {
		setModif(true);
	}
	private void applyStylesheet() {
		final dialog_apply_xslt chooser = new dialog_apply_xslt(data);
		chooser.show();
		if (chooser.isValidated()) {
			if (chooser.isReplace()) {
				doReplaceContent(data.getRootElement(), chooser.getElt(), "text");
			} else {
				final XMLOutputter outputter = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
				String s = outputter.outputString(chooser.getElt());
				JDialog dial = new JDialog(ite.win, "result", true);

				JMenu menuFile = new JMenu("File");
				JMenuItem save = new JMenuItem("Save as...");
				save.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						JFileChooser fchooser = new JFileChooser(System.getProperty("user.dir"));
						filterExtension filter = new filterExtension(".xml", "XML files");
						fchooser.setFileFilter(filter);
						if (fchooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							File fic = fchooser.getSelectedFile();
							int answer = JOptionPane.OK_OPTION;
							if (fic.exists()) {
								answer = JOptionPane.showConfirmDialog(null, fic.getAbsolutePath()+"\" already exist. Do you want to replace it?", "Replace", JOptionPane.YES_NO_OPTION);
							}
							if (answer == JOptionPane.OK_OPTION) {
								File inputFile = fchooser.getSelectedFile();
								try {
									FileOutputStream fos = new FileOutputStream(inputFile);
									OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
									outputter.output(new org.jdom.Document(chooser.getElt()), writer);
								} catch (Exception err) {
									System.out.println(err.getMessage());
								}
							}
						}
					}
				});
				menuFile.add(save);
				JMenuBar mb = new JMenuBar();
				mb.add(menuFile);
				dial.setJMenuBar(mb);

				JEditorPane editor = new JEditorPane(chooser.getMimeType(), s);
				editor.setEditable(false);
				dial.getContentPane().add(new JScrollPane(editor));
				dial.setSize(450, 450);
				dial.show();
			}
		}
	}
	private void setOptions() {
		dialog_options_regex choose = new dialog_options_regex(preferences, this);
		choose.show();
		if (choose.isValidated()) {
			preferences = choose.getPrefs();
		}
	}
	private boolean setXpathPreferences() {
		dialog_options_xpath choose = new dialog_options_xpath(preferences, this);
		choose.show();
		if (choose.isValidated()) {
			preferences = choose.getPrefs();
			return true;
		}
		return false;
	}
	public String getT() {return getPref("T_XPATH");}
	public String getS() {return getPref("S_XPATH");}
	public String getW() {return getPref("W_XPATH");}
	public String getM() {return getPref("M_XPATH");}

	public String getTfromF() {return getPref("FORM2T_XPATH");}
	public String getSfromF() {return getPref("FORM2S_XPATH");}
	public String getWfromF() {return getPref("FORM2W_XPATH");}
	public String getMfromF() {return getPref("FORM2M_XPATH");}
	public String getTfromG() {return getPref("GLOSE2T_XPATH");}
	public String getSfromG() {return getPref("GLOSE2S_XPATH");}
	public String getWfromG() {return getPref("GLOSE2W_XPATH");}
	public String getMfromG() {return getPref("GLOSE2M_XPATH");}

	public String getTancestor() {return getPref("S2T_XPATH");}
	public String getSancestor() {return getPref("W2S_XPATH");}
	public String getWancestor() {return getPref("M2W_XPATH");}

	public String getTF() {return getPref("T_FORM_XPATH");}
	public String getSF() {return getPref("S_FORM_XPATH");}
	public String getWF() {return getPref("W_FORM_XPATH");}
	public String getMF() {return getPref("M_FORM_XPATH");}

	public String getTG() {return getPref("T_GLOSE_XPATH");}
	public String getSG() {return getPref("S_GLOSE_XPATH");}
	public String getWG() {return getPref("W_GLOSE_XPATH");}
	public String getMG() {return getPref("M_GLOSE_XPATH");}

	public String getPref(String key) {
		return (String)preferences.get(key);
	}

	private void saveAs() {
		if (newone) {
			save();
		} else {
			newone = true;
			save();
			newone = false;
		}
	}
	private void save() {
		XMLOutputter outputter = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
		if (newone) {
			JFileChooser chooser = new JFileChooser(inputFile);
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File fic = chooser.getSelectedFile();
				if (fic.exists()) {
					int answer = JOptionPane.showConfirmDialog(null, fic.getAbsolutePath()+"\" already exist. Do you want to replace it?", "Replace", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.OK_OPTION) {
						inputFile = chooser.getSelectedFile();
						filename = inputFile.getName();
						fromItem.setText(filename);
						newone = false;
						save();
					}
				} else {
					inputFile = chooser.getSelectedFile();
					filename = inputFile.getName();
					fromItem.setText(filename);
					newone = false;
					save();
				}
			}
		} else {
			try {
				FileOutputStream fos = new FileOutputStream(inputFile);
				OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
				outputter.output(data, writer);
				setModif(false);
			} catch (Exception err) {
				System.out.println(err.getMessage());
			}
		}
	}

	public void showSentence(int num) {
		currentS = num;
		showCurrent();
	}
	public void showCurrent() {
		Component comp = getView();
		if (comp instanceof panel_level_sentence) {
			panel_level_sentence myPanel = (panel_level_sentence)comp;
			myPanel.loadSentence(getSentence(currentS));
		} else if (comp instanceof panel_level_text) {
			panel_level_text myPanel = (panel_level_text)comp;
			myPanel.reloadElements();
		}
		afficheNum.setText(String.valueOf(currentS));
	}
	/** Lit un fichier input en XML.
	 */
	private org.jdom.Document readFile(File fic) {
		org.jdom.Document doc = null;
		SAXBuilder builder = new SAXBuilder(false);
		try {
			doc = builder.build(fic);
			System.out.println(filename + " is well formed.");
			System.out.println(fic.canWrite()+" "+fic.canRead());
			DocType doctype = doc.getDocType();
		} catch (JDOMException e) {
			JOptionPane.showMessageDialog(null, filename + " is not well formed.\n"+e.getMessage());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IOException.\n"+e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception.\n"+e.getMessage());
		    e.printStackTrace();
		}
		return doc;
	}
	public int askSave() {
		if (modif) {
			int answer = JOptionPane.showConfirmDialog(null, "Save the modifications to \""+ filename +"\" file", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				save();
			}
			return answer;
		} else {
			return JOptionPane.OK_OPTION;
		}
	}
	/** Supprime toutes les gloses de morphème et de mot du lexique
	*/
	private void removeGlosesFormLexique() {
		//ne faut-il pas actualiser le lexique de la phrase avant de le supprimer?
		try {
			XPath myXpath = getXpath(getT()+"/"+getS()+"/"+getW());
			java.util.List results = myXpath.selectNodes(data);
			Iterator resultIter = results.iterator();
			while ( resultIter.hasNext() ) {
				Element w = (Element)resultIter.next();
				XPath aXpath = getXpath(getWF());
				String form = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
				aXpath = getXpath(getWG());
				String gls = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
				if (!gls.equals("")) ite.delCorpusLexique(form, gls, "word");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			XPath myXpath = getXpath(getT()+"/"+getS()+"/"+getW()+"/"+getM());
			java.util.List results = myXpath.selectNodes(data);
			Iterator resultIter = results.iterator();
			while ( resultIter.hasNext() ) {
				Element w = (Element)resultIter.next();
				XPath aXpath = getXpath(getMF());
				String form = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
				aXpath = getXpath(getMG());
				String gls = NormalizeSpaceFunction.evaluate(aXpath.stringValueOf(w), aXpath.getNavigator());
				if (!gls.equals("")) ite.delCorpusLexique(form, gls, "morpheme");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Lorsque l'élément est complexe, demande à l'utilisateur s'il est sur de vouloir l'écraser.
	*/
	static public boolean ask4replace(org.jdom.Element elt) {
		if (elt == null) {
			return true;
		} else {
			ContentFilter filtre = new ContentFilter(ContentFilter.ELEMENT|ContentFilter.COMMENT|ContentFilter.PI);
			if (elt.getContent(filtre).size() != 0) {
				int answer = JOptionPane.showConfirmDialog(null, "This element contains sub-elements. Do you really want to replace it?", "Alert", JOptionPane.YES_NO_OPTION);
				return (answer == JOptionPane.OK_OPTION);
			} else {
				return true;
			}
		}
	}


	//---------------------------------------------------------------------
	//gestion des changements dans les elements (sentence word, morpheme)
	//---------------------------------------------------------------------
	/** Remplace un elt par un autre.
	* @param oldElt       ancien Elément XML
	* @param newElt       nouvel Elément XML
	* @param sentenceNum  position de la phrase dans le texte
	* @param what         quel genre d'objet doit etre remplace
	*/
	public void replaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, int sentenceNum, String what) {
		replaceContent(oldElt, newElt, sentenceNum, what, true);
	}
	public void replaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, int sentenceNum, String what, boolean majLexicon) {
		//on supprime les anciens mots et morphemes aux lexiques
		if (majLexicon) {
			if (what.equals("morpheme")) {
				ite.delGloses(oldElt, ".",    getMF(), getMG(), "morpheme");
			} else if (what.equals("word")) {
				ite.delGloses(oldElt, ".",    getWF(), getWG(), "word");
				ite.delGloses(oldElt, getM(), getMF(), getMG(), "morpheme");
			} else if (what.equals("sentence")) {
				ite.delGloses(oldElt, getW()+"/"+getM(), getMF(), getMG(), "morpheme");
				ite.delGloses(oldElt, getW(),            getWF(), getWG(), "word");
			} else if (what.equals("text")) {
				ite.delGloses(oldElt, getT()+"/"+getS()+"/"+getW()+"/"+getM(), getMF(), getMG(), "morpheme");
				ite.delGloses(oldElt, getT()+"/"+getS()+"/"+getW(),            getWF(), getWG(), "word");
			} else if (what.equals("morpheme glose")) {
				ite.delGloses(oldElt, getMfromG(),    getMF(), getMG(), "morpheme");
			} else if (what.equals("morpheme transcription")) {
				ite.delGloses(oldElt, getMfromF(),    getMF(), getMG(), "morpheme");
			} else if (what.equals("word glose")) {
				ite.delGloses(oldElt, getWfromG(),    getWF(), getWG(), "word");
			} else if (what.equals("word transcription")) {
				ite.delGloses(oldElt, getWfromF(),    getWF(), getWG(), "word");
			}
		}

		if (oldElt.isRootElement()) {
			oldElt.getDocument().setRootElement(newElt);
		} else {
			java.util.List list = oldElt.getParent().getContent();
			int i = list.indexOf(oldElt);
			if (i != -1) {
				list.set(i, newElt);
			}
		}

		if (majLexicon) {
			//on ajoute les nouveaux mots et morphemes aux lexiques
			if (what.equals("morpheme")) {
				ite.addGloses(newElt, ".",    getMF(), getMG(), "morpheme");
			} else if (what.equals("word")) {
				ite.addGloses(newElt, ".",    getWF(), getWG(), "word");
				ite.addGloses(newElt, getM(), getMF(), getMG(), "morpheme");
			} else if (what.equals("sentence")) {
				ite.addGloses(newElt, getW()+"/"+getM(), getMF(), getMG(), "morpheme");
				ite.addGloses(newElt, getW(),            getWF(), getWG(), "word");
			} else if (what.equals("text")) {
				ite.addGloses(newElt, getT()+"/"+getS()+"/"+getW()+"/"+getM(), getMF(), getMG(), "morpheme");
				ite.addGloses(newElt, getT()+"/"+getS()+"/"+getW(),            getWF(), getWG(), "word");
				loadMaxS();
			} else if (what.equals("morpheme glose")) {
				ite.addGloses(newElt, getMfromG(),    getMF(), getMG(), "morpheme");
			} else if (what.equals("morpheme transcription")) {
				ite.addGloses(newElt, getMfromF(),    getMF(), getMG(), "morpheme");
			} else if (what.equals("word glose")) {
				ite.addGloses(newElt, getWfromG(),    getWF(), getWG(), "word");
			} else if (what.equals("word transcription")) {
				ite.addGloses(newElt, getWfromF(),    getWF(), getWG(), "word");
			}
		}
		setModif();
	}
	public void replaceAttributes(org.jdom.Element elt, Vector attributes) {
		java.util.List list = elt.getAttributes();
		while (!list.isEmpty()) {
			list.remove(0);
		}
		for (int i=0; i<attributes.size(); i++) {
			org.jdom.Attribute att = (org.jdom.Attribute)attributes.elementAt(i);
			elt.setAttribute(att.detach());
		}
	}
	//---------------------------------------------------------------------
	//gestion des changements dans les textes
	//---------------------------------------------------------------------
	public void deleteTextContent(boolean isTranscription) {
		org.jdom.Element textElt = getText();
		try {
			XPath aXpath = getXpath(isTranscription?getTF():getTG());
			org.jdom.Element oldElt = (org.jdom.Element)aXpath.selectSingleNode(textElt);
			oldElt.getParent().removeContent(oldElt);
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
	/** Modification d'une phrase.
	* @param oldT             Elément correspondant au texte
	* @param s                nouvelle valeur
	* @param isTranscription  'true' = modification de la transcription, 'false' = modification de la traduction
	*/
	private org.jdom.Element modifText(org.jdom.Element oldT, boolean isTranscription, String s) {
		org.jdom.Element newT = (org.jdom.Element)oldT.clone();
		try {
			XPath aXpath = getXpath(isTranscription?getTF():getTG());
			org.jdom.Element oldElt = (org.jdom.Element)aXpath.selectSingleNode(newT);
			if ((oldElt!=null) && (s!=null)) {
				oldElt.setText(s);
			} else {
				oldElt.getParent().removeContent(oldElt);
			}
		} catch (Exception err) {
			if (s!=null) {
				if (isTranscription) {
					org.jdom.Element elt = domOperator.createLevelTranscr("text");
					elt.setText(s);
					newT = domOperator.embedTranscr2level("text", elt, newT);
				} else {
					org.jdom.Element elt = domOperator.createLevelGlose("text");
					elt.setText(s);
					newT = domOperator.embedGlose2level("text", elt, newT);
				}
			}
		}
		return newT;
	}
	//---------------------------------------------------------------------
	//gestion des changements dans les phrases
	//---------------------------------------------------------------------
	/** Suppression d'une phrase.
	* @param position     position de 0 à maxS
	*/
	public void deleteSentenceAt(int position) {
		if (position >= 0) {
			try {
				Vector v = new Vector();
				XPath myXpath = getXpath(getT()+"/"+getS());
				java.util.List results = myXpath.selectNodes(data);
				org.jdom.Element elt = (org.jdom.Element)results.get(position);
				//on supprime les mots et morphemes des lexiques
				ite.delGloses(getSentence(currentS), getW()+"/"+getM(), getMF(), getMG(), "morpheme");
				ite.delGloses(getSentence(currentS), getW(),            getWF(), getWG(), "word");
				elt.getParent().removeContent(elt);
				decrMaxS();
				if (currentS > maxS) {currentS = maxS;}
				setModif();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/** Insertion d'une phrase a sa place dans le texte.
	* @param newS         Elément XML correspondant a une phrase
	* @param position     position de 0 à maxS
	*/
	public void insertSentenceAt(org.jdom.Element newS, int position) {
		try {
			if (position >= maxS) { //quand il n'y a aucune phrase dans le texte
				XPath myXpath = getXpath(getT());
				org.jdom.Element textElt = (org.jdom.Element)myXpath.selectSingleNode(data);
				textElt = domOperator.embedSentence2text(newS, textElt);
			} else {
				Vector v = new Vector();
				XPath myXpath = getXpath(getT()+"/"+getS());
				java.util.List results = myXpath.selectNodes(data);
				org.jdom.Element parent = ((org.jdom.Element)myXpath.selectSingleNode(data)).getParentElement();
				for (int i=0; i<results.size() ;i++) {
					org.jdom.Element elt = (org.jdom.Element)results.get(i);
					if (i == position) {
						v.addElement(newS);
					}
					v.addElement(elt.clone());
					elt.getParent().removeContent(elt);
				}
				for (int i=0; i<v.size(); i++) {
					parent.addContent((Element)v.get(i));
				}
			}
			incrMaxS();
			setModif();
			//on ajoute les nouveaux mots et morphemes aux lexiques
			ite.addGloses(newS, getW()+"/"+getM(), getMF(), getMG(), "morpheme");
			ite.addGloses(newS, getW(),            getWF(), getWG(), "word");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Modification d'une phrase.
	* @param oldS             Elément correspondant a la phrase
	* @param s                nouvelle valeur
	* @param isTranscription  'true' = modification de la transcription, 'false' = modification de la traduction
	*/
	private org.jdom.Element modifSentence(org.jdom.Element oldS, boolean isTranscription, String s) {
		org.jdom.Element newS = (org.jdom.Element)oldS.clone();
		try {
			XPath aXpath = getXpath(isTranscription?getSF():getSG());
			org.jdom.Element oldElt = (org.jdom.Element)aXpath.selectSingleNode(newS);
			if ((oldElt!=null) && (s!=null)) {
				oldElt.setText(s);
			} else {
				oldElt.getParent().removeContent(oldElt);
			}
		} catch (Exception err) {
			if (s!=null) {
				if (isTranscription) {
					org.jdom.Element elt = domOperator.createLevelTranscr("sentence");
					elt.setText(s);
					newS = domOperator.embedTranscr2level("sentence", elt, newS);
				} else {
					org.jdom.Element elt = domOperator.createLevelGlose("sentence");
					elt.setText(s);
					newS = domOperator.embedGlose2level("sentence", elt, newS);
				}
			}
		}
		return newS;
	}

	//---------------------------------------------------------------------
	//gestion des changements dans les mots
	//---------------------------------------------------------------------
	/** Insertion d'un mot a sa place dans la phrase.
	* @param newW         Elément XML correspondant a un mot
	* @param indexW       position du mot dans la phrase
	* @param sentenceNum  position de la phrase ans le mot
	*/
	public void insertWordAt(org.jdom.Element newW, int indexW, int sentenceNum) {
		try {
			Vector v = new Vector();
			XPath myXpath = getXpath(getW());
			java.util.List list = myXpath.selectNodes(getSentence(sentenceNum));
			if (!list.isEmpty()) {
				for (int pos=0; pos<list.size(); pos++) {
					Element elt = (Element)list.get(pos);
					if (pos == indexW) {
						v.addElement(newW);
					}
					v.addElement(elt.clone());
					elt.getParent().removeContent(elt);
				}
			}
			if (indexW >=list.size()) {
				v.addElement(newW);
			}
			for (int i=0; i<v.size(); i++) {
				getSentence(sentenceNum).addContent((Element)v.get(i));
			}
			//on ajoute le nouveau mot et morphemes aux lexiques
			ite.addGloses(newW, getM(), getMF(), getMG(), "morpheme");
			ite.addGloses(newW, ".",    getWF(), getWG(), "word");
			setModif();
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
	/** Suppression d'un mot.
	* @param indexW       position dans la phase
	* @param sentenceNum  numero de la phrase
	*/
	public void deleteWordAt(int indexW, int sentenceNum) {
		org.jdom.Element w = getWordAt(sentenceNum, indexW);
		w.getParent().removeContent(w);
		//on supprime le mot et morphemes des lexiques
		ite.delGloses(w, getM(), getMF(), getMG(), "morpheme");
		ite.delGloses(w, ".",    getWF(), getWG(), "word");
	}

	private org.jdom.Element modifWord(org.jdom.Element oldW, boolean isTranscription, String s) {
		org.jdom.Element newW = (org.jdom.Element)oldW.clone();
		try {
			XPath aXpath = getXpath(isTranscription?getWF():getWG());
			org.jdom.Element oldElt = (org.jdom.Element)aXpath.selectSingleNode(newW);
			if ((oldElt!=null) && (s!=null)) {
				oldElt.setText(s);
			} else {
				oldElt.getParent().removeContent(oldElt);
			}
		} catch (Exception err) {
			if (s!=null) {
				if (isTranscription) {
					org.jdom.Element elt = domOperator.createLevelTranscr("word");
					elt.setText(s);
					newW = domOperator.embedTranscr2level("word", elt, newW);
				} else {
					org.jdom.Element elt = domOperator.createLevelGlose("word");
					elt.setText(s);
					newW = domOperator.embedGlose2level("word", elt, newW);
				}
			}
		}
		return newW;
	}
	/** Jonction de deux  mots.
	* @param oldW1        Elément XML correspondant au mot 1
	* @param oldW2        Elément XML correspondant au mot 2
	* @param index1       position du mot 1 dans la phase
	* @param index2       position du mot 2 dans la phase
	* @param sentenceNum  numero de la phrase
	*/
	public void joinWordAt(org.jdom.Element oldW1, org.jdom.Element oldW2, int index1, int index2, int sentenceNum) {
		try {
			//on supprime les mot et morphemes des lexiques
			ite.delGloses(oldW1, getM(), getMF(), getMG(), "morpheme");
			ite.delGloses(oldW1, ".",    getWF(), getWG(), "word");
			ite.delGloses(oldW2, getM(), getMF(), getMG(), "morpheme");
			ite.delGloses(oldW2, ".",    getWF(), getWG(), "word");
			org.jdom.Element sentenceElt = getSentence(sentenceNum);
			Vector v = new Vector();
			XPath myXpath = getXpath(getW());
			java.util.List resuts = myXpath.selectNodes(sentenceElt);
			for (int pos=0; pos<resuts.size(); pos++) {
				Element elt = (Element)resuts.get(pos);
				if (pos==index1) {
					org.jdom.Element formW1 = null;
					org.jdom.Element formW2 = null;
					XPath aXpath = getXpath(getWF());
					try {
						formW1 = (org.jdom.Element)aXpath.selectSingleNode(oldW1);
					} catch (NoSuchElementException err) { }
					try {
						formW2 = (org.jdom.Element)aXpath.selectSingleNode(oldW2);
					} catch (NoSuchElementException err) { }
					if ((formW1 == null) && (formW2 == null)) {
						;//les deux sont null, on ne change rien
					} else if ((formW1 != null) && (formW2 != null)) {
						java.util.List list = formW2.getContent();
						for (int i=0; i<list.size(); i++) {
							Object o = list.get(i);
							if (o instanceof org.jdom.Text) {
								formW1.addContent(((org.jdom.Text)((org.jdom.Text)o).clone()));
							} else if (o instanceof org.jdom.Element) {
								formW1.addContent(((org.jdom.Element)((org.jdom.Element)o).clone()));
							} else if (o instanceof org.jdom.Comment) {
								formW1.addContent(((org.jdom.Comment)((org.jdom.Comment)o).clone()));
							} else if (o instanceof org.jdom.CDATA) {
								formW1.addContent(((org.jdom.CDATA)((org.jdom.CDATA)o).clone()));
							} else if (o instanceof org.jdom.ProcessingInstruction) {
								formW1.addContent(((org.jdom.ProcessingInstruction)((org.jdom.ProcessingInstruction)o).clone()));
							} else if (o instanceof org.jdom.EntityRef) {
								formW1.addContent(((org.jdom.EntityRef)((org.jdom.EntityRef)o).clone()));
							}
						}
					} else if (formW1 != null) {
						;//le deuxième mot est null, on ne change rien au premier
					} else { //le premier mot est null, on colle le second a la place
						oldW1 = domOperator.embedTranscr2level("word", ((org.jdom.Element)formW2.clone()), oldW1);
					}
					v.addElement(oldW1.clone());
					//on ajoute le nouveau mot et morphemes aux lexiques
					ite.addGloses(oldW1, getM(), getMF(), getMG(), "morpheme");
					ite.addGloses(oldW1, ".",    getWF(), getWG(), "word");
				} else if (pos==index2) {
				} else {
					v.addElement(elt.clone());
				}
				elt.getParent().removeContent(elt);
			}
			for (int i=0; i<v.size(); i++) {
				sentenceElt.addContent((Element)v.get(i));
			}
			setModif();
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	//---------------------------------------------------------------------
	//gestion des changements dans les morphemes
	//---------------------------------------------------------------------
	public void insertMorphemeAt(org.jdom.Element newM, int indexW, int indexM, int sentenceNum) {
		try {
			Vector v = new Vector();
			XPath myXpath = getXpath(getM());
			java.util.List list = myXpath.selectNodes(getWordAt(sentenceNum, indexW));
			if (!list.isEmpty()) {
				for (int pos=0; pos<list.size(); pos++) {
					Element elt = (Element)list.get(pos);
					if (pos == indexM) {
						v.addElement(newM);
					}
					v.addElement(elt.clone());
					elt.getParent().removeContent(elt);
				}
			}
			if (indexM >=list.size()) {
				v.addElement(newM);
			}
			for (int i=0; i<v.size(); i++) {
				getWordAt(sentenceNum, indexW).addContent((Element)v.get(i));
			}
			//on ajoute le nouveau mot et morphemes aux lexiques
			ite.addGloses(newM, ".",    getMF(), getMG(), "morpheme");
			setModif();
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}
	public void deleteMorphemeAt(int indexW, int indexM, int sentenceNum) {
		org.jdom.Element m = getMorphemeAt(sentenceNum, indexW, indexM);
		m.getParent().removeContent(m);
		//on supprime le morpheme des lexiques
		ite.delGloses(m, ".",    getMF(), getMG(), "morpheme");
	}
	private org.jdom.Element modifMorpheme(org.jdom.Element oldM, boolean isTranscription, String s) {
		org.jdom.Element newM = (org.jdom.Element)oldM.clone();
		try {
			XPath aXpath = getXpath(isTranscription?getMF():getMG());
			org.jdom.Element oldElt = (org.jdom.Element)aXpath.selectSingleNode(newM);
			if ((oldElt!=null) && (s!=null)) {
				oldElt.setText(s);
			} else {
				oldElt.getParent().removeContent(oldElt);
			}
		} catch (Exception err) {
			if (s!=null) {
				if (isTranscription) {
					org.jdom.Element elt = domOperator.createLevelTranscr("morpheme");
					elt.setText(s);
					newM = domOperator.embedTranscr2level("morpheme", elt, newM);
				} else {
					org.jdom.Element elt = domOperator.createLevelGlose("morpheme");
					elt.setText(s);
					newM = domOperator.embedGlose2level("morpheme", elt, newM);
				}
			}
		}
		return newM;
	}
	public void joinMorphemeAt(org.jdom.Element oldM1, org.jdom.Element oldM2, int indexW, int indexM1, int indexM2, int sentenceNum) {
		try {
			//on supprime les mot et morphemes des lexiques
			ite.delGloses(oldM1, ".",    getMF(), getMG(), "morpheme");
			ite.delGloses(oldM2, ".",    getMF(), getMG(), "morpheme");
			org.jdom.Element wordElt = getWordAt(sentenceNum, indexW);;
			Vector v = new Vector();
			XPath myXpath = getXpath(getM());
			java.util.List resuts = myXpath.selectNodes(wordElt);
			for (int pos=0; pos<resuts.size(); pos++) {
				Element elt = (Element)resuts.get(pos);
				if (pos==indexM1) {
					org.jdom.Element formM1 = null;
					org.jdom.Element formM2 = null;
					XPath aXpath = getXpath(getMF());
					try {
						formM1 = (org.jdom.Element)aXpath.selectSingleNode(oldM1);
					} catch (NoSuchElementException err) { }
					try {
						formM2 = (org.jdom.Element)aXpath.selectSingleNode(oldM2);
					} catch (NoSuchElementException err) { }
					if ((formM1 == null) && (formM2 == null)) {
						;//les deux sont null, on ne change rien
					} else if ((formM1 != null) && (formM2 != null)) {
						java.util.List list = formM2.getContent();
						for (int i=0; i<list.size(); i++) {
							Object o = list.get(i);
							if (o instanceof org.jdom.Text) {
								formM1.addContent(((org.jdom.Text)((org.jdom.Text)o).clone()));
							} else if (o instanceof org.jdom.Element) {
								formM1.addContent(((org.jdom.Element)((org.jdom.Element)o).clone()));
							} else if (o instanceof org.jdom.Comment) {
								formM1.addContent(((org.jdom.Comment)((org.jdom.Comment)o).clone()));
							} else if (o instanceof org.jdom.CDATA) {
								formM1.addContent(((org.jdom.CDATA)((org.jdom.CDATA)o).clone()));
							} else if (o instanceof org.jdom.ProcessingInstruction) {
								formM1.addContent(((org.jdom.ProcessingInstruction)((org.jdom.ProcessingInstruction)o).clone()));
							} else if (o instanceof org.jdom.EntityRef) {
								formM1.addContent(((org.jdom.EntityRef)((org.jdom.EntityRef)o).clone()));
							}
						}
					} else if (formM1 != null) {
						;//le deuxième mot est null, on ne change rien au premier
					} else { //le premier mot est null, on colle le second a la place
						oldM1 = domOperator.embedTranscr2level("morpheme", ((org.jdom.Element)formM2.clone()), oldM1);
					}
					v.addElement(oldM1.clone());
					//on ajoute le nouveau mot et morphemes aux lexiques
					ite.addGloses(oldM1, ".",    getMF(), getMG(), "morpheme");
				} else if (pos==indexM2) {
				} else {
					v.addElement(elt.clone());
				}
				elt.getParent().removeContent(elt);
			}
			for (int i=0; i<v.size(); i++) {
				wordElt.addContent((Element)v.get(i));
			}
			setModif();
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
	}

	//---------------------------------------------------------------------
	//gestion du player audio
	//---------------------------------------------------------------------
	private boolean isJMF() {
		try {
			return (Class.forName("javax.media.Player") != null);
		} catch (Exception eQT) {
			return false;
		}
	}
	private boolean isQT4J() {
		try {
			return (Class.forName("quicktime.QTException") != null);
		} catch (Exception eQT) {
			return false;
		}
	}
	private void openPlayer(File mediafile, String playerType) {
		PanelPlayer myplayer = null;
		if (playerType.equals("JMF")) {
			try {
				Class cls = Class.forName("org.yanntool.mediaplayer.JMFPlayer");//pour eviter l'erreur javax.media.ControllerListener du constructeur
				myplayer = (PanelPlayer)cls.newInstance();
				myplayer.setParentContainer(this);
				myplayer.loadMovie(mediafile.toURL());
			} catch (Exception eJMF) {
				System.err.println("myClassNotFoundException: "+eJMF.getMessage());
			}
		} else if (playerType.equals("QT4J")) {
			try {
				myplayer = new QT4JPlayer();
				myplayer.setParentContainer(this);
				myplayer.loadMovie(mediafile.toURL());
			} catch (Exception eQT) {
				System.err.println("ClassNotFoundException: "+eQT.getMessage());
			}
		}
		if (myplayer != null) {
			if (player != null) {
				cmdsPanel.remove(player);
			}
			this.player = myplayer;
			cmdsPanel.add(player);
		}
	}
	private void loadMedia() {
		Vector playersAvailable = new Vector();
		if (isJMF())  playersAvailable.addElement("JMF");
		if (isQT4J()) playersAvailable.addElement("QT4J");
		if (playersAvailable.isEmpty()) {
			JOptionPane.showMessageDialog(null, "You need to install \"Java Media Framework\" or \"QuickTime for Java\" to be able to load a media file.");
		} else {
			JPanel custom = new JPanel();
			final JComboBox playerChooser = new JComboBox(playersAvailable);
			final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
			final JDialog dialog = new JDialog(ite.win, "Choose a media file", true);
			fileChooser.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
						if (!e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION) && fileChooser.getSelectedFile() != null) {
							File mediafile = fileChooser.getSelectedFile();
							String playerType = playerType = (String)playerChooser.getSelectedItem();
							openPlayer(mediafile, playerType);
						}
					}
			});
			custom.add(playerChooser);
			custom.add(fileChooser);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.getContentPane().add(custom, BorderLayout.CENTER);
			dialog.pack();
			dialog.show();
		}
	}
	/** Teste si l'element contient les valeurs start et end.
	*/
	public boolean isMediaTag(String level, org.jdom.Element elt) {
		if (player != null) {
			char letterLevel = ' ';
			if (level.equals("text")) {
				letterLevel = 'T';
			} else if (level.equals("sentence")) {
				letterLevel = 'S';
			} else if (level.equals("word")) {
				letterLevel = 'W';
			} else if (level.equals("morpheme")) {
				letterLevel = 'M';
			}
			try {
				XPath startXpath = new JDOMXPath(getPref("START"+letterLevel+"_XPATH"));
				startXpath.setFunctionContext(new myXPathFunctionContext());
				String start = startXpath.stringValueOf(elt);
				XPath endXpath = new JDOMXPath(getPref("END"+letterLevel+"_XPATH"));
				endXpath.setFunctionContext(new myXPathFunctionContext());
				String end = endXpath.stringValueOf(elt);
				try {
					Float.valueOf(start);
					Float.valueOf(end);
				} catch (NumberFormatException err) {
					return false;
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
	/** Joue un segment avec le player (yanntoolPlayers).
	*/
	public void playMedia(String level, org.jdom.Element elt) {
		if (player != null) {
			char letterLevel = ' ';
			if (level.equals("text")) {
				letterLevel = 'T';
			} else if (level.equals("sentence")) {
				letterLevel = 'S';
			} else if (level.equals("word")) {
				letterLevel = 'W';
			} else if (level.equals("morpheme")) {
				letterLevel = 'M';
			}
			try {
				XPath startXpath = new JDOMXPath(getPref("START"+letterLevel+"_XPATH"));
				startXpath.setFunctionContext(new myXPathFunctionContext());
				String start = startXpath.stringValueOf(elt);
				XPath endXpath = new JDOMXPath(getPref("END"+letterLevel+"_XPATH"));
				endXpath.setFunctionContext(new myXPathFunctionContext());
				String end = endXpath.stringValueOf(elt);
				playMedia(start, end);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}
	private void playMedia(String startTime, String stopTime) {
		if (player != null) {
			try {
				Float start = Float.valueOf(startTime);
				Float end   = Float.valueOf(stopTime);
				float startVal = start.floatValue() * 1000;
				float stopVal  = end.floatValue() * 1000;
				Integer myStart = new Integer((int)startVal);
				Integer myStop = new Integer((int)stopVal);

				player.cmd_playSegment(myStart, myStop);
			} catch (Exception e) {
				System.out.println("err"+e.getMessage());
			}
		}
	}

	public void actionPerformed(ActionEvent e){
		if (e.getSource() == afficheNum) {
			try {
				Integer val = Integer.valueOf(afficheNum.getText());
				if (((!getActiveLevel().equals("text"))) && (val.intValue() > 0) && (val.intValue() <= maxS)) {
					currentS = val.intValue();
					afficheNum.setText(String.valueOf(currentS));
					Component comp = getView();
					if (comp instanceof panel_level_sentence) {
						panel_level_sentence myPanel = (panel_level_sentence)comp;
						myPanel.loadSentence(getSentence(currentS));
					}
				} else {
					afficheNum.setText(String.valueOf(currentS));
				}
			} catch (NumberFormatException err) {
				afficheNum.setText(String.valueOf(currentS));
			}
		} else if (e.getActionCommand().equals("Cancel")) {
			dispose();
		} else if (e.getActionCommand().equals("Glossing")) {
			if (getActiveLevel().equals("word")) {
				viewW.autoGlose();
			} else if (getActiveLevel().equals("morpheme")) {
				viewM.autoGlose();
			}
		} else {
			System.out.println("actionPerformed "+e.getActionCommand());
		}
	}

	//fonctions pour la gestion des evenements de la fenetre
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameClosed(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {
		if (data != null) showCurrent();
	}
	public void internalFrameActivated(InternalFrameEvent e) {
		if (player != null) {
			try{
				player.setVisible(true);
			} catch(Exception err) {
				System.out.println(err.getMessage());
			}
		}
	}
	public void internalFrameDeactivated(InternalFrameEvent e) {
		if (player != null) {
			try{
				player.setVisible(false);
			} catch(Exception err) {
				System.out.println(err.getMessage());
			}
		}
	}
	public void internalFrameClosing(InternalFrameEvent e) {
		int answer = askSave();
		if (answer == JOptionPane.OK_OPTION) {
			removeGlosesFormLexique();
			fromMenu.remove(fromItem);
			dispose();
		} else if (answer == JOptionPane.NO_OPTION) {
			removeGlosesFormLexique();
			fromMenu.remove(fromItem);
			dispose();
		} else if (answer == JOptionPane.CANCEL_OPTION) {
		}
	}

	//---------------------------------------------------------------------
	//fonctions pour la gestion des actions undoable
	//---------------------------------------------------------------------
	public void doReplaceContent(org.jdom.Element oldElt, org.jdom.Element newElt, String what) {
		UndoableEdit edit = new ReplaceEdit(frame_editor.this, oldElt, newElt, currentS, what);
		undoSupport.postEdit(edit);
	}
	public void doReplaceAttributes(org.jdom.Element oldElt, java.util.List attributes, String what) {
		UndoableEdit edit = new ReplaceAttributesEdit(frame_editor.this, oldElt, new Vector(attributes), currentS, what);
		undoSupport.postEdit(edit);
	}
	public void doModifTextContent(String s, boolean isTranscription) {
		org.jdom.Element oldT = getText();
		org.jdom.Element newT = modifText(oldT, isTranscription, s);
		UndoableEdit edit = new ReplaceEdit(frame_editor.this, oldT, newT, currentS, "text", false);
		undoSupport.postEdit(edit);
	}
	/** Insérer une nouvelle phrase a la position courante.
	*/
	public void doAddSentence() {
		org.jdom.Element newS = domOperator.createSentence();
		UndoableEdit edit = new AddSentenceEdit(frame_editor.this, newS, currentS);
		undoSupport.postEdit(edit);
	}
	/** Supprimer la phrase en cours.
	*/
	public void doDeleteSentence() {
		UndoableEdit edit = new DelSentenceEdit(frame_editor.this, getSentence(currentS), currentS-1);
		undoSupport.postEdit(edit);
	}
	/** Dupliquer la phrase en cours.
	*/
	public void doDuplicateSentence() {
		UndoableEdit edit = new AddSentenceEdit(frame_editor.this, getSentence(currentS), currentS);
		undoSupport.postEdit(edit);
	}
	/** Modifier la transcription ou la traduction de la phrase courante.
	*/
	public void doModifSentenceContent(String s, boolean isTranscription) {
		org.jdom.Element oldS = getSentence(currentS);
		org.jdom.Element newS = modifSentence(oldS, isTranscription, s);
		UndoableEdit edit = new ReplaceEdit(frame_editor.this, oldS, newS, currentS, "sentence", false);
		undoSupport.postEdit(edit);
	}
	/** Ajoute un nouveau mot à la phrase courante.
	*/
	public void doAppendWord() {
		org.jdom.Element newW   = domOperator.createWord();
		UndoableEdit edit = new AddWordEdit(frame_editor.this, newW, getWordCount(currentS), currentS);
		undoSupport.postEdit(edit);
	}
	/** Inserer un mot dans la phrase courante.
	*/
	public void doInsertWordAt(org.jdom.Element w, int index) {
		UndoableEdit edit = new AddWordEdit(frame_editor.this, w, index, currentS);
		undoSupport.postEdit(edit);
	}
	/** Supprimer le mot dans la phrase courante.
	*/
	public void doDeleteWord(int indexW) {
		UndoableEdit edit = new DelWordEdit(frame_editor.this, getWordAt(currentS, indexW), indexW, currentS);
		undoSupport.postEdit(edit);
	}
	/** Modifier la transcription ou la glose d'un mot de la phrase courante.
	*/
	public void doModifWordContent(int index, String s, boolean isTranscription, boolean update) {
		org.jdom.Element oldW = getWordAt(currentS, index);
		org.jdom.Element newW = modifWord(oldW, isTranscription, s);
		UndoableEdit edit = new ReplaceEdit(frame_editor.this, oldW, newW, currentS, "word");
		undoSupport.postEdit(edit);
	}
	/** Joindre deux mots dans la phrase courante.
	*/
	public void doJoinWord(int indexW1, int indexW2) {
		UndoableEdit edit = new JoinWordEdit(frame_editor.this, getWordAt(currentS, indexW1), getWordAt(currentS, indexW2), indexW1, indexW2, currentS);
		undoSupport.postEdit(edit);
	}
	/** Découper le mot de la phrase courante en deux.
	*/
	public void doSplitWord(int index, String form1, String form2) {
		org.jdom.Element newW2 = domOperator.createWord();
		org.jdom.Element transcrElt = domOperator.createLevelTranscr("word");
		transcrElt.setText(form2);
		newW2 = domOperator.embedTranscr2level("word", transcrElt, newW2);
		org.jdom.Element newW1 = (org.jdom.Element)(getWordAt(currentS, index).clone());
		try {
			XPath aXpath = getXpath(getWF());
			org.jdom.Element form = (org.jdom.Element)aXpath.selectSingleNode(newW1);
			if (form != null) {
				form.setText(form1);
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		UndoableEdit edit = new SplitWordEdit(frame_editor.this, getWordAt(currentS, index), newW1, newW2, index, currentS);
		undoSupport.postEdit(edit);
	}
	public void doAppendMorpheme(int wordIndex) {
		org.jdom.Element newM = domOperator.createMorpheme();
		UndoableEdit edit = new AddMorphemeEdit(frame_editor.this, newM, wordIndex, getMorphemeCount(currentS, wordIndex), currentS);
		undoSupport.postEdit(edit);
	}
	public void doDeleteMorpheme(int indexW, int indexM) {
		UndoableEdit edit = new DelMorphemeEdit(frame_editor.this, getMorphemeAt(currentS, indexW, indexM), indexW, indexM, currentS);
		undoSupport.postEdit(edit);
	}
	public void doJoinMorpheme(int indexW, int indexM1, int indexM2) {
		UndoableEdit edit = new JoinMorphemeEdit(frame_editor.this, getMorphemeAt(currentS, indexW, indexM1), getMorphemeAt(currentS, indexW, indexM2), indexW, indexM1, indexM2, currentS);
		undoSupport.postEdit(edit);
	}
	public void doModifMorphemeContent(int wordIndex, int morphemeIndex, String s, boolean isTranscription, boolean update) {
		org.jdom.Element oldM = getMorphemeAt(currentS, wordIndex, morphemeIndex);
		org.jdom.Element newM = modifMorpheme(oldM, isTranscription, s);
		UndoableEdit edit = new ReplaceEdit(frame_editor.this, oldM, newM, currentS, "morpheme");
		undoSupport.postEdit(edit);
	}
	public void doSplitMorpheme(int indexW, int indexM, String form1, String form2) {
		org.jdom.Element newM2 = domOperator.createMorpheme();
		org.jdom.Element transcrElt = domOperator.createLevelTranscr("morpheme");
		transcrElt.setText(form2);
		newM2 = domOperator.embedTranscr2level("morpheme", transcrElt, newM2);
		org.jdom.Element newM1 = (org.jdom.Element)(getMorphemeAt(currentS, indexW, indexM).clone());
		try {
			XPath aXpath = getXpath(getMF());
			org.jdom.Element form = (org.jdom.Element)aXpath.selectSingleNode(newM1);
			if (form != null) {
				form.setText(form1);
			}
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
		UndoableEdit edit = new SplitMorphemeEdit(frame_editor.this, getMorphemeAt(currentS, indexW, indexM), newM1, newM2, indexW, indexM, currentS);
		undoSupport.postEdit(edit);
	}
	private void refreshUndoRedo() {
		undoMenuItem.setText(undoManager.getUndoPresentationName());
		undoMenuItem.setEnabled(undoManager.canUndo());
		redoMenuItem.setText(undoManager.getRedoPresentationName());
		redoMenuItem.setEnabled(undoManager.canRedo());
	}
	private class UndoAdapter implements UndoableEditListener {
		public void undoableEditHappened (UndoableEditEvent evt) {
			UndoableEdit edit = evt.getEdit();
			undoManager.addEdit( edit );
			refreshUndoRedo();
		}
	}
};