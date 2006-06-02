package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.xml.DocumentRenderer;
import java.awt.print.*;     
import javax.print.*;        
import javax.print.event.*;  
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;

public abstract class PrintingTask extends BasicTask {
        public abstract void execute(QD qd, String parameters);
	
	public void print(QD qd) {
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintService[  ] services =PrintServiceLookup.lookupPrintServices(flavor, null);
		PrintRequestAttributeSet printAttributes =new HashPrintRequestAttributeSet( );
		PrintService service = ServiceUI.printDialog(null, 100, 100,services, null, null,printAttributes);
		// If the user canceled, don't do anything
		if (service == null) return;
		printDocument(qd, service, printAttributes);
	}
	public void printDocument(QD qd, PrintService service,PrintRequestAttributeSet printAttributes)
	{
		DocumentRenderer printable = new DocumentRenderer(qd.getEditor().getTextPane().getStyledDocument());
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		Doc doc = new SimpleDoc(printable, flavor, null);
		DocPrintJob job = service.createPrintJob();
		
		// Set up a dialog box to  monitor printing status
		final JOptionPane pane = new JOptionPane(qd.messages.getString("Printing"),JOptionPane.PLAIN_MESSAGE);
		JDialog dialog = pane.createDialog(qd.getParentQDShell(), qd.messages.getString("PrintStatus"));
		// This listener object updates the dialog as the status changes
		job.addPrintJobListener(new PrintJobAdapter( ) {
			public void printJobCompleted(PrintJobEvent e) {
				pane.setMessage(QD.messages.getString("PrintingComplete"));
			}
			public void printDataTransferCompleted(PrintJobEvent e) {
				pane.setMessage(QD.messages.getString("DocumentTransferedToPrinter"));
			}
			public void printJobRequiresAttention(PrintJobEvent e) {
				pane.setMessage(QD.messages.getString("OutOfPaper"));
			}
			public void printJobFailed(PrintJobEvent e) {
				pane.setMessage(QD.messages.getString("PrintFailed"));
			}
		});
		
		// Show the dialog, non-modal.
		dialog.setModal(false);
		dialog.setVisible(true);
		
		// Now print the Doc to the DocPrintJob
		try {
			job.print(doc, printAttributes);
		}
		catch(PrintException e) {
			// Display any errors to the dialog box
			pane.setMessage(e.toString( ));
		}
	}
}
