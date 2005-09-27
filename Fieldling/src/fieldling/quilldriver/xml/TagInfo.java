package fieldling.quilldriver.xml;

import java.util.*;
import javax.swing.*;

public class TagInfo {
	private HashMap displayYesNo, displayContentsYesNo, displayAs, displayIcon, editableYesNo;
	@TIBETAN@private HashMap areTagContentsTibetan, isAttributeTextTibetan, isTagItselfTibetan;
	private HashMap attributeDisplayYesNo, attributeDisplayAs, attributeDisplayIcon, attributeEditableYesNo;
        private KeyStroke keyboardShortcut = null;
	private String identifyingName = null;
        private org.jdom.Namespace[] namespaces;
        
	public TagInfo() {
		init();
	}
	public TagInfo(String identifyingName) {
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
                namespaces = new org.jdom.Namespace[0];
	}
	public void setIdentifyingName(String name) {
		identifyingName = name;
	}
	public String getIdentifyingName() {
		return identifyingName;
	}
        public void useNamespaces(org.jdom.Namespace[] namespaces) {
            this.namespaces = namespaces;
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
		if (displayIcon != null) this.displayIcon.put(tag, new ImageIcon(TagInfo.class.getResource(displayIcon)));
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
		if (displayIcon != null) attributeDisplayIcon.put(s, new ImageIcon(TagInfo.class.getResource(displayIcon)));
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
                        //following is for saxon, which supports XPath 2.0
                        String s = XPathUtilities.saxonSelectSingleDOMNodeToString(tag, val, namespaces);
                        //following is for jaxen, which only supports XPath 1.0
                        //Object node = XPathUtilities.selectSingleDOMNode(tag, val.substring(val.indexOf(':')+1));
                        //if (node == null) return name;
			//String s = XPathUtilities.getTextForDOMNode(node);
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
        public void setKeyboardShortcut(KeyStroke key) {
            keyboardShortcut = key;
        }
        public KeyStroke getKeyboardShortcut() {
            return keyboardShortcut;
        }
        public static TagInfo[] getTagInfoFromXMLConfiguration(org.jdom.Element renderingInstructions) {
			List sharedInstructions = renderingInstructions.getChildren("tag");
			List tagViews = renderingInstructions.getChildren("tagview");
			TagInfo[] tagInfo;
			if (tagViews.size() == 0) { //only one set of rendering instructions
				tagInfo = new TagInfo[1];
				tagInfo[0] = new TagInfo();
				if (sharedInstructions.size() > 0) {
					Iterator it = sharedInstructions.iterator();
					while (it.hasNext()) {
						org.jdom.Element e = (org.jdom.Element)it.next();
						@TIBETAN@tagInfo[0].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@TIBETAN@new Boolean(e.getAttributeValue("tibetancontents")), e.getAttributeValue("icon"), 
                                                        @TIBETAN@new Boolean(e.getAttributeValue("editable")), new Boolean(e.getAttributeValue("tibetan")));
						@UNICODE@tagInfo[0].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						List atts = e.getChildren("attribute");
						Iterator it2 = atts.iterator();
						while (it2.hasNext()) {
							org.jdom.Element eAtt = (org.jdom.Element)it2.next();
                                                        @TIBETAN@tagInfo[0].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								@TIBETAN@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								@TIBETAN@eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")),
                                                                @TIBETAN@new Boolean(eAtt.getAttributeValue("tibetan")));
							@UNICODE@tagInfo[0].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								@UNICODE@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								@UNICODE@eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
						}
					}
				}
			} else {
				tagInfo = new TagInfo[tagViews.size()];
				int count = 0;
				Iterator tagViewIter = tagViews.iterator();
				while (tagViewIter.hasNext()) {
					org.jdom.Element tagView = (org.jdom.Element)tagViewIter.next();
					tagInfo[count] = new TagInfo(tagView.getAttributeValue("name"));
					tagInfo[count].setKeyboardShortcut(KeyStroke.getKeyStroke(tagView.getAttributeValue("keystroke")));
					if (sharedInstructions.size() > 0) {
						Iterator it = sharedInstructions.iterator();
						while (it.hasNext()) {
							org.jdom.Element e = (org.jdom.Element)it.next();
							@TIBETAN@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
								@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
								@TIBETAN@new Boolean(e.getAttributeValue("tibetancontents")), e.getAttributeValue("icon"), 
                                                                @TIBETAN@new Boolean(e.getAttributeValue("editable")), new Boolean(e.getAttributeValue("tibetan")));
							@UNICODE@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
								@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
								@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
							List atts = e.getChildren("attribute");
							Iterator it2 = atts.iterator();
							while (it2.hasNext()) {
								org.jdom.Element eAtt = (org.jdom.Element)it2.next();
								@TIBETAN@tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
									@TIBETAN@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
									@TIBETAN@eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")),
                                                                        @TIBETAN@new Boolean(eAtt.getAttributeValue("tibetan")));
								@UNICODE@tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
									@UNICODE@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
									@UNICODE@eAtt.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
							}
						}
					}

					List tagOptions = tagView.getChildren("tag");
					Iterator it = tagOptions.iterator();
					while (it.hasNext()) {
						org.jdom.Element e = (org.jdom.Element)it.next();
						@TIBETAN@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@TIBETAN@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@TIBETAN@new Boolean(e.getAttributeValue("tibetancontents")), e.getAttributeValue("icon"), 
                                                        @TIBETAN@new Boolean(e.getAttributeValue("editable")), new Boolean(e.getAttributeValue("tibetan")));
						@UNICODE@tagInfo[count].addTag(e.getAttributeValue("name"), new Boolean(e.getAttributeValue("visible")),
							@UNICODE@new Boolean(e.getAttributeValue("visiblecontents")), e.getAttributeValue("displayas"),
							@UNICODE@e.getAttributeValue("icon"), new Boolean(e.getAttributeValue("editable")));
						List atts = e.getChildren("attribute");
						Iterator it2 = atts.iterator();
						while (it2.hasNext()) {
							org.jdom.Element eAtt = (org.jdom.Element)it2.next();
							@TIBETAN@tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								@TIBETAN@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								@TIBETAN@e.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")),
                                                                @TIBETAN@new Boolean(eAtt.getAttributeValue("tibetan")));
							@UNICODE@tagInfo[count].addAttribute(eAtt.getAttributeValue("name"), e.getAttributeValue("name"),
								@UNICODE@new Boolean(eAtt.getAttributeValue("visible")), eAtt.getAttributeValue("displayas"),
								@UNICODE@e.getAttributeValue("icon"), new Boolean(eAtt.getAttributeValue("editable")));
						}
					}
					count++;
				}
			}
                        return tagInfo;
        }
}
