package fieldling.util;

import fieldling.quilldriver.PreferenceManager;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.thdl.util.SimplifiedLinkedList;

public class I18n {
	private static Locale locale = null;
	private static ResourceBundle resources = null;
	private static String[] languageCodes = {"en", "zh"};

	private I18n() {}

	public static ResourceBundle getResourceBundle() {
		if (resources == null)
		{
			resources = ResourceBundle.getBundle("MessageBundle", getLocale());
			
			// testing chinese:
			// resources = ResourceBundle.getBundle("MessageBundle", Locale.SIMPLIFIED_CHINESE);
		}	
		
		/*
		 * Load the correct font in order to display properly
		 * the resourceBundle just loaded.
		 */
		if (PreferenceManager.default_interface_font!=null)
			setSystemFont(PreferenceManager.default_interface_font);
			
		return resources;
	}
	
	public static void setLocale(Locale l) {
		Locale[] locales = Locale.getAvailableLocales();
		for (int k=0; k<locales.length; k++)
			if (locales[k].equals(l)) {
				/*comment out for now because this requires JDK 1.4
				Seems like there's no way to set this property globally in 1.3 -
				instead looks like you have to set the locale separately for each
				Component
					JComponent.setDefaultLocale(l);
					*/
				locale = l;
				break;
			}
	}
	
	public static Locale getLocale() {
		if (locale == null)
		{
			int language = PreferenceManager.default_language;
			if (language == -1)
				locale = Locale.getDefault();
			else
				locale = new Locale(languageCodes[language]);
		}
		return locale;
	}
	
	public static String getDefaultDisplayLanguage()
	{
		return resources.getString(locale.getDisplayLanguage());
	}
	
	public static String[] getSupportedLanguages()
	{
		String languages[] = new String[2];
		languages[0] = resources.getString("English");
		languages[1] = resources.getString("Chinese");
		return languages;
	}
	
	public static String[] getSupportedFonts(int lang)
	{
		SimplifiedLinkedList fonts = new SimplifiedLinkedList();
		Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		int fontcount = 0;
		String testStrings[] = {"Ok", "\u786e\u5b9a"};
		
		for (int j = 0; j < allfonts.length; j++)
		{
		    if (allfonts[j].canDisplayUpTo(testStrings[lang]) == -1)
		    { 
		        fonts.addSortedUnique(allfonts[j].getFamily());
		    }
		}
		return fonts.toReverseStringArray();
	}
	
	public static void setSystemFont(String font)
	{
		FontUIResource f = new FontUIResource(font, Font.PLAIN, 13); 
	    java.util.Enumeration keys = UIManager.getDefaults().keys();
	    
	    while (keys.hasMoreElements()) {
	      Object key = keys.nextElement();
	      Object value = UIManager.get (key);
	      if (value instanceof javax.swing.plaf.FontUIResource)
	        UIManager.put (key, f);
	      }		
	}	
}