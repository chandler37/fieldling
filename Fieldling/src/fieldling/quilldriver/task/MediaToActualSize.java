package fieldling.quilldriver.task;

import java.awt.Dimension;
import fieldling.quilldriver.gui.QD;

public class MediaToActualSize extends BasicTask {
    public void execute(QD qd, String parameters) {
        qd.videoFrame.pack();
        WindowPositioningTask.repositionWithActiveWindowPositioner(qd);
    }
}
