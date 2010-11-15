package de.d3web.we.tools;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.ScopeUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;


public class ToolUtils {

	public static ToolProvider[] getProviders(Section<?> section) {
		Extension[] extensions = PluginManager.getInstance().getExtensions("KnowWEExtensionPoints", "ToolProvider");
		extensions = ScopeUtils.getMatchingExtensions(extensions, section);
		ToolProvider[] providers = new ToolProvider[extensions.length];
		for (int i=0; i<extensions.length; i++) {
			Extension extension = extensions[i];
			providers[i] = (ToolProvider) extension.getSingleton();
		}
		return providers;
	}
	
	public static Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		List<Tool> tools = new LinkedList<Tool>();
		for (ToolProvider provider : getProviders(section)) {
			Collections.addAll(tools, provider.getTools(article, section, userContext));
		}
		return tools.toArray(new Tool[tools.size()]);
	}
}
