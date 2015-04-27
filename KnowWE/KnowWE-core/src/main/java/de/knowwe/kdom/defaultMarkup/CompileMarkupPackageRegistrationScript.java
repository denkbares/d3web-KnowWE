/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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

import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class CompileMarkupPackageRegistrationScript extends PackageRegistrationScript<DefaultMarkupType> {

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		Section<DefaultMarkupPackageCompileType> markupCompileSection = Sections.successor(section, DefaultMarkupPackageCompileType.class);
		String[] packagesToCompile = markupCompileSection.get().getPackagesToCompile(markupCompileSection);
		for (String packageName : packagesToCompile) {
			compiler.getPackageManager().addSectionToPackage(section, packageName);
		}
	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		compiler.getPackageManager().removeSectionFromAllPackages(section);
	}

}
