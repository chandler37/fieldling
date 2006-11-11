package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import javax.swing.*;
import java.awt.Component;

public class MediaOverTranscript extends WindowPositioningTask {
    public void repositionWindows(QD qd) {
        qd.textFrame.setLocation(0, 0);
        qd.textFrame.setSize(qd.getSize());
        //qd.videoFrame.pack();
        qd.videoFrame.setLocation(qd.hp.text.getSize().width - qd.videoFrame.getSize().width, qd.hp.getY());
    }
}
