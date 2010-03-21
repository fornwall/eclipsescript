package net.fornwall.eclipsescript.util;

import net.fornwall.eclipsescript.core.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class EclipseUtils {

	/** A runnable to run in the display thread. */
	public static interface DisplayThreadRunnable {
		public void runWithDisplay(Display display);
	}

	public static void runInDisplayThreadAsync(final DisplayThreadRunnable runnable) {
		runInDisplayThread(runnable, false);
	}

	public static void runInDisplayThreadSync(final DisplayThreadRunnable runnable) {
		runInDisplayThread(runnable, true);
	}

	private static void runInDisplayThread(final DisplayThreadRunnable runnable, boolean sync) {
		final Display display = PlatformUI.getWorkbench().getDisplay();

		Runnable showExceptionWrapper = new Runnable() {
			@Override
			public void run() {
				try {
					runnable.runWithDisplay(display);
				} catch (Exception e) {
					Activator.logError(e);
				}
			}
		};

		if (Thread.currentThread().equals(display.getThread())) {
			showExceptionWrapper.run();
		} else {
			if (sync) {
				display.syncExec(showExceptionWrapper);
			} else {
				display.asyncExec(showExceptionWrapper);
			}
		}
	}

	public static Shell getWindowShell() {
		return activeWindow().getShell();
	}

	public static IWorkbenchPage activePage() {
		return activeWindow().getActivePage();
	}

	public static IWorkbenchWindow activeWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static IDocument getCurrentDocument() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor == null)
			return null;
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IDocument document = documentProvider.getDocument(editor.getEditorInput());
		return document;
	}

	public static IEditorInput getCurrentEditorInput() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor == null)
			return null;
		return editor.getEditorInput();
	}

	public static TextSelection getCurrentEditorSelection() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor == null)
			return null;
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			return textSelection;
		}
		return null;
	}

	public static ITextEditor getCurrentTextEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		if (activePage == null)
			return null;
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) activeEditor;
			return textEditor;
		}
		return null;
	}

	public static IEditorDescriptor getDefaultEditor(final IFile file) {
		if (file == null)
			return null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IEditorDescriptor descriptor = workbench.getEditorRegistry().getDefaultEditor(file.getName());
		if (descriptor != null)
			return descriptor;
		descriptor = workbench.getEditorRegistry().getDefaultEditor("foo.txt");
		if (descriptor != null)
			return descriptor;
		descriptor = workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		if (descriptor != null)
			return descriptor;
		return workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	}

	public static IEditorPart openEditor(final IFile file) {
		final IEditorDescriptor descriptor = getDefaultEditor(file);
		try {
			return activePage().openEditor(new FileEditorInput(file), descriptor.getId());
		} catch (PartInitException e) {
			Activator.logError(e);
			return null;
		}
	}
}
