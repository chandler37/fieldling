package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.xml.*;
import javax.xml.xpath.*;

public class PlayNode extends BasicTask {
    public void execute(QD qd, String parameters) {
        qd.playNode(XPathUtilities.saxonSelectSingleDOMNode(qd.getEditor().getNodeForOffset(qd.getEditor().getTextPane().getCaret().getMark()), (XPathExpression)(qd.getConfiguration().getParameters().get("qd.nearestplayableparent"))));
    }
}
