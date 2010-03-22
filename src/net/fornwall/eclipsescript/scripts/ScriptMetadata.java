package net.fornwall.eclipsescript.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import net.fornwall.eclipsescript.core.Activator;

import org.eclipse.core.resources.IFile;

public class ScriptMetadata implements Comparable<ScriptMetadata> {

	private IFile file;
	private String fullPath;
	private int instanceId;
	private String summary;

	private static final AtomicInteger counter = new AtomicInteger();

	public ScriptMetadata(IFile file) {
		this.instanceId = counter.getAndIncrement();
		this.file = file;
		this.fullPath = file.getFullPath().toString();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
			String firstLine = reader.readLine();
			reader.close();
			if (firstLine == null) return;
			firstLine = firstLine.trim();

			if (firstLine.startsWith("//") || firstLine.startsWith("/*")) { //$NON-NLS-1$ //$NON-NLS-2$
				this.summary = firstLine.substring(2).trim();
			}
		} catch (Exception e) {
			Activator.logError(e);
		}
	}

	public IFile getFile() {
		return file;
	}

	public String getFullPath() {
		return fullPath;
	}

	public String getSummary() {
		return summary;
	}

	@Override
	public int compareTo(ScriptMetadata o) {
		return instanceId - o.instanceId;
	}

}
