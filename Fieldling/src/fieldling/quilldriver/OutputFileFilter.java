package fieldling.quilldriver;

		public class OutputFileFilter extends javax.swing.filechooser.FileFilter {
			// accepts all directories and all qd files
			public boolean accept(java.io.File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toLowerCase().endsWith(".rtf") || f.getName().toLowerCase().endsWith(".ps");
			}
			//the description of this filter
			public String getDescription() {
				return "QD Output File Format (" + ".rtf" + ", " + ".ps" + ")";
			}
		}
