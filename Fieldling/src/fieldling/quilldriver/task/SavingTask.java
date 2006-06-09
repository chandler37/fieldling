package fieldling.quilldriver.task;

import java.io.*;
import javax.swing.JOptionPane;
import fieldling.quilldriver.PreferenceManager;
import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.gui.QDShell;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.*;
import fieldling.quilldriver.config.Configuration;
import org.w3c.dom.Document;

public abstract class SavingTask extends BasicTask {
    private static final String BACKUP_PREFIX = "qd_bak_";
    private static final String BACKUP_SUFFIX = ".xml";
    private static final String STYLESHEET_FILE_NAME = "normalize-namespaces.xsl";
    private static ClassLoader loader = QD.class.getClassLoader();
    private static Transformer namespaceNormalizer;
    
    static {
        try {
            namespaceNormalizer = Configuration.getTransformerFactory().newTransformer(new StreamSource(loader.getResourceAsStream(STYLESHEET_FILE_NAME)));
        } catch (TransformerException trex) {
            trex.printStackTrace();
        }
    }
        
    public abstract void execute(QD qd, String parameters);
  
    //DOM FIX!!!
    public boolean saveTranscript(QD qd) {
        if (qd.transcriptFile == null)
            return true;
        try {
            String backupDirectory = PreferenceManager.getValue(PreferenceManager.BACKUP_DIRECTORY_KEY, new String(""));
            if (!backupDirectory.equals("")) {
                int nextId = PreferenceManager.getInt(PreferenceManager.NEXT_BACKUP_ID_KEY, 0) + 1;
                File backupFile = new File(backupDirectory + QDShell.FILE_SEPARATOR + BACKUP_PREFIX + String.valueOf(nextId) + BACKUP_SUFFIX);
                try {
                    copyFile(qd.transcriptFile, backupFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PreferenceManager.setInt(PreferenceManager.NEXT_BACKUP_ID_KEY, nextId);
            }
            if (PreferenceManager.getInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, -1) == 1) { //normalize namespaces
                namespaceNormalizer.transform(new DOMSource(qd.getEditor().getXMLDocument()), new StreamResult(qd.transcriptFile));
            } else { //save as usual
                //serialize XML to file, prettified with indents, and encoded as Unicode (UTF-8)
                org.apache.xml.serialize.OutputFormat formatter = new org.apache.xml.serialize.OutputFormat("xml", "utf-8", true);
		formatter.setPreserveSpace(true); //so as not to remove text nodes that consist only of whitespace, which are significant to QD
		formatter.setLineWidth(0); //prevents line-wrapping (so as not to introduce element-internal whitespace)
		org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(formatter);
		FileOutputStream fous = new FileOutputStream(qd.transcriptFile);
		ser.setOutputByteStream(fous);
		org.apache.xml.serialize.DOMSerializer domser = ser.asDOMSerializer();
		domser.serialize(qd.getEditor().getXMLDocument());                
            }
            qd.getEditor().resetChangedSinceLastSaved();
	    return true;
        } catch (TransformerException trex) {
            trex.printStackTrace();
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }
    
    public static void copyFile(File original, File copy) throws FileNotFoundException, IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(original));
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(copy));
        int b;
        while ( (b=is.read()) != -1) {
            os.write(b);
        }
        is.close();
        os.close();
    }
}
