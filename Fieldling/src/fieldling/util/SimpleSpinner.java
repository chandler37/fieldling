package fieldling.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.*;
import javax.swing.event.EventListenerList;

public class SimpleSpinner extends JPanel implements FocusListener { //ChangeListener, FocusListener {
	private EventListenerList listenerList = new EventListenerList();
	JTextField textSpinner = null;
	Object jSpinner = null;
	Method getMethod, setMethod;

	//add SpinnerNumberModel so that player.getEndTime() is max, and 0 min
	public SimpleSpinner() {
		//jSpinner = SimpleSpinner.createObject("javax.swing.JSpinner");
		if (jSpinner == null) {
			textSpinner = new JTextField("0");
			textSpinner.addFocusListener(this);
			setLayout(new BorderLayout());
			add("Center", textSpinner);
		} else {
			Method acl, getEditor;
			Class[] setParameters = new Class[] {Object.class};
			Object[] argument = new Object[] {this};
			//Class[] aclParameters = new Class[] {ChangeListener.class};
			try {
				setMethod = jSpinner.getClass().getMethod("setValue", setParameters);
				getMethod = jSpinner.getClass().getMethod("getValue", null);
				getEditor = jSpinner.getClass().getMethod("getEditor", null);
				//this doesn't work for some reason: the editor never gets focus!!
				JComponent editor = (JComponent)getEditor.invoke(jSpinner, null);
				editor.addFocusListener(this);
				//acl = jSpinner.getClass().getMethod("addChangeListener", aclParameters);
				//acl.invoke(jSpinner, argument);
			} catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
			} catch (IllegalAccessException illae) {
				illae.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
			setLayout(new BorderLayout());
			add("Center", (JComponent)jSpinner);
		}
	}
	public void focusGained(FocusEvent e) {
		System.out.println("focus gained");
	}
	public void focusLost(FocusEvent e) {
		fireValueChanged(new ChangeEvent(this));
		System.out.println("focus lost");
	}/*
	public void stateChanged(ChangeEvent e) {
		fireValueChanged(e);
	}*/
	public void addSimpleSpinnerListener(SimpleSpinnerListener spl) {
		listenerList.add(SimpleSpinnerListener.class, spl);
	}
	public void removeSimpleSpinnerListener(SimpleSpinnerListener spl) {
		listenerList.remove(SimpleSpinnerListener.class, spl);
	}
	public void removeAllSimpleSpinnerListeners() {
		listenerList = new EventListenerList();
	}
	private void fireValueChanged(ChangeEvent e) {
		//see javadocs on EventListenerList for how following array is structured
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==SimpleSpinnerListener.class)
				((SimpleSpinnerListener)listeners[i+1]).valueChanged(e);
		}
	}
	public void setValue(Integer num) {
		if (jSpinner == null) {
			textSpinner.setText(num.toString());
		} else { //must be JSpinner
			Object[] argument = new Object[] {num};
			try {
				setMethod.invoke(jSpinner, argument);
			} catch (IllegalAccessException illae) {
				illae.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}
	}
	public Integer getValue() {
		if (jSpinner == null) {
			try {
				return new Integer(textSpinner.getText());
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else { //must be JSpinner
			try {
				return (Integer)getMethod.invoke(jSpinner, null);
			} catch (IllegalAccessException illae) {
				illae.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}
		return null;
	}

   static Object createObject(String className) {
      Object object = null;
      try {
          Class classDefinition = Class.forName(className);
          object = classDefinition.newInstance();
      } catch (InstantiationException e) {
          System.out.println(e);
      } catch (IllegalAccessException e) {
          System.out.println(e);
      } catch (ClassNotFoundException e) {
          System.out.println(e);
      }
      return object;
   }
}
