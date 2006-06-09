package fieldling.quilldriver.task.que;

import java.io.File;
import javax.swing.JOptionPane;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import fieldling.quilldriver.task.BasicTask;
import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.config.Configuration;

public class ExportToDelimitedText extends BasicTask {
    private static final String STYLESHEET_FILE_NAME = "export-quechua.xsl";
    private static ClassLoader loader = QD.class.getClassLoader();
    private static Transformer textExporter;
    
    static {
        try {
            textExporter = Configuration.getTransformerFactory().newTransformer(new StreamSource(loader.getResourceAsStream(STYLESHEET_FILE_NAME)));
        } catch (TransformerException trex) {
            trex.printStackTrace();
        }
    }
        
    public void execute(QD qd, String parameters) {
        if (qd.transcriptFile == null)
            return;
        try {
            textExporter.transform(new DOMSource(qd.getEditor().getXMLDocument()), new StreamResult(XMLFileToTextFile(qd.transcriptFile)));
        } catch (TransformerException trex) {
            trex.printStackTrace();
        }
    }
    
    public static File XMLFileToTextFile(File xmlFile) {
        String xmlPath = xmlFile.getAbsolutePath();
        if (xmlPath.substring(xmlPath.length()-4).equals(".xml"))
            return new File(xmlPath.substring(0, xmlPath.length()-4) + ".txt");
        else
            return new File(xmlPath + ".txt");
    }
}
