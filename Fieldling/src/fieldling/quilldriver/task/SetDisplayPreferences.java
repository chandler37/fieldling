package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class SetDisplayPreferences extends BasicTask {
    public void execute(QD qd, String parameters) {
        qd.getParentQDShell().getDisplayPreferences();
    }
}
