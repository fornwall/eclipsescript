package org.eclipsescript.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.ui.ErrorDetailsDialog;
import org.eclipsescript.util.EclipseUtils;
import org.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String IMG_REMOVE_ALL = "img_remove_all"; //$NON-NLS-1$

	private static Activator plugin;

	public static Bundle getBundleExportingClass(String className) {
		int lastIndexOfDot = className.lastIndexOf('.');
		if (lastIndexOfDot > 0 && lastIndexOfDot < className.length() - 1) {
			char firstCharOfClassName = className.charAt(lastIndexOfDot + 1);
			if (Character.isLowerCase(firstCharOfClassName)) {
				// probably a package requested by rhino "org.eclipse" part of "org.eclipse.ui.xxx"
				return null;
			}

			String packageName = className.substring(0, lastIndexOfDot);

			ExportPackageDescription desc = plugin.resolver.resolveDynamicImport(plugin.bundleDescription, packageName);
			if (desc != null) {
				BundleDescription exporter = desc.getExporter();
				long exporterBundleId = exporter.getBundleId();
				Bundle exportingBundle = plugin.context.getBundle(exporterBundleId);
				return exportingBundle;
			}

		}

		return null;
	}

	/**
	 * Returns the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String id) {
		return getDefault().getImageRegistry().getDescriptor(id);
	}

	public static void logError(final Throwable exception) {
		ILog log = plugin.getLog();
		log.log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), exception.getMessage(), exception));

		try {
			EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {
				@Override
				public void runWithDisplay(Display display) {
					ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(), Messages.internalErrorDialogTitle,
							Messages.internalErrorDialogText, exception);
				}
			});
		} catch (Exception e) {
			log.log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	private BundleDescription bundleDescription;

	private BundleContext context;

	private Resolver resolver;

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		Bundle bundle = Platform.getBundle(getBundle().getSymbolicName());
		IPath path = new Path("icons/remove_all_terminated.gif"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		registry.put(IMG_REMOVE_ALL, desc);
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);

		Activator.plugin = this;
		context = bundleContext;

		ServiceReference<PlatformAdmin> platformAdminServiceRef = context.getServiceReference(PlatformAdmin.class);
		PlatformAdmin platformAdminService = context.getService(platformAdminServiceRef);

		resolver = platformAdminService.createResolver();
		State state = platformAdminService.getState(false);
		context.ungetService(platformAdminServiceRef);
		resolver.setState(state);
		bundleDescription = state.getBundle(plugin.getBundle().getSymbolicName(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		Activator.plugin = null;
	}
}
