package org.eclipsescript.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;


import org.eclipse.core.resources.IFile;
import org.eclipsescript.core.Activator;

public class ScriptMetadata implements Comparable<ScriptMetadata> {

	private static final AtomicInteger counter = new AtomicInteger();

	private final IFile file;
	private final int instanceId;
	private final String label;

	public ScriptMetadata(IFile file) {
		this.instanceId = counter.getAndIncrement();
		this.file = file;

		String fileName = file.getName();
		int index = fileName.lastIndexOf(".eclipse."); //$NON-NLS-1$
		String scriptName = fileName.substring(0, index);

		String firstLine = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
			try {
				firstLine = reader.readLine();
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			Activator.logError(e);
		}

		if (firstLine == null) {
			label = scriptName;
		} else {
			firstLine = firstLine.trim();
			if (firstLine.startsWith("//") || firstLine.startsWith("/*")) { //$NON-NLS-1$ //$NON-NLS-2$
				this.label = scriptName + " - " + firstLine.substring(2).trim(); //$NON-NLS-1$
			} else {
				this.label = scriptName;
			}
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

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return instanceId;
	}

}
