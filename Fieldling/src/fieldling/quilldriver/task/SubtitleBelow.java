package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class SubtitleBelow extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.videoFrame.setLocation(0, 0);
        qd.videoFrame.setSize(qd.getSize().width, qd.getSize().height - qd.getSize().height/4);
	qd.textFrame.setLocation(0,qd.videoFrame.getSize().height);
	qd.textFrame.setSize(qd.getSize().width, qd.getSize().height-qd.videoFrame.getSize().height);
    }
}
