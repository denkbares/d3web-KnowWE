/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.plugin;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;

public interface Instantiation {

	/**
	 * Initializes this plugin part for the specified environment, right after the environment's type tree and
	 * compilers are set up. Everything the plugin creates here (services, threads, listeners) should be attached to the
	 * environment, e.g. via {@link Environment#getService(Class, java.util.function.Supplier)} and
	 * {@link Environment#onClose(Runnable)}, so that it is released when the environment is closed.
	 *
	 * @param environment the environment being initialized
	 */
	default void init(@NotNull Environment environment) {
		init(Environment.DEFAULT_WEB);
	}

	/**
	 * @deprecated implement {@link #init(Environment)} instead; the web is always {@link Environment#DEFAULT_WEB}
	 */
	@Deprecated
	default void init(String web) {
		throw new UnsupportedOperationException(getClass().getName() + " implements neither init(Environment) nor init(String)");
	}
}
