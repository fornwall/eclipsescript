package org.eclipsescript.javascript;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipsescript.messages.Messages;
import org.eclipsescript.rhino.javascript.Callable;
import org.eclipsescript.rhino.javascript.Context;
import org.eclipsescript.rhino.javascript.ContextFactory;
import org.eclipsescript.rhino.javascript.Scriptable;
import org.eclipsescript.scripts.ScriptClassLoader;

class CustomContextFactory extends ContextFactory {

	static class CustomContext extends Context {
		public JavascriptRuntime jsRuntime;

		long startTime;
		boolean useTimeout = true;

		public CustomContext(ContextFactory factory) {
			super(factory);
		}
	}

	@Override
	protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		CustomContext mcx = (CustomContext) cx;
		mcx.startTime = System.currentTimeMillis();

		return super.doTopCall(callable, cx, scope, thisObj, args);
	}

	@Override
	public boolean hasFeature(Context cx, int featureIndex) {
		switch (featureIndex) {
		case Context.FEATURE_STRICT_EVAL:
			// error on eval(arg) with non-string arg - sensible
			return true;
		case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
			return true;
		default:
			return super.hasFeature(cx, featureIndex);
		}
	}

	@Override
	protected Context makeContext() {
		final CustomContext cx = new CustomContext(this);
		// prevent generating of java class files loaded into the JVM, use
		// interpreted mode
		cx.setOptimizationLevel(-1);

		if (Thread.currentThread().equals(PlatformUI.getWorkbench().getDisplay().getThread())) {
			// only observe instructions in UI thread to avoid locking UI
			cx.setInstructionObserverThreshold(5000);
		}
		ClassLoader classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
			@Override
			public ClassLoader run() {
				return new ScriptClassLoader(cx.getApplicationClassLoader());
			}
		});

		cx.setApplicationClassLoader(classLoader);
		cx.setLanguageVersion(Context.VERSION_1_8);
		cx.getWrapFactory().setJavaPrimitiveWrap(false);
		return cx;
	}

	@Override
	protected void observeInstructionCount(Context cx, int instructionCount) {
		CustomContext mcx = (CustomContext) cx;
		if (!mcx.useTimeout)
			return;
		final int MAX_SECONDS = 10;
		long currentTime = System.currentTimeMillis();
		if (currentTime - mcx.startTime > MAX_SECONDS * 1000) {
			mcx.jsRuntime.abortRunningScript(NLS.bind(Messages.scriptTimeout, MAX_SECONDS));
		}
	}

}
