package net.fornwall.eclipsescript.ui;

import java.util.List;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;

import org.eclipse.jface.resource.ImageDescriptor;

public class QuickScriptProvider extends QuickAccessProvider {

	static final ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(Activator.getDefault().getBundle()
			.getEntry("icons/run_script.gif")); //$NON-NLS-1$

	@Override
	public QuickAccessElement[] getElements() {
		List<ScriptMetadata> allScripts = ScriptStore.getAllMetadatas();
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
					String label = script.getName();
					if (script.getSummary() != null) {
						label += " - " + script.getSummary(); //$NON-NLS-1$
					}
					return label;
				}
			};
		}
		return elements;
	}

}
