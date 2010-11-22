/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.handling;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages all DynamicCITestHandlers in the system.
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 22.11.2010
 */
public final class DynamicCITestManager {

	private final Set<DynamicCITestHandler> handlers;
	
	public static final DynamicCITestManager INSTANCE = new DynamicCITestManager();
	
	private DynamicCITestManager() {
		handlers = new HashSet<DynamicCITestHandler>();
	}

	public void registerDynamicCITestHandler(DynamicCITestHandler handler) {
		handlers.add(handler);
	}

	public Class<? extends CITest> getCITestClass(String testName) {
		for (DynamicCITestHandler handler : handlers) {
			Class<? extends CITest> clazz = handler.getCITestClass(testName);
			if (clazz != null) {
				return clazz;
			}
		}
		return null;
	}
}
