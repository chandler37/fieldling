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

package fieldling.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JFrame;

//import org.thdl.util.ThdlLazyException;

/**
 * @author David Chandler
 *
 * This class provides functionality that is not present in Java's JDK
 * 1.2.  Because we want to compile and run on a Java 2/1.2 system, we
 * must use the Java Reflection API to provide functionality specific
 * to Java 2/1.3 or later.  At present, I haven't tested on a 1.2 box,
 * but I have tested on a 1.3.1_04 box.
 *
 * If your code will break if some functionality here is not present,
 * test the return value of the function and throw an exception.
 * Avoid such code like the plague.
 *
 * This code is all written for one-shot operations thus-far.  If you
 * plan on executing a piece of code many times, you need to set up
 * the reflection mechanism first, and then re-use it again and again.
 * This is a two-step process.  Three steps if you explicitly
 * deconstruct the mechanism.
 *
 * This class is not instantiable.  */
public final class JdkVersionHacks {
	/** Don't instantiate this class. */
	private JdkVersionHacks() { }

	/** Calls f.setUndecorated(true) if possible.  Returns true if
        successful. */
	public static boolean undecorateJFrame(JFrame f) {
		/* If we're using JDK 1.4, execute
           f.setUndecorated(true).  Otherwise, don't. */

		boolean success = false;

		Method setUndecoratedMethod = null;
		try {
			setUndecoratedMethod
				= JFrame.class.getMethod("setUndecorated",
										 new Class[] {
											 Boolean.TYPE
										 });
		} catch (NoSuchMethodException ex) {
			/* We're not using JDK 1.4 or later. */
		} catch (SecurityException ex) {
			/* We'll never know if we're using JDK 1.4 or later. */
			handleSecurityException(ex);
			return false;
		}
		if (setUndecoratedMethod != null) {
			try {
				setUndecoratedMethod.invoke(f, new Object[] { Boolean.TRUE });
				success = true;

				/* We shouldn't get any of these exceptions: */
			} catch (IllegalAccessException ex) {
				ex.printStackTrace(); //throw new ThdlLazyException(ex);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace(); //throw new ThdlLazyException(ex);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace(); //throw new ThdlLazyException(ex);
			}
		}
		return success;
	}

	/** Calls f.setExtendedState(Frame.MAXIMIZED_BOTH) if possible.
        Returns true if successful. */
	public static boolean maximizeJFrameInBothDirections(JFrame f) {
		/* If we're using JDK 1.4, execute
           f.setExtendedState(Frame.MAXIMIZED_BOTH).  Otherwise,
           don't. */

		boolean success = false;

		Method setExtendedStateMethod = null;
		try {
			setExtendedStateMethod
				= JFrame.class.getMethod("setExtendedState",
										 new Class[] {
											 Integer.TYPE
										 });
		} catch (NoSuchMethodException ex) {
			/* We're not using JDK 1.4 or later. */
			return false;
		} catch (SecurityException ex) {
			/* We'll never know if we're using JDK 1.4 or later. */
			handleSecurityException(ex);
			return false;
		}
		try {
			setExtendedStateMethod.invoke(f,
										  new Object[] {
											  maximizedBothOption()
										  });
			success = true;

		} catch (NoSuchFieldException ex) {
			/* We're not using JDK 1.4 or later. */
			return false;
		/* We shouldn't get any of these exceptions: */
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
						return false;
			//throw new Exception(ex);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(); 
						return false;
			//throw new Exception(ex);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace(); 
						return false;
			//throw new Exception(ex);
		}
		return success;
	}

	/** Returns true iff maximizeJFrameInBothDirections(f) will
		actually have an effect. */
	public static boolean maximizedBothSupported(Toolkit tk) {
		// Execute
		// f.getToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)
		// if possible.
		
		Method isFSSMethod = null;
		try {
			isFSSMethod
				= JFrame.class.getMethod("isFrameStateSupported",
										 new Class[] {
											 Integer.TYPE
										 });
		} catch (NoSuchMethodException ex) {
			/* We're not using JDK 1.4 or later. */
			return false;
		} catch (SecurityException ex) {
			/* We'll never know if we're using JDK 1.4 or later. */
			handleSecurityException(ex);
			return false;
		}
		try {
			return ((Boolean)isFSSMethod.invoke(tk,
												new Object[] {
													maximizedBothOption()
												})).booleanValue();

		} catch (NoSuchFieldException ex) {
			/* We're not using JDK 1.4 or later. */
			return false;

		/* We shouldn't get any of these exceptions: */
		} catch (IllegalAccessException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		}
		return false;
	}

	/** Coming soon: Does what the user desires (via the options he or
			she has set) with this SecurityException, one encountered
			during the process of reflection.

			Currently: does nothing.

			FIXME */
	private static void handleSecurityException(SecurityException ex)
		throws SecurityException
	{
		if (false /* FIXME: I want this exception when debugging: THDLUtils.getBooleanOption("EagerlyThrowExceptions") */)
			throw ex;
	}

	/** Returns the value of Frame.MAXIMIZED_BOTH, wrapped in an
			Integer.

			@throws NoSuchFieldException the field does not exist or
			cannot be accessed because security settings are too limiting */
	private static Object maximizedBothOption()
		throws NoSuchFieldException
	{
		Field maxBothOptionField = null;
		try {
			maxBothOptionField = Frame.class.getField("MAXIMIZED_BOTH");

			/* Don't catch NoSuchFieldException */
		} catch (SecurityException ex) {
			/* We'll never know if we're using JDK 1.4 or later. */
			handleSecurityException(ex);
			throw new NoSuchFieldException("java.awt.Frame.MAXIMIZED_BOTH");
		}

		try {
			return maxBothOptionField.get(null);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		} catch (NullPointerException ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		} catch (ExceptionInInitializerError ex) {
			ex.printStackTrace(); //throw new ThdlLazyException(ex);
		}
		return null;
	}
};

