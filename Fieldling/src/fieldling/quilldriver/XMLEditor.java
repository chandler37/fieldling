package fieldling.quilldriver;



import org.jdom.Document;

import org.jdom.Element;

import org.jdom.Attribute;

import org.jdom.Text;

import org.jdom.DocType;

import java.awt.Color;

import java.awt.Cursor;

import java.awt.event.KeyEvent;

import java.awt.event.ActionEvent;

import java.awt.event.MouseEvent;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseListener;

import java.awt.event.MouseMotionAdapter;

import java.awt.event.FocusEvent;

import java.awt.event.FocusListener;

import java.util.List;

import java.util.Set;

import java.util.Iterator;

import java.util.Map;

import java.util.HashMap;

import java.util.Hashtable;

import java.util.EventObject;

import java.util.EventListener;

import javax.swing.JTextPane;

import javax.swing.text.JTextComponent;

import javax.swing.Action;

import javax.swing.AbstractAction;

import javax.swing.KeyStroke;

import javax.swing.text.Keymap;

import javax.swing.text.Position;

import javax.swing.text.StyleConstants;

import javax.swing.text.StyledDocument;

import javax.swing.text.AttributeSet;

import javax.swing.text.SimpleAttributeSet;

import javax.swing.text.BadLocationException;

import javax.swing.text.DefaultEditorKit;

import javax.swing.event.DocumentListener;

import javax.swing.event.DocumentEvent;

import javax.swing.event.CaretListener;

import javax.swing.event.CaretEvent;

import javax.swing.event.EventListenerList;



public class XMLEditor {

	private EventListenerList listenerList = new EventListenerList();

	private Document xml;

	private JTextPane pane;

	private StyledDocument doc;

	private DocumentListener docListen;

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

	private CaretListener editabilityTracker;

	private XMLTagInfo tagInfo;

	

	public XMLEditor(Document xmlDoc, JTextPane textPane, XMLTagInfo tagInfo) {

		xml = xmlDoc;

		pane = textPane;

		this.tagInfo = tagInfo;

		startOffsets = new HashMap();

		endOffsets = new HashMap();

		

		docListen = new DocumentListener() {

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

		render();

		

		textCursor = new Cursor(Cursor.TEXT_CURSOR);

		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

			

		pane.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				int offset = p.viewToModel(e.getPoint());

				if (isEditable(offset)) p.setCursor(textCursor);

				else p.setCursor(defaultCursor);

			}

		});



		MouseListener[] listeners = (MouseListener[])pane.getListeners(MouseListener.class);

		for (int i=0; i<listeners.length; i++) pane.removeMouseListener(listeners[i]);

		pane.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				int offset = p.viewToModel(e.getPoint());

				if (isEditable(offset)) {

					p.requestFocus();

					if (isEditing) fireEndEditEvent();

					fireStartEditEvent(getNodeForOffset(offset));

					p.setCaretPosition(offset);

				} else {

					Object node = getNodeForOffset(offset);

					if (isEditing) fireEndEditEvent();

					if (node != null) fireCantEditEvent(getNodeForOffset(offset));

				}

			}

		});



		pane.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				if (isEditable(p.getCaretPosition()))

					fireStartEditEvent(getNodeForOffset(p.getCaretPosition()));

			}

			public void focusLost(FocusEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				if (isEditing) fireEndEditEvent();

			}

		});



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

		

		Action selectNodeAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				Object node = getNodeForOffset(p.getCaretPosition());

				if (node != null) {

					p.setSelectionStart(((Position)startOffsets.get(node)).getOffset());

					int end = ((Position)endOffsets.get(node)).getOffset();

					if (node instanceof Text) p.setSelectionEnd(end);

					else if (node instanceof Attribute) p.setSelectionEnd(end-1);

				}

			}	

		};



		Action selForwardAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				int offset = p.getCaretPosition();

				Object node = getNodeForOffset(offset);

				int last = (((Position)endOffsets.get(node)).getOffset());

				if (node instanceof Attribute) last--;

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

					if (node instanceof Attribute) last--;

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

		

		Action begNodeAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				Object node = getNodeForOffset(p.getCaretPosition());

				p.setCaretPosition(((Position)startOffsets.get(node)).getOffset());

			}

		};

		

		Action endNodeAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				Object node = getNodeForOffset(p.getCaretPosition());

				p.setCaretPosition(((Position)endOffsets.get(node)).getOffset());

			}

		};

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

		
		Action loseFocusAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JTextPane p = (JTextPane)e.getSource();
				p.transferFocus(); //moves focus to next component
			}
		};



		/* Action selForwardAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {

				JTextPane p = (JTextPane)e.getSource();

				int offset = p.getCaretPosition();

				

			}

		};

		*/

		createActionTable(pane);
		Keymap keymap = pane.addKeymap("QDBindings", pane.getKeymap());

