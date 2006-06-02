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
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;

public class ConfigurationFactory
{
    public static ClassLoader loader = ConfigurationFactory.class.getClassLoader();
    public static Configuration[] configurations = null;
    
    public static Configuration getConfiguration(String name) {
        if (configurations == null) {
            configurations = getAllQDConfigurations();
            if (configurations == null)
                return null;
        }
	for (int j = 0; j < configurations.length; j++) {
            if (configurations[j].getName().equals(name))
                return configurations[j];
        }
        return null;
    }
    
    public static Configuration[] getAllQDConfigurations() {
	if (configurations != null)
            return configurations;
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
                    try {
                        config[i] = new Configuration(e, loader);
                    } catch (JDOMException jdome) {
                        jdome.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
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
        } catch (TransformerException trex) {
            trex.printStackTrace();
            return null;
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return null;
        } catch (SAXException saxe) {
            saxe.printStackTrace();
            return null;
        }
	return configurations;
    }
}

