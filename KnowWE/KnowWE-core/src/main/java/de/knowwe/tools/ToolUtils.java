package de.knowwe.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.plugin.Extension;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.ScopeExtensions;

public class ToolUtils {

	/**
	 * Special string for the empty category. The spaces in front make it be the first item in a
	 * sorted list (assuming the others use regular letters).
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
		for (Extension match : extensions.getMatches(section)) {
			ToolProvider provider = (ToolProvider) match.getSingleton();
			Tool[] tools = provider.getTools(section, userContext);
			if (tools != null) {
				Collections.addAll(result, tools);
			}
		}
		return result;
	}

	static boolean hasToolInstances(Section<?> section, UserContext userContext) {
		for (Extension match : extensions.getMatches(section)) {
			ToolProvider provider = (ToolProvider) match.getSingleton();
			if (provider.hasTools(section, userContext)) return true;
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

	/**
	 * The method returns the array of tools, being null-secure and removing all null entries from
	 * the specified tools. Therefore, calling this method with any numbers of null will result in
	 * an empty tool array.
	 *
	 * @return the tool array with no null tools
	 * @created 30.11.2013
	 */
	public static Tool[] asArray(Tool... tools) {
		if (tools == null) return emptyToolArray();

		// count tools
		int count = 0;
		for (Tool tool : tools) {
			if (tool != null) count++;
		}

		// check two most common cases
		if (count == 0) return emptyToolArray();
		if (count == tools.length) return tools;

		// otherwise remove null from the tool array
		Tool[] result = new Tool[count];
		int index = 0;
		for (Tool tool : tools) {
			if (tool != null) result[index++] = tool;
		}
		return result;
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
			default:
				return null;
		}
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
			default:
				return null;
		}
	}
}
