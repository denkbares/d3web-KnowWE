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

import org.junit.Assert;
import junit.framework.TestCase;
import connector.DummyConnector;

import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.core.Environment;
import de.knowwe.plugin.Plugins;

/**
 * Simple JUnit test for plugins
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public class PluginTest extends TestCase {

	/**
	 * Tries to load all extensions. Doesn't check anything, if a class is
	 * missing, an Exception will be thrown
	 * 
	 */
	public void testPlugins() {
		try {
			InitPluginManager.init();
			Environment.initInstance(new DummyConnector());
			Plugins.getKnowWEAction();
			Plugins.getPageAppendHandlers();
			// the other methods in Plugin are already called in
			// Environment.initInstance()
			// check again if you added new methods to Plugin
		}
		catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
