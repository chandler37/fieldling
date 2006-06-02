package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import fieldling.mediaplayer.PanelPlayerException;
import fieldling.quilldriver.xml.*;
import javax.xml.xpath.*;

public class PlayEdge extends BasicTask {
    public void execute(QD qd, String parameters) {
        try {
            qd.getTimeCodeModel().setNode(XPathUtilities.saxonSelectSingleDOMNode(qd.getEditor().getNodeForOffset(qd.getEditor().getTextPane().getCaret().getMark()), (XPathExpression)(qd.getConfiguration().getParameters().get("qd.nearestplayableparent"))));
            Long t2 = qd.getTimeCodeModel().getOutTime();
            long t1 = t2.longValue() - PreferenceManager.play_minus;
            if (t1 < 0) t1 = 0;
            qd.getPlayer().cmd_playSegment(new Long(t1), t2);
        } catch (PanelPlayerException ppe) {
            ppe.printStackTrace();
        }
    }
}
