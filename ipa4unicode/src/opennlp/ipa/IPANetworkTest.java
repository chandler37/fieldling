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

import java.io.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
* Unit tests for {@link IPANetwork}.
* @author      Edward Garrett
 */
public class IPANetworkTest extends TestCase {
    //static variables connected with TestData file
    static final String DATA_FILE = "IPANetworkTestData.txt";
    static final String DATA_FILE_PART2 = "IPANetworkTestDataPart2.txt";
    static final String DATA_FILE_ENCODING = "UTF8";
    static final String DATA_DELIMITER = ";";
    static final String DATA_COMMENTER = "#";
    static final IPANetwork IPA_NETWORK = 
        new IPANetwork(IPASymbolLoader.readIPASymbols());
    
    private String ipaChar;
    private Set features;
    
    /** Encapsulates a test of {@link IPANetwork}.
    */
    public IPANetworkTest(String testMethodName, String ipaChar, Set features) {
        super(testMethodName);
        this.ipaChar = ipaChar;
        this.features = features;
    }
    
    /** Tests whether or not {@link IPANetwork#getFeatures(String)}
    * assigns the correct phonological feature set to a given IPA segment.
    */
    public void testGetFeatures() {
        StringBuffer errorMsg = new StringBuffer();
        errorMsg.append("INCORRECT PHONOLOGICAL FEATURE SET FOR ");
        errorMsg.append(ipaChar);
        errorMsg.append(" (");
        for (int i=0; i<ipaChar.length(); i++) {
            errorMsg.append(' ');
            errorMsg.append(Integer.toHexString(ipaChar.charAt(i)));
        }
        errorMsg.append("):");
        assertEquals(errorMsg.toString(), features, IPA_NETWORK.getFeatures(ipaChar));
    }

    
    /** Tests whether or not {@link IPANetwork#getExactMatches(Set)}
    * assigns the correct phonological feature set to a given IPA segment.
    */
    public void testGetExactMatches() {
        StringBuffer errorMsg = new StringBuffer();
        errorMsg.append(features.toString());
        errorMsg.append(' ');
        errorMsg.append("DOESN'T MATCH (");
        errorMsg.append(ipaChar);
        errorMsg.append("):");
        assertEquals(errorMsg.toString(), ipaChar, IPA_NETWORK.getExactMatches(features));
    }
    /** Reads data from 
    * <a href="IPANetworkTestData.txt">a file</a>
    * and runs {@link #testGetFeatures} on a
    * range of input data. The most recent results of this can
    * be viewed <a target="_blank" href="../../../../reports/index.html">here</a>.
    */
    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite();
        BufferedReader in;
        String line;
        
        in = new BufferedReader(new InputStreamReader(
            IPANetworkTest.class.getResource(DATA_FILE).openStream(), DATA_FILE_ENCODING));
        while (null != (line = in.readLine())) {
            if (!line.equals("") && !line.startsWith(DATA_COMMENTER)) {
                String[] s = line.split(DATA_DELIMITER);
                if (s.length == 2) { //otherwise invalid
                    Set featureSet;
                    if (s[1].equals("ILLEGAL"))
                        featureSet = null;
                    else {
                        featureSet = new HashSet();
                        String[] features = s[1].split("\\s+");
                        for (int i=0; i<features.length; i++)
                            featureSet.add(features[i]);
                    }
                    suite.addTest(new IPANetworkTest("testGetFeatures", s[0], featureSet));
                }
            }
        }
        in = new BufferedReader(new InputStreamReader(
            IPANetworkTest.class.getResource(DATA_FILE_PART2).openStream(), DATA_FILE_ENCODING));
        while (null != (line = in.readLine())) {
            if (!line.equals("") && !line.startsWith(DATA_COMMENTER)) {
                String[] s = line.split(DATA_DELIMITER);
                if (s.length == 2) { //otherwise invalid
                    Set featureSet = new HashSet();
                    String[] features = s[1].split("\\s+");
                    for (int i=0; i<features.length; i++)
                        featureSet.add(features[i]);
                    suite.addTest(new IPANetworkTest("testGetExactMatches", s[0], featureSet));
                }
            }
        }
        return suite;
    }
}
