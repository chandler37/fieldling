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

import java.util.Enumeration;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;

/**
* IPATokenizer provides methods to quickly scan a Unicode
 string and tokenize it into potential IPA segments. To verify
* that these segments follow the rules of IPA, however, the
* resulting substrings must be tested with {@link IPANetwork}.
* <P>
        *Note: according to the IPA Handbook Unicode equivalencies page,
        *<a href="http://web.uvic.ca/ling/resources/ipa/charts/unicode_ipa-chart.htm">http://web.uvic.ca/ling/resources/ipa/charts/unicode_ipa-chart.htm</a>,
        *the following code point should be used for ejectives:
        *<UL>
        *<LI>2019;RIGHT SINGLE QUOTATION MARK;Pf;0;ON;;;;;N;SINGLE COMMA QUOTATION MARK;;;;
        *</LI>
        *</UL>
        *According to John Wells' page,
          *  <a href="http://www.phon.ucl.ac.uk/home/wells/ipa-unicode.htm">http://www.phon.ucl.ac.uk/home/wells/ipa-unicode.htm</a>,
        *the following code point should be used:
        *<UL>
        *<LI>02BC;MODIFIER LETTER APOSTROPHE;Lm;0;L;;;;;N;;;;;</LI>
        *</UL>
        *Wells makes more sense, but
        *isn't the reality that users will also use the quotation mark?
        *Other common (if incorrect) substitutions might be the ordinary
        *colon for the length mark (02D0).</P>
        *<P>Does IPA sanction modifier letters occurring before the letter they
        *modify? If so, then this class will have to be revised, and perhaps even
        *made intelligent.</P>
 * @author      Edward Garrett
 */
public class IPATokenizer implements Enumeration {
    int pos, nextStart;
    boolean computedNextStart;
    char[] textArray;
    
    public IPATokenizer(String text) {
        textArray = text.toCharArray();
        pos = -1;
        nextStart = -1;
        computedNextStart = false;
    }
    public boolean hasMoreElements() {
        pos++;
        int i = getNextTokenStart(textArray, pos);
        if (i == -1) {
            computedNextStart = false;
            return false;
        } else {
            nextStart = i;
            computedNextStart = true;
            return true;
        }
    }
    public Object nextElement() {
        if (!computedNextStart)
            nextStart = getNextTokenStart(textArray, pos);
        computedNextStart = false;
        pos = nextStart;
        
        int len = getTokenLength(textArray, nextStart);
        if (len == -1)
            return null;
        
        return new String(textArray, pos, len);
    }
    public boolean hasMoreTokens() {
        return hasMoreElements();
    }
    public String nextToken() {
        return (String)nextElement();
    }
    
