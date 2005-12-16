package fieldling.util;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

public class KeyStrokeUtils {
    /**
    * Takes an ugly key description of the form required by
    * KeyStroke.getKeyStroke(String) (e.g. "control DELETE")
    * and converts it into a pretty key description of the sort
    * used to display shortcuts for menu items (e.g.
    * "Control-DEL").
    *
    * @param uglyKey an ugly key description
    * @return a pretty key description
    * @see KeyStroke.getKeyStroke(String s)
    */
    public static String convertKeyDescriptionToReadableFormat(String uglyKey) {
        KeyStroke stroke = KeyStroke.getKeyStroke(uglyKey);
        return KeyEvent.getKeyModifiersText(stroke.getModifiers()) + "-" 
                + KeyEvent.getKeyText(stroke.getKeyCode());
    }
}
