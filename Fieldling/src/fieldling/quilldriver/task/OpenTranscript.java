package fieldling.quilldriver.task;

import java.io.File;
import javax.swing.JOptionPane;
import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.QDShell;

public class OpenTranscript extends OpeningTask {
    public void execute(QD qd, String parameters) {
        File transcriptFile = OpeningTask.selectTranscriptFile(qd.messages.getString("OpenTranscript"), qd);
        if (transcriptFile != null) {
            open(transcriptFile, qd);
        }
    }
}
