package opennlp.ipa;

import java.util.*;
import java.util.regex.*;

public class IPAUtils {
    /* int flags = Pattern.CANON_EQ;
	note: according to the Java API, passing this 
	flag when compiling a Pattern should cause
	decomposed and composed forms to be treated
	as equivalents for regular expressions searches;
	however, I have found this not to be the case,
	due to documented Java bugs, and so have been
	forced to use a workaround.*/
        
    static final char FILLER = '\uE000';
    IPANetwork ipaNetwork;
    
    public class Sequence {
        public int start, end;
        
        public Sequence(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    public IPAUtils(IPANetwork network) {
        ipaNetwork = network;
    }
    public Sequence[] doPhonologicalQuery(String toSearch, String query) {
        /* find all <phonological feature sets> in the query,
        and add to searchSet */
        Pattern pattern = Pattern.compile("<([^<>]*)>");
        Matcher matcher = pattern.matcher(query);
        Set searchSet = new HashSet();
        while (matcher.find()) {
            String[] props = matcher.group(1).split(" +");
            Set featureSet = new HashSet();
            for (int i=0; i<props.length; i++)
                featureSet.add(props[i]);
            if (!searchSet.contains(featureSet))
                searchSet.add(featureSet);
        }
        
        //replace searchSet with powerset of searchSet
        searchSet = SetUtils.powerSet(searchSet);
        
        /* create Map which assigns to each member of searchSet
        a unique PUA character to replace it in the search string */
        Iterator i1 = searchSet.iterator();
        char c = '\uE001'; //second Unicode character in Private Use Area (PUA)
        Map puaMap = new HashMap();
        while (i1.hasNext()) {
            Set s1 = (Set)i1.next();
            Iterator i2 = s1.iterator();
            Set newSet = new HashSet();
            while (i2.hasNext()) {
                Set s2 = (Set)i2.next();
                newSet.addAll(s2);
            }
            if (newSet.size() > 0 && !puaMap.containsKey(newSet))
                puaMap.put(newSet, new Character(c++));
        }
        
        //for testing
        Iterator i3 = puaMap.keySet().iterator();
        while (i3.hasNext()) {
            Object o = i3.next();
            System.out.print(o + " : ");
            Character puaChar = (Character)puaMap.get(o);
            System.out.println(Integer.toHexString((int)puaChar.charValue()));
        }
        
        /* create a new regular expression query that replaces phonological
        search criteria with PUA character disjunctions */
        matcher.reset();
        StringBuffer newRegex = new StringBuffer();
        while (matcher.find()) {
            String[] props = matcher.group(1).split(" +");
            Set featureSet = new HashSet();
            for (int i=0; i<props.length; i++)
                featureSet.add(props[i]);
            StringBuffer buff = new StringBuffer();
            c = ((Character)puaMap.get(featureSet)).charValue();
            buff.append('(');
            buff.append(c);
            buff.append(FILLER);
            buff.append('*');
            Iterator i6 = puaMap.keySet().iterator();
            while (i6.hasNext()) {
                Set s = (Set)i6.next();
                if (s.size() > featureSet.size() && s.containsAll(featureSet)) {
                    c = ((Character)puaMap.get(s)).charValue();
                    buff.append('|');
                    buff.append(c);
                    buff.append(FILLER);
                    buff.append('*');
                }
            }
            buff.append(')');
            matcher.appendReplacement(newRegex, buff.toString());
        }
        matcher.appendTail(newRegex);
        String processedQuery = newRegex.toString();
        
        //testing
        System.out.println(newRegex.toString());
        
        /* scan text and replace search-relevant characters with
        PUA replacements */
        Set allFeatures = new HashSet();
        Iterator i4 = puaMap.keySet().iterator();
        while (i4.hasNext())
            allFeatures.addAll((Set)i4.next());
        char[] text = toSearch.toCharArray();
        int offset = 0;
        int length = 0;
        do {
            offset = IPATokenizer.getNextTokenStart(text, offset);
            if (offset == -1)
                break;
            length = IPATokenizer.getTokenLength(text, offset);
            Set f = ipaNetwork.getFeatures(new String(text, offset, length));
            if (f != null) {
                f.retainAll(allFeatures);
                if (puaMap.containsKey(f)) {
                    text[offset] = ((Character)puaMap.get(f)).charValue();
                    for (int i=offset+1; i<offset+length; i++)
                        text[i] = FILLER;
                }
            }
            offset = offset + length;
        } while (true);
        
        /* now, with PUA-replaced regular expression and PUA-replaced
        text, search text using standard regular expressions syntax.
        highlight results. */
        Pattern findPattern = Pattern.compile(processedQuery);
        Matcher m = findPattern.matcher(new String(text));
        List sequences = new LinkedList();
        while (m.find())
            sequences.add(new Sequence(m.start(), m.end()));
        return (Sequence[])sequences.toArray(new Sequence[0]);
    }
}
