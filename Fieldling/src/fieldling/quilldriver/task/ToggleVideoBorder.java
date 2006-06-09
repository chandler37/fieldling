package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;

public class ToggleVideoBorder extends BasicTask {
    public void execute(QD qd, String parameters) {
        if (qd.hasContent()) {
            if (PreferenceManager.getInt(PreferenceManager.VIDEO_HAS_BORDER_KEY, -1) == -1) {
                PreferenceManager.setInt(PreferenceManager.VIDEO_HAS_BORDER_KEY, 1);
            } else {
                PreferenceManager.setInt(PreferenceManager.VIDEO_HAS_BORDER_KEY, -1);
            }
            WindowPositioningTask.repositionWithActiveWindowPositioner(qd);
        }
    }
}
