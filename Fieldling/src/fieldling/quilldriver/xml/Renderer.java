/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Edward Garrett
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

package fieldling.quilldriver.xml;

import org.w3c.dom.*;

import java.awt.Color;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.BadLocationException;
import fieldling.quilldriver.PreferenceManager;

public class Renderer {
	private static final float indentIncrement = 15.0F;
	private static final Color attColor = Color.pink;
	private static final Color textColor = Color.darkGray;
	private static Color tagColor;
        private static int font_size;
        private static String font_family;
	
	static
	{
		tagColor = new Color
                        (PreferenceManager.getInt(PreferenceManager.TAG_RED_KEY, PreferenceManager.TAG_RED_DEFAULT),
                        PreferenceManager.getInt(PreferenceManager.TAG_GREEN_KEY, PreferenceManager.TAG_GREEN_DEFAULT),
                        PreferenceManager.getInt(PreferenceManager.TAG_BLUE_KEY, PreferenceManager.TAG_BLUE_DEFAULT));
                font_size = PreferenceManager.getInt(PreferenceManager.FONT_SIZE_KEY, PreferenceManager.FONT_SIZE_DEFAULT);
                font_family = PreferenceManager.getValue(PreferenceManager.FONT_FACE_KEY, PreferenceManager.FONT_FACE_DEFAULT);                
	}
	
	private Renderer() {} //don't instantiate
	
	public static void setTagColor(Color c)
	{
		tagColor = c;
	}
        
        public static void setFontSize(int size) {
                font_size = size;
        }
        
        public static void setFontFamily(String family) {
                font_family = family;
        }
	
