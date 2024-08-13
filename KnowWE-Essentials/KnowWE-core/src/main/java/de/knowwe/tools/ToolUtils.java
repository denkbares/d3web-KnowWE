package de.knowwe.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.CountingSet;
import com.denkbares.plugin.Extension;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Scope;
import de.knowwe.core.utils.ScopeExtensions;

public class ToolUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolUtils.class);

	/**
	 * For each tool provider there might be an (counting) set of scopes, where this provider is blocked.
	 */
	private static final Map<ToolProvider, CountingSet<Scope>> BLOCKED_PROVIDERS = new HashMap<>();
	public static final String SETTINGS_FILE = "settings.toolmenu.json";

	/**
	 * Special string for the empty category. The spaces in front make it be the first item in a sorted list (assuming
	 * the others use regular letters).
	 */
	public final static String EMPTY_CATEGORY = "  empty_category";

	/**
	 * Manages the {@link ToolProvider} extensions with their scopes
	 */
	private static final ScopeExtensions extensions =
			new ScopeExtensions("KnowWEExtensionPoints", "ToolProvider");

	public static ToolSet getTools(Section<?> section, UserContext userContext) {
		return new FutureToolSet(section, userContext);
	}

	static List<Tool> getToolInstances(Section<?> section, UserContext userContext) {
		List<Tool> result = new LinkedList<>();
		Set<Class<?>> toolClasses = new HashSet<>();
		for (Extension match : extensions.getMatches(section)) {
			ToolProvider provider = (ToolProvider) match.getSingleton();
			if (toolClasses.add(provider.getClass())) { // avoid duplicate tools (e.g. overlapping scopes)
				try {
					if (isBlocked(provider, section)) continue;
					if (!provider.hasTools(section, userContext)) continue;
					Tool[] tools = provider.getTools(section, userContext);
					if (tools != null) {
						Arrays.stream(tools).filter(Objects::nonNull).forEach(result::add);
					}
				}
				catch (Exception e) {
					LOGGER.warn("Exception while getting tools from " + provider.getClass()
							.getSimpleName() + " ignoring this provider.", e);
				}
			}
		}
		return result;
	}

	public static boolean hasToolInstances(Section<?> section, UserContext userContext) {
		for (Extension match : extensions.getMatches(section)) {
			ToolProvider provider = (ToolProvider) match.getSingleton();
			try {
				if (!provider.hasTools(section, userContext)) continue;
				if (isBlocked(provider, section)) continue;
				return true;
			}
			catch (Exception e) {
				LOGGER.warn("Exception while checking tools from " + provider.getClass()
						.getSimpleName() + " ignoring this provider.", e);
			}
		}
		return false;
	}

	/**
	 * This method sorts the tool list by the category string.
	 * <p/>
	 * The category string can contain at most one forward slash (/)
	 *
	 * @created 03.03.2011
	 */
	public static Map<String, Map<String, List<Tool>>> groupTools(ToolSet tools) {
		Map<String, Map<String, List<Tool>>> toolMap = new HashMap<>();

		for (Tool t : tools) {
			String category = t.getCategory();

			if (category == null || category.equals("")) {
				if (!toolMap.containsKey(EMPTY_CATEGORY)) {
					toolMap.put(EMPTY_CATEGORY, new HashMap<>());
					toolMap.get(EMPTY_CATEGORY).put(EMPTY_CATEGORY, new LinkedList<>());
				}

				toolMap.get(EMPTY_CATEGORY).get(EMPTY_CATEGORY).add(t);
			}
			else {
				String[] parts = category.split("/", 2);

				if (!toolMap.containsKey(parts[0])) {
					toolMap.put(parts[0], new HashMap<>());
				}

				// items of the type "category"
				if (parts.length < 2) {
					if (!toolMap.get(parts[0]).containsKey(EMPTY_CATEGORY)) {
						toolMap.get(parts[0]).put(EMPTY_CATEGORY, new LinkedList<>());
					}

					toolMap.get(parts[0]).get(EMPTY_CATEGORY).add(t);
					// items of the type "category/subitem"
				}
				else {
					if (!toolMap.get(parts[0]).containsKey(parts[1])) {
						toolMap.get(parts[0]).put(parts[1], new LinkedList<>());
					}

					toolMap.get(parts[0]).get(parts[1]).add(t);
				}
			}
		}

		return toolMap;
	}

	private static final ToolSet EMPTY_TOOL_SET = new DefaultToolSet();

	public static ToolSet emptyTools() {
		return EMPTY_TOOL_SET;
	}

	public static Tool[] emptyToolArray() {
		return emptyTools().getTools();
	}

	public static String getActionAttribute(Tool tool) {
		return getActionAttributeName(tool) + "=\"" + getActionAttributeValue(tool) + "\"";
	}

	public static String getActionAttributeName(Tool tool) {
		switch (tool.getActionType()) {
			case HREF:
				return "href";
			case HREF_SCRIPT:
				return "href";
			case ONCLICK:
				return "onclick";
		}
		throw new IllegalStateException("not implemented: " + tool.getActionType());
	}

	public static String getActionAttributeValue(Tool tool) {
		if (Strings.isBlank(tool.getAction())) return null;
		switch (tool.getActionType()) {
			case HREF:
				return tool.getAction();
			case HREF_SCRIPT:
				return "javascript:" + tool.getAction() + ";_TM.hideToolsPopupMenu()";
			case ONCLICK:
				return tool.getAction() + ";_TM.hideToolsPopupMenu()";
		}
		throw new IllegalStateException("not implemented: " + tool.getActionType());
	}

	/**
	 * Blocks the specified tool provider for all sections of the specified scope. See {@link
	 * #findToolProviders(String)} for more information on the matched tools.
	 * <p>
	 * If this method is called multiple times for the same tool provider, it also becomes blocked multiple times, and
	 * therefore must also be unblocked multiple times before it becomes active again.
	 *
	 * @param extensionIdOrClassName the extension id to identify the class, or the class name of the tool provider
	 * @param scope                  the scope where to block the tool provider
	 * @throws IllegalArgumentException if there is no such tool provider plugged to the system
	 */
	public static void blockToolProvider(String extensionIdOrClassName, Scope scope) {
		final List<ToolProvider> toolProviders;
		try {
			toolProviders = findToolProviders(extensionIdOrClassName);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to find blocked tool provider", e);
		}
		for (ToolProvider provider : toolProviders) {
			blockToolProvider(provider, scope);
		}
	}

	/**
	 * Blocks the specified tool provider for all sections of the specified scope.
	 * <p>
	 * If this method is called multiple times for the same tool provider, it also becomes blocked multiple times, and
	 * therefore must also be unblocked multiple times before it becomes active again.
	 *
	 * @param provider the tool provider to be blocked
	 * @param scope    the scope where to block the tool provider
	 * @throws IllegalArgumentException if there is no such tool provider plugged to the system
	 */
	public static void blockToolProvider(ToolProvider provider, Scope scope) {
		BLOCKED_PROVIDERS.computeIfAbsent(provider, p -> new CountingSet<>()).add(scope);
	}

	/**
	 * Unblocks the specified tool provider for all sections of the specified scope. See {@link
	 * #findToolProviders(String)} for more information on the matched tools.
	 * <p>
	 * If {@link #blockToolProvider(String, Scope)} has been called multiple times, this method must be called the same
	 * number of times to really unblock the tool again. If a tool is unblocked more often than blocked before, an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param extensionIdOrClassName the extension id to identify the class, or the class name of the tool provider
	 * @param scope                  the scope where to unblock the tool provider
	 * @throws IllegalArgumentException if there is no such tool provider plugged to the system
	 * @throws IllegalStateException    if the unblocking is not balanced to the blockings before
	 */
	public static void unblockToolProvider(String extensionIdOrClassName, Scope scope) {
		for (ToolProvider provider : findToolProviders(extensionIdOrClassName)) {
			unblockToolProvider(provider, scope);
		}
	}

	/**
	 * Unblocks the specified tool provider for all sections of the specified scope.
	 * <p>
	 * If {@link #blockToolProvider(ToolProvider, Scope)} has been called multiple times, this method must be called the
	 * same number of times to really unblock the tool again. If a tool is unblocked more often than blocked before, an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param provider the tool provider to be unblocked
	 * @param scope    the scope where to unblock the tool provider
	 * @throws IllegalArgumentException if there is no such tool provider plugged to the system
	 * @throws IllegalStateException    if the unblocking is not balanced to the blockings before
	 */
	public static void unblockToolProvider(ToolProvider provider, Scope scope) {
		CountingSet<Scope> blockedScopes = BLOCKED_PROVIDERS.get(provider);
		if (blockedScopes == null || !blockedScopes.contains(scope)) {
			throw new IllegalStateException("unbalanced tool blocking: " +
											provider.getClass().getSimpleName() + "@" + scope);
		}
		blockedScopes.remove(scope);
		if (blockedScopes.isEmpty()) BLOCKED_PROVIDERS.remove(provider);
	}

	/**
	 * Returns true if the specified provider is blocked for the specified section.
	 */
	private static boolean isBlocked(ToolProvider provider, Section<?> section) {
		Set<Scope> blockedScopes = BLOCKED_PROVIDERS.get(provider);
		if (blockedScopes == null) return false;

		for (Scope scope : blockedScopes) {
			if (scope.matches(section)) return true;
		}
		return false;
	}

	/**
	 * Finds the specified tool provider(s), identified by either the plugin's extension id, or the constructor call
	 * defined in the extension definition, or the tool-providers class name. If a tool-providers superclass is
	 * specified, only the tool providers of exactly that superclass are returned, but not any subclasses' tool
	 * providers. If an extension is specified (either by the extension-id or the plugin-id+"."+extension-id, only the
	 * particular tool provider instance is contained in the returned list, other extensions using the same provider
	 * class are still active.
	 *
	 * @param extensionIdOrClassName the extension id to identify the class, or the class name of the tool provider
	 * @throws IllegalArgumentException if there is no such tool provider plugged to the system
	 */
	@NotNull
	private static List<ToolProvider> findToolProviders(String extensionIdOrClassName) {
		List<ToolProvider> result = new ArrayList<>();
		for (Extension extension : extensions.getAll()) {
			Object provider = extension.getSingleton();
			if (extensionIdOrClassName.equals(extension.getParameter("class"))
				|| extensionIdOrClassName.equals(provider.getClass().getName())
				|| extensionIdOrClassName.equals(provider.getClass().getSimpleName())
				|| extensionIdOrClassName.equals(extension.getID())
				|| extensionIdOrClassName.equals(extension.getPluginID() + "." + extension.getID())) {
				result.add((ToolProvider) provider);
			}
		}
		if (result.isEmpty()) {
			throw new IllegalArgumentException("No such tool provider: " + extensionIdOrClassName);
		}
		return result;
	}

	public static void initSettings(File jsonFile) {
		if (!jsonFile.exists()) return;
		try {
			initSettings(new JSONObject(Files.getText(jsonFile)));
		}
		catch (IOException e) {
			LOGGER.error("Cannot load (existing) properties file with blocked tool providers", e);
		}
	}

	/**
	 * Initializes the blocked tool menus from the specified settings object
	 *
	 * @param settings the settings
	 */
	public static void initSettings(JSONObject settings) {
		//noinspection unchecked
		for (String key : (Set<String>) settings.keySet()) {
			//noinspection SwitchStatementWithTooFewBranches
			switch (key) {
				case "blocked":
					for (JSONObject blocked : getList(settings, "blocked", JSONObject.class)) {
						List<String> providers = getList(blocked, "provider", String.class);
						List<String> scopes = getList(blocked, "scope", String.class);
						if (providers.isEmpty()) throw new IllegalArgumentException("no provider defined");
						if (scopes.isEmpty()) throw new IllegalArgumentException("no scope defined");
						for (String provider : providers) {
							for (String scope : scopes) {
								blockToolProvider(provider, Scope.getScope(scope));
							}
						}
					}
					break;

				default:
					throw new IllegalArgumentException("invalid entry in settings: " + key);
			}
		}
	}

	private static <T> List<T> getList(JSONObject json, String key, Class<T> valueClass) {
		Object value = json.opt(key);
		if (value == null) return Collections.emptyList();
		if (value instanceof JSONArray) {
			List<T> result = new ArrayList<>();
			JSONArray array = (JSONArray) value;
			for (int i = 0; i < array.length(); i++) {
				result.add(valueClass.cast(array.get(i)));
			}
			return result;
		}
		return Collections.singletonList(valueClass.cast(value));
	}
}
