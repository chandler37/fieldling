package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;

public class PrintTranscript extends PrintingTask {
    public void execute(QD qd, String parameters) {
        final QD qdHold = qd;
        Thread runner = new Thread() {
            public void run() { 
                print(qdHold);
            }
        };
        runner.start();
    }
}
