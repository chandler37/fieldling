package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.mediaplayer.PanelPlayerException;

public class PlayPause extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            /* by transferring focus, we don't have to worry about problems caused by
            the cursor position in the editor being different from the highlight,
            since users will have to click on the editor to get back into editing */
            if (qd.getMediaPlayer().getAutoScrolling())
                qd.getEditor().getTextPane().transferFocus();
            if (qd.getMediaPlayer().isPlaying()) qd.getMediaPlayer().cmd_stop();
            else qd.getMediaPlayer().cmd_playOn();
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
