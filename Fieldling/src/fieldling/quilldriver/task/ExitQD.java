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
        System.exit(0);
    }
}
