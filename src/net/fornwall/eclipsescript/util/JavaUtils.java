package net.fornwall.eclipsescript.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class JavaUtils {

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
		try {
			return new String(readAll(in), "utf-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(in);
		}
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
