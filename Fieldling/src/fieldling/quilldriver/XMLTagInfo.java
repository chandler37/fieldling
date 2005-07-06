package fieldling.quilldriver;

import java.util.HashMap;
import fieldling.quilldriver.XMLUtilities;
import javax.swing.ImageIcon;

public class XMLTagInfo {
	private HashMap displayYesNo, displayContentsYesNo, displayAs, displayIcon, editableYesNo;
	@TIBETAN@private HashMap areTagContentsTibetan, isAttributeTextTibetan, isTagItselfTibetan;
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
                @TIBETAN@isTagItselfTibetan = new HashMap();
		@TIBETAN@areTagContentsTibetan = new HashMap();
                @TIBETAN@isAttributeTextTibetan = new HashMap();
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
	@TIBETAN@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs, Boolean areTagContentsTibetan, String displayIcon, Boolean isEditable, Boolean isTagItselfTibetan) {
	@UNICODE@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs, String displayIcon, Boolean isEditable) {
		displayYesNo.put(tag, display);
		displayContentsYesNo.put(tag, displayContents);
		this.displayAs.put(tag, displayAs);
		editableYesNo.put(tag, isEditable);
		if (displayIcon != null) this.displayIcon.put(tag, new ImageIcon(XMLTagInfo.class.getResource(displayIcon)));
		@TIBETAN@this.areTagContentsTibetan.put(tag, areTagContentsTibetan);
                @TIBETAN@this.isTagItselfTibetan.put(tag, isTagItselfTibetan);
	}
	public void removeTag(String tag) {
		displayYesNo.remove(tag);
		displayContentsYesNo.remove(tag);
		displayAs.remove(tag);
		editableYesNo.remove(tag);
		displayIcon.remove(tag);
                @TIBETAN@areTagContentsTibetan.remove(tag);
                @TIBETAN@isTagItselfTibetan.remove(tag);
	}
	@UNICODE@public void addAttribute(String name, String parentTag, Boolean display, String displayAs, String displayIcon, Boolean isEditable) {
	@TIBETAN@public void addAttribute(String name, String parentTag, Boolean display, String displayAs, String displayIcon, Boolean isEditable, Boolean isTibetan) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.put(s, display);
		attributeDisplayAs.put(s, displayAs);
		attributeEditableYesNo.put(s, isEditable);
                @TIBETAN@isAttributeTextTibetan.put(s, isTibetan);
		if (displayIcon != null) attributeDisplayIcon.put(s, new ImageIcon(XMLTagInfo.class.getResource(displayIcon)));
	}
	public void removeAttribute(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.remove(s);
		attributeDisplayAs.remove(s);
		attributeDisplayIcon.remove(s);
		attributeEditableYesNo.remove(s);
                @TIBETAN@isAttributeTextTibetan.remove(s);
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
        @TIBETAN@public boolean isTagItselfTibetan(String tag) {
                @TIBETAN@Object obj = isTagItselfTibetan.get(tag);
                @TIBETAN@if (obj == null) return false;
                @TIBETAN@else return ((Boolean)obj).booleanValue();
        @TIBETAN@}
	@TIBETAN@public boolean areTagContentsTibetan(String tag) {
		@TIBETAN@Object obj = areTagContentsTibetan.get(tag);
		@TIBETAN@if (obj == null) return false;
		@TIBETAN@else return ((Boolean)obj).booleanValue();
	@TIBETAN@}
	public boolean areTagContentsForDisplay(String tag) {
		Object obj = displayContentsYesNo.get(tag);
		if (obj == null) return true; 
		else return ((Boolean)obj).booleanValue();
	}
    public Object getTagDisplay(org.w3c.dom.Element tag) {
		String name = tag.getNodeName();
		Object icon = displayIcon.get(name);
		if (icon == null) {
			Object obj = displayAs.get(name);
			if (obj == null) return name;
			String val = (String)obj;
            Object node = XMLUtilities.selectSingleDOMNode(tag, val.substring(val.indexOf(':')+1));
            if (node == null) return name;
			String s = XMLUtilities.getTextForDOMNode(node);
			if (s == null) return name;
			else return s;
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
	@TIBETAN@public boolean isAttributeTextTibetan(String name, String parentTag) {
                @TIBETAN@String s = parentTag + "/@" + name;
		@TIBETAN@Object obj = isAttributeTextTibetan.get(s);
		@TIBETAN@if (obj == null) return false;
		@TIBETAN@else return ((Boolean)obj).booleanValue();
	@TIBETAN@}
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
