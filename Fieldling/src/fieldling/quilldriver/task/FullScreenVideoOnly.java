package fieldling.quilldriver.task;

import java.awt.Dimension;
import fieldling.quilldriver.gui.QD;

public class FullScreenVideoOnly extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.videoFrame.setLocation(0,0);
        qd.videoFrame.setSize(qd.getSize().width, qd.getSize().height);
        qd.textFrame.setSize(0,0);
        qd.textFrame.setLocation(0,0);
    }
}
