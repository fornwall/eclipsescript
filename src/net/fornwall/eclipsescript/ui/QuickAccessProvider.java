package net.fornwall.eclipsescript.ui;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;

public abstract class QuickAccessProvider {

	private QuickAccessElement[] sortedElements;

	/**
	 * Returns the image descriptor for this provider.
	 * 
	 * @return the image descriptor, or null if not defined
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the elements provided by this provider.
	 * 
	 * @return this provider's elements
	 */
	public abstract QuickAccessElement[] getElements();

	@SuppressWarnings("unchecked")
	public QuickAccessElement[] getElementsSorted() {
		if (sortedElements == null) {
			sortedElements = getElements();
			Arrays.sort(sortedElements, new Comparator() {
				public int compare(Object o1, Object o2) {
					QuickAccessElement e1 = (QuickAccessElement) o1;
					QuickAccessElement e2 = (QuickAccessElement) o2;
					return e1.getLabel().compareTo(e2.getLabel());
				}
			});
		}
		return sortedElements;
	}
}