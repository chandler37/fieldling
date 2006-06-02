package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class MediaToLeft extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.videoFrame.pack();
        qd.videoFrame.setLocation(0,0);
        qd.textFrame.setSize(qd.getSize().width-qd.videoFrame.getSize().width,qd.getSize().height);
        qd.textFrame.setLocation(qd.videoFrame.getSize().width,0);
    }
}
