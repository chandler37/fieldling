/************************************************************************
 
  SignStream is an application for creating and viewing multi-tracked
  annotation transcripts from source video and other media,
  developed primarily for research on ASL and other signed languages.

  Signstream Copyright (C) 1997-2003 Boston University, Dartmouth
  College, and Rutgers the State University of New Jersey.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

  Development of the prototype and versions 1 and 2 was carried out
  by David Greenfield in the Department of Humanities Resources at
  Dartmouth College (Otmar Foelsche, Director).  Other contributors
  included Carol Neidle, Dawn MacLaughlin, and Robert G. Lee
  (Boston University), Benjamin Bahan (Boston University and
  Gallaudet University), and Judy Kegl (Rutgers University).
  Funding was provided by the National Science Foundation (grants
  #SBR-9410562, #SBR-9410562, #IIS-9528985, to Boston University,
  Carol Neidle, PI).

  Development of SignStream version 3 to date has been carried out
  at Boston University by Jason Boyd, with funding from the National
  Science Foundation to Boston University (grants #EIA-9809340,
  #IIS-0012573, and #IIS-0329009), Carol Neidle and Stan Sclaroff,
  co-PIs, with the participation of Robert G. Lee.

  Inquiries about the program should be directed to Prof. Carol Neidle,
  Boston University, Department of Modern Foreign Languages and
  Literatures, 718 Commonwealth Avenue, Boston, MA  02215; 617-353-6218;
  ssdevel@louis-xiv.bu.edu.

**************************************************************************/

/*
 * DefaultTrackEditor.java
 *
 * Created on February 7, 2003, 10:27 AM
 */

package signstream.gui.sequencer;

// import SS2Viewer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.*;

public class DefaultTrackEditor extends AbstractCellEditor
implements TrackEditor
{
  /* protected EventListenerList listenerList = new EventListenerList();
  protected ChangeEvent changeEvent = null; */
  protected Object value;
  
  final Highlighter highlighter = new Highlighter();
  static Palette palette = null;
  
  DefaultTrackEditor()
  {
    // palette = SS2Viewer.samplePalette;
    
    highlighter.listener = new ActionListener()
      {
        public void actionPerformed(ActionEvent a)
        {
          String text = palette.textField.getText();
          if (getCellEditorValue().toString().equals(text))
            fireEditingCanceled();
          
          setCellEditorValue(text);
          fireEditingStopped();
        }
    };
  }
  
  public Component getEditorComponent(TrackItem item, SequencerTrack track)
  {
    setCellEditorValue(item.getValue()); 
    
     palette.textField.setText(item.getValue().toString());
    
    return highlighter;
  }
  
  
  public Object getCellEditorValue()
  {
    return value;
  }
  public void setCellEditorValue(Object obj)
  {
    value = obj;
  }
  
  public boolean isItemEditable(Object data, Object constraints) {
    return true;
  }  
  
  public static class Highlighter extends JComponent
  implements ActionListener
  { 
    
    ActionListener listener = null;
    
    public void addNotify()
    {
      super.addNotify();
      repaint();
      
      if (!palette.isShowing())
        palette.show();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          palette.toFront();
          palette.textField.requestFocus();
        }
      }); 
      
      palette.button.addActionListener(this);
    }
    
    public void removeNotify() {
      super.removeNotify();
      palette.textField.setText("");
      palette.button.removeActionListener(this);
    }
    
    public void actionPerformed(ActionEvent event) {
      listener.actionPerformed(event);
    }
    
    public void paint(Graphics g)
    {
      int w = getWidth();
      int h = getHeight();
      g.setColor(Color.blue);
      g.drawRect(0,0,w-1,h-1);
    }
  }
  
  
  public static class Palette extends JDialog
  {
    JButton button = new JButton("Finish");
    JTextField textField = new JTextField();
    public Palette(Component c)
    {
      super((Frame) SwingUtilities.getWindowAncestor(c),"Sample palette", false);
      getContentPane().setLayout(new GridLayout(0,1));
      textField.setColumns(30);
      getContentPane().add(textField);
      getContentPane().add(button);
      pack();
    }
    
  }
  
  /*
  public void addCellEditorListener(CellEditorListener listener)
  {
    listenerList.add(CellEditorListener.class, listener);
  }
  public void removeCellEditorListener(CellEditorListener listener)
  {
    listenerList.remove(CellEditorListener.class, listener);
  }
   
  public boolean stopCellEditing()
  {
    fireEditingStopped();
    return true;
  }
   
  public void cancelCellEditing()
  {
    fireEditingCanceled();
  }
    protected void fireEditingStopped() {
    Object[] listeners = listenerList.getListenerList();
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == CellEditorListener.class) {
        if (changeEvent == null) changeEvent = new ChangeEvent(this);
        CellEditorListener listener = (CellEditorListener) listeners[i+1];
        listener.editingStopped(changeEvent);
      }
    }
  } // end editingStopped()
   
  protected void fireEditingCanceled() {
    Object[] listeners = listenerList.getListenerList();
    for (int i=listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == CellEditorListener.class) {
        if (changeEvent == null) changeEvent = new ChangeEvent(this);
        CellEditorListener listener = (CellEditorListener) listeners[i+1];
        listener.editingCanceled(changeEvent);
      }
    }
  } // end fireEditingCanceled()
   
   
   
  public boolean isCellEditable(EventObject anEvent)
  {
    return true;
  }
  public boolean shouldSelectCell(EventObject anEvent)
  {
    return false;
  }
   */
}
