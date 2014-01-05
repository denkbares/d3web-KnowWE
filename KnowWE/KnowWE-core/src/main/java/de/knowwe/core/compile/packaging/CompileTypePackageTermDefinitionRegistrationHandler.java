/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.packaging;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Registers a {@link DefaultMarkupType} section compiling with
 * {@link PackageCompileType} section as the definition of the packages it
 * compiles.
 * 
 * @author Stefan Plehn, Albrecht Striffler (denkbares GmbH)
 * @created 12.07.2013
 */
public class CompileTypePackageTermDefinitionRegistrationHandler extends PackageRegistrationScript<DefaultMarkupType> {

	private static final String PACKAGE_DEFINITIONS_KEY = "packageDefinitions";

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {

		String[] packageNames = DefaultMarkupType.getPackages(section,
				PackageManager.COMPILE_ATTRIBUTE_NAME);
		// while destroying, the default packages will already be removed, so we
		// have to store artificially
		section.getSectionStore().storeObject(compiler, PACKAGE_DEFINITIONS_KEY, packageNames);
		for (String annotationString : packageNames) {
			compiler.getTerminologyManager().registerTermDefinition(compiler,
					section, Package.class, new Identifier(annotationString));
		}

	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		String[] packageNames = (String[]) section.getSectionStore().getObject(compiler,
				PACKAGE_DEFINITIONS_KEY);
		for (String annotationString : packageNames) {
			compiler.getTerminologyManager().unregisterTermDefinition(compiler, section,
					Package.class, new Identifier(annotationString));
		}

	}

}
