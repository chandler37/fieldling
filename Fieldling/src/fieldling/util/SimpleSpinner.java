package fieldling.util;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;

public class SimpleSpinner extends JPanel {
	JTextField textSpinner = null;
	Object jSpinner = null;
	Class[] getSetParameterTypes;
	Method getMethod, setMethod;

//add SpinnerNumberModel so that player.getEndTime() is max, and 0 min

	public SimpleSpinner() {
		jSpinner = SimpleSpinner.createObject("javax.swing.JSpinner");
		if (jSpinner == null) {
			textSpinner = new JTextField("0");
			setLayout(new BorderLayout());
			add("Center", textSpinner);
		} else {
			getSetParameterTypes = new Class[] {Object.class};
			setLayout(new BorderLayout());
			add("Center", (JComponent)jSpinner);
		}
	}
	public void setValue(Integer num) {
		if (jSpinner == null) {
			textSpinner.setText(num.toString());
		} else { //must be JSpinner
			Object[] argument = new Object[] {num};
			try {
				setMethod = jSpinner.getClass().getMethod("setValue", getSetParameterTypes);
				setMethod.invoke(jSpinner, argument);
			} catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
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
				setMethod = jSpinner.getClass().getMethod("getValue", null);
				return (Integer)setMethod.invoke(jSpinner, null);
			} catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
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
