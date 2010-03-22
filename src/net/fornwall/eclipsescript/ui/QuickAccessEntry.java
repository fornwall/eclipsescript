package net.fornwall.eclipsescript.ui;

import net.fornwall.eclipsescript.core.Activator;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

final class QuickAccessEntry {
	boolean firstInCategory;
	boolean lastInCategory;
	QuickAccessElement element;
	private int[][] elementMatchRegions;

	QuickAccessEntry(QuickAccessElement element, int[][] elementMatchRegions) {
		this.element = element;
		this.elementMatchRegions = elementMatchRegions;
	}

	Image getImage(ResourceManager resourceManager) {
		Image image = findOrCreateImage(element.getImageDescriptor(), resourceManager);
		if (image == null) {
			throw new IllegalArgumentException("Null image for element: " + element); //$NON-NLS-1$
		}
		return image;
	}

	private Image findOrCreateImage(ImageDescriptor imageDescriptor, ResourceManager resourceManager) {
		if (imageDescriptor == null) {
			return null;
		}
		Image image = (Image) resourceManager.find(imageDescriptor);
		if (image == null) {
			try {
				image = resourceManager.createImage(imageDescriptor);
			} catch (DeviceResourceException e) {
				Activator.logError(e);
			}
		}
		return image;
	}

	public void measure(Event event, TextLayout textLayout, ResourceManager resourceManager, TextStyle boldStyle) {
		Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		event.width = 0;
		switch (event.index) {
		case 1:
			// if (firstInCategory || providerMatchRegions.length > 0) {
			// textLayout.setText(provider.getName());
			// for (int i = 0; i < providerMatchRegions.length; i++) {
			// int[] matchRegion = providerMatchRegions[i];
			// textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			// }
			// } else {
			textLayout.setText(""); //$NON-NLS-1$
			// }
			break;
		case 0:
			Image image = getImage(resourceManager);
			Rectangle imageRect = image.getBounds();
			event.width += imageRect.width + 4;
			event.height = Math.max(event.height, imageRect.height + 2);
			textLayout.setText(element.getLabel());
			for (int i = 0; i < elementMatchRegions.length; i++) {
				int[] matchRegion = elementMatchRegions[i];
				textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			}
			break;
		}
		Rectangle rect = textLayout.getBounds();
		event.width += rect.width + 4;
		event.height = Math.max(event.height, rect.height + 2);
	}

	public void paint(Event event, TextLayout textLayout, ResourceManager resourceManager, TextStyle boldStyle) {
		final Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		switch (event.index) {
		case 1:
			// if (true) { // firstInCategory || providerMatchRegions.length > 0) {
			// textLayout.setText(provider.getName());
			// for (int i = 0; i < providerMatchRegions.length; i++) {
			// int[] matchRegion = providerMatchRegions[i];
			// textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			// }
			// if (providerMatchRegions.length > 0 && !firstInCategory) {
			// event.gc.setForeground(grayColor);
			// }
			// Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
			// Rectangle requiredBounds = textLayout.getBounds();
			// textLayout.draw(event.gc, availableBounds.x + 1, availableBounds.y
			// + (availableBounds.height - requiredBounds.height) / 2);
			// }
			break;
		case 0:
			Image image = getImage(resourceManager);
			event.gc.drawImage(image, event.x + 1, event.y + 1);
			textLayout.setText(element.getLabel());
			for (int i = 0; i < elementMatchRegions.length; i++) {
				int[] matchRegion = elementMatchRegions[i];
				textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			}
			Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
			Rectangle requiredBounds = textLayout.getBounds();
			textLayout.draw(event.gc, availableBounds.x + 1 + image.getBounds().width, availableBounds.y
					+ (availableBounds.height - requiredBounds.height) / 2);
			break;
		}
	}

	public void erase(Event event) {
		// We are only custom drawing the foreground.
		event.detail &= ~SWT.FOREGROUND;
	}
}