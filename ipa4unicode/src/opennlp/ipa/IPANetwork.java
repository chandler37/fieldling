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
import org.jdom.*;
import org.jdom.input.*;
import com.ibm.icu.lang.*;



/**
* IPANetwork converts an XML data file specifying the featural and
* combinatoric properties of IPA transcription symbols (in Unicode) into a
* finite-state transducer capable of detecting invalid sequences
* of IPA symbols and converting valid sequences into phonological
* feature sets.
* <P>Works best with input strings pre-parsed by {@link IPATokenizer}.</P>
 * @author      Edward Garrett
 */
public class IPANetwork {
    static final Character ZERO_CHAR = new Character('X');
    static final int INITIAL_COMBINING_CLASS = -2;
    static final int FINALX_COMBINING_CLASS = 9999;
    
    State initialState, finalXState;
    Set domainRestrictions;
    Map transitionFunction;
    
     /**
     * Exploits descriptive and combinatoric properties of
     * IPA symbols to construct a finite-state transducer
     * to detect valid IPA and to convert from IPA segments
     * to phonological feature-sets.
     * @param database a database of information on 
     * IPA characters
     */   
    public IPANetwork(IPASymbol[] ipaSymbols) {
            Map m = getDomainRestrictions(ipaSymbols);
            LinkedList ll = new LinkedList(m.keySet());
            initialState = new State(INITIAL_COMBINING_CLASS, null, null);
            finalXState = new State(FINALX_COMBINING_CLASS, null, null);
            domainRestrictions = (Set)m.get(ll.getLast());
            Integer[] cclass = (Integer[])m.keySet().toArray(new Integer[0]);
            int[] combiningClasses = new int[cclass.length];
            for (int i=0; i<cclass.length; i++)
                combiningClasses[i] = cclass[cclass.length-1-i].intValue();
            Set initialTransitions = getInitialTransitions(ipaSymbols, domainRestrictions);
            Set[] remainingTransitions = getRemainingTransitions(ipaSymbols, initialTransitions, combiningClasses, domainRestrictions);
            /*for (int i=0; i<remainingTransitions.length; i++) {
                Iterator gitty = remainingTransitions[i].iterator();
                while (gitty.hasNext()) {
                    StateTransition st = (StateTransition)gitty.next();
                    if (st.upper != 'X') {
                        String cp = IPASymbol.newSymbol(new Character(st.upper)).getUnicodeData(IPASymbol.UNICODE_CODE_POINT);
                                                if (cp.equals("032C") )//|| cp.equals("0320")
                                                    System.out.println(st);
                    }
                }
            }*/
            transitionFunction = getTransitionFunction(initialTransitions, remainingTransitions);
            /*System.out.println("\n\nCHECKING TRANSITION FUNCTION\n");
            Iterator tf1 = transitionFunction.keySet().iterator();
            while (tf1.hasNext()) {
                Map gm = (Map)transitionFunction.get(tf1.next());
                if (gm.containsKey(new Character('\u032C')))
                    System.out.println(gm.get(new Character('\u032C')));
            }*/
    }
     /**
     * Determines legality of potential IPA segment based
     * on combinatoric properties of IPA symbols.
     * @param ipaCharacter the proposed IPA segment
     * @return <EM>true</EM> if proposed segment is legal IPA
     * <BR/>
     * <EM>false</EM> if proposed segment is illegal IPA
     */
    public boolean isLegal(String ipaCharacter) {
        char[] c = (ipaCharacter + ZERO_CHAR).toCharArray();
        Map m = (Map)transitionFunction.get(initialState);
        Character bigC = new Character(c[0]);
        if (!m.containsKey(bigC))
            return false;
        StateTransition st = (StateTransition)m.get(bigC);
        State nextState = st.endState;
        for (int i=1; i<c.length; i++) {
            m = (Map)transitionFunction.get(nextState);
            bigC = new Character(c[i]);
            if (!m.containsKey(bigC))
                return true;
            st = (StateTransition)m.get(bigC);
            nextState = st.endState;
        }
        if (nextState.isFinalState())
            return true;
        else
            return false;
    }
     /**
     * Computes phonological feature set for proposed
     * IPA segment.
     * @param ipaCharacter the proposed IPA segment
     * @return null if proposed segment is illegal,<BR/>
     * otherwise, a Set of phonological features, currently
     * specified as text strings.
     */
    public Set getFeatures(String ipaCharacter) {
        char[] c = (ipaCharacter + ZERO_CHAR).toCharArray();
        Map m = (Map)transitionFunction.get(initialState);
        Character bigC = new Character(c[0]);
        if (!m.containsKey(bigC))
            return null;
        Set features = new HashSet();
        StateTransition st = (StateTransition)m.get(bigC);
        features.addAll(st.lower);
        State nextState = st.endState;
        for (int i=1; i<c.length; i++) {
            m = (Map)transitionFunction.get(nextState);
            bigC = new Character(c[i]);
            if (!m.containsKey(bigC))
                return null;
            st = (StateTransition)m.get(bigC);
            features.addAll(st.lower);
            nextState = st.endState;
        }
        if (nextState.isFinalState())
            return features;
        else
            return null;
    }
    private class DistinctState {
        State state;
        Set features;
        String ipa;
        
