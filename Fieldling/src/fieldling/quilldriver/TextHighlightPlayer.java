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

package fieldling.quilldriver;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import fieldling.mediaplayer.AnnotationPlayer;
//import org.thdl.util.ThdlDebug;

public class TextHighlightPlayer extends JPanel implements AnnotationPlayer
{
	protected JTextComponent text;
	protected Hashtable hashStart, hashEnd, highlights;
	protected Highlighter highlighter;
	protected Highlighter.HighlightPainter highlightPainter;
	protected JViewport viewport;
	protected TranscriptView view;

	public TextHighlightPlayer(TranscriptView tView, Color highlightcolor)
	{
		view = tView;
		text = view.getTextComponent();
/*		text.setEditable(false);
		MouseListener[] mls = (MouseListener[])(text.getListeners(MouseListener.class));
		for (int i=0; i<mls.length; i++)
			text.removeMouseListener(mls[i]);
*/	
		highlights = new Hashtable();
		highlighter = text.getHighlighter();
		highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(highlightcolor);
		/*
		text.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				unhighlightAll();
			}
		});		
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				unhighlightAll();
			}
		});
		*/

		hashStart = new Hashtable();
		hashEnd = new Hashtable();
		refresh();
		
		setLayout(new GridLayout(1,1));
		add(new JScrollPane(text));
	}
	public void refresh() {
		//FIXME FIXME FIXME!!!
		if (view instanceof XMLView) {
			XMLView xmlView = (XMLView)view;
			xmlView.refresh(); //start by refreshing the TranscriptView
		}
		hashStart.clear();
		hashEnd.clear();	
		StringTokenizer	stIDS    = new StringTokenizer(view.getIDs(), ",");
		StringTokenizer	stSTARTS = new StringTokenizer(view.getStartOffsets(), ",");
		StringTokenizer	stENDS   = new StringTokenizer(view.getEndOffsets(), ",");
		while ((stIDS.hasMoreTokens()) && (stSTARTS.hasMoreTokens()) && (stENDS.hasMoreTokens())) {
			String sID    = stIDS.nextToken();
			String sStart = stSTARTS.nextToken();
			String sEnd   = stENDS.nextToken();
			try {
				Integer start = new Integer(sStart);
				hashStart.put(sID, start);
			} catch (NumberFormatException err) {
				hashStart.put(sID, new Integer(0));
			}
			try {
				Integer end = new Integer(sEnd);
				hashEnd.put(sID, end);
			} catch (NumberFormatException err) {
				hashEnd.put(sID, new Integer(0));
			}
		}
	}
	public TranscriptView getView() {
		return view;
	}
	public boolean isPlayableAnnotation(String id)
	{
		return hashStart.containsKey(id);
	}
	public void startAnnotation(String id)
	{
		if (isPlayableAnnotation(id))
			highlight(id);
	}
	public void stopAnnotation(String id)
	{
		if (isPlayableAnnotation(id))
			unhighlight(id);
	}
	public void highlight(String id)
	{
		try
		{
			Integer startInt = (Integer)hashStart.get(id);
			Integer endInt = (Integer)hashEnd.get(id);
			int start = startInt.intValue();
			int end = endInt.intValue();
			Object tag = highlighter.addHighlight(start, end, highlightPainter);
			highlights.put(id, tag);

/*			
// scrolls highlighted text to middle of viewport
				JViewport viewport = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, text);
				int halfViewHeight = viewport.getExtentSize().height / 2;
				Rectangle textRectangle = text.modelToView(end);
				int yFactor = textRectangle.y - halfViewHeight;
				if (yFactor > 0) {
					viewport.setViewPosition(new Point(0, yFactor));
					text.repaint();
				}
This often seems disconcerting to the user - so I am removing it. */

// this does scrolling at the bottom of the text window
				Rectangle rect = text.modelToView(end);
				text.scrollRectToVisible(rect);
				text.repaint();

		}
		catch (BadLocationException ble)
		{
			ble.printStackTrace();
			//ThdlDebug.noteIffyCode();
		}
	}
	public void highlight(int start, int end) {
		try {
			highlighter.addHighlight(start, end, highlightPainter);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}
	}
	public void unhighlight(String id)
	{
		if (highlights.containsKey(id))
		{
			highlighter.removeHighlight(highlights.get(id));
			highlights.remove(id);
		}
	}
	public void unhighlightAll() {
		highlighter.removeAllHighlights();
		highlights.clear();
	}
}
