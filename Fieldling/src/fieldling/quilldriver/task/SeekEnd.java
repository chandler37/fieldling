package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import fieldling.mediaplayer.PanelPlayerException;
import fieldling.quilldriver.xml.*;
import javax.xml.xpath.*;

public class SeekEnd extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            qd.getTimeCodeModel().setNode(XPathUtilities.saxonSelectSingleDOMNode(qd.getEditor().getNodeForOffset(qd.getEditor().getTextPane().getCaret().getMark()), (XPathExpression)(qd.getConfiguration().getParameters().get("qd.nearestplayableparent"))));
            Long t = qd.getTimeCodeModel().getOutTime();
            if (qd.getPlayer().isPlaying()) qd.getPlayer().cmd_stop();
            qd.getPlayer().setCurrentTime(t.longValue());
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
