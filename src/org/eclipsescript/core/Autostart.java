package org.eclipsescript.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.scripts.ScriptStore;

public class Autostart implements IStartup {

	@Override
	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				executeAutoScripts();
			}
		});
	}

	void executeAutoScripts() {
		final List<ScriptMetadata> autoScripts = new ArrayList<ScriptMetadata>();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			root.accept(new IResourceProxyVisitor() {
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getName().endsWith(".eclipse.auto.js")) { //$NON-NLS-1$
						IResource resource = proxy.requestResource();
						if (resource instanceof IFile && !resource.isDerived()) {
							IFile file = (IFile) resource;
							autoScripts.add(new ScriptMetadata(file));
						}
					}
					return true;
				}
			}, 0);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		Collections.sort(autoScripts, new Comparator<ScriptMetadata>() {
			@Override
			public int compare(ScriptMetadata o1, ScriptMetadata o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		for (ScriptMetadata script : autoScripts) {
			ScriptStore.executeScript(script);
		}
	}

}
