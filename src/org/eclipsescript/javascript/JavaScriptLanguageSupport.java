package org.eclipsescript.javascript;

import java.io.InputStreamReader;
import java.io.Reader;


import org.eclipsescript.javascript.CustomContextFactory.CustomContext;
import org.eclipsescript.scriptobjects.Eclipse;
import org.eclipsescript.scripts.IScriptLanguageSupport;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.util.JavaUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptLanguageSupport implements IScriptLanguageSupport {

	private final CustomContextFactory contextFactory = new CustomContextFactory();

	@Override
	public void executeScript(final ScriptMetadata script) {
		contextFactory.call(new ContextAction() {
			@Override
			public Object run(Context _context) {
				if (!(_context instanceof CustomContext))
					throw new IllegalArgumentException("Wrong context class: " + _context.getClass()); //$NON-NLS-1$
				CustomContext context = (CustomContext) _context;
				ScriptableObject scope = new ImporterTopLevel(context);
				JavascriptRuntime jsRuntime = new JavascriptRuntime(context, scope, script);

				Eclipse eclipseJavaObject = new Eclipse(jsRuntime);
				Object eclipseJsObject = Context.javaToJS(eclipseJavaObject, scope);
				ScriptableObject.putConstProperty(scope, Eclipse.VARIABLE_NAME, eclipseJsObject);

				Reader reader = null;
				try {
					reader = new InputStreamReader(script.getFile().getContents(true), script.getFile().getCharset());
					jsRuntime.evaluate(reader, script.getFile().getName());
				} catch (Throwable e) {
					jsRuntime.handleExceptionFromScriptRuntime(e);
				} finally {
					JavaUtils.close(reader);
				}
				return null;
			}
		});
	}
}
