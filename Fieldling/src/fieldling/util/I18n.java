package fieldling.util;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JComponent;

public class I18n {
	private static Locale locale = null;
	private static ResourceBundle resources = null;

	private I18n() {}

	public static ResourceBundle getResourceBundle() {
		if (resources == null)
		{
			resources = ResourceBundle.getBundle("MessageBundle", getLocale());
			
			// testing chinese:
			// resources = ResourceBundle.getBundle("MessageBundle", Locale.SIMPLIFIED_CHINESE);
		}
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
			locale = Locale.getDefault();
		return locale;
	}
}