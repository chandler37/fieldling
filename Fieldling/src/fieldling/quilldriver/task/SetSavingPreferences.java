package fieldling.quilldriver.task;

import fieldling.quilldriver.gui.QD;
import fieldling.quilldriver.PreferenceManager;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SetSavingPreferences extends BasicTask {
    public void execute(QD qd, String parameters) {
        //auto-save open transcripts every how many minutes?
        JPanel autoSaveMinutesPanel = new JPanel(new BorderLayout());
        autoSaveMinutesPanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("AutoSaveMinutesQuestion")));
        JSpinner minutesSpinner = new JSpinner();
        int originalMinutesValue = PreferenceManager.getInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, 0);
        minutesSpinner.setValue(new Integer(originalMinutesValue));
        autoSaveMinutesPanel.add(minutesSpinner, BorderLayout.CENTER);
        
        //back transcripts up to what directory?
        JPanel backupDirectoryPanel = new JPanel(new BorderLayout());
        backupDirectoryPanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("BackupDirectoryQuestion")));
        final JTextField directoryField = new JTextField(PreferenceManager.getValue(PreferenceManager.BACKUP_DIRECTORY_KEY, ""));
        JButton browseButton = new JButton(qd.messages.getString("Browse"));
        backupDirectoryPanel.add(directoryField, BorderLayout.CENTER);
        backupDirectoryPanel.add(browseButton, BorderLayout.EAST);
        final QD activeQD = qd;
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                File f = OpeningTask.selectFileOrDirectory(JFileChooser.DIRECTORIES_ONLY, activeQD.messages.getString("SelectDirectoryMessage"), activeQD);
                if (f.isDirectory())
                    directoryField.setText(f.getAbsolutePath());
            }
        });
        
        //normalize namespaces?
        JPanel normalizeNamespacePanel = new JPanel(new BorderLayout());
        normalizeNamespacePanel.setBorder(BorderFactory.createTitledBorder(qd.messages.getString("NamespaceNormalizerQuestion")));
        String[] noYes = {"No", "Yes"};
        JComboBox normalizerBox = new JComboBox(noYes);
        int useNormalizer = PreferenceManager.getInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, -1);
        if (useNormalizer == 1)
            normalizerBox.setSelectedIndex(1);
        else
            normalizerBox.setSelectedIndex(0);
        normalizeNamespacePanel.add(normalizerBox, BorderLayout.CENTER);
        
        //arrange preferences panel
        JPanel preferencesPanel = new JPanel();
        preferencesPanel.setLayout(new GridLayout(3, 1));
        preferencesPanel.add(autoSaveMinutesPanel);
        preferencesPanel.add(backupDirectoryPanel);
        preferencesPanel.add(normalizeNamespacePanel);
        preferencesPanel.setPreferredSize(new Dimension(600, 200));
        JOptionPane pane = new JOptionPane(preferencesPanel);
        JDialog dialog = pane.createDialog(qd, qd.messages.getString("SavingPreferences"));
	dialog.show(); //returns when the user has closed the dialog
        
        //change preferences accordingly
        int n = ((Integer)minutesSpinner.getValue()).intValue();
        if (n != originalMinutesValue) {
            QD.autoSaver.setDelay(0); //stop existing AutoSave thread
            if (n > 0) {
                PreferenceManager.setInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, n);
                QD.autoSaver = new fieldling.quilldriver.AutoSave(qd.transcriptToggler, n * 60000);
                QD.autoSaver.start();
            } else {
                PreferenceManager.setInt(PreferenceManager.AUTO_SAVE_MINUTES_KEY, 0);
            }
        }
        
        PreferenceManager.setValue(PreferenceManager.BACKUP_DIRECTORY_KEY, directoryField.getText());
        
        if (normalizerBox.getSelectedIndex() == 0)
            PreferenceManager.setInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, -1);
        else
            PreferenceManager.setInt(PreferenceManager.NORMALIZE_NAMESPACES_KEY, 1);
    }
}
