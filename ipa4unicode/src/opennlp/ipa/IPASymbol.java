package opennlp.ipa;

import java.util.*;
import java.io.*;

public class IPASymbol {    
    public static final int UNICODE_CODE_POINT = 0;
    public static final int UNICODE_NAME = 1;
    public static final int UNICODE_CATEGORY = 2;
    public static final int UNICODE_COMBINING_CLASS = 3;
    
    private static Map symbols = new HashMap();
    private Character ipaCharacter;
    private String[] unicodeData;
    private Set phoneticFeatures;
    private List profiles;
    private Set incompatibleWith;

    public static IPASymbol[] getAllSymbols() {
        return (IPASymbol[])symbols.values().toArray(new IPASymbol[0]);
    }
    public static IPASymbol newSymbol(Character c) {
        if (!symbols.containsKey(c))
            symbols.put(c, new IPASymbol(c));
        return (IPASymbol)symbols.get(c);
    }
    private IPASymbol(Character c) {
        if (null == c) throw new NullPointerException("IPA SYMBOL MUST CORRESPOND TO CHARACTER");
        ipaCharacter = c;
        phoneticFeatures = new HashSet();
        incompatibleWith = new HashSet();
        profiles = new LinkedList();
    }
    public static boolean existsSymbol(Character c) {
        return (symbols.containsKey(c));
    }
    public void setFeatures(String features) {
        if (null == features) return;
        String[] featuresSplit = features.split(" ");
        for (int i=0; i<featuresSplit.length; i++)
            phoneticFeatures.add(featuresSplit[i]);
    }
    public Set getFeatures() {
        return phoneticFeatures;
    }
    public void setUnicodeData(String[] data) {
        if (null == data) throw new NullPointerException("IPA SYMBOL MUST CORRESPOND TO EXISTING UNICODE CHARACTER");
        unicodeData = data;
    }
    public char getUnicodeCharacter() {
        return UnicodeUtils.getCharacterForCodePoint(unicodeData[UNICODE_CODE_POINT]);
    }
    public String[] getUnicodeData() {
        return unicodeData;
    }
    public String getUnicodeData(int index) {
        if (index > -1 && index < unicodeData.length)
            return unicodeData[index];
        else
            return null;
    }
    public void addProfile(Profile profile) {
        if (null == profile) throw new NullPointerException("PROFILES CANNOT BE NULL");
        profiles.add(profile);
    }
    public List getProfiles() {
        return profiles;
    }
    public void addIncompatibleWith(IPASymbol s) {
        if (null == s) throw new NullPointerException("INCOMPATIBLE SYMBOLS CANNOT BE NULL");
        incompatibleWith.add(s);
    }
    public Set getIncompatibleWith() {
        return incompatibleWith;
    }
    public static int getSymbolCount() {
        return symbols.size();
    }
    
    static class Profile {
        private Set expects, adds, removes;
        
        Profile(String expects, String adds, String removes) {
            this.expects = new HashSet();
            this.adds = new HashSet();
            this.removes = new HashSet();
            if (null != expects) {
                String[] expectsSplit = expects.split(" ");
                for (int i=0; i<expectsSplit.length; i++)
                    this.expects.add(expectsSplit[i]);
            }
            if (null != adds) {
                String[] addsSplit = adds.split(" ");
                for (int i=0; i<addsSplit.length; i++)
                    this.adds.add(addsSplit[i]);
            }
            if (null != removes) {
                String[] removesSplit = removes.split(" ");
                for (int i=0; i<removesSplit.length; i++)
                    this.removes.add(removesSplit[i]);
            }
        }
        public Set expects() {
            return expects;
        }
        public Set adds() {
            return adds;
        }
        public Set removes() {
            return removes;
        }
    }
}
