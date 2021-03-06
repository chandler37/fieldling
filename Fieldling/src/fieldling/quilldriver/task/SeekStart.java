package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.mediaplayer.PanelPlayerException;
import fieldling.quilldriver.xml.*;
import javax.xml.xpath.*;

public class SeekStart extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            qd.getTimeCodeModel().setNode(XPathUtilities.saxonSelectSingleDOMNode(qd.getEditor().getNodeForOffset(qd.getEditor().getTextPane().getCaret().getMark()), (XPathExpression)(qd.getConfiguration().getParameters().get("qd.nearestplayableparent"))));
            Long t = qd.getTimeCodeModel().getInTime();
            if (qd.getMediaPlayer().isPlaying()) qd.getMediaPlayer().cmd_stop();
            qd.getMediaPlayer().setCurrentTime(t.longValue());
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
