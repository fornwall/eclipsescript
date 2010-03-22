package net.fornwall.eclipsescript.scriptobjects;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.util.EclipseUtils;
import net.fornwall.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;
import net.fornwall.eclipsescript.util.JavaUtils.MutableObject;

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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class Editors {

	/** Returns the currently edited document or null if none. */
	public IDocument getDocument() {
		return EclipseUtils.getCurrentDocument();
	}

	/** Returns the current text selection or null if none. */
	public ITextSelection getSelection() {
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

	/** Returns the currently edited file or null if none. */
	public IFile getFile() {
		IEditorInput editorInput = EclipseUtils.getCurrentEditorInput();
		IFile fileEditorInput = null;
		if (editorInput != null) {
			fileEditorInput = (IFile) editorInput.getAdapter(IFile.class);
		}
		return fileEditorInput;
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
				clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
				clipboard.dispose();
			}
		});
	}

	public String getClipboard() throws Exception {
		final MutableObject<String> result = new MutableObject<String>();
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				Clipboard clipboard = new Clipboard(display);
				result.value = (String) clipboard.getContents(TextTransfer.getInstance());
				clipboard.dispose();
			}
		});
		return result.value;
	}

}
