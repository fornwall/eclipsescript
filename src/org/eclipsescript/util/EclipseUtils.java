package org.eclipsescript.util;


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
import org.eclipsescript.core.Activator;
import org.eclipsescript.util.JavaUtils.MutableObject;

public class EclipseUtils {

	public static interface DisplayThreadCallable<T> {
		public T callWithDisplay(Display display) throws Exception;
	}

	/** A runnable to run in the display thread. */
	public static interface DisplayThreadRunnable {
		public void runWithDisplay(Display display) throws Exception;
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
		descriptor = workbench.getEditorRegistry().getDefaultEditor("simple_text_file.txt"); //$NON-NLS-1$
		if (descriptor != null)
			return descriptor;
		descriptor = workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		if (descriptor != null)
			return descriptor;
		return workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	}

	public static Shell getWindowShell() {
		return activeWindow().getShell();
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

	private static <T> T runInDisplayThread(final DisplayThreadCallable<T> runnable, final boolean sync)
			throws Exception {
		final Display display = PlatformUI.getWorkbench().getDisplay();

		if (Thread.currentThread().equals(display.getThread())) {
			return runnable.callWithDisplay(display);
		} else {
			final MutableObject<Exception> exceptionThrownInUIThread = new MutableObject<Exception>();
			final MutableObject<T> result = new MutableObject<T>();

			Runnable showExceptionWrapper = new Runnable() {
				@Override
				public void run() {
					try {
						result.value = runnable.callWithDisplay(display);
					} catch (Exception e) {
						if (sync) {
							exceptionThrownInUIThread.value = e;
						} else {
							Activator.logError(e);
						}
					}
				}
			};

			if (sync) {
				display.syncExec(showExceptionWrapper);
			} else {
				display.asyncExec(showExceptionWrapper);
			}
			if (exceptionThrownInUIThread.value != null) {
				throw exceptionThrownInUIThread.value;
			}

			return result.value;
		}
	}

	public static void runInDisplayThreadAsync(final DisplayThreadRunnable runnable) throws Exception {
		runInDisplayThread(new DisplayThreadCallable<Void>() {

			@Override
			public Void callWithDisplay(Display display) throws Exception {
				runnable.runWithDisplay(display);
				return null;
			}
		}, false);
	}

	public static <T> T runInDisplayThreadSync(final DisplayThreadCallable<T> runnable) throws Exception {
		return runInDisplayThread(runnable, true);
	}

	public static void runInDisplayThreadSync(final DisplayThreadRunnable runnable) throws Exception {
		runInDisplayThread(new DisplayThreadCallable<Void>() {

			@Override
			public Void callWithDisplay(Display display) throws Exception {
				runnable.runWithDisplay(display);
				return null;
			}
		}, false);
	}
}
