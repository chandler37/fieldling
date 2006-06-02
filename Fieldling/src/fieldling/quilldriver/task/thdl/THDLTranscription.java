package fieldling.quilldriver.task.thdl;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.QDShell;
import fieldling.quilldriver.config.*;

public class THDLTranscription extends fieldling.quilldriver.task.BasicTask {
    public void execute(QD qd, String parameters) {
        Configuration thdlTranscription = ConfigurationFactory.getConfiguration("THDLTranscription");
        if (thdlTranscription != null) {
            qd.setConfiguration(thdlTranscription);
            QDShell qdShell = qd.getParentQDShell();
            qdShell.setJMenuBar(qd.getConfiguration().getJMenuBar()); //different configurations have different menu bars
            qdShell.deActivateQD(qd);
            if (qd.hasContent())
                qdShell.activateQD(qd, true); //updates qdShell title, repaint, etc.
            else
                qdShell.activateQD(qd, false);
        }
    }
}
