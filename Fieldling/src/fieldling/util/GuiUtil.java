package fieldling.util;

import java.io.*;
import javax.swing.*;

public class GuiUtil {
        private GuiUtil() {} //don't instantiate
        
        public static JScrollPane getScrollPaneForTextFile(ClassLoader resourceLoader, String textFileName) throws IOException, FileNotFoundException
	{
		InputStream in = resourceLoader.getResourceAsStream(textFileName);
		if (in == null) {
			throw new FileNotFoundException(textFileName);
		}
		try {
			BufferedReader changeReader = new BufferedReader(
					new InputStreamReader(in));
			StringBuffer concat = new StringBuffer();
			String line;
			while (null != (line = changeReader.readLine())) {
				concat.append(line);
				concat.append('\n');
			}
			JTextArea changeText = new JTextArea(concat.toString());
			changeText.setEditable(false);
			JScrollPane sp = new JScrollPane();
			sp.setViewportView(changeText);
			return sp;
		} catch (IOException ioe) {
			throw ioe;
		}
	}
        public static JScrollPane getScrollPaneForString(String s) {
		JTextArea changeText = new JTextArea(s);
		changeText.setEditable(false);
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(changeText);
                return sp;
	}
        public static JScrollPane getScrollPaneForJPanel(JPanel panel) {
            JScrollPane sp = new JScrollPane();
            sp.setViewportView(panel);
            return sp;
        }
}
