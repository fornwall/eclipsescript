package net.fornwall.eclipsescript.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.core.RunLastHandler;
import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.ui.ErrorDetailsDialog;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ScriptStore {

	// mapping from full path to metadata
	private static final Map<String, ScriptMetadata> scriptStore = Collections
			.synchronizedMap(new HashMap<String, ScriptMetadata>());

	public static void addScript(final IFile file) {
		ScriptMetadata m = null;
		try {
			m = new ScriptMetadata(file);
			addScriptInternal(m);
		} catch (Exception e) {
			Activator.logError(e);
			return;
		}

		// since this may be called from a IResourceChangeListener we may not modify resources directly, instead do it
		// in a job
		Job job = new Job(Messages.clearMarkersJobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MarkerManager.clearMarkers(file);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private static void addScriptInternal(final ScriptMetadata script) {
		final Map<String, ScriptMetadata> store = getScriptStore();
		store.put(script.getFullPath(), script);
	}

	public static void clearScripts() {
		final Set<String> keys = new LinkedHashSet<String>(getScriptStore().keySet());
		for (final String key : keys)
			removeScript(key);
		getScriptStore().clear();
	}

	private static void showMessageOfferJumpToScript(ScriptMetadata script, ScriptException e) {
		final String[] choices = new String[] { Messages.scriptErrorWhenRunningScriptOkButton,
				Messages.scriptErrorWhenRunningScriptJumpToScriptButton };

		int result = ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(),
				Messages.scriptErrorWhenRunningScriptDialogTitle, e.getMessage(), e.getCause(), choices, e
						.isShowStackTrace());
		if (result == 1) {
			IEditorPart editorPart = EclipseUtils.openEditor(script.getFile());
			if (editorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editorPart;
				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(textEditor.getEditorInput());
				try {
					int lineNumber = Math.max(e.getLineNumber(), 1);
					int start = document.getLineOffset(lineNumber - 1);
					textEditor.selectAndReveal(start, 0);
					IWorkbenchPage page = textEditor.getSite().getPage();
					page.activate(textEditor);
				} catch (BadLocationException e2) {
					throw new RuntimeException(e2);
				}
			}
		}

	}

	public static void executeScript(ScriptMetadata script) {
		// add this even if script execution fails
		RunLastHandler.lastRun = script;

		try {
			try {
				// not called from resource change listener, so no need to call in separate job
				MarkerManager.clearMarkers(script.getFile());

				ScriptLanguageSupport languageSupport = ScriptLanguageHandler.getScriptSupport(script.getFile());
				languageSupport.executeScript(script);
			} catch (ScriptException error) {
				MarkerManager.addMarker(script.getFile(), error);
				showMessageOfferJumpToScript(script, error);
			}
		} catch (Exception e) {
			Activator.logError(e);
		}

	}

	public static List<ScriptMetadata> getAllMetadatas() {
		return new ArrayList<ScriptMetadata>(scriptStore.values());
	}

	public static Map<String, ScriptMetadata> getScriptStore() {
		return scriptStore;
	}

	public static void removeScript(final String name) {
		final Map<String, ScriptMetadata> store = getScriptStore();
		store.remove(name);
	}
}
