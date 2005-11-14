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

package fieldling.quilldriver.xml;

import java.awt.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.text.View;
import fieldling.util.*;
import java.awt.print.*;

    /**
     *  Note: this class came from 
     *  Kei G. Gauthier (Suite 301
     *  77 Winsor Street
     *  Ludlow, MA  01056)  
     */ 		
public class DocumentRenderer implements Printable {
	int currentPage = -1;  //Used to keep track of when the page to print changes.
	JTextPane jeditorPane;   //Container to hold the Document. This object be used to lay out the Document for printing.
	double pageEndY = 0;                //Location of the current page end.
	double pageStartY = 0;              //Location of the current page start.
	boolean scaleWidthToFit = true;     //boolean to allow control over whether pages too wide to fit on a page will be scaled.
	PageFormat pFormat;
	PrinterJob pJob;
	protected ResourceBundle messages;
		
	public DocumentRenderer() {
		pFormat = new PageFormat();
		pJob = PrinterJob.getPrinterJob();
		messages = I18n.getResourceBundle();
	}
	
	public void pageDialog() {
		pFormat = pJob.pageDialog(pFormat);
	}
	
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
		double scale = 1.0;
		Graphics2D graphics2D;
		View rootView;
		
		graphics2D = (Graphics2D) graphics;
		jeditorPane.setSize((int) pageFormat.getImageableWidth(),Integer.MAX_VALUE);
		jeditorPane.validate();
		rootView = jeditorPane.getUI().getRootView(jeditorPane);
		if ((scaleWidthToFit) && (jeditorPane.getMinimumSize().getWidth() >
		pageFormat.getImageableWidth())) {
			scale = pageFormat.getImageableWidth()/
			jeditorPane.getMinimumSize().getWidth();
			graphics2D.scale(scale,scale);
		}
		
		graphics2D.setClip((int) (pageFormat.getImageableX()/scale),
				(int) (pageFormat.getImageableY()/scale),
				(int) (pageFormat.getImageableWidth()/scale),
				(int) (pageFormat.getImageableHeight()/scale));
		
		if (pageIndex > currentPage) {
			currentPage = pageIndex;
			pageStartY += pageEndY;
			pageEndY = graphics2D.getClipBounds().getHeight();
		}
		
		graphics2D.translate(graphics2D.getClipBounds().getX(),
				graphics2D.getClipBounds().getY());
		Rectangle allocation = new Rectangle(0,
				(int) -pageStartY,
				(int) (jeditorPane.getMinimumSize().getWidth()),
				(int) (jeditorPane.getPreferredSize().getHeight()));
		
		if (printView(graphics2D,allocation,rootView)) {
			return Printable.PAGE_EXISTS;
		}
		else {
			pageStartY = 0;
			pageEndY = 0;
			currentPage = -1;
			return Printable.NO_SUCH_PAGE;
		}
	}
	
	/*print(JTextPane) prints a StyledDocument contained within a JTextPane.*/
	public void print(JTextPane jedPane) {
		setDocument(jedPane);
		printDialog();
	}
	public void print(StyledDocument styledDocument) {
		setDocument(styledDocument);
		printDialog();
	}
	public void printDialog() {
		//pageDialog();
		if (pJob.printDialog()) {
			pJob.setPrintable(this,pFormat);
			try {
				pJob.print();    
				JOptionPane.showMessageDialog(null,messages.getString("PrintComplete"),"Info",JOptionPane.INFORMATION_MESSAGE);
			}
			catch (PrinterException printerException) {
				pageStartY = 0;
				pageEndY = 0;
				currentPage = -1;
				System.out.println("Error Printing Document");
				JOptionPane.showMessageDialog(null,messages.getString("PrintFail"),"Alert",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public boolean printView(Graphics2D graphics2D, Shape allocation,View view) {
		boolean pageExists = false;
		Rectangle clipRectangle = graphics2D.getClipBounds();
		Shape childAllocation;
		View childView;
		
		if (view.getViewCount() > 0) {
			for (int i = 0; i < view.getViewCount(); i++) {
				childAllocation = view.getChildAllocation(i,allocation);
				if (childAllocation != null) {
					childView = view.getView(i);
					if (printView(graphics2D,childAllocation,childView)) {
						pageExists = true;
					}
				}
			}
		} else {
			if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
				pageExists = true;
				
				if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
						(allocation.intersects(clipRectangle))) {
					view.paint(graphics2D,allocation);
				} else {
					
					if (allocation.getBounds().getY() >= clipRectangle.getY()) {
						if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
							view.paint(graphics2D,allocation);
						} else {
							
							if (allocation.getBounds().getY() < pageEndY) {
								pageEndY = allocation.getBounds().getY();
							}
						}
					}
				}
			}
		}
		return pageExists;
	}
	
	public void setDocument(JTextPane jedPane) {
		jeditorPane = new JTextPane();
		setDocument(jedPane.getStyledDocument());
	}
	
	public void setDocument(StyledDocument document) {
		jeditorPane.setDocument(document);
	}
	
	/*Method to set the current choice of the width scaling option.*/
	public void setScaleWidthToFit(boolean scaleWidth) {
		scaleWidthToFit = scaleWidth;
	}
}
