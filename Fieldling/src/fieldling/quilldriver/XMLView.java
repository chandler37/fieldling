package fieldling.quilldriver;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.jdom.Document;
import fieldling.quilldriver.TranscriptView;
import fieldling.quilldriver.XMLEditor;
import fieldling.quilldriver.XMLUtilities;


public class XMLView implements TranscriptView {
	private XMLEditor editor;
	private Map startTimeMap;
	private Map endTimeMap;
	private Map startOffsetMap;
	private Map endOffsetMap;
	private Object jdomContextNode;
	private String getNodesXPath;
	private String getStartXPath;
	private String getEndXPath;
	
	public XMLView(XMLEditor editor, Object jdomContextNode, String getNodesXPath, String getStartXPath, String getEndXPath) {
		this.editor = editor;
		this.jdomContextNode = jdomContextNode;
		this.getNodesXPath = getNodesXPath;
		this.getStartXPath = getStartXPath;
		this.getEndXPath = getEndXPath;
		startTimeMap = new HashMap();
		endTimeMap = new HashMap();
		startOffsetMap = new HashMap();
		endOffsetMap = new HashMap();
		refresh();
	}
	public void refresh(Object newContextNode) {
		this.jdomContextNode = newContextNode;
		refresh();
	}
	public void refresh() {
		startTimeMap.clear();
		endTimeMap.clear();
		startOffsetMap.clear();
		endOffsetMap.clear();
		List audioNodes = XMLUtilities.findNodeSet(jdomContextNode, getNodesXPath);
		Iterator iter = audioNodes.iterator();
		while (iter.hasNext()) {
			Object node = iter.next();
			String id = String.valueOf(node.hashCode());
			Object start = XMLUtilities.findSingleNode(node, getStartXPath);
			String startVal = XMLUtilities.getTextForNode(start);
			Object end = XMLUtilities.findSingleNode(node, getEndXPath);
			String endVal = XMLUtilities.getTextForNode(end);
			int startOffset = editor.getStartOffsetForNode(node);
			int endOffset = editor.getEndOffsetForNode(node);
			if (!(startVal == null || endVal == null || startOffset == -1 || endOffset == -1)) {
				startTimeMap.put(id, startVal);
				endTimeMap.put(id, endVal);
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
	public Document getDocument() {
		return editor.getXMLDocument();
	}
}
