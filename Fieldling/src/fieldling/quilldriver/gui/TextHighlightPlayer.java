/* ***** BEGIN LICENSE BLOCK *****
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
import fieldling.quilldriver.xml.View;
//import org.thdl.util.ThdlDebug;

public class TextHighlightPlayer extends JPanel implements AnnotationPlayer
{
	public JTextComponent text; //public--really?
        
	protected Hashtable hashStart, hashEnd, highlights;
	protected Highlighter highlighter;
	protected Highlighter.HighlightPainter highlightPainter;
	protected JViewport viewport;
	protected JScrollPane scrollPane;
	protected TranscriptView view;
        protected boolean isHighlightInMiddle = true;

	public TextHighlightPlayer(TranscriptView tView, Color highlightColor, String highlightPosition)
	{
        if (!highlightPosition.equals("Middle"))
            isHighlightInMiddle = false;
        
		view = tView;
		text = view.getTextComponent();
/*		text.setEditable(false);
		MouseListener[] mls = (MouseListener[])(text.getListeners(MouseListener.class));
		for (int i=0; i<mls.length; i++)
			text.removeMouseListener(mls[i]);
*/	
		highlights = new Hashtable();
		highlighter = text.getHighlighter();
		setHighlightColor(highlightColor);
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
		scrollPane = new JScrollPane(text);
		
		setLayout(new GridLayout(1,1));
		add(scrollPane);
	}
        public void setHighlightColor(Color highlightColor) {
            highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);
        }
        public Color getHighlightColor() {
            DefaultHighlighter.DefaultHighlightPainter dhp = (DefaultHighlighter.DefaultHighlightPainter)highlightPainter;
            return dhp.getColor();
        }
        public void setHighlightPosition(String highlightPosition) {
            if (highlightPosition.equals("Middle"))
                isHighlightInMiddle = true;
            else
                isHighlightInMiddle = false;
        }
        public String getHighlightPosition() {
            if (isHighlightInMiddle)
                return "Middle";
            else
                return "Bottom";
        }
	public void refresh() {
		//FIXME FIXME FIXME!!!
		if (view instanceof View) {
			View xmlView = (View)view;
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
	public JScrollPane getScroller() {
		return scrollPane;
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
	public void stopAllAnnotations() {
		highlighter.removeAllHighlights();
		highlights.clear();
	}
	private void highlight(String id)
	{
		if (!highlights.containsKey(id)) {
			try
			{
				Integer startInt = (Integer)hashStart.get(id);
				Integer endInt = (Integer)hashEnd.get(id);
				int start = startInt.intValue();
				int end = endInt.intValue();
				Object tag = highlighter.addHighlight(start, end, highlightPainter);
				highlights.put(id, tag);
	
                                if (isHighlightInMiddle) {
					JViewport viewport = (JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, text);
					int halfViewHeight = viewport.getExtentSize().height / 2;
					Rectangle textRectangle = text.modelToView(end);
					int yFactor = textRectangle.y - halfViewHeight;
					if (yFactor > 0) {
						viewport.setViewPosition(new Point(0, yFactor));
						text.repaint();
					}
                                } else { //highlighting should be at bottom
					Rectangle rect = text.modelToView(end);
					text.scrollRectToVisible(rect);
					text.repaint();
                                }
			}
			catch (BadLocationException ble)
			{
				ble.printStackTrace();
				//ThdlDebug.noteIffyCode();
			}
		}
	}
	private void unhighlight(String id)
	{
		if (highlights.containsKey(id))
		{
			highlighter.removeHighlight(highlights.get(id));
			highlights.remove(id);
		}
	}
}
