package de.knowwe.core.compile.packaging;

import java.util.Set;

import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageUnregistrationCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Attaches a warning if the given package is never compiled.
 * <p/>
 *
 * @author Albrecht Striffler (denkbares GmbH) on 26.03.2014.
 */
public class PackageUnregistrationNotCompiledWarningScript implements PackageUnregistrationCompiler.PackageUnregistrationScript<PackageTerm> {

	@Override
	public void compile(PackageUnregistrationCompiler compiler, Section<PackageTerm> section) throws CompilerMessage {
		String packageName = section.get().getTermName(section);
		Set<Section<? extends PackageCompileType>> compileSections = compiler.getPackageManager()
				.getCompileSections(packageName);

		if (compileSections.isEmpty()) {
			Messages.storeMessage(compiler.getRegistrationCompiler(), section, PackageRegistrationCompiler.class,
					Messages.warning("The package '" + packageName + "' is never used to compile knowledge."));
		}
		else {
			Messages.clearMessages(compiler.getRegistrationCompiler(), section, PackageRegistrationCompiler.class);
		}
	}

	@Override
	public void destroy(PackageUnregistrationCompiler compiler, Section<PackageTerm> section) {
		// nothing to do
	}
}
