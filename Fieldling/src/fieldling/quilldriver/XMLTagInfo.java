package fieldling.quilldriver;

import java.util.HashMap;
import org.jdom.Element;
import fieldling.quilldriver.XMLUtilities;

public class XMLTagInfo {
	private HashMap displayYesNo, displayContentsYesNo, displayAs;
	@TIBETAN@private HashMap isTagTextTibetan;
	private HashMap attributeDisplayYesNo, attributeDisplayAs;

	public XMLTagInfo() {
		displayYesNo = new HashMap();
		displayContentsYesNo = new HashMap();
		displayAs = new HashMap();
		@TIBETAN@isTagTextTibetan = new HashMap();
		attributeDisplayYesNo = new HashMap();
		attributeDisplayAs = new HashMap();
	}
	public boolean containsTag(String tag) {
		if (displayYesNo.get(tag) == null) return false;
		else return true;
	}
	@TIBETAN@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs, Boolean isTagTextTibetan) {
	@UNICODE@public void addTag(String tag, Boolean display, Boolean displayContents, String displayAs) {		
		displayYesNo.put(tag, display);
		displayContentsYesNo.put(tag, displayContents);
		this.displayAs.put(tag, displayAs);
		@TIBETAN@this.isTagTextTibetan.put(tag, isTagTextTibetan);
	}
	public void removeTag(String tag) {
		displayYesNo.remove(tag);
		displayContentsYesNo.remove(tag);
		displayAs.remove(tag);
	}
	public void addAttribute(String name, String parentTag, Boolean display, String displayAs) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.put(s, display);
		attributeDisplayAs.put(s, displayAs);
	}
	public void removeAttribute(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		attributeDisplayYesNo.remove(s);
		attributeDisplayAs.remove(s);
	}	
	public boolean isTagForDisplay(String tag) {
		Object obj = displayYesNo.get(tag);
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
	public String getTagDisplay(Element tag) {
		String name = tag.getQualifiedName();
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
	}
	public boolean isAttributeForDisplay(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		Object obj = attributeDisplayYesNo.get(s);
		if (obj == null) return true;
		else return ((Boolean)obj).booleanValue();
	}
	public String getAttributeDisplay(String name, String parentTag) {
		String s = parentTag + "/@" + name;
		Object obj = attributeDisplayAs.get(s);
		if (obj == null) return name;
		else return (String)obj;
	}
}
