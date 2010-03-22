package net.fornwall.eclipsescript.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.messages.Messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Constants;

/**
 * A dialog to display one or more errors to the user, as contained in an <code>IStatus</code> object. If an error
 * contains additional detailed information then a Details button is automatically supplied, which shows or hides an
 * error details viewer when pressed by the user.
 * 
 * <p>
 * This dialog should be considered being a "local" way of error handling. It cannot be changed or replaced by "global"
 * error handling facility ( <code>org.eclipse.ui.statushandler.StatusManager</code>). If product defines its own way of
 * handling errors, this error dialog may cause UI inconsistency, so until it is absolutely necessary,
 * <code>StatusManager</code> should be used.
 * </p>
 * 
 * @see org.eclipse.core.runtime.IStatus
 */
public class ErrorDetailsDialog extends IconAndMessageDialog {

	/**
	 * The Details button.
	 */
	private Button detailsButton;

	/**
	 * The title of the dialog.
	 */
	private String title;

	/**
	 * The SWT list control that displays the error details.
	 */
	private Text text;

	private Throwable exception;

	/**
	 * The current clipboard. To be disposed when closing the dialog.
	 */
	private Clipboard clipboard;

	private boolean showDetails;

	/**
	 * Creates an error dialog. Note that the dialog will have no visual representation (no widgets) until it is told to
	 * open.
	 * <p>
	 * Normally one should use <code>openError</code> to create and open one of these. This constructor is useful only
	 * if the error object being displayed contains child items <it>and </it> you need to specify a mask which will be
	 * used to filter the displaying of these children. The error dialog will only be displayed if there is at least one
	 * child status matching the mask.
	 * </p>
	 * 
	 * @param parentShell
	 *            the shell under which to create this dialog
	 * @param dialogTitle
	 *            the title to use for this dialog, or <code>null</code> to indicate that the default title should be
	 *            used
	 * @param message
	 *            the message to show in this dialog, or <code>null</code> to indicate that the error's message should
	 *            be shown as the primary message
	 * @param choices2
	 */
	ErrorDetailsDialog(Shell parentShell, String dialogTitle, String message, Throwable exception, String[] choices,
			boolean showDetails) {
		super(parentShell);
		this.title = dialogTitle == null ? JFaceResources.getString("Problem_Occurred") : //$NON-NLS-1$
				dialogTitle;
		this.message = message;
		this.exception = exception;
		this.choices = choices;
		this.showDetails = showDetails;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog. Handles the pressing of the Ok or Details button in this dialog. If the
	 * Ok button was pressed then close this dialog. If the Details button was pressed then toggle the displaying of the
	 * error details area. Note that the Details button will only be visible if the error being displayed specifies
	 * child details.
	 */
	@Override
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.DETAILS_ID) {
			// was the details button pressed?
			toggleDetailsArea();
		} else {
			setReturnCode(id);
			close();
		}
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Details buttons
		boolean defaultButton = true;
		int id = 0;
		for (String choice : choices) {
			createButton(parent, id++, choice, defaultButton);
			if (defaultButton)
				defaultButton = false;
		}
		if (showDetails) {
			createDetailsButton(parent);
		}
	}

	/**
	 * Create the details button if it should be included.
	 * 
	 * @param parent
	 *            the parent composite
	 * @since 3.2
	 */
	protected void createDetailsButton(Composite parent) {
		if (shouldShowDetailsButton()) {
			detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL,
					false);
		}
	}

	/**
	 * This implementation of the <code>Dialog</code> framework method creates and lays out a composite. Subclasses that
	 * require a different dialog area may either override this method, or call the <code>super</code> implementation
	 * and add controls to the created composite.
	 * 
	 * Note: Since 3.4, the created composite no longer grabs excess vertical space. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72489. If the old behavior is desired by subclasses, get the
	 * returned composite's layout data and set grabExcessVerticalSpace to true.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// Create a composite with standard margins and spacing
		// Add the messageArea to this composite so that as subclasses add widgets to the messageArea
		// and dialogArea, the number of children of parent remains fixed and with consistent layout.
		// Fixes bug #240135
		Composite composite = new Composite(parent, SWT.NONE);
		createMessageArea(composite);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData childData = new GridData(GridData.FILL_BOTH);
		childData.horizontalSpan = 2;
		childData.grabExcessVerticalSpace = false;
		composite.setLayoutData(childData);
		composite.setFont(parent.getFont());

		return composite;
	}

	/*
	 * @see IconAndMessageDialog#createDialogAndButtonArea(Composite)
	 */
	@Override
	protected void createDialogAndButtonArea(Composite parent) {
		super.createDialogAndButtonArea(parent);
		if (this.dialogArea instanceof Composite) {
			// Create a label if there are no children to force a smaller layout
			Composite dialogComposite = (Composite) dialogArea;
			if (dialogComposite.getChildren().length == 0) {
				new Label(dialogComposite, SWT.NULL);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	@Override
	protected Image getImage() {
		// If it was not a warning or an error then return the error image
		return getErrorImage();
	}

	/**
	 * Create this dialog's drop-down list component.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the drop-down list component
	 */
	protected Text createDropDownList(Composite parent) {
		// create the list
		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);

		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		String stackTraceText = writer.getBuffer().toString();

		String detailsText = NLS.bind(Messages.internalErrorDialogDetails, new Object[] {
				Activator.getDefault().getBundle().getHeaders().get(Constants.BUNDLE_NAME),
				Activator.getDefault().getBundle().getSymbolicName(),
				Activator.getDefault().getBundle().getHeaders().get(Constants.BUNDLE_VERSION), stackTraceText });
		text.setText(detailsText);

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
		data.heightHint = 300;
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		text.setFont(parent.getFont());
		Menu copyMenu = new Menu(text);
		MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
		copyItem.addSelectionListener(new SelectionListener() {
			/** @see SelectionListener.widgetSelected (SelectionEvent) */
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyToClipboard();
			}

			/** @see SelectionListener.widgetDefaultSelected(SelectionEvent) */
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				copyToClipboard();
			}
		});
		copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
		text.setMenu(copyMenu);
		return text;
	}

	/**
	 * Extends <code>Window.open()</code>. Opens an error dialog to display the error. If you specified a mask to filter
	 * the displaying of these children, the error dialog will only be displayed if there is at least one child status
	 * matching the mask.
	 */
	@Override
	public int open() {
		return super.open();
	}

	private String[] choices;

	/**
	 * Opens an error dialog to display the given error. Use this method if the error object being displayed does not
	 * contain child items, or if you wish to display all such items without filtering.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param dialogTitle
	 *            the title to use for this dialog, or <code>null</code> to indicate that the default title should be
	 *            used
	 * @param message
	 *            the message to show in this dialog, or <code>null</code> to indicate that the error's message should
	 *            be shown as the primary message
	 * @param choices
	 * @param status
	 *            the error to show to the user
	 * @return the code of the button that was pressed that resulted in this dialog closing. This will be
	 *         <code>Dialog.OK</code> if the OK button was pressed, or <code>Dialog.CANCEL</code> if this dialog's close
	 *         window decoration or the ESC key was used.
	 */
	public static int openError(Shell parent, String dialogTitle, String message, Throwable exception2,
			String[] choices, boolean showDetails) {
		ErrorDetailsDialog dialog = new ErrorDetailsDialog(parent, dialogTitle, message, exception2, choices,
				showDetails);
		return dialog.open();
	}

	public static int openError(Shell parent, String dialogTitle, String message, Throwable exception) {
		return openError(parent, dialogTitle, message, exception, new String[] { IDialogConstants.OK_LABEL }, true);
	}

	/**
	 * Returns whether the given status object should be displayed.
	 * 
	 * @param status
	 *            a status object
	 * @param mask
	 *            a mask as per <code>IStatus.matches</code>
	 * @return <code>true</code> if the given status should be displayed, and <code>false</code> otherwise
	 * @see org.eclipse.core.runtime.IStatus#matches(int)
	 */
	protected static boolean shouldDisplay(IStatus status, int mask) {
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0) {
			return status.matches(mask);
		}
		for (int i = 0; i < children.length; i++) {
			if (children[i].matches(mask)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Toggles the unfolding of the details area. This is triggered by the user pressing the details button.
	 */
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (text != null && !text.isDisposed()) {
			text.dispose();
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		} else {
			text = createDropDownList((Composite) getContents());
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
			getContents().getShell().layout();
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
	}

	/**
	 * Put the details of the status of the error onto the stream.
	 * 
	 * @param buildingStatus
	 * @param buffer
	 * @param nesting
	 */
	private void populateCopyBuffer(StringBuffer buffer, int nesting) {
		buffer.append(text.getText());
	}

	/**
	 * Copy the contents of the statuses to the clipboard.
	 */
	void copyToClipboard() {
		if (clipboard != null) {
			clipboard.dispose();
		}
		StringBuffer statusBuffer = new StringBuffer();
		populateCopyBuffer(statusBuffer, 0);
		clipboard = new Clipboard(text.getDisplay());
		clipboard.setContents(new Object[] { statusBuffer.toString() }, new Transfer[] { TextTransfer.getInstance() });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		if (clipboard != null) {
			clipboard.dispose();
		}
		return super.close();
	}

	/**
	 * Show the details portion of the dialog if it is not already visible. This method will only work when it is
	 * invoked after the control of the dialog has been set. In other words, after the <code>createContents</code>
	 * method has been invoked and has returned the control for the content area of the dialog. Invoking the method
	 * before the content area has been set or after the dialog has been disposed will have no effect.
	 * 
	 * @since 3.1
	 */
	protected final void showDetailsArea() {
		if (text == null || text.isDisposed()) {
			Control control = getContents();
			if (control != null && !control.isDisposed()) {
				toggleDetailsArea();
			}
		}
	}

	/**
	 * Return whether the Details button should be included. This method is invoked once when the dialog is built. By
	 * default, the Details button is only included if the status used when creating the dialog was a multi-status or if
	 * the status contains an exception. Subclasses may override.
	 * 
	 * @return whether the Details button should be included
	 * @since 3.1
	 */
	protected boolean shouldShowDetailsButton() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

}
