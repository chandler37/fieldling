/* Configuration - Decompiled by JCavaj
 * Visit http://www.bysoft.se/sureshot/jcavaj/
 */
package fieldling.quilldriver;
import java.net.URL;

import org.jdom.Element;

class Configuration
{
    String name = null;
    URL configURL = null;
    URL editURL = null;
    URL newURL = null;
    
    Configuration(Element e, ClassLoader loader) {
	name = e.getAttributeValue("menu-name");
	Element configElem = e.getChild("config");
	Element editElem = e.getChild("edit");
	Element newElem = e.getChild("new");
	if (configElem != null)
	    configURL
		= loader.getResource(configElem.getAttributeValue("href"));
	if (editElem != null)
	    editURL = loader.getResource(editElem.getAttributeValue("href"));
	if (newElem != null)
	    newURL = loader.getResource(newElem.getAttributeValue("href"));
    }
    
    String getName() {
	return name;
    }
    
    URL getConfigURL() {
	return configURL;
    }
    
    URL getEditURL() {
	return editURL;
    }
    
    URL getNewURL() {
	return newURL;
    }
}

