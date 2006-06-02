/******* BEGIN LICENSE BLOCK *****
 *
 *    Copyright 2003 Edward Garrett
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ***** END LICENSE BLOCK ***** */

package fieldling.quilldriver.gui;

import javax.swing.JComboBox;
import java.util.*;

public class TranscriptToggler {
    Vector toggleeVector;
    
    public TranscriptToggler() {
        toggleeVector = new Vector();
    }
    public int getNumberOfTranscripts() {
        return toggleeVector.size();
    }
    public void add(QD qd) {
        toggleeVector.add(new Togglee(qd, qd.transcriptFile.getName()));
        Collections.sort(toggleeVector);
    }
    public void remove(QD qd) {
        int index = getIndexForQD(qd);
        if (index != -1)
            toggleeVector.remove(index);
    }
    public String getLabelForIndex(int i) throws IndexOutOfBoundsException {
        Togglee tog = (Togglee)toggleeVector.get(i);
        return tog.label;
    }
    public QD getQDForIndex(int i) throws IndexOutOfBoundsException {
        Togglee tog = (Togglee)toggleeVector.get(i);
        return tog.qd;
    }
    public int getIndexForQD(QD qd) {
        ListIterator itty = toggleeVector.listIterator();
        while (itty.hasNext()) {
            Togglee tog = (Togglee)itty.next();
            if (qd == tog.qd)
                return itty.previousIndex();
        }
        return -1;
    }
    public JComboBox getToggler(QD qd) {
        JComboBox comboBox = new JComboBox(toggleeVector);
        int index = getIndexForQD(qd);
        if (index == -1)
            comboBox.setSelectedIndex(0);
        else
            comboBox.setSelectedIndex(index);
        return comboBox;
    }
    class Togglee implements Comparable {
        QD qd;
        String label;
        
        public Togglee(QD qd, String label) {
            this.qd = qd;
            this.label = label;
        }
        public int compareTo(Object obj) throws ClassCastException {
            Togglee tog = (Togglee)obj;
            return label.compareTo(tog.label);
        }
        public String toString() {
            return label;
        }
    }
}
