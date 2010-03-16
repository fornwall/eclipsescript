package net.fornwall.eclipsescript.ui;

import java.util.List;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.core.ScriptFilesChangeListener;
import net.fornwall.eclipsescript.scripts.ScriptMetadata;
import net.fornwall.eclipsescript.scripts.ScriptStore;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.jface.resource.ImageDescriptor;

public class QuickScriptProvider extends QuickAccessProvider {

	static final ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(Activator.getDefault().getBundle()
			.getEntry("icons/run_script.gif"));

	@Override
	public QuickAccessElement[] getElements() {
		List<ScriptMetadata> allScripts = ScriptStore.getAllMetadatas();
		QuickAccessElement[] elements = new QuickAccessElement[allScripts.size()];
		for (int i = 0; i < elements.length; i++) {
			final ScriptMetadata script = allScripts.get(i);
			QuickAccessElement element = new QuickAccessElement(this) {

				@Override
				public String getLabel() {
					String label = script.getFile().getName().substring(0,
							script.getFile().getName().length() - ScriptFilesChangeListener.FILE_SUFFIX.length());
					if (script.getSummary() != null) {
						label += " - " + script.getSummary();
					}
					return label;
				}

				@Override
				public ImageDescriptor getImageDescriptor() {
					return imageDescriptor;
				}

				@Override
				public String getId() {
					return script.getFullPath();
				}

				@Override
				public void execute(String command) {
					if ("edit".equals(command)) {
						EclipseUtils.openEditor(script.getFile());
					} else {
						ScriptStore.executeScript(script);
					}
				}
			};
			elements[i] = element;
		}
		return elements;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

}
