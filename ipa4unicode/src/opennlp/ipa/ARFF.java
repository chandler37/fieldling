package opennlp.ipa;

import java.util.*;
import java.io.*;

public class ARFF {
    public static String[] PLACES = {"NULL", "labial-range", "bilabial",
        "labiodental", "linguolabial", "alveolar-range", "dental",
        "postalveolar-range", "alveolar", "postalveolar", "alveolopalatal",
        "palatal", "retroflex", "velar-range", "velar", "uvular", "pharyngeal",
        "epiglottal", "glottal"};
        
    public static String[] PHONATION_TYPES = {"NULL", "voiceless",
        "aspirated", "voiced", "breathy-voice", "creaky-voice"};
    
    public static String[] BASIC_MANNERS = {"NULL", "default-plosive",
        "nasal-release", "lateral-release", "no-release", "trill", "tap",
        "fricative", "affricate", "approximant", "click", "lateral-approximant", "lateral-click"};
    
    public static String[] BONUS_MANNERS = {"lateral", "nasal",
        "ejective", "implosive"};
        
    private static Set places = new HashSet();
    private static Set phonationTypes = new HashSet();
    private static Set basicManners = new HashSet();
    private static Set bonusManners = new HashSet();
    private static String ARFFHeader;
    private static IPANetwork ipaNetwork;
    
    static {
        int i;
        for (i=0; i<PLACES.length; i++)
            places.add(PLACES[i]);
        for (i=0; i<PHONATION_TYPES.length; i++)
            phonationTypes.add(PHONATION_TYPES[i]);
        for (i=0; i<BASIC_MANNERS.length; i++)
            basicManners.add(BASIC_MANNERS[i]);
        for (i=0; i<BONUS_MANNERS.length; i++)
            bonusManners.add(BONUS_MANNERS[i]);
        
        //get the ARFF Header
        StringBuffer buffy = new StringBuffer();
        
        //comments
        buffy.append("%ARFF file for IPA text\n%\n");
        
        //name of relation
        buffy.append("@relation ipatext\n\n");
        
        //attributes
        buffy.append("@attribute n0place {");
        for (i=0; i<PLACES.length-1; i++)
            buffy.append(PLACES[i] + ", ");
        buffy.append(PLACES[PLACES.length-1] + "}\n");
        
        buffy.append("@attribute n0phonation {");
        for (i=0; i<PHONATION_TYPES.length-1; i++)
            buffy.append(PHONATION_TYPES[i] + ", ");
        buffy.append(PHONATION_TYPES[PHONATION_TYPES.length-1] + "}\n");
        
        buffy.append("@attribute n0manner {");
        for (i=0; i<BASIC_MANNERS.length-1; i++)
            buffy.append(BASIC_MANNERS[i] + ", ");
        buffy.append(BASIC_MANNERS[BASIC_MANNERS.length-1] + "}\n");
        /*
        for (i=0; i<BONUS_MANNERS.length; i++) {
            buffy.append("@attribute n0");
            buffy.append(BONUS_MANNERS[i]);
            buffy.append(" {true, false}\n");
        }*/
        buffy.append("@attribute n1place {");
        for (i=0; i<PLACES.length-1; i++)
            buffy.append(PLACES[i] + ", ");
        buffy.append(PLACES[PLACES.length-1] + "}\n");
        
        buffy.append("@attribute n1phonation {");
        for (i=0; i<PHONATION_TYPES.length-1; i++)
            buffy.append(PHONATION_TYPES[i] + ", ");
        buffy.append(PHONATION_TYPES[PHONATION_TYPES.length-1] + "}\n");
        
        buffy.append("@attribute n1manner {");
        for (i=0; i<BASIC_MANNERS.length-1; i++)
            buffy.append(BASIC_MANNERS[i] + ", ");
        buffy.append(BASIC_MANNERS[BASIC_MANNERS.length-1] + "}\n");
        /*
        for (i=0; i<BONUS_MANNERS.length; i++) {
            buffy.append("@attribute n1");
            buffy.append(BONUS_MANNERS[i]);
            buffy.append(" {true, false}\n");
        }*/
        
        buffy.append("\n@data\n");
        
        ARFFHeader = buffy.toString();
    }
    
    private ARFF() {} //do not instantiate
    
    public static void setIPANetwork(IPANetwork network) {
        ipaNetwork = network;
    }
    
