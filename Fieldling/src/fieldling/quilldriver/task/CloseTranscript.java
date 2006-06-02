package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.xml.Editor;
import javax.swing.*;

public class CloseTranscript extends ClosingTask {
    public void execute(QD qd, String parameters) {
        if (closeTranscript(qd) == -1)
            ; //error message
    }
}
