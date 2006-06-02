package fieldling.quilldriver.task;

import java.io.*;
import javax.swing.JOptionPane;
import fieldling.quilldriver.gui.QD;

public abstract class SavingTask extends BasicTask {
    public abstract void execute(QD qd, String parameters);
  
    //DOM FIX!!!
    public boolean saveTranscript(QD qd) {
        if (qd.transcriptFile == null)
            return true;
        try { //serialize XML to file, prettified with indents, and encoded as Unicode (UTF-8)
            org.apache.xml.serialize.OutputFormat formatter = new org.apache.xml.serialize.OutputFormat("xml", "utf-8", true);
            formatter.setPreserveSpace(true); //so as not to remove text nodes that consist only of whitespace, which are significant to QD
	    formatter.setLineWidth(0); //prevents line-wrapping (so as not to introduce element-internal whitespace)
            org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(formatter);
            FileOutputStream fous = new FileOutputStream(qd.transcriptFile);
            ser.setOutputByteStream(fous);
            org.apache.xml.serialize.DOMSerializer domser = ser.asDOMSerializer();
            domser.serialize(qd.getEditor().getXMLDocument());
            qd.getEditor().resetChangedSinceLastSaved();
	    return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }
}
