package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import java.io.*;
import javax.swing.*;
import javax.print.*;

public class PrintToPS extends PrintingTask {
    public void execute(QD qd, String parameters) {
        try {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            String format = "application/postscript";// so far only support ps output
            StreamPrintServiceFactory factory = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, format)[0];
            JFileChooser chooser = new JFileChooser( );
            chooser.setDialogTitle(qd.messages.getString("PrintToPS"));
            chooser.addChoosableFileFilter(new fieldling.quilldriver.OutputFileFilter());
            if (chooser.showSaveDialog(qd.getParentQDShell())!=JFileChooser.APPROVE_OPTION) return;
            File f = chooser.getSelectedFile( );
            FileOutputStream out = new FileOutputStream(f);
            StreamPrintService service = factory.getPrintService(out);
            printDocument(qd, service, null);
            out.close( );
        } catch(Exception e) {
            JOptionPane.showMessageDialog(qd.getParentQDShell(), qd.messages.getString("PrintFail"), qd.messages.getString("Alert"), JOptionPane.ERROR_MESSAGE);   
        }
    }
}
