package org.eclipsescript.ui;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipsescript.core.Activator;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.scripts.ScriptStore;

public class QuickScriptProvider extends QuickAccessProvider {

	static final ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(Activator.getDefault().getBundle()
			.getEntry("icons/run_script.gif")); //$NON-NLS-1$

	@Override
	public QuickAccessElement[] getElements() {
		final List<ScriptMetadata> allScripts = new ArrayList<ScriptMetadata>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			root.accept(new IResourceProxyVisitor() {
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getName().endsWith(".eclipse.js")) { //$NON-NLS-1$
						IResource resource = proxy.requestResource();
						if (resource instanceof IFile && !resource.isDerived()) {
							IFile file = (IFile) resource;
							allScripts.add(new ScriptMetadata(file));
						}
					}
					return true;
				}
			}, 0);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		QuickAccessElement[] elements = new QuickAccessElement[allScripts.size()];
		for (int i = 0; i < elements.length; i++) {
			final ScriptMetadata script = allScripts.get(i);
			elements[i] = new QuickAccessElement() {

				@Override
				public void execute() {
					ScriptStore.executeScript(script);
				}

				@Override
				public ImageDescriptor getImageDescriptor() {
					return imageDescriptor;
				}

				@Override
				public String getLabel() {
					return script.getLabel();
				}
			};
		}
		return elements;
	}
}
