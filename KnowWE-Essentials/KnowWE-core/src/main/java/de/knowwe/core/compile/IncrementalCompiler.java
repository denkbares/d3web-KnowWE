/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface for incremental {@link Compiler}s allowing to add depending
 * {@link Section}s for recompilation (destroy and compile) during an ongoing
 * compilation;
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.01.2014
 */
public interface IncrementalCompiler extends Compiler {

	/**
	 * Adds the given {@link Section} to also be destroyed in the current
	 * compilation. The implementing {@link Compiler} must allow to call this
	 * method during an ongoing compilation.<p>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the section and the compiler are added.
	 *
	 * @param section     the section to additionally destroy
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 04.01.2014
	 */
	void addSectionToDestroy(Section<?> section, Class<?>... scriptFilter);

	/**
	 * Adds the given {@link Section} to also be compiled in the current
	 * compilation. The implementing {@link Compiler} must allow to call this
	 * method during an ongoing compilation.<p>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the section and the compiler are added.
	 *
	 * @param section     the section to additionally compile
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 04.01.2014
	 */
	void addSectionToCompile(Section<?> section, Class<?>... scriptFilter);

	/**
	 * Adds the given subtree of {@link Section}s to also be destroyed in the current
	 * compilation. The implementing {@link Compiler} must allow to call this
	 * method during an ongoing compilation.<p>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the section and the compiler are added.
	 *
	 * @param section      the section to additionally destroy
	 * @param scriptFilter the classes of the scripts you want to destroy
	 * @created 04.01.2014
	 */
	void addSubtreeToDestroy(Section<?> section, Class<?>... scriptFilter);

	/**
	 * Adds the given subtree of {@link Section}s to also be compiled in the current
	 * compilation. The implementing {@link Compiler} must allow to call this
	 * method during an ongoing compilation.<p>
	 * Optionally you can add a filter to only add scripts of the given classes. If no filter is given, all scripts
	 * available for the type of the section and the compiler are added.
	 *
	 * @param section      the section to additionally compile
	 * @param scriptFilter the classes of the scripts you want to add
	 * @created 04.01.2014
	 */
	void addSubtreeToCompile(Section<?> section, Class<?>... scriptFilter);

}
