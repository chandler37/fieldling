/*
The contents of this file are subject to the THDL Open Community License
Version 1.0 (the "License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License on the THDL web site 
(http://www.thdl.org/).

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the 
License for the specific terms governing rights and limitations under the 
License. 

The Initial Developer of this software is the Tibetan and Himalayan Digital
Library (THDL). Portions created by the THDL are Copyright 2001 THDL.
All Rights Reserved. 

Contributor(s): ______________________________________.
*/

package fieldling.quilldriver.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.text.JTextComponent;

import fieldling.mediaplayer.PanelPlayer;
//import org.thdl.util.ThdlDebug;

public class TwoWayTextPlayer extends TextHighlightPlayer
{
	protected TreeMap orderedOffsets = null;
	protected PanelPlayer sound = null;
	protected long doubleClickDelay = 300L;
	protected long lastClickTime;
	protected Point lastClickPoint = null;

	public TwoWayTextPlayer(PanelPlayer sp, TranscriptView view, Color highlightColor, String highlightPosition)
	{
		super(view, highlightColor, highlightPosition);
		sound = sp;

		orderedOffsets = new TreeMap();
		for (Enumeration e = hashEnd.keys() ; e.hasMoreElements() ;)
		{
			String id = (String)e.nextElement();
			orderedOffsets.put(hashEnd.get(id), id);
		}

		for (Enumeration e = hashStart.keys() ; e.hasMoreElements() ;)
		{
			String id = (String)e.nextElement();
			orderedOffsets.put(hashStart.get(id), id);
		}

		view.getTextComponent().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				//note - can't use e.getClickCount() here
				//because every double click event is
				//preceded by a single click event, so
				//no easy way to distinguish the two
				try
				{
					final long currentClickTime = e.getWhen();
					final Point currentClickPoint = e.getPoint();
					if (lastClickPoint != null && currentClickPoint.equals(lastClickPoint))
					{ //this could be the second click of a double click
						if (currentClickTime - lastClickTime < doubleClickDelay)
						{ //must be a double-click: play only one line
							lastClickPoint = null; //makes sure second click won't be interpreted as single click also
							int pos = text.viewToModel(currentClickPoint)+1;
							Integer i = new Integer(pos);				
							SortedMap map = orderedOffsets.headMap(i);
							final String id = (String)map.get(map.lastKey());
							highlighter.removeAllHighlights();
							sound.cmd_playS(id);
							return;
						}
					}
					//otherwise, this click can only be single click or first click
					lastClickTime = currentClickTime;
					lastClickPoint = new Point(currentClickPoint);
					final Timer clickTimer = new Timer(true);
					clickTimer.schedule(new TimerTask()
					{
						public void run()
						{
							if (lastClickPoint != null)
							{ //must be a single click: play line onwards
								int pos = text.viewToModel(currentClickPoint)+1;
								Integer i = new Integer(pos);				
								SortedMap map = orderedOffsets.headMap(i);
								final String id = (String)map.get(map.lastKey());
								highlighter.removeAllHighlights();
								sound.cmd_playFrom(id);
							}
							clickTimer.cancel();
						}
					}, doubleClickDelay);
				}
				catch (NoSuchElementException nsee)
				{
					nsee.printStackTrace();
					//ThdlDebug.noteIffyCode();
				}
			}
		});
	}

	public void reset()
	{
		sound.removeAnnotationPlayer(this);
		highlighter.removeAllHighlights();
	}

	public JTextComponent getTextComponent()
	{
		return text;
	}
}
