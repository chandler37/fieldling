package fieldling.quilldriver.task;

import javax.swing.*;
import java.awt.print.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.config.Configuration;

public class ShowHelp extends BasicTask {
    public void execute(QD qd, String parameters) {
        final Configuration conf = qd.getConfiguration();
        JMenuItem saveAsPDF = new JMenuItem(qd.messages.getString("SaveAsPDF"));
        saveAsPDF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        JMenuItem printItem = new JMenuItem(qd.messages.getString("Print"));
        printItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    PrinterJob printJob = PrinterJob.getPrinterJob();
                    printJob.setJobName(conf.getName());
                    printJob.setCopies(1);
                    printJob.setPrintable(conf.getHelpAsPrintable());
                    if (printJob.printDialog() == false)
                        return;
                    printJob.print();
                } catch (PrinterException pe) {
                    pe.printStackTrace();
                }
            }
        });
        JMenu fmenu = new JMenu(qd.messages.getString("FileMenu"));
        fmenu.add(printItem);
        JMenuBar jbar = new JMenuBar();
        jbar.add(fmenu);
        JFrame f = new JFrame();
        f.setJMenuBar(jbar);
        f.getContentPane().add(conf.getHelpScrollPane());
        f.pack();
        //f.setSize(500, 400);
        f.setVisible(true);
    }
}

/*
I am developing a swing application and have used the XHTMLPanel from
the R6-pre1 release. Everything looks and works perfectly.

 

I would however, like to report an issue I have encountered with
printing using XHTMLPrintable. 

I found that when the print method is called an unhandled exception is
thrown with "Can't load the XML resource (using TRaX transformer)".  I
am trying to print a local XHTML file and discovered that the
setDocument() method of Graphics2DRenderer is passed a base url string
of the parent directory not the actual file itself, which is throwing
the exception. 

 

I have created a workaround by extending the class and adding a
constructor that accepts a File object. The toURL().toString() method of
the File object is then used when passing the base url string to the
Graphics2DRenderer object. 
*/
