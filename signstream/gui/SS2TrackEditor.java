/*
 * SS2TrackEditor.java
 *
 * Created on April 24, 2003, 4:36 PM
 */

package signstream.gui;

import signstream.gui.sequencer.*;
import signstream.io.ss2.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Arrays;
import java.util.Vector;

public class SS2TrackEditor extends AbstractCellEditor
implements
TrackEditor,
TrackRenderer {
  
  JComboBox comboBox = null;
  JTextField textField = null;
  // DefaultTrackRenderer.DefaultItemRenderer rendererComp =
  // new DefaultTrackRenderer.DefaultItemRenderer();
  SS2ItemRenderer rendererComp = new SS2ItemRenderer();
  protected Object value;
  
  public SS2TrackEditor() {
    textField = new JTextField() {
      public void addNotify() {
        super.addNotify();
        requestFocus();
      }
    };
  }
  
  public Component getEditorComponent(final TrackItem item, final SequencerTrack track) {
    final SS2Field ss2field = (SS2Field) track.getConstraints();
    
    Object value = item.getValue();
    setCellEditorValue(value);
    
    if (ss2field.type == SS2FieldSpec.TYPE_NON_MANUAL) {
      Vector vector = new Vector(Arrays.asList(ss2field.values));
      if (value == null || value.equals("") || (SS2Value)value == SS2Value.EMPTY) {
        setCellEditorValue(SS2Value.EMPTY);
        vector.insertElementAt(SS2Value.ONSET,0);
        vector.insertElementAt(SS2Value.OFFSET,0);
        vector.insertElementAt(SS2Value.HOLD,0);
      }
      comboBox = new JComboBox(vector) {
        public void addNotify() {
          super.addNotify();
          showPopup();
        }
      };
      comboBox.setEditable(false);
      comboBox.setSelectedItem(getCellEditorValue());
      comboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          SS2Value oldValue = (SS2Value) getCellEditorValue();
          Object selectedItem = comboBox.getSelectedItem();
          
          if (selectedItem instanceof SS2Value) {
            SS2Value ss2value = (SS2Value) selectedItem;
            
            if (ss2value == SS2Value.ONSET || ss2value == SS2Value.HOLD) {
              TrackItem nextItem = track.getItemAt(item.getEndTime(), true);
              if (nextItem != null)
                // HACK:
                item.setEndTime(nextItem.getStartTime());
            }
            if (ss2value == SS2Value.OFFSET || ss2value == SS2Value.HOLD) {
              java.util.Iterator it = track.getItemIterator();
              TrackItem lastItem = null;
              while (it.hasNext()) {
                TrackItem ti = (TrackItem) it.next();
                if (ti == item) break;
                lastItem = ti;
              }
              if (lastItem != null) {
                item.setStartTime(lastItem.getEndTime());
              }
            }
            setCellEditorValue(selectedItem);
            
          }
          else {
            String str = (String) selectedItem;
            /// NEED SS2Field.getValue(String)
          }
          
          stopCellEditing();
        }
      });
      return comboBox;
    } else {
      textField.setText(value.toString());
      textField.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          String oldText = (String) getCellEditorValue();
          String text = textField.getText();
          if (!(text.equals(oldText) ) ) {
            setCellEditorValue(text);
          }
          stopCellEditing();
        }
      });
      return textField;
    }
  }
  
  // SHOULD have AbstractTrackHandler, AbstractTrackEditor, AbstractTrackRenderer
  public Object getCellEditorValue() {
    return value;
  }
  public void setCellEditorValue(Object obj) {
    value = obj;
  }
  
  public int getHeight() {
    return UIManager.getInt("SequencerTrack.height");
  }
  
  public void paint(Graphics g, SequencerTrack track,
  int startTime, int endTime, int pixPerSecond, java.util.List selectedItems) {
    
    int left = startTime * pixPerSecond / 1000;
    int right = endTime * pixPerSecond / 1000;
    int height = getHeight();
    
    paintEmptyTrack(g, left, right, height);
    
    TrackItem itemToRender = track.getItemAt(startTime,true);
    TrackItem lastItem = null;
    
    while (itemToRender != null) {
      TrackItem nextItem = track.getNextItem(itemToRender);
      
      int s  = itemToRender.getStartTime();
      int e  = itemToRender.getEndTime();
      int itemDuration = itemToRender.getDuration();
      int l = s * pixPerSecond / 1000;
      int w = itemDuration * pixPerSecond / 1000;
      int timeWidth = w;
      boolean selected =  selectedItems.contains(itemToRender);
      
      Object value = itemToRender.getValue();
      
      rendererComp.chains[0] = 
         lastItem != null && 
         lastItem.getEndTime() == itemToRender.getStartTime() &&
         ((lastItem.getValue() instanceof SS2Value) && 
            !((SS2Value)lastItem.getValue()).isSpecialValue());
      rendererComp.chains[1] = 
         nextItem != null && 
         nextItem.getStartTime() == itemToRender.getEndTime() &&
         ((nextItem.getValue() instanceof SS2Value) && 
            !((SS2Value)nextItem.getValue()).isSpecialValue());
      
      if (nextItem != null) {
        w += (nextItem.getStartTime() - e) * pixPerSecond / 1000;
      } else {
        w += (track.getDuration() - e) * pixPerSecond / 1000;
      }
      
      Graphics cg = g.create(l, 0, w, height);
      
      if ( (value instanceof SS2Value) && ((SS2Value)value).isSpecialValue() ) {
        Color c = selected ? Color.red : ((SS2Field)track.getConstraints()).color;
        
        if (!selected) {
          c = new Color(
          (int) ( ( (float) (c.getRed())   +255 ) *0.4f),
          (int) ( ( (float) (c.getGreen()) +255 ) *0.4f),
          (int) ( ( (float) (c.getBlue())  +255 ) *0.4f));
        }
        cg.setColor(c);
        
        int h = height/2;
        if (value == SS2Value.HOLD) {
          for (int i=0; i<timeWidth-1; i+=5) {
            cg.drawLine(i,h,Math.min(i+3, timeWidth-1), h);
          }
        } else {
          cg.drawLine(0,h,timeWidth-1,h);
        }
        
        // draw arrow heads for special values
        if (lastItem == null || lastItem.getEndTime() < s) {
          cg.drawLine(0,h, 3, h-3);
          cg.drawLine(0,h, 3, h+3);
        }
        if (nextItem == null || nextItem.getStartTime() > e) {
          cg.drawLine(timeWidth-1,h, timeWidth-4, h-3);
          cg.drawLine(timeWidth-1,h, timeWidth-4, h+3);
        }
      } else {
        
        rendererComp.durationWidth = timeWidth;
        rendererComp.setSize(w, height);
        rendererComp.text = value.toString();
        rendererComp.selected = selected;
        rendererComp.setForeground(((SS2Field)track.getConstraints()).color);
        rendererComp.paint(cg);
      }
      
      cg.dispose();
      lastItem = itemToRender;
      itemToRender = nextItem;
    }
  }
  
  void paintEmptyTrack(Graphics g, int left, int right, int height) {
    Color backgroundColor = UIManager.getColor("SequencerTrack.backgroundColor");
    Color outlineColor = UIManager.getColor("SequencerTrack.outlineColor");
    
    Color oldColor = g.getColor();
    g.setColor( backgroundColor );
    g.fillRect( left, 0, right+2, height  );
    g.setColor( outlineColor );
    g.drawLine(left,0, right+1, 0);
    g.drawLine(left,height-1, right+1, height-1);
    g.setColor(oldColor);
  }
  
  StringBuffer tooltipBuffer = new StringBuffer();
  public String getTooltip(TrackItem item) {
    if (item == null) return null;
    Object value = item.getValue(); 
    tooltipBuffer.setLength(0);
    tooltipBuffer.append('"');
    tooltipBuffer.append(value instanceof SS2Value ? ((SS2Value)value).name : value);
    tooltipBuffer.append('"');
    tooltipBuffer.append(' ');
    tooltipBuffer.append((float)(item.getDuration()) / 1000.0f);
    tooltipBuffer.append(" sec");
    return tooltipBuffer.toString();
  }
  
  public boolean isItemEditable(Object data, Object constraints) {
    if (data instanceof SS2Value && ((SS2Value)data).isSpecialValue()) {
       return false;
    }
    return true;
  }
  
  public Color getTrackLabelColor(SequencerTrack track) {
    return ((SS2Field)track.getConstraints()).color;
  }
  
  
  public static class SS2ItemRenderer extends JComponent {
    
    public String text;
    public int durationWidth = 0;
    public boolean selected = false;
    boolean[] chains = new boolean[2];
    
    public SS2ItemRenderer() {
      setFont(new Font("SANS-SERIF", Font.PLAIN, 11));
      setBorder(null);
      setForeground(Color.black);
      setOpaque(false); // should set background to track background color
    }
    
    public void paint(Graphics g) {
      int h = SS2ItemRenderer.this.getHeight();
      int w = getWidth();
      
      // paint underline
      g.setColor(selected ? Color.red : getForeground());
      g.drawLine(0, h-4, durationWidth-1, h-4);
      if (selected) {
        // paint time drag handles
        g.fillRect(0,   h-4, 4  , h  );
        g.fillRect(durationWidth-4, h-4, 4, h  );
      }

      // paint chained item locks
      if (chains[0]) {
        g.setColor(Color.white);
        g.fillRect(-1,h-5, 3,3);
        g.setColor(getForeground());
        g.drawRect(-1,h-5, 3,3);
      }
      if (chains[1]) {
        g.setColor(Color.white);
        g.fillRect(durationWidth-2,h-5, 3,3);
        g.setColor(getForeground());
        g.drawRect(durationWidth-2,h-5, 3,3);
      }
          
      // paint item label
      g.setColor(getForeground());
      g.setFont(getFont());
      g.drawString(text, 1, h-6);
    }
  } // end SS2ItemRenderer
  
  /** Custom text field that obscures the background with the same
   color, has no border, and attempts to match the font metrics of the
   equivalent renderer -- note that this is done thru redundant coding and isn't
   very robust. */
  class InvisibleTextField extends JTextField {
    InvisibleTextField() {
      super();
      setOpaque(true);
      setBackground(new Color(0xFF, 0xFF, 0xDD));
      setBorder(new javax.swing.border.EmptyBorder(getInsets()));
      // setHorizontalAlignment(RIGHT);
      Font defaultFont = getFont();
      setFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
      
      getDocument().addDocumentListener(new DocumentListener() {
        public void removeUpdate(DocumentEvent e) {
          resize();
        }
        public void insertUpdate(DocumentEvent e) {
          resize();
        }
        public void changedUpdate(DocumentEvent e) {}
        
        public void resize() {
          java.awt.geom.Rectangle2D rect2D = getFont().getStringBounds(getText(),
          new java.awt.font.FontRenderContext(null, false, false));
          Rectangle newBounds = rect2D.getBounds();
          Insets insets = getInsets();
          newBounds.width += insets.left + insets.right + 8;
          Rectangle bounds = getBounds();
          int widthChange = newBounds.width  - bounds.width;
          bounds.width += widthChange;
          bounds.x -= widthChange;
          setBounds(bounds);
        }
      });
    }
    
    /**
     Overrides default to turn off anti-aliasing and obscure background with
     same color as background.
     */
    protected void paintComponent(Graphics g) {
      // g = g.create();
      Graphics2D g2d = (Graphics2D) g;
      Insets insets = getInsets();
      //Color oldBackground = g2d.getBackground();
      //RenderingHints oldHints = g2d.getRenderingHints();
      g2d.setColor(getBackground());
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      g2d.fillRect(insets.left, insets.top,
      getWidth() - insets.left,
      InvisibleTextField.this.getHeight() - insets.top - insets.bottom - 3);
      super.paintComponent(g);
      //g2d.setColor(oldBackground);
      //g2d.setRenderingHints(oldHints);
      // g.dispose();
    }
    
  } // end InvisibleTextField
}
