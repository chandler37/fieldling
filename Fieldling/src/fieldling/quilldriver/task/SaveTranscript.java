package fieldling.quilldriver.task;

import javax.swing.JOptionPane;
import fieldling.quilldriver.gui.QD;

public class SaveTranscript extends SavingTask {
    public void execute(QD qd, String parameters) {
        boolean result = true;
        if (qd.getEditor().isEditable())
            if (!saveTranscript(qd))
                JOptionPane.showMessageDialog(qd, qd.getMessages().getString("FileCouldNotBeSaved"), qd.getMessages().getString("Alert"), JOptionPane.ERROR_MESSAGE);
    }
}