        DistinctState(State state, Set features, String ipa) {
            this.state = state;
            this.features = features;
            this.ipa = ipa;
        }
        public boolean equals(Object o) {
            DistinctState ds = (DistinctState)o;
            if (ds.state.equals(state) && ds.features.equals(features))
                return true;
            else
                return false;
        }
        public int hashCode() {
            return state.hashCode() + features.hashCode();
        }
    }
    public String getExactMatches(Set features) {
        Set charSet = new HashSet();
        LinkedList potentialMatches = new LinkedList();
        potentialMatches.add(new DistinctState(initialState, new HashSet(), new String("")));
        do {
            DistinctState ds = (DistinctState)potentialMatches.removeLast();
            if (ds.state.equals(finalXState) && ds.features.equals(features))
                charSet.add(ds.ipa);
            else {
                Map m = (Map)transitionFunction.get(ds.state);
                Iterator iter = m.keySet().iterator();
                while (iter.hasNext()) {
                    Character nextChar = (Character)iter.next();
                    StateTransition st = (StateTransition)m.get(nextChar);
                    if (features.containsAll(st.lower)) {
                        Set newFeatures = new HashSet(ds.features);
                        newFeatures.addAll(st.lower);
                        DistinctState dz = new DistinctState(st.endState, newFeatures, ds.ipa + nextChar.toString());
                        if (!potentialMatches.contains(dz))
                            potentialMatches.addLast(dz);
                    }
                }
            }
        } while (!potentialMatches.isEmpty());
        
        StringBuffer buffy = new StringBuffer();
        Iterator jter = charSet.iterator();
        while (jter.hasNext()) {
            String next = (String)jter.next();
            buffy.append(next.toCharArray(), 0, next.length()-1);
            if (jter.hasNext())
                buffy.append('|');
        }
        return buffy.toString();
    }
    private Map getDomainRestrictions(IPASymbol[] ipaSymbols) {
        //start by creating set of combining classes used by IPA symbols
        Set combiningClasses = new TreeSet();
        for (int i=0; i<ipaSymbols.length; i++) {
            String[] characterData = ipaSymbols[i].getUnicodeData();
            Integer comboClass = new Integer(ipaSymbols[i].getUnicodeData(IPASymbol.UNICODE_COMBINING_CLASS));
            if (!combiningClasses.contains(comboClass))
                combiningClasses.add(comboClass);
        }
        Integer[] cc = (Integer[])combiningClasses.toArray(new Integer[0]);
        
        //prepare to map each combining class to phonetic features that it might have in its domain
        Map m = new LinkedHashMap();
        m.put(cc[0], new HashSet()); //for Lm characters with combining class of zero
        for (int i=cc.length-1; i>0; i--)
            m.put(cc[i], new HashSet());
        
        for (int i=0; i<ipaSymbols.length; i++) {
            String[] unicodeData = ipaSymbols[i].getUnicodeData();
            List profiles = ipaSymbols[i].getProfiles();
            Iterator itty = profiles.iterator();
            while (itty.hasNext()) {
                IPASymbol.Profile profile = (IPASymbol.Profile)itty.next();
                Set s = (Set)m.get(new Integer(ipaSymbols[i].getUnicodeData(IPASymbol.UNICODE_COMBINING_CLASS))); //get set corresponding to current combining class
                s.addAll(profile.expects());
            }
        }
        //ensure that later combo classes include all domain restrictions of earlier combo classes
        Iterator iter = m.keySet().iterator();
        Set last = new HashSet();
        while (iter.hasNext()) {
            Integer comboClass = (Integer)iter.next();
            Set current = (Set)m.get(comboClass);
            current.addAll(last);
            last = current;
        }
        return m;
    }
    private Set getInitialTransitions(IPASymbol[] ipaSymbols, Set dr) {
        Set initialTransitions = new HashSet();
        Set resultStates = new HashSet();
        //get all base characters, i.e. consonants and vowels
        for (int i=0; i<ipaSymbols.length; i++) {
            if (!(ipaSymbols[i].getFeatures().isEmpty())) { //symbol must be C or V because it has phonetic features
                Set properties = ipaSymbols[i].getFeatures();
                Iterator iter2 = properties.iterator();
                Set charProps = new HashSet();
                Set otherProps = new HashSet(); //for transducer
                while (iter2.hasNext()) {
                    String prop = (String)iter2.next();
                    if (dr.contains(prop))
                        charProps.add(prop);
                    else
                        otherProps.add(prop); //for transducer
                }
                State resultState = new State(-1, charProps, new HashSet());
                if (!resultStates.contains(resultState))
                    resultStates.add(resultState);
                initialTransitions.add(new StateTransition(initialState, resultState, ipaSymbols[i].getUnicodeCharacter(), otherProps));
            }
        }
        return initialTransitions;
    }
    
