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

package fieldling.quilldriver;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import fieldling.quilldriver.TranscriptView;
import fieldling.quilldriver.XMLEditor;
import fieldling.quilldriver.XMLUtilities;


public class XMLView implements TranscriptView {
	private XMLEditor editor;
	private Map startTimeMap;
	private Map endTimeMap;
	private Map startOffsetMap;
	private Map endOffsetMap;
	private Object domContextNode;
	//private Object jdomContextNode;
	private String getNodesXPath;
	private String getStartXPath;
	private String getEndXPath;
    private org.jdom.Namespace[] namespaces;
	
    public XMLView(XMLEditor editor, Object domContextNode, String getNodesXPath, String getStartXPath, String getEndXPath, org.jdom.Namespace[] namespaces) {
        this.editor = editor;
		this.domContextNode = domContextNode;
		this.getNodesXPath = getNodesXPath;
		this.getStartXPath = getStartXPath;
		this.getEndXPath = getEndXPath;
        this.namespaces = namespaces;
		startTimeMap = new HashMap();
		endTimeMap = new HashMap();
		startOffsetMap = new HashMap();
		endOffsetMap = new HashMap();
		refresh();
	}
	public void refresh(Object newContextNode) {
		this.domContextNode = newContextNode;
		refresh();
	}
	public void refresh() {
		startTimeMap.clear();
		endTimeMap.clear();
		startOffsetMap.clear();
		endOffsetMap.clear();
		List audioNodes = XMLUtilities.selectDOMNodes(domContextNode, getNodesXPath, namespaces);
		Iterator iter = audioNodes.iterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			String id = String.valueOf(node.hashCode());
			Object start = XMLUtilities.selectSingleDOMNode(node, getStartXPath, namespaces);
			String startVal = XMLUtilities.getTextForDOMNode(start);
			Object end = XMLUtilities.selectSingleDOMNode(node, getEndXPath, namespaces);
			String endVal = XMLUtilities.getTextForDOMNode(end);
			int startOffset = editor.getStartOffsetForNode(node);
			int endOffset = editor.getEndOffsetForNode(node);
			if (!(startVal == null || startOffset == -1 || endOffset == -1)) {
				startTimeMap.put(id, startVal);
				if (endVal == null) //for single time-point (no end time), treat as if end time = start time
					endTimeMap.put(id, startVal);
				else endTimeMap.put(id, endVal);
				startOffsetMap.put(id, String.valueOf(startOffset));
				endOffsetMap.put(id, String.valueOf(endOffset));
			}
		}
	}
	public String getTitle() {
		return "No Title";
	}
	public JTextComponent getTextComponent() {
		return (JTextComponent)editor.getTextPane();
	}
	public String getIDs() {
		Set idSet = startTimeMap.keySet();
		Iterator iter = idSet.iterator();
		StringBuffer idBuff = new StringBuffer();
		while (iter.hasNext()) {
			idBuff.append((String)iter.next());
			idBuff.append(',');
		}
		return idBuff.toString();
	}
	public String getT1s() {
		Collection c = startTimeMap.values();
		Iterator iter = c.iterator();
		StringBuffer buff = new StringBuffer();
		while (iter.hasNext()) {
			buff.append((String)iter.next());
			buff.append(',');
		}
		return buff.toString();
	}
	public String getT2s() {
		Collection c = endTimeMap.values();
		Iterator iter = c.iterator();
		StringBuffer buff = new StringBuffer();
		while (iter.hasNext()) {
			buff.append((String)iter.next());
			buff.append(',');
		}
		return buff.toString();		
	}
	public String getStartOffsets() {
		Collection c = startOffsetMap.values();
		Iterator iter = c.iterator();
		StringBuffer buff = new StringBuffer();
		while (iter.hasNext()) {
			buff.append((String)iter.next());
			buff.append(',');
		}
		return buff.toString();		
	}
	public String getEndOffsets() {
		Collection c = endOffsetMap.values();
		Iterator iter = c.iterator();
		StringBuffer buff = new StringBuffer();
		while (iter.hasNext()) {
			buff.append((String)iter.next());
			buff.append(',');
		}
		return buff.toString();
	}
	public org.w3c.dom.Document getDocument() {
		return editor.getXMLDocument();
	}
}
