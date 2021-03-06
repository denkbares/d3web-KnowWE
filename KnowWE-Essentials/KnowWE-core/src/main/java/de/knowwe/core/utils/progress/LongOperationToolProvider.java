package de.knowwe.core.utils.progress;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Tool for defining a menu item starting a long operation.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 30.07.2013
 */
public abstract class LongOperationToolProvider implements ToolProvider {

	private final Icon icon;
	private final String title;
	private final String description;
	private final String category;

	public LongOperationToolProvider(Icon icon, String title, String description, String category) {
		this.icon = icon;
		this.title = title;
		this.description = description;
		this.category = category;
	}

	public LongOperationToolProvider(Icon icon, String title, String description) {
		this(icon, title, description, null);
	}

	private String createJSAction(Section<?> section) {
		return createJSAction(section, getOperation(section), Collections.emptyMap(), getOnSuccessFunction(section));
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] {
				new DefaultTool(icon, title, description, createJSAction(section), category)
		};
	}

	public abstract LongOperation getOperation(Section<?> section);

	public String getOnSuccessFunction(Section<?> section) {
		return null;
	}

	@Override
	public final boolean hasTools(Section<?> section, UserContext userContext) {
		LongOperationUtils.registerLongOperation(section, getOperation(section));
		return true;
	}

	/**
	 * Creates a new JS Action to be used in the tool to be created. The operation is automatically being registered, no
	 * further activity is required to use the returned javascript action.
	 *
	 * @param section   the section to create the tool for
	 * @param operation the operation to be executed
	 * @return a ready to use javascript action, to be used in a tool instance
	 */
	public static String createJSAction(Section<?> section, LongOperation operation) {
		return createJSAction(section, operation, null, null);
	}

	/**
	 * Creates a new JS Action to be used in the tool to be created. The operation is automatically being registered, *
	 * no further activity is required to use the returned javascript action.
	 *
	 * @param section    the section to create the tool for
	 * @param operation  the operation to be executed
	 * @param parameters additional parameters passed to the operation
	 * @param onSuccess  js-code to be executed after the operation has completed
	 * @return a ready to use javascript action, to be used in a tool instance
	 */
	public static String createJSAction(Section<?> section, LongOperation operation, @Nullable Map<String, String> parameters, @Nullable String onSuccess) {
		String id = LongOperationUtils.registerLongOperation(section, operation);

		StringBuilder js = new StringBuilder();
		js.append("KNOWWE.core.plugin.progress.startLongOperation('").append(section.getID()).append("', ")
				.append('\'').append(id).append('\'');

		if (parameters != null && !parameters.isEmpty()) {
			js.append("{");
			AtomicBoolean first = new AtomicBoolean(true);
			parameters.forEach((key, value) -> {
				if (!first.getAndSet(false)) js.append(", ");
				js.append(Strings.quoteSingle(key)).append(": ").append(Strings.quoteSingle(value));
			});
			js.append("}, ");
		}
		else if (Strings.nonBlank(onSuccess)) {
			js.append(",null, ");
		}

		if (Strings.nonBlank(onSuccess)) {
			js.append("function() { ").append(onSuccess).append("}");
		}

		js.append(");");
		return js.toString();
	}
}
