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
package de.knowwe.kdom.defaultMarkup;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Registers a {@link DefaultMarkupType} section compiling with
 * {@link PackageCompileType} section as a reference to the packages it belongs
 * to.
 * 
 * @author Stefan Plehn, Albrecht Striffler (denkbares GmbH)
 * @created 12.07.2013
 */
public class DefaultMarkupPackageReferenceRegistrationHandler extends PackageRegistrationScript<DefaultMarkupType> {

	private static final String PACKAGE_REFERENCES_KEY = "packageReferences";

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {

		String[] packageNames = DefaultMarkupType.getPackages(section);
		// while destroying, the default packages will already be removed, so we
		// have to store artificially
		section.storeObject(compiler, PACKAGE_REFERENCES_KEY, packageNames);
		for (String packageName : packageNames) {
			compiler.getTerminologyManager().registerTermReference(compiler,
					section, Package.class, new Identifier(packageName));
		}

	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		String[] packageNames = (String[]) section.getObject(compiler,
				PACKAGE_REFERENCES_KEY);
		for (String annotationString : packageNames) {
			compiler.getTerminologyManager().unregisterTermReference(compiler, section,
					Package.class, new Identifier(annotationString));
		}

	}

}
