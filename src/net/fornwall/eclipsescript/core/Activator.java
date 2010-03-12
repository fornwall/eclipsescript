package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.ui.ErrorDetailsDialog;
import net.fornwall.eclipsescript.util.EclipseUtils;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	private static Activator plugin;
	private static BundleContext context;
	private static BundleDescription bundleDescription;
	private static Resolver resolver;

	public static final String PLUGIN_ID = "net.fornwall.eclipsescript"; //$NON-NLS-1$

	/**
	 * Returns the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void logError(final Throwable exception) {
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, exception.getMessage(), exception));

		EclipseUtils.runInDisplayThread(new Runnable() {

			public void run() {
				ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(), Messages.internalErrorDialogTitle,
						Messages.internalErrorDialogText, exception);
			}
		});
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);

		Activator.plugin = this;
		Activator.context = bundleContext;

		{
			ServiceReference platformAdminServiceRef = context.getServiceReference(PlatformAdmin.class.getName());
			PlatformAdmin platformAdminService = (PlatformAdmin) context.getService(platformAdminServiceRef);

			Activator.resolver = platformAdminService.createResolver();
			State state = platformAdminService.getState(false);
			context.ungetService(platformAdminServiceRef);
			resolver.setState(state);
			Activator.bundleDescription = state.getBundle(plugin.getBundle().getSymbolicName(), null);
		}

		final ScriptFilesChangeListener listener = new ScriptFilesChangeListener();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

		Job job = new Job(Activator.PLUGIN_ID + ".rescanjob") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				listener.rescanAllFiles();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

	}

	// @Override
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
	}

	@Override
	public void earlyStartup() {
		// do nothing
	}

	public static Bundle getBundleExportingClass(String className) {
		int lastIndexOfDot = className.lastIndexOf('.');
		if (lastIndexOfDot > 0 && lastIndexOfDot < className.length() - 1) {
			char firstCharOfClassName = className.charAt(lastIndexOfDot + 1);
			if (Character.isLowerCase(firstCharOfClassName)) {
				// probably a package requested by rhino "org.eclipse" part of "org.eclipse.ui.xxx"
				return null;
			}

			String packageName = className.substring(0, lastIndexOfDot);

			ExportPackageDescription desc = resolver.resolveDynamicImport(bundleDescription, packageName);
			if (desc != null) {
				BundleDescription exporter = desc.getExporter();
				long exporterBundleId = exporter.getBundleId();
				Bundle exportingBundle = context.getBundle(exporterBundleId);
				return exportingBundle;
			}

		}

		return null;
	}
}
