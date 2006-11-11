package fieldling.quilldriver.task;

import java.awt.*;
import javax.swing.JComponent;
import fieldling.quilldriver.gui.QD;

public class DoubleSize extends BasicTask {
    public void execute(QD qd, String parameters) {
        Dimension actualSize = qd.getMediaPlayer().getPreferredSize();
        JComponent frameBar = ((javax.swing.plaf.basic.BasicInternalFrameUI)qd.videoFrame.getUI()).getNorthPane();
        Insets frameInsets = qd.videoFrame.getInsets();
        int newWidth = frameInsets.left + actualSize.width * 2 + frameInsets.right;
        int newHeight = frameInsets.top + frameBar.getSize().height + actualSize.height * 2 + frameInsets.bottom;
        qd.videoFrame.setSize(new Dimension(newWidth, newHeight));
        //qd.videoFrame.pack();
        WindowPositioningTask.repositionWithActiveWindowPositioner(qd);
    }
}
