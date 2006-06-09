package fieldling.quilldriver.task;

import javax.swing.JOptionPane;
import fieldling.quilldriver.gui.QD;

public class SaveAllTranscripts extends SavingTask {
    public void execute(QD qd, String parameters) {
        if (qd.hasContent()) {
            for (int i=0; i<qd.transcriptToggler.getNumberOfTranscripts(); i++) {
                QD oneQDAtATime = qd.transcriptToggler.getQDForIndex(i);
                if (oneQDAtATime.getEditor().hasChangedSinceLastSaved())
                    saveTranscript(oneQDAtATime);
            }
        }
    }
}
