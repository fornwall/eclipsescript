package net.fornwall.eclipsescript.scripts;

import java.util.concurrent.Callable;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.core.RunLastHandler;
import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.ui.ErrorDetailsDialog;
import net.fornwall.eclipsescript.util.EclipseUtils;
import net.fornwall.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ScriptStore {

	public static <T> T executeRunnableWhichMayThrowScriptException(final ScriptMetadata script, Callable<T> r) {
		try {
			try {
				return r.call();
			} catch (final ScriptException error) {
				MarkerManager.addMarker(script.getFile(), error);
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
		// not called from resource change listener, so no need to call in separate job
		MarkerManager.clearMarkers(script.getFile());
		final IScriptLanguageSupport languageSupport = ScriptLanguageHandler.getScriptSupport(script.getFile());

		executeRunnableWhichMayThrowScriptException(script, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				languageSupport.executeScript(script);
				return null;
			}
		});

	}

	static void showMessageOfferJumpToScript(ScriptMetadata script, ScriptException e) {
		final String[] choices = new String[] { Messages.scriptErrorWhenRunningScriptOkButton,
				Messages.scriptErrorWhenRunningScriptJumpToScriptButton };

		String dialogTitle = Messages.scriptErrorWhenRunningScriptDialogTitle;
		String dialogText = NLS.bind(Messages.scriptErrorWhenRunningScriptDialogText, script.getFile().getName(), e
				.getCause().getMessage());
		int result = ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(), dialogTitle, dialogText, e.getCause(),
				choices, e.isShowStackTrace());
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
}
