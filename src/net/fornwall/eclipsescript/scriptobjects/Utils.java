package net.fornwall.eclipsescript.scriptobjects;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.fornwall.eclipsescript.util.JavaUtils;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class Utils {
	
	public static void setClipboardText(String text) {
		Display display = Display.getCurrent();
		Clipboard clipboard = new Clipboard(display);
		clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
		clipboard.dispose();
	}

	public static IWebBrowser open(String urlString) throws PartInitException, MalformedURLException {
		if (urlString == null)
			throw new IllegalArgumentException("The urlString argument to open(urlString) was null");
		URL url = new URL(urlString);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchBrowserSupport browserSupport = workbench.getBrowserSupport();
		IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, null, null, null);
		browser.openURL(url);
		return browser;
	}

	public static String readURL(String urlString) throws IOException {
		if (urlString == null)
			throw new IllegalArgumentException("The urlString argument to readUrl(urlString) was null");
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();
		Object content = connection.getContent();
		if (content instanceof InputStream) {
			InputStream in = (InputStream) content;
			try {
				String result = JavaUtils.readAllToStringAndClose(in);
				return result;
			} finally {
				in.close();
			}
		}
		return content.toString();
	}

}
