package fieldling.quilldriver.task;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import fieldling.quilldriver.*;
import fieldling.quilldriver.gui.*;

public abstract class OpeningTask extends BasicTask {
    public abstract void execute(QD qd, String parameters);
    
    public static File selectTranscriptFile(String message, QD qd) {
        return selectFileOrDirectory(JFileChooser.FILES_ONLY, message, qd);
    }
    
    public static File selectFileOrDirectory(int fileSelectionMode, String message, QD qd) {
        JFileChooser fc = new JFileChooser(new File(PreferenceManager.getValue(PreferenceManager.WORKING_DIRECTORY_KEY, PreferenceManager.WORKING_DIRECTORY_DEFAULT)));
        fc.setFileSelectionMode(fileSelectionMode);
        fc.addChoosableFileFilter(new QDFileFilter());
	if (fc.showDialog(qd, message) == JFileChooser.APPROVE_OPTION) {
            File transcriptFile = fc.getSelectedFile();
            return transcriptFile;
	} else {
            return null;
	}
    }
    public static void open(File transcriptFile, QD qd) {
        QDShell qdShell = qd.getParentQDShell();
        if (!qdShell.hasLoadedTranscript) {
            String transcriptString = transcriptFile.getAbsolutePath();
            PreferenceManager.setValue(PreferenceManager.WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(QDShell.FILE_SEPARATOR) + 1));
            if (qd.loadTranscript(transcriptFile)) { //success!
                qdShell.activateQD(qd, true);
            }
        } else {
            String transcriptString = transcriptFile.getAbsolutePath();
            PreferenceManager.setValue(PreferenceManager.WORKING_DIRECTORY_KEY, transcriptString.substring(0, transcriptString.lastIndexOf(QDShell.FILE_SEPARATOR) + 1));
            QD newQD = new QD(QD.configuration, qdShell.getMediaPlayer("fieldling.mediaplayer.QT4JPlayer"));
            if (newQD.loadTranscript(transcriptFile)) { //success!
                qdShell.deActivateQD(qd);
                qdShell.activateQD(newQD, true);
            }
        }
    }
}
