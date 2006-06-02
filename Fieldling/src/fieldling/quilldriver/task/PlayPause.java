package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.mediaplayer.PanelPlayerException;

public class PlayPause extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            /* by transferring focus, we don't have to worry about problems caused by
            the cursor position in the editor being different from the highlight,
            since users will have to click on the editor to get back into editing */
            if (qd.getMode() == QD.SCROLLING_HIGHLIGHT_IS_ON)
                qd.getEditor().getTextPane().transferFocus();
            if (qd.getPlayer().isPlaying()) qd.getPlayer().cmd_stop();
            else qd.getPlayer().cmd_playOn();
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
