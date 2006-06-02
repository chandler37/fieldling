package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import java.util.*;
import java.awt.event.*;

public abstract class WindowPositioningTask extends BasicTask {
    public static WindowPositioningTask windowsMode;
    static Set qdList = new HashSet();
    
    public abstract void repositionWindows(QD qd);
    
    public void execute(QD qd, String parameters) {
        registerComponentListenerForQD(qd);
        repositionWindows(qd);
        windowsMode = this;
        PreferenceManager.setValue(PreferenceManager.WINDOW_MODE_KEY, this.getClass().getName());
    }
    
    public static void registerComponentListenerForQD(final QD qd) {
        if (!qdList.contains(qd)) {
            qdList.add(qd);
            qd.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent ce) {
                    windowsMode.repositionWindows(qd);
                }
            });
        }
    }
}