    /**
     * Given a char array and an offset into that array, searches
     * backwards for the start position of the first potential IPA
     * segment.
     * To get the length of the IPA character, pass the
     * result to {@link #getTokenLength(char[], int)}.
     * @param data  the char array to scan
     * @param offset    the offset from which to begin backward scanning.
     * Does not consider a potential segment at offset to be a
     * match.
     * @return the offset to the start position of the previous
     * potential IPA segment
     */
    public static int getPrevTokenStart(char[] data, int offset) {
        /* Note: this and subsequent methods treat
            superscript n as a special case, since it is categorized
            as a Lowercase Letter (Ll) rather than a Modifier Letter (Lm):
                207F;SUPERSCRIPT LATIN SMALL LETTER N;Ll;0;L;<super> 006E;;;;N;;;;;
            The only other such example I could find in UnicodeData.txt 
            was superscript i, which IPA anyway doesn't use:
                2071;SUPERSCRIPT LATIN SMALL LETTER I;Ll;0;L;<super> 0069;;;;N;;;;;
        */ 
        int i=offset-1;
        
        for (; i > -1 && i < data.length; i--) {
            if (isStressMark(data[i]) || isMinorMajorGroupMark(data[i]))
                break;
            if (data[i] != '\u207F' && UCharacter.getType(data[i]) == UCharacterCategory.LOWERCASE_LETTER)
                break;
            if (isWhitespace(data[i]) && (i == 0 || !isWhitespace(data[i-1])))
                break;
        }
        if (i >= data.length)
            return -1;
        else 
            return i;
    } 
     /**
     * Given a char array and an offset into that array, searches
     * forward for the start position of the first potential IPA
     * segment.
     * <BR/>
     * To get the length of the IPA character, pass the
     * result to {@link #getTokenLength(char[], int)}.
     * @param data  the char array to scan
     * @param offset    the offset from which to begin fordward scanning.
     * Considers a potential segment from offset to be a
     * match.
     * @return the offset to the start position of the next
     * potential IPA segment
     */    
    public static int getNextTokenStart(char[] data, int offset) {
         //if (!(data[offset] = "\u0361")) { 
             int i=offset;
             for (; i > -1 && i < data.length; i++) {
                 if (isStressMark(data[i]) || isMinorMajorGroupMark(data[i]))
                     break;
                 if (data[i] != '\u207F' && UCharacter.getType(data[i]) == UCharacterCategory.LOWERCASE_LETTER)
                     break;
                 if (isWhitespace(data[i]) && (i == 0 || !isWhitespace(data[i-1])))
                     break;
             }
             if (i >= data.length) 
                 return -1;
             else 
                 return i;
         /*}else {
        int i=offset+1;
        getNextTokenStart(char[] data, int i);
        */
    }
     /**
     * Gets the length of a potential IPA segment starting at
     * start parameter.
     * <BR/>
     * To be used in conjunction with {@link #getNextTokenStart(char[], int)}
     * or {@link #getPrevTokenStart(char[], int)}.
     * @param data  the char array to scan
     * @param start    the start point within the char array of the
     * potential IPA segment.
     * @return -1 if start is not a possible beginning position for
     * a potential IPA segment<BR/>
     * otherwise, the length of the IPA segment beginning at start.
     */    
    public static int getTokenLength(char[] data, int start) {
        if (start >= data.length)
            return -1;
        int i=start;
        if (UCharacter.getType(data[i]) == UCharacterCategory.LOWERCASE_LETTER) {
            if (i == data.length-1) return 1;
            int type;
            do {
                i++;
                type = UCharacter.getType(data[i]);
            } while (i < data.length && !isStressMark(data[i]) && !isMinorMajorGroupMark(data[i]) &&
                             (type ==  UCharacterCategory.NON_SPACING_MARK || //Mn
                              type == UCharacterCategory.MODIFIER_LETTER || //Lm
                              type == UCharacterCategory.MODIFIER_SYMBOL || //Sk
                              data[i] == '\u207F')); //superscript n
        } else if (isWhitespace(data[i])) {
            for (i = i+1; isWhitespace(data[i]); i++);
        } else if (isStressMark(data[i]) || isMinorMajorGroupMark(data[i])) i++;
        else
            return -1;
        return i-start;
    }
    public static boolean isMinorMajorGroupMark(char c) {
        return (c == '\u007C' || c == '\u2016'); //minor (foot) and major (intonation) group mark
    }
    public static boolean isStressMark(char c) {
        return (c == '\u02C8' || c == '\u02CC'); //primary and secondary stress markers
    }
    public static boolean isWhitespace(char c) {
        if (UCharacter.getType(c) == UCharacterCategory.SPACE_SEPARATOR ||
                UCharacter.getType(c) == UCharacterCategory.LINE_SEPARATOR ||
                UCharacter.getType(c) == UCharacterCategory.PARAGRAPH_SEPARATOR)
                    return true;
        else
                    return false;
    }
    public static boolean isWhitespace(String s) {
        char[] cArray = s.toCharArray();
        int k=0;
        while (k < cArray.length) {
            if (!isWhitespace(cArray[k]))
                return false;
            k++;
        }
        return true;
    }
}
