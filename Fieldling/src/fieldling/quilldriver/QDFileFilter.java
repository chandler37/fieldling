package fieldling.quilldriver;

import java.io.File;

		public class QDFileFilter extends javax.swing.filechooser.FileFilter {
                        /** When opening a file, this is the only extension QuillDriver
                         cares about.  This is case-insensitive. */
                        protected final static String DOT_QUILLDRIVER = ".xml";
                        protected final static String DOT_QUILLDRIVER_TIBETAN = ".qdt";
			// accepts all directories and all savant files
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toLowerCase().endsWith(DOT_QUILLDRIVER) || f.getName().toLowerCase().endsWith(DOT_QUILLDRIVER_TIBETAN);
			}
			//the description of this filter
			public String getDescription() {
				return "QD File Format (" + DOT_QUILLDRIVER + ", " + DOT_QUILLDRIVER_TIBETAN + ")";
			}
		}
