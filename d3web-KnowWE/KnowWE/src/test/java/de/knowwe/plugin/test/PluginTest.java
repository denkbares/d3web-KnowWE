package de.knowwe.plugin.test;

import java.io.IOException;

import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.plugin.Plugins;
import junit.framework.TestCase;

public class PluginTest extends TestCase{
	
	public void testPlugins() throws IOException {
		InitPluginManager.init();
		Plugins.getGlobalTypes();
		Plugins.getInstantiations();
		Plugins.getKnowledgeRepresentationHandlers();
		Plugins.getKnowWEAction();
		Plugins.getPageAppendHandlers();
		Plugins.getRootTypes();
		Plugins.getTagHandlers();
	}

}
