package fieldling.quilldriver;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import java.util.prefs.*;

import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;


public class UserFeedback {

	private static Preferences myPrefs;
	public static final String USER_NAME = "USER_NAME";
	public static final String EMAIL_ADDR = "EMAIL_ADDRESS";
	public static final String SMTP_ADDR = "SMTP_SERVER";


	public static final String[] MODULE_NAME = {"QuillDriver"};
	public static final String[] FEEDBACK_TYPES = {"Bug", "Feature Request", "Inquiry"};
	public static final String[] PRIORITY_LEVELS = {"High", "Medium", "Low"};
	public static String name;
	public static String email;
	public static String smtp;
	public String moduleselected,feedbackselected,priorityselected,subject;
	public JTextField smtpField,emailField,nameField;
	public JComboBox module,feedbackType,priority;

	public UserFeedback(JFrame f)
	{
			myPrefs = Preferences.userNodeForPackage(UserFeedback.class);
			nameField=new JTextField((myPrefs.get(USER_NAME,"<Enter name>")));
			nameField.setBorder(BorderFactory.createTitledBorder("Name"));
			nameField.setSize(150,30);
			emailField = new JTextField((myPrefs.get(EMAIL_ADDR,"<Enter Email>")));
			emailField.setBorder(BorderFactory.createTitledBorder("Email Address"));
			emailField.setSize(150,30);
			smtpField = new JTextField((myPrefs.get(SMTP_ADDR,"<Enter Mail Server>")));
			smtpField.setBorder(BorderFactory.createTitledBorder("Outgoing Mail Server"));
			smtpField.setSize(150,30);
			module = new JComboBox(MODULE_NAME);
			feedbackType = new JComboBox(FEEDBACK_TYPES);
			priority = new JComboBox(PRIORITY_LEVELS);


			JTextArea feedbackText = new JTextArea();
			feedbackText.setSize(150,100);

			JPanel northPanel = new JPanel(new GridLayout(1,0));

			JPanel leftPanel = new JPanel(new GridLayout(0,1));
			leftPanel.add(nameField);
			leftPanel.add(emailField);
			leftPanel.add(smtpField);

			JPanel rightPanel = new JPanel(new GridLayout(3,3));
			rightPanel.add(new JLabel("Affected module: "));
			rightPanel.add(module);
			rightPanel.add(new JLabel("Type of feedback: "));
			rightPanel.add(feedbackType);
			rightPanel.add(new JLabel("Priority: "));
			rightPanel.add(priority);

			northPanel.add(leftPanel);
			northPanel.add(rightPanel);

			JPanel centerPanel = new JPanel(new BorderLayout());
			centerPanel.setPreferredSize(new Dimension(150,150));
			centerPanel.add(new JScrollPane(feedbackText), BorderLayout.CENTER);
			centerPanel.setBorder(BorderFactory.createTitledBorder("Type Message Here"));

			JPanel namePanel = new JPanel();
			namePanel.setLayout(new BorderLayout());
			namePanel.add(northPanel, BorderLayout.NORTH);
			namePanel.add(centerPanel, BorderLayout.CENTER);

		 JOptionPane pane = new JOptionPane(namePanel);
		 JDialog dialog = pane.createDialog(f, "Feedback to Developers");
		 // This returns only when the user has closed the dialog
		 dialog.show();

		 name = nameField.getText();
		 email = emailField.getText();
		 smtp = smtpField.getText();
		 int feedbackCategory = feedbackType.getSelectedIndex();
		 String textToEmail = feedbackText.getText();

		 myPrefs.put(USER_NAME, name);
		 myPrefs.put(EMAIL_ADDR, email);
		 myPrefs.put(SMTP_ADDR, smtp);

		 msgsend();

	}

	void msgsend()
	    {

			String  to, subject = null, from = null;
			String mailhost = null;
			String mailer = "msgsend";
			String protocol = null, host = null, user = null, password = null;


			try {

					moduleselected=(String)module.getSelectedItem();
					feedbackselected=(String)feedbackType.getSelectedItem();
					priorityselected=(String)priority.getSelectedItem();

					to="fieldling-feedback@cvs.sourceforge.net";
					from=emailField.getText();
					user=nameField.getText();
					mailhost=smtpField.getText();


					subject = CreateSubject(user,moduleselected,feedbackselected,priorityselected);
					//System.out.println("Subject: " + subject);


					Properties props = System.getProperties();
					// XXX - could use Session.getTransport() and Transport.connect()
					// XXX - assume we're using SMTP
					if (mailhost != null)
					props.put("mail.smtp.host", mailhost);

					// Get a Session object
					Session session = Session.getInstance(props, null);


					// construct the message
					Message msg = new MimeMessage(session);
					if (from != null)
					msg.setFrom(new InternetAddress(from));
					else
					msg.setFrom();

					msg.setRecipients(Message.RecipientType.TO,
								InternetAddress.parse(to, false));

					msg.setSubject(subject);


					msg.setHeader("X-Mailer", mailer);
					msg.setSentDate(new Date());

					// send the message
					Transport.send(msg);

					System.out.println("\nMail was sent successfully.");

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
   }

	String CreateSubject(String user,String module,String feedback,String priority)
	   {
		   Calendar c=Calendar.getInstance();
		   subject=new String("");
		   StringBuffer sb=new StringBuffer(subject);
		   sb.append(user.substring(0,2));

			Random rn=new Random();
			sb.append(String.valueOf(rn.nextInt()));

			sb.append(" ");
			sb.append((c.getTime()).toString());

			sb.append(" Feedback of type " + feedback + " requested for module "+ module + " with priority "+priority);
			return(sb.toString());


   }
}
