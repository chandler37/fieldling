package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.QDShell;
import fieldling.quilldriver.xml.Editor;
import javax.swing.*;

public class ExitQD extends ClosingTask {
    public void execute(QD qd, String parameters) {
        QDShell qdShell = qd.getParentQDShell();
        qdShell.putPreferences();
        QD closeThisQD = qdShell.getQD();
        while (closeThisQD != null && closeThisQD.hasContent()) {
            if (closeTranscript(qd) == -1)
                return; //user cancelled close
        }
		/* It is not shutting down under certain conditions in windows xp.
		 * This makes sure VM does finish, with a grace period of 10 sec.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        try {
		          Thread.sleep(10000);
		        } catch(InterruptedException ex) {}
		        // halt will bail out without calling further shutdown hooks or
		        // finalizers
		        Runtime.getRuntime().halt(1);
		      }
		    });
        System.exit(0);
    }
}
