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

public class Userinfo extends JFrame  {
	public JTextField fname,lname;
	public JButton okbtn,cancelbtn;
	public JLabel fnamelbl, lnamelbl;
	public JPanel name;
	public static String FirstName;
	public static String LastName;

	public Userinfo()
	{
			setTitle("User Information");
			setSize(275,225);
			name = new JPanel();
			name.setLayout(null);
			setContentPane(name);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			fname=new JTextField("");
			lname=new JTextField("");
			fname.setSize(150,30);
			lname.setSize(150,30);
			fname.setLocation(100,50);
			lname.setLocation(100,100);
			name.add(fname);
			name.add(lname);

			//labels
			fnamelbl=new JLabel("First Name");
			lnamelbl=new JLabel("Last Name");
			fnamelbl.setSize(100,100);
			lnamelbl.setSize(100,100);
			fnamelbl.setLocation(10,25);
			lnamelbl.setLocation(10,70);
			name.add(fnamelbl);
			name.add(lnamelbl);

			// buttons
			okbtn=new JButton("OK");
			okbtn.setSize(70,25);
			okbtn.addActionListener(new okButtonListener());

			cancelbtn=new JButton("Cancel");
			cancelbtn.setSize(100,25);
			cancelbtn.addActionListener(new cancelButtonListener());


			okbtn.setLocation(75,150);
			cancelbtn.setLocation(150,150);
			name.add(okbtn);
			name.add(cancelbtn);

	}


	class okButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			FirstName = fname.getText();
			LastName = lname.getText();
			JOptionPane.showMessageDialog(null,"Thank You " + FirstName +" "+LastName + " for entering your information");
			dispose();

		}
	}

	class cancelButtonListener implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				hide();
			}
	}


}