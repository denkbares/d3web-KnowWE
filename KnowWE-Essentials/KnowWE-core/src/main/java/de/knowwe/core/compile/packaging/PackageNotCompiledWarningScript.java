package de.knowwe.core.compile.packaging;

import java.util.Set;

import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

/**
 * Attaches a warning if the given package is never compiled.
 * <p/>
 * @author Albrecht Striffler (denkbares GmbH) on 26.03.2014.
 */
public class PackageNotCompiledWarningScript extends PackageRegistrationCompiler.PackageRegistrationScript<PackageTerm> {

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<PackageTerm> section) throws CompilerMessage {
		String packageName = section.get().getTermName(section);
		Set<Section<? extends PackageCompileType>> compileSections = compiler.getPackageManager()
				.getCompileSections(packageName);

		if (compileSections.isEmpty()) {
			Messages.storeMessage(compiler, section, this.getClass(),
					Messages.warning("The package '" + packageName + "' is never used to compile knowledge."));
		}
		else {
			Messages.clearMessages(compiler, section, this.getClass());
		}
	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<PackageTerm> section) {
		// nothing to do
	}
}
