package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.border.Border;
import javax.swing.*;

public abstract class WindowPositioningTask extends BasicTask {
    private static Border internalFrameBorder;
    static {
        JInternalFrame jf = new JInternalFrame();
        internalFrameBorder = jf.getBorder();
    }
    
    public static WindowPositioningTask windowsMode;
    static Set qdList = new HashSet();
    
    public abstract void repositionWindows(QD qd);
    
    public void execute(QD qd, String parameters) {
        registerComponentListenerForQD(qd);
  /*      if (qd.videoFrame.getBorder() == null && PreferenceManager.getInt(PreferenceManager.VIDEO_HAS_BORDER_KEY, PreferenceManager.VIDEO_HAS_BORDER_DEFAULT) == 1) {
            qd.videoFrame.setBorder(internalFrameBorder);
            qd.videoFrame.setResizable(true);
            /*JTextArea text = new JTextArea(qd.messages.getString("DragToResize"));
            text.setOpaque(false);
            text.setEnabled(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
    		qd.videoFrame.getContentPane().add(text, BorderLayout.SOUTH, 1);*/
  /*      } else if (qd.videoFrame.getBorder() != null && PreferenceManager.getInt(PreferenceManager.VIDEO_HAS_BORDER_KEY, PreferenceManager.VIDEO_HAS_BORDER_DEFAULT) == -1) {
            qd.videoFrame.setBorder(null);
            ((javax.swing.plaf.basic.BasicInternalFrameUI)qd.videoFrame.getUI()).setNorthPane(null);
            // the index of 1 just makes reference to the above one.
            //qd.videoFrame.getContentPane().remove(1);
        }*/
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
    
    public static void repositionWithActiveWindowPositioner(QD qd) {
        String windowPositioningClassName = PreferenceManager.getValue(PreferenceManager.WINDOW_MODE_KEY, PreferenceManager.WINDOW_MODE_DEFAULT);
        try {
            Class c = Class.forName(windowPositioningClassName);
            if (BasicTask.tasksOnOffer.containsKey(c) && BasicTask.tasksOnOffer.get(c) instanceof WindowPositioningTask) {
                WindowPositioningTask wTask = (WindowPositioningTask)BasicTask.tasksOnOffer.get(c);
                wTask.execute(qd, null);
            }
        } catch (Exception e1) {
            try {
                Class c = Class.forName(PreferenceManager.WINDOW_MODE_DEFAULT);
                WindowPositioningTask wTask = (WindowPositioningTask)BasicTask.tasksOnOffer.get(c);
                wTask.execute(qd, null);
            } catch (Exception e2) {
            }
        }
    }
}
