package net.fornwall.eclipsescript.core;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.ui.ErrorDetailsDialog;
import net.fornwall.eclipsescript.util.EclipseUtils;
import net.fornwall.eclipsescript.util.EclipseUtils.DisplayThreadRunnable;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static Activator plugin;
	private BundleContext context;
	private BundleDescription bundleDescription;
	private Resolver resolver;
	final ScriptFilesChangeListener scriptFilesChangeListener = new ScriptFilesChangeListener();

	/**
	 * Returns the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void logError(final Throwable exception) {
		ILog log = plugin.getLog();
		log.log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), exception.getMessage(), exception));

		EclipseUtils.runInDisplayThreadAsync(new DisplayThreadRunnable() {
			public void runWithDisplay(Display display) {
				ErrorDetailsDialog.openError(EclipseUtils.getWindowShell(), Messages.internalErrorDialogTitle,
						Messages.internalErrorDialogText, exception);
			}
		});
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);

		Activator.plugin = this;
		context = bundleContext;

		{
			ServiceReference platformAdminServiceRef = context.getServiceReference(PlatformAdmin.class.getName());
			PlatformAdmin platformAdminService = (PlatformAdmin) context.getService(platformAdminServiceRef);

			resolver = platformAdminService.createResolver();
			State state = platformAdminService.getState(false);
			context.ungetService(platformAdminServiceRef);
			resolver.setState(state);
			bundleDescription = state.getBundle(plugin.getBundle().getSymbolicName(), null);
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(scriptFilesChangeListener, IResourceChangeEvent.POST_CHANGE);

		Job job = new Job(Messages.rescanScriptsJobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				scriptFilesChangeListener.rescanAllFiles();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(scriptFilesChangeListener);

		super.stop(bundleContext);
		Activator.plugin = null;
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
}
