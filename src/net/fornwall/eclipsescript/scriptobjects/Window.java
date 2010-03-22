package net.fornwall.eclipsescript.scriptobjects;

import java.net.MalformedURLException;
import java.net.URL;

import net.fornwall.eclipsescript.util.EclipseUtils;
import net.fornwall.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;
import net.fornwall.eclipsescript.util.JavaUtils.MutableObject;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class Window {

	public static void alert(final String message) throws Exception {
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				MessageDialog.openInformation(EclipseUtils.activeWindow().getShell(), "Script Alert", message);
			}
		});
	}

	public static String prompt(final String message) throws Exception {
		return prompt(message, "");
	}

	public static boolean confirm(final String message) throws Exception {
		final MutableObject<Boolean> enteredText = new MutableObject<Boolean>();
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				enteredText.value = MessageDialog.openConfirm(EclipseUtils.getWindowShell(), "Script Prompt", message);
			}
		});
		return enteredText.value;
	}

	public static String prompt(final String message, final String initialValue) throws Exception {
		final MutableObject<String> enteredText = new MutableObject<String>();
		EclipseUtils.runInDisplayThreadSync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				final PromptDialog dialog = new PromptDialog(EclipseUtils.getWindowShell(), message);

				// create the window shell so the title can be set
				dialog.create();
				dialog.getShell().setText("Prompt");
				if (initialValue != null) {
					dialog.promptField.setText(initialValue);
				}
				// since the Window has the blockOnOpen property set to true, it
				// will dipose of the shell upon close
				if (dialog.open() == org.eclipse.jface.window.Window.OK) {
					enteredText.value = dialog.enteredText;
				}
			}
		});
		return enteredText.value;
	}

	private static class PromptDialog extends Dialog {

		private final String promptText;
		Text promptField;
		String enteredText;

		public PromptDialog(Shell parentShell, String promptText) {
			super(parentShell);
			this.promptText = promptText;
		}

		@Override
		public boolean close() {
			enteredText = promptField.getText();
			return super.close();
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			final GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			container.setLayout(gridLayout);

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
			nameLabel.setText(promptText + ":");

			promptField = new Text(container, SWT.BORDER);
			promptField.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
			promptField.setText("");

			return container;
		}

	}

	public static IWebBrowser open(String urlString) throws PartInitException, MalformedURLException {
		if (urlString == null)
			throw new IllegalArgumentException("The urlString argument to open(urlString) was null");
		URL url = new URL(urlString);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchBrowserSupport browserSupport = workbench.getBrowserSupport();
		IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, null, null, null);
		browser.openURL(url);
		return browser;
	}

	public void setStatus(final String status) throws Exception {
		EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {
			@Override
			public void runWithDisplay(Display display) {
				IWorkbenchPage page = EclipseUtils.activePage();
				IWorkbenchPart part = page.getActivePart();
				IWorkbenchPartSite site = part.getSite();
				IActionBars actionBars = null;
				if (site instanceof IEditorSite) {
					IEditorSite editorSite = (IEditorSite) site;
					actionBars = editorSite.getActionBars();
				} else if (site instanceof IViewSite) {
					IViewSite viewSite = (IViewSite) site;
					actionBars = viewSite.getActionBars();
				}
				if (actionBars == null)
					return;
				IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
				if (statusLineManager == null)
					return;
				statusLineManager.setMessage(status);
			}
		});
	}

}
