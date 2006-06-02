package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;

public class PrintToRTF extends PrintingTask {
    public void execute(QD qd, String parameters) {
        try{
            RTFEditorKit rtf=new RTFEditorKit();
            JFileChooser chooser = new JFileChooser( );
            chooser.setDialogTitle(qd.messages.getString("PrintToRTF"));
            chooser.addChoosableFileFilter(new fieldling.quilldriver.OutputFileFilter());
            if (chooser.showSaveDialog(qd.getParentQDShell())!=JFileChooser.APPROVE_OPTION) return;
            File f = chooser.getSelectedFile( );
            FileOutputStream out = new FileOutputStream(f);
            StyledDocument doc=qd.getEditor().getTextPane().getStyledDocument();
            rtf.write(out,(Document)doc,0,doc.getLength());
        } catch(Exception e) {
            JOptionPane.showMessageDialog(qd.getParentQDShell(), qd.messages.getString("PrintFail"), qd.messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
