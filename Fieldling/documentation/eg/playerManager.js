/* ***** BEGIN LICENSE BLOCK *****
 *    Copyright 2002 Michel Jacobson jacobson@idf.ext.jussieu.fr
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





//-----------------------------------------------> here you have to change the parameters
var myPlayerFrame   = document;                  //where is your player
if (navigator.appName == "Microsoft Internet Explorer") {   //for ie don't ask me why
	myPlayerFrame   = document.toString();
}
var myTextFrame     = document;                //where is your text
var styleProperty   = "color";                               //what property you want to change for rendering startplay and the value of the styleProperty for a startplay event events (color, backgroundColor, fontSize, fontWeight, fontStyle,...)
var activateState   = "red";                                //the value of the styleProperty for a startplay event
var inactivateState = "black";                              //the value of the styleProperty for a endplay event


//-----------------------------------------------> you do not have to change the following parameters
var ns = (navigator.appName =="Netscape");


//----------------------------------------------> call to the applet
function playOne(id) {
	with(eval('myPlayerFrame')) {
		if (player.cmd_isID(id)) {
			player.cmd_playS(id);
		}
	}
}
function playFrom(id) {
	with(eval('myPlayerFrame')) {
		if (!player.isActive()) {
			alert("Please wait until the applet is active.");
		} else {
			if (player.cmd_isID(id)) {
				player.cmd_playFrom(id);
			}
		}
	}
}
function stopplay() {
	with(eval('myPlayerFrame')) {
		player.cmd_stop();
	}
}//----------------------------------------------> from the applet to the document
//what the browser should do when it recieve a startplay event
function startplay(id) {
	with(eval('myTextFrame')) {
		if (ns) {
			var n = getElementById(id);
			if (n) {
				n.style[styleProperty] = activateState;
				var elements = n.getElementsByTagName('*');
				for(var i=0; i<elements.length; i++) {
					var node = elements.item(i);
					node.style[styleProperty] = activateState;
				}
			}
		} else {
			var theItem = all.item(id);
			eval('theItem.style.'+styleProperty+' = '+ "'" + activateState + "'");
			for (var i=0; i<theItem.all.length; i++) {
				eval('theItem.all(i).style.'+styleProperty+' = '+ "'" + activateState + "'");
			}
		}
	}
}
//what the browser should do when it recieve a stopplay event
function endplay(id) {
	with(eval('myTextFrame')) {
		if (ns) {
			var n = getElementById(id);
			if (n) {
				n.style[styleProperty] = inactivateState;
				var elements = n.getElementsByTagName('*');
				for(var i=0; i<elements.length; i++) {
					var node = elements.item(i);
					node.style[styleProperty] = inactivateState;
				}
			}
		} else {
			var theItem = all.item(id);
			eval('theItem.style.'+styleProperty+' = '+ "'" + inactivateState + "'");
			for (var i=0; i<theItem.all.length; i++) {
				eval('theItem.all(i).style.'+styleProperty+' = '+ "'"+inactivateState+"'");
			}
		}
	}
}