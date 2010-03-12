package net.fornwall.eclipsescript.scripts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.fornwall.eclipsescript.core.Activator;

import org.osgi.framework.Bundle;

public class ScriptClassLoader extends ClassLoader {

	private final ClassLoader loader;
	private List<Bundle> bundles = new CopyOnWriteArrayList<Bundle>();
	private List<ClassLoader> loaders = new CopyOnWriteArrayList<ClassLoader>();

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
			addBundle(bundleContainingClass);
			return bundleContainingClass.loadClass(name);
		}

		return null;
	}
}
