package fieldling.quilldriver.task;

import java.awt.*;
import javax.swing.JComponent;
import fieldling.quilldriver.gui.QD;

public class FitToWindow extends BasicTask {
    public void execute(QD qd, String parameters) {
        Dimension actualSize = qd.getMediaPlayer().getPreferredSize();
        JComponent frameBar = ((javax.swing.plaf.basic.BasicInternalFrameUI)qd.videoFrame.getUI()).getNorthPane();
        Insets frameInsets = qd.videoFrame.getInsets();
        int newVideoHeight = qd.getSize().height - frameInsets.top - frameBar.getSize().height - frameInsets.bottom;
        int newVideoWidth = newVideoHeight * actualSize.width / actualSize.height;
        int newFrameWidth = frameInsets.left + newVideoWidth + frameInsets.right;
        qd.videoFrame.setLocation((qd.getSize().width - newFrameWidth)/2, 0);
        qd.videoFrame.setSize(newFrameWidth, qd.getSize().height);
        qd.textFrame.setSize(0,0);
        qd.textFrame.setLocation(0,0);
    }
}
