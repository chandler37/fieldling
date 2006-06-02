package fieldling.quilldriver.task;

import javax.swing.JFrame;
import fieldling.quilldriver.gui.QD;

public class ShowHelp extends BasicTask {
    public void execute(QD qd, String parameters) {
        JFrame f = new JFrame();
        f.setSize(500, 400);
        f.getContentPane().add(qd.getConfiguration().getHelpScrollPane());
        f.setVisible(true);
    }
}
