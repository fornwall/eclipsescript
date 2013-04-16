package org.eclipsescript.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipsescript.core.Activator;

/**
 * Derived from org.eclipse.ui.internal.QuickAccessDialog.
 */
public final class QuickScriptDialog extends PopupDialog {

	Text filterText;
	final QuickAccessProvider provider = new QuickScriptProvider();
	LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	Table table;
	TextLayout textLayout;

	public QuickScriptDialog(IWorkbenchWindow window) {
		super(/* parent: */window.getShell(), /* shellStyle: */SWT.RESIZE, /* takeFocusOnOpen: */true, /* persistSize: */
		true, /* persistLocation: */false,
		/* showDialogMenu: */false, /* showPersistActions: */false, /* titleText: */"", //$NON-NLS-1$
				/* infoText: */null);
	}

	@Override
	public boolean close() {
		if (textLayout != null && !textLayout.isDisposed()) {
			textLayout.dispose();
		}
		if (resourceManager != null) {
			resourceManager.dispose();
			resourceManager = null;
		}
		return super.close();
	}

	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		boolean isWin32 = "win32".equals(SWT.getPlatform()); //$NON-NLS-1$
		GridLayoutFactory.fillDefaults().extendedMargins(isWin32 ? 0 : 3, 3, 2, 2).applyTo(composite);
		Composite tableComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
		textLayout = new TextLayout(table.getDisplay());
		textLayout.setOrientation(getDefaultOrientation());
		Font boldFont = resourceManager.createFont(FontDescriptor.createFrom(JFaceResources.getDialogFont()).setStyle(
				SWT.BOLD));
		textLayout.setFont(table.getFont());

		// tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(0, maxProviderWidth));
		tableColumnLayout.setColumnData(new TableColumn(table, SWT.NONE), new ColumnWeightData(100, 100));

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP && table.getSelectionIndex() == 0) {
					filterText.setFocus();
				} else if (e.character == SWT.ESC) {
					close();
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (table.getSelectionCount() < 1)
					return;
				if (e.button != 1)
					return;
				if (table.equals(e.getSource())) {
					Object o = table.getItem(new Point(e.x, e.y));
					TableItem selection = table.getSelection()[0];
					if (selection.equals(o))
						handleSelection();
				}
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleSelection();
			}
		});

		// italicsFont = resourceManager.createFont(FontDescriptor.createFrom(
		// table.getFont()).setStyle(SWT.ITALIC));
		final TextStyle boldStyle = new TextStyle(boldFont, null, null);
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				QuickAccessEntry entry = (QuickAccessEntry) event.item.getData();
				if (entry != null) {
					switch (event.type) {
					case SWT.MeasureItem:
						entry.measure(event, textLayout, resourceManager, boldStyle);
						break;
					case SWT.PaintItem:
						entry.paint(event, textLayout, resourceManager, boldStyle);
						break;
					case SWT.EraseItem:
						QuickAccessEntry.erase(event);
						break;
					default:
						break;
					}
				}
			}
		};
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);

		return composite;
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		filterText = new Text(parent, SWT.NONE);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filterText);

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					handleSelection();
					return;
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					int index = table.getSelectionIndex();
					if (index != -1 && table.getItemCount() > index + 1) {
						table.setSelection(index + 1);
					}
					table.setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					int index = table.getSelectionIndex();
					if (index != -1 && index >= 1) {
						table.setSelection(index - 1);
						table.setFocus();
					}
				} else if (e.character == 0x1B) // ESC
					close();
			}
		});
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText().toLowerCase();
				refresh(text);
			}
		});

		return filterText;
	}

	// eclipse 3.4:
	// @Override
	// protected Point getDefaultLocation(Point initialSize) {
	// Point size = new Point(400, 400);
	// Rectangle parentBounds = getParentShell().getBounds();
	// int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
	// int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
	// return new Point(x, y);
	// }

	@Override
	protected Point getDefaultSize() {
		return new Point(600, 600);
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	@Override
	protected Control getFocusControl() {
		return filterText;
	}

	void handleSelection() {
		QuickAccessElement selectedElement = null;
		if (table.getSelectionCount() == 1) {
			QuickAccessEntry entry = (QuickAccessEntry) table.getSelection()[0].getData();
			selectedElement = entry == null ? null : entry.element;
		}
		close();
		if (selectedElement != null) {
			selectedElement.execute();
		}
	}

	void refresh(String filter) {
		List<QuickAccessEntry> entries;
		if (filter.isEmpty()) {
			entries = Collections.emptyList();
		} else {
			entries = new ArrayList<QuickAccessEntry>();
			QuickAccessElement[] elements = provider.getElementsSorted();
			for (QuickAccessElement element : elements) {
				QuickAccessEntry entry = element.match(filter);
				if (entry != null)
					entries.add(entry);
			}
		}

		TableItem[] items = table.getItems();
		int index = 0;
		for (Iterator<QuickAccessEntry> it = entries.iterator(); it.hasNext();) {
			QuickAccessEntry entry = it.next();
			TableItem item;
			if (index < items.length) {
				item = items[index];
				table.clear(index);
			} else {
				item = new TableItem(table, SWT.NONE);
			}
			item.setData(entry);
			item.setText(0, entry.element.getLabel());
			if (SWT.getPlatform().equals("wpf")) { //$NON-NLS-1$
				item.setImage(0, entry.getImage(resourceManager));
			}
			index++;
		}
		if (index < items.length) {
			table.remove(index, items.length - 1);
		}
		if (table.getItems().length > 0)
			table.setSelection(0);
	}

}