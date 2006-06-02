package fieldling.quilldriver.task;

import java.io.*;
import java.net.URL;
import javax.swing.*;
import fieldling.quilldriver.*;
import fieldling.quilldriver.gui.*;

public class NewTranscript extends OpeningTask {
    public void execute(QD qd, String parameters) {
        try {
            QDShell qdShell = qd.getParentQDShell();
            URL newTemplateURL = qdShell.getClass().getClassLoader().getResource(QD.configuration.getNewTemplate());
            if (newTemplateURL == null)
                return;
            File saveAsFile = selectTranscriptFile(qd.messages.getString("SaveTranscriptAs"), qd);
            if (saveAsFile!=null) {
                InputStream in = newTemplateURL.openStream();
                OutputStream out = new FileOutputStream(saveAsFile);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                open(saveAsFile, qd);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
