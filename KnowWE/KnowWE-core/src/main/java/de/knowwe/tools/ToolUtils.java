package de.knowwe.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.ScopeUtils;

public class ToolUtils {

	/**
	 * Special string for the empty category. The spaces in front make it be the
	 * first item in a sorted list (assuming the others use regular letters).
	 */
	public final static String EMPTY_CATEGORY = "  empty_category";

	public static ToolProvider[] getProviders(Section<?> section) {
		Extension[] extensions = PluginManager.getInstance().getExtensions("KnowWEExtensionPoints",
				"ToolProvider");
		extensions = ScopeUtils.getMatchingExtensions(extensions, section);
		ToolProvider[] providers = new ToolProvider[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = extensions[i];
			providers[i] = (ToolProvider) extension.getSingleton();
		}
		return providers;
	}

	public static Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Tool> tools = new LinkedList<Tool>();
		for (ToolProvider provider : getProviders(section)) {
			Collections.addAll(tools, provider.getTools(section, userContext));
		}
		return tools.toArray(new Tool[tools.size()]);
	}

	/**
	 * This method sorts the tool list by the category string.
	 * 
	 * The category string can contain at most one forward slash (/)
	 * 
	 * @created 03.03.2011
	 * @param tools
	 * @return
	 */
	public static Map<String, Map<String, List<Tool>>> groupTools(Tool[] tools) {
		Map<String, Map<String, List<Tool>>> toolMap = new HashMap<String, Map<String, List<Tool>>>();

		for (Tool t : tools) {
			String category = t.getCategory();

			if (category == null || category.equals("")) {
				if (!toolMap.containsKey(EMPTY_CATEGORY)) {
					toolMap.put(EMPTY_CATEGORY, new HashMap<String, List<Tool>>());
					toolMap.get(EMPTY_CATEGORY).put(EMPTY_CATEGORY, new LinkedList<Tool>());
				}

				toolMap.get(EMPTY_CATEGORY).get(EMPTY_CATEGORY).add(t);
			}
			else {
				String[] parts = category.split("/", 2);

				if (!toolMap.containsKey(parts[0])) {
					toolMap.put(parts[0], new HashMap<String, List<Tool>>());
				}

				// items of the type "category"
				if (parts.length < 2) {
					if (!toolMap.get(parts[0]).containsKey(EMPTY_CATEGORY)) {
						toolMap.get(parts[0]).put(EMPTY_CATEGORY, new LinkedList<Tool>());
					}

					toolMap.get(parts[0]).get(EMPTY_CATEGORY).add(t);
					// items of the type "category/subitem"
				}
				else {
					if (!toolMap.get(parts[0]).containsKey(parts[1])) {
						toolMap.get(parts[0]).put(parts[1], new LinkedList<Tool>());
					}

					toolMap.get(parts[0]).get(parts[1]).add(t);
				}
			}
		}

		return toolMap;
	}
}
