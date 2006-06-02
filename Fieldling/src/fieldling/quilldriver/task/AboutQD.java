package fieldling.quilldriver.task;

import javax.swing.*;
import java.io.IOException;
import fieldling.quilldriver.gui.QD;
import fieldling.util.GuiUtil;

public class AboutQD extends BasicTask {
    public JFrame f = null;
    
    public AboutQD() {
        try {
            final JScrollPane sp = GuiUtil.getScrollPaneForTextFile(this.getClass().getClassLoader(), "about.txt");
            f = new JFrame();
            f.setSize(500, 400);
            f.getContentPane().add(sp);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void execute(QD qd, String parameters) {
        if (f != null)
            f.setVisible(true);
    }
}
