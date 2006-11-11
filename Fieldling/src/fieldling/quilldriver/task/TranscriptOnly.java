package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class TranscriptOnly extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.videoFrame.setSize(0,0);
        qd.videoFrame.setLocation(0,0);
        qd.textFrame.setLocation(0,0);
        qd.textFrame.setSize(qd.getSize().width, qd.getSize().height);
    }
}
