/*
 * Copyright (C) 2013 denkbares GmbH
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

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Common interface for compilers compiling a set of packages.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.12.2013
 */
public interface PackageCompiler extends Compiler, NamedCompiler {

	/**
	 * The section the compiler is registered and added to the compilation manager at.
	 */
	@NotNull
	Section<? extends PackageCompileType> getCompileSection();

	/**
	 * Get the associated package manager of this compiler.
	 */
	@NotNull
	PackageManager getPackageManager();

	/**
	 * Get all sections that are currently compiled by this compiler, based on used packages and available sections in
	 * those packages.
	 * @deprecated use {@link Sections#$(PackageCompiler)} instead
	 */
	default Collection<Section<?>> getCompiledSections() {
		return getPackageManager().getSectionsOfPackage(getCompiledPackages());
	}

	/**
	 * The packages compiled by this package compiler
	 */
	String[] getCompiledPackages();
}
