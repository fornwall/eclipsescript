package org.eclipsescript.scripts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipsescript.core.Activator;
import org.osgi.framework.Bundle;

public class ScriptClassLoader extends ClassLoader {

	private final List<Bundle> bundles = new CopyOnWriteArrayList<Bundle>();
	private final ClassLoader loader;
	private final List<ClassLoader> loaders = new CopyOnWriteArrayList<ClassLoader>();

	public ScriptClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	public void addBundle(Bundle bundle) {
		// note that this requires script to load bundles in dependant order...
		bundles.add(0, bundle);
	}

	public void addLoader(ClassLoader classLoader) {
		// note that this requires script to load bundles in dependant order...
		loaders.add(0, classLoader);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return loader.loadClass(name);
		} catch (ClassNotFoundException e) {
			// ignore
		}

		for (Bundle bundle : bundles) {
			try {
				return bundle.loadClass(name);
			} catch (ClassNotFoundException e) {
				// ignore
			}
		}

		for (ClassLoader classLoader : loaders) {
			try {
				return classLoader.loadClass(name);
			} catch (ClassNotFoundException e) {
				// ignore
			}
		}

		Bundle bundleContainingClass = Activator.getBundleExportingClass(name);
		if (bundleContainingClass != null) {
			try {
				Class<?> clazz = bundleContainingClass.loadClass(name);
				addBundle(bundleContainingClass);
				return clazz;
			} catch (ClassNotFoundException e) {
				// handle classes in "split-packages" see http://wiki.osgi.org/wiki/Split_Packages
				Bundle[] bundlesExportingPackage = Activator.getBundlesExportingPackage(name);
				for (Bundle bundle : bundlesExportingPackage) {
					try {
						Class<?> clazz = bundle.loadClass(name);
						addBundle(bundle);
						return clazz;
					} catch (ClassNotFoundException e1) {
						continue;
					}
				}
			}
		}

		return null;
	}
}
