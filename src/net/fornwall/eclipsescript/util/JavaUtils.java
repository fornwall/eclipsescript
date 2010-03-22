package net.fornwall.eclipsescript.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class JavaUtils {

	private static final String UTF8_CHARSET_NAME = "utf-8"; //$NON-NLS-1$

	public static abstract class BaseRunnable implements Runnable {
		@Override
		public final void run() {
			try {
				doRun();
			} catch (Exception e) {
				throw asRuntime(e);
			}
		}

		public abstract void doRun() throws Exception;
	}

	public static class MutableObject<T> {
		public T value;
	}

	public static boolean isNotBlank(String s) {
		return s != null && s.length() != 0;
	}

	public static byte[] readAll(InputStream in) throws IOException {
		try {
			byte[] buffer = new byte[512];
			int n;
			ByteArrayOutputStream baous = new ByteArrayOutputStream();
			while ((n = in.read(buffer)) != -1) {
				baous.write(buffer, 0, n);
			}
			return baous.toByteArray();
		} finally {
			in.close();
		}
	}

	public static String readAllToStringAndClose(InputStream in) {
		return readAllToStringAndClose(in, UTF8_CHARSET_NAME);
	}

	public static String readAllToStringAndClose(InputStream in, String charSetName) {
		try {
			String charSetNameToUse = charSetName;
			if (charSetName == null || charSetName.isEmpty())
				charSetNameToUse = UTF8_CHARSET_NAME;
			return new String(readAll(in), charSetNameToUse);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(in);
		}
	}

	public static String readURL(URL url) throws Exception {
		URLConnection uc = url.openConnection();
		String contentType = uc.getContentType();
		int charSetNameIndex = contentType.indexOf("charset="); //$NON-NLS-1$
		String charSet;
		if (charSetNameIndex == -1) {
			charSet = UTF8_CHARSET_NAME;
		} else {
			charSet = contentType.substring(charSetNameIndex + 8);
		}
		InputStream in = uc.getInputStream();
		return readAllToStringAndClose(in, charSet);
	}

	public static void close(Closeable c) {
		try {
			c.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static RuntimeException asRuntime(Exception e) {
		return (e instanceof RuntimeException) ? ((RuntimeException) e) : new RuntimeException(e);
	}

}
