package fieldling.quilldriver.task.thdl;

import fieldling.quilldriver.gui.QD;
@TIBETAN@	import fieldling.util.I18n;
@TIBETAN@	import fieldling.quilldriver.PreferenceManager;
@TIBETAN@	import javax.swing.*;
@TIBETAN@	import java.util.*;

public class ChangeKeyboard extends fieldling.quilldriver.task.BasicTask {
    @TIBETAN@  static ResourceBundle messages = I18n.getResourceBundle();
    @TIBETAN@  static org.thdl.tib.input.JskadKeyboardManager keybdMgr = null;
    @TIBETAN@  static String[] tibetanKeyboardNames = null;
    @TIBETAN@  static {
  		@TIBETAN@	try {
			@TIBETAN@		keybdMgr = new org.thdl.tib.input.JskadKeyboardManager(org.thdl.tib.input.JskadKeyboardFactory.getAllAvailableJskadKeyboards());
                        @TIBETAN@          tibetanKeyboardNames = new String[keybdMgr.size()];
                        @TIBETAN@          for (int i=0; i< keybdMgr.size(); i++)
                        @TIBETAN@               tibetanKeyboardNames[i] = keybdMgr.elementAt(i).getIdentifyingString();
			@TIBETAN@	}catch (Exception e) {}
    @TIBETAN@}
                        
    public void execute(QD qd, String parameters) {
        @TIBETAN@  String userKeyboard = PreferenceManager.getValue(PreferenceManager.TIBETAN_KEYBOARD_KEY, keybdMgr.elementAt(0).getIdentifyingString());
        @TIBETAN@	String result = (String)JOptionPane.showInputDialog(qd, messages.getString("KeyboardInputMethod"), null,
        @TIBETAN@	        JOptionPane.PLAIN_MESSAGE, null, tibetanKeyboardNames, userKeyboard);
        @TIBETAN@	if (result != null) {
        @TIBETAN@          int i;
        @TIBETAN@		for (i=0; i<keybdMgr.size(); i++)
	@TIBETAN@			if (result.equals(keybdMgr.elementAt(i).getIdentifyingString())) break;
        @TIBETAN@	    qd.changeKeyboard(keybdMgr.elementAt(i));
        @TIBETAN@	    PreferenceManager.setValue(PreferenceManager.TIBETAN_KEYBOARD_KEY, keybdMgr.elementAt(i).getIdentifyingString());
        @TIBETAN@	}
    }
}
