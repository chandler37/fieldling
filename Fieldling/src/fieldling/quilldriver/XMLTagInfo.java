package fieldling.quilldriver;

import java.util.HashMap;
import org.jdom.Element;
import fieldling.quilldriver.XMLUtilities;
import javax.swing.ImageIcon;

public class XMLTagInfo {
	private HashMap displayYesNo, displayContentsYesNo, displayAs, displayIcon, editableYesNo;
	@TIBETAN@private HashMap isTagTextTibetan;
	private HashMap attributeDisplayYesNo, attributeDisplayAs, attributeDisplayIcon, attributeEditableYesNo;
	private String identifyingName = null;

	public XMLTagInfo() {
		init();
	}
	public XMLTagInfo(String identifyingName) {
		setIdentifyingName(identifyingName);
		init();
	}
	private void init() {
		displayYesNo = new HashMap();
		displayContentsYesNo = new HashMap();
		displayAs = new HashMap();
		displayIcon = new HashMap();
		editableYesNo = new HashMap();
		@TIBETAN@isTagTextTibetan = new HashMap();
		attributeDisplayYesNo = new HashMap();
		attributeDisplayAs = new HashMap();
		attributeDisplayIcon = new HashMap();
		attributeEditableYesNo = new HashMap();
	}
	public void setIdentifyingName(String name) {
		identifyingName = name;
	}
	public String getIdentifyingName() {
		return identifyingName;
	}
	public boolean containsTag(String tag) {
		if (displayYesNo.get(tag) == null) return false;
		else return true;
	}
	@TIBETAN@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs, Boolean isTagTextTibetan, String displayIcon, Boolean isEditable) {
	@UNICODE@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs, String displayIcon, Boolean isEditable) {
		displayYesNo.put(tag, display);
		displayContentsYesNo.put(tag, displayContents);
		this.displayAs.put(tag, displayAs);
		editableYesNo.put(tag, isEditable);
		if (displayIcon != null) this.displayIcon.put(tag, new ImageIcon(XMLTagInfo.class.getResource(displayIcon)));
		@TIBETAN@this.isTagTextTibetan.put(tag, isTagTextTibetan);
	}
	public void removeTag(String tag) {
		displayYesNo.remove(tag);
		displayContentsYesNo.remove(tag);
		displayAs.remove(tag);
		editableYesNo.remove(tag);
		displayIcon.remove(tag);
	}
	public void addAttribute(String name, String parentTag, Boolean display, String displayAs, String displayIcon, Boolean isEditable) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.put(s, display);
		attributeDisplayAs.put(s, displayAs);
		attributeEditableYesNo.put(s, isEditable);
		if (displayIcon != null) attributeDisplayIcon.put(s, new ImageIcon(XMLTagInfo.class.getResource(displayIcon)));
	}
	public void removeAttribute(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.remove(s);
		attributeDisplayAs.remove(s);
		attributeDisplayIcon.remove(s);
		attributeEditableYesNo.remove(s);
	}	
	public boolean isTagForDisplay(String tag) {
		Object obj = displayYesNo.get(tag);
		if (obj == null) return true; 
		else return ((Boolean)obj).booleanValue();
	}
	public boolean isTagEditable(String tag) {
		Object obj = editableYesNo.get(tag);
		if (obj == null) return true;
		else return ((Boolean)obj).booleanValue();
	}
	@TIBETAN@public boolean isTagTextTibetan(String tag) {
		@TIBETAN@Object obj = isTagTextTibetan.get(tag);
		@TIBETAN@if (obj == null) return false;
		@TIBETAN@else return ((Boolean)obj).booleanValue();
	@TIBETAN@}
	public boolean areTagContentsForDisplay(String tag) {
		Object obj = displayContentsYesNo.get(tag);
		if (obj == null) return true; 
		else return ((Boolean)obj).booleanValue();
	}
	public Object getTagDisplay(Element tag) {
		String name = tag.getQualifiedName();
		Object icon = displayIcon.get(name);
		if (icon == null) {
			Object obj = displayAs.get(name);
			if (obj == null) return name;
			String val = (String)obj;
			if (val.startsWith("XPATH:")) {
				Object node = XMLUtilities.findSingleNode(tag, val.substring(val.indexOf(':')+1));
				if (node == null) return name;
				String s = XMLUtilities.getTextForNode(node);
				if (s == null) return name;
				else return s;
			} else return val;
		} else return icon;
	}
	public boolean isAttributeEditable(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		Object obj = attributeEditableYesNo.get(s);
		if (obj == null) return true;
		else return ((Boolean)obj).booleanValue();
	}
	public boolean isAttributeForDisplay(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		Object obj = attributeDisplayYesNo.get(s);
		if (obj == null) return true;
		else return ((Boolean)obj).booleanValue();
	}
	public Object getAttributeDisplay(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		Object icon = attributeDisplayIcon.get(s);
		if (icon == null) {
			Object obj = attributeDisplayAs.get(s);
			if (obj == null) return name;
			else return (String)obj;
		} else return icon;
	}
}
