package net.fornwall.eclipsescript.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.fornwall.eclipsescript.core.Activator;
import net.fornwall.eclipsescript.messages.Messages;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.bindings.TriggerSequence;
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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.keys.IBindingService;

/**
 * Derived from org.eclipse.ui.internal.QuickAccessDialog.
 */
public final class QuickScriptDialog extends PopupDialog {

	private static final int MAX_COUNT_TOTAL = 20;

	@SuppressWarnings("unchecked")
	private static List<QuickAccessEntry>[] newQuickAccessEntryArray(int length) {
		return new ArrayList[length];
	}

	private String currentFilterCommand;
	private final Map<String, QuickAccessElement> elementMap = new HashMap<String, QuickAccessElement>();
	Text filterText;
	Command invokingCommand;
	private TriggerSequence[] invokingCommandKeySequences;
	QuickAccessProvider[] providers;
	LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	Table table;
	TextLayout textLayout;

	private final IWorkbenchWindow window;

	public QuickScriptDialog(IWorkbenchWindow window, final Command invokingCommand) {
		// ProgressManagerUtil.getDefaultParent() as first arg?
		super(/* parent: */window.getShell(), /* shellStyle: */SWT.RESIZE, /* takeFocusOnOpen: */true, /* persistSize: */
		true, /* persistLocation: */false,
		/* showDialogMenu: */false, /* showPersistActions: */false, /* titleText: */"", //$NON-NLS-1$
				/* infoText: */null);

		this.window = window;
		BusyIndicator.showWhile(window.getShell() == null ? null : window.getShell().getDisplay(), new Runnable() {
			@Override
			public void run() {
				QuickScriptDialog.this.providers = new QuickAccessProvider[] { new QuickScriptProvider() };
				QuickScriptDialog.this.invokingCommand = invokingCommand;
				if (QuickScriptDialog.this.invokingCommand != null
						&& !QuickScriptDialog.this.invokingCommand.isDefined()) {
					QuickScriptDialog.this.invokingCommand = null;
				} else {
					// pre-fetch key sequence - do not change because scope will change later
					getInvokingCommandKeySequences();
				}
				// create early
				create();
			}
		});
		// Ugly hack to avoid bug 184045. If this gets fixed, replace the
		// following code with a call to refresh("").
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final Shell shell = getShell();
				if (shell != null && !shell.isDisposed()) {
					Point size = shell.getSize();
					shell.setSize(size.x, size.y + 1);
				}
			}
		});
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

	private List<QuickAccessEntry>[] computeMatchingEntries(String filter, QuickAccessElement perfectMatch,
			int maxCountParameter) {
		if (filter.isEmpty())
			return newQuickAccessEntryArray(0);

		int maxCount = maxCountParameter;

		// collect matches in an array of lists
		List<QuickAccessEntry>[] entries = newQuickAccessEntryArray(providers.length);
		int[] indexPerProvider = new int[providers.length];
		int countTotal = 0;
		boolean perfectMatchAdded = true;
		if (perfectMatch != null) {
			// reserve one entry for the perfect match
			maxCount--;
			perfectMatchAdded = false;
		}
		boolean done;
		do {
			// will be set to false if we find a provider with remaining
			// elements
			done = true;
			for (int i = 0; i < providers.length; i++) {
				if (entries[i] == null) {
					entries[i] = new ArrayList<QuickAccessEntry>();
					indexPerProvider[i] = 0;
				}
				int count = 0;
				QuickAccessProvider provider = providers[i];
				QuickAccessElement[] elements = provider.getElementsSorted();
				int j = indexPerProvider[i];
				while (j < elements.length) {
					QuickAccessElement element = elements[j];
					QuickAccessEntry entry;
					if (filter.length() == 0) {
						entry = new QuickAccessEntry(element, new int[0][0]);
					} else {
						entry = element.match(filter);
					}
					if (entry != null) {
						entries[i].add(entry);
						count++;
						countTotal++;
						if (i == 0 && entry.element == perfectMatch) {
							perfectMatchAdded = true;
							maxCount = MAX_COUNT_TOTAL;
						}
					}
					j++;
				}
				indexPerProvider[i] = j;
				if (j < elements.length) {
					done = false;
				}
			}
		} while (!done);
		if (!perfectMatchAdded) {
			if (perfectMatch == null)
				throw new NullPointerException("perfectMatch is null"); //$NON-NLS-1$
			QuickAccessEntry entry = perfectMatch.match(filter);
			if (entry != null) {
				if (entries[0] == null) {
					entries[0] = new ArrayList<QuickAccessEntry>();
					indexPerProvider[0] = 0;
				}
				entries[0].add(entry);
			}
		}
		return entries;
	}

	private int computeNumberOfItems() {
		Rectangle rect = table.getClientArea();
		int itemHeight = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();
		return (rect.height - headerHeight + itemHeight - 1) / (itemHeight + table.getGridLineWidth());
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

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleSelection();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// do nothing
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
						entry.erase(event);
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

		filterText.addKeyListener(new KeyListener() {
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

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
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

	final protected TriggerSequence[] getInvokingCommandKeySequences() {
		if (invokingCommandKeySequences == null) {
			if (invokingCommand != null) {
				IBindingService bindingService = (IBindingService) window.getWorkbench().getAdapter(
						IBindingService.class);
				invokingCommandKeySequences = bindingService.getActiveBindingsFor(invokingCommand.getId());
			}
		}
		return invokingCommandKeySequences;
	}

	protected void handleElementSelected(Object selectedElement) {
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			if (selectedElement instanceof QuickAccessElement) {
				QuickAccessElement element = (QuickAccessElement) selectedElement;
				element.execute(currentFilterCommand);
			}
		}
	}

	void handleSelection() {
		QuickAccessElement selectedElement = null;
		if (table.getSelectionCount() == 1) {
			QuickAccessEntry entry = (QuickAccessEntry) table.getSelection()[0].getData();
			selectedElement = entry == null ? null : entry.element;
		}
		close();
		if (selectedElement != null) {
			handleElementSelected(selectedElement);
		}
	}

	void refresh(String filterParam) {
		String filter = filterParam;
		if (filter.toLowerCase().startsWith(Messages.scriptLaunchDialogEditCommand + " ")) { //$NON-NLS-1$
			currentFilterCommand = Messages.scriptLaunchDialogEditCommand;
			filter = filter.substring(Messages.scriptLaunchDialogEditCommand.length() + 1).trim();
		} else {
			currentFilterCommand = null;
		}

		int numItems = computeNumberOfItems();

		// perfect match, to be selected in the table if not null
		QuickAccessElement perfectMatch = elementMap.get(filter);

		List<QuickAccessEntry>[] entries = computeMatchingEntries(filter, perfectMatch, numItems);

		int selectionIndex = refreshTable(perfectMatch, entries);

		if (table.getItemCount() > 0) {
			table.setSelection(selectionIndex);
		} else if (filter.length() == 0) {
			// {
			// TableItem item = new TableItem(table, SWT.NONE);
			// item.setText(0, QuickAccessMessages.QuickAccess_AvailableCategories);
			// item.setForeground(0, grayColor);
			// }
			// for (int i = 0; i < providers.length; i++) {
			// QuickAccessProvider provider = providers[i];
			// TableItem item = new TableItem(table, SWT.NONE);
			// item.setText(1, provider.getName());
			// item.setForeground(1, grayColor);
			// }
		}
	}

	private int refreshTable(QuickAccessElement perfectMatch, List<QuickAccessEntry>[] entries) {
		if (table.getItemCount() > entries.length) {
			table.removeAll();
		}
		TableItem[] items = table.getItems();
		int selectionIndex = -1;
		int index = 0;
		for (int i = 0; i < providers.length; i++) {
			if (entries[i] != null) {
				for (Iterator<QuickAccessEntry> it = entries[i].iterator(); it.hasNext();) {
					QuickAccessEntry entry = it.next();
					TableItem item;
					if (index < items.length) {
						item = items[index];
						table.clear(index);
					} else {
						item = new TableItem(table, SWT.NONE);
					}
					if (perfectMatch == entry.element && selectionIndex == -1) {
						selectionIndex = index;
					}
					item.setData(entry);
					// item.setText(0, entry.provider.getName());
					item.setText(0, entry.element.getLabel());
					if (SWT.getPlatform().equals("wpf")) { //$NON-NLS-1$
						item.setImage(0, entry.getImage(resourceManager));
					}
					index++;
				}
			}
		}
		if (index < items.length) {
			table.remove(index, items.length - 1);
		}
		if (selectionIndex == -1) {
			selectionIndex = 0;
		}
		return selectionIndex;
	}

}