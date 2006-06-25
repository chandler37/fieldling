package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import javax.swing.*;
import java.awt.*;

public class SetTimeCodingPreferences extends BasicTask {
    public void execute(QD qd, String parameters) {
		//allows user to change slow adjust, rapid adjust, and play minus parameters
		/*JPanel slowAdjustPanel = new JPanel(new BorderLayout());
		slowAdjustPanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("SlowIncreaseDecreaseValue")));
		JTextField slowAdjustField = new JTextField(String.valueOf(PreferenceManager.slow_adjust));
		slowAdjustField.setPreferredSize(new Dimension(240, 30));
		slowAdjustPanel.add(slowAdjustField);
		JPanel rapidAdjustPanel = new JPanel(new BorderLayout());
		rapidAdjustPanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("RapidDncreaseDecreaseValue")));
		JTextField rapidAdjustField = new JTextField(String.valueOf(PreferenceManager.rapid_adjust));
		rapidAdjustField.setPreferredSize(new Dimension(240, 30));
		rapidAdjustPanel.add(rapidAdjustField);
		JPanel playMinusPanel = new JPanel(new BorderLayout());
		playMinusPanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("PlayVinusValue")));
		JTextField playMinusField = new JTextField(String.valueOf(PreferenceManager.play_minus));
		playMinusField.setPreferredSize(new Dimension(240, 30));
		playMinusPanel.add(playMinusField);
		JPanel preferencesPanel = new JPanel();
		preferencesPanel.setLayout(new GridLayout(3, 1));
		preferencesPanel.add(slowAdjustPanel);
		preferencesPanel.add(rapidAdjustPanel);
		preferencesPanel.add(playMinusPanel);
		JOptionPane pane = new JOptionPane(preferencesPanel);
		JDialog dialog = pane.createDialog(qd, qd.messages.getString("TimeCodingPreferences"));
		// This returns only when the user has closed the dialog
		dialog.show();
		int old_slow_adjust = PreferenceManager.slow_adjust;
		try {
			PreferenceManager.slow_adjust = Integer.parseInt(slowAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
		int old_rapid_adjust = PreferenceManager.rapid_adjust;
		try {
			PreferenceManager.rapid_adjust = Integer.parseInt(rapidAdjustField.getText());
		} catch (NumberFormatException ne) {
		}
		int old_play_minus = PreferenceManager.play_minus;
		try {
			PreferenceManager.play_minus = Integer.parseInt(playMinusField.getText());
		} catch (NumberFormatException ne) {
		}
		// note: if these become negative numbers no error
		if (old_slow_adjust != PreferenceManager.slow_adjust)
			PreferenceManager.setInt(PreferenceManager.SLOW_ADJUST_KEY, PreferenceManager.slow_adjust);
		if (old_rapid_adjust != PreferenceManager.rapid_adjust)
                        PreferenceManager.setInt(PreferenceManager.RAPID_ADJUST_KEY, PreferenceManager.rapid_adjust);
		if (old_play_minus != PreferenceManager.play_minus)
			PreferenceManager.setInt(PreferenceManager.PLAY_MINUS_KEY, PreferenceManager.play_minus);*/
    }
}
