package net.fornwall.eclipsescript.javascript;

import net.fornwall.eclipsescript.messages.Messages;
import net.fornwall.eclipsescript.scripts.ScriptClassLoader;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

class CustomContextFactory extends ContextFactory {

	static class CustomContext extends Context {
		public CustomContext(ContextFactory factory) {
			super(factory);
		}

		long startTime;
		public JavascriptRuntime jsRuntime;
	}

	@Override
	protected Context makeContext() {
		CustomContext cx = new CustomContext(this);
		// prevent generating of java class files loaded into the JVM, use
		// interpreted mode
		cx.setOptimizationLevel(-1);

		if (Thread.currentThread().equals(PlatformUI.getWorkbench().getDisplay().getThread())) {
			// only observe instructions in UI thread to avoid locking UI
			cx.setInstructionObserverThreshold(5000);
		}
		cx.setApplicationClassLoader(new ScriptClassLoader(cx.getApplicationClassLoader()));
		cx.setLanguageVersion(Context.VERSION_1_7);
		cx.getWrapFactory().setJavaPrimitiveWrap(false);
		return cx;
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
	protected void observeInstructionCount(Context cx, int instructionCount) {
		CustomContext mcx = (CustomContext) cx;
		final int MAX_SECONDS = 10;
		long currentTime = System.currentTimeMillis();
		if (currentTime - mcx.startTime > MAX_SECONDS * 1000) {
			mcx.jsRuntime.abortRunningScript(NLS.bind(Messages.scriptTimeout, MAX_SECONDS));
		}
	}

	@Override
	protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		CustomContext mcx = (CustomContext) cx;
		mcx.startTime = System.currentTimeMillis();

		return super.doTopCall(callable, cx, scope, thisObj, args);
	}

}
