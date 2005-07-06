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

package fieldling.quilldriver;

import java.io.IOException;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

public class XMLEditor {
	private EventListenerList listenerList = new EventListenerList();
    private org.w3c.dom.Document xml;
	private JTextPane pane;
	private StyledDocument doc;

	//listeners
	private DocumentListener docListener;
	private MouseMotionAdapter mouseMotionListener;
	private MouseAdapter mouseListener;
	private FocusListener focusListener;
	private CaretListener editabilityTracker;

	private Map startOffsets, endOffsets;
	private final float indentIncrement = 15.0F;
	private final Color tagColor = Color.magenta;
	private final Color attColor = Color.pink;
	private final Color textColor = Color.darkGray;
	private Cursor textCursor;
	private Cursor defaultCursor;
	private boolean isEditing = false;
	private Object editingNode = null;
	private boolean hasChanged = false;
	private Hashtable actions;
	private XMLTagInfo tagInfo;

    public XMLEditor(org.w3c.dom.Document xmlDoc, JTextPane textPane, XMLTagInfo tagInfo) {
		xml = xmlDoc;
		pane = textPane;
		this.tagInfo = tagInfo;
		startOffsets = new HashMap();
		endOffsets = new HashMap();
        //For some reason uppercase color names, e.g. Color.CYAN and Color.RED,
        //are not recognized on Mac OS X, for Java 1.3.1 at least!!
		pane.setSelectionColor(Color.cyan);
		pane.setSelectedTextColor(Color.red);
		doc = pane.getStyledDocument();
		textCursor = new Cursor(Cursor.TEXT_CURSOR);
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		initializeListeners();
		render();
		installKeymap();
	}
	public void setTagInfo(XMLTagInfo tagInfo) {
		this.tagInfo = tagInfo;
		render();
	}
    public XMLTagInfo getTagInfo() {
        return tagInfo;
    }
    public Map getStartOffsets() {
        return startOffsets;
    }
    public Map getEndOffsets() {
        return endOffsets;
    }
	private void installKeymap() {
		Action selForwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.getCaretPosition();
				Object node = getNodeForOffset(offset);
				int last = (((Position)endOffsets.get(node)).getOffset());
				if (node instanceof org.w3c.dom.Attr) last--;
				if (offset < last) p.getCaret().moveDot(offset++);
			}
		};
		Action selBackwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.getCaretPosition();
				int first = (((Position)startOffsets.get(getNodeForOffset(offset))).getOffset());
				if (offset > first) p.getCaret().moveDot(offset--);
			}
		};
		Action selectToNodeEndAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				Object node = getNodeForOffset(p.getCaret().getMark());
				if (node != null) {
					int last = (((Position)endOffsets.get(node)).getOffset());
					if (node instanceof org.w3c.dom.Attr) last--;
					p.getCaret().moveDot(last);
				}
			}
		};
		Action selectToNodeStartAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.getCaretPosition();
				Object node = getNodeForOffset(p.getCaret().getMark());
				if (node != null) {
					int first = (((Position)startOffsets.get(node)).getOffset());
					p.getCaret().moveDot(first);
				}
			}
		};
		Action loseFocusAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				p.transferFocus(); //moves focus to next component
			}
		};
		createActionTable(pane);
		Keymap keymap = pane.addKeymap("QDBindings", pane.getKeymap());

		//backspace
		Action deletePrevAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.getCaretPosition();
				int first = (((Position)startOffsets.get(getNodeForOffset(offset))).getOffset());
				if (offset > first) {
					StyledDocument d = p.getStyledDocument();
					try {
						d.remove(offset-1, 1);
					} catch (BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			}
		};
		KeyStroke backSpace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		keymap.addActionForKeyStroke(backSpace, deletePrevAction);

		/* The Java bug database has several related bugs concerning the treatment
		of backspace. Here I adopt solution based on fix of bug 4402080:
		Evaluation  The text components now key off of KEY_TYPED with a keyChar == 8 to do the
		deletion. The motivation for this can be found in bug 4256901.
		xxxxx@xxxxx 2001-01-05 */
		pane.addKeyListener(new java.awt.event.KeyAdapter() {
			/* NOT ACTUALLY NEEDED, AT LEAST FOR WINDOWS
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_DELETE) {//LOGGINGSystem.out.println("delete released"); kev.consume();}
			}*/
			public void keyTyped(KeyEvent kev) {
				if (kev.getKeyChar() == 8) kev.consume();
				/* Above is equivalent to:
					if(kev.paramString().indexOf("Backspace") != -1) kev.consume();	*/
				else if (kev.getKeyCode() == KeyEvent.VK_TAB) kev.consume();
			}
		});

		//delete
		Action deleteNextAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.getCaretPosition();
				int last = (((Position)endOffsets.get(getNodeForOffset(offset))).getOffset());
				if (offset < last) {
					StyledDocument d = p.getStyledDocument();
					try {
						d.remove(offset, 1);
					} catch (BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			}
		};
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		keymap.addActionForKeyStroke(delete, deleteNextAction);

		KeyStroke[] selEndLineKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionEndLineAction));
		if (selEndLineKeys != null)
			for (int i=0; i<selEndLineKeys.length; i++)
				keymap.addActionForKeyStroke(selEndLineKeys[i], selectToNodeEndAction);
		KeyStroke[] selEndParaKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionEndParagraphAction));
		if (selEndParaKeys != null)
			for (int i=0; i<selEndParaKeys.length; i++)
				keymap.addActionForKeyStroke(selEndParaKeys[i], selectToNodeEndAction);
		KeyStroke[] selBegLineKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionBeginLineAction));
		if (selBegLineKeys != null)
			for (int i=0; i<selBegLineKeys.length; i++)
				keymap.addActionForKeyStroke(selBegLineKeys[i], selectToNodeStartAction);
		KeyStroke[] selBegParaKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionBeginParagraphAction));
		if (selBegParaKeys != null)
			for (int i=0; i<selBegParaKeys.length; i++)
				keymap.addActionForKeyStroke(selBegParaKeys[i], selectToNodeStartAction);

		//home
		Action begNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				Object node = getNodeForOffset(p.getCaretPosition());
				p.setCaretPosition(((Position)startOffsets.get(node)).getOffset());
			}
		};
		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), begNodeAction);

		//end
		Action endNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				Object node = getNodeForOffset(p.getCaretPosition());
				p.setCaretPosition(((Position)endOffsets.get(node)).getOffset());
			}
		};
		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), endNodeAction);

		//select all (Ctrl-A)
		Action selectNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				Object node = getNodeForOffset(p.getCaretPosition());
				if (node != null) {
					p.setSelectionStart(((Position)startOffsets.get(node)).getOffset());
					int end = ((Position)endOffsets.get(node)).getOffset();
					if (node instanceof org.w3c.dom.Text) p.setSelectionEnd(end);
					else if (node instanceof org.w3c.dom.Attr) p.setSelectionEnd(end-1);
				}
			}
		};
		keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), selectNodeAction);

		//cut (Ctrl-X)
		Action cutAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int j = p.getSelectionStart();
				int k = p.getSelectionEnd();
				if (editingNode != getNodeForOffset(j)) {
					fireEndEditEvent();
					fireStartEditEvent(getNodeForOffset(j));
				}
				if (getNodeForOffset(j) == getNodeForOffset(k) && isEditable(getNodeForOffset(j))) {
					p.cut();
				}
			}
		};
		KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK);
		keymap.addActionForKeyStroke(cut, cutAction);

		//copy (Ctrl-C)
		Action copyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int j = p.getSelectionStart();
				int k = p.getSelectionEnd();
				if (editingNode != getNodeForOffset(j)) {
					fireEndEditEvent();
					fireStartEditEvent(getNodeForOffset(j));
				}
				if (getNodeForOffset(j) == getNodeForOffset(k) && isEditable(getNodeForOffset(j))) {
					p.copy();
				}
			}
		};
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK);
		keymap.addActionForKeyStroke(copy, copyAction);

		//paste (Ctrl-V)
		Action pasteAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int j = p.getSelectionStart();
				int k = p.getSelectionEnd();
				Object node = getNodeForOffset(j);
				if (editingNode != node) {
					fireEndEditEvent();
					fireStartEditEvent(node);
				}
				if (node == getNodeForOffset(k) && isEditable(node)) {
					//only works for element text, not for attribute values
					@TIBETAN@boolean isTibetan = false;
					@TIBETAN@if (node instanceof org.w3c.dom.Text) {
						@TIBETAN@org.w3c.dom.Text nodeText = (org.w3c.dom.Text)node;
						@TIBETAN@isTibetan = XMLEditor.this.tagInfo.areTagContentsTibetan(nodeText.getParentNode().getNodeName());
					@TIBETAN@}

					//if Tibetan, then use DuffPane's build-in RTF copy and paste support, which means
					//that it will be possible to paste non-Tibetan into Tibetan field
					@TIBETAN@if (isTibetan) p.paste();
					@TIBETAN@else { // use String flavor of system clipboard for all else
						@TIBETAN@Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
						@TIBETAN@try {
						    @TIBETAN@if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							@TIBETAN@String text = (String)t.getTransferData(DataFlavor.stringFlavor);
							@TIBETAN@p.replaceSelection(text);
						    @TIBETAN@}
						@TIBETAN@} catch (UnsupportedFlavorException ufe) {
						@TIBETAN@	ufe.printStackTrace();
						@TIBETAN@} catch (IOException ioe) {
						@TIBETAN@	ioe.printStackTrace();
						@TIBETAN@}
					@TIBETAN@}
					@UNICODE@p.paste();
					SimpleAttributeSet sas = new SimpleAttributeSet();
					sas.addAttribute("xmlnode", node);
					p.getStyledDocument().setCharacterAttributes(getStartOffsetForNode(node), getEndOffsetForNode(node)-getStartOffsetForNode(node), sas, false);
				}
			}
		};
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK);
		keymap.addActionForKeyStroke(paste, pasteAction);

		//left arrow key
		Action backwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int prePos = p.getCaretPosition();
				int newPos = prePos-1;
				while (newPos>-1 && !isEditable(newPos)) newPos--;
				if (newPos != -1) {
					if (getNodeForOffset(prePos) != getNodeForOffset(newPos)) {
						fireEndEditEvent();
						fireStartEditEvent(getNodeForOffset(newPos));
					}
					p.setCaretPosition(newPos);
				}
			}
		};
		KeyStroke back = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		keymap.addActionForKeyStroke(back, backwardAction);

		//right arrow key
		Action forwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int prePos = p.getCaretPosition();
				int newPos = prePos+1;
				while (newPos<p.getDocument().getLength() && !isEditable(newPos)) newPos++;
				if (newPos != p.getDocument().getLength()) {
					if (getNodeForOffset(prePos) != getNodeForOffset(newPos)) {
						fireEndEditEvent();
						fireStartEditEvent(getNodeForOffset(newPos));
					}
					p.setCaretPosition(newPos);
				}
			}
		};
		KeyStroke forward = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		keymap.addActionForKeyStroke(forward, forwardAction);

		//previous node: up arrow
		Action prevNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int prePos = p.getCaretPosition();
				Object node = getNodeForOffset(prePos);
				int i = prePos-1;
				while (i>-1 && isEditable(i) && getNodeForOffset(i) == node) i--;
				while (i>-1 && !isEditable(i)) i--;
				node = getNodeForOffset(i);
				if (isEditing) fireEndEditEvent();
				fireStartEditEvent(getNodeForOffset(i));
				p.setCaretPosition(i);
			}
		};
		KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		keymap.addActionForKeyStroke(up, prevNodeAction);

		//next node: down arrow, TAB, and ENTER
		Action nextNodeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int prePos = p.getCaretPosition();
				Object node = getNodeForOffset(prePos);
				int i = prePos+1;
				while (i<p.getDocument().getLength() && isEditable(i) && getNodeForOffset(i) == node) i++;
				while (i<p.getDocument().getLength() && !isEditable(i)) i++;
				node = getNodeForOffset(i);
				while (i<p.getDocument().getLength() && isEditable(i) && getNodeForOffset(i) == node) i++;
				if (isEditing) fireEndEditEvent();
				i--;
				fireStartEditEvent(getNodeForOffset(i));
				p.setCaretPosition(i);
			}
		};
		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		keymap.addActionForKeyStroke(down, nextNodeAction);
		KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		if (tabKey != null) keymap.addActionForKeyStroke(tabKey, nextNodeAction);
		KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		if (enterKey != null) keymap.addActionForKeyStroke(enterKey, nextNodeAction);

		KeyStroke[] forwardKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.forwardAction));
		if (forwardKeys != null)
			for (int i=0; i<forwardKeys.length; i++)
				keymap.addActionForKeyStroke(forwardKeys[i], forwardAction);
		KeyStroke[] selForwardKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionForwardAction));
		if (selForwardKeys != null)
			for (int i=0; i<selForwardKeys.length; i++)
				keymap.addActionForKeyStroke(selForwardKeys[i], selForwardAction);
		KeyStroke[] selBackKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectionBackwardAction));
		if (selBackKeys != null)
			for (int i=0; i<selBackKeys.length; i++)
				keymap.addActionForKeyStroke(selBackKeys[i], selBackwardAction);

		//escape
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		if (escapeKey != null) keymap.addActionForKeyStroke(escapeKey, loseFocusAction);

		//default keypress
		final Action parentDefault = keymap.getDefaultAction();
		Action thisDefault = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) ||
				((e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK) ||
				((e.getModifiers() & ActionEvent.META_MASK) == ActionEvent.META_MASK)) return;

				if (e.getActionCommand() != null) {
					int p = pane.getCaretPosition();
					if (editingNode != getNodeForOffset(p)) {
						fireEndEditEvent();
						fireStartEditEvent(getNodeForOffset(p));
					}
					Object xmlNode = pane.getCharacterAttributes().getAttribute("xmlnode");
					SimpleAttributeSet sas = new SimpleAttributeSet();
					sas.addAttribute("xmlnode", xmlNode);
					parentDefault.actionPerformed(e);
					pane.getStyledDocument().setCharacterAttributes(pane.getCaretPosition()-1, 1, sas, false);
				}
			}
		};
		keymap.setDefaultAction(thisDefault);
		pane.setKeymap(keymap);
