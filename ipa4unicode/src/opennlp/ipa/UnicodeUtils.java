/*
Copyright (c) 2003, Edward Garrett

    This file is part of larkpie.

    larkpie is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    larkpie is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with larkpie; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package opennlp.ipa;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** This class provides convenient static methods for
* working with Unicode data. 
*/
public class UnicodeUtils {

	static final Pattern pattern = Pattern.compile("(\\\\*)\\\\u(.{4})");

	private UnicodeUtils() {} //cannot instantiate

    /** Returns the character corresponding to the given
    * Unicode code point.
    * @param unicodeHex a 16-bit Unicode code point, specified in hexidecimal notation
    * @return the Unicode character for this code point
    */
	public static char getCharacterForCodePoint(String unicodeHex) {
		try {
			int uniNum = Integer.parseInt(unicodeHex, 16);
			return (char)uniNum;
		} catch (NumberFormatException nfe) {
			return ' ';
		}
	}
    /** Returns the String corresponding to the given
    * Unicode code point.
    * @param unicodeHex a 16-bit Unicode code point, specified in hexidecimal notation
    * @return the Unicode character for this code point, wrapped in a String
    */
	public static String getStringForCodePoint(String unicodeHex) {
		try {
			int uniNum = Integer.parseInt(unicodeHex, 16);
			return String.valueOf((char)uniNum);
		} catch (NumberFormatException nfe) {
			return "";
		}
	}
    /** Takes a string and replaces all occurrences of escaped 
    * Unicode character references with the actual characters themselves.
    * @param s the string you would like to search
    * @return the string after performing all replaces
    */
	public static String replaceUnicodeEscapes(String s) {
		Matcher m = pattern.matcher(s);
		StringBuffer buff = new StringBuffer();
		while(m.find()) {
			String backslashes;
			if (m.group(1) == null) backslashes = "";
			else backslashes = m.group(1);
			String hex = m.group(2);
			String ch = getStringForCodePoint(hex);
			if (backslashes.length() % 2 == 0 && ch.length() > 0)
				m.appendReplacement(buff, backslashes+ch);
		}
		return m.appendTail(buff).toString();
	}
}