/*		KeyStroke[] tabKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.insertTabAction));

		if (tabKeys != null)

			for (int i=0; i<tabKeys.length; i++)

				keymap.addActionForKeyStroke(tabKeys[i], nextNodeAction);
*/
		//tab
		KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		if (tabKey != null) keymap.addActionForKeyStroke(tabKey, nextNodeAction);
		
		//enter
		KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		if (enterKey != null) keymap.addActionForKeyStroke(enterKey, nextNodeAction);			

		//backspace
		/* The Java bug database has several related bugs concerning the treatment
		of backspace. Here I adopt solution based on fix of bug 4402080:
		Evaluation  The text components now key off of KEY_TYPED with a keyChar == 8 to do the
		deletion. The motivation for this can be found in bug 4256901.
		xxxxx@xxxxx 2001-01-05 */
		KeyStroke backSpace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		keymap.addActionForKeyStroke(backSpace, deletePrevAction);
		pane.addKeyListener(new java.awt.event.KeyAdapter() {
			/* NOT ACTUALLY NEEDED, AT LEAST FOR WINDOWS
			public void keyReleased(KeyEvent kev) {
				if (kev.getKeyCode() == KeyEvent.VK_DELETE) {System.out.println("delete released"); kev.consume();}
			}*/
			public void keyTyped(KeyEvent kev) {
				if (kev.getKeyChar() == 8) kev.consume();
				/* Above is equivalent to:
					if(kev.paramString().indexOf("Backspace") != -1) kev.consume();	*/
				else if (kev.getKeyCode() == KeyEvent.VK_TAB) kev.consume();
			}
		});

		//delete
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		keymap.addActionForKeyStroke(delete, deleteNextAction);
		/*
		KeyStroke[] delNextKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.deleteNextCharAction));

		if (delNextKeys != null)

			for (int i=0; i<delNextKeys.length; i++)

				keymap.addActionForKeyStroke(delNextKeys[i], deleteNextAction);
		*/
		
		KeyStroke[] selectAllKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectAllAction));

		if (selectAllKeys != null)
			for (int i=0; i<selectAllKeys.length; i++)

				keymap.addActionForKeyStroke(selectAllKeys[i], selectNodeAction);

		KeyStroke[] selLineKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectLineAction));

		if (selLineKeys != null)

			for (int i=0; i<selLineKeys.length; i++)

				keymap.addActionForKeyStroke(selLineKeys[i], selectNodeAction);			

		KeyStroke[] selParaKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.selectParagraphAction));

		if (selParaKeys != null)

			for (int i=0; i<selParaKeys.length; i++)

				keymap.addActionForKeyStroke(selParaKeys[i], selectNodeAction);

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



		KeyStroke[] endLineKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.endLineAction));

		if (endLineKeys != null)

			for (int i=0; i<endLineKeys.length; i++)

				keymap.addActionForKeyStroke(endLineKeys[i], endNodeAction);

		KeyStroke[] endParaKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.endParagraphAction));

		if (endParaKeys != null)

			for (int i=0; i<endParaKeys.length; i++)

				keymap.addActionForKeyStroke(endParaKeys[i], endNodeAction);

		KeyStroke[] begLineKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.beginLineAction));

		if (begLineKeys != null)

			for (int i=0; i<begLineKeys.length; i++)

				keymap.addActionForKeyStroke(begLineKeys[i], begNodeAction);

		KeyStroke[] begParaKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.beginParagraphAction));

		if (begParaKeys != null)

			for (int i=0; i<begParaKeys.length; i++)

				keymap.addActionForKeyStroke(begParaKeys[i], begNodeAction);

		KeyStroke back = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		keymap.addActionForKeyStroke(back, backwardAction);
		
		/*KeyStroke[] backwardKeys = keymap.getKeyStrokesForAction(getActionByName(DefaultEditorKit.backwardAction));

		if (backwardKeys != null)

			for (int i=0; i<backwardKeys.length; i++)

				keymap.addActionForKeyStroke(backwardKeys[i], backwardAction);
		*/

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

		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		if (escapeKey != null) keymap.addActionForKeyStroke(escapeKey, loseFocusAction);

		final Action parentDefault = keymap.getDefaultAction();
		Action thisDefault = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) || 
				((e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK) ||
				((e.getModifiers() & ActionEvent.META_MASK) == ActionEvent.META_MASK)) {
					System.out.println("yo yo yo yo");
					return;
				}
				if (e.getActionCommand() != null) { 				
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



	public void setEditabilityTracker(boolean bool) {

		if (bool) {

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

		else pane.removeCaretListener(editabilityTracker);

	}

	public void updateNode(Object node) {

		System.out.println("updating: " + node.toString());

		if (node == null)

			return;

		try {		

			if (node instanceof Text) {
				int p1 = ((Position)startOffsets.get(node)).getOffset();
				int p2 = ((Position)endOffsets.get(node)).getOffset();
				
				//TIBETAN-SPECIFIC
				Text t = (Text)node;
				String val;
				if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.isTagTextTibetan(t.getParent().getQualifiedName())) {
					org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
					val = duff.getTibDoc().getWylie(p1, p2);
				} else val = pane.getDocument().getText(p1, p2-p1).trim();
				if (val.length()==0) val=new String(" ");
				Text text = (Text)node;
				text.setText(val);
			} else if (node instanceof Attribute) {
				int p1 = ((Position)startOffsets.get(node)).getOffset();
				int p2 = ((Position)endOffsets.get(node)).getOffset()-1; //remove right quote
				String val = pane.getDocument().getText(p1, p2-p1).trim();		
				Attribute att = (Attribute)node;
				att.setValue(val);
			}

			System.out.println("updated: " + node.toString());

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
		//TIBETAN-SPECIFIC
		if (pane instanceof org.thdl.tib.input.DuffPane && node instanceof Text) {
			Text t = (Text)node;
			org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
			if (tagInfo.isTagTextTibetan(t.getParent().getQualifiedName())) {
				if (duff.isRomanMode()) duff.toggleLanguage();
			} else if (!duff.isRomanMode()) duff.toggleLanguage();
		}
		
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
		
		//TIBETAN-SPECIFIC
		if (pane instanceof org.thdl.tib.input.DuffPane && editingNode instanceof Text) {
			Text t = (Text)editingNode;
			org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
			if (!duff.isRomanMode()) duff.toggleLanguage();
		}
		
		//see javadocs on EventListenerList for how following array is structured
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==NodeEditListener.class)
				((NodeEditListener)listeners[i+1]).nodeEditPerformed(new EndEditEvent(editingNode));
		}
		if (hasChanged) updateNode(editingNode);
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



	public void setXMLDocument(Document d, String doctype_elementName, String doctype_systemID) {

		xml = d;

		xml.setDocType(new DocType(doctype_elementName, doctype_systemID));

		render();

	}

	public void render() {

		System.out.println("Rendering the document");

		doc = pane.getStyledDocument();

		int len = doc.getLength();

		try {

			if (len > 0) doc.remove(0, len);

			doc.insertString(0, "\n", null);

		} catch (BadLocationException ble) {

			ble.printStackTrace();

		}

		startOffsets.clear();

		endOffsets.clear();

		Element root = xml.getRootElement();

		renderElement(root, 0.0F, doc.getLength());



		SimpleAttributeSet eColor = new SimpleAttributeSet();

		eColor.addAttribute("xmlnode", root);

		doc.setParagraphAttributes(doc.getLength(), 1, eColor, false);



		fixOffsets();



		doc.addDocumentListener(docListen);

		pane.setCaretPosition(0);

		setEditabilityTracker(true);

	}



	public void fixOffsets() {

		//replace Integer values in startOffsets and endOffsets with Positions

		Set startKeys = startOffsets.keySet();

		Iterator iter = startKeys.iterator();

		while (iter.hasNext()) {

			Object key = iter.next();

			Object obj = startOffsets.get(key);

			//if (obj instanceof Position)

			//	startOffsets.put(key, obj); 	//actually we don't have to do anything here, do we

								//since the startoffsets are already set!!

			//else

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

			//if (obj instanceof Position)

			//	endOffsets.put(key, obj);	//actually we don't have to do anything here, do we

								//since the endoffsets are already set!!

			//else 

			if (obj instanceof Integer) try {

				Integer val = (Integer)obj;

				endOffsets.put(key, doc.createPosition(val.intValue()));

			} catch (BadLocationException ble) {

				ble.printStackTrace();

			}

		}

	}

	public int renderElement(Element e, float indent, int insertOffset) {

		try {

			Position pos = doc.createPosition(insertOffset);

			SimpleAttributeSet eAttributes = new SimpleAttributeSet();

			StyleConstants.setLeftIndent(eAttributes, indent);

			SimpleAttributeSet eColor = new SimpleAttributeSet();

			//StyleConstants.setLeftIndent(eColor, indent);

			StyleConstants.setForeground(eColor, tagColor);
			
			//added for Tibetan version
			StyleConstants.setFontSize(eColor, 14);
			StyleConstants.setFontFamily(eColor, "Monospaced");

			eColor.addAttribute("xmlnode", e);

			if (pos.getOffset()>0) {

				String s = doc.getText(pos.getOffset()-1, 1);

				if (s.charAt(0)!='\n') {

					AttributeSet attSet = doc.getCharacterElement(pos.getOffset()-1).getAttributes();
					
					//added for Tibetan version
					SimpleAttributeSet sas = new SimpleAttributeSet(attSet);
					StyleConstants.setFontSize(sas, 14);
					StyleConstants.setFontFamily(sas, "Monospaced");
					
					doc.insertString(pos.getOffset(), "\n", sas);

				}

			}

			int start = pos.getOffset();

			startOffsets.put(e, new Integer(start));

			String tagDisplay;

			if (tagInfo == null) tagDisplay = e.getQualifiedName();

			else tagDisplay = tagInfo.getTagDisplay(e);
			
			doc.insertString(pos.getOffset(), tagDisplay, eColor); //insert element begin tag

			if (tagInfo == null || tagInfo.areTagContentsForDisplay(e.getQualifiedName())) {

			List attributes = e.getAttributes();

			Iterator iter = attributes.iterator();

			while (iter.hasNext()) {

				Attribute att = (Attribute)iter.next();

				if (tagInfo == null || tagInfo.isAttributeForDisplay(att.getQualifiedName(), e.getQualifiedName()))

					renderAttribute(att, pos.getOffset());

			}

			doc.insertString(pos.getOffset(), ":", eColor);

			doc.setParagraphAttributes(start, pos.getOffset()-start, eAttributes, false);

			//doc.insertString(pos.getOffset(), "\n", null);

			List list = e.getContent();

			iter = list.iterator();

			while (iter.hasNext()) {

				Object next = iter.next();

				if (next instanceof Element) {

					Element ne = (Element)next;

					if (tagInfo == null || tagInfo.isTagForDisplay(ne.getQualifiedName()))

						renderElement(ne, indent + indentIncrement, pos.getOffset());

				} else if (next instanceof Text) {

					Text t = (Text)next;

					if (t.getParent().getContent().size() == 1 || t.getTextTrim().length() > 0)

						renderText(t, indent + indentIncrement, pos.getOffset());

				}

				// Also: Comment ProcessingInstruction CDATA EntityRef

			}

			}

			//start = pos.getOffset();

			//doc.insertString(start, "}", eColor); //insert element end tag

			//doc.setParagraphAttributes(start, pos.getOffset(), eAttributes, false);

			

			if (pos.getOffset()>0) {

				//String s = doc.getText(pos.getOffset()-1, 1);

				if (doc.getText(pos.getOffset()-1,1).charAt(0)=='\n')

					endOffsets.put(e, new Integer(pos.getOffset()-1));

				else

					endOffsets.put(e, new Integer(pos.getOffset()));

			}

			//endOffsets.put(e, new Integer(pos.getOffset()));

			return pos.getOffset();

			//doc.insertString(pos.getOffset(), "\n", null);

		} catch (BadLocationException ble) {

			ble.printStackTrace();

			return -1;

		}

	}

	

	public int renderAttribute(Attribute att, int insertOffset) {

		try {

			Position pos = doc.createPosition(insertOffset);

			SimpleAttributeSet aColor = new SimpleAttributeSet();

			StyleConstants.setForeground(aColor, attColor);

			//added for Tibetan version
			StyleConstants.setFontSize(aColor, 14);
			StyleConstants.setFontFamily(aColor, "Monospaced");
			
			SimpleAttributeSet tColor = new SimpleAttributeSet();

			StyleConstants.setForeground(tColor, textColor);

			//added for Tibetan version
			StyleConstants.setFontSize(tColor, 14);
			StyleConstants.setFontFamily(tColor, "Monospaced");
			
			tColor.addAttribute("xmlnode", att);

			String name = att.getQualifiedName();

			String value = att.getValue();

			if (pos.getOffset()>0) {

				String s = doc.getText(pos.getOffset()-1, 1);

				if (s.charAt(0)!='\n') {

					AttributeSet attSet = doc.getCharacterElement(pos.getOffset()-1).getAttributes();

					//added for Tibetan version
					SimpleAttributeSet sas = new SimpleAttributeSet(attSet);
					StyleConstants.setFontSize(sas, 14);
					StyleConstants.setFontFamily(sas, "Monospaced");
			
					doc.insertString(pos.getOffset(), " ", sas);

				}

			}

			String displayName;

			if (tagInfo == null) displayName = att.getQualifiedName();

			else displayName = tagInfo.getAttributeDisplay(att.getQualifiedName(), att.getParent().getQualifiedName());

			doc.insertString(pos.getOffset(), displayName+"=", aColor);

			startOffsets.put(att, new Integer(pos.getOffset()+1)); //add one so that begin quote is not part of attribute value

			doc.insertString(pos.getOffset(), "\"" + att.getValue()+"\"", tColor);

			endOffsets.put(att, new Integer(pos.getOffset()));

			return pos.getOffset();

		} catch (BadLocationException ble) {

			ble.printStackTrace();

			return -1;

		}

	}

	

	public int renderText(Text t, float indent, int insertOffset) {

		try {

			Position pos = doc.createPosition(insertOffset);

			SimpleAttributeSet tAttributes = new SimpleAttributeSet();

			//StyleConstants.setLeftIndent(tAttributes, indent);

			StyleConstants.setForeground(tAttributes, textColor);
			StyleConstants.setFontSize(tAttributes, 14);
			StyleConstants.setFontFamily(tAttributes, "Monospaced");
			tAttributes.addAttribute("xmlnode", t);
			doc.insertString(pos.getOffset(), " ", tAttributes); //insert space with text attributes so first character has correct color, xmlnode attribute, etc.

			String s = t.getTextTrim();
			int start = pos.getOffset();
			startOffsets.put(t, new Integer(start));
			
			//TIBETAN-SPECIFIC:
			if (pane instanceof org.thdl.tib.input.DuffPane && tagInfo.isTagTextTibetan(t.getParent().getQualifiedName())) {
				org.thdl.tib.input.DuffPane duff = (org.thdl.tib.input.DuffPane)pane;
				duff.toTibetanMachineWeb(s, pos.getOffset());
				SimpleAttributeSet tibAtt = new SimpleAttributeSet();
				tibAtt.addAttribute("xmlnode", t);
				doc.setCharacterAttributes(start, pos.getOffset()-start, tibAtt, false);
			} else {
				doc.insertString(pos.getOffset(), s, tAttributes); //insert text
			}
			
			int end = pos.getOffset();
			endOffsets.put(t, new Integer(end));
			doc.insertString(pos.getOffset(), "\n", tAttributes);
			return pos.getOffset();
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return -1;
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

		if ((node instanceof Text) && 

			(offset<getStartOffsetForNode(node) || offset>getEndOffsetForNode(node)))

				return false;

		else if (node instanceof Attribute &&

			(offset<getStartOffsetForNode(node) || offset>getEndOffsetForNode(node)-1))

				return false;

		else 

			return isEditable(node);

	}

	public boolean isEditable(Object node) {

		if (node == null) return false;

		else if (node instanceof Element) return false;

		else if (node instanceof Text) return true;

		else if (node instanceof Attribute) return true;

		else return false;

	}

	public JTextPane getTextPane() {

		return pane;

	}

	public Document getXMLDocument() {

		return xml;

	}

}

