package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.*;
import fieldling.quilldriver.xml.Editor;
import javax.swing.*;

public abstract class ClosingTask extends BasicTask {
    public abstract void execute(QD qd, String parameters);
    
    public int closeTranscript(QD qd) {
        QDShell qdShell = qd.getParentQDShell();
	qdShell.putPreferences();
        if (qd.hasContent()) { //there's an editor: save and close
            if (qd.getEditor().isEditable() && qd.getEditor().hasChangedSinceLastSaved()) {
                int option = JOptionPane.showConfirmDialog(qdShell, qd.messages.getString("WantToSaveChanges"), "QuillDriver", JOptionPane.YES_NO_CANCEL_OPTION);
                if (option==JOptionPane.YES_OPTION) {
                    try {
                        BasicTask saveTask = BasicTask.getTaskForClass("fieldling.quilldriver.gui.SaveTranscript");
                        saveTask.execute(qd, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (option == JOptionPane.CANCEL_OPTION) {
                    return -1;
                }
            }
            if (qd.transcriptToggler.getNumberOfTranscripts() > 1) {
                QD switchTo = QD.lastQD;
                if (switchTo == null || qd.transcriptToggler.getIndexForQD(switchTo) == -1) { //activate first QD on list
                    try {
                        switchTo = qd.transcriptToggler.getQDForIndex(0);
                    } catch (IndexOutOfBoundsException iobe) {
                        iobe.printStackTrace();
                    }
                }
                qd.removeContent();
                qdShell.deActivateQD(qd);
                qdShell.activateQD(switchTo, true);
            } else { //only one transcript--close and keep empty qd
                qd.removeContent();
                qdShell.deActivateQD(qd);
                qdShell.hasLoadedTranscript = false;
                qdShell.activateQD(qd, false);
            }
	}
        return 1;
    }
}
