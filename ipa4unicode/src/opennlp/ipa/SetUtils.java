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

import java.util.*;

/**
* This class provides convenient static methods for
* working with {@link java.util.Set}s.
 * @author      Edward Garrett
 */
public class SetUtils {
    /** Recursively computes the powerset of a set.
    * @param set the set to compute the powerset of
    * @return the powerset of the passed parameter
    */
    public static Set powerSet(Set set) {
        Set poSet = new HashSet();
        poSet.add(set);
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            Set subSet = new HashSet(set);
            subSet.remove(iter.next());
            if (!poSet.contains(subSet))
                poSet.addAll(powerSet(subSet));
        }
        return poSet;
    }
}
