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

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.PackageUnregistrationCompiler;
import de.knowwe.core.compile.packaging.PackageRegistrationNotCompiledWarningScript;
import de.knowwe.core.compile.packaging.PackageUnregistrationNotCompiledWarningScript;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;

/**
 * Handles unregistration of packages both from the terminology and package manager.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class DefaultMarkupPackageUnregistrationScript extends DefaultMarkupPackageScript implements PackageUnregistrationCompiler.PackageUnregistrationScript<DefaultMarkupType> {

	@Override
	public void destroy(PackageUnregistrationCompiler compiler, Section<DefaultMarkupType> section) {
		compiler.getPackageManager().removeSectionFromAllPackagesAndRules(section);

		DefaultMarkupPackageScript.PackageInfo packageInfo = getStoredPackageInfo(section);
		if (packageInfo == null) return;
		boolean isCompileMarkup = isCompileMarkup(section);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		for (String packageName : getAllPackageNames(packageInfo)) {

			Identifier packageIdentifier = new Identifier(packageName);
			if (isCompileMarkup) {
				terminologyManager.unregisterTermDefinition(compiler, section, Package.class, packageIdentifier);
				Compilers.destroyAndRecompileReferences(compiler, packageIdentifier, PackageUnregistrationNotCompiledWarningScript.class);
			}
			else {
				terminologyManager.unregisterTermReference(compiler, section, Package.class, packageIdentifier);
			}
		}
	}

	@Override
	public Class<PackageUnregistrationCompiler> getCompilerClass() {
		return PackageUnregistrationCompiler.class;
	}
}
