package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import fieldling.mediaplayer.PanelPlayerException;

public class PlayBack extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            long t = qd.getMediaPlayer().getCurrentTime() - PreferenceManager.getInt(PreferenceManager.PLAY_MINUS_KEY, PreferenceManager.PLAY_MINUS_DEFAULT);
            if (t < 0) t = 0;
            if (qd.getMediaPlayer().isPlaying()) qd.getMediaPlayer().cmd_stop();
            qd.getMediaPlayer().setCurrentTime(t);
            qd.getMediaPlayer().cmd_playOn();
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