    public static void writeARFF(String text, Writer writer) throws IOException {
        writeHeader(writer);
        writeInstances(text, writer);
    }
    public static void writeHeader(Writer writer) throws IOException {
        writer.write(ARFFHeader);
        writer.flush();
    }
    public static void writeInstances(String text, Writer writer) throws IOException {
        String[] instances = getInstances(text);
        for (int i=0; i<instances.length; i++)
            writer.write(instances[i] + "\n");
        writer.flush();
    }
    public static String[] getInstances(String text) {
        IPATokenizer ipaTok = new IPATokenizer(text);
        List tokList = new LinkedList();
        while (ipaTok.hasMoreTokens())
            tokList.add(ipaTok.nextToken());
        Set[] featureArray = new HashSet[tokList.size()];
        int k = 0;
        Iterator itty = tokList.iterator();
        while (itty.hasNext()) {
            String ipaChar = (String)itty.next();
            Set s = ipaNetwork.getFeatures(ipaChar);
            if (s == null)
                featureArray[k++] = new HashSet();
            else
                featureArray[k++] = s;
        }
        
/* need to output the following attributes for each instance:
@attribute n0place {labial-range, bilabial, labiodental, linguolabial, alveolar-range, dental, postalveolar-range, alveolar, postalveolar, alveolopalatal, palatal, retroflex, velar-range, velar, uvular, pharyngeal, epiglottal, glottal}
@attribute n0phonation {voiceless, aspirated, voiced, breathy-voice, creaky-voice}
@attribute n0manner {default-plosive, nasal-release, lateral-release, no-release, trill, tap, fricative, affricate, approximant, click, lateral-approximant, lateral-click}
@attribute n0lateral {true, false}
@attribute n0nasal {true, false}
@attribute n0ejective {true, false}
@attribute n0implosive {true, false}
@attribute n1place {labial-range, bilabial, labiodental, linguolabial, alveolar-range, dental, postalveolar-range, alveolar, postalveolar, alveolopalatal, palatal, retroflex, velar-range, velar, uvular, pharyngeal, epiglottal, glottal}
@attribute n1phonation {voiceless, aspirated, voiced, breathy-voice, creaky-voice}
@attribute n1manner {default-plosive, nasal-release, lateral-release, no-release, trill, tap, fricative, affricate, approximant, click, lateral-approximant, lateral-click}
@attribute n1lateral {true, false}
@attribute n1nasal {true, false}
@attribute n1ejective {true, false}
@attribute n1implosive {true, false}
*/
        String[] characterAttributes = new String[featureArray.length];
        Set s;
        Object[] vals;
        String nullData = new String("NULL, NULL, NULL");
        //String nullData = new String("NULL, NULL, NULL, false, false, false, false");
        
        for (int i=0; i<featureArray.length; i++) {
            StringBuffer buffy = new StringBuffer();
            if (featureArray[i].size() == 0)
                buffy.append(nullData);
            else {
                s = new HashSet(featureArray[i]);
                s.retainAll(places);
                vals = s.toArray();
                buffy.append((String)vals[0]);
                buffy.append(", ");
                s = new HashSet(featureArray[i]);
                s.retainAll(phonationTypes);
                vals = s.toArray();
                buffy.append((String)vals[0]);
                buffy.append(", ");
                s = new HashSet(featureArray[i]);
                s.retainAll(basicManners);
                vals = s.toArray();
                buffy.append((String)vals[0]);
          /*      buffy.append(", ");
                if (featureArray[i].contains("lateral"))
                    buffy.append("true, ");
                else
                    buffy.append("false, ");
                if (featureArray[i].contains("nasal"))
                    buffy.append("true, ");
                else
                    buffy.append("false, ");
                if (featureArray[i].contains("ejective"))
                    buffy.append("true, ");
                else
                    buffy.append("false, ");
                if (featureArray[i].contains("implosive"))
                    buffy.append("true");
                else
                    buffy.append("false");
                */
            }
            characterAttributes[i] = buffy.toString();
        }
        
        Vector instances = new Vector();
        for (int i=0; i<characterAttributes.length-1; i++)
            if (!characterAttributes[i].equals(nullData) && !characterAttributes[i+1].equals(nullData))
                instances.add(characterAttributes[i] + ", " + characterAttributes[i+1]);
        
        return (String[])instances.toArray(new String[0]);
    }
}
