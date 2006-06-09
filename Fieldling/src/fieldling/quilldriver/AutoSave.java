package fieldling.quilldriver;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.TranscriptToggler;
import fieldling.quilldriver.task.BasicTask;

public class AutoSave extends Thread {
    private boolean done;
    private long delay;
    private TranscriptToggler toggler;
    
    private static BasicTask saveTask;
    
    static {
        try {
            saveTask = BasicTask.getTaskForClass("fieldling.quilldriver.task.SaveAllTranscripts");
        } catch (Exception e) {
            e.printStackTrace();
            saveTask = null;
        }
    }
    
    public AutoSave(TranscriptToggler toggler, long delay) {
        this.toggler = toggler;
        done = false;
        setDelay(delay);
    }
    
    public void setDelay(long delay) {
        if (delay <= 0) {
            done = true;
        } else {
            this.delay = delay;
        }
    }
    
    public void run() {
        while (!done) {
            try {
                sleep(delay);
            } catch (InterruptedException ie) {
            }
            if (toggler.getNumberOfTranscripts() > 0) {
                QD qd = toggler.getQDForIndex(0);
                if (saveTask != null)
                    saveTask.execute(qd, null);
            }
        }
    }
}
