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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.regex.*;
import com.ibm.icu.text.Normalizer;


/**
* IPARegex presents a demonstration window with an IPA text,
* which can be searched using Unicode-sensitive regular expressions,
* as well as using an extended regular expressions syntax that
* supports queries driven by phonological feature sets.
 * @author      Edward Garrett
 */
public class IPARegex extends JPanel {
	BufferedReader in;
	String originalText;
	JTextArea textArea = null;
	JTextField findField = null;
	JTextField replaceField = null;
	Highlighter highlighter;
	Highlighter.HighlightPainter highlightPainter;
	
	/* int flags = Pattern.CANON_EQ;
	note: according to the Java API, passing this 
	flag when compiling a Pattern should cause
	decomposed and composed forms to be treated
	as equivalents for regular expressions searches;
	however, I have found this not to be the case,
	due to documented Java bugs, and so have been
	forced to use a workaround.*/
	
    static final char FILLER = '\uE000';
    
    private static IPANetwork ipaNetwork;
        
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setSize(new Dimension(600, 400));
		f.setLocation(0,0);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new IPARegex(IPACharacterDatabase.class.getResource("IPAHindi.txt")), BorderLayout.CENTER);
		f.setTitle("IPA Regular Expressions Tester");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		f.setVisible(true);
	}
    /** Creates an IPARegex window with the specified text.
    * @param textUrl the text to open
    */
	public IPARegex(URL textUrl) {
		this();
		setText(textUrl);
	}
    /** Creates an IPARegex window with no text.
    */
	public IPARegex() {
		textArea = new JTextArea();
		textArea.setEditable(false);
		originalText = "";
		JPanel regexInput = getRegexPanel();
		setLayout(new BorderLayout());
		add(regexInput, BorderLayout.NORTH);
		add(new JScrollPane(textArea, 
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
			BorderLayout.CENTER);
		highlighter = textArea.getHighlighter();
		highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
	}
    /** Assigns an {@link IPANetwork} to this window.
    * @param ipaNet a finite-state network for recognizing IPA segments
    */
    public void setIPANetwork(IPANetwork ipaNet) {
        ipaNetwork = ipaNet;
    }
    /** Accessor for {@link IPANetwork}.
    * @return the network used by this object
    */
    public IPANetwork getIPANetwork() {
        return ipaNetwork;
    }
    /** Sets the text used by this window.
    * @param textUrl the text to open
    */
	public void setText(URL textUrl) {
		try {
			in = new BufferedReader(new InputStreamReader(textUrl.openStream(), "UTF8"));
		} catch (IOException ioe) {
			return;
		}
		StringBuffer ipaBuffer = new StringBuffer();
		try {
			String line;
			while ((line = in.readLine()) != null) {
				ipaBuffer.append(Normalizer.decompose(line, false));
				ipaBuffer.append('\n');
			}
			in.close();
		} catch (IOException ioe) {
			return;
		}
		originalText = ipaBuffer.toString();
		textArea.setText(originalText);
	}
    /** Gets the content of the text area.
    * @return the text
    */
    public String getText() {
        if (textArea == null)
            return "";
        else
            return textArea.getText();
    }
    /** Applies a regular-expressions search to the
    * text, with an optional replacement value, highlighting
    * all found text sequences.
    * <P>The regular expressions syntax used is that
    described in the
    <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">Java
    1.4 API</a>.</P>
    * @param regex the regular expression search string
    * @param replace the replacement string, or null if
    * you want to search without replacing
    */
	public void applyRegex(String regex, String replace) {
		highlighter.removeAllHighlights();
		regex = UnicodeUtils.replaceUnicodeEscapes(regex);
		Pattern pattern = Pattern.compile(Normalizer.decompose(regex, false));
		Matcher matcher = pattern.matcher(textArea.getText());
		if (replace == null) {
			while(matcher.find()) {
				try {
					highlighter.addHighlight(matcher.start(), matcher.end(), highlightPainter);
				} catch (BadLocationException ble) {
				}
			}
		} else
			textArea.setText(matcher.replaceAll(replace));
    }
    /** Applies a regular-expressions search to the
    * text, with syntax extensions for specifying
    * phonologically-based search criteria.
    * <P>The regular expressions syntax used is that
    described in the
    <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">Java
    1.4 API</a>. The standard syntax can be extended
    * in the following way:
    * <UL>
    * <LI>Phonological feature sets can be embedded in
    * a search string by enclosing a whitespace-delimited
    * list of phonological features in angled-brackets.
    * For example: &lt;feature1 feature2 feature3&gt;
    * <LI>Angled-brackets that are part of the target search
    * must be escaped, e.g.: "\&lt;" and "\&gt;".</LI>
    * </UL>
    * Examples:
    * <UL>
    * <LI>"&lt;nasal&gt;[^\p{Space}]*&lt;nasal&gt;" finds all
    * nasals not separated by whitespace.</LI>
    * <LI>"&lt;voiceless bilabial&gt;&lt;voiced bilabial&gt;"
    * finds all text sequences consisting of a voiceless bilabial
    * consonant followed by a voiced bilabial consonant.</LI>
    * </UL>
    * </P>
    * @param query the regular expression search string
    */
    public void doPhonologicalQuery(String query) {
        /* find all <phonological feature sets> in the query,
        and add to searchSet */
        Pattern pattern = Pattern.compile("<([^<>]*)>");
        Matcher matcher = pattern.matcher(query);
        Set searchSet = new HashSet();
        while (matcher.find()) {
            String[] props = matcher.group(1).split(" +");
            Set featureSet = new HashSet();
            for (int i=0; i<props.length; i++)
                featureSet.add(props[i]);
            if (!searchSet.contains(featureSet))
                searchSet.add(featureSet);
        }
        
        //replace searchSet with powerset of searchSet
        searchSet = SetUtils.powerSet(searchSet);
        
        /* create Map which assigns to each member of searchSet
        a unique PUA character to replace it in the search string */
        Iterator i1 = searchSet.iterator();
        char c = '\uE001'; //second Unicode character in Private Use Area (PUA)
        Map puaMap = new HashMap();
        while (i1.hasNext()) {
            Set s1 = (Set)i1.next();
            Iterator i2 = s1.iterator();
            Set newSet = new HashSet();
            while (i2.hasNext()) {
                Set s2 = (Set)i2.next();
                newSet.addAll(s2);
            }
            if (newSet.size() > 0 && !puaMap.containsKey(newSet))
                puaMap.put(newSet, new Character(c++));
        }
        
        //for testing
        Iterator i3 = puaMap.keySet().iterator();
        while (i3.hasNext()) {
            Object o = i3.next();
            System.out.print(o + " : ");
            Character puaChar = (Character)puaMap.get(o);
            System.out.println(Integer.toHexString((int)puaChar.charValue()));
        }
        
        /* create a new regular expression query that replaces phonological
        search criteria with PUA character disjunctions */
        matcher.reset();
        StringBuffer newRegex = new StringBuffer();
        while (matcher.find()) {
            String[] props = matcher.group(1).split(" +");
            Set featureSet = new HashSet();
            for (int i=0; i<props.length; i++)
                featureSet.add(props[i]);
            StringBuffer buff = new StringBuffer();
            c = ((Character)puaMap.get(featureSet)).charValue();
            buff.append('(');
            buff.append(c);
            buff.append(FILLER);
            buff.append('*');
            Iterator i6 = puaMap.keySet().iterator();
            while (i6.hasNext()) {
                Set s = (Set)i6.next();
                if (s.size() > featureSet.size() && s.containsAll(featureSet)) {
                    c = ((Character)puaMap.get(s)).charValue();
                    buff.append('|');
                    buff.append(c);
                    buff.append(FILLER);
                    buff.append('*');
                }
            }
            buff.append(')');
            matcher.appendReplacement(newRegex, buff.toString());
        }
        matcher.appendTail(newRegex);
        String processedQuery = newRegex.toString();
        
        //testing
        System.out.println(newRegex.toString());
        
        /* scan text and replace search-relevant characters with
        PUA replacements */
        Set allFeatures = new HashSet();
        Iterator i4 = puaMap.keySet().iterator();
        while (i4.hasNext())
            allFeatures.addAll((Set)i4.next());
        char[] text = textArea.getText().toCharArray();
        int offset = 0;
        int length = 0;
        do {
            offset = IPATokenizer.getNextTokenStart(text, offset);
            if (offset == -1)
                break;
            length = IPATokenizer.getTokenLength(text, offset);
            Set f = ipaNetwork.getFeatures(new String(text, offset, length));
            if (f != null) {
                f.retainAll(allFeatures);
                if (puaMap.containsKey(f)) {
                    text[offset] = ((Character)puaMap.get(f)).charValue();
                    for (int i=offset+1; i<offset+length; i++)
                        text[i] = FILLER;
                }
            }
            offset = offset + length;
        } while (true);
        
        /* now, with PUA-replaced regular expression and PUA-replaced
        text, search text using standard regular expressions syntax.
        highlight results. */
        Pattern findPattern = Pattern.compile(processedQuery);
        Matcher m = findPattern.matcher(new String(text));
        while (m.find()) {
            try {
                highlighter.addHighlight(m.start(), m.end(), highlightPainter);
            } catch (BadLocationException ble) {
            }
        }
    }
    /** Sets the font for the text area.
    * @param font the font
    */
	public void setFont(Font font) {
		if (font == null || textArea == null || findField == null)
			return;
		else {
			textArea.setFont(font);
			findField.setFont(font);
		}
	}
	private JPanel getRegexPanel() {
        JLabel segLabel = new JLabel("Segmental Analysis:");
        final JTextField segField = new JTextField();
        segField.setEditable(false);
        JButton prevSegButton = new JButton("<");
        prevSegButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!getText().equals("")) {
                    char[] paneText = getText().toCharArray();
                    int i = textArea.getSelectionStart(); //caret position OR beginning of selection
                    int start = IPATokenizer.getPrevTokenStart(paneText, i);
                    if (start == -1)
                        return;
                    int length = IPATokenizer.getTokenLength(paneText, start);
                    if (length == -1) 
                        return;
                    textArea.requestFocus();
                    textArea.setSelectionStart(start);
                    textArea.setSelectionEnd(start+length);
                    Set s = ipaNetwork.getFeatures(textArea.getSelectedText());
                    if (s == null)
                        segField.setText("ILLEGAL IPA");
                    else
                        segField.setText(IPANetwork.getSetAsString(s));
                    //segField.setText(ipaNetwork.getLegality(textArea.getSelectedText()));
                }
            }
        });
        JButton nextSegButton = new JButton(">");
        nextSegButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (!getText().equals("")) {
                    char[] paneText = getText().toCharArray();
                    int i = textArea.getCaretPosition();
                    int start = IPATokenizer.getNextTokenStart(paneText, i);
                    if (start == -1)
                        return;
                    int length = IPATokenizer.getTokenLength(paneText, start);
                    if (length == -1) 
                        return;
                    textArea.requestFocus();
                    textArea.setSelectionStart(start);
                    textArea.setSelectionEnd(start+length);
                    Set s = ipaNetwork.getFeatures(textArea.getSelectedText());
                    if (s == null)
                        segField.setText("ILLEGAL IPA");
                    else
                        segField.setText(IPANetwork.getSetAsString(s));
                    //segField.setText(ipaNetwork.getLegality(textArea.getSelectedText()));
                }
            }
        });
        
        JPanel segPanel = new JPanel(new BorderLayout());
        segPanel.setPreferredSize(new Dimension(200, 100));
        segPanel.add(segLabel, BorderLayout.NORTH);
        JPanel segButtonPanel = new JPanel();
        segButtonPanel.add(prevSegButton);
        segButtonPanel.add(nextSegButton);
        segPanel.add(segButtonPanel, BorderLayout.SOUTH);
        segPanel.add(new JScrollPane(segField), BorderLayout.CENTER);
		findField = new JTextField();
		replaceField = new JTextField();
        final JRadioButton regSearch = new JRadioButton("Regular expressions");
        final JRadioButton phonSearch = new JRadioButton("Phonological features");
        ButtonGroup bg = new ButtonGroup();
        bg.add(regSearch);
        bg.add(phonSearch);
        regSearch.setSelected(true);
		JButton findButton = new JButton("Find");
		findButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                highlighter.removeAllHighlights();
                if (regSearch.isSelected())
                    applyRegex(findField.getText(), null);
                else { //apply phonological search
                    doPhonologicalQuery(findField.getText());
                }
			}
		});	
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findField.setText("");
				replaceField.setText("");
				highlighter.removeAllHighlights();
			}
		});
		JButton replaceButton = new JButton("Replace");
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyRegex(findField.getText(), replaceField.getText());
			}
		});
		JButton reloadButton = new JButton("Reload");
		reloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findField.setText("");
				replaceField.setText("");
				highlighter.removeAllHighlights();
				textArea.setText(originalText);
			}
		});
		findButton.setPreferredSize(new Dimension(100,30));
		clearButton.setPreferredSize(new Dimension(100,30));
		replaceButton.setPreferredSize(new Dimension(100,30));
		reloadButton.setPreferredSize(new Dimension(100,30));
		JPanel top = new JPanel(new BorderLayout());
		top.add(findField, BorderLayout.CENTER);
		JPanel east = new JPanel(new GridLayout(0,1));
        east.add(regSearch);
        east.add(phonSearch);
        east.add(findButton);
        east.add(clearButton);
		top.add(east, BorderLayout.EAST);
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(replaceField, BorderLayout.CENTER);
		JPanel east2 = new JPanel();
		east2.add(replaceButton);
		east2.add(reloadButton);
		bottom.add(east2, BorderLayout.EAST);
        JPanel pLeft = new JPanel(new GridLayout(0,1));
        pLeft.add(top);
        pLeft.add(bottom);
        JPanel p = new JPanel(new BorderLayout());
        p.add(top, BorderLayout.CENTER);
        //p.add(pLeft, BorderLayout.CENTER);
        p.add(segPanel, BorderLayout.EAST);
		//JPanel p = new JPanel(new GridLayout(0,1));
		//p.add(top);
		//p.add(bottom);
		return p;
	}
}
