package fieldling.quilldriver.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import fieldling.util.*;
import fieldling.mediaplayer.*;
import fieldling.quilldriver.gui.*;

public class TimeCodeView extends JPanel implements TimeCodeModelListener, SimpleSpinnerListener {
	TimeCodeModel tcm;
	JTextField currentTimeField;
	SimpleSpinner startSpinner, stopSpinner;
	long currentTime=-1, startTime=-1, stopTime=-1;
	final int TEXT_WIDTH, TEXT_HEIGHT;
	
	TimeCodeView(final PanelPlayer player, TimeCodeModel time_model) {
		tcm = time_model;
		JButton inButton = new JButton(new ImageIcon(QD.class.getResource("right-arrow.gif")));
		JButton outButton = new JButton(new ImageIcon(QD.class.getResource("left-arrow.gif")));
		inButton.setBorder(null);
		outButton.setBorder(null);
		inButton.setPreferredSize(new Dimension(inButton.getIcon().getIconWidth(), inButton.getIcon().getIconHeight()));
		outButton.setPreferredSize(new Dimension(outButton.getIcon().getIconWidth(), outButton.getIcon().getIconHeight()));
		inButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long t = player.getCurrentTime();
				if (t != -1) {
					setStartTime(t);
					tcm.setTimeCodes(t, stopTime, tcm.getCurrentNode());
				}
			}
		});
		outButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				long t = player.getCurrentTime();
				if (t != -1) {
					setStopTime(t);
					tcm.setTimeCodes(startTime, t, tcm.getCurrentNode());
					try {
						player.cmd_stop();
					} catch (PanelPlayerException smpe) {
						smpe.printStackTrace();
					}
				}
			}
		});
		TEXT_WIDTH = 60;
		TEXT_HEIGHT = inButton.getPreferredSize().height;
		currentTimeField = new JTextField();
		currentTimeField.setEditable(false);
		currentTimeField.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
		startSpinner = new fieldling.util.SimpleSpinner();
		stopSpinner = new fieldling.util.SimpleSpinner();
		startSpinner.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
		stopSpinner.setPreferredSize(new Dimension(TEXT_WIDTH, TEXT_HEIGHT));
		startSpinner.addSimpleSpinnerListener(this);
		stopSpinner.addSimpleSpinnerListener(this);
		setCurrentTime(0);
		setStartTime(0);
		setStopTime(0);
		setLayout(new BorderLayout(0,0));
		JPanel jp_center = new JPanel(new FlowLayout(FlowLayout.LEFT,2,0));
		jp_center.add(inButton);
		jp_center.add(startSpinner);
		jp_center.add(currentTimeField);
		jp_center.add(stopSpinner);
		jp_center.add(outButton);
		add("Center", jp_center);
		tcm.addTimeCodeModelListener(this);
	}
	
	public void valueChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj == startSpinner) startTime = startSpinner.getValue().longValue();
		else if (obj == stopSpinner) stopTime = stopSpinner.getValue().longValue();
		//if (obj == startSpinner) startTime = startSpinner.getValue().intValue();
		//else if (obj == stopSpinner) stopTime = stopSpinner.getValue().intValue();
		tcm.setTimeCodes(startTime, stopTime, tcm.getCurrentNode());
	}
	void setCurrentTime(long t) {
		if (t != currentTime) {
			currentTime = t;
			currentTimeField.setText(String.valueOf(new Long(t)));
		}
	}
	public void setStartTime(long t) {
		if (t != startTime) {
			startTime = t;
			startSpinner.setValue(new Long(t));
		}
	}
	public void setStopTime(long t) {
		if (t != stopTime) {
			stopTime = t;
			stopSpinner.setValue(new Long(t));
		}
	}
}

