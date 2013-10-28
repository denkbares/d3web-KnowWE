/*
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.plugin.test;

import java.io.IOException;

import junit.framework.TestCase;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.plugin.Plugins;

/**
 * Simple JUnit test for plugins
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public class PluginTest extends TestCase {

	/**
	 * Tries to load all extensions. Doesn't check anything, if a class is
	 * missing, a ClassNotFoundException will be thrown
	 * 
	 * @throws IOException
	 */
	public void testPlugins() throws IOException {
		InitPluginManager.init();
		Plugins.getInstantiations();
		Plugins.getKnowledgeRepresentationHandlers();
		Plugins.getKnowWEAction();
		Plugins.getPageAppendHandlers();
		RootType root = RootType.getInstance();
		Type[] path = new Type[] { root };
		Plugins.addChildrenTypesToType(root, path);
		Plugins.addSubtreeHandlersToType(root, path);
		Plugins.addRendererToType(root, path);
		Plugins.getTagHandlers();
	}

}
