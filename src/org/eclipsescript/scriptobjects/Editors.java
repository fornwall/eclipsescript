package org.eclipsescript.scriptobjects;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.util.EclipseUtils;
import org.eclipsescript.util.EclipseUtils.DisplayThreadCallable;
import org.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;
import org.eclipsescript.util.JavaUtils.MutableObject;

public class Editors {

	public String getClipboard() throws Exception {
		final MutableObject<String> result = new MutableObject<String>();
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				Clipboard clipboard = new Clipboard(display);
				try {
					result.value = (String) clipboard.getContents(TextTransfer.getInstance());
				} finally {
					clipboard.dispose();
				}
			}
		});
		return result.value;
	}

	/** Returns the currently edited document or null if none. */
	public IDocument getDocument() throws Exception {
		return EclipseUtils.runInDisplayThreadSync(new DisplayThreadCallable<IDocument>() {
			@Override
			public IDocument callWithDisplay(Display display) throws Exception {
				return EclipseUtils.getCurrentDocument();
			}
		});
	}

	/** Returns the currently edited file or null if none. */
	public IFile getFile() throws Exception {
		return EclipseUtils.runInDisplayThreadSync(new DisplayThreadCallable<IFile>() {
			@Override
			public IFile callWithDisplay(Display display) throws Exception {
				IEditorInput editorInput = EclipseUtils.getCurrentEditorInput();
				IFile fileEditorInput = null;
				if (editorInput != null) {
					fileEditorInput = editorInput.getAdapter(IFile.class);
				}
				return fileEditorInput;
			}
		});
	}

	/**
	 * Returns the current text selection or null if none.
	 */
	public ITextSelection getSelection() throws Exception {
		return EclipseUtils.runInDisplayThreadSync(new DisplayThreadCallable<ITextSelection>() {
			@Override
			public ITextSelection callWithDisplay(Display display) throws Exception {
				ITextEditor editor = EclipseUtils.getCurrentTextEditor();
				if (editor == null)
					return null;
				ISelectionProvider provider = editor.getSelectionProvider();
				if (provider == null)
					return null;
				ISelection selection = provider.getSelection();
				if (!(selection instanceof ITextSelection))
					return null;
				return (ITextSelection) selection;
			}
		});
	}

	public void insert(final String textToInsert) throws Exception {
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) throws BadLocationException {

				ITextEditor editor = EclipseUtils.getCurrentTextEditor();
				if (editor == null)
					throw new IllegalArgumentException(Messages.noTextEditorSelected);
				IDocument document = EclipseUtils.getCurrentDocument();
				if (document == null)
					throw new IllegalArgumentException(Messages.noDocumentSelected);

				StyledText styledText = (StyledText) editor.getAdapter(Control.class);
				int offset = styledText.getCaretOffset();
				document.replace(offset, 0, textToInsert);
			}
		});
	}

	public void open(final IFile file) throws Exception {
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) throws Exception {

				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();

				IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
						.getDefaultEditor(file.getName());
				activePage.openEditor(new FileEditorInput(file), editorDescriptor.getId());

			}
		});
	}

	public void replaceSelection(final String newText) throws Exception {
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) throws Exception {
				TextSelection selection = EclipseUtils.getCurrentEditorSelection();
				if (selection == null)
					throw new IllegalArgumentException(Messages.noSelectionSelected);
				ITextEditor editor = EclipseUtils.getCurrentTextEditor();
				if (editor == null)
					throw new IllegalArgumentException(Messages.noTextEditorSelected);
				IDocumentProvider documentProvider = editor.getDocumentProvider();
				IDocument document = documentProvider.getDocument(editor.getEditorInput());
				if (document == null)
					throw new IllegalArgumentException(Messages.noDocumentSelected);
				document.replace(selection.getOffset(), selection.getLength(), newText);
				editor.selectAndReveal(selection.getOffset(), newText.length());
			}
		});
	}

	public void setClipboard(final String text) throws Exception {
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				Clipboard clipboard = new Clipboard(display);
				try {
					clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
				} finally {
					clipboard.dispose();
				}
			}
		});
	}

}
