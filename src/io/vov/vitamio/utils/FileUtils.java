package io.vov.vitamio.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
	private static final String FILE_NAME_RESERVED = "|\\?*<\":>+[]/'";

	public static String getUniqueFileName(String name, String id) {
		StringBuilder sb = new StringBuilder();
		for (Character c : name.toCharArray()) {
			if (FILE_NAME_RESERVED.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		name = sb.toString();
		if (name.length() > 16) {
			name = name.substring(0, 16);
		}
		id = Crypto.md5(id);
		name += id;
		try {
			File f = File.createTempFile(name, null);
			if (f.exists()) {
				f.delete();
				return name;
			}
		} catch (IOException e) {
		}
		return id;
	}

	public static String getCanonical(File f) {
		if (f == null)
			return null;

		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			return f.getAbsolutePath();
		}
	}

	/**
	 * Get the path for the file:/// only
	 * 
	 * @param uri
	 * @return
	 */
	public static String getPath(String uri) {
		Log.i("FileUtils#getPath(%s)", uri);
		if (TextUtils.isEmpty(uri))
			return null;
		if (uri.startsWith("file://") && uri.length() > 7)
			return Uri.decode(uri.substring(7));
		return Uri.decode(uri);
	}

	public static String getName(String uri) {
		String path = getPath(uri);
		if (path != null)
			return new File(path).getName();
		return null;
	}

	public static void deleteDir(File f) {
		if (f.exists() && f.isDirectory()) {
			for (File file : f.listFiles()) {
				if (file.isDirectory())
					deleteDir(file);
				file.delete();
			}
			f.delete();
		}
	}
}