package opennlp.ipa;

import org.jdom.*;
import java.util.*;
import java.io.*;

public class IPASymbolLoader {
    public static IPASymbol[] readIPASymbols() {
        try {
            Document d = new org.jdom.input.SAXBuilder().build(IPASymbolLoader.class.getResource("ipa4unicode.xml"));
            List symbols = d.getRootElement().getChildren("symbol");
            Iterator it = symbols.iterator();
            while (it.hasNext()) {
                Element e = (Element)it.next();
                char s = UnicodeUtils.getCharacterForCodePoint(e.getAttributeValue("codepoint").substring(2));
                if (s == ' ')
                    System.out.println("ERROR!");
                else  {
                    IPASymbol symbol = IPASymbol.newSymbol(new Character(s));
                    symbol.setFeatures(e.getChildText("features"));
                    List profiles = e.getChildren("profile");
                    Iterator pitty = profiles.iterator();
                    while (pitty.hasNext()) {
                        Element profile = (Element)pitty.next();
                        symbol.addProfile(new IPASymbol.Profile(profile.getChildText("expects"), profile.getChildText("adds"), profile.getChildText("removes")));
                    }
                }
            }
            List incompatibilities = d.getRootElement().getChildren("exclusive-group");
            Iterator itty = incompatibilities.iterator();
            while (itty.hasNext()) {
                Element e = (Element)itty.next();
                String[] s = e.getText().split(" ");
                Character[] c = new Character[s.length];
                for (int i=0; i<s.length; i++)
                    c[i] = new Character(UnicodeUtils.getCharacterForCodePoint(s[i].substring(2)));
                for (int i=0; i<c.length; i++)
                    for (int k=0; k<c.length; k++)
                        IPASymbol.newSymbol(c[i]).addIncompatibleWith(IPASymbol.newSymbol(c[k]));
            }
            System.out.println("number of IPA symbols = " + symbols.size());
            
            int k=0;
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(IPASymbolLoader.class.getResource("UnicodeData.txt").openStream()));
            while ((line = reader.readLine()) != null) {
                Character codePoint = UnicodeUtils.getCharacterForCodePoint(line.substring(0,4));
                if (IPASymbol.existsSymbol(codePoint)) {
                    IPASymbol.newSymbol(codePoint).setUnicodeData(line.split(";"));
                    k++;
                }
            }
            System.out.println("added data for " + k + " Unicode symbols");
            return IPASymbol.getAllSymbols();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JDOMException jdome) {
            jdome.printStackTrace();
        }
        return null;
    }
}
            
            /*IPASymbol[] allSymbols = IPASymbol.getAllSymbols();
            Map combiningClasses = new TreeMap();
            for (int i=0; i<allSymbols.length; i++) {
                String[] dataForSymbol = allSymbols[i].getUnicodeData();
                try {
                    Integer cClass = Integer.parseInt(dataForSymbol[3]);
                    //We are assigning arbitrary combining class of 999 to Letter Modifiers like 
                    //aspiration and secondary articulations, to ensure that they are attached to a
                    //segment last:
                    if (cClass.intValue() == 0 && dataForSymbol[2].equals("Lm"))
                        cClass = new Integer(999);
                    if (!combiningClasses.containsKey(cClass))
                        combiningClasses.put(cClass, new HashSet());
                    Set symbolsInClass = (Set)combiningClasses.get(cClass);
                    symbolsInClass.add(allSymbols[i]);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
            Iterator mitty = combiningClasses.keySet().iterator();
            while (mitty.hasNext()) {
                Integer s = (Integer)mitty.next();
                System.out.print(s + ": ");
                Set res = (Set)combiningClasses.get(s);
                Iterator literator = res.iterator();
                while (literator.hasNext()) {
                    IPASymbol bymbol = (IPASymbol)literator.next();
                    String[] data = bymbol.getUnicodeData();
                    System.out.print(data[1] + ", ");
                }
                System.out.println("");
            }
            for (int j=0; j<allSymbols.length; j++) {
                String[] data = allSymbols[j].getUnicodeData();
                System.out.println(data[1] + ": ");
                List profiles = allSymbols[j].getProfiles();
                Iterator gitter = profiles.iterator();
                while (gitter.hasNext()) {
                    IPASymbol.Profile p = (IPASymbol.Profile)gitter.next();
                    System.out.println("PROFILE");
                    Iterator chitty;
                    System.out.print("expects: ");
                    chitty = p.expects().iterator();
                    while (chitty.hasNext())
                        System.out.print(chitty.next() + " ");
                    System.out.println("");
                    System.out.print("adds: ");
                    chitty = p.adds().iterator();
                    while (chitty.hasNext())
                        System.out.print(chitty.next() + " ");
                    System.out.println("");
                    System.out.print("removes: ");
                    chitty = p.removes().iterator();
                    while (chitty.hasNext())
                        System.out.print(chitty.next() + " ");
                    System.out.println("\n");
                }
            }*/
        /*
                                    in = new BufferedReader(new InputStreamReader(
                                            IPACharacterDatabase.class.getResource("Chars-IPA.txt").openStream()));
                            
                            //created sorted Map of IPA Characters with IPA Names as key values
                            Map ipaMap = new TreeMap();
                                    while ((line = in.readLine()) != null) {
                                            String[] data = line.split(";");
                                            ipaMap.put(data[0], data[1]);
                                    }
                                    
                                                                    in = new BufferedReader(new InputStreamReader(
                                            IPACharacterDatabase.class.getResource("UnicodeData.txt").openStream()));
                            
                            List precomposedIPA = new LinkedList();
                            List ipaUnicodeDataList = new LinkedList();
    
                                    while ((line = in.readLine()) != null) {
                                            String[] data = line.split(";");
                                            if (ipaChars.contains(line.substring(0, line.indexOf(";"))))
                                                    ipaUnicodeDataList.add(data);
                                                    
                                                    }
                                                    */
