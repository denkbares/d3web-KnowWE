/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
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
package de.d3web.we.ci4ke.testing;

import java.util.Map;

import de.d3web.testing.Test;

/**
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 22.11.2010
 */
public interface DynamicCITestHandler {

	/**
	 * Gets a dynamically implemented CITest Class via its name.
	 * 
	 * @created 22.11.2010
	 * @param testName the name of the test, as it gets called in CIDashboards
	 * @return
	 */
	public Class<? extends Test<?>> getCITestClass(String testName);

	/**
	 * Gets a Map of all dynamically implemented CITest Classes
	 * 
	 * @created 26.11.2010
	 * @return
	 */
	public Map<String, Class<? extends Test<?>>> getAllCITestClasses();

}