/*
Actions that still need to be defined:
Fields inherited from class javax.swing.text.DefaultEditorKit
beepAction, beginAction, beginWordAction, copyAction, cutAction,
defaultKeyTypedAction,
downAction, endAction, EndOfLineStringProperty,
endWordAction, insertBreakAction, insertContentAction,
nextWordAction, pageDownAction, pageUpAction,
pasteAction, previousWordAction, readOnlyAction
selectionBeginAction, selectionBeginWordAction,
selectionDownAction, selectionEndAction, selectionEndWordAction,
selectionNextWordAction, selectionPreviousWordAction,
selectionUpAction, selectWordAction,
upAction, writableAction
*/
	}
	public void setEditabilityTracker(boolean bool) {
		if (bool) {
			CaretListener[] caretListeners = (CaretListener[])pane.getListeners(CaretListener.class);
			boolean alreadyInstalled = false;
			for (int i=0; i<caretListeners.length; i++) {
				if (caretListeners[i] == editabilityTracker) {
					alreadyInstalled = true;
					break;
				}
			}
			if (!alreadyInstalled) {
				int p = pane.getCaretPosition();
				int q;
				if (pane.getDocument().getLength() == 0)
					q=0;
				else {
					if (p>0) q=p-1;
					else q=p+1;
				}
				pane.setCaretPosition(q);
				pane.addCaretListener(editabilityTracker); //shouldn't do if already installed
				pane.setCaretPosition(p);
			}
		}
		else pane.removeCaretListener(editabilityTracker);
	}
	private void activateListeners() {
		doc.addDocumentListener(docListener);
		pane.addMouseMotionListener(mouseMotionListener);
		pane.addMouseListener(mouseListener);
		pane.addFocusListener(focusListener);
	}
	private void deactivateListeners() {
		setEditabilityTracker(false);
		doc.removeDocumentListener(docListener);
		pane.removeMouseMotionListener(mouseMotionListener);
		pane.removeMouseListener(mouseListener);
		pane.removeFocusListener(focusListener);
	}
	private void initializeListeners() {
		MouseListener[] listeners = (MouseListener[])pane.getListeners(MouseListener.class);
		for (int i=0; i<listeners.length; i++) pane.removeMouseListener(listeners[i]);
		docListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				hasChanged = true;
			}
			public void insertUpdate(DocumentEvent e) {
				hasChanged = true;
				if (getStartOffsetForNode(editingNode) > e.getOffset()) {
					javax.swing.text.Document d = e.getDocument();
					try {
						startOffsets.put(editingNode, d.createPosition(e.getOffset()));
					} catch (BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			}
			public void removeUpdate(DocumentEvent e) {
				hasChanged = true;
			}
		};
		mouseMotionListener = new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.viewToModel(e.getPoint());
				if (isEditable(offset)) p.setCursor(textCursor);
				else p.setCursor(defaultCursor);
			}
		};
		mouseListener = new MouseAdapter() {
			/* Here's when these methods get called:
				mousePressed: always
				mouseClicked: only when you don't move the mouse in between pressing and releasing
				mouseReleased: always
			This is crucial info for handling cut, copy and paste, since making a selection
			involves pressing, moving, and then releasing the mouse. */

			public void mouseClicked(MouseEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.viewToModel(e.getPoint());
				if (!isEditable(offset)) {
//LOGGING//LOGGINGSystem.out.println("clicked on uneditable " + String.valueOf(offset));
					Object node = getNodeForOffset(offset);
					if (isEditing) fireEndEditEvent();
					if (node != null) fireCantEditEvent(getNodeForOffset(offset));
				}
			}
			public void mousePressed(MouseEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int offset = p.viewToModel(e.getPoint());
				if (isEditable(offset)) {
//LOGGING//LOGGINGSystem.out.println("clicked on editable " + String.valueOf(offset));
					p.requestFocus();
					if (isEditing) fireEndEditEvent();
					fireStartEditEvent(getNodeForOffset(offset));
					p.getCaret().setDot(offset);
				}
			}
			public void mouseReleased(MouseEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				int j = p.getCaretPosition();
				if (editingNode != getNodeForOffset(j)) {
					fireEndEditEvent();
					fireStartEditEvent(getNodeForOffset(j));
				}
			}
		};
		focusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				if (isEditable(p.getCaretPosition()))
					fireStartEditEvent(getNodeForOffset(p.getCaretPosition()));
			}
			public void focusLost(FocusEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				if (isEditing) fireEndEditEvent();
			}
		};
		editabilityTracker = new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				int dot = e.getDot();
				if (getNodeForOffset(dot) != getNodeForOffset(e.getMark()))
					pane.getCaret().setDot(dot);
				if (!isEditable(dot)) {
					while (!isEditable(dot) && dot<pane.getDocument().getLength()) dot++;
					if (dot == pane.getDocument().getLength()) {
						dot = e.getDot();
						do {
							dot--;
						} while (!isEditable(dot) && dot>-1);
						if (dot == -1) return; //what to do? there's nothing to edit in this pane
					}
					if (isEditable(dot)) {
						pane.getCaret().setDot(dot);
						if (getNodeForOffset(dot) != null) fireStartEditEvent(getNodeForOffset(dot));
					}
				} else if (editingNode == null) //need to start editing because cursor happens to be on an editable node
					fireStartEditEvent(getNodeForOffset(dot));
			}
		};
	}
	private void createActionTable(JTextComponent textComponent) {
	    actions = new Hashtable();
	    Action[] actionsArray = textComponent.getActions();
	    for (int i = 0; i < actionsArray.length; i++) {
		Action a = actionsArray[i];
		actions.put(a.getValue(Action.NAME), a);
	    }
	}
	private Action getActionByName(String name) {
	    return (Action)(actions.get(name));
	}
	public void updateNode(Object node) {
		//LOGGINGSystem.out.println("updating: " + node.toString());
		if (node == null)
			return;
		try {
			if (node instanceof org.w3c.dom.Text) {
				int p1 = ((Position)startOffsets.get(node)).getOffset();
				int p2 = ((Position)endOffsets.get(node)).getOffset();

				String val;

				@TIBETAN@org.w3c.dom.Text t = (org.w3c.dom.Text)node;
				@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.areTagContentsTibetan(t.getParentNode().getNodeName())) {
					@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
                    @TIBETAN@boolean[] noSuchWylie = new boolean[1];
					@TIBETAN@val = duff.getTibDoc().getWylie(p1, p2, noSuchWylie);
				@TIBETAN@} else val = pane.getDocument().getText(p1, p2-p1).trim();
				@UNICODE@val = pane.getDocument().getText(p1, p2-p1).trim();
				if (val.length()==0) val=new String(" ");
				org.w3c.dom.Text text = (org.w3c.dom.Text)node;
				text.setNodeValue(val);
			} else if (node instanceof org.w3c.dom.Attr) {
				int p1 = ((Position)startOffsets.get(node)).getOffset();
				int p2 = ((Position)endOffsets.get(node)).getOffset()-1; //remove right quote
				String val = pane.getDocument().getText(p1, p2-p1).trim();
				org.w3c.dom.Attr att = (org.w3c.dom.Attr)node;
				att.setNodeValue(val);
			}
			//LOGGINGSystem.out.println("updated: " + node.toString());
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}
	}
	interface NodeEditListener extends EventListener {
		public void nodeEditPerformed(NodeEditEvent ned);
	}
	public void addNodeEditListener(NodeEditListener ned) {
		listenerList.add(NodeEditListener.class, ned);
	}
	public void removeNodeEditListener(NodeEditListener ned) {
		listenerList.remove(NodeEditListener.class, ned);
	}
	class NodeEditEvent extends EventObject {
		Object node;

		NodeEditEvent(Object node) {
			super(node);
			this.node = node;
		}
		public Object getNode() {
			return node;
		}
	}
	class StartEditEvent extends NodeEditEvent {
		StartEditEvent(Object node) {
			super(node);
		}
	}
	class EndEditEvent extends NodeEditEvent {
		public EndEditEvent(Object node) {
			super(node);
		}
		public boolean hasBeenEdited() {
			return hasChanged;
		}
	}
	class CantEditEvent extends NodeEditEvent {
		public CantEditEvent(Object node) {
			super(node);
		}
	}
	public void fireStartEditEvent(Object node) {
		@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && node instanceof org.w3c.dom.Text) {
			@TIBETAN@org.w3c.dom.Text t = (org.w3c.dom.Text)node;
			@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
			@TIBETAN@if (tagInfo.areTagContentsTibetan(t.getParentNode().getNodeName())) {
				@TIBETAN@if (duff.isRomanMode()) duff.toggleLanguage();
			@TIBETAN@} else if (!duff.isRomanMode()) duff.toggleLanguage();
		@TIBETAN@}

		//see javadocs on EventListenerList for how following array is structured
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==NodeEditListener.class)
				((NodeEditListener)listeners[i+1]).nodeEditPerformed(new StartEditEvent(node));
		}

		isEditing = true;
		editingNode = node;
		hasChanged = false;
	}
	public void fireEndEditEvent() {
		if (!isEditing) return;

		@TIBETAN@if (pane instanceof org.thdl.tib.input.DuffPane && editingNode instanceof org.w3c.dom.Text) {
			@TIBETAN@org.w3c.dom.Text t = (org.w3c.dom.Text)editingNode;
			@TIBETAN@org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
			@TIBETAN@if (!duff.isRomanMode()) duff.toggleLanguage();
		@TIBETAN@}

		//see javadocs on EventListenerList for how following array is structured
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==NodeEditListener.class)
				((NodeEditListener)listeners[i+1]).nodeEditPerformed(new EndEditEvent(editingNode));
		}
		if (hasChanged) { //FIXME allows text on screen to be changed even when node itself cannot be changed!
			if (editingNode instanceof org.w3c.dom.Text) {
				org.w3c.dom.Text t = (org.w3c.dom.Text)editingNode;
                if (tagInfo.isTagEditable(t.getParentNode().getNodeName()))
                        updateNode(editingNode);
			} else if (editingNode instanceof org.w3c.dom.Attr) {
				org.w3c.dom.Attr a = (org.w3c.dom.Attr)editingNode;
				if (tagInfo.isTagEditable(a.getOwnerElement().getNodeName()))
					updateNode(editingNode);
			}
		}
		isEditing = false;
		editingNode = null;
		hasChanged = false;
	}
	public void fireCantEditEvent(Object node) {
		//see javadocs on EventListenerList for how following array is structured
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==NodeEditListener.class)
				((NodeEditListener)listeners[i+1]).nodeEditPerformed(new CantEditEvent(node));
		}
	}
	public void setXMLDocument(org.w3c.dom.Document d, String doctype_elementName, String doctype_systemID) {
		xml = d;
		render();
	}
    public void render() {
		//LOGGINGSystem.out.println("rendering xml");
		boolean makeUneditable = false;
		if (!isEditable()) { //if you don't make the pane editable while rendering, then icons cannot be inserted
			makeUneditable = true;
			setEditable(true);
		}
		int len = doc.getLength();
		try {
			if (len > 0) {
				deactivateListeners();
				doc.remove(0, len);
			}
			doc.insertString(0, "\n", null);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
		}
		startOffsets.clear();
		endOffsets.clear();

        org.w3c.dom.Element root = xml.getDocumentElement();
		XMLRenderer.renderElement(root, pane, doc.getLength(), 0.0F, tagInfo, startOffsets, endOffsets);
		
        SimpleAttributeSet eColor = new SimpleAttributeSet();
		eColor.addAttribute("xmlnode", root);
		doc.setParagraphAttributes(doc.getLength(), 1, eColor, false);
		fixOffsets();
		activateListeners();
		pane.setCaretPosition(0);
		setEditabilityTracker(true);
		if (makeUneditable) setEditable(false);
	}
	public void fixOffsets() {
		//replace Integer values in startOffsets and endOffsets with Positions
		Set startKeys = startOffsets.keySet();
		Iterator iter = startKeys.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object obj = startOffsets.get(key);
			if (obj instanceof Integer) try {
				Integer val = (Integer)obj;
				startOffsets.put(key, doc.createPosition(val.intValue()));
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
		Set endKeys = endOffsets.keySet();
		iter = endKeys.iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			Object obj = endOffsets.get(key);
			if (obj instanceof Integer) try {
				Integer val = (Integer)obj;
				endOffsets.put(key, doc.createPosition(val.intValue()));
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}
	}
	public void removeNode(Object node) {
		if (startOffsets.containsKey(node)) { //note: should recursively eliminate all sub-nodes too!!
			startOffsets.remove(node);
			endOffsets.remove(node);
		}
	}
	public Object getNodeForOffset(int offset) {
		AttributeSet attSet = doc.getCharacterElement(offset).getAttributes();
		return attSet.getAttribute("xmlnode");
	}
	public int getStartOffsetForNode(Object node) {
		Position pos = (Position)startOffsets.get(node);
		if (pos == null) return -1;
		else return pos.getOffset();
	}
	public int getEndOffsetForNode(Object node) {
		Position pos = (Position)endOffsets.get(node);
		if (pos == null) return -1;
		else return pos.getOffset();
	}
	public boolean isEditable(int offset) {
		Object node = getNodeForOffset(offset);
		if ((node instanceof org.w3c.dom.Text) &&
			(offset<getStartOffsetForNode(node) || offset>getEndOffsetForNode(node)))
				return false;
		else if (node instanceof org.w3c.dom.Attr &&
			(offset<getStartOffsetForNode(node) || offset>getEndOffsetForNode(node)-1))
				return false;
		else
			return isEditable(node);
	}
	public boolean isEditable(Object node) {
		if (node == null) return false;
		else if (node instanceof org.w3c.dom.Element) return false;
		else if (node instanceof org.w3c.dom.Text) return true;
		else if (node instanceof org.w3c.dom.Attr) return true;
		else return false;
	}
	public JTextPane getTextPane() {
		return pane;
	}
	public org.w3c.dom.Document getXMLDocument() {
		return xml;
	}
	public void setEditable(boolean bool) {
		pane.setEditable(bool);
	}
	public boolean isEditable() {
		return pane.isEditable();
	}
}
