package org.eclipsescript.javascript;

import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipsescript.javascript.CustomContextFactory.CustomContext;
import org.eclipsescript.rhino.javascript.Context;
import org.eclipsescript.rhino.javascript.ContextAction;
import org.eclipsescript.rhino.javascript.ImporterTopLevel;
import org.eclipsescript.rhino.javascript.ScriptableObject;
import org.eclipsescript.scriptobjects.Eclipse;
import org.eclipsescript.scripts.IScriptLanguageSupport;
import org.eclipsescript.scripts.ScriptMetadata;
import org.eclipsescript.util.JavaUtils;

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
					IFile scriptFile = script.getFile();
					reader = new InputStreamReader(scriptFile.getContents(true), scriptFile.getCharset());
					String name = scriptFile.getFullPath().toPortableString();
					jsRuntime.evaluate(reader, name, false);
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