     /*
     KEY FOR FINDING THOSE TO ADD AS REMAINING TRANSITIONS:
     if (iT.endState.properties.containsAll(domainSet)) {*/
    
    private Set[] getRemainingTransitions(IPASymbol[] ipaSymbols, Set initialTransitions, int[] combiningClasses, Set domainRestrictions) {
        Set[] transitionsForClass = new HashSet[combiningClasses.length];
        Set[] modifiersForClass = new HashSet[combiningClasses.length];
        for (int i=0; i<modifiersForClass.length; i++)
            modifiersForClass[i] = new HashSet();
        for (int i=0; i<ipaSymbols.length; i++) {
            if (!(ipaSymbols[i].getProfiles().isEmpty())) { //diacritics and other modifiers have profiles instead of features
                String codePoint = ipaSymbols[i].getUnicodeData(IPASymbol.UNICODE_CODE_POINT);
                int cclass = new Integer(ipaSymbols[i].getUnicodeData(IPASymbol.UNICODE_COMBINING_CLASS)).intValue();
                for (int j = 0; j<combiningClasses.length; j++) {
                    if (combiningClasses[j] == cclass) {
                        modifiersForClass[j].add(ipaSymbols[i]);
                        break;
                    }
                }
            }
        }
        for (int i=0; i<combiningClasses.length; i++) {
            transitionsForClass[i] = new HashSet();
            Iterator mIter = modifiersForClass[i].iterator();
            while (mIter.hasNext()) {
                        //FIXME TO DEAL WITH MORE THAN ONE PROFILE PER DIACRITIC
                        IPASymbol symbol = (IPASymbol)mIter.next();
                        List profiles = symbol.getProfiles();
                        Iterator udder = initialTransitions.iterator();
                        while (udder.hasNext()) {
                            StateTransition iT = (StateTransition)udder.next();
                            if (!iT.endState.incompatibleWith.contains(symbol)) {
                                Iterator pIterator = profiles.iterator();
                                while (pIterator.hasNext()) {
                                    IPASymbol.Profile profile = (IPASymbol.Profile)pIterator.next();
                                    if (iT.endState.properties.containsAll(profile.expects())) {
                                        Set gainSet = new HashSet(profile.adds());
                                        Set leftOverGain = new HashSet(gainSet);
                                        leftOverGain.removeAll(domainRestrictions); //for lower tape we only want those things that can't be possible restriction
                                        gainSet.retainAll(domainRestrictions); //for continuation we only care about what could be possible restriction
                                        Set outputState = new HashSet(iT.endState.properties);
                                        outputState.removeAll(profile.removes());
                                        outputState.addAll(gainSet);
                                        Set incompatibleWith = new HashSet(iT.endState.incompatibleWith);
                                        incompatibleWith.addAll(symbol.getIncompatibleWith());
                                        State res = new State(combiningClasses[i], outputState, incompatibleWith);
                                        StateTransition st = new StateTransition(iT.endState, res, symbol.getUnicodeCharacter(), leftOverGain);
                                        transitionsForClass[i].add(st);
                                        break;
                                    }
                                }
                            }
                        }
                        for (int k=0; k<i; k++) {
                            Iterator iterX = transitionsForClass[k].iterator();
                            while (iterX.hasNext()) {
                                StateTransition iT = (StateTransition)iterX.next();
                                if (!iT.endState.incompatibleWith.contains(symbol)) {
                                    Iterator pIterator = profiles.iterator();
                                    while (pIterator.hasNext()) {
                                        IPASymbol.Profile profile = (IPASymbol.Profile)pIterator.next();
                                        if (iT.endState.properties.containsAll(profile.expects())) {
                                            Set gainSet = new HashSet(profile.adds());
                                            Set leftOverGain = new HashSet(gainSet);
                                            leftOverGain.removeAll(domainRestrictions); //for lower tape we only want those things that can't be possible restriction
                                            gainSet.retainAll(domainRestrictions); //for continuation we only care about what could be possible restriction
                                            Set outputState = new HashSet(iT.endState.properties);
                                            outputState.removeAll(profile.removes());
                                            outputState.addAll(gainSet);
                                            Set incompatibleWith = new HashSet(iT.endState.incompatibleWith);
                                            incompatibleWith.addAll(symbol.getIncompatibleWith());
                                            State res = new State(combiningClasses[i], outputState, incompatibleWith);
                                            StateTransition st = new StateTransition(iT.endState, res, symbol.getUnicodeCharacter(), leftOverGain);
                                            transitionsForClass[i].add(st);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                }
                Set bonus;
                do {
                    bonus = new HashSet();
                    mIter = modifiersForClass[i].iterator();
                    while (mIter.hasNext()) {
                            //FIXME TO DEAL WITH MORE THAN ONE PROFILE PER DIACRITIC
                            IPASymbol symbol = (IPASymbol)mIter.next();
                            List profiles = symbol.getProfiles();
                            Iterator iterX = transitionsForClass[i].iterator();
                            while (iterX.hasNext()) {
                                StateTransition iT = (StateTransition)iterX.next();
                                if (!iT.endState.incompatibleWith.contains(symbol)) {
                                    Iterator pIterator = profiles.iterator();
                                    while (pIterator.hasNext()) {
                                        IPASymbol.Profile profile = (IPASymbol.Profile)pIterator.next();
                                        if (iT.endState.properties.containsAll(profile.expects())) {
                                            Set gainSet = new HashSet(profile.adds());
                                            Set leftOverGain = new HashSet(gainSet);
                                            leftOverGain.removeAll(domainRestrictions); //for lower tape we only want those things that can't be possible restriction
                                            gainSet.retainAll(domainRestrictions); //for continuation we only care about what could be possible restriction
                                            Set outputState = new HashSet(iT.endState.properties);
                                            outputState.removeAll(profile.removes());
                                            outputState.addAll(gainSet);
                                            Set incompatibleWith = new HashSet(iT.endState.incompatibleWith);
                                            incompatibleWith.addAll(symbol.getIncompatibleWith());
                                            State res = new State(combiningClasses[i], outputState, incompatibleWith);
                                            StateTransition st = new StateTransition(iT.endState, res, symbol.getUnicodeCharacter(), leftOverGain);
                                            if (!(transitionsForClass[i].contains(st) || bonus.contains(st)))
                                                bonus.add(st);
                                            break;
                                        }
                                    }
                                }
                            }
                    }
                    transitionsForClass[i].addAll(bonus);
            } while (bonus.size() > 0); //there are still more states
        }
        return transitionsForClass;
    }
    private Map getTransitionFunction(Set initialTransitions, Set[] remainingTransitions) {
        Map f = new HashMap(); //maps states to input to output state map
        Iterator iter;
        
        //start by adding initial transitions
        Map g = new HashMap(); //maps input character to output state (DETERMINISTIC)
        iter = initialTransitions.iterator();
        while (iter.hasNext()) {
            StateTransition st = (StateTransition)iter.next();
            g.put(st.upper, st);
            Map h = new HashMap();
            h.put(ZERO_CHAR, new StateTransition(st.endState, finalXState, ZERO_CHAR.charValue(), st.endState.properties));
            f.put(st.endState, h);
        }
        f.put(initialState, g);
        
        //now add remaining transitions
        for (int i=0; i<remainingTransitions.length; i++) {
            iter = remainingTransitions[i].iterator();
            while (iter.hasNext()) {
                StateTransition st = (StateTransition)iter.next();
                State s = st.startState;
                Map m, h;
                if (f.containsKey(s))
                    m = (Map)f.get(s);
                else {
                    m = new HashMap();
                    m.put(ZERO_CHAR, new StateTransition(st.startState, finalXState, ZERO_CHAR.charValue(), st.startState.properties));
                    f.put(s, m);
                }
                m.put(st.upper, st);
                if (f.containsKey(st.endState))
                    h = (Map)f.get(st.endState);
                else {
                    h = new HashMap();
                    f.put(st.endState, h);
                }
                h.put(ZERO_CHAR, new StateTransition(st.endState, finalXState, ZERO_CHAR.charValue(), st.endState.properties));
            }
        }
        return f;
    }
    /**
    * Models a transition in a finite-state automaton or transducer
    * from one state to another state, given upper and lower
    * string symbols.
    * @author Edward Garrett
    */
    private class StateTransition {
        State startState, endState;
        Character upper;
        Set lower;
        
        /**
        * Creates a transition from one {@link #IPANetwork.State} to another,
        * given upper (input) and lower (output) values.
        * @param startState the state from which the transition begins
        * @param endState the state to which the transition occurs
        * @param upper the character on the upper (input) tape
        * <BR/>This corresponds to the IPA symbol being read.
        * @param lower the String on the lower (output) tape
        * <BR/>This corresponds to the phonological featural
        * breakdown for the IPA symbol.
        */
        StateTransition(State startState, State endState, char upper, Set lower) {
            this.startState = startState;
            this.endState = endState;
            this.upper = new Character(upper);
            this.lower = lower;
        }
        public boolean equals(Object o) {
            StateTransition st = (StateTransition)o;
            if (!st.upper.equals(upper))
                return false;
            if (!st.startState.equals(startState))
                return false;
            if (!st.endState.equals(endState))
                return false;
            if (!st.lower.equals(lower))
                return false;
            return true;
        }
        public int hashCode() {
            return startState.hashCode() + endState.hashCode() + upper.hashCode() + lower.hashCode();
        }
        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append('{');
            buff.append(startState.toString());
            buff.append(", ");
            buff.append(upper);
            buff.append(":");
            buff.append(getSetAsString(lower));
            buff.append(", ");
            buff.append(endState.toString());
            buff.append("}");
            return buff.toString();
        }
    }        
    /**
    * Models a state in a finite-state network, with additional
    * assumptions and fields helpful to the analysis of IPA
    * segments.
    * @author Edward Garrett
    */
    private class State {
        int combiningClass;
        Set properties;
        Set incompatibleWith;
        private boolean isFinalState;
        
        /**
        * Creates a state in a finite-network.
        * @param combiningClass the Unicode combining class value of
        * this state:
        * <UL>
        * <LI>-2 if this is the initial state of the network</LI>
        * <LI>-1 if this is a state reached from having input a base IPA symbol</LI>
        * <LI>302 if this is the final pseudo-state of the network</LI>
        * <LI>otherwise, the combining class of the IPA symbol whose
        * input led to this state</LI>
        * </UL>
        * @param properties the set of phonological properties which
        * this state needs to "expel".
        * @param mutuallyExclusiveWith the set of IPA symbols through which
        * there may be no path from this state
        * <P>If a finite-transducer is made from this state data, then a final
        * empty pseudo-input is provided, at which point the property
        * set is expelled to the output.</P>
        * <P>If this state is the initial or final pseudo-state, then this
        * value is null.
        */
        State(int combiningClass, Set properties, Set mutuallyExclusiveWith) {
            this.combiningClass = combiningClass; //INITIAL_COMBINING_CLASS if start state, FINALX_COMBINING_CLASS if final pseudo-state
            this.properties = properties; //null if start state or final pseudo-state
            incompatibleWith = mutuallyExclusiveWith;
            if (combiningClass == FINALX_COMBINING_CLASS)
                isFinalState = true;
            else
                isFinalState = false;
        }
        /**
        * Says whether this state is a final, accepting state.
        * @return false if the state is the initial state<BR/>
        * true otherwise
        */
        public boolean isFinalState() {
            return isFinalState;
        }
        public boolean equals(Object o) {
            if (!(o instanceof State))
                return false;
            State s2 = (State)o;
            if (combiningClass == s2.combiningClass) {
                if (combiningClass == INITIAL_COMBINING_CLASS || combiningClass == FINALX_COMBINING_CLASS)
                    return true;
                if (properties.equals(s2.properties) && incompatibleWith.equals(s2.incompatibleWith))
                    return true;
            }
            return false;
        }
        public int hashCode() {
            if (properties != null && incompatibleWith != null)
                return combiningClass + properties.hashCode() + incompatibleWith.hashCode();
            else
                return -100;
        }
        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append(combiningClass);
            buff.append('-');
            if (properties == null)
                buff.append("null");
            else 
                buff.append(getSetAsString(properties));
            return buff.toString();
        }
    }
    static public String getSetAsString(Set s) {
        if (s.size() == 0)
            return("");
        else {
            Iterator iter = s.iterator();
            StringBuffer buff = new StringBuffer();
            while (iter.hasNext())
                buff.append(iter.next().toString() + " ");
            return buff.toString();
        }
    }
}