	public static int render(Node node, JTextPane pane, int offset, float indent, TagInfo tagInfo, Map startOffsets, Map endOffsets, Map indentTracker) {
		if (offset == -1) return -1;
		
		if (node instanceof DocumentFragment) {
			NodeList chillern = node.getChildNodes();
			for (int z=0; z<chillern.getLength(); z++)
				offset = render(node, pane, offset, indent, tagInfo, startOffsets, endOffsets, indentTracker);
			return offset;
		} else if (node instanceof Element)
			return renderElement((Element)node, pane, offset, indent, tagInfo, startOffsets, endOffsets, indentTracker);
		else if (node instanceof Attr)
			return renderAttribute((Attr)node, pane, offset, tagInfo, startOffsets, endOffsets);
		else if (node instanceof Text)
			return renderText((Text)node, pane, offset, tagInfo, startOffsets, endOffsets);
		else
			return -1;
	}
	public static int renderElement(Element e, JTextPane pane, int offset, float indent, TagInfo tagInfo, Map startOffsets, Map endOffsets, Map indentTracker) {
                //should really throw exception if user tries to insert at offset 0
                StyledDocument doc = pane.getStyledDocument();
                try {
                        Position pos = doc.createPosition(offset);
			SimpleAttributeSet eAttributes = new SimpleAttributeSet();
			StyleConstants.setLeftIndent(eAttributes, indent);
                        indentTracker.put(e, new Float(indent));
			SimpleAttributeSet eColor = new SimpleAttributeSet();
			StyleConstants.setForeground(eColor, tagColor);
			StyleConstants.setFontSize(eColor, font_size);
			StyleConstants.setFontFamily(eColor, font_family);
			eColor.addAttribute("xmlnode", e);
			@TIBETAN@SimpleAttributeSet tibAtt = new SimpleAttributeSet();
			@TIBETAN@StyleConstants.setForeground(tibAtt, tagColor);
			@TIBETAN@tibAtt.addAttribute("xmlnode", e);
			if (pos.getOffset()>0) {
				String s = doc.getText(pos.getOffset()-1, 1);
				if (s.charAt(0)!='\n') {
					AttributeSet attSet = doc.getCharacterElement(pos.getOffset()-1).getAttributes();
					SimpleAttributeSet sas = new SimpleAttributeSet(attSet);
					//StyleConstants.setFontSize(sas, PreferenceManager.font_size);
					//StyleConstants.setFontFamily(sas, PreferenceManager.font_face);
					doc.insertString(pos.getOffset(), "\n", sas);
				}
			}
                        int start = pos.getOffset();
                        startOffsets.put(e, new Integer(start));
                        
			//should check to make sure tagInfo isn't null!
			if (tagInfo.isTagForDisplay(e.getNodeName())) { //then display tag and its attributes
				Object tagDisplay;
				if (tagInfo == null) tagDisplay = new String(e.getNodeName());
				else tagDisplay = tagInfo.getTagDisplay(e);
				if (tagDisplay instanceof String) {
					@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.isTagItselfTibetan(e.getNodeName())) {
						@TIBETAN@int n = pos.getOffset();
						@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
						@TIBETAN@duff.toTibetanMachineWeb((String)tagDisplay, n);
						@TIBETAN@doc.setCharacterAttributes(n, pos.getOffset()-n, tibAtt, false);
					@TIBETAN@} else {
                                                doc.insertString(pos.getOffset(), (String)tagDisplay, eColor); //insert element begin tag
                                        @TIBETAN@}
				}
				else if (tagDisplay instanceof Icon) {
					pane.setCaretPosition(pos.getOffset());
					pane.insertIcon((Icon)tagDisplay);
					doc.setCharacterAttributes(pos.getOffset()-1, 1, eColor, false);
				}
				//need space before attributes
				//otherwise clicking on right-side of icon won't cause item to play
				if (tagDisplay instanceof Icon) doc.insertString(pos.getOffset(), " ", eColor);
                                
				NamedNodeMap attributes = e.getAttributes();
				for (int z=0; z<attributes.getLength(); z++) {
					Attr att = (Attr)attributes.item(z);
					if (tagInfo == null || tagInfo.isAttributeForDisplay(att.getNodeName(), e.getNodeName()))
						renderAttribute(att, pane, pos.getOffset(), tagInfo, startOffsets, endOffsets);
				}
				@UNICODE@if (tagDisplay instanceof String) doc.insertString(pos.getOffset(), " ", eColor);
			}
                        
			doc.setParagraphAttributes(start, pos.getOffset()-start, eAttributes, false);
			if (tagInfo.areTagContentsForDisplay(e.getNodeName())) {
				NodeList chillern = e.getChildNodes();
				for (int z=0; z<chillern.getLength(); z++) {
					Node next = chillern.item(z);
					if (next instanceof Element) {
						Element ne = (Element)next;
						if (tagInfo.isTagForDisplay(ne.getNodeName()) || tagInfo.areTagContentsForDisplay(ne.getLocalName())) {
							if (tagInfo.isTagForDisplay(e.getNodeName()))
								renderElement(ne, pane, pos.getOffset(), indent + indentIncrement, tagInfo, startOffsets, endOffsets, indentTracker);
							else //only indent if the current tag is displayed
								renderElement(ne, pane, pos.getOffset(), indent, tagInfo, startOffsets, endOffsets, indentTracker);
						}
					} else if (next instanceof Text) {
						Text t = (Text)next;
						if (t.getParentNode().getChildNodes().getLength() == 1 || t.getNodeValue().trim().length() > 0)
							renderText(t, pane, pos.getOffset(), tagInfo, startOffsets, endOffsets);
					}
					// Also: Comment ProcessingInstruction CDATA EntityRef
				}
			}
			if (pos.getOffset()>0) {
				if (doc.getText(pos.getOffset()-1,1).charAt(0)=='\n')
					endOffsets.put(e, new Integer(pos.getOffset()-1));
				else
					endOffsets.put(e, new Integer(pos.getOffset()));
			}
			return pos.getOffset();
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return -1;
		}
	}
	public static int renderAttribute(Attr att, JTextPane pane, int offset, TagInfo tagInfo, Map startOffsets, Map endOffsets) {
		StyledDocument doc = pane.getStyledDocument();
		try {
			Position pos = doc.createPosition(offset);
			SimpleAttributeSet aColor = new SimpleAttributeSet();
			StyleConstants.setForeground(aColor, attColor);
			//added for Tibetan version
			StyleConstants.setFontSize(aColor, font_size);
			StyleConstants.setFontFamily(aColor, font_family);
			SimpleAttributeSet tColor = new SimpleAttributeSet();
			StyleConstants.setForeground(tColor, textColor);
			//added for Tibetan version
			StyleConstants.setFontSize(tColor, font_size);
			StyleConstants.setFontFamily(tColor, font_family);
			tColor.addAttribute("xmlnode", att);
			String name = att.getNodeName();
			String value = att.getValue();
			if (pos.getOffset()>0) {
				String s = doc.getText(pos.getOffset()-1, 1);
				if (s.charAt(0)!='\n') {
					AttributeSet attSet = doc.getCharacterElement(pos.getOffset()-1).getAttributes();
					//added for Tibetan version
					SimpleAttributeSet sas = new SimpleAttributeSet(attSet);
					StyleConstants.setFontSize(sas, font_size);
					StyleConstants.setFontFamily(sas, font_family);
					doc.insertString(pos.getOffset(), " ", sas);
				}
			}
			
			Object displayName;
			if (tagInfo == null) displayName = new String(att.getNodeName());
			else displayName = tagInfo.getAttributeDisplay(att.getNodeName(), att.getOwnerElement().getNodeName());
			if (displayName instanceof String)
				doc.insertString(pos.getOffset(), displayName+"=", aColor);
			else if (displayName instanceof Icon) {
				pane.setCaretPosition(pos.getOffset());
				pane.insertIcon((Icon)displayName);
				doc.setCharacterAttributes(pos.getOffset()-1, 1, aColor, false);
				doc.insertString(pos.getOffset(), "=", aColor);
			}
			startOffsets.put(att, new Integer(pos.getOffset()+1)); //add one so that begin quote is not part of attribute value
			doc.insertString(pos.getOffset(), "\"", tColor);
			@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.isAttributeTextTibetan(att.getNodeName(), att.getOwnerElement().getNodeName())) {
				@TIBETAN@int n = pos.getOffset();
				@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
				@TIBETAN@duff.toTibetanMachineWeb(att.getValue(), n);
				@TIBETAN@SimpleAttributeSet tibAtt = new SimpleAttributeSet();
				@TIBETAN@tibAtt.addAttribute("xmlnode", att);
				@TIBETAN@doc.setCharacterAttributes(n, pos.getOffset()-n, tibAtt, false);
				@TIBETAN@} else {
					@TIBETAN@doc.insertString(pos.getOffset(), att.getValue(), tColor);
					@TIBETAN@}
			@UNICODE@doc.insertString(pos.getOffset(), att.getValue(), tColor);
			doc.insertString(pos.getOffset(), "\"", tColor);
			endOffsets.put(att, new Integer(pos.getOffset()));
			return pos.getOffset();
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return -1;
		}
	}
	public static int renderText(Text t, JTextPane pane, int offset, TagInfo tagInfo, Map startOffsets, Map endOffsets) {
		StyledDocument doc = pane.getStyledDocument();
		try {
			Position pos = doc.createPosition(offset);
			SimpleAttributeSet tAttributes = new SimpleAttributeSet();
			//StyleConstants.setLeftIndent(tAttributes, indent);
			StyleConstants.setForeground(tAttributes, textColor);
			StyleConstants.setFontSize(tAttributes, font_size);
			StyleConstants.setFontFamily(tAttributes, font_family);
			tAttributes.addAttribute("xmlnode", t);

			/*SimpleAttributeSet minimalTAttributes = new SimpleAttributeSet();
			minimalTAttributes.addAttribute("xmlnode", t);
			doc.insertString(pos.getOffset(), " ", minimalTAttributes);*/

			@TIBETAN@SimpleAttributeSet tibAtt = new SimpleAttributeSet();
			@TIBETAN@tibAtt.addAttribute("xmlnode", t);
			@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.areTagContentsTibetan(t.getParentNode().getNodeName())) {
				@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
                                @TIBETAN@duff.toTibetanMachineWeb("_", pos.getOffset());
                                @TIBETAN@int start = pos.getOffset();
                                @TIBETAN@startOffsets.put(t, new Integer(start));
                                @TIBETAN@String s = t.getNodeValue();
                                @TIBETAN@if (s.equals(" "))
				        @TIBETAN@s = "";
				@TIBETAN@duff.toTibetanMachineWeb(s, pos.getOffset());
				@TIBETAN@doc.setCharacterAttributes(start-1, pos.getOffset()-(start-1), tibAtt, false);
			@TIBETAN@} else {
                                doc.insertString(pos.getOffset(), " ", tAttributes); //insert space with text attributes so first character has correct color, xmlnode attribute, etc.
                                int start = pos.getOffset();
                                startOffsets.put(t, new Integer(start));
                                String s = t.getNodeValue();
                                if (s.equals(" "))
                                    s = "";
				doc.insertString(pos.getOffset(), s, tAttributes); //insert text
			@TIBETAN@}

			int end = pos.getOffset();
			endOffsets.put(t, new Integer(end));
			@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane) {
                            @TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
                            @TIBETAN@duff.toTibetanMachineWeb("\n", pos.getOffset());
                            @TIBETAN@doc.setCharacterAttributes(pos.getOffset()-1, 1, tibAtt, false);
                        @TIBETAN@} else {
                            doc.insertString(pos.getOffset(), "\n", tAttributes);
                            //doc.insertString(pos.getOffset(), "\n", minimalTAttributes);
                        @TIBETAN@}
			
			return pos.getOffset();
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return -1;
		} catch (DOMException dome) {
			dome.printStackTrace();
			return -1;
		}
	}
}
