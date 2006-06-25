package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.mediaplayer.PanelPlayerException;

public class StopMedia extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            if (qd.getMediaPlayer().isPlaying()) qd.getMediaPlayer().cmd_stop();
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
