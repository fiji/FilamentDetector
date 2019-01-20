package sc.fiji.filamentdetector;

import java.io.File;

public class FilesUtils {
	public static String getFileNameWithoutExtension(String fname) {
		return getFileNameWithoutExtension(new File(fname));
	}

	public static String getFileNameWithoutExtension(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		if (pos > 0 && pos < (name.length() - 1)) {
			// there is a '.' and it's not the first, or last character.
			return name.substring(0, pos);
		}
		return name;
	}
}
