/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2003 Michel Jacobson jacobson@idf.ext.jussieu.fr
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

/*-----------------------------------------------------------------------*/
package org.jaxen;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

import java.util.List;
/*-----------------------------------------------------------------------*/

public class MatchesFunction implements Function {

    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 2) {
            return evaluate(args.get(0), args.get(1), context.getNavigator() );
        }
        throw new FunctionCallException( "matches() requires two arugments." );
    }

    public static Boolean evaluate(Object strArg, Object matchArg, Navigator nav) {
        String str   = StringFunction.evaluate( strArg, nav );
        String match = StringFunction.evaluate( matchArg, nav );
        //return ( str.matches(match)
        //         ? Boolean.TRUE
        //         : Boolean.FALSE
        //         );
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(match);
		java.util.regex.Matcher m = p.matcher(str);
        return ( m.find()
                 ? Boolean.TRUE
                 : Boolean.FALSE
                 );
    }
}
