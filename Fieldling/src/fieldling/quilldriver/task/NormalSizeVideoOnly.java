package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class NormalSizeVideoOnly extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.textFrame.setSize(0,0);
	qd.textFrame.setLocation(0,0);
        qd.videoFrame.pack();
        qd.videoFrame.setLocation(qd.getSize().width/2 - qd.videoFrame.getSize().width/2, qd.getSize().height/2 - qd.videoFrame.getSize().height/2);
    }
}
