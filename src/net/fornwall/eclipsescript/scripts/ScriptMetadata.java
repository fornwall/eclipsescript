package net.fornwall.eclipsescript.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import net.fornwall.eclipsescript.core.Activator;

import org.eclipse.core.resources.IFile;

public class ScriptMetadata implements Comparable<ScriptMetadata> {

	private static final AtomicInteger counter = new AtomicInteger();

	private final IFile file;
	private final int instanceId;
	private String summary;

	public ScriptMetadata(IFile file) {
		this.instanceId = counter.getAndIncrement();
		this.file = file;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
			String firstLine = null;
			try {
				firstLine = reader.readLine();
			} finally {
				reader.close();
			}
			if (firstLine == null)
				return;
			firstLine = firstLine.trim();

			if (firstLine.startsWith("//") || firstLine.startsWith("/*")) { //$NON-NLS-1$ //$NON-NLS-2$
				this.summary = firstLine.substring(2).trim();
			}
		} catch (Exception e) {
			Activator.logError(e);
		}
	}

	@Override
	public int compareTo(ScriptMetadata o) {
		return instanceId - o.instanceId;
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof ScriptMetadata) && ((ScriptMetadata) other).instanceId == instanceId;
	}

	public IFile getFile() {
		return file;
	}

	public String getFullPath() {
		return file.getFullPath().toString();
	}

	public String getName() {
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".eclipse."); //$NON-NLS-1$
		return fileName.substring(0, index);
	}

	public String getSummary() {
		return summary;
	}

	@Override
	public int hashCode() {
		return instanceId;
	}

}
