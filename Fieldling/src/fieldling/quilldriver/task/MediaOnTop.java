package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class MediaOnTop extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.videoFrame.pack();
        qd.videoFrame.setLocation(qd.getSize().width/2 - qd.videoFrame.getSize().width/2, 0);
	qd.textFrame.setLocation(0,qd.videoFrame.getSize().height);
	qd.textFrame.setSize(qd.getSize().width, qd.getSize().height-qd.videoFrame.getSize().height);
    }
}
