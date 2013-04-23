package org.eclipsescript.scripts;

import java.util.concurrent.Callable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipsescript.core.Activator;
import org.eclipsescript.core.RunLastHandler;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.ui.ErrorDetailsDialog;
import org.eclipsescript.util.EclipseUtils;
import org.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;

public class ScriptStore {

	public static <T> T executeRunnableWhichMayThrowScriptException(final ScriptMetadata script, Callable<T> r) {
		try {
			try {
				return r.call();
			} catch (final ScriptException error) {
				IFile file = getFile(script, error);
				MarkerManager.addMarker(file, error);
				EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
					@Override
					public void runWithDisplay(Display display) throws Exception {
						showMessageOfferJumpToScript(script, error);
					}
				});
			}
		} catch (Exception e) {
			Activator.logError(e);
		}
		return null;
	}

	public static void executeScript(final ScriptMetadata script) {
		// add this even if script execution fails
		RunLastHandler.lastRun = script;
		final IScriptLanguageSupport languageSupport = ScriptLanguageHandler.getScriptSupport(script.getFile());

		executeRunnableWhichMayThrowScriptException(script, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				languageSupport.executeScript(script);
				return null;
			}
		});
	}

	static IFile getFile(ScriptMetadata script, ScriptException scriptException) {
		IFile file = null;
		try {
			String sourceName = scriptException.getSourceName();
			int hashIdx = sourceName.indexOf('#');
			if (hashIdx != -1) {
				sourceName = sourceName.substring(0, hashIdx);
			}
			IPath path = new Path(sourceName);
			file = script.getFile().getWorkspace().getRoot().getFile(path);
		} catch (Throwable t) {
			file = script.getFile();
		}
		return file;
	}

	static void showMessageOfferJumpToScript(ScriptMetadata script, ScriptException e) {
		final String[] choices = new String[] { Messages.scriptErrorWhenRunningScriptOkButton,
				Messages.scriptErrorWhenRunningScriptJumpToScriptButton };

		int lineNumber = Math.max(e.getLineNumber(), 1);
		IFile file = getFile(script, e);

		String dialogTitle = Messages.scriptErrorWhenRunningScriptDialogTitle;
		String dialogText = NLS.bind(Messages.scriptErrorWhenRunningScriptDialogText, new Object[] {
				script.getFile().getName(), e.getCause().getMessage(), Integer.toString(lineNumber) });

		if (e.getScriptStackTrace() != null && !e.getScriptStackTrace().isEmpty()) {
			dialogText = dialogText + "\n\n" + e.getScriptStackTrace(); //$NON-NLS-1$
		}

		int result = ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(), dialogTitle, dialogText, e.getCause(),
				choices, e.isShowStackTrace());
		if (result == 1) {
			IEditorPart editorPart = EclipseUtils.openEditor(file);
			if (editorPart instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) editorPart;
				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(textEditor.getEditorInput());
				try {
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
}
