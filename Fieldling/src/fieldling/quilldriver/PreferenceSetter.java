package fieldling.quilldriver;

import javax.swing.JComponent;

public interface PreferenceSetter {
        public JComponent getComponent();
        public void setPreferences();
        public String getDisplayName();
}
