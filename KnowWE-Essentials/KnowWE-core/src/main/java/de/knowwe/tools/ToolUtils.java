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
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Scope;
import de.knowwe.core.utils.ScopeExtensions;

public class ToolUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ToolUtils.class);

	/**
	 * For each tool provider there might be an (counting) set of scopes, where this provider is blocked.
	 */
	/**
	 * The tool providers blocked for certain scopes, one registry per environment.
	 */
	public static final class BlockedToolProviders {
		private final Map<ToolProvider, CountingSet<Scope>> byProvider = new HashMap<>();
	}

	private static Map<ToolProvider, CountingSet<Scope>> blocked(@NotNull Environment environment) {
		return environment.getService(BlockedToolProviders.class, BlockedToolProviders::new).byProvider;
	}
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

			if (category == null || category.isEmpty()) {
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
		return switch (tool.getActionType()) {
			case HREF -> "href";
			case HREF_SCRIPT -> "href";
			case ONCLICK -> "onclick";
		};
	}

	public static String getActionAttributeValue(Tool tool) {
		if (Strings.isBlank(tool.getAction())) return null;
		return switch (tool.getActionType()) {
			case HREF -> tool.getAction();
			case HREF_SCRIPT -> "javascript:" + tool.getAction() + ";_TM.hideToolsPopupMenu()";
			case ONCLICK -> tool.getAction() + ";_TM.hideToolsPopupMenu()";
		};
	}

	/**
	 * Blocks the specified tool provider(s) in the specified environment, see {@link #blockToolProvider(String, Scope)}.
	 */
	public static void blockToolProvider(@NotNull Environment environment, String extensionIdOrClassName, Scope scope) {
		final List<ToolProvider> toolProviders;
		try {
			toolProviders = findToolProviders(extensionIdOrClassName);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to find blocked tool provider", e);
		}
		for (ToolProvider provider : toolProviders) {
			blockToolProvider(environment, provider, scope);
		}
	}

	/**
	 * Blocks the specified tool provider in the specified environment, see {@link #blockToolProvider(ToolProvider, Scope)}.
	 */
	public static void blockToolProvider(@NotNull Environment environment, ToolProvider provider, Scope scope) {
		Map<ToolProvider, CountingSet<Scope>> blocked = blocked(environment);
		synchronized (blocked) {
			blocked.computeIfAbsent(provider, p -> new CountingSet<>()).add(scope);
		}
	}

	/**
	 * Unblocks the specified tool provider(s) in the specified environment, see {@link #unblockToolProvider(String, Scope)}.
	 */
	public static void unblockToolProvider(@NotNull Environment environment, String extensionIdOrClassName, Scope scope) {
		for (ToolProvider provider : findToolProviders(extensionIdOrClassName)) {
			unblockToolProvider(environment, provider, scope);
		}
	}

	/**
	 * Unblocks the specified tool provider in the specified environment, see {@link #unblockToolProvider(ToolProvider, Scope)}.
	 */
	public static void unblockToolProvider(@NotNull Environment environment, ToolProvider provider, Scope scope) {
		Map<ToolProvider, CountingSet<Scope>> blocked = blocked(environment);
		synchronized (blocked) {
			CountingSet<Scope> blockedScopes = blocked.get(provider);
			if (blockedScopes == null || !blockedScopes.contains(scope)) {
				throw new IllegalStateException("unbalanced tool blocking: " +
												provider.getClass().getSimpleName() + "@" + scope);
			}
			blockedScopes.remove(scope);
			if (blockedScopes.isEmpty()) blocked.remove(provider);
		}
	}

	/**
	 * Returns true if the specified provider is blocked for the specified section.
	 */
	private static boolean isBlocked(ToolProvider provider, Section<?> section) {
		Environment environment = KnowWEUtils.findEnvironment(section);
		if (environment == null) return false;
		Set<Scope> blockedScopes;
		Map<ToolProvider, CountingSet<Scope>> blocked = blocked(environment);
		synchronized (blocked) {
			CountingSet<Scope> scopes = blocked.get(provider);
			blockedScopes = scopes == null ? null : new HashSet<>(scopes);
		}
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

	/**
	 * Loads the blocked tool providers from the specified settings file into the specified environment.
	 */
	public static void initSettings(@NotNull Environment environment, File jsonFile) {
		if (!jsonFile.exists()) return;
		try {
			initSettings(environment, new JSONObject(Files.getText(jsonFile)));
		}
		catch (IOException e) {
			LOGGER.error("Cannot load (existing) properties file with blocked tool providers", e);
		}
	}

	/**
	 * Initializes the blocked tool menus of the specified environment from the specified settings object.
	 */
	public static void initSettings(@NotNull Environment environment, JSONObject settings) {
		for (String key : settings.keySet()) {
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
								blockToolProvider(environment, provider, Scope.getScope(scope));
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
		if (value instanceof JSONArray array) {
			List<T> result = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				result.add(valueClass.cast(array.get(i)));
			}
			return result;
		}
		return Collections.singletonList(valueClass.cast(value));
	}
}
