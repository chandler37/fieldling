/* ConfigurationFactory - Decompiled by JCavaj
 * Visit http://www.bysoft.se/sureshot/jcavaj/
 */
package fieldling.quilldriver.config;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ConfigurationFactory
{
    public static Configuration[] getAllQDConfigurations(ClassLoader loader) {
	Configuration[] configurations;
	try {
	    SAXBuilder builder = new SAXBuilder();
	    Document doc = builder.build(loader.getResource("qd-configurations.xml"));
	    Element root = doc.getRootElement();
	    List tagOptions = root.getChildren("configuration");
	    Configuration[] config = new Configuration[tagOptions.size()];
	    if (tagOptions.size() > 0) {
		int i = 0;
		Iterator it = tagOptions.iterator();
		while (it.hasNext()) {
		    Element e = (Element) it.next();
		    config[i] = new Configuration(e, loader);
		    i++;
		}
	    }
	    configurations = config;
	} catch (JDOMException jdome) {
	    jdome.printStackTrace();
	    return null;
	} catch (IOException ioe) {
        ioe.printStackTrace();
        return null;
    }
	return configurations;
    }
}

