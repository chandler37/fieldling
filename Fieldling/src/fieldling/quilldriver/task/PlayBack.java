package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import fieldling.mediaplayer.PanelPlayerException;

public class PlayBack extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            long t = qd.getPlayer().getCurrentTime() - PreferenceManager.play_minus;
            if (t < 0) t = 0;
            if (qd.getPlayer().isPlaying()) qd.getPlayer().cmd_stop();
            qd.getPlayer().setCurrentTime(t);
            qd.getPlayer().cmd_playOn();
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
