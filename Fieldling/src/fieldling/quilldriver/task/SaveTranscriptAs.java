package fieldling.quilldriver.task;

import java.io.*;
import javax.swing.*;
import fieldling.quilldriver.*;
import fieldling.quilldriver.QDFileFilter;
import fieldling.quilldriver.gui.QD;

public class SaveTranscriptAs extends SavingTask {
    public void execute(QD qd, String parameters) {
        boolean result = true;
        if (qd.getEditor().isEditable()) {
            JFileChooser fd=new JFileChooser(new File(PreferenceManager.getValue(PreferenceManager.WORKING_DIRECTORY_KEY, System.getProperty("user.home"))));
            fd.setDialogTitle(qd.messages.getString("SaveAs"));
            fd.addChoosableFileFilter(new QDFileFilter());
            try {
                int action=fd.showSaveDialog(qd.getParentQDShell());                      
		if (action==JFileChooser.APPROVE_OPTION){
                    File oldFile=qd.transcriptFile;
                    File newFile=fd.getSelectedFile();
                    qd.transcriptFile=newFile;                              
                    saveTranscript(qd);
                    //makeRecentlyOpened(newFile.getAbsolutePath(), qd.player.getMediaURL().toString());
                }
            } catch (Exception ex) {
                System.out.println("Errors are occured when saving: "+ex.toString());
                JOptionPane.showMessageDialog(qd.getParentQDShell(), qd.messages.getString("SaveError"),"Alert",JOptionPane.ERROR_MESSAGE);
            }
        } else
            JOptionPane.showMessageDialog(qd, qd.getMessages().getString("OnlyTranscriptionMode"), qd.getMessages().getString("Alert"), JOptionPane.ERROR_MESSAGE);
    }
}
