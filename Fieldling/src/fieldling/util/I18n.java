package fieldling.util;

import fieldling.quilldriver.PreferenceManager;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.util.*;

import org.thdl.util.SimplifiedLinkedList;

public class I18n {            
            public static final int LANGUAGE_INDEX = 0;
            public static final int COUNTRY_INDEX = 1;
            public static final int VARIANT_INDEX = 2;
            public static final Set COUNTRIES;
            static {
                COUNTRIES = new HashSet();
                String[] isoCountries = Locale.getISOCountries();
                for (int i=0; i<isoCountries.length; i++)
                    COUNTRIES.add(isoCountries[i]);
            }
	private static Locale locale = null;
	private static ResourceBundle resources = null;
	private static String[] languageCodes = {"en", "zh"};

	private I18n() {}

	public static ResourceBundle getResourceBundle() {
		if (resources == null)
		{
			resources = ResourceBundle.getBundle("QdResources", getLocale());
			
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
				Component*/
                                JComponent.setDefaultLocale(l);
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
                //languages[2] = resources.getString("Tibetan");
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
        public static Locale[] getLocalesUpToFallback(Locale startLocale, Locale fallbackLocale) {
            List localeList = new LinkedList();
            localeList.add(startLocale);
            Locale current = startLocale;
            Locale next;
            while ( (next = oneStepToFallbackLocale(current, fallbackLocale)) != null) {
                localeList.add(next);
                current = next;
            }
            return (Locale[])localeList.toArray(new Locale[0]);
        }
        public static Locale oneStepToFallbackLocale(Locale locale, Locale fallbackLocale) {
            if (locale.equals(fallbackLocale))
                return null;
            String[] localeInfo = getLanguageCountryVariant(locale);
            if (localeInfo[COUNTRY_INDEX].equals("")) { //no country
                if (localeInfo[VARIANT_INDEX].equals("")) { //switch to default
                    return fallbackLocale;
                } else {
                    return new Locale(localeInfo[LANGUAGE_INDEX]);
                }
            } else { //country (and presumably language) is filled in
                if (localeInfo[VARIANT_INDEX].equals("")) { //no variant
                    return new Locale(localeInfo[LANGUAGE_INDEX]);
                } else {
                    return new Locale(localeInfo[LANGUAGE_INDEX], localeInfo[COUNTRY_INDEX]);
                }
            }
        }
            public static String[] getLanguageCountryVariant(Locale locale) {
                String[] languageCountryVariant = new String[3];
                String localeAsString = locale.toString();
                String[] localeAsArray = localeAsString.split("_");
                if (localeAsArray.length == 3) {
                    languageCountryVariant[LANGUAGE_INDEX] = localeAsArray[0];
                    languageCountryVariant[COUNTRY_INDEX] = localeAsArray[1];
                    languageCountryVariant[VARIANT_INDEX] = localeAsArray[2];
                } else if (localeAsArray.length == 2) {
                    languageCountryVariant[LANGUAGE_INDEX] = localeAsArray[0];
                    if (COUNTRIES.contains(localeAsArray[1])) {
                        languageCountryVariant[COUNTRY_INDEX] = localeAsArray[1];
                        languageCountryVariant[VARIANT_INDEX] = "";
                    } else {
                        languageCountryVariant[COUNTRY_INDEX] = "";
                        languageCountryVariant[VARIANT_INDEX] = localeAsArray[1];
                    }
                } else {
                    languageCountryVariant[LANGUAGE_INDEX] = localeAsArray[0];
                    languageCountryVariant[COUNTRY_INDEX] = "";
                    languageCountryVariant[VARIANT_INDEX] = "";
                }
                return languageCountryVariant;
            }
}